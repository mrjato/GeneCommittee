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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderBy;

import es.uvigo.ei.sing.genomics.genes.GeneSet;

@Entity(name="GeneSet")
public class GeneSetMetaData {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private String geneSetId;
	
	@Basic
	private String name;
	
	@Basic
	private String source;
	
	@Column(precision=10)
	private double pValue;
	
	@Basic
	private int numGenes;
	
	@Basic
	private boolean selected;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@OrderBy("genes")
	@CollectionTable(
		name="Gene",
		joinColumns=@JoinColumn(name="geneSet_id")
	)
	private Set<String> genes;
	
	public Integer getId() {
		return id;
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
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the pvalue
	 */
	public double getPValue() {
		return pValue;
	}

	/**
	 * @param pValue the pvalue to set
	 */
	public void setPValue(double pValue) {
		this.pValue = pValue;
	}
	
	/**
	 * @return the genes
	 */
	public Set<String> getGenes() {
		return genes;
	}

	/**
	 * @param genes the genes to set
	 */
	void setGenes(Set<String> genes) {
		this.genes = genes;
	}
	
	public int getNumGenes() {
		return this.numGenes;
	}
	
	void setNumGenes(int numGenes) {
		this.numGenes = numGenes;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getFormattedPValue() {
		return String.format("%.4f", this.getPValue());
	}

	public void changeGenes(Set<String> genes) {
		if (this.getGenes() != null) {
			this.getGenes().clear();
			this.getGenes().addAll(genes);
		} else {
			this.setGenes(new HashSet<String>(genes));
		}
		
		this.setNumGenes(this.getGenes().size());
	}
	
	public GeneSet toGeneSet() {
		return this.toGeneSet(false);
	}
	
	public GeneSet toGeneSet(boolean useIdAsName) {
		final Set<String> genes = this.getGenes();
		
		return new GeneSet(
			(useIdAsName?this.getGeneSetId():this.getName()), 
			genes.toArray(new String[genes.size()])
		);
	}

	public static class NameSorter implements Comparator<GeneSetMetaData> {
		@Override
		public int compare(GeneSetMetaData o1, GeneSetMetaData o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
	
	public static class SourceSorter implements Comparator<GeneSetMetaData> {
		@Override
		public int compare(GeneSetMetaData o1, GeneSetMetaData o2) {
			return o1.getSource().compareToIgnoreCase(o2.getSource());
		}
	}
	
	public static class PValueSorter implements Comparator<GeneSetMetaData> {
		@Override
		public int compare(GeneSetMetaData o1, GeneSetMetaData o2) {
			return Double.compare(o1.getPValue(), o2.getPValue());
		}
	}
	
	public static class NumGenesSorter implements Comparator<GeneSetMetaData> {
		@Override
		public int compare(GeneSetMetaData o1, GeneSetMetaData o2) {
			return o1.getNumGenes() - o2.getNumGenes();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
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
		if (!(obj instanceof GeneSetMetaData)) {
			return false;
		}
		GeneSetMetaData other = (GeneSetMetaData) obj;
		if (getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getName())
			.append(" (Source: ").append(this.getSource()).append(')');
		return sb.toString();
	}
}
