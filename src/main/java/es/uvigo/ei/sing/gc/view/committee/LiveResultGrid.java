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

public class LiveResultGrid extends Grid {
	private static final long serialVersionUID = 1L;
	
	static {
		AbstractComponent.addClientEvent(LiveResultGrid.class, Events.ON_SELECTION, 0);
	}

	private PerformanceFunction performanceFunction;
	private String performanceClass;
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
	
	public LiveResultGrid() {
		this.performanceClass = PerformanceFunction.ALL_CLASSES_LABEL;
		this.performanceFunction = PerformanceFunction.KAPPA;
		
		this.editable = false;
		this.classifierNames = new TreeSet<String>();
		this.geneIdToName = new HashMap<String, String>();
		this.values = new TreeMap<String, Map<String, String>>();
		this.results = new TreeMap<String, Map<String, ExpertResult>>();
		this.labels = new TreeMap<String, Map<String, Label>>();
		this.eventListeners = new TreeMap<String, Map<String, SelectionEventListener>>();
		
		final Frozen frozen = new Frozen();
		frozen.setColumns(1);
		this.appendChild(frozen);
	}
	
	public synchronized boolean getAutoPerformanceUpdate() {
		return this.autoPerformanceUpdate;
	}
	
	public synchronized void setAutoPerformanceUpdate(boolean autoPerformanceUpdate) {
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
				this.rebuildUI();
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
				this.rebuildUI();
			} else {
				this.performanceUpdated = true;
			}
		}
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		
		this.rebuildUI();
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
				
				for (GeneSetMetaData gsMetaData : this.experiment.getCommittee().getGeneSets()) {
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
	
	
	
	@SuppressWarnings("unchecked")
	private synchronized void rebuildUI() {
		if (this.experiment == null || this.values.isEmpty()) {
			ZKUtils.emptyComponent(this);
			
			this.setEmptyMessage("No experiment");
			
			this.invalidate();
		} else {
			if (this.labels.isEmpty()) {
				ZKUtils.emptyComponent(this, Columns.class, Rows.class);
				
				final Columns columns = new Columns();
				columns.appendChild(new Column("Gene Set"));
			
				for (String columnName : this.classifierNames) {
					columns.appendChild(new Column(columnName));
				}
				
				final Rows rows = new Rows();
				for (Map.Entry<String, Map<String, String>> rowValues : this.values.entrySet()) {
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
				for (Map.Entry<String, Map<String, String>> rowValues : this.values.entrySet()) {
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
				this.cell.setStyle("cursor: pointer; background-color: #AED; font-weight: bold;");
			} else {
				this.label.setStyle("cursor: pointer;");
				this.cell.setStyle("cursor: pointer;");
			}
		}

		@Override
		public void onEvent(Event event) throws Exception {
			this.result.setSelected(!this.result.isSelected());
			
			this.updateStyles();
			
			Events.postEvent(Events.ON_SELECTION, LiveResultGrid.this, this.result);
		}
	}
}
