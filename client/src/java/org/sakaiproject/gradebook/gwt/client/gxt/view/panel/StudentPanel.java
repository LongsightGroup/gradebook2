/**********************************************************************************
 * Copyright (c) 2008, 2009, 2010, 2011 The Regents of the University of California
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
package org.sakaiproject.gradebook.gwt.client.gxt.view.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.RestBuilder;
import org.sakaiproject.gradebook.gwt.client.RestCallback;
import org.sakaiproject.gradebook.gwt.client.RestBuilder.Method;
import org.sakaiproject.gradebook.gwt.client.UrlArgsCallback;
import org.sakaiproject.gradebook.gwt.client.gxt.NewModelCallback;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.model.EntityOverlay;
import org.sakaiproject.gradebook.gwt.client.gxt.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.gxt.model.StatisticsComparator;
import org.sakaiproject.gradebook.gwt.client.gxt.model.StatisticsModel;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;
import org.sakaiproject.gradebook.gwt.client.model.Item;
import org.sakaiproject.gradebook.gwt.client.model.Statistics;
import org.sakaiproject.gradebook.gwt.client.model.key.LearnerKey;
import org.sakaiproject.gradebook.gwt.client.model.key.StatisticsKey;
import org.sakaiproject.gradebook.gwt.client.model.type.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.type.GradeType;
import org.sakaiproject.gradebook.gwt.client.util.Base64;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;

public class StudentPanel extends GradebookPanel {

	private enum Key { 
		S_CTGRY_NM, S_ITM_ID, S_ITM_NM, S_ITM_WGHT, 
		S_GRD, S_MEAN, S_STDV, S_MEDI, S_MODE, S_RANK, 
		S_COMMENT, S_ORDER, S_ID, S_OUTOF, T_DATEDUE, B_DROPPED
	};

	private static final String COURSE_GRADE_ID = "-1";
	
	private static final int PI_ROW_NAME = 1; 
	private static final int PI_ROW_EMAIL = 2; 
	private static final int PI_ROW_ID = 3; 
	private static final int PI_ROW_SECTION = 4;  
	private static final int PI_ROW_COURSE_GRADE = 5; 
	private static final int PI_ROW_CALCULATED_GRADE = 6;
	private static final int PI_ROW_RANK = 7; 
	
	private static final int PI_COL_HEADING = 0; 
	private static final int PI_COL_VALUE = 1; 

	private static final String FIRST_COLUMN_WIDTH = "170px";
	
	private TextField<String> defaultTextField= new TextField<String>();
	private TextArea defaultTextArea = new TextArea();
//	private NumberFormat defaultNumberFormat = NumberFormat.getFormat("#.###");
//	private NumberField defaultNumberField = new NumberField();
	private FlexTable studentInformation;
	private ContentPanel studentInformationPanel, gradeInformationPanel, textPanel, topPanel;
	private Html textNotification;
	private LayoutContainer cardLayoutContainer;
	private CardLayout cardLayout;
	private FormPanel commentsPanel;
	private Grid<BaseModel> grid;
	private GroupingStore<BaseModel> store;
	private ListLoader<ListLoadResult<ModelData>> loader;
	private ColumnModel cm;
	private FormBinding formBinding;
	private GridSelectionModel<BaseModel> selectionModel;
	private TextArea commentArea;
	private ModelData learnerGradeRecordCollection;
	private ColumnConfig categoryColumn, weightColumn, outOfColumn, dateDueColumn, meanColumn, medianColumn, modeColumn, stdvColumn;

	private boolean isStudentView;
	private boolean displayRank; 
	private boolean isAnyCommentPopulated;

	private Gradebook selectedGradebook;

	private boolean isPossibleStatsChanged = true;
	
	private StatisticsComparator statisticsComparator = new StatisticsComparator();

	private List<Statistics> statsList;
	
	// Statistics Charts
	private StatisticsChartPanel statisticsChartPanel;
	private DataTable dataTable;
	private Map<String, DataTable> dataTableCache = new HashMap<String, DataTable>();
	private String selectedAssignmentId;

	public StudentPanel(I18nConstants i18n, boolean isStudentView, boolean displayRank) {
		super();
		this.isStudentView = isStudentView;
//		this.defaultNumberField.setFormat(defaultNumberFormat);
//		this.defaultNumberField.setSelectOnFocus(true);
//		this.defaultNumberField.addInputStyleName(resources.css().gbNumericFieldInput());
		this.defaultTextArea.addInputStyleName(resources.css().gbTextAreaInput());
		this.defaultTextField.addInputStyleName(resources.css().gbTextFieldInput());
		this.displayRank = displayRank;
		
		setFrame(true);
		setHeaderVisible(false);
		setLayout(new FlowLayout());
		setScrollMode(Scroll.AUTO);

		studentInformation = new FlexTable(); 
		studentInformation.setStyleName(resources.css().gbStudentInformation());
		studentInformationPanel = new ContentPanel();
		studentInformationPanel.setBorders(true);
		studentInformationPanel.setFrame(true);
		studentInformationPanel.setHeaderVisible(false);
		// Make it the same height as the chart
		studentInformationPanel.setHeight(AppConstants.CHART_HEIGHT);
		studentInformationPanel.setLayout(new FitLayout());
		studentInformationPanel.setStyleAttribute("padding", "5px");
		studentInformationPanel.add(studentInformation);
		
		statisticsChartPanel = new StatisticsChartPanel();
		statisticsChartPanel.setLegendPosition(LegendPosition.TOP);
		statisticsChartPanel.setSize(AppConstants.CHART_WIDTH, AppConstants.CHART_HEIGHT);
		statisticsChartPanel.setChartHeight(AppConstants.CHART_HEIGHT - 50);
		statisticsChartPanel.setStyleAttribute("padding", "5px");
		statisticsChartPanel.mask();
		
		/*
		 *  TODO: May have to override onResize() similar to what we do in gradeInformationPanel
		 */
		topPanel = new ContentPanel();
		topPanel.setLayout(new ColumnLayout());
		
		topPanel.add(studentInformationPanel, new ColumnData(500));
		topPanel.add(statisticsChartPanel, new ColumnData(610));
		
		add(topPanel); 

		store = new GroupingStore<BaseModel>();
		store.setGroupOnSort(false);
		store.setSortField(Key.S_ORDER.name());
		store.setSortDir(Style.SortDir.ASC);
		store.setModelComparer(new ModelComparer<BaseModel>() {

			public boolean equals(BaseModel m1, BaseModel m2) {
				if (m1 == null || m2 == null)
					return false;

				String id1 = m1.get(Key.S_ID.name());
				String id2 = m2.get(Key.S_ID.name());

				if (id1 != null && id2 != null) {
					return id1.equals(id2);
				}

				return false;
			}

		});
		store.setStoreSorter(new StoreSorter<BaseModel>() {

			public int compare(Store<BaseModel> store, BaseModel m1, BaseModel m2, String property) {
				if (property != null) {

					// We do not want the sort by category to take
					if (property.equals(Key.S_CTGRY_NM.name()))
						return 0;

					if (property.equals(Key.S_ITM_NM.name()))
						property = Key.S_ORDER.name();

					Object v1 = m1.get(property);
					Object v2 = m2.get(property);
					return comparator.compare(v1, v2);
				}
				return comparator.compare(m1, m2);
			}

		});

		loader = RestBuilder.getDelayLoader(AppConstants.LIST_ROOT, 
				EnumSet.allOf(StatisticsKey.class), Method.GET, new UrlArgsCallback() {

			public String getUrlArg() {
				String uid = learnerGradeRecordCollection.get(LearnerKey.S_UID.name());
				return Base64.encode(uid);
			}

		},	
		new NewModelCallback() {

			public ModelData newModelInstance(EntityOverlay overlay) {
				return new StatisticsModel(overlay);
			}

		},
		GWT.getModuleBaseURL(), AppConstants.REST_FRAGMENT, AppConstants.STATISTICS_FRAGMENT);

		loader.addLoadListener(new LoadListener() {

			public void loaderLoad(LoadEvent event) {
				BaseListLoadResult<Statistics> result = event.getData();
				statsList = result.getData();
				refreshGradeData(learnerGradeRecordCollection, statsList);
				isPossibleStatsChanged = false;
			}

		});


		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();

		categoryColumn = new ColumnConfig(Key.S_CTGRY_NM.name(), i18n.categoryName(), 250);
		categoryColumn.setGroupable(true);
		categoryColumn.setHidden(true);
		categoryColumn.setMenuDisabled(true);
		columns.add(categoryColumn);

		ColumnConfig column = new ColumnConfig(Key.S_ITM_NM.name(), i18n.itemName(), 160);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		columns.add(column);

		weightColumn = new ColumnConfig(Key.S_ITM_WGHT.name(), i18n.weightName(), 90);
		weightColumn.setGroupable(false);
		weightColumn.setMenuDisabled(true);
		columns.add(weightColumn);

		column = new ColumnConfig(Key.S_GRD.name(), i18n.scoreName(), 60);
		column.setGroupable(false);
		//column.setAlignment(Style.HorizontalAlignment.RIGHT);
		column.setMenuDisabled(true);
		column.setRenderer(new GridCellRenderer<ModelData>() {

			public Object render(ModelData model, String property,
					com.extjs.gxt.ui.client.widget.grid.ColumnData config,
					int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {

				if (DataTypeConversionUtil.checkBoolean((Boolean)model.get(Key.B_DROPPED.name()))) {
					return new StringBuilder().append("<span class=\"").append(resources.css().gbCellDropped()).append("\">")
					.append(model.get(property)).append("</span>");
				}

				return model.get(property);
			}

		});
		columns.add(column);

		outOfColumn = new ColumnConfig(Key.S_OUTOF.name(), i18n.outOfName(), 60);
		outOfColumn.setGroupable(false);
		//outOfColumn.setAlignment(Style.HorizontalAlignment.RIGHT);
		outOfColumn.setMenuDisabled(true);
		columns.add(outOfColumn);

		dateDueColumn = new ColumnConfig(Key.T_DATEDUE.name(), i18n.dateDueName(), 90);
		dateDueColumn.setGroupable(false);
		dateDueColumn.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
		dateDueColumn.setMenuDisabled(true);
		columns.add(dateDueColumn);

		meanColumn = new ColumnConfig(Key.S_MEAN.name(), i18n.meanName(), 60);
		meanColumn.setGroupable(false);
		meanColumn.setMenuDisabled(true);
		meanColumn.setHidden(true);
		columns.add(meanColumn);

		stdvColumn = new ColumnConfig(Key.S_STDV.name(), i18n.stdvName(), 60);
		stdvColumn.setGroupable(false);
		stdvColumn.setMenuDisabled(true);
		stdvColumn.setHidden(true);
		columns.add(stdvColumn);

		medianColumn = new ColumnConfig(Key.S_MEDI.name(), i18n.medianName(), 60);
		medianColumn.setGroupable(false);
		medianColumn.setMenuDisabled(true);
		medianColumn.setHidden(true);
		columns.add(medianColumn);

		modeColumn = new ColumnConfig(Key.S_MODE.name(), i18n.modeName(), 150);
		modeColumn.setGroupable(false);
		modeColumn.setMenuDisabled(true);
		modeColumn.setHidden(true);
		columns.add(modeColumn);

		cm = new ColumnModel(columns);

		selectionModel = new GridSelectionModel<BaseModel>();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<BaseModel> sce) {
				BaseModel score = sce.getSelectedItem();
				
				if (score != null) {
					
					// Don't show the comments form if the user clicks on the Course Grade item
					if(COURSE_GRADE_ID.equals(score.get(Key.S_ID.name()))) {
						commentsPanel.hide();
					}
					else {
						
						getGradeItemStatisticsChartData((String)score.get(Key.S_ITM_ID.name()));
						formBinding.bind(score);
						commentsPanel.show();
					}
				}
				else {
					commentsPanel.hide();
					formBinding.unbind();
				}
			}
		});

		GroupingView view = new GroupingView();

		grid = new Grid<BaseModel>(store, cm);
		grid.setBorders(true);
		//grid.setAutoHeight(true);
		grid.setSelectionModel(selectionModel);
		grid.setView(view);

		cardLayoutContainer = new LayoutContainer() {
			protected void onResize(final int width, final int height) {
				super.onResize(width, height);

				if (gradeInformationPanel.getWidth() != width ||
						gradeInformationPanel.getHeight() != 340)
					gradeInformationPanel.setSize(width, 340);
			}
		};
		cardLayout = new CardLayout();
		cardLayoutContainer.setLayout(cardLayout);
		cardLayoutContainer.setHeight(365);

		gradeInformationPanel = new ContentPanel() {

			protected void onResize(final int width, final int height) {
				super.onResize(width, height);

				grid.setHeight(height - 42);
				grid.setSize(width - 300, height - 42);
				if (grid.isRendered() && grid.getView() != null)
					grid.getView().refresh(true);
				commentArea.setHeight(height - 76);
			}

		};
		gradeInformationPanel.setBorders(true);
		gradeInformationPanel.setFrame(true);
		gradeInformationPanel.setHeading("Individual Scores (click on a row to see comments and statistics char)");
		gradeInformationPanel.setLayout(new ColumnLayout());
		gradeInformationPanel.add(grid, new ColumnData(795));
		FormLayout commentLayout = new FormLayout();
		commentLayout.setLabelAlign(LabelAlign.TOP);
		commentLayout.setDefaultWidth(280);

		commentsPanel = new FormPanel();
		commentsPanel.setHeaderVisible(false);
		commentsPanel.setLayout(commentLayout);
		commentsPanel.setVisible(false);
		commentsPanel.setWidth(300);

		commentArea = new TextArea();
		commentArea.setName(Key.S_COMMENT.name());
		commentArea.setFieldLabel(i18n.commentName());
		commentArea.setWidth(275);
		commentArea.setHeight(300);
		commentArea.setReadOnly(true);
		commentsPanel.add(commentArea);

		gradeInformationPanel.add(commentsPanel, new ColumnData(305));

		formBinding = new FormBinding(commentsPanel, true);

		textPanel = new ContentPanel();
		textPanel.setBorders(true);
		textPanel.setFrame(true);
		textPanel.setHeaderVisible(false);

		textNotification = textPanel.addText("");

		cardLayoutContainer.add(gradeInformationPanel);
		cardLayout.setActiveItem(gradeInformationPanel);

		cardLayoutContainer.add(textPanel);
		add(cardLayoutContainer);
	}

	public ModelData getStudentRow() {
		return learnerGradeRecordCollection;
	}

	public void onRefreshGradebookSetup(Gradebook selectedGradebook) {
		this.selectedGradebook = selectedGradebook;

		updateCourseGrade((String)learnerGradeRecordCollection.get(LearnerKey.S_LTR_GRD.name()), (String)learnerGradeRecordCollection.get(LearnerKey.S_CALC_GRD.name()));

		isPossibleStatsChanged = true;
	}

	public void onChangeModel(Gradebook selectedGradebook, final ModelData learnerGradeRecordCollection) {
		if (learnerGradeRecordCollection != null) {
			this.selectedGradebook = selectedGradebook;
			this.learnerGradeRecordCollection = learnerGradeRecordCollection;

			updateCourseGrade((String)learnerGradeRecordCollection.get(LearnerKey.S_LTR_GRD.name()), (String)learnerGradeRecordCollection.get(LearnerKey.S_CALC_GRD.name()));

			if (isPossibleStatsChanged || statsList == null) {
				loader.load();
			} else {
				refreshGradeData(learnerGradeRecordCollection, statsList);
			}
		}
	}

	public void onItemUpdated(Item itemModel) {

	}

	public void onLearnerGradeRecordUpdated(ModelData learnerGradeRecordModel) {
		this.isPossibleStatsChanged = true;
	}

	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);

	}

	public void onResize(int width, int height) {
		super.onResize(width, height);

		gradeInformationPanel.setHeight(height - 225);
	}

	public void refreshColumns() {

	}

	public void refreshData() {

	}

	public boolean isStudentView() {
		return isStudentView;
	}

	public void setStudentView(boolean isStudentView) {
		this.isStudentView = isStudentView;
	}

	public ModelData getStudentModel() {
		return learnerGradeRecordCollection;
	}

	private void updateCourseGrade(String newGrade, String calcGrade)
	{

		if (!isStudentView || (selectedGradebook != null && selectedGradebook.getGradebookItemModel() != null 
				&& DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseGrades()))) {
			// To force a refresh, let's first hide the owning panel
			studentInformationPanel.hide();
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, newGrade);

			boolean isLetterGrading = selectedGradebook.getGradebookItemModel().getGradeType() == GradeType.LETTERS;

			if (!isLetterGrading)
				studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, calcGrade);

			studentInformationPanel.show();
		} else {
			studentInformationPanel.hide();
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_HEADING, "");
			studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, "");
			studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, "");
			studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, "");
			studentInformationPanel.show();
		}
	}

	private void refreshGradeData(ModelData learnerGradeRecordCollection, List<Statistics> statsList) {
		
		// GRBK-777 : We are sorting the statsList because the following calls to getStatsModelForItem(...) 
		// perform a binary search. We only want to sort the list once for performance reasons. 
		Collections.sort(statsList, statisticsComparator);
		
		// Get the course level stats
		Statistics m = getStatsModelForItem(String.valueOf(Long.valueOf(-1)), statsList);
		setStudentInfoTable(m);
		
		// Populate the assignment's table
		setGradeInfoTable(selectedGradebook, learnerGradeRecordCollection, statsList);
	}

	private void setStudentInfoTable(Statistics courseGradeStats) {		
		// To force a refresh, let's first hide the owning panel
		studentInformationPanel.hide();

		// Now, let's update the student information table
		FlexCellFormatter formatter = studentInformation.getFlexCellFormatter();

		studentInformation.setText(PI_ROW_NAME, PI_COL_HEADING, "Name");
		formatter.setWidth(PI_ROW_NAME, PI_COL_HEADING, FIRST_COLUMN_WIDTH);
		formatter.setStyleName(PI_ROW_NAME, PI_COL_HEADING, resources.css().gbImpact());
		formatter.setWordWrap(PI_ROW_NAME, PI_COL_HEADING, false);
		studentInformation.setText(PI_ROW_NAME, PI_COL_VALUE, (String)learnerGradeRecordCollection.get(LearnerKey.S_DSPLY_NM.name()));

		studentInformation.setText(PI_ROW_EMAIL, PI_COL_HEADING, "Email");
		formatter.setWidth(PI_ROW_EMAIL, PI_COL_HEADING, FIRST_COLUMN_WIDTH);
		formatter.setStyleName(PI_ROW_EMAIL, PI_COL_HEADING, resources.css().gbImpact());
		formatter.setWordWrap(PI_ROW_EMAIL, PI_COL_HEADING, false);
		studentInformation.setText(PI_ROW_EMAIL, PI_COL_VALUE, (String)learnerGradeRecordCollection.get(LearnerKey.S_EMAIL.name()));

		studentInformation.setText(PI_ROW_ID, PI_COL_HEADING, "Id");
		formatter.setWidth(PI_ROW_ID, PI_COL_HEADING, FIRST_COLUMN_WIDTH);
		formatter.setStyleName(PI_ROW_ID, PI_COL_HEADING, resources.css().gbImpact());
		formatter.setWordWrap(PI_ROW_ID, PI_COL_HEADING, false);
		studentInformation.setText(PI_ROW_ID, PI_COL_VALUE, (String)learnerGradeRecordCollection.get(LearnerKey.S_DSPLY_ID.name()));

		studentInformation.setText(PI_ROW_SECTION, PI_COL_HEADING, "Section");
		formatter.setWidth(PI_ROW_SECTION, PI_COL_HEADING, FIRST_COLUMN_WIDTH);
		formatter.setStyleName(PI_ROW_SECTION, PI_COL_HEADING, resources.css().gbImpact());
		formatter.setWordWrap(PI_ROW_SECTION, PI_COL_HEADING, false);
		studentInformation.setText(PI_ROW_SECTION, PI_COL_VALUE, (String)learnerGradeRecordCollection.get(LearnerKey.S_SECT.name()));

		boolean doReleaseGrades = DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseGrades());
		boolean doReleaseItems = DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseItems());

		if (!isStudentView || doReleaseGrades || doReleaseItems) {

			Item gradebookItemModel = selectedGradebook.getGradebookItemModel();
			boolean isShowMean = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMean());
			boolean isShowMedian = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMedian());
			boolean isShowMode = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMode());
			boolean isShowRank = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowRank());
			boolean isShowItemStatistics = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowItemStatistics());


			if (doReleaseGrades) {
				studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_HEADING, "Course Grade");
				formatter.setStyleName(PI_ROW_COURSE_GRADE, PI_COL_HEADING, resources.css().gbImpact());
				studentInformation.setText(PI_ROW_COURSE_GRADE, PI_COL_VALUE, (String)learnerGradeRecordCollection.get(LearnerKey.S_LTR_GRD.name()));

				boolean isLetterGrading = gradebookItemModel.getGradeType() == GradeType.LETTERS;

				if (!isLetterGrading)
				{
					String calculatedGrade = (String)learnerGradeRecordCollection.get(LearnerKey.S_CALC_GRD.name());
					studentInformation.setHTML(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, "Calculated Grade");
					formatter.setStyleName(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, resources.css().gbImpact());
					formatter.setWordWrap(PI_ROW_CALCULATED_GRADE, PI_COL_HEADING, false);
					studentInformation.setText(PI_ROW_CALCULATED_GRADE, PI_COL_VALUE, calculatedGrade);
				}
				
				if (isShowRank)
				{
					studentInformation.setText(PI_ROW_RANK, PI_COL_HEADING, "Rank");
					formatter.setStyleName(PI_ROW_RANK, PI_COL_HEADING, resources.css().gbImpact());
					if (displayRank)
						studentInformation.setText(PI_ROW_RANK, PI_COL_VALUE, courseGradeStats.getRank());
					else
						studentInformation.setText(PI_ROW_RANK, PI_COL_VALUE, "Visible to Student");
				} else {
					studentInformation.setText(PI_ROW_RANK, PI_COL_HEADING, "");
					studentInformation.setText(PI_ROW_RANK, PI_COL_VALUE, "");
				}

			} 

			if (doReleaseItems) {
				
				cm.setHidden(cm.getIndexById(meanColumn.getId()), !isShowItemStatistics || !isShowMean);
				cm.setHidden(cm.getIndexById(stdvColumn.getId()), !isShowItemStatistics || !isShowMean);	
				cm.setHidden(cm.getIndexById(medianColumn.getId()), !isShowItemStatistics || !isShowMedian);	
				cm.setHidden(cm.getIndexById(modeColumn.getId()), !isShowItemStatistics || !isShowMode);
			}
			else
			{
				GWT.log("Course stats is null", null);
			}

		} 
		studentInformationPanel.show();
	}


	private BaseModel populateGradeInfoRow(int row, ItemModel item, ItemModel category, ModelData learner, Statistics stats, CategoryType categoryType, GradeType gradeType) {
		
		String itemId = item.getIdentifier();
		Object value = learner.get(itemId);
		String commentFlag = DataTypeConversionUtil.buildCommentTextKey(String.valueOf(itemId));
		String comment = learner.get(commentFlag);
		String excusedFlag = DataTypeConversionUtil.buildDroppedKey(String.valueOf(itemId));

		String mean = (stats == null ? "" : stats.getMean());
		String stdDev = (stats == null ? "" : stats.getStandardDeviation()); 
		String median = (stats == null ? "" : stats.getMedian());
		String mode = (stats == null ? "" : stats.getMode()); 
		String rank = (stats == null ? "" : stats.getRank());

		boolean isExcused = DataTypeConversionUtil.checkBoolean((Boolean)learner.get(excusedFlag));
		boolean isIncluded = DataTypeConversionUtil.checkBoolean((Boolean)item.getIncluded());

		BaseModel model = new BaseModel();

		StringBuilder id = new StringBuilder();
		StringBuilder categoryName = new StringBuilder();
		categoryName.append(item.getCategoryName());

		switch (categoryType) {
		case WEIGHTED_CATEGORIES:
			categoryName.append(" (").append(category.getPercentCourseGrade()).append("% of course grade)");
			if (!isIncluded)
				model.set(Key.S_ITM_WGHT.name(), "Excluded");
			else if (isExcused) 
				model.set(Key.S_ITM_WGHT.name(), "Dropped");
			else
				model.set(Key.S_ITM_WGHT.name(), NumberFormat.getDecimalFormat().format(((Double)item.getPercentCourseGrade())));

		case SIMPLE_CATEGORIES:
			if (category != null) {
				int dropLowest = category.getDropLowest() == null ? 0 : category.getDropLowest().intValue();
				if (dropLowest > 0)
					categoryName.append(" (drop lowest ").append(dropLowest).append(")");
			}
			model.set(Key.S_CTGRY_NM.name(), categoryName.toString());
			id.append(item.getCategoryId()).append(":");
		default:
			model.set(Key.S_ITM_NM.name(), item.getName());
			model.set(Key.S_COMMENT.name(), comment);
			id.append(itemId);
		}

		model.set(Key.S_ID.name(), id.toString());
		model.set(Key.T_DATEDUE.name(), item.getDueDate());

		switch (gradeType) {
		case POINTS:
			if (item.getPoints() != null)
				model.set(Key.S_OUTOF.name(), NumberFormat.getDecimalFormat().format((item.getPoints().doubleValue())));
			break;
		};

		StringBuilder resultBuilder = new StringBuilder();
		if (value == null)
			resultBuilder.append("- ");
		else {

			switch (gradeType) {
			case POINTS:
				resultBuilder.append(NumberFormat.getDecimalFormat().format(((Double)value).doubleValue()));

				break;
			case PERCENTAGES:
				resultBuilder.append(NumberFormat.getDecimalFormat().format(((Double)value).doubleValue()))
				.append("%");

				break;
			case LETTERS:
				resultBuilder.append(value);
				break;
			}

		}

		if (!isIncluded || isExcused) 
			model.set(Key.B_DROPPED.name(), Boolean.TRUE);

		model.set(Key.S_GRD.name(), resultBuilder.toString());

		if (stats != null) {
			model.set(Key.S_MEAN.name(), mean);
			model.set(Key.S_STDV.name(), stdDev);
			model.set(Key.S_MEDI.name(), median);
			model.set(Key.S_MODE.name(), mode);
			model.set(Key.S_RANK.name(), rank);
			model.set(Key.S_ITM_ID.name(), stats.getAssignmentId());
		}


		model.set(Key.S_ORDER.name(), String.valueOf(row));

		return model;
	}

	// So for stats, we'll have the following columns: 
	// grade | Mean | Std Deviation | Median | Mode | Comment 

	private void setGradeInfoTable(Gradebook selectedGradebook, ModelData learner, List<Statistics> statsList) {		

		ItemModel gradebookItemModel = (ItemModel)selectedGradebook.getGradebookItemModel();
		CategoryType categoryType = gradebookItemModel.getCategoryType();
		GradeType gradeType = gradebookItemModel.getGradeType();

		int weightColumnIndex = cm.findColumnIndex(weightColumn.getDataIndex());
		int outOfColumnIndex = cm.findColumnIndex(outOfColumn.getDataIndex());

		switch (categoryType) {
		case WEIGHTED_CATEGORIES:
			cm.setHidden(weightColumnIndex, false);
			break;
		default:
			cm.setHidden(weightColumnIndex, true);
		}

		switch (gradeType) {
		case POINTS:
			cm.setHidden(outOfColumnIndex, false);
			break;
		default:
			cm.setHidden(outOfColumnIndex, true);
		}

		store.removeAll();

		boolean isDisplayReleasedItems = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getReleaseItems());
		
		if (isDisplayReleasedItems) {
		
			boolean isNothingToDisplay = true;
			isAnyCommentPopulated = false;
			int row=0;

			ArrayList<BaseModel> models = new ArrayList<BaseModel>();
			
			/*
			 * Adding the gradebook level statistics information to the table
			 */
			boolean doReleaseGrades = DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseGrades());
			boolean doReleaseItems = DataTypeConversionUtil.checkBoolean(selectedGradebook.getGradebookItemModel().getReleaseItems());

			if (!isStudentView || doReleaseGrades || doReleaseItems) {

				boolean isShowMean = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMean());
				boolean isShowMedian = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMedian());
				boolean isShowMode = DataTypeConversionUtil.checkBoolean(gradebookItemModel.getShowMode());
				
				if (doReleaseGrades) {
				
					Statistics gradebookStatistics = getStatsModelForItem(String.valueOf(Long.valueOf(-1)), statsList);
					
					if(null != gradebookStatistics) {
					
						BaseModel gradebookStatisticsModel = new BaseModel();

						gradebookStatisticsModel.set(Key.S_ID.name(), COURSE_GRADE_ID);
						gradebookStatisticsModel.set(Key.S_CTGRY_NM.name(), "Course Statistics");
						gradebookStatisticsModel.set(Key.S_ITM_NM.name(), gradebookStatistics.getName());
						gradebookStatisticsModel.set(Key.S_ITM_WGHT.name(), "-");
						gradebookStatisticsModel.set(Key.S_GRD.name(), "-");
						gradebookStatisticsModel.set(Key.S_OUTOF.name(), "-");
						gradebookStatisticsModel.set(Key.T_DATEDUE.name(), "-");

						gradebookStatisticsModel.set(Key.S_MEAN.name(), (isShowMean ? gradebookStatistics.getMean() : ""));
						gradebookStatisticsModel.set(Key.S_STDV.name(), (isShowMean ? gradebookStatistics.getStandardDeviation() : ""));
						gradebookStatisticsModel.set(Key.S_MEDI.name(), (isShowMedian ? gradebookStatistics.getMedian() : ""));
						gradebookStatisticsModel.set(Key.S_MODE.name(), (isShowMode ? gradebookStatistics.getMode() : ""));

						models.add(gradebookStatisticsModel);
						row++;
					}
				}
			}

			
			/*
			 * Adding category and assignment level statistics information to the table
			 */
			int childCount = gradebookItemModel.getChildCount();
			if (childCount > 0) {
				
				/*
				 * The children are either Categories or Items depending on the gradebook setup
				 */
				for (int i=0;i<childCount;i++) {
					
					ModelData m = gradebookItemModel.getChild(i);
					ItemModel child = (ItemModel)m;
					switch (child.getItemType()) {
					case CATEGORY:
						int itemCount = child.getChildCount();
						if (itemCount > 0) {
							/*
							 * Adding all the assignments within a category
							 */
							for (int j=0;j<itemCount;j++) {
								
								ItemModel item = (ItemModel)child.getChild(j);
								
								if (DataTypeConversionUtil.checkBoolean(item.getReleased())) {
									
									Statistics stats = null; 
									stats = getStatsModelForItem(item.getIdentifier(), statsList); 

									BaseModel model = populateGradeInfoRow(i*1000 + j + 10000, item, child, learner, stats, categoryType, gradeType);
									if (!isAnyCommentPopulated && model.get(Key.S_COMMENT.name()) != null)
										isAnyCommentPopulated = true;
									models.add(model);
									isNothingToDisplay = false;
									row++;
								} 
							}
						}
						break;
					case ITEM:
						Statistics stats = getStatsModelForItem(child.getIdentifier(), statsList); 

						if (DataTypeConversionUtil.checkBoolean(child.getReleased())) {
							BaseModel model = populateGradeInfoRow(row, child, null, learner, stats, categoryType, gradeType);
							if (!isAnyCommentPopulated && model.get(Key.S_COMMENT.name()) != null)
								isAnyCommentPopulated = true;
							models.add(model);
							isNothingToDisplay = false;
							row++;
						}
						break;
					}
				}

				store.add(models);
				if (categoryType == CategoryType.NO_CATEGORIES) {
					categoryColumn.setHidden(true);
					store.clearGrouping();
				} else
					store.groupBy(Key.S_CTGRY_NM.name());
			}

			if (isNothingToDisplay) {
				cardLayout.setActiveItem(textPanel);
				I18nConstants i18n = Registry.get(AppConstants.I18N);
				textNotification.setHtml(i18n.notifyNoReleasedItems());
				textPanel.show();
			} else {
				cardLayout.setActiveItem(gradeInformationPanel);
				gradeInformationPanel.show();
			}

		} else {
			I18nConstants i18n = Registry.get(AppConstants.I18N);
			textNotification.setHtml(i18n.notifyNotDisplayingReleasedItems());
			cardLayout.setActiveItem(textPanel);
			textPanel.show();
		}
	}

	private Statistics getStatsModelForItem(String id, List<Statistics> statsList) {
		int idx = -1; 

		Statistics key = new StatisticsModel();
		key.setAssignmentId(id);

		// GRBK-777 : NOTE the statsList is sorted at this point. The sorting is done 
		// earlier in refreshGradeData(...)
		idx = Collections.binarySearch(statsList, key, statisticsComparator);

		if (idx >= 0)
		{
			return statsList.get(idx); 
		}

		return null;
	}
	
	private void getGradeItemStatisticsChartData(String assignmentId) {

		// First we check the cache if we have the data already
		String cacheKey = assignmentId;
		
		if(dataTableCache.containsKey(cacheKey)) {
			
			// Cache hit
			dataTable = dataTableCache.get(cacheKey);
			statisticsChartPanel.setDataTable(dataTable);
			statisticsChartPanel.show();
		}
		else {
			
			statisticsChartPanel.showUserFeedback(null);
			
			// Data is not in cache yet
			Gradebook gbModel = Registry.get(AppConstants.CURRENT);
			
			RestBuilder builder = RestBuilder.getInstance(
					Method.GET,
					GWT.getModuleBaseURL(),
					AppConstants.REST_FRAGMENT,
					AppConstants.STATISTICS_FRAGMENT,
					AppConstants.STUDENT_FRAGMENT,
					gbModel.getGradebookUid(),
					gbModel.getGradebookId().toString(),
					assignmentId);


			// Keeping track of the assignmentId so that we can add the dataTable to
			// the cache once the call returns
			selectedAssignmentId = assignmentId;

			builder.sendRequest(200, 400, null, new RestCallback() {

				public void onError(Request request, Throwable caught) {
					
					statisticsChartPanel.hideUserFeedback(null);
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.statisticsDataErrorTitle(), i18n.statisticsDataErrorMsg(), true));
				}

				public void onFailure(Request request, Throwable exception) {
					
					statisticsChartPanel.hideUserFeedback(null);
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.statisticsDataErrorTitle(), i18n.statisticsDataErrorMsg(), true));
				}

				public void onSuccess(Request request, Response response) {

					String jsonText = response.getText(); 
					
					if (jsonText != null && !"".equals(jsonText) ) {
					
						JSONValue jsonValue = JSONParser.parseStrict(jsonText);
						JSONArray jsonArray = jsonValue.isArray();

						JSONArray positiveFrequencies = jsonArray.get(AppConstants.POSITIVE_NUMBER).isArray();

						dataTable = DataTable.create();
						dataTable.addColumn(ColumnType.STRING, i18n.statisticsChartLabelDistribution());
						dataTable.addColumn(ColumnType.NUMBER, i18n.statisticsChartLabelFrequency());
						dataTable.addRows(positiveFrequencies.size());

						String[] xAxisRangeLabels = statisticsChartPanel.getXAxisRangeLabels();
						
						for (int i = 0; i < positiveFrequencies.size(); i++) {

							// Set label
							dataTable.setValue(i, 0, xAxisRangeLabels[i]);

							// Set value
							double positiveValue = positiveFrequencies.get(i).isNumber().doubleValue();
							dataTable.setValue(i, 1, positiveValue);
						}

						// adding the dataTable to the cache
						dataTableCache.put(selectedAssignmentId, dataTable);
						statisticsChartPanel.setDataTable(dataTable);
						statisticsChartPanel.show();
					}
					else
					{
						Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.statisticsDataErrorTitle(), i18n.statisticsDataErrorMsg(), true));
					}
					
					statisticsChartPanel.hideUserFeedback(null);
				}
			});
		}
	}
}
