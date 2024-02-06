/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This mediator checks the message context for a value of a pre-configured claim which should be sent in the
 * JWT access token. This claim value is also configured in the policy which is uploaded to each API resource
 * in the API Manager publisher. For the mediator to return true, the claim name and the claim value sent in the JWT
 * access token must be identical to the configured claim name and the value. If one of those are not identical,
 * the mediator will return false and an error will be displayed to the client. The value sent in the token claim
 * must conform to a regex which is also configurable.
 */
public class APIResourceAccessValidationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(APIResourceAccessValidationMediator.class);
    private String accessVerificationClaim;

    public void setAccessVerificationClaim(String claimValue) {

        this.accessVerificationClaim = claimValue;
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration();
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        Map<String, String> transportHeaders = (Map<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String configuredClaimName = apiManagerConfiguration.getResourceAccessValidationClaimName();
        String configuredClaimValueRegex = apiManagerConfiguration.getResourceAccessValidationClaimValueRegex();
        String configuredClaimValue = (String) messageContext.getProperty(APIMgtGatewayConstants
                .ACCESS_VERIFICATION_CLAIM_NAME);
        Pattern pattern = Pattern.compile(configuredClaimValueRegex);

        String claimSentInToken = null;
        String authHeader = transportHeaders.get(HTTPConstants.HEADER_AUTHORIZATION);

        // authHeader cannot be empty or null at this point.
        if (StringUtils.contains(authHeader, APIConstants.AUTHORIZATION_BEARER)) {

            String token = authHeader.replace(APIConstants.AUTHORIZATION_BEARER, StringUtils.EMPTY);

            // Decode and get claim
            JSONObject payloadClaims;
            String jwtPayload = token.split("\\.")[1];
            try {
                payloadClaims = new JSONObject(new String(Base64.getDecoder().decode(jwtPayload),
                        String.valueOf(StandardCharsets.UTF_8)));
            } catch (UnsupportedEncodingException e) {
                log.error("Error while decoding JWT claims.", e);
                handleFailure(HttpStatus.SC_BAD_REQUEST, messageContext, "Error when decoding JWT payload.",
                        null);
                return false;
            }
            log.debug("Decoding JWT payload claims successful.");
            if (payloadClaims.has(configuredClaimName)) {
                claimSentInToken = payloadClaims.getString(configuredClaimName);
            } else {
                String errorMsg = "Couldn't find the configure claim \"" + configuredClaimName + "\" in the JWT token.";
                log.error(errorMsg);
                handleFailure(HttpStatus.SC_BAD_REQUEST, messageContext, errorMsg, null);
                return false;
            }
        } else {
            // This validation is not meant for APIs with basic auth. Hence, skip the validation.
            log.debug("The received token is of Basic auth, hence skipping this validation.");
            return true;
        }

        if (StringUtils.isBlank(claimSentInToken)) {
            String errorMsg = "The expected resource access validation claim \"" + configuredClaimName + "\" is " +
                    "not sent in the token.";
            log.error(errorMsg);
            handleFailure(HttpStatus.SC_UNAUTHORIZED, messageContext, errorMsg, null);
            return false;
        }

        // Check whether the claim value sent in token matches the configured regex pattern
        Matcher matcher = pattern.matcher(claimSentInToken);
        if (!matcher.matches()) {
            String errorMsg = "The value of claim \"" + claimSentInToken + "\" sent in the JWT token doesn't conform" +
                    " to the configured regex " + configuredClaimValueRegex + ".";
            log.error(errorMsg);
            handleFailure(HttpStatus.SC_BAD_REQUEST, messageContext, errorMsg, null);
            return false;
        } else {
            if (StringUtils.equals(configuredClaimValue, claimSentInToken)) {
                log.debug( "The configured claim value \"" + configuredClaimValue + "\" matches with  the " +
                        "claim sent in token\"" + claimSentInToken + "\".");
                return true;
            } else {
                String errorMsg = "This token doesn't allow accessing the resource " +
                        messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
                log.error(errorMsg);
                handleFailure(HttpStatus.SC_UNAUTHORIZED, messageContext,
                        errorMsg, null);
                return false;
            }
        }
    }

    /**
     * Sends a fault response to the client.
     *
     * @param errorCode   error code of the failure
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleFailure(int errorCode, MessageContext messageContext,
                               String errorMessage, String errorDescription) {

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(APISecurityConstants.BACKEND_AUTH_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, errorCode);
    }
}
