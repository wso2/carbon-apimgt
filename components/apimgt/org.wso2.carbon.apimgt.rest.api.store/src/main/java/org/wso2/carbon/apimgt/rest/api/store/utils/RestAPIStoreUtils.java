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
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationScopeDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.apimgt.impl.APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE;
import static org.wso2.carbon.apimgt.impl.APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE;
import static org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil.getAPIIdentifierFromApiIdOrUUID;

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

    /**
     * To get the relevant scopes for the application based on the subscribed APIs
     *
     * @param userName          UserName of the user, who is requesting the scopes
     * @param application       Application which the scopes is requested against to
     * @param filterByUserRoles Whether to filter scopes based on user roles.
     * @return relevant scopes .
     */
    public static ScopeListDTO getScopesForApplication(String userName, Application application,
            boolean filterByUserRoles) throws APIManagementException {
        String applicationUUID = application.getUUID();
        String cacheName = filterByUserRoles ? APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE : APP_SUBSCRIPTION_SCOPE_CACHE;
        String cacheKey = filterByUserRoles ? applicationUUID + "-" + userName : applicationUUID;
        Set<Scope> filteredScopes = getValueFromCache(cacheName, cacheKey);
        if (filteredScopes != null) {
            if (log.isDebugEnabled()) {
                log.debug("Scopes for the application " + applicationUUID + " is found in the cache");
            }
            return convertScopeSetToScopeList(filteredScopes);
        }

        if (log.isDebugEnabled()) {
            log.debug("Scopes for the application " + applicationUUID + " is not found in the cache, retrieving it "
                    + "from the database for the user " + userName);
        }
        /* If the relevant scope details for the particular application, and user is not there in cache, get it from
            the regular db call.
         */
        Subscriber subscriber = new Subscriber(userName);
        Set<SubscribedAPI> subscriptions;
        APIConsumer apiConsumer = RestApiUtil.getConsumer(userName);
        subscriptions = apiConsumer.getSubscribedAPIs(subscriber, application.getName(), application.getGroupId());
        Iterator<SubscribedAPI> subscribedAPIIterator = subscriptions.iterator();
        List<APIIdentifier> identifiers = new ArrayList<>();

        while (subscribedAPIIterator.hasNext()) {
            SubscribedAPI subscribedAPI = subscribedAPIIterator.next();
            identifiers.add(subscribedAPI.getApiId());
            if (log.isDebugEnabled()) {
                log.debug(
                        "API " + subscribedAPI.getApiId() + " is subscribed to the the application " + applicationUUID);
            }
        }
        if (!identifiers.isEmpty()) {
            //get scopes for subscribed apis
            Set<Scope> scopeSet = apiConsumer.getScopesBySubscribedAPIs(identifiers);
            if (scopeSet != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Number of un-filtered set of scopes retrieved for the application " + applicationUUID
                            + "is " + scopeSet.size());
                }
            /*
             * Based on the requirement directly send the scope list or filter it based on the role of the customer.
             */
                if (filterByUserRoles) {
                    filteredScopes = getFilteredScopeList(scopeSet, userName);
                } else {
                    filteredScopes = scopeSet;
                }
                addToApplicationScopeCache(cacheName, cacheKey, filteredScopes);
            }
        }
        return convertScopeSetToScopeList(filteredScopes);
    }

    /**
     * Coverts the scope set to ScopeListDTO.
     *
     * @param scopeSet Set of scopes.
     * @return ScopeListDTO.
     */
    protected static ScopeListDTO convertScopeSetToScopeList(Set<Scope> scopeSet) {
        ScopeListDTO scopeListDTO = new ScopeListDTO();
        List<ApplicationScopeDTO> scopeDTOList = new ArrayList<>();
        if (scopeSet == null) {
            return null;
        }
        for (Scope scope : scopeSet) {
            ApplicationScopeDTO scopeDTO = new ApplicationScopeDTO();
            scopeDTO.setKey(scope.getKey());
            scopeDTO.setName(scope.getName());
            scopeDTO.setDescription(scope.getDescription());
            scopeDTO.setRoles(scope.getRoles());
            scopeDTOList.add(scopeDTO);
        }
        scopeListDTO.setList(scopeDTOList);
        return scopeListDTO;
    }

    /**
     * To get the relevant application scope Cache.
     *
     * @param cacheName - Name of the Cache
     * @param key       - Key of the entry that need to be added.
     * @param value     - Value of the entry that need to be added.
     */
    protected static void addToApplicationScopeCache(String cacheName, String key, Set<Scope> value) {
        if (isStoreCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Store cache is enabled, adding the scopes set for the key " + key + " to the cache '" +
                        cacheName + "'");
            }
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName).put(key, value);
        }
    }

    /**
     * To get the value from the cache.
     *
     * @param cacheName Name of the cache.
     * @param key       Key of the cache entry.
     * @return Scope set relevant to the key
     */
    protected static Set<Scope> getValueFromCache(String cacheName, String key) {
        if (isStoreCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Store cache is enabled, retrieving the scopes set for the key " + key + " from the cache "
                        + "'" + cacheName + "'");
            }
            Cache<String, Set<Scope>> appScopeCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache(cacheName);
            return appScopeCache.get(key);
        }
        return null;
    }

    /**
     * To filter the role list for the user.
     *
     * @param scopeSet   Scope set
     * @param userName Name of the user, which the scopes need to filtered against.
     * @return filtered scope set based on user roles.
     */
    protected static Set<Scope> getFilteredScopeList(Set<Scope> scopeSet, String userName)
            throws APIManagementException {
        String[] userRoleArray = APIUtil.getListOfRoles(userName);
        List<String> userRoleList = null;
        Set<Scope> filteredScopes = new LinkedHashSet<>();
        if (log.isDebugEnabled()) {
            log.debug("Roles of the user " + userName + " are " + Arrays.toString(userRoleArray));
        }
        if (userRoleArray != null) {
            userRoleList = Arrays.asList(userRoleArray);
        }
        for (Scope scope : scopeSet) {
            if (scope.getRoles() == null || scope.getRoles().isEmpty()) {
                filteredScopes.add(scope);
            } else if (userRoleList != null && !userRoleList.isEmpty()) {
                List<String> roleList = new ArrayList<>(
                        Arrays.asList(scope.getRoles().replaceAll("\\s+", "").split(",")));
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    filteredScopes.add(scope);
                }
            }
        }
        return filteredScopes;
    }
}
