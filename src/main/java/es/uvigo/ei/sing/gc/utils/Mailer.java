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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.HibernateException;

import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.gc.view.ZKUtils.SessionObject;

public class Mailer {
	public final static void send(String from, String to, String subject, String content, String contentType)
	throws AddressException, MessagingException {
		final Session session = Configuration.getInstance().getMailSession();
		
		final MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setContent(content, contentType);
		
		Transport.send(message);
	}
	
	public final static void sendFeedback(String from, String feedback) 
	throws AddressException, MessagingException {
		Mailer.send(
			Configuration.getInstance().getEmailAccount(), 
			Configuration.getInstance().getEmailAccount(), 
			"Feedback from " + from, 
			feedback, 
			"text/plain"
		);
	}
	
	public final static void checkAndSendFinishNotification(String to, String content) 
	throws AddressException, MessagingException {
		final SessionObject<User> sessionObject = ZKUtils.hLoad(User.class, to, true);
		final User user = sessionObject.getObject();
		
		if (user.isNotify()) {
			Mailer.sendFinishNotification(to, content);
		}
		
		if (sessionObject.getSession().isOpen()) {
			try { sessionObject.getSession().close(); }
			catch (HibernateException he) {}
		}
	}
	
	public final static void sendFinishNotification(String to, String content)
	throws AddressException, MessagingException {
		Mailer.send(
			Configuration.getInstance().getEmailAccount(), 
			to, 
			"GeneCommittee Task Finished", 
			content, 
			"text/plain"
		);
	}
	
	public final static void sendConfirmPassword(String to, String url)
	throws AddressException, MessagingException {
		final String content = "In order to complete your GeneCommittee registration go to the following URL: " + url;
		
		Mailer.send(
			Configuration.getInstance().getEmailAccount(), 
			to, 
			"GeneCommittee Login Confirmation", 
			content, 
			"text/plain"
		);
	}
	
	public final static void sendForgot(String to, String url)
	throws AddressException, MessagingException {
        final String content = 
			 "<p>Hi there, </br>" +
			 "There was recently a request to change the password on your account." +
			 "If you requested this password change, please set a new password by following the link below:</br>" +
			 "<a href=\"" + url + "\">" + url + "</a><br/>" +
			 "If you don't want to change your password, ignore this message.<br/>" +
			 "Thanks for using WhichModel </p>" +	            				 
			 "<br/>" +
			 "<p>This is an automated response. Please do not reply this e-mail.</p>";

		Mailer.send(
			Configuration.getInstance().getEmailAccount(), 
			to, 
			"GeneCommittee Password Recovery", 
			content, 
			"text/html"
		);
	}
}
