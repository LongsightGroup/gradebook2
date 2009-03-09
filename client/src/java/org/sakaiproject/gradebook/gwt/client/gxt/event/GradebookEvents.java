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
package org.sakaiproject.gradebook.gwt.client.gxt.event;


public class GradebookEvents {

	protected GradebookEvents() {
		
	}
	
	public static final int BrowseLearner 				= 14001;
	public static final int Confirmation				= 14002;
	public static final int CloseNotification			= 14003;
	public static final int DeleteItem					= 14004;
	public static final int ExpandEastPanel 			= 14015;
	public static final int ItemUpdated					= 14016;
	public static final int LearnerGradeRecordUpdated	= 14017; 
	public static final int LoadItemTreeModel 			= 14020;
	public static final int NewCategory					= 14025;
	public static final int NewItem						= 14030;
	public static final int Notification				= 14031;
	public static final int RevertItem					= 14034;
	public static final int SelectItem					= 14035;
	public static final int ShowColumns					= 14040;
	public static final int SingleGrade					= 14045;
	public static final int SingleView 					= 14050;
	public static final int StartEditItem				= 14051;
	public static final int StartExport					= 14052;
	public static final int StartImport					= 14053;
	public static final int Startup 					= 14055;
	public static final int HideEastPanel				= 14060;
	public static final int SwitchGradebook 			= 14065;
	public static final int UpdateLearnerGradeRecord	= 14070;
	public static final int UpdateItem					= 14075;
	
	public static final int Exception 				= 14100;
	
	
	public static final int EqualWeight = 12000;
	
	public static final int DoSearch = 12001;
	
	public static final int ClearSearch = 12002;
	
	public static final int RefreshCourseGrades = 12003;
	
	//public static final int SingleView = 12004;
	
	public static final int GradebookChange = 12005;
	
	public static final int UserChange = 12009;
	
	public static final int Refresh = 12010;
	
}
