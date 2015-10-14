/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.pojo.SubscriberCountByAPIs;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class UsageClient {
    private static final Log log = LogFactory.getLog(UsageClient.class);
    private static APIUsageStatisticsClient usageStatisticsClient;

    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {

        try {
            APIUsageStatisticsClient client = UsageClient.getStatisticClient();
            client.initializeDataSource();
        } catch (APIMgtUsageQueryServiceClientException e) {
            throw new APIMgtUsageQueryServiceClientException("Error in initializing data sources", e);
        }

    }

    public static APIUsageStatisticsClient getClient() throws APIMgtUsageQueryServiceClientException {
        if (isDataPublishingEnabled()) {
            try {
                APIUsageStatisticsClient client = UsageClient.getStatisticClient();
                return client;
            } catch (APIMgtUsageQueryServiceClientException e) {
                throw new APIMgtUsageQueryServiceClientException("Error getting Statistics usage client instance", e);
            }
        } else {
            return null;
        }
    }

    public static boolean isDataPublishingEnabled() {
        APIManagerAnalyticsConfiguration con = APIManagerAnalyticsConfiguration.getInstance();
        return con.isAnalyticsEnabled();
    }

    public static List<SubscriberCountByAPIs> getSubscriberCountByAPIs(String loggedUser)
            throws APIManagementException {
        String providerName = null;

        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);

        List<SubscriberCountByAPIs> list = new ArrayList<SubscriberCountByAPIs>();
        boolean isTenantFlowStarted = false;
        try {
            providerName = APIUtil.replaceEmailDomain(loggedUser);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {
                List<API> apiSet;
                if (providerName.equals("__all_providers__")) {
                    apiSet = apiProvider.getAllAPIs();
                } else {
                    apiSet = apiProvider.getAPIsByProvider(APIUtil.replaceEmailDomain(providerName));
                }

                //                List<SubscriberCountByAPIs> subscriptionData = new ArrayList<SubscriberCountByAPIs>();
                //                Map<String, Long> subscriptions = new TreeMap<String, Long>();

                for (API api : apiSet) {
                    if (api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    if (count == 0) {
                        continue;
                    }

                    SubscriberCountByAPIs apiSub = new SubscriberCountByAPIs();
                    List<String> apiName = new ArrayList<String>();
                    apiName.add(api.getId().getApiName());
                    apiName.add(api.getId().getVersion());
                    apiName.add(api.getId().getProviderName());

                    apiSub.setCount(count);
                    apiSub.setApiName(apiName);
                    list.add(apiSub);
                }

            }
        } catch (Exception e) {
            //handleException("Error while getting subscribers of the provider: " + providerName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return list;
    }

    private static APIUsageStatisticsClient getStatisticClient() throws APIMgtUsageQueryServiceClientException {

        if (usageStatisticsClient != null) {
            return usageStatisticsClient;
        }

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String className = config.getFirstProperty("StatisticClientProvider");

        try {
            usageStatisticsClient = (APIUsageStatisticsClient) Class.forName(className).getConstructor(String.class)
                    .newInstance("");
        } catch (InstantiationException e) {
            throw new APIMgtUsageQueryServiceClientException("Cannot instantiate Statistic Client class: " + className,
                    e);
        } catch (IllegalAccessException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Cannot access the constructor in Statistic Client class: " + className, e);
        } catch (InvocationTargetException e) {
            throw new APIMgtUsageQueryServiceClientException("");
        } catch (NoSuchMethodException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Cannot found expected constructor in Statistic Client class: " + className, e);
        } catch (ClassNotFoundException e) {
            throw new APIMgtUsageQueryServiceClientException("Cannot found the Statistic Client class: " + className,
                    e);
        }

        return usageStatisticsClient;
    }

}
