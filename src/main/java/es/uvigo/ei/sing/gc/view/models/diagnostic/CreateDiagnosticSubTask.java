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

import java.io.File;
import java.io.IOException;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.zkoss.util.media.Media;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.Committee.Compatibility;
import es.uvigo.ei.sing.gc.model.entities.Diagnostic;
import es.uvigo.ei.sing.gc.model.entities.PatientSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.ZKUtils;

class CreateDiagnosticSubTask implements Subtask<Diagnostic> {
	private final Media media;
	private final Integer committeeId;
	
	private Task<Diagnostic> task;
	private boolean aborted;
	
	public CreateDiagnosticSubTask(Integer committeeId, Media media) {
		super();
		this.media = media;
		this.committeeId = committeeId;
		
		this.task = null;
		this.aborted = false;
	}
	
	private void checkAborted() throws AbortException {
		if (this.aborted) throw new AbortException();
	}
	
	@Override
	public Diagnostic call() throws Exception {
		Transaction transaction = null;
		Session session = null;
		
		PatientSetMetaData patients = null;
		try {
			this.checkAborted();
			
			final Data data = ZKUtils.loadDataFromMedia(this.media);
			
			this.checkAborted();
			
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			
			final Committee committee = ZKUtils.hLoad(Committee.class, this.committeeId, session);
			
			if (committee.checkCompatibility(data) == Compatibility.NONE) {
				throw new IOException("The provided data set is not compatible with the committee");
			}
			
			final User user = committee.getUser();//ZKUtils.hLoad(User.class, this.userId, session);
			patients = user.addPatientSet(data);
			session.persist(user);
			
			this.checkAborted();
			
			session.flush();
			
			final Diagnostic diagnostic = new Diagnostic();
			diagnostic.setPatientData(patients);
			diagnostic.setCommittee(committee);
			diagnostic.setName(patients.getName());
			
			session.persist(diagnostic);
			
			this.checkAborted();
			
			session.flush();
			transaction.commit();
			
			return diagnostic;
		} catch (Throwable t) {
			if (transaction != null)
				try { transaction.rollback(); }
				catch (HibernateException he) {}
			
			if (patients != null && patients.hasFile()) {
				new File(patients.getFileName()).delete();
			}
			
			t.printStackTrace();
			if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new Exception(t);
			}
		} finally {
			if (session.isOpen()) 
				try { session.close(); }
				catch (HibernateException e) {}
		}
	}

	@Override
	public void abort() {
		this.aborted = true;
	}

	@Override
	public Task<Diagnostic> getTask() {
		return this.task;
	}

	@Override
	public void setTask(Task<Diagnostic> task) {
		this.task = task;
	}
}