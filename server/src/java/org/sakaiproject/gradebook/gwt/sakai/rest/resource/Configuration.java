package org.sakaiproject.gradebook.gwt.sakai.rest.resource;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.gradebook.gwt.client.exceptions.InvalidInputException;
import org.sakaiproject.gradebook.gwt.client.model.key.ConfigurationKey;

@Path("/gradebook/rest/config")
public class Configuration extends Resource {

	private static final Log log = LogFactory.getLog(Configuration.class);
	
	@PUT @Path("{gradebookId}")
	@Consumes({"application/xml", "application/json"})
	public void update(@PathParam("gradebookId") Long gradebookId,
			String model) throws InvalidInputException {

		Map<String,Object> map = fromJson(model, Map.class);
		
		for (String field : map.keySet()) {
			if (!field.equals(ConfigurationKey.GRADEBOOKID.name()) &&
					!field.equals(ConfigurationKey.USERUID.name())) {
				String value = String.valueOf(map.get(field));
				service.updateConfiguration(gradebookId, field, value);
			}
		}
	}

	
}
