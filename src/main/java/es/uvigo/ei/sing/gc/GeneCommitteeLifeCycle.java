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
package es.uvigo.ei.sing.gc;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import es.uvigo.ei.sing.datatypes.data.DataFactory.ReplicationMode;
import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;

public class GeneCommitteeLifeCycle implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.setProperty("sing.data", "disk");
		System.setProperty("sing.data.stringbytes", "124");
		
		final File usersDir = Configuration.getInstance().getUsersDirectory();
		final File guestsDir = Configuration.getInstance().getGuestsDirectory();
		final File tmpDir = Configuration.getInstance().getTmpDirectory();
		
		if (!usersDir.isDirectory())
			if (usersDir.mkdirs())
				System.out.println("usersDir");
			else System.err.println("UsersDir: " + usersDir);
		if (!guestsDir.isDirectory())
			if (guestsDir.mkdirs())
				System.out.println("guestsDir");
			else System.err.println("GuestsDir: " + guestsDir);
		if (!tmpDir.isDirectory())
			if (tmpDir.mkdirs())
				System.out.println("tmpDir");
			else System.err.println("TmpDir: " + tmpDir);
		

    	ConvertUtils.register(new AbstractConverter() {
			@Override
			protected Class<ReplicationMode> getDefaultType() {
				return ReplicationMode.class;
			}
			
			@Override
			@SuppressWarnings("rawtypes") 
			protected Object convertToType(Class type, Object value) throws Throwable {
				if (value instanceof String) {
					return ReplicationMode.valueOf((String) value);
				} else {
					throw new IllegalArgumentException("value must be a String");
				}
			}
		}, ReplicationMode.class);
    	
    	
    	this.deleteGuestUsers();
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		System.out.println("Shutting down execution engine...");
		ExecutionEngine.getSingleton().shutdown();
		
		this.deleteGuestUsers();
	}
	
	private void deleteGuestUsers() {
		Session session = null;
    	try {
    		session = HibernateUtil.currentSession();
    		session.beginTransaction();
	    	
	    	@SuppressWarnings("unchecked")
			final List<User> users = (List<User>) session.createCriteria(User.class).list();
	    	for (User user : users) {
	    		if (user.isGuest()) {
	    			session.delete(user);
	    			user.deleteDirectories();
	    		}
	    	}
	    	
	    	session.getTransaction().commit();
    	} catch (Exception e) {
    		e.printStackTrace();
    		
    		if (session != null) {
    			try {
    				session.getTransaction().rollback();
    			} catch (HibernateException he) {
    				he.printStackTrace();
    			}
    		}
    	}
    }
}
