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
package es.uvigo.ei.sing.gc.model.entities;

import es.uvigo.ei.sing.wekabridge.ASEvaluationBuilder;
import es.uvigo.ei.sing.wekabridge.attributes.evaluators.ChiSquaredAttributeEvalBuilder;
import es.uvigo.ei.sing.wekabridge.attributes.evaluators.GainRatioAttributeEvalBuilder;
import es.uvigo.ei.sing.wekabridge.attributes.evaluators.InfoGainAttributeEvalBuilder;
import es.uvigo.ei.sing.wekabridge.attributes.evaluators.ReliefFAttributeEvalBuilder;

/**
 *
 * @author Miguel Reboiro-Jato
 *
 */
public enum GeneSelectionType {
	CHI_SQUARE("Chi-square", new ChiSquaredAttributeEvalBuilder(), "http://weka.sourceforge.net/doc/weka/attributeSelection/ChiSquaredAttributeEval.html"), 
	GAIN_RATIO("Gain Ratio", new GainRatioAttributeEvalBuilder(), "http://weka.sourceforge.net/doc/weka/attributeSelection/GainRatioAttributeEval.html"), 
	INFO_GAIN("Info Gain", new InfoGainAttributeEvalBuilder(), "http://weka.sourceforge.net/doc/weka/attributeSelection/InfoGainAttributeEval.html"), 
	RELIEF_F("Relief-F", new ReliefFAttributeEvalBuilder(), "http://weka.sourceforge.net/doc/weka/attributeSelection/ReliefFAttributeEval.html");
	
	private final String name;
	private final ASEvaluationBuilder builder;
	private final String infoURL;
	
	private GeneSelectionType(String name, ASEvaluationBuilder builder, String infoURL) {
		this.name = name;
		this.builder = builder;
		this.infoURL = infoURL;
	}

	public String getInfoURL() {
		return infoURL;
	}
	
	public static GeneSelectionType getDefault() {
		return GeneSelectionType.values()[GeneSelectionType.getDefaultIndex()];
	}
	
	public static int getDefaultIndex() {
		return 0;
	}
	
	public ASEvaluationBuilder getBuilder() {
		return this.builder;
	}
	
	public GeneSelectionMetaData buildGeneSelection() {
		return new GeneSelectionMetaData(
			this.builder
		);
	}
	
	public static GeneSelectionType getTypeForBuilder(ASEvaluationBuilder builder) {
		for (GeneSelectionType type : GeneSelectionType.values()) {
			if (type.equalsBuilder(builder)) return type;
		}
		
		return null;
	}
	
	public static GeneSelectionType getTypeForBuilder(String builderClassName) {
		for (GeneSelectionType type : GeneSelectionType.values()) {
			if (type.builder.getClass().getName().equals(builderClassName))
				return type;
		}
		
		return null;
	}
	
	public boolean equalsBuilder(ASEvaluationBuilder builder) {
		return this.builder.getClass().equals(builder.getClass());
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
