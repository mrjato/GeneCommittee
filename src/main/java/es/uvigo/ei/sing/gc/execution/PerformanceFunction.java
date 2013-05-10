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

import es.uvigo.ei.sing.datatypes.maths.Maths;
import es.uvigo.ei.sing.datatypes.validation.ClassificationPerformance;

public enum PerformanceFunction {
	KAPPA("Kappa", true, true), 
	ACCURACY("Accuracy", true, true),
	PRECISION("Precision", false, true),
	RECALL("Recall (Sensitivity)", false, true),
//	SENSITIVITY("Sensitivity", false, true),
	SPECIFITY("Specifity", false, true),
	F_MEASURE("F-Measure", false, true),;
	
	public final static String ALL_CLASSES_LABEL = "<ALL>";
	
	private final String name;
	private final boolean all;
	private final boolean perClass;
	
	private PerformanceFunction(String name, boolean all, boolean perClass) {
		this.name = name;
		this.all = all;
		this.perClass = perClass;
	}

	public double calculate(ClassificationPerformance cp) {
		switch(this) {
		case PRECISION:
			return Maths.averagePrecision(cp.getConfusionMatrix());
		case RECALL:
			return Maths.averageRecall(cp.getConfusionMatrix());
//		case SENSITIVITY:
//			return Maths.sensitivity(className, cp.getConfusionMatrix());
		case SPECIFITY:
			return Maths.averageSpecificity(cp.getConfusionMatrix());
		case F_MEASURE:
			return Maths.averageFMeasure(cp.getConfusionMatrix());
		case ACCURACY:
			return Maths.accuracy(cp.getConfusionMatrix());
		case KAPPA:
			return Maths.kappa(cp.getConfusionMatrix());
		default:
			return Double.NaN;
		}
	}
	
	public double calculate(ClassificationPerformance cp, String className) {
		if (ALL_CLASSES_LABEL.equals(className)) {
			return this.calculate(cp);
		} else {
			switch(this) {
			case PRECISION:
				return Maths.precision(className, cp.getConfusionMatrix());
			case RECALL:
				return Maths.recall(className, cp.getConfusionMatrix());
	//		case SENSITIVITY:
	//			return Maths.sensitivity(className, cp.getConfusionMatrix());
			case SPECIFITY:
				return Maths.specificity(className, cp.getConfusionMatrix());
			case F_MEASURE:
				return Maths.fMeasure(className, cp.getConfusionMatrix());
			case ACCURACY:
				return Maths.accuracyPerClass(cp.getConfusionMatrix()).get(className);
			case KAPPA:
				return Maths.kappaPerClass(cp.getConfusionMatrix()).get(className);
			default:
				return Double.NaN;
			}
		}
	}
	
	public String format(double value) {
		switch(this) {
		case ACCURACY:
			return String.format("%.2f%%", value*100d);
		default:
			return String.format("%.4f", value);
		}
	}
	
	public double unformat(String formattedValue) {
		switch(this) {
		case ACCURACY:
			return Double.valueOf(formattedValue.substring(0, formattedValue.length()-1))/100d;
		default:
			return Double.valueOf(formattedValue);
		}
	}
	
	public boolean hasAll() {
		return this.all;
	}
	
	public boolean hasPerClass() {
		return this.perClass;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
