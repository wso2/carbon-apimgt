/*
* Copyright (c) 2015, WSO2 Inc.(http://www.wso2.org) All Rights Reserved.
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* */

package org.wso2.carbon.apimgt.gateway.handlers.logging;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Simple extension handler for message logging for the apis deployed in the API Gateway.
 * This handler first looks for a sequence named WSO2AM--Ext--[Dir], where [Dir] could be
 * either In or Out depending on the direction of the message. If such a sequence is found,
 * It log the information of the particular API. If such an API specific sequence is found, that
 * is also invoked. If no extension is found either at the global level or at the per API level
 * this mediator simply returns true.
 */
public class APILogMessageHandler extends AbstractHandler {

    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";

    private static final Log log = LogFactory.getLog(APILogMessageHandler.class);

    private boolean mediate(MessageContext messageContext, String direction) {

        String applicationName = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
        String endUserName = (String) messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME);
        boolean isLoginRequest = false;
        String logMessage = "";

        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (applicationName != null) {
            logMessage = " appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " , userName=" + endUserName;
        }
        String httpMethod = String.valueOf(axisMC.getProperty(Constants.Configuration.HTTP_METHOD));
        if (httpMethod != null) {
            logMessage = logMessage + " , httpMethod=" + httpMethod;
        }
        Map headers = (Map) axisMC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String logID = (String) headers.get(APIConstants.ACTIVITY_ID);


        if (DIRECTION_OUT.equals(direction) && logID == null) {
            try {
                org.apache.axis2.context.MessageContext inMessageContext =
                        axisMC.getOperationContext().getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                if (inMessageContext != null) {
                    Object inTransportHeaders =
                            inMessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                    if (inTransportHeaders != null) {
                        String inID = (String) ((Map) inTransportHeaders).get(APIConstants.ACTIVITY_ID);
                        if (inID != null) {
                            logID = inID;
                        }
                    }
                }
            } catch (AxisFault axisFault) {
                //Ignore Axis fault to continue logging
                log.error("Cannot get Transport headers from Gateway", axisFault);
            }
        }
        if (logID != null) {
            logMessage = logMessage + " , transactionId=" + logID;
        }
        String userAgent = (String) ((Map<String, Object>) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIConstants.USER_AGENT);
        if (userAgent != null) {
            logMessage = logMessage + " , userAgent=" + userAgent;
        }

        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " , requestURI=" + requestURI;
            if ("/token/".equalsIgnoreCase(requestURI)) {
                isLoginRequest = true;
            }
        }
        long reqIncomingTimestamp = Long.parseLong((String) ((Axis2MessageContext) messageContext).
                getAxis2MessageContext().getProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME));
        Date incomingReqTime = new Date(reqIncomingTimestamp);
        logMessage = logMessage + " , requestTime=" + incomingReqTime;

        String remoteIP = (String) ((Map<String, Object>) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        if (remoteIP != null) {
            if (remoteIP.indexOf(',') > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(','));
            }
        } else {
            remoteIP = (String) axisMC.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }
        //null check before add it to log message
        if (remoteIP != null) {
            logMessage = logMessage + " , clientIP=" + remoteIP;
        }
        if (DIRECTION_OUT.equals(direction)) {
            String statusCode = String.valueOf(axisMC.getProperty(NhttpConstants.HTTP_SC));

            if (StringUtils.isNotEmpty(statusCode)) {
                logMessage = logMessage + " , statusCode=" + statusCode;
            }
        }
        if (isLoginRequest) {

            if (DIRECTION_IN.equals(direction)) {
                log.debug("Inbound OAuth token request from client to gateway: " + logMessage);

            } else if (DIRECTION_OUT.equals(direction)) {
                log.debug("Outbound OAuth token response from gateway to client: " + logMessage);
            }
        } else {
            if (DIRECTION_IN.equals(direction)) {
                log.debug("Inbound API call from client to gateway: " + logMessage);

            } else if (DIRECTION_OUT.equals(direction)) {
                logMessage = logMessage + " , EndPointURL=" + messageContext.getProperty(
                        SynapseConstants.ENDPOINT_PREFIX);
                log.debug("Outbound API call from gateway to client: " + logMessage);
            }
        }
        return true;
    }

    public boolean handleRequest(MessageContext messageContext) {
        return !log.isDebugEnabled() || mediate(messageContext, DIRECTION_IN);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return !log.isDebugEnabled() || mediate(messageContext, DIRECTION_OUT);
    }
}
