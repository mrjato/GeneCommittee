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
package es.uvigo.ei.sing.gc;


import java.io.File;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class Configuration {
	private static Configuration instance = new Configuration();
	
	public static Configuration getInstance() {
		return Configuration.instance;
	}

	private InitialContext initialContext;
	
	private Configuration() {}
	
	private synchronized void createInitialContext() throws NamingException {
		if (this.initialContext == null)
			this.initialContext = new InitialContext();
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getConfigParam(String param){
		param = "genecommittee." + param;
		try {
			if (this.initialContext == null)
				this.createInitialContext();
			
			return (T) this.initialContext.lookup("java:comp/env/" + param);
		} catch (NamingException e) {
			return null;
		}
	}
	
	public File getDataDirectory() {
		final String directory = this.getConfigParam("storage.files.dataDirectory");
		return new File(directory);
	}
	
	public File getSamplesDirectory() {
		final String file = this.getConfigParam("storage.files.samplesDirectory");
		return new File(this.getDataDirectory(), file);
	}
	
	public String[] getSamplesNames() {
		final String names = this.getConfigParam("storage.files.samplesNames");
		
		return names.split(",");
	}
	
	public File[] getSamplesFiles() {
		final String samplesFiles = this.getConfigParam("storage.files.samplesFiles");
		final String[] fileNames = samplesFiles.split(",");
		final File[] files = new File[fileNames.length];
		
		final File samplesDir = this.getSamplesDirectory();
		for (int i = 0; i < fileNames.length; i++) {
			files[i] = new File(samplesDir, fileNames[i]);
		}
		
		return files;
	}
	
	public File getUsersDirectory() {
		final String file = this.getConfigParam("storage.files.usersDirectory");
		return new File(this.getDataDirectory(), file);
	}
	
	public File getGuestsDirectory() {
		final String file = this.getConfigParam("storage.files.guestsDirectory");
		return new File(this.getDataDirectory(), file);
	}
	
	public File getTmpDirectory() {
		final String file = this.getConfigParam("storage.files.tmpDirectory");
		return new File(this.getDataDirectory(), file);
	}
	
	public String getServer() {
		return this.getConfigParam("server");
	}
	
	public int getExperimentsCorePoolSize() {
		return this.getConfigParam("execution.experiments.corePoolSize");
	}
	
	public int getExperimentsMaximumPoolSize() {
		return this.getConfigParam("execution.experiments.maximumPoolSize");
	}
	
	public int getUtilityCorePoolSize() {
		return this.getConfigParam("execution.utility.corePoolSize");
	}
	
	public int getUtilityMaximumPoolSize() {
		return this.getConfigParam("execution.utility.maximumPoolSize");
	}

	public int getExperimentsQueueSize() {
		return this.getConfigParam("execution.experiments.queueSize");
	}
	
	public String getEmailAccount() {
		return this.getConfigParam("emailAccount");
	}
	
	public Session getMailSession() {
		try {
			if (this.initialContext == null)
				this.createInitialContext();
			
			final Context envCtx = (Context) this.initialContext.lookup("java:comp/env");
			return (Session) envCtx.lookup("mail/Session");
		} catch (NamingException e) {
			return null;
		}
	}
	
	public String getGeneBrowserURL() {
		return this.getConfigParam("genebrowser.url");
	}
	
	public String getGeneBrowserGenesMarker() {
		return this.getConfigParam("genebrowser.genesMarker");
	}
	
	public Integer getGeneBrowserMaxInput() {
		return this.getConfigParam("genebrowser.maxInput");
	}
	
	public Integer getGeneBrowserDefaultInput() {
		return this.getConfigParam("genebrowser.defaultInput");
	}
	
	public Integer getGeneBrowserMaxQuery() {
		return this.getConfigParam("genebrowser.maxQuery");
	}
}
