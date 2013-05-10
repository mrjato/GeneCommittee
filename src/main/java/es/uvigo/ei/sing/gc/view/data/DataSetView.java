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
package es.uvigo.ei.sing.gc.view.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.ext.Sortable;

import es.uvigo.ei.sing.datatypes.data.Data;
import es.uvigo.ei.sing.datatypes.data.Sample;
import es.uvigo.ei.sing.datatypes.data.Variable;
import es.uvigo.ei.sing.ensembles.Utils;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class DataSetView extends Grid {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private final DataListModel model;
	private final Columns columns;
	private final Auxhead header;

	public DataSetView() {
		this(null);
	}
	
	public DataSetView(Data data) {
		super();

		this.model = new DataListModel(data);
		
		this.setSclass("data_grid");
		this.setModel(this.model);
		this.setRowRenderer(new DataRowRenderer());
		
		this.setAutopaging(true);
		this.setMold("paging");
		
		final Frozen frozen = new Frozen();
		frozen.setColumns(1);
		
		this.columns = new Columns();
		this.columns.setSizable(true);
		this.header = new Auxhead();
		
		this.updateColumns(data);
		
		this.appendChild(frozen);
		this.appendChild(this.columns);
		this.appendChild(header);
	}
	
	@SuppressWarnings("unchecked")
	private void updateColumns(Data data) {
		ZKUtils.emptyComponent(this.columns, Column.class);
		ZKUtils.emptyComponent(this.header, Auxheader.class);
		
		if (data != null) {
			final Variable classVariable = Utils.getClassVariable(data);
			
			final Column columnVariable = new Column("Gene");
			this.columns.appendChild(columnVariable);
			columnVariable.setWidth("80px");
			columnVariable.setSortAscending(this.model.getVariableComparator());
			columnVariable.setSortDescending(this.model.getVariableComparator());
			
			final Auxheader headerClass = new Auxheader("Condition");
			headerClass.setWidth("80px");
			this.header.appendChild(headerClass);
			
			int i = 0;
			for (Sample sample : Utils.listSamples(data)) {
				final Column columnSample = new Column(String.valueOf(sample.getSampleIdentifier()));
				columnSample.setWidth("80px");
				
				final Comparator<Variable> rowComparator = this.model.getRowComparator(i++);
				columnSample.setSortAscending(rowComparator);
				columnSample.setSortDescending(rowComparator);
				this.columns.appendChild(columnSample);
				
				final Auxheader headerSample = new Auxheader(String.valueOf(sample.getVariableValue(classVariable)));
				headerSample.setWidth("80px");
				this.header.appendChild(headerSample);
			}
		}
		
		this.invalidate();
	}
	
	public void setDataSet(DataSetMetaData dataSet) throws IllegalArgumentException, Exception {
		if (dataSet == null) {
			this.setData(null);
		} else {
			this.setData(dataSet.loadData());
		}
	}
	
	public Data getData() {
		return this.model.getData();
	}
	
	public void setData(Data data) throws IllegalArgumentException {
		if (data != this.model.getData()) {
			this.updateColumns(data);
			this.model.setData(data);
		}
	}
	
	public static class DataListModel extends AbstractListModel<Variable> implements Sortable<Variable> {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;
		
		private Data data;
		private Variable classVariable;
		private final Map<Integer, Variable> indexMap;
		private final boolean includeClass;
		
		public DataListModel() {
			this(null, false);
		}

		public DataListModel(boolean includeClass) {
			this(null, includeClass);
		}
		
		public DataListModel(Data data) {
			this(data, false);
		}
		
		public DataListModel(Data data, boolean includeClass) {
			this.indexMap = new Hashtable<Integer, Variable>();
			this.includeClass = includeClass;
			
			this.setData(data);
		}
		
		public void setData(Data data) {
			if (this.data != data) {
				this.data = data;
				this.indexMap.clear();
				
				if (this.data == null) {
					this.classVariable = null;
				} else {
					this.classVariable = Utils.getClassVariable(this.data);
					
					int i = 0;
					if (this.includeClass)
						this.indexMap.put(i++, this.classVariable);
					
					for (Variable variable : Utils.listVariables(this.data)) {
						if (!variable.equals(this.classVariable)) {
							this.indexMap.put(i++, variable);
						}
					}
				}
				
				this.fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);
			}
		}
		
		public Data getData() {
			return data;
		}

		@Override
		public Variable getElementAt(int index) {
			return this.indexMap.get(index);
		}

		@Override
		public int getSize() {
			if (this.data == null) {
				return 0;
			} else {
				if (this.includeClass)
					return this.data.getVariableCount();
				else 
					return this.data.getVariableCount() - 1;
			}
		}
		
		public Comparator<Variable> getVariableComparator() {
			return new Comparator<Variable>() {
				@Override
				public int compare(Variable o1, Variable o2) {
					if (o1.equals(DataListModel.this.classVariable)) {
						return -1;
					} else if (o2.equals(DataListModel.this.classVariable)) {
						return 1;
					} else {
						return o1.compareTo(o2);
					}
				}
			};
		}
		
		public Comparator<Variable> getRowComparator(int index) {
			return new RowComparator(index);
		}
		
		public class RowComparator implements Comparator<Variable> {
			private final int index;
			private Sample sample;
			
			public RowComparator(int index) {
				this.index = index;
			}
			
			private Sample getSample() {
				if (this.sample == null) {
					synchronized(this) {
						if (this.sample == null)
							this.sample = DataListModel.this.data.getSampleAt(index);
					}
				}
				
				return this.sample;
			}

			@Override
			public int compare(Variable o1, Variable o2) {
				if (o1.equals(DataListModel.this.classVariable)) {
					return -1;
				} else if (o2.equals(DataListModel.this.classVariable)) {
					return 1;
				} else if (o1.getType() == Variable.TYPE_FLOAT && o2.getType() == Variable.TYPE_FLOAT) {
					return Float.compare(
						((Float) this.getSample().getVariableValue(o1)), 
						((Float) this.getSample().getVariableValue(o2))
					);
				} else {
					return String.valueOf(this.getSample().getVariableValue(o1)).compareTo(
						String.valueOf(this.getSample().getVariableValue(o2))
					);
				}
			}
		}

		@Override
		public void sort(Comparator<Variable> cmpr, boolean ascending) {
			final List<Variable> variables = new ArrayList<Variable>(this.indexMap.values());
			if (ascending) {
				Collections.sort(variables, cmpr);			
			} else {
				Collections.sort(variables, Collections.reverseOrder(cmpr));
				if (this.includeClass)
					variables.add(0, variables.remove(variables.size() - 1));
			}
			
			this.indexMap.clear();
			for (int i = 0; i < variables.size(); i++) {
				this.indexMap.put(i, variables.get(i));
			}
			
			this.fireEvent(ListDataEvent.CONTENTS_CHANGED, -1, -1);
		}

		@Override
		public String getSortDirection(Comparator<Variable> cmpr) {
			return "natural";
		}
	}
	
	public static class DataRowRenderer implements RowRenderer<Variable> {
		@Override
		public void render(Row row, Variable variable, int index) throws Exception {
			final Cell cellVariable = new Cell();
			cellVariable.setSclass("firstColumn");
			cellVariable.setHeight("12px");
			
			final Label lblVariable = new Label(variable.getName());
			lblVariable.setSclass("firstColumnText");
			
			cellVariable.appendChild(lblVariable);
			row.appendChild(cellVariable);
			
			final int numSamples = variable.getData().getSampleCount();
			for (int i = 0; i < numSamples; i++) {
				final Sample sample = variable.getData().getSampleAt(i);
				
				final Object value = sample.getVariableValue(variable);
				final Label label;
				if (value == null) {
					label = new Label();
				} else {
					if (variable.getType() == Variable.TYPE_FLOAT) {
						label = new Label(String.format("%.4f", value));
					} else {						
						label = new Label(value.toString());
					}
				}
				final Cell cell = new Cell();
				cell.setHeight("12px");
				cell.appendChild(label);
				cell.setAlign("center");
				row.appendChild(cell);
			}
		}
	}
}
