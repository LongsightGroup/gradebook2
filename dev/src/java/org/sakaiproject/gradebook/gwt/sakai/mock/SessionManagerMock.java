package org.sakaiproject.gradebook.gwt.sakai.mock;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

public class SessionManagerMock implements SessionManager {

	public int getActiveUserCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Session getCurrentSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentSessionUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	public ToolSession getCurrentToolSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public Session getSession(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Session> getSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	public String makeSessionId(HttpServletRequest arg0, Principal arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCurrentSession(Session arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setCurrentToolSession(ToolSession arg0) {
		// TODO Auto-generated method stub
		
	}

	public Session startSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public Session startSession(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
