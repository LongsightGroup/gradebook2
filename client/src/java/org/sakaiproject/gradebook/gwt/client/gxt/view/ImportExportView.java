package org.sakaiproject.gradebook.gwt.client.gxt.view;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebook.gwt.client.AppConstants;
import org.sakaiproject.gradebook.gwt.client.ExportDetails;
import org.sakaiproject.gradebook.gwt.client.I18nConstants;
import org.sakaiproject.gradebook.gwt.client.gxt.event.GradebookEvents;
import org.sakaiproject.gradebook.gwt.client.gxt.event.NotificationEvent;
import org.sakaiproject.gradebook.gwt.client.gxt.view.panel.ImportPanel;
import org.sakaiproject.gradebook.gwt.client.model.Gradebook;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ImportExportView extends View {

	private ContentPanel importPanel;
	private FormPanel downloadFileForm;
	private final I18nConstants i18n = (I18nConstants) GWT.create(I18nConstants.class);
	
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
			String sectionUid = ed.getSectionUid();
			boolean includeComments = ed.includeComments();
			String fileType = "";
			
			if (ed.getFileType() != null) {
				fileType = ed.getFileType().name();
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
			
			uri.append("?").append(AppConstants.REQUEST_FORM_FIELD_FORM_TOKEN).append("=").append(Cookies.getCookie(AppConstants.GB2_TOKEN));
			
			downloadFileForm = new FormPanel();
			
			downloadFileForm.setAction(uri.toString());
			
			downloadFileForm.setEncoding(FormPanel.ENCODING_URLENCODED);
			
			downloadFileForm.setMethod(FormPanel.METHOD_POST);
			
			VerticalPanel panel = new VerticalPanel();
			downloadFileForm.setWidget(panel);
			panel.setVisible(false);
			
			if (sectionUid != null) { 
				List<String> sectionsAsList = new ArrayList<String>();
				sectionsAsList.add(sectionUid);
				/*
				 *  this is being coded as if sectionUid were *not* a single value
				 *  so that it can be used with a list later. The above two could then
				 *  be removed
				 */
				
				
				for (String section : sectionsAsList) {
					if (section != null) {
						TextBox s = new TextBox();
						s.setName(AppConstants.SECTIONS_FIELD);
						s.setValue(section);
						panel.add(s);
					}
					
				}
				
			}
			
			TextBox commentsFlag = new TextBox();
			commentsFlag.setName(AppConstants.INCLUDE_COMMENTS_FIELD);
			commentsFlag.setValue(""+includeComments);
			
			panel.add(commentsFlag);
			
			downloadFileForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
				
				public void onSubmitComplete(SubmitCompleteEvent event) {
					/// if this is called then something went wrong in the download
					Dispatcher.forwardEvent(GradebookEvents.Notification.getEventType(), 
							new NotificationEvent(i18n.errorOccurredGeneric(), i18n.exportError()), true);

					
				}
			});		    
			
			RootPanel.get().add(downloadFileForm);
			
			downloadFileForm.submit();
		
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
