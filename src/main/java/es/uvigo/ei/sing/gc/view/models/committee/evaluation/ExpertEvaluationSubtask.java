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

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import es.uvigo.ei.sing.ensembles.data.FeatureSubspace;
import es.uvigo.ei.sing.ensembles.evaluation.ExpertsEvaluator;
import es.uvigo.ei.sing.ensembles.training.Expert;
import es.uvigo.ei.sing.ensembles.training.IBaseClassificationProblem;
import es.uvigo.ei.sing.ensembles.training.IExpert;
import es.uvigo.ei.sing.ensembles.training.events.ExpertsEvaluationEvent;
import es.uvigo.ei.sing.ensembles.training.listeners.ExpertsEvaluationAdapter;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.SampleClassification;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.genomics.genensembles.GeneFeatureSet;
import es.uvigo.ei.sing.genomics.genes.GeneSet;

final class ExpertEvaluationSubtask extends ExpertsEvaluationAdapter implements Subtask<ExpertResult> {
	private static final int MAX_TRIES = 5;

	private static final long serialVersionUID = 1L;
	
	private final ExpertsEvaluator<?> evaluator;
	private final IExpert expert;
	private final Integer experimentId;
	private final String builderName;
	private final String geneSetId;
	private final String geneSetName;
	
	private Task<ExpertResult> task;
	private ExpertResult result;
	private boolean aborted;

	public ExpertEvaluationSubtask(
		String builderName, 
		Integer experimentId,
		ExpertsEvaluator<?> evaluator,
		Map<String, String> geneSetIdToName,
		IBaseClassificationProblem problem
	) throws IllegalArgumentException {
		final GeneSet geneSet = ExpertEvaluationSubtask.extractProblemGeneSet(problem);
		
		this.geneSetId = geneSet.getName();
		this.geneSetName = geneSetIdToName.get(this.geneSetId);
		
		this.experimentId = experimentId;
		this.builderName = builderName;
		this.evaluator = evaluator;
		this.aborted = false;
		this.expert = new Expert(problem);
	}

	private static GeneSet extractProblemGeneSet(
		IBaseClassificationProblem problem
	) throws IllegalArgumentException {
		if (problem.getSubspace() instanceof FeatureSubspace) {
			final FeatureSubspace subspace = (FeatureSubspace) problem.getSubspace();
			
			if (subspace.getFeatureSet() instanceof GeneFeatureSet) {
				final GeneFeatureSet geneFeatureSet = (GeneFeatureSet) subspace.getFeatureSet();

				return geneFeatureSet.getGeneSet();
			} else {
				throw new IllegalArgumentException("problem.getSubspace().getFeatureSet() must be a GeneFeatureSet");
			}
		} else {
			throw new IllegalArgumentException("problem.getSubspace() must be a FeatureSubspace");
		}
	}
	
	private static class StorageException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public StorageException(String message) {
			super(message);
		}
	}

	private void storeResult(ExpertResult result, Integer experimentId)
	throws StorageException {
		synchronized (ExpertEvaluationSubtask.class) {
			boolean done = false;
			int tries = 0;
			do {
				Session session = null;
				Transaction transaction = null;
				ExperimentMetaData experiment = null;
				try {
					this.checkAbort();
					
					session = HibernateUtil.getSessionFactory().openSession();
					transaction = session.beginTransaction();

					this.checkAbort();
					
					experiment = (ExperimentMetaData) session.load(ExperimentMetaData.class, experimentId);

					for (SampleClassification sample : result.getSamples()) {
						session.persist(sample);
					}
					
					this.checkAbort();
					
					session.flush();
					session.persist(result);
					
					this.checkAbort();
					
					session.flush();
					experiment.getResults().add(result);

					this.checkAbort();
					
					session.update(experiment);
					transaction.commit();
					
					done = true;
				} catch (Exception e) {
					try {
						if (transaction != null)
							transaction.rollback();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					result.setId(null);
					for (SampleClassification sample : result.getSamples()) {
						sample.setId(null);
					}
					if (experiment != null)
						experiment.getResults().remove(result);
					
					if (this.aborted) return;
					
					e.printStackTrace();
				} finally {
					try {
						if (session != null && session.isOpen()) 
							session.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} while (!done && ++tries < ExpertEvaluationSubtask.MAX_TRIES);
			
			if (!done) {
				final String message = "Evaluation result could not be stored: " + result.toString();
				System.err.println(message);
				throw new StorageException(message);
			}
		}
	}

	private void checkAbort() throws AbortException {
		if (this.aborted) throw new AbortException();
	}
	
	@Override
	public ExpertResult call() throws StorageException {
		try {
			this.checkAbort();
			
			this.evaluator.addExpertsEvaluationListener(this);
		
			this.checkAbort();
			
			this.evaluator.evaluateExpert(this.expert);
			this.evaluator.removeExpertsEvaluationListener(this);
		
			return result; // Updated through the listening methods
		} catch (AbortException ae) {
			return null;
		}
	}

	@Override
	public void expertEvaluated(ExpertsEvaluationEvent event) throws StorageException {
		if (event.getStepObject().getExpert() == this.expert) {
			this.storeResult(this.result = new ExpertResult(
				this.builderName, 
				this.geneSetName, 
				this.geneSetId, 
				event.getStepObject().getClassificationPerformance()
			), this.experimentId);
		}
	}

	@Override
	public void expertEvaluationAborted(ExpertsEvaluationEvent event) throws StorageException {
		if (event.getStepObject().getExpert() == this.expert) {
			final Throwable error = event.getError();
			
			if (error instanceof ExpertEvaluationSubtask.StorageException) {
				throw (ExpertEvaluationSubtask.StorageException) error;
			} else {
				this.storeResult(this.result = new ExpertResult(
					this.builderName, 
					this.geneSetName, 
					this.geneSetId,
					error
				), this.experimentId);
			}
		}
	}
	
	@Override
	public Task<ExpertResult> getTask() {
		return this.task;
	}
	
	@Override
	public void setTask(Task<ExpertResult> task) {
		this.task = task;
	}

	@Override
	public void abort() {
		this.aborted = true;
	}
}