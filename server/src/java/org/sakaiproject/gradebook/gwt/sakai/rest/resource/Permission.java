package org.sakaiproject.gradebook.gwt.sakai.rest.resource;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;

@Path("/gradebook/rest/permission")
public class Permission extends Resource {

	@POST @Path("{uid}/{id}")
	@Consumes({"application/xml", "application/json"})
	public String create(@PathParam("uid") String gradebookUid, @PathParam("id") Long gradebookId, 
			String model) throws InvalidInputException {
		
		Map<String,Object> map = fromJson(model, Map.class);
		Map<String,Object> result = service.createPermission(gradebookUid, gradebookId, map);
		
		return toJson(result);
	}
		
	@DELETE
	@Consumes({"application/xml", "application/json"})
	public String remove(String model) throws InvalidInputException {
		Map<String,Object> map = fromJson(model, Map.class);
		Map<String,Object> result = service.deletePermission(map);
		
		return toJson(result);
	}
	
}
