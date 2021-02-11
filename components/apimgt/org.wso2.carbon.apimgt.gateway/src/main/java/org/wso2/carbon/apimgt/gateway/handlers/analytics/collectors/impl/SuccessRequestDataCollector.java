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
package org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponseEvent;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.usage.publisher.RequestDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.impl.SuccessRequestDataPublisher;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.UUID;

/**
 * Success request data collector
 */
public class SuccessRequestDataCollector extends CommonRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(SuccessRequestDataCollector.class);
    private RequestDataPublisher processor;

    public SuccessRequestDataCollector() {
        this(new SuccessRequestDataPublisher());
    }

    public SuccessRequestDataCollector(RequestDataPublisher processor) {
        this.processor = processor;
    }

    public void collectData(MessageContext messageContext) {
        log.debug("Handling success analytics types");
        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        boolean isCacheHit = messageContext.getPropertyKeySet().contains(Constants.CACHED_RESPONSE_KEY);

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext != null) {
            if (APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername())) {
                authContext.setApplicationName(Constants.ANONYMOUS_VALUE);
                authContext.setApplicationId(Constants.ANONYMOUS_VALUE);
                authContext.setSubscriber(Constants.ANONYMOUS_VALUE);
                authContext.setKeyType(Constants.ANONYMOUS_VALUE);
            }
        } else {
            log.warn("Ignore API request without authentication context.");
            return;
        }

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

        long backendLatency = getBackendLatency(messageContext);
        long responseLatency = getResponseLatency(messageContext);
        long requestMediationLatency = getRequestMediationLatency(messageContext);
        long responseMediationLatency = getResponseMediationLatency(messageContext);

        API api = getAPIMetaData(messageContext);
        if (api == null) {
            log.error("API not found and ignore publishing event.");
        }

        ResponseEvent responseEvent = new ResponseEvent();
        setApplicationData(authContext, responseEvent);
        responseEvent.setCorrelationId(UUID.randomUUID().toString());
        responseEvent.setApiId(api.getUuid());
        responseEvent.setApiType(api.getApiType());
        responseEvent.setApiName(api.getApiName());
        responseEvent.setApiVersion(api.getApiVersion());
        responseEvent.setApiCreator(api.getApiProvider());
        responseEvent.setApiMethod(httpMethod);
        responseEvent.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(api.getApiProvider()));
        responseEvent.setApiResourceTemplate(apiResourceTemplate);
        responseEvent.setDestination(endpointAddress);

        responseEvent.setRegionId(Constants.REGION_ID);
        responseEvent.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        responseEvent.setUserAgent(userAgent);
        responseEvent.setProxyResponseCode(proxyResponseCode);
        responseEvent.setTargetResponseCode(targetResponseCode);
        responseEvent.setResponseCacheHit(isCacheHit);
        responseEvent.setResponseLatency(responseLatency);
        responseEvent.setBackendLatency(backendLatency);
        responseEvent.setRequestMediationLatency(requestMediationLatency);
        responseEvent.setResponseMediationLatency(responseMediationLatency);

        responseEvent.setUserAgent(Constants.UNKNOWN_VALUE);
        responseEvent.setPlatform(Constants.UNKNOWN_VALUE);
        responseEvent.setDeploymentId(Constants.DEPLOYMENT_ID);
        responseEvent.setEventType(Constants.SUCCESS_EVENT_TYPE);
        long requestInTime = getRequestTime(messageContext);
        String offsetDateTime = AnalyticsUtils.getTimeInISO(requestInTime);
        responseEvent.setRequestTimestamp(offsetDateTime);

        this.processor.publish(responseEvent);
    }

}
