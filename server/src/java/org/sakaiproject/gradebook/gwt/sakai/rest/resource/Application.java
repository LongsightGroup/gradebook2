package org.sakaiproject.gradebook.gwt.sakai.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sakaiproject.gradebook.gwt.client.exceptions.GradebookCreationException;

@Path("application")
public class Application extends Resource {
	
	@GET
    @Produces("application/json")
    public String get() throws GradebookCreationException {
		return toJson(service.getApplicationSetup());
	}

}
