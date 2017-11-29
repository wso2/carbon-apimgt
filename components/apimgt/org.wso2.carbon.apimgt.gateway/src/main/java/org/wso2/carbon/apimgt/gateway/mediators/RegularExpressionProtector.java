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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * This mediator would protect the backend resources from the threat vulnerabilities by matching the
 * special key words in the request headers, query/path parameters and body.
 */
public class RegularExpressionProtector extends AbstractMediator {
    private Boolean enabledCheckBody = true;
    private String threatType = null;
    private Pattern pattern = null;
    private Boolean enabledCheckHeaders;
    private Boolean enabledCheckPathParam;

    /**
     * This mediate method gets the message context and validate against the special characters.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return A boolean value.True if successful and false if not.
     */
    public boolean mediate(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("RegularExpressionProtector mediator is activated...");
        }
        Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.REGEX_PATTERN);
        if (messageProperty != null) {
            pattern = Pattern.compile(messageProperty.toString(), Pattern.CASE_INSENSITIVE);
        } else {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    "Threat detection key words are missing");
        }
        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY);
        if (messageProperty != null) {
            enabledCheckBody = Boolean.valueOf(messageProperty.toString());
        }
        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM);
        if (messageProperty != null) {
            enabledCheckPathParam = Boolean.valueOf(messageProperty.toString());
        }
        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_HEADERS);
        if (messageProperty != null) {
            enabledCheckHeaders = Boolean.valueOf(messageProperty.toString());
        }
        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.THREAT_TYPE);
        if (messageProperty != null) {
            threatType = String.valueOf(messageProperty);
        }
        checkRequestBody(messageContext);
        checkRequestHeaders(messageContext);
        checkRequestPath(messageContext);
        return true;
    }

    /**
     * This method checks whether the request body contains matching vulnerable key words.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    private void checkRequestBody(MessageContext messageContext) {
        SOAPEnvelope soapEnvelope;
        SOAPBody soapBody;
        OMElement omElement;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        if (enabledCheckBody) {
            soapEnvelope = axis2MC.getEnvelope();
            if (soapEnvelope == null) {
                return;
            }
            soapBody = soapEnvelope.getBody();
            if (soapBody == null) {
                return;
            }
            omElement = soapBody.getFirstElement();
            if (omElement == null) {
                return;
            }
            String payload = omElement.toString();
            if (pattern != null && payload != null && pattern.matcher(payload).find()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                            payload, pattern));
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                        threatType + " " + APIMgtGatewayConstants.PAYLOAD_THREAT_MSG);
            }
        }
    }

    /**
     * This method checks whether the request header contains matching vulnerable keywords.
     * * @param messageContext contains the message properties of the relevant API request which was
     * enabled the regexValidator message mediation in flow.
     */
    private void checkRequestPath(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        if (enabledCheckPathParam) {
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
    }

    /**
     * This method checks whether the request path contains matching vulnerable keywords.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    private void checkRequestHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        if (enabledCheckHeaders) {
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
    }

    /**
     * This method checks the status of the {enabledCheckBody} property which comes from the custom sequence.
     * If a client ask to check the message body,Method returns true else It will return false.
     * If the {isContainetAware} method returns false, The request message payload wont be build.
     * Building a payload will directly affect to the performance.
     *
     * @return If enabledCheckBody is true,The method returns true else it returns false
     */
    public boolean isContentAware() {
        return enabledCheckBody;
    }

}

