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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Transient;

import es.uvigo.ei.aibench.core.CoreUtils;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.wekabridge.ClassifierBuilder;

@Entity(name="ClassifierBuilder")
public class ClassifierBuilderMetaData implements Cloneable {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private String name;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(
		name="ClassifierBuilderValues",
		joinColumns=@JoinColumn(name="classifierBuilder_id")
	)
	private Map<String, String> values;
	
	@Transient
	private ClassifierBuilder builder;
	@Transient
	private Map<String, String> defaultValues;
	@Transient
	private Map<String, Method> setters;
	@Transient
	private Map<String, Class<?>> types;
	@Transient
	private Map<String, String> paramDescriptions;
	
	public ClassifierBuilderMetaData() {
		this(null, null, new HashMap<String, String>());
	}
	
	public ClassifierBuilderMetaData(String name, ClassifierBuilder builder) {
		this(name, builder, new HashMap<String, String>());
	}
	
	public ClassifierBuilderMetaData(String name, ClassifierBuilder builder, Map<String, String> values) {
		this.name = name;
		
		this.setBuilder(builder);
		
		this.values = values;
	}
	
	public Integer getId() {
		return id;
	}
	
	@Column(name="builderClassName")
	@Access(AccessType.PROPERTY)
	public String getBuilderClassName() {
		if (this.builder == null)
			return null;
		else
			return this.builder.getClass().getName();
	}
	
	public void setBuilderClassName(String builderName) throws RuntimeException {
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends ClassifierBuilder> clazz =
				(Class<? extends ClassifierBuilder>) Class.forName(builderName);
			this.setBuilderClass(clazz);
		} catch (Exception e) {
			throw new RuntimeException("Error creating builder", e);
		}
	}
	
	public void setBuilderClass(Class<? extends ClassifierBuilder> builder) throws InstantiationException, IllegalAccessException {
		this.setBuilder(builder.newInstance());
	}
	
	public void setBuilder(ClassifierBuilder builder) {
		this.builder = builder;
		
		this.values = new HashMap<String, String>();
		this.defaultValues = new TreeMap<String, String>();
		this.setters = new TreeMap<String, Method>();
		this.types = new TreeMap<String, Class<?>>();
		this.paramDescriptions = new TreeMap<String, String>();

		if (this.builder != null) {
			final Class<? extends ClassifierBuilder> builderClass = 
				builder.getClass();
			
			final Method[] methods = builderClass.getMethods();
			for (Method method : methods) {
				final Port annotation = method.getAnnotation(Port.class);
				if (annotation != null && annotation.direction() == Direction.INPUT) {
					this.defaultValues.put(annotation.name(), annotation.defaultValue());
					this.setters.put(annotation.name(), method);
					this.types.put(annotation.name(), method.getParameterTypes()[0]);
					this.paramDescriptions.put(annotation.name(), annotation.description());
				}
			}
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> parameters() {
		return new ArrayList<String>(this.defaultValues.values());
	}
	
	public String getValue(String param) {
		return this.getValue(param, true);
	}
	
	public String getValue(String param, boolean useDefaultOnMissing) {
		if (this.getValues().containsKey(param)) {
			return this.getValues().get(param);
		} else if (useDefaultOnMissing) {
			return this.getDefaultValues().get(param);
		} else {
			return null;
		}
	}
	
	public String getDefaultValue(String param) {
		return this.defaultValues.get(param);
	}
		
	public String getUnchangedValue(String param) {
		if (this.values.containsKey(param))
			return null;
		else
			return this.defaultValues.get(param);
	}
	
	public void setValue(String param, String value) {
		if (value == null || this.defaultValues.get(param).equals(value)) {
			this.values.remove(value);
		} else {
			this.values.put(param, value);
		}
	}

	public ClassifierBuilder getBuilder() {
		return builder;
	}

	public Map<String, String> getDefaultValues() {
		return defaultValues;
	}

	public Map<String, Method> getSetters() {
		return setters;
	}

	public Map<String, Class<?>> getTypes() {
		return types;
	}

	public Map<String, String> getParamDescriptions() {
		return paramDescriptions;
	}

	public Map<String, String> getValues() {
		return values;
	}
	
	public ClassifierBuilder createNewBuilder() throws Exception {
		CoreUtils.setDefaultPortValues(this.builder);
		for (Map.Entry<String, String> parameter : this.getValues().entrySet()) {
			CoreUtils.setPortValue(this.builder, this.setters.get(parameter.getKey()), parameter.getValue());
		}
		
		return this.builder;
	}
	
	public void copyValuesOf(ClassifierBuilderMetaData classifierBuilder) {
		this.setName(classifierBuilder.getName());
		this.setBuilder(classifierBuilder.getBuilder());
		this.getValues().clear();
		this.getValues().putAll(classifierBuilder.getValues());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ClassifierBuilderMetaData)) {
			return false;
		}
		ClassifierBuilderMetaData other = (ClassifierBuilderMetaData) obj;
		if (getId() != other.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	public ClassifierBuilderMetaData clone() {
		final ClassifierBuilderMetaData newClassifierBuilder = new ClassifierBuilderMetaData(
			this.getName(),
			this.getBuilder(),
			this.getValues()
		);
		newClassifierBuilder.id = this.id;
		
		return newClassifierBuilder;
	}
	
	public static class ClassifierBuilderMetaDataComparator implements Comparator<ClassifierBuilderMetaData> {
		public int compare(ClassifierBuilderMetaData o1, ClassifierBuilderMetaData o2) {
			final int nameCmp = o1.getName().compareTo(o2.getName());
			
			if (nameCmp == 0) {
				return o1.getId() - o2.getId();
			} else {
				return nameCmp;
			}
		};
	}
}
