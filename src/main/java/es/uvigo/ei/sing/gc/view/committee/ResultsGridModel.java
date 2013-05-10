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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.RowRendererExt;
import org.zkoss.zul.event.ListDataEvent;

import es.uvigo.ei.sing.gc.execution.PerformanceFunction;
import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.view.committee.ResultsGridModel.RowValues;

public class ResultsGridModel extends AbstractListModel<RowValues> {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, Integer> geneSetIndexes;
	private final Map<String, Integer> classifierIndexes;
	private final RowValues[] values;
	
	private PerformanceFunction performanceFunction;
	private String performanceClass;
	
	private int rows;
	private boolean editable;
	
	
	public ResultsGridModel(Committee committee) {
		super();
		final int numClassifiers = committee.getClassifiers().size();
		final Set<GeneSetMetaData> selectedGeneSets = committee.getSelectedGeneSets();
		this.rows = selectedGeneSets.size();
		this.values = new RowValues[this.rows];
		this.geneSetIndexes = new HashMap<String, Integer>();
		this.classifierIndexes = new HashMap<String, Integer>();
		
		int i = 0;
		for (GeneSetMetaData geneSet : selectedGeneSets) {
			this.geneSetIndexes.put(geneSet.getGeneSetId(), i);
			
			this.values[i++] = new RowValues(geneSet.getName(), numClassifiers);
		}

		final List<String> classifierNames = new ArrayList<String>(numClassifiers);
		for (ClassifierBuilderMetaData classifier : committee.getClassifiers()) {
			classifierNames.add(classifier.getName());
		}
		
		Collections.sort(classifierNames);
		
		i = 0;
		for (String classifierName : classifierNames) {
			this.classifierIndexes.put(classifierName, i++);
		}
	}
	
	public void addValue(ExpertResult result) {
		final int geneSetIndex = this.geneSetIndexes.get(result.getGeneSetId());
		final int classifierIndex = this.classifierIndexes.get(result.getClassifierName());
		
		this.values[geneSetIndex].addResult(classifierIndex, result	);

		ResultsGridModel.this.fireEvent(ListDataEvent.CONTENTS_CHANGED, geneSetIndex, geneSetIndex);
	}
	
	private void clearValues() {
		for (RowValues values : this.values) {
			values.clearValues();
		}
	}

	public PerformanceFunction getPerformanceFunction() {
		return performanceFunction;
	}

	public void setPerformanceFunction(PerformanceFunction performanceFunction) {
		if (this.performanceFunction != performanceFunction) {
			this.performanceFunction = performanceFunction;
			
			this.clearValues();
			
			this.fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, this.rows);
		}
	}

	public String getPerformanceClass() {
		return performanceClass;
	}

	public void setPerformanceClass(String performanceClass) {
		if (this.performanceClass != performanceClass) {
			this.performanceClass = performanceClass;
			
			this.clearValues();
			
			this.fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, this.rows);
		}
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		if (this.editable != editable) {
			this.editable = editable;
			
			this.fireEvent(ListDataEvent.CONTENTS_CHANGED, 0, this.rows);
		}
	}

	@Override
	public RowValues getElementAt(int index) {
		return this.values[index];
	}
	
	@Override
	public int getSize() {
		return this.rows;
	}
	
	private static class SelectableRow extends Row {
		private static final long serialVersionUID = 1L;

		static {
			AbstractComponent.addClientEvent(SelectableRow.class, Events.ON_SELECTION, 0);
		}
	}
	
	public class RowValuesRenderer implements RowRendererExt, RowRenderer<RowValues> {
		@Override
		public void render(final Row row, final RowValues data, final int index) throws Exception {
			final Cell cellGeneSetName = new Cell();
			cellGeneSetName.appendChild(new Label(data.getGeneSetName()));
			row.appendChild(cellGeneSetName);
			
			for (int i = 0; i < data.getNumResults(); i++) {
				final ExpertResult result = data.getResult(i);
				final Cell cell = new Cell();
				final Label label = new Label(data.getFormattedValue(i));
				
				if (data.isSelected(i)) {
					label.setStyle("cursor: pointer; font-weight: bold;");
					cell.setStyle("cursor: pointer; background-color: #AED; font-weight: bold;");
				}
				
				if (ResultsGridModel.this.isEditable()) {
					cell.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						public void onEvent(Event event) throws Exception {
							result.setSelected(!result.isSelected());
							
							if (result.isSelected()) {
								label.setStyle("cursor: pointer; font-weight: bold;");
								cell.setStyle("cursor: pointer; background-color: #AED; font-weight: bold;");
							} else {
								label.setStyle("cursor: pointer;");
								cell.setStyle("cursor: pointer;");
							}
							
							Events.postEvent(Events.ON_SELECTION, row.getParent(), result);
						};
					});
				}
				
				cell.appendChild(label);
				row.appendChild(cell);
			}
		}

		@Override
		public Row newRow(Grid grid) {
			return new SelectableRow();
		}

		@Override
		public Component newCell(Row row) {
			return null;
		}

		@Override
		public int getControls() {
			return 0;
		}
	}
	
	public class RowValues {
		private final String geneSetName;
		private final ExpertResult[] results;
		private final double[] values;
		
		public RowValues(String geneSetName, int rowSize) {
			this.geneSetName = geneSetName;
			this.results = new ExpertResult[rowSize];
			this.values = new double[rowSize];
			
			this.clearValues();
		}
		
		public ExpertResult getResult(int index) {
			return this.results[index];
		}

		public RowValues(String geneSetName, ExpertResult[] results) {
			this(geneSetName, results.length);
			
			System.arraycopy(results, 0, this.results, 0, results.length);
		}
		
		public void addResult(int index, ExpertResult result) {
			this.results[index] = result;
		}

		public String getGeneSetName() {
			return this.geneSetName;
		}
		
		public boolean isSelected(int index) {
			return this.results[index] != null && this.results[index].isSelected();
		}
		
		public double getValue(int index) {
			if (Double.isNaN(this.values[index])) {
				this.values[index] = ResultsGridModel.this.performanceFunction.calculate(
					this.results[index].getClassificationPerformance(),
					ResultsGridModel.this.performanceClass
				);
			}
			
			return this.values[index];
		}
		
		public String getFormattedValue(int index) {
			if (this.results[index] == null) {
				return "";
			} else	if (this.results[index].isAborted()) {
				return "ERROR";
			} else {
				return ResultsGridModel.this.performanceFunction.format(
					this.getValue(index)
				);
			}
		}
		
		public int getNumResults() {
			return this.results.length;
		}
		
		public void clearValues() {
			for (int i = 0; i < this.values.length; i++) {
				this.values[i] = Double.NaN;
			}
		}
	}
	
	public static class GeneSetNameComparator implements Comparator<RowValues> {
		@Override
		public int compare(RowValues o1, RowValues o2) {
			return o1.getGeneSetName().compareTo(o2.getGeneSetName());
		}
	}
	
	public class ClassifierComparator implements Comparator<RowValues> {
		private final int index;
		
		public ClassifierComparator(String name) {
			this.index = ResultsGridModel.this.classifierIndexes.get(name);
		}
		
		@Override
		public int compare(RowValues o1, RowValues o2) {
			if (o2 == null) {
				return 1;
			} else if (o1 == null) {
				return -1;
			} else {
				return Double.compare(o1.getValue(this.index), o2.getValue(this.index));
			}
		}
	}
}
