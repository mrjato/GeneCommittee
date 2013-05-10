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
package es.uvigo.ei.sing.gc.view.models.committee.evaluation;

import java.util.List;

import org.hibernate.Session;

import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExperimentStatus;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;

final class ExperimentExecutionTask extends Task<ExpertResult> {
	private final Integer committeeId;
	private final Integer experimentId;

	public ExperimentExecutionTask(
		String userId,
		Integer committeeId,
		Integer experimentId,
		List<ExpertEvaluationSubtask> subtasks
	) {
		super(GlobalEvents.EVENT_EXECUTION, userId, "Experiment execution", subtasks);
		this.committeeId = committeeId;
		this.experimentId = experimentId;
	}

	@Override
	public void preStart() {
		final Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		
		final ExperimentMetaData experiment = 
			(ExperimentMetaData) session.get(ExperimentMetaData.class, experimentId);
		experiment.setStatus(ExperimentStatus.Running);
		experiment.setStart(System.currentTimeMillis());
		
		session.update(experiment);
		
		session.getTransaction().commit();
		session.close();
	}

	@Override
	public void postFinish() {
		final Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		
		if (this.isAborted()) {
			final Committee committee = (Committee) session.load(Committee.class, committeeId);
			
			committee.returnToState(CommitteeStatus.EXPERIMENT_EXECUTION);
			session.update(committee);
		} else {
			final ExperimentMetaData experiment = 
					(ExperimentMetaData) session.get(ExperimentMetaData.class, experimentId);
			experiment.setStatus(ExperimentStatus.Finished);
			experiment.setEnd(System.currentTimeMillis());
			session.update(experiment);
		}
		
		session.getTransaction().commit();
		session.close();
	}
}