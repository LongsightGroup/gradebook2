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
package org.sakaiproject.gradebook.gwt.client.gxt.model;

import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.model.key.GradeEventKey;

public class GradeEventModel extends EntityModel {

	private static final long serialVersionUID = 1L;

	public GradeEventModel() {
		super();
	}
	
	public GradeEventModel(Map<String, Object> properties) {
		super(properties);
	}
	
	public String getIdentifier() {
		return get(GradeEventKey.ID.name());
	}
	
	public void setIdentifier(String id) {
		set(GradeEventKey.ID.name(), id);
	}
	
	public String getGraderName() {
		return get(GradeEventKey.GRADER_NAME.name());
	}
	
	public void setGraderName(String graderName) {
		set(GradeEventKey.GRADER_NAME.name(), graderName);
	}
	
	public String getGrade() {
		return get(GradeEventKey.GRADE.name());
	}
	
	public void setGrade(String grade) {
		set(GradeEventKey.GRADE.name(), grade);
	}
	
	public String getDateGraded() {
		return get(GradeEventKey.DATE_GRADED.name());
	}
	
	public void setDateGraded(String dateGraded) {
		set(GradeEventKey.DATE_GRADED.name(), dateGraded);
	}

	@Override
	public String getDisplayName() {
		return getGrade();
	}

	
}
