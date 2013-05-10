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

import es.uvigo.ei.sing.gc.execution.GlobalEvents;
import es.uvigo.ei.sing.gc.execution.Task;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.Committee.Compatibility;

class DiagnosticTask extends Task<ExpertResult> {
	private final Integer diagnosticId;
	private final Compatibility compatibility;

	public DiagnosticTask(
		String userId, Integer diagnosticId,
		Compatibility compatibility, 
		DiagnosticSubTask... subtasks
	) {
		super(
			GlobalEvents.EVENT_PATIENT_DIAGNOSTIC, 
			userId,
			"Evaluating patient", 
			subtasks
		);

		this.diagnosticId = diagnosticId;
		this.compatibility = compatibility;
	}

	public Integer getDiagnosticId() {
		return this.diagnosticId;
	}

	public Compatibility getCompatibility() {
		return compatibility;
	}
}