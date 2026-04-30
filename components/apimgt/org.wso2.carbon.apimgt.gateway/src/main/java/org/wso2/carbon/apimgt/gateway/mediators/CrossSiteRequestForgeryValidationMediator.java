/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.text.ParseException;
import java.util.Map;

/**
 * Service Implementation for CSRF Token Validation Mediator
 */
public class CrossSiteRequestForgeryValidationMediator extends AbstractMediator {
    private String csrfCookieName;

    public CrossSiteRequestForgeryValidationMediator() {
    }

    /**
     * This method checks whether the JWT token has the CSRF binding claims. If so, it verifies the CSRF token from the
     * cookie in the request headers.
     *
     * @param messageContext This message context contains the request message properties of the relevant API which was
     *                       enabled the CSRF validation mediation in flow.
     */
    public boolean mediate(MessageContext messageContext) {
        AuthenticationContext authContext = (AuthenticationContext) messageContext.getProperty(
                APISecurityUtils.API_AUTH_CONTEXT);
        String jwtToken = authContext.getAccessToken();

        try {
            SignedJWT parsedJWTToken = (SignedJWT) JWTParser.parse(jwtToken);
            JWTClaimsSet payload = parsedJWTToken.getJWTClaimsSet();
            if (payload != null && payload.getStringClaim(APIConstants.BINDING_REF) != null && payload.getStringClaim(
                    APIConstants.BINDING_TYPE) != null && APIConstants.COOKIE.toLowerCase()
                    .equals(payload.getStringClaim(APIConstants.BINDING_TYPE))) {

                log.debug("Verifying CSRF token mismatch");
                validateCsrf(payload, messageContext, jwtToken);
            }
        } catch (ParseException e) {
            String maskedToken = getMaskedToken(jwtToken);
            log.error("Error occurred while retrieving binding claims from payload. Token: " + maskedToken, e);
            messageContext.setProperty("CSRF_ERROR", "General error");
            return false;
        } catch (APISecurityException e) {
            GatewayUtils.handleAuthFailure(messageContext, e, null, null,
                    (String) messageContext.getProperty(APIMgtGatewayConstants.AUTHENTICATORS_CHALLENGE_STRING), null);
            return false;
        }
        return true;
    }

    private void validateCsrf(JWTClaimsSet payload, MessageContext messageContext, String jwtToken)
            throws ParseException, APISecurityException {
        String cookieBindingValue = "";
        boolean isCSRFAttackDetected = true;
        String cookieName = csrfCookieName;
        String cookieBindingRef = payload.getStringClaim(APIConstants.BINDING_REF);
        Map<String, String> headers = extractHeaders(messageContext);
        if (StringUtils.isBlank(cookieName)) {
            cookieName = APIConstants.DEFAULT_BINDING_TYPE;
        }
        if (headers != null && headers.get(APIConstants.COOKIE) != null) {
            String[] cookieArray = headers.get(APIConstants.COOKIE).toString().split(";");
            for (String ele : cookieArray) {
                if (ele.trim().startsWith(cookieName + "=")) {
                    cookieBindingValue = ele.split("=")[1];
                    break;
                }
            }
        }
        if (DigestUtils.md5Hex(cookieBindingValue).equals(cookieBindingRef)) {
            isCSRFAttackDetected = false;
        }
        if (isCSRFAttackDetected) {
            String maskedToken = getMaskedToken(jwtToken);
            log.warn("CSRF attack has been detected " + maskedToken);
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
        }
    }

    private Map<String, String> extractHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, String> headers = (Map) msgContext.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        return headers;
    }

    private String getMaskedToken(String jwtToken) {
        String[] splitToken = jwtToken.split("\\.");
        return GatewayUtils.getMaskedToken(splitToken[0]);
    }

    public void setCsrfCookieName(String csrfCookieName) {
        this.csrfCookieName = csrfCookieName;
    }

    public String getCsrfCookieName() {
        return csrfCookieName;
    }

}