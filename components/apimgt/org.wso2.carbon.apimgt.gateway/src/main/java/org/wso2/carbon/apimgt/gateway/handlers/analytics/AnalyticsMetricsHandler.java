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
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.RequestDataCollector;

/**
 * Global synapse handler to publish analytics data to analytics cloud.
 */
public class AnalyticsMetricsHandler extends AbstractExtendedSynapseHandler {
    private static final Log log = LogFactory.getLog(AnalyticsMetricsHandler.class);
    private final RequestDataCollector dataCollector = new GenericRequestDataCollector();

    @Override
    public boolean handleError(MessageContext messageContext) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        messageContext.setProperty(Constants.REQUEST_START_TIME_PROPERTY, System.currentTimeMillis());
        //Set user agent in request flow
        String userAgent = AnalyticsUtils.getUserAgent(messageContext);
        messageContext.setProperty(Constants.USER_AGENT_PROPERTY, userAgent);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        messageContext.setProperty(Constants.BACKEND_START_TIME_PROPERTY, System.currentTimeMillis());
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        long backendStartTime = (long) messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        messageContext.setProperty(Constants.BACKEND_LATENCY_PROPERTY, (System.currentTimeMillis() - backendStartTime));
        Object responseCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        messageContext.setProperty(Constants.BACKEND_RESPONSE_CODE, responseCode);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        dataCollector.collectData(messageContext);
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

}
