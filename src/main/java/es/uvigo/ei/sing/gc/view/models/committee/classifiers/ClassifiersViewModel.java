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
package es.uvigo.ei.sing.gc.view.models.committee.classifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import es.uvigo.ei.sing.gc.utils.HibernateUtil;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
import es.uvigo.ei.sing.gc.model.entities.Committee;
import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
import es.uvigo.ei.sing.gc.model.entities.DataSetMetaData;
import es.uvigo.ei.sing.gc.model.entities.EvaluationStrategy;
import es.uvigo.ei.sing.gc.model.entities.ExpertsEvaluatorMetaData;
import es.uvigo.ei.sing.gc.view.ZKUtils;
import es.uvigo.ei.sing.gc.view.committee.ClassifierTypes;
import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;

public class ClassifiersViewModel extends CommitteeViewModel {
	private ClassifierTypes currentClassifierType;
	private Integer selectedClassifierBuilderId;
	private AbstractValidator nameValidator;
	
	@Init
	@Override
	public void init() {
		super.init();
		
		final Committee committee = this.getCommittee(false); // Refreshed in init
		
		if (!committee.hasEvaluator()) {
			final Session session = HibernateUtil.currentSession();
			
			final Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("stratified", "true");
			parameters.put("replicationMode", "VIEW");
			parameters.put("k", "5");
			
			final ExpertsEvaluatorMetaData evaluator = new ExpertsEvaluatorMetaData(
				EvaluationStrategy.XValidation,
				parameters
			);
			
			session.persist(evaluator);
			
			for (ClassifierTypes classifier : ClassifierTypes.values()) {
				try {
					final ClassifierBuilderMetaData cbmd = new ClassifierBuilderMetaData(
						classifier.toString(),
						classifier.getBuilder()
					);
					
					session.persist(cbmd);
					
					committee.getClassifiers().add(cbmd);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			session.flush();
			
			
			committee.setEvaluator(evaluator);
			session.persist(committee);
			session.flush();
		}
	
		this.currentClassifierType = ClassifierTypes.IBk;
		this.nameValidator = new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				final String name = (String) ctx.getProperty().getValue();
				
				final Committee committee = ClassifiersViewModel.this.getCommittee();
				for (ClassifierBuilderMetaData cbMetaData : committee.getClassifiers()) {
					if (cbMetaData.getName().equals(name)) {
						this.addInvalidMessage(ctx, "name", "Classifier names can not be repeated");
					}
				}
			}
		};
		
		StatusViewModel.changeInitialStatus("Select the base classifiers and an evaluation method");
	}
	
	public DataSetMetaData getDataSetMetaData() {
		return this.getCommittee().getDataSet();
	}
	
	public List<ClassifierTypes> getClassifierTypes() {
		return Arrays.asList(ClassifierTypes.values());
	}
	
	public ClassifierTypes getCurrentClassifierType() {
		return currentClassifierType;
	}

	public void setCurrentClassifierType(ClassifierTypes currentClassifierType) {
		this.currentClassifierType = currentClassifierType;
	}
	
	public List<ClassifierBuilderMetaData> getClassifiers() {
		final List<ClassifierBuilderMetaData> classifiers =
			new ArrayList<ClassifierBuilderMetaData>(this.getCommittee().getClassifiers());
		
		Collections.sort(classifiers, new Comparator<ClassifierBuilderMetaData>(){
			@Override
			public int compare(ClassifierBuilderMetaData o1, ClassifierBuilderMetaData o2) {
				return o1.getId() - o2.getId();
			}
		});
		
		return classifiers;
	}
	
	public ClassifierBuilderMetaData getSelectedClassifierBuilder() {
		if (this.selectedClassifierBuilderId == null) {
			return null;
		} else {
			return ZKUtils.hLoad(
				ClassifierBuilderMetaData.class, 
				this.selectedClassifierBuilderId, 
				HibernateUtil.currentSession()
			).clone();
		}
	}

	public void setSelectedClassifierBuilder(
		ClassifierBuilderMetaData selectedClassifierBuilder
	) {
		this.selectedClassifierBuilderId = selectedClassifierBuilder.getId();
	}
	
	public ExpertsEvaluatorMetaData getExpertsEvaluator() {
		return this.getCommittee().getEvaluator().clone();
	}

	@Command
	public void updateExpertsEvaluator(
		@BindingParam("evaluator") final ExpertsEvaluatorMetaData evaluator
	) {
		this.dataLostNotification(
			"Evaluation Modification", 
			"Modifying the evaluation method will provoke data deletion. Do you wish to continue?", 
			CommitteeStatus.EXPERIMENT_EXECUTION,
			CommitteeStatus.EVALUATOR,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					final ExpertsEvaluatorMetaData currentEvaluator = 
						ZKUtils.hLoad(ExpertsEvaluatorMetaData.class, evaluator.getId(), session);
					currentEvaluator.copyValuesOf(evaluator);
					
					session.update(currentEvaluator);
					
					BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "expertsEvaluator");
				}
			},
			new Runnable() {
				@Override
				public void run() {
					BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "expertsEvaluator");
				}
			}
		);
	}
	
	@Command
	public void updateSelectedClassifierBuilder(
		@BindingParam("classifierBuilder") final ClassifierBuilderMetaData classifierBuilder
	) {
		this.dataLostNotification(
			"Classifier Modification", 
			"Modifying classifier parameters will provoke data deletion. Do you wish to continue?", 
			CommitteeStatus.EXPERIMENT_EXECUTION,
			CommitteeStatus.EVALUATOR,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					final ClassifierBuilderMetaData currentClassifierBuilder = 
						ZKUtils.hLoad(ClassifierBuilderMetaData.class, classifierBuilder.getId(), session);
					currentClassifierBuilder.copyValuesOf(classifierBuilder);
					
					session.update(currentClassifierBuilder);
					
					BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "selectedClassifierBuilder");
				}
			},
			new Runnable() {
				@Override
				public void run() {
					BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "selectedClassifierBuilder");
				}
			}
		);
	}
	
	public Validator getClassifierNameValiator() {
		return nameValidator;
	}
	
	@Command("addClassifier")
	public void addClassifier() {
		final ClassifierTypes classifierType = this.getCurrentClassifierType();
		
		this.dataLostNotification(
			"Add Classifier", 
			"Adding a new classifier will provoke data deletion. Do you wish to continue?", 
			CommitteeStatus.EXPERIMENT_EXECUTION,
			CommitteeStatus.EVALUATOR,
			new Runnable() {
				@Override
				public void run() {
					try {
						final Committee committee = ClassifiersViewModel.this.getCommittee();
						
						final Set<String> names = new HashSet<String>();
						for (ClassifierBuilderMetaData cbMetaData : committee.getClassifiers()) {
							names.add(cbMetaData.getName());
						}
						
						String name = classifierType.toString();
						int i = 1;
						while (names.contains(name)) {
							name = classifierType.toString() + " [" + i++ + "]";
						}
						
						final ClassifierBuilderMetaData classifierBuilder = new ClassifierBuilderMetaData(name, classifierType.getBuilder());
						final Session session = HibernateUtil.currentSession();
						session.persist(classifierBuilder);
						session.flush();
						
						committee.getClassifiers().add(classifierBuilder);
						
						session.update(committee);

						BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "classifiers");
						ClassifiersViewModel.this.updateExecutions();
					} catch (Exception e) {
						e.printStackTrace();
						Messagebox.show("Error creating new classifier", "Error", Messagebox.OK, Messagebox.ERROR);
					}
				}
			}
		);
	}
	
	@Command("deleteClassifier")
	public void deleteClassifier(@BindingParam("classifier") final ClassifierBuilderMetaData classifier) {
		this.dataLostNotification(
			"Delete Classifier", 
			String.format("Deleting this classifier will provoke data deletion. Are you sure you want to delete the %s classifier?", classifier.getName()), 
			CommitteeStatus.EXPERIMENT_EXECUTION,
			CommitteeStatus.EVALUATOR,
			new Runnable() {
				@Override
				public void run() {
					final Session session = HibernateUtil.currentSession();
					final Committee committee = ClassifiersViewModel.this.getCommittee();
					
					session.refresh(classifier);
					committee.getClassifiers().remove(classifier);
					session.delete(classifier);
					session.update(committee);
					
					BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "classifiers");
					ClassifiersViewModel.this.updateExecutions();
				}
			},
			null,
			new Runnable() {
				@Override
				public void run() {
					Messagebox.show(
						String.format("Are you sure you want to delete the %s classifier?", classifier.getName()),
						"Delete Classifier", 
						Messagebox.YES | Messagebox.NO, 
						Messagebox.EXCLAMATION, 
						new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								if (((Integer) event.getData()).intValue() == Messagebox.YES) {
									final Session session = HibernateUtil.currentSession();
									final Committee committee = ClassifiersViewModel.this.getCommittee();
									
									session.refresh(classifier);
									committee.getClassifiers().remove(classifier);
									session.delete(classifier);
									session.update(committee);
									
									BindUtils.postNotifyChange(null, null, ClassifiersViewModel.this, "classifiers");
									ClassifiersViewModel.this.updateExecutions();
								}
							}
						}
					);
				};
			}
		);
	}
	
	@Command("updateClassifier")
	@NotifyChange("classifiers")
	public void updateClassifier(@BindingParam("classifier") ClassifierBuilderMetaData classifier) {
		if (!this.isUserTaskRunning())
			HibernateUtil.currentSession().merge(classifier);
	}
	
	@Command("editClassifier")
	@NotifyChange("selectedClassifierBuilder")
	public void editClassifier(@BindingParam("classifier") ClassifierBuilderMetaData classifier) {
		if (!this.isUserTaskRunning()) {
			HibernateUtil.currentSession().refresh(classifier);
			this.setSelectedClassifierBuilder(classifier);
		}
	}
	
	@Override
	public String getCurrentStep() {
		return CommitteeViewModel.CLASSIFIERS_STEP;
	}
	
	@Override
	public String getNextHref() {
		return CommitteeViewModel.EVALUATION_PATH;
	}
	
	@Override
	public String getPreviousHref() {
		return CommitteeViewModel.ENRICHMENT_PATH;
	}
	
	@Override
	@DependsOn("classifiersCompleted")
	public boolean getHasNext() {
		return this.isClassifiersCompleted();
	}

	@Override
	protected CommitteeStatus stepStatus() {
		return CommitteeStatus.EVALUATOR;
	}
}
