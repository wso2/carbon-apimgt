package org.apache.synapse.endpoints.dispatch;

/**
 * Represents an HTTP Cookie used for Session
 */
public class SessionCookie {

	private String sessionId;
	private String path;
	
	public SessionCookie() {
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "SessionCookie [sessionId=" + sessionId + ", path=" + path + "]";
	}
}
