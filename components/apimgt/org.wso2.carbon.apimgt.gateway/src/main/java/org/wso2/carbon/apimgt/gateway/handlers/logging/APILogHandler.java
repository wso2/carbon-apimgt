/*
 * Copyright (c) 2022, WSO2 Inc.(http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.ThreadContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class APILogHandler {
    private static final Log log = LogFactory.getLog(APILogHandler.class);
    private static final Log logger = LogFactory.getLog(APIConstants.API_LOGGER);
    private static final String API_TO = "API_TO";

    private APILogHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method handles the logging of the API request entities
     *
     * @param flow           Direction of the call (ex:- client to gateway = requestIn)
     * @param messageContext MessageContext of the request
     */
    public static void logAPI(String flow, MessageContext messageContext) {
        // Get the log level and exit if the log level is OFF
        String logLevel = (String) messageContext.getProperty(APIConstants.LOG_LEVEL);
        if (APIConstants.LOG_LEVEL_OFF.equals(logLevel)) {
            return;
        }

        // Print debug log
        if (log.isDebugEnabled()) {
            log.debug("Initiating logging request for " + messageContext.getProperty(API_TO) + " with log level "
                    + logLevel);
        }

        // Add properties to the logMessage according to the log level
        JSONObject logMessage = new JSONObject();
        switch (logLevel.toUpperCase()) {
            case APIConstants.LOG_LEVEL_BASIC:
                addBasicProperties(logMessage, messageContext, flow);
                break;
            case APIConstants.LOG_LEVEL_STANDARD:
                addStandardProperties(logMessage, messageContext, flow);
                break;
            case APIConstants.LOG_LEVEL_FULL:
                addFullProperties(logMessage, messageContext, flow);
                break;
            default:
                break;
        }

        // Get API details and set custom properties to ThreadContext
        String path = ApiUtils.getFullRequestPath(messageContext);
        TreeMap<String, API> selectedApis = Utils.getSelectedAPIList(path, GatewayUtils.getTenantDomain());
        if (selectedApis.size() > 0) {
            String selectedPath = selectedApis.firstKey();
            API selectedApi = selectedApis.get(selectedPath);
            ThreadContext.put("apiId", selectedApi.getUuid());
            ThreadContext.put("apiName", selectedApi.getApiName());
            ThreadContext.put("apiContext", selectedApi.getContext());
            ThreadContext.put("apiVersion", selectedApi.getApiVersion());
            if (messageContext.getProperty(API_TO) != null) {
                String apiTo = "/" + messageContext.getProperty(API_TO);
                ThreadContext.put("resourceName", apiTo.replaceFirst(selectedApi.getContext(), ""));
            }
        }
        ThreadContext.put("tenantDomain", (String) messageContext
                .getProperty(APIConstants.TENANT_DOMAIN_INFO_PROPERTY));
        ThreadContext.put("logCorrelationId", ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext().getLogCorrelationID());

        // Log the logMessage and clear the ThreadContext
        try {
            logger.info(logMessage);
        } finally {
            ThreadContext.clearAll();
        }
    }

    private static void addBasicProperties(JSONObject logMessage, MessageContext messageContext, String flow) {
        logMessage.put("apiTo", messageContext.getProperty(API_TO));
        logMessage.put("correlationId", messageContext.getProperty("correlation_id"));
        logMessage.put("flow", flow);
        String verb = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
        if (verb == null) {
            verb = (String) messageContext.getProperty(APIConstants.REST_METHOD);
        }
        logMessage.put("verb", verb);
        if (flow.contains("REQUEST")) {
            logMessage.put("sourceIP", GatewayUtils.getClientIp(messageContext));
        } else {
            logMessage.put("statusCode", ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .getProperty("HTTP_SC"));
        }
    }

    private static void addStandardProperties(JSONObject logMessage, MessageContext messageContext, String flow) {
        addBasicProperties(logMessage, messageContext, flow);
        JSONArray headers = new JSONArray();
        Map transportHeaders = (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        for (Object transportHeader : transportHeaders.entrySet()) {
            headers.put(transportHeader);
        }
        logMessage.put("headers", headers);
    }

    private static void addFullProperties(JSONObject logMessage, MessageContext messageContext, String flow) {
        addStandardProperties(logMessage, messageContext, flow);
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axis2MC);
        } catch (IOException | XMLStreamException e) {
            log.error("Error occurred while building the message ", e);
        }
        String payload;
        if (JsonUtil.hasAJsonPayload(axis2MC)) {
            payload = JsonUtil.jsonPayloadToString(axis2MC);
        } else {
            payload = messageContext.getEnvelope().toString();
        }
        logMessage.put("payload", payload);
    }
}
