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
import org.wso2.carbon.apimgt.usage.client.pojo.APIFirstAccess;
import org.wso2.carbon.apimgt.usage.client.pojo.SubscriberCountByAPIs;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * usageClient class it use to expose the Statistic class instance. it responsible to make instance of the class that is provided by the api-manager.xml
 */
public class UsageClient {
    private static final Log log = LogFactory.getLog(UsageClient.class);
    private static APIUsageStatisticsClient usageStatisticsClient;

    /**
     * central point to initialise datasources or related configuration done by the admin-dashboard analytics section
     *
     * @throws APIMgtUsageQueryServiceClientException
     */
    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {

        try {
            APIUsageStatisticsClient client = UsageClient.getStatisticClient(null);
            client.initializeDataSource();
        } catch (APIMgtUsageQueryServiceClientException e) {
            throw new APIMgtUsageQueryServiceClientException("Error in initializing data sources", e);
        }

    }

    /**
     * central public method used to get the instance if the statistic client
     *
     * @return return the APIUsageStatisticsClient implementation
     * @throws APIMgtUsageQueryServiceClientException if error in creating instance
     */
    public static APIUsageStatisticsClient getClient(String user) throws APIMgtUsageQueryServiceClientException {
        if (isDataPublishingEnabled()) {
            try {
                return UsageClient.getStatisticClient(user);
            } catch (APIMgtUsageQueryServiceClientException e) {
                throw new APIMgtUsageQueryServiceClientException("Error getting Statistics usage client instance", e);
            }
        } else {
            return null;
        }
    }

    /**
     * Use to check whether analytics is enabled
     *
     * @return return boolean value indicating whether analytics enable
     */
    public static boolean isDataPublishingEnabled() {
        APIManagerAnalyticsConfiguration con = APIManagerAnalyticsConfiguration.getInstance();
        return con.isAnalyticsEnabled();
    }

    /**
     * Use to get instance of implementation class of the APIUsageStatisticsClient that is defined in the apim-manager.xml
     *
     * @return instance of a APIUsageStatisticsClient
     * @throws APIMgtUsageQueryServiceClientException throws if instantiation problem occur
     */
    private static APIUsageStatisticsClient getStatisticClient(String user)
            throws APIMgtUsageQueryServiceClientException {

        //read the api-manager.xml and get the Statistics class name
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String className = config.getFirstProperty("StatisticClientProvider");

        try {

            //get the Class from the class name
            Class statClass = APIUtil.getClassForName(className);
            //use the constructor and pass appropriate args to get a instance
            if (user != null) {
                usageStatisticsClient = (APIUsageStatisticsClient) statClass.getConstructor(String.class)
                        .newInstance(user);
            } else {
                usageStatisticsClient = (APIUsageStatisticsClient) statClass.getConstructor().newInstance();
            }

        } catch (InstantiationException e) {
            throw new APIMgtUsageQueryServiceClientException("Cannot instantiate Statistic Client class: " + className,
                    e);
        } catch (IllegalAccessException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Cannot access the constructor in Statistic Client class: " + className, e);
        } catch (InvocationTargetException e) {
            throw new APIMgtUsageQueryServiceClientException("Error occurred while getting constructor");
        } catch (NoSuchMethodException e) {
            throw new APIMgtUsageQueryServiceClientException(
                    "Cannot found expected constructor in Statistic Client class: " + className, e);
        } catch (ClassNotFoundException e) {
            throw new APIMgtUsageQueryServiceClientException("Cannot found the Statistic Client class: " + className,
                    e);
        }

        return usageStatisticsClient;
    }

    /**
     * Get the Subscriber count and information related to the APIs
     *
     * @param loggedUser user of the current session
     * @return return list of SubscriberCountByAPIs objects. which contain the list of apis and related subscriber counts
     * @throws APIManagementException throws exception if error occur
     */
    public static List<SubscriberCountByAPIs> getSubscriberCountByAPIs(String loggedUser, boolean isAllStatistics)
            throws APIManagementException {

        //get the provider
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);
        String providerName = null;
        if (isAllStatistics) {
            providerName = "__all_providers__";
        } else {
            providerName = loggedUser;
        }

        List<SubscriberCountByAPIs> list = new ArrayList<SubscriberCountByAPIs>();
        boolean isTenantFlowStarted = false;
        try {
            loggedUser = APIUtil.replaceEmailDomain(loggedUser);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(loggedUser));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {
                List<API> apiSet;
                //get the apis
                if (providerName.equals("__all_providers__")) {
                    apiSet = apiProvider.getAllAPIs();
                } else {
                    apiSet = apiProvider.getAPIsByProvider(APIUtil.replaceEmailDomain(loggedUser));
                }

                //iterate over apis
                for (API api : apiSet) {
                    //ignore created apis
                    if (api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }
                    //ignore 0 counts
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
        /*} catch (Exception e) {
            log.error("Error while getting subscribers of the provider: " + providerName, e);
            throw new APIManagementException("Error while getting subscribers of the provider: " + providerName, e);
            */
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return list;

    }

    /**
     * getting the configured the statistics client type
     *
     * @return string value indicating type
     */
    public static String getStatClientType() {
        String type = null;
        try {
            type = UsageClient.getStatisticClient(null).getClientType();
        } catch (APIMgtUsageQueryServiceClientException e) {
            //throw new APIMgtUsageQueryServiceClientException("Error getting Statistics usage client instance", e);
            log.warn("Error geting usage statistic client...");
        }

        return type;
    }
}
