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

import weka.attributeSelection.Ranker;
import es.uvigo.ei.aibench.core.CoreUtils;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.datatypes.featureselection.FeatureSelector;
import es.uvigo.ei.sing.gc.Configuration;
import es.uvigo.ei.sing.wekabridge.ASEvaluationBuilder;
import es.uvigo.ei.sing.wekabridge.adapters.WekaFeatureSelector;

@Entity(name = "GeneSelection")
public class GeneSelectionMetaData implements Cloneable {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Basic
	private int numGenes;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(
		name="GeneSelectionValues",
		joinColumns=@JoinColumn(name="geneSelection_id")
	)
	private Map<String, String> values;
	
	@Transient
	private ASEvaluationBuilder builder;
	@Transient
	private Map<String, String> defaultValues;
	@Transient
	private Map<String, Method> setters;
	@Transient
	private Map<String, Class<?>> types;
	@Transient
	private Map<String, String> paramDescriptions;
	
	public GeneSelectionMetaData() {
		this(null, new HashMap<String, String>());
	}
	
	public GeneSelectionMetaData(ASEvaluationBuilder builder) {
		this(builder, new HashMap<String, String>());
	}
	
	public GeneSelectionMetaData(ASEvaluationBuilder builder, Map<String, String> values) {
		this.numGenes = Configuration.getInstance().getGeneBrowserDefaultInput();
		
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
			final Class<? extends ASEvaluationBuilder> clazz =
				(Class<? extends ASEvaluationBuilder>) Class.forName(builderName);
			this.setBuilderClass(clazz);
		} catch (Exception e) {
			throw new RuntimeException("Error creating builder", e);
		}
	}
	
	public void setBuilderClass(Class<? extends ASEvaluationBuilder> builder) throws InstantiationException, IllegalAccessException {
		this.setBuilder(builder.newInstance());
	}
	
	public void setBuilder(ASEvaluationBuilder builder) {
		this.builder = builder;
		
		this.values = new HashMap<String, String>();
		this.defaultValues = new TreeMap<String, String>();
		this.setters = new TreeMap<String, Method>();
		this.types = new TreeMap<String, Class<?>>();
		this.paramDescriptions = new TreeMap<String, String>();

		if (this.builder != null) {
			final Class<? extends ASEvaluationBuilder> builderClass = 
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
	
	public int getNumGenes() {
		return numGenes;
	}
	
	public void setNumGenes(int numGenes) {
		this.numGenes = numGenes;
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
			this.values.remove(param);
		} else {
			this.values.put(param, value);
		}
	}

	public ASEvaluationBuilder getBuilder() {
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
	
	public ASEvaluationBuilder getASEvaluationBuilder() throws Exception {
		CoreUtils.setDefaultPortValues(this.builder);
		for (Map.Entry<String, String> parameter : this.getValues().entrySet()) {
			CoreUtils.setPortValue(this.builder, this.setters.get(parameter.getKey()), parameter.getValue());
		}
		
		return this.builder;
	}
	
	public FeatureSelector createFeatureSelection() throws Exception {
		final Ranker ranker = new Ranker();
		ranker.setNumToSelect(this.numGenes);
		
		return new WekaFeatureSelector(
			this.getASEvaluationBuilder().buildEvaluator(),
			ranker
		);
	}
	
	public void copyValuesOf(GeneSelectionMetaData geneSelection) {
		this.setNumGenes(geneSelection.getNumGenes());
		this.setBuilder(geneSelection.getBuilder());
		this.getValues().clear();
		this.getValues().putAll(geneSelection.getValues());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
		if (!(obj instanceof GeneSelectionMetaData)) {
			return false;
		}
		GeneSelectionMetaData other = (GeneSelectionMetaData) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	public GeneSelectionMetaData clone() {
		final GeneSelectionMetaData newGeneSelection = new GeneSelectionMetaData(
			this.getBuilder(),
			this.getValues()
		);
		newGeneSelection.setNumGenes(this.getNumGenes());
		newGeneSelection.id = this.id;
		
		return newGeneSelection;
	}
}
