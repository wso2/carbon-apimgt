/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
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
        application = apiConsumer.getApplicationById(appId);
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
     * Import and add subscriptions of a particular application for the available APIs and API products
     *
     * @param appDetails details of the imported application
     * @param userId     username of the subscriber
     * @param appId      application Id
     * @return a list of APIIdentifiers of the skipped subscriptions
     * @throws APIManagementException if an error occurs while importing and adding subscriptions
     */
    public List<APIIdentifier> importSubscriptions(Application appDetails, String userId, int appId, Boolean update)
            throws APIManagementException, UserStoreException {
        List<APIIdentifier> skippedAPIList = new ArrayList<>();
        Set<SubscribedAPI> subscribedAPIs = appDetails.getSubscribedAPIs();
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            APIIdentifier apiIdentifier = subscribedAPI.getApiId();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack
                    (apiIdentifier.getProviderName()));
            if (!StringUtils.isEmpty(tenantDomain) && APIUtil.isTenantAvailable(tenantDomain)) {
                String name = apiIdentifier.getApiName();
                String version = apiIdentifier.getVersion();
                //creating a solr compatible search query, here we will execute a search query without wildcard *s
                StringBuilder searchQuery = new StringBuilder();
                String[] searchCriteria = {name, "version:" + version};
                for (int i = 0; i < searchCriteria.length; i++) {
                    if (i == 0) {
                        searchQuery = new StringBuilder(
                                APIUtil.getSingleSearchCriteria(searchCriteria[i]).replace("*", ""));
                    } else {
                        searchQuery.append(APIConstants.SEARCH_AND_TAG)
                                .append(APIUtil.getSingleSearchCriteria(searchCriteria[i]).replace("*", ""));
                    }
                }
                Map matchedAPIs;
                matchedAPIs = apiConsumer.searchPaginatedAPIs(searchQuery.toString(), tenantDomain, 0,
                        Integer.MAX_VALUE,
                        false);
                Set<Object> apiSet = (Set<Object>) matchedAPIs.get("apis");
                if (apiSet != null && !apiSet.isEmpty()) {
                    Object type = apiSet.iterator().next();
                    ApiTypeWrapper apiTypeWrapper = null;
                    Identifier id = null;
                    //Check whether the object is ApiProduct
                    if (isApiProduct(type)) {
                        APIProduct apiProduct = (APIProduct) apiSet.iterator().next();
                        apiTypeWrapper = new ApiTypeWrapper(apiProduct);
                        id = apiProduct.getId();
                    } else {
                        API api = (API) apiSet.iterator().next();
                        apiTypeWrapper = new ApiTypeWrapper(api);
                        id = api.getId();
                    }
                    //tier of the imported subscription
                    Tier tier = subscribedAPI.getTier();
                    //checking whether the target tier is available
                    if (isTierAvailable(tier, apiTypeWrapper) && apiTypeWrapper.getStatus() != null &&
                            APIConstants.PUBLISHED.equals(apiTypeWrapper.getStatus())) {
                        apiTypeWrapper.setTier(tier.getName());
                        // add subscription if update flag is not specified
                        // it will throw an error if subscriber already exists
                        if (update == null || !update) {
                            apiConsumer.addSubscription(apiTypeWrapper, userId, appId);
                        } else if (!apiConsumer.isSubscribedToApp(id, userId, appId)) {
                            // on update skip subscriptions that already exists
                            apiConsumer.addSubscription(apiTypeWrapper, userId, appId);
                        }
                    } else {
                        log.error("Failed to import Subscription as API/API Product " + name + "-" + version +
                                " as one or more tiers may be unavailable or the API/API Product may not have been published ");
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
     * @param targetTier     Target Tier
     * @param apiTypeWrapper - {@link ApiTypeWrapper}
     * @return true, if the target tier is available
     */
    private boolean isTierAvailable(Tier targetTier, ApiTypeWrapper apiTypeWrapper) {
        Set<Tier> availableTiers = null;
        API api = null;
        APIProduct apiProduct = null;
        if (!apiTypeWrapper.isAPIProduct()) {
            api = apiTypeWrapper.getApi();
            availableTiers = api.getAvailableTiers();
        } else {
            apiProduct = apiTypeWrapper.getApiProduct();
            availableTiers = apiProduct.getAvailableTiers();
        }
        if (availableTiers.contains(targetTier)) {
            return true;
        } else {
            if (!apiTypeWrapper.isAPIProduct()) {
                log.error("Tier:" + targetTier.getName() + " is not available for API " + api.getId().getApiName() + "-" +
                        api.getId().getVersion());
            } else {
                log.error("Tier:" + targetTier.getName() + " is not available for API Product " + apiProduct.getId().getName() + "-" +
                        apiProduct.getId().getVersion());
            }
            return false;
        }
    }

    /**
     * Check whether the object is a type of ApiProduct
     *
     * @param object        - {@link Object}
     * @return true, if the object is an ApiProduct, otherwise false
     */
    private boolean isApiProduct(Object object) {
        //Check whether the object is an instance of ApiProduct
        return (object) instanceof APIProduct;
    }

    /**
     * Adds a key to a given Application
     *
     * @param username    User for import application
     * @param application Application used to add key
     * @param apiKey      API key for adding to application
     * @throws APIManagementException
     */
    public void addApplicationKey(String username, Application application, APIKey apiKey) throws APIManagementException {
        String[] accessAllowDomainsArray = {"ALL"};
        JSONObject jsonParamObj = new JSONObject();
        jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
        String grantTypes = apiKey.getGrantTypes();
        if (!StringUtils.isEmpty(grantTypes)) {
            jsonParamObj.put(APIConstants.JSON_GRANT_TYPES, grantTypes);
        }
        /* Read clientId & clientSecret from ApplicationKeyGenerateRequestDTO object.
           User can provide clientId only or both clientId and clientSecret
           User cannot provide clientSecret only
         */
        if (!StringUtils.isEmpty(apiKey.getConsumerKey())) {
            jsonParamObj.put(APIConstants.JSON_CLIENT_ID, apiKey.getConsumerKey());
            if (!StringUtils.isEmpty(apiKey.getConsumerSecret())) {
                jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, apiKey.getConsumerSecret());
            }
        }
        if (!StringUtils.isEmpty(apiKey.getCallbackUrl())) {
            jsonParamObj.put(APIConstants.JSON_CALLBACK_URL, apiKey.getCallbackUrl());
        }
        if (apiKey.getAdditionalProperties() != null) {
            jsonParamObj.put(APIConstants.JSON_ADDITIONAL_PROPERTIES, apiKey.getAdditionalProperties());
        }
        String jsonParams = jsonParamObj.toString();
        String tokenScopes = apiKey.getTokenScope();
        apiConsumer.requestApprovalForApplicationRegistration(
                username, application.getName(), apiKey.getType(), apiKey.getCallbackUrl(),
                accessAllowDomainsArray, Long.toString(apiKey.getValidityPeriod()), tokenScopes, application.getGroupId(),
                jsonParams, apiKey.getKeyManager(), null, true);
        // TODO: 2020-12-23 get the store domain and pass it as the tenant domain );
    }
}
