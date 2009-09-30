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

package org.sakaiproject.gradebook.gwt.client.gxt.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.Gradebook2RPCServiceAsync;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.SecureToken;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.gxt.NotifyingAsyncCallback;
import org.sakaiproject.gradebook.gwt.client.gxt.event.BrowseLearner;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ShowColumnsEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.multigrade.MultiGradeContentPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.multigrade.MultiGradeLoadConfig;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.ConfigurationModel;
import org.sakaiproject.gradebook.gwt.client.model.EntityModelComparer;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelReader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MultigradeView extends View {

	private MultiGradeContentPanel multigrade;

	private BasePagingLoader<PagingLoadResult<StudentModel>> multigradeLoader;
	private ListStore<StudentModel> multigradeStore;
	private Listener<StoreEvent> storeListener;


	public MultigradeView(Controller controller, I18nConstants i18n) {
		super(controller);
		storeListener = new Listener<StoreEvent>() {

			public void handleEvent(StoreEvent se) {
				String sortField = ((ListStore)se.getStore()).getSortField();
				SortDir sortDir = ((ListStore)se.getStore()).getSortDir();
				boolean isAscending = sortDir == SortDir.ASC;

				GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
				ConfigurationModel configModel = new ConfigurationModel(selectedGradebook.getGradebookId());
				configModel.setSortField(AppConstants.MULTIGRADE, sortField);
				configModel.setSortDirection(AppConstants.MULTIGRADE, Boolean.valueOf(isAscending));

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

				service.update(configModel, EntityType.CONFIGURATION, null, SecureToken.get(), callback);
			}

		};
		this.multigrade = new MultiGradeContentPanel(null, i18n) {

			protected BasePagingLoader<PagingLoadResult<StudentModel>> newLoader() {
				return multigradeLoader;
			}

			protected ListStore<StudentModel> newStore(BasePagingLoader<PagingLoadResult<StudentModel>> loader) {
				return multigradeStore;
			}
		};
	}

	public ListStore<StudentModel> getStore() {
		return multigrade.getStore();
	}

	public void deselectAll() {
		multigrade.deselectAll();
	}

	@Override
	protected void handleEvent(AppEvent event) {
		switch(GradebookEvents.getEvent(event.getType()).getEventKey()) {
			case BEGIN_ITEM_UPDATES:
				onBeginItemUpdates();
				break;
			case BROWSE_LEARNER:
				onBrowseLearner((BrowseLearner)event.getData());
				break;
			case END_ITEM_UPDATES:
				onEndItemUpdates();
				break;
			case LEARNER_GRADE_RECORD_UPDATED:
				onLearnerGradeRecordUpdated((UserEntityAction<?>)event.getData());
				break;
			case ITEM_CREATED:
				onItemCreated((ItemModel)event.getData());
				break;
			case ITEM_DELETED:
				onItemDeleted((ItemModel)event.getData());
				break;
			case ITEM_UPDATED:
				onItemUpdated((ItemModel)event.getData());
				break;
			case REFRESH_COURSE_GRADES:
				onRefreshCourseGrades();
				break;
			case REFRESH_GRADEBOOK_ITEMS:
				onRefreshGradebookItems((GradebookModel)event.getData());
				break;
			case REFRESH_GRADEBOOK_SETUP:
				onRefreshGradebookSetup((GradebookModel)event.getData());
				break;
			case SHOW_COLUMNS:
				onShowColumns((ShowColumnsEvent)event.getData());
				break;
			case STARTUP:
				ApplicationModel applicationModel = (ApplicationModel)event.getData();
				initUI(applicationModel);
				GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
				onSwitchGradebook(selectedGradebook);
				break;
			case SWITCH_GRADEBOOK:
				onSwitchGradebook((GradebookModel)event.getData());
				break;
			case USER_CHANGE:
				onUserChange((UserEntityAction<?>)event.getData());
				break;
		}
	}

	protected void initUI(ApplicationModel model) {

		RpcProxy<PagingLoadResult<StudentModel>> proxy = 
			new RpcProxy<PagingLoadResult<StudentModel>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<StudentModel>> callback) {
				Gradebook2RPCServiceAsync service = Registry.get("service");
				GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
				service.getPage(selectedGradebook.getGradebookUid(), selectedGradebook.getGradebookId(), EntityType.LEARNER, (PagingLoadConfig)loadConfig, SecureToken.get(), callback);
			}

			@Override
			public void load(final DataReader<PagingLoadResult<StudentModel>> reader, 
					final Object loadConfig, final AsyncCallback<PagingLoadResult<StudentModel>> callback) {
				load(loadConfig, new NotifyingAsyncCallback<PagingLoadResult<StudentModel>>() {

					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						callback.onFailure(caught);
					}

					public void onSuccess(PagingLoadResult<StudentModel> result) {
						try {
							PagingLoadResult<StudentModel> data = null;
							if (reader != null) {
								data = reader.read(loadConfig, result);
							} else {
								data = result;
							}
							callback.onSuccess(data);
						} catch (Exception e) {
							callback.onFailure(e);
						}
					}

				});
			}
		};

		multigradeLoader = new BasePagingLoader<PagingLoadResult<StudentModel>>(proxy, new ModelReader()) {
			protected PagingLoadConfig newLoadConfig() {
				PagingLoadConfig config = new MultiGradeLoadConfig();
				return config;
			}
		};
		multigradeLoader.setReuseLoadConfig(false);

		multigradeStore = new ListStore<StudentModel>(multigradeLoader);
		multigradeStore.setModelComparer(new EntityModelComparer<StudentModel>());
		multigradeStore.setMonitorChanges(true);
		multigradeStore.setDefaultSort(StudentModel.Key.LAST_NAME_FIRST.name(), SortDir.ASC);

		multigradeStore.addListener(Store.Sort, storeListener);
	}

	protected void onBeginItemUpdates() {
		multigrade.onBeginItemUpdates();
	}

	protected void onBrowseLearner(BrowseLearner event) {
		multigrade.onBrowseLearner(event);
	}

	protected void onEndItemUpdates() {
		multigrade.onEndItemUpdates();
	}

	protected void onItemCreated(ItemModel itemModel) {
		multigrade.onItemCreated(itemModel);
	}

	protected void onItemDeleted(ItemModel itemModel) {
		multigrade.onItemDeleted(itemModel);
	}

	protected void onItemUpdated(ItemModel itemModel) {	
		multigrade.onItemUpdated(itemModel);
	}

	protected void onLearnerGradeRecordUpdated(UserEntityAction<?> action) {
		multigrade.onLearnerGradeRecordUpdated(action);
	}

	protected void onRefreshCourseGrades() {
		multigrade.onRefreshCourseGrades();
	}

	protected void onRefreshGradebookItems(GradebookModel gradebookModel) {
		multigrade.onRefreshGradebookItems(gradebookModel);
	}

	protected void onRefreshGradebookSetup(GradebookModel gradebookModel) {
		multigrade.onRefreshGradebookSetup(gradebookModel);
	}

	protected void onShowColumns(ShowColumnsEvent event) {
		multigrade.onShowColumns(event);
	}

	protected void onSwitchGradebook(GradebookModel selectedGradebook) {
		multigrade.onSwitchGradebook(selectedGradebook);
	}

	protected void onUserChange(UserEntityAction<?> action) {
		multigrade.onUserChange(action);
	}

	public MultiGradeContentPanel getMultiGradeContentPanel() {
		return multigrade;
	}

}
