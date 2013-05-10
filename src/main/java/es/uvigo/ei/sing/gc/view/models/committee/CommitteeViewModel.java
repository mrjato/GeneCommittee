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
package es.uvigo.ei.sing.gc.view.models.committee;

import org.hibernate.Session;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.models.UserTasksViewModel;
import es.uvigo.ei.sing.gc.view.models.UserViewModel;

public abstract class CommitteeViewModel extends UserTasksViewModel {
	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";

	public static final String DATA_SET_STEP = "dataSet";
	public static final String GENE_SET_STEP = "geneSet";
	public static final String ENRICHMENT_STEP = "enrichment";
	public static final String CLASSIFIERS_STEP = "classifiers";
	public static final String EVALUATION_STEP = "evaluation";
	public static final String SUMMARY_STEP = "summary";
	
	public static final String DATA_SET_PATH = "/committee/dataSet.zul";
	public static final String GENE_SET_PATH = "/committee/geneSet.zul";
	public static final String ENRICHMENT_PATH = "/committee/enrichment.zul";
	public static final String CLASSIFIERS_PATH = "/committee/classifiers.zul";
	public static final String EVALUATION_PATH = "/committee/evaluation.zul";
	public static final String SUMMARY_PATH = "/committee/summary.zul";

	static {
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_SCHEDULED, 
			CommitteeViewModel.GC_UPDATE_EXECUTIONS
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_STARTED, 
			CommitteeViewModel.GC_UPDATE_EXECUTIONS
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_FINISHED, 
			CommitteeViewModel.GC_UPDATE_EXECUTIONS
		);
		GlobalEvents.fullActionRegisterGlobalCommand(
			GlobalEvents.ACTION_ABORTED, 
			CommitteeViewModel.GC_UPDATE_EXECUTIONS
		);
	}
	
	private final static String[] NAVIGATION_PROPERTIES = new String[] {
		"dataSetCompleted",
		"geneSetCompleted",
		"enrichmentCompleted",
		"classifiersCompleted",
		"evaluationCompleted",
		"userTaskRunning"
	};
	
	private Committee committee;
	private Integer committeeId;
	
	@Init
	public void init() {
		super.init();
		
		final User user = UserViewModel.getUser();
		
		if (user.getActiveCommittee() == null) {
			final Committee newCommittee = new Committee();
			user.addCommittee(newCommittee);
			
			final Session session = HibernateUtil.currentSession();
			session.persist(newCommittee);
			session.flush();
		}
		
		this.committee = user.getActiveCommittee();
		this.committeeId = this.committee.getId();
	}
	
	public Integer getCommitteeId() {
		return this.committeeId;
	}
	
	public Committee getCommittee() {
		return this.getCommittee(true);
	}
	
	public Committee getCommittee(boolean refresh) {
		if (refresh) {
//			HibernateUtil.currentSession().refresh(this.committee);
			this.refreshCommittee();
		}
		
		return this.committee;
	}
	
	private void refreshCommittee() {
		final Session session = HibernateUtil.currentSession();
		this.committee = (Committee) session.get(Committee.class, this.getCommitteeId());
	}
	
	protected boolean isCompleted(CommitteeStatus state) {
		return this.getCommittee().isCompleted(state);
	}
	
	public boolean isDataSetCompleted() {
		return this.isCompleted(CommitteeStatus.DATA_SET);
	}
	
	public boolean isGeneSetCompleted() {
		return this.isCompleted(CommitteeStatus.RANKED_GENES);
	}
	
	public boolean isEnrichmentCompleted() {
		return this.isCompleted(CommitteeStatus.GENE_SETS_SELECTED);
	}
	
	public boolean isClassifiersCompleted() {
		return this.isCompleted(CommitteeStatus.EVALUATOR);
	}
	
	public boolean isEvaluationCompleted() {
		return this.isCompleted(CommitteeStatus.EXPERTS_SELECTED);
	}
	
	@GlobalCommand(CommitteeViewModel.GC_UPDATE_EXECUTIONS)
	public void updateExecutions() {
		for (String property : NAVIGATION_PROPERTIES) {
			BindUtils.postNotifyChange(null, null, this, property);
		}
	}
	
	protected final void dataLostNotification(
		final String title,
		final String message,
		final CommitteeStatus nextStatus,
		final CommitteeStatus returnToStatus,
		final Runnable yesCallback
	) {
		this.dataLostNotification(title, message, nextStatus, returnToStatus, yesCallback, null, yesCallback);
	}
	
	protected final void dataLostNotification(
		final String title,
		final String message,
		final CommitteeStatus nextStatus,
		final CommitteeStatus returnToStatus,
		final Runnable yesCallback,
		final Runnable noCallback	
	) {
		this.dataLostNotification(title, message, nextStatus, returnToStatus, yesCallback, noCallback, yesCallback);
	}
	
	protected final void dataLostNotification(
		final String title,
		final String message,
		final CommitteeStatus nextStatus,
		final CommitteeStatus returnToStatus,
		final Runnable yesCallback,
		final Runnable noCallback,
		final Runnable noDataLost
	) {
		final Committee committee  = this.getCommittee();
		
		if (committee.isCompleted(nextStatus)) {
			Messagebox.show(
				message,
				title,
				Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (((Integer) event.getData()) == Messagebox.YES) {
							final Session session = HibernateUtil.currentSession();
							final Committee committee = CommitteeViewModel.this.getCommittee();
							
							committee.returnToState(returnToStatus);
							
							session.update(committee);
							session.flush();
							
							CommitteeViewModel.this.updateExecutions();
							
							if (yesCallback != null) yesCallback.run();
						} else {
							if (noCallback != null) noCallback.run();
						}
					}
				}
			);
		} else {
			noDataLost.run();
		}
	}
	
	@Command
	public void resetToStep() {
		if (ExecutionEngine.getSingleton().hasTaskRunning(this.getUserId())) {
			Messagebox.show(
				"You can't modify a committee while you have running tasks. Please, abort the execution before modifying the committee",
				"Delete Committee",
				Messagebox.OK,
				Messagebox.ERROR
			);
		} else {
			Messagebox.show(
				"Are you sure you want to return the committee to the current step and delete further data?",
				"Return to Current Step",
				Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (((Integer) event.getData()) == Messagebox.YES) {
							final Session session = HibernateUtil.currentSession();
							final Committee committee = CommitteeViewModel.this.getCommittee();
	
							committee.returnToState(CommitteeViewModel.this.stepStatus());
							
							session.update(committee);
							session.flush();
							
							CommitteeViewModel.this.updateExecutions();
						}
					}
				}
			);
		}
	}
	
	@Command
	public void deleteCommittee() {
		if (ExecutionEngine.getSingleton().hasTaskRunning(this.getUserId())) {
			Messagebox.show(
				"You can't delete a committee while you have running tasks. Please, abort the execution before deleting the committee",
				"Delete Committee",
				Messagebox.OK,
				Messagebox.ERROR
			);
		} else {
			Messagebox.show(
				"Are you sure you want to delete the current committee?",
				"Delete Committee",
				Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (((Integer) event.getData()) == Messagebox.YES) {
							final Session session = HibernateUtil.currentSession();
							final Committee committee = CommitteeViewModel.this.getCommittee();
	
							session.delete(committee);
							session.flush();
							
							Executions.sendRedirect("/home.zul");
						}
					}
				}
			);
		}
	}
	
	protected abstract CommitteeStatus stepStatus();
	public abstract String getCurrentStep();
	
	public String getNextHref() {
		return null;
	}
	public String getPreviousHref() {
		return null;
	}
	public boolean getHasNext() {
		return this.getNextHref() != null;
	}
	
	public boolean getHasPrevious() {
		return this.getPreviousHref() != null;
	}
	
	public static String getCurrentURL() {
		final User user = UserViewModel.getUser();
		
		if (user == null) {
			return null;
		} else if (user.getActiveCommittee() == null) {
			return CommitteeViewModel.DATA_SET_PATH;
		} else {
			switch(user.getActiveCommittee().getCurrentState()) {
			case FINISHED:
			case EXPERTS_SELECTED:
				return CommitteeViewModel.SUMMARY_PATH;
			case EXPERT_SELECTION:
			case EXPERIMENT_EXECUTION:
				return CommitteeViewModel.EVALUATION_PATH;
			case EVALUATOR:
				return CommitteeViewModel.CLASSIFIERS_PATH;
			case GENE_SETS_SELECTED:
			case GENE_ENRICHMENT:
				return CommitteeViewModel.ENRICHMENT_PATH;
			case RANKED_GENES:
			case GENE_SELECTION:
				return CommitteeViewModel.GENE_SET_PATH;
			case DATA_SET:
			case INIT:
			default:
				return CommitteeViewModel.DATA_SET_PATH;
			}
		}
	}
	
	public static boolean isStepCompleted(String step) {
		final User user = UserViewModel.getUser();
		if (user == null) return false;
		
		final Committee committee = user.getActiveCommittee();
		if (committee == null) return false;
		
		if (step.equals(CommitteeViewModel.DATA_SET_STEP)) {
			return committee.isCompleted(CommitteeStatus.DATA_SET);
		} else if (step.equals(CommitteeViewModel.GENE_SET_STEP)) {
			return committee.isCompleted(CommitteeStatus.RANKED_GENES);
		} else if (step.equals(CommitteeViewModel.ENRICHMENT_STEP)) {
			return committee.isCompleted(CommitteeStatus.GENE_SETS_SELECTED);
		} else if (step.equals(CommitteeViewModel.CLASSIFIERS_STEP)) {
			return committee.isCompleted(CommitteeStatus.EVALUATOR);
		} else if (step.equals(CommitteeViewModel.EVALUATION_STEP)) {
			return committee.isCompleted(CommitteeStatus.EXPERTS_SELECTED);
		} else if (step.equals(CommitteeViewModel.SUMMARY_STEP)) {
			return committee.isCompleted(CommitteeStatus.FINISHED);
		} else {
			return false;
		}
	}
}
