package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class LastAccessTimesByAPIValue {
	long lastAccessTime;
	List<String> api_version_userId_context_facet;
	public LastAccessTimesByAPIValue(int lastAccessTime,
			List<String> api_version_userId_facet) {
		super();
		this.lastAccessTime = lastAccessTime;
		this.api_version_userId_context_facet = api_version_userId_facet;
	}
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	public List<String> getColumnNames() {
		return api_version_userId_context_facet;
	}
	public void setColumnNames(List<String> api_version_userId_facet) {
		this.api_version_userId_context_facet = api_version_userId_facet;
	}
	
}
