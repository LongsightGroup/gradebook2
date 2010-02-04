package org.sakaiproject.gradebook.gwt.sakai;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;
import org.sakaiproject.gradebook.gwt.client.model.Item;
import org.sakaiproject.gradebook.gwt.client.model.Learner;
import org.sakaiproject.gradebook.gwt.client.model.Roster;
import org.sakaiproject.gradebook.gwt.client.model.Statistics;
import org.sakaiproject.gradebook.gwt.sakai.InstitutionalAdvisor.Column;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.site.api.Group;

public interface Gradebook2ComponentService {

	public Learner assignComment(String assignmentId, String studentUid, String text);
	
	public Learner assignScore(String gradebookUid, String studentUid, String assignmentId, Double value, Double previousValue) throws InvalidInputException;
	
	public Learner assignScore(String gradebookUid, String studentUid, String assignmentId, String value, String previousValue) throws InvalidInputException;
	
	public Item createItem(String gradebookUid, Long gradebookId, Item item, boolean enforceNoNewCategories) throws InvalidInputException;
	
	public Map<String, Object> createPermission(String gradebookUid, Long gradebookId, Map<String, Object> attributes) throws InvalidInputException;
	
	public Map<String, Object> deletePermission(Map<String, Object> attributes);
	
	public List<UserDereference> findAllUserDereferences();
	
	public List<String> getExportCourseManagementSetEids(Group group);

	public String getExportCourseManagementId(String userEid, Group group, List<String> enrollmentSetEids);

	public String getExportUserId(UserDereference dereference);

	public String getFinalGradeUserId(UserDereference dereference);
	
	public Map<String, Object> getApplicationMap(String... gradebookUids);
	
	public String getAuthorizationDetails(String... gradebookUids);
	
	public List<Map<String,Object>> getAvailableGradeFormats(String gradebookUid, Long gradebookId);
	
	public Gradebook getGradebook(String uid);
	
	public List<Map<String,Object>> getGradeEvents(Long assignmentId, String studentUid);
	
	public List<Map<String,Object>> getGradeMaps(String gradebookUid);
	
	public List<Map<String, Object>> getGraders(String gradebookUid, Long gradebookId);
	
	public Map<String,Object> getGradesVerification(String gradebookUid, Long gradebookId);
	
	public List<Map<String,Object>> getHistory(String gradebookUid, Long gradebookId, Integer offset, Integer limit);
	
	public Map<String,Object> getItem(String gradebookUid, Long gradebookId, String type);
	
	public List<Map<String,Object>> getItems(String gradebookUid, Long gradebookId, String type);
	
	public Roster getRoster(String gradebookUid, Long gradebookId, Integer limit, Integer offset, String sectionUuid, String searchString, String sortField, boolean includeCMId, boolean isDescending);
	
	public List<Map<String,Object>> getPermissions(String gradebookUid, Long gradebookId, String graderId);
	
	public List<Statistics> getStatistics(String gradebookUid, Long gradebookId, String studentId);
	
	public List<Map<String,Object>> getVisibleSections(String gradebookUid, boolean enableAllSectionsEntry, String allSectionsEntryTitle);
	
	public void postEvent(String message, String gradebookId, String... args);
	
	public void resetGradeMap(String gradebookUid);
	
	public void submitFinalGrade(List<Map<Column, String>> studentDataList, String gradebookUid, HttpServletRequest request, HttpServletResponse response);
	
	public Boolean updateConfiguration(Long gradebookId, String field, String value);
	
	public void updateGradeMap(String gradebookUid, String affectedLetterGrade, Object value) throws InvalidInputException;
	
	public Item updateItem(Item item) throws InvalidInputException;

	public Map<String, Object> upload(String gradebookUid, Long gradebookId, Map<String, Object> attributes) throws InvalidInputException;
	
}
