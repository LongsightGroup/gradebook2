package org.sakaiproject.gradebook.gwt.client.gxt.view.panel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaButton;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaTabPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.event.BrowseLearner;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradeRecordUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.BrowseLearner.BrowseType;
import org.sakaiproject.gradebook.gwt.client.gxt.view.components.BlurringNumberField;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
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
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class LearnerSummaryPanel extends ContentPanel {

	private static final String ITEM_IDENTIFIER_FLAG = "itemIdentifier";
	private static final String BUTTON_SELECTOR_FLAG = "buttonSelector";
	private enum ButtonSelector { CLOSE, COMMENT, NEXT, PREVIOUS, VIEW_AS_LEARNER };
	
	private I18nConstants i18n;
	
	private ContentPanel learnerInfoPanel;
	private FormBinding formBinding;
	private FormPanel formPanel;
	private LayoutContainer commentFormPanel;
	private LayoutContainer scoreFormPanel;
	private KeyListener keyListener;
	private SelectionListener<ComponentEvent> selectionListener;
	private StudentModel learner;
	
	private FormLayout scoreFormLayout;
	private FormLayout commentFormLayout;
	
	private FlexTableContainer learnerInfoTable;
	
	
	public LearnerSummaryPanel(final ListStore<StudentModel> store) {
		setHeaderVisible(false);
		setLayout(new RowLayout());
		
		// TODO: Should this be passed in as an argument to the constructor?
		i18n = Registry.get(AppConstants.I18N);
		
		initListeners();
		
		add(newLearnerInfoPanel(), new RowData(1, -1));
		
		formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setLayout(new FitLayout());
		
		TabPanel tabPanel = new AriaTabPanel();
		tabPanel.setPlain(true);
		tabPanel.setBorderStyle(true);
		
		TabItem tab = new TabItem(i18n.learnerTabGradeHeader());
		tab.add(newGradeFormPanel());
		tabPanel.add(tab);
		
		tab = new TabItem(i18n.learnerTabCommentHeader());
		tab.setLayout(new FitLayout());
		tab.add(newCommentFormPanel());
		tabPanel.add(tab);
		
		formPanel.add(tabPanel);
		
		add(formPanel, new RowData(1, 1));
		
		/*TextField<String> first = new TextField<String>();
		first.setFieldLabel("First Name");
		left.add(first, formData);*/ 
		
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

	public void onChangeModel(ListStore<StudentModel> store, TreeStore<ItemModel> treeStore, StudentModel learner) {
		this.learner = learner;
		updateLearnerInfo(learner);

		for (Component item : scoreFormPanel.getItems()) {
			if (item instanceof Field) {
				Field<?> field = (Field<?>)item;
				field.setEnabled(true);
			}
		}
		
		for (Component item : commentFormPanel.getItems()) {
			if (item instanceof Field)
				((Field<?>)item).setEnabled(true);
		}
		
		if (learner != null) {
			
			//displayName.setHtml(learner.getDisplayName());
			//section.setHtml(learner.getStudentSections());
			
			verifyFormPanelComponents(treeStore);
			
			formBinding.setStore(store);
			formBinding.bind(learner);
			
			
			/*this.selectedGradebook = selectedGradebook;
			this.learnerGradeRecordCollection = learnerGradeRecordCollection;
			
			if (gradeItemsPanel == null)
				gradeItemsPanel = newGradeItemsPanel();
			
			if (logColumn != null)
				logColumn.setStudent(learnerGradeRecordCollection);
			updateCourseGrade(learnerGradeRecordCollection.getStudentGrade());
			gradeItemsPanel.getLoader().load(0, pageSize);
			setStudentInfoTable();*/
		} else if (formBinding != null) {
			formBinding.unbind();
		}
	}
	
	@Override
	protected void onResize(final int width, final int height) {
		/*System.out.println("Width: " + width);
		
		learnerInfoPanel.setWidth(width);
		*/
		commentFormLayout.setDefaultWidth(width - 40);
		//commentFormPanel.setWidth(width - 10);
		
		super.onResize(width, height);
	}
	
	private void addField(Set<String> itemIdSet, ItemModel item) {
		String itemId = new StringBuilder().append(AppConstants.LEARNER_SUMMARY_FIELD_PREFIX).append(item.getIdentifier()).toString();
		String source = item.getSource();
		boolean isStatic = source != null && source.equals(AppConstants.STATIC);
		
		if (!itemIdSet.contains(itemId) && !isStatic) {
			
			String dataType = item.getDataType();
			
			if (dataType != null && dataType.equals(AppConstants.NUMERIC_DATA_TYPE)) {
				BlurringNumberField field = new BlurringNumberField();
				
				field.setItemId(itemId);
				field.addInputStyleName("gbNumericFieldInput");
				field.addKeyListener(keyListener);
				field.setFieldLabel(item.getName());
				field.setFormat(DataTypeConversionUtil.getDefaultNumberFormat());
				field.setName(item.getIdentifier());
				
				scoreFormPanel.add(field);
				
				String commentId = new StringBuilder(item.getIdentifier()).append(StudentModel.COMMENT_TEXT_FLAG).toString();
				TextArea textArea = new TextArea();
				textArea.addInputStyleName("gbTextAreaInput");
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
				switch (event.event.getKeyCode()) {
				case KeyboardListener.KEY_ENTER:
					//((BlurringNumberField)event.component).blurIt();
					break;
				}
			}
			
		};
		
		selectionListener = new SelectionListener<ComponentEvent>() {

			@Override
			public void componentSelected(ComponentEvent be) {
				ButtonSelector selector = be.component.getData(BUTTON_SELECTOR_FLAG);
				
				BrowseLearner bse = null;
				
				switch (selector) {
				case CLOSE:
					Dispatcher.forwardEvent(GradebookEvents.HideEastPanel, Boolean.FALSE);
					break;
				case COMMENT:
					String id = be.component.getData(ITEM_IDENTIFIER_FLAG);
					
					Info.display("Id", id);
					break;
				case NEXT:
					bse = new BrowseLearner(learner, BrowseType.NEXT);
					Dispatcher.forwardEvent(GradebookEvents.BrowseLearner, bse);
					break;
				case PREVIOUS:
					bse = new BrowseLearner(learner, BrowseType.PREV);
					Dispatcher.forwardEvent(GradebookEvents.BrowseLearner, bse);
					break;
				case VIEW_AS_LEARNER:
					Dispatcher.forwardEvent(GradebookEvents.SingleView, learner);
					break;
				}
				
			}
			
			
		};	
		
	}
	
	private LayoutContainer newCommentFormPanel() {
		commentFormPanel = new LayoutContainer();
		//commentFormPanel.setHeaderVisible(false);
		commentFormLayout = new FormLayout();
		commentFormLayout.setLabelAlign(LabelAlign.TOP);
		//commentFormLayout.setDefaultWidth(370);
		commentFormPanel.setLayout(commentFormLayout);
		commentFormPanel.setScrollMode(Scroll.AUTOY);
		
		return commentFormPanel;
	}
	
	private LayoutContainer newGradeFormPanel() {
		scoreFormPanel = new LayoutContainer();

		scoreFormLayout = new FormLayout();
		scoreFormLayout.setDefaultWidth(50);
		scoreFormLayout.setLabelSeparator("");

		scoreFormPanel.setLayout(scoreFormLayout);
		scoreFormPanel.setScrollMode(Scroll.AUTOY);
		
		return scoreFormPanel;
	}
	
	private ContentPanel newLearnerInfoPanel() {
		learnerInfoTable = new FlexTableContainer(new FlexTable()); 
		learnerInfoTable.setStyleName("gbStudentInformation");
		learnerInfoPanel = new ContentPanel();
		learnerInfoPanel.setHeaderVisible(false);
		learnerInfoPanel.setHeading("Individual Grade Summary");
		learnerInfoPanel.setLayout(new FitLayout());
		learnerInfoPanel.setScrollMode(Scroll.AUTO);
		learnerInfoPanel.add(learnerInfoTable);
		
		return learnerInfoPanel;
	}
	
	private void updateLearnerInfo(StudentModel learnerGradeRecordCollection) {		
		// To force a refresh, let's first hide the owning panel
		learnerInfoPanel.hide();
	
		// Now, let's update the student information table
		FlexCellFormatter formatter = learnerInfoTable.getFlexCellFormatter();

		learnerInfoTable.setText(1, 0, i18n.columnTitleDisplayName());
        formatter.setStyleName(1, 0, "gbImpact");
        learnerInfoTable.setText(1, 1, learnerGradeRecordCollection.getStudentName());

        learnerInfoTable.setText(2, 0, i18n.columnTitleEmail());
        formatter.setStyleName(2, 0, "gbImpact");
        learnerInfoTable.setText(2, 1, learnerGradeRecordCollection.getStudentEmail());

        learnerInfoTable.setText(3, 0, i18n.columnTitleDisplayId());
        formatter.setStyleName(3, 0, "gbImpact");
        learnerInfoTable.setText(3, 1, learnerGradeRecordCollection.getStudentDisplayId());

        learnerInfoTable.setText(4, 0, i18n.columnTitleSection());
        formatter.setStyleName(4, 0, "gbImpact");
        learnerInfoTable.setText(4, 1, learnerGradeRecordCollection.getStudentSections());
    
        learnerInfoTable.setText(5, 0, "");
        formatter.setColSpan(5, 0, 2);
        
        GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
        
        if (selectedGradebook.isReleaseGrades() != null && selectedGradebook.isReleaseGrades().booleanValue()) {
        	learnerInfoTable.setText(6, 0, "Course Grade");
	        formatter.setStyleName(6, 0, "gbImpact");
	        learnerInfoTable.setText(6, 1, learnerGradeRecordCollection.getStudentGrade());
        }
        learnerInfoPanel.show();
	}
	
	private void verifyFormPanelComponents(TreeStore<ItemModel> treeStore) {
		
		List<ItemModel> rootItems = treeStore.getRootItems();
		
		List<Component> allItems = scoreFormPanel.getItems();
		Set<String> itemIdSet = new HashSet<String>();
		if (allItems != null) {
			for (Component c : allItems) {
				itemIdSet.add(c.getItemId());
			}
		}
		
		if (rootItems != null) {
			for (ItemModel root : rootItems) {
				
				if (root.getChildCount() > 0) {
					for (ItemModel child : root.getChildren()) {
					
						if (child.getChildCount() > 0) {
							
							for (ItemModel subchild : child.getChildren()) {
								addField(itemIdSet, subchild);
							}
							
						} else {
							addField(itemIdSet, child);
						}
						
					}
				} 
				
			}
		}
		
		if (formBinding != null) {
			formBinding.unbind();
			formBinding.clear();
			formBinding = null;
		}
		
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
									e.field.setEnabled(false);
									Dispatcher.forwardEvent(GradebookEvents.UpdateLearnerGradeRecord, new GradeRecordUpdate(store, learner, e.field.getName(), e.oldValue, e.value));
								}
								
								@Override
								protected void onModelChange(PropertyChangeEvent event) {
									super.onModelChange(event);
									
									if (field != null)
										field.setEnabled(true);
								}
							};
							bindings.put(f, b);
						}
					}
				}
			}
		};
	}
	
	
	
	public class FlexTableContainer extends WidgetComponent {
		
		private FlexTable table;
		
		public FlexTableContainer(FlexTable table) {
			super(table);
			this.table = table;
			//table = new FlexTable();
			//wrapWidget(table);
		}
		
		public FlexCellFormatter getFlexCellFormatter() {
			return table.getFlexCellFormatter();
		}
		
		 public void setText(int row, int column, String text) {
			table.setText(row, column, text);
		}
		
	}
	
	
	
	
}
