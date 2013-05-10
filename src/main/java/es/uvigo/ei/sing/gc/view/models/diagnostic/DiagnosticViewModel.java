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
package es.uvigo.ei.sing.gc.view.models.diagnostic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.UserGlobalEventListener;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.Committee.Compatibility;
import es.uvigo.ei.sing.gc.model.entities.Committee.ExpertIncompatibility;
import es.uvigo.ei.sing.gc.model.entities.Diagnostic;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.PatientSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.SampleClassification;
import es.uvigo.ei.sing.gc.model.entities.User;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import es.uvigo.ei.sing.gc.utils.Property;
import es.uvigo.ei.sing.gc.utils.Utils;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.gc.view.committee.ClassifierTypes;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.UserTasksViewModel;
import es.uvigo.ei.sing.gc.view.models.UserViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.summary.SummaryViewModel;

public class DiagnosticViewModel extends UserTasksViewModel {
	public static final String GC_DIAGNOSTIC_CREATION_ERROR = "diagnosticCreationError";
	public static final String GC_DIAGNOSTIC_CREATED = "diagnosticCreated";

	static {
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_DIAGNOSTIC_CREATION_SUBTASK_ERROR, 
			DiagnosticViewModel.GC_DIAGNOSTIC_CREATION_ERROR
		);
		GlobalEvents.registerGlobalCommand(
			GlobalEvents.EVENT_PATIENT_DIAGNOSTIC_FINISHED, 
			DiagnosticViewModel.GC_DIAGNOSTIC_CREATED
		);
	}
	
	private Committee selectedCommittee;
	private Diagnostic selectedDiagnostic;
	private Compatibility selectedDiagnosticCompatibility;
	private Map<Integer, ExpertIncompatibility> selectedDiagnosticIncompatibilities;
	
	private String committeeNewName;
	private String diagnosticNewName;
	
	private List<DiagnosticInfo> selectedDiagnosticInfo;
	private List<String> columnNames = new ArrayList<String>();
	
	@Init
	public void init(){
        super.init();
        
        if (this.getCommittees().isEmpty()) {
        	Messagebox.show(
        		"You do not have any committee. " +
        		"If you want to use this section go to 'Committee Training' and train a committee.",
        		"No Committees",
        		Messagebox.OK,
        		Messagebox.EXCLAMATION,
        		new EventListener<Event>() {
        			public void onEvent(Event event) throws Exception {
        				Executions.sendRedirect("/home.zul");
        			}
				}
        	);
        } else {
        	this.setSelectedCommittee(this.getCommittees().get(0));
        }
        
		StatusViewModel.changeInitialStatus("Upload your data to start diagnostic");
	}
	
	public List<Committee> getCommittees() {
		final User user = UserViewModel.getUser();
		
		final List<Committee> committees = new ArrayList<Committee>(user.getCommittees());
		if (user.getActiveCommittee() != null) 
			committees.remove(user.getActiveCommittee());
		Collections.sort(committees, new Committee.CommitteeComparator());
		
		return committees;
	}
	
	public List<Diagnostic> getDiagnostics() {
		if (this.selectedCommittee == null) {
			return Collections.emptyList();
		} else {
			this.selectedCommittee = ZKUtils.hLoad(Committee.class, this.selectedCommittee.getId()).getObject();
			final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>(this.selectedCommittee.getDiagnostics());
			
			Collections.sort(diagnostics, new Diagnostic.DiagnosticComparator());
			
			return diagnostics;
		}
	}
	
	public Committee getSelectedCommittee() {
		return selectedCommittee;
	}

	@NotifyChange({
		"selectedDiagnostic", 
		"selectedDiagnosticInfo", 
		"selectedDiagnosticCompatibility", 
		"selectedDiagnosticIncompatibilities", 
		"diagnostics", 
		"diagnosticSelected", 
		"diagnosticNewName",
		"columnNames", 
		"committeeNewName", 
		"selectedCommitteeProperties", 
		"committeeSelected",
		"selectedCommittee"
	})
	public void setSelectedCommittee(Committee selectedCommittee) {
		this.selectedCommittee = selectedCommittee;
		this.committeeNewName = this.selectedCommittee.getName();
		this.setSelectedDiagnostic(null);
	}

	public boolean isCommitteeSelected() {
		return this.getSelectedCommittee() != null;
	}
	
	public Compatibility getSelectedDiagnosticCompatibility() {
		return selectedDiagnosticCompatibility;
	}
	
	public List<ExpertIncompatibility> getSelectedDiagnosticIncompatibilities() {
		if (this.selectedDiagnosticIncompatibilities == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList<ExpertIncompatibility>(this.selectedDiagnosticIncompatibilities.values());
		}
	}

	public Diagnostic getSelectedDiagnostic() {
		return selectedDiagnostic;
	}

	@NotifyChange({
		"diagnosticNewName", 
		"diagnosticSelected", 
		"selectedDiagnostic",
		"selectedDiagnosticInfo", 
		"selectedDiagnosticCompatibility", 
		"selectedDiagnosticIncompatibilities", 
		"columnNames", 
		"diagnosticSelected", 
		"diagnosticNewName"
	})
	public void setSelectedDiagnostic(Diagnostic selectedDiagnostic) {
		if ((selectedDiagnostic == null && this.selectedDiagnostic != null) ||
			(selectedDiagnostic != null && !selectedDiagnostic.equals(this.selectedDiagnostic))
		) {
			this.selectedDiagnostic = selectedDiagnostic;
			
			this.columnNames.clear();
			
			if (this.selectedDiagnostic == null) {
				this.selectedDiagnosticInfo = null;
				this.selectedDiagnosticCompatibility = null;
				this.selectedDiagnosticIncompatibilities = null;
				this.diagnosticNewName = "";
			} else {
				this.selectedDiagnostic = ZKUtils.hLoad(Diagnostic.class, this.selectedDiagnostic.getId()).getObject();
				try {
					final PatientSetMetaData patientData = this.selectedDiagnostic.getPatientData();
					final Data data = patientData.loadData();
					
					final Committee committee = this.selectedDiagnostic.getCommittee();
					this.selectedDiagnosticIncompatibilities =
						committee.getExpertCompatibility(data);
					this.selectedDiagnosticCompatibility =
						committee.checkCompatibility(this.selectedDiagnosticIncompatibilities);
					System.out.println(this.selectedDiagnosticIncompatibilities);
				} catch (Exception e) {
					e.printStackTrace();
					
					this.selectedDiagnosticCompatibility = null;
					this.selectedDiagnosticIncompatibilities = null;
				}
				
				this.diagnosticNewName = this.selectedDiagnostic.getName();
				this.selectedDiagnosticInfo = DiagnosticViewModel.createDiagnosticInfo(this.selectedDiagnostic);
				
				this.columnNames.add(this.selectedDiagnostic.getName());
				
				if (!this.selectedDiagnostic.getResults().isEmpty()) {
					final SortedSet<SampleClassification> scOrdered = new TreeSet<SampleClassification>(
						new SampleClassification.SampleClassificationComparator()
					);
					scOrdered.addAll(this.selectedDiagnostic.getResults().iterator().next().getSamples());
					
					for (SampleClassification sc : scOrdered) {
						this.columnNames.add(sc.getSampleId());
					}
				}
			}
		}
	}
	
	public boolean isDiagnosticSelected() {
		return this.getSelectedDiagnostic() != null;
	}

	public List<Property> getSelectedCommitteeProperties() {
		if (this.selectedCommittee == null) {
			return Collections.emptyList();
		} else {
			this.selectedCommittee = ZKUtils.hLoad(Committee.class, this.selectedCommittee.getId()).getObject();
			
			return SummaryViewModel.extractCommitteeProperties(this.selectedCommittee);
		}
	}

	public List<String> getColumnNames() {
		return this.columnNames;
	}
	
	public int getNumColumns() {
		return this.getColumnNames().size();
	}
	
	public List<DiagnosticInfo> getSelectedDiagnosticInfo() {
		return selectedDiagnosticInfo;
	}
	
	public String getCommitteeNewName() {
		return committeeNewName;
	}

	public void setCommitteeNewName(String committeeNewName) {
		this.committeeNewName = committeeNewName;
	}

	public String getDiagnosticNewName() {
		return diagnosticNewName;
	}

	public void setDiagnosticNewName(String diagnosticNewName) {
		this.diagnosticNewName = diagnosticNewName;
	}

	@Command
	public void renameCommittee() {
		final String name = this.committeeNewName;
		if (this.isCommitteeSelected() && name != null && 
			!name.equals(this.getSelectedCommittee().getName())
		) {
			final Committee committee = ZKUtils.hLoad(Committee.class, this.getSelectedCommittee().getId()).getObject();
			committee.setName(name);
			HibernateUtil.currentSession().update(committee);
			
			BindUtils.postNotifyChange(null, null, this, "committees");
			BindUtils.postNotifyChange(null, null, this, "diagnostics");
			BindUtils.postNotifyChange(null, null, this, "selectedCommittee");
		}
	}

	@Command
	public void renameDiagnostic() {
		final String name = this.diagnosticNewName;
		if (this.isDiagnosticSelected() && name != null && 
			!name.equals(this.getSelectedDiagnostic().getName())
		) {
			final Diagnostic diagnostic = ZKUtils.hLoad(
				Diagnostic.class, 
				this.getSelectedDiagnostic().getId()
			).getObject();
			diagnostic.setName(name);
			HibernateUtil.currentSession().update(diagnostic);
			this.selectedDiagnostic = diagnostic;
			
			BindUtils.postNotifyChange(null, null, this, "diagnostics");
			BindUtils.postNotifyChange(null, null, this, "selectedCommittee");
		}
	}
	
	@Command
	public void deleteCommittee() {
		final Committee committee = this.getSelectedCommittee();
		Messagebox.show(
			String.format("Are you sure you want to delete '%s' committee and all its diagnostics?", committee.getName()),
			"Delete Committee",
			Messagebox.YES | Messagebox.NO,
			Messagebox.EXCLAMATION,
			new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if (((Integer) event.getData()).intValue() == Messagebox.YES) {
						final Committee toDelete = ZKUtils.hLoad(Committee.class, committee.getId()).getObject();
						
						toDelete.getUser().removeCommittee(toDelete);
						HibernateUtil.currentSession().delete(toDelete);
						
						for (Diagnostic diagnostic : toDelete.getDiagnostics()) {
							if (diagnostic.getPatientData().hasFile()) {
								new File(diagnostic.getPatientData().getFileName()).delete();
							}
						}
						
						final List<Committee> committees = DiagnosticViewModel.this.getCommittees();
						if (committees.isEmpty()) {
							Messagebox.show(
								"You do not have any committee to perform a diagnostic. " +
								"Please, train a new committee using 'Train Committee' option. " +
								"(You are going to be redirected to the home)",
								"No Committee",
								Messagebox.OK,
								Messagebox.EXCLAMATION,
								new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										Executions.sendRedirect("/home.zul");
									}
								}
							);
						} else {
							System.out.println("Notifying changes");
							DiagnosticViewModel.this.setSelectedCommittee(committees.get(0));
							
							BindUtils.postNotifyChange(null, null, this, "committees");
							BindUtils.postNotifyChange(null, null, this, "selectedCommittee");
						}
					}
				};
			}
		);
	}
	
	@Command
	public void deleteDiagnostic() {
		final Diagnostic diagnostic = this.getSelectedDiagnostic();
		Messagebox.show(
			String.format("Are you sure you want to delete '%s' diagnostic?", diagnostic.getName()),
			"Delete Committee",
			Messagebox.YES | Messagebox.NO,
			Messagebox.EXCLAMATION,
			new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if (((Integer) event.getData()).intValue() == Messagebox.YES) {
						final Diagnostic toDelete = ZKUtils.hLoad(Diagnostic.class, diagnostic.getId()).getObject();
						
						toDelete.getCommittee().getDiagnostics().remove(toDelete);
						toDelete.setCommittee(null);
						HibernateUtil.currentSession().delete(toDelete);
						
						if (toDelete.getPatientData().hasFile()) {
							new File(toDelete.getPatientData().getFileName()).delete();
						}
						
						DiagnosticViewModel.this.setSelectedDiagnostic(null);
						
						BindUtils.postNotifyChange(null, null, this, "diagnostics");
						BindUtils.postNotifyChange(null, null, this, "selectedDiagnostic");
					}
				};
			}
		);
	}
	
	@Command
	public void downloadDiagnostic() {
		final Diagnostic diagnostic = this.getSelectedDiagnostic();
		final StringBuilder sb = new StringBuilder();
		
		for (String columnName : this.getColumnNames()) {
			if (sb.length() > 0) sb.append(';');
			sb.append(columnName);
		}
		sb.append('\n');
		
		for (DiagnosticInfo info : this.getSelectedDiagnosticInfo()) {
			if (info.isHeader()) {
				sb.append(info.getTitle()).append('\n');
			} else {
				boolean first = true;
				for (String value : info.getRow()) {
					if (first) first = false;
					else sb.append(';');
					
					sb.append(value);
				}
				sb.append('\n');
			}
		}
		
		final String flnm = diagnostic.getName().toLowerCase().endsWith(".csv")?
			diagnostic.getName() : diagnostic.getName() + ".csv";
		
		Filedownload.save(
			sb.toString().getBytes(), "text/csv", flnm
		);
	}
	
	@Command
	public void downloadPatientData() {
		Diagnostic diagnostic = (Diagnostic) HibernateUtil.currentSession().load(
			Diagnostic.class, this.getSelectedDiagnostic().getId()
		);
		
		final PatientSetMetaData patientData = diagnostic.getPatientData();
		
		final File data = new File(
			patientData.getUser().getPatientSetsDirectory(), patientData.getFileName()
		);
		
		try {
			Filedownload.save(data, "text/csv");
		} catch (IOException ioe) {
			Clients.alert(
				"Internal error. Sorry, file could not be downloaded", 
				"Download Error", 
				Clients.NOTIFICATION_TYPE_ERROR
			);
		}
	}
	
	@GlobalCommand(DiagnosticViewModel.GC_DIAGNOSTIC_CREATION_ERROR)
	public void diagnosticError(
		@BindingParam(UserGlobalEventListener.KEY_DATA) CreateDiagnosticSubTask subtask
	) {
		Clients.showNotification(
			"The provided data set is not compatible with diagnostic data set",
			Clients.NOTIFICATION_TYPE_ERROR,
			null,
			"middle_center",
			0,
			true
		);
	}
	
	@GlobalCommand(DiagnosticViewModel.GC_DIAGNOSTIC_CREATED)
	public void diagnosticAdded(
		@BindingParam(UserGlobalEventListener.KEY_DATA) DiagnosticTask diagnosticTask
	) {
		final Diagnostic diagnostic = 
			ZKUtils.hLoad(Diagnostic.class, diagnosticTask.getDiagnosticId()).getObject(); 
		
		if (diagnostic.getCommittee().equals(this.selectedCommittee)) {
			BindUtils.postNotifyChange(null, null, this, "diagnostics");
		}
		
		if (diagnosticTask.getCompatibility() == Compatibility.FULL) {
			Clients.showNotification(
				String.format(
					"Diagnostic '%s' evaluation finished. You can find it in committee '%s'.",
					diagnostic.getName(),
					diagnostic.getCommittee().getName()
				),
				Clients.NOTIFICATION_TYPE_INFO,
				null,
				"middle_center",
				0,
				true
			);
		} else {
			Clients.showNotification(
				String.format(
					"Diagnostic '%s' evaluation finished but incomplete because of data set " +
					"incompatibility problems. You can find the diagnostic in committee '%s'. ",
					diagnostic.getName(),
					diagnostic.getCommittee().getName()
				),
				Clients.NOTIFICATION_TYPE_WARNING,
				null,
				"middle_center",
				0,
				true
			);
		}
	}
	
	@Command("upload")
	public void uploadAndClassify(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		ExecutionEngine.getSingleton().execute(
			new CreateDiagnosticTask(
				UserViewModel.getUser(false).getEmail(), 
				this.getSelectedCommittee().getId(), 
				((UploadEvent) ctx.getTriggerEvent()).getMedia()
			)
		);
	}
	
	private static List<DiagnosticInfo> createDiagnosticInfo(Diagnostic diagnostic) {
		final int numSamples = diagnostic.getPatientData().getSamples();
		
		final TreeSet<String> classNames = 
			new TreeSet<String>(diagnostic.getCommittee().getDataSet().getClassNames());
		final String[][][] values = new String[][][] {
			new String[diagnostic.getResults().size()][numSamples + 1],
			null,
			null,
			new String[classNames.size() + 1][numSamples + 1]
		};
		
		final SortedMap<String, String> geneSetIds = new TreeMap<String, String>();
		final SortedSet<String> classifierIds = new TreeSet<String>();
		
		final Map<String, Map<String, Voting>> geneSets = 
			new TreeMap<String, Map<String, Voting>>();
		final Map<String, Map<String, Voting>> classifiers = 
			new TreeMap<String, Map<String,Voting>>();
		final Map<String, Voting> voting = new TreeMap<String, Voting>();
		
		int i = 0;
		for (ExpertResult result : diagnostic.getResults()) {
			values[0][i][0] = result.toString();
			
			final SortedSet<SampleClassification> scOrdered = new TreeSet<SampleClassification>(
				new SampleClassification.SampleClassificationComparator()
			);
			scOrdered.addAll(result.getSamples());
			
			final String geneSetId = result.getGeneSetId();
			final String classifierName = result.getClassifierName();
			final ClassifierTypes classifierType = ClassifierTypes.getClassifierType(
				diagnostic.getCommittee().getClassifier(classifierName).getBuilder().getClass()
			);
			final String classifierTypeName = classifierType.toString();
			
			geneSetIds.put(geneSetId, result.getGeneSetName());
			classifierIds.add(classifierTypeName);
			
			int j = 1;
			for (SampleClassification sc : scOrdered) {
				final String predictedClass = sc.getPredictedClass();
				values[0][i][j++] = predictedClass;
				
				final String sampleId = sc.getSampleId();
				if (!voting.containsKey(sampleId)) {
					voting.put(sampleId, new Voting());
					geneSets.put(sampleId, new HashMap<String, Voting>());
					classifiers.put(sampleId, new HashMap<String, Voting>());
				}
				
				final Map<String, Voting> sampleGeneSets = geneSets.get(sampleId);
				final Map<String, Voting> sampleClassifiers = classifiers.get(sampleId);
				
				if (!sampleGeneSets.containsKey(geneSetId)) {
					sampleGeneSets.put(geneSetId, new Voting());
				}
				if (!sampleClassifiers.containsKey(classifierTypeName)) {
					sampleClassifiers.put(classifierTypeName, new Voting());
				}
				
				voting.get(sampleId).addVote(predictedClass);
				sampleGeneSets.get(geneSetId).addVote(predictedClass);
				sampleClassifiers.get(classifierTypeName).addVote(predictedClass);
			}
			
			i++;
		}
		
		values[1] = new String[geneSetIds.size()][numSamples + 1];
		values[2] = new String[classifierIds.size()][numSamples + 1];
		
		i = 0;
		for (Map.Entry<String, String> geneSetEntry : geneSetIds.entrySet()) {
			values[1][i][0] = geneSetEntry.getValue();
			
			int j = 1;
			for (Map<String, Voting> geneVoting : geneSets.values()) {
				values[1][i][j++] = geneVoting.get(geneSetEntry.getKey()).getBest();
			}
			
			i++;
		}
		
		i = 0;
		for (String classifier : classifierIds) {
			values[2][i][0] = classifier;
			
			int j = 1;
			for (Map<String, Voting> classifierVoting : classifiers.values()) {
				values[2][i][j++] = classifierVoting.get(classifier).getBest();
			}
			
			i++;
		}
		
		i = 0;
		for (String className : classNames) {
			values[3][i++][0] = className;
		}
		values[3][i][0] = "Unweighted Voting";
		
		i = 1;
		for (Voting votes : voting.values()) {
			int j = 0;
			
			for (String className : classNames) {
				values[3][j++][i] = Integer.toString(votes.getVotes(className));
			}
			
			values[3][j][i++] = votes.getBest();
		}
		
		final Comparator<String[]> comparator = new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return o1[0].compareTo(o2[0]);
			}
		};
		Arrays.sort(values[0], comparator);
		Arrays.sort(values[1], comparator);
		Arrays.sort(values[2], comparator);

		final List<DiagnosticInfo> info = new ArrayList<DiagnosticInfo>();
		info.add(new DiagnosticInfo("Committee"));
		
		for (String[] values0 : values[0]) {
			info.add(new DiagnosticInfo(values0));
		}
		info.add(new DiagnosticInfo("By Gene Set"));
		
		for (String[] values1 : values[1]) {
			info.add(new DiagnosticInfo(values1));
		}
		info.add(new DiagnosticInfo("By Classifier"));
		
		for (String[] values2 : values[2]) {
			info.add(new DiagnosticInfo(values2));
		}
		info.add(new DiagnosticInfo("Voting"));
		
		for (String[] values3 : values[3]) {
			info.add(new DiagnosticInfo(values3));
		}
		
		return info;
	}

	private final static class Voting {
		private Map<String, AtomicInteger> votes = new HashMap<String, AtomicInteger>();
		
		public void addVote(String className) {
			if (this.votes.containsKey(className)) {
				this.votes.get(className).incrementAndGet();
			} else {
				this.votes.put(className, new AtomicInteger(1));
			}
		}
		
		public int getVotes(String className) {
			if (this.votes.containsKey(className)) {
				return this.votes.get(className).get();
			} else {
				return 0;
			}
		}
		
		public String getBest() {
			final SortedMap<AtomicInteger, Set<String>> invertedVotes = 
				Utils.invertMap(this.votes, new TreeMap<AtomicInteger, Set<String>>(new Comparator<AtomicInteger>() {
					@Override
					public int compare(AtomicInteger o1, AtomicInteger o2) {
						return o2.intValue() - o1.intValue();
					}
				}));
			
			final StringBuilder bestSB = new StringBuilder();
			final Set<String> bests = invertedVotes.get(invertedVotes.firstKey());
			for (String best : bests) {
				if (bestSB.length() > 0) bestSB.append(", ");
				bestSB.append(best);
			}
			
			return bestSB.toString();
		}
	}
}
