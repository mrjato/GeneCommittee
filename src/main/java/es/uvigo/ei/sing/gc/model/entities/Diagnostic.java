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

@Entity(name="Diagnostic")
public class Diagnostic {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Committee committee;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private PatientSetMetaData patientData;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="diagnostic_id")
	private Set<ExpertResult> results;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Committee getCommittee() {
		return committee;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public PatientSetMetaData getPatientData() {
		return patientData;
	}

	public void setPatientData(PatientSetMetaData patientData) {
		this.patientData = patientData;
	}

	public Set<ExpertResult> getResults() {
		return results;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (!(obj instanceof Diagnostic)) {
			return false;
		}
		Diagnostic other = (Diagnostic) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
	public static class DiagnosticComparator implements Comparator<Diagnostic> {
		@Override
		public int compare(Diagnostic o1, Diagnostic o2) {
			if (o1 == o2) {
				return 0;
			} else if (o1 == null || o1.getName() == null) {
				return 1;
			} else if (o2 == null || o2.getName() == null) {
				return -1;
			} else {
				final int cmpName = o1.getName().compareTo(o2.getName());
				
				if (cmpName == 0) {
					if (o1.getId() == null && o2.getId() == null) {
						return 0;
					} else 	if (o1.getId() == null) {
						return 1;
					} else if (o2.getId() == null) {
						return -1;
					} else {
						return o1.getId() - o2.getId();
					}
				} else {
					return cmpName;
				}
			}
		}
	}
}
