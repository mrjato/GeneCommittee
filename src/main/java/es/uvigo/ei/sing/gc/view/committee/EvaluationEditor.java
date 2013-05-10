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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Vbox;

import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.EvaluationStrategy;
import es.uvigo.ei.sing.gc.model.entities.ExpertsEvaluatorMetaData;

public class EvaluationEditor extends Groupbox {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		AbstractComponent.addClientEvent(EvaluationEditor.class, Events.ON_CHANGE, 0);
		AbstractComponent.addClientEvent(EvaluationEditor.class, Events.ON_CHANGING, 0);
	}
	
	private ExpertsEvaluatorMetaData evaluator;
	private int maxSamples;
	
	private final LOOCValidationBox loocValidationBox;
	private final XValidationBox xValidationBox;
	private final SplitValidationBox splitValidationBox;
	
	public EvaluationEditor() {
		super();
		
		this.evaluator = null;
		this.maxSamples = Integer.MAX_VALUE;
		
		final Radiogroup radiogroup = new Radiogroup();
		
		this.loocValidationBox = new LOOCValidationBox(radiogroup);
		this.xValidationBox = new XValidationBox(radiogroup);
		this.splitValidationBox = new SplitValidationBox(radiogroup);
		
		final Separator sep1 = new Separator();
		final Separator sep2 = new Separator();
		sep1.setHeight("5px");
		sep2.setHeight("5px");
		
		
		this.appendChild(this.loocValidationBox);
		this.appendChild(sep1);
		this.appendChild(this.xValidationBox);
		this.appendChild(sep2);
		this.appendChild(this.splitValidationBox);
		
		this.appendChild(radiogroup);
		
		this.addEventListener(Events.ON_CHANGING, this.loocValidationBox);
		this.addEventListener(Events.ON_CHANGING, this.xValidationBox);
		this.addEventListener(Events.ON_CHANGING, this.splitValidationBox);
	}
	
	public boolean isValid() {
		return this.evaluator != null;
	}
	
	public void setDataMetaData(DataSetMetaData data) {
		this.maxSamples = data.getSamples();
		this.notifyChanging();
	}
	
	public ExpertsEvaluatorMetaData getExpertsEvaluatorMetaData() {
		return this.evaluator;
	}
	
	public void setExpertsEvaluatorMetaData(ExpertsEvaluatorMetaData evaluator) {
		this.evaluator = evaluator;
		this.notifyChanging();
	}
	
	
	public void setDisabled(boolean disabled) {
		this.loocValidationBox.setDisabled(disabled);
		this.xValidationBox.setDisabled(disabled);
		this.splitValidationBox.setDisabled(disabled);
	}
	
	private final class LOOCValidationBox extends Vbox implements EventListener<Event>/* implements ValidationSource*/ {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		private final Radio rdLOOCV;
		
		public LOOCValidationBox(Radiogroup group) {
			super();
			
			this.rdLOOCV = new Radio();
			this.rdLOOCV.setRadiogroup(group);
			this.rdLOOCV.setSclass("formLabel");
			
			final Label lblRadio = new Label("leave-one-out");
			lblRadio.setSclass("formLabel");
			
			final Hbox radioBox = new Hbox(new Component[]{ this.rdLOOCV, lblRadio });
			radioBox.setSpacing("5px");
			radioBox.setAlign("center");
			
			this.appendChild(radioBox);
			
			this.rdLOOCV.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (rdLOOCV.isSelected() && evaluator.getStrategy() != EvaluationStrategy.LOOCValidation) {
						evaluator.setStrategy(EvaluationStrategy.LOOCValidation);
						evaluator.setParameters(Collections.singletonMap("replicationMode", "VIEW"));
						EvaluationEditor.this.notifyChange();
					}
				}
			});
		}
		
		public void setDisabled(boolean disabled) {
			this.rdLOOCV.setDisabled(disabled);
		}

		@Override
		public void onEvent(Event event) throws Exception {
			if (event.getName().equals(Events.ON_CHANGING)) {
				if (evaluator != null && evaluator.getStrategy() == EvaluationStrategy.LOOCValidation) {
					if (!this.rdLOOCV.isSelected()) {
						this.rdLOOCV.setSelected(true);
					}
				} else {
					this.rdLOOCV.setSelected(false);
				}
			}
		}
	}
	
	private final class XValidationBox extends Vbox implements EventListener<Event>/* implements ValidationSource*/ {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		private final Radio rdXValidation;
		private final Spinner spnFolds;
		
		private final Map<String, String> parameters;
		
		public XValidationBox(Radiogroup group) {
			super();
			
			this.parameters = new HashMap<String, String>();
			this.parameters.put("stratified", "true");
			this.parameters.put("replicationMode", "VIEW");
			this.parameters.put("k", "5");
			
			this.rdXValidation = new Radio();
			this.rdXValidation.setRadiogroup(group);
			
			this.spnFolds = new Spinner();
			this.spnFolds.setConstraint("min 1 max " + maxSamples);
			this.spnFolds.setValue(4);
			this.spnFolds.setCols(3);
			this.spnFolds.setMaxlength(3);
			this.spnFolds.setDisabled(true);
			
			final Label lblRadio = new Label("- fold cross");
			lblRadio.setSclass("formLabel");

			final Hbox radioBox = new Hbox(new Component[]{ this.rdXValidation, this.spnFolds, lblRadio });
			radioBox.setSpacing("5px");
			radioBox.setAlign("center");
			
			final Cell lblTrainCell = new Cell();
			lblTrainCell.setWidth("100px");
			lblTrainCell.setHeight("100%");
			lblTrainCell.setAlign("left");
			lblTrainCell.setStyle("padding-left: 20px;");
			lblTrainCell.setValign("middle");
			lblTrainCell.setSclass("blueText");
			
			lblTrainCell.appendChild(new Label("Gene expression"));
			
			this.appendChild(radioBox);
			
			this.rdXValidation.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (rdXValidation.isSelected() && evaluator.getStrategy() != EvaluationStrategy.XValidation) {
						spnFolds.setDisabled(false);
						
						evaluator.setStrategy(EvaluationStrategy.XValidation);
						evaluator.setParameters(XValidationBox.this.parameters);
						
						if (XValidationBox.this.parameters.containsKey("k")) {
							spnFolds.setValue(Integer.parseInt(XValidationBox.this.parameters.get("k")));
						}
						
						EvaluationEditor.this.notifyChange();
					}
				}
			});
			
			this.spnFolds.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					XValidationBox.this.parameters.put("k", XValidationBox.this.spnFolds.getValue().toString());
					
					evaluator.setParameters(XValidationBox.this.parameters);
					EvaluationEditor.this.notifyChange();
				}
			});
		}
		
		public void setDisabled(boolean disabled) {
			this.rdXValidation.setDisabled(disabled);
			this.spnFolds.setDisabled(disabled?true:(evaluator != null && evaluator.getStrategy() != EvaluationStrategy.XValidation));
		}

		@Override
		public void onEvent(Event event) throws Exception {
			if (event.getName().equals(Events.ON_CHANGING)) {
				if (evaluator != null && evaluator.getStrategy() == EvaluationStrategy.XValidation) {
					if (!this.rdXValidation.isSelected()) {
						this.rdXValidation.setSelected(true);
						this.spnFolds.setDisabled(false);
						
						if (evaluator.getParameter("k") != null) {
							this.spnFolds.setValue(Integer.parseInt(evaluator.getParameter("k")));
						}
					}
				} else {
					this.rdXValidation.setSelected(false);
					this.spnFolds.setDisabled(true);
				}
			}
		}
	}
	
	private final class SplitValidationBox extends Vbox implements EventListener<Event>/* implements ValidationSource*/ {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		private final Radio rdSplitValidation;
		private final Spinner spnPercentage;
		
		private final Map<String, String> parameters;

		public SplitValidationBox(Radiogroup group) {
			super();
			
			this.parameters = new HashMap<String, String>();
			this.parameters.put("stratified", "true");
			this.parameters.put("trainPercentage", "0.75f");
			
			this.rdSplitValidation = new Radio();
			this.rdSplitValidation.setRadiogroup(group);
			
			this.spnPercentage = new Spinner();
			this.spnPercentage.setConstraint("min 1 max 100");
			this.spnPercentage.setCols(3);
			this.spnPercentage.setMaxlength(3);
			this.spnPercentage.setValue(75);
			this.spnPercentage.setDisabled(true);
			
			final Label lblRadio = new Label("% train");
			lblRadio.setSclass("formLabel");

			final Hbox radioBox = new Hbox(new Component[]{ this.rdSplitValidation, this.spnPercentage, lblRadio });
			radioBox.setSpacing("5px");
			radioBox.setAlign("center");
			
			this.appendChild(radioBox);
			
			this.rdSplitValidation.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (rdSplitValidation.isSelected() && evaluator.getStrategy() != EvaluationStrategy.SplitValidation) {
						spnPercentage.setDisabled(false);
						
						evaluator.setStrategy(EvaluationStrategy.SplitValidation);
						evaluator.setParameters(SplitValidationBox.this.parameters);
						
						if (SplitValidationBox.this.parameters.containsKey("trainPercentage")) {
							spnPercentage.setValue((int) (Float.valueOf(SplitValidationBox.this.parameters.get("trainPercentage"))*100));
						}
						
						EvaluationEditor.this.notifyChange();
					}
				}
			});
			
			this.spnPercentage.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					final float train = (float) SplitValidationBox.this.spnPercentage.getValue();
					
					SplitValidationBox.this.parameters.put(
						"trainPercentage", 
						Float.toString(train/100f)
					);
					
					evaluator.setParameters(SplitValidationBox.this.parameters);
					EvaluationEditor.this.notifyChange();
				}
			});
		}
		
		public void setDisabled(boolean disabled) {
			this.rdSplitValidation.setDisabled(disabled);
			this.spnPercentage.setDisabled(disabled?true:(evaluator != null && evaluator.getStrategy() != EvaluationStrategy.SplitValidation));
		}
		
		@Override
		public void onEvent(Event event) throws Exception {
			if (event.getName().equals(Events.ON_CHANGING)) {
				if (evaluator != null && evaluator.getStrategy() == EvaluationStrategy.SplitValidation) {
					if (!this.rdSplitValidation.isSelected()) {
						this.rdSplitValidation.setSelected(true);
						this.spnPercentage.setDisabled(false);
						
						if (evaluator.getParameter("trainPercentage") != null) {
							this.spnPercentage.setValue((int) (Float.parseFloat(evaluator.getParameter("trainPercentage"))*100));
						}
					}
				} else {
					this.rdSplitValidation.setSelected(false);
					this.spnPercentage.setDisabled(true);
				}
			}
		}
	}
	
	protected void notifyChanging() {
		Events.sendEvent(Events.ON_CHANGING, this, null);
	}
	
	protected void notifyChange() {
		Events.sendEvent(Events.ON_CHANGE, this, null);
	}
}
