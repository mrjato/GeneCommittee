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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.model.Model;
import es.uvigo.ei.sing.datatypes.validation.ModelOutputEvaluator;
import es.uvigo.ei.sing.ensembles.evaluation.ExpertsEvaluator;

@Entity(name = "ExpertsEvaluator")
public class ExpertsEvaluatorMetaData implements Cloneable {
	@Id
	@GeneratedValue
	private Integer id;
	
	@Enumerated(EnumType.STRING)
	private EvaluationStrategy strategy;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@MapKeyColumn(name="name")
	@Column(name="value")
	@CollectionTable(
		name="ExpertsEvaluatorValues", 
		joinColumns = @JoinColumn(name="expertsEvaluator_id")
	)
	private Map<String, String> parameters;
	
	public ExpertsEvaluatorMetaData() {
		this.strategy = null;
		this.parameters = new HashMap<String, String>();
	}
	
	public ExpertsEvaluatorMetaData(EvaluationStrategy strategy) {
		this(strategy, new HashMap<String, String>());
	}
	
	public ExpertsEvaluatorMetaData(EvaluationStrategy strategy, Map<String, String> parameters) {
		this.parameters = parameters;
		this.strategy = strategy;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setStrategy(EvaluationStrategy strategy) {
		this.strategy = strategy;
		this.parameters.clear();
	}
	
	public EvaluationStrategy getStrategy() {
		return this.strategy;
	}
	
	public Map<String, String> getParameters() {
		return this.parameters;
	}
	
	public void setParameter(String param, String value) {
		this.parameters.put(param, value);
	}
	
	public String getParameter(String param) {
		return this.parameters.get(param);
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters.clear();
		this.parameters.putAll(parameters);
	}

	public ExpertsEvaluator<?> build() throws Exception {
		return this.strategy.build(null, null, null, this.getParameters());
	}
	
	public ExpertsEvaluator<?> build(Data data, Model model, ModelOutputEvaluator mEvaluator) throws Exception {
		return this.strategy.build(data, model, mEvaluator, this.getParameters());
	}
	
	public void copyValuesOf(ExpertsEvaluatorMetaData evaluator) {
		this.setStrategy(evaluator.getStrategy());
		this.getParameters().clear();
		this.getParameters().putAll(evaluator.getParameters());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
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
		if (!(obj instanceof ExpertsEvaluatorMetaData)) {
			return false;
		}
		ExpertsEvaluatorMetaData other = (ExpertsEvaluatorMetaData) obj;
		if (this.getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!this.getId().equals(other.getId())) {
			return false;
		}
		return true;
	}
	
	@Override
	public ExpertsEvaluatorMetaData clone() {
		final ExpertsEvaluatorMetaData newEvaluator = new ExpertsEvaluatorMetaData(
			this.getStrategy(),
			this.getParameters()
		);
		newEvaluator.id = this.id;
		
		return newEvaluator;
	}
}
