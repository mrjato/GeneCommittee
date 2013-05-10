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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.RankedGene;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.ws.ArrayOfString;
import es.uvigo.ei.sing.gc.ws.GenebrowserSoap_GenebrowserSoap12_Client;

final class GeneEnrichmentSubtask implements Subtask<Set<GeneSetMetaData>> {
	private static final Map<String, String> SOURCE_TRANSLATION = new HashMap<String, String>();
	
	static {
		SOURCE_TRANSLATION.put("MIM", "OMIM");
		SOURCE_TRANSLATION.put("KO", "KEGG Orthology");
		SOURCE_TRANSLATION.put("GO", "Gene Ontology");
		SOURCE_TRANSLATION.put("Reactome", "Reactome Pathways");
	}
	
	private final Integer committeeId;
	private final Set<RankedGene> geneList;
	private Task<Set<GeneSetMetaData>> task;
	private boolean aborted;
	
	public GeneEnrichmentSubtask(Integer committeeId, Set<RankedGene> geneList) {
		this.committeeId = committeeId;
		this.geneList = geneList;
		this.aborted = false;
	}
	
	@Override
	public Set<GeneSetMetaData> call() throws Exception {
		if (this.aborted) throw new AbortException();
		
		final ArrayOfString result = GeneEnrichmentSubtask.enrich("Homo sapiens (human)", geneList);
	
		if (this.aborted) throw new AbortException();
		
		final List<GeneSetMetaData> geneSets = new ArrayList<GeneSetMetaData>();
		
		int missingCount = 0;
		for (String data : result.getString()) {
			final String[] split = data.split(";");
			
			final String sourceId = split[0];
			final String sourceName = SOURCE_TRANSLATION.containsKey(sourceId) ?
				SOURCE_TRANSLATION.get(sourceId):
				sourceId;
			final String id = split[1];
			final String name = split[2].equals("-.") ? 
				"[" + sourceId + "-" + ++missingCount + "]":
				split[2];
			final String[] genes = split[3].split(",");
			final String pvalueString = split[4].replace(",", ".");
			final double pvalue = Double.parseDouble(pvalueString);
			
			final GeneSetMetaData gs = new GeneSetMetaData();
			gs.setSource(sourceName);
			gs.setGeneSetId(id);
			gs.setName(name);
			gs.setPValue(pvalue);
			
			gs.changeGenes(new HashSet<String>(Arrays.asList(genes)));
			
			geneSets.add(gs);
		}
		
		if (this.aborted) throw new AbortException();
		
		final Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		final Committee committee = (Committee) session.get(Committee.class, committeeId);
		
		for (GeneSetMetaData geneSet : geneSets) {
			session.persist(geneSet);
		}
		session.flush();
		
		committee.getGeneSets().addAll(geneSets);
		session.update(committee);
		
		session.getTransaction().commit();
		session.close();
		
		return committee.getGeneSets();
	}

	@Override
	public Task<Set<GeneSetMetaData>> getTask() {
		return this.task;
	}
	
	@Override
	public void setTask(Task<Set<GeneSetMetaData>> task) {
		this.task = task;
	}

	@Override
	public void abort() {
		this.aborted = true;
	}

	private static ArrayOfString enrich(String specieName, Set<RankedGene> geneList) {
		final StringBuilder sb = new StringBuilder();
		
		for (RankedGene genes : geneList) {
			if (sb.length() > 0) sb.append(',');
			sb.append(genes.getGene());
		}
		
		return GeneEnrichmentSubtask.enrich(specieName, sb.toString());
	}

	private static ArrayOfString enrich(String specieName, String geneList) {
		return GenebrowserSoap_GenebrowserSoap12_Client.query(specieName, geneList);
	}
}