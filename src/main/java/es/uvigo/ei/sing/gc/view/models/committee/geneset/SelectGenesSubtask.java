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
package es.uvigo.ei.sing.gc.view.models.committee.geneset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import es.uvigo.ei.sing.datatypes.data.Variable;
import es.uvigo.ei.sing.datatypes.featureselection.FeatureSelectionResults;
import es.uvigo.ei.sing.datatypes.featureselection.FeatureSelector;
import es.uvigo.ei.sing.gc.execution.AbortException;
import es.uvigo.ei.sing.gc.execution.Subtask;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.RankedGene;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;

final class SelectGenesSubtask implements Subtask<Set<RankedGene>> {
	private final FeatureSelector fs;
	private final Integer committeeId;
	private Task<Set<RankedGene>> task;
	private boolean aborted;

	public SelectGenesSubtask(FeatureSelector fs, int committeeId) {
		this.fs = fs;
		this.committeeId = committeeId;
	}

	@Override
	public Set<RankedGene> call() throws Exception {
		try {
			if (this.aborted) throw new AbortException();
			
			final FeatureSelectionResults fsResults = fs.obtainFeatureSelection();
			final List<Variable> selectedVariables = fsResults.getSelectedVariables();
			final List<Double> ranking = fsResults.getRanking();
			
			final Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			final Set<RankedGene> genes = new HashSet<RankedGene>();
			
			int i = 0;
			for (Variable selectedVariable : selectedVariables) {
				if (this.aborted) throw new AbortException();
				final RankedGene gene = new RankedGene(
					i+1, 
					selectedVariable.getName(), 
					ranking.get(i++)
				);
				session.persist(gene);
				session.flush();
				genes.add(gene);
			}
			
			if (this.aborted) throw new AbortException();
			
			final Committee committee = (Committee) session.get(Committee.class, committeeId);
			
			committee.getRankedGenes().clear();
			committee.getRankedGenes().addAll(genes);
			session.update(committee);
			
			session.getTransaction().commit();
			session.close();
			return committee.getRankedGenes();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void abort() {
		this.aborted = true;
	}

	@Override
	public Task<Set<RankedGene>> getTask() {
		return this.task;
	}

	@Override
	public void setTask(Task<Set<RankedGene>> task) {
		this.task = task;
	}
}