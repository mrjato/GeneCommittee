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
package es.uvigo.ei.sing.gc.view.models;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Session;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.model.entities.PasswordRecovery;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.Validator;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class PasswordRecoveryViewModel {
	private boolean isValid;
	private String email;
	
	private String newPassword = "";
	private String repeatPassword = "";
	
	@Init
	public void init() {
		final String code = Executions.getCurrent().getParameter("code");
		this.email = Executions.getCurrent().getParameter("email");
		this.isValid = false;
		
		if (code == null || email == null) {
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
					this.isValid = true;
				} else {
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
	
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

	@DependsOn({ "newPassword", "repeatPassword" })
	public boolean isPasswordOk() {
		return Validator.isPassword(this.newPassword) &&
				this.newPassword.equals(this.repeatPassword);
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	@Command
	public void changePassword() {
		if (this.isPasswordOk()) {
			final Session session = HibernateUtil.currentSession();
			
			final User user = ZKUtils.hLoad(User.class, this.email, session);
			
			user.setPassword(DigestUtils.md5Hex(this.newPassword));
			session.persist(user);
			session.flush();
			
			Messagebox.show(
				"Password successfully changed. Press 'Ok' to go to the login page.",
				"Password Changed",
				Messagebox.OK,
				Messagebox.INFORMATION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Executions.sendRedirect("index.zul");
					}
				}
			);
		}
	}
}
