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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.sakaiproject.gradebook.gwt.client.gxt.upload.ImportFile;
import org.sakaiproject.gradebook.gwt.client.model.Upload;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility;
import org.sakaiproject.gradebook.gwt.server.NewImportExportUtility;
import org.sakaiproject.gradebook.gwt.server.NewImportExportUtility.Delimiter;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class GradebookImportController extends SimpleFormController {

	private static final Log log = LogFactory.getLog(GradebookImportController.class);

	private Gradebook2ComponentService service;
	private GradebookToolService gbToolService;

	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response,
			Object command, BindException errors) throws Exception {

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;

		String gradebookUid = multipartRequest.getParameter("gradebookUid");

		EnumSet<Delimiter> delimiterSet = EnumSet.noneOf(Delimiter.class);
		for (Enumeration<String> paramNames = multipartRequest.getParameterNames();paramNames.hasMoreElements();) {
			String name = paramNames.nextElement();
			if (name.equals("delimiter:comma")) 
				delimiterSet.add(Delimiter.COMMA);
			else if (name.equals("delimiter:tab")) 
				delimiterSet.add(Delimiter.TAB);
			else if (name.equals("delimiter:space"))
				delimiterSet.add(Delimiter.SPACE);
			else if (name.equals("delimiter:colon"))
				delimiterSet.add(Delimiter.COLON);
		}
		
		String preventScantronOverwrite = multipartRequest.getParameter("preventScantronOverwrite");
		boolean doPreventScrantronOverwrite = preventScantronOverwrite == null ? Boolean.FALSE : Boolean.valueOf(preventScantronOverwrite);
		
		if (service.isOldImport()) {
			ImportExportUtility utility = new ImportExportUtility();
			
			for (Iterator<String> fileNameIterator = multipartRequest.getFileNames();fileNameIterator.hasNext();) {
				String fileName = fileNameIterator.next();
	
				MultipartFile file = multipartRequest.getFile(fileName);
				String origName = file.getOriginalFilename(); 
				ImportFile importFile;
	
				log.debug("Original Name: " + origName);
				if (origName.toLowerCase().endsWith("xls"))
				{
					log.debug("Excel file detected"); 
					importFile = utility.parseImportXLS(service, gradebookUid, file.getInputStream(), origName.toLowerCase(), gbToolService);
	
				}
				else
				{
					log.debug("Assuming CSV file"); 
					InputStreamReader reader = new InputStreamReader(file.getInputStream());
					importFile = utility.parseImportCSV(service, gradebookUid, reader);
				}
	
				PrintWriter writer = response.getWriter();
				response.setContentType("text/html");
				XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
				//log.debug("json: " + xstream.toXML(importFile) ); 
				
				writer.write(xstream.toXML(importFile)); 
				writer.flush();
				writer.close();
			}
			
		} else {
		
			NewImportExportUtility utility = new NewImportExportUtility();
			
			for (Iterator<String> fileNameIterator = multipartRequest.getFileNames();fileNameIterator.hasNext();) {
				String fileName = fileNameIterator.next();
	
				MultipartFile file = multipartRequest.getFile(fileName);
				String origName = file.getOriginalFilename(); 
				Upload importFile;
	
				log.debug("Original Name: " + origName);
				if (origName.toLowerCase().endsWith("xls"))
				{
					log.debug("Excel file detected"); 
					importFile = utility.parseImportXLS(service, gradebookUid, file.getInputStream(), origName.toLowerCase(), gbToolService, doPreventScrantronOverwrite);
	
				}
				else
				{
					log.debug("Assuming CSV file"); 
					InputStreamReader reader = new InputStreamReader(file.getInputStream());
					importFile = utility.parseImportCSV(service, gradebookUid, reader);
				}
	
				PrintWriter writer = response.getWriter();
				response.setContentType("text/html");
				//saveJsonToFile(importFile, "/tmp/data.json"); 
				
				writer.write(toJson(importFile)); 
				writer.flush();
				writer.close();
			}
		}

		return null;
	}
	
	private void saveJsonToFile(Upload importFile, String outfile) {

		File f = new File(outfile);
		f.delete(); 

		PrintWriter out;
		try {
			out = new PrintWriter(f);
			out.write(toJson(importFile, true));
			out.flush();
			out.close(); 
		} catch (FileNotFoundException e) {
			log.warn("Caught exception: " + e, e); 
		} 
	}

	protected String toJson(Object o)
	{
		return toJson(o, false); 
	}
	protected String toJson(Object o, boolean prettyPrint) {
		ObjectMapper mapper = new ObjectMapper();
		if (prettyPrint)
		{
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true); 
		}
		StringWriter w = new StringWriter();
		try {
			mapper.writeValue(w, o);
		} catch (Exception e) {
			log.error("Caught an exception serializing to JSON: ", e);
		}
		
		return w.toString();
	}

	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		// to actually be able to convert Multipart instance to byte[]
		// we have to register a custom editor
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		// now Spring knows how to handle multipart object and convert them
	}

	public Gradebook2ComponentService getService() {
		return service;
	}

	public void setService(Gradebook2ComponentService service) {
		this.service = service;
	}

	public GradebookToolService getGbToolService() {
		return gbToolService;
	}

	public void setGbToolService(GradebookToolService gbToolService) {
		this.gbToolService = gbToolService;
	}

}
