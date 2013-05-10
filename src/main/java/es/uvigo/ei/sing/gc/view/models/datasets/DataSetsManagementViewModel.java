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
package es.uvigo.ei.sing.gc.view.models.datasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.models.UserTasksViewModel;
import es.uvigo.ei.sing.gc.view.models.UserViewModel;

public class DataSetsManagementViewModel extends UserTasksViewModel {
	public static final String GC_DATA_UPLOADED_CREATED = "dataUploaded";
	public static final String GC_DATA_UPLOAD_ERROR= "dataUploadError";

	static {
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_DATA_UPLOADING_FINISHED, 
			DataSetsManagementViewModel.GC_DATA_UPLOADED_CREATED
		);
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_DATA_UPLOADING_SUBTASK_ERROR, 
			DataSetsManagementViewModel.GC_DATA_UPLOAD_ERROR
		);
	}
	
	private DataSetMetaData currentDataSet;
	private String filter = "";
	
	@Init
	public void init() {
		super.init();
	}
	
	@DependsOn("filter")
	public List<DataSetMetaData> getDataSets() {
		final List<DataSetMetaData> datasets = new ArrayList<DataSetMetaData>(
			UserViewModel.getUser().getDataSets()
		);
		
		if (!this.filter.trim().isEmpty()) {
			final String filterUpper = this.filter.toUpperCase();
			
			final Iterator<DataSetMetaData> iterator = datasets.iterator();
			while (iterator.hasNext()) {
				if (!iterator.next().getName().toUpperCase().contains(filterUpper)) {
					iterator.remove();
				}
			}
			
		}
		Collections.sort(datasets, this.getNameAscSorter());
		
		return datasets;
	}
	
	@Command
	@NotifyChange("filter")
	public void clearFilter() {
		this.setFilter("");
	}
	
	public DataSetMetaData getCurrentDataSet() {
		return currentDataSet;
	}

	public void setCurrentDataSet(DataSetMetaData currentDataSet) {
		this.currentDataSet = currentDataSet;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public Comparator<DataSetMetaData> getNameAscSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
	}
	
	public Comparator<DataSetMetaData> getNameDescSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o2.getName().compareTo(o1.getName());
			}
		};
	}
	
	public Comparator<DataSetMetaData> getSamplesAscSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o1.getSamples() - o2.getSamples();
			}
		};
	}
	
	public Comparator<DataSetMetaData> getSamplesDescSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o2.getSamples() - o1.getSamples();
			}
		};
	}
	
	public Comparator<DataSetMetaData> getGenesAscSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o1.getVariables() - o2.getVariables();
			}
		};
	}
	
	public Comparator<DataSetMetaData> getGenesDescSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o2.getVariables() - o1.getVariables();
			}
		};
	}
	
	public Comparator<DataSetMetaData> getConditionsAscSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o1.getNumClasses() - o2.getNumClasses();
			}
		};
	}
	
	public Comparator<DataSetMetaData> getConditionsDescSorter() {
		return new Comparator<DataSetMetaData>() {
			@Override
			public int compare(DataSetMetaData o1, DataSetMetaData o2) {
				return o2.getNumClasses() - o1.getNumClasses();
			}
		};
	}
	
	@Command("renameDataSet")
	public void renameDataSet(@BindingParam("dataSet") DataSetMetaData dataSet) {
		if (!this.isUserTaskRunning()) {
			HibernateUtil.currentSession().update(dataSet);
		}
	}
	
	@Command("changeCurrentDataSet")
	@NotifyChange("currentDataSet")
	public void changeCurrentDataSet(@BindingParam("dataSet") DataSetMetaData dataSet) {
		this.setCurrentDataSet(dataSet);
	}
	
	@Command("deleteDataSet")
	@NotifyChange("dataSets")
	public void deleteDataSet(@BindingParam("dataSet") final DataSetMetaData dataSet) {
		Messagebox.show(
			"Are you sure you want to delete the selected data set?", 
			"Delete Data Set", 
			Messagebox.YES | Messagebox.NO, 
			Messagebox.EXCLAMATION, 
			new DeleteDataSetEventListener(dataSet.getId())
		);
	}
	
	@Command("downloadDataSet")
	public void downloadDataSet(@BindingParam("dataSet") DataSetMetaData dataSet) {
		if (!this.isUserTaskRunning()) {
			try {
				final File file = new File(
					dataSet.getUser().getDataSetsDirectory(), 
					dataSet.getFileName()
				);
				
				Filedownload.save(file, "text/csv");
			} catch (IOException e) {
				Clients.alert(
					"Internal error. Sorry, file could not be downloaded", 
					"Download Error", 
					Clients.NOTIFICATION_TYPE_ERROR
				);
			}
		}
	}
	
	@Command
	public void upload(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		ExecutionEngine.getSingleton().execute(
			new UploadDataTask(
				UserViewModel.getUser(false).getEmail(), 
				((UploadEvent) ctx.getTriggerEvent()).getMedia()
			)
		);
	}
	
	@GlobalCommand(DataSetsManagementViewModel.GC_DATA_UPLOADED_CREATED)
	@NotifyChange({ "dataSets" })
	public void dataUploaded() {}
	
	@GlobalCommand(DataSetsManagementViewModel.GC_DATA_UPLOAD_ERROR)
	public void dataUploadError() {
		Clients.showNotification(
			"There was an error while uploading the data set. Please, review the help to " +
			"ensure that the file format is correct.",
			Clients.NOTIFICATION_TYPE_ERROR,
			null,
			"middle_center",
			0,
			true
		);
	}
	
	private final class DeleteDataSetEventListener implements EventListener<Event> {
		private Integer dataSetId;
		
		public DeleteDataSetEventListener(Integer dataSetId) {
			this.dataSetId = dataSetId;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			if (((Integer) event.getData()) == Messagebox.YES) {
				final Session session = HibernateUtil.currentSession();
				
				final DataSetMetaData dataSet = (DataSetMetaData)
					session.get(DataSetMetaData.class, this.dataSetId);
				final User user = dataSet.getUser();
				user.removeDataSet(dataSet);
				
				session.delete(dataSet);
				session.update(user);
				
				BindUtils.postNotifyChange(null, null, DataSetsManagementViewModel.this, "dataSets");
				
				if (dataSet.equals(DataSetsManagementViewModel.this.getCurrentDataSet())) {
					DataSetsManagementViewModel.this.setCurrentDataSet(null);
					BindUtils.postNotifyChange(null, null, DataSetsManagementViewModel.this, "currentDataSet");
				}
			}
		}
	}
}
