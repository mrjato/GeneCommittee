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

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

public class HomeViewModel extends NavigationViewModel {
	@Init
	public void init() {
		Session session = Sessions.getCurrent();
		
		if (UserViewModel.getUser(false).isGuest() && !session.hasAttribute("guest_warned")) {
			Clients.showNotification(
				"This is a guest account. All the data and information generated " +
				"will be deleted when the session is closed. If you want to keep " +
				"your data, please, register as a full user.", 
				true
			);
			session.setAttribute("guest_warned", Boolean.TRUE);
		}
	}
}
