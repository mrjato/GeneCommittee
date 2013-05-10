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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


@Entity(name="Experiment")
public class ExperimentMetaData {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private String name;
	
	@Column(nullable = false)
	private long created = System.currentTimeMillis();
	
	@Column(nullable = true)
	private Long start;
	
	@Column(nullable = true)
	private Long end;
	
	@Column(nullable = false)
	private ExperimentStatus status;
	
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private Committee committee;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="experiment_id")
	private Set<ExpertResult> results;
	
	public ExperimentMetaData() {
	}
	
	public ExperimentMetaData(
		String name, 
		Committee committee
	) {
		super();
		this.name = name;
		this.created = System.currentTimeMillis();
		this.start = null;
		this.end = null;
		this.status = ExperimentStatus.Unscheduled;
		this.committee = committee;
		this.results = new HashSet<ExpertResult>();
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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
	 * @return the created
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(long created) {
		this.created = created;
	}

	/**
	 * @return the start
	 */
	public Long getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Long start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Long getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(Long end) {
		this.end = end;
	}

	/**
	 * @return the status
	 */
	public ExperimentStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(ExperimentStatus status) {
		if (this.status != status) {
			this.status = status;
		}
	}

	/**
	 * @return the committee
	 */
	public Committee getCommittee() {
		return committee;
	}

	/**
	 * @param committee the committee to set
	 */
	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	/**
	 * @return the results
	 */
	public Set<ExpertResult> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(Set<ExpertResult> results) {
		this.results = results;
	}

	public Set<ExpertResult> clearResults() {
		this.start = null;
		this.end = null;
		this.status = ExperimentStatus.Unscheduled;
		
		final Set<ExpertResult> results = new HashSet<ExpertResult>(this.results);
		this.results.clear();
		
		return results;
	}
	
	public void reset() {
		this.getResults().clear();
		this.setStatus(ExperimentStatus.Unscheduled);
	}
	
	public void selectAll() {
		if (this.hasExpertResults()) {
			for (ExpertResult result : this.getResults()) {
				result.setSelected(true);
			}
		}
	}
	
	public void unselectAll() {
		if (this.hasExpertResults()) {
			for (ExpertResult result : this.getResults()) {
				result.setSelected(false);
			}
		}
	}
	
	public boolean hasExpertResults() {
		return this.getResults() != null && !this.getResults().isEmpty();
	}
	
	public boolean hasAnyExpertSelected() {
		if (this.hasExpertResults()) {
			for (ExpertResult result : this.getResults()) {
				if (result.isSelected()) return true;
			}
		}
		
		return false;
	}
	
	public List<ExpertResult> getSelectedExperts() {
		final List<ExpertResult> selected = new ArrayList<ExpertResult>();
		
		if (this.hasExpertResults()) {
			for (ExpertResult result : this.getResults()) {
				if (result.isSelected()) selected.add(result);
			}
		}
		
		return selected;
	}
	
	public int getSelectedCount() {
		int count = 0;
		
		for (ExpertResult result : this.results) {
			if (result.isSelected()) count++;
		}
		
		return count;
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
		if (!(obj instanceof ExperimentMetaData)) {
			return false;
		}
		ExperimentMetaData other = (ExperimentMetaData) obj;
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
