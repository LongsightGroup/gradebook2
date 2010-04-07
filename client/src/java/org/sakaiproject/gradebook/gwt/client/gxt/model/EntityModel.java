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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.gradebook.gwt.client.gxt.JsonUtil;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.core.FastSet;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;

public class EntityModel extends BaseModel implements EntityOverlayOwner {

	private static final long serialVersionUID = 1L;

	private EntityOverlay overlay = null;

	public EntityModel() {
		super();
		this.overlay = JsonUtil.toOverlay("{ }");
	}
	
	public EntityModel(EntityOverlay overlay) {
		this.overlay = overlay;
	}
	
	public EntityModel(Map<String, Object> properties) {
		super(properties);
	}
	
	public EntityOverlay getOverlay() {
		return overlay;
	}
	
	public String getJSON() {
		if (overlay != null) {
			JSONObject jso = new JSONObject(overlay);
			return jso.toString();
		}
			
		return null;
	}
	
	@Override
	public <X> X get(String name) {
		
		if (null != overlay) 
			return (X)overlay.safeGet(this, name);
		
		return null;
	}
	
	public Long getLong(String name) {
		
		if (null != overlay) 
			return (Long)overlay.safeGet(this, name);
		
		return null;
	}
	
	public Map<String, Object> getProperties() {
		if (null == overlay)
			return super.getProperties();
		
		Map<String, Object> newMap = new FastMap<Object>();
		Collection<String> keys = getPropertyNames();
		if (keys != null) {
			for (String key : keys) {
				newMap.put(key, overlay.safeGet(this, key));
			}
		}
		return newMap;
	}
	
	public Collection<String> getPropertyNames() {
		if (null == overlay)
			return super.getPropertyNames();
		
		Set<String> set = new FastSet();
	    JsArrayString array = overlay.getKeys();
	    
	    if (array != null) {
		    for (int i=0;i<array.length();i++) {
		    	set.add(array.get(i));
		    }
	    }
	    
	    return set;
	}
	
	@Override
	public <X> X set(String name, X value) {
		
		if (null != overlay) {
			X oldValue = (X)overlay.safeGet(this, name);
			overlay.safeSet(this, name, value);
			notifyPropertyChanged(name, value, oldValue);
			return oldValue;
		} 
		
		return null;
	}
	
	/*@Override
	public boolean equals(Object obj) {
		if (obj instanceof EntityModel) {
			EntityModel other = (EntityModel) obj;

			if (getIdentifier() == null || other.getIdentifier() == null)
				return false;
			
			return getIdentifier().equals(other.getIdentifier());
		}
		return false;
	}
	
	 @Override
	 public int hashCode() {
		 String id = getIdentifier();
		 int hash = 0;
		 if (id != null) 
			 hash = id.hashCode();
		 return hash;
	 }*/
	 
	 public boolean isChildString(String property) {
		 return isGradeType(property) || isCategoryType(property);
	 }
	 
	 public boolean isGradeType(String property) {
		 return false;
	 }
	 
	 public boolean isCategoryType(String property) {
		 return false;
	 }
	 
	 /*public boolean isDate(String property) {
		 return false;
	 }
	 
	 public boolean isLong(String property) {
		 if (property == null)
			 return false;
		 
		 return property.equals(GradeFormatKey.L_ID.name()) ||
		 	property.equals(ConfigurationKey.L_GB_ID.name()) ||
		 	property.equals(GradebookKey.L_GB_ID.name()) ||
		 	property.equals(Key.ASSIGNMENT_ID.name()) ||
		 	property.equals(PermissionKey.L_CTGRY_ID.name()) ||
		 	property.equals(PermissionKey.L_GB_ID.name()) ||
		 	property.equals(PermissionKey.L_ID.name());
	 }*/
	 
	 public DateTimeFormat getDateTimeFormat(String property) {
		 return DateTimeFormat.getMediumDateFormat();
	 }
	 
	 public boolean isChildModel(String property) {
		 return false;
	 }
	 
	 public ModelData newChildModel(String property, EntityOverlay overlay) {
		 return new BaseModel();
	 }
	 
}
