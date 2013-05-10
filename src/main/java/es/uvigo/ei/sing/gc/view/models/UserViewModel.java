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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.commons.codec.digest.DigestUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.model.entities.PasswordRecovery;
import es.uvigo.ei.sing.gc.model.entities.SingUpConfirmation;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.utils.Mailer;
import es.uvigo.ei.sing.gc.utils.Validator;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.wekabridge.io.operations.LoadClassificationData;

public class UserViewModel {
	private User user;
	private String loginEmail = "";
	private String loginPassword = "";
	
	private String singUpEmail = "";
	private String singUpPassword = "";
	private String singUpRepeatPassword = "";
	
	public static User getUser() {
		return UserViewModel.getUser(true);
	}
	
	public static User getUser(boolean bind) {
		final Session session = Sessions.getCurrent(false);
		
		if (session != null && session.hasAttribute("user")) {
			User user = (User) session.getAttribute("user");
			
			if (bind) {
				user = (User) HibernateUtil.getSessionFactory().getCurrentSession()
					.load(User.class, user.getEmail());
				session.setAttribute("user", user);
			}
			
			return user;
		} else {
			return null;
		}
	}
	
	public static boolean isUserLogged() {
		return UserViewModel.getUser(false) != null;
	}
	
	@Init
	public void init() {
		this.user = UserViewModel.getUser();
	}
	
	public boolean hasUser() {
		return this.user != null;
	}
	
	public void setLoginEmail(String loginUser) {
		this.loginEmail = loginUser;
	}
	
	public String getLoginEmail() {
		return loginEmail;
	}
	
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	
	public String getLoginPassword() {
		return loginPassword;
	}
	
	public String getSingUpEmail() {
		return singUpEmail;
	}

	public void setSingUpEmail(String singUpEmail) {
		this.singUpEmail = singUpEmail;
	}

	public String getSingUpPassword() {
		return singUpPassword;
	}

	public void setSingUpPassword(String singUpPassword) {
		this.singUpPassword = singUpPassword;
	}

	public String getSingUpRepeatPassword() {
		return singUpRepeatPassword;
	}

	public void setSingUpRepeatPassword(String singUpRepeatPassword) {
		this.singUpRepeatPassword = singUpRepeatPassword;
	}
	
	@DependsOn({ "singUpEmail", "singUpPassword", "singUpRepeatPassword" })
	public boolean isSingUpOk() {
		return Validator.isEmail(this.singUpEmail) &&
				Validator.isPassword(this.singUpPassword) &&
				this.singUpPassword.equals(this.singUpRepeatPassword);
	}

	@Command
	public void checkLogin() {
		org.hibernate.Session session = HibernateUtil.currentSession();
		
		final Object value = session.get(User.class, this.loginEmail);
		if (value instanceof User) {
			final User user = (User) value;
			
			if (Validator.isPassword(this.loginPassword) && 
				user.getPassword().equals(DigestUtils.md5Hex(this.loginPassword))
			) {
				final Session webSession = Sessions.getCurrent();
				webSession.setAttribute("user", user);
				this.user = user;
				
				Executions.getCurrent().sendRedirect("home.zul");
			} else {
				Messagebox.show("Incorrect password.", "Invalid login", Messagebox.OK, Messagebox.ERROR);
			}
		} else {
			Messagebox.show("User does not exists.", "Invalid login", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	@Command
	public void guestLogin() throws FileNotFoundException, Exception {
		org.hibernate.Session session = HibernateUtil.currentSession();
		
		User user = new User(
			"guest_" + System.currentTimeMillis(),
			"",
			false
		);

		session.persist(user);
		session.flush();
		
		final File[] sampleFiles = Configuration.getInstance().getSamplesFiles();
		final String[] sampleNames = Configuration.getInstance().getSamplesNames();
		
		if (sampleFiles.length != sampleNames.length) {
			throw new IllegalStateException("Sample names and files do not have the same length. Please, review the web.xml file");
		}
		
		for (int i = 0; i < sampleFiles.length; i++) {
			final Data data = LoadClassificationData.loadData(
				new FileReader(sampleFiles[i]),
				sampleNames[i], null, null, true, null
			);
			
			session.persist(user.addDataSet(data, sampleFiles[i]));
		}
		
		session.flush();
		
		final Session webSession = Sessions.getCurrent();
		webSession.setAttribute("user", user);
		this.user = user;
		Executions.getCurrent().sendRedirect("home.zul");
	}
	
	@Command
	public void passwordForgot(
		@ContextParam(ContextType.COMPONENT) Component component
	) {
		if (Validator.isEmail(this.loginEmail)) {
			final String email = this.loginEmail;
			
			Messagebox.show(
				"An email with the information to recover your password will be sent to your email direction. Do you wish to continue?",
				"Password Recovery",
				Messagebox.YES | Messagebox.NO,
				Messagebox.INFORMATION,
				new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (((Integer) event.getData()).intValue() == Messagebox.YES) {
							final org.hibernate.Session session = HibernateUtil.currentSession();
							
							final PasswordRecovery recovery = new PasswordRecovery(email);
							session.persist(recovery);
							
							try {
								Mailer.sendForgot(email, Configuration.getInstance().getServer() + "passwordRecovery.zul?email=" + email + "&code=" + recovery.getVerificationCode());

								Messagebox.show(
									"An email with a password recovery link has been sent to you. Please, check your email for further instructions.", 
									"Password Recovery", 
									Messagebox.OK, 
									Messagebox.INFORMATION
								);
							} catch (Exception e) {
								Messagebox.show(
									"Sorry, an error happened while trying to sending you a recovery email. Please, try again later.", 
									"Password Recovery Error", 
									Messagebox.OK, 
									Messagebox.ERROR
								);
								
								session.delete(recovery);
							}
						}
					}
				}
			);
		} else {
			Clients.showNotification(
				"Please, introduce a valid email direction before requesting password recovery.",
				Clients.NOTIFICATION_TYPE_WARNING,
				component,
				"after_center",
				0
			);
		}
	}
	
	@Command
	@NotifyChange({ "singUpEmail", "singUpPassword", "singUpRepeatPassword"})
	public void singUp() {
		org.hibernate.Session session = HibernateUtil.currentSession();
		
		final User value = ZKUtils.hGet(User.class, this.singUpEmail, session);
		
		if (value == null) {
			if (this.singUpPassword.equals(this.singUpRepeatPassword)) {
				final SingUpConfirmation confirmation = new SingUpConfirmation(
					this.singUpEmail,
					DigestUtils.md5Hex(this.singUpPassword)
				);
				
				session.persist(confirmation);
				session.flush();
				
				try {
					Mailer.sendConfirmPassword(
						this.singUpEmail, 
						String.format("%sloginConfirmation.zul?email=%s&code=%s",
							Configuration.getInstance().getServer(),
							this.singUpEmail,
							confirmation.getVerificationCode()
						)
					);

					Messagebox.show(
						"An email with a confirmation link has been sent to you. Please, in order to continue, check your email.", 
						"Confirmation Sent", 
						Messagebox.OK, 
						Messagebox.INFORMATION
					);
					
					this.singUpEmail = "";
					this.singUpPassword = "";
					this.singUpRepeatPassword = "";
				} catch (Exception e) {
					Messagebox.show(
						"Sorry, an error happened while trying to singing you up. Please, try again later.", 
						"Sing Up Error", 
						Messagebox.OK, 
						Messagebox.ERROR
					);
					
					session.delete(confirmation);
				}
			} else {
				Messagebox.show(
					"You must introduce the same password in both password fields.", 
					"Password does not match", 
					Messagebox.OK, 
					Messagebox.ERROR
				);
			}
		} else {
			Messagebox.show(
				"User already exists. If you forgot your password press 'I forgot my password' in the login box.", 
				"User already exists", 
				Messagebox.OK, 
				Messagebox.ERROR
			);
		}
	}
}
