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
package es.uvigo.ei.sing.gc.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class GlobalEvents {
	public static final String ACTION_MARKER = "#";
	
	public static final String ACTION_STARTED = "Started";
	public static final String ACTION_FINISHED = "Finished";
	public static final String ACTION_ABORTED = "Aborted";
	public static final String ACTION_SCHEDULED = "Scheduled";
	public static final String ACTION_SUBTASK_STARTED = "SubtaskStarted";
	public static final String ACTION_SUBTASK_FINISHED = "SubtaskFinished";
	public static final String ACTION_SUBTASK_ABORTED = "SubtaskAborted";
	public static final String ACTION_SUBTASK_ERROR = "SubtaskError";
	
	public static final String SUFFIX_STARTED = ACTION_MARKER + ACTION_STARTED;
	public static final String SUFFIX_FINISHED = ACTION_MARKER + ACTION_FINISHED;
	public static final String SUFFIX_ABORTED = ACTION_MARKER + ACTION_ABORTED;
	public static final String SUFFIX_SCHEDULED = ACTION_MARKER + ACTION_SCHEDULED;
	public static final String SUFFIX_SUBTASK_STARTED = ACTION_MARKER + ACTION_SUBTASK_STARTED;
	public static final String SUFFIX_SUBTASK_FINISHED = ACTION_MARKER + ACTION_SUBTASK_FINISHED;
	public static final String SUFFIX_SUBTASK_ABORTED = ACTION_MARKER + ACTION_SUBTASK_ABORTED;
	public static final String SUFFIX_SUBTASK_ERROR = ACTION_MARKER + ACTION_SUBTASK_ERROR;
	
	public static final String EVENT_GENE_SELECTION = "eventGeneSelection";
	public static final String EVENT_GENE_SELECTION_STARTED = EVENT_GENE_SELECTION + SUFFIX_STARTED;
	public static final String EVENT_GENE_SELECTION_FINISHED = EVENT_GENE_SELECTION + SUFFIX_FINISHED;
	public static final String EVENT_GENE_SELECTION_ABORTED = EVENT_GENE_SELECTION + SUFFIX_ABORTED;
	
	public static final String EVENT_ENRICH_GENE_SET = "eventEnrichGeneSet";
	public static final String EVENT_ENRICH_GENE_SET_STARTED = EVENT_ENRICH_GENE_SET + SUFFIX_STARTED;
	public static final String EVENT_ENRICH_GENE_SET_FINISHED = EVENT_ENRICH_GENE_SET + SUFFIX_FINISHED;
	public static final String EVENT_ENRICH_GENE_SET_ABORTED = EVENT_ENRICH_GENE_SET + SUFFIX_ABORTED;
	
	public static final String EVENT_EXECUTION = "eventExecution";
	public static final String EVENT_EXECUTION_STARTED = EVENT_EXECUTION + SUFFIX_STARTED;
	public static final String EVENT_EXECUTION_FINISHED = EVENT_EXECUTION + SUFFIX_FINISHED;
	public static final String EVENT_EXECUTION_ABORTED = EVENT_EXECUTION + SUFFIX_ABORTED;
	public static final String EVENT_EXECUTION_SCHEDULED = EVENT_EXECUTION + SUFFIX_SCHEDULED;
	public static final String EVENT_EXECUTION_SUBTASK_STARTED = EVENT_EXECUTION + SUFFIX_SUBTASK_STARTED;
	public static final String EVENT_EXECUTION_SUBTASK_FINISHED = EVENT_EXECUTION + SUFFIX_SUBTASK_FINISHED;
	public static final String EVENT_EXECUTION_SUBTASK_ABORTED = EVENT_EXECUTION + SUFFIX_SUBTASK_ABORTED;
	public static final String EVENT_EXECUTION_SUBTASK_ERROR = EVENT_EXECUTION + SUFFIX_SUBTASK_ERROR;
	
	public static final String EVENT_DIAGNOSTIC_CREATION = "eventDiagnosticCreation";
	public static final String EVENT_DIAGNOSTIC_CREATION_STARTED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_STARTED;
	public static final String EVENT_DIAGNOSTIC_CREATION_FINISHED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_FINISHED;
	public static final String EVENT_DIAGNOSTIC_CREATION_ABORTED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_ABORTED;
	public static final String EVENT_DIAGNOSTIC_CREATION_SCHEDULED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_SCHEDULED;
	public static final String EVENT_DIAGNOSTIC_CREATION_SUBTASK_STARTED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_SUBTASK_STARTED;
	public static final String EVENT_DIAGNOSTIC_CREATION_SUBTASK_FINISHED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_SUBTASK_FINISHED;
	public static final String EVENT_DIAGNOSTIC_CREATION_SUBTASK_ABORTED = EVENT_DIAGNOSTIC_CREATION + SUFFIX_SUBTASK_ABORTED;
	public static final String EVENT_DIAGNOSTIC_CREATION_SUBTASK_ERROR = EVENT_DIAGNOSTIC_CREATION + SUFFIX_SUBTASK_ERROR;
	
	public static final String EVENT_PATIENT_DIAGNOSTIC = "eventPatientDiagnostic";
	public static final String EVENT_PATIENT_DIAGNOSTIC_STARTED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_STARTED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_FINISHED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_FINISHED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_ABORTED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_ABORTED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_SCHEDULED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_SCHEDULED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_SUBTASK_STARTED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_SUBTASK_STARTED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_SUBTASK_FINISHED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_SUBTASK_FINISHED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_SUBTASK_ABORTED = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_SUBTASK_ABORTED;
	public static final String EVENT_PATIENT_DIAGNOSTIC_SUBTASK_ERROR = EVENT_PATIENT_DIAGNOSTIC + SUFFIX_SUBTASK_ERROR;

	
	public static final String EVENT_DATA_UPLOADING = "eventDataUploading";
	public static final String EVENT_DATA_UPLOADING_STARTED = EVENT_DATA_UPLOADING + SUFFIX_STARTED;
	public static final String EVENT_DATA_UPLOADING_FINISHED = EVENT_DATA_UPLOADING + SUFFIX_FINISHED;
	public static final String EVENT_DATA_UPLOADING_ABORTED = EVENT_DATA_UPLOADING + SUFFIX_ABORTED;
	public static final String EVENT_DATA_UPLOADING_SCHEDULED = EVENT_DATA_UPLOADING + SUFFIX_SCHEDULED;
	public static final String EVENT_DATA_UPLOADING_SUBTASK_STARTED = EVENT_DATA_UPLOADING + SUFFIX_SUBTASK_STARTED;
	public static final String EVENT_DATA_UPLOADING_SUBTASK_FINISHED = EVENT_DATA_UPLOADING + SUFFIX_SUBTASK_FINISHED;
	public static final String EVENT_DATA_UPLOADING_SUBTASK_ABORTED = EVENT_DATA_UPLOADING + SUFFIX_SUBTASK_ABORTED;
	public static final String EVENT_DATA_UPLOADING_SUBTASK_ERROR = EVENT_DATA_UPLOADING + SUFFIX_SUBTASK_ERROR;
	
	
	private static final String[] SUFFIXES = new String[] {
		SUFFIX_STARTED, 
		SUFFIX_FINISHED, 
		SUFFIX_ABORTED, 
		SUFFIX_SCHEDULED, 
		SUFFIX_SUBTASK_ABORTED, 
		SUFFIX_SUBTASK_FINISHED, 
		SUFFIX_SUBTASK_STARTED
	};
	public static final String[] EVENTS = new String[] {
		EVENT_GENE_SELECTION, 
		EVENT_ENRICH_GENE_SET, 
		EVENT_EXECUTION, 
		EVENT_DIAGNOSTIC_CREATION, 
		EVENT_PATIENT_DIAGNOSTIC, 
		EVENT_DATA_UPLOADING
	};

	private final static Map<String, List<String>> EVENT_GLOBAL_COMMANDS = 
		Collections.synchronizedMap(new HashMap<String, List<String>>());
	
	private GlobalEvents() {}

	public final static void fullRegisterGlobalCommand(String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.fullStatesRegisterGlobalCommand(eventId, globalCommand);
			}
		}
	}
	
	public final static void fullUnregisterGlobalCommand(String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String eventId : GlobalEvents.SUFFIXES) {
				GlobalEvents.fullStatesUnregisterGlobalCommand(eventId, globalCommand);
			}
		}
	}
	
	public final static void fullStatesRegisterGlobalCommand(String eventId, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String suffix : GlobalEvents.SUFFIXES) {
				GlobalEvents.registerGlobalCommand(eventId + suffix, globalCommand);
			}
		}
	}
	
	public final static void fullStatesUnregisterGlobalCommand(String eventId, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String suffix : GlobalEvents.SUFFIXES) {
				GlobalEvents.unregisterGlobalCommand(eventId + suffix, globalCommand);
			}
		}
	}
	
	public final static void fullActionRegisterGlobalCommand(String action, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.registerGlobalCommand(eventId + GlobalEvents.ACTION_MARKER + action, globalCommand);
			}
		}
	}
	
	public final static void fullActionUnregisterGlobalCommand(String action, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.unregisterGlobalCommand(eventId + GlobalEvents.ACTION_MARKER + action, globalCommand);
			}
		}
	}
	
	public final static void registerGlobalCommand(String eventId, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			if (!EVENT_GLOBAL_COMMANDS.containsKey(eventId)) {
				EVENT_GLOBAL_COMMANDS.put(eventId, new LinkedList<String>());
			}
			
			if (!EVENT_GLOBAL_COMMANDS.get(eventId).contains(globalCommand)) {
				EVENT_GLOBAL_COMMANDS.get(eventId).add(globalCommand);
			}
		}
	}
	
	public final static void unregisterGlobalCommand(String eventId, String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			if (EVENT_GLOBAL_COMMANDS.containsKey(eventId)) {
				EVENT_GLOBAL_COMMANDS.get(eventId).remove(globalCommand);
				
				if (EVENT_GLOBAL_COMMANDS.get(eventId).isEmpty()) {
					EVENT_GLOBAL_COMMANDS.remove(eventId);
				}
			}
		}
	}
	
	public final static List<String> getEventGlobalCommands(String eventId) {
		final List<String> commands = EVENT_GLOBAL_COMMANDS.get(eventId);
		
		return (commands == null)?new ArrayList<String>():new ArrayList<String>(commands);
	}
}
