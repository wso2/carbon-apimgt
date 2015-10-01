package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class APIUsageByDestinationValue {
	int totalRequestCount;
	List<String> api_version_context_dest_facet;
	public APIUsageByDestinationValue(int lastAccessTime,
			List<String> api_version_userId_facet) {
		super();
		this.totalRequestCount = lastAccessTime;
		this.api_version_context_dest_facet = api_version_userId_facet;
	}
	public int getTotalRequesCount() {
		return totalRequestCount;
	}
	public void setTotalRequesCount(int lastAccessTime) {
		this.totalRequestCount = lastAccessTime;
	}
	public List<String> getColumnNames() {
		return api_version_context_dest_facet;
	}
	public void setColumnNames(List<String> api_version_userId_facet) {
		this.api_version_context_dest_facet = api_version_userId_facet;
	}
	
}
