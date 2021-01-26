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
import org.apache.synapse.AbstractExtendedSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.dto.ResponseEvent;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Global synapse handler to publish analytics data to analytics cloud.
 */
public class AnalyticsMetricsHandler extends AbstractExtendedSynapseHandler {
    private static final Log log = LogFactory.getLog(AnalyticsMetricsHandler.class);
    private static final String REQUEST_START_TIME_PROPERTY = "apim.analytics.request.start.time";
    private static final String BACKEND_START_TIME_PROPERTY = "apim.analytics.backend.start.time";
    private static final String BACKEND_LATENCY_PROPERTY = "api.analytics.backend.latency";
    private static final String BACKEND_RESPONSE_CODE = "api.analytics.backend.response_code";
    private static final String USER_AGENT_PROPERTY = "api.analytics.user.agent";
    private static final String CACHED_RESPONSE_KEY = "CachableResponse";

    private static final String REGION_ID = "asia";
    private static final String DEPLOYMENT_ID = "prod";
    private static final String SUCCESS_EVENT_TYPE = "response";
    private static final String METRIC_NAME = "apim.response";
    private static final String UNKNOWN_VALUE = "UNKNOWN";

    @Override
    public boolean handleError(MessageContext messageContext) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        messageContext.setProperty(REQUEST_START_TIME_PROPERTY, System.currentTimeMillis());
        //Set user agent in request flow
        String userAgent = getUserAgent(messageContext);
        messageContext.setProperty(USER_AGENT_PROPERTY, userAgent);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        messageContext.setProperty(BACKEND_START_TIME_PROPERTY, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        long backendStartTime = (long) messageContext.getProperty(BACKEND_START_TIME_PROPERTY);
        messageContext.setProperty(BACKEND_LATENCY_PROPERTY, (System.currentTimeMillis() - backendStartTime));
        Object responseCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        messageContext.setProperty(BACKEND_RESPONSE_CODE, responseCode);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        if (!isSuccessRequest(messageContext)) {
            log.warn("Not support un-managed APIs and requests not flowing in default path.");
            return true;
        }

        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        String apiName = (String) messageContext.getProperty(APIMgtGatewayConstants.API);
        String apiUuid = (String) messageContext.getProperty(APIMgtGatewayConstants.API_UUID_PROPERTY);
        String apiCreator = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        boolean isCacheHit = messageContext.getPropertyKeySet().contains(CACHED_RESPONSE_KEY);

        long requestInTime = (long) messageContext.getProperty(REQUEST_START_TIME_PROPERTY);
        long responseTime = System.currentTimeMillis() - requestInTime;
        long backendLatency = (Long) messageContext.getProperty(BACKEND_LATENCY_PROPERTY);

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext != null) {
            if (APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername())) {
                authContext.setApplicationName(UNKNOWN_VALUE);
                authContext.setApplicationId(UNKNOWN_VALUE);
                authContext.setSubscriber(UNKNOWN_VALUE);
                authContext.setKeyType(UNKNOWN_VALUE);
            }
        } else {
            log.warn("Ignore API request without authentication context.");
            return true;
        }
        String applicationName = authContext.getApplicationName();
        String applicationId = authContext.getApplicationId();
        String applicationOwner = authContext.getSubscriber();
        String keyType = authContext.getKeyType();

        String endpointAddress = (String) messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        int targetResponseCode = (Integer) messageContext.getProperty(BACKEND_RESPONSE_CODE);

        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }

        String userAgent = (String) messageContext.getProperty(USER_AGENT_PROPERTY);
        long reqMediationLatency = getRequestMediationLatency(messageContext);
        long resMediationLatency = getResponseMediationLatency(messageContext);

        ResponseEvent responseEvent = new ResponseEvent();
        responseEvent.setCorrelationId(UUID.randomUUID().toString());
        responseEvent.setKeyType(keyType);
        responseEvent.setApiId(apiUuid);
        responseEvent.setApiName(apiName);
        responseEvent.setApiVersion(apiVersion);
        responseEvent.setApiCreator(apiCreator);
        responseEvent.setApiMethod(httpMethod);
        responseEvent.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(apiCreator));
        responseEvent.setApiResourceTemplate(apiResourceTemplate);
        responseEvent.setDestination(endpointAddress);
        responseEvent.setApplicationId(applicationId);
        responseEvent.setApplicationName(applicationName);
        responseEvent.setApplicationOwner(applicationOwner);

        responseEvent.setRegionId(REGION_ID);
        responseEvent.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        responseEvent.setUserAgent(userAgent);
        responseEvent.setProxyResponseCode(String.valueOf(proxyResponseCode));
        responseEvent.setTargetResponseCode(String.valueOf(targetResponseCode));
        responseEvent.setResponseCacheHit(String.valueOf(isCacheHit));
        responseEvent.setResponseLatency(String.valueOf(responseTime));
        responseEvent.setBackendLatency(String.valueOf(backendLatency));
        responseEvent.setRequestMediationLatency(String.valueOf(reqMediationLatency));
        responseEvent.setResponseMediationLatency(String.valueOf(resMediationLatency));
        responseEvent.setDeploymentId(DEPLOYMENT_ID);
        responseEvent.setEventType(SUCCESS_EVENT_TYPE);

        OffsetDateTime time = OffsetDateTime.now(Clock.systemUTC());
        responseEvent.setRequestTimeStamp(time.toString());

        PublisherUtils.doPublish(METRIC_NAME, responseEvent);

        return true;
    }

    @Override
    public boolean handleServerInit() {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleServerShutDown() {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleArtifactDeployment(String s, String s1, String s2) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleArtifactUnDeployment(String s, String s1, String s2) {
        // Nothing to implement
        return true;
    }

    private boolean isSuccessRequest(MessageContext messageContext) {
        return !messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)
                && APISecurityUtils.getAuthenticationContext(messageContext) != null;
    }

    private String getUserAgent(MessageContext messageContext) {
        Map<?, ?> headers = (Map<?, ?>) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        return (String) headers.get(APIConstants.USER_AGENT);
    }

    public static long getRequestMediationLatency(MessageContext messageContext) {

        Object reqMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY);
        return reqMediationLatency == null ? 0 : ((Number) reqMediationLatency).longValue();
    }

    public static long getResponseMediationLatency(MessageContext messageContext) {

        Object resMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY);
        return resMediationLatency == null ? 0 : ((Number) resMediationLatency).longValue();
    }

}
