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
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultyEvent;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.FaultHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.RequestHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.fault.AuthFaultyRequestHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.fault.TargetFaultyRequestHandler;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.fault.ThrottledFaultyRequestHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Faulty request data collector
 */
public class FaultyRequestHandler implements RequestHandler {
    private static final Log log = LogFactory.getLog(FaultyRequestHandler.class);
    private FaultHandler authFaultyHandler;
    private FaultHandler throttledFaultyHandler;
    private FaultHandler targetFaultyHandler;

    public FaultyRequestHandler() {
        this(new AuthFaultyRequestHandler(), new ThrottledFaultyRequestHandler(), new TargetFaultyRequestHandler());
    }

    public FaultyRequestHandler(FaultHandler authFaultyHandler, FaultHandler throttledFaultyHandler,
            FaultHandler targetFaultyHandler) {
        this.authFaultyHandler = authFaultyHandler;
        this.throttledFaultyHandler = throttledFaultyHandler;
        this.targetFaultyHandler = targetFaultyHandler;
    }

    public void handleRequest(MessageContext messageContext) {
        log.debug("Handling faulty analytics types");
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        FaultyEvent faultyEvent = getFaultyEvent(messageContext);

        if (AnalyticsUtils.isAuthFaultRequest(errorCode)) {
            handleAuthFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isThrottledFaultRequest(errorCode)) {
            handleThrottledFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isTargetFaultRequest(errorCode)) {
            handleTargetFaultRequest(messageContext, faultyEvent);
        } else {
            handleOtherFaultRequest(messageContext, faultyEvent);
        }
    }

    private void handleAuthFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        authFaultyHandler.handleFault(messageContext, faultyEvent);
    }

    private void handleThrottledFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        throttledFaultyHandler.handleFault(messageContext, faultyEvent);
    }

    private void handleTargetFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        targetFaultyHandler.handleFault(messageContext, faultyEvent);
    }

    private void handleOtherFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        log.debug("Skip other faulty analytics types");
    }

    private FaultyEvent getFaultyEvent(MessageContext messageContext) {
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        String errorMessage = (String) messageContext.getProperty(SynapseConstants.ERROR_MESSAGE);
        String apiUUID = (String) messageContext.getProperty(APIMgtGatewayConstants.API_UUID_PROPERTY);
        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }

        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        if (apiPublisher == null) {
            int ind = apiVersion.indexOf("--");
            apiPublisher = apiVersion.substring(0, ind);
            if (apiPublisher.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
                apiPublisher = apiPublisher
                        .replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT, APIConstants.EMAIL_DOMAIN_SEPARATOR);
            }
        }
        int index = apiVersion.indexOf("--");
        if (index != -1) {
            apiVersion = apiVersion.substring(index + 2);
        }
        String apiName = apiVersion.split(":v")[0];
        apiVersion = apiVersion.split(":v")[1];

        FaultyEvent event = new FaultyEvent();
        event.setCorrelationId(UUID.randomUUID().toString());
        event.setErrorCode(String.valueOf(errorCode));
        event.setErrorMessage(errorMessage);
        event.setApiId(apiUUID);
        event.setApiName(apiName);
        event.setApiVersion(apiVersion);
        event.setApiCreator(apiPublisher);
        event.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
        event.setRegionId(Constants.REGION_ID);
        event.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        event.setProxyResponseCode(String.valueOf(proxyResponseCode));
        event.setTargetResponseCode(Constants.UNKNOWN_VALUE);
        event.setDeploymentId(Constants.DEPLOYMENT_ID);
        event.setEventType(Constants.FAULTY_EVENT_TYPE);
        OffsetDateTime time = OffsetDateTime.now(Clock.systemUTC());
        event.setRequestTimestamp(time.toString());

        return event;
    }
}
