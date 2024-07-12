/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.ThreadContext;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APILoggerManager;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.logging.APILogHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.correlation.MethodCallsCorrelationConfigDataHolder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * This Handler can be used to log all external calls done by the api manager via synapse.
 */
public class LogsHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(APIConstants.CORRELATION_LOGGER);
    private String apiTo = null;

    private static final String AUTH_HEADER = "AUTH_HEADER";
    private static final String ORG_ID_HEADER = "ORG_ID_HEADER";
    private static final String SRC_ID_HEADER = "SRC_ID_HEADER";
    private static final String APP_ID_HEADER = "APP_ID_HEADER";
    private static final String UUID_HEADER = "UUID_HEADER";
    private static final String CORRELATION_ID_HEADER = "CORRELATION_ID_HEADER";
    protected static final String LOG_LEVEL = "LOG_LEVEL";
    protected static final String RESOURCE_PATH = "RESOURCE_PATH";
    protected static final String RESOURCE_METHOD = "RESOURCE_METHOD";
    private static final String REQUEST_BODY_SIZE_ERROR = "Error occurred while building the message to calculate" +
            " the response body size";
    private static final String REQUEST_EVENT_PUBLICATION_ERROR = "Cannot publish request event. ";
    private static final String RESPONSE_EVENT_PUBLICATION_ERROR = "Cannot publish response event. ";

    private static final String REQUEST_IN = "REQUEST_IN";
    private static final String REQUEST_OUT = "REQUEST_OUT";
    private static final String RESPONSE_IN = "RESPONSE_IN";
    private static final String RESPONSE_OUT = "RESPONSE_OUT";

    public LogsHandler() {
        log.info("Started log handler");
    }

    private boolean isEnabled() {
        return MethodCallsCorrelationConfigDataHolder.isEnable();
    }

    public boolean handleRequestInFlow(MessageContext messageContext) {
        if (isEnabled()) {
            try {
                apiTo = LogUtils.getTo(messageContext);
            } catch (Exception e) {
                log.error(REQUEST_EVENT_PUBLICATION_ERROR + e.getMessage(), e);
                return false;
            }
        }
        // Get the log level of if logs are enabled to the API belongs to current API request
        String log = getAPILogLevel(messageContext);
        // If it presents log the details
        if ((log) != null) {
            APILogHandler.logAPI(REQUEST_IN, messageContext);
        }
        return true;
    }

    public boolean handleRequestOutFlow(MessageContext messageContext) {
        if (isEnabled()) {
            try {
                Map headers = LogUtils.getTransportHeaders(messageContext);
                String correlationIdHeader = null;
                if (headers != null) {
                    Set<String> key = headers.keySet();
                    String authHeader = LogUtils.getAuthorizationHeader(headers);
                    String orgIdHeader = LogUtils.getOrganizationIdHeader(headers);
                    String SrcIdHeader = LogUtils.getSourceIdHeader(headers);
                    String applIdHeader = LogUtils.getApplicationIdHeader(headers);
                    String uuIdHeader = LogUtils.getUuidHeader(headers);
                    correlationIdHeader = LogUtils.getCorrelationHeader(headers);
                    messageContext.setProperty(AUTH_HEADER, authHeader);
                    messageContext.setProperty(ORG_ID_HEADER, orgIdHeader);
                    messageContext.setProperty(SRC_ID_HEADER, SrcIdHeader);
                    messageContext.setProperty(APP_ID_HEADER, applIdHeader);
                    messageContext.setProperty(UUID_HEADER, uuIdHeader);
                }

                if (ThreadContext.get(APIConstants.CORRELATION_ID) != null) {
                    correlationIdHeader = ThreadContext.get(APIConstants.CORRELATION_ID);
                }
                messageContext.setProperty(CORRELATION_ID_HEADER, correlationIdHeader);
                // apiTo = LogUtils.getTo(messageContext);
            } catch (Exception e) {
                log.error(REQUEST_EVENT_PUBLICATION_ERROR + e.getMessage(), e);
                return false;
            }
        }
        String log = (String) messageContext.getProperty(LOG_LEVEL);
        if (log != null) {
            APILogHandler.logAPI(REQUEST_OUT, messageContext);
        }
        return true;
    }

    public boolean handleResponseInFlow(MessageContext messageContext) {
        if (isEnabled()) {
            // default API would have the property LoggedResponse as true.
            String defaultAPI = (String) messageContext.getProperty("DefaultAPI");
            if ("true".equals(defaultAPI)) {
                log.debug("Default API is invoked");
            } else {
                try {
                    long responseTime = getResponseTime(messageContext);
                    long beTotalLatency = getBackendLatency(messageContext);
                    long responseSize = getContentLength(messageContext);
                    String authHeader = (String) messageContext.getProperty(AUTH_HEADER);
                    String orgIdHeader = (String) messageContext.getProperty(ORG_ID_HEADER);
                    String SrcIdHeader = (String) messageContext.getProperty(SRC_ID_HEADER);
                    String applIdHeader = (String) messageContext.getProperty(APP_ID_HEADER);
                    String uuIdHeader = (String) messageContext.getProperty(UUID_HEADER);
                    String correlationIdHeader = (String) messageContext.getProperty(CORRELATION_ID_HEADER);
                    ThreadContext.put(APIConstants.CORRELATION_ID, correlationIdHeader);
                    log.info(beTotalLatency + "|HTTP|" + LogUtils.getAPIName(messageContext) + "|"
                            + LogUtils.getRestMethod(messageContext) + "|" + LogUtils.getAPICtx(messageContext)
                            + LogUtils.getElectedResource(messageContext) + "|" + apiTo + "|" + authHeader + "|"
                            + orgIdHeader + "|" + SrcIdHeader + "|" + applIdHeader + "|" + uuIdHeader + "|"
                            + getContentLength(messageContext) + "|" + responseSize + "|"
                            + LogUtils.getRestHttpResponseStatusCode(messageContext) + "|"
                            + LogUtils.getApplicationName(messageContext) + "|"
                            + LogUtils.getConsumerKey(messageContext) + "|" + responseTime);
                } catch (Exception e) {
                    log.error(RESPONSE_EVENT_PUBLICATION_ERROR + e.getMessage(), e);
                    return false;
                }
            }
        }
        // if PER API logging is available
        String log = (String) messageContext.getProperty(LOG_LEVEL);
        if (log != null) {
            APILogHandler.logAPI(RESPONSE_IN, messageContext);
        }
        return true;
    }

    public boolean handleResponseOutFlow(MessageContext messageContext) {
        // if PER API logging is available
        String log = (String) messageContext.getProperty(LOG_LEVEL);
        if (log != null) {
            APILogHandler.logAPI(RESPONSE_OUT, messageContext);
        }
        return true;
    }

    /*
     * getBackendLatency
     */
    private long getBackendLatency(org.apache.synapse.MessageContext messageContext) {
        long beTotalLatency = 0;
        long beStartTime = 0;
        long beEndTime = 0;
        long executionStartTime = 0;
        try {
            if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) == null) {
                if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME) != null) {
                    executionStartTime = Long.parseLong(
                            (String) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME));
                }
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY,
                        System.currentTimeMillis() - executionStartTime);
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME, System.currentTimeMillis());
            }
            if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME) != null) {
                beStartTime = Long.parseLong(
                        (String) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME));
            }
            if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) != null) {
                beEndTime = (Long) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME);
            }
            beTotalLatency = beEndTime - beStartTime;

        } catch (Exception e) {
            log.error("Error getBackendLatency -  " + e.getMessage(), e);
        }
        return beTotalLatency;
    }

    /*
     * getResponseTime
     */
    private long getResponseTime(org.apache.synapse.MessageContext messageContext) {
        long responseTime = 0;
        try {
            long rtStartTime = 0;
            if (messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME) != null) {
                Object objRtStartTime = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME);
                rtStartTime = (objRtStartTime == null ? 0 : Long.parseLong((String) objRtStartTime));
            }
            responseTime = System.currentTimeMillis() - rtStartTime;
        } catch (Exception e) {
            log.error("Error getResponseTime -  " + e.getMessage(), e);
        }
        return responseTime;
    }

    private long getContentLength(org.apache.synapse.MessageContext messageContext) {
        long requestSize = -1;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String contentLength = null;
        if (headers != null) {
            contentLength = (String) headers.get(HttpHeaders.CONTENT_LENGTH);
        }
        if (contentLength != null) {
            requestSize = Integer.parseInt(contentLength);
            // request size is left as -1 if chunking is enabled. this is to avoid building the message
        }
        return requestSize;
    }

    private long buildResponseMessage(org.apache.synapse.MessageContext messageContext) {
        long responseSize = 0;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String contentLength = (String) headers.get(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            responseSize = Integer.parseInt(contentLength);
        } else {
            // When chunking is enabled
            try {
                RelayUtils.buildMessage(axis2MC);
            } catch (IOException | XMLStreamException ex) {
                // In case of an exception, it won't be propagated up,and set response size to 0
                log.error(REQUEST_BODY_SIZE_ERROR, ex);
            }
        }
        SOAPEnvelope env = messageContext.getEnvelope();
        if (env != null) {
            SOAPBody soapbody = env.getBody();
            if (soapbody != null) {
                byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                responseSize = size.length;
            }

        }
        return responseSize;

    }
    /**
     * Sync the node's map based on the user given values.
     *
     * @param map Map containing API context and logLevel
     */
    public static Map<Map<String, String>, String> syncAPILogData(Map<String, Object> map) {
        String apictx = (String) map.get("context");
        String logLevel = (String) map.get("value");
        log.debug("Log level for " + apictx + " is changed to " + logLevel);
        return APILoggerManager.getInstance().getPerAPILoggerList();
    }
    public static String getLogData(String context) {
        return APILoggerManager.getInstance().getPerAPILoggerList().get(context);
    }

    public static Map<Map<String, String>, String> getLogData() {
        return APILoggerManager.getInstance().getPerAPILoggerList();
    }

    /**
     * Check if the incoming API need to be logged, if yes return the loglevel, if not return null
     *
     * @param ctx MessageContext of the incoming request
     * @return log level of the API or null if not
     */
    private String getAPILogLevel(MessageContext ctx) {
        Map<Map<String, String>, String> logProperties = APILoggerManager.getInstance().getPerAPILoggerList();
        // if the logging API data holder is empty or null return null
        if (!logProperties.isEmpty()) {
            return LogUtils.getMatchingLogLevel(ctx, logProperties);
        }
        return null;
    }
}
