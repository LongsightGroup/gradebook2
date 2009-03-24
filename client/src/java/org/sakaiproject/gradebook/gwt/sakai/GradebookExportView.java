package org.sakaiproject.gradebook.gwt.sakai;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.gradebook.gwt.client.GradebookToolFacade;
import org.sakaiproject.gradebook.gwt.client.exceptions.FatalException;
import org.sakaiproject.gradebook.gwt.client.model.StudentModel;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility;
import org.springframework.web.servlet.view.AbstractView;


public class GradebookExportView extends AbstractView {

	private static final Log log = LogFactory.getLog(GradebookExportView.class);
	
	private GradebookToolFacade delegateFacade;
	
	public GradebookExportView(GradebookToolFacade delegateFacade) {
		super();
		this.delegateFacade = delegateFacade;
	}
	
	public String getContentType() {
		return "application/ms-excel";
	}

	@Override
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		//response.setContentType("application/ms-excel");
		response.setHeader("Content-Disposition", "inline; filename=" + "gradebook.csv");
		
		PrintWriter writer = response.getWriter();
		
		String queryString = request.getQueryString();
		int n = queryString.indexOf("gradebookUid=") + 13;
		String gradebookUid = queryString.substring(n);
		
		log.warn("GradebookUid: " + gradebookUid);
		try {
			ImportExportUtility.exportGradebook(delegateFacade, gradebookUid, writer);
			/*UserEntityGetAction<GradebookModel> getGradebookAction = new UserEntityGetAction<GradebookModel>(gradebookUid, EntityType.GRADEBOOK);
			getGradebookAction.setEntityId(gradebookUid);
			GradebookModel gradebook = delegateFacade.getEntity(getGradebookAction);
				
			UserEntityGetAction<AssignmentModel> getHeadersAction = new UserEntityGetAction<AssignmentModel>(gradebookUid, EntityType.GRADE_ITEM);
			List<AssignmentModel> headers = delegateFacade.getEntityList(getHeadersAction);
			
			UserEntityGetAction<StudentModel> getRowsAction = new UserEntityGetAction<StudentModel>(gradebookUid, EntityType.STUDENT);
			List<StudentModel> rows = delegateFacade.getEntityList(getRowsAction);
			
			String[] headerIds = null;
			if (headers != null) {
				writer.print("Learner,Id");	
				headerIds = new String[headers.size()];
				int i=0;
				for (AssignmentModel header : headers) {
					headerIds[i] = header.getIdentifier();
					writer.print(",");
					writer.print(header.getName());
					
					switch (gradebook.getGradeType()) {
					case POINTS:
						String points = DecimalFormat.getInstance().format(header.getPoints());
						writer.print(" (");
						writer.print(points);
						writer.print(")");
						break;
					case PERCENTAGES:
						writer.print(" (%)");
						break;
					} 
					
					i++;
				}
				writer.println();
			
				if (rows != null) {
					for (StudentModel row : rows) {
						writer.print(row.getDisplayName());
						writer.print(",");
						writer.print(getExportId(row));
						for (int column = 0;column<headerIds.length;column++) {
							writer.print(",");
							if (headerIds[column] != null) {
								Object value = row.get(headerIds[column]);
								if (value != null)
									writer.print(value);
							} else {
								System.out.println("Null column at " + column);
							}
						}
						writer.println();
					}
				} else {
					writer.println();
				}
			}*/
		} catch (FatalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.flush();
		writer.close();
	}

	/*private String getExportId(StudentModel model) {
		String exportId = model.getEid();
		
		if (exportId == null)
			exportId = model.getIdentifier();
		
		return exportId;
	}*/
	
	public GradebookToolFacade getDelegateFacade() {
		return delegateFacade;
	}

	public void setDelegateFacade(GradebookToolFacade delegateFacade) {
		this.delegateFacade = delegateFacade;
	}

}
