package org.sakaiproject.gradebook.gwt.client.gxt.view.panel;

import java.util.List;

import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemCreate;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.view.AppView;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.GradeType;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PropertyChangeEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class ItemFormPanel extends ContentPanel {

	private enum SelectionType { CLOSE, CREATE, CANCEL, DELETE, SAVE };
	
	private static final String selectionTypeField = "selectionType";
	
	private FormPanel formPanel;
	private FormBinding formBindings;
	
	private LabelField directionsField;
	private TextField<String> nameField;
	private ComboBox<ModelData> categoryTypePicker;
	private ComboBox<ModelData> gradeTypePicker;
	private ComboBox<ItemModel> categoryPicker;
	private CheckBox includedField;
	private CheckBox extraCreditField;
	private CheckBox equallyWeightChildrenField;
	private CheckBox releasedField;
	private NumberField percentCourseGradeField;
	private NumberField percentCategoryField;
	private NumberField pointsField;
	private NumberField dropLowestField;
	private DateField dueDateField;
	private TextField<String> sourceField;
	
	private ListStore<ItemModel> categoryStore;
	private TreeStore<ItemModel> treeStore;
	
	private Listener<FieldEvent> extraCreditChangeListener;
	private SelectionListener<ButtonEvent> selectionListener;
	private SelectionChangedListener<ItemModel> categorySelectionChangedListener;
	
	private I18nConstants i18n;
	
	private RowLayout layout;
	private RowData topRowData, bottomRowData;
	private Button okButton, cancelButton;
	private boolean isFull;
	
	private GradebookModel selectedGradebook;
	private ItemModel selectedItemModel;
	private Type createItemType;
	
	private boolean isDelete;
	
	@SuppressWarnings("unchecked")
	public ItemFormPanel(I18nConstants i18n) {
		this.i18n = i18n;
		this.isFull = false;
		setHeaderVisible(false);
		setFrame(true);
		
		layout = new RowLayout();
		setLayout(layout);
		layout.setOrientation(Orientation.VERTICAL);
		
		initListeners();
		
		formPanel = new FormPanel();
		formPanel.setHeaderVisible(false);
		formPanel.setLabelWidth(120);

		directionsField = new LabelField();
		directionsField.setName("directions");
		//formPanel.add(directionsField);
		
		nameField = new TextField<String>();
		nameField.setAllowBlank(false);
		nameField.setName(ItemModel.Key.NAME.name());
		nameField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.NAME));
		
		formPanel.add(nameField);
	
		categoryPicker = new ComboBox<ItemModel>();
		categoryPicker.setDisplayField(ItemModel.Key.NAME.name());
		categoryPicker.setName(ItemModel.Key.CATEGORY_ID.name());
		categoryPicker.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.CATEGORY_NAME));
		categoryPicker.addSelectionChangedListener(categorySelectionChangedListener);
		categoryPicker.setVisible(false);
		formPanel.add(categoryPicker);
		
		
		categoryTypePicker = new ComboBox<ModelData>();
		categoryTypePicker.setDisplayField("name");
		categoryTypePicker.setName(ItemModel.Key.CATEGORYTYPE.name());
		categoryTypePicker.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.CATEGORYTYPE));
		categoryTypePicker.setVisible(false);
		formPanel.add(categoryTypePicker);
		
		gradeTypePicker = new ComboBox<ModelData>();
		gradeTypePicker.setDisplayField("name");
		gradeTypePicker.setName(ItemModel.Key.GRADETYPE.name());
		gradeTypePicker.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.GRADETYPE));
		gradeTypePicker.setVisible(false);
		formPanel.add(gradeTypePicker);

		percentCourseGradeField = new NumberField();
		percentCourseGradeField.setName(ItemModel.Key.PERCENT_COURSE_GRADE.name());
		percentCourseGradeField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.PERCENT_COURSE_GRADE));
		percentCourseGradeField.setFormat(DataTypeConversionUtil.getLongNumberFormat());
		percentCourseGradeField.setAllowDecimals(true);
		percentCourseGradeField.setMinValue(Double.valueOf(0.000000d));
		percentCourseGradeField.setMaxValue(Double.valueOf(100.000000d));
		percentCourseGradeField.setVisible(false);
		formPanel.add(percentCourseGradeField);
		
		percentCategoryField = new NumberField();
		percentCategoryField.setName(ItemModel.Key.PERCENT_CATEGORY.name());
		percentCategoryField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.PERCENT_CATEGORY));
		percentCategoryField.setFormat(DataTypeConversionUtil.getLongNumberFormat());
		percentCategoryField.setAllowDecimals(true);
		percentCategoryField.setMinValue(Double.valueOf(0.000000d));
		percentCategoryField.setMaxValue(Double.valueOf(100.000000d));
		percentCategoryField.setVisible(false);
		formPanel.add(percentCategoryField);
			
		pointsField = new NumberField();
		pointsField.setName(ItemModel.Key.POINTS.name());
		pointsField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.POINTS));
		pointsField.setFormat(DataTypeConversionUtil.getDefaultNumberFormat());
		pointsField.setAllowDecimals(true);
		pointsField.setMinValue(Double.valueOf(0.000000d));
		pointsField.setVisible(false);
		formPanel.add(pointsField);
		
		dropLowestField = new NumberField();
		dropLowestField.setName(ItemModel.Key.DROP_LOWEST.name());
		dropLowestField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.DROP_LOWEST));
		dropLowestField.setAllowDecimals(false);
		dropLowestField.setPropertyEditorType(Integer.class);
		dropLowestField.setVisible(false);
		formPanel.add(dropLowestField);
		
		dueDateField = new DateField();
		dueDateField.setName(ItemModel.Key.DUE_DATE.name());
		dueDateField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.DUE_DATE));
		dueDateField.setVisible(false);
		formPanel.add(dueDateField);
		
		sourceField = new TextField<String>();
		sourceField.setName(ItemModel.Key.SOURCE.name());
		sourceField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.SOURCE));
		sourceField.setEnabled(false);
		sourceField.setEmptyText("Gradebook");
		sourceField.setVisible(false);
		formPanel.add(sourceField);
		
		LayoutContainer checkBoxContainer = new LayoutContainer();
		ColumnLayout columnLayout = new ColumnLayout();
		checkBoxContainer.setLayout(columnLayout);
		
		LayoutContainer left = new LayoutContainer();
		LayoutContainer right = new LayoutContainer();
		
		setLayoutData(left, new MarginData(0));
		setLayoutData(right, new MarginData(0));
		
		FormLayout leftFormLayout = new FormLayout();
		leftFormLayout.setPadding(0);
		leftFormLayout.setLabelWidth(120);
		
		left.setLayout(leftFormLayout);
		
		FormLayout rightFormLayout = new FormLayout();
		rightFormLayout.setPadding(0);
		rightFormLayout.setLabelWidth(120);
		
		right.setLayout(rightFormLayout);
		
		includedField = new CheckBox();
		includedField.setName(ItemModel.Key.INCLUDED.name());
		includedField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.INCLUDED));
		includedField.setVisible(false);
		left.add(includedField);
		
		extraCreditField = new CheckBox();
		extraCreditField.setName(ItemModel.Key.EXTRA_CREDIT.name());
		extraCreditField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.EXTRA_CREDIT));
		extraCreditField.setVisible(false);
		extraCreditField.addListener(Events.Change, extraCreditChangeListener);
		left.add(extraCreditField);
		
		equallyWeightChildrenField = new CheckBox();
		equallyWeightChildrenField.setName(ItemModel.Key.EQUAL_WEIGHT.name());
		equallyWeightChildrenField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.EQUAL_WEIGHT));
		equallyWeightChildrenField.setVisible(false);
		right.add(equallyWeightChildrenField);
		
		releasedField = new CheckBox();
		releasedField.setName(ItemModel.Key.RELEASED.name());
		releasedField.setFieldLabel(ItemModel.getPropertyName(ItemModel.Key.RELEASED));
		releasedField.setVisible(false);
		right.add(releasedField);
		
		checkBoxContainer.add(left, new ColumnData(200));
		checkBoxContainer.add(right, new ColumnData(200));
		
		formPanel.add(checkBoxContainer);
		
		okButton = new Button("Blank", selectionListener);
		addButton(okButton);
		
		cancelButton = new Button(i18n.cancelButton(), selectionListener);
		cancelButton.setData(selectionTypeField, SelectionType.CANCEL);
		
		addButton(cancelButton);
		
		topRowData = new RowData(1, 70, new Margins(10));
		bottomRowData = new RowData(1, 1, new Margins(0, 0, 5, 0));
		add(directionsField, topRowData);
		add(formPanel, bottomRowData);
	}
	
	public void onActionCompleted() {
		okButton.setEnabled(true);
	}
	
	public void onConfirmDeleteItem(ItemModel itemModel) {
		this.createItemType = null;
		this.selectedItemModel = itemModel;
		
		if (formBindings == null)
			initFormBindings();
		
		okButton.setText(i18n.deleteButton());
		okButton.setData(selectionTypeField, SelectionType.DELETE);
		
		if (itemModel != null) {
			Type itemType = itemModel.getItemType();
			initState(itemType, itemModel, true);
			directionsField.setText(i18n.directionsConfirmDeleteItem());
			directionsField.setVisible(true);
			
			formBindings.bind(itemModel);
			
			Dispatcher.forwardEvent(GradebookEvents.ExpandEastPanel.getEventType(), AppView.EastCard.DELETE_ITEM);
		}
	}
	
	public void onEditItem(ItemModel itemModel, boolean expand) {
		if (!expand && !isVisible())
			return;
		
		if (expand)
			Dispatcher.forwardEvent(GradebookEvents.ExpandEastPanel.getEventType(), AppView.EastCard.EDIT_ITEM);
		
		if (selectedItemModel != null && itemModel != null && itemModel.equals(selectedItemModel))
			return;
		
		this.createItemType = null;
		this.selectedItemModel = itemModel;
		this.directionsField.setText("");
		this.directionsField.setVisible(false);
		
		if (formBindings == null)
			initFormBindings();
		
		okButton.setText(i18n.saveButton());
		okButton.setData(selectionTypeField, SelectionType.SAVE);
		
		if (itemModel != null) {
			Type itemType = itemModel.getItemType();
			initState(itemType, itemModel, false);
			formBindings.bind(itemModel);
		} else {
			formBindings.unbind();
		}
	}

	public void onItemCreated(ItemModel itemModel) {
		if (itemModel.getItemType() == Type.CATEGORY) {
			categoryStore.add(itemModel);
		}
	}
	
	public void onItemUpdated(ItemModel itemModel) {
		categoryPicker.setEnabled(true);
		categoryTypePicker.setEnabled(true);
		gradeTypePicker.setEnabled(true);	
	}
	
	public void onLoadItemTreeModel(ItemModel rootItemModel) {
		
		if (categoryStore != null) {
			categoryStore.removeAllListeners();
			categoryStore.removeAll();
		}
		
		// FIXME: Do we need to eliminate old category stores?  
		categoryStore = new ListStore<ItemModel>();
		for (ItemModel gradebook : rootItemModel.getChildren()) {
			for (ItemModel category : gradebook.getChildren()) {
			
				// Ensure that we're dealing with a category
				if (category.getItemType() == Type.CATEGORY) {
					categoryStore.add(category);
				}
			
			}
		}
		categoryPicker.setStore(categoryStore);
		
		
		ListStore<ModelData> categoryTypeStore = new ListStore<ModelData>();
		
		categoryTypeStore.add(getCategoryTypeModel(GradebookModel.CategoryType.NO_CATEGORIES));
		categoryTypeStore.add(getCategoryTypeModel(GradebookModel.CategoryType.SIMPLE_CATEGORIES));
		categoryTypeStore.add(getCategoryTypeModel(GradebookModel.CategoryType.WEIGHTED_CATEGORIES));
		
		categoryTypePicker.setStore(categoryTypeStore);
		
		
		ListStore<ModelData> gradeTypeStore = new ListStore<ModelData>();
		
		gradeTypeStore.add(getGradeTypeModel(GradebookModel.GradeType.POINTS));
		gradeTypeStore.add(getGradeTypeModel(GradebookModel.GradeType.PERCENTAGES));
		
		gradeTypePicker.setStore(gradeTypeStore);

	}
	
	public void onNewCategory(ItemModel itemModel) {
		this.directionsField.setText("");
		this.directionsField.setVisible(false);
		this.createItemType = Type.CATEGORY;
		this.selectedItemModel = null;
		
		if (formBindings != null) 
			formBindings.unbind();

		includedField.setValue(Boolean.TRUE);
		
		okButton.setText(i18n.createButton());
		okButton.setData(selectionTypeField, SelectionType.CREATE);
		
		if (itemModel != null) 	
			initState(Type.CATEGORY, itemModel, false);
		
	}
	
	public void onNewItem(ItemModel itemModel) {
		this.directionsField.setText("");
		this.directionsField.setVisible(false);
		this.createItemType = Type.ITEM;
		this.selectedItemModel = null;
		
		if (formBindings != null) 
			formBindings.unbind();

		okButton.setText(i18n.createButton());
		okButton.setData(selectionTypeField, SelectionType.CREATE);
		
		includedField.setValue(Boolean.TRUE);
		
		if (itemModel != null) {
			if (itemModel.getCategoryId() != null) {
				List<ItemModel> models = treeStore.findModels(ItemModel.Key.ID.name(), String.valueOf(itemModel.getCategoryId()));
				for (ItemModel category : models) {
					if (category.getItemType() == Type.CATEGORY)
						categoryPicker.setValue(category);
				}
			}
		}
		
		initState(Type.ITEM, itemModel, false);
		
		
	}
	
	public void onTreeStoreInitialized(TreeStore<ItemModel> treeStore) {
		this.treeStore = treeStore;
		
		if (formBindings != null) {
			formBindings.unbind();
			formBindings.clear();
			formBindings = null;
		}
		
		if (! rendered) {
			return;
		}
		
		initFormBindings();
	}
	
	public void onSwitchGradebook(GradebookModel selectedGradebook) {
		this.selectedGradebook = selectedGradebook;
	}

	private CategoryType getCategoryType(ModelData categoryTypeModel) {
		return categoryTypeModel.get("value");
	}
	
	private ModelData getCategoryTypeModel(CategoryType categoryType) {
		ModelData model = new BaseModelData();
		
		// Initialize type picker
		switch (categoryType) {
	    case NO_CATEGORIES:
	    	model.set("name", i18n.orgTypeNoCategories());
	    	model.set("value", GradebookModel.CategoryType.NO_CATEGORIES);
	    	break;
	    case SIMPLE_CATEGORIES:
	    	model.set("name", i18n.orgTypeCategories());
	    	model.set("value", GradebookModel.CategoryType.SIMPLE_CATEGORIES);
	    	break;	
	    case WEIGHTED_CATEGORIES:
	    	model.set("name", i18n.orgTypeWeightedCategories());
	    	model.set("value", GradebookModel.CategoryType.WEIGHTED_CATEGORIES);
	    	break;	
	    }
		
		return model;
	}
	
	private GradeType getGradeType(ModelData gradeTypeModel) {
		return gradeTypeModel.get("value");
	}
	
	private ModelData getGradeTypeModel(GradeType gradeType) {
		ModelData model = new BaseModelData();
		
		switch (gradeType) {
		case POINTS:
			model.set("name", i18n.gradeTypePoints());
			model.set("value", GradebookModel.GradeType.POINTS);
			break;
		case PERCENTAGES:
			model.set("name", i18n.gradeTypePercentages());
			model.set("value", GradebookModel.GradeType.PERCENTAGES);
			break;
		}
		
		return model;
	}	
	
	private void initState(Type itemType, ItemModel itemModel, boolean isDelete) {
		this.isDelete = isDelete;
		
		okButton.setEnabled(true);
		
		CategoryType categoryType = selectedGradebook.getGradebookItemModel().getCategoryType();
		
		boolean hasCategories = categoryType != CategoryType.NO_CATEGORIES;
		boolean hasWeights = categoryType == CategoryType.WEIGHTED_CATEGORIES;
		boolean isNotGradebook = itemType != Type.GRADEBOOK;
		boolean isCategory = itemType == Type.CATEGORY;
		boolean isItem = itemType == Type.ITEM;
		boolean isExternal = false;
		
		boolean isPercentCategoryVisible = false;

		if (itemModel != null) {
			String source = itemModel.get(ItemModel.Key.SOURCE.name());
			isExternal = source != null && source.trim().length() > 0;
			ItemModel category = null;
			switch (itemModel.getItemType()) {
			case GRADEBOOK:
				isPercentCategoryVisible = false;
				break;
			case CATEGORY:
				category = itemModel;
				if (category != null && category.getItemType() == Type.CATEGORY)
					isPercentCategoryVisible = hasCategories && hasWeights && !DataTypeConversionUtil.checkBoolean(category.getEqualWeightAssignments());
				break;
			case ITEM:
				category = itemModel.getParent();
				if (category != null && category.getItemType() == Type.CATEGORY)
					isPercentCategoryVisible = hasCategories && hasWeights && !DataTypeConversionUtil.checkBoolean(category.getEqualWeightAssignments());
				break;
			default:
				isPercentCategoryVisible = hasCategories && hasWeights && isItem;
			}
		} else {
			isPercentCategoryVisible = hasCategories && hasWeights && isItem;
		}
		
		initField(nameField, !isDelete, true);
		initField(pointsField, !isDelete && !isExternal, isItem);
		initField(percentCategoryField, !isDelete, isPercentCategoryVisible);
		initField(percentCourseGradeField, !isDelete, isCategory);
		initField(equallyWeightChildrenField, !isDelete, isCategory && hasWeights);
		initField(extraCreditField, !isDelete, isNotGradebook);
		initField(dropLowestField, !isDelete, isCategory);
		initField(dueDateField, !isDelete && !isExternal, isItem);
		initField(includedField, !isDelete, isNotGradebook);
		initField(releasedField, !isDelete, isItem);
		initField(categoryPicker, !isDelete, hasCategories && isItem);
		initField(categoryTypePicker, true, !isNotGradebook);
		initField(gradeTypePicker, true, !isNotGradebook);
		initField(sourceField, false, isItem);

	}
	
	private void initField(Field field, boolean isEnabled, boolean isVisible) {
		if (field.isEnabled() != isEnabled)
			field.setEnabled(isEnabled);
		
		//if (!field.isRendered() || field.isVisible() != isVisible)
			field.setVisible(isVisible);
		
		field.clearInvalid();
		field.clearState();
		
		if (formBindings != null) {
			FieldBinding fieldBinding = formBindings.getBinding(field);
			if (fieldBinding != null && fieldBinding.getModel() != null && fieldBinding.getProperty() != null)
				fieldBinding.updateField();
		}
	}
	
	
	private void initFormBindings() {
		formBindings = new FormBinding(formPanel, true) {
			public void autoBind() {
				for (Field f : panel.getFields()) {
					if (!bindings.containsKey(f)) {
						String name = f.getName();
						if (name != null && name.length() > 0) {
							FieldBinding b = new FieldBinding(f, f.getName()) {
								
								@Override
								public void updateField() {
									/*if (field != null && field instanceof ComboBox) {
										Object val = onConvertModelValue(model.get(property));
									    ((ComboBox<ItemModel>)field).setValue((ItemModel)val);
									} else*/
										super.updateField();
								}
								
								@Override
								protected void onFieldChange(FieldEvent e) {									
									ItemModel itemModel = (ItemModel)this.model;
									e.field.setEnabled(false);
									
									String property = e.field.getName();
									
									selectedItemModel.set(property, e.value);

									if (property.equals(ItemModel.Key.CATEGORY_ID.name())) {
										
										ItemModel oldModel = (ItemModel)e.oldValue;
										ItemModel newModel = (ItemModel)e.value;
										
										selectedItemModel.setCategoryId(newModel.getCategoryId());
										
										//Dispatcher.forwardEvent(GradebookEvents.UpdateItem, new ItemUpdate(store, itemModel, e.field.getName(), oldModel.getCategoryId(), newModel.getCategoryId()));
										return;
									} else if (property.equals(ItemModel.Key.CATEGORYTYPE.name())) {
										
										CategoryType oldCategoryType = getCategoryType((ModelData)e.oldValue);
										CategoryType newCategoryType = getCategoryType((ModelData)e.value);
										
										selectedItemModel.setCategoryType(newCategoryType);
										
										//Dispatcher.forwardEvent(GradebookEvents.UpdateItem, new ItemUpdate(store, itemModel, e.field.getName(), oldCategoryType, newCategoryType));
										return;
									} else if (property.equals(ItemModel.Key.GRADETYPE.name())) {
										
										GradeType oldGradeType = getGradeType((ModelData)e.oldValue);
										GradeType newGradeType = getGradeType((ModelData)e.value);
										
										selectedItemModel.setGradeType(newGradeType);
										
										//Dispatcher.forwardEvent(GradebookEvents.UpdateItem, new ItemUpdate(store, itemModel, e.field.getName(), oldGradeType, newGradeType));
										return;
									}
									
									//Dispatcher.forwardEvent(GradebookEvents.UpdateItem, new ItemUpdate(store, itemModel, e.field.getName(), e.oldValue, e.value));
									
								}
								
								@Override
								protected void onModelChange(PropertyChangeEvent event) {
									super.onModelChange(event);
									
									if (field != null)
										field.setEnabled(true);
								}
							};
							/*if (f instanceof ListField) {
								b.setConvertor(new Converter() {
									public Object convertModelValue(Object value) {
										if (value == null)
											return null;
										
										CategoryType categoryType = (CategoryType)value;
									    return getCategoryTypeModel(categoryType);
									}
								});
							}*/
							
							if (name.equals(ItemModel.Key.CATEGORY_ID.name())) {
								b.setConvertor(new Converter() {
									public Object convertFieldValue(Object value) {
										
										if (value instanceof ItemModel)
											return ((ItemModel)value).getCategoryId();
										
										
										return value;
									}
									 
									public Object convertModelValue(Object value) {
										if (value == null)
											return null;
										
										if (value instanceof Long) {
											Long categoryId = (Long)value;
										
											return store.findModel(ItemModel.Key.ID.name(), String.valueOf(categoryId));
										}
										
										return null;
									}
								});
							} else if (name.equals(ItemModel.Key.CATEGORYTYPE.name()) ||
									name.equals(ItemModel.Key.GRADETYPE.name())) {
								b.setConvertor(new Converter() {
									public Object convertFieldValue(Object value) {
										return value;
									}
									 
									public Object convertModelValue(Object value) {
										if (value == null)
											return null;
										
										if (value instanceof CategoryType)
											return getCategoryTypeModel((CategoryType)value);
										if (value instanceof GradeType)
											return getGradeTypeModel((GradeType)value);
										else if (value instanceof ModelData) {
											return value;
										}
										
										return null;
									}
								});
								
							} 
							bindings.put(f, b);
						}
					}
				}
			}
		};
		formBindings.setStore(treeStore);
	}
	
	private void initListeners() {
		
		categorySelectionChangedListener = new SelectionChangedListener<ItemModel>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<ItemModel> se) {
				ItemModel itemModel = se.getSelectedItem();
				CategoryType categoryType = selectedGradebook.getGradebookItemModel().getCategoryType();
				
				boolean hasCategories = categoryType != CategoryType.NO_CATEGORIES;
				boolean hasWeights = categoryType == CategoryType.WEIGHTED_CATEGORIES;
				boolean isPercentCategoryVisible = false;
				
				if (itemModel != null) {
				
					ItemModel category = null;
					switch (itemModel.getItemType()) {
					case GRADEBOOK:
						isPercentCategoryVisible = false;
						break;
					case CATEGORY:
						category = itemModel;
						if (category != null && category.getItemType() == Type.CATEGORY)
							isPercentCategoryVisible = hasCategories && hasWeights 
								&& (!DataTypeConversionUtil.checkBoolean(category.getEqualWeightAssignments()) || 
								DataTypeConversionUtil.checkBoolean(extraCreditField.getValue()));
						break;
					case ITEM:
						category = itemModel.getParent();
						if (category != null && category.getItemType() == Type.CATEGORY)
							isPercentCategoryVisible = hasCategories && hasWeights && 
								(!DataTypeConversionUtil.checkBoolean(category.getEqualWeightAssignments()) || 
										DataTypeConversionUtil.checkBoolean(extraCreditField.getValue()));
						break;
					}
				} 
				
				initField(percentCategoryField, !isDelete, isPercentCategoryVisible || DataTypeConversionUtil.checkBoolean(extraCreditField.getValue()));
			}
			
		};
		
		extraCreditChangeListener = new Listener<FieldEvent>() {

			public void handleEvent(FieldEvent fe) {
				Boolean isChecked = ((CheckBox)fe.field).getValue();
				CategoryType categoryType = selectedGradebook.getGradebookItemModel().getCategoryType();
				ItemModel category = categoryPicker.getValue();
				Boolean isEqualWeight = category == null ? Boolean.FALSE : category.getEqualWeightAssignments();
				boolean hasWeights = categoryType == CategoryType.WEIGHTED_CATEGORIES;
				
				if (selectedItemModel != null) {
					switch (selectedItemModel.getItemType()) {
					case ITEM:
						initField(percentCategoryField, !isDelete, hasWeights 
								&& (!DataTypeConversionUtil.checkBoolean(isEqualWeight) 
										|| (isChecked != null && isChecked.booleanValue())));
						break;
					}
				} else if (createItemType == Type.ITEM) {
					initField(percentCategoryField, !isDelete, hasWeights 
							&& (!DataTypeConversionUtil.checkBoolean(isEqualWeight) || (isChecked != null && isChecked.booleanValue())));
				}
				
			}
			
		};
		
		selectionListener = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent be) {
				Button button = be.button;
				if (button != null) {
					SelectionType selectionType = button.getData(selectionTypeField);
					if (selectionType != null) {
						switch (selectionType) {
						case CLOSE:
							Dispatcher.forwardEvent(GradebookEvents.HideEastPanel.getEventType(), Boolean.FALSE);
							break;
						case CREATE:
							if (nameField.getValue() == null) {
								MessageBox.alert(i18n.itemNameRequiredTitle(), i18n.itemNameRequiredText(), null);
								return;
							}
							
							ItemModel item = new ItemModel();
							
							ItemModel category = categoryPicker.getValue();
							
							if (category != null) 
								item.setCategoryId(category.getCategoryId());
							
							item.setName(nameField.getValue());
							item.setExtraCredit(extraCreditField.getValue());
							item.setEqualWeightAssignments(equallyWeightChildrenField.getValue());
							item.setIncluded(includedField.getValue());
							item.setReleased(releasedField.getValue());
							item.setPercentCourseGrade((Double)percentCourseGradeField.getValue());
							item.setPercentCategory((Double)percentCategoryField.getValue());
							item.setPoints((Double)pointsField.getValue());
							item.setDueDate(dueDateField.getValue());
							item.setItemType(createItemType);
							
							okButton.setEnabled(false);
							
							Dispatcher.forwardEvent(GradebookEvents.CreateItem.getEventType(), new ItemCreate(treeStore, item));
							break;
						case DELETE:
							Dispatcher.forwardEvent(GradebookEvents.HideEastPanel.getEventType(), Boolean.FALSE);
							Dispatcher.forwardEvent(GradebookEvents.DeleteItem.getEventType(), new ItemUpdate(treeStore, selectedItemModel, ItemModel.Key.REMOVED.name(), Boolean.FALSE, Boolean.TRUE));
							break;
						case CANCEL:
							Dispatcher.forwardEvent(GradebookEvents.HideEastPanel.getEventType(), Boolean.FALSE);
							break;
						case SAVE:
							if (nameField.validate() 
									&& (!percentCategoryField.isVisible() || percentCategoryField.validate()) 
									&& (!percentCourseGradeField.isVisible() || percentCourseGradeField.validate())
									&& (!pointsField.isVisible() || pointsField.validate())) {
								okButton.setEnabled(false);
								Dispatcher.forwardEvent(GradebookEvents.UpdateItem.getEventType(), new ItemUpdate(treeStore, selectedItemModel));
							}
							break;
						}
					}
				}

			}
			
		};
		
	}


	public TreeStore<ItemModel> getTreeStore() {
		return treeStore;
	}

	
}
