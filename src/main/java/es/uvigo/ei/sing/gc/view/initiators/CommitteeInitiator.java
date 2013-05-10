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

import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;

import es.uvigo.ei.sing.gc.view.models.UserViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class CommitteeInitiator implements Initiator {
	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		if (UserViewModel.isUserLogged()) {
			final String requestPath = page.getRequestPath();
			
			if ((requestPath.equals(CommitteeViewModel.GENE_SET_PATH) && 
				 !CommitteeViewModel.isStepCompleted(CommitteeViewModel.DATA_SET_STEP)) ||
				(requestPath.equals(CommitteeViewModel.ENRICHMENT_PATH) && 
				 !CommitteeViewModel.isStepCompleted(CommitteeViewModel.GENE_SET_STEP)) ||
				(requestPath.equals(CommitteeViewModel.CLASSIFIERS_PATH) && 
				 !CommitteeViewModel.isStepCompleted(CommitteeViewModel.ENRICHMENT_STEP)) ||
				(requestPath.equals(CommitteeViewModel.EVALUATION_PATH) && 
				 !CommitteeViewModel.isStepCompleted(CommitteeViewModel.CLASSIFIERS_STEP)) ||
				(requestPath.equals(CommitteeViewModel.SUMMARY_PATH) && 
				 !CommitteeViewModel.isStepCompleted(CommitteeViewModel.EVALUATION_STEP))
			) {
				Executions.sendRedirect(CommitteeViewModel.getCurrentURL());
			}
		}
	}
}
