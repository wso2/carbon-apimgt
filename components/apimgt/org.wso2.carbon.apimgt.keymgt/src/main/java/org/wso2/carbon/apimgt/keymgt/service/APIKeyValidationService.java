/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.APIPolicyConditionGroup;
import org.wso2.carbon.apimgt.keymgt.model.entity.ApiPolicy;
import org.wso2.carbon.apimgt.keymgt.model.entity.Condition;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class APIKeyValidationService {
    private static final Log log = LogFactory.getLog(APIKeyValidationService.class);

    /**
     * Validates the access tokens issued for a particular user to access an API.
     *
     * @param context     Requested context
     * @param accessToken Provided access token
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     * @throws APIKeyMgtException Error occurred when accessing the underlying database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken,
                                               String requiredAuthenticationLevel, String clientDomain,
                                               String matchingResource, String httpVerb,String tenantDomain,
                                               List keyManagers)
            throws APIKeyMgtException, APIManagementException {

        TracingSpan validateMainSpan = null;
        TracingSpan getAccessTokenCacheSpan = null;
        TracingSpan fetchingKeyValDTOSpan = null;
        TracingSpan validateTokenSpan = null;
        TracingSpan validateSubscriptionSpan = null;
        TracingSpan validateScopeSpan = null;
        TracingSpan generateJWTSpan = null;
        TracingSpan keyCache = null;
        TracingSpan keyValResponseSpan = null;
        TracingTracer tracer = Util.getGlobalTracer();

        Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_MAIN"));
        Timer.Context timerContext = timer.start();

        MessageContext axis2MessageContext = MessageContext.getCurrentMessageContext();
        if (Util.tracingEnabled() && axis2MessageContext != null) {
            Map map = (Map) axis2MessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            TracingSpan spanContext = Util.extract(tracer, map);
            validateMainSpan = Util.startSpan(TracingConstants.VALIDATE_MAIN, spanContext, tracer);
        }
        Map headersMap = null;
        String activityID = null;
        try {
            if (axis2MessageContext != null) {
                MessageContext responseMessageContext = axis2MessageContext.getOperationContext().
                        getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (responseMessageContext != null) {
                    if (log.isDebugEnabled()) {
                        List headersList = new ArrayList();
                        Object headers = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                        if (headers != null && headers instanceof Map) {
                            headersMap = (Map) headers;
                            activityID = (String) headersMap.get("activityID");
                        }
                        if(headersMap != null) {
                            headersList.add(new Header("activityID", (String) headersMap.get("activityID")));
                        }
                        responseMessageContext.setProperty(HTTPConstants.HTTP_HEADERS, headersList);
                    }
                }
            }
        } catch (AxisFault axisFault) {
            throw new APIKeyMgtException("Error while building response messageContext: " + axisFault.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            String logMsg = "KeyValidation request from gateway: requestTime= "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()) + " , for:"
                    + context + " with accessToken=" + accessToken;
            if (activityID != null) {
                logMsg = logMsg + " , transactionId=" + activityID;
            }
            log.debug(logMsg);
        }

        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setAccessToken(accessToken);
        validationContext.setClientDomain(clientDomain);
        validationContext.setContext(context);
        validationContext.setHttpVerb(httpVerb);
        validationContext.setMatchingResource(matchingResource);
        validationContext.setRequiredAuthenticationLevel(requiredAuthenticationLevel);
        validationContext.setValidationInfoDTO(new APIKeyValidationInfoDTO());
        validationContext.setVersion(version);
        validationContext.setTenantDomain(tenantDomain);
        validationContext.setKeyManagers(keyManagers);

        if (Util.tracingEnabled()) {
            getAccessTokenCacheSpan =
                    Util.startSpan(TracingConstants.GET_ACCESS_TOKEN_CACHE_KEY, validateMainSpan, tracer);
        }
        String cacheKey = APIUtil.getAccessTokenCacheKey(accessToken,
                                                         context, version, matchingResource, httpVerb, requiredAuthenticationLevel);

        validationContext.setCacheKey(cacheKey);
        if (Util.tracingEnabled()) {
            Util.finishSpan(getAccessTokenCacheSpan);
            fetchingKeyValDTOSpan =
                    Util.startSpan(TracingConstants.FETCHING_API_KEY_VAL_INFO_DTO_FROM_CACHE, validateMainSpan, tracer);
        }
        APIKeyValidationInfoDTO infoDTO = APIKeyMgtUtil.getFromKeyManagerCache(cacheKey);
        if (Util.tracingEnabled()) {
            Util.finishSpan(fetchingKeyValDTOSpan);
        }

        if (infoDTO != null) {
            validationContext.setCacheHit(true);
            log.debug("APIKeyValidationInfoDTO fetched from cache. Setting cache hit to true...");
            validationContext.setValidationInfoDTO(infoDTO);
        }

        log.debug("Before calling Validate Token method...");

        Timer timer2 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_TOKEN"));
        Timer.Context timerContext2 = timer2.start();
        if (Util.tracingEnabled()) {
            validateTokenSpan = Util.startSpan(TracingConstants.VALIDATE_TOKEN, validateMainSpan, tracer);
        }
        KeyValidationHandler keyValidationHandler =
                ServiceReferenceHolder.getInstance().getKeyValidationHandler(tenantDomain);
        boolean state = keyValidationHandler.validateToken(validationContext);
        timerContext2.stop();
        if (Util.tracingEnabled()) {
            Util.finishSpan(validateTokenSpan);
        }
        log.debug("State after calling validateToken ... " + state);

        if (state) {
            Timer timer3 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_SUBSCRIPTION"));
            Timer.Context timerContext3 = timer3.start();
            if (Util.tracingEnabled()) {
                validateSubscriptionSpan =
                        Util.startSpan(TracingConstants.VALIDATE_SUBSCRIPTION, validateMainSpan, tracer);
            }
            state = keyValidationHandler.validateSubscription(validationContext);
            timerContext3.stop();
            if (Util.tracingEnabled()) {
                Util.finishSpan(validateSubscriptionSpan);
            }
        }

        log.debug("State after calling validateSubscription... " + state);

        if (state) {
            Timer timer4 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "VALIDATE_SCOPES"));
            Timer.Context timerContext4 = timer4.start();
            if (Util.tracingEnabled()) {
                validateScopeSpan = Util.startSpan(TracingConstants.VALIDATE_SCOPES, validateMainSpan, tracer);
            }
            state = keyValidationHandler.validateScopes(validationContext);
            timerContext4.stop();
            if (Util.tracingEnabled()) {
                Util.finishSpan(validateScopeSpan);
            }
        }

        log.debug("State after calling validateScopes... " + state);

        if (state && APIKeyMgtDataHolder.isJwtGenerationEnabled() &&
                validationContext.getValidationInfoDTO().getEndUserName() != null && !validationContext.isCacheHit()) {
            Timer timer5 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GENERATE_JWT"));
            Timer.Context timerContext5 = timer5.start();
            if (Util.tracingEnabled()) {
                generateJWTSpan = Util.startSpan(TracingConstants.GENERATE_JWT, validateMainSpan, tracer);
            }
            keyValidationHandler.generateConsumerToken(validationContext);
            timerContext5.stop();
            if (Util.tracingEnabled()) {
                Util.finishSpan(generateJWTSpan);
            }
        }
        log.debug("State after calling generateConsumerToken... " + state);

        if (!validationContext.isCacheHit()) {
            if (Util.tracingEnabled()) {
                keyCache = Util.startSpan(TracingConstants.WRITE_TO_KEY_MANAGER_CACHE, validateMainSpan, tracer);
            }
            APIKeyMgtUtil.writeToKeyManagerCache(cacheKey, validationContext.getValidationInfoDTO());
            if (Util.tracingEnabled()) {
                Util.finishSpan(keyCache);
            }
        }

        if (Util.tracingEnabled()) {
            keyValResponseSpan =
                    Util.startSpan(TracingConstants.PUBLISHING_KEY_VALIDATION_RESPONSE, validateMainSpan, tracer);
        }
        if (log.isDebugEnabled() && axis2MessageContext != null) {
            logMessageDetails(axis2MessageContext, validationContext.getValidationInfoDTO());
        }

        if (log.isDebugEnabled()) {
            log.debug("APIKeyValidationInfoDTO before returning : " + validationContext.getValidationInfoDTO());
            log.debug("KeyValidation response from keymanager to gateway for access token:" + accessToken + " at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        if (Util.tracingEnabled()) {
            Util.finishSpan(keyValResponseSpan);
        }
        timerContext.stop();
        if (Util.tracingEnabled() && validateMainSpan != null) {
            Util.finishSpan(validateMainSpan);
        }
        return validationContext.getValidationInfoDTO();
    }

    /**
     * Return the URI Templates for an API
     *
     * @param context Requested context
     * @param version API Version
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     */
    public ArrayList<URITemplate> getAllURITemplates(String context, String version)
            throws APIManagementException {
        Timer timer6 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GET_URI_TEMPLATE"));
        Timer.Context timerContext6 = timer6.start();
        if (log.isDebugEnabled()) {
            log.debug("getAllURITemplates request from gateway to keymanager: requestTime="
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date())
                    + " ,for:" + context);
        }
        ArrayList<URITemplate> templates = getTemplates(context, version);
        if (log.isDebugEnabled()) {
            log.debug("getAllURITemplates response from keyManager to gateway for:" + context + " at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        timerContext6.stop();
        return templates;
    }

    private ArrayList<URITemplate> getTemplates(String context, String version) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(context);
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        ArrayList<URITemplate> templates = new ArrayList<URITemplate>();

        SubscriptionDataStore store = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (store == null) {
            return templates;
        }
        API api = store.getApiByContextAndVersion(context, version);
        if (api == null) {
            log.debug("SubscriptionDataStore didn't contains API metadata reading from rest api context: " + context +
                    " And version " + version);
            api = new SubscriptionDataLoaderImpl().getApi(context, version, tenantDomain);
            if (api != null) {
                store.addOrUpdateAPI(api);
                if (log.isDebugEnabled()) {
                    log.debug("Update SubscriptionDataStore api for " + api.getCacheKey());
                }
            }
        }
        if (api == null || api.getApiId() == 0) {
            return templates;
        }
        List<URLMapping> mapping = api.getResources();
        if (mapping == null || mapping.isEmpty()) {
            return templates;
        }
        int apiTenantId = APIUtil.getTenantId(APIUtil.replaceEmailDomainBack(api.getApiProvider()));
        if (log.isDebugEnabled()) {
            log.debug("Tenant domain: " + tenantDomain + " tenantId: " + apiTenantId);
        }
        ApiPolicy apiPolicy;
        URITemplate template;
        for (URLMapping urlMapping : mapping) {
            template = new URITemplate();
            template.setHTTPVerb(urlMapping.getHttpMethod());
            template.setAuthType(urlMapping.getAuthScheme());
            template.setUriTemplate(urlMapping.getUrlPattern());
            template.setThrottlingTier(urlMapping.getThrottlingPolicy());

            if (store.isApiPoliciesInitialized()) {
                log.debug("SubscriptionDataStore Initialized. Reading API Policies from SubscriptionDataStore");
                apiPolicy = store.getApiPolicyByName(urlMapping.getThrottlingPolicy(), apiTenantId);
                if (apiPolicy == null) {
                    //could be null for situations where invoke before map is updated
                    log.debug("API Policies not found in the SubscriptionDataStore. Retrieving from the Rest API");
                    apiPolicy = new SubscriptionDataLoaderImpl().getAPIPolicy(urlMapping.getThrottlingPolicy(),
                            tenantDomain);
                    if (apiPolicy != null) {
                        if (apiPolicy.getName() != null) {
                            store.addOrUpdateApiPolicy(apiPolicy);
                            if (log.isDebugEnabled()) {
                                log.debug("Update SubscriptionDataStore API Policy for " + apiPolicy.getCacheKey());
                            }
                        } else {
                            throw new APIManagementException("Exception while loading api policy for " +
                                    urlMapping.getThrottlingPolicy() + " for domain " + tenantDomain);
                        }
                    }

                }
            } else {
                log.debug("SubscriptionDataStore not Initialized. Reading API Policies from Rest API");
                apiPolicy = new SubscriptionDataLoaderImpl().getAPIPolicy(urlMapping.getThrottlingPolicy(),
                        tenantDomain);
                if (apiPolicy != null) {
                    if (apiPolicy.getName() != null) {
                        store.addOrUpdateApiPolicy(apiPolicy);
                        if (log.isDebugEnabled()) {
                            log.debug("Update SubscriptionDataStore API Policy for " + apiPolicy.getCacheKey());
                        }
                    } else {
                        throw new APIManagementException("Exception while loading api policy for " +
                                urlMapping.getThrottlingPolicy() + " for domain " + tenantDomain);
                    }
                }
            }

            List<String> tiers = new ArrayList<String>();
            tiers.add(urlMapping.getThrottlingPolicy() + ">" + apiPolicy.isContentAware());
            template.setThrottlingTiers(tiers);
            template.setApplicableLevel(apiPolicy.getApplicableLevel());

            List<APIPolicyConditionGroup> conditions = apiPolicy.getConditionGroups();
            List<ConditionGroupDTO> conditionGroupsList = new ArrayList<ConditionGroupDTO>();
            for (APIPolicyConditionGroup cond : conditions) {
                Set<Condition> condSet = cond.getCondition();
                if (condSet.isEmpty()) {
                    continue;
                }
                List<ConditionDTO> conditionDtoList = new ArrayList<ConditionDTO>();
                for (Condition condition : condSet) {
                    ConditionDTO item = new ConditionDTO();
                    item.setConditionName(condition.getName());
                    item.setConditionType(condition.getConditionType());
                    item.setConditionValue(condition.getValue());
                    item.isInverted(condition.isInverted());
                    conditionDtoList.add(item);
                }
                ConditionGroupDTO group = new ConditionGroupDTO();
                group.setConditionGroupId("_condition_" + cond.getConditionGroupId());
                group.setConditions(conditionDtoList.toArray(new ConditionDTO[]{}));
                conditionGroupsList.add(group);
            }
            ConditionGroupDTO defaultGroup = new ConditionGroupDTO();
            defaultGroup.setConditionGroupId(APIConstants.THROTTLE_POLICY_DEFAULT);
            conditionGroupsList.add(defaultGroup);
            template.getThrottlingConditions().add(APIConstants.THROTTLE_POLICY_DEFAULT);
            template.setConditionGroups(conditionGroupsList.toArray(new ConditionGroupDTO[]{}));

            templates.add(template);
        }
        return templates;
    }

    public ArrayList<URITemplate> getAPIProductURITemplates(String context, String version)
            throws APIManagementException {
        Timer timer6 = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GET_URI_TEMPLATE"));
        Timer.Context timerContext6 = timer6.start();
        if (log.isDebugEnabled()) {
            log.debug("getAllURITemplates request from gateway to keymanager: requestTime="
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date())
                    + " ,for:" + context);
        }
        ArrayList<URITemplate> templates = getTemplates(context, version);
        if (log.isDebugEnabled()) {
            log.debug("getAllURITemplates response from keyManager to gateway for:" + context + " at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        timerContext6.stop();
        return templates;
    }

    private void logMessageDetails(MessageContext messageContext, APIKeyValidationInfoDTO apiKeyValidationInfoDTO) {
        String applicationName = apiKeyValidationInfoDTO.getApplicationName();
        String endUserName = apiKeyValidationInfoDTO.getEndUserName();
        String consumerKey = apiKeyValidationInfoDTO.getConsumerKey();
        Boolean isAuthorize = apiKeyValidationInfoDTO.isAuthorized();
        //Do not change this log format since its using by some external apps
        String logMessage = "";
        if (applicationName != null) {
            logMessage = " , appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " , userName=" + endUserName;
        }
        Map headers = (Map) messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String logID = (String) headers.get("activityID");
        if (logID != null) {
            logMessage = logMessage + " , transactionId=" + logID;
        }
        if (consumerKey != null) {
            logMessage = logMessage + " , consumerKey=" + consumerKey;
        }
        logMessage = logMessage + " , isAuthorized=" + isAuthorize;
        logMessage = logMessage + " , responseTime=" + new Date(System.currentTimeMillis());

        log.debug("OAuth token response from keyManager to gateway: " + logMessage);
    }

    /**
     * validate access token for websocket handshake
     *
     * @param context context of the API
     * @param version version of the API
     * @param accessToken access token of the request
     * @return api information
     * @throws APIKeyMgtException
     * @throws APIManagementException
     */
    public APIKeyValidationInfoDTO validateKeyForHandshake(String context, String version,
                                                           String accessToken, String tenantDomain,
                                                           List<String> keyManagers)
            throws APIKeyMgtException, APIManagementException {
        boolean defaultVersionInvoked = false;
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(false);
        TokenValidationContext validationContext = new TokenValidationContext();
        validationContext.setAccessToken(accessToken);
        validationContext.setContext(context);
        validationContext.setValidationInfoDTO(new APIKeyValidationInfoDTO());
        validationContext.setVersion(version);
        validationContext.setTenantDomain(tenantDomain);
        validationContext.setRequiredAuthenticationLevel("Any");
        validationContext.setKeyManagers(keyManagers);
        KeyValidationHandler keyValidationHandler =
                ServiceReferenceHolder.getInstance().getKeyValidationHandler(tenantDomain);
        boolean state = keyValidationHandler.validateToken(validationContext);
        if (state) {
            state = keyValidationHandler.validateSubscription(validationContext);
            if (state) {
                if (APIConstants.DEFAULT_WEBSOCKET_VERSION.equals(version)) {
                    version = info.getApiVersion();
                    defaultVersionInvoked = true;
                }
                if (defaultVersionInvoked) {
                    validationContext.getValidationInfoDTO().setApiName(info.getApiName() + "*" + version);
                }
                if (APIKeyMgtDataHolder.isJwtGenerationEnabled() &&
                        validationContext.getValidationInfoDTO().getEndUserName() != null
                        && !validationContext.isCacheHit()) {
                    Application application = APIUtil.getApplicationByClientId(validationContext.getValidationInfoDTO()
                            .getConsumerKey());
                    validationContext.getValidationInfoDTO().setApplicationId(String.valueOf(application.getId()));
                    validationContext.getValidationInfoDTO().setApplicationTier(application.getTier());
                    keyValidationHandler.generateConsumerToken(validationContext);
                    info.setEndUserToken(validationContext.getValidationInfoDTO().getEndUserToken());
                }
            }
            return validationContext.getValidationInfoDTO();
        }
        return info;
    }

    /**
     * Validates the subscriptions of a particular API.
     *
     * @param context     Requested context
     * @param version Version of the API
     * @param consumerKey Consumer Key
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     */
    public APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey,
                                                        String tenantDomain,String keyManager)
            throws APIKeyMgtException, APIManagementException {

        KeyValidationHandler keyValidationHandler =
                ServiceReferenceHolder.getInstance().getKeyValidationHandler(tenantDomain);
        return keyValidationHandler.validateSubscription(context, version, consumerKey,keyManager);
    }

    /**
     * Validates the subscriptions of a particular API.
     *
     * @param context     Requested context
     * @param version Version of the API
     * @param appId Application ID
     * @return APIKeyValidationInfoDTO with authorization info and tier info if authorized. If it is not
     * authorized, tier information will be <pre>null</pre>
     */
    public APIKeyValidationInfoDTO validateSubscription(String context, String version, int appId,
                                                        String tenantDomain)
            throws APIKeyMgtException, APIManagementException {

        KeyValidationHandler keyValidationHandler =
                ServiceReferenceHolder.getInstance().getKeyValidationHandler(tenantDomain);
        return keyValidationHandler.validateSubscription(context, version, appId);
    }

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes of the token.
     *
     * @param tokenValidationContext Token validation context
     * @param tenantDomain           Tenant domain
     * @return <code>true</code> if scope validation is successful and
     * <code>false</code> if scope validation failed
     * @throws APIKeyMgtException in case of scope validation failure
     */
    public boolean validateScopes(TokenValidationContext tokenValidationContext, String tenantDomain)
            throws APIKeyMgtException {

        KeyValidationHandler keyValidationHandler =
                ServiceReferenceHolder.getInstance().getKeyValidationHandler(tenantDomain);
        return keyValidationHandler.validateScopes(tokenValidationContext);
    }

    public Map<String, Scope> retrieveScopes(String tenantDomain) {

        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            return new HashMap<>();
        }
        return subscriptionDataStore.getScopesByTenant(tenantDomain);
    }
}
