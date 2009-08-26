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
import org.sakaiproject.gradebook.gwt.client.Gradebook2RPCServiceAsync;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.SecureToken;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.gxt.a11y.AriaButton;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.model.EntityModelComparer;
import org.sakaiproject.gradebook.gwt.client.model.GradeScaleRecordModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.StatisticsModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class StatisticsPanel extends ContentPanel {

	private ListLoader loader;

	public StatisticsPanel(I18nConstants i18n) {
		super();

		setHeading("Statistics");
		setFrame(true);
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig();  
		column.setId(StatisticsModel.Key.NAME.name());  
		column.setHeader(StatisticsModel.Key.NAME.getPropertyName());
		column.setWidth(200);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		column.setSortable(false);
		configs.add(column); 

		column = new ColumnConfig();  
		column.setId(StatisticsModel.Key.MEAN.name());  
		column.setHeader(StatisticsModel.Key.MEAN.getPropertyName());
		column.setWidth(80);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		column.setSortable(false);
		configs.add(column);

		column = new ColumnConfig();  
		column.setId(StatisticsModel.Key.STANDARD_DEVIATION.name());  
		column.setHeader(StatisticsModel.Key.STANDARD_DEVIATION.getPropertyName());
		column.setWidth(80);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		column.setSortable(false);
		configs.add(column);

		column = new ColumnConfig();  
		column.setId(StatisticsModel.Key.MEDIAN.name());  
		column.setHeader(StatisticsModel.Key.MEDIAN.getPropertyName());
		column.setWidth(80);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		column.setSortable(false);
		configs.add(column);

		column = new ColumnConfig();  
		column.setId(StatisticsModel.Key.MODE.name());  
		column.setHeader(StatisticsModel.Key.MODE.getPropertyName());
		column.setWidth(160);
		column.setGroupable(false);
		column.setMenuDisabled(true);
		column.setSortable(false);
		column.setResizable(true); 
		configs.add(column);


		RpcProxy<ListLoadConfig, ListLoadResult<GradeScaleRecordModel>> proxy = new RpcProxy<ListLoadConfig, ListLoadResult<GradeScaleRecordModel>>() {

			@Override
			protected void load(ListLoadConfig listLoadConfig, AsyncCallback<ListLoadResult<GradeScaleRecordModel>> callback) {
				GradebookModel gbModel = Registry.get(AppConstants.CURRENT);
				Gradebook2RPCServiceAsync service = Registry.get("service");
				service.getPage(gbModel.getGradebookUid(), gbModel.getGradebookId(), EntityType.STATISTICS, null, SecureToken.get(), callback);
			}

		};

		loader = new BaseListLoader(proxy);  


		final ListStore<StatisticsModel> store = new ListStore<StatisticsModel>(loader);
		store.setModelComparer(new EntityModelComparer<StatisticsModel>());

		//loader.load();  

		final ColumnModel cm = new ColumnModel(configs);


		setBodyBorder(true);
		setButtonAlign(HorizontalAlignment.RIGHT);
		setLayout(new FitLayout());
		//setSize(600, 300);

		final Grid<StatisticsModel> grid = new Grid<StatisticsModel>(store, cm);  
		grid.setStyleAttribute("borderTop", "none");   
		grid.setBorders(true);

		add(grid); 

		Button button = new AriaButton(i18n.close(), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent be) {
				Dispatcher.forwardEvent(GradebookEvents.StopStatistics.getEventType(), Boolean.FALSE);
			}

		});
		addButton(button);
	}

	public void onLearnerGradeRecordUpdated(StudentModel learner) {
		loader.load();
	}
}
