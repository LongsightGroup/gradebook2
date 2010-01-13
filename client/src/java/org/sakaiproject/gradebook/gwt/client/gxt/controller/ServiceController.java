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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.Gradebook2RPCServiceAsync;
import org.sakaiproject.gradebook.gwt.client.RestBuilder;
import org.sakaiproject.gradebook.gwt.client.SecureToken;
import org.sakaiproject.gradebook.gwt.client.RestBuilder.Method;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction.ClassType;
import org.sakaiproject.gradebook.gwt.client.gxt.ItemModelProcessor;
import org.sakaiproject.gradebook.gwt.client.gxt.JsonTranslater;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradeRecordUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemCreate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ShowColumnsEvent;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationKey;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.ConfigurationModel;
import org.sakaiproject.gradebook.gwt.client.model.FixedColumnModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemKey;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.LearnerKey;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServiceController extends Controller {

	public static final String FAILED_FLAG = ":F";

	private DelayedTask showColumnsTask;
	
	public ServiceController() {
		registerEventTypes(GradebookEvents.Configuration.getEventType());
		registerEventTypes(GradebookEvents.CreateItem.getEventType());
		registerEventTypes(GradebookEvents.DeleteItem.getEventType());
		registerEventTypes(GradebookEvents.RevertItem.getEventType());
		registerEventTypes(GradebookEvents.ShowColumns.getEventType());
		registerEventTypes(GradebookEvents.UpdateLearnerGradeRecord.getEventType());
		registerEventTypes(GradebookEvents.UpdateItem.getEventType());
	}

	@Override
	public void handleEvent(AppEvent event) {
		switch (GradebookEvents.getEvent(event.getType()).getEventKey()) {
			case CONFIGURATION:
				onConfigure((ConfigurationModel)event.getData());
				break;
			case CREATE_ITEM:
				onCreateItem((ItemCreate)event.getData());
				break;
			case DELETE_ITEM:
				onDeleteItem((ItemUpdate)event.getData());
				break;
			case REVERT_ITEM:
				onRevertItem((ItemUpdate)event.getData());
				break;
			case SHOW_COLUMNS:
				onShowColumns((ShowColumnsEvent)event.getData());
				break;
			case UPDATE_LEARNER_GRADE_RECORD:
				onUpdateGradeRecord((GradeRecordUpdate)event.getData());
				break;
			case UPDATE_ITEM:
				onUpdateItem((ItemUpdate)event.getData());
				break;
		}
	}

	private void onConfigure(final ConfigurationModel event) {
		doConfigure(event);
	}
	
	private void doConfigure(final ConfigurationModel model) {

		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);	
		
		Long gradebookId = selectedGradebook.getGradebookId();

		JSONObject json = RestBuilder.convertModel(model);

		RestBuilder builder = RestBuilder.getInstance(Method.PUT, 
				GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.CONFIG_FRAGMENT, String.valueOf(gradebookId));
		
		try {
			builder.sendRequest(json.toString(), new RequestCallback() {

				public void onError(Request request, Throwable caught) {
					
				}

				public void onResponseReceived(Request request, Response response) {
					
					if (response.getStatusCode() != 204) {
						Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent("Status", "Code: " + response.getStatusCode(), true));
						return;
					}

					GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
					ConfigurationModel configModel = selectedGradebook.getConfigurationModel();

					Collection<String> propertyNames = model.getPropertyNames();
					if (propertyNames != null) {
						List<String> names = new ArrayList<String>(propertyNames);

						for (int i=0;i<names.size();i++) {
							String name = names.get(i);
							String value = model.get(name);
							configModel.set(name, value);
						}
					}
					
				}
				
			});
		} catch (RequestException e) {
			Dispatcher.forwardEvent(GradebookEvents.Exception.getEventType(), new NotificationEvent(e));
		}
		
		
		/*Gradebook2RPCServiceAsync service = Registry.get(AppConstants.SERVICE);

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

		service.update(model, EntityType.CONFIGURATION, null, SecureToken.get(), callback);*/
	}
	
	private void onCreateItem(final ItemCreate event) {
		Dispatcher.forwardEvent(GradebookEvents.MaskItemTree.getEventType());
		
		GradebookModel gbModel = Registry.get(AppConstants.CURRENT);
		
		RestBuilder builder = RestBuilder.getInstance(Method.POST, 
				GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.ITEM_FRAGMENT, gbModel.getGradebookUid(),
				String.valueOf(gbModel.getGradebookId()));
		
		try {
			JSONObject jsonObject = RestBuilder.convertModel(event.item);
			
			builder.sendRequest(jsonObject.toString(), new RequestCallback() {

				public void onError(Request request, Throwable caught) {
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught, "Failed to create item: "));
					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
				}

				public void onResponseReceived(Request request, Response response) {
					
					if (response.getStatusCode() != 200) {
						Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent("Failed to create item: ", response.getText(), true));
						Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
						return;
					}
					
					String result = response.getText();

					JsonTranslater translater = new JsonTranslater(EnumSet.allOf(ItemKey.class)) {
						protected ModelData newModelInstance() {
							return new ItemModel();
						}
					};
					ItemModel itemModel = (ItemModel)translater.translate(result);
					
					if (event.close)
						Dispatcher.forwardEvent(GradebookEvents.HideFormPanel.getEventType(), Boolean.FALSE);

					switch (itemModel.getItemType()) {
						case GRADEBOOK:
							GradebookModel selectedGradebook = Registry
							.get(AppConstants.CURRENT);
							selectedGradebook.setGradebookItemModel(itemModel);
							Dispatcher
							.forwardEvent(GradebookEvents.ItemUpdated.getEventType(), itemModel);
							Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(),
									selectedGradebook);
							break;
						case CATEGORY:
							if (itemModel.isActive())
								doCreateItem(event, itemModel);
							else
								doUpdateItem(event.store, null, null, itemModel);

							for (ModelData m : itemModel.getChildren()) {
								ItemModel item = (ItemModel)m;
								if (item.isActive())
									doCreateItem(event, item);
								else
									doUpdateItem(event.store, null, null, item);
							}
							break;
						case ITEM:
							if (itemModel.isActive())
								doCreateItem(event, itemModel);
							else
								doUpdateItem(event.store, null, null, itemModel);
							break;
					}

					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
				}
				
			});
		} catch (RequestException e) {
			Dispatcher.forwardEvent(GradebookEvents.Exception.getEventType(), new NotificationEvent(e));
		}
		
		/*
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

						for (ModelData m : result.getChildren()) {
							ItemModel item = (ItemModel)m;
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
		*/
	}

	private void onDeleteItemSuccess(ItemUpdate event) {
		Dispatcher.forwardEvent(GradebookEvents.ItemDeleted.getEventType(), event.item);
		TreeStore<ItemModel> treeStore = (TreeStore<ItemModel>)event.store;
		treeStore.remove((ItemModel) event.item.getParent(), event.item);
	}

	private void onDeleteItem(final ItemUpdate event) {
		Dispatcher.forwardEvent(GradebookEvents.MaskItemTree.getEventType());
		event.item.setRemoved(Boolean.TRUE);

		RestBuilder builder = RestBuilder.getInstance(Method.DELETE, 
				GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.ITEM_FRAGMENT);
		
		try {
			JSONObject jsonObject = RestBuilder.convertModel(event.item);
			
			builder.sendRequest(jsonObject.toString(), new RequestCallback() {

				public void onError(Request request, Throwable caught) {
					onUpdateItemFailure(event, caught);
					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
				}

				public void onResponseReceived(Request request, Response response) {
					
					if (response.getStatusCode() != 200) {
						onUpdateItemFailure(event, null);
						Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
						return;
					}
					
					String result = response.getText();

					JsonTranslater translater = new JsonTranslater(EnumSet.allOf(ItemKey.class)) {
						protected ModelData newModelInstance() {
							return new ItemModel();
						}
					};
					ItemModel itemModel = (ItemModel)translater.translate(result);
					
					Dispatcher.forwardEvent(GradebookEvents.BeginItemUpdates.getEventType());
					onUpdateItemSuccess(event, itemModel);
					onDeleteItemSuccess(event);
					Dispatcher.forwardEvent(GradebookEvents.EndItemUpdates.getEventType());
					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());

				}
				
			});
		} catch (RequestException e) {
			Dispatcher.forwardEvent(GradebookEvents.Exception.getEventType(), new NotificationEvent(e));
		}
		
		/*Gradebook2RPCServiceAsync service = Registry.get("service");
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

		service.update((ItemModel)event.item, EntityType.ITEM, null, SecureToken.get(), callback);*/
	}

	private void onUpdateGradeRecordFailure(GradeRecordUpdate event, Throwable caught, int status) {
		Record record = event.record;
		record.beginEdit();

		String property = event.property;

		// Save the exception message on the record
		String failedProperty = property + FAILED_FLAG;
		if (caught != null) {
			record.set(failedProperty, caught.getMessage());
		} else {
			record.set(failedProperty, "Received status code of " + status);
		}
		
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
	
	private void onUpdateGradeRecordSuccess(GradeRecordUpdate event, ModelData result) {
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

		String courseGrade = result.get(LearnerKey.COURSE_GRADE.name());

		record.set(LearnerKey.COURSE_GRADE.name(), null);
		if (courseGrade != null) 
			record.set(LearnerKey.COURSE_GRADE.name(), courseGrade);
		
		String calculatedGrade = result.get(LearnerKey.CALCULATED_GRADE.name());
		record.set(LearnerKey.CALCULATED_GRADE.name(), null);
		if (calculatedGrade != null)
			record.set(LearnerKey.CALCULATED_GRADE.name(), calculatedGrade);
		
		String letterGrade = result.get(LearnerKey.LETTER_GRADE.name());
		record.set(LearnerKey.LETTER_GRADE.name(), null);
		if (letterGrade != null)
			record.set(LearnerKey.LETTER_GRADE.name(), letterGrade);

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
		String displayName = (String)record.get(LearnerKey.DISPLAY_NAME.name());
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
			.append("' and recalculated course grade to '").append(result.get(LearnerKey.COURSE_GRADE.name()))
			.append("'").toString();
		}

		Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent("Success", message));
	}

	private void onUpdateGradeRecord(final GradeRecordUpdate event) {

		final GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);	
		
		ClassType classType = StudentModel.lookupClassType(event.property, selectedGradebook.getGradebookItemModel().getGradeType());

		final Record record = event.record;
		final UserEntityUpdateAction<ModelData> action = new UserEntityUpdateAction<ModelData>(selectedGradebook, record.getModel(), event.property, classType, event.value, event.oldValue);		

		String gradebookUid = selectedGradebook.getGradebookUid();
		String entity = null;
		String studentUid = (String)record.getModel().get(LearnerKey.UID.name());
		String itemId = (String)event.property;
		
		JSONObject json = new JSONObject();

		switch (classType) {
		case STRING:
			if (event.value != null)
				json.put("stringValue", new JSONString((String)event.value));
			if (event.oldValue != null)
				json.put("previousStringValue", new JSONString((String)event.oldValue));
			json.put("numeric", JSONBoolean.getInstance(false));
			entity = "string";
			break;
		case DOUBLE:
			if (event.value != null)
				json.put("value", new JSONNumber((Double)event.value));
			if (event.oldValue != null)
				json.put("previousValue", new JSONNumber((Double)event.oldValue));
			json.put("numeric", JSONBoolean.getInstance(true));
			entity = "numeric";
			break;
		}
		
		if (event.property.endsWith(StudentModel.COMMENT_TEXT_FLAG))
			entity = "comment";

		RestBuilder builder = RestBuilder.getInstance(Method.PUT, 
				GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.LEARNER_FRAGMENT, entity, gradebookUid, itemId, studentUid);
		
		try {
			builder.sendRequest(json.toString(), new RequestCallback() {

				public void onError(Request request, Throwable caught) {
					onUpdateGradeRecordFailure(event, caught, -1);
				}

				public void onResponseReceived(Request request, Response response) {
					
					if (response.getStatusCode() != 200) {
						Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent("Status", "Code: " + response.getStatusCode(), true));
						onUpdateGradeRecordFailure(event, null, response.getStatusCode());
						return;
					}
					
					final ModelType type = new ModelType();  
					for (LearnerKey key : EnumSet.allOf(LearnerKey.class)) {
						type.addField(key.name(), key.name()); 
					}

					ItemModelProcessor processor = new ItemModelProcessor(selectedGradebook.getGradebookItemModel()) {
						public void doItem(ItemModel itemModel) {
							String id = itemModel.getIdentifier();
							type.addField(id, id);
							String droppedKey = DataTypeConversionUtil.buildDroppedKey(id);
							type.addField(droppedKey, droppedKey);
							
							String commentedKey = DataTypeConversionUtil.buildCommentKey(id);
							type.addField(commentedKey, commentedKey);
							
							String commentTextKey = DataTypeConversionUtil.buildCommentTextKey(id);
							type.addField(commentTextKey, commentTextKey);
							
							String excusedKey = DataTypeConversionUtil.buildExcusedKey(id);
							type.addField(excusedKey, excusedKey);
						}
					};
					
					processor.process();
					
					JsonTranslater reader = new JsonTranslater(type);
					ModelData result = reader.translate(response.getText());
					
					record.beginEdit();
					onUpdateGradeRecordSuccess(event, result);
					record.endEdit();
					Dispatcher.forwardEvent(GradebookEvents.LearnerGradeRecordUpdated.getEventType(), action);
				}
				
			});
		} catch (RequestException e) {
			Dispatcher.forwardEvent(GradebookEvents.Exception.getEventType(), new NotificationEvent(e));
		}
		
		/*
		AsyncCallback<ModelData> callback = new AsyncCallback<ModelData>() {

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

			public void onSuccess(ModelData result) {
				record.beginEdit();
				onUpdateGradeRecordSuccess(event, result);
				record.endEdit();
				Dispatcher.forwardEvent(GradebookEvents.LearnerGradeRecordUpdated.getEventType(), action);
			}		

		};

		Gradebook2RPCServiceAsync service = Registry.get("service");
		service.update((ModelData)record.getModel(), EntityType.LEARNER, action, SecureToken.get(), callback);
		*/
	}

	private void onRevertItem(final ItemUpdate event) {
		String property = event.property;
		Record record = event.record;

		record.set(property, null);
		record.set(property, event.oldValue);

		record.setValid(property, false);
	}

	private void onShowColumns(final ShowColumnsEvent event) {
		if (showColumnsTask != null)
			showColumnsTask.cancel();
		
		showColumnsTask = new DelayedTask(new Listener<BaseEvent>() {
		
			public void handleEvent(BaseEvent be) {
				doShowColumns(event);
			}
		});
		
		showColumnsTask.delay(1000);
	}
	
	private void doShowColumns(ShowColumnsEvent event) {
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
		ConfigurationModel model = new ConfigurationModel(selectedGradebook.getGradebookId());
		
		if (event.isSingle) {
			boolean hidden = event.isHidden;

			if (event.isFixed)
				buildColumnConfigModel(model, event.fixedModel, hidden);
			else
				buildColumnConfigModel(model, event.model, hidden);

			
		} else {
			
			for (String id : event.fullStaticIdSet) {
				boolean isHidden = !event.visibleStaticIdSet.contains(id);
				buildColumnConfigModel(model, id, isHidden);
			}

		}
		
		doConfigure(model);
		
		/*
		
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

		service.update(model, EntityType.CONFIGURATION, null, SecureToken.get(), callback);*/
	}
	
	private void buildColumnConfigModel(ConfigurationModel model, String identifier, boolean isHidden) {
		model.setColumnHidden(AppConstants.ITEMTREE, identifier, Boolean.valueOf(isHidden));
	}
	
	private void buildColumnConfigModel(ConfigurationModel model, FixedColumnModel fixedModel, boolean isHidden) {
		model.setColumnHidden(AppConstants.ITEMTREE, fixedModel.getIdentifier(), Boolean.valueOf(isHidden));
	}
	
	private void buildColumnConfigModel(ConfigurationModel model, ItemModel itemModel, boolean isHidden) {
		switch (itemModel.getItemType()) {
		case GRADEBOOK:
		case CATEGORY:
			for (int i=0;i<itemModel.getChildCount();i++) {
				ItemModel child = (ItemModel)itemModel.getChild(i);
				buildColumnConfigModel(model, child, isHidden);
			}
			break;
		case ITEM:
			model.setColumnHidden(AppConstants.ITEMTREE, itemModel.getIdentifier(), Boolean.valueOf(isHidden));
			break;
		}
	}

	private void onUpdateItemFailure(ItemUpdate event, Throwable caught) {

		if (event.record != null) {
			Map<String, Object> changes = event.record.getChanges();
			
			event.record.reject(false);
		}
		
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

			isGradeScaleUpdated = changes != null && changes.get(ItemKey.GRADESCALEID.name()) != null;
			isGradeTypeUpdated = changes != null && changes.get(ItemKey.GRADETYPE.name()) != null;
			isCategoryTypeUpdated = changes != null && changes.get(ItemKey.CATEGORYTYPE.name()) != null;
			isReleaseGradesUpdated = changes != null && changes.get(ItemKey.RELEASEGRADES.name()) != null;
			isReleaseItemsUpdated = changes != null && changes.get(ItemKey.RELEASEITEMS.name()) != null;
			isExtraCreditScaled = changes != null && changes.get(ItemKey.EXTRA_CREDIT_SCALED.name()) != null;
			
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

				for (ModelData item : result.getChildren()) {

					doUpdateItem(event, (ItemModel) item);
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
		
		RestBuilder builder = RestBuilder.getInstance(Method.PUT, 
				GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.ITEM_FRAGMENT);
		
		try {
			JSONObject jsonObject = RestBuilder.convertModel(event.item);
			
			builder.sendRequest(jsonObject.toString(), new RequestCallback() {

				public void onError(Request request, Throwable caught) {
					onUpdateItemFailure(event, caught);
					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());

				}

				public void onResponseReceived(Request request, Response response) {
					
					if (response.getStatusCode() != 200) {
						onUpdateItemFailure(event, null);
						Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());
						return;
					}
					
					String result = response.getText();

					JsonTranslater translater = new JsonTranslater(EnumSet.allOf(ItemKey.class)) {
						protected ModelData newModelInstance() {
							return new ItemModel();
						}
					};
					ItemModel itemModel = (ItemModel)translater.translate(result);
					
					Dispatcher.forwardEvent(GradebookEvents.BeginItemUpdates.getEventType());
					onUpdateItemSuccess(event, itemModel);
					Dispatcher.forwardEvent(GradebookEvents.EndItemUpdates.getEventType());
					Dispatcher.forwardEvent(GradebookEvents.UnmaskItemTree.getEventType());

				}
				
			});
		} catch (RequestException e) {
			Dispatcher.forwardEvent(GradebookEvents.Exception.getEventType(), new NotificationEvent(e));
		}
		
		/*
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
		*/		
	}

	private void doCreateItem(ItemCreate itemCreate, ItemModel createdItem) {
		TreeStore<ItemModel> treeStore = (TreeStore<ItemModel>)itemCreate.store;
		treeStore.add((ItemModel) createdItem.getParent(), createdItem, true);
		Dispatcher.forwardEvent(GradebookEvents.ItemCreated.getEventType(), createdItem);
		doUpdatePercentCourseGradeTotal(itemCreate.store, itemCreate.item, createdItem);
	}

	private void doUpdatePercentCourseGradeTotal(Store store, ItemModel oldItem, ItemModel updatedItem) {
		switch (updatedItem.getItemType()) {
			case CATEGORY:
				ItemModel gradebookItemModel = (ItemModel) updatedItem.getParent();
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

		for (ModelData m : parent.getChildren()) {
			ItemModel c = (ItemModel)m;
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
