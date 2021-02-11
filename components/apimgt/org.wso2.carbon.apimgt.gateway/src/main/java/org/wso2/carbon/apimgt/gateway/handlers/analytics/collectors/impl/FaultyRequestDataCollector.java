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
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl.fault.*;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultyEvent;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.FaultDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.UUID;

/**
 * Faulty request data collector
 */
public class FaultyRequestDataCollector extends CommonRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(FaultyRequestDataCollector.class);
    private FaultDataCollector authDataCollector;
    private FaultDataCollector throttledDataCollector;
    private FaultDataCollector targetDataCollector;
    private FaultDataCollector resourceNotFoundDataCollector;
    private FaultDataCollector methodNotAllowedDataCollector;
    private FaultDataCollector unclassifiedFaultDataCollector;

    public FaultyRequestDataCollector() {
        this.authDataCollector = new AuthFaultDataCollector();
        this.throttledDataCollector = new ThrottledFaultDataCollector();
        this.targetDataCollector = new TargetFaultDataCollector();
        this.resourceNotFoundDataCollector = new ResourceNotFoundFaultDataCollector();
        this.methodNotAllowedDataCollector = new MethodNotAllowedFaultDataCollector();
        this.unclassifiedFaultDataCollector = new UnclassifiedFaultDataCollector();
    }

    public void collectData(MessageContext messageContext) {
        log.debug("Handling faulty analytics types");
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        FaultyEvent faultyEvent = getFaultyEvent(messageContext);

        if (AnalyticsUtils.isAuthFaultRequest(errorCode)) {
            handleAuthFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isThrottledFaultRequest(errorCode)) {
            handleThrottledFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isTargetFaultRequest(errorCode)) {
            handleTargetFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isResourceNotFound(messageContext)) {
            handleResourceNotFoundFaultRequest(messageContext, faultyEvent);
        } else if (AnalyticsUtils.isMethodNotAllowed(messageContext)) {
            handleMethodNotAllowedFaultRequest(messageContext, faultyEvent);
        } else {
            handleOtherFaultRequest(messageContext, faultyEvent);
        }
    }

    private void handleAuthFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        authDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private void handleThrottledFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        throttledDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private void handleTargetFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        targetDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private void handleResourceNotFoundFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        resourceNotFoundDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private void handleMethodNotAllowedFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        methodNotAllowedDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private void handleOtherFaultRequest(MessageContext messageContext, FaultyEvent faultyEvent) {
        unclassifiedFaultDataCollector.collectFaultData(messageContext, faultyEvent);
    }

    private FaultyEvent getFaultyEvent(MessageContext messageContext) {
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        String errorMessage = (String) messageContext.getProperty(SynapseConstants.ERROR_MESSAGE);
        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }

        API api = getAPIMetaData(messageContext);
        if (api == null) {
            log.error("API not found and ignore publishing event.");
        }
        FaultyEvent event = new FaultyEvent();
        event.setCorrelationId(UUID.randomUUID().toString());
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        event.setApiId(api.getUuid());
        event.setApiType(api.getApiType());
        event.setApiName(api.getApiName());
        event.setApiVersion(api.getApiVersion());
        event.setApiCreator(api.getApiProvider());
        event.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(api.getApiProvider()));
        event.setRegionId(Constants.REGION_ID);
        event.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        event.setProxyResponseCode(proxyResponseCode);
        event.setTargetResponseCode(Constants.UNKNOWN_INT_VALUE);
        event.setDeploymentId(Constants.DEPLOYMENT_ID);
        event.setEventType(Constants.FAULTY_EVENT_TYPE);
        long requestInTime = getRequestTime(messageContext);
        String offsetDateTime = AnalyticsUtils.getTimeInISO(requestInTime);
        event.setRequestTimestamp(offsetDateTime);

        return event;
    }
}
