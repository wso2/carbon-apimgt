/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationKeyMapping;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApplicationPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.keymgt.model.entity.SubscriptionPolicy;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.token.TokenGenerator;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKeyValidationHandler implements KeyValidationHandler {

    private static final Log log = LogFactory.getLog(AbstractKeyValidationHandler.class);

    @Override
    public boolean validateSubscription(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext == null || validationContext.getValidationInfoDTO() == null) {
            return false;
        }

        if (validationContext.isCacheHit()) {
            return true;
        }

        APIKeyValidationInfoDTO dto = validationContext.getValidationInfoDTO();


        if (validationContext.getTokenInfo() != null) {
            if (validationContext.getTokenInfo().isApplicationToken()) {
                dto.setUserType(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION);
            } else {
                dto.setUserType(APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN);
            }

            AccessTokenInfo tokenInfo = validationContext.getTokenInfo();

            // This block checks if a Token of Application Type is trying to access a resource protected with
            // Application Token
            if (!hasTokenRequiredAuthLevel(validationContext.getRequiredAuthenticationLevel(), tokenInfo)) {
                dto.setAuthorized(false);
                dto.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE);
                return false;
            }
        }

        boolean state = false;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Before validating subscriptions : " + dto);
                log.debug("Validation Info : { context : " + validationContext.getContext() + " , " + "version : "
                        + validationContext.getVersion() + " , consumerKey : " + dto.getConsumerKey() + " }");
            }

            state = validateSubscriptionDetails(validationContext.getContext(), validationContext.getVersion(),
                    dto.getConsumerKey(), dto.getKeyManager(), dto);

            if (log.isDebugEnabled()) {
                log.debug("After validating subscriptions : " + dto);
            }


        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }

        return state;
    }

    /**
     * Determines whether the provided token is an ApplicationToken.
     *
     * @param tokenInfo - Access Token Information
     */
    protected void setTokenType(AccessTokenInfo tokenInfo) {


    }

    /**
     * Resources protected with Application token type can only be accessed using Application Access Tokens. This method
     * verifies if a particular resource can be accessed using the obtained token.
     *
     * @param authScheme Type of token required by the resource (Application | User Token)
     * @param tokenInfo  Details about the Token
     * @return {@code true} if token is of the type required, {@code false} otherwise.
     */
    protected boolean hasTokenRequiredAuthLevel(String authScheme,
                                                AccessTokenInfo tokenInfo) {

        if (authScheme == null || authScheme.isEmpty() || tokenInfo == null) {
            return false;
        }

        if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authScheme)) {
            return tokenInfo.isApplicationToken();
        } else if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authScheme)) {
            return !tokenInfo.isApplicationToken();
        }

        return true;

    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext validationContext) throws APIKeyMgtException {

      TokenGenerator generator = APIKeyMgtDataHolder.getTokenGenerator();

        try {
            String jwt = generator.generateToken(validationContext);
            validationContext.getValidationInfoDTO().setEndUserToken(jwt);
            return true;

        } catch (APIManagementException e) {
            log.error("Error occurred while generating JWT. ", e);
        }

        return false;
    }

    @Override
    public APIKeyValidationInfoDTO validateSubscription(String apiContext, String apiVersion, String consumerKey,
                                                        String keyManager) {
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO =  new APIKeyValidationInfoDTO();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Before validating subscriptions");
                log.debug("Validation Info : { context : " + apiContext + " , " + "version : "
                        + apiVersion + " , consumerKey : " + consumerKey + " }");
            }
            validateSubscriptionDetails(apiContext, apiVersion, consumerKey, keyManager, apiKeyValidationInfoDTO);
            if (log.isDebugEnabled()) {
                log.debug("After validating subscriptions");
            }
        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }
        return apiKeyValidationInfoDTO;
    }

    @Override
    public APIKeyValidationInfoDTO validateSubscription(String apiContext, String apiVersion, int appId) {
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO =  new APIKeyValidationInfoDTO();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Before validating subscriptions");
                log.debug("Validation Info : { context : " + apiContext + " , " + "version : "
                        + apiVersion + " , appId : " + appId + " }");
            }
            validateSubscriptionDetails(apiContext, apiVersion, appId, apiKeyValidationInfoDTO);
            if (log.isDebugEnabled()) {
                log.debug("After validating subscriptions");
            }
        } catch (APIManagementException e) {
            log.error("Error Occurred while validating subscription.", e);
        }
        return apiKeyValidationInfoDTO;
    }

    private boolean validateSubscriptionDetails(String context, String version, String consumerKey, String keyManager,
                                                APIKeyValidationInfoDTO infoDTO) throws APIManagementException {
        boolean defaultVersionInvoked = false;
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        int apiOwnerTenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        // Check if the api version has been prefixed with _default_
        if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            defaultVersionInvoked = true;
            // Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }

        validateSubscriptionDetails(infoDTO, context, version, consumerKey, keyManager, defaultVersionInvoked);
        return infoDTO.isAuthorized();
    }

    private boolean validateSubscriptionDetails(String context, String version, int appId,
                                                APIKeyValidationInfoDTO infoDTO) throws APIManagementException {
        boolean defaultVersionInvoked = false;
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        int apiOwnerTenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        // Check if the api version has been prefixed with _default_
        if (version != null && version.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            defaultVersionInvoked = true;
            // Remove the prefix from the version.
            version = version.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }

        validateSubscriptionDetails(infoDTO, context, version, appId, defaultVersionInvoked);
        return infoDTO.isAuthorized();
    }
    
    private APIKeyValidationInfoDTO validateSubscriptionDetails(APIKeyValidationInfoDTO infoDTO, String context,
            String version, String consumerKey, String keyManager, boolean defaultVersionInvoked) {
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        API api = null;
        ApplicationKeyMapping key = null;
        Application app = null;
        Subscription sub = null;
        
        SubscriptionDataStore datastore = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(apiTenantDomain);
        //TODO add a check to see whether datastore is initialized an load data using rest api if it is not loaded
        if (datastore != null) {
            api = datastore.getApiByContextAndVersion(context, version);
            if (api == null && APIConstants.DEFAULT_WEBSOCKET_VERSION.equals(version)) {
                // for websocket default version.
                api = datastore.getDefaultApiByContext(context);
            }
            if (api != null) {
                key = datastore.getKeyMappingByKeyAndKeyManager(consumerKey, keyManager);
                if (key != null) {
                    app = datastore.getApplicationById(key.getApplicationId());
                    if (app != null) {
                        sub = datastore.getSubscriptionById(app.getId(), api.getApiId());
                        if (sub != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("All information is retrieved from the inmemory data store.");
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Valid subscription not found for appId " + app.getId() + " and apiId "
                                        + api.getApiId());
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Application not found in the datastore for id " + key.getApplicationId());
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Application keymapping not found in the datastore for id consumerKey " + consumerKey);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("API not found in the datastore for " + context + ":" + version);
                }
            }
        } else {
            log.error("Subscription datastore is not initialized for tenant domain " + apiTenantDomain);
        }
        
        if (api != null && app != null && key != null && sub != null) {
            validate(infoDTO, apiTenantDomain, tenantId, datastore, api, key, app, sub, keyManager);
        } else if (!infoDTO.isAuthorized() && infoDTO.getValidationStatus() == 0) {
            //Scenario where validation failed and message is not set
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        } else {
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        }

        return infoDTO;
    }

    private APIKeyValidationInfoDTO validateSubscriptionDetails(APIKeyValidationInfoDTO infoDTO, String context,
                                                                String version, int appId, boolean defaultVersionInvoked) {
        String apiTenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (apiTenantDomain == null) {
            apiTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        int tenantId = APIUtil.getTenantIdFromTenantDomain(apiTenantDomain);
        API api = null;
        Subscription sub = null;
        Application app = null;
        SubscriptionDataStore datastore = SubscriptionDataHolder.getInstance()
                .getTenantSubscriptionStore(apiTenantDomain);
        //TODO add a check to see whether datastore is initialized an load data using rest api if it is not loaded
        if (datastore != null) {
            app = datastore.getApplicationById(appId);
            api = datastore.getApiByContextAndVersion(context, version);
            if (api == null && APIConstants.DEFAULT_WEBSOCKET_VERSION.equals(version)) {
                // for websocket default version.
                api = datastore.getDefaultApiByContext(context);
            }
            if (api != null) {
                sub = datastore.getSubscriptionById(appId, api.getApiId());
                if (sub != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("All information is retrieved from the inmemory data store.");
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Valid subscription not found for appId " + appId + " and apiId "
                                + api.getApiId());
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("API not found in the datastore for " + context + ":" + version);
                }
            }
        } else {
            log.error("Subscription datastore is not initialized for tenant domain " + apiTenantDomain);
        }

        if (api != null && sub != null) {
            validate(infoDTO, apiTenantDomain, tenantId, datastore, api, app, sub);
        } else if (!infoDTO.isAuthorized() && infoDTO.getValidationStatus() == 0) {
            //Scenario where validation failed and message is not set
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        } else {
            infoDTO.setAuthorized(false);
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        }

        return infoDTO;
    }

    private APIKeyValidationInfoDTO validate(APIKeyValidationInfoDTO infoDTO, String apiTenantDomain, int tenantId,
            SubscriptionDataStore datastore, API api, ApplicationKeyMapping key, Application app, Subscription sub,
            String keyManager) {
        String subscriptionStatus = sub.getSubscriptionState();
        String type = key.getKeyType();
        if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            infoDTO.setAuthorized(false);
            return infoDTO;
        } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus)
                || APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
            infoDTO.setAuthorized(false);
            return infoDTO;
        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus)
                && !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            infoDTO.setType(type);
            infoDTO.setAuthorized(false);
            return infoDTO;
        }
        infoDTO.setTier(sub.getPolicyId());
        infoDTO.setSubscriber(app.getSubName());
        infoDTO.setApplicationId(app.getId().toString());
        infoDTO.setApiName(api.getApiName());
        infoDTO.setApiVersion(api.getApiVersion());
        infoDTO.setApiPublisher(api.getApiProvider());
        infoDTO.setApplicationName(app.getName());
        infoDTO.setApplicationTier(app.getPolicy());
        infoDTO.setApplicationUUID(app.getUUID());
        infoDTO.setAppAttributes(app.getAttributes());
        infoDTO.setType(type);

        // Advanced Level Throttling Related Properties
        String apiTier = api.getApiTier();
        String subscriberUserId = sub.getSubscriptionId();
        String subscriberTenant = MultitenantUtils.getTenantDomain(app.getSubName());

        ApplicationPolicy appPolicy = datastore.getApplicationPolicyByName(app.getPolicy(),
                tenantId);
        if (appPolicy == null) {
            try {
                appPolicy = new SubscriptionDataLoaderImpl()
                        .getApplicationPolicy(app.getPolicy(), apiTenantDomain);
                datastore.addOrUpdateApplicationPolicy(appPolicy);
            } catch (DataLoadingException e) {
                log.error("Error while loading ApplicationPolicy");
            }
        }
        SubscriptionPolicy subPolicy = datastore.getSubscriptionPolicyByName(sub.getPolicyId(),
                tenantId);
        if (subPolicy == null) {
            try {
                subPolicy = new SubscriptionDataLoaderImpl()
                        .getSubscriptionPolicy(sub.getPolicyId(), apiTenantDomain);
                datastore.addOrUpdateSubscriptionPolicy(subPolicy);
            } catch (DataLoadingException e) {
                log.error("Error while loading SubscriptionPolicy");
            }
        }
        ApiPolicy apiPolicy = datastore.getApiPolicyByName(api.getApiTier(), tenantId);

        boolean isContentAware = false;
        if (appPolicy.isContentAware() || subPolicy.isContentAware()
                || (apiPolicy != null && apiPolicy.isContentAware())) {
            isContentAware = true;
        }
        infoDTO.setContentAware(isContentAware);

        // TODO this must implement as a part of throttling implementation.
        int spikeArrest = 0;
        String apiLevelThrottlingKey = "api_level_throttling_key";

        if (subPolicy.getRateLimitCount() > 0) {
            spikeArrest = subPolicy.getRateLimitCount();
        }

        String spikeArrestUnit = null;

        if (subPolicy.getRateLimitTimeUnit() != null) {
            spikeArrestUnit = subPolicy.getRateLimitTimeUnit();
        }
        boolean stopOnQuotaReach = subPolicy.isStopOnQuotaReach();
        int graphQLMaxDepth = 0;
        if (subPolicy.getGraphQLMaxDepth() > 0) {
            graphQLMaxDepth = subPolicy.getGraphQLMaxDepth();
        }
        int graphQLMaxComplexity = 0;
        if (subPolicy.getGraphQLMaxComplexity() > 0) {
            graphQLMaxComplexity = subPolicy.getGraphQLMaxComplexity();
        }
        List<String> list = new ArrayList<String>();
        list.add(apiLevelThrottlingKey);
        infoDTO.setSpikeArrestLimit(spikeArrest);
        infoDTO.setSpikeArrestUnit(spikeArrestUnit);
        infoDTO.setStopOnQuotaReach(stopOnQuotaReach);
        infoDTO.setSubscriberTenantDomain(subscriberTenant);
        infoDTO.setGraphQLMaxDepth(graphQLMaxDepth);
        infoDTO.setGraphQLMaxComplexity(graphQLMaxComplexity);
        if (apiTier != null && apiTier.trim().length() > 0) {
            infoDTO.setApiTier(apiTier);
        }
        // We also need to set throttling data list associated with given API. This need to have
        // policy id and
        // condition id list for all throttling tiers associated with this API.
        infoDTO.setThrottlingDataList(list);
        infoDTO.setAuthorized(true);
        return infoDTO;
    }

    private APIKeyValidationInfoDTO validate(APIKeyValidationInfoDTO infoDTO, String apiTenantDomain, int tenantId,
                                             SubscriptionDataStore datastore, API api, Application app, Subscription sub) {
        String subscriptionStatus = sub.getSubscriptionState();
        String type = app.getTokenType();
        if (APIConstants.SubscriptionStatus.BLOCKED.equals(subscriptionStatus)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            infoDTO.setAuthorized(false);
            return infoDTO;
        } else if (APIConstants.SubscriptionStatus.ON_HOLD.equals(subscriptionStatus)
                || APIConstants.SubscriptionStatus.REJECTED.equals(subscriptionStatus)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.SUBSCRIPTION_INACTIVE);
            infoDTO.setAuthorized(false);
            return infoDTO;
        } else if (APIConstants.SubscriptionStatus.PROD_ONLY_BLOCKED.equals(subscriptionStatus)
                && !APIConstants.API_KEY_TYPE_SANDBOX.equals(type)) {
            infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_BLOCKED);
            infoDTO.setType(type);
            infoDTO.setAuthorized(false);
            return infoDTO;
        }
        infoDTO.setTier(sub.getPolicyId());
        infoDTO.setSubscriber(app.getSubName());
        infoDTO.setApplicationId(app.getId().toString());
        infoDTO.setApiName(api.getApiName());
        infoDTO.setApiVersion(api.getApiVersion());
        infoDTO.setApiPublisher(api.getApiProvider());
        infoDTO.setApplicationName(app.getName());
        infoDTO.setApplicationTier(app.getPolicy());
        infoDTO.setApplicationUUID(app.getUUID());
        infoDTO.setAppAttributes(app.getAttributes());
        infoDTO.setType(type);

        // Advanced Level Throttling Related Properties
        String apiTier = api.getApiTier();
        String subscriberUserId = sub.getSubscriptionId();
        String subscriberTenant = MultitenantUtils.getTenantDomain(app.getSubName());

        ApplicationPolicy appPolicy = datastore.getApplicationPolicyByName(app.getPolicy(),
                tenantId);
        if (appPolicy == null) {
            try {
                appPolicy = new SubscriptionDataLoaderImpl()
                        .getApplicationPolicy(app.getPolicy(), apiTenantDomain);
                datastore.addOrUpdateApplicationPolicy(appPolicy);
            } catch (DataLoadingException e) {
                log.error("Error while loading ApplicationPolicy");
            }
        }
        SubscriptionPolicy subPolicy = datastore.getSubscriptionPolicyByName(sub.getPolicyId(),
                tenantId);
        if (subPolicy == null) {
            try {
                subPolicy = new SubscriptionDataLoaderImpl()
                        .getSubscriptionPolicy(sub.getPolicyId(), apiTenantDomain);
                datastore.addOrUpdateSubscriptionPolicy(subPolicy);
            } catch (DataLoadingException e) {
                log.error("Error while loading SubscriptionPolicy");
            }
        }
        ApiPolicy apiPolicy = datastore.getApiPolicyByName(api.getApiTier(), tenantId);

        boolean isContentAware = false;
        if (appPolicy.isContentAware() || subPolicy.isContentAware()
                || (apiPolicy != null && apiPolicy.isContentAware())) {
            isContentAware = true;
        }
        infoDTO.setContentAware(isContentAware);

        // TODO this must implement as a part of throttling implementation.
        int spikeArrest = 0;
        String apiLevelThrottlingKey = "api_level_throttling_key";

        if (subPolicy.getRateLimitCount() > 0) {
            spikeArrest = subPolicy.getRateLimitCount();
        }

        String spikeArrestUnit = null;

        if (subPolicy.getRateLimitTimeUnit() != null) {
            spikeArrestUnit = subPolicy.getRateLimitTimeUnit();
        }
        boolean stopOnQuotaReach = subPolicy.isStopOnQuotaReach();
        int graphQLMaxDepth = 0;
        if (subPolicy.getGraphQLMaxDepth() > 0) {
            graphQLMaxDepth = subPolicy.getGraphQLMaxDepth();
        }
        int graphQLMaxComplexity = 0;
        if (subPolicy.getGraphQLMaxComplexity() > 0) {
            graphQLMaxComplexity = subPolicy.getGraphQLMaxComplexity();
        }
        List<String> list = new ArrayList<String>();
        list.add(apiLevelThrottlingKey);
        infoDTO.setSpikeArrestLimit(spikeArrest);
        infoDTO.setSpikeArrestUnit(spikeArrestUnit);
        infoDTO.setStopOnQuotaReach(stopOnQuotaReach);
        infoDTO.setSubscriberTenantDomain(subscriberTenant);
        infoDTO.setGraphQLMaxDepth(graphQLMaxDepth);
        infoDTO.setGraphQLMaxComplexity(graphQLMaxComplexity);
        if (apiTier != null && apiTier.trim().length() > 0) {
            infoDTO.setApiTier(apiTier);
        }
        // We also need to set throttling data list associated with given API. This need to have
        // policy id and
        // condition id list for all throttling tiers associated with this API.
        infoDTO.setThrottlingDataList(list);
        infoDTO.setAuthorized(true);
        return infoDTO;
    }
}
