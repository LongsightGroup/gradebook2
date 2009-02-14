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
package org.sakaiproject.gradebook.gwt.client.gxt;

import java.util.List;

import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.GradebookToolFacadeAsync;
import org.sakaiproject.gradebook.gwt.client.PersistentStore;
import org.sakaiproject.gradebook.gwt.client.action.PageRequestAction;
import org.sakaiproject.gradebook.gwt.client.action.RemoteCommand;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction.ClassType;
import org.sakaiproject.gradebook.gwt.client.custom.widget.grid.BaseCustomGridView;
import org.sakaiproject.gradebook.gwt.client.custom.widget.grid.CustomColumnModel;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.UserChangeEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.multigrade.MultiGradeLoadConfig;
import org.sakaiproject.gradebook.gwt.client.model.EntityModel;
import org.sakaiproject.gradebook.gwt.client.model.EntityModelComparer;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelReader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.data.TreeModelReader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeSelectionModel;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class GridPanel<M extends EntityModel> extends ContentPanel {

	public enum RefreshAction { NONE, REFRESHDATA, REFRESHCOLUMNS, REFRESHLOCALCOLUMNS, REFRESHCOLUMNSANDDATA };
	
	public static final String FAILED_FLAG = ":F";
	
	protected EditorGrid<M> grid;
	protected BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> loader;
	protected ListStore<M> store;
	
	protected String gradebookUid;
	protected String gridId;
	protected PagingToolBar pagingToolBar;
	protected EntityType entityType;
	
	protected NumberFormat defaultNumberFormat;
	
	protected CustomColumnModel cm;
	protected PagingLoadConfig loadConfig;
	protected int pageSize = 16;
	
	protected RefreshAction refreshAction = RefreshAction.NONE;
	
	public GridPanel(String gradebookUid, String gridId, EntityType entityType) {
		super();
		this.gradebookUid = gradebookUid;
		this.gridId = gridId;
		this.entityType = entityType;
		
		setHeaderVisible(false);
		setLayout(new FitLayout());
		setIconStyle("icon-table");
		setMonitorResize(true);
	
		this.defaultNumberFormat = DataTypeConversionUtil.getDefaultNumberFormat();
		
		// This event listener takes care of deferred refresh actions, the goal being to avoid
		// multiple refresh requests coming from other components and resulting in unnecessary repeated
		// calls to the server
		/*addListener(Events.BeforeShow, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				switch (refreshAction) {
				case REFRESHDATA:
					if (pagingToolBar != null)
						pagingToolBar.refresh();
					break;
				case REFRESHCOLUMNS:
				case REFRESHLOCALCOLUMNS:
				case REFRESHCOLUMNSANDDATA:
					refreshGrid(refreshAction);
					break;
				}
				refreshAction = RefreshAction.NONE;
				Info.display("Hey", "BeforeShow");
			}
		
		});*/
		
		addListener(GradebookEvents.Refresh, new Listener<BaseEvent>() {

			public void handleEvent(BaseEvent be) {
				switch (refreshAction) {
				case REFRESHDATA:
					if (pagingToolBar != null)
						pagingToolBar.refresh();
					break;
				case REFRESHCOLUMNS:
				case REFRESHLOCALCOLUMNS:
				case REFRESHCOLUMNSANDDATA:
					refreshGrid(refreshAction);
					break;
				}
				refreshAction = RefreshAction.NONE;
			}
		
		});
		
		// GRBK-31 : We only add the categories and items tree to multigrade
		if(gridId.equals("multigrade")) {
			add(newLayoutContainer(newGrid(), newTree()));
		}
		else {
			add(newGrid());
		}
	}
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		//add(newGrid());
		loader.load(0, pageSize);
	}
	
	public void editCell(Record record, String property, Object value, Object startValue, GridEvent ge) {
		UserEntityUpdateAction<M> action = newEntityUpdateAction(record, property, value, startValue, ge);
		
		RemoteCommand<M> remoteCommand = newRemoteCommand(record, property, value, startValue, ge);
		
		if (validateEdit(remoteCommand, action, record, ge)) {
			doEdit(remoteCommand, action);
		}
	}
	
	public CustomColumnModel getColumnModel() {
		return cm;
	}
	
	public ListStore<M> getStore() {
		return store;
	}
	
	public PagingToolBar getToolBar() {
		return pagingToolBar;
	}
	
	//protected abstract BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> newLoader();

	protected abstract CustomColumnModel newColumnModel();
		
	protected void addComponents()  {
		// Empty
	}
	
	protected void addGridListenersAndPlugins(EditorGrid<M> grid) {
		// Empty
	}
	
	protected void afterUpdateView(UserEntityAction<M> action, Record record, M model) {
		String property = action.getKey();
		String propertyName = property;
		
		ColumnConfig config = cm.getColumnById(property);
		propertyName = config.getHeader();
		
		action.announce(model.getDisplayName(), propertyName, model.get(property));				
	
		fireEvent(GradebookEvents.UserChange, new UserChangeEvent(action));
	}
	
	protected void beforeUpdateView(UserEntityAction<M> action, Record record, M model) {
		
	}
	
	protected Menu newContextMenu() {
		return null;
	}
	
	
	// GRBK-31
	protected LayoutContainer newLayoutContainer(Component center, Component west) {
	
		final LayoutContainer layoutContainer = new LayoutContainer(); 
		
		final BorderLayout borderLayout = new BorderLayout();  
		layoutContainer.setLayout(borderLayout);
		
		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 150);  
		westData.setSplit(true);  
		westData.setCollapsible(true);  
		westData.setMargins(new Margins(5));  

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);  
		centerData.setMargins(new Margins(5, 0, 5, 0));  

		ContentPanel contentPanel = new ContentPanel();
		contentPanel.add(west);
		layoutContainer.add(center, centerData);
		layoutContainer.add(contentPanel, westData);
		return layoutContainer;
	}
	
	
	// GRBK-31
	protected Tree newTree() {
		
		GradebookModel model = Registry.get(gradebookUid);
		ItemModel rootItemModel = model.getRootItemModel();
		
		final Tree tree = new Tree();
		tree.setSelectionModel(new TreeSelectionModel(SelectionMode.MULTI));

		TreeLoader<ItemModel> treeLoader = new BaseTreeLoader(new TreeModelReader());  
		final TreeStore<ItemModel> treeStore = new TreeStore<ItemModel>(treeLoader);
		
		TreeBinder<ItemModel> treeBinder = new TreeBinder<ItemModel>(tree, treeStore);
		treeBinder.setDisplayProperty(ItemModel.Key.NAME.name());
		treeBinder.setAutoLoad(true);
		treeBinder.addSelectionChangedListener(new SelectionChangedListener<ItemModel>() {

			@Override
			public void selectionChanged(SelectionChangedEvent<ItemModel> se) {
				
				List<ItemModel> allItemModels = treeStore.getAllItems();
				List<ItemModel> selectedItemModels = se.getSelection();
				
				// First we hide all the items
				for(ItemModel itemModel : allItemModels) {
					
					if(itemModel.getItemType().equals(Type.ITEM.getName())) {
						
						cm.setHidden(cm.getIndexById(itemModel.getIdentifier()), true);
					}
				}
				
				// Then we show the items that were selected
				for(ItemModel selectedItemModel : selectedItemModels) {
					
					// Check if user selected a gradebook, category, or item
					if(selectedItemModel.getItemType().equals(Type.GRADEBOOK.getName())) {
						
						List<ItemModel> categoryItemModels = treeStore.getChildren(selectedItemModel);
						
						for(ItemModel categoryItemModel : categoryItemModels) {
							
							List<ItemModel> assignmentItemModels = treeStore.getChildren(categoryItemModel);
							
							for(ItemModel assignmentItemModel : assignmentItemModels) {
								
								cm.setHidden(cm.getIndexById(assignmentItemModel.getIdentifier()), false);
							}
						}
					}
					else if(selectedItemModel.getItemType().equals(Type.CATEGORY.getName())) {
						
						List<ItemModel> assignmentItemModels = treeStore.getChildren(selectedItemModel);
						
						for(ItemModel assignmentItemModel : assignmentItemModels) {
							
							cm.setHidden(cm.getIndexById(assignmentItemModel.getIdentifier()), false);
						}
					}
					else if(selectedItemModel.getItemType().equals(Type.ITEM.getName())) {
						
						cm.setHidden(cm.getIndexById(selectedItemModel.getIdentifier()), false);
					}
				}
			}
			
		});

		treeLoader.load(rootItemModel);
		
		return tree;
	}
	
	
	protected Grid<M> newGrid() {

		loader = newLoader();
		loader.setRemoteSort(true);
		
		store = newStore(loader);
		
		String storedSortField = PersistentStore.getPersistentField(gradebookUid, gridId, "sortField");
		String storedSortDirection = PersistentStore.getPersistentField(gradebookUid, gridId, "sortDir");
		
		SortDir sortDir = null;
		if (storedSortDirection != null) {
			if (storedSortDirection.equals("Descending"))
				sortDir = SortDir.DESC;
			else
				sortDir = SortDir.ASC;
		}
		
		if (storedSortField != null) 
			store.setDefaultSort(storedSortField, sortDir);
		
		loadConfig = newLoadConfig(store, pageSize);

		loader.useLoadConfig(loadConfig);
		
		//loader.load(0, pageSize);
		
		pagingToolBar = newPagingToolBar(pageSize);
		pagingToolBar.bind(loader);
		
		addComponents();

		cm = newColumnModel();
		grid = new EditorGrid<M>(store, cm);
		
		GridView view = newGridView();
		
		grid.setView(view);
		grid.setLoadMask(true);
		grid.setBorders(true);
	
		grid.addListener(Events.ValidateEdit, new Listener<GridEvent>() {

			public void handleEvent(final GridEvent ge) {
				// By setting ge.doit to false, we ensure that the AfterEdit event is not thrown. Which means we have to throw it ourselves onSuccess
				ge.doit = false;
				
				editCell(ge.record, ge.property, ge.value, ge.startValue, ge);
			}

		});
		
		addGridListenersAndPlugins(grid);
				
		grid.setSelectionModel(new CellSelectionModel<M>());
		grid.setTrackMouseOver(true);
		grid.setStripeRows(true);
		
		Menu gridContextMenu = newContextMenu();
		
		if (gridContextMenu != null)
			grid.setContextMenu(gridContextMenu);
		
		return grid;
	}
	
	protected GridView newGridView() {
		return new BaseCustomGridView();
	}
	
	protected PagingLoadConfig newLoadConfig(ListStore<M> store, int pageSize) {
		SortInfo sortInfo = store.getSortState();
		MultiGradeLoadConfig loadConfig = new MultiGradeLoadConfig();
		loadConfig.setLimit(0);
		loadConfig.setOffset(pageSize);	
		if (sortInfo != null)
			loadConfig.setSortInfo(sortInfo);
		
		return loadConfig;
	}
	
	protected BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> newLoader() {
		RpcProxy<PagingLoadConfig, PagingLoadResult<M>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<M>>() {
			@Override
			protected void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<M>> callback) {
				GradebookToolFacadeAsync service = Registry.get("service");
				PageRequestAction pageAction = newPageRequestAction();
				service.getEntityPage(pageAction, loadConfig, callback);
			}
			
			@Override
			public void load(final DataReader<PagingLoadConfig, PagingLoadResult<M>> reader, final PagingLoadConfig loadConfig, final AsyncCallback<PagingLoadResult<M>> callback) {
				load(loadConfig, new NotifyingAsyncCallback<PagingLoadResult<M>>() {

					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						callback.onFailure(caught);
					}

					public void onSuccess(PagingLoadResult<M> result) {
						try {
							PagingLoadResult<M> data = null;
							if (reader != null) {
								data = reader.read(loadConfig, result);
							} else {
								data = result;
							}
							callback.onSuccess(data);
						} catch (Exception e) {
							callback.onFailure(e);
						}
					}

				});
			}
		};
		return new BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>>(proxy, new ModelReader<PagingLoadConfig>());
	}
	
	protected PageRequestAction newPageRequestAction() {
		GradebookModel model = Registry.get(gradebookUid);
		return new PageRequestAction(entityType, model.getGradebookUid(), model.getGradebookId());
	}
	
	protected PagingToolBar newPagingToolBar(int pageSize) {
		return new PagingToolBar(pageSize);
	}
	
	protected ListStore<M> newStore(BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> loader) {
		ListStore<M> store = new ListStore<M>(loader);
		store.setModelComparer(new EntityModelComparer<M>());
		store.setMonitorChanges(true);
		return store;
	}
	
	protected void refreshGrid(RefreshAction action) {
		cm = newColumnModel();
		grid.reconfigure(store, cm);
		grid.el().unmask();
	}
	
	protected void queueDeferredRefresh(RefreshAction newRefreshAction) {
		switch (this.refreshAction) {
		// We don't want to 'demote' a refresh columns action to a refresh data action
		case NONE:
			this.refreshAction = newRefreshAction;
			break;
		case REFRESHDATA:
			switch (newRefreshAction) {
			case REFRESHDATA:
				this.refreshAction = newRefreshAction;
				break;
			case REFRESHLOCALCOLUMNS:
			case REFRESHCOLUMNS:
				this.refreshAction = RefreshAction.REFRESHCOLUMNSANDDATA;
				break;
			}
			break;
		case REFRESHLOCALCOLUMNS:
			switch (newRefreshAction) {
			case REFRESHDATA:
				this.refreshAction = RefreshAction.REFRESHCOLUMNSANDDATA;
				break;
			case REFRESHLOCALCOLUMNS:
			case REFRESHCOLUMNS:
				this.refreshAction = RefreshAction.REFRESHCOLUMNS;
				break;
			}
			break;
		case REFRESHCOLUMNS:
			switch (newRefreshAction) {
			case REFRESHDATA:
				this.refreshAction = RefreshAction.REFRESHCOLUMNSANDDATA;
				break;
			case REFRESHLOCALCOLUMNS:
			case REFRESHCOLUMNS:
				this.refreshAction = RefreshAction.REFRESHCOLUMNS;
				break;
			}
			break;
		}
	}
	
	protected void updateView(UserEntityAction<M> action, Record record, M model) {
		String property = action.getKey();
		if (model.get(property) == null)
			record.set(property, null);
		else
			record.set(property, model.get(property));
	}

	protected UserEntityUpdateAction<M> newEntityUpdateAction(final Record record, final String property, 
			final Object value, final Object startValue, final GridEvent gridEvent) {
		ColumnConfig config = null;
		
		if (gridEvent != null) {
			String className = grid.getView().getCell(gridEvent.rowIndex, gridEvent.colIndex).getClassName();
			className = className.replace(" gbCellDropped", "");
			grid.getView().getCell(gridEvent.rowIndex, gridEvent.colIndex).setClassName(className);
			grid.getView().getCell(gridEvent.rowIndex, gridEvent.colIndex).setInnerText("Saving...");
		
			config = cm.getColumn(gridEvent.colIndex);
		}
		
		ClassType classType = ClassType.BOOLEAN;
		
		if (config != null && config.getEditor() != null) {
			
			if (config.getEditor().getField() instanceof NumberField) {
				Class type = ((NumberField)config.getEditor().getField()).getPropertyEditorType();
				
				classType = ClassType.DOUBLE;
				
				if (type != null && type.equals(Integer.class))
					classType = ClassType.INTEGER;
			
			} else if (config.getEditor().getField() instanceof TextField)
				classType = ClassType.STRING;
			
		}
		M model = (M)record.getModel();
		
		GradebookModel gbModel = Registry.get(gradebookUid);
		UserEntityUpdateAction<M> action = new UserEntityUpdateAction<M>(gbModel, model, property, classType, value, startValue);
		
		if (config != null) {
			String entityName = new StringBuilder().append(config.getHeader())
				.append(" : ").append(model.getDisplayName()).toString();
			action.setEntityName(entityName);
		}
		
		return action;
	}
	
	protected RemoteCommand<M> newRemoteCommand(final Record record, final String property, 
			final Object value, final Object startValue, final GridEvent gridEvent) {
		return new RemoteCommand<M>() {
			
			private static final long serialVersionUID = 1L;

			public void onCommandFailure(UserEntityAction<M> action, Throwable caught) {
				String property = action.getKey();
						
				// Save the exception message on the record
				String failedProperty = property + FAILED_FLAG;
				record.set(failedProperty, caught.getMessage());
						
				// We have to fool the system into thinking that the value has changed, since
				// we snuck in that "Saving grade..." under the radar.
				record.set(property, null);
				record.set(property, action.getStartValue());
						
				if (gridEvent != null)
					grid.fireEvent(Events.AfterEdit, gridEvent);
			}
			
			public void onCommandSuccess(UserEntityAction<M> action, M result) {

				// Ensure that we clear out any older failure messages
				// Save the exception message on the record
				String failedProperty = action.getKey() + FAILED_FLAG;
				record.set(failedProperty, null);
						
				beforeUpdateView(action, record, result);
				
				updateView(action, record, result);
						
				afterUpdateView(action, record, result);
				
				if (gridEvent != null) {
					grid.fireEvent(Events.AfterEdit, gridEvent);
				}
						
			}		
		};
	}
	
	protected void doEdit(RemoteCommand<M> remoteCommand, UserEntityUpdateAction<M> action) {
		
		remoteCommand.execute(action);

	}
	
	protected boolean validateEdit(RemoteCommand<M> remoteCommand, UserEntityUpdateAction<M> action, Record record, GridEvent gridEvent) {
		return true;
	}

	public EditorGrid<M> getGrid() {
		return grid;
	}

	public BasePagingLoader<PagingLoadConfig, PagingLoadResult<M>> getLoader() {
		return loader;
	}

	public PagingToolBar getPagingToolBar() {
		return pagingToolBar;
	}
	
	
}
