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
public class RankedGene {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private int position;
	
	@Basic
	private String gene;
	
	@Basic
	private double ranking;
	
	public RankedGene() {}
	
	public RankedGene(int position, String gene, double ranking) {
		super();
		this.position = position;
		this.gene = gene;
		this.ranking = ranking;
	}
	
	public Integer getId() {
		return id;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return the name
	 */
	public String getGene() {
		return gene;
	}

	/**
	 * @return the ranking
	 */
	public double getRanking() {
		return ranking;
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
		if (!(obj instanceof RankedGene)) {
			return false;
		}
		RankedGene other = (RankedGene) obj;
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
		return new StringBuffer(this.gene).append(" - ")
			.append(this.position).append('/')
			.append(this.ranking)
		.toString();
	}
	

	public static class PositionSorter implements Comparator<RankedGene> {
		private boolean isAscending;
		
		public PositionSorter(boolean isAscending) {
			this.isAscending = isAscending;
		}
		
		@Override
		public int compare(RankedGene o1, RankedGene o2) {
			return (this.isAscending?1:-1) * (o1.getPosition() - o2.getPosition());
		}
	}
	
	public static class GeneSorter implements Comparator<RankedGene> {
		private boolean isAscending;
		
		public GeneSorter(boolean isAscending) {
			this.isAscending = isAscending;
		}

		@Override
		public int compare(RankedGene o1, RankedGene o2) {
			final int cmp = o1.getGene().compareTo(o2.getGene());
			
			if (cmp == 0) return cmp;
			else return cmp * ((this.isAscending)?1:-1);
		}
	}
	
	public static class RankingSorter implements Comparator<RankedGene> {
		private boolean isAscending;
		
		public RankingSorter(boolean isAscending) {
			this.isAscending = isAscending;
		}
		
		@Override
		public int compare(RankedGene o1, RankedGene o2) {
			final int cmp = Double.compare(o1.getRanking(), o2.getRanking());
			
			if (cmp == 0) return cmp;
			else return cmp * ((this.isAscending)?1:-1);
		}
	}
}
