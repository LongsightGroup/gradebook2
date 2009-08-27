/**********************************************************************************
 *
 * $Id:$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Regents of the University of California
 *
 * Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.osedu.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.gradebook.gwt.client.gxt.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.Gradebook2RPCServiceAsync;
import org.sakaiproject.gradebook.gwt.client.SecureToken;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction.ClassType;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradeRecordUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemCreate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ShowColumnsEvent;
import org.sakaiproject.gradebook.gwt.client.model.ConfigurationModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServiceController extends Controller {

	public static final String FAILED_FLAG = ":F";

	public ServiceController() {
		registerEventTypes(GradebookEvents.CreateItem.getEventType());
		registerEventTypes(GradebookEvents.DeleteItem.getEventType());
		registerEventTypes(GradebookEvents.RevertItem.getEventType());
		registerEventTypes(GradebookEvents.ShowColumns.getEventType());
		registerEventTypes(GradebookEvents.UpdateLearnerGradeRecord.getEventType());
		registerEventTypes(GradebookEvents.UpdateItem.getEventType());
	}

	@Override
	public void handleEvent(AppEvent<?> event) {
		switch (GradebookEvents.getEvent(event.type).getEventKey()) {
			case CREATE_ITEM:
				onCreateItem((ItemCreate)event.data);
				break;
			case DELETE_ITEM:
				onDeleteItem((ItemUpdate)event.data);
				break;
			case REVERT_ITEM:
				onRevertItem((ItemUpdate)event.data);
				break;
			case SHOW_COLUMNS:
				onShowColumns((ShowColumnsEvent)event.data);
				break;
			case UPDATE_LEARNER_GRADE_RECORD:
				onUpdateGradeRecord((GradeRecordUpdate)event.data);
				break;
			case UPDATE_ITEM:
				onUpdateItem((ItemUpdate)event.data);
				break;
		}
	}

	private void onCreateItem(final ItemCreate event) {
		Dispatcher.forwardEvent(GradebookEvents.MaskItemTree.getEventType());
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);

		EntityType entityType = EntityType.GRADE_ITEM;

		if (event.item.getItemType() == Type.CATEGORY)
			entityType = EntityType.CATEGORY;

		Gradebook2RPCServiceAsync service = Registry.get("service");
		AsyncCallback<ItemModel> callback = new AsyncCallback<ItemModel>() {

			public void onFailure(Throwable caught) {

				Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught, "Failed to create item: "));
				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}

			public void onSuccess(ItemModel result) {
				if (event.close)
					Dispatcher.forwardEvent(GradebookEvents.HideFormPanel.getEventType(), Boolean.FALSE);

				switch (result.getItemType()) {
					case GRADEBOOK:
						GradebookModel selectedGradebook = Registry
						.get(AppConstants.CURRENT);
						selectedGradebook.setGradebookItemModel(result);
						Dispatcher
						.forwardEvent(GradebookEvents.ItemUpdated.getEventType(), result);
						Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(),
								selectedGradebook);
						break;
					case CATEGORY:
						if (result.isActive())
							doCreateItem(event, result);
						else
							doUpdateItem(event.store, null, null, result);

						for (ItemModel item : result.getChildren()) {
							if (item.isActive())
								doCreateItem(event, item);
							else
								doUpdateItem(event.store, null, null, item);
						}
						break;
					case ITEM:
						if (result.isActive())
							doCreateItem(event, result);
						else
							doUpdateItem(event.store, null, null, result);
						break;
				}

				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}
		};		

		service.create(selectedGradebook.getGradebookUid(), selectedGradebook.getGradebookId(), event.item, entityType, SecureToken.get(), callback);
	}

	private void onDeleteItemSuccess(ItemUpdate event) {
		Dispatcher.forwardEvent(GradebookEvents.ItemDeleted.getEventType(), event.item);
		TreeStore<ItemModel> treeStore = (TreeStore<ItemModel>)event.store;
		treeStore.remove(event.item.getParent(), event.item);
	}

	private void onDeleteItem(final ItemUpdate event) {
		Dispatcher.forwardEvent(GradebookEvents.MaskItemTree.getEventType());
		event.item.setRemoved(Boolean.TRUE);

		Gradebook2RPCServiceAsync service = Registry.get("service");
		AsyncCallback<ItemModel> callback = new AsyncCallback<ItemModel>() {

			public void onFailure(Throwable caught) {
				onUpdateItemFailure(event, caught);
				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}

			public void onSuccess(ItemModel result) {
				onUpdateItemSuccess(event, result);
				onDeleteItemSuccess(event);
				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}
		};

		service.update((ItemModel)event.item, EntityType.ITEM, null, SecureToken.get(), callback);
	}

	private void onUpdateGradeRecordSuccess(GradeRecordUpdate event, StudentModel result) {
		Record record = event.record;
		String property = event.property;

		// Need to refresh any items that may have been dropped
		for (String p : result.getPropertyNames()) {
			boolean needsRefreshing = false;

			int index = -1;

			if (p.endsWith(StudentModel.DROP_FLAG)) {
				index = p.indexOf(StudentModel.DROP_FLAG);
				needsRefreshing = true;
			} else if (p.endsWith(StudentModel.COMMENTED_FLAG)) {
				index = p.indexOf(StudentModel.COMMENTED_FLAG);
				needsRefreshing = true;
			}

			if (needsRefreshing && index != -1) {
				String assignmentId = p.substring(0, index);
				Object value = result.get(assignmentId);
				Boolean recordFlagValue = (Boolean)record.get(p);
				Boolean resultFlagValue = result.get(p);

				boolean isDropped = resultFlagValue != null && resultFlagValue.booleanValue();
				boolean wasDropped = recordFlagValue != null && recordFlagValue.booleanValue();

				record.set(p, resultFlagValue);

				if (isDropped || wasDropped) {
					record.set(assignmentId, null);
					record.set(assignmentId, value);
				}
			}
		}

		String courseGrade = result.get(StudentModel.Key.COURSE_GRADE.name());

		record.set(StudentModel.Key.COURSE_GRADE.name(), null);
		if (courseGrade != null) 
			record.set(StudentModel.Key.COURSE_GRADE.name(), courseGrade);
		
		String calculatedGrade = result.get(StudentModel.Key.CALCULATED_GRADE.name());
		record.set(StudentModel.Key.CALCULATED_GRADE.name(), null);
		if (calculatedGrade != null)
			record.set(StudentModel.Key.CALCULATED_GRADE.name(), calculatedGrade);
		
		String letterGrade = result.get(StudentModel.Key.LETTER_GRADE.name());
		record.set(StudentModel.Key.LETTER_GRADE.name(), null);
		if (letterGrade != null)
			record.set(StudentModel.Key.LETTER_GRADE.name(), letterGrade);

		// Ensure that we clear out any older failure messages
		// Save the exception message on the record
		String failedProperty = property + FAILED_FLAG;
		record.set(failedProperty, null);

		record.setValid(property, true);

		Object value = result.get(property);

		if (value == null)
			record.set(property, null);
		else
			record.set(property, value);

		// FIXME: Move all this to a log event listener
		StringBuilder buffer = new StringBuilder();
		String displayName = (String)record.get(StudentModel.Key.DISPLAY_NAME.name());
		if (displayName != null)
			buffer.append(displayName);
		buffer.append(":").append(event.label);
		//		"Stored item grade as '{0}' and recalculated course grade to '{1}' ", result.get(property), result.get(StudentModel.Key.COURSE_GRADE.name()));

		String message = null;
		if (property.endsWith(StudentModel.COMMENT_TEXT_FLAG)) {
			message = buffer.append("- stored comment as '")
			.append(result.get(property))
			.append("'").toString();
		} else {
			message = buffer.append("- stored item grade as '")
			.append(result.get(property))
			.append("' and recalculated course grade to '").append(result.get(StudentModel.Key.COURSE_GRADE.name()))
			.append("'").toString();
		}

		Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent("Success", message));
	}

	private void onUpdateGradeRecord(final GradeRecordUpdate event) {

		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
		ClassType classType = StudentModel.lookupClassType(event.property, selectedGradebook.getGradebookItemModel().getGradeType());

		final Record record = event.record;
		final UserEntityUpdateAction<StudentModel> action = new UserEntityUpdateAction<StudentModel>(selectedGradebook, (StudentModel)record.getModel(), event.property, classType, event.value, event.oldValue);		

		AsyncCallback<StudentModel> callback = new AsyncCallback<StudentModel>() {

			public void onFailure(Throwable caught) {

				record.beginEdit();

				String property = event.property;

				// Save the exception message on the record
				String failedProperty = property + FAILED_FLAG;
				record.set(failedProperty, caught.getMessage());

				// We have to fool the system into thinking that the value has changed, since
				// we snuck in that "Saving grade..." under the radar.
				if (event.oldValue == null && event.value != null)
					record.set(property, event.value);
				else 
					record.set(property, null);
				record.set(property, event.oldValue);

				record.setValid(property, false);

				record.endEdit();

				Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught, "Failed to update grade: "));			
			}

			public void onSuccess(StudentModel result) {
				record.beginEdit();
				onUpdateGradeRecordSuccess(event, result);
				record.endEdit();
				Dispatcher.forwardEvent(GradebookEvents.LearnerGradeRecordUpdated.getEventType(), action);
			}		

		};

		Gradebook2RPCServiceAsync service = Registry.get("service");
		service.update((StudentModel)record.getModel(), EntityType.LEARNER, action, SecureToken.get(), callback);
	}

	private void onRevertItem(final ItemUpdate event) {
		String property = event.property;
		Record record = event.record;

		record.set(property, null);
		record.set(property, event.oldValue);

		record.setValid(property, false);
	}

	private void onShowColumns(ShowColumnsEvent event) {
		if (event.isSingle) {
			String columnId = event.itemModelId;
			boolean hidden = event.isHidden;

			GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);

			ConfigurationModel configModel = selectedGradebook.getConfigurationModel();

			if (configModel.isColumnHidden(AppConstants.ITEMTREE, columnId, !hidden) != hidden) {
				ConfigurationModel model = new ConfigurationModel(selectedGradebook.getGradebookId());
				model.setColumnHidden(AppConstants.ITEMTREE, columnId, Boolean.valueOf(hidden));

				Gradebook2RPCServiceAsync service = Registry.get(AppConstants.SERVICE);

				AsyncCallback<ConfigurationModel> callback = new AsyncCallback<ConfigurationModel>() {

					public void onFailure(Throwable caught) {
						// FIXME: Should we notify the user when this fails?
					}

					public void onSuccess(ConfigurationModel result) {
						GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
						ConfigurationModel configModel = selectedGradebook.getConfigurationModel();

						Collection<String> propertyNames = result.getPropertyNames();
						if (propertyNames != null) {
							List<String> names = new ArrayList<String>(propertyNames);

							for (int i=0;i<names.size();i++) {
								String name = names.get(i);
								String value = result.get(name);
								configModel.set(name, value);
							}
						}
					}

				};

				service.update(model, EntityType.CONFIGURATION, null, SecureToken.get(), callback);
			}
		}
	}

	private void onUpdateItemFailure(ItemUpdate event, Throwable caught) {

		if (event.record != null)
			event.record.reject(false);
		
		Dispatcher.forwardEvent(GradebookEvents.FailedToUpdateItem.getEventType(), event);
		Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught, "Failed to update item: "));
	}

	private void onUpdateItemSuccess(ItemUpdate event, ItemModel result) {
		if (event.close)
			Dispatcher.forwardEvent(GradebookEvents.HideFormPanel.getEventType(), Boolean.FALSE);

		boolean isCategoryTypeUpdated = false;
		boolean isGradeTypeUpdated = false;
		boolean isGradeScaleUpdated = false;
		boolean isReleaseGradesUpdated = false;
		boolean isReleaseItemsUpdated = false;
		boolean isExtraCreditScaled = false;
		
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);

		if (event.record != null && event.record.isEditing()) {
			Map<String, Object> changes = event.record.getChanges();

			isGradeScaleUpdated = changes != null && changes.get(ItemModel.Key.GRADESCALEID.name()) != null;
			isGradeTypeUpdated = changes != null && changes.get(ItemModel.Key.GRADETYPE.name()) != null;
			isCategoryTypeUpdated = changes != null && changes.get(ItemModel.Key.CATEGORYTYPE.name()) != null;
			isReleaseGradesUpdated = changes != null && changes.get(ItemModel.Key.RELEASEGRADES.name()) != null;
			isReleaseItemsUpdated = changes != null && changes.get(ItemModel.Key.RELEASEITEMS.name()) != null;
			isExtraCreditScaled = changes != null && changes.get(ItemModel.Key.EXTRA_CREDIT_SCALED.name()) != null;
			
			event.record.commit(false);
		}

		switch (result.getItemType()) {
			case GRADEBOOK:

				Dispatcher.forwardEvent(GradebookEvents.ItemUpdated.getEventType(), result);

				selectedGradebook.setGradebookItemModel(result);
				
				if (isCategoryTypeUpdated || isReleaseGradesUpdated || isReleaseItemsUpdated) {
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookSetup.getEventType(),
							selectedGradebook);
				} 

				if (isGradeScaleUpdated) {
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradeScale.getEventType(),
							selectedGradebook);
				}
				
				if (isGradeTypeUpdated) {
					Dispatcher.forwardEvent(GradebookEvents.GradeTypeUpdated.getEventType(), selectedGradebook);
				}

				if (event.item.getItemType() != Type.GRADEBOOK || isGradeTypeUpdated || isCategoryTypeUpdated ||
						isExtraCreditScaled) {
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(),
							selectedGradebook);

					Dispatcher.forwardEvent(GradebookEvents.RefreshCourseGrades.getEventType(),
							selectedGradebook);
				}

				break;
			case CATEGORY:

				doUpdateItem(event, result);

				for (ItemModel item : result.getChildren()) {

					doUpdateItem(event, item);
				}

				if (event.getModifiedItem() != null && event.getModifiedItem().getItemType() != Type.CATEGORY)
					return;

				break;
			case ITEM:
				doUpdateItem(event, result);
				break;
		}

	}

	private void onUpdateItem(final ItemUpdate event) {
		Dispatcher.forwardEvent(GradebookEvents.MaskItemTree.getEventType());

		Gradebook2RPCServiceAsync service = Registry.get("service");
		AsyncCallback<ItemModel> callback = new AsyncCallback<ItemModel>() {

			public void onFailure(Throwable caught) {
				onUpdateItemFailure(event, caught);
				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}

			public void onSuccess(ItemModel result) {
				Dispatcher.forwardEvent(GradebookEvents.BeginItemUpdates.getEventType());
				onUpdateItemSuccess(event, result);
				Dispatcher.forwardEvent(GradebookEvents.EndItemUpdates.getEventType());
				Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
			}
		};

		service.update((ItemModel)event.getModifiedItem(), EntityType.ITEM, null, SecureToken.get(), callback);
	}

	private void doCreateItem(ItemCreate itemCreate, ItemModel createdItem) {
		TreeStore<ItemModel> treeStore = (TreeStore<ItemModel>)itemCreate.store;
		treeStore.add(createdItem.getParent(), createdItem, true);
		Dispatcher.forwardEvent(GradebookEvents.ItemCreated.getEventType(), createdItem);
		doUpdatePercentCourseGradeTotal(itemCreate.store, itemCreate.item, createdItem);
	}

	private void doUpdatePercentCourseGradeTotal(Store store, ItemModel oldItem, ItemModel updatedItem) {
		switch (updatedItem.getItemType()) {
			case CATEGORY:
				ItemModel gradebookItemModel = updatedItem.getParent();
				if (gradebookItemModel != null && gradebookItemModel.getItemType() == Type.GRADEBOOK)
					doUpdateItem(store, null, null, gradebookItemModel);
				break;
		}
	}

	private void doUpdateItem(ItemUpdate itemUpdate, ItemModel updatedItem) {
		doUpdatePercentCourseGradeTotal(itemUpdate.store, itemUpdate.item, updatedItem);
		doUpdateItem(itemUpdate.store, itemUpdate.property, itemUpdate.record, updatedItem);
	}

	private void doUpdateItem(Store store, String property, Record record, ItemModel updatedItem) {
		TreeStore<ItemModel> treeStore = (TreeStore<ItemModel>)store;

		if (updatedItem.isActive() && record != null) {
			record.beginEdit();
			for (String p : updatedItem.getPropertyNames()) {
				replaceProperty(p, record, updatedItem);
			}
			record.commit(false);
			Dispatcher.forwardEvent(GradebookEvents.ItemUpdated.getEventType(), updatedItem);
		} else {
			treeStore.update(updatedItem);
		}
	}

	private boolean doUpdateViaRecord(Record record, ItemModel item) {
		// Don't modify the record unless the record's item model has been passed in
		if (!record.getModel().equals(item)) 
			return false;

		record.beginEdit();

		for (String property : item.getPropertyNames()) {
			// Do it for the property being explicitly changed
			replaceProperty(property, record, item);
		}

		record.endEdit();

		return true;
	}

	private void replaceProperty(String property, Record record, ItemModel item) {
		Object value = item.get(property);

		record.set(property, null);

		if (value != null)
			record.set(property, value);
	}

	private ItemModel getActiveItem(ItemModel parent) {
		if (parent.isActive())
			return parent;

		for (ItemModel c : parent.getChildren()) {
			if (c.isActive()) {
				return c;
			}

			if (c.getChildCount() > 0) {
				ItemModel activeItem = getActiveItem(c);

				if (activeItem != null)
					return activeItem;
			}
		}

		return null;
	}
}
