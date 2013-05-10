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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.apache.commons.io.FileUtils;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.gc.utils.Validator;
import es.uvigo.ei.sing.wekabridge.io.operations.LoadClassificationData;
import es.uvigo.ei.sing.wekabridge.io.operations.SaveClassificationData;

/**
 * User entity.
 * 
 * @author Miguel Reboiro-Jato
 */
@Entity
public class User {
	@Id
	@Column(nullable = false, unique=true, length=50)
	private String email;
	
	@Column(nullable = false, length=32)
	private String password;
	
	@Basic(fetch=FetchType.EAGER)
	private boolean notify;
	
	@Version
	private long version;
	
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	private Set<Committee> committees;
	
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	private Set<DataSetMetaData> dataSets;
	
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	private Set<PatientSetMetaData> patientSets;
	
	public User() {
		this.committees = new HashSet<Committee>();
		this.dataSets = new HashSet<DataSetMetaData>();
		this.patientSets = new HashSet<PatientSetMetaData>();
	}
	
	public User(String email, String password, boolean notify) {
		this.email = email;
		this.password = password;
		this.notify = notify;
		this.committees = new HashSet<Committee>();
		this.dataSets = new HashSet<DataSetMetaData>();
		this.patientSets = new HashSet<PatientSetMetaData>();
	}

	public String getEmail() {
		return this.email;
	}
	
	void setEmail(String email) {
		this.email = email;
	}

	public void changeEmail(String email) throws IOException, NullPointerException, IllegalStateException {
		if (email == null)
			throw new NullPointerException("email");
		if (this.isGuest())
			throw new IllegalStateException("guest users email can not be changed");
		
		// If it is a directory change.
		if (this.email != null) {
			FileUtils.copyDirectory(
				this.getUserDirectory(), 
				User.getUserDirectory(email), 
				true
			);
		}
		
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}
	
	public Set<DataSetMetaData> getDataSets() {
		return dataSets;
	}
	
	public Set<PatientSetMetaData> getPatientSets() {
		return patientSets;
	}
	
	public Set<Committee> getCommittees() {
		return committees;
	}
	
	public void addCommittee(Committee committee) {
		this.getCommittees().add(committee);
		committee.setUser(this);
	}
	
	public boolean removeCommittee(Committee committee) {
		if (this.getCommittees().remove(committee)) {
			committee.setUser(null);
			return true;
		} else {
			return false;
		}
	}
	
	public Committee getActiveCommittee() {
		for (Committee committee : this.getCommittees()) {
			if (!committee.isFinished())
				return committee;
		}
		
		return null;
	}
	
	public DataSetMetaData addDataSet(Data data, File dataFile) throws IOException {
		final String fileName = UUID.randomUUID().toString() + ".csv";
		final File file = new File(this.getDataSetsDirectory(), fileName);
		
		FileUtils.copyFile(dataFile, file);
		
		final DataSetMetaData metadata = new DataSetMetaData(this, data.getName(), file, data);
		
		this.getDataSets().add(metadata);
		
		return metadata;
	}
	
	public DataSetMetaData addDataSet(Data data) throws FileNotFoundException {
		final String fileName = UUID.randomUUID().toString() + ".csv";
		final File file = new File(this.getDataSetsDirectory(), fileName);
		
		SaveClassificationData.saveData(file, data);
		
		final DataSetMetaData metadata = new DataSetMetaData(this, data.getName(), file, data);
		
		this.getDataSets().add(metadata);
		
		return metadata;
	}
	
	public boolean removeDataSet(DataSetMetaData dataSet) {
		if (this.getDataSets().remove(dataSet)) {
			final File dataSetFile = new File(this.getDataSetsDirectory(), dataSet.getFileName());
			
			dataSetFile.delete();
			
			return true;
		} else {
			return false;
		}
	}
	
	public PatientSetMetaData addPatientSet(Data data, File dataFile) throws IOException {
		final String fileName = UUID.randomUUID().toString() + ".csv";
		final File file = new File(this.getPatientSetsDirectory(), fileName);
		
		FileUtils.copyFile(dataFile, file);
		
		final PatientSetMetaData metadata = new PatientSetMetaData(this, data.getName(), file, data);
		
		this.getPatientSets().add(metadata);
		
		return metadata;
	}
	
	public PatientSetMetaData addPatientSet(Data data) throws FileNotFoundException {
		final String fileName = UUID.randomUUID().toString() + ".csv";
		final File file = new File(this.getPatientSetsDirectory(), fileName);
		
		SaveClassificationData.saveData(file, data);
		
		final PatientSetMetaData metadata = new PatientSetMetaData(this, data.getName(), file, data);
		
		this.getPatientSets().add(metadata);
		
		return metadata;
	}
	
	public boolean removePatientSet(PatientSetMetaData PatientSet) {
		if (this.getPatientSets().remove(PatientSet)) {
			final File patientSetFile = new File(this.getPatientSetsDirectory(), PatientSet.getFileName());
			
			patientSetFile.delete();
			
			return true;
		} else {
			return false;
		}
	}
	
	private static File getUserDirectory(String email) {
		final File directory = Validator.isGuestEmail(email)?
			new File(Configuration.getInstance().getGuestsDirectory(), email):
			new File(Configuration.getInstance().getUsersDirectory(), email);
		
		if (!directory.isDirectory()) 
			directory.mkdirs();
		
		return directory;
	}
	
	public File getUserDirectory() {
		return User.getUserDirectory(this.getEmail());
	}
	
	public File getDataSetsDirectory() {
		final File directory = new File(this.getUserDirectory(), "datasets");
		if (!directory.isDirectory()) 
			directory.mkdirs();
		
		return directory;
	}
	
	public File getPatientSetsDirectory() {
		final File directory = new File(this.getUserDirectory(), "patients");
		if (!directory.isDirectory())
			directory.mkdirs();
		
		return directory;
	}
	
	public Data loadData(PatientSetMetaData metadata) throws Exception {
		for (PatientSetMetaData patientSet : this.getPatientSets()) {
			if (patientSet.equals(metadata)) {
				return LoadClassificationData.loadData(
					new File(this.getPatientSetsDirectory(), metadata.getFileName())
				);
			}
		}
		
		throw new IllegalArgumentException("Metadata does not belong to this user. Id: " + metadata.getId());
	}
	
	public Data loadData(DataSetMetaData metadata) throws Exception {
		for (DataSetMetaData dataSet : this.getDataSets()) {
			if (dataSet.equals(metadata)) {
				return LoadClassificationData.loadData(
					new File(this.getDataSetsDirectory(), metadata.getFileName())
				);
			}
		}
		
		throw new IllegalArgumentException("Metadata does not belong to this user. Id: " + metadata.getId());
	}
	
	public boolean isGuest() {
		return Validator.isGuestEmail(this.getEmail());
	}
	
	public void deleteDirectories() throws IOException {
		FileUtils.deleteDirectory(this.getUserDirectory());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.getEmail();
	}
}
