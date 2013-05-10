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
package es.uvigo.ei.sing.gc.view.models.committee.summary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.GeneSelectionMetaData;
import es.uvigo.ei.sing.gc.model.entities.GeneSelectionType;
import es.uvigo.ei.sing.gc.utils.Property;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class SummaryViewModel extends CommitteeViewModel {
	@Init
	@Override
	public void init() {
		super.init();
		
		StatusViewModel.changeInitialStatus("Check your generated committee");
	}
	
	public void setCommitteeName(String name) {
		this.getCommittee().setName(name);
		HibernateUtil.currentSession().update(this.getCommittee());
	}
	
	public String getCommitteeName() {
		return this.getCommittee().getName();
	}
	
	public List<Property> getProperties() {
		return SummaryViewModel.extractCommitteeProperties(this.getCommittee());
	}
	
	public static List<Property> extractCommitteeProperties(Committee committee) {
		final List<Property> properties = new LinkedList<Property>();
		
		final DataSetMetaData dataSet = committee.getDataSet();
		final GeneSelectionMetaData geneSet = committee.getGeneSelection();
		final ExperimentMetaData experiment = committee.getExperiment();
		
		properties.add(new Property("Data Set"));
		properties.add(Property.create("Name", dataSet.getName()));
		properties.add(Property.create("Conditions", dataSet.getClassNames()));
		properties.add(Property.create("Num. Conditions", dataSet.getNumClasses()));
		properties.add(Property.create("Num. Samples", dataSet.getSamples()));
		properties.add(Property.create("Num. Genes", dataSet.getVariables()));
		
		
		properties.add(new Property("Gene Selection"));
		properties.add(Property.create("Name", GeneSelectionType.getTypeForBuilder(geneSet.getBuilderClassName())));
		properties.add(Property.create("Num. Genes", geneSet.getNumGenes()));
		
		properties.add(new Property("Committee"));
		properties.add(Property.create("Num. Total Experts", experiment.getResults().size()));
		properties.add(Property.create("Num. Selected Experts", experiment.getSelectedCount()));
		boolean first = true;
		final List<ExpertResult> selectedExperts = experiment.getSelectedExperts();
		Collections.sort(selectedExperts, new ExpertResult.ExpertResultMetaDataComparator());
		
		for (ExpertResult expert : selectedExperts) {
			if (first) {
				properties.add(Property.create("Experts", expert));
				first = false;
			} else {
				properties.add(Property.create("", expert));
			}
		}
		
		
		return properties;
	}
	
	@Command("saveAndFinish")
	public void saveAndFinish() throws Exception {
		final Committee committee = this.getCommittee();

		if (committee.isFinished()) {
			throw new IllegalStateException("Committee is already completed");
		} else {
			final Session session = HibernateUtil.currentSession();
			
			Committee sameName = (Committee) session.createCriteria(Committee.class)
				.setMaxResults(1)
				.add(Restrictions.eq("user", committee.getUser()))
				.add(Restrictions.eq("name", committee.getName()))
				.add(Restrictions.not(Restrictions.eq("id", committee.getId())))
			.uniqueResult();
			
			if (sameName == null) {
				committee.setFinished(true);
				
				session.persist(committee);
				
				Executions.sendRedirect("/home.zul");
			} else {
				Messagebox.show(
					"There is another committee with the same name. Please, change the name in order to continue.", 
					"Invalid Name", 
					Messagebox.OK, 
					Messagebox.ERROR
				);
			}
		}
	}
	
	@Override
	public String getCurrentStep() {
		return CommitteeViewModel.SUMMARY_STEP;
	}
	
	@Override
	public String getPreviousHref() {
		return CommitteeViewModel.EVALUATION_PATH;
	}

	@Override
	protected CommitteeStatus stepStatus() {
		return CommitteeStatus.EXPERTS_SELECTED;
	}
}
