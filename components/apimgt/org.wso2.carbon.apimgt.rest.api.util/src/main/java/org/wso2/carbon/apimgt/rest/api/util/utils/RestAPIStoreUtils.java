/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains REST API Store related utility operations
 */
public class RestAPIStoreUtils {
    private static final Log log = LogFactory.getLog(RestAPIStoreUtils.class);
    private static boolean isStoreCacheEnabled;

    static {
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isStoreCacheEnabledConfiguration = apiManagerConfiguration
                .getFirstProperty(APIConstants.SCOPE_CACHE_ENABLED);
        isStoreCacheEnabled =
                isStoreCacheEnabledConfiguration != null && Boolean.parseBoolean(isStoreCacheEnabledConfiguration);
    }

    /**
     * check whether current logged in user has access to the specified application
     *
     * @param application Application object
     * @return true if current logged in consumer has access to the specified application
     */
    public static boolean isUserAccessAllowedForApplication(Application application) {
        String groupId;

        if (application != null) {
            groupId = application.getGroupId();
            //If application  subscriber and the current logged in user  same then user can retrieve application
            // irrespective of the groupId
            if (application.getSubscriber() != null && isUserOwnerOfApplication(application)) {
                return true;
            }
            // Check for shared apps
            if (!StringUtils.isEmpty(groupId)) {
                String userGroupId = RestApiUtil.getLoggedInUserGroupId();
                //Check whether there is a common groupId between user and application
                if (userGroupId != null) {
                    List<String> groupIdList = new ArrayList<>(
                            Arrays.asList(groupId.split(APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT)));
                    for (String id : userGroupId.split(APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT)) {
                        if (groupIdList.contains(id)) {
                            return true;
                        }
                    }

                }
            }
        }
        //user don't have access
        return false;
    }

    /**
     * check whether current logged in user is the owner of the application
     *
     * @param application Application object
     * @return true if current logged in consumer is the owner of the specified application
     */
    public static boolean isUserOwnerOfApplication(Application application) {
        String username = RestApiUtil.getLoggedInUsername();

        if (application.getSubscriber().getName().equals(username)) {
            return true;
        } else if (application.getSubscriber().getName().toLowerCase().equals(username.toLowerCase())) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String comparisonConfig = configuration
                    .getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
            return (StringUtils.isNotEmpty(comparisonConfig) && Boolean.valueOf(comparisonConfig));
        }

        return false;
    }

    /**
     * check whether current logged in user has access to the specified subscription
     *
     * @param subscribedAPI SubscribedAPI object
     * @return true if current logged in user has access to the specified subscription
     */
    public static boolean isUserAccessAllowedForSubscription(SubscribedAPI subscribedAPI)
            throws APIManagementException {
        String username = RestApiUtil.getLoggedInUsername();
        Application application = subscribedAPI.getApplication();
        APIIdentifier apiIdentifier = subscribedAPI.getApiId();
        if (apiIdentifier != null && application != null) {
            try {
                if (!isUserAccessAllowedForAPI(apiIdentifier)) {
                    return false;
                }
            } catch (APIManagementException e) {
                String message =
                        "Failed to retrieve the API " + apiIdentifier.toString() + " to check user " + username
                                + " has access to the subscription " + subscribedAPI.getUUID();
                throw new APIManagementException(message, e);
            }
            if (isUserAccessAllowedForApplication(application)) {
                return true;
            }
        }

        //user don't have access
        return false;
    }

    /**
     * Check whether the specified API exists and the current logged in user has access to it.
     * <p>
     * When it tries to retrieve the resource from the registry, it will fail with AuthorizationFailedException if user
     * does not have enough privileges. If the API does not exist, this will throw a APIMgtResourceNotFoundException
     *
     * @param apiId API UUID
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPIByUUID(String apiId, String tenantDomain) throws APIManagementException {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer consumer = RestApiUtil.getLoggedInUserConsumer();
        //this is just to check whether the user has access to the api or the api exists. 
        try {
            consumer.getLightweightAPIByUUID(apiId, tenantDomain);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                String message =
                        "user " + username + " failed to access the API " + apiId + " due to an authorization failure";
                log.info(message);
                return false;
            } else {
                //This is an unexpected failure
                String message =
                        "Failed to retrieve the API " + apiId + " to check user " + username + " has access to the API";
                throw new APIManagementException(message, e);
            }
        }
        return true;
    }

    /**
     * Check whether the specified API exists and the current logged in user has access to it.
     * <p>
     * When it tries to retrieve the resource from the registry, it will fail with AuthorizationFailedException if user
     * does not have enough privileges. If the API does not exist, this will throw a APIMgtResourceNotFoundException
     *
     * @param apiId API identifier
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPI(APIIdentifier apiId) throws APIManagementException {
        String username = RestApiUtil.getLoggedInUsername();
        //this is just to check whether the user has access to the api or the api exists. 
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            apiConsumer.getLightweightAPI(apiId);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                String message =
                        "user " + username + " failed to access the API " + apiId + " due to an authorization failure";
                log.info(message);
                return false;
            } else {
                //This is an unexpected failure
                String message =
                        "Failed to retrieve the API " + apiId + " to check user " + username + " has access to the API";
                throw new APIManagementException(message, e);
            }
        }
        return true;
    }

    /**
     * Check if the specified subscription is allowed for the logged in user
     *
     * @param apiIdentifier API identifier
     * @param tier          the subscribing tier of the API
     * @throws APIManagementException if the subscription allow check was failed. If the user is not allowed to add the
     *                                subscription, this will throw an instance of APIMgtAuthorizationFailedException with the reason as the message
     */
    public static void checkSubscriptionAllowed(APIIdentifier apiIdentifier, String tier)
            throws APIManagementException {

        String username = RestApiUtil.getLoggedInUsername();
        String userTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String providerName = apiIdentifier.getProviderName();
        String apiTenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));

        APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
        API api = apiConsumer.getAPI(apiIdentifier);

        String apiSecurity = api.getApiSecurity();
        if (apiSecurity != null && !apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            String msg = "Subscription is not allowed for API " + apiIdentifier.toString() + ". To access the API, "
                    + "please use the client certificate";
            throw new APIMgtAuthorizationFailedException(msg);
        }
        Set<Tier> tiers = api.getAvailableTiers();

        //Tenant based validation for subscription
        boolean subscriptionAllowed = false;
        if (!userTenantDomain.equals(apiTenantDomain)) {
            String subscriptionAvailability = api.getSubscriptionAvailability();
            if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                subscriptionAllowed = true;
            } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                String subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
                String allowedTenants[];
                if (subscriptionAllowedTenants != null) {
                    allowedTenants = subscriptionAllowedTenants.split(",");
                    for (String tenant : allowedTenants) {
                        if (tenant != null && userTenantDomain.equals(tenant.trim())) {
                            subscriptionAllowed = true;
                            break;
                        }
                    }
                }
            }
        } else {
            subscriptionAllowed = true;
        }
        if (!subscriptionAllowed) {
            throw new APIMgtAuthorizationFailedException("Subscription is not allowed for " + userTenantDomain);
        }

        //check whether the specified tier is within the allowed tiers for the API
        Iterator<Tier> iterator = tiers.iterator();
        boolean isTierAllowed = false;
        List<String> allowedTierList = new ArrayList<>();
        while (iterator.hasNext()) {
            Tier t = iterator.next();
            if (t.getName() != null && (t.getName()).equals(tier)) {
                isTierAllowed = true;
            }
            allowedTierList.add(t.getName());
        }
        if (!isTierAllowed) {
            String msg = "Tier " + tier + " is not allowed for API " + apiIdentifier.getApiName() + "-" + apiIdentifier
                    .getVersion() + ". Only " + Arrays.toString(allowedTierList.toArray()) + " Tiers are allowed.";
            throw new APIMgtAuthorizationFailedException(msg);
        }
        if (apiConsumer.isTierDeneid(tier)) {
            throw new APIMgtAuthorizationFailedException("Tier " + tier + " is not allowed for user " + username);
        }
    }

    /**
     * Retrieves the API Identifier object from given API UUID and tenant domain
     *
     * @param apiId API Identifier UUID
     * @param requestedTenantDomain tenant which API resides
     * @return API Identifier object 
     * @throws APIManagementException if the retrieval fails
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
        API api = apiConsumer.getLightweightAPIByUUID(apiId, requestedTenantDomain);
        return  api.getId();
    }
}
