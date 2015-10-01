package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class UsageByAPIVersionsValue {
	int totalRequestCount;
	List<String> api_version_context_facet;
	public UsageByAPIVersionsValue(int lastAccessTime,
			List<String> api_version_userId_facet) {
		super();
		this.totalRequestCount = lastAccessTime;
		this.api_version_context_facet = api_version_userId_facet;
	}
	public int getTotalRequestCount() {
		return totalRequestCount;
	}
	public void setTotalRequestCount(int lastAccessTime) {
		this.totalRequestCount = lastAccessTime;
	}
	public List<String> getColumnNames() {
		return api_version_context_facet;
	}
	public void setColumnNames(List<String> api_version_userId_facet) {
		this.api_version_context_facet = api_version_userId_facet;
	}
	
}
