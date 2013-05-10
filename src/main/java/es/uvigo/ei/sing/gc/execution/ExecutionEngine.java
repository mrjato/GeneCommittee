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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;

import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.execution.Task.TaskStatus;
import es.uvigo.ei.sing.gc.utils.Mailer;

public class ExecutionEngine {
	private final static ExecutionEngine singleton = new ExecutionEngine();
	
	public static ExecutionEngine getSingleton() {
		return singleton;
	}
	
	private final ConcurrentMap<String, Task<?>> userTasks;
	private final Map<Task<?>, Set<SubtaskExecutor<?>>> taskExecutors;
	private final Map<SubtaskExecutor<?>, RunnableFuture<?>> subtaskFutures;
	private final ThreadPoolExecutor executor;
	
	private final CustomBlockingQueue workQueue;
	
	private ExecutionEngine() {
		this.userTasks = new ConcurrentHashMap<String, Task<?>>();
		this.taskExecutors = new HashMap<Task<?>, Set<SubtaskExecutor<?>>>();
		this.subtaskFutures = new Hashtable<ExecutionEngine.SubtaskExecutor<?>, RunnableFuture<?>>();
		
		this.workQueue = new CustomBlockingQueue();
		this.executor = new ThreadPoolExecutor(
			Configuration.getInstance().getExperimentsCorePoolSize(), 
			Configuration.getInstance().getExperimentsMaximumPoolSize(), 
			Long.MAX_VALUE, TimeUnit.DAYS,
			this.workQueue
		);
	}
	
	public <T> void execute(final Task<T> task) throws IllegalStateException {
		if (this.executor.isShutdown())
			throw new IllegalStateException("Executor is shutted down");
		
		final String userId = task.getUserId();
		
		if (this.userTasks.containsKey(userId)) {
			throw new IllegalStateException("User already have a scheduled task.");
		} else {
			synchronized (this.userTasks) {
				if (this.userTasks.containsKey(userId)) {
					throw new IllegalStateException("User already have a scheduled task.");
				} else {
					final HashSet<SubtaskExecutor<?>> subtasks = new HashSet<ExecutionEngine.SubtaskExecutor<?>>();
					
					this.userTasks.put(userId, task);
					this.taskExecutors.put(task, subtasks);
					
					this.workQueue.beginBatchAdd(userId);
					try {
						synchronized(task) {
							for (final Subtask<T> subtask : task.getSubtasks()) {
								final SubtaskExecutor<T> subtaskExecutor = new SubtaskExecutor<T>(subtask);
								
								final RunnableFuture<?> runnableFuture = 
									(RunnableFuture<?>) this.executor.submit(subtaskExecutor);
								this.subtaskFutures.put(subtaskExecutor, runnableFuture);
								subtasks.add(subtaskExecutor);
							}
							
							task.schedule();
							ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SCHEDULED, task);
						}
					} finally {
						this.workQueue.endBatchAdd();
					}
					
				}
			}
		}
	}
	
	private final static void publishEvent(Task<?> task, String suffix, Object value) {
		final EventQueue<Event> queue = EventQueueUtils.getUserQueue(task.getUserId());
		
		if (suffix == null) {
			queue.publish(new Event(task.getTaskId(), null, new EventTaskData(task, value)));
		} else {
			queue.publish(new Event(task.getTaskId() + suffix, null, new EventTaskData(task, value)));
		}
	}
	
	public void shutdown() {
		synchronized (this.userTasks) {
			for (Task<?> task : this.userTasks.values()) {
				task.abort();
			}
		
			this.executor.shutdownNow();
			try {
				this.executor.awaitTermination(10l, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.userTasks.clear();
				this.taskExecutors.clear();
				this.subtaskFutures.clear();
			}
		}
	}

	public Task<?> getUserTask(String email) {
		return this.userTasks.get(email);
	}

	public boolean hasTaskRunning(String email) {
		return this.userTasks.containsKey(email);
	}
	
	public void cancelUserTaks(String email) {
		if (this.userTasks.containsKey(email)) {
			synchronized (this.userTasks) {
				if (this.userTasks.containsKey(email)) {
					final Task<?> userTask = this.userTasks.remove(email);
					
					synchronized(userTask) {
						if (!userTask.isFinished()) {
							userTask.abort();
							
							final List<Subtask<?>> unfinished = new ArrayList<Subtask<?>>();
							
							this.workQueue.beginBatchRemove();
							
							try {
								for (SubtaskExecutor<?> subtaskExecutor : this.taskExecutors.remove(userTask)) {
									if (this.executor.remove(this.subtaskFutures.remove(subtaskExecutor))) {
										unfinished.add(subtaskExecutor.getSubtask());
									}
								}
							} finally {
								this.workQueue.endBatchRemove();
							}
							
							userTask.postFinish(); //TODO Replace with postAbort?
							ExecutionEngine.publishEvent(userTask, GlobalEvents.SUFFIX_ABORTED, unfinished);
						}
					}
				}
			}
		}
	}
	
	private final class SubtaskExecutor<T> implements Runnable {
		private final Subtask<T> subtask;

		public SubtaskExecutor(Subtask<T> subtask) {
			this.subtask = subtask;
		}
		
		public Subtask<T> getSubtask() {
			return subtask;
		}
		
		private Task<T> getTask() {
			return this.getSubtask().getTask();
		}
		
		private void checkAborted() throws AbortException {
			if (this.getTask().isAborted()) throw new AbortException();
		}

		@Override
		public void run() {
			try {
				final Task<T> task = this.getTask();
				
				if (task.getStatus() == TaskStatus.Unscheduled) {
					// Forces subtask to wait until the task is completely scheduled
					synchronized(task){}
				}
				
				this.checkAborted();
				
				if (task.getStatus() == TaskStatus.Scheduled) {
					synchronized (task) {
						if (task.getStatus() == TaskStatus.Scheduled) {
							task.preStart();
							task.start();
							ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_STARTED, task);
						}
					}
				}
				
				this.checkAborted();
				
				task.preSubtaskStart(this.subtask);
				task.subtaskStarted(this.subtask);
				ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_STARTED, this.subtask);
				
				try {
					this.checkAborted();
					
					final T result = this.subtask.call();
					
					this.checkAborted();
					task.subtaskFinished(this.subtask);
					task.postSubtaskFinish(this.subtask, result);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_FINISHED, result);
				} catch (AbortException e) {
					e.printStackTrace();
					this.checkAborted();
					task.subtaskAborted(this.subtask, e);
					task.postSubtaskAbort(this.subtask, e);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_ABORTED, this.subtask);
				} catch (Exception e) {
					e.printStackTrace();
					this.checkAborted();
					task.subtaskError(this.subtask, e);
					task.postSubtaskError(this.subtask, e);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_ERROR, this.subtask);
				}
				
				synchronized(ExecutionEngine.this.userTasks) {
					synchronized(task) {
						if (task.isFinished()) {
							ExecutionEngine.this.userTasks.remove(task.getUserId());
							ExecutionEngine.this.taskExecutors.get(task).remove(this);
							ExecutionEngine.this.subtaskFutures.remove(this);
							
							task.postFinish();
							
							ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_FINISHED, task);
							
							try {
								Mailer.checkAndSendFinishNotification(
									task.getUserId(), "Task '" + task.getDescription() + "' finished at '" + new SimpleDateFormat().format(new Date())
								);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (AbortException ae) {}
		}
	}
}
