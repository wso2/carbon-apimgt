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
 * special key words in the request headers, query parameters and body.
 */
public class RegularExpressionProtector extends AbstractMediator {
    private Boolean enabledCheckBody = true;

    /**
     * This mediate method get the message context and validate against special characters.
     *
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled the regexValidator message mediation in flow.
     * @return A boolean value.True if successful and false if not.
     */
    public boolean mediate(MessageContext messageContext) {
        if (log.isDebugEnabled()) {
            log.debug("RegularExpressionProtector mediator is activated...");
        }
        Object messageProperty;
        Pattern pattern = null;

        Boolean enabledCheckQueryParam = true;
        Boolean enabledCheckPathParam = true;
        org.apache.axis2.context.MessageContext axis2MC;
        axis2MC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.REGEX_PATTERN);
        if (messageProperty != null) {
            pattern = Pattern.compile(messageProperty.toString(), Pattern.CASE_INSENSITIVE);
        }

        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_BODY);
        if (messageProperty != null) {
            enabledCheckBody = Boolean.valueOf(messageProperty.toString());
        }

        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_PATHPARAM);
        if (messageProperty != null) {
            enabledCheckPathParam = Boolean.valueOf(messageProperty.toString());
        }

        messageProperty = messageContext.getProperty(APIMgtGatewayConstants.ENABLED_CHECK_QUERYPARAM);
        if (messageProperty != null) {
            enabledCheckQueryParam = Boolean.valueOf(messageProperty.toString());
        }

        String queryParams = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
        Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (enabledCheckBody) {
            String payload = axis2MC.getEnvelope().getBody().getFirstElement().toString();
            if (pattern != null && payload != null && pattern.matcher(payload).find()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                            payload, pattern));
                }
                GatewayUtils.handleException(messageContext, APIMgtGatewayConstants.PAYLOAD_THREAT_MSG);
            }
        }
        if (enabledCheckQueryParam) {
            if (pattern != null && queryParams != null && pattern.matcher(queryParams).find()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Threat detected in query parameters [ %s ] by regex [ %s ]",
                            queryParams, pattern));
                }
                GatewayUtils.handleException(messageContext, APIMgtGatewayConstants.QPARAM_THREAT_MSG);
            }
        }
        if (enabledCheckPathParam) {
            if (pattern != null && transportHeaders != null && pattern.matcher(transportHeaders.toString()).find()) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Threat detected in Transport headers [ %s ] by regex [ %s ]",
                            transportHeaders, pattern));
                }
                GatewayUtils.handleException(messageContext, APIMgtGatewayConstants.HTTP_HEADER_THREAT_MSG);
            }
        }
        return true;
    }

    /**
     * This method checks the status of the checkbody status which comes from the custom sequence.If client ask to check
     * the message body isContentenAware method returns true else It will return false.
     * @return enabledCheckBody is true this returns true else it returns false to avoid the message build.
     */

    public boolean isContentAware() {
        return !enabledCheckBody;
    }
}
