package org.wso2.carbon.apimgt.usage.client.bean;

import java.util.List;

public class APIResponseFaultCountValue {
	int totalFaultCount;
	List<String> api_version_apiPublisher_context_facet;
	public APIResponseFaultCountValue(int totalFaultCount,
			List<String> api_version_userId_facet) {
		super();
		this.totalFaultCount = totalFaultCount;
		this.api_version_apiPublisher_context_facet = api_version_userId_facet;
	}
	public int getTotalFaultCount() {
		return totalFaultCount;
	}
	public void setTotalFaultCount(int totalFaultCount) {
		this.totalFaultCount = totalFaultCount;
	}
	public List<String> getColumnNames() {
		return api_version_apiPublisher_context_facet;
	}
	public void setColumnNames(List<String> api_version_userId_facet) {
		this.api_version_apiPublisher_context_facet = api_version_userId_facet;
	}
	
}
