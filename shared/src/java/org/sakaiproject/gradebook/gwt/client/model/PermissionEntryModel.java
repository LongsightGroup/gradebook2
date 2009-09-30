/**********************************************************************************
 *
 * $Id$
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


public class PermissionEntryModel extends EntityModel {
	
	private static final long serialVersionUID = 1L;

	public enum Key {ID, USER_ID, USER_DISPLAY_NAME, PERMISSION_ID, CATEGORY_ID, CATEGORY_DISPLAY_NAME, SECTION_ID, SECTION_DISPLAY_NAME, DELETE_ACTION };
	
	public PermissionEntryModel() {
		super();
	}

	public void setId(Long id) {
		set(Key.ID.name(), id);
	}
	
	public Long getId() {
		return get(Key.ID.name());
	}
	
	public void setUserId(String userId) {
		set(Key.USER_ID.name(), userId);
	}
	
	public String getUserId() {
		return get(Key.USER_ID.name());
	}
	
	public void setUserDisplayName(String userDisplayName) {
		set(Key.USER_DISPLAY_NAME.name(), userDisplayName);
	}
	
	public String getUserDisplayName() {
		return get(Key.USER_DISPLAY_NAME.name());
	}
	
	public void setPermissionId(String permissionId) {
		set(Key.PERMISSION_ID.name(), permissionId);
	}
	
	public String getPermissionId() {
		return get(Key.PERMISSION_ID.name());
	}
	
	public void setCategoryId(Long categoryId) {
		set(Key.CATEGORY_ID.name(), categoryId);
	}
	
	public Long getCategoryId() {
		return get(Key.CATEGORY_ID.name());
	}
	
	public void setCategoryDisplayName(String categoryDisplayName) {
		set(Key.CATEGORY_DISPLAY_NAME.name(), categoryDisplayName);
	}
	
	public String getCategoryDisplayName() {
		return get(Key.CATEGORY_DISPLAY_NAME.name());
	}
	
	public void setSectionId(String sectionId) {
		set(Key.SECTION_ID.name(), sectionId);
	}
	
	public String getSectionId() {
		return get(Key.SECTION_ID.name());
	}
	
	public void setSectionDisplayName(String sectionDisplayName) {
		set(Key.SECTION_DISPLAY_NAME.name(), sectionDisplayName);
	}
	
	public String getSectionDisplayName() {
		return get(Key.SECTION_DISPLAY_NAME.name());
	}
	
	public void setDeleteAction(String deleteAction) {
		set(Key.DELETE_ACTION.name(), deleteAction);
	}
	
	public String getDeleteAction() {
		return get(Key.DELETE_ACTION.name());
	}
	
	@Override
	public String getIdentifier() {
		Long id = getId();
		return (null == id) ? null : getId().toString();
	}
	
	@Override
	public String getDisplayName() {
		return getIdentifier();
	}
}
