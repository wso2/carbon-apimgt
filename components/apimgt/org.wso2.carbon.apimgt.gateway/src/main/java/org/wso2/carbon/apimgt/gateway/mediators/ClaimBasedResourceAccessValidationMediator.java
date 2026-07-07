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
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    @Override
    public boolean mediate(MessageContext messageContext) {

        try {
            Map<String, Object> jwtTokenClaims = (Map<String, Object>) messageContext
                    .getProperty(APIMgtGatewayConstants.JWT_CLAIMS);
            Object claimValueSentInToken = jwtTokenClaims == null ? null : jwtTokenClaims.get(accessVerificationClaim);
            List<String> tokenClaimValues = getClaimValues(claimValueSentInToken);

            if (tokenClaimValues.isEmpty()) {
                log.error("The configured resource access validation claim is " + "not present in the token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_INVALID,
                                               APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_INVALID_MESSAGE,
                                               String.format("Token doesn't contain the " + "claim \"%s\"",
                                                             accessVerificationClaim));
            }

            if (isClaimMatched(tokenClaimValues) || shouldAllowValidation) {
                log.debug("Claim values match or the flow is configured to allow when claims doesn't match. "
                                  + "Hence the flow is allowed.");
                return true;
            }

            log.debug("Claim values don't match. Hence the flow is not allowed.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH,
                                           APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH_MESSAGE,
                                           APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH_DESCRIPTION);
        } catch (APISecurityException e) {
            handleAuthFailure(e.getErrorCode(), messageContext, e.getMessage(), e.getDescription());
            return false;
        }
    }

    private List<String> getClaimValues(Object claimValueSentInToken) {

        List<String> tokenClaimValues = new ArrayList<>();
        if (claimValueSentInToken == null) {
            return tokenClaimValues;
        }

        if (claimValueSentInToken instanceof Collection<?>) {
            for (Object claimValue : (Collection<?>) claimValueSentInToken) {
                addClaimValue(tokenClaimValues, claimValue);
            }
        } else if (claimValueSentInToken.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(claimValueSentInToken); i++) {
                addClaimValue(tokenClaimValues, Array.get(claimValueSentInToken, i));
            }
        } else {
            addClaimValue(tokenClaimValues, claimValueSentInToken);
        }
        return tokenClaimValues;
    }

    private void addClaimValue(List<String> tokenClaimValues, Object claimValue) {

        if (claimValue != null && StringUtils.isNotBlank(String.valueOf(claimValue))) {
            tokenClaimValues.add(String.valueOf(claimValue));
        }
    }

    private boolean isClaimMatched(List<String> tokenClaimValues) {

        if (StringUtils.isNotBlank(accessVerificationClaimValueRegex)) {
            log.debug("A regex is provided, hence, validating the claim values using the provided regex.");
            Pattern pattern = Pattern.compile(accessVerificationClaimValueRegex);
            Matcher configuredClaimValueMatcher = pattern.matcher(StringUtils.defaultString(accessVerificationClaimValue));
            return configuredClaimValueMatcher.matches()
                    && tokenClaimValues.stream().anyMatch(tokenClaimValue -> pattern.matcher(tokenClaimValue).matches());
        }

        log.debug("A regex is not provided, validating the claim values based on equality.");
        return tokenClaimValues.stream()
                .anyMatch(tokenClaimValue -> StringUtils.equals(accessVerificationClaimValue, tokenClaimValue));
    }

    /**
     * Sends a fault response to the client.
     *
     * @param errorCode   error code of the failure
     * @param messageContext   message context of the request
     * @param errorMessage     error message of the failure
     * @param errorDescription error description of the failure
     */
    private void handleAuthFailure(int errorCode, MessageContext messageContext, String errorMessage,
                                   String errorDescription) {

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        Mediator sequence = messageContext.getSequence(APISecurityConstants.BACKEND_AUTH_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }
        Utils.sendFault(messageContext, HttpStatus.SC_FORBIDDEN);
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
