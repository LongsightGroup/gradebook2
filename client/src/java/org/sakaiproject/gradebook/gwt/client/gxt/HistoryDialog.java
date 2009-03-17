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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.action.Action;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.gxt.custom.widget.grid.CustomColumnModel;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.UserChangeEvent;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

public class HistoryDialog extends ContentPanel {

	private GridPanel<UserEntityAction> gridPanel;
	
	public HistoryDialog() {
		super();
		setFrame(false);
		setHeaderVisible(false);
		setLayout(new FitLayout());
	/*}
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);
	*/
		//setSize(XDOM.getViewportSize().width - 40, XDOM.getViewportSize().height - 200);
		
		gridPanel = 
			new GridPanel<UserEntityAction>("history", EntityType.ACTION) {

			@Override
			protected void addComponents() {
				setTopComponent(pagingToolBar);
			}
			
			@Override
			protected Grid<UserEntityAction> newGrid(CustomColumnModel cm) {
				Grid<UserEntityAction> grid = super.newGrid(cm);
				grid.setAutoExpandColumn(Action.Key.DESCRIPTION.name());
				return grid;
			}
			
			@Override
			protected CustomColumnModel newColumnModel(GradebookModel selectedGradebook) {
				GradebookModel selectedGradebookModel = Registry.get(AppConstants.CURRENT);
				String gradebookUid = selectedGradebookModel.getGradebookUid();
				
				List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
				
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
				
				ColumnConfig entityName = new ColumnConfig(Action.Key.ENTITY_NAME.name(),
						"Item", 200);
				configs.add(entityName);
				
				ColumnConfig description = new ColumnConfig(Action.Key.DESCRIPTION.name(),
						"Description", 230);
				
				configs.add(description);
				
				ColumnConfig graderName = new ColumnConfig(Action.Key.GRADER_NAME.name(),
						"Grader", 120);
				
				configs.add(graderName);
				
				CustomColumnModel cm = new CustomColumnModel(gradebookUid, gridId, configs);
				
				return cm;
			}
			
		};
		
		
		add(gridPanel);
		
		addListener(GradebookEvents.UserChange, new Listener<UserChangeEvent>() {

			public void handleEvent(UserChangeEvent uce) {
				UserEntityAction action = uce.getAction();
				action.setDescription(action.toString());
				
				gridPanel.getStore().insert(action, 0);
			}
			
		});
	}
	
	protected void onRender(Element parent, int pos) {
		super.onRender(parent, pos);		
		
		gridPanel.loader.load(0, gridPanel.pageSize);
	}	

}
