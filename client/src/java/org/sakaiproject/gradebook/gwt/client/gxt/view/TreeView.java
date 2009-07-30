package org.sakaiproject.gradebook.gwt.client.gxt.view;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ItemFormPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ItemTreePanel;
import org.sakaiproject.gradebook.gwt.client.model.FixedColumnModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModelComparer;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModelReader;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.TreeStore;

public class TreeView extends View {

	private ItemTreePanel treePanel;
	private ItemFormPanel formPanel;
	
	private TreeLoader<ItemModel> treeLoader;
	private TreeStore<ItemModel> treeStore;
	
	private GradebookModel selectedGradebook;
	
	private boolean isInitialized;
	
	public TreeView(Controller controller, I18nConstants i18n, boolean isEditable) {
		super(controller);
		this.treePanel = new ItemTreePanel(i18n, isEditable);
		this.formPanel = new ItemFormPanel(i18n);
		this.isInitialized = false;
	}

	@Override
	protected void handleEvent(AppEvent<?> event) {
		switch(GradebookEvents.getEvent(event.type).getEventKey()) {
		case CONFIRM_DELETE_ITEM:
			onConfirmDeleteItem((ItemModel)event.data);
			break;
		case SELECT_DELETE_ITEM:
			onConfirmDeleteItem((String)event.data);
			break;
		case ITEM_CREATED:
			onItemCreated((ItemModel)event.data);
			break;
		case ITEM_DELETED:
			onItemDeleted((ItemModel)event.data);
			break;
		case ITEM_UPDATED:
			onItemUpdated((ItemModel)event.data);
			break;
		case HIDE_COLUMN:
			onHideColumn((String)event.data);
			break;
		case SINGLE_GRADE:
			onSingleGrade();
			break;
		case START_EDIT_ITEM:
			onEditItem((ItemModel)event.data);
			break;
		case HIDE_EAST_PANEL:
			onEditItemComplete((Boolean)event.data);
			break;
		case LOAD_ITEM_TREE_MODEL:
			onLoadItemTreeModel((GradebookModel)event.data);
			break;
		case NEW_CATEGORY:
			onNewCategory((ItemModel)event.data);
			break;
		case NEW_ITEM:
			onNewItem((ItemModel)event.data);
			break;
		case REFRESH_GRADEBOOK_ITEMS:
			onRefreshGradebookItems((GradebookModel)event.data);
			break;
		case REFRESH_GRADEBOOK_SETUP:
			onRefreshGradebookSetup((GradebookModel)event.data);
			break;
		case SELECT_ITEM:
			onSelectItem((String)event.data);
			break;
		case STARTUP:
			GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
			onSwitchGradebook(selectedGradebook);
			break;
		case SWITCH_EDIT_ITEM:
			onSwitchEditItem((ItemModel)event.data);
			break;
		case SWITCH_GRADEBOOK:
			onSwitchGradebook((GradebookModel)event.data);
			break;
		case USER_CHANGE:
			onUserChange((UserEntityAction<?>)event.data);
			break;
		/*case MASK_ITEM_TREE:
			onMaskItemTree();
			break;
		case UNMASK_ITEM_TREE:
			onUnmaskItemTree();
			break;*/
		}
	}
	
	protected void onConfirmDeleteItem(String itemModelId) {
		ItemModel itemModel = findItemByColumnId(itemModelId);
	
		if (itemModel != null)
			formPanel.onConfirmDeleteItem(itemModel);
	}
	
	protected void onConfirmDeleteItem(ItemModel itemModel) {
		formPanel.onConfirmDeleteItem(itemModel);
	}
	
	protected void onEditItem(ItemModel itemModel) {
		formPanel.onEditItem(itemModel, true);
	}
	
	protected void onEditItemComplete(Boolean doCommit) {
		if (doCommit.booleanValue())
			treeStore.commitChanges();
		else
			treeStore.rejectChanges();	
	}
	
	protected void onHideColumn(String columnId) {
		ItemModel itemModel = findItemByColumnId(columnId);
		
		if (itemModel != null)
			treePanel.onHideColumn(itemModel);
		else {
			// It's probably a fixed column
			FixedColumnModel fixedModel = findFixedByColumnId(columnId);
			treePanel.onHideColumn(fixedModel);
		}
	}
	
	protected void onItemCreated(ItemModel itemModel) {
		treePanel.onItemCreated(itemModel);
		formPanel.onItemCreated(itemModel);
	}
	
	protected void onItemDeleted(ItemModel itemModel) {
		formPanel.onItemDeleted(itemModel);
	}
	
	protected void onItemUpdated(ItemModel itemModel) {
		formPanel.onItemUpdated(itemModel);
	}
	
	protected void onLoadItemTreeModel(GradebookModel selectedGradebook) {
		/*
		if (isTreeRefreshUnnecessary(selectedGradebook)) 
			return;
		
		onMaskItemTree();
		treeStore.removeAll();
		ItemModel gradebookItemModel = selectedGradebook.getGradebookItemModel();
		ItemModel rootItemModel = new ItemModel();
		rootItemModel.setItemType(Type.ROOT);
		rootItemModel.setName("Root");
		gradebookItemModel.setParent(rootItemModel);
		rootItemModel.add(gradebookItemModel);
		treePanel.onBeforeLoadItemTreeModel(selectedGradebook, rootItemModel);
		treePanel.onLoadItemTreeModel(selectedGradebook, treeLoader, rootItemModel);
		formPanel.onLoadItemTreeModel(rootItemModel);
		
		treePanel.expandTrees();
		onUnmaskItemTree();*/
	}
	
	protected void onMaskItemTree() {
		treePanel.onMaskItemTree();
	}
	
	protected void onNewCategory(ItemModel itemModel) {
		formPanel.onNewCategory(itemModel);
	}
	
	protected void onNewItem(ItemModel itemModel) {
		formPanel.onNewItem(itemModel);
	}
	
	protected void onRefreshGradebookItems(GradebookModel gradebookModel) {
		onMaskItemTree();
		treeStore.removeAll();
		ItemModel gradebookItemModel = gradebookModel.getGradebookItemModel();
		ItemModel rootItemModel = new ItemModel();
		rootItemModel.setItemType(Type.ROOT);
		rootItemModel.setName("Root");
		gradebookItemModel.setParent(rootItemModel);
		rootItemModel.add(gradebookItemModel);
		treePanel.onBeforeLoadItemTreeModel(gradebookModel, rootItemModel);
		treePanel.onRefreshGradebookItems(gradebookModel, treeLoader, rootItemModel);
		formPanel.onLoadItemTreeModel(rootItemModel);
		
		treePanel.expandTrees();
		onUnmaskItemTree();
	}
	
	protected void onRefreshGradebookSetup(GradebookModel gradebookModel) {
		treePanel.onRefreshGradebookSetup(gradebookModel);
	}
	
	protected void onSelectItem(String itemModelId) {
		
		if (treeStore != null) {
			List<ItemModel> itemModels = treeStore.findModels(ItemModel.Key.ID.name(), itemModelId);
			if (itemModels != null) {
				for (ItemModel itemModel : itemModels) {
					Type itemType = itemModel.getItemType();
					if (itemType == Type.ITEM) {
						onEditItem(itemModel);
						break;
					}
				}
			}
		}
	}
	
	protected void onSingleGrade() {
		treePanel.onSingleGrade();
	}
	
	protected void onSwitchEditItem(ItemModel itemModel) {
		formPanel.onEditItem(itemModel, false);
	}
	
	@SuppressWarnings("unchecked")
	protected void onSwitchGradebook(final GradebookModel selectedGradebook) {
		this.selectedGradebook = selectedGradebook;
		
		formPanel.onSwitchGradebook(selectedGradebook);
		treePanel.onSwitchGradebook(selectedGradebook);
		
		
		// FIXME: Need to send an event to show which ones are checked
		
		
		if (treeLoader == null) {
			treeLoader = new BaseTreeLoader(new TreeModelReader() {
	
				@Override
				protected List<? extends ModelData> getChildren(ModelData parent) {
					List visibleChildren = new ArrayList();
					List<? extends ModelData> children = super.getChildren(parent);
					
					for (ModelData model : children) {
						//String source = model.get(ItemModel.Key.SOURCE.name());
						//if (source == null || !source.equals("Static"))
						visibleChildren.add(model);
					}
					
					return visibleChildren;
				}
			});
		}
		
		if (treeStore == null) {
			treeStore = new TreeStore<ItemModel>(treeLoader);
			treeStore.setModelComparer(new ItemModelComparer());

			treePanel.onTreeStoreInitialized(treeStore, selectedGradebook.isUserAbleToEditAssessments());
			formPanel.onTreeStoreInitialized(treeStore);
		}

		//onLoadItemTreeModel(selectedGradebook);
		onRefreshGradebookItems(selectedGradebook);
		onRefreshGradebookSetup(selectedGradebook);
	}
	
	protected void onUnmaskItemTree() {
		treePanel.onUnmaskItemTree();
		formPanel.onActionCompleted();
	}
	
	protected void onUserChange(UserEntityAction<?> action) {
		treePanel.onUserChange(action);
	}
	
	private FixedColumnModel findFixedByColumnId(String fixedId) {
		FixedColumnModel fixedModel = null;
		
		if (selectedGradebook != null) {
			List<FixedColumnModel> fixedColumns = selectedGradebook.getColumns();
			
			for (FixedColumnModel current : fixedColumns) {
				
				if (current.getIdentifier().equals(fixedId)) {
					fixedModel = current;
					break;
				}
				
			}
		}
		
		return fixedModel;
	}
	
	private ItemModel findItemByColumnId(String itemModelId) {
		ItemModel itemModel = null;
		
		List<ItemModel> itemModels = treeStore.findModels(ItemModel.Key.ID.name(), itemModelId);
		if (itemModels != null) {
			for (ItemModel current : itemModels) {
				Type itemType = current.getItemType();
				if (itemType == Type.ITEM) {
					itemModel = current;
					break;
				}
			}
		}
	
		return itemModel;
	}
	
	// Public accessors
	
	public ItemTreePanel getTreePanel() {
		return treePanel;
	}

	public ItemFormPanel getFormPanel() {
		return formPanel;
	}

	public TreeStore<ItemModel> getTreeStore() {
		return treeStore;
	}

	public void setTreeStore(TreeStore<ItemModel> treeStore) {
		this.treeStore = treeStore;
	}
	

	// Helper methods
	
	private boolean isTreeRefreshUnnecessary(GradebookModel selectedGradebook) {
		// First thing we need to do here is decide whether we can avoid making expensive ui changes
		ItemModel oldGradebookItemModel = this.selectedGradebook == null ? null : this.selectedGradebook.getGradebookItemModel();
		ItemModel newGradebookItemModel = selectedGradebook == null ? null : selectedGradebook.getGradebookItemModel();
		CategoryType oldCategoryType = oldGradebookItemModel == null ? null : oldGradebookItemModel.getCategoryType();
		CategoryType newCategoryType = newGradebookItemModel == null ? null : newGradebookItemModel.getCategoryType();
			
		this.selectedGradebook = selectedGradebook;
		
		return (isInitialized && oldCategoryType != null && newCategoryType != null
				&& oldCategoryType == newCategoryType);
	}

}
