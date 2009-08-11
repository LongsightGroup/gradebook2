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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.gradebook.gwt.client.exceptions.FatalException;
import org.sakaiproject.gradebook.gwt.server.ImportExportUtility;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class GradebookExportController implements Controller {

	private static final Log log = LogFactory.getLog(GradebookExportController.class);

	private Gradebook2Service service;

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		PrintWriter writer = response.getWriter();

		String queryString = request.getQueryString();
		int n = queryString.indexOf("gradebookUid=") + 13;
		int m = queryString.indexOf("&include=");

		boolean doIncludeStructure = m != -1;

		String gradebookUid = queryString.substring(n);

		if (doIncludeStructure)
			gradebookUid = queryString.substring(n, m);

		try {
			ImportExportUtility.exportGradebook(service, gradebookUid, doIncludeStructure, false, writer, response);
		} catch (FatalException e) {
			log.error("EXCEPTION: Wasn't able to export gradebook: " + gradebookUid, e);
			// 500 Internal Server Error
			response.setStatus(500);
		}
		writer.flush();
		writer.close();

		return null;
	}

	public Gradebook2Service getService() {
		return service;
	}

	public void setService(Gradebook2Service service) {
		this.service = service;
	}
}
