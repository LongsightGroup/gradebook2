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
package org.sakaiproject.gradebook.gwt.client;

import com.google.gwt.i18n.client.Constants;

public interface I18nConstants extends Constants {

	String allyItemTreeTableSummary();
	
	String categoryName();
	String itemName();
	String scoreName();
	String outOfName();
	String meanName();
	String stdvName();
	String medianName();
	String modeName();
	String rankName();
	String commentName();
	
	String itemNameToolTip();
	String releaseGradesToolTip();
	String releaseItemsToolTip();
	String showMeanToolTip();
	String showMedianToolTip();
	String showModeToolTip();
	String showRankToolTip();
	String showItemStatsToolTip();
	String scaledExtraCreditToolTip();
	String percentCourseGradeToolTip();
	String percentCategoryToolTip();
	String dropLowestToolTip();
	String includedToolTip();
	String extraCreditToolTip();
	String equallyWeightChildrenToolTip();
	String releasedToolTip();
	String nullsAsZerosToolTip();
	String enforcePointWeightingToolTip();
	
	String nameFieldLabel();
	String categoryTypeFieldLabel();
	String gradeTypeFieldLabel();
	String releaseGradesFieldLabel();
	String releaseItemsFieldLabel();
	String showItemStatsFieldLabel();
	String showRankFieldLabel();
	String showModeFieldLabel();
	String showMedianFieldLabel();
	String showMeanFieldLabel();
	String scaledExtraCreditFieldLabel();
	String percentCourseGradeFieldLabel();
	String percentCategoryFieldLabel();
	String pointsFieldLabel();
	String dropLowestFieldLabel();
	String dueDateFieldLabel();
	String sourceFieldLabel();
	String includedFieldLabel();
	String extraCreditFieldLabel();
	String equallyWeightChildrenFieldLabel();
	String releasedFieldLabel();
	String nullsAsZerosFieldLabel();
	String enforcePointWeightingFieldLabel();
	
	String actionDateFieldLabel();
	String actionDescriptionFieldLabel();
	String actionEntityFieldLabel();
	String actionStudentNameFieldLabel();
	String actionDetails();
	String actionActor();
	
	String pointsFieldEmptyText();
	String dueDateEmptyText();
	
	String gradeTypeLetters();
	String gradeTypePoints();
	String gradeTypePercentages();
	
	String orgTypeNoCategories();
	String orgTypeCategories();
	String orgTypeWeightedCategories();

	String navigationPanelHeader();
	String navigationPanelDynamicTabHeader();
	String navigationPanelFixedTabHeader();
	
	String columnTitleDisplayId();
	String columnTitleDisplayName();
	String columnTitleEmail();
	String columnTitleSection();
	
	String displayToStudentsHeading();
	
	String newMenuHeader();
	String prefMenuHeader();
	String prefMenuEnablePopups();
	String prefMenuGradebookName();
	String prefMenuOrgTypeHeader();
	String prefMenuOrgTypeLabel();
	String prefMenuGradeTypeHeader();
	String prefMenuReleaseGradesYes();
	String prefMenuReleaseGradesNo();
	String viewMenuHeader();
	String moreMenuHeader();
	String helpMenuHeader();
	
	String tabGradesHeader();
	String tabGradeScaleHeader();
	String tabSetupHeader();
	String tabHistoryHeader();
	String tabGraderPermissionSettingsHeader();
	String tabStatisticsHeader();
	
	String singleViewHeader();
	String singleGradeHeader();
	
	String nextLearner();
	String prevLearner();
	String viewAsLearner();
	String close();
	
	String resetGradingScale(); 
	
	String addCategoryHeading();
	
	String addItemHeading();
	String addItemDirections();
	String addItemName();
	String addItemPointsEmpty();
	String addItemPoints();
	String addItemWeightEmpty();
	String addItemWeight();
	String addItemDueDateEmpty();
	String addItemDueDate();
	String addItemNoCategoryHeading();
	String addItemNoCategoryMessage();
	
	String deleteCategoryHeading();
	String deleteItemHeading();
	String editCategoryHeading();
	String editGradebookHeading();
	String editItemHeading();
	String gradeScaleHeading();
	String graderPermissionSettingsHeading();
	String helpHeading();
	String historyHeading();
	String learnerSummaryHeading();
	String newCategoryHeading();
	String newItemHeading();
	String statisticsHeading();
	
	String headerAddCategory();
	String headerAddCategoryTitle();
	String headerAddItem();
	String headerAddItemTitle();
	String headerEditCategory();
	String headerEditCategoryTitle();
	String headerEditItem();
	String headerEditItemTitle();
	String headerFinalGrade();
	String headerFinalGradeTitle();
	String headerExport();
	String headerExportTitle();
	String headerExportData();
	String headerExportDataTitle();
	String headerExportStructure();
	String headerExportStructureTitle();
	String headerExportCSV(); 
	String headerExportCSVTitle(); 
	String headerExportXLS();
	String headerExportXLSTitle();
	String headerDeleteCategory();
	String headerDeleteCategoryTitle();
	String headerDeleteItem();
	String headerDeleteItemTitle();
	String headerGradeScale();
	String headerGradeScaleTitle();
	String headerHideItem();
	String headerHideItemTitle();
	String headerHistory();
	String headerHistoryTitle();
	String headerImport();
	String headerImportTitle();
	String headerSortAscending();
	String headerSortDescending();
	String headerSortAscendingTitle();
	String headerSortDescendingTitle();
	String requiredLabel();
	
	String cancelButton();
	String closeButton();
	String createButton();
	String createAndCloseButton();
	String deleteButton();
	String saveButton();
	String saveAndCloseButton();

	String hasChangesTitle();
	String hasChangesMessage();
	
	String itemNameRequiredTitle();
	String itemNameRequiredText();
	
	String directionsConfirmDeleteItem();
	
	String confirmChangingWeightEquallyWeighted();
	String changingPointsRecalculatesGrades();
	String doRecalculatePointsTitle();
	String doRecalculatePointsMessage();
	
	String helpHtml();

	String learnerTabCommentHeader();
	String learnerTabExcuseHeader();
	String learnerTabGradeHeader();
		
	String searchLearnerEmptyText();
	
	String unknownException();
	
	String uploadingLearnerGradesPrefix();
	String uploadingLearnerGradesSuffix();
	String uploadingLearnerGradesStatus();
	String uploadingLearnerGradesTitle();
	
	String finalGradeSubmissionTitle();
	String finalGradeSubmissionConfirmTitle();
	String finalGradeSubmissionConfirmText();
	String finalGradeSubmissionVerificationTitle();
	String finalGradeSubmissionWarningPrefix1(); 
	String finalGradeSubmissionWarningSuffix1(); 
	String finalGradeSubmissionWarningPrefix2(); 
	String finalGradeSubmissionMessageText1a();
	String finalGradeSubmissionMessageText1b();
	String finalGradeSubmissionMessageText1c();
	String finalGradeSubmissionMessageText2a();
	String finalGradeSubmissionMessageText3a();
	String finalGradeSubmissionMessageText4a();
	String finalGradeSubmissionMessageText5a();
	String finalGradeSubmissionMessageText6a();
	String finalGradeSubmissionMessageText7a();
	String finalGradeSubmissionMessageText8a();
	
	String notifyNotDisplayingReleasedItems();
	String notifyNoReleasedItems();
	
	String importSetupRequiredTitle();
	String importDefaultShowPanelMessage();
	String importProgressTitle();
	String importReadingFileMessage();
	String importParsingMessage();
	String importGradesFailedTitle();
	String importGradesFailedMessage();

}
