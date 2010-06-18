/**********************************************************************************
*
* $Id:$
*
***********************************************************************************
*
* Copyright (c) 2008, 2009, 2010 The Regents of the University of California
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
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.gxt.ItemModelProcessor;
import org.sakaiproject.gradebook.gwt.client.gxt.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.gxt.model.ItemUtil;
import org.sakaiproject.gradebook.gwt.client.model.Item;
import org.sakaiproject.gradebook.gwt.client.model.key.ItemKey;
import org.sakaiproject.gradebook.gwt.client.model.type.CategoryType;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;

public class ItemSetupPanel extends GradebookPanel {

	private ComboBox<ItemModel> categoryComboBox;
	private ListStore<ItemModel> categoryStore;
	private CellEditor categoryCellEditor;
	private ListStore<ItemModel> itemStore;
	private EditorGrid<ItemModel> itemGrid;
	
	private Item gradebookItemModelRef;
	
	private final String ITEM_MARKER = "+";
	private final String ITEM_PREFIX = ":";
	

	public ItemSetupPanel() {

		super();

		categoryStore = new ListStore<ItemModel>();

		setLayout(new FitLayout());

		// Grid setup / configuration
		ArrayList<ColumnConfig> itemColumns = new ArrayList<ColumnConfig>();

		TextField<String> textField = new TextField<String>();
		textField.addInputStyleName(resources.css().gbTextFieldInput());
		CellEditor textCellEditor = new CellEditor(textField);

		ColumnConfig name = new ColumnConfig(ItemKey.S_NM.name(), "Item", 200);
		name.setEditor(textCellEditor);
		itemColumns.add(name);

		ColumnConfig percentCategory = new ColumnConfig(ItemKey.D_PCT_CTGRY.name(), "% Category", 100);
		percentCategory.setEditor(new CellEditor(new NumberField()));
		itemColumns.add(percentCategory);

		ColumnConfig points = new ColumnConfig(ItemKey.D_PNTS.name(), "Points", 100);
		points.setEditor(new CellEditor(new NumberField()));
		itemColumns.add(points);

		categoryComboBox = new ComboBox<ItemModel>(); 
		categoryComboBox.setDisplayField(ItemKey.S_NM.name());  
		categoryComboBox.setEditable(true);
		categoryComboBox.setTriggerAction(TriggerAction.ALL);
		categoryComboBox.setForceSelection(true);
		categoryComboBox.setStore(categoryStore);

		ColumnConfig category = new ColumnConfig(ItemKey.S_ID.name(), "Category", 140);

		categoryCellEditor = new CellEditor(categoryComboBox) {

			// Called before the editor sets the value on the wrapped field
			@Override
			public Object preProcessValue(Object value) {
				
				// Method argument is the selected grid item model id
				String assignmentId = (String) value;
				
				// Get the assignment and the associated category name
				ItemModel assignment = itemStore.findModel(ItemKey.S_ID.name(), assignmentId);
				String categoryName = assignment.get(ItemKey.S_PARENT.name());
				
				// Find the category from the category name
				ItemModel category = categoryStore.findModel(ItemKey.S_NM.name(), categoryName);
				
				// Mark the assignment as the one the user is performing an action on.
				// We will use the marker in the postProcessValue() method to find the assignment,
				// since the assignment is not readily available in that method.
				assignment.set(ItemKey.S_CTGRY_ID.name(), ITEM_MARKER);
				
				// FIXME:
				// Returning the category. Interestingly, I am not quite sure what the returned
				// object is used for. Testing this with returning null didn't change anything.
				// Following GXT sample code ??
				return category;
			}
			
			// Called after the editor completes an edit.
			@Override
			public Object postProcessValue(Object value) {
				
				// Method argument is the selected category model
				ItemModel category = (ItemModel) value;
				
				// Get the categoryId
				String categoryId = category.get(ItemKey.S_ID.name());
				
				// We search through all the assignments to find the one that has been marked
				// by the preProcessValue() method
				List<ItemModel> assignments = itemStore.getModels();

				for(ItemModel assignment : assignments) {

					if(ITEM_MARKER.equals(assignment.get(ItemKey.S_CTGRY_ID.name()))) {
						
						// We have found the marked assignment
						// In the marked assignment, we set the categoryId 
						assignment.set(ItemKey.S_CTGRY_ID.name(), categoryId);
						
						// Returning the assignmentId but prefix it so that the renderer thinks
						// that something changes. If we were to just return the assignmentId,
						// the renderer is not called. This also sets the assignmentId to this new
						// prefixed ID. This is fixed in the renderer code.
						return ITEM_PREFIX + assignment.get(ItemKey.S_ID.name()).toString();
					}
				}
				
				// In case we didn't find a marked assignment, we return null
				return null;
			}
		};
		
		category.setEditor(categoryCellEditor);

		category.setRenderer(new GridCellRenderer<ItemModel>() {

			public String render(ItemModel model, String property, ColumnData config, 
					int rowIndex, int colIndex, ListStore<ItemModel> store, Grid<ItemModel> grid) {

				// Method argument "model" is the selected grid item model				
				String categoryId = model.get(ItemKey.S_CTGRY_ID.name());
				
				// Case when we render the grid for the first time
				if(null == categoryId || "".equals(categoryId)) {
					
					return model.get(ItemKey.S_PARENT.name());
				}
				else { // Case where a user selects a different category from the ComboBox

					// First we "restore" the itemId since we prefixed it in the postProcessValue() method
					String assignmentId = model.get(ItemKey.S_ID.name());
					
					if(assignmentId.startsWith(ITEM_PREFIX)) {
						
						String fixedAssignmentId = assignmentId.substring(ITEM_PREFIX.length());
						model.set(ItemKey.S_ID.name(), fixedAssignmentId);
					}
					
					ItemModel category = categoryStore.findModel(ItemKey.S_ID.name(), categoryId);
					String categoryName = category.get(ItemKey.S_NM.name());
					model.set(ItemKey.S_PARENT.name(), categoryName);
					
					return categoryName;
				}
			}
		});
		
		itemColumns.add(category);

		ColumnModel itemColumnModel = new ColumnModel(itemColumns);
		itemStore = new ListStore<ItemModel>();

		itemGrid = new EditorGrid<ItemModel>(itemStore, itemColumnModel);
		itemGrid.setBorders(true);
		//itemGrid.setView(new BaseCustomGridView());
		itemGrid.setView(new GridView());
		add(itemGrid);

	}

	public void onRender(Item gradebookItemModel) {
		
		this.gradebookItemModelRef = gradebookItemModel;
		
		refreshCategoryPickerStore(gradebookItemModel);

		List<ItemModel> gradeItems = (List<ItemModel>) getGradeItems(gradebookItemModel);
		itemStore.add(gradeItems);
	}
	

	/*
	 * Get all the grade items
	 */
	private ArrayList<? extends Item> getGradeItems(Item gradebookItemModel) {

		ArrayList<Item> items = new ArrayList<Item>();

		CategoryType categoryType = gradebookItemModel.getCategoryType();

		if(CategoryType.NO_CATEGORIES == categoryType) {

			items.addAll(gradebookItemModel.getSubItems());
		}
		else {

			List<Item> categories = gradebookItemModel.getSubItems();

			for(Item category : categories) {

				items.addAll(category.getSubItems());
			}
		}

		return items;
	}

	
	private void refreshCategoryPickerStore(Item gradebookItemModel) {
		categoryStore.removeAll();
		if (gradebookItemModel != null) {

			ItemModelProcessor processor = new ItemModelProcessor(gradebookItemModel) {

				@Override
				public void doCategory(Item categoryModel) {
					categoryStore.add((ItemModel)categoryModel);
				}

			};

			processor.process();
		}
	}
	
	public void showItems() {
		List<ItemModel> items = itemStore.getModels();
		for(Item item : items) {
			GWT.log("DEBUG: XX Item : S_ID = " + item.get(ItemKey.S_ID.name()) + 
					" : S_PARENT = " + item.get(ItemKey.S_PARENT.name()) + 
					" : L_CTGRY_ID = " + item.get(ItemKey.L_CTGRY_ID.name()) +
					" : S_CTGRY_ID = " + item.get(ItemKey.S_CTGRY_ID.name()));
		}
	}
}
