package org.sakaiproject.gradebook.gwt.client.gxt.controller;

import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.view.AppView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.FinalGradeSubmissionView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.ImportExportView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.InstructorView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.MultigradeView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.NotificationView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.SingleGradeView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.StudentView;
import org.sakaiproject.gradebook.gwt.client.gxt.view.TreeView;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

public class AppController extends Controller {
	
	private AppView appView;
	private SingleGradeView singleGrade;
	private SingleGradeView singleView;
	private TreeView treeView;
	private NotificationView notificationView;
	private ImportExportView importExportView;
	private MultigradeView multigradeView;
	private FinalGradeSubmissionView finalGradeSubmissionView;
	
	public AppController() {
		registerEventTypes(GradebookEvents.BeginItemUpdates.getEventType());
		registerEventTypes(GradebookEvents.BrowseLearner.getEventType());
		registerEventTypes(GradebookEvents.CloseNotification.getEventType());
		registerEventTypes(GradebookEvents.Confirmation.getEventType());
		registerEventTypes(GradebookEvents.ConfirmDeleteItem.getEventType());
		registerEventTypes(GradebookEvents.EndItemUpdates.getEventType());
		registerEventTypes(GradebookEvents.ExpandEastPanel.getEventType());
		registerEventTypes(GradebookEvents.HideColumn.getEventType());
		registerEventTypes(GradebookEvents.HideEastPanel.getEventType());
		registerEventTypes(GradebookEvents.ItemCreated.getEventType());
		registerEventTypes(GradebookEvents.ItemDeleted.getEventType());
		registerEventTypes(GradebookEvents.ItemUpdated.getEventType());
		registerEventTypes(GradebookEvents.LearnerGradeRecordUpdated.getEventType());
		registerEventTypes(GradebookEvents.LoadItemTreeModel.getEventType());
		registerEventTypes(GradebookEvents.MaskItemTree.getEventType());
		registerEventTypes(GradebookEvents.NewCategory.getEventType());
		registerEventTypes(GradebookEvents.NewItem.getEventType());
		registerEventTypes(GradebookEvents.Notification.getEventType());
		registerEventTypes(GradebookEvents.RefreshCourseGrades.getEventType());
		registerEventTypes(GradebookEvents.RevertItem.getEventType());
		registerEventTypes(GradebookEvents.SelectDeleteItem.getEventType());
		registerEventTypes(GradebookEvents.SelectLearner.getEventType());
		registerEventTypes(GradebookEvents.SelectItem.getEventType());
		registerEventTypes(GradebookEvents.ShowColumns.getEventType());
		registerEventTypes(GradebookEvents.ShowGradeScale.getEventType());
		registerEventTypes(GradebookEvents.ShowHistory.getEventType());
		registerEventTypes(GradebookEvents.SingleGrade.getEventType());
		registerEventTypes(GradebookEvents.SingleView.getEventType());
		registerEventTypes(GradebookEvents.StartEditItem.getEventType());
		registerEventTypes(GradebookEvents.StartExport.getEventType());
		registerEventTypes(GradebookEvents.StartImport.getEventType());
		registerEventTypes(GradebookEvents.Startup.getEventType());
		registerEventTypes(GradebookEvents.StopImport.getEventType());
		registerEventTypes(GradebookEvents.SwitchEditItem.getEventType());
		registerEventTypes(GradebookEvents.SwitchGradebook.getEventType());
		registerEventTypes(GradebookEvents.UnmaskItemTree.getEventType());
		registerEventTypes(GradebookEvents.UpdateLearnerGradeRecord.getEventType());
		registerEventTypes(GradebookEvents.UserChange.getEventType());
		registerEventTypes(GradebookEvents.StartFinalgrade.getEventType());
	}
	
	@Override
	public void handleEvent(AppEvent<?> event) {
		// Note: the 'missing' break statements in this switch are intentional, they
		// allow certain events to drop through to multiple views
		switch (GradebookEvents.getEvent(event.type).getEventKey()) {
		case BEGIN_ITEM_UPDATES:
		case END_ITEM_UPDATES:
			forwardToView(multigradeView, event);
			break;
		case CONFIRMATION:
		case CLOSE_NOTIFICATION:
		case NOTIFICATION:
			forwardToView(appView, event);
			forwardToView(notificationView, event);
			break;
		case HIDE_COLUMN:
			forwardToView(treeView, event);
			break;
		case BROWSE_LEARNER:
			forwardToView(multigradeView, event);
			break;
		case CONFIRM_DELETE_ITEM:
		case SELECT_DELETE_ITEM:
			forwardToView(treeView, event);
			break;
		case LEARNER_GRADE_RECORD_UPDATED:
			forwardToView(multigradeView, event);
			forwardToView(appView, event);
			if (singleView != null && singleView.isDialogVisible())
				forwardToView(singleView, event);
			break;
		case NEW_CATEGORY:
		case NEW_ITEM:
			forwardToView(appView, event);
			forwardToView(treeView, event);
			break;
		case REFRESH_COURSE_GRADES:
			forwardToView(multigradeView, event);
			break;
		case SELECT_LEARNER:
			forwardToView(appView, event);
			if (singleView != null && singleView.isDialogVisible())
				forwardToView(singleView, event);
			break;
		case START_IMPORT:
		case START_EXPORT:
			if (importExportView == null)
				importExportView = new ImportExportView(this);
			
			forwardToView(importExportView, event);
			forwardToView(appView, event);
			break;
		case STOP_IMPORT:
			forwardToView(appView, event);
			break;
		case STARTUP:
			onStartup(event);
			break;
		case SINGLE_GRADE:
			forwardToView(appView, event);
			
			if (treeView != null)
				forwardToView(treeView, event);
			
			if (singleView != null && singleView.isDialogVisible())
				forwardToView(singleView, event);
			break;
		case SINGLE_VIEW:
			if (singleView == null)
				singleView = new SingleGradeView(this, false);
			
			forwardToView(singleView, event);
			break;
		case SWITCH_GRADEBOOK:
			forwardToView(appView, event);
			forwardToView(multigradeView, event);
			forwardToView(treeView, event);
			break;
		case USER_CHANGE:
			if (singleGrade != null)
				forwardToView(singleGrade, event);
			forwardToView(multigradeView, event);
			break;
		case LOAD_ITEM_TREE_MODEL:
			forwardToView(multigradeView, event);
			forwardToView(treeView, event);
			break;
		case SHOW_COLUMNS:
			forwardToView(multigradeView, event);
			break;
		case SHOW_GRADE_SCALE:
		case SHOW_HISTORY:	
			forwardToView(appView, event);
			break;
		case START_EDIT_ITEM:
		case HIDE_EAST_PANEL:
		case SELECT_ITEM:
		case SWITCH_EDIT_ITEM:
			forwardToView(treeView, event);
			forwardToView(appView, event);
			break;
		case EXPAND_EAST_PANEL:
			forwardToView(appView, event);
			break;
		case ITEM_UPDATED:
			forwardToView(multigradeView, event);
			forwardToView(treeView, event);
			if (singleView != null)
				forwardToView(singleView, event);
			break;
		case ITEM_CREATED:
			forwardToView(appView, event);
			forwardToView(multigradeView, event);
			forwardToView(treeView, event);
			break;
		case ITEM_DELETED:
			forwardToView(multigradeView, event);
			break;
		case MASK_ITEM_TREE:
		case UNMASK_ITEM_TREE:
			forwardToView(treeView, event);
			break;
		case START_FINAL_GRADE:
			finalGradeSubmissionView = new FinalGradeSubmissionView(this);
			forwardToView(finalGradeSubmissionView, event);
			//forwardToView(appView, event);
			break;
		}
	}

	@Override
	public void initialize() {
		
	}
	
	
	private void onStartup(AppEvent<?> event) {
		ApplicationModel model = (ApplicationModel)event.data;
		
		List<GradebookModel> gradebookModels = model.getGradebookModels();
		I18nConstants i18n = Registry.get(AppConstants.I18N);
		
		// FIXME: Currently we only evaluate the first gradebook model to determine if we have
		// FIXME: an instructor or a student. This needs to be refined.
		for (GradebookModel gbModel : gradebookModels) {
			Registry.register(gbModel.getGradebookUid(), gbModel);
			Registry.register(AppConstants.CURRENT, gbModel);
			boolean isUserAbleToGrade = gbModel.isUserAbleToGrade() == null ? false : gbModel.isUserAbleToGrade().booleanValue();
			boolean isUserAbleToViewOwnGrades = gbModel.isUserAbleToViewOwnGrades() == null ? false : gbModel.isUserAbleToViewOwnGrades().booleanValue();
			boolean isUserAbleToEditItems = DataTypeConversionUtil.checkBoolean(gbModel.isUserAbleToEditAssessments());
			
			this.notificationView = new NotificationView(this);
			if (isUserAbleToGrade) {
				this.treeView = new TreeView(this, i18n, isUserAbleToEditItems);
				this.multigradeView = new MultigradeView(this, i18n);
				this.importExportView = new ImportExportView(this);
				this.appView = new InstructorView(this, treeView, multigradeView, notificationView, importExportView, isUserAbleToEditItems);
				forwardToView(treeView, event);
				forwardToView(multigradeView, event);
			} else if (isUserAbleToViewOwnGrades) {
				this.appView = new StudentView(this, notificationView);
			}
			
			forwardToView(appView, event);

			return;
		}
	}
	
}
