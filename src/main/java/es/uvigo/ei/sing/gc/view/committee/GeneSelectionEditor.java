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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import es.uvigo.ei.sing.gc.model.entities.GeneSelectionMetaData;
import es.uvigo.ei.sing.gc.model.entities.GeneSelectionType;
import es.uvigo.ei.sing.gc.utils.FormatUtils;
import es.uvigo.ei.sing.gc.view.EnumCombobox;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class GeneSelectionEditor extends Groupbox {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		AbstractComponent.addClientEvent(GeneSelectionEditor.class, Events.ON_CHANGE, 0);
	}
	
	private static final String BUILDER_PARAMETER = "BuilderParameter";
	private GeneSelectionMetaData geneSelection;
	private int columns;
	
	private final Toolbarbutton btnParamInfo;
	
	private final List<Box> boxes;
	private final List<Checkbox> checkBoxes;
	private final List<EnumCombobox<?>> enumComboboxes;
	private final List<Decimalbox> decimalBoxes;
	private final List<Spinner> spinners;
	private final List<Textbox> textboxes;
	
	private final EventListener<Event> elEventPost = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			GeneSelectionEditor.this.updateBuilderConfiguration();
			Events.postEvent(Events.ON_CHANGE, GeneSelectionEditor.this, event.getData());
		}
	};

	public GeneSelectionEditor() {
		this(null);
	}
	
	public GeneSelectionEditor(GeneSelectionMetaData featureSelection) {
		super();
		
		this.geneSelection = featureSelection;
		
		this.btnParamInfo = new Toolbarbutton("Parameters Info");
		this.btnParamInfo.setId("btnParamInfo");
		this.btnParamInfo.setTarget("_blank");
		
		this.boxes = new LinkedList<Box>();
		this.checkBoxes = new LinkedList<Checkbox>();
		this.enumComboboxes = new LinkedList<EnumCombobox<?>>();
		this.decimalBoxes = new LinkedList<Decimalbox>();
		this.spinners = new LinkedList<Spinner>();
		this.textboxes = new LinkedList<Textbox>();
		
		this.columns = 4;
		
		this.setClosable(false);
		
		this.rebuildUI();
	}
	
	public void setDisabled(boolean disabled) {
		for (Checkbox cb : this.checkBoxes)
			cb.setDisabled(disabled);
		for (EnumCombobox<?> elb : this.enumComboboxes)
			elb.setDisabled(disabled);
		for (Decimalbox dlb : this.decimalBoxes) 
			dlb.setDisabled(disabled);
		for (Spinner spn : this.spinners) 
			spn.setDisabled(disabled);
		for (Textbox tb : this.textboxes) 
			tb.setDisabled(disabled);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void rebuildUI() {
		for (Checkbox cb : this.checkBoxes)
			cb.removeEventListener(Events.ON_CHECK, this.elEventPost);
		for (EnumCombobox<?> elb : this.enumComboboxes)
			elb.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		for (Decimalbox dlb : this.decimalBoxes) 
			dlb.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		for (Spinner spn : this.spinners) 
			spn.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		for (Textbox tb : this.textboxes) 
			tb.removeEventListener(Events.ON_CHANGE, this.elEventPost);
		
		ZKUtils.emptyComponent(this, Hbox.class);
		this.boxes.clear();
		this.checkBoxes.clear();
		this.enumComboboxes.clear();
		this.decimalBoxes.clear();
		this.spinners.clear();
		this.textboxes.clear();
		
		if (this.geneSelection == null) {
			this.btnParamInfo.setDisabled(true);
		} else {
			final Map<String, Class<?>> types = this.geneSelection.getTypes();
			this.btnParamInfo.setHref(
				GeneSelectionType.getTypeForBuilder(
					this.geneSelection.getBuilder()
				).getInfoURL()
			);
			this.btnParamInfo.setDisabled(false);
			
			final Caption caption = this.getCaption();
			if (caption != null && caption.getFellowIfAny("btnParamInfo") == null) {
				caption.appendChild(this.btnParamInfo);
			}
			
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
				final String name = FormatUtils.formatParameterName(parameter);
				final Class<?> type = desc.getValue();
				
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
			
			final Hbox containerBox = new Hbox(this.boxes.toArray(new Vbox[this.boxes.size()]));
			containerBox.setAlign("center");
			containerBox.setPack("start");
			containerBox.setSpacing("20px");
			containerBox.setHflex("true");
			
			this.appendChild(containerBox);
		}
		
		this.invalidate();
	}
	
	public void setGeneSelection(GeneSelectionMetaData featureSelection) {
		this.geneSelection = featureSelection;
		
		this.rebuildUI();
	}
	
	public GeneSelectionMetaData getGeneSelection() {
		this.updateBuilderConfiguration();
		return this.geneSelection;
	}
	
	private static final String getBuildParameter(HtmlBasedComponent component) {
		return (String) component.getAttribute(BUILDER_PARAMETER);
	}

	public void updateBuilderConfiguration() {
		for (Checkbox checkbox : this.checkBoxes) {
			final String buildParameter = GeneSelectionEditor.getBuildParameter(checkbox);
			final boolean value = checkbox.isChecked();
			
			this.geneSelection.setValue(buildParameter, Boolean.toString(value));
		}
		
		for (EnumCombobox<? extends Enum<?>> listbox : this.enumComboboxes) {
			final String buildParameter = GeneSelectionEditor.getBuildParameter(listbox);
			final String enumName = listbox.getSelectedConstant().name();
			
			this.geneSelection.setValue(buildParameter, enumName);
		}
		
		for (Decimalbox decimalBox : this.decimalBoxes) {
			final String buildParameter = GeneSelectionEditor.getBuildParameter(decimalBox);
			final BigDecimal value = decimalBox.getValue();
			
			this.geneSelection.setValue(buildParameter, value.toString());
		}
		
		for (Spinner spinner : this.spinners) {
			final String buildParameter = GeneSelectionEditor.getBuildParameter(spinner);
			final Integer value = spinner.getValue();
			
			this.geneSelection.setValue(buildParameter, value.toString());
		}
		
		for (Textbox textbox : this.textboxes) {
			final String buildParameter = GeneSelectionEditor.getBuildParameter(textbox);
			final String value = textbox.getValue();
			
			this.geneSelection.setValue(buildParameter, value);
		}
	}

	protected final Checkbox buildCheckbox(String text, String parameter) {
		final Checkbox checkbox = new Checkbox(text);
		checkbox.setAttribute(BUILDER_PARAMETER, parameter);
		checkbox.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		checkbox.setChecked(Boolean.parseBoolean(this.geneSelection.getValue(parameter)));
		
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
		label.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
		final EnumCombobox<E> combobox = 
			(enumValues == null)?new EnumCombobox<E>(enumClass):new EnumCombobox<E>(enumValues);
		combobox.setAttribute(BUILDER_PARAMETER, parameter);
		combobox.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		combobox.setSelectedConstant(Enum.valueOf(enumClass, this.geneSelection.getValue(parameter)));
		
		this.enumComboboxes.add(combobox);
		combobox.addEventListener(Events.ON_CHANGE, this.elEventPost);

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
		label.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
		final Decimalbox decimalBox = new Decimalbox();
		decimalBox.setFormat((format == null)?"0.00":format);
		decimalBox.setCols((format == null)?4:format.length());
		decimalBox.setValue(this.geneSelection.getValue(parameter));
		decimalBox.setAttribute(BUILDER_PARAMETER, parameter);
		decimalBox.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
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
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
		final Spinner spinner = new Spinner();
		spinner.setCols(3);
		spinner.setValue(Integer.parseInt(this.geneSelection.getValue(parameter)));
		spinner.setAttribute(BUILDER_PARAMETER, parameter);
		spinner.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
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
		final Label label = new Label(text);
		label.setSclass("boldText");
		label.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
		final Textbox textbox = new Textbox(this.geneSelection.getValue(parameter));
		textbox.setAttribute(BUILDER_PARAMETER, parameter);
		textbox.setTooltiptext(this.geneSelection.getParamDescriptions().get(parameter));
		
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