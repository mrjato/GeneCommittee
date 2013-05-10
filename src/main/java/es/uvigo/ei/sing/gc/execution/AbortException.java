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

public class AbortException extends Exception {
	private static final long serialVersionUID = 1L;

	public AbortException() {
		super();
	}

	public AbortException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbortException(String message) {
		super(message);
	}

	public AbortException(Throwable cause) {
		super(cause);
	}
}
