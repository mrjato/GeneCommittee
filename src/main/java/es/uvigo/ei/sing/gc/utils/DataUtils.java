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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.data.Sample;
import es.uvigo.ei.sing.datatypes.data.Variable;
import es.uvigo.ei.sing.ensembles.Utils;

public final class DataUtils {
	private DataUtils() {}
	
	public static List<String> getVariableNames(Data data, boolean keepTargetVariables) {
		final int variableCount = data.getVariableCount();
		
		final List<String> names = new ArrayList<String>(variableCount);
		final Set<String> targetVariables = new HashSet<String>();
		
		if (!keepTargetVariables) {
			for (Variable variable : data.getTargetVariables()) {
				targetVariables.add(variable.getName());
			}
		}
		
		for (int i = 0; i < variableCount; i++) {
			final String variable = data.getVariableAt(i).getName();
			
			if (!targetVariables.contains(variable)) {
				names.add(variable);
			}
		}
		
		return names;
	}
	
	public static Map<String, Integer> getConditionsCount(Data data) {
		final Variable classVariable = DataUtils.getClassVariable(data);
		
		if (classVariable == null)
			throw new IllegalArgumentException("data does not have a class variable");
		
		final int sampleCount = data.getSampleCount();
		final Map<String, Integer> conditions = new HashMap<String, Integer>();
		for (int i = 0; i < sampleCount; i++) {
			final Sample sample = data.getSampleAt(i);
			
			final String value = sample.getVariableValue(classVariable).toString();
			
			if (conditions.containsKey(value)) {
				conditions.put(value, conditions.get(value) + 1);
			} else {
				conditions.put(value, 1);
			}
		}
		
		return conditions;
	}
	
	public static Variable getClassVariable(Data data) {
		return Utils.getClassVariable(data);
	}
	
	public static List<String> getClassValuesAsString(Data data) {
		return Utils.getClassValuesAsString(data);
	}
}
