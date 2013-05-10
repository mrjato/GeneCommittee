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
package es.uvigo.ei.sing.gc.view.committee;

import es.uvigo.ei.sing.wekabridge.ClassifierBuilder;
import es.uvigo.ei.sing.wekabridge.classifiers.bayes.NaiveBayesSimpleBuilder;
import es.uvigo.ei.sing.wekabridge.classifiers.functions.LibSVMBuilder;
import es.uvigo.ei.sing.wekabridge.classifiers.lazy.IBkBuilder;
import es.uvigo.ei.sing.wekabridge.classifiers.trees.J48Builder;
import es.uvigo.ei.sing.wekabridge.classifiers.trees.RandomForestBuilder;

public enum ClassifierTypes {
	IBk("k-Nearest Neighbours (IBk)", "IBk", IBkBuilder.class, "http://weka.sourceforge.net/doc.dev/weka/classifiers/lazy/IBk.html"),
	J48("Decision Tree (C4.5)", "J48", J48Builder.class, "http://weka.sourceforge.net/doc/weka/classifiers/trees/J48.html"),
	SMO("Support Vector Machine (SMO)", "SMO", LibSVMBuilder.class, "http://weka.sourceforge.net/doc/weka/classifiers/functions/SMO.html"),
	NBS("Naïve Bayes Simple", "NBS", NaiveBayesSimpleBuilder.class, "http://weka.sourceforge.net/doc/weka/classifiers/bayes/NaiveBayesSimple.html"),
	RANDOM_FOREST("Random Forest", "RF", RandomForestBuilder.class, "http://weka.sourceforge.net/doc/weka/classifiers/trees/RandomForest.html");
	
	private final String name;
	private final String prefix;
	private final Class<? extends ClassifierBuilder> builder;
	private final String infoURL;
	
	private ClassifierTypes(String name, String prefix, Class<? extends ClassifierBuilder> builder, String infoURL) {
		this.name = name;
		this.prefix = prefix;
		this.builder = builder;
		this.infoURL = infoURL;
	}
	
	public ClassifierBuilder getBuilder() throws InstantiationException, IllegalAccessException {
		return this.builder.newInstance();
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public static ClassifierTypes getClassifierType(Class<? extends ClassifierBuilder> cbClass) {
		for (ClassifierTypes type : ClassifierTypes.values()) {
			if (type.builder.equals(cbClass)) {
				return type;
			}
		}
		
		return null;
	}
	
	public String getInfoURL() {
		return this.infoURL;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
