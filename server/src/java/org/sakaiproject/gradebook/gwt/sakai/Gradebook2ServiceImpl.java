package org.sakaiproject.gradebook.gwt.sakai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityCreateAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityGradeAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.action.Action.ActionType;
import org.sakaiproject.gradebook.gwt.client.action.Action.EntityType;
import org.sakaiproject.gradebook.gwt.client.exceptions.BusinessRuleException;
import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.gxt.multigrade.MultiGradeLoadConfig;
import org.sakaiproject.gradebook.gwt.client.model.ApplicationModel;
import org.sakaiproject.gradebook.gwt.client.model.AuthModel;
import org.sakaiproject.gradebook.gwt.client.model.CategoryModel;
import org.sakaiproject.gradebook.gwt.client.model.CommentModel;
import org.sakaiproject.gradebook.gwt.client.model.ConfigurationModel;
import org.sakaiproject.gradebook.gwt.client.model.FixedColumnModel;
import org.sakaiproject.gradebook.gwt.client.model.GradeEventModel;
import org.sakaiproject.gradebook.gwt.client.model.GradeFormatModel;
import org.sakaiproject.gradebook.gwt.client.model.GradeScaleRecordModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel;
import org.sakaiproject.gradebook.gwt.client.model.PermissionEntryListModel;
import org.sakaiproject.gradebook.gwt.client.model.PermissionEntryModel;
import org.sakaiproject.gradebook.gwt.client.model.SectionModel;
import org.sakaiproject.gradebook.gwt.client.model.SpreadsheetModel;
import org.sakaiproject.gradebook.gwt.client.model.StatisticsModel;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.client.model.SubmissionVerificationModel;
import org.sakaiproject.gradebook.gwt.client.model.UserModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.CategoryType;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel.GradeType;
import org.sakaiproject.gradebook.gwt.client.model.ItemModel.Type;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel.Key;
import org.sakaiproject.gradebook.gwt.sakai.InstitutionalAdvisor.Column;
import org.sakaiproject.gradebook.gwt.sakai.mock.SiteMock;
import org.sakaiproject.gradebook.gwt.sakai.model.ActionRecord;
import org.sakaiproject.gradebook.gwt.sakai.model.GradeStatistics;
import org.sakaiproject.gradebook.gwt.sakai.model.UserConfiguration;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereferenceRealmUpdate;
import org.sakaiproject.gradebook.gwt.server.DataTypeConversionUtil;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingScale;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class Gradebook2ServiceImpl implements Gradebook2Service {

	private static final Log log = LogFactory.getLog(Gradebook2ServiceImpl.class);

	private BusinessLogic businessLogic;
	private GradebookFrameworkService frameworkService;
	private GradebookToolService gbService;
	private GradeCalculations gradeCalculations;
	private Gradebook2Authz authz;
	private SectionAwareness sectionAwareness;
	private InstitutionalAdvisor advisor;
	private SiteService siteService;
	private ToolManager toolManager;
	private UserDirectoryService userService;
	private SessionManager sessionManager;
	private ServerConfigurationService configService;

	/**
	 * Method to add a new grade item to the gradebook.
	 * 
	 * Business rules: (1) If points is null, set points to 100 (2) If weight is
	 * null, set weight to be equivalent to points value -- needs to happen
	 * after #1
	 * 
	 * - When category type is "No Categories": (3) new item name must not
	 * duplicate an active (removed = false) item name in gradebook, otherwise
	 * throw exception (NoDuplicateItemNamesRule)
	 * 
	 * - When category type is "Categories" or "Weighted Categories" (4) new
	 * item name must not duplicate an active (removed = false) item name in the
	 * same category, otherwise throw exception (5) if item is "included" and
	 * category has "equal weighting" then recalculate all item weights for this
	 * category (6) item must include a valid category id
	 * 
	 * @param gradebookUid
	 * @param gradebookId
	 * @param item
	 * @return ItemModel representing either (a) the gradebook, (b) the item's
	 *         category, or (c) the item
	 * @throws InvalidInputException
	 */
	public ItemModel createItem(String gradebookUid, Long gradebookId, final ItemModel item, boolean enforceNoNewCategories) throws InvalidInputException {

		if (item.getItemType() != null) {
			switch (item.getItemType()) {
				case CATEGORY:
					return addItemCategory(gradebookUid, gradebookId, item);
			}
		}

		Gradebook gradebook = gbService.getGradebook(gradebookUid);
		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;

		Long assignmentId = doCreateItem(gradebook, item, hasCategories, enforceNoNewCategories);

		if (!hasCategories) {
			List<Assignment> assignments = gbService.getAssignments(gradebookId);
			return getItemModel(gradebook, assignments, null, null, assignmentId);
		}

		List<Assignment> assignments = gbService.getAssignments(gradebookId);
		List<Category> categories = getCategoriesWithAssignments(gradebookId, assignments, true);
		return getItemModel(gradebook, assignments, categories, null, assignmentId);
	}

	public ConfigurationModel createOrUpdateConfigurationModel(Long gradebookId, String field, String value) {

		ConfigurationModel model = new ConfigurationModel();

		gbService.createOrUpdateUserConfiguration(getCurrentUser(), gradebookId, field, value);

		model.set(field, value);

		return model;
	}

	/**
	 * Method to add a new category to a gradebook
	 * 
	 * Business rules: (1) if no other categories exist, then make the category
	 * weight 100% (2) new category name must not duplicate an existing category
	 * name
	 * 
	 * @param gradebookUid
	 * @param gradebookId
	 * @param item
	 * @return
	 * @throws BusinessRuleException
	 */
	public ItemModel addItemCategory(String gradebookUid, Long gradebookId, ItemModel item) throws BusinessRuleException {

		ActionRecord actionRecord = new ActionRecord(gradebookUid, gradebookId, EntityType.CATEGORY.name(), ActionType.CREATE.name());
		actionRecord.setEntityName(item.getName());
		Map<String, String> propertyMap = actionRecord.getPropertyMap();

		for (String property : item.getPropertyNames()) {
			String value = String.valueOf(item.get(property));
			if (value != null)
				propertyMap.put(property, value);
		}

		// Category category = null;
		Gradebook gradebook = null;
		List<Assignment> assignments = null;
		List<Category> categories = null;
		Long categoryId = null;

		try {
			String name = item.getName();
			Double weight = item.getPercentCourseGrade();
			Boolean isEqualWeighting = item.getEqualWeightAssignments();
			Boolean isIncluded = item.getIncluded();
			Integer dropLowest = item.getDropLowest();
			Boolean isExtraCredit = item.getExtraCredit();
			Integer categoryOrder = item.getItemOrder();
			
			boolean isUnweighted = !DataTypeConversionUtil.checkBoolean(isIncluded);

			gradebook = gbService.getGradebook(gradebookUid);

			boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
			boolean hasWeights = gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY;

			if (hasCategories) {
				categories = gbService.getCategories(gradebook.getId()); // getCategoriesWithAssignments(gradebook.getId());
				if (categoryOrder == null)
					categoryOrder = categories == null || categories.isEmpty() ? Integer.valueOf(0) : Integer.valueOf(categories.size());
			}
			
			double w = weight == null ? 0d : ((Double)weight).doubleValue() * 0.01;
			
			if (hasCategories) {
				int dropLowestInt = dropLowest == null ? 0 : dropLowest.intValue();
				boolean equalWeighting = isEqualWeighting == null ? false : isEqualWeighting.booleanValue();

				businessLogic.applyNoDuplicateCategoryNamesRule(gradebook.getId(), item.getName(), null, categories);
				if (hasWeights)
					businessLogic.applyOnlyEqualWeightDropLowestRule(dropLowestInt, equalWeighting);
			}

			categoryId = gbService.createCategory(gradebookId, name, Double.valueOf(w), dropLowest, isEqualWeighting, Boolean.valueOf(isUnweighted), isExtraCredit, categoryOrder);

			assignments = gbService.getAssignments(gradebook.getId());
			categories = null;
			if (hasCategories) {
				categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
			}
			
		} catch (RuntimeException e) {
			actionRecord.setStatus(ActionRecord.STATUS_FAILURE);
			throw e;
		} finally {
			gbService.storeActionRecord(actionRecord);
		}

		return getItemModel(gradebook, assignments, categories, categoryId, null);

	}

	public CommentModel createOrUpdateComment(Long assignmentId, String studentUid, String text) {

		Comment comment = gbService.getCommentForItemForStudent(assignmentId, studentUid);

		if (comment == null) {
			Assignment assignment = gbService.getAssignment(assignmentId);
			comment = new Comment(studentUid, text, assignment);
		} else
			comment.setCommentText(text);

		gbService.updateComment(comment);

		return createOrUpdateCommentModel(null, comment);
	}

	public PermissionEntryListModel getPermissionEntryList(Long gradebookId, String learnerId) {

		PermissionEntryListModel permissionEntryListModel = new PermissionEntryListModel();
		List<PermissionEntryModel> permissionEntryModelList = new ArrayList<PermissionEntryModel>();

		List<Permission> permissions = gbService.getPermissionsForUser(gradebookId, learnerId);

		for (Permission permission : permissions) {

			PermissionEntryModel permissionEntryModel = new PermissionEntryModel();
			permissionEntryModel.setId(permission.getId());
			permissionEntryModel.setUserId(permission.getUserId());

			try {

				User user = userService.getUser(permission.getUserId());

				if (null != user) {
					permissionEntryModel.setUserDisplayName(user.getDisplayName());
				} else {
					log.error("Was not able go get an User object from userId = " + permission.getUserId());
				}
			} catch (UserNotDefinedException e) {
				log.error("Was not able go get an User object from userId = " + permission.getUserId());
				e.printStackTrace();
			}

			permissionEntryModel.setPermissionId(permission.getFunction());

			// If category id is null, the all categories were selected
			if (null != permission.getCategoryId()) {

				permissionEntryModel.setCategoryId(permission.getCategoryId());
				Category category = gbService.getCategory(permission.getCategoryId());
				if (null != category) {
					permissionEntryModel.setCategoryDisplayName(category.getName());
				} else {
					// TODO: handle error
				}

			} else {

				permissionEntryModel.setCategoryId(null);
				permissionEntryModel.setCategoryDisplayName("All");
			}

			// If section id is null, then all sections were selected
			if (null != permission.getGroupId()) {

				permissionEntryModel.setSectionId(permission.getGroupId());
				CourseSection courseSection = sectionAwareness.getSection(permission.getGroupId());
				if (null != courseSection) {
					permissionEntryModel.setSectionDisplayName(courseSection.getTitle());
				} else {
					// TODO: handle error
				}

			} else {

				permissionEntryModel.setSectionId(null);
				permissionEntryModel.setSectionDisplayName("All");
			}

			permissionEntryModel.setDeleteAction("Delete");
			permissionEntryModelList.add(permissionEntryModel);
		}

		permissionEntryListModel.setEntries(permissionEntryModelList);
		return permissionEntryListModel;
	}

	public <X extends BaseModel> ListLoadResult<X> getCategoriesNotRemoved(Long gradebookId) {

		List<X> categoryModelList = new ArrayList<X>();
		List<Category> categoryList = gbService.getCategories(gradebookId);

		// Adding all categories entry

		categoryModelList.add((X) new CategoryModel(null, "All Categories"));

		for (Category category : categoryList) {
			if (!category.isRemoved()) {
				categoryModelList.add((X) new CategoryModel(category.getId(), category.getName()));
			}
		}

		ListLoadResult<X> result = new BaseListLoadResult<X>(categoryModelList);
		return result;
	}

	public PermissionEntryModel createPermissionEntry(Long gradebookId, PermissionEntryModel permissionEntryModel) {

		Permission permission = new Permission();
		permission.setGradebookId(gradebookId);
		permission.setCategoryId(permissionEntryModel.getCategoryId());
		permission.setFunction(permissionEntryModel.getPermissionId());
		permission.setUserId(permissionEntryModel.getUserId());
		permission.setGroupId(permissionEntryModel.getSectionId());
		Long id = gbService.createPermission(permission);
		permissionEntryModel.setId(id);
		return permissionEntryModel;
	}

	public PermissionEntryModel deletePermissionEntry(Long gradebookId, PermissionEntryModel permissionEntryModel) {

		Permission permission = new Permission();
		permission.setGradebookId(gradebookId);
		permission.setId(permissionEntryModel.getId());
		permission.setCategoryId(permissionEntryModel.getCategoryId());
		permission.setFunction(permissionEntryModel.getPermissionId());
		permission.setUserId(permissionEntryModel.getUserId());
		permission.setGroupId(permissionEntryModel.getSectionId());
		gbService.deletePermission(permission);
		return permissionEntryModel;
	}

	public SpreadsheetModel createOrUpdateSpreadsheet(String gradebookUid, SpreadsheetModel spreadsheetModel) throws InvalidInputException {

		Gradebook gradebook = gbService.getGradebook(gradebookUid);
		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
		Map<String, Assignment> idToAssignmentMap = new HashMap<String, Assignment>();
		List<ItemModel> headers = spreadsheetModel.getHeaders();

		if (headers != null) {

			Set<Long> newCategoryIdSet = new HashSet<Long>();
			for (ItemModel item : headers) {
				String id = item.getIdentifier();
				if (id != null) {
					Long categoryId = item.getCategoryId();
					String name = item.getName();
					Double weight = item.getPercentCategory();
					Double points = item.getPoints();
					boolean isExtraCredit = DataTypeConversionUtil.checkBoolean(item.getExtraCredit());
					boolean isIncluded = DataTypeConversionUtil.checkBoolean(item.getIncluded());

					if (id.startsWith("NEW:")) {

						ItemModel itemModel = new ItemModel();
						itemModel.setCategoryId(categoryId);
						itemModel.setName(name);
						itemModel.setPercentCategory(weight);
						itemModel.setPoints(points);
						itemModel.setIncluded(Boolean.valueOf(isIncluded));
						itemModel.setExtraCredit(Boolean.valueOf(isExtraCredit));

						Long assignmentId = doCreateItem(gradebook, itemModel, hasCategories, false);
						// Long assignmentId =
						// gbService.createAssignmentForCategory(gradebook.getId(),
						// categoryId, name, points, weight, null,
						// Boolean.valueOf(!isIncluded), isExtraCredit,
						// Boolean.FALSE, Boolean.FALSE);
						item.setIdentifier(String.valueOf(assignmentId));

						Assignment assignment = gbService.getAssignment(assignmentId);
						idToAssignmentMap.put(id, assignment);

						/*
						 * ItemModel itemModel = new ItemModel();
						 * itemModel.setCategoryId(categoryId);
						 * itemModel.setName(name);
						 * itemModel.setPercentCategory(weight);
						 * itemModel.setPoints(points);
						 * itemModel.setIncluded(Boolean.valueOf(isIncluded));
						 * itemModel
						 * .setExtraCredit(Boolean.valueOf(isExtraCredit));
						 * ItemModel model = null;
						 * 
						 * model = createItem(gradebookUid, gradebook.getId(),
						 * itemModel, false);
						 * 
						 * 
						 * if (categoryId != null) {
						 * newCategoryIdSet.add(categoryId); }
						 * 
						 * ItemModel activeItem = getActiveItem(model);
						 * 
						 * if (activeItem != null) { Assignment assignment =
						 * gbService
						 * .getAssignment(Long.valueOf(activeItem.getIdentifier
						 * ())); idToAssignmentMap.put(id, assignment);
						 * item.setIdentifier(activeItem.getIdentifier()); }
						 */

					} else {
						Assignment assignment = gbService.getAssignment(Long.valueOf(id));

						boolean isModified = false;

						if (points != null && assignment.getPointsPossible() != null && !points.equals(assignment.getPointsPossible())) {
							assignment.setPointsPossible(points);
							isModified = true;
						}

						if (weight != null && assignment.getAssignmentWeighting() != null && !weight.equals(assignment.getAssignmentWeighting())) {

							weight = weight.doubleValue() * 0.01d;

							assignment.setAssignmentWeighting(weight);
							isModified = true;
						}

						boolean wasIncluded = !DataTypeConversionUtil.checkBoolean(assignment.isUnweighted());

						if (wasIncluded != isIncluded) {
							assignment.setUnweighted(Boolean.valueOf(!isIncluded));
							isModified = true;
						}

						boolean wasExtraCredit = DataTypeConversionUtil.checkBoolean(assignment.isExtraCredit());

						if (wasExtraCredit != isExtraCredit) {
							assignment.setExtraCredit(Boolean.valueOf(isExtraCredit));
							isModified = true;
						}

						if (isModified)
							gbService.updateAssignment(assignment);

						idToAssignmentMap.put(id, assignment);
					}
				}
			}

			// Apply business rules after item creation
			if (hasCategories) {
				List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
				List<Category> categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

				Map<Long, Category> categoryMap = new HashMap<Long, Category>();

				if (categories != null) {
					for (Category category : categories) {
						categoryMap.put(category.getId(), category);
					}
				}

				if (newCategoryIdSet != null && !newCategoryIdSet.isEmpty()) {

					for (Long categoryId : newCategoryIdSet) {
						Category category = categoryMap.get(categoryId);
						List<Assignment> assigns = category.getAssignmentList();
						// Business rule #5
						if (businessLogic.checkRecalculateEqualWeightingRule(category))
							recalculateAssignmentWeights(category, Boolean.FALSE, assigns);
					}

				}
			}
		}

		Long gradebookId = gradebook.getId();

		Site site = getSite();
		Map<String, UserRecord> userRecordMap = new HashMap<String, UserRecord>();

		String[] learnerRoleNames = advisor.getLearnerRoleNames();
		String siteId = site == null ? null : site.getId();

		String[] realmIds = null;

		if (siteId == null) {
			if (log.isInfoEnabled())
				log.info("No siteId defined");
			throw new InvalidInputException("No site defined!");
		}

		realmIds = new String[1];
		realmIds[0] = new StringBuffer().append("/site/").append(siteId).toString();

		List<AssignmentGradeRecord> allGradeRecords = gbService.getAllAssignmentGradeRecords(gradebookId, realmIds, learnerRoleNames);

		if (allGradeRecords != null) {
			for (AssignmentGradeRecord gradeRecord : allGradeRecords) {
				gradeRecord.setUserAbleToView(true);
				String studentUid = gradeRecord.getStudentId();
				UserRecord userRecord = userRecordMap.get(studentUid);

				if (userRecord == null) {
					userRecord = new UserRecord(studentUid);
					userRecordMap.put(studentUid, userRecord);
				}

				Map<Long, AssignmentGradeRecord> studentMap = userRecord.getGradeRecordMap();
				if (studentMap == null) {
					studentMap = new HashMap<Long, AssignmentGradeRecord>();
				}
				GradableObject go = gradeRecord.getGradableObject();
				studentMap.put(go.getId(), gradeRecord);

				userRecord.setGradeRecordMap(studentMap);
			}
		}

		List<String> results = new ArrayList<String>();

		// Since we index the new items by a phony id e.g. "NEW:123", we need to
		// use this set to iterate
		Set<String> idKeySet = idToAssignmentMap.keySet();
		if (idKeySet != null) {
			for (StudentModel student : spreadsheetModel.getRows()) {
				UserRecord userRecord = userRecordMap.get(student.getIdentifier());

				StringBuilder builder = new StringBuilder();

				builder.append("Grading ");

				if (userRecord != null) {
					if (userRecord.getDisplayName() == null)
						builder.append(userRecord.getDisplayId()).append(": ");
					else
						builder.append(userRecord.getDisplayName()).append(": ");
				} else {
					builder.append(student.get("NAME")).append(": ");
				}

				/*
				 * if (userRecord == null) { builder.append("User not found!");
				 * results.add(builder.toString()); continue; }
				 */

				Map<Long, AssignmentGradeRecord> gradeRecordMap = userRecord == null ? null : userRecord.getGradeRecordMap();

				for (String id : idKeySet) {
					Assignment assignment = idToAssignmentMap.get(id);
					// This is the value stored on the client
					Object v = student.get(id);

					Double value = null;
					if (v != null && v instanceof String) {
						String strValue = (String) v;
						if (strValue.trim().length() > 0)
							value = Double.valueOf(Double.parseDouble((String) v));

					} else
						value = (Double) v;

					AssignmentGradeRecord assignmentGradeRecord = null;

					if (gradeRecordMap != null)
						assignmentGradeRecord = gradeRecordMap.get(assignment.getId()); // gbService.getAssignmentGradeRecordForAssignmentForStudent(assignment,
					// student.getIdentifier());

					Double oldValue = null;

					if (assignmentGradeRecord == null)
						assignmentGradeRecord = new AssignmentGradeRecord();

					switch (gradebook.getGrade_type()) {
						case GradebookService.GRADE_TYPE_POINTS:
							oldValue = assignmentGradeRecord.getPointsEarned();
							break;
						case GradebookService.GRADE_TYPE_PERCENTAGE:
							oldValue = assignmentGradeRecord.getPercentEarned();
							break;
					}

					if (oldValue == null && value == null)
						continue;

					student.set(AppConstants.IMPORT_CHANGES, Boolean.TRUE);

					try {
						scoreItem(gradebook, assignment, assignmentGradeRecord, student.getIdentifier(), value, true, false);

						builder.append(assignment.getName()).append(" (");

						if (oldValue != null)
							builder.append(oldValue).append("->");

						builder.append(value).append(") ");

						// results.add("Successfully scored " +
						// assignment.getName() + " for " +
						// student.getIdentifier() + " to " + value);
					} catch (InvalidInputException e) {
						String failedProperty = new StringBuilder().append(assignment.getId()).append(StudentModel.FAILED_FLAG).toString();
						student.set(failedProperty, e.getMessage());
						log.warn("Failed to score numeric item for " + student.getIdentifier() + " and item " + assignment.getId() + " to " + value);

						if (oldValue != null)
							builder.append(oldValue);

						builder.append("Invalid) ");
					} catch (Exception e) {

						String failedProperty = new StringBuilder().append(assignment.getId()).append(StudentModel.FAILED_FLAG).toString();
						student.set(failedProperty, e.getMessage());

						log.warn("Failed to score numeric item for " + student.getIdentifier() + " and item " + assignment.getId() + " to " + value, e);

						if (oldValue != null)
							builder.append(oldValue);

						builder.append("Failed) ");
					}

				}

				results.add(builder.toString());
			}
		}
		spreadsheetModel.setResults(results);
		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		List<Category> categories = null;
		if (hasCategories)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
		spreadsheetModel.setGradebookItemModel(getItemModel(gradebook, assignments, categories, null, null));

		return spreadsheetModel;
	}

	private List<UserRecord> doSearchUsers(String searchString, List<String> studentUids, Map<String, UserRecord> userRecordMap) {

		// Make sure that our search criterion is case insensitive
		if (searchString != null)
			searchString = searchString.toUpperCase();

		List<UserRecord> userRecords = new ArrayList<UserRecord>();

		// To do a search, we have to get all the users . . . this is also
		// desirable even if we're not searching, if we want to sort on these
		// properties
		List<User> users = userService.getUsers(studentUids);

		if (users != null) {
			for (User user : users) {
				String lastName = user.getLastName() == null ? "" : user.getLastName();
				String firstName = user.getFirstName() == null ? "" : user.getFirstName();

				String sortName = new StringBuilder().append(lastName.toUpperCase()).append("::").append(firstName.toUpperCase()).toString();
				// Make sure that our search field is case insensitive
				if (sortName != null)
					sortName = sortName.toUpperCase();

				// If we're not searching, then return everybody
				if (searchString == null || sortName.contains(searchString)) {
					UserRecord userRecord = userRecordMap.get(user.getId());
					userRecord.populate(user);
					userRecords.add(userRecord);
				}
			}
		}

		return userRecords;
	}

	private Long doCreateItem(Gradebook gradebook, ItemModel item, boolean hasCategories, boolean enforceNoNewCategories) throws BusinessRuleException {

		ActionRecord actionRecord = new ActionRecord(gradebook.getUid(), gradebook.getId(), EntityType.ITEM.name(), ActionType.CREATE.name());
		actionRecord.setEntityName(item.getName());
		Map<String, String> propertyMap = actionRecord.getPropertyMap();

		for (String property : item.getPropertyNames()) {
			String value = String.valueOf(item.get(property));
			if (value != null)
				propertyMap.put(property, value);
		}

		boolean hasNewCategory = false;

		Category category = null;
		Long assignmentId = null;

		List<Assignment> assignments = null;
		Long categoryId = null;

		try {
			boolean includeInGrade = DataTypeConversionUtil.checkBoolean(item.getIncluded());

			categoryId = item.getCategoryId();
			String name = item.getName();
			Double weight = item.getPercentCategory();
			Double points = item.getPoints();
			Boolean isReleased = Boolean.valueOf(DataTypeConversionUtil.checkBoolean(item.getReleased()));
			Boolean isIncluded = Boolean.valueOf(includeInGrade);
			Boolean isExtraCredit = Boolean.valueOf(DataTypeConversionUtil.checkBoolean(item.getExtraCredit()));
			Date dueDate = item.getDueDate();
			Integer itemOrder = item.getItemOrder();

			// Business rule #1
			if (points == null)
				points = new Double(100d);
			// Business rule #2
			if (weight == null)
				weight = Double.valueOf(points.doubleValue());

			if (hasCategories && item.getCategoryId() == null && item.getCategoryName() != null && item.getCategoryName().trim().length() > 0) {
				ItemModel newCategory = new ItemModel();
				newCategory.setName(item.getCategoryName());
				newCategory.setIncluded(Boolean.TRUE);
				newCategory = getActiveItem(addItemCategory(gradebook.getUid(), gradebook.getId(), newCategory));
				categoryId = newCategory.getCategoryId();
				item.setCategoryId(categoryId);
				hasNewCategory = true;
			}

			if (categoryId == null || categoryId.equals(Long.valueOf(-1l))) {
				category = findDefaultCategory(gradebook.getId());
				categoryId = null;
			}

			if (category == null && categoryId != null)
				category = gbService.getCategory(categoryId);

			// Apply business rules before item creation
			if (hasCategories) {
				if (categoryId != null) {
					assignments = gbService.getAssignmentsForCategory(categoryId);
					// Business rule #4
					businessLogic.applyNoDuplicateItemNamesWithinCategoryRule(categoryId, name, null, assignments);
					// Business rule #6
					if (enforceNoNewCategories)
						businessLogic.applyMustIncludeCategoryRule(item.getCategoryId());
				}

			} else {
				assignments = gbService.getAssignments(gradebook.getId());
				businessLogic.applyNoDuplicateItemNamesRule(gradebook.getId(), name, null, assignments);
			}

			if (itemOrder == null)
				itemOrder = assignments == null || assignments.isEmpty() ? Integer.valueOf(0) : Integer.valueOf(assignments.size());

			// if (assignments == null || assignments.isEmpty())
			// weight = new Double(100d);

			double w = weight == null ? 0d : ((Double) weight).doubleValue() * 0.01;

			assignmentId = gbService.createAssignmentForCategory(gradebook.getId(), categoryId, name, points, Double.valueOf(w), dueDate, Boolean.valueOf(!DataTypeConversionUtil.checkBoolean(isIncluded)), isExtraCredit, Boolean.FALSE,
					isReleased, itemOrder);

			// Apply business rules after item creation
			if (hasCategories) {
				assignments = gbService.getAssignmentsForCategory(categoryId);
				// Business rule #5
				if (businessLogic.checkRecalculateEqualWeightingRule(category))
					recalculateAssignmentWeights(category, Boolean.FALSE, assignments);
			}

			actionRecord.setStatus(ActionRecord.STATUS_SUCCESS);
		} catch (RuntimeException e) {
			actionRecord.setStatus(ActionRecord.STATUS_FAILURE);
			throw e;
		} finally {
			gbService.storeActionRecord(actionRecord);
		}

		return assignmentId;
	}

	private List<UserRecord> doSearchAndSortUserRecords(Gradebook gradebook, List<Assignment> assignments, List<Category> categories, List<String> studentUids, Map<String, UserRecord> userRecordMap, PagingLoadConfig config) {

		String searchString = null;
		if (config instanceof MultiGradeLoadConfig) {
			searchString = ((MultiGradeLoadConfig) config).getSearchString();
		}

		List<UserRecord> userRecords = null;
		StudentModel.Key sortColumnKey = null;

		String columnId = null;

		// This is slightly painful, but since it's a String that gets passed
		// up, we have to iterate
		if (config != null && config.getSortInfo() != null && config.getSortInfo().getSortField() != null) {
			columnId = config.getSortInfo().getSortField();

			for (StudentModel.Key key : EnumSet.allOf(StudentModel.Key.class)) {
				if (columnId.equals(key.name())) {
					sortColumnKey = key;
					break;
				}
			}

			if (sortColumnKey == null)
				sortColumnKey = StudentModel.Key.ASSIGNMENT;

		}

		if (sortColumnKey == null)
			sortColumnKey = StudentModel.Key.DISPLAY_NAME;

		boolean isDescending = config != null && config.getSortInfo() != null && config.getSortInfo().getSortDir() == SortDir.DESC;

		// Check to see if we're sorting or not
		if (sortColumnKey != null) {
			switch (sortColumnKey) {
				case DISPLAY_NAME:
				case LAST_NAME_FIRST:
				case DISPLAY_ID:
				case SECTION:
				case EMAIL:
					if (userRecords == null) {
						userRecords = doSearchUsers(searchString, studentUids, userRecordMap);
					}
					break;
				case COURSE_GRADE:
				case GRADE_OVERRIDE:
				case ASSIGNMENT:
					if (userRecords == null) {
						userRecords = new ArrayList<UserRecord>(userRecordMap.values());
					}
					break;
			}

			Comparator<UserRecord> comparator = null;
			switch (sortColumnKey) {
				case DISPLAY_NAME:
				case LAST_NAME_FIRST:
					comparator = SORT_NAME_COMPARATOR;
					break;
				case DISPLAY_ID:
					comparator = DISPLAY_ID_COMPARATOR;
					break;
				case EMAIL:
					comparator = EMAIL_COMPARATOR;
					break;
				case SECTION:
					comparator = SECTION_TITLE_COMPARATOR;
					break;
				case COURSE_GRADE:
					// In this case we need to ensure that we've calculated
					// everybody's course grade
					for (UserRecord record : userRecords) {
						record.setDisplayGrade(getDisplayGrade(gradebook, record.getUserUid(), record.getCourseGradeRecord(), assignments, categories, record.getGradeRecordMap()));
						record.setCalculated(true);
					}
					comparator = new CourseGradeComparator(isDescending);
					break;
				case GRADE_OVERRIDE:
					comparator = new EnteredGradeComparator(isDescending);
					break;
				case ASSIGNMENT:
					Long assignmentId = Long.valueOf(columnId);
					comparator = new AssignmentComparator(assignmentId, isDescending);
					break;
			}

			if (comparator != null) {
				if (isDescending)
					comparator = Collections.reverseOrder(comparator);

				Collections.sort(userRecords, comparator);
			}
		}

		if (userRecords == null) {
			// Of course, we need to do this regardless or it will be null
			// This is pretty silly on one level, since it means that we don't
			// take advantage of the database to do this, but it's equivalent to
			// what
			// section awareness is doing behind the scenes and it gives us more
			// control over the process
			if (searchString != null)
				userRecords = doSearchUsers(searchString, studentUids, userRecordMap);
			else
				userRecords = new ArrayList<UserRecord>(userRecordMap.values());

			// This seems a little stupid, but the fact of the matter is that we
			// get an unordered list
			// back from the Map.keySet call, so we do want to ensure that we
			// get the same order each time
			// even when the user has not chosen to sort
			Collections.sort(userRecords, DEFAULT_ID_COMPARATOR);
		}

		return userRecords;
	}

	public StudentModel excuseNumericItem(String gradebookUid, StudentModel student, String id, Boolean value, Boolean previousValue) throws InvalidInputException {

		int indexOf = id.indexOf(StudentModel.EXCUSE_FLAG);

		if (indexOf == -1)
			return null;

		String assignmentId = id.substring(0, indexOf);

		Assignment assignment = gbService.getAssignment(Long.valueOf(assignmentId));
		Gradebook gradebook = assignment.getGradebook();

		List<AssignmentGradeRecord> gradeRecords = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), student.getIdentifier());

		AssignmentGradeRecord assignmentGradeRecord = null;

		for (AssignmentGradeRecord currentGradeRecord : gradeRecords) {
			Assignment a = currentGradeRecord.getAssignment();
			if (a.getId().equals(assignment.getId()))
				assignmentGradeRecord = currentGradeRecord;
		}

		if (assignmentGradeRecord == null) {
			assignmentGradeRecord = new AssignmentGradeRecord();
		}

		assignmentGradeRecord.setExcluded(value);

		// Prepare record for update
		assignmentGradeRecord.setGradableObject(assignment);
		assignmentGradeRecord.setStudentId(student.getIdentifier());

		Collection<AssignmentGradeRecord> updateGradeRecords = new LinkedList<AssignmentGradeRecord>();
		updateGradeRecords.add(assignmentGradeRecord);
		gbService.updateAssignmentGradeRecords(assignment, updateGradeRecords, gradebook.getGrade_type());

		gradeRecords = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), student.getIdentifier());

		return refreshLearnerData(gradebook, student, assignment, gradeRecords);
	}

	public List<UserDereference> findAllUserDereferences() {

		Site site = getSite();
		String siteId = site == null ? null : site.getId();

		String[] learnerRoleNames = advisor.getLearnerRoleNames();
		verifyUserDataIsUpToDate(site, learnerRoleNames);

		String[] realmIds = null;
		if (siteId == null) {
			if (log.isInfoEnabled())
				log.info("No siteId defined");
			return new ArrayList<UserDereference>();
		}

		realmIds = new String[1];
		realmIds[0] = new StringBuffer().append("/site/").append(siteId).toString();

		List<UserDereference> dereferences = gbService.getUserDereferences(realmIds, "sortName", null, null, -1, -1, true, learnerRoleNames);

		return dereferences;
	}

	@SuppressWarnings("unchecked")
	public <X extends BaseModel> PagingLoadResult<X> getActionHistory(String gradebookUid, PagingLoadConfig config) {

		Integer size = gbService.getActionRecordSize(gradebookUid);
		List<ActionRecord> actionRecords = gbService.getActionRecords(gradebookUid, config.getOffset(), config.getLimit());
		List<X> models = new ArrayList<X>();

		for (ActionRecord actionRecord : actionRecords) {
			UserEntityAction actionModel = null;
			try {
				UserEntityAction.ActionType actionType = UserEntityAction.ActionType.valueOf(actionRecord.getActionType());
				UserEntityAction.EntityType entityType = UserEntityAction.EntityType.valueOf(actionRecord.getEntityType());
				switch (actionType) {
					case CREATE:
						actionModel = new UserEntityCreateAction();
						break;
					case GRADED:
						actionModel = new UserEntityGradeAction();
						break;
					case UPDATE:
						actionModel = new UserEntityUpdateAction();
						break;
				}

				actionModel.setIdentifier(String.valueOf(actionRecord.getId()));
				actionModel.setGradebookUid(actionRecord.getGradebookUid());
				actionModel.setGradebookId(actionRecord.getGradebookId());
				actionModel.setEntityType(entityType);
				if (actionRecord.getEntityId() != null)
					actionModel.setEntityId(actionRecord.getEntityId());
				if (actionRecord.getEntityName() != null)
					actionModel.setEntityName(actionRecord.getEntityName());
				if (actionRecord.getParentId() != null)
					actionModel.setParentId(Long.valueOf(actionRecord.getParentId()));
				actionModel.setStudentUid(actionRecord.getStudentUid());
				/*
				 * actionModel.setKey(actionRecord.getField());
				 * actionModel.setValue(actionRecord.getValue());
				 * actionModel.setStartValue(actionRecord.getStartValue());
				 */

				actionModel.setGraderName(actionRecord.getGraderId());

				if (userService != null && actionRecord.getGraderId() != null) {

					try {
						User user = userService.getUser(actionRecord.getGraderId());
						actionModel.setGraderName(user.getDisplayName());
					} catch (UserNotDefinedException e) {
						log.warn("Unable to find grader name for " + actionRecord.getGraderId(), e);
					}

				}

				actionModel.setDatePerformed(actionRecord.getDatePerformed());
				actionModel.setDateRecorded(actionRecord.getDateRecorded());

				Map<String, String> propertyMap = actionRecord.getPropertyMap();

				/*
				 * switch (entityType) { case CATEGORY: case GRADEBOOK: case
				 * ITEM: ItemModel itemModel = new ItemModel(); if (propertyMap
				 * != null) { for (String key : propertyMap.keySet()) { String
				 * value = propertyMap.get(key); itemModel.set(key, value); } }
				 * actionModel.setModel(itemModel); break; default:
				 */
				if (propertyMap != null) {
					for (String key : propertyMap.keySet()) {
						String value = propertyMap.get(key);
						actionModel.set(key, value);
					}
				}
				// }

				actionModel.setDescription(actionModel.toString());

				models.add((X) actionModel);
			} catch (Exception e) {
				log.warn("Failed to retrieve history record for " + actionRecord.getId());
			}
		}

		return new BasePagingLoadResult<X>(models, config.getOffset(), size.intValue());
	}

	public class WorkerThread extends Thread {

		private Site site;
		private String[] learnerRoleKeys;

		public WorkerThread(Site site, String[] learnerRoleKeys) {

			this.site = site;
			this.learnerRoleKeys = learnerRoleKeys;
		}

		public void run() {

			verifyUserDataIsUpToDate(site, learnerRoleKeys);
		}
	}

	public ApplicationModel getApplicationModel(String... gradebookUids) {

		ApplicationModel model = new ApplicationModel();
		model.setGradebookModels(getGradebookModels(gradebookUids));

		if (configService != null) {
			String url = configService.getString(AppConstants.HELP_URL_CONFIG_ID);

			if (url != null)
				model.setHelpUrl(url);
		}

		return model;
	}

	public AuthModel getAuthorization(String... gradebookUids) {

		if (gradebookUids == null || gradebookUids.length == 0)
			gradebookUids = new String[] { lookupDefaultGradebookUid() };

		for (int i = 0; i < gradebookUids.length; i++) {
			boolean isNewGradebook = false;
			Gradebook gradebook = null;
			try {
				// First thing, grab the default gradebook if one exists
				gradebook = gbService.getGradebook(gradebookUids[i]);
			} catch (GradebookNotFoundException gnfe) {
				// If it doesn't exist, then create it
				if (frameworkService != null) {
					frameworkService.addGradebook(gradebookUids[i], "My Default Gradebook");
					gradebook = gbService.getGradebook(gradebookUids[i]);

					// Add the default configuration settings
					gbService.createOrUpdateUserConfiguration(getCurrentUser(), gradebook.getId(), 
							ConfigurationModel.getColumnHiddenId(AppConstants.ITEMTREE, 
									StudentModel.Key.DISPLAY_ID.name()), String.valueOf(Boolean.TRUE));
					gbService.createOrUpdateUserConfiguration(getCurrentUser(), gradebook.getId(), 
							ConfigurationModel.getColumnHiddenId(AppConstants.ITEMTREE, 
									StudentModel.Key.DISPLAY_NAME.name()), String.valueOf(Boolean.TRUE));
					gbService.createOrUpdateUserConfiguration(getCurrentUser(), gradebook.getId(), 
							ConfigurationModel.getColumnHiddenId(AppConstants.ITEMTREE, 
									StudentModel.Key.EMAIL.name()), String.valueOf(Boolean.TRUE));
					gbService.createOrUpdateUserConfiguration(getCurrentUser(), gradebook.getId(), 
							ConfigurationModel.getColumnHiddenId(AppConstants.ITEMTREE, 
									StudentModel.Key.SECTION.name()), String.valueOf(Boolean.TRUE));
				
				} 
			}
			AuthModel authModel = new AuthModel();
			boolean isUserAbleToGrade = authz.isUserAbleToGrade(gradebookUids[i]);
			boolean isUserAbleToViewOwnGrades = authz.isUserAbleToViewOwnGrades(gradebookUids[i]);

			authModel.setUserAbleToGrade(Boolean.valueOf(isUserAbleToGrade));
			authModel.setUserAbleToEditAssessments(Boolean.valueOf(authz.isUserAbleToEditAssessments(gradebookUids[i])));
			authModel.setUserAbleToViewOwnGrades(Boolean.valueOf(isUserAbleToViewOwnGrades));
			authModel.setUserHasGraderPermissions(Boolean.valueOf(authz.hasUserGraderPermissions(gradebook.getUid())));
			authModel.setNewGradebook(Boolean.valueOf(isNewGradebook));
			authModel.setPlacementId(getPlacementId());

			return authModel;
		}
		return null;
	}

	public List<String> getExportCourseManagementSetEids(Group group) {

		return advisor.getExportCourseManagementSetEids(group);
	}

	public String getExportCourseManagementId(String userEid, Group group, List<String> enrollmentSetEids) {

		return advisor.getExportCourseManagementId(userEid, group, enrollmentSetEids);
	}

	public String getExportUserId(UserDereference dereference) {

		return advisor.getExportUserId(dereference);
	}

	public String getFinalGradeUserId(UserDereference dereference) {

		return advisor.getFinalGradeUserId(dereference);
	}

	public GradebookModel getGradebook(String uid) {

		Gradebook gradebook = gbService.getGradebook(uid);
		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		List<Category> categories = null;
		if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
		return createGradebookModel(gradebook, assignments, categories, false);
	}

	public <X extends BaseModel> ListLoadResult<X> getGradeEvents(String studentId, Long assignmentId) {

		List<X> models = new ArrayList<X>();
		Assignment assignment = gbService.getAssignment(assignmentId);
		Collection<GradableObject> gradableObjects = new LinkedList<GradableObject>();
		gradableObjects.add(assignment);

		Map<GradableObject, List<GradingEvent>> map = gbService.getGradingEventsForStudent(studentId, gradableObjects);

		List<GradingEvent> events = map.get(assignment);

		if (events != null) {
			Collections.sort(events, new Comparator<GradingEvent>() {

				public int compare(GradingEvent o1, GradingEvent o2) {

					if (o2.getDateGraded() == null || o1.getDateGraded() == null)
						return 0;

					return o2.getDateGraded().compareTo(o1.getDateGraded());
				}

			});

			for (GradingEvent event : events) {
				models.add((X) createOrUpdateGradeEventModel(null, event));
			}
		}

		ListLoadResult<X> result = new BaseListLoadResult<X>(models);

		return result;
	}

	public <X extends BaseModel> ListLoadResult<X> getGradeFormats(String gradebookUid, 
			Long gradebookId) {
		
		List<X> models = new ArrayList<X>();
		
		Set<GradeMapping> gradeMappings = gbService.getGradeMappings(gradebookId);
		
		for (GradeMapping mapping : gradeMappings) {
			GradeFormatModel model = new GradeFormatModel();
			model.setIdentifier(String.valueOf(mapping.getId()));
			model.setName(mapping.getName());
			models.add((X)model);
		}
		
		return new BaseListLoadResult<X>(models);
	}

	public <X extends BaseModel> ListLoadResult<X> getCategories(String gradebookUid, Long gradebookId, PagingLoadConfig config) {

		List<Category> categories = gbService.getCategories(gradebookId);

		List<X> models = new LinkedList<X>();

		for (Category category : categories) {
			models.add((X) createItemModel(null, category, null));
		}

		return new BaseListLoadResult<X>(models);
	}

	public <X extends BaseModel> PagingLoadResult<X> getSections(String gradebookUid, Long gradebookId, PagingLoadConfig config, boolean enableAllSectionsEntry, String allSectionsEntryTitle) {

		List<CourseSection> viewableSections = authz.getViewableSections(gradebookUid);

		List<X> sections = new LinkedList<X>();

		if (enableAllSectionsEntry) {
			SectionModel allSections = new SectionModel();
			// allSections.setSectionId("all");
			allSections.setSectionName(allSectionsEntryTitle);
			sections.add((X) allSections);
		}

		if (viewableSections != null) {
			for (CourseSection courseSection : viewableSections) {
				SectionModel sectionModel = new SectionModel();
				sectionModel.setSectionId(courseSection.getUuid());
				sectionModel.setSectionName(courseSection.getTitle());
				sections.add((X) sectionModel);
			}
		}

		return new BasePagingLoadResult<X>(sections, config.getOffset(), viewableSections.size());
	}

	public <X extends BaseModel> ListLoadResult<X> getSelectedGradeMapping(String gradebookUid) {

		List<X> gradeScaleMappings = new ArrayList<X>();
		Gradebook gradebook = gbService.getGradebook(gradebookUid);
		GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
		GradingScale gradingScale = gradeMapping.getGradingScale();
		// Map<String, Double> gradingScaleMap =
		// gradingScale.getDefaultBottomPercents();

		List<String> letterGradesList = new ArrayList<String>(gradeMapping.getGradeMap().keySet());

		Collections.sort(letterGradesList, LETTER_GRADE_COMPARATOR);

		Double upperScale = null;

		for (String letterGrade : letterGradesList) {

			upperScale = (null == upperScale) ? new Double(100d) : upperScale.equals(Double.valueOf(0d)) ? Double.valueOf(0d) : Double.valueOf(upperScale.doubleValue() - 0.00001d);

			GradeScaleRecordModel gradeScaleModel = new GradeScaleRecordModel(letterGrade, gradeMapping.getGradeMap().get(letterGrade), upperScale);
			gradeScaleMappings.add((X) gradeScaleModel);
			upperScale = gradeMapping.getGradeMap().get(letterGrade);
		}

		ListLoadResult<X> result = new BaseListLoadResult<X>(gradeScaleMappings);

		return result;
	}

	public <X extends BaseModel> ListLoadResult<X> getStatistics(String gradebookUid, Long gradebookId) {

		Gradebook gradebook = null;
		if (gradebookId == null) {
			gradebook = gbService.getGradebook(gradebookUid);
			gradebookId = gradebook.getId();
		}

		List<Assignment> assignments = gbService.getAssignments(gradebookId);

		// Don't bother going out to the db for the Gradebook if we've already
		// retrieved it
		if (gradebook == null && assignments != null && assignments.size() > 0)
			gradebook = assignments.get(0).getGradebook();

		if (gradebook == null)
			gradebook = gbService.getGradebook(gradebookId);

		List<Category> categories = null;
		if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

		int gradeType = gradebook.getGrade_type();

		String siteId = getSiteId();

		String[] realmIds = new String[1];
		realmIds[0] = new StringBuffer().append("/site/").append(siteId).toString();

		String[] learnerRoleNames = advisor.getLearnerRoleNames();

		List<AssignmentGradeRecord> allGradeRecords = gbService.getAllAssignmentGradeRecords(gradebook.getId(), realmIds, learnerRoleNames);
		Map<String, Map<Long, AssignmentGradeRecord>> studentGradeRecordMap = new HashMap<String, Map<Long, AssignmentGradeRecord>>();

		List<String> gradedStudentUids = new ArrayList<String>();

		Map<Long, BigDecimal> assignmentSumMap = new HashMap<Long, BigDecimal>();
		Map<Long, List<BigDecimal>> assignmentGradeListMap = new HashMap<Long, List<BigDecimal>>();

		if (allGradeRecords != null) {
			for (AssignmentGradeRecord gradeRecord : allGradeRecords) {
				gradeRecord.setUserAbleToView(true);
				String studentUid = gradeRecord.getStudentId();
				Map<Long, AssignmentGradeRecord> studentMap = studentGradeRecordMap.get(studentUid);
				if (studentMap == null) {
					studentMap = new HashMap<Long, AssignmentGradeRecord>();
				}
				GradableObject go = gradeRecord.getGradableObject();
				studentMap.put(go.getId(), gradeRecord);

				BigDecimal value = null;
				if (gradeRecord.getPointsEarned() != null) {
					Assignment assignment = (Assignment) gradeRecord.getGradableObject();
					switch (gradeType) {
						case GradebookService.GRADE_TYPE_POINTS:
							value = BigDecimal.valueOf(gradeRecord.getPointsEarned().doubleValue());
							break;
						case GradebookService.GRADE_TYPE_PERCENTAGE:
							value = gradeCalculations.getPointsEarnedAsPercent(assignment, gradeRecord);
							break;
					}

					if (value != null && assignment != null) {
						Long assignmentId = assignment.getId();
						List<BigDecimal> gradeList = assignmentGradeListMap.get(assignmentId);
						if (gradeList == null) {
							gradeList = new ArrayList<BigDecimal>();
							assignmentGradeListMap.put(assignmentId, gradeList);
						}
						gradeList.add(value);

						BigDecimal itemSum = assignmentSumMap.get(assignmentId);
						if (itemSum == null)
							itemSum = BigDecimal.ZERO;
						itemSum = itemSum.add(value);
						assignmentSumMap.put(assignmentId, itemSum);
					}

				}
				studentGradeRecordMap.put(studentUid, studentMap);

				if (!gradedStudentUids.contains(studentUid))
					gradedStudentUids.add(studentUid);
			}
		}

		// Now we can calculate the mean course grade
		List<BigDecimal> courseGradeList = new ArrayList<BigDecimal>();
		BigDecimal sumCourseGrades = BigDecimal.ZERO;
		for (String studentUid : gradedStudentUids) {
			Map<Long, AssignmentGradeRecord> studentMap = studentGradeRecordMap.get(studentUid);
			BigDecimal courseGrade = null;

			switch (gradebook.getCategory_type()) {
				case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
					courseGrade = gradeCalculations.getCourseGrade(gradebook, assignments, studentMap);
					break;
				default:
					courseGrade = gradeCalculations.getCourseGrade(gradebook, categories, studentMap);
			}

			if (courseGrade != null) {
				sumCourseGrades = sumCourseGrades.add(courseGrade);
				courseGradeList.add(courseGrade);
			}
		}

		GradeStatistics courseGradeStatistics = gradeCalculations.calculateStatistics(courseGradeList, sumCourseGrades);

		List<X> statsList = new ArrayList<X>();

		long id = 0;
		statsList.add((X) createStatisticsModel("Course Grade", courseGradeStatistics, Long.valueOf(id)));
		id++;

		if (assignments != null) {
			for (Assignment assignment : assignments) {
				Long assignmentId = assignment.getId();
				String name = assignment.getName();

				List<BigDecimal> gradeList = assignmentGradeListMap.get(assignmentId);
				BigDecimal sum = assignmentSumMap.get(assignmentId);

				if (gradeList != null && sum != null) {
					GradeStatistics assignmentStatistics = gradeCalculations.calculateStatistics(gradeList, sum);

					statsList.add((X) createStatisticsModel(name, assignmentStatistics, Long.valueOf(id)));
					id++;
				}
			}
		}

		ListLoadResult<X> result = new BaseListLoadResult<X>(statsList);

		return result;
	}

	private StatisticsModel createStatisticsModel(String name, GradeStatistics statistics, Long id) {

		StatisticsModel courseGradeStats = new StatisticsModel();
		courseGradeStats.setId(String.valueOf(id));
		courseGradeStats.setName(name);
		if (statistics.getMean() != null)
			courseGradeStats.setMean(statistics.getMean().setScale(2, RoundingMode.HALF_EVEN).toString());
		if (statistics.getMedian() != null)
			courseGradeStats.setMedian(statistics.getMedian().setScale(2, RoundingMode.HALF_EVEN).toString());
		if (statistics.getMode() != null)
			courseGradeStats.setMode(statistics.getMode().setScale(2, RoundingMode.HALF_EVEN).toString());
		if (statistics.getStandardDeviation() != null)
			courseGradeStats.setStandardDeviation(statistics.getStandardDeviation().setScale(2, RoundingMode.HALF_EVEN).toString());

		return courseGradeStats;
	}

	public <X extends BaseModel> PagingLoadResult<X> getStudentRows(String gradebookUid, Long gradebookId, PagingLoadConfig config, Boolean includeExportCourseManagementId) {

		boolean includeCMId = DataTypeConversionUtil.checkBoolean(includeExportCourseManagementId);

		List<X> rows = new ArrayList<X>();

		String[] learnerRoleNames = advisor.getLearnerRoleNames();

		List<UserRecord> userRecords = null;

		String sectionUuid = null;

		if (config != null && config instanceof MultiGradeLoadConfig) {
			sectionUuid = ((MultiGradeLoadConfig) config).getSectionUuid();
		}

		Gradebook gradebook = null;
		if (gradebookId == null) {
			gradebook = gbService.getGradebook(gradebookUid);
			gradebookId = gradebook.getId();
		}

		List<Assignment> assignments = gbService.getAssignments(gradebookId);

		// Don't bother going out to the db for the Gradebook if we've already
		// retrieved it
		if (gradebook == null && assignments != null && assignments.size() > 0)
			gradebook = assignments.get(0).getGradebook();

		if (gradebook == null)
			gradebook = gbService.getGradebook(gradebookId);

		List<Category> categories = null;
		if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

		String columnId = null;
		StudentModel.Key sortColumnKey = null;

		int offset = -1;
		int limit = -1;

		String searchField = null;
		String searchCriteria = null;

		// This is slightly painful, but since it's a String that gets passed
		// up, we have to iterate
		if (config != null) {
			offset = config.getOffset();
			limit = config.getLimit();
			if (config.getSortInfo() != null && config.getSortInfo().getSortField() != null) {
				columnId = config.getSortInfo().getSortField();

				for (StudentModel.Key key : EnumSet.allOf(StudentModel.Key.class)) {
					if (columnId.equals(key.name())) {
						sortColumnKey = key;
						break;
					}
				}

				if (sortColumnKey == null)
					sortColumnKey = StudentModel.Key.ASSIGNMENT;
			}

			if (config instanceof MultiGradeLoadConfig) {
				searchField = "sortName";
				searchCriteria = ((MultiGradeLoadConfig) config).getSearchString();

				if (searchCriteria != null)
					searchCriteria = searchCriteria.toUpperCase();
			}
		}

		if (sortColumnKey == null)
			sortColumnKey = StudentModel.Key.DISPLAY_NAME;

		boolean isDescending = config != null && config.getSortInfo() != null && config.getSortInfo().getSortDir() == SortDir.DESC;

		int totalUsers = 0;
		Site site = getSite();
		String siteId = (site == null) ? null : site.getId();

		// Check if the user is a TA and assigned to a section via the Section
		// Info tool
		List<CourseSection> courseSections = sectionAwareness.getSections(getSiteContext());
		boolean isUserAssignedSectionTa = false;

		for (CourseSection courseSection : courseSections) {
			isUserAssignedSectionTa = authz.isUserTAinSection(courseSection.getUuid());
			if (isUserAssignedSectionTa) {
				break;
			}
		}
		// Is the user an Instructor or is the user a TA?
		boolean isInstructor = authz.isUserAbleToGradeAll(gradebook.getUid());

		// Is the user a TA and was he/she granted view/grade access via the
		// Grader Permission UI?
		boolean hasUserGraderPermissions = authz.hasUserGraderPermissions(gradebook.getUid());

		// If the user doesn't have any of the necessary credentials we return
		// an empty data set
		if (!isInstructor && !hasUserGraderPermissions && !isUserAssignedSectionTa) {
			return new BasePagingLoadResult<X>(rows, 0, totalUsers);
		}

		// Was a section selected
		boolean isLimitedToSelectedSection = false;
		Set<String> authorizedGroups = new HashSet<String>();

		if (sectionUuid != null) {
			authorizedGroups.add(sectionUuid);
			isLimitedToSelectedSection = true;
		}

		// Get the site's groups
		Collection<Group> groups = (site == null) ? new ArrayList<Group>() : site.getGroups();
		Map<String, Group> groupReferenceMap = new HashMap<String, Group>();
		List<String> groupReferences = new ArrayList<String>();

		if (groups != null) {

			for (Group group : groups) {

				String reference = group.getReference();
				groupReferences.add(reference);
				groupReferenceMap.put(reference, group);

				String sectionProviderId = group.getProviderGroupId();

				if (!isLimitedToSelectedSection) {

					if (!isInstructor && (sectionProviderId != null) && (authz.isUserTAinSection(reference) || authz.hasUserGraderPermission(gradebook.getUid(), reference))) {

						authorizedGroups.add(reference);
					}
				}
			}
		}

		// Turn the groupId set into a String array
		String[] realmIds = null;
		if (!authorizedGroups.isEmpty())
			realmIds = authorizedGroups.toArray(new String[authorizedGroups.size()]);

		if (realmIds == null) {

			// If there are not groupIds we return an empty result set
			if (siteId == null) {
				if (log.isInfoEnabled())
					log.info("No siteId defined");
				return new BasePagingLoadResult<X>(rows, 0, totalUsers);
			}

			realmIds = new String[1];
			realmIds[0] = new StringBuffer().append("/site/").append(siteId).toString();
		}

		// Check to see if we're sorting or not
		if (sortColumnKey != null) {
			switch (sortColumnKey) {
				case DISPLAY_NAME:
				case LAST_NAME_FIRST:
				case DISPLAY_ID:
				case EMAIL:
					String sortField = "lastNameFirst";

					switch (sortColumnKey) {
						case DISPLAY_ID:
							sortField = "displayId";
							break;
						case DISPLAY_NAME:
						case LAST_NAME_FIRST:
							sortField = "sortName";
							break;
						case EMAIL:
							sortField = "email";
							break;
					}

					userRecords = findLearnerRecordPage(gradebook, site, realmIds, groupReferences, groupReferenceMap, sortField, searchField, searchCriteria, offset, limit, !isDescending, includeCMId);
					totalUsers = gbService.getUserCountForSite(realmIds, sortField, searchField, searchCriteria, learnerRoleNames);

					int startRow = config == null ? 0 : config.getOffset();

					List<FixedColumnModel> columns = getColumns();

					rows = new ArrayList<X>(userRecords == null ? 0 : userRecords.size());

					// We only want to populate the rowData and rowValues for
					// the
					// requested rows
					for (UserRecord userRecord : userRecords) {
						rows.add((X) buildStudentRow(gradebook, userRecord, columns, assignments, categories));
					}

					return new BasePagingLoadResult<X>(rows, startRow, totalUsers);

				case SECTION:
				case COURSE_GRADE:
				case GRADE_OVERRIDE:
				case ASSIGNMENT:

					userRecords = findLearnerRecordPage(gradebook, site, realmIds, groupReferences, groupReferenceMap, null, searchField, searchCriteria, -1, -1, !isDescending, includeCMId);

					Map<String, UserRecord> userRecordMap = new HashMap<String, UserRecord>();

					for (UserRecord userRecord : userRecords) {
						userRecordMap.put(userRecord.getUserUid(), userRecord);
					}

					List<String> studentUids = new ArrayList<String>(userRecordMap.keySet());

					userRecords = doSearchAndSortUserRecords(gradebook, assignments, categories, studentUids, userRecordMap, config);
					totalUsers = userRecords.size();
					break;
			}
		}

		int startRow = 0;
		int lastRow = totalUsers;

		if (config != null) {
			startRow = config.getOffset();
			lastRow = startRow + config.getLimit();
		}

		if (lastRow > totalUsers) {
			lastRow = totalUsers;
		}

		List<FixedColumnModel> columns = getColumns();

		// We only want to populate the rowData and rowValues for the requested
		// rows
		for (int row = startRow; row < lastRow; row++) {
			// Everything is indexed by the user, since it's by user id that the
			// rows are distinguished
			UserRecord userRecord = userRecords.get(row);

			// Populate the user record on the fly if necessary
			if (!userRecord.isPopulated()) {
				User user = null;
				try {
					user = userService.getUser(userRecord.getUserUid());
					userRecord.setUserEid(user.getEid());
					userRecord.setDisplayId(user.getDisplayId());
					userRecord.setDisplayName(user.getDisplayName());
					userRecord.setLastNameFirst(user.getSortName());
					userRecord.setSortName(user.getSortName());
					userRecord.setEmail(user.getEmail());
				} catch (UserNotDefinedException e) {
					log.error("No sakai user defined for this member '" + userRecord.getUserUid() + "'", e);
				}
			}

			rows.add((X) buildStudentRow(gradebook, userRecord, columns, assignments, categories));
		}

		return new BasePagingLoadResult<X>(rows, startRow, totalUsers);
	}

	public SubmissionVerificationModel getSubmissionVerification(String gradebookUid, Long gradebookId) {

		String[] roleNames = advisor.getLearnerRoleNames();
		Site site = getSite();
		String siteId = site == null ? null : site.getId();
		String[] realmIds = new String[1];
		realmIds[0] = new StringBuffer().append("/site/").append(siteId).toString();

		verifyUserDataIsUpToDate(site, roleNames);

		List<UserDereference> dereferences = gbService.getUserDereferences(realmIds, "sortName", null, null, -1, -1, true, roleNames);

		// List<AssignmentGradeRecord> records =
		// gbService.getAllAssignmentGradeRecords(gradebookId, realmIds,
		// roleNames);

		Gradebook gradebook = gbService.getGradebook(gradebookId);

		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
		boolean isMissingScores = false;

		if (dereferences != null) {
			for (UserDereference dereference : dereferences) {

				if (gbService.isStudentMissingScores(gradebookId, dereference.getUserUid(), hasCategories)) {
					isMissingScores = true;
					break;
				}
			}
		}

		int numberOfLearners = dereferences == null ? 0 : dereferences.size();

		return new SubmissionVerificationModel(numberOfLearners, isMissingScores);
	}

	public StudentModel scoreNumericItem(String gradebookUid, StudentModel student, String assignmentId, Double value, Double previousValue) throws InvalidInputException {

		Assignment assignment = gbService.getAssignment(Long.valueOf(assignmentId));
		Gradebook gradebook = assignment.getGradebook();

		ActionRecord actionRecord = new ActionRecord(gradebook.getUid(), gradebook.getId(), EntityType.GRADE_RECORD.name(), ActionType.GRADED.name());
		actionRecord.setEntityName(new StringBuilder().append(student.getDisplayName()).append(" : ").append(assignment.getName()).toString());
		actionRecord.setEntityId(String.valueOf(assignment.getId()));
		Map<String, String> propertyMap = actionRecord.getPropertyMap();

		propertyMap.put("score", String.valueOf(value));

		List<AssignmentGradeRecord> gradeRecords = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), student.getIdentifier());

		AssignmentGradeRecord assignmentGradeRecord = null;

		for (AssignmentGradeRecord currentGradeRecord : gradeRecords) {
			Assignment a = currentGradeRecord.getAssignment();
			if (a.getId().equals(assignment.getId()))
				assignmentGradeRecord = currentGradeRecord;
		}

		if (assignmentGradeRecord == null) {
			assignmentGradeRecord = new AssignmentGradeRecord();
			// gradeRecords.add(assignmentGradeRecord);
		}

		scoreItem(gradebook, assignment, assignmentGradeRecord, student.getIdentifier(), value, false, false);

		gradeRecords = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), student.getIdentifier());

		refreshLearnerData(gradebook, student, assignment, gradeRecords);
		student.set(assignmentId, value);

		gbService.storeActionRecord(actionRecord);

		return student;
	}

	public StudentModel scoreTextItem(String gradebookUid, StudentModel student, String property, String value, String previousValue) throws InvalidInputException {

		if (value != null && value.trim().equals(""))
			value = null;

		if (value != null)
			value = value.toUpperCase();

		// FIXME: Currently only handles grade override edits -- this should
		// handle non-numeric grades too
		Gradebook gradebook = gbService.getGradebook(gradebookUid);
		CourseGradeRecord courseGradeRecord = gbService.getStudentCourseGradeRecord(gradebook, student.getIdentifier());
		courseGradeRecord.setEnteredGrade(value);
		Collection<CourseGradeRecord> gradeRecords = new LinkedList<CourseGradeRecord>();
		gradeRecords.add(courseGradeRecord);
		// FIXME: We shouldn't be looking up the CourseGrade if we don't use it
		// anywhere.
		CourseGrade courseGrade = gbService.getCourseGrade(gradebook.getId());

		GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
		Set<String> scaledGrades = gradeMapping.getGradeMap().keySet();

		if (value != null && !advisor.isValidOverrideGrade(value, student.getEid(), student.getStudentDisplayId(), gradebook, scaledGrades))
			throw new InvalidInputException("This is not a valid override grade for this individual in this course.");

		gbService.updateCourseGradeRecords(courseGrade, gradeRecords);

		List<Category> categories = null;
		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

		Map<Long, AssignmentGradeRecord> studentGradeMap = new HashMap<Long, AssignmentGradeRecord>();
		List<AssignmentGradeRecord> records = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), student.getIdentifier());

		if (records != null) {
			for (AssignmentGradeRecord record : records) {
				studentGradeMap.put(record.getAssignment().getId(), record);
			}
		}

		String freshCourseGrade = getDisplayGrade(gradebook, student.getIdentifier(), courseGradeRecord, assignments, categories, studentGradeMap);// requestCourseGrade(gradebookUid,
		// student.getIdentifier());
		student.set(StudentModel.Key.GRADE_OVERRIDE.name(), courseGradeRecord.getEnteredGrade());
		student.set(StudentModel.Key.COURSE_GRADE.name(), freshCourseGrade);

		return student;
	}

	public void submitFinalGrade(List<Map<Column, String>> studentDataList, String gradebookUid, HttpServletRequest request, HttpServletResponse response) {

		advisor.submitFinalGrade(studentDataList, gradebookUid, request, response);
	}

	/**
	 * Method to update an item model
	 * 
	 * Business rules: (1) If points is null, set points to 100 (2) If weight is
	 * null, set weight to be equivalent to points value -- needs to happen
	 * after #1
	 * 
	 * - When category type is "No Categories": (3) updated item name must not
	 * duplicate an active (removed = false) item name in gradebook, otherwise
	 * throw exception (NoDuplicateItemNamesRule) (4) must not include an item
	 * in grading that has been deleted (removed = true)
	 * 
	 * - When category type is "Categories" or "Weighted Categories" (5) new
	 * item name must not duplicate an active (removed = false) item name in the
	 * same category, otherwise throw exception (6) must not include an item in
	 * grading that has been deleted (removed = true) or that has a category
	 * that has been deleted (removed = true) (7) if item is "included" and
	 * category has "equal weighting" then recalculate all item weights for this
	 * category (8) item must include a valid category id (9) if category has
	 * changed, then if the old category had equal weighting and the item was
	 * included in that category, then recalculate all item weights for that
	 * category (10) if item order changes, re-order remaining items for that
	 * category (11) if category is not included, then cannot include item (12)
	 * if category is removed, then cannot unremove item
	 * 
	 * @param item
	 * @return
	 * @throws InvalidInputException
	 */
	public ItemModel updateItemModel(ItemModel item) throws InvalidInputException {

		switch (item.getItemType()) {
			case CATEGORY:
				return updateCategoryModel(item);
			case GRADEBOOK:
				return updateGradebookModel(item);
		}

		boolean isWeightChanged = false;
		boolean havePointsChanged = false;

		Long assignmentId = Long.valueOf(item.getIdentifier());
		Assignment assignment = gbService.getAssignment(assignmentId);

		Category oldCategory = null;
		Category category = assignment.getCategory();

		Gradebook gradebook = assignment.getGradebook();

		if (category == null)
			category = findDefaultCategory(gradebook.getId());

		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
		boolean hasCategoryChanged = false;

		if (hasCategories) {
			if (item.getCategoryId() == null && item.getCategoryName() != null) {
				ItemModel newCategory = new ItemModel();
				newCategory.setName(item.getCategoryName());
				newCategory.setIncluded(Boolean.TRUE);
				newCategory = addItemCategory(gradebook.getUid(), gradebook.getId(), newCategory);
				item.setCategoryId(newCategory.getCategoryId());
				hasCategoryChanged = true;
			} else
				hasCategoryChanged = !category.getId().equals(item.getCategoryId());
		}

		ActionRecord actionRecord = new ActionRecord(gradebook.getUid(), gradebook.getId(), EntityType.ITEM.name(), ActionType.UPDATE.name());
		actionRecord.setEntityName(assignment.getName());
		actionRecord.setEntityId(String.valueOf(assignment.getId()));

		Map<String, String> propertyMap = actionRecord.getPropertyMap();
		for (String propertyName : item.getPropertyNames()) {
			Object value = item.get(propertyName);
			if (value != null)
				propertyMap.put(propertyName, String.valueOf(value));
		}

		boolean isRemoved = false;
		
		List<Assignment> assignments = null;
		try {

			// Check to see if the category id has changed -- this means the
			// user switched the item's category
			if (hasCategories && hasCategoryChanged)
				oldCategory = category;

			boolean wasExtraCredit = DataTypeConversionUtil.checkBoolean(assignment.isExtraCredit());
			boolean isExtraCredit = DataTypeConversionUtil.checkBoolean(item.getExtraCredit());

			isWeightChanged = wasExtraCredit != isExtraCredit;

			// Business rule #1
			Double points = null;
			Double oldPoints = assignment.getPointsPossible();
			if (item.getPoints() == null)
				points = Double.valueOf(100.0d);
			else
				points = convertDouble(item.getPoints());

			havePointsChanged = points != null && oldPoints != null && points.compareTo(oldPoints) != 0;

			Double newAssignmentWeight = item.getPercentCategory();
			Double oldAssignmentWeight = assignment.getAssignmentWeighting();

			Integer newItemOrder = item.getItemOrder();
			Integer oldItemOrder = assignment.getItemOrder();

			isWeightChanged = isWeightChanged || DataTypeConversionUtil.notEquals(newAssignmentWeight, oldAssignmentWeight);

			boolean isUnweighted = !convertBoolean(item.getIncluded()).booleanValue();
			boolean wasUnweighted = DataTypeConversionUtil.checkBoolean(assignment.isUnweighted());

			isRemoved = convertBoolean(item.getRemoved()).booleanValue();
			boolean wasRemoved = assignment.isRemoved();

			// We only want to update the weights when we're dealing with an
			// included item
			if (!isUnweighted && !isRemoved) {
				// Business rule #2
				assignment.setAssignmentWeighting(gradeCalculations.calculateItemWeightAsPercentage(newAssignmentWeight, points));
			} else {
				newAssignmentWeight = oldAssignmentWeight;
			}

			isWeightChanged = isWeightChanged || isUnweighted != wasUnweighted;
			isWeightChanged = isWeightChanged || isRemoved != wasRemoved;

			if (hasCategories && category != null) {
				if (hasCategoryChanged) {
					category = gbService.getCategory(item.getCategoryId());
					assignment.setCategory(category);
				}
				
				boolean isCategoryIncluded = !DataTypeConversionUtil.checkBoolean(category.isUnweighted());
				assignments = gbService.getAssignmentsForCategory(category.getId());

				// Business rule #12
				businessLogic.applyCannotUnremoveItemWithRemovedCategory(isRemoved, category);

				// Business rule #5
				businessLogic.applyNoDuplicateItemNamesWithinCategoryRule(item.getCategoryId(), item.getName(), assignment.getId(), assignments);

				// Business rule #6
				businessLogic.applyCannotIncludeDeletedItemRule(wasRemoved && isRemoved, category.isRemoved(), isUnweighted);

				// Business rule #11
				if (!hasCategoryChanged)
					businessLogic.applyCannotIncludeItemFromUnincludedCategoryRule(isCategoryIncluded, !isUnweighted, !wasUnweighted);

				// Business rule #8
				businessLogic.applyMustIncludeCategoryRule(item.getCategoryId());

				/*if (hasCategoryChanged) {
					category = gbService.getCategory(item.getCategoryId());
					assignment.setCategory(category);
				}*/

			} else {
				assignments = gbService.getAssignments(gradebook.getId());

				// Business rule #3
				businessLogic.applyNoDuplicateItemNamesRule(gradebook.getId(), item.getName(), assignment.getId(), assignments);

				// Business rule #4
				businessLogic.applyCannotIncludeDeletedItemRule(wasRemoved && isRemoved, false, isUnweighted);

			}

			// If we don't know the old item order then we need to determine it
			if (oldItemOrder == null) {
				if (assignments != null) {
					int count = 0;
					for (Assignment a : assignments) {
						if (a.isRemoved())
							continue;
						
						if (a.getId().equals(assignmentId))
							oldItemOrder = Integer.valueOf(count);
						count++;
					}
				}
			}

			if ((!hasCategories || oldCategory == null) && oldItemOrder != null 
					&& newItemOrder != null && oldItemOrder.compareTo(newItemOrder) < 0)
				newItemOrder = Integer.valueOf(newItemOrder.intValue() - 1);

			if (newItemOrder == null)
				newItemOrder = oldItemOrder;
			
			// Modify the assignment name
			assignment.setName(convertString(item.getName()));

			assignment.setExtraCredit(Boolean.valueOf(isExtraCredit));
			assignment.setReleased(convertBoolean(item.getReleased()).booleanValue());
			assignment.setPointsPossible(points);
			assignment.setDueDate(convertDate(item.getDueDate()));
			assignment.setRemoved(isRemoved);
			assignment.setUnweighted(Boolean.valueOf(isUnweighted || isRemoved));
			assignment.setItemOrder(newItemOrder);
			gbService.updateAssignment(assignment);

			if (hasCategories) {

				List<Assignment> oldAssignments = null;

				boolean applyBusinessRule7 = isUnweighted != wasUnweighted || isRemoved != wasRemoved || isExtraCredit != wasExtraCredit || oldCategory != null;

				// Business rule #9
				if (oldCategory != null && businessLogic.checkRecalculateEqualWeightingRule(oldCategory)) {
					oldAssignments = gbService.getAssignmentsForCategory(oldCategory.getId());
					recalculateAssignmentWeights(oldCategory, Boolean.valueOf(!wasUnweighted), oldAssignments);
				}

				// Business rule #7 -- only apply this rule when
				// included/unincluded, deleted/undeleted, made
				// extra-credit/non-extra-credit, or changed category
				if (applyBusinessRule7) {
					isWeightChanged = true;
					if (businessLogic.checkRecalculateEqualWeightingRule(category)) {
						assignments = gbService.getAssignmentsForCategory(category.getId());
						recalculateAssignmentWeights(category, !isUnweighted, assignments);
					}
				}

				boolean applyBusinessRule10 = oldItemOrder == null || newItemOrder.compareTo(oldItemOrder) != 0 || oldCategory != null;

				if (applyBusinessRule10)
					businessLogic.reorderAllItemsInCategory(assignmentId, category, oldCategory, newItemOrder, oldItemOrder);
			} else {

				if (oldItemOrder == null || (newItemOrder != null && newItemOrder.compareTo(oldItemOrder) != 0))
					businessLogic.reorderAllItems(gradebook.getId(), assignment.getId(), newItemOrder, oldItemOrder);

			}

			if (businessLogic.checkRecalculatePointsRule(assignmentId, points, oldPoints))
				recalculateAssignmentGradeRecords(assignment, points, oldPoints);

		} catch (RuntimeException e) {
			actionRecord.setStatus(ActionRecord.STATUS_FAILURE);
			throw e;
		} finally {
			gbService.storeActionRecord(actionRecord);
		}

		// The first case is that we're in categories mode and the category has
		// changed
		if (hasCategories && (oldCategory != null || isRemoved)) {
			assignments = gbService.getAssignments(gradebook.getId());
			List<Category> categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
			return getItemModel(gradebook, assignments, categories, null, assignment.getId());
		}

		// If neither the weight nor the points have changed, then we can just
		// return
		// the item model itself
		if (!isWeightChanged && !havePointsChanged) {
			ItemModel itemModel = createItemModel(category, assignment, null);
			itemModel.setActive(true);
			return itemModel;
		} else if (!hasCategories) {
			assignments = gbService.getAssignments(gradebook.getId());
			// Otherwise if we're in no categories mode then we want to return
			// the gradebook
			return getItemModel(gradebook, assignments, null, null, assignment.getId());
		}

		// Otherwise we can return the category parent
		ItemModel categoryItemModel = getItemModelsForCategory(category, item.getParent(), assignment.getId());

		String assignmentIdAsString = String.valueOf(assignment.getId());
		for (ModelData model : categoryItemModel.getChildren()) {
			ItemModel itemModel = (ItemModel) model;
			if (itemModel.getIdentifier().equals(assignmentIdAsString))
				itemModel.setActive(true);
		}

		return categoryItemModel;
	}

	public <X extends BaseModel> List<X> updateGradeScaleField(String gradebookUid, Object value, String affectedLetterGrade) {

		// FIXME: Need to store action record for this change.

		List<X> gradeScaleMappings = new ArrayList<X>();
		Gradebook gradebook = gbService.getGradebook(gradebookUid);
		GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
		GradingScale gradingScale = gradeMapping.getGradingScale();
		// Map<String, Double> gradingScaleMap =
		// gradingScale.getDefaultBottomPercents();
		// Map<String, Double> newGradingScaleMap = new HashMap<String,
		// Double>();
		List<String> letterGradesList = new ArrayList<String>(gradeMapping.getGradeMap().keySet());

		Collections.sort(letterGradesList, LETTER_GRADE_COMPARATOR);

		Double upperScale = null;

		GradeScaleRecordModel gradeScaleModel = null;

		for (String letterGrade : letterGradesList) {

			upperScale = (null == upperScale) ? new Double(100d) : upperScale.equals(Double.valueOf(0d)) ? Double.valueOf(0d) : Double.valueOf(upperScale.doubleValue() - 0.00001d);

			if (affectedLetterGrade.equals(letterGrade)) {
				gradeScaleModel = new GradeScaleRecordModel(letterGrade, (Double) value, upperScale);
				gradeMapping.getGradeMap().put(letterGrade, (Double) value);
				upperScale = (Double) value;
			} else {
				gradeScaleModel = new GradeScaleRecordModel(letterGrade, gradeMapping.getGradeMap().get(letterGrade), upperScale);
				// gradeMapping.getGradeMap().put(letterGrade,
				// gradingScaleMap.get(letterGrade));
				upperScale = gradeMapping.getGradeMap().get(letterGrade);
			}

			gradeScaleMappings.add((X) gradeScaleModel);
		}

		// gradingScale.setDefaultBottomPercents(newGradingScaleMap);
		// gradebook.setSelectedGradeMapping(new GradeMapping(gradingScale));
		// gbService.saveOrUpdateLetterGradePercentMapping(newGradingScaleMap,
		// gradebook);
		gbService.updateGradebook(gradebook);

		return gradeScaleMappings;
	}

	public <X extends BaseModel> ListLoadResult<X> getUsers() {

		List<X> userList = new ArrayList<X>();

		String placementId = lookupDefaultGradebookUid();
		List<ParticipationRecord> participationList = sectionAwareness.getSiteMembersInRole(placementId, Role.TA);

		for (ParticipationRecord participationRecord : participationList) {

			org.sakaiproject.section.api.coursemanagement.User user = participationRecord.getUser();
			UserModel userModel = new UserModel(user.getUserUid(), user.getDisplayName());
			userList.add((X) userModel);
		}

		ListLoadResult<X> result = new BaseListLoadResult<X>(userList);
		return result;
	}

	/*
	 * PROTECTED METHODS
	 */
	protected String getPlacementId() {

		if (toolManager == null)
			return null;

		Placement placement = toolManager.getCurrentPlacement();

		if (placement == null)
			return null;

		return placement.getId();
	}

	/*
	 * PRIVATE METHODS
	 */

	private Map<String, Object> appendItemData(Long assignmentId, Map<String, Object> cellMap, UserRecord userRecord, Gradebook gradebook) {

		AssignmentGradeRecord gradeRecord = null;

		String id = String.valueOf(assignmentId);
		// String id = item.getIdentifier();
		// Long assignmentId = Long.valueOf(id);

		boolean isCommented = userRecord.getCommentMap() != null && userRecord.getCommentMap().get(assignmentId) != null;

		if (isCommented) {
			cellMap.put(concat(id, StudentModel.COMMENTED_FLAG), Boolean.TRUE);
			cellMap.put(concat(id, StudentModel.COMMENT_TEXT_FLAG), userRecord.getCommentMap().get(assignmentId).getCommentText());
		}

		Map<Long, AssignmentGradeRecord> studentGradeMap = userRecord.getGradeRecordMap();

		if (studentGradeMap != null) {
			gradeRecord = studentGradeMap.get(assignmentId);

			if (gradeRecord != null) {
				boolean isExcused = gradeRecord.isExcluded() != null && gradeRecord.isExcluded().booleanValue();
				boolean isDropped = gradeRecord.isDropped() != null && gradeRecord.isDropped().booleanValue();

				if (isDropped || isExcused)
					cellMap.put(concat(id, StudentModel.DROP_FLAG), Boolean.TRUE);

				if (isExcused)
					cellMap.put(concat(id, StudentModel.EXCUSE_FLAG), Boolean.TRUE);

				switch (gradebook.getGrade_type()) {
					case GradebookService.GRADE_TYPE_POINTS:
						cellMap.put(id, gradeRecord.getPointsEarned());
						break;
					case GradebookService.GRADE_TYPE_PERCENTAGE:
						BigDecimal percentage = gradeCalculations.getPointsEarnedAsPercent((Assignment) gradeRecord.getGradableObject(), gradeRecord);
						Double percentageDouble = percentage == null ? null : Double.valueOf(percentage.doubleValue());
						cellMap.put(id, percentageDouble);
						break;
					case GradebookService.GRADE_TYPE_LETTER:
						cellMap.put(id, "No letter grades");
						break;
					default:
						cellMap.put(id, "Not implemented");
						break;
				}
			}
		}

		return cellMap;
	}

	private StudentModel buildStudentRow(Gradebook gradebook, UserRecord userRecord, List<FixedColumnModel> columns, List<Assignment> assignments, List<Category> categories) {

		Map<Long, AssignmentGradeRecord> studentGradeMap = userRecord.getGradeRecordMap();

		// This is an intermediate map for data to be placed in the record
		Map<String, Object> cellMap = new HashMap<String, Object>();

		// This is how we track which column is which - by the user's uid
		cellMap.put(StudentModel.Key.UID.name(), userRecord.getUserUid());
		cellMap.put(StudentModel.Key.EID.name(), userRecord.getUserEid());
		cellMap.put(StudentModel.Key.EXPORT_CM_ID.name(), userRecord.getExportCourseManagemntId());
		cellMap.put(StudentModel.Key.EXPORT_USER_ID.name(), userRecord.getExportUserId());
		cellMap.put(StudentModel.Key.FINAL_GRADE_USER_ID.name(), userRecord.getFinalGradeUserId());
		// Need this to show the grade override
		CourseGradeRecord courseGradeRecord = userRecord.getCourseGradeRecord(); // gradebookManager.getStudentCourseGradeRecord(gradebook,
		// userRecord.getUserUid());

		String enteredGrade = null;
		String displayGrade = null;

		if (courseGradeRecord != null)
			enteredGrade = courseGradeRecord.getEnteredGrade();

		if (userRecord.isCalculated())
			displayGrade = userRecord.getDisplayGrade();
		else
			displayGrade = getDisplayGrade(gradebook, userRecord.getUserUid(), courseGradeRecord, assignments, categories, studentGradeMap);

		if (columns != null) {
			for (FixedColumnModel column : columns) {
				StudentModel.Key key = StudentModel.Key.valueOf(column.getKey());
				switch (key) {
					case DISPLAY_ID:
						cellMap.put(StudentModel.Key.DISPLAY_ID.name(), userRecord.getDisplayId());
						break;
					case DISPLAY_NAME:
						// For the single view, maybe some redundancy, but not
						// much
						String displayName = userRecord.getDisplayName();

						if (displayName == null)
							displayName = "[User name not found]";

						cellMap.put(StudentModel.Key.DISPLAY_NAME.name(), displayName);
						cellMap.put(StudentModel.Key.LAST_NAME_FIRST.name(), userRecord.getLastNameFirst());
						cellMap.put(StudentModel.Key.EMAIL.name(), userRecord.getEmail());
						break;
					case SECTION:
						cellMap.put(StudentModel.Key.SECTION.name(), userRecord.getSectionTitle());
						break;
					case COURSE_GRADE:
						if (displayGrade != null)
							cellMap.put(StudentModel.Key.COURSE_GRADE.name(), displayGrade);
						break;
					case GRADE_OVERRIDE:
						cellMap.put(StudentModel.Key.GRADE_OVERRIDE.name(), enteredGrade);
						break;
				}
				;
			}
		}

		if (assignments != null) {

			for (Assignment assignment : assignments) {
				cellMap = appendItemData(assignment.getId(), cellMap, userRecord, gradebook);
			}

		} else {

			for (AssignmentGradeRecord gradeRecord : studentGradeMap.values()) {
				Assignment assignment = gradeRecord.getAssignment();
				cellMap = appendItemData(assignment.getId(), cellMap, userRecord, gradebook);
			}

		}

		return new StudentModel(cellMap);
	}

	// FIXME: This should be moved into GradeCalculations
	private void calculateItemCategoryPercent(Gradebook gradebook, Category category, ItemModel gradebookItemModel, ItemModel categoryItemModel, List<Assignment> assignments, Long assignmentId) {

		double pG = categoryItemModel == null || categoryItemModel.getPercentCourseGrade() == null ? 0d : categoryItemModel.getPercentCourseGrade().doubleValue();

		boolean isCategoryExtraCredit = category != null && DataTypeConversionUtil.checkBoolean(category.isExtraCredit());
		BigDecimal percentGrade = BigDecimal.valueOf(pG);
		BigDecimal percentCategorySum = BigDecimal.ZERO;
		BigDecimal pointsSum = BigDecimal.ZERO;
		if (assignments != null) {
			for (Assignment a : assignments) {
				double assignmentCategoryPercent = a.getAssignmentWeighting() == null ? 0.0 : a.getAssignmentWeighting().doubleValue() * 100.0;
				BigDecimal points = BigDecimal.valueOf(a.getPointsPossible().doubleValue());

				boolean isRemoved = a.isRemoved();
				boolean isExtraCredit = a.isExtraCredit() != null && a.isExtraCredit().booleanValue();
				boolean isUnweighted = a.isUnweighted() != null && a.isUnweighted().booleanValue();

				if ((isCategoryExtraCredit || !isExtraCredit) && !isUnweighted && !isRemoved) {
					percentCategorySum = percentCategorySum.add(BigDecimal.valueOf(assignmentCategoryPercent));
					pointsSum = pointsSum.add(points);
				}

			}

			for (Assignment a : assignments) {

				boolean isUnweighted = a.isUnweighted() != null && a.isUnweighted().booleanValue();

				BigDecimal courseGradePercent = BigDecimal.ZERO;
				if (!isUnweighted) {
					double w = a == null || a.getAssignmentWeighting() == null ? 0d : a.getAssignmentWeighting().doubleValue();
					BigDecimal assignmentWeight = BigDecimal.valueOf(w);
					courseGradePercent = gradeCalculations.calculateItemGradePercent(percentGrade, percentCategorySum, assignmentWeight);
				}

				ItemModel assignmentItemModel = createItemModel(category, a, courseGradePercent);

				if (assignmentId != null && a.getId().equals(assignmentId))
					assignmentItemModel.setActive(true);

				if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY) {
					assignmentItemModel.setParent(gradebookItemModel);
					gradebookItemModel.add(assignmentItemModel);
				} else {
					assignmentItemModel.setParent(categoryItemModel);
					categoryItemModel.add(assignmentItemModel);
				}

			}

		}

		if (gradebookItemModel != null) {
			gradebookItemModel.setPoints(Double.valueOf(pointsSum.doubleValue()));
		}

		if (categoryItemModel != null) {
			categoryItemModel.setPercentCategory(Double.valueOf(percentCategorySum.doubleValue()));
			categoryItemModel.setPoints(Double.valueOf(pointsSum.doubleValue()));
		}
	}

	private GradebookModel createGradebookModel(Gradebook gradebook, List<Assignment> assignments, List<Category> categories, boolean isNewGradebook) {

		Site site = null;
		
		if (siteService != null) {
			try {
				site = siteService.getSite(getSiteContext());
			
				if (site.getId().equals(gradebook.getName())) {
					gradebook.setName("My Default Gradebook");
				}
			
			} catch (IdUnusedException e) {
				log.error("Unable to find the current site", e);
			}
		}
		
		
		GradebookModel model = new GradebookModel();
		model.setNewGradebook(Boolean.valueOf(isNewGradebook));
		String gradebookUid = gradebook.getUid();

		CategoryType categoryType = null;

		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
				categoryType = CategoryType.NO_CATEGORIES;
				break;
			case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
				categoryType = CategoryType.SIMPLE_CATEGORIES;
				break;
			case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
				categoryType = CategoryType.WEIGHTED_CATEGORIES;
		}

		boolean isUserAbleToGrade = authz.isUserAbleToGrade(gradebookUid);
		boolean isUserAbleToViewOwnGrades = authz.isUserAbleToViewOwnGrades(gradebookUid);

		boolean isSingleUserView = isUserAbleToViewOwnGrades && !isUserAbleToGrade;

		
		model.setCategoryType(categoryType);

		model.setGradebookUid(gradebookUid);
		model.setGradebookId(gradebook.getId());
		model.setName(gradebook.getName());

		// GRBK-233 create new assignment and category list
		ItemModel gradebookItemModel = null;
		
		if(null != categories) {
			List<Category> filteredCategories = new ArrayList<Category>();
			List<Assignment> filteredAssignments = assignments;

			for(Category category : categories) {

				// First we check if the user has view permission for the category
				boolean canView = isSingleUserView || authz.canUserViewCategory(gradebook.getUid(), category.getId());
				if(canView) {
					filteredCategories.add(category);
				}
				else {
					// User has no view permission, so let's check if user has grader permission for the category
					boolean canGrade = authz.canUserGradeCategory(gradebook.getUid(), category.getId());
					if(canGrade) {
						filteredCategories.add(category);
					}
				}

				if(null != assignments) {
					// Since the user doesn't have permission to either view or grade the category, we 
					// need to remove any associated assignments
					List<Assignment> tempAssignments = new ArrayList<Assignment>();
					if (filteredAssignments != null) {
						for(Assignment assignment : filteredAssignments) {
	
							if (assignment.getCategory() != null) {
								if(!assignment.getCategory().getId().equals(category.getId())) {
									
									if (!isSingleUserView || assignment.isReleased())
										tempAssignments.add(assignment);
									
								}
							}
						}
					}
					filteredAssignments = tempAssignments;
				}

			}
			
			gradebookItemModel = getItemModel(gradebook, filteredAssignments, filteredCategories, null, null);
		}
		else {
			gradebookItemModel = getItemModel(gradebook, assignments, categories, null, null);
		}

		
		model.setGradebookItemModel(gradebookItemModel);
		List<FixedColumnModel> columns = getColumns();

		model.setUserAbleToGrade(isUserAbleToGrade);
		model.setUserAbleToEditAssessments(authz.isUserAbleToEditAssessments(gradebookUid));
		model.setUserAbleToViewOwnGrades(isUserAbleToViewOwnGrades);
		model.setUserHasGraderPermissions(authz.hasUserGraderPermissions(gradebookUid));

		ConfigurationModel configModel = new ConfigurationModel();

		if (userService != null) {
			User user = userService.getCurrentUser();

			if (user != null) {

				List<UserConfiguration> configs = gbService.getUserConfigurations(user.getId(), gradebook.getId());

				if (configs != null) {
					for (UserConfiguration config : configs) {
						configModel.set(config.getConfigField(), config.getConfigValue());
					}
				}

				// Don't take the hit of looking this stuff up unless we're in
				// single user view
				if (isSingleUserView) {

					UserRecord userRecord = new UserRecord(user);
					if (site != null) {
						
						Collection<Group> groups = site.getGroupsWithMember(user.getId());
						if (!groups.isEmpty()) {
							for (Group group : groups) {
								// FIXME: We probably don't just want to grab
								// the first group the user is in
								userRecord.setSectionTitle(group.getTitle());
								break;
							}
						}
						
					}

					List<AssignmentGradeRecord> records = gbService.getAssignmentGradeRecordsForStudent(gradebook.getId(), userRecord.getUserUid());

					if (records != null) {
						for (AssignmentGradeRecord gradeRecord : records) {
							gradeRecord.setUserAbleToView(true);
							String studentUid = gradeRecord.getStudentId();
							Map<Long, AssignmentGradeRecord> studentMap = userRecord.getGradeRecordMap();
							if (studentMap == null) {
								studentMap = new HashMap<Long, AssignmentGradeRecord>();
								userRecord.setGradeRecordMap(studentMap);
							}
							GradableObject go = gradeRecord.getGradableObject();
							studentMap.put(go.getId(), gradeRecord);
						}
					}

					model.setUserAsStudent(buildStudentRow(gradebook, userRecord, columns, assignments, categories));
				}

				model.setUserName(user.getDisplayName());
			}
		} else {
			String[] realmIds = { "/site/mock" };
			List<UserRecord> userRecords = findLearnerRecordPage(gradebook, getSite(), realmIds, null, null, null, null, null, -1, -1, true, false);

			// Map<String, UserRecord> userRecordMap =
			// //findStudentRecords(gradebookUid, gradebook.getId(), null,
			// null);

			if (userRecords != null && userRecords.size() > 0) {
				UserRecord userRecord = userRecords.get(0);
				model.setUserName(userRecord.getDisplayName());
				model.setUserAsStudent(buildStudentRow(gradebook, userRecord, columns, assignments, categories));
			}
		}

		model.setConfigurationModel(configModel);
		model.setColumns(columns);

		return model;
	}

	private ItemModel createItemModel(Gradebook gradebook) {

		ItemModel itemModel = new ItemModel();
		itemModel.setName(gradebook.getName());
		itemModel.setItemType(Type.GRADEBOOK);
		itemModel.setIdentifier(gradebook.getUid());

		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
				itemModel.setCategoryType(CategoryType.NO_CATEGORIES);
				break;
			case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
				itemModel.setCategoryType(CategoryType.SIMPLE_CATEGORIES);
				break;
			case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
				itemModel.setCategoryType(CategoryType.WEIGHTED_CATEGORIES);
				break;
		}
		switch (gradebook.getGrade_type()) {
			case GradebookService.GRADE_TYPE_POINTS:
				itemModel.setGradeType(GradeType.POINTS);
				break;
			case GradebookService.GRADE_TYPE_PERCENTAGE:
				itemModel.setGradeType(GradeType.PERCENTAGES);
				break;
			case GradebookService.GRADE_TYPE_LETTER:
				itemModel.setGradeType(GradeType.LETTERS);
				break;
		}

		itemModel.setReleaseGrades(Boolean.valueOf(gradebook.isCourseGradeDisplayed()));
		itemModel.setReleaseItems(Boolean.valueOf(gradebook.isAssignmentsDisplayed()));
		itemModel.setGradeScaleId(gradebook.getSelectedGradeMapping().getId());

		return itemModel;
	}

	private ItemModel createItemModel(Gradebook gradebook, Category category, List<Assignment> assignments) {

		ItemModel model = new ItemModel();

		boolean isDefaultCategory = category.getName().equalsIgnoreCase(AppConstants.DEFAULT_CATEGORY_NAME);

		double categoryWeight = category.getWeight() == null ? 0d : category.getWeight().doubleValue() * 100d;
		boolean isIncluded = category.isUnweighted() == null ? !isDefaultCategory : !isDefaultCategory && !category.isUnweighted().booleanValue();

		// if (! isIncluded || category.isRemoved())
		// categoryWeight = 0d;

		if (gradebook != null)
			model.setGradebook(gradebook.getName());
		// model.setIdentifier(new
		// StringBuilder().append(AppConstants.CATEGORY).append(String.valueOf(category.getId())).toString());
		model.setIdentifier(String.valueOf(category.getId()));
		model.setName(category.getName());
		model.setCategoryId(category.getId());
		model.setWeighting(Double.valueOf(categoryWeight));
		model.setEqualWeightAssignments(category.isEqualWeightAssignments());
		model.setExtraCredit(category.isExtraCredit() == null ? Boolean.FALSE : category.isExtraCredit());
		model.setIncluded(Boolean.valueOf(isIncluded));
		model.setDropLowest(category.getDrop_lowest() == 0 ? null : Integer.valueOf(category.getDrop_lowest()));
		model.setRemoved(Boolean.valueOf(category.isRemoved()));
		model.setPercentCourseGrade(Double.valueOf(categoryWeight));
		model.setItemType(Type.CATEGORY);
		model.setEditable(!isDefaultCategory);
		model.setItemOrder(category.getCategoryOrder());

		return model;
	}

	private ItemModel createItemModel(Category category, Assignment assignment, BigDecimal percentCourseGrade) {

		ItemModel model = new ItemModel();

		double assignmentWeight = assignment.getAssignmentWeighting() == null ? 0d : assignment.getAssignmentWeighting().doubleValue() * 100.0;
		Boolean isAssignmentIncluded = assignment.isUnweighted() == null ? Boolean.TRUE : Boolean.valueOf(!assignment.isUnweighted().booleanValue());
		Boolean isAssignmentExtraCredit = assignment.isExtraCredit() == null ? Boolean.FALSE : assignment.isExtraCredit();
		Boolean isAssignmentReleased = Boolean.valueOf(assignment.isReleased());
		Boolean isAssignmentRemoved = Boolean.valueOf(assignment.isRemoved());

		Gradebook gradebook = assignment.getGradebook();
		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;

		// We don't want to delete assignments based on category when we don't
		// have categories
		if (hasCategories && category != null) {

			if (category.isRemoved())
				isAssignmentRemoved = Boolean.TRUE;

			if (category.isUnweighted() != null && category.isUnweighted().booleanValue())
				isAssignmentIncluded = Boolean.FALSE;

			isAssignmentExtraCredit = Boolean.valueOf(DataTypeConversionUtil.checkBoolean(isAssignmentExtraCredit) || DataTypeConversionUtil.checkBoolean(category.isExtraCredit()));
		}

		// if (! isAssignmentIncluded.booleanValue() || assignment.isRemoved())
		// assignmentWeight = 0d;

		String categoryName = gradebook.getName();
		if (hasCategories && category != null) {
			categoryName = category.getName();
			model.setCategoryName(categoryName);
			model.setCategoryId(category.getId());
		}

		model.setIdentifier(String.valueOf(assignment.getId()));
		model.setName(assignment.getName());
		model.setItemId(assignment.getId());
		model.setWeighting(Double.valueOf(assignmentWeight));
		model.setReleased(isAssignmentReleased);
		model.setIncluded(isAssignmentIncluded);
		model.setDueDate(assignment.getDueDate());
		model.setPoints(assignment.getPointsPossible());
		model.setExtraCredit(isAssignmentExtraCredit);
		model.setRemoved(isAssignmentRemoved);
		model.setSource(assignment.getExternalAppName());
		model.setDataType(AppConstants.NUMERIC_DATA_TYPE);
		model.setStudentModelKey(Key.ASSIGNMENT.name());
		model.setItemOrder(assignment.getItemOrder());

		if (percentCourseGrade == null && hasCategories && category != null) {
			List<Assignment> assignments = category.getAssignmentList();

			boolean isIncluded = category.isUnweighted() == null ? true : !category.isUnweighted().booleanValue();

			double sum = 0d;
			if (assignments != null && isIncluded) {
				for (Assignment a : assignments) {
					double assignWeight = a.getAssignmentWeighting() == null ? 0.0 : a.getAssignmentWeighting().doubleValue() * 100.0;
					boolean isExtraCredit = a.isExtraCredit() != null && a.isExtraCredit().booleanValue();
					boolean isUnweighted = a.isUnweighted() != null && a.isUnweighted().booleanValue();
					if (!isExtraCredit && !isUnweighted)
						sum += assignWeight;
				}
			}
			percentCourseGrade = new BigDecimal(String.valueOf(Double.valueOf(sum)));
		}

		model.setPercentCategory(Double.valueOf(assignmentWeight));
		model.setPercentCourseGrade(Double.valueOf(percentCourseGrade.doubleValue()));
		model.setItemType(Type.ITEM);

		return model;
	}

	private CommentModel createOrUpdateCommentModel(CommentModel model, Comment comment) {

		if (comment == null)
			return null;

		if (model == null) {
			model = new CommentModel();
		}

		String graderName = "";
		if (userService != null) {
			try {
				User grader = userService.getUser(comment.getGraderId());
				if (grader != null)
					graderName = grader.getDisplayName();
			} catch (UserNotDefinedException e) {
				log.warn("Couldn't find the grader for " + comment.getGraderId());
			}
		}

		if (comment.getId() != null)
			model.setIdentifier(String.valueOf(comment.getId()));
		model.setAssignmentId(comment.getGradableObject().getId());
		model.setText(comment.getCommentText());
		model.setGraderName(graderName);
		model.setStudentUid(comment.getStudentId());

		return model;
	}

	private GradeEventModel createOrUpdateGradeEventModel(GradeEventModel model, GradingEvent event) {

		SimpleDateFormat dateFormat = new SimpleDateFormat();

		if (model == null) {
			model = new GradeEventModel();
		}

		String graderName = event.getGraderId();

		try {
			if (userService != null) {
				User grader = userService.getUser(event.getGraderId());
				if (grader != null)
					graderName = grader.getDisplayName();
			}
		} catch (UserNotDefinedException e) {
			log.info("Failed to find a user for the id " + event.getGraderId());
		}

		model.setIdentifier(String.valueOf(event.getId()));
		model.setGraderName(graderName);
		model.setGrade(event.getGrade());
		model.setDateGraded(dateFormat.format(event.getDateGraded()));

		return model;
	}

	private GradebookModel createOrRetrieveGradebookModel(String gradebookUid) {

		GradebookModel model = null;
		Gradebook gradebook = null;

		boolean isNewGradebook = false;

		try {
			// First thing, grab the default gradebook if one exists
			gradebook = gbService.getGradebook(gradebookUid);
		} catch (GradebookNotFoundException gnfe) {
			// If it doesn't exist, then create it
			if (frameworkService != null) {
				frameworkService.addGradebook(gradebookUid, "My Default Gradebook");
				gradebook = gbService.getGradebook(gradebookUid);
				isNewGradebook = true;
			}
		}

		// If we have a gradebook already, then we have to ensure that it's set
		// up correctly for the new tool
		if (gradebook != null) {

			List<Assignment> assignments = gbService.getAssignments(gradebook.getId());

			List<Category> categories = null;
			if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
				categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

			// There are different ways that unassigned assignments can appear -
			// old gradebooks, external apps
			/*
			 * List<Assignment> unassignedAssigns = new ArrayList<Assignment>();
			 * 
			 * if (assignments != null) { for (Assignment assignment :
			 * assignments) { if (assignment.getCategory() == null)
			 * unassignedAssigns.add(assignment); } }
			 * 
			 * // If we have any that are unassigned, we want to assign them to
			 * the default category if (unassignedAssigns != null &&
			 * !unassignedAssigns.isEmpty()) { // Let's see if we already have a
			 * default category in existence Long defaultCategoryId = null; if
			 * (categories != null && ! categories.isEmpty()) { // First, look
			 * for it by name for (Category category : categories) { if
			 * (category
			 * .getName().equalsIgnoreCase(AppConstants.DEFAULT_CATEGORY_NAME))
			 * { defaultCategoryId = category.getId(); break; } } }
			 * 
			 * boolean isCategoryNew = false;
			 * 
			 * // If we don't have one already, then let's create one if
			 * (defaultCategoryId == null) { defaultCategoryId =
			 * gbService.createCategory(gradebook.getId(),
			 * AppConstants.DEFAULT_CATEGORY_NAME, Double.valueOf(1d), 0, null,
			 * null, null); isCategoryNew = true; }
			 * 
			 * // TODO: This is a just in case check -- we should probably throw
			 * an exception here instead, since it means we weren't able to //
			 * TODO: create the category for some reason -- but that probably
			 * would throw an exception anyway, so... if (defaultCategoryId !=
			 * null) { Category defaultCategory =
			 * gbService.getCategory(defaultCategoryId);
			 * 
			 * // Just in case we just created it, or if it happens to have been
			 * deleted since it was created if (isCategoryNew ||
			 * defaultCategory.isRemoved()) {
			 * defaultCategory.setEqualWeightAssignments(Boolean.TRUE);
			 * defaultCategory.setRemoved(false);
			 * gbService.updateCategory(defaultCategory); }
			 * 
			 * // Assuming we have the default category by now (which we almost
			 * definitely should) then we move all the unassigned items into it
			 * if (defaultCategory != null) { for (Assignment assignment :
			 * unassignedAssigns) { // Think we need to grab each assignment
			 * again - this is stupid, but I'm pretty sure it's what hibernate
			 * requires //Assignment assignment =
			 * gbService.getAssignment(a.getId());
			 * //assignment.setCategory(defaultCategory);
			 * gbService.updateAssignment(assignment); } List<Assignment>
			 * unassignedAssignments =
			 * gbService.getAssignmentsForCategory(defaultCategory.getId()); //
			 * This will only recalculate assuming that the category has
			 * isEqualWeighting as TRUE
			 * recalculateAssignmentWeights(defaultCategory, null,
			 * unassignedAssignments); } }
			 * 
			 * }
			 */

			model = createGradebookModel(gradebook, assignments, categories, isNewGradebook);
		}

		return model;
	}

	private synchronized Category findDefaultCategory(Long gradebookId) {

		Category category = new Category();
		category.setName(AppConstants.DEFAULT_CATEGORY_NAME);
		category.setWeight(Double.valueOf(0d));
		category.setExtraCredit(Boolean.FALSE);
		category.setUnweighted(Boolean.TRUE);
		category.setId(Long.valueOf(-1l));

		return category;

		/*
		 * List<Category> categories = gbService.getCategories(gradebookId); //
		 * Let's see if we already have a default category in existence Long
		 * defaultCategoryId = null; if (categories != null &&
		 * !categories.isEmpty()) { // First, look for it by name for (Category
		 * category : categories) { if (category.getName().equalsIgnoreCase(
		 * AppConstants.DEFAULT_CATEGORY_NAME)) { defaultCategoryId =
		 * category.getId(); break; } } }
		 * 
		 * // If we don't have one already, then let's create one if
		 * (defaultCategoryId == null) { defaultCategoryId =
		 * gbService.createCategory(gradebookId,
		 * AppConstants.DEFAULT_CATEGORY_NAME, Double.valueOf(0d), 0, null,
		 * null, null); }
		 * 
		 * // TODO: This is a just in case check -- we should probably throw an
		 * // exception here instead, since it means we weren't able to // TODO:
		 * create the category for some reason -- but that probably would //
		 * throw an exception anyway, so... if (defaultCategoryId != null) {
		 * Category defaultCategory = gbService.getCategory(defaultCategoryId);
		 * 
		 * if (defaultCategory.isRemoved()) { defaultCategory.setRemoved(false);
		 * gbService.updateCategory(defaultCategory); } return defaultCategory;
		 * }
		 * 
		 * return null;
		 */
	}

	protected List<UserRecord> findLearnerRecordPage(Gradebook gradebook, Site site, String[] realmIds, List<String> groupReferences, Map<String, Group> groupReferenceMap, String sortField, String searchField, String searchCriteria,
			int offset, int limit, boolean isAscending, boolean includeCMId) {

		String[] learnerRoleKeys = advisor.getLearnerRoleNames();
		verifyUserDataIsUpToDate(site, learnerRoleKeys);

		List<UserDereference> dereferences = gbService.getUserDereferences(realmIds, sortField, searchField, searchCriteria, offset, limit, isAscending, learnerRoleKeys);
		List<AssignmentGradeRecord> allGradeRecords = gbService.getAllAssignmentGradeRecords(gradebook.getId(), realmIds, learnerRoleKeys);
		Map<String, Map<Long, AssignmentGradeRecord>> studentGradeRecordMap = new HashMap<String, Map<Long, AssignmentGradeRecord>>();

		if (allGradeRecords != null) {
			for (AssignmentGradeRecord gradeRecord : allGradeRecords) {
				gradeRecord.setUserAbleToView(true);
				String studentUid = gradeRecord.getStudentId();
				Map<Long, AssignmentGradeRecord> studentMap = studentGradeRecordMap.get(studentUid);
				if (studentMap == null) {
					studentMap = new HashMap<Long, AssignmentGradeRecord>();
				}
				GradableObject go = gradeRecord.getGradableObject();
				studentMap.put(go.getId(), gradeRecord);

				studentGradeRecordMap.put(studentUid, studentMap);
			}
		}

		List<CourseGradeRecord> courseGradeRecords = gbService.getAllCourseGradeRecords(gradebook.getId(), realmIds, sortField, searchField, searchCriteria, offset, limit, isAscending, learnerRoleKeys);
		Map<String, CourseGradeRecord> studentCourseGradeRecordMap = new HashMap<String, CourseGradeRecord>();

		if (courseGradeRecords != null) {
			for (CourseGradeRecord courseGradeRecord : courseGradeRecords) {
				String studentUid = courseGradeRecord.getStudentId();
				studentCourseGradeRecordMap.put(studentUid, courseGradeRecord);
			}
		}

		List<Comment> comments = gbService.getComments(gradebook.getId(), realmIds, learnerRoleKeys, sortField, searchField, searchCriteria, offset, limit, isAscending);
		Map<String, Map<Long, Comment>> studentItemCommentMap = new HashMap<String, Map<Long, Comment>>();

		if (comments != null) {
			for (Comment comment : comments) {
				String studentUid = comment.getStudentId();
				Map<Long, Comment> commentMap = studentItemCommentMap.get(studentUid);
				if (commentMap == null)
					commentMap = new HashMap<Long, Comment>();

				commentMap.put(comment.getGradableObject().getId(), comment);
				studentItemCommentMap.put(studentUid, commentMap);
			}
		}

		Map<String, Set<Group>> userGroupMap = new HashMap<String, Set<Group>>();
		Map<String, List<String>> groupEnrollmentSetEidsMap = new HashMap<String, List<String>>();

		List<Object[]> tuples = gbService.getUserGroupReferences(groupReferences, learnerRoleKeys);

		if (tuples != null) {
			for (Object[] tuple : tuples) {
				String userUid = (String) tuple[0];
				String realmId = (String) tuple[1];

				Group group = groupReferenceMap.get(realmId);

				if (includeCMId && advisor.isExportCourseManagementIdByGroup()) {
					List<String> enrollmentSetEids = advisor.getExportCourseManagementSetEids(group);
					if (enrollmentSetEids != null)
						groupEnrollmentSetEidsMap.put(group.getId(), enrollmentSetEids);
				}

				Set<Group> userGroups = userGroupMap.get(userUid);

				if (userGroups == null) {
					userGroups = new HashSet<Group>();
					userGroupMap.put(userUid, userGroups);
				}

				userGroups.add(group);
			}
		}

		List<UserRecord> userRecords = new ArrayList<UserRecord>();
		if (dereferences != null) {
			for (UserDereference dereference : dereferences) {
				UserRecord userRecord = new UserRecord(dereference.getUserUid(), dereference.getEid(), dereference.getDisplayId(), dereference.getDisplayName(), dereference.getLastNameFirst(), dereference.getSortName(), dereference
						.getEmail());

				Map<Long, AssignmentGradeRecord> gradeRecordMap = studentGradeRecordMap.get(dereference.getUserUid());
				userRecord.setGradeRecordMap(gradeRecordMap);
				CourseGradeRecord courseGradeRecord = studentCourseGradeRecordMap.get(dereference.getUserUid());
				userRecord.setCourseGradeRecord(courseGradeRecord);
				Map<Long, Comment> commentMap = studentItemCommentMap.get(dereference.getUserUid());
				userRecord.setCommentMap(commentMap);

				Set<Group> userGroupSet = userGroupMap.get(userRecord.getUserUid());
				if (userGroupSet != null) {
					List<Group> userGroups = new ArrayList<Group>(userGroupSet);
					Collections.sort(userGroups, new Comparator<Group>() {

						public int compare(Group o1, Group o2) {

							if (o1 != null && o2 != null && o1.getTitle() != null && o2.getTitle() != null) {
								return o1.getTitle().compareTo(o2.getTitle());
							}

							return 0;
						}

					});
					StringBuilder groupTitles = new StringBuilder();
					StringBuilder courseManagementIds = new StringBuilder();
					for (Iterator<Group> groupIter = userGroups.iterator(); groupIter.hasNext();) {
						Group group = groupIter.next();
						groupTitles.append(group.getTitle());

						if (includeCMId) {
							List<String> enrollmentSetEids = groupEnrollmentSetEidsMap.get(group.getId());
							courseManagementIds.append(advisor.getExportCourseManagementId(userRecord.getUserEid(), group, enrollmentSetEids));
						}

						if (groupIter.hasNext()) {
							groupTitles.append(",");
							courseManagementIds.append(",");
						}
					}
					userRecord.setSectionTitle(groupTitles.toString());

					if (includeCMId)
						userRecord.setExportCourseManagemntId(courseManagementIds.toString());
				}
				if (includeCMId) {
					userRecord.setExportUserId(advisor.getExportUserId(dereference));
					userRecord.setFinalGradeUserId(advisor.getFinalGradeUserId(dereference));
				}

				userRecords.add(userRecord);
			}
		}

		return userRecords;
	}

	protected List<User> findAllMembers(Site site, String[] learnerRoleKeys) {

		List<User> users = new ArrayList<User>();
		if (site != null) {
			List<String> userUids = gbService.getFullUserListForSite(site.getId(), learnerRoleKeys);

			if (userService != null && userUids != null)
				users = userService.getUsers(userUids);

			/*
			 * Set<Member> members = site == null ? new HashSet<Member>() :
			 * site.getMembers(); List<String> learnerRoleKeySet =
			 * Arrays.asList(learnerRoleKeys); if (members != null) {
			 * Set<String> userUids = new HashSet<String>(); for (Member member
			 * : members) { String userUid = member.getUserId();
			 * 
			 * if (learnerRoleKeySet.contains(member.getRole().getId()) &&
			 * !userUids.contains(userUid)) userUids.add(userUid); }
			 * 
			 * if (userService != null) { users =
			 * userService.getUsers(userUids); } }
			 */
		}
		return users;
	}

	private List<Category> getCategoriesWithAssignments(Long gradebookId, List<Assignment> assignments, boolean includeEmpty) {

		Map<Long, Category> categoryMap = new HashMap<Long, Category>();
		List<Category> categories = null;

		Category defaultCategory = null;

		int categoryOrder = 0;

		// If we need to include categories that do not have any items under
		// them (as we do for the item tree), then we have to do an additional
		// query.
		if (includeEmpty) {
			categories = gbService.getCategories(gradebookId);

			if (categories != null) {
				for (Category category : categories) {
					if (!category.isRemoved()) {
						categoryMap.put(category.getId(), category);
						Integer order = category.getCategoryOrder();
						if (order == null)
							category.setCategoryOrder(categoryOrder++);

						// if
						// (category.getName().equalsIgnoreCase(AppConstants.DEFAULT_CATEGORY_NAME))
						// {
						// defaultCategory = category;
						// }
					}
				}
			}
		}

		List<Assignment> assignmentList = null;

		if (assignments != null) {
			for (Assignment assignment : assignments) {
				Category category = null;

				if (assignment.isRemoved())
					continue;

				if (assignment.getCategory() != null)
					category = categoryMap.get(assignment.getCategory().getId());

				if (null == category) {

					category = assignment.getCategory();

					if (null == category) {
						if (defaultCategory == null) {
							defaultCategory = findDefaultCategory(gradebookId);
							if (categories != null) {
								categories.add(defaultCategory);
								defaultCategory.setCategoryOrder(categoryOrder++);
							} else
								categoryMap.put(defaultCategory.getId(), defaultCategory);
						}

						category = defaultCategory;
					}
				}

				if (null != category) {

					assignmentList = category.getAssignmentList();

					if (null == assignmentList) {
						assignmentList = new ArrayList<Assignment>();
						category.setAssignmentList(assignmentList);
					}

					if (!assignmentList.contains(assignment)) {
						Integer itemOrder = assignment.getItemOrder();
						if (itemOrder == null)
							itemOrder = Integer.valueOf(assignmentList.size());
						assignmentList.add(assignment);
					}
				}
			}
		}

		if (categories == null && categoryMap.size() > 0)
			categories = new ArrayList<Category>(categoryMap.values());

		// Make sure the default category has one or more children if it's going
		// to be visible
		/*
		 * if (defaultCategory != null) {
		 * 
		 * if (defaultCategory.getAssignmentList() == null ||
		 * defaultCategory.getAssignmentList().isEmpty()) {
		 * defaultCategory.setRemoved(true);
		 * gbService.updateCategory(defaultCategory);
		 * categories.remove(defaultCategory); } }
		 */

		return categories;
	}

	private <X extends BaseModel> List<X> getColumns() {

		List<X> columns = new LinkedList<X>();

		columns.add((X) new FixedColumnModel(StudentModel.Key.DISPLAY_ID, 80, true));
		columns.add((X) new FixedColumnModel(StudentModel.Key.DISPLAY_NAME, 180, false));
		columns.add((X) new FixedColumnModel(StudentModel.Key.LAST_NAME_FIRST, 180, true));
		columns.add((X) new FixedColumnModel(StudentModel.Key.EMAIL, 230, true));
		columns.add((X) new FixedColumnModel(StudentModel.Key.SECTION, 120, true));
		columns.add((X) new FixedColumnModel(StudentModel.Key.COURSE_GRADE, 120, false));
		FixedColumnModel gradeOverrideColumn = new FixedColumnModel(StudentModel.Key.GRADE_OVERRIDE, 120, true);
		gradeOverrideColumn.setEditable(true);
		columns.add((X) gradeOverrideColumn);

		return columns;
	}

	private String getDisplayGrade(Gradebook gradebook, String studentUid, CourseGradeRecord courseGradeRecord, List<Assignment> assignments, List<Category> categories, Map<Long, AssignmentGradeRecord> studentGradeMap) {

		BigDecimal autoCalculatedGrade = null;

		boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;

		switch (gradebook.getCategory_type()) {
			case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
				autoCalculatedGrade = gradeCalculations.getCourseGrade(gradebook, assignments, studentGradeMap);
				break;
			default:
				autoCalculatedGrade = gradeCalculations.getCourseGrade(gradebook, categories, studentGradeMap);
		}

		if (autoCalculatedGrade != null)
			autoCalculatedGrade = autoCalculatedGrade.setScale(2, RoundingMode.HALF_EVEN);

		Double calculatedGrade = autoCalculatedGrade == null ? null : Double.valueOf(autoCalculatedGrade.doubleValue());

		String enteredGrade = null;
		String displayGrade = null;
		String letterGrade = null;

		boolean isOverridden = false;

		if (courseGradeRecord != null)
			enteredGrade = courseGradeRecord.getEnteredGrade();

		if (enteredGrade == null && calculatedGrade != null)
			letterGrade = getLetterGrade(autoCalculatedGrade, gradebook.getSelectedGradeMapping());
		// gradebook.getSelectedGradeMapping().getGrade(
		// calculatedGrade);
		else {
			letterGrade = enteredGrade;
			isOverridden = true;
		}

		String missingGradesMarker = "";

		if (gbService.isStudentMissingScores(gradebook.getId(), studentUid, hasCategories))
			missingGradesMarker = "***";

		/*
		 * if (assignments != null) { for (Assignment assignment : assignments)
		 * { if (assignment.isRemoved() ||
		 * DataTypeConversionUtil.checkBoolean(assignment.isUnweighted()))
		 * continue;
		 * 
		 * if (gradebook.getCategory_type() !=
		 * GradebookService.CATEGORY_TYPE_NO_CATEGORY) { Category category =
		 * assignment.getCategory();
		 * 
		 * // If the assignment belongs to a category that's removed or
		 * unweighted, skip if (category != null && (category.isRemoved() ||
		 * DataTypeConversionUtil.checkBoolean(category.isUnweighted())))
		 * continue; }
		 * 
		 * // The student is missing one or more grades if /// (a) there's no
		 * studentGradeMap /// (b) there's no AssignmentGradeRecord for this
		 * assignment /// (c) there's no points earned for this
		 * AssignmentGradeRecord if (studentGradeMap != null &&
		 * studentGradeMap.get(assignment.getId()) != null) {
		 * 
		 * AssignmentGradeRecord record =
		 * studentGradeMap.get(assignment.getId());
		 * 
		 * boolean isExcused = record.isExcluded() != null &&
		 * record.isExcluded().booleanValue(); boolean isDropped =
		 * record.isDropped() != null && record.isDropped().booleanValue(); if
		 * (record.getPointsEarned() == null && !isExcused && !isDropped)
		 * missingGradesMarker = "***";
		 * 
		 * } else missingGradesMarker = "***"; } // for } // if
		 */

		if (letterGrade != null) {
			StringBuilder buffer = new StringBuilder(letterGrade);

			if (isOverridden) {
				buffer.append(" (override)").append(missingGradesMarker);
			} else if (autoCalculatedGrade != null) {
				buffer.append(" (").append(autoCalculatedGrade.toString()).append("%) ").append(missingGradesMarker);
			}

			displayGrade = buffer.toString();
		}

		return displayGrade;
	}

	protected String lookupDefaultGradebookUid() {

		if (toolManager == null)
			return "TESTGRADEBOOK";

		Placement placement = toolManager.getCurrentPlacement();
		if (placement == null) {
			log.error("Placement is null!");
			return null;
		}

		return placement.getContext();
	}

	private ItemModel getActiveItem(ItemModel parent) {

		if (parent.isActive())
			return parent;

		for (ItemModel c : parent.getChildren()) {
			if (c.isActive()) {
				return c;
			}

			if (c.getChildCount() > 0) {
				ItemModel activeItem = getActiveItem(c);

				if (activeItem != null)
					return activeItem;
			}
		}

		return null;
	}

	private List<GradebookModel> getGradebookModels(String[] gradebookUids) {

		List<GradebookModel> models = new LinkedList<GradebookModel>();

		if (gradebookUids == null || gradebookUids.length == 0)
			gradebookUids = new String[] { lookupDefaultGradebookUid() };

		for (int i = 0; i < gradebookUids.length; i++)
			models.add(createOrRetrieveGradebookModel(gradebookUids[i]));

		return models;
	}

	private ItemModel getItemModel(Gradebook gradebook, List<Assignment> assignments, List<Category> categories, Long categoryId, Long assignmentId) {

		ItemModel gradebookItemModel = createItemModel(gradebook);

		boolean isNotInCategoryMode = gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY;

		if (isNotInCategoryMode) {
			calculateItemCategoryPercent(gradebook, null, gradebookItemModel, null, assignments, assignmentId);

		} else {
			// List<Category> categories =
			// getCategoriesWithAssignments(gradebook.getId(), assignments,
			// true);

			if (categories != null) {
				BigDecimal gradebookWeightSum = BigDecimal.ZERO;
				BigDecimal gradebookPointsSum = BigDecimal.ZERO;
				for (Category category : categories) {
					boolean isExtraCredit = category.isExtraCredit() != null && category.isExtraCredit().booleanValue();
					boolean isUnweighted = category.isUnweighted() != null && category.isUnweighted().booleanValue();

					if (!category.isRemoved() || isNotInCategoryMode) {
						double categoryWeight = category.getWeight() == null ? 0d : category.getWeight().doubleValue() * 100d;

						List<Assignment> items = category.getAssignmentList();
						ItemModel categoryItemModel = createItemModel(gradebook, category, items);

						if (!isNotInCategoryMode) {
							categoryItemModel.setParent(gradebookItemModel);
							gradebookItemModel.add(categoryItemModel);
						}

						if (categoryId != null && category.getId().equals(categoryId))
							categoryItemModel.setActive(true);

						calculateItemCategoryPercent(gradebook, category, gradebookItemModel, categoryItemModel, items, assignmentId);

						double categoryPoints = categoryItemModel.getPoints() == null ? 0d : categoryItemModel.getPoints().doubleValue();

						if (!isExtraCredit && !isUnweighted) {
							gradebookWeightSum = gradebookWeightSum.add(BigDecimal.valueOf(categoryWeight));
							gradebookPointsSum = gradebookPointsSum.add(BigDecimal.valueOf(categoryPoints));
						}

					}
				}
				gradebookItemModel.setPoints(Double.valueOf(gradebookPointsSum.doubleValue()));
				gradebookItemModel.setPercentCourseGrade(Double.valueOf(gradebookWeightSum.doubleValue()));
			}
		}

		return gradebookItemModel;
	}

	private ItemModel getItemModelsForCategory(Category category, ItemModel gradebookItemModel, Long assignmentId) {

		if (category == null)
			return null;

		Gradebook gradebook = category.getGradebook();

		List<Assignment> assignments = gbService.getAssignmentsForCategory(category.getId());

		// ItemModel gradebookItemModel = createItemModel(gradebook);

		ItemModel categoryItemModel = createItemModel(gradebook, category, null);
		categoryItemModel.setParent(gradebookItemModel);
		gradebookItemModel.add(categoryItemModel);

		calculateItemCategoryPercent(gradebook, category, gradebookItemModel, categoryItemModel, assignments, assignmentId);

		return categoryItemModel;
	}

	private String getLetterGrade(BigDecimal value, GradeMapping mapping) {

		if (value == null || mapping == null)
			return null;

		Map<String, Double> gradeMap = mapping.getGradeMap();
		Collection<String> grades = mapping.getGrades();

		if (gradeMap == null || grades == null)
			return null;

		for (Iterator<String> iter = grades.iterator(); iter.hasNext();) {
			String grade = iter.next();
			Double mapVal = (Double) gradeMap.get(grade);
			double m = mapVal == null ? 0d : mapVal.doubleValue();
			BigDecimal bigMapVal = BigDecimal.valueOf(m).setScale(2, RoundingMode.HALF_EVEN);

			// If the value in the map is less than the value passed, then the
			// map value is the letter grade for this value
			if (bigMapVal != null && bigMapVal.compareTo(value) <= 0) {
				return grade;
			}
		}
		// As long as 'F' is zero, this should never happen.
		return null;
	}

	protected Site getSite() {

		String context = getSiteContext();
		Site site = null;

		try {

			if (siteService != null)
				site = siteService.getSite(context);
			else
				site = new SiteMock(getSiteId());

		} catch (IdUnusedException iue) {
			log.error("IDUnusedException : SiteContext = " + context);
			iue.printStackTrace();
		}

		return site;
	}

	protected String getSiteContext() {

		if (toolManager == null)
			return "TESTSITECONTEXT";

		return toolManager.getCurrentPlacement().getContext();
	}

	private String getSiteId() {

		String context = getSiteContext();
		String siteId = null;

		if (siteService == null)
			return "TESTSITEID";

		try {

			Site site = siteService.getSite(context);
			siteId = site.getId();

		} catch (IdUnusedException iue) {
			log.error("IDUnusedException : SiteContext = " + context);
			iue.printStackTrace();
		}

		return siteId;
	}

	private void logActionRecord(ActionRecord actionRecord, ItemModel item) {

		Map<String, String> propertyMap = actionRecord.getPropertyMap();
		for (String propertyName : item.getPropertyNames()) {
			Object value = item.get(propertyName);
			if (value != null)
				propertyMap.put(propertyName, String.valueOf(value));
		}

		gbService.storeActionRecord(actionRecord);
	}

	private void recalculateAssignmentGradeRecords(Assignment assignment, Double value, Double startValue) {

		// Assignment assignment = gbService.getAssignment(assignmentId);

		// List<UserDereference> dereferences = findAllUserDereferences();

		// FIXME: Ensure that only users with access to all the students'
		// records can call this method!!!
		// Map<String, EnrollmentRecord> enrollmentRecordMap =
		// authz.findEnrollmentRecords(gradebook.getUid(), gradebook.getId(),
		// null, null);
		// List<String> studentUids = new
		// ArrayList<String>(enrollmentRecordMap.keySet());
		// List<EnrollmentRecord> enrollmentRecords = new
		// ArrayList<EnrollmentRecord>(enrollmentRecordMap.values());

		// Collections.sort(enrollmentRecords, ENROLLMENT_NAME_COMPARATOR);

		List<AssignmentGradeRecord> gradeRecords = gbService.getAssignmentGradeRecords(assignment);
		List<AssignmentGradeRecord> updatedRecords = new ArrayList<AssignmentGradeRecord>();

		if (gradeRecords != null) {
			for (AssignmentGradeRecord gradeRecord : gradeRecords) {
				if (gradeRecord.getPointsEarned() != null) {
					BigDecimal newPoints = gradeCalculations.getNewPointsGrade(gradeRecord.getPointsEarned(), value, startValue);
					gradeRecord.setPointsEarned(Double.valueOf(newPoints.doubleValue()));
					updatedRecords.add(gradeRecord);
				}
			}

			if (!updatedRecords.isEmpty()) {
				gbService.updateAssignmentGradeRecords(assignment, updatedRecords);
			}
		}
	}

	private List<Assignment> recalculateAssignmentWeights(Category category, Boolean enforceEqualWeighting, List<Assignment> assignments) {

		List<Assignment> updatedAssignments = new ArrayList<Assignment>();

		int weightedCount = 0;
		if (assignments != null) {
			for (Assignment assignment : assignments) {
				boolean isRemoved = assignment.isRemoved();
				boolean isWeighted = assignment.isUnweighted() == null ? true : !assignment.isUnweighted().booleanValue();
				boolean isExtraCredit = assignment.isExtraCredit() == null ? false : assignment.isExtraCredit().booleanValue();
				if (isWeighted && !isExtraCredit && !isRemoved) {
					weightedCount++;
				}
			}
		}

		boolean doRecalculate = false;

		if (enforceEqualWeighting != null && enforceEqualWeighting.booleanValue() && !category.isEqualWeightAssignments().equals(Boolean.TRUE)) {
			category.setEqualWeightAssignments(enforceEqualWeighting);
			gbService.updateCategory(category);
		}

		doRecalculate = category.isEqualWeightAssignments() == null ? true : category.isEqualWeightAssignments().booleanValue();

		if (doRecalculate) {
			Double newWeight = gradeCalculations.calculateEqualWeight(weightedCount);
			if (assignments != null) {
				for (Assignment assignment : assignments) {
					boolean isRemoved = assignment.isRemoved();
					boolean isWeighted = assignment.isUnweighted() == null ? true : !assignment.isUnweighted().booleanValue();
					boolean isExtraCredit = assignment.isExtraCredit() == null ? false : assignment.isExtraCredit().booleanValue();
					if (!isRemoved && isWeighted) {
						if (isExtraCredit)
							updatedAssignments.add(assignment);
						else {
							Assignment persistAssignment = gbService.getAssignment(assignment.getId());
							persistAssignment.setAssignmentWeighting(newWeight);
							gbService.updateAssignment(persistAssignment);
							updatedAssignments.add(persistAssignment);
						}
					}
				}
			}
		}
		return updatedAssignments;
	}

	/*
	 * private void recalculateAssignmentGradeRecords(Long assignmentId, Double
	 * value, Double startValue) { Assignment assignment =
	 * gbService.getAssignment(assignmentId); Gradebook gradebook =
	 * assignment.getGradebook();
	 * 
	 * // FIXME: Ensure that only users with access to all the students' records
	 * can call this method!!! Map<String, EnrollmentRecord> enrollmentRecordMap
	 * = authz.findEnrollmentRecords(gradebook.getUid(), gradebook.getId(),
	 * null, null); List<String> studentUids = new
	 * ArrayList<String>(enrollmentRecordMap.keySet()); List<EnrollmentRecord>
	 * enrollmentRecords = new
	 * ArrayList<EnrollmentRecord>(enrollmentRecordMap.values());
	 * 
	 * //Collections.sort(enrollmentRecords, ENROLLMENT_NAME_COMPARATOR);
	 * 
	 * List<AssignmentGradeRecord> gradeRecords =
	 * gbService.getAssignmentGradeRecords(assignment, studentUids);
	 * List<AssignmentGradeRecord> updatedRecords = new
	 * ArrayList<AssignmentGradeRecord>();
	 * 
	 * if (gradeRecords != null) { for (AssignmentGradeRecord gradeRecord :
	 * gradeRecords) { if (gradeRecord.getPointsEarned() != null) { BigDecimal
	 * newPoints =
	 * gradeCalculations.getNewPointsGrade(gradeRecord.getPointsEarned(), value,
	 * startValue);
	 * gradeRecord.setPointsEarned(Double.valueOf(newPoints.doubleValue()));
	 * updatedRecords.add(gradeRecord); } }
	 * 
	 * if (!updatedRecords.isEmpty()) {
	 * gbService.updateAssignmentGradeRecords(assignment, updatedRecords); } } }
	 * 
	 * 
	 * private List<Assignment> recalculateAssignmentWeights(Long categoryId,
	 * Boolean isEqualWeighting) { List<Assignment> updatedAssignments = new
	 * ArrayList<Assignment>(); List<Assignment> assignments =
	 * gbService.getAssignmentsForCategory(categoryId);
	 * 
	 * int weightedCount = 0; if (assignments != null) { for (Assignment
	 * assignment : assignments) { boolean isRemoved = assignment.isRemoved();
	 * boolean isWeighted = assignment.isUnweighted() == null ? true : !
	 * assignment.isUnweighted().booleanValue(); boolean isExtraCredit =
	 * assignment.isExtraCredit() == null ? false :
	 * assignment.isExtraCredit().booleanValue(); if (isWeighted &&
	 * !isExtraCredit && !isRemoved) { weightedCount++; } } }
	 * 
	 * boolean doRecalculate = false;
	 * 
	 * Category category = gbService.getCategory(categoryId); if
	 * (isEqualWeighting != null) {
	 * category.setEqualWeightAssignments(isEqualWeighting);
	 * gbService.updateCategory(category); }
	 * 
	 * doRecalculate = category.isEqualWeightAssignments() == null ? true :
	 * category.isEqualWeightAssignments().booleanValue();
	 * 
	 * if (doRecalculate) { Double newWeight =
	 * gradeCalculations.calculateEqualWeight(weightedCount); if (assignments !=
	 * null) { for (Assignment assignment : assignments) { boolean isRemoved =
	 * assignment.isRemoved(); boolean isWeighted = assignment.isUnweighted() ==
	 * null ? true : ! assignment.isUnweighted().booleanValue(); boolean
	 * isExtraCredit = assignment.isExtraCredit() == null ? false :
	 * assignment.isExtraCredit().booleanValue(); if (!isRemoved && isWeighted)
	 * { if (isExtraCredit) updatedAssignments.add(assignment); else {
	 * Assignment persistAssignment =
	 * gbService.getAssignment(assignment.getId());
	 * persistAssignment.setAssignmentWeighting(newWeight);
	 * gbService.updateAssignment(persistAssignment);
	 * updatedAssignments.add(persistAssignment); } } } } } return
	 * updatedAssignments; }
	 */

	private StudentModel refreshLearnerData(Gradebook gradebook, StudentModel student, Assignment assignment, List<AssignmentGradeRecord> assignmentGradeRecords) {

		Map<Long, AssignmentGradeRecord> studentGradeMap = new HashMap<Long, AssignmentGradeRecord>();

		for (AssignmentGradeRecord gradeRecord : assignmentGradeRecords) {
			Assignment a = gradeRecord.getAssignment();
			studentGradeMap.put(a.getId(), gradeRecord);
		}

		/*
		 * if (assignments != null) { for (Assignment a : assignments) {
		 * AssignmentGradeRecord record =
		 * gbService.getAssignmentGradeRecordForAssignmentForStudent(a,
		 * student.getIdentifier()); record.setGradableObject(a);
		 * studentGradeMap.put(a.getId(), record); } }
		 */

		// FIXME: There has to be a more efficient way of doing this -- all we
		// really need this for is to determine if the learner has been graded
		// for all assignments
		// FIXME: We should be able to replace that logic in getDisplayGrade
		// with a clever db query.
		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		List<Category> categories = null;
		if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
		CourseGradeRecord courseGradeRecord = gbService.getStudentCourseGradeRecord(gradebook, student.getIdentifier());
		String displayGrade = getDisplayGrade(gradebook, student.getIdentifier(), courseGradeRecord, assignments, categories, studentGradeMap);

		for (AssignmentGradeRecord record : assignmentGradeRecords) {
			Long aId = record.getGradableObject().getId();
			String dropProperty = concat(String.valueOf(aId), StudentModel.DROP_FLAG);
			String excuseProperty = concat(String.valueOf(aId), StudentModel.EXCUSE_FLAG);
			boolean isDropped = record.isDropped() != null && record.isDropped().booleanValue();
			boolean isExcluded = record.isExcluded() != null && record.isExcluded().booleanValue();

			if (isDropped)
				student.set(dropProperty, Boolean.TRUE);
			else
				student.set(dropProperty, null);

			if (isExcluded) {
				student.set(excuseProperty, Boolean.TRUE);
				student.set(dropProperty, Boolean.TRUE);
			} else {
				student.set(excuseProperty, null);
				if (!isDropped)
					student.set(dropProperty, null);
			}
		}

		/*
		 * String gradedProperty = assignment.getId() +
		 * StudentModel.GRADED_FLAG; if
		 * (gbService.isStudentGraded(student.getIdentifier(),
		 * assignment.getId())) student.set(gradedProperty, Boolean.TRUE); else
		 * student.set(gradedProperty, null);
		 */

		String commentedProperty = assignment.getId() + StudentModel.COMMENTED_FLAG;
		if (gbService.isStudentCommented(student.getIdentifier(), assignment.getId()))
			student.set(commentedProperty, Boolean.TRUE);
		else
			student.set(commentedProperty, null);

		student.set(StudentModel.Key.COURSE_GRADE.name(), displayGrade);

		return student;
	}

	private AssignmentGradeRecord scoreItem(Gradebook gradebook, Assignment assignment, AssignmentGradeRecord assignmentGradeRecord, String studentUid, Double value, boolean includeExcluded, boolean deferUpdate)
			throws InvalidInputException {

		boolean isUserAbleToGrade = authz.isUserAbleToGradeAll(gradebook.getUid()) || authz.isUserAbleToGradeItemForStudent(gradebook.getUid(), assignment.getId(), studentUid);

		if (!isUserAbleToGrade)
			throw new InvalidInputException("You are not authorized to grade this student for this item.");

		if (assignment.isExternallyMaintained())
			throw new InvalidInputException("This grade item is maintained externally. Please input and edit grades through " + assignment.getExternalAppName());

		if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS && value != null) {
			if (value.compareTo(assignment.getPointsPossible()) > 0)
				throw new InvalidInputException("This grade cannot be larger than " + DataTypeConversionUtil.formatDoubleAsPointsString(assignment.getPointsPossible()));
			else if (value.compareTo(Double.valueOf(0d)) < 0) {
				double v = value.doubleValue();

				if (v < -1d * assignment.getPointsPossible().doubleValue())
					throw new InvalidInputException("The absolute value of a negative point score assigned to a student cannot be greater than the total possible points allowed for an item");
			}

		} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE && value != null) {
			if (value.compareTo(Double.valueOf(100d)) > 0)
				throw new InvalidInputException("This grade cannot be larger than " + DataTypeConversionUtil.formatDoubleAsPointsString(100d) + "%");

			else if (value.compareTo(Double.valueOf(0d)) < 0)
				throw new InvalidInputException("This grade cannot be less than " + DataTypeConversionUtil.formatDoubleAsPointsString(0d) + "%");
		}

		if (!includeExcluded && assignmentGradeRecord.isExcluded() != null && assignmentGradeRecord.isExcluded().booleanValue())
			throw new InvalidInputException("The student has been excused from this assignment. It is no longer possible to assign him or her a grade.");

		switch (gradebook.getGrade_type()) {
			case GradebookService.GRADE_TYPE_POINTS:
				assignmentGradeRecord.setPointsEarned(value);
				break;
			case GradebookService.GRADE_TYPE_PERCENTAGE:
				BigDecimal pointsEarned = gradeCalculations.getPercentAsPointsEarned(assignment, value);
				Double pointsEarnedDouble = pointsEarned == null ? null : Double.valueOf(pointsEarned.doubleValue());
				assignmentGradeRecord.setPointsEarned(pointsEarnedDouble);
				assignmentGradeRecord.setPercentEarned(value);
				break;
		}

		// Prepare record for update
		assignmentGradeRecord.setGradableObject(assignment);
		assignmentGradeRecord.setStudentId(studentUid);

		if (!deferUpdate) {
			Collection<AssignmentGradeRecord> gradeRecords = new LinkedList<AssignmentGradeRecord>();
			gradeRecords.add(assignmentGradeRecord);
			gbService.updateAssignmentGradeRecords(assignment, gradeRecords, gradebook.getGrade_type());
		}

		return assignmentGradeRecord;
	}

	private void verifyUserDataIsUpToDate(Site site, String[] learnerRoleKeys) {

		String siteId = site == null ? null : site.getId();

		int totalUsers = gbService.getFullUserCountForSite(siteId, null, learnerRoleKeys);
		int dereferencedUsers = gbService.getDereferencedUserCountForSite(siteId, null, learnerRoleKeys);

		int diff = totalUsers - dereferencedUsers;

		UserDereferenceRealmUpdate lastUpdate = gbService.getLastUserDereferenceSync(siteId, null);

		int realmCount = lastUpdate == null || lastUpdate.getRealmCount() == null ? -1 : lastUpdate.getRealmCount().intValue();

		// log.info("Total users: " + totalUsers + " Dereferenced users: " +
		// dereferencedUsers + " Realm count: " + realmCount);

		// Obviously if the realm count has changed, then we need to update, but
		// let's also do it if more than an hour has passe
		long ONEHOUR = 1000l * 60l * 60l;
		if (lastUpdate == null || lastUpdate.getRealmCount() == null || !lastUpdate.getRealmCount().equals(Integer.valueOf(diff)) || lastUpdate.getLastUpdate() == null
				|| lastUpdate.getLastUpdate().getTime() + ONEHOUR < new Date().getTime()) {
			gbService.syncUserDereferenceBySite(siteId, null, findAllMembers(site, learnerRoleKeys), diff, learnerRoleKeys);
		}
	}

	private ItemModel updateGradebookModel(ItemModel item) {

		Gradebook gradebook = gbService.getGradebook(item.getIdentifier());

		ActionRecord actionRecord = new ActionRecord(gradebook.getUid(), gradebook.getId(), EntityType.GRADEBOOK.name(), ActionType.UPDATE.name());
		actionRecord.setEntityName(gradebook.getName());
		actionRecord.setEntityId(gradebook.getUid());

		logActionRecord(actionRecord, item);

		gradebook.setName(item.getName());

		int oldCategoryType = gradebook.getCategory_type();
		int newCategoryType = -1;

		boolean hasCategories = item.getCategoryType() != CategoryType.NO_CATEGORIES;

		switch (item.getCategoryType()) {
			case NO_CATEGORIES:
				newCategoryType = GradebookService.CATEGORY_TYPE_NO_CATEGORY;
				break;
			case SIMPLE_CATEGORIES:
				newCategoryType = GradebookService.CATEGORY_TYPE_ONLY_CATEGORY;
				break;
			case WEIGHTED_CATEGORIES:
				newCategoryType = GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY;
				break;
		}

		gradebook.setCategory_type(newCategoryType);

		int oldGradeType = gradebook.getGrade_type();
		int newGradeType = -1;

		switch (item.getGradeType()) {
			case POINTS:
				newGradeType = GradebookService.GRADE_TYPE_POINTS;
				break;
			case PERCENTAGES:
				newGradeType = GradebookService.GRADE_TYPE_PERCENTAGE;
				break;
			case LETTERS:
				newGradeType = GradebookService.GRADE_TYPE_LETTER;
				break;
		}

		gradebook.setGrade_type(newGradeType);

		boolean wasReleaseGrades = gradebook.isCourseGradeDisplayed();
		boolean isReleaseGrades = DataTypeConversionUtil.checkBoolean(item.getReleaseGrades());

		gradebook.setCourseGradeDisplayed(isReleaseGrades);

		boolean wasReleaseItems = gradebook.isAssignmentsDisplayed();
		boolean isReleaseItems = DataTypeConversionUtil.checkBoolean(item.getReleaseItems());
		
		gradebook.setAssignmentsDisplayed(isReleaseItems);
		
		GradeMapping mapping = gradebook.getSelectedGradeMapping();
		Long gradeScaleId = item.getGradeScaleId();
		if (mapping != null && gradeScaleId != null && !mapping.getId().equals(gradeScaleId)) {
			GradeMapping newMapping = gbService.getGradeMapping(gradeScaleId);
			gradebook.setSelectedGradeMapping(newMapping);
		}
		
		gbService.updateGradebook(gradebook);

		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		List<Category> categories = null;
		if (hasCategories)
			categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);
		return getItemModel(gradebook, assignments, categories, null, null);
	}

	/**
	 * Method to update a category model
	 * 
	 * Business rules: (1) if weight is null or zero, uninclude it (2) new
	 * category name must not duplicate an existing category name (3) if equal
	 * weighting is set, then recalculate all item weights of child items, (4)
	 * if category is extra credit, ensure that none of its items are extra
	 * credit
	 * 
	 * @param item
	 * @return
	 * @throws InvalidInputException
	 */
	private ItemModel updateCategoryModel(ItemModel item) throws InvalidInputException {

		boolean isWeightChanged = false;

		Category category = gbService.getCategory(Long.valueOf(item.getIdentifier()));
		Gradebook gradebook = category.getGradebook();

		ActionRecord actionRecord = new ActionRecord(gradebook.getUid(), gradebook.getId(), EntityType.CATEGORY.name(), ActionType.UPDATE.name());
		actionRecord.setEntityName(category.getName());
		actionRecord.setEntityId(String.valueOf(category.getId()));

		Map<String, String> propertyMap = actionRecord.getPropertyMap();

		for (String property : item.getPropertyNames()) {
			String value = String.valueOf(item.get(property));
			if (value != null)
				propertyMap.put(property, value);
		}

		try {

			// category.setName(convertString(item.getName()));

			boolean originalExtraCredit = DataTypeConversionUtil.checkBoolean(category.isExtraCredit());
			boolean currentExtraCredit = DataTypeConversionUtil.checkBoolean(item.getExtraCredit());

			isWeightChanged = originalExtraCredit != currentExtraCredit;

			// category.setExtraCredit(Boolean.valueOf(currentExtraCredit));

			Double newCategoryWeight = item.getPercentCourseGrade();
			Double oldCategoryWeight = category.getWeight();

			isWeightChanged = isWeightChanged || DataTypeConversionUtil.notEquals(newCategoryWeight, oldCategoryWeight);

			double w = newCategoryWeight == null ? 0d : ((Double) newCategoryWeight).doubleValue() * 0.01;

			boolean isEqualWeighting = DataTypeConversionUtil.checkBoolean(item.getEqualWeightAssignments());
			boolean wasEqualWeighting = DataTypeConversionUtil.checkBoolean(category.isEqualWeightAssignments());

			isWeightChanged = isWeightChanged || isEqualWeighting != wasEqualWeighting;

			boolean isUnweighted = !DataTypeConversionUtil.checkBoolean(item.getIncluded());
			boolean wasUnweighted = DataTypeConversionUtil.checkBoolean(category.isUnweighted());

			if (wasUnweighted && !isUnweighted && category.isRemoved())
				throw new InvalidInputException("You cannot include a deleted category in grade. Please undelete the category first.");

			int oldDropLowest = category.getDrop_lowest();
			int newDropLowest = convertInteger(item.getDropLowest()).intValue();

			boolean isRemoved = DataTypeConversionUtil.checkBoolean(item.getRemoved());
			boolean wasRemoved = category.isRemoved();

			Integer newCategoryOrder = item.getItemOrder();
			Integer oldCategoryOrder = category.getCategoryOrder();

			// FIXME: Do we want to do this?
			/*
			 * if (!isUnweighted && !isRemoved) { // Since we don't want to
			 * leave the category weighting as 0 if a category has been
			 * re-included, // but we don't know what the user wants it to be,
			 * we set it to 1% double aw = category.getWeight() == null ? 0d :
			 * category.getWeight().doubleValue(); if (aw == 0d)
			 * category.setWeight(Double.valueOf(0.01)); }
			 */

			List<BusinessLogicImpl> beforeCreateRules = new ArrayList<BusinessLogicImpl>();
			List<BusinessLogicImpl> afterCreateRules = new ArrayList<BusinessLogicImpl>();
			boolean hasCategories = gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
			boolean hasWeights = gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY;

			if (hasCategories) {
				List<Category> categories = gbService.getCategories(gradebook.getId());
				// Business rule #2
				businessLogic.applyNoDuplicateCategoryNamesRule(gradebook.getId(), item.getName(), category.getId(), categories);

				if (hasWeights)
					businessLogic.applyOnlyEqualWeightDropLowestRule(newDropLowest, isEqualWeighting);

				if (oldCategoryOrder == null) {
					if (categories != null) {
						int count = 0;
						for (Category c : categories) {
							if (c.isRemoved())
								continue;
							
							if (c.getId().equals(category.getId()))
								oldCategoryOrder = Integer.valueOf(count);
							else if (c.getCategoryOrder() == null) {
								c.setCategoryOrder(Integer.valueOf(count));
								gbService.updateCategory(c);
							}
							count++;
						}
					}
				}
			}

			if (oldCategoryOrder != null && newCategoryOrder != null && oldCategoryOrder.compareTo(newCategoryOrder) < 0)
				newCategoryOrder = Integer.valueOf(newCategoryOrder.intValue() - 1);

			category.setName(convertString(item.getName()));
			category.setExtraCredit(Boolean.valueOf(currentExtraCredit));
			category.setWeight(Double.valueOf(w));
			// Business rule #1
			if (w == 0d)
				category.setUnweighted(Boolean.TRUE);
			category.setEqualWeightAssignments(Boolean.valueOf(isEqualWeighting));
			category.setDrop_lowest(newDropLowest);
			category.setRemoved(isRemoved);
			category.setUnweighted(Boolean.valueOf(isUnweighted || isRemoved));

			if (newCategoryOrder != null)
				category.setCategoryOrder(newCategoryOrder);
			else if (oldCategoryOrder != null)
				category.setCategoryOrder(oldCategoryOrder);

			gbService.updateCategory(category);

			if (hasCategories) {
				List<Assignment> assignmentsForCategory = gbService.getAssignmentsForCategory(category.getId());

				if (isRemoved && !wasRemoved)
					businessLogic.applyRemoveChildItemsWhenCategoryRemoved(category, assignmentsForCategory);

				// Business rule #3
				if (isEqualWeighting && !wasEqualWeighting && businessLogic.checkRecalculateEqualWeightingRule(category))
					recalculateAssignmentWeights(category, Boolean.FALSE, assignmentsForCategory);

				if (oldCategoryOrder == null || (newCategoryOrder != null && newCategoryOrder.compareTo(oldCategoryOrder) != 0))
					businessLogic.reorderAllCategories(gradebook.getId(), category.getId(), newCategoryOrder, oldCategoryOrder);
			}

		} catch (RuntimeException e) {
			actionRecord.setStatus(ActionRecord.STATUS_FAILURE);
			throw e;
		} finally {
			gbService.storeActionRecord(actionRecord);
		}

		List<Assignment> assignments = gbService.getAssignments(gradebook.getId());
		List<Category> categories = getCategoriesWithAssignments(gradebook.getId(), assignments, true);

		ItemModel gradebookItemModel = getItemModel(gradebook, assignments, categories, category.getId(), null);

		/*
		 * for (ItemModel child : gradebookItemModel.getChildren()) { if
		 * (child.equals(item)) { child.setActive(true); } }
		 */
		return gradebookItemModel;
	}

	public String getCurrentUser() {

		if(null == sessionManager) {
			return "0";
		}

		return sessionManager.getCurrentSessionUserId();
	}

	public String getCurrentSession() {

		if (null == sessionManager) {
			return null;
		}

		Session session = sessionManager.getCurrentSession();

		if (null == session) {
			return null;
		}

		return session.getId();
	}

	public List<Category> getCategoriesWithAssignments(Long gradebookId) {

		List<Category> categories = gbService.getCategories(gradebookId);
		List<Category> categoriesWithAssignments = new ArrayList<Category>();
		if (categories != null) {
			for (Category category : categories) {

				if (category != null) {
					List<Assignment> assignments = gbService.getAssignmentsForCategory(category.getId());
					category.setAssignmentList(assignments);
					categoriesWithAssignments.add(category);
				}
			}
		}

		return categoriesWithAssignments;
	}

	/*
	 * UTILITY HELPER METHODS
	 */
	private String concat(String... vars) {

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < vars.length; i++) {
			builder.append(vars[i]);
		}

		return builder.toString();
	}

	private String convertString(Object value) {

		return value == null ? "" : (String) value;
	}

	private Date convertDate(Object value) {

		return value == null ? null : (Date) value;
	}

	private Double convertDouble(Object value) {

		return value == null ? Double.valueOf(0.0) : (Double) value;
	}

	private Boolean convertBoolean(Object value) {

		return value == null ? Boolean.FALSE : (Boolean) value;
	}

	private Integer convertInteger(Object value) {

		return value == null ? Integer.valueOf(0) : (Integer) value;
	}

	/**
	 * INNER CLASSES
	 */

	/**
	 * COMPARATORS
	 */
	// Code taken from
	// "org.sakaiproject.service.gradebook.shared.GradebookService.lettergradeComparator"
	static final Comparator<String> LETTER_GRADE_COMPARATOR = new Comparator<String>() {

		public int compare(String o1, String o2) {

			char c1 = o1.toLowerCase().charAt(0);
			char c2 = o2.toLowerCase().charAt(0);
			
			if (c1 == 'P' && c2 == 'N')
				return -1;
			else if (c2 == 'N' && c1 == 'P')
				return 1;
			
			if (o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {

				if (o1.length() == 2 && o2.length() == 2) {

					if (o1.charAt(1) == '+')
						return 0;
					else
						return 1;

				}

				if (o1.length() == 1 && o2.length() == 2) {

					if (o2.charAt(1) == '+')
						return 1;
					else
						return 0;
				}

				if (o1.length() == 2 && o2.length() == 1) {

					if (o1.charAt(1) == '+')
						return 0;
					else
						return 1;
				}

				return 0;

			} else {

				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		}
	};

	static final Comparator<EnrollmentRecord> ENROLLMENT_NAME_COMPARATOR = new Comparator<EnrollmentRecord>() {

		public int compare(EnrollmentRecord o1, EnrollmentRecord o2) {

			return o1.getUser().getSortName().compareToIgnoreCase(o2.getUser().getSortName());
		}
	};

	static final Comparator<UserRecord> DEFAULT_ID_COMPARATOR = new Comparator<UserRecord>() {

		public int compare(UserRecord o1, UserRecord o2) {

			if (o1.getUserUid() == null || o2.getUserUid() == null)
				return 0;

			return o1.getUserUid().compareToIgnoreCase(o2.getUserUid());
		}
	};

	static final Comparator<UserRecord> SORT_NAME_COMPARATOR = new Comparator<UserRecord>() {

		public int compare(UserRecord o1, UserRecord o2) {

			if (o1.getSortName() == null || o2.getSortName() == null)
				return 0;

			return o1.getSortName().compareToIgnoreCase(o2.getSortName());
		}
	};

	static final Comparator<UserRecord> DISPLAY_ID_COMPARATOR = new Comparator<UserRecord>() {

		public int compare(UserRecord o1, UserRecord o2) {

			if (o1.getDisplayId() == null || o2.getDisplayId() == null)
				return 0;

			return o1.getDisplayId().compareToIgnoreCase(o2.getDisplayId());
		}
	};

	static final Comparator<UserRecord> EMAIL_COMPARATOR = new Comparator<UserRecord>() {

		public int compare(UserRecord o1, UserRecord o2) {

			if (o1.getEmail() == null || o2.getEmail() == null)
				return 0;

			return o1.getEmail().compareToIgnoreCase(o2.getEmail());
		}
	};

	static final Comparator<UserRecord> SECTION_TITLE_COMPARATOR = new Comparator<UserRecord>() {

		public int compare(UserRecord o1, UserRecord o2) {

			if (o1.getSectionTitle() == null || o2.getSectionTitle() == null)
				return 0;

			return o1.getSectionTitle().compareToIgnoreCase(o2.getSectionTitle());
		}
	};

	/*
	 * DEPENDENCY INJECTION ACCESSORS
	 */

	public GradebookFrameworkService getFrameworkService() {

		return frameworkService;
	}

	public void setFrameworkService(GradebookFrameworkService frameworkService) {

		this.frameworkService = frameworkService;
	}

	public GradebookToolService getGbService() {

		return gbService;
	}

	public void setGbService(GradebookToolService gbService) {

		this.gbService = gbService;
	}

	public GradeCalculations getGradeCalculations() {

		return gradeCalculations;
	}

	public void setGradeCalculations(GradeCalculations gradeCalculations) {

		this.gradeCalculations = gradeCalculations;
	}

	public void setAuthz(Gradebook2Authz authz) {

		this.authz = authz;
	}

	public void setSectionAwareness(SectionAwareness sectionAwareness) {

		this.sectionAwareness = sectionAwareness;
	}

	public InstitutionalAdvisor getAdvisor() {

		return advisor;
	}

	public void setAdvisor(InstitutionalAdvisor advisor) {

		this.advisor = advisor;
	}

	public SiteService getSiteService() {

		return siteService;
	}

	public void setSiteService(SiteService siteService) {

		this.siteService = siteService;
	}

	public ToolManager getToolManager() {

		return toolManager;
	}

	public void setToolManager(ToolManager toolManager) {

		this.toolManager = toolManager;
	}

	public UserDirectoryService getUserService() {

		return userService;
	}

	public void setUserService(UserDirectoryService userService) {

		this.userService = userService;
	}

	public BusinessLogic getBusinessLogic() {

		return businessLogic;
	}

	public void setBusinessLogic(BusinessLogic businessLogic) {

		this.businessLogic = businessLogic;
	}

	public SessionManager getSessionManager() {

		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {

		this.sessionManager = sessionManager;
	}

	public ServerConfigurationService getConfigService() {

		return configService;
	}

	public void setConfigService(ServerConfigurationService configService) {

		this.configService = configService;
	}

}
