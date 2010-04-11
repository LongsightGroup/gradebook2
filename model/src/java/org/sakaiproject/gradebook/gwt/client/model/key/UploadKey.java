/**
 * 
 */
package org.sakaiproject.gradebook.gwt.client.model.key;

public enum UploadKey { 
	S_ID("id"), 
	S_NM("displayName"), 
	A_HDRS("headers"), 
	A_ROWS("rows"), 
	B_PCT("isPercentage"), 
	V_RSTS("results"), 
	M_GB_ITM("gradebookItem"), 
	I_NUM_ROWS("numberOfRows"),
	B_HAS_ERRS("hasErrors"),
	S_NOTES("notes"),
	B_NTFY_ITM_NM("isNotifyItemName"),
	G_GRD_TYPE("gradeType"),
	C_CTGRY_TYPE("categoryType");
	
	private String property;

	private UploadKey(String property) {
		this.property = property;
	}
	
	public String getProperty() {
		return property;
	}
	
}
