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
package org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponseEvent;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.RequestHandler;
import org.wso2.carbon.apimgt.usage.publisher.RequestDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.impl.SuccessRequestDataPublisher;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Success request data collector
 */
public class SuccessRequestHandler implements RequestHandler {
    private static final Log log = LogFactory.getLog(SuccessRequestHandler.class);
    private RequestDataPublisher processor;

    public SuccessRequestHandler() {
        this(new SuccessRequestDataPublisher());
    }

    public SuccessRequestHandler(RequestDataPublisher processor) {
        this.processor = processor;
    }

    public void handleRequest(MessageContext messageContext) {
        log.debug("Handling success analytics types");
        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        String apiName = (String) messageContext.getProperty(APIMgtGatewayConstants.API);
        String apiUuid = (String) messageContext.getProperty(APIMgtGatewayConstants.API_UUID_PROPERTY);
        String apiCreator = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        boolean isCacheHit = messageContext.getPropertyKeySet().contains(Constants.CACHED_RESPONSE_KEY);

        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        long responseTime = System.currentTimeMillis() - requestInTime;
        long backendLatency = (Long) messageContext.getProperty(Constants.BACKEND_LATENCY_PROPERTY);

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext != null) {
            if (APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername())) {
                authContext.setApplicationName(Constants.UNKNOWN_VALUE);
                authContext.setApplicationId(Constants.UNKNOWN_VALUE);
                authContext.setSubscriber(Constants.UNKNOWN_VALUE);
                authContext.setKeyType(Constants.UNKNOWN_VALUE);
            }
        } else {
            log.warn("Ignore API request without authentication context.");
            return;
        }
        String applicationName = authContext.getApplicationName();
        String applicationId = authContext.getApplicationId();
        String applicationOwner = authContext.getSubscriber();
        String keyType = authContext.getKeyType();

        String endpointAddress = (String) messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        int targetResponseCode = (Integer) messageContext.getProperty(Constants.BACKEND_RESPONSE_CODE);

        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }

        String userAgent = (String) messageContext.getProperty(Constants.USER_AGENT_PROPERTY);
        long reqMediationLatency = AnalyticsUtils.getRequestMediationLatency(messageContext);
        long resMediationLatency = AnalyticsUtils.getResponseMediationLatency(messageContext);

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

        responseEvent.setRegionId(Constants.REGION_ID);
        responseEvent.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        responseEvent.setUserAgent(userAgent);
        responseEvent.setProxyResponseCode(String.valueOf(proxyResponseCode));
        responseEvent.setTargetResponseCode(String.valueOf(targetResponseCode));
        responseEvent.setResponseCacheHit(String.valueOf(isCacheHit));
        responseEvent.setResponseLatency(String.valueOf(responseTime));
        responseEvent.setBackendLatency(String.valueOf(backendLatency));
        responseEvent.setRequestMediationLatency(String.valueOf(reqMediationLatency));
        responseEvent.setResponseMediationLatency(String.valueOf(resMediationLatency));
        responseEvent.setDeploymentId(Constants.DEPLOYMENT_ID);
        responseEvent.setEventType(Constants.SUCCESS_EVENT_TYPE);

        OffsetDateTime time = OffsetDateTime.now(Clock.systemUTC());
        responseEvent.setRequestTimestamp(time.toString());

        this.processor.publish(responseEvent);
    }

}
