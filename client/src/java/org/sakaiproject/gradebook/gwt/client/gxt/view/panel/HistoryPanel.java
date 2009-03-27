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
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.action.Action;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.gxt.GridPanel;
import org.sakaiproject.gradebook.gwt.client.gxt.custom.widget.grid.CustomColumnModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

@SuppressWarnings("unchecked")
public class HistoryPanel extends GridPanel<UserEntityAction> {

	public HistoryPanel() {
		super(AppConstants.HISTORY, EntityType.ACTION);
		setFrame(false);
		setHeaderVisible(false);
		setLayout(new FitLayout());
		
		GradebookModel selectedGradebook = Registry.get(AppConstants.CURRENT);
		//add(newGrid(newColumnModel(selectedGradebook)));
	
		
		createExpander();
	
	}
	
	 private void createExpander() {
		/*List<BaseModel> stocks = new ArrayList<BaseModel>();
		BaseModel model = new BaseModel();
		model.set("name", "International Business Machines");
		stocks.add(model);
		
		model = new BaseModel();
		model.set("name", "Cisco");
		stocks.add(model);
		
		for (BaseModel s : stocks) {
			s.set(
							"desc",
							"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed metus nibh, sodales a, porta at, vulputate eget, dui. Pellentesque ut nisl. Maecenas tortor turpis, interdum non, sodales non, iaculis ac, lacus. Vestibulum auctor, tortor quis iaculis malesuada, libero lectus bibendum purus, sit amet tincidunt quam turpis vel lacus. In pellentesque nisl non sem. Suspendisse nunc sem, pretium eget, cursus a, fringilla vel, urna.<br/><br/>Aliquam commodo ullamcorper erat. Nullam vel justo in neque porttitor laoreet. Aenean lacus dui, consequat eu, adipiscing eget, nonummy non, nisi. Morbi nunc est, dignissim non, ornare sed, luctus eu, massa. Vivamus eget quam. Vivamus tincidunt diam nec urna. Curabitur velit.");
		}*/

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		XTemplate tpl = XTemplate.create("<p><b>Type:</b> {ENTITY_TYPE}</p>");

		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);

		configs.add(expander);

		ColumnConfig column = new ColumnConfig();
		column.setId(Action.Key.ENTITY_NAME.name());
		column.setHeader("Name");
		column.setWidth(200);
		configs.add(column);

		
		loader = newLoader();
		ListStore<BaseModel> store = new ListStore<BaseModel>(loader);
		//store.add(stocks);

		ColumnModel cm = new ColumnModel(configs);

		Grid<BaseModel> grid = new Grid<BaseModel>(store, cm);
		grid.addPlugin(expander);
		grid.getView().setForceFit(true);
		add(grid);

	} 
	
	@Override
	protected void addComponents() {
		setBottomComponent(pagingToolBar);
	}
	
	@Override
	protected Grid<UserEntityAction> newGrid(CustomColumnModel cm) {
		ComponentPlugin plugin = (ComponentPlugin)cm.getColumn(0);
		Grid<UserEntityAction> grid = super.newGrid(cm);
		grid.addPlugin(plugin);
		grid.getView().setForceFit(true); 
		//grid.setAutoExpandColumn(Action.Key.DESCRIPTION.name());
		return grid;
	}
	
	@Override
	protected CustomColumnModel newColumnModel(GradebookModel selectedGradebook) {
		
		String gradebookUid = selectedGradebook.getGradebookUid();
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		String html = new StringBuilder()
			.append("<p><b>Desc:</b> {").append(Action.Key.DESCRIPTION.name()).append("}</p>")
			.append("<p><b>Name:</b> {").append(ItemModel.Key.NAME.name()).append("}</p>")
			//.append("<tpl for=\"itemModel\" if=\"{ITEM_TYPE}==\'ITEM\'\"><p>{NAME}</p></tpl>'")
			.toString();
		
		XTemplate tpl = XTemplate.create(html);

		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);

		configs.add(expander); 
		
		ColumnConfig datePerformed = new ColumnConfig(Action.Key.DATE_PERFORMED.name(),
				"Timestamp", 200);
		datePerformed.setDateTimeFormat(DateTimeFormat.getMediumDateTimeFormat());
		datePerformed.setHidden(true);
		configs.add(datePerformed);
		
		ColumnConfig dateRecorded = new ColumnConfig(Action.Key.DATE_RECORDED.name(),
				"Time Recorded", 200);
		dateRecorded.setDateTimeFormat(DateTimeFormat.getMediumDateTimeFormat());
		dateRecorded.setHidden(false);
		configs.add(dateRecorded);
		
		ColumnConfig entityType = new ColumnConfig(Action.Key.ENTITY_TYPE.name(),
				"Type", 120);
		configs.add(entityType);
		
		/*
		ColumnConfig entityName = new ColumnConfig(Action.Key.ENTITY_NAME.name(),
				"Item", 200);
		configs.add(entityName);
		
		ColumnConfig description = new ColumnConfig(Action.Key.DESCRIPTION.name(),
				"Description", 230);
		
		configs.add(description);
		*/
		ColumnConfig graderName = new ColumnConfig(Action.Key.GRADER_NAME.name(),
				"Grader", 120);
		
		configs.add(graderName);
		
		CustomColumnModel cm = new CustomColumnModel(gradebookUid, gridId, configs);
		
		return cm;
	}
	
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
		loader.load(0, pageSize);
	}

}
