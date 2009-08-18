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
package org.sakaiproject.gradebook.gwt.client.model;

import java.util.Map;

import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction.ClassType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.GradeType;

public class StudentModel extends EntityModel implements Comparable<StudentModel> {
	
	public static final String COMMENTED_FLAG = ":C";
	public static final String COMMENT_TEXT_FLAG = ":T";
	public static final String DROP_FLAG = ":D";
	public static final String EXCUSE_FLAG = ":E";
	public static final String FAILED_FLAG = ":F";
	public static final String GRADED_FLAG = ":G";
	
	
	public enum Group {
		STUDENT_INFORMATION("Student Information"),
		GRADES("Grades"), 
		ASSIGNMENTS("Assignments");
		
		private String displayName;
		
		private Group(String displayName) {
			this.displayName = displayName;
		}
		
		public String getDisplayName() {
			return displayName;
		}
	};
	
	public enum Key { 
		UID(Group.STUDENT_INFORMATION, ""), 
		EID(Group.STUDENT_INFORMATION, ""),
		DISPLAY_ID(Group.STUDENT_INFORMATION, "Id"), 
		DISPLAY_NAME(Group.STUDENT_INFORMATION, "Display Name"), 
		LAST_NAME_FIRST(Group.STUDENT_INFORMATION, "Last Name, First"),
		EMAIL(Group.STUDENT_INFORMATION, "Email"), 
		SECTION(Group.STUDENT_INFORMATION, "Section"), 
		COURSE_GRADE(Group.GRADES, "Course Grade"), 
		LETTER_GRADE(Group.GRADES, "Letter Grade"),
		CALCULATED_GRADE(Group.GRADES, "Calculated Grade"),
		GRADE_OVERRIDE(Group.GRADES, "Grade Override"), 
		ASSIGNMENT(Group.ASSIGNMENTS, ""),
		EXPORT_CM_ID(Group.STUDENT_INFORMATION, "Export CM Id"),
		EXPORT_USER_ID(Group.STUDENT_INFORMATION, "Export User Id"),
		FINAL_GRADE_USER_ID(Group.STUDENT_INFORMATION, "Final Grade User Id");

	
		private Group group;
		private String displayName;
	
		private Key(Group group, String displayName) {
			this.group = group;
			this.displayName = displayName;
		}
	
		public Group getGroup() {
			return group;
		}
		
		public String getDisplayName() {
			return displayName;
		}
	
	};
	
	private static final long serialVersionUID = 1L;

	public StudentModel() {
		super();
	}
	
	public StudentModel(Map<String, Object> properties) {
		super(properties);
	}
	
	public static ClassType lookupClassType(String property, GradeType gradeType) {
		
		if (property.equals(Key.GRADE_OVERRIDE.name()))
			return ClassType.STRING;
		
		if (property.endsWith(COMMENT_TEXT_FLAG))
			return ClassType.STRING;
		
		if (property.endsWith(EXCUSE_FLAG))
			return ClassType.BOOLEAN;
		
		if (gradeType == GradeType.LETTERS)
			return ClassType.STRING;
		
		return ClassType.DOUBLE;
	}
	
	public String getIdentifier() {
		return get(Key.UID.name());
	}

	public void setIdentifier(String id) {
		set(Key.UID.name(), id);
	}
	
	public String getEid() {
		return get(Key.EID.name());
	}
	
	public void setEid(String eid) {
		set(Key.EID.name(), eid);
	}
	
	public String getDisplayName() {
		return get(Key.DISPLAY_NAME.name());
	}
	
	public String getLastNameFirst() {
		return get(Key.LAST_NAME_FIRST.name());
	}
	
	public void setLastNameFirst(String name) {
		set(Key.LAST_NAME_FIRST.name(), name);
	}
	
	public String getStudentName()
	{
		return get(Key.DISPLAY_NAME.name());
	}
	
	public void setStudentName(String studentName)
	{
		set(Key.DISPLAY_NAME.name(), studentName);
	}

	public String getStudentDisplayId()
	{
		return get(Key.DISPLAY_ID.name());
	}
	
	public void setStudentDisplayId(String studentDisplayId)
	{
		set(Key.DISPLAY_ID.name(), studentDisplayId);
	}
	
	public String getStudentEmail()
	{
		return get(Key.EMAIL.name());
	}
	
	public void setStudentEmail(String studentEmail)
	{
		set(Key.EMAIL.name(), studentEmail);
	}

	public String getStudentSections()
	{
		return get(Key.SECTION.name());
	}
	
	public void setStudentSections(String studentSections)
	{
		set(Key.SECTION.name(), studentSections);
	}

	public String getStudentGrade()
	{
		return get(Key.COURSE_GRADE.name());
	}
	
	public void setStudentGrade(String studentGrade)
	{
		set(Key.COURSE_GRADE.name(), studentGrade);
	}
	
	public String getExportCmId()
	{
		return get(Key.EXPORT_CM_ID.name());
	}
	
	public void setExportCmId(String exportCmId)
	{
		set(Key.EXPORT_CM_ID.name(), exportCmId);
	}
	
	public String getExportUserId()
	{
		return get(Key.EXPORT_USER_ID.name());
	}
	
	public void setExportUserId(String exportUserId)
	{
		set(Key.EXPORT_USER_ID.name(), exportUserId);
	}
	
	public String getFinalGradeUserId() {
		return get(Key.FINAL_GRADE_USER_ID.name());
	}
	
	public void setFinalGradeUserId(String finalGradeUserId) {
		set(Key.FINAL_GRADE_USER_ID.name(), finalGradeUserId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StudentModel) {
			StudentModel other = (StudentModel)obj;
		
			return getIdentifier().equals(other.getIdentifier());
		}
		return false;
	}

	public int compareTo(StudentModel o) {
		return getIdentifier().compareTo(o.getIdentifier());
	}
	
}
