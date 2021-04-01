/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.EventCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;
import java.util.Map;

public class SynapseAnalyticsDataProvider implements AnalyticsDataProvider {

    private static final Log log = LogFactory.getLog(SynapseAnalyticsDataProvider.class);
    private MessageContext messageContext;

    public SynapseAnalyticsDataProvider(MessageContext messageContext) {

        this.messageContext = messageContext;
    }

    public static String sortGraphQLOperations(String apiResourceTemplates) {

        if (apiResourceTemplates == null || !apiResourceTemplates.contains(",")) {
            return apiResourceTemplates;
        }
        String[] list = apiResourceTemplates.split(",");
        // sorting alphabetical order
        Arrays.sort(list);
        return String.join(",", list);
    }

    @Override
    public EventCategory getEventCategory() {

        if (isSuccessRequest()) {
            return EventCategory.SUCCESS;
        } else if (isFaultRequest()) {
            return EventCategory.FAULT;
        } else {
            return EventCategory.INVALID;
        }
    }

    @Override
    public boolean isAnonymous() {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        return isAuthenticated() && APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername());
    }

    @Override
    public boolean isAuthenticated() {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        return authContext != null && authContext.isAuthenticated();
    }

    @Override
    public FaultCategory getFaultType() {

        if (isAuthFaultRequest()) {
            return FaultCategory.AUTH;
        } else if (isThrottledFaultRequest()) {
            return FaultCategory.THROTTLED;
        } else if (isTargetFaultRequest()) {
            return FaultCategory.TARGET_CONNECTIVITY;
        } else {
            return FaultCategory.OTHER;
        }
    }

    @Override
    public API getApi() throws DataNotFoundException {

        String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        org.wso2.carbon.apimgt.keymgt.model.entity.API apiObj = store.getApiByContextAndVersion(apiContext, apiVersion);
        API api = new API();
        if (apiObj == null) {
            try {
                apiObj = new SubscriptionDataLoaderImpl().getApi(apiContext, apiVersion);
            } catch (DataLoadingException e) {
                log.error("Error occurred when getting api.", e);
                throw new DataNotFoundException("Error occurred when getting API information", e);
            }
        }

        if (apiObj != null) {
            api.setApiId(apiObj.getUuid());
            api.setApiType(apiObj.getApiType());
            api.setApiName(apiObj.getApiName());
            api.setApiVersion(apiObj.getApiVersion());
            api.setApiCreator(apiObj.getApiProvider());
            api.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(api.getApiCreator()));
        }
        return api;
    }

    @Override
    public Application getApplication() throws DataNotFoundException {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext == null) {
            throw new DataNotFoundException("Error occurred when getting Application information");
        }
        Application application = new Application();
        application.setApplicationId(authContext.getApplicationUUID());
        application.setApplicationName(authContext.getApplicationName());
        application.setApplicationOwner(authContext.getSubscriber());
        application.setKeyType(authContext.getKeyType());
        return application;
    }

    @Override
    public Operation getOperation() throws DataNotFoundException {

        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        Operation operation = new Operation();
        operation.setApiMethod(httpMethod);
        if (APIConstants.GRAPHQL_API.equalsIgnoreCase(getApi().getApiType())) {
            String orderedOperations = sortGraphQLOperations(apiResourceTemplate);
            operation.setApiResourceTemplate(orderedOperations);
        } else {
            operation.setApiResourceTemplate(apiResourceTemplate);
        }
        return operation;
    }

    @Override
    public Target getTarget() {

        Target target = new Target();

        String endpointAddress = (String) messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        int targetResponseCode = getTargetResponseCode();
        target.setResponseCacheHit(isCacheHit());
        target.setDestination(endpointAddress);
        target.setTargetResponseCode(targetResponseCode);
        return target;
    }

    @Override
    public Latencies getLatencies() {

        long backendLatency = getBackendLatency();
        long responseLatency = getResponseLatency();
        long requestMediationLatency = getRequestMediationLatency();
        long responseMediationLatency = getResponseMediationLatency();

        Latencies latencies = new Latencies();
        latencies.setResponseLatency(responseLatency);
        latencies.setBackendLatency(backendLatency);
        latencies.setRequestMediationLatency(requestMediationLatency);
        latencies.setResponseMediationLatency(responseMediationLatency);
        return latencies;
    }

    private boolean isCacheHit() {

        Object cacheHitObject = messageContext.getProperty(Constants.REQUEST_CACHE_HIT);
        boolean isCacheHit = false;
        if (cacheHitObject instanceof String) {
            isCacheHit = Boolean.parseBoolean((String) cacheHitObject);
        } else if (cacheHitObject instanceof Boolean) {
            isCacheHit = (boolean) cacheHitObject;
        }
        return isCacheHit;
    }

    @Override
    public MetaInfo getMetaInfo() {

        MetaInfo metaInfo = new MetaInfo();
        Object correlationId = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(CorrelationConstants.CORRELATION_ID);
        if (correlationId instanceof String) {
            metaInfo.setCorrelationId((String) correlationId);
        }
        metaInfo.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        Map<String, String> configMap = ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIAnalyticsConfiguration().getReporterProperties();
        String region;
        if (System.getProperties().containsKey(Constants.REGION_ID_PROP)) {
            region = System.getProperty(Constants.REGION_ID_PROP);
        } else if (configMap != null && configMap.containsKey(Constants.REGION_ID_PROP)) {
            region = configMap.get(Constants.REGION_ID_PROP);
        } else {
            region = Constants.DEFAULT_REGION_ID;
        }
        metaInfo.setRegionId(region);
        return metaInfo;
    }

    @Override
    public int getProxyResponseCode() {

        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }
        return proxyResponseCode;
    }

    @Override
    public int getTargetResponseCode() {

        Object responseCodeObject = messageContext.getProperty(Constants.BACKEND_RESPONSE_CODE);
        if (responseCodeObject != null) {
            return (int) responseCodeObject;
        }
        Object responseCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        return (int) responseCode;
    }

    @Override
    public long getRequestTime() {

        return (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
    }

    @Override
    public Error getError(FaultCategory faultCategory) {

        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        FaultCodeClassifier faultCodeClassifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory faultSubCategory = faultCodeClassifier.getFaultSubCategory(faultCategory, errorCode);
        Error error = new Error();
        error.setErrorCode(errorCode);
        error.setErrorMessage(faultSubCategory);
        return error;
    }

    @Override
    public String getUserAgentHeader() {

        return (String) messageContext.getProperty(Constants.USER_AGENT_PROPERTY);
    }

    @Override
    public String getEndUserIP() {

        if (messageContext.getPropertyKeySet().contains(Constants.USER_IP_PROPERTY)) {
            return (String) messageContext.getProperty(Constants.USER_IP_PROPERTY);
        }
        return null;
    }

    private boolean isSuccessRequest() {

        return !messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)
                && APISecurityUtils.getAuthenticationContext(messageContext) != null;
    }

    private boolean isFaultRequest() {

        return messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE);
    }

    private boolean isAuthFaultRequest() {

        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.AUTH_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.AUTH_FAILURE__END;
    }

    private boolean isThrottledFaultRequest() {

        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE__END;
    }

    private boolean isTargetFaultRequest() {

        int errorCode = getErrorCode();
        return (errorCode >= Constants.ERROR_CODE_RANGES.TARGET_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.TARGET_FAILURE__END)
                || errorCode == Constants.ENDPOINT_SUSPENDED_ERROR_CODE;
    }

    private int getErrorCode() {

        return (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
    }

    public long getBackendLatency() {

        if (isCacheHit()) {
            return 0L;
        }
        long backendStartTime = (long) messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        long backendEndTime = (long) messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        return backendEndTime - backendStartTime;
    }

    public long getResponseLatency() {

        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        return System.currentTimeMillis() - requestInTime;
    }

    public long getRequestMediationLatency() {

        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        if (isCacheHit()) {
            return System.currentTimeMillis() - requestInTime;
        }
        long backendStartTime = (long) messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        return backendStartTime - requestInTime;
    }

    public long getResponseMediationLatency() {

        if (isCacheHit()) {
            return 0;
        }
        long backendEndTime = (long) messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        return System.currentTimeMillis() - backendEndTime;
    }

}
