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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.zkoss.util.media.Media;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.data.Variable;
import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.Diagnostic;
import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.Committee.ExpertIncompatibility;
import es.uvigo.ei.sing.gc.utils.DataUtils;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public final class CreateDiagnosticTask extends Task<Diagnostic> {
	private Diagnostic diagnostic;

	public CreateDiagnosticTask(
		String userId,
		Integer committeeId,
		Media media
	) {
		super(
			GlobalEvents.EVENT_DIAGNOSTIC_CREATION, 
			userId, 
			"Diagnostic creation (data uploading)", 
			Arrays.asList(new CreateDiagnosticSubTask(committeeId, media))
		);
	}

	public void postSubtaskFinish(Subtask<Diagnostic> subtask, Diagnostic result) {
		this.diagnostic = result;
	}

	public void postFinish() {
		if (this.isAborted() || this.isError()) {
			if (this.diagnostic != null) {
				Session session = null;
				Transaction transaction = null;
				try {
					session = HibernateUtil.getSessionFactory().openSession();
					transaction = session.beginTransaction();
					
					this.diagnostic = ZKUtils.hLoad(Diagnostic.class, this.diagnostic.getId(), session);
					this.diagnostic.getCommittee().getDiagnostics().remove(this.diagnostic);
					session.delete(this.diagnostic);
					
					transaction.commit();
				} catch (Exception e) {
					if (transaction != null)
						try { transaction.rollback(); }
						catch (HibernateException he) {}
					e.printStackTrace();
				} finally {
					if (session != null) 
						try { session.close(); }
						catch (HibernateException he) {}
				}
			}
		} else {
			if (this.diagnostic != null) {
				Session session = null;
				Transaction transaction = null;
				try {
					session = HibernateUtil.getSessionFactory().openSession();
					transaction = session.beginTransaction();
					
					this.diagnostic = ZKUtils.hLoad(Diagnostic.class, this.diagnostic.getId(), session);
					
					final Committee committee = this.diagnostic.getCommittee();
					final Data trainingData = committee.getDataSet().loadData();
					final Data testingData = this.diagnostic.getPatientData().loadData();
					
					// In testingData, class values are replaced by arbitrary compatible class values
					final List<String> classValues = DataUtils.getClassValuesAsString(trainingData);
					final int numClassValues = classValues.size();
					final int numSamples = testingData.getSampleCount();
					
					Variable classVariable = DataUtils.getClassVariable(testingData);
					if (classVariable == null) {
						classVariable = new Variable("CLASS", Variable.TYPE_STRING, testingData);
						testingData.setTargetVariable(classVariable);
					}
					classVariable.setLimitedPossibleValues(new ArrayList<Object>(classValues));
					
					final List<Object> values = new ArrayList<Object>(numSamples);
					for (int i = 0; i < numSamples; i++) {
						values.add(classValues.get(i%numClassValues));
					}

					testingData.setVariableValues(classVariable, values);
					
					final Map<Integer, ExpertIncompatibility> incomps = 
						committee.getExpertCompatibility(testingData);
					
					final ExperimentMetaData experiment = committee.getExperiment();
					final DiagnosticSubTask[] subtasks = 
						new DiagnosticSubTask[experiment.getSelectedCount() - incomps.size()];
					int i = 0;
					for (ExpertResult result : experiment.getSelectedExperts()) {
						if (!incomps.containsKey(result.getId())) {
							subtasks[i++] = new DiagnosticSubTask(
								result.getId(), this.diagnostic.getId(), trainingData, testingData
							);
						}
					}
					transaction.commit();
					
					ExecutionEngine.getSingleton().execute(new DiagnosticTask(
						this.getUserId(),
						this.diagnostic.getId(),
						committee.checkCompatibility(incomps),
						subtasks
					));
				} catch (Exception e) {
					if (transaction != null)
						try { transaction.rollback(); }
						catch (HibernateException he) {}
					e.printStackTrace();
				} finally {
					if (session != null) 
						try { session.close(); }
						catch (HibernateException he) {}
				}
			} else {
				System.err.println("Missing diagnostic");
			}
		}
	}
}