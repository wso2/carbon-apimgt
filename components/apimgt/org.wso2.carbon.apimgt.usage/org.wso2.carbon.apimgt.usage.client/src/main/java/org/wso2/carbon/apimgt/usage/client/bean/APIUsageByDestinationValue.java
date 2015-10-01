package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class APIUsageByDestinationValue {
	int totalRequesCount;
	List<String> api_version_context_method_facet;
	public APIUsageByDestinationValue(int lastAccessTime,
			List<String> api_version_userId_facet) {
		super();
		this.totalRequesCount = lastAccessTime;
		this.api_version_context_method_facet = api_version_userId_facet;
	}
	public int getTotalRequesCount() {
		return totalRequesCount;
	}
	public void setTotalRequesCount(int lastAccessTime) {
		this.totalRequesCount = lastAccessTime;
	}
	public List<String> getColumnNames() {
		return api_version_context_method_facet;
	}
	public void setColumnNames(List<String> api_version_userId_facet) {
		this.api_version_context_method_facet = api_version_userId_facet;
	}
	
}
