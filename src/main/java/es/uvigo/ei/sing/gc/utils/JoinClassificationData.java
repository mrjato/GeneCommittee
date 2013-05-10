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
/**
 *  Weka (http://www.cs.waikato.ac.nz/ml/weka/) bridge classes for 
 *	AIBench (http://www.aibench.org).
 *	Copyright (C) 2010  SING Group (http://sing.ei.uvigo.es)
 *	Escuela Superior de Ingeniería Informática.
 *  Edificio Politécnico - Office 408.
 *	Campus Universitario As Lagoas s/n. 32004 - Ourense, Spain.
 *	Tel:	+ 34 988 387015
 *	e-mail:	riverola@uvigo.es
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.ei.sing.gc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.data.DataFactory;
import es.uvigo.ei.sing.datatypes.data.DataUtils;
import es.uvigo.ei.sing.datatypes.data.Sample;
import es.uvigo.ei.sing.datatypes.data.Variable;

/**
 * @author Miguel Reboiro-Jato
 * @date Jun 29, 2012
 */
@Operation(name = "Join Classification Data")
public class JoinClassificationData {
	public enum JoinType {
		EXACT, UNION, INTERSECTION, LEFT, RIGHT;
		
		public SortedSet<Variable> getVariableNames(Variable[] left, Variable[] right) {
			final SortedSet<Variable> leftSet = new TreeSet<Variable>(Arrays.asList(left));
			final SortedSet<Variable> rightSet = new TreeSet<Variable>(Arrays.asList(right));
			
			switch (this) {
			case EXACT:
				if (leftSet.equals(rightSet))
					return leftSet;
				else
					throw new IllegalArgumentException("EXACT JOIN: left and right are not equal");
			case UNION:
				leftSet.addAll(rightSet);
				return leftSet;
			case INTERSECTION:
				leftSet.retainAll(rightSet);
				return leftSet;
			case LEFT:
				return leftSet;
			case RIGHT:
				return rightSet;
			default:
				throw new IllegalStateException("Unknown JOIN_TYPE");
			}
		}
	}
	
	private String name;
	private Data leftData, rightData;
	private JoinType joinType;
	private String dataType;
	
	@Port(
		name = "Name",
		direction = Direction.INPUT,
		allowNull = true,
		order = 1
	)
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param leftData the leftData to set
	 */
	@Port(
		name = "Left Data",
		direction = Direction.INPUT,
		allowNull = false,
		order = 2
	)
	public void setLeftData(Data leftData) {
		this.leftData = leftData;
	}
	
	/**
	 * @param rightData the rightData to set
	 */
	@Port(
		name = "Right Data",
		direction = Direction.INPUT,
		allowNull = false,
		order = 3
	)
	public void setRightData(Data rightData) {
		this.rightData = rightData;
	}

	@Port(
		name = "Data Type",
		description = "Possibles values: ram, disk or system",
		direction = Direction.INPUT, 
		allowNull = true, 
		defaultValue = "system",
		order = 4
	)
	public void setDataType(String dataType) {
		if (dataType != null && (dataType.equals("ram") || dataType.equals("disk")))
			this.dataType = dataType;
		else
			this.dataType = null;
	}
	
	@Port(direction = Direction.OUTPUT, order = 1000)
	public Data merge() {
		return join(this.name, this.leftData, this.rightData, this.dataType, this.joinType);
	}

	public static Data join(
		String name,
		Data leftData, 
		Data rightData,
		String dataType,
		JoinType joinType
	) {
		final Data data;
		if (dataType.equals("ram") || !dataType.equals("disk")) {
			data = DataFactory.createData(name, dataType);
		} else {
			data = DataFactory.createData(name);
		}
		
		final SortedSet<Variable> variables = joinType.getVariableNames(
			DataUtils.getVariables(leftData), 
			DataUtils.getVariables(rightData)
		);
		final SortedSet<Variable> targetVariables = joinType.getVariableNames(
			DataUtils.getTargetVariables(leftData),
			DataUtils.getTargetVariables(rightData)
		);
		
		final SortedSet<Variable> newVariables = new TreeSet<Variable>();
		for (Variable variable : variables) {
			final Variable newVariable = variable.clone(data);
			newVariables.add(newVariable);
		}
		
		for (Variable targetVariable : targetVariables) {
			data.addTargetVariable(targetVariable);
		}
		
		for (Sample sample : DataUtils.getSamples(leftData)) {
			final List<Object> values = new ArrayList<Object>(variables.size());
			
			for (Variable variable : variables) {
				values.add(sample.getVariableValue(variable));
			}
			
			new Sample(sample.getSampleIdentifier(), values, data);
		}
		
		for (Sample sample : DataUtils.getSamples(rightData)) {
			final List<Object> values = new ArrayList<Object>(variables.size());
			
			for (Variable variable : variables) {
				values.add(sample.getVariableValue(variable));
			}
			
			new Sample(sample.getSampleIdentifier(), values, data);
		}
		
		return data;
	}
}
