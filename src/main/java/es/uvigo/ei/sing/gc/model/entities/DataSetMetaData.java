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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
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
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderBy;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.utils.DataUtils;

@Entity(name="DataSet")
public class DataSetMetaData {
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
	
	@ElementCollection(fetch=FetchType.EAGER)
	@MapKeyColumn(name="class")
	@Column(name="samples")
	@CollectionTable(
		name="DataSetConditions",
		joinColumns=@JoinColumn(name="dataSet_id")
	)
	private Map<String, Integer> conditions;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@OrderBy("geneNames")
	@CollectionTable(
		name="DataSetGeneNames",
		joinColumns=@JoinColumn(name="dataSet_id")
	)
	private Set<String> geneNames;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	private User user;
	
	public DataSetMetaData() {}
	
	DataSetMetaData(User user, String name, File file, Data data) {
		this(user, name, file, 
			DataUtils.getConditionsCount(data),
			DataUtils.getVariableNames(data, false),
			data.getSampleCount(),
			data.getVariableCount()
		);
	}
	
	DataSetMetaData(
		User user, 
		String name, 
		File file, 
		/*Set<String> classValues*/
		Map<String, Integer> conditionsCount, 
		Collection<String> geneNames, 
		int samples, 
		int variables
	) {
		this.user = user;
		
		this.name = name;
		this.fileName = file.getName();
		this.samples = samples;
		this.variables = variables;
		
		this.conditions = conditionsCount;
		this.geneNames = new HashSet<String>(geneNames);
	}
	
	DataSetMetaData(User user, String name, String fileName, Data data) {
		this(user, name, fileName, 
			DataUtils.getConditionsCount(data),
			DataUtils.getVariableNames(data, false),
			data.getSampleCount(), 
			data.getVariableCount()
		);
	}
	
	DataSetMetaData(
		User user, 
		String name, 
		String fileName, 
		Map<String, Integer> conditionsCount,
		Collection<String> geneNames, 
		int samples, 
		int variables
	) {
		this.user = user;
		this.name = name;
		this.fileName = fileName;
		this.samples = samples;
		this.variables = variables;
		
		this.conditions = conditionsCount;
		this.geneNames = new HashSet<String>(geneNames);
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

	public Set<String> getClassNames() {
//		return this.classNames;
		return this.conditions.keySet();
	}

	public int getNumClasses() {
//		return this.classNames.size();
		return this.conditions.size();
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
	
	public Map<String, Integer> getConditions() {
		return conditions;
	}
	
	public Set<String> getGeneNames() {
		return geneNames;
	}

	public String getClassNamesDescription() {
		final StringBuilder sb = new StringBuilder();
		
		for (String className : this.getClassNames()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(className);
		}
		
		return sb.toString();
	}
	
	public Data loadData() throws Exception {
		return this.getUser().loadData(this);
	}


	public static class DataSetMetaDataComparator implements Comparator<DataSetMetaData> {
		@Override
		public int compare(DataSetMetaData o1, DataSetMetaData o2) {
			final int cmpName = o1.getName().compareToIgnoreCase(o2.getName());
			
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
		if (!(obj instanceof DataSetMetaData)) {
			return false;
		}
		DataSetMetaData other = (DataSetMetaData) obj;
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
			.append(this.getVariables()).append(" variables, ")
			.append(this.getNumClasses()).append(" conditions)")
		.toString();
	}
}
