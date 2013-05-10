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
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

public class EnumListbox<E extends Enum<E>> extends Listbox implements Component {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private final Class<E> enumClass;
	private final E[] enumValues;
	
	public EnumListbox(Class<E> enumClass) {
		this(enumClass, enumClass.getEnumConstants());
	}
	
	public EnumListbox(Class<E> enumClass, E[] enumValues) {
		super();
		
		this.enumClass = enumClass;
		this.enumValues = enumValues;
		this.setModel(new EnumListModel());
		this.setItemRenderer(new EnumListRenderer());
	}
	
	@SuppressWarnings("unchecked")
	public E[] getSelectedConstants() {
		if (this.enumValues.length > 0) {
			int i = 0;
			
			for (Object item : this.getItems()) {
				if (item instanceof Listitem) {
					final Listitem listitem = (Listitem) item;
					
					if (listitem.isSelected() && this.enumClass.equals(listitem.getValue().getClass())) {
						this.enumValues[i++] = (E) listitem.getValue();
						
						if (i >= this.enumValues.length)
							break;
					}
				}
			}
		}
		
		return this.enumValues;
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
	
	private final class EnumListModel extends AbstractListModel<E> {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public E getElementAt(int index) {
			return EnumListbox.this.enumValues[index];
		}

		@Override
		public int getSize() {
			return EnumListbox.this.enumValues.length;
		}
	}
	
	private final class EnumListRenderer implements ListitemRenderer<E> {
		@Override
		public void render(Listitem item, E data, int index) throws Exception {
			item.setValue(data);
			item.appendChild(new Listcell(String.valueOf(data)));
		}
	}
}
