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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import es.uvigo.ei.sing.ensembles.training.IExpert;

@Entity(name="Expert")
public class ExpertMetaData {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Column
	private String geneSetId;
	@Column
	private String geneSetName;
	@Column
	private String classifierName;
	
	@ManyToOne(
		fetch=FetchType.LAZY,
		optional=false
	)
	private GeneSetMetaData geneSet;

	@ManyToOne(
		fetch=FetchType.LAZY,
		optional=false
	)
	private ClassifierBuilderMetaData classifier;
	
	@OneToOne(
		fetch=FetchType.LAZY,
		optional=false
	)
	private ExpertResult result;
	
	@Transient
	private IExpert expert;

	public ExpertMetaData() {}
	
	public ExpertMetaData(ExpertResult result, Committee committee) throws Exception {
		this.geneSetId = result.getGeneSetId();
		this.geneSetName = result.getGeneSetName();
		this.classifierName = result.getClassifierName();
		
		this.geneSet = committee.getGeneSet(this.geneSetId);
		this.classifier = committee.getClassifier(this.classifierName);
		this.result = result;
	}

	public Integer getId() {
		return id;
	}

	public String getGeneSetId() {
		return geneSetId;
	}

	public String getGeneSetName() {
		return geneSetName;
	}

	public String getClassifierName() {
		return classifierName;
	}

	public GeneSetMetaData getGeneSet() {
		return geneSet;
	}

	public ClassifierBuilderMetaData getClassifier() {
		return classifier;
	}

	public ExpertResult getResult() {
		return result;
	}

	public IExpert getExpert() {
		return expert;
	}

	@SuppressWarnings("unused")
	private void setExpertBlob(byte[] data) throws IOException, ClassNotFoundException {
		final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		
		this.expert = (IExpert) ois.readObject();
	}

	@Column(name="expert", length=1048576, nullable=false)
	@Lob
	@Access(AccessType.PROPERTY)
	private byte[] getExpertBlob() throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(this.expert);
		
		return baos.toByteArray();
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
		if (!(obj instanceof ExpertMetaData)) {
			return false;
		}
		ExpertMetaData other = (ExpertMetaData) obj;
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
