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

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.util.Clients;

public class EmailValidator extends AbstractValidator {
	@Override
	public void validate(ValidationContext ctx) {
		final String email = ctx.getProperty().getValue().toString();
		
		if (!Validator.isEmail(email)) {
			Clients.showNotification(
				"Introduced email is not a valid email direction. Please, introduce a valid email direction.", 
				Clients.NOTIFICATION_TYPE_ERROR, 
				ctx.getBindContext().getComponent(), 
				"end_center", 
				0
			);
			this.addInvalidMessage(ctx, "Introduced email is not a valid email direction. Please, introduce a valid email direction.");
		}
	}
}