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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.model.Model;
import es.uvigo.ei.sing.datatypes.validation.ModelOutputEvaluator;
import es.uvigo.ei.sing.ensembles.evaluation.ExpertsEvaluator;
import es.uvigo.ei.sing.ensembles.evaluation.LOOCVExpertsEvaluator;
import es.uvigo.ei.sing.ensembles.evaluation.SplitValidationExpertsEvaluator;
import es.uvigo.ei.sing.ensembles.evaluation.XValidationExpertsEvaluator;

/**
 *
 * @author Miguel Reboiro-Jato
 *
 */
public enum EvaluationStrategy {
	LOOCValidation("LOOC", LOOCVExpertsEvaluator.class),
	SplitValidation("Split", SplitValidationExpertsEvaluator.class),
	XValidation("X-Cross", XValidationExpertsEvaluator.class);
	
	private final String displayName;
	private final Class<? extends ExpertsEvaluator<?>> evaluatorClass;
	
	private EvaluationStrategy(String displayName,
			Class<? extends ExpertsEvaluator<?>> evaluatorClass) {
		this.displayName = displayName;
		this.evaluatorClass = evaluatorClass;
	}
    
    public ExpertsEvaluator<?> build(Data data, Model model, ModelOutputEvaluator mEvaluator, Map<String, String> properties) 
    throws IllegalAccessException, InvocationTargetException, InstantiationException {
    	final ExpertsEvaluator<?> evaluator = this.evaluatorClass.newInstance();
    	
    	if (data != null)
    		evaluator.setData(data);
    	if (model != null)
    		evaluator.setModel(model);
    	if (mEvaluator != null)
    		evaluator.setModelOutputEvaluator(mEvaluator);
    	
    	BeanUtils.populate(evaluator, properties);
    	
    	return evaluator;
    }
    
    @Override
    public String toString() {
    	return this.displayName;
    }
}
