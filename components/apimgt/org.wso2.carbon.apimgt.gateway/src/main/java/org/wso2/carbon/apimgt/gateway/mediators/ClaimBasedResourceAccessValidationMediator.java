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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;

import java.util.Map;

/**
 * This mediator checks the message context for a value of a pre-configured claim which should be sent in the
 * JWT access token. This claim value is also configured in the policy which is uploaded to each API resource
 * in the API Manager publisher. For the mediator to return true, the claim name and the claim value sent in the JWT
 * access token must be identical to the configured claim name and the value. If one of those are not identical,
 * the mediator will return false and an error will be displayed to the client.
 */
public class ClaimBasedResourceAccessValidationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(ClaimBasedResourceAccessValidationMediator.class);

    @Override
    public boolean mediate(MessageContext messageContext) {

        boolean shouldAllowValidation = Boolean.parseBoolean((String) messageContext.getProperty(APIMgtGatewayConstants
                .SHOULD_ALLOW_ACCESS_VALIDATION));

        if (shouldAllowValidation) {

            log.debug("Policy allows claim based resource access validation.");
            String configuredClaim = (String) messageContext.getProperty(APIMgtGatewayConstants
                    .ACCESS_GRANT_CLAIM_NAME);
            String configuredClaimValue = (String) messageContext.getProperty(APIMgtGatewayConstants
                    .ACCESS_GRANT_CLAIM_VALUE);
            Map<String, String> jwtTokenClaims = (Map<String, String>) messageContext
                    .getProperty(APIMgtGatewayConstants.JWT_CLAIMS);

            String claimValueSentInToken;
            claimValueSentInToken = jwtTokenClaims.get(configuredClaim);

            if (StringUtils.isBlank(claimValueSentInToken)) {
                String errorMsg = "The configured resource access validation claim is " +
                        "different form the claim sent in the token or vice versa.";
                log.error(errorMsg);
                handleFailure(HttpStatus.SC_UNAUTHORIZED, messageContext, errorMsg, null);
                return false;
            }

            if (StringUtils.equals(configuredClaimValue, claimValueSentInToken)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The configured claim value \"%s\" matches with  the " +
                            "claim sent in token\"%s\".", configuredClaim, claimValueSentInToken));
                }
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The configured claim value \"%s\" doesn't matche with  the " +
                            "claim sent in token\"%s\".", configuredClaim, claimValueSentInToken));
                }
                String errorMsg = String.format("Configured claim value (%s) and claim value sent in token (%s) " +
                                "doesn't match. The claims must match to access the resource \"%s\"", configuredClaim,
                        claimValueSentInToken, messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE));
                log.error(errorMsg);
                handleFailure(HttpStatus.SC_UNAUTHORIZED, messageContext, errorMsg, null);
                return false;
            }
        } else {
            log.debug("Policy is configured not to allow claim based resource access validation.");
            return true;
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
