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

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil.getAPIIdentifierFromApiIdOrUUID;

/**
 * This class contains REST API Store related utility operations
 */
public class RestAPIStoreUtils {

    private static final Log log = LogFactory.getLog(RestAPIStoreUtils.class);

    /**
     * check whether current logged in user has access to the specified application
     *
     * @param application Application object
     * @return true if current logged in consumer has access to the specified application
     */
    public static boolean isUserAccessAllowedForApplication(Application application) {
        String username = RestApiUtil.getLoggedInUsername();

        if (application != null) {
            //if groupId is null or empty, it is not a shared app 
            if (StringUtils.isEmpty(application.getGroupId())) {
                //if the application is not shared, its subscriber and the current logged in user must be same
                if (application.getSubscriber() != null) {
                    if (application.getSubscriber().getName().equals(username)) {
                        return true;
                    } else if (application.getSubscriber().getName().toLowerCase().equals(username.toLowerCase())) {
                        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                        String comparisonConfig = configuration
                                .getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
                        if (StringUtils.isNotEmpty(comparisonConfig) && Boolean.valueOf(comparisonConfig)) {
                            return true;
                        }
                    }
                }
            } else {
                String userGroupId = RestApiUtil.getLoggedInUserGroupId();
                //if the application is a shared one, application's group id and the user's group id should be same
                if (application.getGroupId().equals(userGroupId)) {
                    return true;
                }
            }
        }

        //user don't have access
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
     * @param apiId API identifier
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPI(String apiId, String tenantDomain) throws APIManagementException {
        String username = RestApiUtil.getLoggedInUsername();
        //this is just to check whether the user has access to the api or the api exists. 
        try {
            APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
        } catch (APIManagementException | UnsupportedEncodingException e) {
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
     * Removes x-mediation-scripts from swagger as they should not be provided to store consumers
     *
     * @param apiSwagger swagger definition of API
     * @return swagger which exclude x-mediation-script elements
     */
    public static String removeXMediationScriptsFromSwagger(String apiSwagger) {
        //removes x-mediation-script key:values
        String mediationScriptRegex = "\"x-mediation-script\":\".*?(?<!\\\\)\"";
        Pattern pattern = Pattern.compile("," + mediationScriptRegex);
        Matcher matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        pattern = Pattern.compile(mediationScriptRegex + ",");
        matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        return apiSwagger;
    }

    public static String getLastUpdatedTimeByApplicationId(String applicationId) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            String lastUpdated = application.getLastUpdatedTime();
            return lastUpdated != null ? lastUpdated : application.getCreatedTime();
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving resource timestamps due to " + e.getMessage(), e);
            }
            RestApiUtil.handleInternalServerError("Error while getting application with id " + applicationId, e, log);
        }
        return null;
    }

    public static String getLastUpdatedTimeBySubscriptionId(String subscriptionId) {
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            SubscribedAPI subscribedAPI = apiConsumer.getSubscriptionByUUID(subscriptionId);
            if (subscribedAPI != null) {
                if (RestAPIStoreUtils.isUserAccessAllowedForSubscription(subscribedAPI)) {
                    String updatedTime = subscribedAPI.getUpdatedTime();
                    return updatedTime != null ? updatedTime : subscribedAPI.getCreatedTime();
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_SUBSCRIPTION, subscriptionId, log);
            }
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving resource timestamps due to " + e.getMessage(), e);
            }
            RestApiUtil.handleInternalServerError("Error while getting subscription with id " + subscriptionId, e, log);
        }
        return null;

    }


    public static String apisApiIdGetLastUpdated(String apiId, String xWSO2Tenant) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            API api;
            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }
            if (RestApiUtil.isUUID(apiId) && apiConsumer != null) {
                api = apiConsumer.getLightweightAPIByUUID(apiId, requestedTenantDomain);
            } else {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                api = apiConsumer != null ? apiConsumer.getAPI(apiIdentifier) : null;
            }
            if (api != null) {
                return api.getLastUpdated() != null ? String.valueOf(api.getLastUpdated().getTime()) : api.getCreatedTime();
            }
        } catch (APIManagementException | UnsupportedEncodingException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public static String apisApiIdSwaggerGetLastUpdated(String xWSO2Tenant, String apiId) {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }
            APIIdentifier apiIdentifier = getAPIIdentifierFromApiIdOrUUID(apiId, requestedTenantDomain);
            Map<String, String> swaggerDefinitionTimeStamps = apiConsumer.getSwaggerDefinitionTimeStamps(apiIdentifier);
            if (swaggerDefinitionTimeStamps != null) {
                String updatedTime = swaggerDefinitionTimeStamps.get("UPDATED_TIME");
                if (updatedTime != null) {
                    return updatedTime;
                } else {  //   the api is not updated yet
                    return swaggerDefinitionTimeStamps.get("CREATED_TIME");
                }
            }
        } catch (APIManagementException | UnsupportedEncodingException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
            log.error("Failed to fetch last updated time for the resource due to " + e.getMessage(), e);
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public static String apisApiIdThumbnailGetLastUpdated(String apiId) {
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            return apiConsumer.getThumbnailLastUpdatedTime(apiIdentifier);
        } catch (APIManagementException | UnsupportedEncodingException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public static String apisApiIdDocumentIdGetLastUpdated(String documentId, String xWSO2Tenant) {
        Documentation documentation;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        String username = RestApiUtil.getLoggedInUsername();
        APIConsumer apiConsumer;
        try {
            apiConsumer = RestApiUtil.getConsumer(username);
            documentation = apiConsumer.getDocumentation(documentId, requestedTenantDomain);
            Date updatedTime = documentation.getLastUpdated();
            return updatedTime == null ? String.valueOf(documentation.getCreatedDate().getTime()) : String.valueOf(updatedTime.getTime());
        } catch (APIManagementException e) {
            String errorMessage = "Error while getting lastUpdated Time for document id";
            if (log.isDebugEnabled()) {
                log.error(errorMessage + e.getMessage(), e);
            }
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

}
