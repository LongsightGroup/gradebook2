package org.sakaiproject.gradebook.gwt.client.gxt.view;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ItemFormPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ItemTreePanel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModelComparer;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModelReader;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;

public class TreeView extends View {

	private ItemTreePanel treePanel;
	private ItemFormPanel formPanel;
	
	private TreeLoader<ItemModel> treeLoader;
	private TreeStore<ItemModel> treeStore;
	
	// We have to track which dynamic columns are visible somewhere, makes sense to do it in the view, since it
	// distinguishes between things that need to be rendered (components) and things that don't (views)
	// Multigrade depends on TreeView to establish the visible columns
	//private List<ItemModel> selectedItemModels;
	
	// We have to track which static columns are visible somewhere
	//private Set<String> fullStaticIdSet;
	//private Set<String> visibleStaticIdSet;
	
	private boolean isEditable;
	
	public TreeView(Controller controller, I18nConstants i18n, boolean isEditable) {
		super(controller);
		this.isEditable = isEditable;
		this.treePanel = new ItemTreePanel(i18n, isEditable);
		this.formPanel = new ItemFormPanel(i18n);
		//this.fullStaticIdSet = new HashSet<String>();
		//this.visibleStaticIdSet = new HashSet<String>();
	}

	@Override
	protected void handleEvent(AppEvent<?> event) {
		switch(event.type) {
		case GradebookEvents.ConfirmDeleteItem:
			onConfirmDeleteItem((ItemModel)event.data);
			break;
		case GradebookEvents.SelectDeleteItem:
			onConfirmDeleteItem((String)event.data);
			break;
		case GradebookEvents.ItemCreated:
			onItemCreated((ItemModel)event.data);
			break;
		case GradebookEvents.ItemUpdated:
			onItemUpdated((ItemModel)event.data);
			break;
		case GradebookEvents.HideColumn:
			onHideColumn((String)event.data);
			break;
		case GradebookEvents.SingleGrade:
			onSingleGrade();
			break;
		case GradebookEvents.StartEditItem:
			onEditItem((ItemModel)event.data);
			break;
		case GradebookEvents.HideEastPanel:
			onEditItemComplete((Boolean)event.data);
			break;
		case GradebookEvents.LoadItemTreeModel:
			onLoadItemTreeModel((GradebookModel)event.data);
			break;
		case GradebookEvents.NewCategory:
			onNewCategory((ItemModel)event.data);
			break;
		case GradebookEvents.NewItem:
			onNewItem((ItemModel)event.data);
			break;
		case GradebookEvents.SelectItem:
			onSelectItem((String)event.data);
			break;
		case GradebookEvents.Startup:
			GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
			onSwitchGradebook(selectedGradebook);
			break;
		case GradebookEvents.SwitchEditItem:
			onSwitchEditItem((ItemModel)event.data);
			break;
		case GradebookEvents.SwitchGradebook:
			onSwitchGradebook((GradebookModel)event.data);
			break;
		case GradebookEvents.UserChange:
			onUserChange((UserEntityAction<?>)event.data);
			break;
		case GradebookEvents.MaskItemTree:
			onMaskItemTree();
			break;
		case GradebookEvents.UnmaskItemTree:
			onUnmaskItemTree();
			break;
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
	}
	
	protected void onItemCreated(ItemModel itemModel) {
		treePanel.onItemCreated(itemModel);
		formPanel.onItemCreated(itemModel);
	}
	
	protected void onItemUpdated(ItemModel itemModel) {
		formPanel.onItemUpdated(itemModel);
	}
	
	protected void onLoadItemTreeModel(GradebookModel selectedGradebook) {
		//System.out.println("Tree View: Load Tree Model");
		treeStore.removeAll();
		ItemModel gradebookItemModel = selectedGradebook.getGradebookItemModel();
		ItemModel rootItemModel = new ItemModel();
		rootItemModel.setItemType(Type.ROOT);
		rootItemModel.setName("Root");
		gradebookItemModel.setParent(rootItemModel);
		rootItemModel.add(gradebookItemModel);
		treePanel.onBeforeLoadItemTreeModel(selectedGradebook, rootItemModel);
		//treeLoader.load(rootItemModel);
		treePanel.onLoadItemTreeModel(selectedGradebook, treeLoader, rootItemModel);
		formPanel.onLoadItemTreeModel(rootItemModel);
		
		treePanel.expandTrees();
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
		//System.out.println("TreeView: Switch Gradebook");
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
			treeStore.setStoreSorter(new StoreSorter<ItemModel>() {

				@Override
				public int compare(Store store, ItemModel m1, ItemModel m2,
						String property) {
					boolean m1Category = m1.getItemType() == Type.CATEGORY;
					boolean m2Category = m2.getItemType() == Type.CATEGORY;

					if (m1Category && !m2Category) {
						return -1;
					} else if (!m1Category && m2Category) {
						return 1;
					}

					return super.compare(store, m1, m2, property);
				}
			});
			treeStore.setModelComparer(new ItemModelComparer());

			treePanel.onTreeStoreInitialized(treeStore);
			formPanel.onTreeStoreInitialized(treeStore);
		}

		onLoadItemTreeModel(selectedGradebook);
	}
	
	protected void onUnmaskItemTree() {
		treePanel.onUnmaskItemTree();
		formPanel.onActionCompleted();
	}
	
	protected void onUserChange(UserEntityAction<?> action) {
		treePanel.onUserChange(action);
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
	
	/*private void showColumns(List<ItemModel> selectedItemModels) {
		Set<String> selectedItemModelIdSet = new HashSet<String>();
		boolean selectAll = false;
		
		for (ItemModel selectedItemModel : selectedItemModels) {
			//selectedItemModelIdSet.add(selectedItemModel.getIdentifier());
			
			// If the root or gradebook is selected then we don't need to mess around any further
			switch (selectedItemModel.getItemType()) {
			case ITEM:
				selectedItemModelIdSet.add(selectedItemModel.getIdentifier());
				break;
			}
		}

		Dispatcher.forwardEvent(GradebookEvents.ShowColumns, 
				new ShowColumnsEvent(selectAll, fullStaticIdSet, visibleStaticIdSet, selectedItemModelIdSet));
	
	}*/
	
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

}
