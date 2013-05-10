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

import java.lang.reflect.Array;
import java.util.Collection;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

public class Property implements Converter<String, Object, Component>{
	private final String name;
	private final String value;
	private final boolean isGroup;
	
	public Property() {
		this.name = null;
		this.value = null;
		this.isGroup = false;
	}
	
	public Property(String name) {
		this.name = name;
		this.value = null;
		this.isGroup = true;
	}
	
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
		this.isGroup = false;
	}
	
	public static Property create(String name, Object object) {
		if (object == null) {
			return new Property(name, "");
		} else if (object instanceof Collection) {
			final Collection<?> collection = (Collection<?>) object;
			
			final StringBuilder sb = new StringBuilder();
			for (Object value : collection) {
				if (value != null) {
					if (sb.length() > 0) sb.append(", ");
					
					sb.append(value.toString());
				}
			}
			
			return new Property(name, sb.toString());
		} else if (object.getClass().isArray()) {
			final StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < Array.getLength(object); i++) {
				final Object value = Array.get(object, i);
				
				if (value != null) {
					if (sb.length() > 0) sb.append(", ");
					
					sb.append(value.toString());
				}
			}
			
			return new Property(name, sb.toString());
		} else {
			return new Property(name, object.toString());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isGroup() {
		return isGroup;
	}
	
	public static String valueToString(Object value) {
		if (value == null) {
			return "";
		} else if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			
			final StringBuilder sb = new StringBuilder();
			for (Object singleValue : collection) {
				if (singleValue != null) {
					if (sb.length() > 0) sb.append(", ");
					
					sb.append(singleValue.toString());
				}
			}
			
			return sb.toString();
		} else if (value.getClass().isArray()) {
			final StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < Array.getLength(value); i++) {
				final Object singleValue = Array.get(value, i);
				
				if (singleValue != null) {
					if (sb.length() > 0) sb.append(", ");
					
					sb.append(singleValue.toString());
				}
			}
			
			return sb.toString();
		} else {
			return value.toString();
		}
	}
	
	@Override
	public String coerceToUi(Object val, Component component, BindContext ctx) {
		return Property.valueToString(val);
	}
	
	@Override
	public Object coerceToBean(String val, Component component, BindContext ctx) {
		return null;
	}
}