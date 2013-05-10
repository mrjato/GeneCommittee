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
package es.uvigo.ei.sing.gc.view.models;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;

import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.execution.UserGlobalEventListener;

public class StatusViewModel extends UserTasksViewModel {
	private static final String STATUS_NOT_RUNNING_IMAGE = "/img/notrunning.png";
	private static final String STATUS_RUNNING_IMAGE = "/img/running.gif";
	private static final String STATUS_BAR_RUNNING_CLASS = "statusBarRunning";
	private static final String STATUS_BAR_CLASS = "statusBar";
	
	public static final String GC_CHANGE_INITIAL_STATUS = "changeInitialStatus";
	public static final String GC_UPDATE_STATUS = "updateStatus";

	static {
		GlobalEvents.fullRegisterGlobalCommand(StatusViewModel.GC_UPDATE_STATUS);
	}
	
	private boolean initialMessage = false;
	private String status = "";
	private String statusImage = StatusViewModel.STATUS_NOT_RUNNING_IMAGE;
	private String statusClass = StatusViewModel.STATUS_BAR_CLASS;
	
	@Init
	@Override
	public void init() {
		super.init();
		
		final Task<?> task = ExecutionEngine.getSingleton().getUserTask(this.getUserId());
		if (task != null) {
			if (task.getStatus() == Task.TaskStatus.Started) {
				if (task.getNumTasks() == 1) {
					this.status = "[RUNNING] " + task.getDescription();
				} else {
					this.status = String.format("[RUNNING] %s (%.2f%% completed - %d tasks running)", 
						task.getDescription(), task.getCompletedPercentage()*100f, task.getRunningTasks()
					);
				}
				
				this.statusImage = StatusViewModel.STATUS_RUNNING_IMAGE;
				this.statusClass = StatusViewModel.STATUS_BAR_RUNNING_CLASS;
				this.initialMessage = true;
			}
		}
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getStatusImage() {
		return statusImage;
	}
	
	public String getStatusClass() {
		return statusClass;
	}

	@GlobalCommand(StatusViewModel.GC_UPDATE_STATUS)
	@NotifyChange({ "status", "statusImage", "statusClass", "userTaskRunning" })
	public void updateStatus(
		@BindingParam(UserGlobalEventListener.KEY_TASK) Task<?> task,
		@BindingParam(UserGlobalEventListener.KEY_DATA) Object data,
		@BindingParam(UserGlobalEventListener.KEY_EVENT) String event,
		@BindingParam(UserGlobalEventListener.KEY_ACTION) String action
	) {
		if (action.equals(GlobalEvents.ACTION_FINISHED)) {
			this.status = "[FINISHED] " + task.getDescription();
			this.statusImage = StatusViewModel.STATUS_NOT_RUNNING_IMAGE;
			this.statusClass = StatusViewModel.STATUS_BAR_CLASS;
		} else if (action.equals(GlobalEvents.ACTION_ABORTED)) {
			Clients.showNotification(
				"Task '" + task.getDescription() + "' was aborted. ", 
				Clients.NOTIFICATION_TYPE_WARNING, 
				null, 
				"middle_center", 
				4000
			);
			
			this.status = "[ABORTED] " + task.getDescription();
			this.statusImage = StatusViewModel.STATUS_NOT_RUNNING_IMAGE;
			this.statusClass = StatusViewModel.STATUS_BAR_CLASS;
		} else if (action.equals(GlobalEvents.ACTION_SUBTASK_ERROR)) {
			final String title = "Error " + task.getDescription();
			
			final String message;
			if (data instanceof Throwable) {
				final Throwable error = (Throwable) data;
				message = "Error execution task " + task.getDescription() + ": " + error.getMessage() + ".";
			} else {
				message = "Error execution task " + task.getDescription() + ".";
			}
			
			Messagebox.show(
				message,
				title,
				Messagebox.OK,
				Messagebox.EXCLAMATION
			);
			
			if (task.getNumTasks() == 1) {
				this.status = "[RUNNING] " + task.getDescription();
			} else {
				this.status = String.format("[RUNNING] %s (%.2f%% completed - %d tasks running)", 
					task.getDescription(), task.getCompletedPercentage()*100f, task.getRunningTasks()
				);
			}
					
			this.statusImage = StatusViewModel.STATUS_RUNNING_IMAGE;
			this.statusClass = StatusViewModel.STATUS_BAR_RUNNING_CLASS;
		} else if (action.equals(GlobalEvents.ACTION_SCHEDULED)) {
			this.status = String.format(
				"[SCHEDULED] Task '%s' scheduled. Execution will start soon", 
				task.getDescription()
			);
			this.statusImage = StatusViewModel.STATUS_RUNNING_IMAGE;
			this.statusClass = StatusViewModel.STATUS_BAR_RUNNING_CLASS;
		} else if (action.equals(GlobalEvents.ACTION_STARTED) || 
			action.equals(GlobalEvents.ACTION_SUBTASK_STARTED) ||
			action.equals(GlobalEvents.ACTION_SUBTASK_FINISHED) ||
			action.equals(GlobalEvents.ACTION_SUBTASK_ABORTED)
//				action.equals(GlobalEvents.ACTION_SUBTASK_ERROR)
		) {
			if (task.getNumTasks() == 1) {
				this.status = "[RUNNING] " + task.getDescription();
			} else {
				this.status = String.format("[RUNNING] %s (%.2f%% completed - %d tasks running)", 
					task.getDescription(), task.getCompletedPercentage()*100f, task.getRunningTasks()
				);
			}
					
			this.statusImage = StatusViewModel.STATUS_RUNNING_IMAGE;
			this.statusClass = StatusViewModel.STATUS_BAR_RUNNING_CLASS;
		} 
	}
	
	@GlobalCommand(StatusViewModel.GC_CHANGE_INITIAL_STATUS)
	@NotifyChange({"status", "statusImage", "statusClass", "userTaskRunning" })
	public void changeInitialStatusCommand(
		@BindingParam("message") String message,
		@BindingParam("image") String image,
		@BindingParam("statusClass") String statusClass
	) {
		if (!this.initialMessage) {
			this.status = message;
			this.statusImage = image;
			this.statusClass = statusClass;
		}
	}
	
	public static void changeInitialStatus(String status) {
		StatusViewModel.changeInitialStatus(
			status, 
			StatusViewModel.STATUS_NOT_RUNNING_IMAGE, 
			StatusViewModel.STATUS_BAR_CLASS
		);
	}
	
	public static void changeInitialStatus(String status, String statusImage, String statusClass) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", status);
		params.put("image", statusImage);
		params.put("statusClass", statusClass);
		
		BindUtils.postGlobalCommand(null, null, StatusViewModel.GC_CHANGE_INITIAL_STATUS, params);
	}
	
	@Command
	public void abortExecution() {
		final String userId = this.getUserId();
		
		Messagebox.show(
			"Are you sure you want to cancel current execution?",
			"Cancel execution",
			Messagebox.YES | Messagebox.NO,
			Messagebox.EXCLAMATION,
			new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (((Integer) event.getData()) == Messagebox.YES) {
						ExecutionEngine.getSingleton().cancelUserTaks(userId);
					}
				}
			}
		);
	}
}
