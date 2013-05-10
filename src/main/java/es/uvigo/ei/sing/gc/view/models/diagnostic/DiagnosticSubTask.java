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
package es.uvigo.ei.sing.gc.view.models.diagnostic;

import java.util.Collections;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.validation.ClassificationPerformance;
import es.uvigo.ei.sing.ensembles.basemodels.IBaseModelPrediction;
import es.uvigo.ei.sing.ensembles.basemodels.IBaseModelsFactory;
import es.uvigo.ei.sing.ensembles.data.IFeatureSubspaceGenerator;
import es.uvigo.ei.sing.ensembles.training.BaseClassificationProblemGenerator;
import es.uvigo.ei.sing.ensembles.training.Expert;
import es.uvigo.ei.sing.ensembles.training.IExpert;
import es.uvigo.ei.sing.ensembles.weka.basemodels.WekaBaseModelFactory;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.Diagnostic;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.SampleClassification;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.genomics.genensembles.GFMSubspaceGenerator;
import es.uvigo.ei.sing.genomics.genensembles.GeneFeatureMatrix;
import es.uvigo.ei.sing.genomics.genensembles.GeneFeatureSet;

final class DiagnosticSubTask implements Subtask<ExpertResult> {
	private final Integer expertResultId;
	private final Integer diagnosticId;
	private final Data trainingData;
	private final Data testingData;
	
	private Task<ExpertResult> task;
	private boolean aborted;
	
	public DiagnosticSubTask(
		Integer expertResultId,
		Integer diagnosticId,
		Data trainingData,
		Data testingData
	) {
		super();
		this.diagnosticId = diagnosticId;
		this.expertResultId = expertResultId;
		this.trainingData = trainingData;
		this.testingData = testingData;
		this.task = null;
		this.aborted = false;
	}
	
	private Expert createExpert() throws Exception {
		synchronized (DiagnosticViewModel.class) {
			Session session = null;
			Transaction transaction = null;
			
			try {
				if (this.aborted) throw new AbortException();
				session = HibernateUtil.getSessionFactory().openSession();
				transaction = session.beginTransaction();
				
				if (this.aborted) throw new AbortException();
				final Diagnostic diagnostic = ZKUtils.hLoad(Diagnostic.class, this.diagnosticId, session);
				final Committee committee = diagnostic.getCommittee();//(Committee) session.load(Committee.class, this.committeeId);
				final ExpertResult result = ZKUtils.hLoad(ExpertResult.class, this.expertResultId, session);

				if (this.aborted) throw new AbortException();
				
				final ClassifierBuilderMetaData classifier = 
						committee.getClassifier(result.getClassifierName());
				final GeneSetMetaData geneSet = committee.getGeneSet(result.getGeneSetId());
				
				final GeneFeatureSet featureSet = new GeneFeatureSet(geneSet.toGeneSet(), trainingData);
				final IFeatureSubspaceGenerator subspaceGenerator = new GFMSubspaceGenerator(
					new GeneFeatureMatrix(Collections.singletonList(featureSet))
				);
				final IBaseModelsFactory baseModelsFactory = WekaBaseModelFactory.createMultipleWekaModelFactory(
					classifier.createNewBuilder()
				);
				
				final BaseClassificationProblemGenerator generator = new BaseClassificationProblemGenerator(
					subspaceGenerator, baseModelsFactory
				);
				
				if (this.aborted) throw new AbortException();
				
				return new Expert(generator.generate(this.trainingData)[0]);
			} catch (Exception e) {
				if (transaction != null) 
					try { transaction.rollback(); }
					catch (HibernateException he) {}
				e.printStackTrace();
				throw e;
			} finally {
				if (session != null && session.isOpen())
					try { session.close(); }
					catch (HibernateException he) {}
			}
		}
	}
	
	private ExpertResult storeResult(ClassificationPerformance performance) throws Exception {
		synchronized (DiagnosticViewModel.class) {
			Session session = null;
			Transaction transaction = null;
			
			try {
				if (this.aborted) throw new AbortException();
				session = HibernateUtil.getSessionFactory().openSession();
				transaction = session.beginTransaction();
				
				if (this.aborted) throw new AbortException();
				final Diagnostic diagnostic = ZKUtils.hLoad(Diagnostic.class, this.diagnosticId, session);
				final ExpertResult result = ZKUtils.hLoad(ExpertResult.class, this.expertResultId, session);
				
				if (this.aborted) throw new AbortException();
				
				final ExpertResult patientClassification = new ExpertResult(
					result.getClassifierName(), 
					result.getGeneSetName(), 
					result.getGeneSetId(), 
					performance
				);
				
				for (SampleClassification sc : patientClassification.getSamples()) {
					session.persist(sc);
					if (this.aborted) throw new AbortException();
					session.flush();
				}
				
				session.persist(patientClassification);
				if (this.aborted) throw new AbortException();
				session.flush();
				
				diagnostic.getResults().add(patientClassification);
				if (this.aborted) throw new AbortException();
				session.update(diagnostic);
				
				transaction.commit();

				return patientClassification;
			} catch (Exception e) {
				if (transaction != null) 
					try { transaction.rollback(); }
					catch (HibernateException he) {}
				e.printStackTrace();
				throw e;
			} finally {
				if (session != null && session.isOpen())
					try { session.close(); }
					catch (HibernateException he) {}
			}
		}
	}

	@Override
	public ExpertResult call() throws Exception {
		try {
			final IExpert expert = Expert.train(this.createExpert());
			
			if (this.aborted) throw new AbortException();
			
			final Data preparedData = expert.prepareData(this.testingData);
			
			if (this.aborted) throw new AbortException();
			
			final IBaseModelPrediction predictions = expert.getExpert().predict(preparedData);
			
			if (this.aborted) throw new AbortException();
			return this.storeResult(predictions.getClassificationPerformance());
		} catch (AbortException ae) {
			throw ae;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void abort() {
		this.aborted = true;
	}

	@Override
	public Task<ExpertResult> getTask() {
		return this.task;
	}
	
	@Override
	public void setTask(Task<ExpertResult> task) {
		this.task = task;
	}
}