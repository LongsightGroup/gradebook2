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

package org.sakaiproject.gradebook.gwt.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.sakaiproject.gradebook.gwt.client.exceptions.FatalException;
import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportFile;
import org.sakaiproject.gradebook.gwt.sakai.Gradebook2Service;
import org.sakaiproject.gradebook.gwt.sakai.GradebookToolService;
import org.sakaiproject.gradebook.gwt.sakai.mock.IocMock;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility.Delimiter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class ImportHandler extends HttpServlet {

	private static final String CONTENT_TYPE = "text/html";
	private static final long serialVersionUID = 1L;

	private IocMock iocMock = IocMock.getInstance();

	private Gradebook2Service service;

	private GradebookToolService gbToolService; 

	public void init() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"test.xml", "db.xml"});

		service = (Gradebook2Service)context.getBean("org.sakaiproject.gradebook.gwt.sakai.Gradebook2Service");
		gbToolService = (GradebookToolService)context.getBean("org.sakaiproject.gradebook.gwt.sakai.GradebookToolService");

	}

	public void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter writer = response.getWriter();

		String gradebookUid = req.getParameter("gradebookUid");
		String include = req.getParameter("include");
		try {
			boolean doIncludeStructure = include != null;
			ImportExportUtility.exportGradebook(service, gradebookUid, doIncludeStructure, true, writer, response);
		} catch (FatalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());

		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();

		String feedback = "nothing";
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		if (isMultipart) {
			final DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(1000000);
			final ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				final List<FileItem> items = (List<FileItem>) upload.parseRequest(req);

				EnumSet<Delimiter> delimiterSet = EnumSet.noneOf(Delimiter.class);
				String gradebookUid = null;
				FileItem uploadedFile = null;
				for (int i = 0; i < items.size(); i++) {
					FileItem current = items.get(i);

					if (current.isFormField()) {

						String name = current.getFieldName();
						if (name != null) { 
							if (name.equals("gradebookUid"))
								gradebookUid = current.getString();
							else if (name.equals("delimiter:comma")) 
								delimiterSet.add(Delimiter.COMMA);
							else if (name.equals("delimiter:tab")) 
								delimiterSet.add(Delimiter.TAB);
							else if (name.equals("delimiter:space"))
								delimiterSet.add(Delimiter.SPACE);
							else if (name.equals("delimiter:colon"))
								delimiterSet.add(Delimiter.COLON);
						}
					} else
						uploadedFile = current;

				}
				String origName = uploadedFile.getName(); 
				ImportFile importFile;

				if (origName.toLowerCase().endsWith("xls"))
				{
					importFile = ImportExportUtility.parseImportXLS(service, gradebookUid, uploadedFile.getInputStream(), origName.toLowerCase(), gbToolService);

				}
				else
				{
					InputStreamReader reader = new InputStreamReader(uploadedFile.getInputStream());
					importFile = ImportExportUtility.parseImportCSV(service, gradebookUid, reader);
				}

				out.write(xstream.toXML(importFile)); 


			} catch (FileUploadException e) {
				System.out.println(e.getMessage());
			} catch (InvalidInputException iee) {
				System.out.println(iee.getMessage());
			} catch (FatalException fe) {
				System.out.println(fe.getMessage());
			}

		} else {
			System.out.println("Not multipart");
		}
	}

}
