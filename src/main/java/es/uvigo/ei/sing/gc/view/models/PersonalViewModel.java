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

import org.apache.commons.codec.digest.DigestUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.util.Clients;

import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.Validator;

public class PersonalViewModel {
	private User user;
	
	private String oldPassword;
	private String newPassword;
	private String repeatPassword;
	
	@Init
	public void init() {
		this.user = UserViewModel.getUser();
		
		this.oldPassword = "";
		this.newPassword = "";
		this.repeatPassword = "";
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
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
	
	public boolean isGuest() {
		return this.user.isGuest();
	}

	public boolean isNotifyByEmail() {
		return this.user.isNotify();
	}

	public void setNotifyByEmail(boolean notifyByEmail) {
		if (!this.user.isGuest()) {
			this.user = UserViewModel.getUser();
			this.user.setNotify(notifyByEmail);
		}
	}

	@DependsOn({ "oldPassword", "newPassword", "repeatPassword" })
	public boolean isPasswordOk() {
		return Validator.isPassword(this.oldPassword) &&
				this.user.getPassword().equals(DigestUtils.md5Hex(this.oldPassword)) &&
				Validator.isPassword(this.newPassword) &&
				this.newPassword.equals(this.repeatPassword);
	}
	
	@Command
	@NotifyChange({ "oldPassword", "newPassword", "repeatPassword" })
	public void changePassword(
		@ContextParam(ContextType.BIND_CONTEXT) BindContext context
	) {
		if (this.isPasswordOk()) {
			this.user = UserViewModel.getUser();
			this.user.setPassword(DigestUtils.md5Hex(this.newPassword));
			
			this.oldPassword = "";
			this.newPassword = "";
			this.repeatPassword = "";
			
			Clients.showNotification(
				"Password changed",
				Clients.NOTIFICATION_TYPE_INFO,
				context.getComponent(),
				"end_center",
				5000
			);
		}
	}
}
