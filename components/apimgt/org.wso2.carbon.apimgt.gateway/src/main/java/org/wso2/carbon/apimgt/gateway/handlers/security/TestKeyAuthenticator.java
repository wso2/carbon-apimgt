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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
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
        if (header == null) {
            handleAuthenticationError(messageContext);
        }
        String requestHeader = header.toString();
        if (originalToken.equals(requestHeader)) {
            logger.debug("Successfully authenticating the user for the API");
            return true;
        } else {
            handleAuthenticationError(messageContext);
            return false;
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    private void handleAuthenticationError(MessageContext mc) {
        mc.setProperty(SynapseConstants.ERROR_CODE, "401");
        mc.setProperty(SynapseConstants.ERROR_MESSAGE, "Authorize error");
        mc.setProperty(SynapseConstants.ERROR_EXCEPTION, "Either test token is missing or found an invalid test token");

        Mediator sequence = mc.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);

        mc.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, HttpStatus.SC_UNAUTHORIZED);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(mc)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        sendFault(mc, HttpStatus.SC_UNAUTHORIZED);
    }

    protected void sendFault(MessageContext messageContext, int status) {
        Utils.sendFault(messageContext, status);
    }
}
