/**********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 The Regents of the University of California
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

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.resource.GradebookResources;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.ColumnChart;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;


public class StatisticsChartPanel extends ContentPanel {

	private final I18nConstants i18n;
	private GradebookResources resources;
	
	private Image columnChartIcon;
	private Image pieChartIcon;
	private Image lineChartIcon;
	
	private HorizontalPanel graphPanel;
	private HorizontalPanel chartIconPanel;
	
	private DataTable dataTable;
	
	private LegendPosition legendPosition = LegendPosition.RIGHT;
	
	private int chartWidth = AppConstants.CHART_WIDTH;
	private int chartHeight = AppConstants.CHART_HEIGHT;
	
	private StatisticsChartLoaderListener statisticsChartLoaderListener;
	
	// This is used by the various panels that use chars
	private final static String[] RANGE = new String[] {
		"0-9",
		"10-19",
		"20-29",
		"30-39",
		"40-49",
		"50-59",
		"60-69",
		"70-79",
		"80-89",
		"90-100"};
	
	private boolean hasActiveNotifications = false;
	
	private boolean isVisualizationApiLoaded = false;

	public StatisticsChartPanel() { 
	
		this(null);
	}
	
	public StatisticsChartPanel(StatisticsChartLoaderListener statisticsChartLoaderListener) {
		
		this.statisticsChartLoaderListener = statisticsChartLoaderListener;
		
		// Loading visualization APIs
		VisualizationUtils.loadVisualizationApi(new VisualizationRunnable(), CoreChart.PACKAGE);
		
		this.i18n = Registry.get(AppConstants.I18N);
		this.resources = Registry.get(AppConstants.RESOURCES);
		
		setFrame(true);
		setBodyBorder(true);
		setTitle(i18n.statisticsChartTitle());
		setHeading(i18n.statisticsChartTitle());
		
		graphPanel = new HorizontalPanel();
		add(graphPanel);
		
		// Create the image icons
		columnChartIcon = new Image(resources.chart_bar());
		pieChartIcon = new Image(resources.chart_pie());
		lineChartIcon = new Image(resources.chart_line());

		columnChartIcon.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				graphPanel.removeAll();
				graphPanel.add(new ColumnChart(dataTable, createColumnChartOptions()));
				graphPanel.layout();
			}
		});

		lineChartIcon.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				graphPanel.removeAll();
				graphPanel.add(new LineChart(dataTable, createLineChartOptions()));
				graphPanel.layout();
			}
		});

		pieChartIcon.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				graphPanel.removeAll();
				graphPanel.add(new PieChart(dataTable, createPieChartOptions()));
				graphPanel.layout();
			}
		});

		chartIconPanel = new HorizontalPanel();
		chartIconPanel.setSpacing(15);
		chartIconPanel.add(columnChartIcon);
		chartIconPanel.add(pieChartIcon);
		chartIconPanel.add(lineChartIcon);
		add(chartIconPanel);
		
		layout();
	}
	
	@Override
	public void show() {
		
		// Before we instantiate a graph, we check
		// if the Visualization APIs have been loaded properly
		if(!isVisualizationApiLoaded) {
		
			Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.statisticsDataErrorTitle(), i18n.statisticsVisualizationErrorMsg(), true));
			hide();
			return;
		}
			
		if(null != dataTable) {
			
			super.show();
			graphPanel.removeAll();
			graphPanel.add(new ColumnChart(dataTable, createColumnChartOptions()));
			graphPanel.layout();
			
		}
		else {
			
			Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), new NotificationEvent(i18n.statisticsDataErrorTitle(), i18n.statisticsDataErrorMsg(), true));
		}
	}
	
	public void setDataTable(DataTable dataTable) {
		
		this.dataTable = dataTable;
	}
	
	public void setLegendPosition(LegendPosition legendPosition) {
		
		this.legendPosition = legendPosition;
	}
	
	public void setChartHeight(int height) {
		
		this.chartHeight = height;
	}
	
	public void setChartWidth(int width) {
		
		this.chartWidth = width;
	}
	
	public String[] getXAxisRangeLabels() {
		
		return RANGE;
	}
	
	/*
	 * If no additional condition needs to be checked, pass "null"
	 * as the function argument
	 */
	public void showUserFeedback(Boolean additionalCondition) {

		/*
		 * If additionalCondition is not null, we use its boolean value.
		 * If additionalCondition is null we use true so that we don't alter the boolean logic
		 * X && TRUE = X
		 */
		boolean condition = (null != additionalCondition ? additionalCondition.booleanValue() : true);
		
		if(!hasActiveNotifications && condition) {
		
			Dispatcher.forwardEvent(GradebookEvents.ShowUserFeedback.getEventType(), i18n.statisticsGradebookLoadingChart(), false);
			mask();
			hasActiveNotifications = true;
		}
	}
	
	/*
	 * If no additional condition needs to be checked, pass "null"
	 * as the function argument
	 */
	public void hideUserFeedback(Boolean additionalCondition) {
		
		/*
		 * If additionalCondition is not null, we use its boolean value.
		 * If additionalCondition is null we use true so that we don't alter the boolean logic
		 * X && TRUE = X
		 */
		boolean condition = (null != additionalCondition ? additionalCondition.booleanValue() : true);
		
		if(hasActiveNotifications && condition) {
		
			Dispatcher.forwardEvent(GradebookEvents.HideUserFeedback.getEventType(), false);
			unmask();
			hasActiveNotifications = false;
		}
	}
	
	/*
	 * Creates a DataTabel, keeps a reference to it,
	 * and returns it
	 */
	public DataTable createDataTable() {
		
		dataTable = DataTable.create();
		return dataTable;
	}
	
	private PieChart.PieOptions createPieChartOptions() {
		
		PieChart.PieOptions options = PieChart.createPieOptions();
		options.setWidth(chartWidth);
		options.setHeight(chartHeight);
		options.set3D(AppConstants.IS_CHART_3D);
		options.setLegend(legendPosition);
		return options;
	}

	private Options createColumnChartOptions() {
		
		Options options = Options.create();
		options.setWidth(chartWidth);
		options.setHeight(chartHeight);
		options.setLegend(legendPosition);
		return options;
	}

	private Options createLineChartOptions() {
		
		Options options = Options.create();
		options.setWidth(chartWidth);
		options.setHeight(chartHeight);
		options.setLegend(legendPosition);
		return options;
	}
	
	/*
	 * An instance of this runnable class is called once the
	 * Visualization APIs have been loaded via
	 * VisualizationUtils.loadVisualizationApi(...)
	 */
	private class VisualizationRunnable implements Runnable {

		public void run() {
			
			isVisualizationApiLoaded = true;
			
			if(null != statisticsChartLoaderListener) {
			
				statisticsChartLoaderListener.load();
			}
		}
	}
}
