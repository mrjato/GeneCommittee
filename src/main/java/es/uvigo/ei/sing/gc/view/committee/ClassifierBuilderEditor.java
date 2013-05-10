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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Box;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;

import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
import es.uvigo.ei.sing.gc.utils.FormatUtils;
import es.uvigo.ei.sing.gc.view.EnumCombobox;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class ClassifierBuilderEditor extends Groupbox {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String BUILDER_PARAMETER = "BuilderParameter";
	private static final Set<String> HIDDEN_PARAMETERS = new HashSet<String>();
	
	static {
		AbstractComponent.addClientEvent(ClassifierBuilderEditor.class, Events.ON_CHANGE, 0);
		HIDDEN_PARAMETERS.add("debug");
	}
	
	private ClassifierBuilderMetaData classifierBuilder;
	private String noClassifierText;
	private int columns;
	
	private final Caption caption;
	private final Toolbarbutton btnParamInfo;
	
	private final List<Box> boxes;
	private final List<Checkbox> checkBoxes;
	private final List<EnumCombobox<?>> enumListboxes;
	private final List<Decimalbox> decimalBoxes;
	private final List<Spinner> spinners;
	private final List<Textbox> textboxes;
	
	private final EventListener<Event> elEventPost = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			ClassifierBuilderEditor.this.updateBuilderConfiguration();
			Events.postEvent(Events.ON_CHANGE, ClassifierBuilderEditor.this, event.getData());
		}
	};


	public ClassifierBuilderEditor() {
		this(null);
	}
	
	public ClassifierBuilderEditor(ClassifierBuilderMetaData classifierBuilder) {
		super();
		this.caption = new Caption("No Classifier");
		this.btnParamInfo = new Toolbarbutton("Parameters Info");
		this.btnParamInfo.setTarget("_blank");
		this.caption.appendChild(this.btnParamInfo);
		this.appendChild(this.caption);
		this.setMold("3d");
		
		this.classifierBuilder = classifierBuilder;
		
		this.boxes = new LinkedList<Box>();
		this.checkBoxes = new LinkedList<Checkbox>();
		this.enumListboxes = new LinkedList<EnumCombobox<?>>();
		this.decimalBoxes = new LinkedList<Decimalbox>();
		this.spinners = new LinkedList<Spinner>();
		this.textboxes = new LinkedList<Textbox>();
		
		this.noClassifierText = "";
		this.columns = 4;
		
		this.setClosable(false);
		
		this.rebuildUI();
	}
	
	public void setClassifierBuilder(ClassifierBuilderMetaData classifierBuilder) {
		this.classifierBuilder = classifierBuilder;
		
		this.rebuildUI();
	}
	
	public ClassifierBuilderMetaData getClassifierBuilder() {
		this.updateBuilderConfiguration();
		return this.classifierBuilder;
	}
	
	public String getNoClassifierText() {
		return noClassifierText;
	}
	
	public void setNoClassifierText(String noClassifierText) {
		this.setNoClassifierText(noClassifierText, false);
	}
	
	public void setNoClassifierText(String noClassifierText, boolean forceRebuildUI) {
		this.noClassifierText = noClassifierText;
		
		if (forceRebuildUI)
			this.rebuildUI();
	}
	
	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.setColumns(columns, false);
	}
	
	public void setColumns(int columns, boolean forceRebuildUI) {
		this.columns = columns;
		
		if (forceRebuildUI)
			this.rebuildUI();
	}

	public void setDisabled(boolean disabled) {
		for (Checkbox cb : this.checkBoxes)
			cb.setDisabled(disabled);
		for (EnumCombobox<?> elb : this.enumListboxes)
			elb.setDisabled(disabled);
		for (Decimalbox dlb : this.decimalBoxes) 
			dlb.setDisabled(disabled);
		for (Spinner spn : this.spinners) 
			spn.setDisabled(disabled);
		for (Textbox tb : this.textboxes) 
			tb.setDisabled(disabled);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void rebuildUI() {
		for (Checkbox cb : this.checkBoxes)
			cb.removeEventListener(Events.ON_CHECK, this.elEventPost);
		for (EnumCombobox<?> elb : this.enumListboxes)
			elb.removeEventListener(Events.ON_SELECT, this.elEventPost);
		for (Decimalbox dlb : this.decimalBoxes) 
			dlb.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		for (Spinner spn : this.spinners) 
			spn.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		for (Textbox tb : this.textboxes) 
			tb.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		
		ZKUtils.emptyComponent(this, Hbox.class, Label.class);
		this.boxes.clear();
		this.checkBoxes.clear();
		this.enumListboxes.clear();
		this.decimalBoxes.clear();
		this.spinners.clear();
		this.textboxes.clear();
		
		if (this.classifierBuilder == null) {
			this.caption.setLabel("No Classifier");
			this.btnParamInfo.setDisabled(true);
			this.appendChild(new Label(this.noClassifierText));
		} else {
			this.caption.setLabel(this.classifierBuilder.getName());
			this.btnParamInfo.setHref(
				ClassifierTypes.getClassifierType(
					this.classifierBuilder.getBuilder().getClass()
				).getInfoURL()
			);
			this.btnParamInfo.setDisabled(false);

			final Map<String, Class<?>> types = this.classifierBuilder.getTypes();
			
			int i = 0;
			int max = (types.isEmpty())?0:types.size()/this.columns;
			if (types.size()%this.columns != 0) {
				max++;
			}
			
			Vbox box = null;
			for (Map.Entry<String, Class<?>> desc : types.entrySet()) {
				if (i == 0) {
					box = new Vbox();
					box.setHflex("true");
					this.boxes.add(box);
				}
				
				final String parameter = desc.getKey();
				
				if (!ClassifierBuilderEditor.HIDDEN_PARAMETERS.contains(parameter)) {
					final Class<?> type = desc.getValue();
					
					final String name = FormatUtils.formatParameterName(parameter);
					final Component component;
					if (type.equals(Boolean.class) || type.equals(boolean.class)) {
						component = this.buildCheckbox(name, parameter);
					} else if (type.equals(Integer.class) || type.equals(int.class) || 
							type.equals(Short.class) || type.equals(short.class) ||
							type.equals(Long.class) || type.equals(long.class)) {
						component = this.buildSpinner(name, parameter);
					} else if (type.equals(Float.class) || type.equals(float.class) || 
							type.equals(Double.class) || type.equals(double.class)) {
						component = this.buildDecimalBox(name, parameter);
					} else if (type.isEnum()) {
						component = this.buildSelect(name, parameter, (Class<? extends Enum>) type);
					} else {
						component = this.buildTextBox(name, parameter);
					}
				
					box.appendChild(component);
					
					if (++i == max) i = 0;
				}
			}
			
			final Hbox containerBox = new Hbox(this.boxes.toArray(new Vbox[this.boxes.size()]));
			containerBox.setSpacing("20px");
			containerBox.setHflex("true");
			
			this.appendChild(containerBox);
		}
	}
	
	private static final String getBuildParameter(HtmlBasedComponent component) {
		return (String) component.getAttribute(BUILDER_PARAMETER);
	}

	public void updateBuilderConfiguration() {
		for (Checkbox checkbox : this.checkBoxes) {
			final String buildParameter = ClassifierBuilderEditor.getBuildParameter(checkbox);
			final boolean value = checkbox.isChecked();
				
			final String currentValue = this.classifierBuilder.getDefaultValue(buildParameter);
			if (currentValue != null && Boolean.parseBoolean(currentValue) == value) {
				this.classifierBuilder.setValue(buildParameter, null);
			} else {
				this.classifierBuilder.setValue(buildParameter, Boolean.toString(value));
			}
		}
		
		for (EnumCombobox<? extends Enum<?>> listbox : this.enumListboxes) {
			final String buildParameter = ClassifierBuilderEditor.getBuildParameter(listbox);
			final String enumName = listbox.getSelectedConstant().name();
			
			this.classifierBuilder.setValue(buildParameter, enumName);
		}
		
		for (Decimalbox decimalBox : this.decimalBoxes) {
			final String buildParameter = ClassifierBuilderEditor.getBuildParameter(decimalBox);
			final BigDecimal value = decimalBox.getValue();
			
			final String currentValue = this.classifierBuilder.getDefaultValue(buildParameter);
			try {
				if (currentValue != null && new BigDecimal(currentValue).equals(value))
					this.classifierBuilder.setValue(buildParameter, null);
				else 
					this.classifierBuilder.setValue(buildParameter, value.toString());
			} catch (NumberFormatException nfe) {
				this.classifierBuilder.setValue(buildParameter, value.toString());
			}
		}
		
		for (Spinner spinner : this.spinners) {
			final String buildParameter = ClassifierBuilderEditor.getBuildParameter(spinner);
			final Integer value = spinner.getValue();
			
			final String currentValue = this.classifierBuilder.getDefaultValue(buildParameter);
			
			try {
				if (currentValue != null && Integer.valueOf(currentValue).equals(value))
					this.classifierBuilder.setValue(buildParameter, null);
				else
					this.classifierBuilder.setValue(buildParameter, value.toString());
			} catch (NumberFormatException nfe) {
				this.classifierBuilder.setValue(buildParameter, value.toString());
			}
		}
		
		for (Textbox textbox : this.textboxes) {
			final String buildParameter = ClassifierBuilderEditor.getBuildParameter(textbox);
			final String value = textbox.getValue();
			
			this.classifierBuilder.setValue(buildParameter, value);
		}
	}

	protected final Checkbox buildCheckbox(String text, String parameter) {
		final Checkbox checkbox = new Checkbox(text);
		checkbox.setAttribute(BUILDER_PARAMETER, parameter);
		checkbox.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		checkbox.setChecked(Boolean.parseBoolean(this.classifierBuilder.getValue(parameter)));
		
		this.checkBoxes.add(checkbox);
		checkbox.addEventListener(Events.ON_CHECK, this.elEventPost);
		
		return checkbox;
	}
	
	protected final <E extends Enum<E>> Hlayout buildSelect(String text, String parameter, Class<E> enumClass) {
		return this.buildSelect(text, parameter, enumClass, null);
	}
	
	protected final <E extends Enum<E>> Hlayout buildSelect(String text, String parameter, Class<E> enumClass, E[] enumValues) {
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		
		final EnumCombobox<E> combobox = 
			(enumValues == null)?new EnumCombobox<E>(enumClass):new EnumCombobox<E>(enumValues);
		combobox.setAttribute(BUILDER_PARAMETER, parameter);
		combobox.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		combobox.setSelectedConstant(Enum.valueOf(enumClass, this.classifierBuilder.getValue(parameter)));
		
		this.enumListboxes.add(combobox);
		combobox.addEventListener(Events.ON_SELECT, this.elEventPost);

		final Hlayout layout = new Hlayout();
		layout.setValign("middle");
		layout.setSclass("z-valign-middle");
		layout.setSpacing("5px");
		layout.appendChild(label);
		layout.appendChild(combobox);
		
		return layout;
	}

	protected final Hlayout buildDecimalBox(String text, String parameter) {
		return this.buildDecimalBox(text, parameter, null);
	}

	protected final Hlayout buildDecimalBox(String text, String parameter, String format) {
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		
		final Decimalbox decimalBox = new Decimalbox();
		decimalBox.setFormat((format == null)?"0.00":format);
		decimalBox.setCols((format == null)?4:format.length());
		decimalBox.setValue(this.classifierBuilder.getValue(parameter));
		decimalBox.setAttribute(BUILDER_PARAMETER, parameter);
		decimalBox.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		
		this.decimalBoxes.add(decimalBox);
		decimalBox.addEventListener(Events.ON_CHANGE, this.elEventPost);

		final Hlayout layout = new Hlayout();
		layout.setValign("middle");
		layout.setSclass("z-valign-middle");
		layout.setSpacing("5px");
		layout.appendChild(label);
		layout.appendChild(decimalBox);
		
		return layout;
	}

	protected final Hlayout buildSpinner(String text, String parameter) {
		return this.buildSpinner(text, parameter, null);
	}

	protected final Hlayout buildSpinner(String text, String parameter, String constraint) {
		final String description = this.classifierBuilder.getParamDescriptions().get(parameter);
		
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(description);
		
		final Spinner spinner = new Spinner();
		spinner.setCols(3);
		spinner.setValue(Integer.parseInt(this.classifierBuilder.getValue(parameter)));
		spinner.setAttribute(BUILDER_PARAMETER, parameter);
		spinner.setTooltiptext(description);
		if (constraint != null)
			spinner.setConstraint(constraint);
		
		this.spinners.add(spinner);
		spinner.addEventListener(Events.ON_CHANGE, this.elEventPost);
		
		final Hlayout layout = new Hlayout();
		layout.setValign("middle");
		layout.setSclass("z-valign-middle");
		layout.setSpacing("5px");
		layout.appendChild(label);
		layout.appendChild(spinner);
		
		return layout;
	}

	protected final Hlayout buildTextBox(String text, String parameter) {
		final String description = this.classifierBuilder.getParamDescriptions().get(parameter);
		
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(description);
		
		final Textbox textbox = new Textbox(this.classifierBuilder.getValue(parameter));
		textbox.setAttribute(BUILDER_PARAMETER, parameter);
		textbox.setTooltiptext(this.classifierBuilder.getParamDescriptions().get(parameter));
		
		this.textboxes.add(textbox);
		textbox.addEventListener(Events.ON_CHANGE, this.elEventPost);

		final Hlayout layout = new Hlayout();
		layout.setValign("middle");
		layout.setSclass("z-valign-middle");
		layout.setSpacing("5px");
		layout.appendChild(label);
		layout.appendChild(textbox);
		
		return layout;
	}
}