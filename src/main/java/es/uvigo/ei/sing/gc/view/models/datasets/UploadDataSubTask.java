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
package es.uvigo.ei.sing.gc.view.models.datasets;

import java.io.File;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.util.media.Media;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.ZKUtils;

class UploadDataSubTask implements Subtask<DataSetMetaData> {
	private final Media media;
	private final String userId;
	
	private Task<DataSetMetaData> task;
	private boolean aborted;
	
	public UploadDataSubTask(String userId, Media media) {
		super();
		this.media = media;
		this.userId = userId;
		
		this.task = null;
		this.aborted = false;
	}
	
	@Override
	public DataSetMetaData call() throws Exception {
		Transaction transaction = null;
		Session session = null;
		
		DataSetMetaData dataSet = null;
		try {
			if (this.aborted) throw new AbortException();
			
			final Data data = ZKUtils.loadDataFromMedia(this.media);
			
			if (this.aborted) throw new AbortException();
			
			session = HibernateUtil.getSessionFactory().openSession();
			transaction = session.beginTransaction();
			final User user = ZKUtils.hLoad(User.class, this.userId, session);
			dataSet = user.addDataSet(data);
			session.persist(user);
			
			if (this.aborted) throw new AbortException();
			
			session.flush();
			
			transaction.commit();
			
			return dataSet;
		} catch (Throwable e) {
			e.printStackTrace();
			
			if (transaction != null)
				try { transaction.rollback(); }
				catch (HibernateException he) {}
			
			if (dataSet != null && dataSet.hasFile()) {
				new File(dataSet.getFileName()).delete();
			}
			
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new Exception(e);
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
	public Task<DataSetMetaData> getTask() {
		return this.task;
	}

	@Override
	public void setTask(Task<DataSetMetaData> task) {
		this.task = task;
	}
}