/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class APIMgtLatencyStatsHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(APIMgtLatencyStatsHandler.class);


    public boolean handleRequest(MessageContext messageContext) {
        if (messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, Long.toString(System
                    .currentTimeMillis()));
        }
        /*
        * The axis2 message context is set here so that the method level logging can access the transport headers
        */
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MC);
        long currentTime = System.currentTimeMillis();
        messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        /*
         * The axis2 message context is set here so that the method level logging can access the
         * transport headers
         */
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MC);
        if (APIUtil.isAnalyticsEnabled()) {
            if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) == null) {

                long executionStartTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants
                        .BACKEND_REQUEST_START_TIME));
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY, System.currentTimeMillis() -
                        executionStartTime);
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME, System.currentTimeMillis());
            }
        }
        return true;
    }

}
