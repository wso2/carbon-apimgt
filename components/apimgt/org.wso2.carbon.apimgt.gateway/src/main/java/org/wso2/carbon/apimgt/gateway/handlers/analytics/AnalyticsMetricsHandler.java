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
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.impl.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

/**
 * Global synapse handler to publish analytics data to analytics cloud.
 */
public class AnalyticsMetricsHandler extends AbstractExtendedSynapseHandler {
    private static final Log log = LogFactory.getLog(AnalyticsMetricsHandler.class);

    @Override
    public boolean handleError(MessageContext messageContext) {
        // Nothing to implement
        return true;
    }

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        messageContext.setProperty(Constants.REQUEST_START_TIME_PROPERTY, System.currentTimeMillis());
        //Set user agent in request flow
        String userAgent = getUserAgent(messageContext);
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
        messageContext.setProperty(Constants.BACKEND_END_TIME_PROPERTY, System.currentTimeMillis());
        Object responseCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        messageContext.setProperty(Constants.BACKEND_RESPONSE_CODE, responseCode);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        Object skipPublishMetrics = messageContext.getProperty(Constants.SKIP_DEFAULT_METRICS_PUBLISHING);
        if (skipPublishMetrics != null && (Boolean) skipPublishMetrics) {
            return true;
        }
      
        AnalyticsDataProvider provider = new SynapseAnalyticsDataProvider(messageContext);
        GenericRequestDataCollector dataCollector = new GenericRequestDataCollector(provider);
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

    private String getUserAgent(MessageContext messageContext) {
        Map<?, ?> headers = (Map<?, ?>) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        return (String) headers.get(APIConstants.USER_AGENT);
    }

}
