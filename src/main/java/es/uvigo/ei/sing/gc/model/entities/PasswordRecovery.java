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
package es.uvigo.ei.sing.gc.model.entities;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PasswordRecovery {
	@Id
	@Basic
	private String email;
	
	@Basic
	private String verificationCode;
	
	@Column
	private Date date;
	
	public PasswordRecovery() {
		this.email = null;
		this.date = null;
		this.verificationCode = null;
	}

	public PasswordRecovery(String email) {
		super();
		this.email = email;
		this.date = new Date();
		this.verificationCode = UUID.randomUUID().toString();
	}

	public String getEmail() {
		return email;
	}

	public Date getDate() {
		return date;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PasswordRecovery)) {
			return false;
		}
		PasswordRecovery other = (PasswordRecovery) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		return true;
	}
}
