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

import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.routines.EmailValidator;

public final class Validator {
	public static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,18})";
	
	private Validator() {}
	
	public static boolean isGuestEmail(String email) {
		return email.matches("guest_[0123456789]+");
	}
	
	public static boolean isEmail(String email) {
		return EmailValidator.getInstance().isValid(email);
	}
	
	public static boolean isPassword(String password) {
		return Validator.isNotEmpty(password) && password.matches(Validator.PASSWORD_PATTERN);
	}
	
	public static boolean isNotEmpty(String text) {
		return !GenericValidator.isBlankOrNull(text);
	}
}
