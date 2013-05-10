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

import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.model.entities.User;

public abstract class UserTasksViewModel {
	private static final String GC_UPDATE_USER_TASK_RUNNING = "updateUserTaskRunning";

	static {
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_STARTED, 
			GC_UPDATE_USER_TASK_RUNNING
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_FINISHED, 
			GC_UPDATE_USER_TASK_RUNNING
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_ABORTED, 
			GC_UPDATE_USER_TASK_RUNNING
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_SCHEDULED, 
			GC_UPDATE_USER_TASK_RUNNING
		);
	}
	
	private boolean isGuest;
	private String userId;

	@Init
	public void init() {
		final User user = UserViewModel.getUser(false);
		this.isGuest = user.isGuest();
		this.setUserId(user.getEmail());
	}
	
	public boolean isGuest() {
		return this.isGuest;
	}
	
	protected String getUserId() {
		return this.userId;
	}
	
	protected void setUserId(String userId) {
		this.userId = userId;
	}
	
	@GlobalCommand(UserTasksViewModel.GC_UPDATE_USER_TASK_RUNNING)
	@NotifyChange("userTaskRunning")
	public void updateUserTaskRunning() {}

	public boolean isUserTaskRunning() {
		return ExecutionEngine.getSingleton().hasTaskRunning(this.getUserId());
	}
}