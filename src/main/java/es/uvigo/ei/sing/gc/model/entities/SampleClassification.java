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

import java.util.Comparator;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 * @author Miguel Reboiro-Jato
 *
 */
@Entity
public class SampleClassification {
	@Id
	@GeneratedValue
	private Integer id;

	@Basic
	private String sampleId;
	@Basic
	private String realClass;
	@Basic
	private String predictedClass;
	@Basic
	private Integer step;
	
	public SampleClassification() {
	}
	
	public SampleClassification(String sampleId, String realClass,
			String predictedClass) {
		this(sampleId, realClass, predictedClass, null);
	}
	
	public SampleClassification(String sampleId, String realClass,
			String predictedClass, Integer step) {
		this.sampleId = sampleId;
		this.realClass = realClass;
		this.predictedClass = predictedClass;
		this.step = step;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @return the sampleId
	 */
	public String getSampleId() {
		return sampleId;
	}
	/**
	 * @param sampleId the sampleId to set
	 */
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	/**
	 * @return the realClass
	 */
	public String getRealClass() {
		return realClass;
	}
	/**
	 * @param realClass the realClass to set
	 */
	public void setRealClass(String realClass) {
		this.realClass = realClass;
	}
	/**
	 * @return the predictedClass
	 */
	public String getPredictedClass() {
		return predictedClass;
	}
	/**
	 * @param predictedClass the predictedClass to set
	 */
	public void setPredictedClass(String predictedClass) {
		this.predictedClass = predictedClass;
	}
	/**
	 * @return the step
	 */
	public Integer getStep() {
		return step;
	}
	/**
	 * @param step the step to set
	 */
	public void setStep(Integer step) {
		this.step = step;
	}
	
	public boolean isMultiStep() {
		return this.step != null;
	}
	
	public static class SampleClassificationComparator implements Comparator<SampleClassification> {
		@Override
		public int compare(SampleClassification o1, SampleClassification o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null || o1.getSampleId() == null) {
				return 1;
			} else if (o2 == null || o2.getSampleId() == null) {
				return -1;
			} else {
				return o1.getSampleId().compareTo(o2.getSampleId());
			}
		}
	}
}
