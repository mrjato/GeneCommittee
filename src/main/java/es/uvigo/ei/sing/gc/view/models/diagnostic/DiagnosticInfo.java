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

public class DiagnosticInfo {
	private final String title;
	private final String[] row;
	
	public DiagnosticInfo(String header) {
		super();
		this.title = header;
		this.row = null;
	}
	
	public DiagnosticInfo(String[] row) {
		super();
		this.title = null;
		this.row = row;
	}
	
	public boolean isHeader() {
		return this.title != null;
	}

	public String getTitle() {
		return title;
	}

	public String[] getRow() {
		return row;
	}
}