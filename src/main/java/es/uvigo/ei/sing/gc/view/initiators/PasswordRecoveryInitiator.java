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

import java.util.Date;
import java.util.Map;

import org.hibernate.Session;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Initiator;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.model.entities.PasswordRecovery;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class PasswordRecoveryInitiator implements Initiator {
	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		final String code = Executions.getCurrent().getParameter("code");
		final String email = Executions.getCurrent().getParameter("email");
		
		if (code == null || email == null) {
			Executions.getCurrent().setAttribute("isValid", false);
			Messagebox.show(
				"Missing or invalid recovery code. Press 'Ok' to return to GeneCommittee.",
				"Password Recovery Error",
				Messagebox.OK,
				Messagebox.ERROR,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Executions.sendRedirect("/index.zul");
					}
				}
			);
		} else {
			final Session session = HibernateUtil.currentSession();
			final PasswordRecovery confirmation = ZKUtils.hGet(PasswordRecovery.class, (String) email, session);
			
			if (confirmation == null || !confirmation.getVerificationCode().equals(code)) {
				Executions.getCurrent().setAttribute("isValid", false);
				Messagebox.show(
					"Missing or invalid recovery code. Press 'Ok' to return to GeneCommittee.",
					"Password Recovery Error",
					Messagebox.OK,
					Messagebox.ERROR,
					new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Executions.sendRedirect("/index.zul");
						}
					}
				);
			} else {
				session.delete(confirmation);
				
				if (lessThan48hours(confirmation.getDate())) {
					Executions.getCurrent().setAttribute("isValid", true);
				} else {
					Executions.getCurrent().setAttribute("isValid", false);
					Messagebox.show(
						"Password recovery has expired. Remember that you have 48 hours to recover your password after your request. Press 'Ok' to return to GeneCommittee.",
						"Password Recovery Expired",
						Messagebox.OK,
						Messagebox.ERROR,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Executions.sendRedirect("/index.zul");
							}
						}
					);
				}
			}
		}
	}
	
	private static boolean lessThan48hours(Date date) {
		final long difference = System.currentTimeMillis() - date.getTime();
		
		return difference <= 48 * 60 * 60 * 1000; // 48 hours in milliseconds
	}
}
