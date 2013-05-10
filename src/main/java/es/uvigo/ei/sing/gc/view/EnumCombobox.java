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
package es.uvigo.ei.sing.gc.view;


import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

public class EnumCombobox<E extends Enum<E>> extends Combobox implements Component {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private final E[] enumValues;
	
	public EnumCombobox(Class<E> enumClass) {
		this(enumClass.getEnumConstants());
	}
	
	public EnumCombobox(E[] enumValues) {
		super();
		
		this.enumValues = enumValues;
		for (E value : enumValues) {
			this.appendChild(new Comboitem(value.toString()));
		}
	}
	
	public boolean setSelectedConstant(E constant) {
		for (int i = 0; i < this.enumValues.length; i++) {
			if (this.enumValues[i] == constant) {
				this.setSelectedIndex(i);
				return true;
			}
		}
		
		return false;
	}
	
	public E getSelectedConstant() {
		final int selectedIndex = this.getSelectedIndex();
		
		if (selectedIndex >= 0) {
			return this.enumValues[selectedIndex];
		} else {
			return null;
		}
	}
}
