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

import java.io.File;
import java.util.Comparator;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import es.uvigo.ei.sing.datatypes.data.Data;

@Entity(name="PatientSet")
public class PatientSetMetaData {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(nullable=false, length=50)
	private String name;
	
	@Column(nullable=true, length=50)
	private String fileName;
	
	@Basic
	private int samples;
	
	@Basic
	private int variables;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	private User user;
	
	public PatientSetMetaData() {}
	
	PatientSetMetaData(User user, String name, File file, Data data) {
		this(user, name, file, 
			data.getSampleCount(), data.getVariableCount()
		);
	}
	
	PatientSetMetaData(User user, String name, File file, int samples, int variables) {
		this.user = user;
		
		this.name = name;
		this.fileName = file.getName();
		this.samples = samples;
		this.variables = variables;
	}
	
	PatientSetMetaData(User user, String name, String fileName, Data data) {
		this(user, name, fileName, 
			data.getSampleCount(), data.getVariableCount()
		);
	}
	
	PatientSetMetaData(User user, String name, String fileName, int samples, int variables) {
		this.user = user;
		this.name = name;
		this.fileName = fileName;
		this.samples = samples;
		this.variables = variables;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public boolean hasFile() {
		return this.fileName != null && new File(this.fileName).exists();
	}

	public int getSamples() {
		return this.samples;
	}

	public int getVariables() {
		return this.variables;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Data loadData() throws Exception {
		return this.getUser().loadData(this);
	}


	public static class PatientSetMetaDataComparator implements Comparator<PatientSetMetaData> {
		@Override
		public int compare(PatientSetMetaData o1, PatientSetMetaData o2) {
			final int cmpName = o1.getName().compareTo(o2.getName());
			
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
		if (!(obj instanceof PatientSetMetaData)) {
			return false;
		}
		PatientSetMetaData other = (PatientSetMetaData) obj;
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
		return new StringBuilder(this.getName())
			.append(" (").append(this.getSamples()).append(" samples, ")
			.append(this.getVariables()).append(" variables)")
		.toString();
	}
}
