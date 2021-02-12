/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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


package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;
import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.SSE_THROTTLE_DTO;

/**
 * Wraps the authentication handler for the purpose of changing the http method before calling it.
 */
public class SseApiHandler extends APIAuthenticationHandler {

    private GenericRequestDataCollector dataCollector = new GenericRequestDataCollector();

    @Override
    public boolean handleRequest(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Object httpVerb = axisCtx.getProperty(HTTP_METHOD);
        axisCtx.setProperty(HTTP_METHOD, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
        boolean isAuthenticated = super.handleRequest(synCtx);
        axisCtx.setProperty(Constants.Configuration.HTTP_METHOD, httpVerb);
        prepareThrottleData(synCtx);
        publishSubscriptionEvent(synCtx);
        return isAuthenticated;
    }

    private void prepareThrottleData(MessageContext synCtx) {

        AuthenticationContext authenticationContext = (AuthenticationContext) synCtx.getProperty("xxxxx");
        ThrottleDTO throttleDTO = new ThrottleDTO(authenticationContext);
        synCtx.setProperty(SSE_THROTTLE_DTO, throttleDTO);
    }

    private void publishSubscriptionEvent(MessageContext synCtx) {
        dataCollector.collectData(synCtx);
    }

}

