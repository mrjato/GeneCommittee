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
/**
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.ei.sing.gc.model.entities;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.SerializationUtils;

import es.uvigo.ei.sing.datatypes.validation.ClassificationPerformance;
import es.uvigo.ei.sing.datatypes.validation.DefaultClassificationPerformance;
import es.uvigo.ei.sing.datatypes.validation.DefaultMultiStepClassificationPerformance;
import es.uvigo.ei.sing.datatypes.validation.MultiStepClassificationPerformance;

/**
 *
 * @author Miguel Reboiro-Jato
 *
 */
@Entity
public class ExpertResult {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column
	private String classifierName;
	@Column
	private String geneSetName;
	@Column
	private String geneSetId;
	@Column(nullable=false)
	private boolean selected;
	
	@Transient
	private ClassificationPerformance performance;
	
	private byte[] abortCause;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="expertResult_id")
	@Column(nullable=true)
	private Set<SampleClassification> samples;
	
	public ExpertResult() {
	}
	
	public ExpertResult(
		String classifierName,
		String geneSetName,
		String geneSetId,
		Throwable abortCause
	) {
		this.abortCause = SerializationUtils.serialize(abortCause);
		this.performance = null;
		
		this.classifierName = classifierName;
		this.geneSetName = geneSetName;
		this.geneSetId = geneSetId;
		this.selected = false;
		
		this.samples = new HashSet<SampleClassification>();
	}

	public ExpertResult(
		String classifierName,
		String geneSetName,
		String geneSetId,
		ClassificationPerformance performance
	) {
		this.abortCause = null;
		this.performance = performance;
		
		this.classifierName = classifierName;
		this.geneSetName = geneSetName;
		this.geneSetId = geneSetId;
		this.selected = false;
		
		this.samples = ExpertResult.extractSampleClassification(performance);
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Set<SampleClassification> getSamples() {
		return samples;
	}

	public void setSamples(Set<SampleClassification> samples) {
		this.samples = samples;
	}
	
	private synchronized void initClassificationPerformance() {
		if (this.performance == null && this.samples != null && !this.samples.isEmpty()) {
			final Set<Object> classes = new HashSet<Object>();
			Boolean multi = null;
			for (SampleClassification sample : this.samples) {
				if (multi == null) {
					multi = sample.isMultiStep();
				} else if (multi != sample.isMultiStep()) {
					throw new IllegalStateException("Different sample types");
				}
				
				classes.add(sample.getRealClass());
			}
			
			
			final Object[] classArray = classes.toArray(new Object[classes.size()]);
			if (multi) {
				final SortedMap<Integer, List<SampleClassification>> sampleMap = 
					new TreeMap<Integer, List<SampleClassification>>();
				
				for (SampleClassification sample : this.samples) {
					if (!sampleMap.containsKey(sample.getStep())) {
						sampleMap.put(sample.getStep(), new LinkedList<SampleClassification>());
					}
					
					sampleMap.get(sample.getStep()).add(sample);
				}
				
				for (Map.Entry<Integer, List<SampleClassification>> entry : sampleMap.entrySet()) {
					if (this.performance == null) {
						this.performance = new DefaultMultiStepClassificationPerformance( 
							ExpertResult.createClassificationPerformance(
								Integer.toString(this.getId()) + "-Step " + entry.getKey(), 
								classArray, 
								entry.getValue()
							)
						);
					} else {
						this.performance = this.performance.merge(
							ExpertResult.createClassificationPerformance(
								Integer.toString(this.getId()) + "-Step " + entry.getKey(), 
								classArray, 
								entry.getValue()
							)
						); 
					}
				}
			} else {
				this.performance = ExpertResult.createClassificationPerformance(
					Integer.toString(this.getId()), 
					classArray, 
					samples
				);
			}
		}
	}
	
	private final static Set<SampleClassification> extractSampleClassification(
		ClassificationPerformance performance
	) {
		final Set<SampleClassification> samples = new HashSet<SampleClassification>();
		
		if (performance instanceof MultiStepClassificationPerformance
				&& ((MultiStepClassificationPerformance) performance).numSteps() > 1) {
			final MultiStepClassificationPerformance msPerformance =
				(MultiStepClassificationPerformance) performance;
			
			for (int i = 0; i < msPerformance.numSteps(); i++) {
				final ClassificationPerformance stepPerformance = 
					msPerformance.getStepPerformance(i);
				
				final Set<SampleClassification> stepSamples = 
					ExpertResult.extractSampleClassification(stepPerformance);
				for (SampleClassification sample : stepSamples) {
					sample.setStep(i);
				}
				
				samples.addAll(stepSamples);
			}
		} else {
			for (Object key : performance.getClassesKeys()) {
				samples.add(
					new SampleClassification(
						key.toString(), 
						performance.getRealClasses().get(key).toString(), 
						performance.getPredictedClasses().get(key).toString()
					)
				);
			}
		}
		
		return samples;
	}
	
	private final static ClassificationPerformance createClassificationPerformance(
		String name,
		Object[] classes,
		Collection<SampleClassification> samples
	) {
		final Map<Object, Object> realClasses = new HashMap<Object, Object>();
		final Map<Object, Object> predictedClasses = new HashMap<Object, Object>();
		
		for (SampleClassification sample : samples) {
			predictedClasses.put(sample.getSampleId(), sample.getPredictedClass());
			realClasses.put(sample.getSampleId(), sample.getRealClass());
		}
		
		return new DefaultClassificationPerformance(
			name, 
			classes, 
			predictedClasses, 
			realClasses
		);
	}
	
	@Transient
	public Throwable getAbortCause() {
		if (this.abortCause == null) {
			return null;
		} else {
			return (Throwable) SerializationUtils.deserialize(this.abortCause);
		}
	}
	
	@Column(name="abortCause", length=1048576, nullable=true)
	@Lob
	@Access(AccessType.PROPERTY)
	private void setSerializedAbortCause(byte[] abortCause) {
		this.abortCause = abortCause;
	}
	
	@SuppressWarnings("unused")
	private byte[] getSerializedAbortCause() {
		return this.abortCause;
	}
	
	/**
	 * @return the classifierName
	 */
	public String getClassifierName() {
		return classifierName;
	}

	/**
	 * @param classifierName the classifierName to set
	 */
	public void setClassifierName(String classifierName) {
		this.classifierName = classifierName;
	}

	/**
	 * @return the geneSetName
	 */
	public String getGeneSetName() {
		return geneSetName;
	}

	/**
	 * @param geneSetName the geneSetName to set
	 */
	public void setGeneSetName(String geneSetName) {
		this.geneSetName = geneSetName;
	}
	
	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the geneSetId
	 */
	public String getGeneSetId() {
		return geneSetId;
	}

	/**
	 * @param geneSetId the geneSetId to set
	 */
	public void setGeneSetId(String geneSetId) {
		this.geneSetId = geneSetId;
	}

	public ClassificationPerformance getClassificationPerformance() {
		if (this.performance == null) {
			this.initClassificationPerformance();
		}
		return this.performance;
	}
	
	public MultiStepClassificationPerformance getMultiStepClassificationPerformance() {
		return (MultiStepClassificationPerformance) this.getClassificationPerformance();
	}

	public boolean isAborted() {
		return this.getAbortCause() != null;
	}
	
	public boolean isPerformance() {
		return this.getClassificationPerformance() != null;
	}
	
	public boolean isSinglePerformance() {
		return this.isPerformance() && 
			!(this.getClassificationPerformance() instanceof MultiStepClassificationPerformance);
	}
	
	public boolean isMultiStepPerformance() {
		return this.isPerformance() && 
			this.getClassificationPerformance() instanceof MultiStepClassificationPerformance;
	}


	public static class ExpertResultMetaDataComparator implements Comparator<ExpertResult> {
		@Override
		public int compare(ExpertResult o1, ExpertResult o2) {
			final int cmpName = o1.toString().compareToIgnoreCase(o2.toString());
			
			if (cmpName == 0) {
				return o1.getId() - o2.getId();
			} else {
				return cmpName;
			}
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
		if (!(obj instanceof ExpertResult)) {
			return false;
		}
		ExpertResult other = (ExpertResult) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.classifierName + " # " + this.geneSetName;
	}
}
