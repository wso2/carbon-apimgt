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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.TargetResponse;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;

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
            TargetResponse targetResponse = (TargetResponse) ((Axis2MessageContext)messageContext)
                    .getAxis2MessageContext().getProperty("pass-through.Target-Response");
            int statusCode = targetResponse.getStatus();
            if (statusCode == 401) {
                try {
                    OAuthTokenGenerator.checkTokenValidity(OAuthMediator.oAuthEndpoint, null);
                    log.error("OAuth 2.0 access token has been rejected by the backend...");
                    handleFailure(APISecurityConstants.OAUTH_TEMPORARY_SERVER_ERROR, messageContext,
                            APISecurityConstants.OAUTH_TEMPORARY_SERVER_ERROR_MESSAGE, "Please try again");
                } catch (APISecurityException e) {
                    log.error("Error when generating oauth 2.0 access token...", e);
                }
            }
        }
        return true;
    }

    /**
     * Sends a fault response to the DevPortal console
     * @param errorCodeValue error code of the failure
     * @param messageContext message context of the request
     * @param errorMessage error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(int errorCodeValue, MessageContext messageContext,
            String errorMessage, String errorDescription) {
        OMElement payload = getFaultPayload(errorCodeValue, errorMessage, errorDescription);
        Utils.setFaultPayload(messageContext, payload);
        Utils.sendFault(messageContext, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves the organized fault payload
     * @param errorCodeValue error code
     * @param message fault message
     * @param description description of the fault message
     * @return the OMElement object containing the payload
     */
    private OMElement getFaultPayload(int errorCodeValue, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APISecurityConstants.API_SECURITY_NS,
                APISecurityConstants.API_SECURITY_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(errorCodeValue + "");
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }
}
