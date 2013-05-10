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
package es.uvigo.ei.sing.gc.utils;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

@SuppressWarnings("deprecation")
public class HibernateUtil {
	/**
	 * Get the singleton hibernate Session Factory.
	 */
	public static SessionFactory getSessionFactory() {
		return org.zkoss.zkplus.hibernate.HibernateUtil.getSessionFactory();
	}
	
	/**
	 * Wrapping HibernateUtil.getSessionFactory().getCurrentSession() into a simple API.
	 */
	public static Session currentSession() throws HibernateException {
		return org.zkoss.zkplus.hibernate.HibernateUtil.currentSession();
	}
	
	/**
	 * Wrapping HibernateUtil.getSessionFactory().getCurrentSession().close() into a simple API.
	 */
	public static void closeSession() throws HibernateException {
		org.zkoss.zkplus.hibernate.HibernateUtil.closeSession();
	}
}
