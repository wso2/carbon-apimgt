/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.usage.client.info;

public class APIResponseFaultCount {
	private String apiName;
	private String apiVersion;
	private String apiContext;
	private Long requestTime;
	private double faultPercentage;
	private Long totalRequestCount;
	private Long count;

	public Long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiContext() {
		return apiContext;
	}

	public void setApiContext(String apiContext) {
		this.apiContext = apiContext;
	}

	public double getFaultPercentage() {
		return faultPercentage;
	}

	public void setFaultPercentage(double faultPercentage) {
		this.faultPercentage = faultPercentage;
	}

	public Long getTotalRequestCount() {
		return totalRequestCount;
	}

	public void setTotalRequestCount(Long totalRequestCount) {
		this.totalRequestCount = totalRequestCount;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
}
