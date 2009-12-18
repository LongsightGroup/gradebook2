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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.gxt.InlineEditField;
import org.sakaiproject.gradebook.gwt.client.gxt.InlineEditNumberField;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaButton;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaTabPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.event.BrowseLearner;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradeRecordUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.BrowseLearner.BrowseType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.GradeType;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PropertyChangeEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class LearnerSummaryPanel extends GradebookPanel {

	private static final String FIELD_STATE_FIELD = "fieldState";
	private static final String ITEM_IDENTIFIER_FLAG = "itemIdentifier";
	private static final String BUTTON_SELECTOR_FLAG = "buttonSelector";
	private enum ButtonSelector { CLOSE, COMMENT, NEXT, PREVIOUS, VIEW_AS_LEARNER };

	private ContentPanel learnerInfoPanel;
	private FormBinding formBinding;
	private FormPanel formPanel;
	private LayoutContainer commentFormPanel;
	private LayoutContainer excuseFormPanel;
	private LayoutContainer scoreFormPanel;
	private KeyListener keyListener;
	private SelectionListener<ComponentEvent> selectionListener;
	private StudentModel learner;

	private FormLayout commentFormLayout;
	private FormLayout excuseFormLayout;
	private FormLayout scoreFormLayout;

	private FlexTableContainer learnerInfoTable;

	private boolean isPossibleGradeTypeChanged = false;
	
	public LearnerSummaryPanel() {
		setHeaderVisible(false);
		setId("learnerSummaryPanel");
		setLayout(new FlowLayout());
		setScrollMode(Scroll.AUTO);
		setWidth(400);

		initListeners();

		add(newLearnerInfoPanel()); //, new RowData(1, -1));

		FlowLayout formLayout = new FlowLayout();
		
		formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setLayout(formLayout);
		formPanel.setScrollMode(Scroll.AUTO);

		TabPanel tabPanel = new AriaTabPanel();
		tabPanel.setPlain(true);
		tabPanel.setBorderStyle(true);

		TabItem tab = new TabItem(i18n.learnerTabGradeHeader());
		tab.addStyleName(resources.css().gbTabMargins());
		tab.setLayout(new FlowLayout());
		tab.add(newGradeFormPanel());
		tab.setScrollMode(Scroll.AUTOY);
		tabPanel.add(tab);

		tab = new TabItem(i18n.learnerTabCommentHeader());
		tab.addStyleName(resources.css().gbTabMargins());
		tab.setLayout(new FitLayout());
		tab.add(newCommentFormPanel());
		tab.setScrollMode(Scroll.AUTOY);
		tabPanel.add(tab);

		tab = new TabItem(i18n.learnerTabExcuseHeader());
		tab.addStyleName(resources.css().gbTabMargins());
		tab.setLayout(new FitLayout());
		tab.add(newExcuseFormPanel());
		tab.setScrollMode(Scroll.AUTOY);
		tabPanel.add(tab);

		formPanel.add(tabPanel);
		//setLayoutData(formPanel, new MarginData(10));

		add(formPanel); //, new RowData(1, 1));

		Button button = new AriaButton(i18n.prevLearner(), selectionListener);
		button.setData(BUTTON_SELECTOR_FLAG, ButtonSelector.PREVIOUS);
		addButton(button);

		button = new AriaButton(i18n.nextLearner(), selectionListener);
		button.setData(BUTTON_SELECTOR_FLAG, ButtonSelector.NEXT);
		addButton(button);

		button = new AriaButton(i18n.viewAsLearner(), selectionListener);
		button.setData(BUTTON_SELECTOR_FLAG, ButtonSelector.VIEW_AS_LEARNER);
		addButton(button);

		button = new AriaButton(i18n.close(), selectionListener);
		button.setData(BUTTON_SELECTOR_FLAG, ButtonSelector.CLOSE);
		addButton(button);
	}

	public void onChangeModel(ListStore<StudentModel> learnerStore, TreeStore<ItemModel> treeStore, StudentModel learner) {
		this.learner = learner;
		updateLearnerInfo(learner, false);

		if (learner != null) {
			verifyFormPanelComponents(treeStore, learnerStore);

			formBinding.setStore(learnerStore);
			formBinding.bind(learner);
		}

		for (Component item : scoreFormPanel.getItems()) {
			if (item instanceof Field) {
				Field<?> field = (Field<?>)item;
				field.setEnabled(true);
				verifyFieldState(field, learner);
			}
		}

		for (Component item : commentFormPanel.getItems()) {
			if (item instanceof Field)
				((Field<?>)item).setEnabled(true);
		}

		for (Component item : excuseFormPanel.getItems()) {
			if (item instanceof Field)
				((Field<?>)item).setEnabled(true);
		}
	}

	public void onGradeTypeUpdated(GradebookModel selectedGradebook) {
		this.isPossibleGradeTypeChanged = true;
	}
	
	public void onLearnerGradeRecordUpdated(StudentModel learner) {
		if (this.learner != null && learner != null 
				&& this.learner.getIdentifier().equals(learner.getIdentifier())) 
			updateLearnerInfo(learner, true);
	}

	public void onRefreshGradebookSetup(GradebookModel gradebookModel) {
		
	}
	
	@Override
	protected void onResize(final int width, final int height) {
		commentFormLayout.setDefaultWidth(width - 60);

		super.onResize(width, height);
	}

	private void addField(Set<String> itemIdSet, ItemModel item, int row, GradeType gradeType) {
		String itemId = new StringBuilder().append(AppConstants.LEARNER_SUMMARY_FIELD_PREFIX).append(item.getIdentifier()).toString();
		String source = item.getSource();
		boolean isStatic = source != null && source.equals(AppConstants.STATIC);

		if (!itemIdSet.contains(itemId) && !isStatic) {

			String dataType = item.getDataType();

			if (dataType != null) {
				StringBuilder emptyText = new StringBuilder();
				boolean isEmptyTextFilled = false;
				switch (gradeType) {
				case PERCENTAGES:
					emptyText.append("Enter a value between 0 and 100");
					isEmptyTextFilled = true;
				case POINTS:
					NumberField field = new InlineEditNumberField();
	
					if (!isEmptyTextFilled)
						emptyText.append("Enter a value between 0 and ").append(DataTypeConversionUtil.formatDoubleAsPointsString(item.getPoints()));
	
					field.setItemId(itemId);
					field.addInputStyleName(resources.css().gbNumericFieldInput());
					field.addKeyListener(keyListener);
					field.setFieldLabel(item.getName());
					field.setFormat(DataTypeConversionUtil.getDefaultNumberFormat());
					field.setName(item.getIdentifier());
					field.setToolTip(emptyText.toString());
					field.setWidth(50);
					field.setLabelStyle("overflow: hidden");
	
					verifyFieldState(field, item);
	
					scoreFormPanel.add(field);
					break;
				case LETTERS:
					TextField<String> textField = new InlineEditField<String>();

					emptyText.append("Enter a letter grade");
					
					textField.setItemId(itemId);
					textField.addInputStyleName(resources.css().gbTextFieldInput());
					textField.addKeyListener(keyListener);
					textField.setFieldLabel(item.getName());
					textField.setName(item.getIdentifier());
					textField.setToolTip(emptyText.toString());
					textField.setWidth(50);
					textField.setLabelStyle("overflow: hidden");
	
					verifyFieldState(textField, item);
	
					scoreFormPanel.add(textField);
				}

				String checkBoxName = new StringBuilder().append(item.getIdentifier()).append(StudentModel.EXCUSE_FLAG).toString();
				CheckBox checkbox = new CheckBox();
				checkbox.setFieldLabel(item.getName());
				checkbox.setName(checkBoxName);
				checkbox.setLabelStyle("overflow: hidden;");
				excuseFormPanel.add(checkbox);

				String commentId = new StringBuilder(item.getIdentifier()).append(StudentModel.COMMENT_TEXT_FLAG).toString();
				TextArea textArea = new TextArea();
				textArea.addInputStyleName(resources.css().gbTextAreaInput());
				textArea.setFieldLabel(item.getName());
				textArea.setItemId(itemId);
				textArea.setName(commentId);

				commentFormPanel.add(textArea);

			}
		}

	}

	private void initListeners() {

		keyListener = new KeyListener() {

			@Override
			public void componentKeyPress(ComponentEvent event) {
				switch (event.getEvent().getKeyCode()) {
					case KeyCodes.KEY_ENTER:
						break;
				}
			}

		};

		selectionListener = new SelectionListener<ComponentEvent>() {

			@Override
			public void componentSelected(ComponentEvent be) {
				ButtonSelector selector = be.getComponent().getData(BUTTON_SELECTOR_FLAG);

				BrowseLearner bse = null;

				switch (selector) {
					case CLOSE:
						Dispatcher.forwardEvent(GradebookEvents.HideEastPanel.getEventType(), Boolean.FALSE);
						break;
					case COMMENT:
						String id = be.getComponent().getData(ITEM_IDENTIFIER_FLAG);
						break;
					case NEXT:
						bse = new BrowseLearner(learner, BrowseType.NEXT);
						Dispatcher.forwardEvent(GradebookEvents.BrowseLearner.getEventType(), bse);
						break;
					case PREVIOUS:
						bse = new BrowseLearner(learner, BrowseType.PREV);
						Dispatcher.forwardEvent(GradebookEvents.BrowseLearner.getEventType(), bse);
						break;
					case VIEW_AS_LEARNER:
						Dispatcher.forwardEvent(GradebookEvents.SingleView.getEventType(), learner);
						break;
				}

			}


		};	

	}

	private LayoutContainer newCommentFormPanel() {
		commentFormPanel = new LayoutContainer();
		commentFormLayout = new FormLayout();
		commentFormLayout.setLabelAlign(LabelAlign.TOP);
		commentFormPanel.setLayout(commentFormLayout);
		commentFormPanel.setScrollMode(Scroll.AUTOY);

		return commentFormPanel;
	}

	private LayoutContainer newExcuseFormPanel() {
		excuseFormPanel = new LayoutContainer();
		excuseFormLayout = new FormLayout();
		excuseFormLayout.setLabelAlign(LabelAlign.LEFT);
		excuseFormLayout.setLabelSeparator("");
		excuseFormLayout.setLabelWidth(180);
		excuseFormPanel.setLayout(excuseFormLayout);
		excuseFormPanel.setScrollMode(Scroll.AUTOY);

		return excuseFormPanel;
	}

	private LayoutContainer newGradeFormPanel() {
		scoreFormPanel = new LayoutContainer();
		scoreFormLayout = new FormLayout();
		scoreFormLayout.setDefaultWidth(50);
		scoreFormLayout.setLabelSeparator("");
		scoreFormLayout.setLabelWidth(180);
		scoreFormPanel.setLayout(scoreFormLayout);
		scoreFormPanel.setScrollMode(Scroll.AUTOY);

		return scoreFormPanel;
	}

	private ContentPanel newLearnerInfoPanel() {
		learnerInfoTable = new FlexTableContainer(new FlexTable()); 
		learnerInfoTable.setStyleName(resources.css().gbStudentInformation());
		learnerInfoPanel = new ContentPanel();
		learnerInfoPanel.setHeaderVisible(false);
		learnerInfoPanel.setHeading("Individual Grade Summary");
		//learnerInfoPanel.setLayout(new FillLayout());
		learnerInfoPanel.setScrollMode(Scroll.AUTO);
		learnerInfoPanel.add(learnerInfoTable);

		return learnerInfoPanel;
	}

	private static final String rowHeight = "22px";
	
	private void updateLearnerInfo(StudentModel learnerGradeRecordCollection, boolean isByEvent) {		
		// To force a refresh, let's first hide the owning panel
		learnerInfoPanel.hide();

		// Now, let's update the student information table
		FlexCellFormatter formatter = learnerInfoTable.getFlexCellFormatter();

		learnerInfoTable.setText(1, 0, i18n.columnTitleDisplayName());
		formatter.setStyleName(1, 0, resources.css().gbImpact());
		formatter.setHeight(1, 0, rowHeight);
		learnerInfoTable.setText(1, 1, learnerGradeRecordCollection.getStudentName());
		formatter.setHeight(1, 1, rowHeight);
		learnerInfoTable.setAutoHeight(true);
		
		learnerInfoTable.setText(2, 0, i18n.columnTitleEmail());
		formatter.setStyleName(2, 0, resources.css().gbImpact());
		formatter.setHeight(2, 0, rowHeight);
		learnerInfoTable.setText(2, 1, learnerGradeRecordCollection.getStudentEmail());
		formatter.setHeight(2, 1, rowHeight);
		
		learnerInfoTable.setText(3, 0, i18n.columnTitleDisplayId());
		formatter.setStyleName(3, 0, resources.css().gbImpact());
		formatter.setHeight(3, 0, rowHeight);
		learnerInfoTable.setText(3, 1, learnerGradeRecordCollection.getStudentDisplayId());
		formatter.setHeight(3, 1, rowHeight);
		
		learnerInfoTable.setText(4, 0, i18n.columnTitleSection());
		formatter.setStyleName(4, 0, resources.css().gbImpact());
		formatter.setHeight(4, 0, rowHeight);
		learnerInfoTable.setText(4, 1, learnerGradeRecordCollection.getStudentSections());
		formatter.setHeight(4, 1, rowHeight);
		
		//learnerInfoTable.setText(5, 0, "");
		//formatter.setColSpan(5, 0, 2);
		//formatter.setHeight(25, 0, "20px");

		learnerInfoTable.setText(5, 0, "Course Grade");
		formatter.setStyleName(5, 0, resources.css().gbImpact());
		formatter.setHeight(5, 0, rowHeight);
		learnerInfoTable.setText(5, 1, learnerGradeRecordCollection.getStudentGrade());
		formatter.setHeight(5, 1, rowHeight);
		learnerInfoPanel.show();
	}

	private void verifyFormPanelComponents(TreeStore<ItemModel> treeStore, final ListStore<StudentModel> learnerStore) {

		boolean isLayoutNecessary = false;
		if (isPossibleGradeTypeChanged) {
			scoreFormPanel.removeAll();
			excuseFormPanel.removeAll();
			commentFormPanel.removeAll();
			formBinding.unbind();
			formBinding = null;
			this.isPossibleGradeTypeChanged = false;
			isLayoutNecessary = true;
		}
		
		List<ItemModel> rootItems = treeStore.getRootItems();

		List<Component> allItems = scoreFormPanel.getItems();
		Set<String> itemIdSet = new HashSet<String>();
		if (allItems != null) {
			for (Component c : allItems) {
				itemIdSet.add(c.getItemId());
			}
		}

		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
		GradeType gradeType = selectedGradebook.getGradebookItemModel().getGradeType();

		int row = 0;
		if (rootItems != null) {
			for (ItemModel root : rootItems) {

				if (root.getChildCount() > 0) {
					for (ModelData m : root.getChildren()) {
						ItemModel child = (ItemModel)m;
						if (child.getChildCount() > 0) {

							for (ModelData m2 : child.getChildren()) {
								ItemModel subchild = (ItemModel)m2;
								addField(itemIdSet, subchild, row, gradeType);
								row++;
							}

						} else {
							addField(itemIdSet, child, row, gradeType);
							row++;
						}

					}
				} 

			}
		}
		
		if (isLayoutNecessary) {
			scoreFormPanel.layout();
		}

		if (formBinding == null) {
			formBinding = new FormBinding(formPanel, true) {
				public void autoBind() {
					for (Field f : panel.getFields()) {
						if (!bindings.containsKey(f)) {
							String name = f.getName();
							if (name != null && name.length() > 0) {
								FieldBinding b = new FieldBinding(f, f.getName()) {

									@Override
									protected void onFieldChange(FieldEvent e) {									
										StudentModel learner = (StudentModel)this.model;
										e.getField().setEnabled(false);

										Dispatcher.forwardEvent(GradebookEvents.UpdateLearnerGradeRecord.getEventType(), new GradeRecordUpdate(learnerStore, learner, e.getField().getName(), e.getField().getFieldLabel(), e.getOldValue(), e.getValue()));
									}

									@Override
									protected void onModelChange(PropertyChangeEvent event) {
										super.onModelChange(event);

										if (field != null) {
											verifyFieldState(field, event.getSource());

											boolean isEnabled = true;
											if (!field.isEnabled())
												field.setEnabled(isEnabled);
										}
									}
								};
								bindings.put(f.getId(), b);
							}
						}
					}
				}
			};
		}
	}


	private void verifyFieldState(Field field, Model model) {
		String dropFlag = new StringBuilder().append(field.getName()).append(StudentModel.DROP_FLAG).toString();

		Boolean dropFlagValue = model.get(dropFlag);
		boolean isDropped = dropFlagValue != null && dropFlagValue.booleanValue();

		if (isDropped) {
			field.setData(FIELD_STATE_FIELD, Boolean.TRUE);
			field.addInputStyleName(resources.css().gbCellDropped());
		} else {
			dropFlagValue = field.getData(FIELD_STATE_FIELD);
			isDropped = dropFlagValue != null && dropFlagValue.booleanValue();
			if (isDropped)
				field.removeInputStyleName(resources.css().gbCellDropped());
		}
	}


	public class FlexTableContainer extends WidgetComponent {

		private FlexTable table;

		public FlexTableContainer(FlexTable table) {
			super(table);
			this.table = table;
		}

		public FlexCellFormatter getFlexCellFormatter() {
			return table.getFlexCellFormatter();
		}

		public void setText(int row, int column, String text) {
			table.setText(row, column, text);
		}

		public void setWidget(int row, int column, Widget widget) {
			table.setWidget(row, column, widget);
		}

	}

}
