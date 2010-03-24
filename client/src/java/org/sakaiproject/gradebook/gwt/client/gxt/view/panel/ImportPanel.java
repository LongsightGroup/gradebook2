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

package org.sakaiproject.gradebook.gwt.client.gxt.view.panel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.RestBuilder;
import org.sakaiproject.gradebook.gwt.client.RestCallback;
import org.sakaiproject.gradebook.gwt.client.gxt.ItemModelProcessor;
import org.sakaiproject.gradebook.gwt.client.gxt.JsonTranslater;
import org.sakaiproject.gradebook.gwt.client.gxt.custom.widget.grid.BaseCustomGridView;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.model.EntityModelComparer;
import org.sakaiproject.gradebook.gwt.client.gxt.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportHeader;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportHeader.Field;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;
import org.sakaiproject.gradebook.gwt.client.model.Item;
import org.sakaiproject.gradebook.gwt.client.model.key.ItemKey;
import org.sakaiproject.gradebook.gwt.client.model.key.LearnerKey;
import org.sakaiproject.gradebook.gwt.client.model.key.UploadKey;
import org.sakaiproject.gradebook.gwt.client.model.type.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.type.ItemType;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Element;

public class ImportPanel extends GradebookPanel {

	private FileUploadField file;
	private String msgsFromServer; 
	private ListStore<ModelData> rowStore;
	private ListStore<ItemModel> itemStore;
	private ListStore<ItemModel> categoriesStore;
	private Grid<ModelData> grid;
	private EditorGrid<ItemModel> itemGrid;
	private FormPanel fileUploadPanel;
	private Map<String, ImportHeader> headerMap;
	private ComboBox<ItemModel> categoryPicker;
	private CellEditor categoryEditor; 

	private LayoutContainer mainCardLayoutContainer; //, subCardLayoutContainer;
	private CardLayout mainCardLayout; //, subCardLayout;

	private TabPanel tabPanel;

	private TabItem previewTab, columnsTab;

	private Button submitButton, cancelButton, errorReturnButton; 

	private ContentPanel step1Container;
	private ContentPanel errorContainer; 

	private ColumnConfig percentCategory;

	private LayoutContainer fileUploadContainer;

	private ArrayList<ColumnConfig> previewColumns;
	private ArrayList<ItemModel> invisibleItemModels;

	private MessageBox uploadBox;

	private boolean isGradingFailure;
	private MessageBox uploadingBox;
	private RowNumberer r;

	public ImportPanel() {
		super();
		setCollapsible(false);
		setFrame(true);
		setHeaderVisible(true);
		setHeading(i18n.headerImport());
		setHideCollapseTool(true);
		setLayout(new FitLayout());

		headerMap = new HashMap<String, ImportHeader>();

		categoriesStore = new ListStore<ItemModel>();
	}

	protected void onClose() {

	}

	private void refreshCategoryPickerStore(Item gradebookItemModel) {
		categoriesStore.removeAll();
		if (gradebookItemModel != null) {

			ItemModelProcessor processor = new ItemModelProcessor(gradebookItemModel) {

				@Override
				public void doCategory(Item categoryModel) {
					categoriesStore.add((ItemModel)categoryModel);
				}

			};

			processor.process();
		}
	}

	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		this.isGradingFailure = false;
		
		Gradebook selectedGradebook = Registry.get(AppConstants.CURRENT);

		if (selectedGradebook != null) 
			refreshCategoryPickerStore(selectedGradebook.getGradebookItemModel());


		mainCardLayout = new CardLayout();
		mainCardLayoutContainer = new LayoutContainer();
		mainCardLayoutContainer.setLayout(mainCardLayout);

		tabPanel = new TabPanel();

		r = new RowNumberer();
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		//configs.add(r);
		
		rowStore = new ListStore<ModelData>();
		rowStore.setMonitorChanges(true);
		rowStore.setModelComparer(new EntityModelComparer<ModelData>(LearnerKey.UID.name()));

		ColumnModel cm = new ColumnModel(configs);
		grid = new Grid<ModelData>(rowStore, cm);
		grid.setLoadMask(false);
		grid.addPlugin(r);

		CellSelectionModel<ModelData> cellSelectionModel = new CellSelectionModel<ModelData>();
		cellSelectionModel.setSelectionMode(SelectionMode.SINGLE);
		grid.setSelectionModel(cellSelectionModel);
		grid.setView(new BaseCustomGridView() {

			protected boolean isDropped(ModelData model, String property) {
				String droppedProperty = DataTypeConversionUtil.buildDroppedKey(property);
				Boolean isDropped = model.get(droppedProperty);

				return isDropped != null && isDropped.booleanValue();
			}

			protected boolean isReleased(ModelData model, String property) {
				return false;
			}

			@Override
			protected String markupCss(Record r, ModelData model, String property, boolean isShowDirtyCells, boolean isPropertyChanged) {

				if (property == null || property.equalsIgnoreCase("numberer"))
					return null;
				
				boolean isUserNotFound = DataTypeConversionUtil.checkBoolean((Boolean)model.get("userNotFound"));

				if (isUserNotFound)
					return resources.css().gbCellDropped();

				StringBuilder css = new StringBuilder();

				if (r != null) {
					String failedProperty = new StringBuilder().append(property).append(AppConstants.FAILED_FLAG).toString();
					String failedMessage = (String)r.get(failedProperty);

					String successProperty = new StringBuilder().append(property).append(AppConstants.SUCCESS_FLAG).toString();
					String successMessage = (String)r.get(successProperty);
					
					if (failedMessage != null) {
						css.append(" ").append(resources.css().gbCellFailedImport());
						isGradingFailure = true;
					} else if (successMessage != null) {
						if (GXT.isIE)
							css.append(" ieGbCellSucceeded");
						else
							css.append(" ").append(resources.css().gbCellSucceeded());
					}
				}

				if (isDropped(model, property)) {
					css.append(" ").append(resources.css().gbCellDropped());
				}

				if (isReleased(model, property)) {
					css.append(" ").append(resources.css().gbReleased());
				}

				if (css.length() > 0)
					return css.toString();

				return null;
			}
		});

		boolean hasCategories = selectedGradebook.getGradebookItemModel().getCategoryType() != CategoryType.NO_CATEGORIES;
		
		previewTab = new TabItem("Data");
		previewTab.setLayout(new FlowLayout());
		previewTab.add(grid);

		tabPanel.add(previewTab);

		columnsTab = new TabItem("Setup");
		columnsTab.setLayout(new FlowLayout());
		columnsTab.add(buildItemGrid());

		tabPanel.add(columnsTab);

		if (!hasCategories) 
			columnsTab.setVisible(false);

		step1Container = new ContentPanel();
		step1Container.setHeaderVisible(false);
		step1Container.setLayout(new FitLayout());
		step1Container.add(tabPanel);

		submitButton = new Button("Next");
		submitButton.setMinWidth(120);
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {	
				itemGrid.stopEditing(); 

				List<ModelData> allItemModels = new ArrayList<ModelData>();
				allItemModels.addAll(itemStore.getModels());
				allItemModels.addAll(invisibleItemModels);
				List<ModelData> rowModels = rowStore.getModels();
				int numberOfRows = rowModels == null ? 0 : rowModels.size();
				JSONObject spreadsheetModel = composeSpreadsheetModel(allItemModels, rowModels, previewColumns);

				submitButton.setVisible(false);
				uploadSpreadsheet(spreadsheetModel, numberOfRows);
			}
		});
		step1Container.addButton(submitButton);

		cancelButton = new Button("Cancel");
		cancelButton.setMinWidth(120);
		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Dispatcher.forwardEvent(GradebookEvents.StopImport.getEventType());
				fileUploadPanel.clear();
			}
		});

		step1Container.addButton(cancelButton);

		errorReturnButton = new Button("Return");
		errorReturnButton.setMinWidth(120);
		errorReturnButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Dispatcher.forwardEvent(GradebookEvents.StopImport.getEventType());
				fileUploadPanel.clear();
			}
		});
		errorContainer = new ContentPanel(); 
		errorContainer.setHeaderVisible(false); 
		errorContainer.setLayout(new FitLayout()); 
		errorContainer.addButton(errorReturnButton);

		fileUploadContainer = new LayoutContainer();
		fileUploadContainer.setLayout(new FlowLayout());
		fileUploadContainer.add(buildFileUploadPanel());

		mainCardLayoutContainer.add(fileUploadContainer);
		mainCardLayoutContainer.add(step1Container);
		mainCardLayoutContainer.add(errorContainer); 
		mainCardLayout.setActiveItem(fileUploadContainer);
		add(mainCardLayoutContainer); 
	}


	@Override
	protected void onResize(final int width, final int height) {
		super.onResize(width, height);

		if (tabPanel != null)
			tabPanel.setHeight(height);

		if (grid != null)
			grid.setHeight(height - 100);

		if (itemGrid != null)
			itemGrid.setHeight(height - 100);

	}

	private JSONObject composeSpreadsheetModel(List<ModelData> items, 
			List<ModelData> importRows, List<ColumnConfig> previewColumns) {

		JSONObject spreadsheetModel = new JSONObject();
		JSONArray itemArray = RestBuilder.convertList(items);
		
		spreadsheetModel.put(UploadKey.HEADERS.name(), itemArray);

		JSONArray rows = new JSONArray();
		int i=0;
		for (ModelData importRow : importRows) {
			
			boolean isUserNotFound = DataTypeConversionUtil.checkBoolean((Boolean)importRow.get("userNotFound"));

			if (isUserNotFound)
				continue;

			String uid = importRow.get("userUid");
			if (uid == null)
				uid = importRow.get("userImportId");

			JSONObject student = new JSONObject();
			student.put(LearnerKey.UID.name(), new JSONString(uid));

			for (ColumnConfig column : previewColumns) {
				String id = column.getId();
				Object value = importRow.get(id);
				if (value instanceof String)
					student.put(id, new JSONString((String)value));
				else if (value instanceof Double)
					student.put(id, new JSONNumber((Double)value));
			}
			rows.set(i++, student);
		}
		spreadsheetModel.put(UploadKey.ROWS.name(), rows);
		spreadsheetModel.put(UploadKey.NUMBER_OF_ROWS.name(), new JSONNumber(Double.valueOf(i)));

		return spreadsheetModel;
	}
	
	private void uploadSpreadsheet(JSONObject spreadsheetModel, int numberOfLearners) {
		Gradebook gbModel = Registry.get(AppConstants.CURRENT);

		String message = new StringBuilder().append(i18n.uploadingLearnerGradesPrefix()).append(" ")
		 .append(numberOfLearners).append(" ").append(i18n.uploadingLearnerGradesSuffix()).toString();

		uploadingBox = MessageBox.wait(i18n.uploadingLearnerGradesTitle(), message, i18n.uploadingLearnerGradesStatus());

		RestBuilder builder = RestBuilder.getInstance(RestBuilder.Method.PUT, GWT.getModuleBaseURL(),
				AppConstants.REST_FRAGMENT,
				AppConstants.UPLOAD_FRAGMENT, gbModel.getGradebookUid(), String.valueOf(gbModel.getGradebookId()));

		builder.sendRequest(200, 400, spreadsheetModel.toString(), new RestCallback() {

			@Override
			public void onError(Request request, Throwable caught) {
				Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught));
				uploadingBox.close();
				submitButton.setVisible(true);
			}
			
			@Override
			public void onSuccess(Request request, Response response) {
				
				try {
					
					JsonTranslater translater = new JsonTranslater(EnumSet.allOf(UploadKey.class));
					
					ModelData result = translater.translate(response.getText());
					
					List<ModelData> rows = result.get(UploadKey.ROWS.name());
					
					int numberOfScoresChanged = 0;
					if (rows != null) {
						int rowNumber = 0;
						for (ModelData student : rows) {
	
							boolean hasChanges = DataTypeConversionUtil.checkBoolean((Boolean)student.get(AppConstants.IMPORT_CHANGES));
	
							if (hasChanges) {
								ModelData model = rowStore.getAt(rowNumber);
								String learnerUid = model.get(LearnerKey.UID.name());
								String currentUid = student.get(LearnerKey.UID.name());
								if (learnerUid == null || !learnerUid.equals(currentUid))
									model = rowStore.findModel(LearnerKey.UID.name(), currentUid);
								Record record = rowStore.getRecord(model);
								record.beginEdit();
	
								for (String p : student.getPropertyNames()) {
									boolean needsRefreshing = false;
	
									int index = -1;
	
									if (p.endsWith(AppConstants.FAILED_FLAG)) {
										index = p.indexOf(AppConstants.FAILED_FLAG);
										needsRefreshing = true;
									} else if (p.endsWith(AppConstants.SUCCESS_FLAG)) {
										index = p.indexOf(AppConstants.SUCCESS_FLAG);
										needsRefreshing = true;
										numberOfScoresChanged++;
									}
	
									if (needsRefreshing && index != -1) {
										String assignmentId = p.substring(0, index);
										Object value = student.get(assignmentId);
	
										record.set(p, student.get(p));
										
										record.set(assignmentId, null);
										record.set(assignmentId, value);
									}
								}
								record.endEdit();
								
							}
							
							rowNumber++;
						}
					}
					
					StringBuilder heading = new StringBuilder().append("Result Data (").append(numberOfScoresChanged).append(" scores modified)");
					previewTab.setText(heading.toString());
					
					uploadingBox.setProgressText("Loading");

					cancelButton.setText("Done");

					Gradebook selectedGradebook = Registry.get(AppConstants.CURRENT);
					selectedGradebook.setGradebookGradeItem((ItemModel)result.get(UploadKey.GRADEBOOK_ITEM_MODEL.name()));
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookSetup.getEventType(), selectedGradebook);
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(), selectedGradebook);
					// Have to do this one, otherwise the multigrid is not fully refreshed. 
					Dispatcher.forwardEvent(GradebookEvents.RefreshCourseGrades.getEventType());
				} catch (Exception e) {
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(e));
				} finally {
					uploadingBox.close();
				}
				
				if (isGradingFailure) {
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.importGradesFailedTitle(), i18n.importGradesFailedMessage(), true, true));
				}
			}
			
		});
		
		/*
		AsyncCallback<SpreadsheetModel> callback =
			new AsyncCallback<SpreadsheetModel>() {

			public void onFailure(Throwable caught) {
				Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(caught));
				uploadingBox.close();
			}

			public void onSuccess(SpreadsheetModel result) {

				try {
					List<ModelData> rows = result.getRows();
					int size = rows == null ? 0 : rows.size();
					
					StringBuilder heading = new StringBuilder().append("Result Data (").append(size).append(" records uploaded)");
					previewTab.setText(heading.toString());
					
					for (ModelData student : rows) {

						boolean hasChanges = DataTypeConversionUtil.checkBoolean((Boolean)student.get(AppConstants.IMPORT_CHANGES));

						if (hasChanges) {
							Record record = rowStore.getRecord(student);
							record.beginEdit();

							for (String p : student.getPropertyNames()) {
								boolean needsRefreshing = false;

								int index = -1;

								if (p.endsWith(StudentModel.FAILED_FLAG)) {
									index = p.indexOf(StudentModel.FAILED_FLAG);
									needsRefreshing = true;
								} 

								if (needsRefreshing && index != -1) {
									String assignmentId = p.substring(0, index);
									Object value = result.get(assignmentId);

									record.set(assignmentId, null);
									record.set(assignmentId, value);

								}
							}
							record.endEdit();
						}
					}

					uploadingBox.setProgressText("Loading");

					cancelButton.setText("Done");

					GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
					selectedGradebook.setGradebookItemModel(result.getGradebookItemModel());
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookSetup.getEventType(), selectedGradebook);
					Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(), selectedGradebook);
					// Have to do this one, otherwise the multigrid is not fully refreshed. 
					Dispatcher.forwardEvent(GradebookEvents.RefreshCourseGrades.getEventType());
				} catch (Exception e) {
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(e));
				} finally {
					uploadingBox.close();
				}
				
				if (isGradingFailure) {
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.importGradesFailedTitle(), i18n.importGradesFailedMessage(), true, true));
				}
			}
		};

		Gradebook2RPCServiceAsync service = Registry.get(AppConstants.SERVICE);

		service.create(gbModel.getGradebookUid(), gbModel.getGradebookId(), spreadsheetModel, EntityType.SPREADSHEET, SecureToken.get(), callback);
		*/
	}

	private FormPanel buildFileUploadPanel() {
		final Gradebook gbModel = Registry.get(AppConstants.CURRENT);

		FormLayout formLayout = new FormLayout();
		formLayout.setDefaultWidth(350);
		formLayout.setLabelWidth(120);

		fileUploadPanel = new FormPanel();

		fileUploadPanel.setHeaderVisible(false);

		String action = new StringBuilder().append(GWT.getHostPageBaseURL())
			.append(AppConstants.IMPORT_SERVLET).toString();
		
		fileUploadPanel.setFrame(true);
		fileUploadPanel.setAction(action);
		fileUploadPanel.setEncoding(Encoding.MULTIPART);
		fileUploadPanel.setMethod(Method.POST);
		fileUploadPanel.setPadding(4);
		fileUploadPanel.setButtonAlign(HorizontalAlignment.RIGHT);

		fileUploadPanel.setLayout(formLayout);


		file = new FileUploadField() {
			@Override
			protected void onChange(ComponentEvent ce) {
				super.onChange(ce);
			}
		};
		file.setAllowBlank(false);
		file.setFieldLabel(i18n.fileLabel());
		file.setName("Test");

		fileUploadPanel.add(file);

		HiddenField<String> gradebookUidField = new HiddenField<String>();
		gradebookUidField.setName("gradebookUid");
		gradebookUidField.setValue(gbModel.getGradebookUid());
		fileUploadPanel.add(gradebookUidField);

		Button submitButton = new Button(i18n.nextButton());
		submitButton.setMinWidth(120);
		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				readFile();
			}

		});
		fileUploadPanel.addButton(submitButton);

		Button cancelButton = new Button(i18n.cancelButton());
		cancelButton.setMinWidth(120);
		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				Dispatcher.forwardEvent(GradebookEvents.StopImport.getEventType());
				fileUploadPanel.clear();
			}
		});

		fileUploadPanel.addButton(cancelButton);

		fileUploadPanel.addListener(Events.Submit, new Listener<FormEvent>() {

			public void handleEvent(FormEvent fe) {
				readSubmitResponse(fe.getResultHtml());
			}

		});

		return fileUploadPanel;
	}

	private void readSubmitResponse(String result) {

		try {

			rowStore.removeAll();

			result = result.replaceAll("\\u000a", " ");
			result = result.replaceAll("\\n", "\\\\n");
			JSONValue jsonValue = JSONParser.parse(result);

			if (jsonValue == null)
				throw new Exception("Server response incorrect. Unable to parse result.");

			JSONObject jsonWrapper = jsonValue.isObject();

			if (jsonWrapper == null)
				throw new Exception("Server response incorrect. Unable to read data.");

			JSONObject jsonObject = jsonWrapper.get("org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportFile").isObject();

			JSONArray headersArray = getArray(jsonObject, "items");
			previewColumns = new ArrayList<ColumnConfig>();
			previewColumns.add(r);
			boolean hasErrors = DataTypeConversionUtil.checkBoolean(getBoolean(jsonObject, "hasErrors")); 
			boolean hasAssignmentNameIssueForScantron = DataTypeConversionUtil.checkBoolean(getBoolean(jsonObject, "notifyAssignmentName")); 


			msgsFromServer = getString(jsonObject, "notes");

			// If we have errors we want to do something different
			if (hasErrors) 
			{
				errorContainer.addText(msgsFromServer); 
				mainCardLayout.setActiveItem(errorContainer); 
				tabPanel.hide();
				return; 
			}
			Boolean hasWeightsBoolean = getBoolean(jsonObject, "hasWeights");
			Boolean hasCategoriesBoolean = getBoolean(jsonObject, "hasCategories");
			Boolean isPointsModeBoolean = getBoolean(jsonObject, "isPointsMode");

			boolean hasWeights = DataTypeConversionUtil.checkBoolean(hasWeightsBoolean);
			boolean hasCategories = DataTypeConversionUtil.checkBoolean(hasCategoriesBoolean);
			boolean isPointsMode = DataTypeConversionUtil.checkBoolean(isPointsModeBoolean);
			
			percentCategory.setHidden(!hasWeights);
			
			if (hasCategories) {
				columnsTab.setVisible(true);

				final Gradebook selectedGradebook = Registry.get(AppConstants.CURRENT);

				if (selectedGradebook != null) {

					if (selectedGradebook.getGradebookItemModel().getCategoryType() == CategoryType.NO_CATEGORIES) {
						
						RestBuilder builder = RestBuilder.getInstance(RestBuilder.Method.GET, 
								GWT.getModuleBaseURL(),
								AppConstants.REST_FRAGMENT,
								AppConstants.ITEM_FRAGMENT,
								selectedGradebook.getGradebookUid(),
								String.valueOf(selectedGradebook.getGradebookId()));
						
					
						builder.sendRequest(200, 400, null, new RestCallback() {

							public void onSuccess(Request request, Response response) {
								String result = response.getText();

								JsonTranslater translater = new JsonTranslater(EnumSet.allOf(ItemKey.class)) {
									protected ModelData newModelInstance() {
										return new ItemModel();
									}
								};
								ItemModel itemModel = (ItemModel)translater.translate(result);
								
								if (itemModel != null) {
									refreshCategoryPickerStore(itemModel);

									selectedGradebook.setGradebookGradeItem(itemModel);
									Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookSetup.getEventType(), selectedGradebook);
									Dispatcher.forwardEvent(GradebookEvents.RefreshGradebookItems.getEventType(), selectedGradebook);
								}
							}
							
						});

					}

				}
			}

			boolean hasUnassignedItem = false;

			if (headersArray != null) {	
				headerMap.clear();
				for (int i=0;i<headersArray.size();i++) {
					JSONValue value = headersArray.get(i);
					if (value == null) 
						continue;

					JSONObject jsonHeaderObject = value.isObject();

					if (jsonHeaderObject == null)
						continue;

					String name = getString(jsonHeaderObject, "value");
					String id = getString(jsonHeaderObject, "id");
					String headerName = getString(jsonHeaderObject, "headerName");
					Double points = getDouble(jsonHeaderObject, "points");
					String field = getString(jsonHeaderObject, "field");
					String categoryName = getString(jsonHeaderObject, "categoryName");
					String categoryId = getString(jsonHeaderObject, "categoryId");
					Double percentCategory = getDouble(jsonHeaderObject, "percentCategory");
					Boolean isExtraCredit = getBoolean(jsonHeaderObject, "extraCredit");
					Boolean isUnincluded = getBoolean(jsonHeaderObject, "unincluded");
					String assignmentId = getString(jsonHeaderObject, "assignmentId");

					int width = 200;

					StringBuilder nameBuilder = new StringBuilder();
					nameBuilder.append(name);

					if (id == null)
						continue;

					if (categoryId != null && categoryId.equals("null"))
						categoryId = null;
					
					if (id.equals("ID"))
						width = 100;
					else if (!id.equals("NAME"))
						width = name.length() * 7;

					if (id.startsWith("NEW:")) {
						nameBuilder.append("*");

						if (categoryId == null)
							hasUnassignedItem = true;
					}

					ColumnConfig column = new ColumnConfig(id, nameBuilder.toString(), width);
					previewColumns.add(column);


					ImportHeader header = new ImportHeader(Field.valueOf(field), headerName);
					header.setId(id);
					header.setHeaderName(headerName);
					header.setPoints(points);
					header.setField(field);
					header.setCategoryName(categoryName);
					header.setCategoryId(categoryId);
					header.setPercentCategory(percentCategory);
					header.setExtraCredit(isExtraCredit);
					header.setUnincluded(isUnincluded);
					header.setAssignmentId(assignmentId);

					if (header.getField() != null)
						headerMap.put(id, header);
				}
				ColumnModel cm = new ColumnModel(previewColumns);
				grid.reconfigure(rowStore, cm);
			}


			mainCardLayout.setActiveItem(step1Container);



			boolean pointsIssue = false; 
			StringBuilder pointsAssignments = null;
			JSONArray rowsArray = getArray(jsonObject, "rows");
			ArrayList<ModelData> models = new ArrayList<ModelData>();
			
			boolean hasUserNotFound = false;
			if (rowsArray != null) {
				StringBuilder heading = new StringBuilder().append("Preview Data (").append(rowsArray.size()).append(" records)");
				previewTab.setText(heading.toString());

				for (int i=0;i<rowsArray.size();i++) {
					JSONValue value = rowsArray.get(i);
					if (value == null)
						continue;

					JSONObject rowObject = value.isObject();
					String userUid = getString(rowObject.isObject(), "userUid");
					String userImportId = getString(rowObject.isObject(), "userImportId");
					String userDisplayName = getString(rowObject.isObject(), "userDisplayName");
					Boolean userNotFound = getBoolean(rowObject.isObject(), "isUserNotFound");
					JSONArray columnsArray = getArray(rowObject, "columns");

					if (!hasUserNotFound && DataTypeConversionUtil.checkBoolean(userNotFound))
						hasUserNotFound = userNotFound;
					
					ModelData model = new BaseModel();
					if (userUid != null)
						model.set(LearnerKey.UID.name(), userUid);
					else if (userImportId != null)
						model.set(LearnerKey.UID.name(), userImportId);

					model.set("userUid", userUid);
					model.set("userImportId", userImportId);
					model.set("userDisplayName", userDisplayName);
					model.set("userNotFound", userNotFound);

					if (columnsArray != null) {
						for (int j=0;j<columnsArray.size();j++) {
							if (previewColumns != null && previewColumns.size() -1 > j) {
								ColumnConfig config = previewColumns.get(j+1);
								if (config != null) {									
									JSONValue itemValue = columnsArray.get(j);
									if (itemValue == null)
										continue;
									JSONString itemString = itemValue.isString();
									if (itemString == null)
										continue;
									String configId = config.getId(); 
									ImportHeader h = headerMap.get(configId);
									if (isPointsMode && null != h && h.getField().equals(Field.ITEM.name()) &&
											null != itemString && !"".equals(itemString.stringValue())) {
										
										Double maxPoints = h.getPoints(); 
										if (maxPoints == null)
										{
											maxPoints = new Double(100.0); 
										}
										double itemPoints = Double.parseDouble(itemString.stringValue());
										if (itemPoints > maxPoints.doubleValue())
										{
											pointsIssue = true; 
											if (pointsAssignments == null)
											{
												pointsAssignments = new StringBuilder();
												pointsAssignments.append(h.getHeaderName()); 
											}
											else
											{
												if (pointsAssignments.indexOf( h.getHeaderName() ) == -1)
												{
													pointsAssignments.append(", "); 
													pointsAssignments.append(h.getHeaderName()); 
												}
											}
										}
									}
									model.set(config.getId(), itemString.stringValue());
								}
							}
						}

					}

					models.add(model);
				}
			}

			/*
			 * If there are unassigned assignments in a categories gradebook, problems with the assignment 
			 * name, or a points issue(the latter two being scantron problem) then we'll put a window up. 
			 * 
			 */
			boolean showPanel =  false; 
			boolean hasDefaultMsg = false;
			StringBuilder sb = null; 
			if (hasUnassignedItem && hasCategories) 
			{
				showPanel = true; 
				hasDefaultMsg = true; 
			} 

			if (hasUserNotFound) 
			{
				if (sb == null)
				{
					sb = new StringBuilder(); 
				}
				else
				{
					sb.append("<BR>"); 
				}
				showPanel = true; 
				sb.append("<BR>One or more users were not found based on the import identifier provided. This could indicate that the wrong import id is being used, or that the file is incorrectly formatted for import."
				);
			}
			
			if (hasAssignmentNameIssueForScantron)
			{

				if (sb == null)
				{
					sb = new StringBuilder(); 
				}
				else
				{
					sb.append("<BR>"); 
				}
				showPanel = true; 
				sb.append("<BR>The scantron assignment entered has previously been imported.  We have changed the assignment name so that it will be imported uniquely. If you wanted to replace the old data, then please change it back."
				); 				
			}

			if (pointsIssue)
			{
				if (sb == null)
				{
					sb = new StringBuilder(); 
				}
				else
				{
					sb.append("<BR>"); 
				}
				showPanel = true; 
				sb.append("<BR>A student's entered points value is greater than the max points value for an assignment.");
				sb.append("<br>The assignments are: ");
				sb.append(pointsAssignments.toString()); 
				sb.append("<br><br>If you do not increase the max points of the particular assignment(s), then any student grade data that is greater than the points value will not be imported.");
				pointsAssignments = null; 
			}

			if (showPanel)
			{

				String sendText = ""; 
				if (sb != null )
				{
					sendText = sb.toString(); 
					sb = null;
				}
				showSetupPanel(sendText, !hasDefaultMsg);
			}

			if (models != null && !models.isEmpty())
				rowStore.add(models);
			else
				tabPanel.setSelection(columnsTab);

			ColumnModel cm = grid.getColumnModel();
			ArrayList<ImportHeader> headers = new ArrayList<ImportHeader>();

			// First, we need to ensure that all of the assignments exist
			for (int i=0;i<cm.getColumnCount();i++) {
				ColumnConfig config = cm.getColumn(i);

				if (config == null)
					continue;

				String id = config.getId();

				ImportHeader header = headerMap.get(id);

				if (header != null) {
					headers.add(header);
				}					
			}

			ArrayList<ItemModel> itemModels = convertHeadersToItemModels(headers);
			HashMap<Long, String> categoryIdNameMap = new HashMap<Long, String>();

			ArrayList<ItemModel> visibleItemModels = new ArrayList<ItemModel>();
			invisibleItemModels = new ArrayList<ItemModel>();
			
			for (int i=0;i<itemModels.size();i++) {
				ItemModel itemModel = itemModels.get(i);

				if (itemModel.getItemType() != ItemType.COMMENT)
					visibleItemModels.add(itemModel);
				else 
					invisibleItemModels.add(itemModel);
					
				String itemName = itemModel.getName();
				Long categoryId = itemModel.getCategoryId();
				String categoryName = categoryIdNameMap.get(categoryId);
				if (categoryName == null) {

					ItemModel categoryModel = categoriesStore.findModel(ItemKey.ID.name(), String.valueOf(categoryId));

					if (categoryModel == null && itemModel.getCategoryName() != null) {
						categoryModel = new ItemModel();
						categoryModel.setName(itemModel.getCategoryName());
						categoryModel.setIdentifier(String.valueOf(categoryId));
						categoryModel.setCategoryId(categoryId);
						categoriesStore.add(categoryModel);
					}

					if (categoryModel != null)
						categoryIdNameMap.put(categoryId, categoryModel.getName());
				}
			}

			itemStore.add(visibleItemModels);


		} catch (Exception e) {
			Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(e));
		} finally {
			uploadBox.close();
		}

	}

	private EditorGrid<ItemModel> buildItemGrid() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new FitLayout());
		panel.setHeaderVisible(false);

		ArrayList<ColumnConfig> itemColumns = new ArrayList<ColumnConfig>();

		TextField<String> textField = new TextField<String>();
		textField.addInputStyleName(resources.css().gbTextFieldInput());
		CellEditor textCellEditor = new CellEditor(textField);

		ColumnConfig name = new ColumnConfig(ItemKey.NAME.name(), "Item", 200);
		name.setEditor(textCellEditor);
		itemColumns.add(name);

		percentCategory = new ColumnConfig(ItemKey.PERCENT_CATEGORY.name(), "% Category", 100);
		percentCategory.setEditor(new CellEditor(new NumberField()));
		itemColumns.add(percentCategory);

		ColumnConfig points = new ColumnConfig(ItemKey.POINTS.name(), "Points", 100);
		points.setEditor(new CellEditor(new NumberField()));
		itemColumns.add(points);

		categoryPicker = new ComboBox<ItemModel>(); 
		categoryPicker.setAllowBlank(false); 
		categoryPicker.setAllQuery(null);
		categoryPicker.setDisplayField(ItemKey.NAME.name());  
		categoryPicker.setEditable(true);
		categoryPicker.setEmptyText("Required");
		categoryPicker.setFieldLabel("Category");
		categoryPicker.setForceSelection(true);
		categoryPicker.setStore(categoriesStore);
		categoryPicker.setValueField(ItemKey.ID.name());
		categoryPicker.addInputStyleName(resources.css().gbTextFieldInput());

		ColumnConfig category = new ColumnConfig(ItemKey.CATEGORY_ID.name(), "Category", 140);
				
		categoryEditor =	new CellEditor(categoryPicker) {

			@Override
			public Object postProcessValue(Object value) {
				if (value != null) {
					Item model = (Item)value;
					return model.getIdentifier();
				}
				return "None/Default";
			}

			@Override
			public Object preProcessValue(Object value) {
				Long id = (Long)value;

				return categoriesStore.findModel(ItemKey.ID.name(), String.valueOf(id));
			}

		};
		category.setEditor(categoryEditor);

		category.setRenderer(new GridCellRenderer() {

			public String render(ModelData model, String property, ColumnData config, 
					int rowIndex, int colIndex, ListStore store, Grid grid) {

				Object identifier = model.get(property);

				String lookupId = null;

				if (identifier instanceof Long) 
					lookupId = String.valueOf(identifier);
				else
					lookupId = (String)identifier;

				Item itemModel = categoriesStore.findModel(ItemKey.ID.name(), lookupId);

				if (itemModel == null)
					return AppConstants.DEFAULT_CATEGORY_NAME;

				return itemModel.getName();
			}

		});
		itemColumns.add(category);

		ColumnModel itemColumnModel = new ColumnModel(itemColumns);
		itemStore = new ListStore<ItemModel>();

		itemGrid = new EditorGrid<ItemModel>(itemStore, itemColumnModel);
		itemGrid.setBorders(true);
		itemGrid.setView(new BaseCustomGridView());

		LayoutContainer container = new LayoutContainer();
		container.setLayout(new FitLayout());

		return itemGrid;
	}
	
	private ArrayList<ItemModel> convertHeadersToItemModels(ArrayList<ImportHeader> headers) {
		ArrayList<ItemModel> items = new ArrayList<ItemModel>();

		if (headers != null) {
			for (ImportHeader header : headers) {

				ItemModel itemModel = new ItemModel();

				if (header == null)
					continue;

				if (header.getId().equals("ID"))
					continue;

				if (header.getId().equals("NAME"))
					continue;
				
				ItemType type = ItemType.ITEM;
				
				if (header.getField().equals(Field.COMMENT.name()))
					type = ItemType.COMMENT;

				itemModel.setIdentifier(header.getId());
				itemModel.setItemType(type);
				itemModel.setName(header.getHeaderName());
				itemModel.setPoints(header.getPoints());
				itemModel.setExtraCredit(header.getExtraCredit());
				itemModel.setIncluded(Boolean.valueOf(!DataTypeConversionUtil.checkBoolean(header.getUnincluded())));
				if (header.getCategoryId() != null) {
					try {
						itemModel.setCategoryId(Long.valueOf(header.getCategoryId()));
					} catch (NumberFormatException nfe) {
						Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(nfe));
					}
					itemModel.setCategoryName(header.getCategoryName());
				}
				itemModel.setPercentCategory(header.getPercentCategory());

				items.add(itemModel);

			}
		}

		return items;
	}

	private JSONArray getArray(JSONObject object, String property) {

		JSONValue value = object.get(property);

		if (value == null)
			return null;

		return value.isArray();
	}

	private Double getDouble(JSONObject object, String property) {
		if (object == null)
			return null;

		JSONValue value = null;
		if (property != null) {
			value = object.get(property);
			if (value == null)
				return null;
		} else {
			value = object;
		}

		JSONNumber number = value.isNumber();
		if (number == null)
			return null;

		return Double.valueOf(number.doubleValue());
	}

	private Boolean getBoolean(JSONObject object, String property) {
		if (object == null)
			return Boolean.FALSE;

		JSONValue value = null;
		if (property != null) {
			value = object.get(property);
			if (value == null)
				return Boolean.FALSE;
		} else {
			value = object;
		}

		JSONBoolean bool = value.isBoolean();
		if (bool == null)
			return Boolean.FALSE;

		return Boolean.valueOf(bool.booleanValue());
	}

	private String getString(JSONObject object, String property) {
		if (object == null)
			return null;

		JSONValue value = null;
		if (property != null) {
			value = object.get(property);
			if (value == null)
				return null;
		} else {
			value = object;
		}

		JSONString string = value.isString();
		if (string == null)
			return null;

		return string.stringValue();
	}

	private void readFile() {

		if (file.getValue() != null && file.getValue().trim().length() > 0) {
			uploadBox = MessageBox.wait(i18n.importProgressTitle(), i18n.importReadingFileMessage(), i18n.importParsingMessage());
			fileUploadPanel.submit();
		}
	}


	private void showSetupPanel(String alertText, boolean overrideText) {
		String defaultMessageText =  i18n.importDefaultShowPanelMessage();
		String messageText;
		StringBuilder sb; 
		if (overrideText)
		{
			if (alertText == null || "".equals(alertText))
			{
				// If they give us nothing and want to override, then we'll still put the default
				sb = new StringBuilder(defaultMessageText); 
			}
			else
			{
				sb = new StringBuilder(); 
			}
		}
		else
		{
			sb = new StringBuilder(defaultMessageText); 
			if (alertText != null && !"".equals(alertText))
			{
				sb.append("<br>");
			}
		}

		sb.append(alertText);
		messageText = sb.toString(); 
		sb = null; 

		MessageBox.alert(i18n.importSetupRequiredTitle(), messageText, 
				new Listener<MessageBoxEvent>() {

			public void handleEvent(MessageBoxEvent be) {
				tabPanel.setSelection(columnsTab);
			}

		});
	}

}
