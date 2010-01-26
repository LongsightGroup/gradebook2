package org.sakaiproject.gradebook.gwt.sakai.rest.resource;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/gradebook/rest/statistics/{uid}/{id}")
public class Statistics extends Resource {

	@GET 
    @Produces("application/json")
    public String get(@PathParam("uid") String gradebookUid, @PathParam("id") Long gradebookId) {
		List<Map<String,Object>> list = service.getStatistics(gradebookUid, gradebookId, null);
		return toJson(list, list.size());
	}
	
	@GET @Path("{studentUid}")
    @Produces("application/json")
    public String get(@PathParam("uid") String gradebookUid, @PathParam("id") Long gradebookId,
    		@PathParam("studentUid") String studentUid) {
		List<Map<String,Object>> list = service.getStatistics(gradebookUid, gradebookId, studentUid);
		return toJson(list, list.size());
	}
	
}
