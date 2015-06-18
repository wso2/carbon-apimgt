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
package org.wso2.carbon.apimgt.usage.client.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.client.info.APIAccessTime;
import org.wso2.carbon.apimgt.usage.client.info.APIDestinationUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIHourlyRequestUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIPerUserAPIUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIResourcePathUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIResponseFaultCount;
import org.wso2.carbon.apimgt.usage.client.info.APIResponseTime;
import org.wso2.carbon.apimgt.usage.client.info.APIUserAgentUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIUserUsage;
import org.wso2.carbon.apimgt.usage.client.info.APIVersionLastAccessTime;
import org.wso2.carbon.apimgt.usage.client.info.APIVersionUsageCount;

import java.util.List;

public interface APIPublisherUsageService {

	public List<APIResponseFaultCount> getAPIFaultyAnalyzeByTime(String providerName);

	public List<APIVersionUsageCount> getSubscriberCountByAPIVersions(String apiName, String providerName) throws
			APIManagementException;

	public List<APIResponseFaultCount> getAPIResponseFaultCount(String providerName, String fromDate, String toDate);

	public List<APIResponseTime> getProviderAPIServiceTime(String providerName, String fromDate, String toDate);

	public List<APIHourlyRequestUsage> getAPIRequestsPerHour(String apiName, String fromDate, String toDate);

	public APIAccessTime getFirstAccessTime(String providerName);

	public List<APIUserAgentUsage> getUserAgentSummaryForALLAPIs();

	public List<APIVersionLastAccessTime> getProviderAPIVersionUserLastAccess(String providerName, String fromDate, String
			toDate);

	public List<APIPerUserAPIUsage> getProviderAPIVersionUserUsage(String providerName, String apiName, String apiVersion);

	public List<APIUserUsage> getAPIUsageByUser(String providerName, String fromDate, String toDate);

	public List<APIDestinationUsage> getAPIUsageByDestination(String providerName, String fromDate, String toDate);

	public List<APIResourcePathUsage> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate);

	public List<APIUserUsage> getProviderAPIUserUsage(String providerName, String apiName);

	public List<APIUserUsage> getProviderAPIUsage(String providerName, String fromDate, String toDate);

	public List<APIUserUsage> getProviderAPIVersionUsage(String providerName, String apiName);
}
