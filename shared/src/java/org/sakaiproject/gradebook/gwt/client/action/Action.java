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
package org.sakaiproject.gradebook.gwt.client.action;

import java.util.Date;

import org.sakaiproject.gradebook.gwt.client.model.ActionKey;
import org.sakaiproject.gradebook.gwt.client.model.EntityModel;
import org.sakaiproject.gradebook.gwt.client.model.GradebookModel;

public abstract class Action extends EntityModel {

	private static final long serialVersionUID = 1L;

	public enum ActionType { CREATE("Create", "Added "), 
		GET("Get", "retrieved"), 
		GRADED("Grade", "Set grade to"),
		UPDATE("Update", "Updated "), 
		DELETE("Delete", "Deleted "),
		SUBMITTED("Submit", "Submitted ");

	private String desc;
	private String verb;

	private ActionType(String desc, String verb) {
		this.desc = desc;
		this.verb = verb;
	}

	public String getVerb() {
		return verb;
	}

	@Override
	public String toString() {
		return desc;
	}

	};


	public enum EntityType { AUTH("authorization"), APPLICATION("application"), GRADE_ITEM("grade item"),
		CATEGORY("category"), COLUMN("column"), COMMENT("comment"), CONFIGURATION("configuration"), CATEGORY_NOT_REMOVED("category not removed"),
		GRADEBOOK("gradebook"), GRADE_SCALE("grade scale"), COURSE_GRADE_RECORD("course grade record"), GRADE_RECORD("grade record"), 
		GRADE_EVENT("grade event"), USER("user"), PERMISSION_ENTRY("permission entry"),
		SECTION("section"), PERMISSION_SECTIONS("permission sections"), LEARNER("learner"), LEARNER_ID("learner id"), ACTION("action"), ITEM("item"),
		SPREADSHEET("spreadsheet"), SUBMISSION_VERIFICATION("submission verification"), 
		STATISTICS("statistics"), GRADE_FORMAT("grade format"), GRADE_SUBMISSION("grade submission");

	private String name;

	private EntityType(String name) {
		this.name = name;
	}

	@Override

	public String toString() {
		return name;
	}

	};

	public Action() {
		super();
		setDatePerformed(new Date());
	}

	public Action(GradebookModel gbModel) {
		this();
		setGradebookUid(gbModel.getGradebookUid());
		setGradebookId(gbModel.getGradebookId());
		setGraderName(gbModel.getUserName());
	}

	public Action(GradebookModel gbModel, ActionType actionType) {
		this(gbModel);
		setActionType(actionType);
	}

	public Action(ActionType actionType) {
		this();
		setActionType(actionType);
	}

	public Action(EntityType entityType) {
		this();
		setEntityType(entityType);
	}

	public Action(ActionType actionType, EntityType entityType) {
		this();
		setActionType(actionType);
		setEntityType(entityType);
	}

	public Action(String gradebookUid, Long gradebookId) {
		this();
		setGradebookUid(gradebookUid);
		setGradebookId(gradebookId);
	}

	public Action(EntityType entityType, String gradebookUid, Long gradebookId) {
		this(entityType);
		setGradebookUid(gradebookUid);
		setGradebookId(gradebookId);
	}

	@Override
	public String getDisplayName() {
		return getEntityName();
	}

	@Override
	public String getIdentifier() {
		return get(ActionKey.ID.name());
	}

	public void setIdentifier(String id) {
		set(ActionKey.ID.name(), id);
	}

	public String getGradebookUid() {
		return get(ActionKey.GRADEBOOK_UID.name());
	}

	public void setGradebookUid(String gradebookUid) {
		set(ActionKey.GRADEBOOK_UID.name(), gradebookUid);
	}

	public Long getGradebookId() {
		return get(ActionKey.GRADEBOOK_ID.name());
	}

	public void setGradebookId(Long gradebookId) {
		set(ActionKey.GRADEBOOK_ID.name(), gradebookId);
	}

	public ActionType getActionType() {
		String actionType = get(ActionKey.ACTION_TYPE.name());
		if (actionType == null)
			return null;
		return ActionType.valueOf(actionType);
	}

	public void setActionType(ActionType actionType) {
		set(ActionKey.ACTION_TYPE.name(), actionType.name());
	}

	public EntityType getEntityType() {
		String entityType = get(ActionKey.ENTITY_TYPE.name());
		if (entityType == null)
			return null;
		return EntityType.valueOf(entityType);
	}

	public void setEntityType(EntityType entityType) {
		set(ActionKey.ENTITY_TYPE.name(), entityType.name());
	}

	public String getEntityName() {
		return get(ActionKey.ENTITY_NAME.name());
	}

	public void setEntityName(String entityName) {
		set(ActionKey.ENTITY_NAME.name(), entityName);
	}

	public String getStudentUid() {
		return get(ActionKey.STUDENT_UID.name());
	}

	public void setStudentUid(String studentUid) {
		set(ActionKey.STUDENT_UID.name(), studentUid);
	}

	public String getStudentName() {
		return get(ActionKey.STUDENT_NAME.name());
	}

	public void setStudentName(String studentName) {
		set(ActionKey.STUDENT_NAME.name(), studentName);
	}

	public Date getDatePerformed() {
		return get(ActionKey.DATE_PERFORMED.name());
	}

	public void setDatePerformed(Date date) {
		set(ActionKey.DATE_PERFORMED.name(), date);
	}

	public Date getDateRecorded() {
		return get(ActionKey.DATE_RECORDED.name());
	}

	public void setDateRecorded(Date date) {
		set(ActionKey.DATE_RECORDED.name(), date);
	}

	public String getEntityId() {
		return get(ActionKey.ENTITY_ID.name());
	}

	public void setEntityId(String entityId) {
		set(ActionKey.ENTITY_ID.name(), entityId);
	}

	public Boolean getIncludeAll() {
		return get(ActionKey.INCLUDE_ALL.name());
	}

	public void setIncludeAll(Boolean includeAll) {
		set(ActionKey.INCLUDE_ALL.name(), includeAll);
	}

	public String getGraderName() {
		return get(ActionKey.GRADER_NAME.name());
	}

	public void setGraderName(String graderName) {
		set(ActionKey.GRADER_NAME.name(), graderName);
	}

	public String getDescription() {
		return get(ActionKey.DESCRIPTION.name());
	}

	public void setDescription(String description) {
		set(ActionKey.DESCRIPTION.name(), description);
	}

}
