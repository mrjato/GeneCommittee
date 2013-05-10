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
package es.uvigo.ei.sing.gc.utils;

public class FormatUtils {
	public static final String formatParameterName(String name) {
		name = name.trim();
		
		if (name.isEmpty()) {
			return name;
		} else if (name.length() == 1) {
			return Character.toString(name.charAt(0));
		} else {
			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
			
			StringBuilder sb = new StringBuilder(name);
			
			boolean lower = false;
			for (int i = 0; i < sb.length(); i++) {
				final char c = sb.charAt(i);
				if (lower) {
					if (Character.isUpperCase(c)) {
						sb.insert(i++, ' ');
						lower = false;
					}
				} else {
					if (Character.isLowerCase(c)) {
						lower = true;
					}
				}
			}
			
			return sb.toString();
		}
	}
}
