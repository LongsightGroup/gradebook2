package org.sakaiproject.gradebook.gwt.client.gxt.view;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.DataTypeConversionUtil;
import org.sakaiproject.gradebook.gwt.client.ExportDetails;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ImportPanel;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class ImportExportView extends View {

	private ContentPanel importPanel;
	private Frame downloadFileFrame;
	
	public ImportExportView(Controller controller) {
		super(controller);
	}

	@Override
	protected void handleEvent(AppEvent event) {
		
		switch (GradebookEvents.getEvent(event.getType()).getEventKey()) {
		case START_IMPORT:
			
			break;
		case STOP_IMPORT:
			importPanel = null;
			break;
		case START_EXPORT:
			ExportDetails ed = (ExportDetails) event.getData(); 
			boolean includeStructure = ed.isIncludeStructure(); 
			String fileType = "";
			
			switch (ed.getFileType())
			{
			case XLS97:
				fileType = "xls97";
				break; 
			case CSV:
			default:
				fileType = "csv"; 
				break;
			}
		
			Gradebook selectedGradebook = Registry.get(AppConstants.CURRENT);
			StringBuilder uri = new StringBuilder().append(GWT.getModuleBaseURL())
				.append(AppConstants.REST_FRAGMENT)
				.append("/").append(AppConstants.EXPORT_SERVLET)
				.append("/").append(selectedGradebook.getGradebookUid());
			
			if (includeStructure)
				uri.append("/").append("structure").append("/").append("true");
			if (fileType != "") {
				uri.append("/").append("filetype").append("/").append(fileType);
			}
			uri.append("?form-token=").append(Cookies.getCookie("JSESSIONID"));
			if (downloadFileFrame == null) {
				downloadFileFrame = new Frame(uri.toString());
				downloadFileFrame.setVisible(false);
				RootPanel.get().add(downloadFileFrame);
			} else {
				downloadFileFrame.setUrl(uri.toString());
			}
			break;
		}
	}

	public ContentPanel getImportDialog() {
		if (importPanel == null) {
			importPanel = new ImportPanel();
		}
		return importPanel;
	}

}
