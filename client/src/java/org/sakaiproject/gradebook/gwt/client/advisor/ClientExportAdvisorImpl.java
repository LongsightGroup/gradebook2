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

package org.sakaiproject.gradebook.gwt.client.advisor;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.api.ClientExportAdvisor;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.Info;

// GRBK-417
public class ClientExportAdvisorImpl implements ClientExportAdvisor {

private I18nConstants i18n;
	
	
	public ClientExportAdvisorImpl() {
		
		i18n = Registry.get(AppConstants.I18N);
	}
	
	public void handleServerResponse(String responseText) {
		
		if(null == responseText || "".equals(responseText)) {
			
			Info.display(i18n.finalGradeSubmissionTitle(), i18n.finalGradeSubmissionMessageText8a());
		}
		else {
			
			Info.display(i18n.finalGradeSubmissionTitle(), i18n.finalGradeSubmissionMessageText4a());
			com.google.gwt.user.client.Window.open(responseText, "_blank","status=0,toolbar=0,menubar=0,location=0,scrollbars=1,resizable=1");
		}
	}

}
