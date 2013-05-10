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
package es.uvigo.ei.sing.gc.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Task<T> {
	public enum TaskStatus {
		Unscheduled,
		Scheduled,
		Started,
		Finished,
		Aborted;
	}

	private final String taskId;
	private final String userId;
	private final List<Subtask<T>> subtasks;
	private final Map<Subtask<T>, AbortException> abortCause;
	private final Map<Subtask<T>, Throwable> errors;
	
	private TaskStatus status;
	private String description;
	
	private int runningTasks;
	private int finishedTasks; 
	
	public Task(
		String taskId,
		String userId, 
		String description,
		Subtask<T> ... subtasks
	) {
		this(taskId, userId, description, Arrays.asList(subtasks));
	}
	
	public Task(
		String taskId,
		String userId, 
		String description,
		List<? extends Subtask<T>> subtasks
	) {
		this.taskId = taskId;
		this.userId = userId;
		this.status = TaskStatus.Unscheduled;
		this.description = description;
		
		this.abortCause = new HashMap<Subtask<T>, AbortException>();
		this.errors = new HashMap<Subtask<T>, Throwable>();
		
		this.subtasks = Collections.unmodifiableList(new ArrayList<Subtask<T>>(subtasks));
		for (Subtask<T> subtask : this.subtasks) {
			subtask.setTask(this);
		}
	}
	
	public void preSchedule() {}
	public void postSchedule() {}
	public void preStart() {}
	public void preSubtaskStart(Subtask<T> subtask) {}
	public void postSubtaskFinish(Subtask<T> subtask, T result) {}
	public void postSubtaskAbort(Subtask<T> subtask, Throwable cause) {}
	public void postSubtaskError(Subtask<T> subtask, Throwable cause) {}
	public void postFinish() {}
	
	
	public synchronized void schedule() {
		this.status = TaskStatus.Scheduled;
		this.runningTasks = 0;
		this.finishedTasks = 0;
	}
	
	public synchronized void start() {
		if (this.status != TaskStatus.Scheduled)
			throw new IllegalStateException("Task must be scheduled before start");
		
		this.status = TaskStatus.Started;
	}
	
	/**
	 * 
	 * @param subtask
	 * @return <code>true</code> if <code>subtask</code> is the first subtask started. 
	 * <code>false</code> otherwise.
	 * @throws IllegalStateException
	 */
	public synchronized void subtaskStarted(Subtask<T> subtask) 
	throws IllegalStateException {
		if (this.status == TaskStatus.Aborted) {
			return;
		} else if (this.status == TaskStatus.Started) {
			this.runningTasks++;
		} else {
			throw new IllegalStateException("Subtask started in state: " + this.status);
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return <code>true</code> there is no more tasks to execute. 
	 * <code>false</code> otherwise.
	 * @throws IllegalStateException
	 */
	public synchronized void subtaskFinished(Subtask<T> task) 
	throws IllegalStateException {
		if (this.status == TaskStatus.Aborted) {
			return;
		} else if (this.status == TaskStatus.Started) {
			this.runningTasks--;
			this.finishedTasks++;
		} else {
			throw new IllegalStateException("Subtask finished in state: " + this.status);
		}
	}
	
	public synchronized void subtaskAborted(Subtask<T> subtask, AbortException cause) {
		this.abortCause.put(subtask, cause);
		
		this.subtaskFinished(subtask);
	}
	
	public synchronized void subtaskError(Subtask<T> subtask, Throwable cause) {
		this.errors.put(subtask, cause);
		
		this.subtaskFinished(subtask);
	}
	
	public synchronized void abort() {
		this.status = TaskStatus.Aborted;
		
		for (Subtask<T> subtask : this.subtasks) {
			subtask.abort();
		}
	}
	
	public boolean isAborted() {
		return this.status == TaskStatus.Aborted;
	}
	
	public synchronized boolean isError() {
		if (this.isFinished()) {
			final Set<Subtask<T>> errorTasks = new HashSet<Subtask<T>>(this.errors.keySet());
			errorTasks.addAll(this.abortCause.keySet());
			
			return errorTasks.containsAll(this.getSubtasks());
		} else {
			return false;
		}
	}

	public TaskStatus getStatus() {
		return status;
	}
	
	public String getTaskId() {
		return taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserId() {
		return userId;
	}
	
	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public List<Subtask<T>> getSubtasks() {
		return Collections.unmodifiableList(subtasks);
	}
	
	public int getNumTasks() {
		return this.subtasks.size();
	}
	
	public int getRunningTasks() {
		return runningTasks;
	}
	
	public int getFinishedTasks() {
		return finishedTasks;
	}
	
	public int getTotalTasks() {
		return this.subtasks.size();
	}
	
	public boolean isFinished() {
		return !this.isAborted() && this.getFinishedTasks() == this.getTotalTasks();
	}
	
	public float getCompletedPercentage() {
		return (float) this.finishedTasks / (float) this.subtasks.size();
	}
}
