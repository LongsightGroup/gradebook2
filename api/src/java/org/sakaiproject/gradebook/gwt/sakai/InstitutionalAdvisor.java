package org.sakaiproject.gradebook.gwt.sakai;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingScale;

public interface InstitutionalAdvisor {

	public enum Column { STUDENT_NAME, STUDENT_GRADE, EXPORT_USER_ID, EXPORT_CM_ID, FINAL_GRADE_USER_ID };
	
	/**
	 * 
	 * @param group : The Authz Group
	 * @return List of enrollment set eids
	 */
	public List<String> getExportCourseManagementSetEids(Group group);
	
	/**
	 * 
	 * 
	 * @param userEid : The user's EID
	 * @param group : The user's Authz Group
	 * 
	 * @return the export course management id 
	 */
	public String getExportCourseManagementId(String userEid, Group group, List<String> enrollmentSetEids);
	
	/**
	 * 
	 * 
	 * @param dereference : UserDereference, a representation of user data
	 * @return the id to be used for this individual on both import/export
	 */
	public String getExportUserId(UserDereference dereference);
	
	/**
	 * @param dereference : UserDereference, a representation of user data
	 */
	public String getFinalGradeUserId(UserDereference dereference);
	
	/**
	 * Method to retrieve the array of role keys for valid learners 
	 * 
	 * @return array of Long objects representing the actual Sakai role keys
	 */
	public String[] getLearnerRoleNames();

	/**
	 * Method to determine if the institution considers this user/member of site a "learner"
	 * for the purposes of grading
	 * 
	 * @param member
	 * @return true if user is a learner, false otherwise
	 */
	public boolean isLearner(Member member);

	public boolean isExportCourseManagementIdByGroup();
	
	public boolean isValidOverrideGrade(String grade, String learnerEid, String learnerDisplayId, Gradebook gradebook, GradingScale gradingScale);
	
	
	/**
	 * Method to submit final grades to the SIS.
	 * 
	 * @param studentDataList : a list of Map objects containing all the student data properties
	 * @param gradebookUid : a String identifier for this gradebook
	 * @param request : the current HttpServletRequest 
	 * @param response : the current HttpServletResponse
	 */
	public void submitFinalGrade(List<Map<Column,String>> studentDataList, String gradebookUid, HttpServletRequest request, HttpServletResponse response);

	
}
