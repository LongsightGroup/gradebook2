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
package org.sakaiproject.gradebook.gwt.sakai;

import java.util.List;

import org.gwtwidgets.server.spring.GWTSpringController;
import org.sakaiproject.gradebook.gwt.client.GradebookToolFacade;
import org.sakaiproject.gradebook.gwt.client.action.PageRequestAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityCreateAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityGetAction;
import org.sakaiproject.gradebook.gwt.client.action.UserEntityUpdateAction;
import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.model.CategoryModel;
import org.sakaiproject.gradebook.gwt.client.model.EntityModel;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class GradebookToolFacadeImpl extends GWTSpringController implements GradebookToolFacade {

	private static final long serialVersionUID = 1L;
	
	private GradebookToolFacade delegateFacade;
	
	
	public <X extends EntityModel> X createEntity(UserEntityCreateAction<X> action) {
	
		return delegateFacade.createEntity(action);
	}

	public <X extends EntityModel> X getEntity(UserEntityGetAction<X> action) {
		
		return delegateFacade.getEntity(action);
	}
	
	public <X extends EntityModel> List<X> getEntityList(UserEntityGetAction<X> action) {
		
		return delegateFacade.getEntityList(action);
	}
	
	public <X extends EntityModel> PagingLoadResult<X> getEntityPage(PageRequestAction action,
			PagingLoadConfig config) {
		
		return delegateFacade.getEntityPage(action, config);
	}	

	public List<CategoryModel> recalculateEqualWeightingCategories(
			String gradebookUid, Long gradebookId, Boolean isEqualWeighting) {
		
		return delegateFacade.recalculateEqualWeightingCategories(gradebookUid, gradebookId, isEqualWeighting);
	}

	public <X extends EntityModel> X updateEntity(UserEntityUpdateAction<X> action) throws InvalidInputException {
		
		return delegateFacade.updateEntity(action);
	}
	
	public <X extends EntityModel> List<X> updateEntityList(UserEntityUpdateAction<X> action) throws InvalidInputException {
		
		return delegateFacade.updateEntityList(action);
	}

	public GradebookToolFacade getDelegateFacade() {
		return delegateFacade;
	}

	public void setDelegateFacade(GradebookToolFacade delegateFacade) {
		this.delegateFacade = delegateFacade;
	}


}
