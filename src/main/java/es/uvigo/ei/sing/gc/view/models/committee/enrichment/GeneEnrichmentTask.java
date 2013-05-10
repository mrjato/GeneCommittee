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
package es.uvigo.ei.sing.gc.view.models.committee.enrichment;

import java.util.Arrays;
import java.util.Set;

import org.hibernate.Session;

import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.RankedGene;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.view.ZKUtils;

final class GeneEnrichmentTask extends Task<Set<GeneSetMetaData>> {
	private final Integer committeeId;

	public GeneEnrichmentTask(
		String userId,
		Integer committeeId, 
		Set<RankedGene> geneList
	) {
		super(
			GlobalEvents.EVENT_ENRICH_GENE_SET, 
			userId, 
			"Gene set enrichment", 
			Arrays.asList(new GeneEnrichmentSubtask(committeeId, geneList))
		);
		this.committeeId = committeeId;
	}

	public void postFinish() {
		if (this.isAborted()) {
			final Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			final Committee committee = ZKUtils.hLoad(Committee.class, committeeId, session);
			
			committee.returnToState(CommitteeStatus.RANKED_GENES);
			session.update(committee);
			
			session.getTransaction().commit();
		}
	}
}