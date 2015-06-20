package org.wso2.carbon.apimgt.usage.client.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.usage.client.service.APIPublisherUsageService;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.usage.client.info.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.dto.APIDestinationUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIRequestsByHourDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIRequestsByUserAgentsDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResourcePathUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResponseFaultCountDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIResponseTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageByUserDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionLastAccessTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.PerUserAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class APIPublisherUsageServiceImpl implements APIPublisherUsageService {

	private static final Log log = LogFactory.getLog(APIPublisherUsageServiceImpl.class);
	private APIProvider apiProvider;
	private String username;
	private APIUsageStatisticsClient client;

	public APIPublisherUsageServiceImpl(String username) throws APIManagementException {
		apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
		this.username = username;
		try {
			if(APIUtil.isStatPublishingEnabled() && APIUtil.isUsageDataSourceSpecified()) {
				//Client instance will only be created if data source configured and stat publishing enabled
				client = new APIUsageStatisticsClient(username);
			}
		} catch (APIMgtUsageQueryServiceClientException e) {
			String msg = "Error while creating the api usage client";
			log.error(msg, e);
			throw new APIManagementException(msg, e);
		}
	}

	public List<APIResponseFaultCount> getAPIFaultyAnalyzeByTime(String providerName) {
		List<APIResponseFaultCount> list = new ArrayList<APIResponseFaultCount>();
		if (!APIUtil.isStatPublishingEnabled()) {
			return list;
		}
		if (!APIUtil.isUsageDataSourceSpecified()) {
			return list;
		}

		List<APIResponseFaultCountDTO> faultCountList = null;
		try {
			faultCountList = client.getAPIFaultyAnalyzeByTime(providerName);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		APIResponseFaultCount apiFaultCount;
		if (faultCountList != null) {
			for (APIResponseFaultCountDTO fault : faultCountList) {
				apiFaultCount = new APIResponseFaultCount();
				long faultTime = Long.parseLong(fault.getRequestTime());
				apiFaultCount.setApiName(fault.getApiName());
				apiFaultCount.setApiVersion(fault.getVersion());
				apiFaultCount.setApiContext(fault.getContext());
				apiFaultCount.setRequestTime(faultTime);
				list.add(apiFaultCount);
			}
		}
		return list;
	}

	//TODO currently dates are taken as string params. Should take date params
	@Override
	public List<APIResponseFaultCount> getAPIResponseFaultCount(String providerName, String fromDate, String toDate) {
		List<APIResponseFaultCount> responseFaultCounts = new ArrayList<APIResponseFaultCount>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return responseFaultCounts;
		}
		if (!APIUtil.isUsageDataSourceSpecified()) {
			return responseFaultCounts;
		}

		List<APIResponseFaultCountDTO> responseFaultCountsDTOs = null;
		try {
			responseFaultCountsDTOs = client.getAPIResponseFaultCount(providerName, fromDate, toDate);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		APIResponseFaultCount responseFaultCount;
		if (responseFaultCountsDTOs != null) {
			for (APIResponseFaultCountDTO responseFault : responseFaultCountsDTOs) {
				responseFaultCount = new APIResponseFaultCount();
				responseFaultCount.setApiName(responseFault.getApiName());
				responseFaultCount.setApiVersion(responseFault.getVersion());
				responseFaultCount.setApiContext(responseFault.getContext());
				responseFaultCount.setCount(responseFault.getCount());
				responseFaultCount.setFaultPercentage(responseFault.getFaultPercentage());
				responseFaultCount.setTotalRequestCount(responseFault.getRequestCount());
				responseFaultCounts.add(responseFaultCount);
			}
		}
		return responseFaultCounts;
	}

	//TODO refactor to return either date or generic type rather returning strings for year, date and day
	public APIAccessTime getFirstAccessTime(String providerName) {
		APIAccessTime apiAccessTime = new APIAccessTime();
		if (!APIUtil.isStatPublishingEnabled()) {
			return apiAccessTime;
		}
		if (!APIUtil.isUsageDataSourceSpecified()) {
			return apiAccessTime;
		}

		List<String> list = null;

		try {
			list = client.getFirstAccessTime(providerName, 1);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		if (!list.isEmpty()) {
			apiAccessTime.setYear(list.get(0).toString());
			apiAccessTime.setDate(list.get(1).toString());
			apiAccessTime.setDay(list.get(2).toString());
		}
		return apiAccessTime;
	}

	public List<APIResponseTime> getProviderAPIServiceTime(String providerName, String fromDate, String toDate) {

		List<APIResponseTime> responseTimes = new ArrayList<APIResponseTime>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return responseTimes;
		}

		if (!APIUtil.isUsageDataSourceSpecified()) {
			return responseTimes;
		}

		List<APIResponseTimeDTO> apiResponseTimeDTOs = null;
		try {
			apiResponseTimeDTOs = client.getResponseTimesByAPIs(providerName, fromDate, toDate, 50);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIServiceTime", e);
		}

		APIResponseTime apiResponseTime;
		if (apiResponseTimeDTOs != null) {
			for (APIResponseTimeDTO responseTime : apiResponseTimeDTOs) {
				apiResponseTime = new APIResponseTime();
				apiResponseTime.setApiName(responseTime.getApiName());
				apiResponseTime.setServiceTime(responseTime.getServiceTime());
				responseTimes.add(apiResponseTime);
			}
		}
		return responseTimes;
	}

	public List<APIHourlyRequestUsage> getAPIRequestsPerHour(String apiName, String fromDate, String toDate) {
		List<APIHourlyRequestUsage> apiHourlyRequests = new ArrayList<APIHourlyRequestUsage>();

		if (!APIUtil.isUsageDataSourceSpecified()) {
			return apiHourlyRequests;
		}
		List<APIRequestsByHourDTO> list = null;

		try {
			list = client.getAPIRequestsByHour(fromDate, toDate, apiName);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionLastAccess", e);
		}
		APIHourlyRequestUsage usage;
		if (list != null) {
			for (APIRequestsByHourDTO apiRequestByHour : list) {
				usage = new APIHourlyRequestUsage();
				usage.setApiName(apiRequestByHour.getApi());
				usage.setDate(apiRequestByHour.getDate());
				usage.setRequestCount(apiRequestByHour.getRequestCount());
				usage.setTierName(apiRequestByHour.getTier());
				apiHourlyRequests.add(usage);
			}
		}
		return apiHourlyRequests;
	}

	@Override
	public List<APIUserAgentUsage> getUserAgentSummaryForALLAPIs() {
		List<APIUserAgentUsage> apiUserAgentUsages = new ArrayList<APIUserAgentUsage>();

		List<APIRequestsByUserAgentsDTO> userAgentUsages = null;
		try {
			userAgentUsages = client.getUserAgentSummaryForALLAPIs();
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionLastAccess", e);
		}

		APIUserAgentUsage userAgentUsage;

		if (userAgentUsages != null) {
			for(APIRequestsByUserAgentsDTO usage : userAgentUsages) {
				userAgentUsage = new APIUserAgentUsage();
				userAgentUsage.setUserAgentName(APIUtil.userAgentParser(usage.getUserAgent()));
				userAgentUsage.setRequestCount(usage.getCount());
				apiUserAgentUsages.add(userAgentUsage);
			}
		}

		return apiUserAgentUsages;
	}


	public List<APIVersionLastAccessTime> getProviderAPIVersionUserLastAccess(String providerName, String fromDate, String toDate) {
		List<APIVersionLastAccessTime> accessTimes = new ArrayList<APIVersionLastAccessTime>();


		if (!APIUtil.isStatPublishingEnabled()) {
			return accessTimes;
		}

		if (!APIUtil.isUsageDataSourceSpecified()) {
			return accessTimes;
		}

		List<APIVersionLastAccessTimeDTO> usages = null;
		try {
			usages = client.getLastAccessTimesByAPI(providerName, fromDate, toDate, 50);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionLastAccess", e);
		}

		APIVersionLastAccessTime apiVersionLastAccessTime;
		if(usages != null) {
			for(APIVersionLastAccessTimeDTO usage :usages) {
				apiVersionLastAccessTime = new APIVersionLastAccessTime();
				apiVersionLastAccessTime.setApiName(usage.getApiName());
				apiVersionLastAccessTime.setApiVersion(usage.getApiVersion());
				apiVersionLastAccessTime.setUsername(usage.getUser());
				apiVersionLastAccessTime.setLastAccess(new Date(String.valueOf(usage.getLastAccessTime())));
			}
		}

		return accessTimes;
	}

	@Override
	public List<APIPerUserAPIUsage> getProviderAPIVersionUserUsage(String providerName, String apiName, String apiVersion) {
		List<APIPerUserAPIUsage> perUserAPIUsages = new ArrayList<APIPerUserAPIUsage>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return perUserAPIUsages;
		}

		if (!APIUtil.isUsageDataSourceSpecified()) {
			return perUserAPIUsages;
		}

		List<PerUserAPIUsageDTO> usages = null;
		try {
			usages = client.getUsageBySubscribers(providerName, apiName, apiVersion, 10);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
		}

		APIPerUserAPIUsage perUserAPIUsage;
		if(usages != null) {
			for (PerUserAPIUsageDTO userAPIUsageDTO : usages) {
				perUserAPIUsage = new APIPerUserAPIUsage();
				perUserAPIUsage.setUsername(userAPIUsageDTO.getUsername());
				perUserAPIUsage.setCount(userAPIUsageDTO.getCount());
				perUserAPIUsages.add(perUserAPIUsage);
			}
		}
		return perUserAPIUsages;
	}

	@Override
	public List<APIUserUsage> getAPIUsageByUser(String providerName, String fromDate, String toDate) {
		List<APIUserUsage> apiUserUsages = new ArrayList<APIUserUsage>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return apiUserUsages;
		}
		if(!APIUtil.isUsageDataSourceSpecified()){
			return apiUserUsages;
		}

		List<APIUsageByUserDTO> usages = null;
		try {
			usages = client.getAPIUsageByUser(providerName,fromDate,toDate);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		APIUserUsage apiUserUsage;
		if(usages != null) {
			for(APIUsageByUserDTO usage : usages) {
				apiUserUsage = new APIUserUsage();
				apiUserUsage.setApiName(usage.getApiName());
				apiUserUsage.setApiVersion(usage.getVersion());
				apiUserUsage.setUserId(usage.getUserID());
				apiUserUsage.setCount(usage.getCount());

			}
		}

		return apiUserUsages;
	}

	@Override
	public List<APIDestinationUsage> getAPIUsageByDestination(String providerName, String fromDate, String toDate) {

		List<APIDestinationUsage> destinationUsages = new ArrayList<APIDestinationUsage>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return destinationUsages;
		}
		if (!APIUtil.isUsageDataSourceSpecified()) {
			return destinationUsages;
		}

		List<APIDestinationUsageDTO> usages = null;
		try {

			usages = client.getAPIUsageByDestination(providerName, fromDate, toDate);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage ", e);
		}

		if(usages != null) {
			APIDestinationUsage destinationUsage;
			for (APIDestinationUsageDTO usage : usages) {
				destinationUsage = new APIDestinationUsage();
				destinationUsage.setApiName(usage.getApiName());
				destinationUsage.setApiVersion(usage.getVersion());
				destinationUsage.setDestination(usage.getDestination());
				destinationUsage.setApiContext(usage.getContext());
				destinationUsage.setCount(usage.getCount());
				}
			}
		return destinationUsages;
	}

	@Override
	public List<APIResourcePathUsage> getAPIUsageByResourcePath(String providerName, String fromDate, String toDate) {
		List<APIResourcePathUsage> apiResourcePathUsages = new ArrayList<APIResourcePathUsage>();

		if (!APIUtil.isStatPublishingEnabled()) {
			return apiResourcePathUsages;
		}
		if (!APIUtil.isUsageDataSourceSpecified()) {
			return apiResourcePathUsages;
		}

		List<APIResourcePathUsageDTO> usages = null;
		try {
			usages = client.getAPIUsageByResourcePath(providerName, fromDate, toDate);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		if(usages != null) {
			APIResourcePathUsage resourcePathUsage;
			for(APIResourcePathUsageDTO usage : usages) {
				resourcePathUsage = new APIResourcePathUsage();
				resourcePathUsage.setApiName(usage.getApiName());
				resourcePathUsage.setApiVersion(usage.getVersion());
				resourcePathUsage.setMethod(usage.getMethod());
				resourcePathUsage.setApiContext(usage.getContext());
				resourcePathUsage.setCount(usage.getCount());
				resourcePathUsage.setTime(usage.getTime());
				apiResourcePathUsages.add(resourcePathUsage);
			}
		}
		return apiResourcePathUsages;
	}

	@Override
	public List<APIUserUsage> getProviderAPIUserUsage(String providerName, String apiName) {

		List<APIUserUsage> apiUserUsages = new ArrayList<APIUserUsage>();
		if (!APIUtil.isStatPublishingEnabled()) {
			return apiUserUsages;
		}
		if(!APIUtil.isUsageDataSourceSpecified()){
			return apiUserUsages;
		}

		List<PerUserAPIUsageDTO> usages = null;
		try {
			usages = client.getUsageBySubscribers(providerName, apiName, 10);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
		}

		APIUserUsage apiUserUsage;
		if(usages != null) {
			for(PerUserAPIUsageDTO usage : usages) {
				apiUserUsage = new APIUserUsage();
				apiUserUsage.setUsername(usage.getUsername());
				apiUserUsage.setCount(usage.getCount());
			}
		}
		return apiUserUsages;
	}

	@Override
	public List<APIUserUsage> getProviderAPIUsage(String providerName, String fromDate, String toDate) {

		List<APIUserUsage> apiUserUsages = new ArrayList<APIUserUsage>();
		if (!APIUtil.isStatPublishingEnabled()) {
			return apiUserUsages;
		}
		if(!APIUtil.isUsageDataSourceSpecified()){
			return apiUserUsages;
		}

		List<APIUsageDTO> usages = null;
		try {
			usages = client.getUsageByAPIs(providerName, fromDate, toDate, 10);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
		}

		APIUserUsage apiUserUsage;
		if(usages != null) {
			for(APIUsageDTO usage : usages) {
				apiUserUsage = new APIUserUsage();
				apiUserUsage.setApiName(usage.getApiName());
				apiUserUsage.setCount(usage.getCount());
			}
		}
		return apiUserUsages;
	}

	@Override
	public List<APIUserUsage> getProviderAPIVersionUsage(String providerName, String apiName) {

		List<APIUserUsage> apiUserUsages = new ArrayList<APIUserUsage>();
		if (!APIUtil.isStatPublishingEnabled()) {
			return apiUserUsages;
		}
		if(!APIUtil.isUsageDataSourceSpecified()){
			return apiUserUsages;
		}

		List<APIVersionUsageDTO> usages = null;
		try {
			usages = client.getUsageByAPIVersions(providerName, apiName);
		} catch (APIMgtUsageQueryServiceClientException e) {
			log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionUsage", e);
		}

		APIUserUsage apiUserUsage;
		if(usages != null) {
			for(APIVersionUsageDTO usage : usages) {
				apiUserUsage = new APIUserUsage();
				apiUserUsage.setApiVersion(usage.getVersion());
				apiUserUsage.setCount(usage.getCount());
			}
		}
		return apiUserUsages;
	}

	public List<APIVersionUsageCount> getSubscriberCountByAPIVersions(String apiName, String providerName)
			throws APIManagementException {
		List<APIVersionUsageCount> apiUsageCounts = new ArrayList<APIVersionUsageCount>();
		boolean isTenantFlowStarted = false;
		try {
			providerName = APIUtil.replaceEmailDomain(providerName);
			String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
			if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				isTenantFlowStarted = true;
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
			}
			APIVersionUsageCount apiUsageCount;
			if (providerName != null && apiName != null) {
				Set<String> versions = apiProvider.getAPIVersions(APIUtil.replaceEmailDomain(providerName), apiName);
				for (String version : versions) {
					apiUsageCount = new APIVersionUsageCount();
					APIIdentifier id = new APIIdentifier(providerName, apiName, version);
					API api = apiProvider.getAPI(id);
					if (api.getStatus() == APIStatus.CREATED) {
						continue;
					}
					long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
					if (count == 0) {
						continue;
					}
					apiUsageCount.setApiName(apiName);
					apiUsageCount.setApiVersion(api.getId().getVersion());
					apiUsageCount.setCount(count);
					apiUsageCounts.add(apiUsageCount);
				}
			}
		} catch (Exception e) {
			log.error("Error while getting subscribers of the " +
			          "provider: " + providerName + " and API: " + apiName, e);
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
		return apiUsageCounts;
	}

}
