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

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class APIMgtLatencyStatsHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(APIMgtLatencyStatsHandler.class);
    private OpenAPI openAPI;
    private String apiUUID;
    private String swagger;

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public boolean handleRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, Long.toString(System
                    .currentTimeMillis()));
            String method = (String) (axis2MsgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD));
            messageContext.setProperty(APIMgtGatewayConstants.HTTP_METHOD, method);
        }
        /*
        * The axis2 message context is set here so that the method level logging can access the transport headers
        */
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MsgContext);
        long currentTime = System.currentTimeMillis();
        messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
        setSwaggerToMessageContext(messageContext);
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
        if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME, System.currentTimeMillis());
            if (APIUtil.isAnalyticsEnabled()) {
                long executionStartTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants
                        .BACKEND_REQUEST_START_TIME));
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY, System.currentTimeMillis() -
                        executionStartTime);
            }
        }
        return true;
    }

    private void setSwaggerToMessageContext(MessageContext messageContext) {
        // Read OpenAPI from local entry
        if (openAPI == null && apiUUID != null) {
            synchronized (this) {
                if (openAPI == null) {
                    long startTime = System.currentTimeMillis();
                    Entry localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(apiUUID);
                    if (localEntryObj != null) {
                        swagger = localEntryObj.getValue().toString();
                        OpenAPIParser parser = new OpenAPIParser();
                        openAPI = parser.readContents(swagger,
                                null, null).getOpenAPI();
                    }
                    long endTime = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        log.debug("Time to parse the swagger(ms) : " + (endTime - startTime));
                    }
                }
            }
        }
        // Add OpenAPI to message context
        messageContext.setProperty(APIMgtGatewayConstants.OPEN_API_OBJECT, openAPI);
        // Add swagger String to message context
        messageContext.setProperty(APIMgtGatewayConstants.OPEN_API_STRING, swagger);
    }

}
