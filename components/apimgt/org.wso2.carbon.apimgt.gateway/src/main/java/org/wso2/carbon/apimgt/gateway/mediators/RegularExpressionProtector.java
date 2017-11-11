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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.ThreatProtectorConstants;

import java.util.Map;
import java.util.regex.Pattern;

public class RegularExpressionProtector extends AbstractMediator {
    org.apache.axis2.context.MessageContext axis2MC;


    public boolean mediate(MessageContext messageContext) {
        axis2MC  = ((Axis2MessageContext)messageContext).getAxis2MessageContext();
        Pattern pattern = Pattern.compile((String)messageContext.getProperty(ThreatProtectorConstants.REGEX_PATTERN),
                Pattern.CASE_INSENSITIVE);
        String queryParams = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
        Map transportHeaders = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String payload = JsonUtil.jsonPayloadToString(axis2MC);

        if (queryParams != null && pattern.matcher(queryParams).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in query parameters [ %s ] by regex [ %s ]",
                        queryParams, pattern));
            }
            handleThreat(messageContext, ThreatProtectorConstants.QPARAM_THREAT_CODE,
                    ThreatProtectorConstants.QPARAM_THREAT_MSG,
                    ThreatProtectorConstants.QPARAM_THREAT_DESC);
            return false;
        }
        if (transportHeaders != null && pattern.matcher(transportHeaders.toString()).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in Transport headers [ %s ] by regex [ %s ]",
                        transportHeaders, pattern));
            }
            handleThreat(messageContext,
                    ThreatProtectorConstants.HTTP_HEADER_THREAT_CODE,
                    ThreatProtectorConstants.HTTP_HEADER_THREAT_MSG,
                    ThreatProtectorConstants.HTTP_HEADER_THREAT_DESC);
            return false;
        }
        if (payload != null && pattern.matcher(payload).find()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Threat detected in request payload [ %s ] by regex [ %s ]))",
                          payload, pattern));
            }
            handleThreat(messageContext,
                    ThreatProtectorConstants.PAYLOAD_THREAT_CODE,
                    ThreatProtectorConstants.PAYLOAD_THREAT_MSG,
                    ThreatProtectorConstants.PAYLOAD_THREAT_DESC);
        }
        return true;

    }

    private void handleThreat(MessageContext messageContext, String threatCode, String threatMsg, String threatDesc) {
        messageContext.setProperty(ThreatProtectorConstants.THREAT_FOUND, true);
        messageContext.setProperty(ThreatProtectorConstants.THREAT_CODE, threatCode);
        messageContext.setProperty(ThreatProtectorConstants.THREAT_MSG, threatMsg);
        messageContext.setProperty(ThreatProtectorConstants.THREAT_DESC, threatDesc);

        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message
            // in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(threatCode, threatMsg, threatDesc));
        } else {
            Utils.setSOAPFault(messageContext, "Client", threatMsg, threatDesc);
        }
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }

    private OMElement getFaultPayload(String threatStatus, String threatMsg, String threatDescription) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(threatStatus));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(threatMsg);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(threatDescription);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
}
