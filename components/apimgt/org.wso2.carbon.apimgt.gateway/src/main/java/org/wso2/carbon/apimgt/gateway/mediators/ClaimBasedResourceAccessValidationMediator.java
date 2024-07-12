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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This mediator checks for a value of a pre-configured claim which should be sent in the
 * JWT access token. This claim value is also configured in the policy which is uploaded to each API resource
 * in the API Manager publisher. For the mediator to return true, the claim name and the claim value sent in the JWT
 * access token must be identical to the configured claim name and the value. There is also an option to configure a
 * so the claim values will be matched against that regex. If one of those are not identical,
 * the mediator will return false. The value sent in the token claim must be equal or match with the optionally
 * provided regex. If the "shouldAllowValidation" is true, the flow will be allowed if the claim values are matched. If
 * it is false, the flow will be allowed if the claim values are not matched.
 */
public class ClaimBasedResourceAccessValidationMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(ClaimBasedResourceAccessValidationMediator.class);
    private String accessVerificationClaim;
    private String accessVerificationClaimValue;
    private String accessVerificationClaimValueRegex;
    private boolean shouldAllowValidation;
    public static final String CLAIMS_MISMATCH_ERROR_MSG = "Configured claim and claim " +
            "sent in token do not match.";

    @Override
    public boolean mediate(MessageContext messageContext) {

        String claimValueSentInToken;
        Map<String, String> jwtTokenClaims = (Map<String, String>) messageContext
                .getProperty(APIMgtGatewayConstants.JWT_CLAIMS);

        claimValueSentInToken = jwtTokenClaims.get(accessVerificationClaim);

        if (StringUtils.isBlank(claimValueSentInToken)) {
            log.error("The configured resource access validation claim is " +
                    "not present in the token.");
            handleFailure(HttpStatus.SC_FORBIDDEN, messageContext, String.format("Token doesn't contain the " +
                    "claim \"%s\"", accessVerificationClaim), null);
            return false;
        }

        if (StringUtils.isNotBlank(accessVerificationClaimValueRegex)) {
            log.debug("A regex is provided, hence, validating the claim values using the provided regex.");
            Pattern pattern = Pattern.compile(accessVerificationClaimValueRegex);
            Matcher configuredClaimValueMatcher = pattern.matcher(accessVerificationClaimValue);
            Matcher tokenSentClaimValueMatcher = pattern.matcher(claimValueSentInToken);

            if ((configuredClaimValueMatcher.matches() && tokenSentClaimValueMatcher.matches())
                    || shouldAllowValidation) {
                log.debug("Claim values match or the flow is configured to allow when claims doesn't match. " +
                        "Hence the flow is allowed.");
                return true;
            } else {
                log.debug("Claim values don't match. Hence the flow is not allowed.");
                handleFailure(HttpStatus.SC_FORBIDDEN, messageContext, CLAIMS_MISMATCH_ERROR_MSG, null);
                return false;
            }
        } else {
            log.debug("A regex is not provided, validating the claim values based on equality.");
            if ((StringUtils.equals(accessVerificationClaimValue, claimValueSentInToken)) || shouldAllowValidation) {
                log.debug("Claim values match or the flow is configured to allow when claims doesn't match. " +
                        "Hence the flow is allowed.");
                return true;
            } else {
                log.debug("Claim values don't match. Hence the flow is not allowed.");
                handleFailure(HttpStatus.SC_FORBIDDEN, messageContext, CLAIMS_MISMATCH_ERROR_MSG, null);
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

    public void setAccessVerificationClaim(String accessVerificationClaim) {
        this.accessVerificationClaim = accessVerificationClaim;
    }

    public void setAccessVerificationClaimValue(String accessVerificationClaimValue) {
        this.accessVerificationClaimValue = accessVerificationClaimValue;
    }

    public void setShouldAllowValidation(boolean shouldAllowValidation) {
        this.shouldAllowValidation = shouldAllowValidation;
    }

    public void setAccessVerificationClaimValueRegex(String accessVerificationClaimValueRegex) {
        this.accessVerificationClaimValueRegex = accessVerificationClaimValueRegex;
    }
}
