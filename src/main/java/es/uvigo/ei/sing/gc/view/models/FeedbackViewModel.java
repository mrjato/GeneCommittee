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

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.util.Clients;

import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.Mailer;
import es.uvigo.ei.sing.gc.utils.Validator;

public class FeedbackViewModel {
	private String email;
	private String feedback;
	
	@Init
	public void init() {
		final User user = UserViewModel.getUser(false);
		
		this.email = user == null ? "" : user.getEmail();
		this.feedback = "";
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	
	@DependsOn({ "email", "feedback" })
	public boolean isFeedbackOk() {
		return Validator.isEmail(this.getEmail()) &&
				!this.getFeedback().isEmpty();
	}

	@Command
	@NotifyChange({ "email", "feedback" })
	public void sendFeedback() {
		try {
			Mailer.sendFeedback(email, feedback);
			
			Clients.showNotification(
				"Thanks for helping us to improve GeneCommittee!",
				Clients.NOTIFICATION_TYPE_INFO,
				null,
				"middle_center",
				0
			);
		} catch (Exception e) {
			e.printStackTrace();
			Clients.showNotification(
				"Sorry, an error happened while trying to send your feedback. Please, try again later.",
				Clients.NOTIFICATION_TYPE_ERROR,
				null,
				"middle_center",
				0
			);
		} finally {
			this.init();
		}
		
	}
}
