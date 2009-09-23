package org.sakaiproject.gradebook.gwt.client;

public class ExportDetails {
	public enum ExportType { XLS97, CSV }; 

	private ExportType fileType; 
	private boolean includeStructure;
	public ExportDetails(ExportType fileType, boolean includeStructure) {
		super();
		this.fileType = fileType;
		this.includeStructure = includeStructure;
	}
	public ExportType getFileType() {
		return fileType;
	}
	public void setFileType(ExportType fileType) {
		this.fileType = fileType;
	}
	public boolean isIncludeStructure() {
		return includeStructure;
	}
	public void setIncludeStructure(boolean includeStructure) {
		this.includeStructure = includeStructure;
	} 

}
