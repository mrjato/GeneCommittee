/*
	This file is part of GeneCommittee.

	GeneCommittee is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	GeneCommittee is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with GeneCommittee.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.gc.view.models.committee.geneset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.datatypes.featureselection.FeatureSelector;
import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.GeneSelectionMetaData;
import es.uvigo.ei.sing.gc.model.entities.GeneSelectionType;
import es.uvigo.ei.sing.gc.model.entities.RankedGene;
import es.uvigo.ei.sing.gc.model.entities.RankedGene.GeneSorter;
import es.uvigo.ei.sing.gc.model.entities.RankedGene.PositionSorter;
import es.uvigo.ei.sing.gc.model.entities.RankedGene.RankingSorter;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class GeneSetViewModel extends CommitteeViewModel {

	private static final String GC_UPDATE_GENE_SELECTION = "updateGeneSelection";
	
	static {
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_GENE_SELECTION_FINISHED, 
			GC_UPDATE_GENE_SELECTION
		);
	}

	private String geneURL;
	private boolean geneURLVisible = false;

	@Init
	@Override
	public void init() {
		super.init();
		
		this.geneURL = "/loading.zul";
		this.geneURLVisible = false;
		
		final Committee committee = this.getCommittee(false); // Refreshed in init
		if (!committee.hasGeneSelection()) {
			final Session session = HibernateUtil.currentSession();
			final GeneSelectionMetaData geneSelection = GeneSelectionType.getDefault().buildGeneSelection();
			committee.setGeneSelection(geneSelection);
			
			session.persist(geneSelection);
			session.flush();
			session.update(geneSelection);
		}
		
		StatusViewModel.changeInitialStatus("Select the best genes using a feature selection algorithm");
	}

	public List<GeneSelectionType> getGeneSelectionTypes() {
		return Arrays.asList(GeneSelectionType.values());
	}
	
	public boolean getHasRankedGenes() {
		return this.getCommittee().hasRankedGenes();
	}
	
	public GeneSelectionType getGeneSelectionType() {
		try {
			final String builderClassName = this.getCommittee().getGeneSelection().getBuilderClassName();
			return GeneSelectionType.getTypeForBuilder(builderClassName);
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public GeneSelectionMetaData getGeneSelection() {
		final GeneSelectionMetaData geneSelection = this.getCommittee().getGeneSelection();
		
		return geneSelection.clone();
	}
	
	public List<RankedGene> getRankedGenes() {
		final List<RankedGene> genes = new ArrayList<RankedGene>();
		final Committee committee = this.getCommittee();
		
		if (committee.hasRankedGenes()) {
			genes.addAll(committee.getRankedGenes());
			Collections.sort(genes, this.getAscPositionSorter());
		}
		
		return genes;
	}
	
	public int getNumGenes() {
		return this.getCommittee().getGeneSelection().getNumGenes();
	}
	
	public void setNumGenes(final int numGenes) {
		final Committee committee = this.getCommittee();
		
		if (committee.getGeneSelection().getNumGenes() != numGenes) {
			this.dataLostNotification(
				"Gene set selection",
				"Gene set selection modification will provoke data deletion (gene set, classifiers, etc.). Do you wish to continue?",
				CommitteeStatus.RANKED_GENES,
				CommitteeStatus.GENE_SELECTION,
				new Runnable() {
					@Override
					public void run() {
						final Session session = HibernateUtil.currentSession();
						final GeneSelectionMetaData geneSelection = GeneSetViewModel.this.getCommittee().getGeneSelection();
						geneSelection.setNumGenes(numGenes);
						
						session.update(geneSelection);
						
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "rankedGenes");
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelection");
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelectionType");
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "numGenes");
						GeneSetViewModel.this.updateExecutions();
					}
				},
				new Runnable() {
					public void run() {
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "numGenes");
					};
				}
			);
		}
	}
	
	public String getGenesConstraint() {
		return "min 1, max " + Configuration.getInstance().getGeneBrowserMaxInput();
	}
	
	@Command
	public void notifyConfigurationChanged(
		@BindingParam("geneSelection") GeneSelectionMetaData geneSelection
	) {
		this.setGeneSelection(geneSelection);
	}

	public void setGeneSelection(final GeneSelectionMetaData geneSelection) {
		final Committee committee = this.getCommittee();
		final GeneSelectionMetaData currentGS = committee.getGeneSelection();
		
		if (!geneSelection.getBuilder().equals(currentGS.getBuilder()) || 
			geneSelection.getNumGenes() != currentGS.getNumGenes() ||
			!geneSelection.getValues().equals(currentGS.getValues())
		) {
			this.dataLostNotification(
				"Gene set selection",
				"Gene set selection saving will provoke data deletion (gene set, classifiers, etc.). Do you wish to continue?",
				CommitteeStatus.RANKED_GENES,
				CommitteeStatus.GENE_SELECTION,
				new Runnable() {
					@Override
					public void run() {
						final Session session = HibernateUtil.currentSession();
						
						final Committee committee = GeneSetViewModel.this.getCommittee();
						final GeneSelectionMetaData currentGS = committee.getGeneSelection();
						currentGS.copyValuesOf(geneSelection);
						
						session.update(currentGS);
						session.flush();
						
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "rankedGenes");
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelection");
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelectionTypes"); // Should be geneSelectionType, but it does not work well
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "numGenes");
					}
				},
				new Runnable() {
					public void run() {
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelectionTypes"); // Should be geneSelectionType, but it does not work well
						BindUtils.postNotifyChange(null, null, GeneSetViewModel.this, "geneSelection");
					};
				}
			);
		}
	}
	
	public void setGeneSelectionType(GeneSelectionType geneSelectionType) {
		if (!geneSelectionType.equals(this.getGeneSelectionType())) {
			final GeneSelectionMetaData geneSelection = geneSelectionType.buildGeneSelection();
			geneSelection.setNumGenes(this.getNumGenes());
			this.setGeneSelection(geneSelection);
		}
	}
	
	@GlobalCommand(GC_UPDATE_GENE_SELECTION)
	@NotifyChange({ "rankedGenes", "geneSetCompleted" })
	public void executionFinished() {}
	
	@Command
	public void updateRankedGenes() throws Exception {
		final Integer committeeId = this.getCommitteeId();
		final String userId = this.getUserId();

		this.dataLostNotification(
			"Gene set selection",
			"Gene set selection execution will provoke data deletion (gene set, classifiers, etc.). Do you wish to continue?",
			CommitteeStatus.GENE_ENRICHMENT,
			CommitteeStatus.RANKED_GENES,
			new Runnable() {
				@Override
				public void run() {
					final Committee committee = GeneSetViewModel.this.getCommittee();
					
					FeatureSelector fs;
					try {
						fs = committee.getGeneSelection().createFeatureSelection();
						fs.setData(committee.getDataSet().loadData());
						
						ExecutionEngine.getSingleton().execute(
							new SelectGenesTask(userId, committeeId, fs)
						);
					} catch (Exception e) {
						Messagebox.show(
							"Error while performing gene selection: " + e.getMessage(),
							"Error",
							Messagebox.OK,
							Messagebox.ERROR
						);
					}
				}
			}
		);
	}
	

	public PositionSorter getAscPositionSorter() {
		return new PositionSorter(true);
	}
	
	public PositionSorter getDescPositionSorter() {
		return new PositionSorter(false);
	}
	
	public GeneSorter getAscGeneSorter() {
		return new GeneSorter(true);
	}
	
	public GeneSorter getDescGeneSorter() {
		return new GeneSorter(false);
	}
	
	public RankingSorter getAscRankingSorter() {
		return new RankingSorter(true);
	}
	
	public RankingSorter getDescRankingSorter() {
		return new RankingSorter(false);
	}
	
	@Override
	public String getCurrentStep() {
		return CommitteeViewModel.GENE_SET_STEP;
	}
	
	@Override
	public String getNextHref() {
		return CommitteeViewModel.ENRICHMENT_PATH;
	}
	
	@Override
	public String getPreviousHref() {
		return CommitteeViewModel.DATA_SET_PATH;
	}
	
	public String getGeneURL() {
		return this.geneURL;
	}
	
	public boolean isGeneURLVisible() {
		return this.geneURLVisible;
	}
	
	@Command("closeGeneBrowserWindow")
	@NotifyChange({"geneURL", "geneURLVisible"})
	public void closeGeneBrowserWindow(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		final Event event = ctx.getTriggerEvent();
		
		this.geneURLVisible = false;
		this.geneURL = "/loading.zul";
		event.stopPropagation();
	}
	
	@Command("showGene")
	@NotifyChange({"geneURL", "geneURLVisible"})
	public void showGene(@BindingParam("gene") String gene) {
		final String geneBrowserURL = Configuration.getInstance().getGeneBrowserURL();
		final String geneMarker = Configuration.getInstance().getGeneBrowserGenesMarker();
		
		this.geneURL = geneBrowserURL.replace(geneMarker, gene);
		this.geneURLVisible = true;
		System.out.println(this.geneURL);
	}
	
	@Override
	@DependsOn("geneSetCompleted")
	public boolean getHasNext() {
		return this.isGeneSetCompleted();
	}

	@Override
	protected CommitteeStatus stepStatus() {
		return CommitteeStatus.GENE_SELECTION;
	}
}
