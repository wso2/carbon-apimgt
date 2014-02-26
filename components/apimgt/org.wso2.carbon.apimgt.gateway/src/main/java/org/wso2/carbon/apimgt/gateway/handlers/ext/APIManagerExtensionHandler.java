/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.ext;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple extension handler for the APIs deployed in the API gateway. This handler first
 * looks for a sequence named WSO2AM--Ext--[Dir], where [Dir] could be either In or Out
 * depending on the direction of the message. If such a sequence is found, it is invoked.
 * Following that a more API specific extension sequence is looked up by using the name
 * pattern provider--api--version--[Dir]. If such an API specific sequence is found, that
 * is also invoked. If no extension is found either at the global level or at the per API level
 * this mediator simply returns true.
 */
public class APIManagerExtensionHandler extends AbstractHandler {

    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";
    private static final Log log = LogFactory.getLog(APIManagerExtensionHandler.class);

    public boolean mediate(MessageContext messageContext, String direction) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence != null && sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        sequence = localRegistry.get(apiName + "--" + direction);
        if (sequence != null && sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }

    private void logMessageDetails(MessageContext messageContext, String direction) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.gateway.handlers
        String applicationName = (String) messageContext.getProperty("APPLICATION_NAME");
        boolean isLoginRequest = false;
        String endUserName = (String) messageContext.getProperty("END_USER_NAME");
        Date incomingReqTime = new Date();
        //Do not change this log format since its using by some external apps
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String logMessage = "";
        if (applicationName != null) {
            logMessage = " appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " , userName=" + endUserName;
        }
        //String logID = axisMC.getOptions().getMessageId();
        Map headers = (Map) axisMC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String logID = (String) headers.get("activityID");
        if (DIRECTION_OUT.equals(direction) && logID  == null) {
            try {
                org.apache.axis2.context.MessageContext inMessageContext = axisMC.getOperationContext().getMessageContext(
                        WSDL2Constants.MESSAGE_LABEL_IN);
                if (inMessageContext != null) {
                    Object inTransportHeaders = inMessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                    if (inTransportHeaders != null) {
                        String inID = (String) ((Map) inTransportHeaders).get("activityID");
                        if (inID != null) {
                            logID = inID;
                        }
                    }
                }
            } catch (AxisFault axisFault) {
                //ignore
            }
        }
        if (logID != null) {
            logMessage = logMessage + " , transactionId=" + logID;
        }
        try{
            String userAgent = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("User-Agent");
            if (userAgent != null) {
                logMessage = logMessage + " , userAgent=" + userAgent;
            }
        }catch (Exception e){
            //We do nothing here simply this parameter will skip in debug logs;
        }
        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " , requestURI=" + requestURI;
            if (requestURI.equalsIgnoreCase("/login/")) {
                isLoginRequest = true;
            }
        }
        try{
        long reqIncomingTimestamp = Long.parseLong((String) ((Axis2MessageContext) messageContext).
                getAxis2MessageContext().getProperty("wso2statistics.request.received.time"));
        incomingReqTime = new Date(reqIncomingTimestamp);
        if (incomingReqTime != null) {
            logMessage = logMessage + " , requestTime=" + incomingReqTime;
        }
        }catch (Exception e){
            //We do nothing here simply this parameter will skip in debug logs
        }
        try {
            String remoteIP = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("X-Forwarded-For");
            if (remoteIP != null) {
                if (remoteIP.indexOf(",") > 0) {
                    remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
                }
            } else {

                remoteIP = (String) axisMC.getProperty("REMOTE_ADDR");
            }
            //null check before add it to log message
            if (remoteIP != null) {
                logMessage = logMessage + " , clientIP=" + remoteIP;
            }
        } catch (Exception e) {
            //We do nothing here simply this parameter will skip in debug logs
        }
        if (isLoginRequest) {

            if (DIRECTION_IN.equals(direction)) {
                log.debug("Inbound OAuth token request from client to gateway: " + logMessage);
            } else if (DIRECTION_OUT.equals(direction)) {
                //logMessage = logMessage + " EndPointURL=" + axisMC.getProperty("ENDPOINT_PREFIX");
                log.debug("Outbound OAuth token response from gateway to client: " + logMessage);
            }

        } else {
            if (DIRECTION_IN.equals(direction)) {
                log.debug("Inbound API call from client to gateway: " + logMessage);
            } else if (DIRECTION_OUT.equals(direction)) {
                logMessage = logMessage + " , EndPointURL=" + axisMC.getProperty("ENDPOINT_PREFIX");
                log.debug("Outbound API call from gateway to client: " + logMessage);
            }
        }
    }

    private void logInBoundMessageDetails(MessageContext messageContext) {
        logMessageDetails(messageContext, DIRECTION_IN);
    }

    private void logOutBoundMessageDetails(MessageContext messageContext) {
        logMessageDetails(messageContext, DIRECTION_OUT);
    }

    public boolean handleRequest(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            logInBoundMessageDetails(messageContext);
        }
        return mediate(messageContext, DIRECTION_IN);
    }

    public boolean handleResponse(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            logOutBoundMessageDetails(messageContext);
        }
        return mediate(messageContext, DIRECTION_OUT);
    }
}

