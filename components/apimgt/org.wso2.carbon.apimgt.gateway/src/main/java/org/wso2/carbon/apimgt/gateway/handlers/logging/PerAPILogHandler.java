/*
 * Copyright (c) 2021, WSO2 Inc.(http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.util.Iterator;
import java.util.Map;

public class PerAPILogHandler {

    private static final Log log = LogFactory.getLog(PerAPILogHandler.class);
    private static final String HEADER = "HEADER";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String IN = "in";

    /**
     * This method handles the logging of the API request entities
     *
     * @param flow           If from where to where ( client to gateway = requestIn etc)
     * @param messageContext MessageContext of the request
     */
    public static synchronized void logAPI(String flow, MessageContext messageContext) {
        String apiTo = (String) messageContext.getProperty("API_TO");
        String level = (String) messageContext.getProperty("LOG_LEVEL");
        log.debug("Initiating logging request for API " + apiTo + "flow " + flow + " level " + level);
        String logID = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getLogCorrelationID();
        // The following approach will get the HTTP METHOD in request PATH
        String verb = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                                                                     .getProperty("HTTP_METHOD");
        if (verb == null) {
            // In response flow, HTTP METHOD can be taken from following approach.
            verb = (String) (messageContext).getProperty("REST_METHOD");
        }
        switch (level) {
            case "FULL":
                logHeaders(logID, flow, apiTo, verb, messageContext);
                logPayload(logID, flow, apiTo, verb, messageContext);
                break;
            case "STANDARD":
                logPayload(logID, flow, apiTo, verb, messageContext);
                break;
            case "BASIC":
                logHeaders(logID, flow, apiTo, verb, messageContext);
                break;
            case "OFF":
                break;
            default:
                break;
        }
    }

    /**
     * This methods log the header entities of the API request
     *
     * @param logID          log ID of the API request to correlate the logs
     * @param flow           If from where to where ( client to gateway = requestIn etc)
     * @param apiTo          API context with version with the resource
     * @param verb           HTTP verb of the API
     * @param messageContext MessageContext of the request
     */
    private static void logHeaders(String logID, String flow, String apiTo, String verb,
                                                MessageContext messageContext) {
        Map headers = getTransportHeaders(messageContext);
        Iterator iterator = headers.entrySet().iterator();
        log.debug("Initiating logging headers for the API request " + apiTo + "flow " + flow);
        while (iterator.hasNext()) {
            Map.Entry header = (Map.Entry) iterator.next();
            logEntity(logID, flow, verb, apiTo, HEADER, header);
        }
    }

    /**
     * This method logs the payload of the API request
     *
     * @param logID          log ID of the API request to correlate the logs
     * @param flow           If from where to where ( client to gateway = requestIn etc)
     * @param apiTo          API context with version with the resource
     * @param verb           HTTP verb of the API
     * @param messageContext MessageContext of the request
     */
    private static void logPayload(String logID, String flow, String apiTo, String verb,
                                                MessageContext messageContext) {
        String payload;
        if (IN.equals(flow)) {
            payload = collectPayload(messageContext);
        } else {
            payload = getBuiltPayload(messageContext);
        }

        log.debug("Initiating logging payload for the API request " + apiTo + "flow " + flow);
        logEntity(logID, flow, verb, apiTo, PAYLOAD, payload);
    }

    /**
     * This method logs the entity that it has been provided
     *
     * @param logID log ID of the API request to correlate the logs
     * @param flow  If from where to where ( client to gateway = requestIn etc)
     * @param apiTo API context with version with the resource
     * @param verb  HTTP verb of the API
     * @param type  type of the entry (payload or header)
     * @param entry the object to  be printed
     */
    private static void logEntity(String logID, String flow, String verb, String apiTo, String type, Object entry) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("logging the entity of the API request %s flow %s entity %s", apiTo, flow, entry));
        }

        if (flow.contains(IN)) {
            flow = flow + ">>>>";
        } else {
            flow = flow + "<<<<";
        }
        log.info(logID + " | " + flow + " | " + verb + " | " + apiTo +  " | " + type + " | " + entry);
    }

    /**
     * Get the headers from the message context
     *
     * @param messageContext MessageContext of the API request
     * @return Map containing headers
     */
    private static Map getTransportHeaders(org.apache.synapse.MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                                                           .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Building the payload of  the incoming API request
     *
     * @param messageContext MessageContext of the API request
     * @return built payload as a string
     */
    private static String collectPayload(MessageContext messageContext) {
        String payload = null;
        try {
            org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            RelayUtils.buildMessage(a2mc);
            if (JsonUtil.hasAJsonPayload(a2mc)) {
                payload = JsonUtil.jsonPayloadToString(a2mc);
            } else {
                payload = messageContext.getEnvelope().toString();
            }
        } catch (Exception e) {
            // SOAP envelop is not created yet, hence setting the payload to NONE and continue the flow
            // after logging the error
            log.error("Error occurred while building the message", e);
            payload = "NONE";
        }
        return payload;
    }

    /**
     * Get the payload from the request
     *
     * @param messageContext MessageContext of the API request
     * @return payload as a string
     */
    private static String getBuiltPayload(MessageContext messageContext) {
        StringBuffer sb = new StringBuffer();
        try {
            org.apache.axis2.context.MessageContext a2mc = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            if (JsonUtil.hasAJsonPayload(a2mc)) {
                sb.append(JsonUtil.jsonPayloadToString(a2mc));
            } else if (messageContext.getEnvelope() != null) {
                sb.append(messageContext.getEnvelope());
            }
        } catch (Exception e) {
            SOAPEnvelope envelope = messageContext.isSOAP11() ?
                                    OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope() :
                                    OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
            try {
                messageContext.setEnvelope(envelope);
            } catch (Exception ex) {
                // continue the flow
                log.error("Could not replace faulty SOAP Envelop. Error: ", ex);
                return sb.toString();
            }
        }
        return sb.toString();
    }
}
