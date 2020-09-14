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

import com.google.re2j.Pattern;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This mediator would protect the backend resources from the threat vulnerabilities by matching the
 * special key words in the request headers, query/path parameters and body.
 */
public class RegularExpressionProtector extends AbstractMediator {

    private static final Log logger = LogFactory.getLog(RegularExpressionProtector.class);
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
        if (logger.isDebugEnabled()) {
            logger.debug("RegularExpressionProtector mediator is activated...");
        }
        if (!isTenantAllowed(messageContext)) {
            return true;
        }
        Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY);

        if (messageProperty != null) {
            enabledCheckBody = Boolean.valueOf(messageProperty.toString());
        }
        if (isContentAware() && isPayloadSizeExceeded(messageContext)) {
            return true;
        }

        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.REGEX_PATTERN);
        if (messageProperty != null) {
            if (pattern == null) {
                pattern = Pattern.compile(messageProperty.toString(), Pattern.CASE_INSENSITIVE);
            }
        } else {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    "Threat detection key words are missing");
            return true;
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
        if (isRequestBodyVulnerable(messageContext) || isRequestHeadersVulnerable(messageContext) ||
                isRequestPathVulnerable(messageContext)) {
            return true;
        }
        return true;
    }

    /**
     * Using Regex Threat Protector mediator will be restricted to the tenants defined by the system property
     * 'regexThreatProtectorEnabledTenants' as a list of comma separated values and super tenant. If this system
     * property is not defined, then this restriction will not be applied at all. If invoked API is existing within a
     * tenant, which was defined in this list, this method returns true. If this system property is not defined, this
     * check won't be done and so will return true, hence all the tenants will be allowed to use this mediator
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return true if the tenant is allowed to use this Mediator
     */
    private boolean isTenantAllowed(MessageContext messageContext) {
        String allowedTenants = System.getProperty(APIMgtGatewayConstants.REGEX_THREAT_PROTECTOR_ENABLED_TENANTS);
        if (allowedTenants == null) {
            return true;
        }
        List<String> allowedTenantsList = Arrays.asList(allowedTenants.split(","));
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(RESTUtils.getFullRequestPath
                (messageContext));
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        if (!allowedTenantsList.contains(tenantDomain) &&
                !(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME).equals(tenantDomain)) {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                    "This tenant is not allowed to use Regular Expression Threat Protector mediator");
            return false;
        }
        return true;
    }
    /**
     * This method returns true if the request payload size exceeds the system property
     * 'payloadSizeLimitForRegexThreatProtector' value (in KB) defined. If this system property is not defined, this
     * check won't be done.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return true if the payload size has exceeded the defined value in system property
     */
    private boolean isPayloadSizeExceeded(MessageContext messageContext) {
        // payloadSizeLimit is in KB
        Integer payloadSizeLimit = Integer.getInteger(APIMgtGatewayConstants.PAYLOAD_SIZE_LIMIT_FOR_REGEX_TREAT_PROTECTOR);
        if (payloadSizeLimit == null) {
            return false;
        }
        long requestPayloadSize = 0;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String contentLength = (String) headers.get(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            requestPayloadSize = Integer.parseInt(contentLength);
        } else {  //When chunking is enabled
            SOAPEnvelope env = messageContext.getEnvelope();
            if (env != null) {
                SOAPBody soapbody = env.getBody();
                if (soapbody != null) {
                    byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                    requestPayloadSize = size.length;
                }
            }
        }
        if (requestPayloadSize > payloadSizeLimit * 1024) {
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, "Exceeded Request Payload " +
                    "size limit allowed to be used with the enabledCheckBody option of Regular Expression Threat " +
                    "Protector mediator");
            return true;
        }
        return false;
    }

    /**
     * This method checks whether the request body contains matching vulnerable key words.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     */
    private boolean isRequestBodyVulnerable(MessageContext messageContext) {
        SOAPEnvelope soapEnvelope;
        SOAPBody soapBody;
        OMElement omElement;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        if (enabledCheckBody) {
            soapEnvelope = axis2MC.getEnvelope();
            if (soapEnvelope == null) {
                return false;
            }
            soapBody = soapEnvelope.getBody();
            if (soapBody == null) {
                return false;
            }
            omElement = soapBody.getFirstElement();
            if (omElement == null) {
                return false;
            }
            String payload = omElement.toString();
            if (pattern != null && payload != null && pattern.matcher(payload).find()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                            payload, pattern));
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                        threatType + " " + APIMgtGatewayConstants.PAYLOAD_THREAT_MSG);
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks whether the request path contains matching vulnerable keywords.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return true if request path contains matching vulnerable keywords.
     */
    private boolean isRequestPathVulnerable(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String parameter = null;
        if (enabledCheckPathParam) {
            String queryParams = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
            try {
                if (queryParams == null) {
                    return false;
                }
                parameter = URLDecoder.decode(queryParams, APIMgtGatewayConstants.UTF8);
            } catch (UnsupportedEncodingException e) {
                String message = "Error occurred while decoding the query/path parameters: " + parameter;
                logger.error(message, e);
                GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE,
                        message + e.getMessage());
                return true;
            }
            if (pattern != null && parameter != null && pattern.matcher(parameter).find()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Threat detected in query parameters [ %s ] by regex [ %s ]",
                            queryParams, pattern));
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                        threatType + " " + APIMgtGatewayConstants.QPARAM_THREAT_MSG);
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks whether the request path contains matching vulnerable keywords.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return true if request Headers contain matching vulnerable keywords
     */
    private boolean isRequestHeadersVulnerable(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        if (enabledCheckHeaders) {
            Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (pattern != null && transportHeaders != null && pattern.matcher(transportHeaders.toString()).find()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Threat detected in Transport headers [ %s ] by regex [ %s ]",
                            transportHeaders, pattern));
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                        threatType + " " + APIMgtGatewayConstants.HTTP_HEADER_THREAT_MSG);
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks the status of the {enabledCheckBody} property which comes from the custom sequence.
     * If a client ask to check the message body,Method returns true else It will return false.
     * If the {isContentAware} method returns false, The request message payload wont be build.
     * Building a payload will directly affect to the performance.
     *
     * @return If enabledCheckBody is true,The method returns true else it returns false
     */
    @Override
    public boolean isContentAware() {
        return enabledCheckBody;
    }

}

