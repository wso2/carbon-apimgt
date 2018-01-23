/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.threatprotection.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.mediators.DigestAuthMediator;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class would protect the backend resources from the threat vulnerabilities by matching the
 * special key words in the request headers, query/path parameters and body.
 */
public class RegexValidator {

    private static final Log log = LogFactory.getLog(DigestAuthMediator.class);
    private static String threatType = null;
    private static Pattern pattern = null;

    /**
     * This method checks whether the request body contains matching vulnerable key words.
     *
     * @param bufferedInputStream Request message buffered InputStream to be validated
     * @param messageContext      contains the message properties of the relevant API request which was
     *                            enabled the regexValidator message mediation in flow.
     */
    public static void checkRequestBody(BufferedInputStream bufferedInputStream, MessageContext messageContext) {
        byte[] contents = new byte[1024];
        int bytesRead;
        String payload = null;
        String SAL_THREAT_PROTECTION_MSG_PREFIX = "Threat Protection-REGEX_VALIDATION:";

        try {
            while ((bytesRead = bufferedInputStream.read(contents)) != -1) {
                payload += new String(contents, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("Error occurred while parsing the Buffered InputStream.");
        }
        getPattern(messageContext);
        getThreatType(messageContext);
        if ((pattern != null) && (payload != null) && pattern.matcher(payload).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                        payload, pattern));
            }
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    threatType + ": " + SAL_THREAT_PROTECTION_MSG_PREFIX
                            + APIMgtGatewayConstants.PAYLOAD_THREAT_MSG);
        }
    }

    /**
     *  This method checks whether the request header contains matching vulnerable keywords.
     * @param messageContext ontains the message properties of the relevant API request which was
     * enabled the regexValidator message mediation in flow.
     */
    public static void checkRequestPath(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        getPattern(messageContext);
        getThreatType(messageContext);
        String queryParams = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
        if (pattern != null && queryParams != null && pattern.matcher(queryParams).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in query parameters [ %s ] by regex [ %s ]",
                        queryParams, pattern));
            }
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    threatType + " " + APIMgtGatewayConstants.QPARAM_THREAT_MSG);
        }
    }


    /**
     * This method checks whether the request path contains matching vulnerable keywords.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    public static void checkRequestHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        getPattern(messageContext);
        getThreatType(messageContext);
        Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (pattern != null && transportHeaders != null && pattern.matcher(transportHeaders.toString()).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in Transport headers [ %s ] by regex [ %s ]",
                        transportHeaders, pattern));
            }
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    threatType + " " + APIMgtGatewayConstants.HTTP_HEADER_THREAT_MSG);
        }
    }

    /***
     * This method use to get the defined threat patterns from the message context.
     *
     * @param messageContext Contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    private static void getPattern(MessageContext messageContext) {

        Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.REGEX_PATTERN);
        if (messageProperty != null) {
            pattern = Pattern.compile(messageProperty.toString(), Pattern.CASE_INSENSITIVE);
        } else {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    "Threat detection key words are missing");
        }
    }

    /**
     * This method use to get the threat type from the message context.
     *
     * @param messageContext Contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    private static void getThreatType(MessageContext messageContext) {
        Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.THREAT_TYPE);
        if (messageProperty != null) {
            threatType = String.valueOf(messageProperty);
        }
    }

}
