/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators.oauth;

import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.TargetResponse;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;

/**
 * OAuthResponseMediator to handle error responses from OAuth 2.0 protected backends
 */
public class OAuthResponseMediator extends AbstractMediator implements ManagedLifecycle {

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        // Ignore
    }

    @Override
    public void destroy() {
        // Ignore
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (messageContext != null) {
            TargetResponse targetResponse = (TargetResponse) ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext().getProperty("pass-through.Target-Response");
            int statusCode = targetResponse.getStatus();
            if (statusCode == 401) {
                Object oauthEndpointObject = messageContext.getProperty(APIMgtGatewayConstants.OAUTH_ENDPOINT_INSTANCE);
                if (oauthEndpointObject instanceof OAuthEndpoint) {
                    try {
                        OAuthTokenGenerator.generateToken((OAuthEndpoint) oauthEndpointObject, null);
                        log.error("OAuth 2.0 access token has been rejected by the backend...");
                        handleFailure(APISecurityConstants.OAUTH_TEMPORARY_SERVER_ERROR, messageContext,
                                APISecurityConstants.OAUTH_TEMPORARY_SERVER_ERROR_MESSAGE, "Please try again");
                    } catch (APISecurityException e) {
                        log.error("Error when generating oauth 2.0 access token...", e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sends a fault response to the DevPortal console
     *
     * @param errorCodeValue   error code of the failure
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(int errorCodeValue, MessageContext messageContext,
                               String errorMessage, String errorDescription) {

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCodeValue);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(APISecurityConstants.BACKEND_AUTH_FAILURE_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        Utils.sendFault(messageContext, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

}
