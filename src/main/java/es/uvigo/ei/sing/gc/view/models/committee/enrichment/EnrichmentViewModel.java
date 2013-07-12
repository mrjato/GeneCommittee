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
package es.uvigo.ei.sing.gc.view.models.committee.enrichment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;

import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.UserGlobalEventListener;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData.NameSorter;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData.NumGenesSorter;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData.PValueSorter;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData.SourceSorter;
import es.uvigo.ei.sing.gc.model.entities.RankedGene;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class EnrichmentViewModel extends CommitteeViewModel {
	private final static String GC_UPDATE_ENRICHED_GENES = "updateEnrichedGenes";
	
	static {
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_ENRICH_GENE_SET_FINISHED, 
			EnrichmentViewModel.GC_UPDATE_ENRICHED_GENES
		);
	}

	private String geneSetURL = "/loading.zul";
	private boolean geneSetURLVisible = false;
	private String nameFilter = "";
	private String sourceFilter = "";
	private double pValueFilter = 0.05;
	private int coverageFilter = 10;
	
	private Map<String, Integer> geneSetCoverage = new HashMap<String, Integer>();
	
	@Init
	@Override
	public void init() {
		super.init();
		
		this.updateCoverage(); // Already checks if committee has gene sets
		
		StatusViewModel.changeInitialStatus("Enrich your gene selection using GeneBrowser");
	}
	
	public String getNameFilter() {
		return nameFilter;
	}

	public void setNameFilter(String filter) {
		if (!this.nameFilter.toUpperCase().equals(filter.toUpperCase())) {
			this.nameFilter = filter;
			
			BindUtils.postNotifyChange(null, null, this, "geneSets");
		}
	}
	
	public String getSourceFilter() {
		return sourceFilter;
	}

	public void setSourceFilter(String sourceFilter) {
		if (!this.sourceFilter.equals(sourceFilter)) {
			this.sourceFilter = sourceFilter;

			BindUtils.postNotifyChange(null, null, this, "geneSets");
		}
	}
	
	public double getPValueFilter() {
		return this.pValueFilter;
	}
	
	public void setPValueFilter(double filter) {
		if (this.pValueFilter != filter) {
			this.pValueFilter = filter;
			
			BindUtils.postNotifyChange(null, null, this, "geneSets");
		}
	}
	
	public int getCoverageFilter() {
		return this.coverageFilter;
	}
	
	public void setCoverageFilter(int filter) {
		if (this.coverageFilter != filter) {
			this.coverageFilter = filter;
			
			BindUtils.postNotifyChange(null, null, this, "geneSets");
		}
	}
	
	public List<String> getSources() {
		final Committee committee = this.getCommittee();
		
		if (committee.hasGeneSets()) {
			final SortedSet<String> sources = new TreeSet<String>();
			
			for (GeneSetMetaData metadata : this.getAllGeneSets()) {
				sources.add(metadata.getSource());
			}
			
			final ArrayList<String> sourceList = new ArrayList<String>(sources);
			sourceList.add(0, "");
			
			return sourceList;
		} else {
			return Collections.emptyList();
		}
	}

	public boolean getHasGeneSets() {
		return this.getCommittee().hasGeneSets();
	}
	
	public int getNumSelectedGeneSets() {
		return this.getCommittee().getNumSelectedGenes();
	}
	
	public int getCoverage(GeneSetMetaData geneSet) {
		return this.geneSetCoverage.get(geneSet.getGeneSetId());
	}
	
	private void updateCoverage() {
		final Committee committee = this.getCommittee();
		
		if (committee.hasGeneSets()) {
			this.geneSetCoverage.clear();
			
			final Set<String> dataSetGenes = committee.getDataSet().getGeneNames();
			
			for (GeneSetMetaData geneSet : committee.getGeneSets()) {
				final Set<String> geneSetGenes = new HashSet<String>(geneSet.getGenes());
				geneSetGenes.retainAll(dataSetGenes);
				
				this.geneSetCoverage.put(geneSet.getGeneSetId(), geneSetGenes.size());
			}
		}
	}
	
	private List<GeneSetMetaData> getAllGeneSets() {
		final Committee committee = this.getCommittee();
		
		if (committee.hasGeneSets()) {
			return new ArrayList<GeneSetMetaData>(committee.getGeneSets());
		} else {
			return Collections.emptyList();
		}
	}
	
	public List<GeneSetMetaData> getGeneSets() {
		final Committee committee = this.getCommittee();
		
		if (committee.hasGeneSets()) {
			final List<GeneSetMetaData> currentGeneSets = new ArrayList<GeneSetMetaData>(committee.getGeneSets());
			
			final boolean nameEmpty = this.nameFilter.trim().isEmpty();
			final boolean sourceEmpty = this.sourceFilter.isEmpty();
			final String filterUpper = this.nameFilter.toUpperCase();
			
			final Iterator<GeneSetMetaData> iterator = currentGeneSets.iterator();
			while (iterator.hasNext()) {
				final GeneSetMetaData metadata = iterator.next();
				
				if ((!nameEmpty && !metadata.getName().toUpperCase().contains(filterUpper)) ||
					(!sourceEmpty && !metadata.getSource().equals(this.sourceFilter)) ||
					metadata.getPValue() > this.pValueFilter ||
					this.getCoverage(metadata) < this.coverageFilter
				) {
					iterator.remove();
				}
			}
			
			return currentGeneSets;
		} else {
			return Collections.emptyList();
		}
	}
	
	@Command
	@NotifyChange("geneSets")
	public void selectAll() {
		this.dataLostNotification(
			"Gene set selection modification",
			"Gene set selection modification will provoke data deletion (evaluation configuration, execution results, etc.). Do you wish to continue?",
			CommitteeStatus.EVALUATOR,
			CommitteeStatus.GENE_SETS_SELECTED,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					for (GeneSetMetaData geneSet : EnrichmentViewModel.this.getGeneSets()) {
						geneSet.setSelected(true);
						session.update(geneSet);
					}
					EnrichmentViewModel.this.updateExecutions();
					BindUtils.postNotifyChange(null, null, EnrichmentViewModel.this, "numSelectedGeneSets");
				}
			}
		);
	}
	
	@Command
	@NotifyChange({ "geneSets", "nameFilter", "sourceFilter", "PValueFilter", "coverageFilter" })
	public void clearFilters() {
		this.nameFilter = "";
		this.sourceFilter = "";
		this.pValueFilter = 0.05;
		this.coverageFilter = 10;
	}
	
	@Command
	@NotifyChange("geneSets")
	public void unselectAll() {
		this.dataLostNotification(
			"Gene set selection modification",
			"Gene set selection modification will provoke data deletion (evaluation configuration, execution results, etc.). Do you wish to continue?",
			CommitteeStatus.EVALUATOR,
			CommitteeStatus.GENE_SETS_SELECTED,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					for (GeneSetMetaData geneSet : EnrichmentViewModel.this.getGeneSets()) {
						geneSet.setSelected(false);
						session.update(geneSet);
					}
					EnrichmentViewModel.this.updateExecutions();
					BindUtils.postNotifyChange(null, null, EnrichmentViewModel.this, "numSelectedGeneSets");
				}
			}
		);
	}
	
	@Command
	@NotifyChange("geneSets")
	public void invertSelection() {
		this.dataLostNotification(
			"Gene set selection modification",
			"Gene set selection modification will provoke data deletion (evaluation configuration, execution results, etc.). Do you wish to continue?",
			CommitteeStatus.EVALUATOR,
			CommitteeStatus.GENE_SETS_SELECTED,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					for (GeneSetMetaData geneSet : EnrichmentViewModel.this.getGeneSets()) {
						geneSet.setSelected(!geneSet.isSelected());
						session.update(geneSet);
					}
					EnrichmentViewModel.this.updateExecutions();
					BindUtils.postNotifyChange(null, null, EnrichmentViewModel.this, "numSelectedGeneSets");
				}
			}
		);
	}
	
	@Command
	public synchronized void geneSetChecked(
		@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
		@BindingParam("geneSet") final GeneSetMetaData geneSet,
		@BindingParam("checked") final boolean checked
	) {
		final Checkbox checkbox = (Checkbox) ctx.getComponent();
		
		this.dataLostNotification(
			"Gene set selection modification",
			"Gene set selection modification will provoke data deletion (evaluation configuration, execution results, etc.). Do you wish to continue?",
			CommitteeStatus.EVALUATOR,
			CommitteeStatus.GENE_SETS_SELECTED,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					final GeneSetMetaData geneSetMD = (GeneSetMetaData) session.load(GeneSetMetaData.class, geneSet.getId());
					geneSet.setSelected(checked);
					geneSetMD.setSelected(checked);
					session.update(geneSetMD);
					
					EnrichmentViewModel.this.updateExecutions();
					BindUtils.postNotifyChange(null, null, EnrichmentViewModel.this, "numSelectedGeneSets");
				}
			},
			new Runnable() {
				@Override
				public void run() {
					checkbox.setChecked(geneSet.isSelected());
					EnrichmentViewModel.this.updateExecutions();
					BindUtils.postNotifyChange(null, null, EnrichmentViewModel.this, "numSelectedGeneSets");
				}
			}
		);
	}
	
	public String getGeneSetURL() {
		return this.geneSetURL;
	}
	
	public boolean isGeneSetURLVisible() {
		return this.geneSetURLVisible;
	}
	
	@Command("closeGeneBrowserWindow")
	@NotifyChange({ "geneSetURLVisible", "geneSetURL" })
	public void closeGeneBrowserWindow(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		final Event event = ctx.getTriggerEvent();
		
		this.geneSetURLVisible = false;
		this.geneSetURL = "/loading.zul";
		event.stopPropagation();
	}
	
	@Command("showGeneSet")
	@NotifyChange({"geneSetURL", "geneSetURLVisible"})
	public void showGeneSet(
		@BindingParam("geneSet") GeneSetMetaData geneSet,
		@BindingParam("onlyCovered") boolean onlyCovered
	) {
		final Committee committee = this.getCommittee();
		geneSet = committee.getGeneSet(geneSet.getGeneSetId());
		
		final Set<String> geneNames = new HashSet<String>(geneSet.getGenes());
		
		if (onlyCovered) {
			geneNames.retainAll(committee.getDataSet().getGeneNames());
		}
		
		if (geneNames.size() < Configuration.getInstance().getGeneBrowserMaxQuery()) {
			final String url = Configuration.getInstance().getGeneBrowserURL();
			
			String genes = "";
			for (String gene : geneNames) {
				if (gene.matches("[a-zA-Z0-9]+")) {
					if (!genes.isEmpty())
						genes += ",";
					genes += gene;
				}
			}
			
			this.geneSetURL = url.replace(Configuration.getInstance().getGeneBrowserGenesMarker(), genes);
			this.geneSetURLVisible = true;
			
			System.out.println(this.geneSetURL);
		} else {
			System.err.println("Illegal Number of Genes");
		}
	}
	
	@GlobalCommand(GC_UPDATE_ENRICHED_GENES)
	@NotifyChange({ "geneSets", "enrichmentCompleted", "sources" })
	public void geneSetEnrichmentFinished(
		@BindingParam(UserGlobalEventListener.KEY_DATA) GeneEnrichmentTask task
	) {
		if (task.hasErrors()) {
			Clients.showNotification(
				"Connection error. Please, try again in a few minutes", 
				Clients.NOTIFICATION_TYPE_ERROR,
				null,
				"middle_center",
				0,
				true
			);
		} else {
			this.updateCoverage();
		}
	}
	
	@Command("enrich")
	public void enrich() {
		final Set<RankedGene> geneList = this.getCommittee().getRankedGenes();
		final Integer committeeId = this.getCommitteeId();
		final String userId = this.getUserId();
		
		this.dataLostNotification(
			"Gene set enrichment",
			"Gene set enrichment will provoke data deletion (evaluation configuration, execution results, etc.). Do you wish to continue?",
			CommitteeStatus.EVALUATOR,
			CommitteeStatus.GENE_SETS_SELECTED,
			new Runnable() {
				@Override
				public void run() {
					ExecutionEngine.getSingleton().execute(
						new GeneEnrichmentTask(
							userId,
							committeeId,
							geneList
						)
					);
				}
			}
		);
	}
	
	public Comparator<GeneSetMetaData> getAscNameSorter() {
		return new NameSorter();
	}
	
	public Comparator<GeneSetMetaData> getDescNameSorter() {
		return Collections.reverseOrder(new NameSorter());
	}
	
	public Comparator<GeneSetMetaData> getAscSourceSorter() {
		return new SourceSorter();
	}
	
	public Comparator<GeneSetMetaData> getDescSourceSorter() {
		return Collections.reverseOrder(new SourceSorter());
	}
	
	public Comparator<GeneSetMetaData> getAscPValueSorter() {
		return new PValueSorter();
	}
	
	public Comparator<GeneSetMetaData> getDescPValueSorter() {
		return Collections.reverseOrder(new PValueSorter());
	}
	
	public Comparator<GeneSetMetaData> getAscNumGenesSorter() {
		return new NumGenesSorter();
	}
	
	public Comparator<GeneSetMetaData> getDescNumGenesSorter() {
		return Collections.reverseOrder(new NumGenesSorter());
	}
	
	public Comparator<GeneSetMetaData> getAscNumCoveredGenesSorter() {
		return new NumCoveredGenesSorter();
	}
	
	public Comparator<GeneSetMetaData> getDescNumCoveredGenesSorter() {
		return Collections.reverseOrder(new NumCoveredGenesSorter());
	}

	private final class NumCoveredGenesSorter implements Comparator<GeneSetMetaData> {
		@Override
		public int compare(GeneSetMetaData o1, GeneSetMetaData o2) {
			final int coverage1 = EnrichmentViewModel.this.getCoverage(o1);
			final int coverage2 = EnrichmentViewModel.this.getCoverage(o2);
			
			return coverage1 - coverage2;
		}
	}
	
	@Override
	public String getCurrentStep() {
		return CommitteeViewModel.ENRICHMENT_STEP;
	}
	
	@Override
	public String getNextHref() {
		return CommitteeViewModel.CLASSIFIERS_PATH;
	}
	
	@Override
	public String getPreviousHref() {
		return CommitteeViewModel.GENE_SET_PATH;
	}
	
	@Override
	@DependsOn("enrichmentCompleted")
	public boolean getHasNext() {
		return this.isEnrichmentCompleted();
	}

	@Override
	protected CommitteeStatus stepStatus() {
		return CommitteeStatus.GENE_SETS_SELECTED;
	}
}
