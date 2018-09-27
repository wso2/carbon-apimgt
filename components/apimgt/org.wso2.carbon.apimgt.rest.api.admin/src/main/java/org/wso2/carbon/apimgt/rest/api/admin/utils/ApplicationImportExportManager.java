/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationImportExportManager {
    private static final Log log = LogFactory.getLog(ApplicationImportExportManager.class);
    private APIConsumer apiConsumer;

    ApplicationImportExportManager(APIConsumer apiConsumer) {
        this.apiConsumer = apiConsumer;
    }

    /**
     * Retrieve all the details of an Application by name for a given user.
     *
     * @param appName name of the application
     * @return {@link Application} instance
     * @throws APIManagementException if an error occurs while retrieving Application details
     */
    public Application getApplicationDetails(String appName, String username) throws
            APIManagementException {
        Application application;
        int appId = APIUtil.getApplicationId(appName, username);
        String groupId = apiConsumer.getGroupId(appId);

        if (Boolean.getBoolean(RestApiConstants.MIGRATION_ENABLED)) {
            application = apiConsumer.getApplicationsByName(username, appName, groupId);
        } else {
            application = apiConsumer.getApplicationById(appId);
        }
        if (application != null) {
            application.setGroupId(groupId);
            application.setOwner(application.getSubscriber().getName());
        }
        return application;
    }

    /**
     * Check whether a provided userId corresponds to a valid consumer of the store and subscribe if valid
     *
     * @param userId  username of the Owner
     * @param groupId the groupId to which the target subscriber belongs to
     * @throws APIManagementException if an error occurs while checking the validity of user
     */
    public void validateOwner(String userId, String groupId) throws APIManagementException {
        Subscriber subscriber = apiConsumer.getSubscriber(userId);
        try {
            if (subscriber == null && !APIUtil.isPermissionCheckDisabled()) {
                APIUtil.checkPermission(userId, APIConstants.Permissions.API_SUBSCRIBE);
                apiConsumer.addSubscriber(userId, groupId);
            }
        } catch (APIManagementException e) {
            String errorMsg = "Provided Application Owner is Invalid";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Check whether a provided userId corresponds to a valid Application Owner
     *
     * @param userId username of the Owner
     * @return true, if subscriber is available
     * @throws APIManagementException if an error occurs while checking the availability of user
     */
    public boolean isOwnerAvailable(String userId) throws APIManagementException {
        if (userId != null) {
            Subscriber subscriber = apiConsumer.getSubscriber(userId);
            return subscriber != null;
        }
        return false;
    }

    /**
     * Import and add subscriptions of a particular application for the available APIs
     *
     * @param appDetails details of the imported application
     * @param userId     username of the subscriber
     * @param appId      application Id
     * @return a list of APIIdentifiers of the skipped subscriptions
     * @throws APIManagementException if an error occurs while importing and adding subscriptions
     */
    public List<APIIdentifier> importSubscriptions(Application appDetails, String userId, int appId)
            throws APIManagementException, UserStoreException {
        List<APIIdentifier> skippedAPIList = new ArrayList<>();
        Set<SubscribedAPI> subscribedAPIs = appDetails.getSubscribedAPIs();
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            APIIdentifier apiIdentifier = subscribedAPI.getApiId();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack
                    (apiIdentifier.getProviderName()));
            if (!StringUtils.isEmpty(tenantDomain) && RestApiUtil.isTenantAvailable(tenantDomain)) {
                String name = apiIdentifier.getApiName();
                String version = apiIdentifier.getVersion();
                //creating a solr compatible search query
                StringBuilder searchQuery = new StringBuilder();
                String[] searchCriteria = {name, "version:" + version};
                for (int i = 0; i < searchCriteria.length; i++) {
                    if (i == 0) {
                        searchQuery = new StringBuilder(APIUtil.getSingleSearchCriteria(searchCriteria[i]));
                    } else {
                        searchQuery.append(APIConstants.SEARCH_AND_TAG)
                                .append(APIUtil.getSingleSearchCriteria(searchCriteria[i]));
                    }
                }
                Map matchedAPIs;
                matchedAPIs = apiConsumer.searchPaginatedAPIs(searchQuery.toString(), tenantDomain, 0,
                        Integer.MAX_VALUE,
                        false);
                Set<API> apiSet = (Set<API>) matchedAPIs.get("apis");
                if (apiSet != null && !apiSet.isEmpty()) {
                    API api = apiSet.iterator().next();
                    APIIdentifier apiId = api.getId();
                    //tier of the imported subscription
                    Tier tier = subscribedAPI.getTier();
                    //checking whether the target tier is available
                    if (isTierAvailable(tier, api) && api.getStatus() != null &&
                            APIConstants.PUBLISHED.equals(api.getStatus())) {
                        apiId.setTier(tier.getName());
                        apiConsumer.addSubscription(apiId, userId, appId);
                    } else {
                        log.error("Failed to import Subscription as API " + name + "-" + version +
                                " as one or more tiers may be unavailable or the API may not have been published ");
                        skippedAPIList.add(subscribedAPI.getApiId());
                    }
                } else {
                    log.error("Failed to import Subscription as API " + name + "-" + version + " is not available");
                    skippedAPIList.add(subscribedAPI.getApiId());
                }
            } else {
                log.error("Failed to import Subscription as Tenant domain: " + tenantDomain + " is not available");
                skippedAPIList.add(subscribedAPI.getApiId());
            }
        }
        return skippedAPIList;
    }

    /**
     * Check whether a target Tier is available to subscribe
     *
     * @param targetTier Target Tier
     * @param api        - {@link org.wso2.carbon.apimgt.api.model.API}
     * @return true, if the target tier is available
     */
    private boolean isTierAvailable(Tier targetTier, API api) {
        APIIdentifier apiId = api.getId();
        Set<Tier> availableTiers = api.getAvailableTiers();
        if (availableTiers.contains(targetTier)) {
            return true;
        } else {
            log.error("Tier:" + targetTier.getName() + " is not available for API " + apiId.getApiName() + "-" +
                    apiId.getVersion());
            return false;
        }
    }
}

