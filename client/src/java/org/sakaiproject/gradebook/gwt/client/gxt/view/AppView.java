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

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.gxt.event.FullScreen;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradeRecordUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.view.components.Viewport;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class AppView extends View {

	public enum EastCard { DELETE_CATEGORY, DELETE_ITEM, EDIT_CATEGORY, EDIT_GRADEBOOK, EDIT_ITEM, 
		GRADE_SCALE, HELP, HISTORY, LEARNER_SUMMARY, NEW_CATEGORY, NEW_ITEM,
		STATISTICS };


	protected Viewport realViewport;
	protected CardLayout viewportLayout;
	
	protected LayoutContainer viewport;

	
	public AppView(Controller controller) {
		super(controller);
		this.viewportLayout = new CardLayout();
		this.realViewport = new Viewport() {
			protected void onRender(Element parent, int pos) {
			    super.onRender(parent, pos);
			    Accessibility.setRole(el().dom, "application");
			}
		};
		realViewport.setEnableScroll(false);
		realViewport.setLayout(new FillLayout());
		realViewport.setLoadingPanelId("loading");
		
		viewport = new LayoutContainer();
		realViewport.add(viewport);
		
		viewport.setPosition(0, 0);
		viewport.setLayout(viewportLayout);
		
		RootPanel.get().add(realViewport);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		switch(GradebookEvents.getEvent(event.getType()).getEventKey()) {
		case CONFIRMATION:
		case NOTIFICATION:
			onOpenNotification();
			break;
		case CLOSE_NOTIFICATION:
			onCloseNotification();
			break;
		case FAILED_TO_UPDATE_ITEM:
			onFailedToUpdateItem((ItemUpdate)event.getData());
			break;
		case LOAD_ITEM_TREE_MODEL:
			onLoadItemTreeModel((GradebookModel)event.getData());
			break;
		case LEARNER_GRADE_RECORD_UPDATED:
			onLearnerGradeRecordUpdated((UserEntityUpdateAction)event.getData());
			break;
		case GRADE_TYPE_UPDATED:
			onGradeTypeUpdated((GradebookModel)event.getData());
			break;
		case NEW_CATEGORY:
			onNewCategory((ItemModel)event.getData());
			break;
		case NEW_ITEM:
			onNewItem((ItemModel)event.getData());
			break;
		case REFRESH_GRADEBOOK_ITEMS:
			onRefreshGradebookItems((GradebookModel)event.getData());
			break;
		case REFRESH_GRADEBOOK_SETUP:
			onRefreshGradebookSetup((GradebookModel)event.getData());
			break;
		case REFRESH_GRADE_SCALE:
			onRefreshGradeScale((GradebookModel)event.getData());
			break;
		case SELECT_LEARNER:
			onSelectLearner((ModelData)event.getData());
			break;
		case SHOW_GRADE_SCALE:
			onShowGradeScale((Boolean)event.getData());
			break;
		case SHOW_HISTORY:
			onShowHistory((String)event.getData());
			break;
		case SHOW_STATISTICS:
			onShowStatistics();
			break;
		case STOP_STATISTICS:
			onStopStatistics();
			break;
		case SINGLE_VIEW:
			onSingleView((ModelData)event.getData());
			break;
		case START_IMPORT:
			onStartImport();
			break;
		case START_GRADER_PERMISSION_SETTINGS:
			onStartGraderPermissions();
			break;
		case STOP_GRADER_PERMISSION_SETTINGS:
			onStopGraderPermissions();
			break;
		case START_EDIT_ITEM:
			onStartEditItem((ItemModel)event.getData());
			break;
		case STOP_IMPORT:
			onStopImport();
			break;
		case HIDE_EAST_PANEL:
			onHideEastPanel((Boolean)event.getData());
			break;
		case HIDE_FORM_PANEL:
			onHideFormPanel();
			break;
		case EXPAND_EAST_PANEL:
			onExpandEastPanel((EastCard)event.getData());
			break;
		case ITEM_CREATED:
			onItemCreated((ItemModel)event.getData());
			break;
		case SINGLE_GRADE:
			onSingleGrade((ModelData)event.getData());
			break;
		case STARTUP:
			//RootPanel.get().add(realViewport);
			ApplicationModel applicationModel = (ApplicationModel)event.getData();
			initUI(applicationModel);
			GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
			onSwitchGradebook(selectedGradebook);
			break;
		case SWITCH_GRADEBOOK:
			onSwitchGradebook((GradebookModel)event.getData());
			break;
		case UPDATE_LEARNER_GRADE_RECORD:
			onGradeStudent((GradeRecordUpdate)event.getData());
			break;
		case USER_CHANGE:
			onUserChange((UserEntityAction<?>)event.getData());
			break;
		}
	}
	
	@Override
	protected void initialize() {
		
	}
	
	protected abstract void initUI(ApplicationModel model);

		
	protected void onCloseNotification() {
		
	}
	
	protected void onExpandEastPanel(EastCard activeCard) {
		
	}

	protected void onFailedToUpdateItem(ItemUpdate itemUpdate) {
		
	}
	
	protected void onFullScreen(FullScreen fullscreen) {
		
	}
	
	protected void onGradeStudent(GradeRecordUpdate event) {
		
	}
	
	protected void onGradeTypeUpdated(GradebookModel selectedGradebook) {
		
	}
	
	protected void onHideEastPanel(Boolean doCommit) {
		
	}
	
	protected void onHideFormPanel() {
		
	}
	
	protected void onItemCreated(ItemModel itemModel) {
		
	}
	
	protected void onLearnerGradeRecordUpdated(UserEntityUpdateAction action) {
		
	}
	
	protected void onLoadItemTreeModel(GradebookModel selectedGradebook) {
		
	}
		
	protected void onOpenNotification() {
		
	}
	
	protected void onNewCategory(ItemModel itemModel) {
		
	}
	
	protected void onNewItem(ItemModel itemModel) {
		
	}
	
	protected void onRefreshGradebookItems(GradebookModel gradebookModel) {
		
	}
	
	protected void onRefreshGradebookSetup(GradebookModel gradebookModel) {
		
	}
	
	protected void onRefreshGradeScale(GradebookModel gradebookModel) {
		
	}
	
	protected void onSelectLearner(ModelData learner) {
		
	}
	
	protected void onSingleView(ModelData learner) {
		
	}
	
	protected void onShowGradeScale(Boolean show) {
		
	}
	
	protected void onShowHistory(String identifier) {
		
	}
	
	protected void onShowStatistics() {
		
	}
	
	protected void onStopStatistics() {
		
	}
	
	protected void onSingleGrade(ModelData student) {
		
	}
	
	protected void onStartEditItem(ItemModel itemModel) {
		
	}
	
	protected void onStartImport() {
		
	}
	
	protected void onStopImport() {
		
	}
	
	protected void onStartGraderPermissions() {
	
	}
	
	protected void onStopGraderPermissions() {
		
	}
		
	protected void onSwitchGradebook(GradebookModel selectedGradebook) {
		
	}
	
	protected void onUserChange(UserEntityAction<?> action) {
		
	}

	public CardLayout getViewportLayout() {
		return viewportLayout;
	}

	public LayoutContainer getViewport() {
		return viewport;
	}
}
