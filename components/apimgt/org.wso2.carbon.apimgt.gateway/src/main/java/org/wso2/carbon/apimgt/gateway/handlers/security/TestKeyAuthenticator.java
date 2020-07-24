/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;

import java.util.Map;

/**
 * Authenticate the publisher's test console request call.
 */
public class TestKeyAuthenticator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private String testKey;

    /**
     * Get the testKey.
     *
     * @return
     */
    public String getTestKey() {
        return testKey;
    }

    /**
     * Set the testKey.
     *
     * @param testKey testKey value
     */
    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    /**
     * Retrieve the request's test key header and validate whether its authorized to access the resources.
     *
     * @param messageContext message context
     * @return continue or terminate the request flow.
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) {
        logger.debug("Validating the API request header for the test console request authentication");
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        axis2MC.getProperty((APIMgtGatewayConstants.TRANSPORT_HEADERS));
        Map headers = (Map) axis2MC.getProperty((APIMgtGatewayConstants.TRANSPORT_HEADERS));
        String originalToken = testKey;
        Object header = headers.get(APIMgtGatewayConstants.TEST_KEY);
        int errorStatus;
        if (header == null) {
            errorStatus = APISecurityConstants.API_AUTH_MISSING_CREDENTIALS;
            handleAuthenticationError(messageContext, errorStatus);
        }
        String requestHeader = header.toString();
        if (originalToken.equals(requestHeader)) {
            logger.debug("Successfully authenticating the user for the API");
            return true;
        } else {
            errorStatus = APISecurityConstants.API_AUTH_INVALID_CREDENTIALS;
            handleAuthenticationError(messageContext, errorStatus);
            return false;
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private void handleAuthenticationError(MessageContext mc, int status) {
        mc.setProperty(SynapseConstants.ERROR_CODE, HttpStatus.SC_UNAUTHORIZED);
        mc.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, HttpStatus.SC_UNAUTHORIZED);
        mc.setProperty(SynapseConstants.ERROR_DETAIL, "Either test token is missing or found an invalid test token");

        Mediator sequence = mc.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);
        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            logger.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");
        mc.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, status);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(mc)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        APISecurityException e = new APISecurityException(status, mc.getProperty(SynapseConstants.
                ERROR_DETAIL).toString());
        if (mc.isDoingPOX() || mc.isDoingGET()) {
            setFaultPayload(mc, e);
        } else {
            setSOAPFault(mc, e);
        }
        sendFault(mc, HttpStatus.SC_UNAUTHORIZED);
    }

    protected void sendFault(MessageContext messageContext, int status) {
        Utils.sendFault(messageContext, status);
    }

    protected void setFaultPayload(MessageContext messageContext, APISecurityException e) {
        Utils.setFaultPayload(messageContext, getFaultPayload(e));
    }

    protected void setSOAPFault(MessageContext messageContext, APISecurityException e) {
        Utils.setSOAPFault(messageContext, "Client", "Authentication Failure", e.getMessage());
    }

    protected OMElement getFaultPayload(APISecurityException e) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(e.getErrorCode()));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage()));

        // if custom auth header is configured, the error message should specify its name instead of default value
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_MISSING_CREDENTIALS) {
            String errorDescription =
                    APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage()) +
                            " test-key is missing";
            errorDetail.setText(errorDescription);
        }

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
}
