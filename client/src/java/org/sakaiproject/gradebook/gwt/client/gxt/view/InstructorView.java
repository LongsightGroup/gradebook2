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

package org.sakaiproject.gradebook.gwt.client.gxt.view;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.ExportDetails;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.ExportDetails.ExportType;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaButton;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaMenu;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaMenuItem;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.ItemUpdate;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.BorderLayoutPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.GradeScalePanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.GraderPermissionSettingsPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.HistoryPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ItemFormPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.LearnerSummaryPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.StatisticsPanel;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.resource.GradebookResources;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

public class InstructorView extends AppView {

	private static final String MENU_SELECTOR_FLAG = "menuSelector";
	public enum MenuSelector { ADD_CATEGORY, ADD_ITEM, IMPORT, EXPORT, EXPORT_DATA, EXPORT_DATA_CSV, EXPORT_STRUCTURE, EXPORT_STRUCTURE_CSV, EXPORT_DATA_XLS, EXPORT_STRUCTURE_XLS, FINAL_GRADE, GRADE_SCALE, SETUP, HISTORY, GRADER_PERMISSION_SETTINGS, STATISTICS };
	
	// The instructor view maintains a link to tree view, since it is required to instantiate multigrade
	private TreeView treeView;
	private MultigradeView multigradeView;
	private SingleGradeView singleGradeView;

	private ContentPanel borderLayoutContainer;
	private LayoutContainer centerLayoutContainer;
	private ContentPanel eastLayoutContainer;
	private BorderLayout borderLayout;
	private CardLayout centerCardLayout;
	private CardLayout eastCardLayout;
	private LearnerSummaryPanel singleGradeContainer;
	//private HelpPanel helpPanel;
	private GradeScalePanel gradeScalePanel;
	private HistoryPanel historyPanel;
	private GraderPermissionSettingsPanel graderPermissionSettingsPanel;
	private StatisticsPanel statisticsPanel;

	private List<TabConfig> tabConfigurations;

	private Listener<MenuEvent> menuEventListener;
	private SelectionListener<MenuEvent> menuSelectionListener;
	private SelectionListener<ButtonEvent> toolBarSelectionListener;

	private ToolBar toolBar;

	private Menu fileMenu;
	private Menu windowMenu;

	private MenuItem addCategoryMenuItem;

	private BorderLayoutData centerData;
	private BorderLayoutData eastData;
	private BorderLayoutData northData;
	private BorderLayoutData westData;
	
	private GradebookResources resources;
	private I18nConstants i18n;
	private boolean isEditable;

	public InstructorView(Controller controller, TreeView treeView, MultigradeView multigradeView, 
			SingleGradeView singleGradeView, 
			boolean isEditable, final boolean isNewGradebook) {
		super(controller);
		this.isEditable = isEditable;
		this.tabConfigurations = new ArrayList<TabConfig>();
		this.treeView = treeView;
		this.multigradeView = multigradeView;
		this.singleGradeView = singleGradeView;
		this.i18n = Registry.get(AppConstants.I18N);
		this.resources = Registry.get(AppConstants.RESOURCES);
		
		initListeners();
		
		toolBar = new ToolBar();
		borderLayoutContainer = new BorderLayoutPanel(); 
		borderLayoutContainer.setId("borderLayoutContainer");
		borderLayoutContainer.setHeaderVisible(false);
		borderLayoutContainer.setTopComponent(toolBar);

		borderLayout = new BorderLayout();  
		borderLayoutContainer.setLayout(borderLayout);

		centerData = new BorderLayoutData(LayoutRegion.CENTER); 
		centerData.setMinSize(100);
		centerData.setMargins(new Margins(5, 0, 5, 0)); 

		eastData = new BorderLayoutData(LayoutRegion.EAST, 420);
		eastData.setSplit(true);
		eastData.setCollapsible(true);
		eastData.setFloatable(false);
		eastData.setMargins(new Margins(5));
		eastData.setMaxSize(800);
		eastData.setHidden(true);

		northData = new BorderLayoutData(LayoutRegion.NORTH, 50);
		northData.setCollapsible(false);
		northData.setHidden(true);

		westData = new BorderLayoutData(LayoutRegion.WEST, 400, 100, 800);  
		westData.setSplit(true);  
		westData.setCollapsible(true);  
		westData.setMargins(new Margins(5));

		centerLayoutContainer = new LayoutContainer();
		centerCardLayout = new CardLayout();
		centerLayoutContainer.setLayout(centerCardLayout);

		centerLayoutContainer.add(multigradeView.getMultiGradeContentPanel());
		centerLayoutContainer.add(treeView.getFormPanel());
		centerCardLayout.setActiveItem(multigradeView.getMultiGradeContentPanel());

		eastLayoutContainer = new ContentPanel() {
			protected void onRender(Element parent, int index) {
				super.onRender(parent, index);
			}
		};

		/*helpPanel = new HelpPanel() {
			protected void onRender(Element parent, int index) {
				super.onRender(parent, index);
				//if (borderLayoutContainer.isRendered())
				//	borderLayout.collapse(LayoutRegion.EAST);
			}
		};*/

		eastLayoutContainer.setId("cardLayoutContainer");
		eastLayoutContainer.setWidth(400);
		eastLayoutContainer.setBorders(true);
		eastLayoutContainer.setBodyBorder(true);
		eastLayoutContainer.setFrame(true);
		eastCardLayout = new CardLayout();
		eastLayoutContainer.setLayout(eastCardLayout);
		//eastLayoutContainer.add(helpPanel);
		//eastCardLayout.setActiveItem(helpPanel);

		borderLayoutContainer.add(new LayoutContainer(), northData);
		borderLayoutContainer.add(treeView.getTreePanel(), westData);
		borderLayoutContainer.add(centerLayoutContainer, centerData);
		borderLayoutContainer.add(eastLayoutContainer, eastData);

		if (isEditable) {
			tabConfigurations.add(new TabConfig(AppConstants.TAB_SETUP, i18n.tabSetupHeader(), resources.css().gbSetupButton(), true, MenuSelector.SETUP));
			tabConfigurations.add(new TabConfig(AppConstants.TAB_GRADESCALE, i18n.tabGradeScaleHeader(), resources.css().gbGradeScaleButton(), true, MenuSelector.GRADE_SCALE));
			tabConfigurations.add(new TabConfig(AppConstants.TAB_GRADER_PER_SET, i18n.tabGraderPermissionSettingsHeader(), resources.css().gbGraderPermissionSettings(), true, MenuSelector.GRADER_PERMISSION_SETTINGS));
			tabConfigurations.add(new TabConfig(AppConstants.TAB_HISTORY, i18n.tabHistoryHeader(), resources.css().gbHistoryButton(), true, MenuSelector.HISTORY));
		}
		tabConfigurations.add(new TabConfig(AppConstants.TAB_STATISTICS, i18n.tabStatisticsHeader(), resources.css().gbStatisticsButton(), true, MenuSelector.STATISTICS));

		populateToolBar(i18n);

		viewport.add(borderLayoutContainer);
		viewportLayout.setActiveItem(borderLayoutContainer);
	}

	@Override
	protected void initialize() {
		super.initialize();

	}

	@Override
	protected void initUI(ApplicationModel model) {
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);		

		addCategoryMenuItem.setVisible(selectedGradebook.getGradebookItemModel().getCategoryType() != CategoryType.NO_CATEGORIES);
		
		if (DataTypeConversionUtil.checkBoolean(selectedGradebook.isNewGradebook()))
			Dispatcher.forwardEvent(GradebookEvents.StartEditItem.getEventType(), selectedGradebook.getGradebookItemModel());
	}

	@Override
	protected void onCloseNotification() {
	}

	@Override
	protected void onExpandEastPanel(EastCard activeCard) {

		ItemFormPanel formPanel = treeView.getFormPanel();

		switch (activeCard) {
			case GRADE_SCALE:
			case HELP:
			case HISTORY:
			case LEARNER_SUMMARY:
			case STATISTICS:
				borderLayout.show(LayoutRegion.EAST);
				borderLayout.expand(LayoutRegion.EAST);
				break;
			default:
				borderLayout.hide(LayoutRegion.EAST);
				break;
		}

		switch (activeCard) {
			case DELETE_CATEGORY:
				formPanel.setHeading(i18n.deleteCategoryHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case DELETE_ITEM:
				formPanel.setHeading(i18n.deleteItemHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case GRADE_SCALE:
				eastLayoutContainer.setHeading(i18n.gradeScaleHeading());
				eastCardLayout.setActiveItem(gradeScalePanel);
				break;
			case STATISTICS:
				//eastLayoutContainer.setHeading(i18n.statisticsHeading());
				//eastCardLayout.setActiveItem(statisticsPanel);
				viewportLayout.setActiveItem(statisticsPanel);
				break;
			//case HELP:
			//	eastLayoutContainer.setHeading(i18n.helpHeading());
			//	eastCardLayout.setActiveItem(helpPanel);
			//	break;
			case HISTORY:
				//eastLayoutContainer.setHeading(i18n.historyHeading());
				//eastCardLayout.setActiveItem(historyPanel);
				viewportLayout.setActiveItem(historyPanel);
				break;
			case NEW_CATEGORY:
				formPanel.setHeading(i18n.newCategoryHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case NEW_ITEM:
				formPanel.setHeading(i18n.newItemHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case EDIT_CATEGORY:
				formPanel.setHeading(i18n.editCategoryHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case EDIT_GRADEBOOK:
				formPanel.setHeading(i18n.editGradebookHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case EDIT_ITEM:
				formPanel.setHeading(i18n.editItemHeading());
				centerCardLayout.setActiveItem(treeView.getFormPanel());
				multigradeView.deselectAll();
				break;
			case LEARNER_SUMMARY:
				eastLayoutContainer.setHeading(i18n.learnerSummaryHeading());
				eastCardLayout.setActiveItem(singleGradeContainer);
				break;
		}

	}

	@Override
	protected void onFailedToUpdateItem(ItemUpdate itemUpdate) {
		if (gradeScalePanel != null) {
			gradeScalePanel.onFailedToUpdateItem(itemUpdate);
		}
	}
	
	@Override
	protected void onGradeTypeUpdated(GradebookModel selectedGradebook) {
		if (singleGradeContainer != null) {
			singleGradeContainer.onGradeTypeUpdated(selectedGradebook);
		}
	}
	
	@Override
	protected void onItemCreated(ItemModel itemModel) {
		onHideEastPanel(Boolean.FALSE);
	}

	@Override
	protected void onLearnerGradeRecordUpdated(UserEntityUpdateAction action) {
		if (singleGradeContainer != null && singleGradeContainer.isVisible()) {
			singleGradeContainer.onLearnerGradeRecordUpdated(action.getModel());
		}

		if (statisticsPanel != null && statisticsPanel.isVisible()) {
			statisticsPanel.onLearnerGradeRecordUpdated(action.getModel());
		}
	}

	@Override
	protected void onLoadItemTreeModel(GradebookModel selectedGradebook) {

	}

	@Override
	protected void onNewCategory(ItemModel itemModel) {
		onExpandEastPanel(EastCard.NEW_CATEGORY);
	}

	@Override
	protected void onNewItem(ItemModel itemModel) {
		onExpandEastPanel(EastCard.NEW_ITEM);
	}

	@Override
	protected void onOpenNotification() {

	}

	@Override
	protected void onRefreshGradebookItems(GradebookModel gradebookModel) {

	}

	@Override
	protected void onRefreshGradebookSetup(GradebookModel gradebookModel) {
		if (addCategoryMenuItem != null)
			addCategoryMenuItem.setVisible(gradebookModel.getGradebookItemModel().getCategoryType() != CategoryType.NO_CATEGORIES);
	
		if (singleGradeContainer != null)
			singleGradeContainer.onRefreshGradebookSetup(gradebookModel);
	}

	@Override
	protected void onRefreshGradeScale(GradebookModel gradebookModel) {
		if (gradeScalePanel != null) 
			gradeScalePanel.onRefreshGradeScale(gradebookModel);
	}

	@Override
	protected void onSelectLearner(ModelData learner) {
		if (singleGradeContainer != null && singleGradeContainer.isVisible()) {
			onSingleGrade(learner);
		}
	}

	@Override
	protected void onSingleGrade(final ModelData learnerGradeRecordCollection) {
		/*GWT.runAsync(new RunAsyncCallback() {
			public void onFailure(Throwable caught) {

			}

			public void onSuccess() {*/
				if (singleGradeContainer == null) {
					singleGradeContainer = new LearnerSummaryPanel();
					eastLayoutContainer.add(singleGradeContainer);
				}
				singleGradeContainer.onChangeModel(multigradeView.getStore(), treeView.getTreeStore(), learnerGradeRecordCollection);
				onExpandEastPanel(EastCard.LEARNER_SUMMARY);
			/*}
		});*/
	}

	@Override
	protected void onSingleView(ModelData learner) {
		viewport.add(singleGradeView.getDialog());
		viewportLayout.setActiveItem(singleGradeView.getDialog());
	}

	@Override
	protected void onShowGradeScale(Boolean show) {
		/*GWT.runAsync(new RunAsyncCallback() {
			public void onFailure(Throwable caught) {

			}

			public void onSuccess() {*/
				if (gradeScalePanel == null) {
					gradeScalePanel = new GradeScalePanel(isEditable, treeView);
					eastLayoutContainer.add(gradeScalePanel);
				}
				onExpandEastPanel(EastCard.GRADE_SCALE);
			/*}
		});*/
	}

	@Override
	protected void onShowHistory(String identifier) {
		/*GWT.runAsync(new RunAsyncCallback() {
			public void onFailure(Throwable caught) {

			}

			public void onSuccess() {*/
				if (historyPanel == null) {
					historyPanel = new HistoryPanel(i18n);
					viewport.add(historyPanel);
				}
				viewportLayout.setActiveItem(historyPanel);
			/*}
		});*/
	}

	protected void onShowSetup() {
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
		
		if (selectedGradebook != null) {
			ItemModel itemModel = selectedGradebook.getGradebookItemModel();
			Dispatcher.forwardEvent(GradebookEvents.StartEditItem.getEventType(), itemModel);
		}
	}
	
	@Override
	protected void onShowStatistics() {
		if (statisticsPanel == null) {
			statisticsPanel = new StatisticsPanel(i18n);
			viewport.add(statisticsPanel);
		}
		statisticsPanel.onLearnerGradeRecordUpdated(null);
		viewportLayout.setActiveItem(statisticsPanel);
		//onExpandEastPanel(EastCard.STATISTICS);
	}

	@Override
	protected void onStopStatistics() {
		viewportLayout.setActiveItem(borderLayoutContainer);
	}
	
	@Override
	protected void onStartEditItem(ItemModel itemModel) {
		AppView.EastCard activeCard = AppView.EastCard.EDIT_ITEM;

		if (itemModel != null) {
			switch (itemModel.getItemType()) {
				case CATEGORY:
					activeCard = AppView.EastCard.EDIT_CATEGORY;
					break;
				case GRADEBOOK:
					activeCard = AppView.EastCard.EDIT_GRADEBOOK;
			}
		}
		onExpandEastPanel(activeCard);
	}

	@Override
	protected void onStartImport() {
		
	}

	@Override
	protected void onStopImport() {
		viewportLayout.setActiveItem(borderLayoutContainer);
	}

	@Override
	protected void onStartGraderePermissions() {

		if(graderPermissionSettingsPanel == null) {
			graderPermissionSettingsPanel = new GraderPermissionSettingsPanel(i18n, isEditable);
		}
		viewport.add(graderPermissionSettingsPanel);
		viewportLayout.setActiveItem(graderPermissionSettingsPanel);
	}

	@Override
	protected void onStopGraderPermissions() {
		viewportLayout.setActiveItem(borderLayoutContainer);
	}


	@Override
	protected void onHideEastPanel(Boolean doCommit) {
		borderLayout.hide(LayoutRegion.EAST);
	}

	protected void onHideFormPanel() {
		centerCardLayout.setActiveItem(multigradeView.getMultiGradeContentPanel());
	}

	@Override
	protected void onSwitchGradebook(GradebookModel selectedGradebook) {

		if (addCategoryMenuItem != null)
			addCategoryMenuItem.setVisible(selectedGradebook.getGradebookItemModel().getCategoryType() != CategoryType.NO_CATEGORIES);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onUserChange(UserEntityAction<?> action) {

		switch (action.getEntityType()) {
			case GRADEBOOK:
				switch (action.getActionType()) {
					case UPDATE:
						UserEntityUpdateAction<GradebookModel> updateAction = (UserEntityUpdateAction<GradebookModel>)action;
						GradebookModel.Key gradebookModelKey = GradebookModel.Key.valueOf(updateAction.getKey());
						switch (gradebookModelKey) {
							case CATEGORYTYPE:
								GradebookModel selectedGradebook = updateAction.getModel();
								addCategoryMenuItem.setVisible(selectedGradebook.getGradebookItemModel().getCategoryType() != CategoryType.NO_CATEGORIES);
								break;
						}
						break;
				}
				break;
		}
	}

	/*
	 * The goal here is to reduce the number of overall listeners in the application to a minimum
	 */
	private void initListeners() {

		menuEventListener = new Listener<MenuEvent>() {

			public void handleEvent(MenuEvent me) {

				if (me.getType().equals(Events.Select)) {
					MenuItem menuItem = (MenuItem)me.getItem();
					MenuSelector menuSelector = menuItem.getData(MENU_SELECTOR_FLAG);

					switch (menuSelector) {
						case GRADE_SCALE:
							onShowGradeScale(Boolean.TRUE);
							break;
						case HISTORY:
							onShowHistory(null);
							break;
						case GRADER_PERMISSION_SETTINGS:
							onStartGraderePermissions();
							break;
						case STATISTICS:
							onShowStatistics();
							break;
						case SETUP:
							onShowSetup();
							break;
					}
				}
			}

		};

		menuSelectionListener = new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent me) {
				MenuSelector selector = me.getItem().getData(MENU_SELECTOR_FLAG);
				ExportDetails ex;
				switch (selector) {
					case ADD_CATEGORY:
						Dispatcher.forwardEvent(GradebookEvents.NewCategory.getEventType());
						break;
					case ADD_ITEM:
						Dispatcher.forwardEvent(GradebookEvents.NewItem.getEventType());
						break;
					case EXPORT_DATA_XLS:
						ex = new ExportDetails(ExportType.XLS97, false);
						Dispatcher.forwardEvent(GradebookEvents.StartExport.getEventType(), ex);
						break;
					case EXPORT_STRUCTURE_XLS:
						ex = new ExportDetails(ExportType.XLS97, true);
						Dispatcher.forwardEvent(GradebookEvents.StartExport.getEventType(), ex);
						break;
					case EXPORT_DATA_CSV:
						ex = new ExportDetails(ExportType.CSV, false);
						Dispatcher.forwardEvent(GradebookEvents.StartExport.getEventType(), ex);
						break;
					case EXPORT_STRUCTURE_CSV:
						ex = new ExportDetails(ExportType.CSV, true);
						Dispatcher.forwardEvent(GradebookEvents.StartExport.getEventType(), ex);
						break;
					case IMPORT:
						Dispatcher.forwardEvent(GradebookEvents.StartImport.getEventType());
						break;
					case FINAL_GRADE:
						Dispatcher.forwardEvent(GradebookEvents.StartFinalgrade.getEventType());
						break;
				}
			}

		};

		toolBarSelectionListener = new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent tbe) {

				String helpUrl = Registry.get(AppConstants.HELP_URL);
				Window.open(helpUrl, "_blank", "resizable=yes,scrollbars=yes,outerHeight=600,outerWidth=350");
			}

		};
	}

	/*
	 * Create a top-level toolbar with menu drop downs
	 */
	private ToolBar populateToolBar(I18nConstants i18n) {

		if (isEditable) {
			AriaButton fileItem = new AriaButton(i18n.newMenuHeader());
			fileItem.setMenu(newFileMenu(i18n));
			toolBar.add(fileItem);
		}

		AriaButton windowItem = new AriaButton(i18n.viewMenuHeader());
		windowMenu = newWindowMenu(i18n);
		windowItem.setMenu(windowMenu);

		AriaButton moreItem = new AriaButton(i18n.moreMenuHeader());
		moreItem.setMenu(newMoreActionsMenu());

		AriaButton helpItem = new AriaButton(i18n.helpMenuHeader());
		helpItem.addSelectionListener(toolBarSelectionListener);

		toolBar.add(windowItem);
		toolBar.add(moreItem);
		toolBar.add(helpItem);
		
		toolBar.add(new FillToolItem());
		
		String version = Registry.get(AppConstants.VERSION);
		LabelField versionLabel = new LabelField(version);
		toolBar.add(versionLabel);

		return toolBar;
	}

	/*
	 * Create a new file menu 
	 */
	private Menu newFileMenu(I18nConstants i18n) {

		// This should be obvious. Just create the required menu object and its menu items
		fileMenu = new AriaMenu();
		addCategoryMenuItem = new AriaMenuItem(i18n.categoryName(), menuSelectionListener);
		addCategoryMenuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.ADD_CATEGORY);
		addCategoryMenuItem.setIconStyle(resources.css().gbAddCategoryIcon());
		addCategoryMenuItem.setId(AppConstants.ID_ADD_CATEGORY_MENUITEM);
		MenuItem addItem = new AriaMenuItem(i18n.itemName(), menuSelectionListener);
		addItem.setData(MENU_SELECTOR_FLAG, MenuSelector.ADD_ITEM);
		addItem.setIconStyle(resources.css().gbAddItemIcon());
		addItem.setId(AppConstants.ID_ADD_ITEM_MENUITEM);

		// Attach the items to the menu
		fileMenu.add(addCategoryMenuItem);
		fileMenu.add(addItem);


		return fileMenu;
	}

	private Menu newWindowMenu(I18nConstants i18n) {
		Menu windowMenu = new AriaMenu();

		for (TabConfig tabConfig : tabConfigurations) {
			MenuItem windowMenuItem = newWindowMenuItem(tabConfig);
			windowMenu.add(windowMenuItem);
		}

		return windowMenu;
	}

	private MenuItem newWindowMenuItem(TabConfig tabConfig) {
		String id = new StringBuilder().append(AppConstants.WINDOW_MENU_ITEM_PREFIX).append(tabConfig.id).toString();
		MenuItem menuItem = new AriaMenuItem();
		menuItem.setText(tabConfig.header);
		menuItem.setData(MENU_SELECTOR_FLAG, tabConfig.menuSelector);
		menuItem.setEnabled(tabConfig.isClosable);
		menuItem.setId(id);
		menuItem.setIconStyle(tabConfig.iconStyle);
		tabConfig.menuItemId = id;

		menuItem.addListener(Events.Select, menuEventListener);

		return menuItem;
	}


	private Menu newMoreActionsMenu() {
		Menu moreActionsMenu = new AriaMenu();

		MenuItem menuItem = new AriaMenuItem(i18n.headerExport());
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT);
		menuItem.setIconStyle(resources.css().gbExportItemIcon());
		menuItem.setTitle(i18n.headerExportTitle());
		moreActionsMenu.add(menuItem);

		Menu subMenu = new AriaMenu();
		menuItem.setSubMenu(subMenu);


		menuItem = new AriaMenuItem(i18n.headerExportData());
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_DATA);
		menuItem.setTitle(i18n.headerExportDataTitle());
		subMenu.add(menuItem);

		Menu typeMenu = new AriaMenu();
		menuItem.setSubMenu(typeMenu);
		
		menuItem = new AriaMenuItem(i18n.headerExportCSV(), menuSelectionListener);
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_DATA_CSV);
		menuItem.setTitle(i18n.headerExportCSVTitle());
		typeMenu.add(menuItem);

		menuItem = new AriaMenuItem(i18n.headerExportXLS(), menuSelectionListener);
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_DATA_XLS);
		menuItem.setTitle(i18n.headerExportXLSTitle());
		typeMenu.add(menuItem);

		
		menuItem = new AriaMenuItem(i18n.headerExportStructure());
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_STRUCTURE);
		menuItem.setTitle(i18n.headerExportStructureTitle());
		subMenu.add(menuItem);

		
		typeMenu = new AriaMenu();
		menuItem.setSubMenu(typeMenu);
		
		menuItem = new AriaMenuItem(i18n.headerExportCSV(), menuSelectionListener);
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_STRUCTURE_CSV);
		menuItem.setTitle(i18n.headerExportCSVTitle());
		typeMenu.add(menuItem);

		menuItem = new AriaMenuItem(i18n.headerExportXLS(), menuSelectionListener);
		menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.EXPORT_STRUCTURE_XLS);
		menuItem.setTitle(i18n.headerExportXLSTitle());
		typeMenu.add(menuItem);
		

		if (isEditable) {
			menuItem = new AriaMenuItem(i18n.headerImport(), menuSelectionListener);
			menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.IMPORT);
			menuItem.setIconStyle(resources.css().gbImportItemIcon());
			menuItem.setTitle(i18n.headerImportTitle());
			moreActionsMenu.add(menuItem);


			moreActionsMenu.add(new SeparatorMenuItem());

			// GRBK-37 : TPA
			menuItem = new AriaMenuItem(i18n.headerFinalGrade(), menuSelectionListener);
			menuItem.setData(MENU_SELECTOR_FLAG, MenuSelector.FINAL_GRADE);
			menuItem.setIconStyle(resources.css().gbExportItemIcon());
			menuItem.setTitle(i18n.headerFinalGradeTitle());
			moreActionsMenu.add(menuItem);
		}

		return moreActionsMenu;
	}

}
