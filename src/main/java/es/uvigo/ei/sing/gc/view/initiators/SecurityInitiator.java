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
package es.uvigo.ei.sing.gc.view.initiators;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zk.ui.util.SessionCleanup;

import es.uvigo.ei.sing.gc.execution.EventQueueUtils;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.gc.view.ZKUtils.SessionObject;

public class SecurityInitiator implements Initiator, DesktopCleanup, SessionCleanup {
	private final static Set<String> IGNORE_PAGES = new HashSet<String>();
	
	static {
		IGNORE_PAGES.add("/index.zul");
		IGNORE_PAGES.add("/loginConfirmation.zul");
		IGNORE_PAGES.add("/passwordRecovery.zul");
	}
	
	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		final String requestPath = page.getRequestPath();
		
		if (requestPath.equals("/logout.zul")) {
			final Session session = Sessions.getCurrent(false);
			if (session != null) session.invalidate();
			
			Executions.sendRedirect("/index.zul");
		} else 	if (!IGNORE_PAGES.contains(requestPath)) {
			final Session session = Sessions.getCurrent(false);
			if (session == null || !session.hasAttribute("user")) {
				Executions.sendRedirect("/index.zul");
			} else {
				EventQueueUtils.registerUserGlobalListener();
			}
		}
	}
	
	@Override
	public void cleanup(Desktop desktop) throws Exception {
		EventQueueUtils.unregisterUserGlobalListener();
	}
	
	@Override
	public void cleanup(Session sess) throws Exception {
		if (sess.hasAttribute("user")) {
			if (sess.getAttribute("user") instanceof User) {
				User user = (User) sess.getAttribute("user");
				
				if (user.isGuest()) {
					SessionObject<User> sessionObject = null;
					try {
						sessionObject = ZKUtils.hGet(User.class, user.getEmail());
						user = sessionObject.getObject();
						
						sessionObject.getSession().delete(user);
						
						user.deleteDirectories();
						
						sessionObject.getSession().getTransaction().commit();
					} catch (HibernateException he) {
						if (sessionObject != null)
							sessionObject.getSession().getTransaction().rollback();
					} finally {
						if (sessionObject != null && sessionObject.isCurrent()) {
							try {
								sessionObject.getSession().close();
							} catch (HibernateException he) {
								he.printStackTrace();
							}
						}
					}
				}
			}
			sess.removeAttribute("user");
		}
	}
}
