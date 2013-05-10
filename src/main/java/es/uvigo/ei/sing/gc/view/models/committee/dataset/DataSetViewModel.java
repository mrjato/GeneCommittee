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
package es.uvigo.ei.sing.gc.view.models.committee.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Session;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.utils.Property;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class DataSetViewModel extends CommitteeViewModel {
	@Init
	@Override
	public void init() {
		super.init();
        
        if (this.getDataSets().isEmpty()) {
        	Messagebox.show(
        		"You do not have any data set. " +
        		"If you want to use this section go to 'Data Management' and upload a new data set.",
        		"No Data Sets",
        		Messagebox.OK,
        		Messagebox.EXCLAMATION,
        		new EventListener<Event>() {
        			public void onEvent(Event event) throws Exception {
        				Executions.sendRedirect("/home.zul");
        			}
				}
        	);
        }

		StatusViewModel.changeInitialStatus("Select a data set");
	}
	
	public List<DataSetMetaData> getDataSets() {
		final ArrayList<DataSetMetaData> dataSets = new ArrayList<DataSetMetaData>(
			this.getCommittee().getUser().getDataSets()
		);
		Collections.sort(dataSets, new DataSetMetaData.DataSetMetaDataComparator());
		
		return dataSets;
	}

	public boolean hasDataSet() {
		return this.getCommittee().hasDataSet();
	}
	
	@DependsOn(CommitteeViewModel.DATA_SET_STEP)
	public List<Property> getDataSetProperties() throws Exception {
		final List<Property> properties = new LinkedList<Property>();
		
		final Committee committee = this.getCommittee();
		if (committee.hasDataSet()) {
			final DataSetMetaData dataSet = committee.getDataSet();
			final Map<String, Integer> conditions = 
				new TreeMap<String, Integer>(dataSet.getConditions());
			
			properties.add(new Property("Metadata"));
			properties.add(Property.create("Conditions", dataSet.getClassNames()));
			properties.add(Property.create("Num. Conditions", dataSet.getNumClasses()));
			properties.add(Property.create("Num. Samples", dataSet.getSamples()));
			properties.add(Property.create("Num. Genes", dataSet.getVariables()));
			
			properties.add(new Property("Conditions"));
			for (Map.Entry<String, Integer> condition : conditions.entrySet()) {
				properties.add(Property.create(
					condition.getKey(), 
					String.format("%.2f%% [%d samples]", 
						condition.getValue().floatValue() / dataSet.getSamples() * 100f,
						condition.getValue().intValue()
					)
				));
			}
		}
		
		return properties;
	}
	
	public DataSetMetaData getDataSet() {
		return this.getCommittee().getDataSet();
	}
	
	public void setDataSet(DataSetMetaData dataSet) {
		final Committee committee = this.getCommittee();
		final Integer dataSetId = dataSet.getId();
		
		if (!dataSet.equals(committee.getDataSet())) {
			this.dataLostNotification(
				"Data Set Selection",
				"Data set changing will provoke data deletion (gene set, classifiers, etc.). Do you wish to continue?",
				CommitteeStatus.RANKED_GENES,
				CommitteeStatus.DATA_SET,
				new Runnable() {
					public void run() {
						final Session session = HibernateUtil.currentSession();
						final Committee committee = DataSetViewModel.this.getCommittee();
						final DataSetMetaData dataSet = (DataSetMetaData) session.get(DataSetMetaData.class, dataSetId);
						committee.setDataSet(dataSet);
						committee.setName(dataSet.getName());
						session.persist(committee);
						DataSetViewModel.this.updateExecutions();
						
						BindUtils.postNotifyChange(null, null, DataSetViewModel.this, CommitteeViewModel.DATA_SET_STEP);
					}
				}
			);
		}
	}
	
	@Override
	public String getCurrentStep() {
		return CommitteeViewModel.DATA_SET_STEP;
	}
	
	@Override
	public String getNextHref() {
		return CommitteeViewModel.GENE_SET_PATH;
	}
	
	@Override
	@DependsOn("dataSetCompleted")
	public boolean getHasNext() {
		return this.isDataSetCompleted();
	}

	@Override
	protected CommitteeStatus stepStatus() {
		return CommitteeStatus.DATA_SET;
	}
}
