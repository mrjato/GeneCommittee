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

public enum ExperimentStatus {
	Queued("Queued"), 
	Running("Running"), 
	Pausing("Pausing"), 
	Paused("Paused"), 
	Finished("Finished"), 
	Removing("Removing"), 
	Unscheduled("Unscheduled"),
	Deleted("Deleted");
	
    private final String displayName;

    private ExperimentStatus(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
}