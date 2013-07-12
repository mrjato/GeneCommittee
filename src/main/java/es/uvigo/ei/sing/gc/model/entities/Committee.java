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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.utils.DataUtils;
import es.uvigo.ei.sing.genomics.genes.GeneMatrix;
import es.uvigo.ei.sing.genomics.genes.GeneSet;
import es.uvigo.ei.sing.wekabridge.ClassifierBuilder;

@Entity
public class Committee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic(optional=false)
	private String name;
	
	@Basic(optional=false)
	private boolean finished;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private DataSetMetaData dataSet;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="geneSelection_id")
	private GeneSelectionMetaData geneSelection;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="committee_id")
	private Set<RankedGene> rankedGenes;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="committee_id")
	private Set<GeneSetMetaData> geneSets;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="evaluator_id")
	private ExpertsEvaluatorMetaData evaluator;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="committee_id")
	private Set<ClassifierBuilderMetaData> classifiers;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="experiment_id")
	private ExperimentMetaData experiment;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="committee_id")
	private Set<Diagnostic> diagnostics;
	
	public Committee() {
		this.name = "New Committee";
		this.finished = false;
		this.rankedGenes = new HashSet<RankedGene>();
		this.geneSets = new HashSet<GeneSetMetaData>();
		this.geneSelection = null;
		this.classifiers = new HashSet<ClassifierBuilderMetaData>();
	}
	
	public Integer getId() {
		return id;
	}

	public CommitteeStatus getCurrentState() {
		if (this.isFinished()) {
			return CommitteeStatus.FINISHED;
		} else if (this.hasExperiment()) {
			if (this.getExperiment().getStatus() == ExperimentStatus.Finished) {
				if (this.getExperiment().hasAnyExpertSelected()) 
					return CommitteeStatus.EXPERTS_SELECTED;
				else
					return CommitteeStatus.EXPERT_SELECTION;
			} else {
				return CommitteeStatus.EXPERIMENT_EXECUTION;
			}
		} else if (this.hasEvaluator() && this.hasClassifiers()) {
			return CommitteeStatus.EVALUATOR;
		} else if (this.hasSelectedGeneSets()) {
			return CommitteeStatus.GENE_SETS_SELECTED;
		} else if (this.hasGeneSets()) {
			return CommitteeStatus.GENE_ENRICHMENT;
		} else if (this.hasRankedGenes()) {
			return CommitteeStatus.RANKED_GENES;
		} else if (this.hasGeneSelection()) {
			return CommitteeStatus.GENE_SELECTION;
		} else if (this.hasDataSet()) {
			return CommitteeStatus.DATA_SET;
		} else {
			return CommitteeStatus.INIT;
		}
	}
	
	public boolean isCompleted(CommitteeStatus state) {
		final CommitteeStatus currentState = this.getCurrentState();
		if (state == currentState) return true;
		
		final CommitteeStatus[] values = CommitteeStatus.values();
		int currentIndex = -1;
		int stateIndex = -1;
		
		for (int i = 0; i < values.length; i++) {
			if (values[i] == currentState) currentIndex = i;
			if (values[i] == state) stateIndex = i;
			
			if (currentIndex != -1 && stateIndex != -1) break;
		}
		
		return currentIndex >= stateIndex;
	}
	
	public void returnToState(CommitteeStatus state) {
		switch(state) {
		case INIT:
			this.setDataSet(null);
		case DATA_SET:
			this.setGeneSelection(null);
		case GENE_SELECTION:
			this.getRankedGenes().clear();
		case RANKED_GENES:
			this.getGeneSets().clear();
		case GENE_ENRICHMENT:
			this.deselectAllGeneSets();
		case GENE_SETS_SELECTED:
			this.setEvaluator(null);
			this.getClassifiers().clear();
		case EVALUATOR:
			if (this.hasExperiment()) {
				this.getExperiment().setCommittee(null);
				this.setExperiment(null);
			}
		case EXPERIMENT_EXECUTION:
			if (this.hasExperiment()) {
//				this.getExperiment().getResults().clear();
				this.getExperiment().clearResults();
			}
		case EXPERT_SELECTION:
			if (this.hasExperiment())
				this.getExperiment().unselectAll();
		case EXPERTS_SELECTED:
			this.setFinished(false);
		default:
			break;
		}
	}

	public void deselectAllGeneSets() {
		if (this.hasSelectedGeneSets()) {
			for (GeneSetMetaData geneSet : this.getGeneSets()) {
				geneSet.setSelected(false);
			}
		}
	}
	
	public void unselectAllGeneSets() {
		if (this.hasSelectedGeneSets()) {
			for (GeneSetMetaData geneSet : this.getGeneSets()) {
				geneSet.setSelected(true);
			}
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * @param finished the finished to set
	 */
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * @param user the user to set
	 */
	void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the dataSet
	 */
	public DataSetMetaData getDataSet() {
		return dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public void setDataSet(DataSetMetaData dataSet) {
		this.dataSet = dataSet;
	}

	public boolean hasDataSet() {
		return this.getDataSet() != null;
	}
	
	/**
	 * @return the featureSelection
	 */
	public GeneSelectionMetaData getGeneSelection() {
		return geneSelection;
	}

	/**
	 * @param geneSelection the featureSelection to set
	 */
	public void setGeneSelection(GeneSelectionMetaData geneSelection) {
		this.geneSelection = geneSelection;
		
		this.rankedGenes.clear();
	}
	
	public boolean hasGeneSelection() {
		return this.getGeneSelection() != null;
	}
	
	/**
	 * @return the features
	 */
	public Set<RankedGene> getRankedGenes() {
		return this.rankedGenes;
	}

	/**
	 * @param rankedGenes the features to set
	 */
	public void setRankedGenes(Set<RankedGene> rankedGenes) {
		this.rankedGenes = rankedGenes;
	}
	
	public boolean hasRankedGenes() {
		return this.getRankedGenes() != null && !this.getRankedGenes().isEmpty();
	}
	
	/**
	 * @return the geneSets
	 */
	public Set<GeneSetMetaData> getGeneSets() {
		return this.geneSets;
	}
	
	public Set<GeneSetMetaData> getSelectedGeneSets() {
		final Set<GeneSetMetaData> geneSets = new HashSet<GeneSetMetaData>(this.getGeneSets());
		
		final Iterator<GeneSetMetaData> itGeneSets = geneSets.iterator();
		while (itGeneSets.hasNext()) {
			if (!itGeneSets.next().isSelected()) {
				itGeneSets.remove();
			}
		}
		
		return geneSets;
	}
	
	public int getNumSelectedGenes() {
		return this.getSelectedGeneSets().size();
	}
	
	public boolean hasSelectedGeneSets() {
		if (this.hasGeneSets()) {
			for (GeneSetMetaData geneSet : this.getGeneSets()) {
				if (geneSet.isSelected())
					return true;
			}
		}
		
		return false;
	}

	/**
	 * @param geneSets the geneSets to set
	 */
	public void setGeneSets(Set<GeneSetMetaData> geneSets) {
		this.geneSets = geneSets;
	}
	
	public boolean hasGeneSets() {
		return this.getGeneSets() != null && !this.getGeneSets().isEmpty();
	}

	public boolean hasExpertResults() {
		return this.hasExperiment() && this.getExperiment().hasExpertResults();
	}
	
	public int getNumExperts() {
		return this.experiment.getSelectedCount();
	}
	
	/**
	 * @return the evaluator
	 */
	public ExpertsEvaluatorMetaData getEvaluator() {
		return evaluator;
	}

	/**
	 * @param evaluator the evaluator to set
	 */
	public void setEvaluator(ExpertsEvaluatorMetaData evaluator) {
		this.evaluator = evaluator;
	}

	public boolean hasEvaluator() {
		return this.getEvaluator() != null;
	}

	/**
	 * @return the classifiers
	 */
	public Set<ClassifierBuilderMetaData> getClassifiers() {
		return this.classifiers;
	}

	/**
	 * @param classifiers the classifiers to set
	 */
	public void setClassifiers(Set<ClassifierBuilderMetaData> classifiers) {
		this.classifiers = classifiers;
	}
	
	public boolean hasClassifiers() {
		return this.getClassifiers() != null && !this.getClassifiers().isEmpty();
	}
	
	/**
	 * @return the experiment
	 */
	public ExperimentMetaData getExperiment() {
		return experiment;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(ExperimentMetaData experiment) {
		if (this.experiment != null)
			this.experiment.setCommittee(null);
		
		this.experiment = experiment;
	}

	public boolean hasExperiment() {
		return this.experiment != null;
	}
	
	public int getSelectedCount() {
		if (this.hasExperiment())
			return this.getExperiment().getSelectedCount();
		else return 0;
	}
	
	public Set<Diagnostic> getDiagnostics() {
		return diagnostics;
	}
	
	public boolean hasDiagnostics() {
		return this.diagnostics != null && !this.diagnostics.isEmpty();
	}

	@Override
	public String toString() {
		return this.name;
	}

	public ClassifierBuilder[] getClassifierBuilders() throws Exception {
		final ClassifierBuilder[] builders = new ClassifierBuilder[this.classifiers.size()];
		
		int i = 0;
		for (ClassifierBuilderMetaData metadata : this.getClassifiers()) {
			builders[i++] = metadata.createNewBuilder();
		}
		
		return builders;
	}
	
	public GeneSetMetaData getGeneSet(String geneSetId) {
		for (GeneSetMetaData gsmd : this.getGeneSets()) {
			if (gsmd.getGeneSetId().equals(geneSetId)) {
				return gsmd;
			}
		}
		
		return null;
	}
	
	public ClassifierBuilderMetaData getClassifier(String name) {
		for (ClassifierBuilderMetaData metadata : this.getClassifiers()) {
			if (metadata.getName().equals(name)) {
				return metadata;
			}
		}
		
		return null;
	}
	
	public static class CommitteeComparator implements Comparator<Committee> {
		@Override
		public int compare(Committee o1, Committee o2) {
			if (o1.getName() == null && o2.getName() == null) {
				return o1.getId() - o2.getId();
			} else if (o1.getName() == null) {
				return -1;
			} else if (o2.getName() == null) {
				return 1;
			} else {
				final int cmpName = o1.getName().compareTo(o2.getName());
				
				if (cmpName == 0) {
					return o1.getId() - o2.getId();
				} else {
					return cmpName;
				}
			}
		}
	}

	public GeneMatrix getGeneMatrix() {
		return this.getGeneMatrix(false);
	}
	
	public GeneMatrix getGeneMatrix(boolean full) {
		final List<GeneSet> geneSets = new ArrayList<GeneSet>();
		
		for (GeneSetMetaData geneSet : this.getGeneSets()) {
			if (full || geneSet.isSelected())
				geneSets.add(geneSet.toGeneSet(true));
		}
		
		return new GeneMatrix(geneSets);
	}
	
	public Compatibility checkCompatibility(Data data) throws Exception {
		return this.checkCompatibility(this.getExpertCompatibility(data));
	}
	
	public Compatibility checkCompatibility(
		Map<Integer, ExpertIncompatibility> expertCompatibility
	) {
		if (expertCompatibility.isEmpty()) {
			return Compatibility.FULL;
		} else if (expertCompatibility.size() == this.getExperiment().getSelectedCount()) {
			return Compatibility.NONE;
		} else {
			return Compatibility.INCOMPLETE;
		}
	}
	
	public Map<Integer, ExpertIncompatibility> getExpertCompatibility(Data data) throws Exception {
		if (!this.isFinished()) {
			throw new IllegalStateException("Committee must be finished before invoking this method");
		}
		
		final Map<Integer, ExpertIncompatibility> incompatibilities = 
			new HashMap<Integer, ExpertIncompatibility>();
		
		final Set<String> patientGenes = new HashSet<String>(
			DataUtils.getVariableNames(data, false)
		);
		final Set<String> dataSetGenes = new HashSet<String>(
			this.getDataSet().getGeneNames()
		);
		
		if (!patientGenes.containsAll(dataSetGenes)) {
			final ExperimentMetaData experiment = this.getExperiment();
			for (ExpertResult expert : experiment.getSelectedExperts()) {
				final GeneSetMetaData geneSet = this.getGeneSet(expert.getGeneSetId());
				final Set<String> genes = new HashSet<String>(geneSet.getGenes());
				
				genes.retainAll(dataSetGenes);
				genes.removeAll(patientGenes);
				
				if (!genes.isEmpty()) {
					incompatibilities.put(
						expert.getId(),
						new ExpertIncompatibility(
							expert.getId(), 
							expert.toString(), 
							geneSet.getId(), 
							geneSet.getName(), 
							new HashSet<String>(geneSet.getGenes()), 
							genes
						)
					);
				}
			}
		}
			
		return incompatibilities;
	}
	
	public static enum Compatibility {
		FULL, INCOMPLETE, NONE;
	}
	
	public static class ExpertIncompatibility {
		private final Integer expertId;
		private final String expertName;
		private final Integer geneSetId;
		private final String geneSetName;
		private final Set<String> geneSetGenes;
		private final Set<String> missingGenes;
		
		public ExpertIncompatibility(
			Integer expertId, 
			String expertName, 
			Integer geneSetId,
			String geneSetName, 
			Set<String> geneSetGenes,
			Set<String> missingGenes
		) {
			super();
			this.expertId = expertId;
			this.expertName = expertName;
			this.geneSetId = geneSetId;
			this.geneSetName = geneSetName;
			this.geneSetGenes = geneSetGenes;
			this.missingGenes = missingGenes;
		}
		
		public Integer getExpertId() {
			return expertId;
		}

		public String getExpertName() {
			return expertName;
		}

		public Integer getGeneSetId() {
			return geneSetId;
		}

		public String getGeneSetName() {
			return geneSetName;
		}

		public Set<String> getGeneSetGenes() {
			return geneSetGenes;
		}

		public Set<String> getMissingGenes() {
			return missingGenes;
		}
		
		public String getGeneSetDescription() {
			final StringBuilder sb = new StringBuilder();
			
			for (String geneName : this.getGeneSetGenes()) {
				if (sb.length() > 0) sb.append(", ");
				sb.append(geneName);
			}
			
			return sb.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		if (!(obj instanceof Committee)) {
			return false;
		}
		Committee other = (Committee) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
}
