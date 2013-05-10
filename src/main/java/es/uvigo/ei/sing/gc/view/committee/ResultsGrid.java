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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import es.uvigo.ei.sing.gc.execution.PerformanceFunction;
import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
import es.uvigo.ei.sing.gc.view.ZKUtils;

public class ResultsGrid extends Grid {
	private static final long serialVersionUID = 1L;
	
	static {
		AbstractComponent.addClientEvent(ResultsGrid.class, Events.ON_SELECTION, 0);
	}

	private PerformanceFunction performanceFunction;
	private String performanceClass;
	private String sortClassifier; 
	private boolean sortAscending; 
	private ExperimentMetaData experiment;
	private boolean autoPerformanceUpdate;
	private boolean performanceUpdated;
	private boolean editable;
	
	private final SortedSet<String> classifierNames;
	private final Map<String, String> geneIdToName;
	private final SortedMap<String, Map<String, String>> values;
	private final SortedMap<String, Map<String, ExpertResult>> results;
	private final SortedMap<String, Map<String, Label>> labels;
	private final SortedMap<String, Map<String, SelectionEventListener>> eventListeners;
	
	public ResultsGrid() {
		this.performanceClass = PerformanceFunction.ALL_CLASSES_LABEL;
		this.performanceFunction = PerformanceFunction.KAPPA;
		
		this.editable = false;
		this.classifierNames = new TreeSet<String>();
		this.geneIdToName = new HashMap<String, String>();
		this.values = new TreeMap<String, Map<String, String>>();
		this.results = new TreeMap<String, Map<String, ExpertResult>>();
		this.labels = new TreeMap<String, Map<String, Label>>();
		this.eventListeners = new TreeMap<String, Map<String, SelectionEventListener>>();
		
		this.sortClassifier = null;
		this.sortAscending = true;
		
		final Frozen frozen = new Frozen();
		frozen.setColumns(1);
		this.appendChild(frozen);
	}
	
	private class ValuesComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			final String sortClassifier = ResultsGrid.this.sortClassifier;
			
			if (sortClassifier == null) {
				final String name1 = ResultsGrid.this.geneIdToName.get(o1);
				final String name2 = ResultsGrid.this.geneIdToName.get(o2);
				
				final int cmp = name1.compareTo(name2);
				return cmp == 0 ? o1.compareTo(o2) : cmp;
			} else {
				final String value1 = ResultsGrid.this.values.get(o1).get(sortClassifier);
				final String value2 = ResultsGrid.this.values.get(o2).get(sortClassifier);
				
				if (value1.equals(value2)) {
					final String name1 = ResultsGrid.this.geneIdToName.get(o1);
					final String name2 = ResultsGrid.this.geneIdToName.get(o2);
					
					final int cmp = name1.compareTo(name2);
					return cmp == 0 ? o1.compareTo(o2) : cmp;
				} else if (value1.equals("ERROR")) {
					return -1;
				} else if (value2.equals("ERROR")) {
					return 1;
				} else {
					final double dValue1 = ResultsGrid.this.performanceFunction.unformat(value1);
					final double dValue2 = ResultsGrid.this.performanceFunction.unformat(value2);
					
					return Double.compare(dValue1, dValue2);
				}
			}
		}
	}
	
	protected void updateSorting(String classifierName) {
		if (this.sortClassifier == null) {
			this.sortClassifier = classifierName;
			this.sortAscending = false;
		} else if (this.sortClassifier.equals(classifierName)) {
			if (this.sortAscending) {
				this.sortClassifier = null;
			} else {
				this.sortAscending = true;
			}
		} else {
			this.sortClassifier = classifierName;
			this.sortAscending = false;
		}
	}
	
	public boolean getAutoPerformanceUpdate() {
		return this.autoPerformanceUpdate;
	}
	
	public void setAutoPerformanceUpdate(boolean autoPerformanceUpdate) {
		this.autoPerformanceUpdate = autoPerformanceUpdate;
	}
	
	public PerformanceFunction getPerformanceFunction() {
		return this.performanceFunction;
	}

	public synchronized void setPerformanceFunction(PerformanceFunction performanceFunction) {
		if (this.performanceFunction != performanceFunction) {
			this.performanceFunction = performanceFunction;
			
			if (this.autoPerformanceUpdate) {
				this.fillValues(false, true);
				this.fullRebuildUI();
			} else {
				this.performanceUpdated = true;
			}
		}
	}

	public String getPerformanceClass() {
		return performanceClass;
	}

	public synchronized void setPerformanceClass(String performanceClass) {
		if (performanceClass == null) {
			throw new IllegalArgumentException("performanceClass can not be null");
		} else if (!this.performanceClass.equals(performanceClass)) {
			this.performanceClass = performanceClass;
			
			if (this.autoPerformanceUpdate) {
				this.fillValues(false, true);
				this.fullRebuildUI();
			} else {
				this.performanceUpdated = true;
			}
		}
	}
	
	public void setEditable(boolean editable) {
		if (this.editable != editable) {
			this.editable = editable;
			
			this.fullRebuildUI();
		}
	}
	
	public boolean isEditable() {
		return this.editable;
	}

	public ExperimentMetaData getExperiment() {
		return experiment;
	}

	public synchronized void setExperiment(ExperimentMetaData experiment) {
		if (this.experiment == null && experiment == null) return;
		
		if ((this.experiment != null && experiment == null) ||
			(this.experiment == null && experiment != null) ||
			(this.experiment.getId() != experiment.getId())
		) {
			this.experiment = experiment;
			
			this.fillValues(true, this.performanceUpdated);
			this.rebuildUI();
			
		} else {
			final boolean experimentChanged = !this.experiment.getResults().equals(experiment.getResults());
			this.experiment = experiment;
			
			this.fillValues(experimentChanged, this.performanceUpdated);
			this.rebuildUI();
		}
		
		this.performanceUpdated = false;
	}

	private synchronized void fillValues(boolean experimentChanged, boolean performanceChanged) {
		if (this.experiment == null) {
			this.values.clear();
			this.results.clear();
			this.classifierNames.clear();
			this.geneIdToName.clear();
			this.labels.clear();
		} else {
			if (experimentChanged) {
				this.values.clear();
				this.results.clear();
				this.classifierNames.clear();
				this.geneIdToName.clear();
				this.labels.clear();
				this.eventListeners.clear();
				
				for (ClassifierBuilderMetaData cbMetadata : this.experiment.getCommittee().getClassifiers()) {
					this.classifierNames.add(cbMetadata.getName());
				}
				
				for (GeneSetMetaData gsMetaData : this.experiment.getCommittee().getSelectedGeneSets()) {
					this.geneIdToName.put(gsMetaData.getGeneSetId(), gsMetaData.getName());
					
					this.values.put(gsMetaData.getGeneSetId(), new HashMap<String, String>());
					this.results.put(gsMetaData.getGeneSetId(), new HashMap<String, ExpertResult>());
				}
				
				for (ExpertResult result : this.experiment.getResults()) {
					final String classifierName = result.getClassifierName();
					final String geneSetId = result.getGeneSetId();
					
					this.results.get(geneSetId).put(classifierName, result);
					if (result.isAborted()) {
						this.values.get(geneSetId).put(
							classifierName, 
							"ERROR"
						);
					} else {
						this.values.get(geneSetId).put(
							classifierName, 
							this.performanceFunction.format(this.performanceFunction.calculate(
								result.getClassificationPerformance(), 
								this.performanceClass
							))
						);
					}
				}
			} else {
				for (ExpertResult result : this.experiment.getResults()) {
					final String classifierName = result.getClassifierName();
					final String geneSetId = result.getGeneSetId();
					
					if (!this.results.get(geneSetId).containsKey(classifierName)) {
						this.results.get(geneSetId).put(classifierName, result);
						
						if (result.isAborted()) {
							this.values.get(geneSetId).put(
								classifierName, 
								"ERROR"
							);
						} else {
							this.values.get(geneSetId).put(
								classifierName, 
								this.performanceFunction.format(this.performanceFunction.calculate(
									result.getClassificationPerformance(), 
									this.performanceClass
								))
							);
						}
					} else if (performanceChanged && result.isPerformance()) {
						this.values.get(geneSetId).put(
							classifierName, 
							this.performanceFunction.format(this.performanceFunction.calculate(
								result.getClassificationPerformance(), 
								this.performanceClass
							))
						);
					}
				}
			}
		}
	}
	
	protected synchronized void fullRebuildUI() {
		this.labels.clear();
		this.eventListeners.clear();
		this.rebuildUI();
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void rebuildUI() {
		if (this.experiment == null) {
			ZKUtils.emptyComponent(this);
			
			this.setEmptyMessage("No experiment");
			
			this.invalidate();
		} else {
			final SortedMap<String, Map<String, String>> gridValues;
			
			// When the evaluation has finished, the results are sorted
			if (this.editable) {
				Comparator<String> comparator = new ValuesComparator();
				
				if (!this.sortAscending) {
					comparator = Collections.reverseOrder(comparator);
				}
				
				gridValues = new TreeMap<String, Map<String,String>>(comparator);
				gridValues.putAll(this.values);
			} else {
				gridValues = this.values;
			}
			
			if (this.labels.isEmpty()) {
				ZKUtils.emptyComponent(this, Columns.class, Rows.class);
				
				final Columns columns = new Columns();
				columns.setSizable(this.editable);
				columns.appendChild(new Column("Gene Set"));
			
				for (final String classifierName : this.classifierNames) {
					final Column column = new Column(classifierName);
					columns.appendChild(column);
					
					if (this.editable) {
						try {
							column.setSort("auto");
							if (classifierName.equals(this.sortClassifier)) {
								column.setSortDirection(this.sortAscending?"ascending":"descending");
							}
							
							column.addEventListener(Events.ON_SORT, new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									ResultsGrid.this.updateSorting(classifierName);
									ResultsGrid.this.fullRebuildUI();
									
									event.stopPropagation();
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				final Rows rows = new Rows();
				for (Map.Entry<String, Map<String, String>> rowValues : gridValues.entrySet()) {
					final String geneSetId = rowValues.getKey();
					
					final Row row = new Row();
					row.appendChild(new Label(this.geneIdToName.get(geneSetId)));

					final Map<String, Label> rowLabelsMap = new HashMap<String, Label>();
					
					this.labels.put(geneSetId, rowLabelsMap);
					
					for (String classifierName : this.classifierNames) {
						final Label label;
						
						if (rowValues.getValue().containsKey(classifierName)) {
							label = new Label(rowValues.getValue().get(classifierName));
						} else {
							label = new Label();
						}
						
						final Cell cell = new Cell();
						cell.appendChild(label);
						row.appendChild(cell);
						
						rowLabelsMap.put(classifierName, label);
					}
					
					rows.appendChild(row);
				}
				
				this.appendChild(columns);
				this.appendChild(rows);
				
				this.invalidate();
			} else {
				for (Map.Entry<String, Map<String, String>> rowValues : gridValues.entrySet()) {
					final Map<String, Label> rowLabelsMap = this.labels.get(rowValues.getKey());
					final Map<String, String> rowValuesMap = rowValues.getValue();
					
					for (String classifierName : this.classifierNames) {
						final String value = rowValuesMap.get(classifierName);
						final Label label = rowLabelsMap.get(classifierName);
						
						if ((value == null && !label.getValue().isEmpty()) ||
							(value != null && !label.getValue().equals(value))
						) {
							label.setValue(value);
						}
					}
				}
			}
			
			if (this.editable) {
				if (this.eventListeners.isEmpty()) {
					for (Map.Entry<String, Map<String, ExpertResult>> rowResults : this.results.entrySet()) {
						final String geneSetId = rowResults.getKey();
						
						final Map<String, SelectionEventListener> rowELMap = new HashMap<String, SelectionEventListener>();
						this.eventListeners.put(geneSetId, rowELMap);
						
						for (Map.Entry<String, ExpertResult> resultEntry : rowResults.getValue().entrySet()) {
							final ExpertResult result = resultEntry.getValue();
							
							if (result.isPerformance()) {
								final String classifierName = resultEntry.getKey();
								
								final Label label = this.labels.get(geneSetId).get(classifierName);
								rowELMap.put(classifierName, new SelectionEventListener(label, result));
							}
						}
					}
				}
			} else {
				if (!this.eventListeners.isEmpty()) {
					for (Map<String, SelectionEventListener> rowELMap : this.eventListeners.values()) {
						
						for (SelectionEventListener listener : rowELMap.values()) {
							listener.remove();
						}
					}
					
					this.eventListeners.clear();
				}
			}
		}
	}
	
	private class SelectionEventListener implements EventListener<Event> {
		private final Label label;
		private final Cell cell;
		private final ExpertResult result;
		
		public SelectionEventListener(Label label, ExpertResult result) {
			super();
			this.label = label;
			this.cell = (Cell) label.getParent();
			this.result = result;
			
			this.updateStyles();
			this.cell.addEventListener(Events.ON_CLICK, this);
		}
		
		public void remove() {
			this.cell.removeEventListener(Events.ON_CLICK, this);
		}
		
		private void updateStyles() {
			if (this.result.isSelected()) {
				this.label.setStyle("cursor: pointer; font-weight: bold;");
				this.cell.setStyle("cursor: pointer; background-color: #ABC8E2; font-weight: bold;");
			} else {
				this.label.setStyle("cursor: pointer;");
				this.cell.setStyle("cursor: pointer;");
			}
		}

		@Override
		public void onEvent(Event event) throws Exception {
			this.result.setSelected(!this.result.isSelected());
			
			this.updateStyles();
			
			Events.postEvent(Events.ON_SELECTION, ResultsGrid.this, this.result);
		}
	}
}
