/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.ApiKeyAuthenticatorUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;

import java.text.ParseException;

/**
 * This class is used to authenticate web socket API requests when using API Key as the authentication mechanism.
 */
public class ApiKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(ApiKeyAuthenticator.class);
    private String apiKey;
    private String[] splitToken;
    private JWSHeader decodedHeader;
    private JWTClaimsSet payload;
    private SignedJWT signedJWT;

    @Override
    public boolean validateToken(InboundMessageContext inboundMessageContext) throws APISecurityException {

        if (InboundWebsocketProcessorUtil.isAuthenticatorEnabled(APIConstants.API_SECURITY_API_KEY,
                inboundMessageContext)) {
            log.debug("ApiKey Authentication initialized");
            try {
                apiKey = inboundMessageContext.getRequestHeaders().get(APIConstants.API_KEY_HEADER_QUERY_PARAM);
                splitToken = apiKey.split("\\.");
                ApiKeyAuthenticatorUtils.validateAPIKeyFormat(splitToken);
                signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                decodedHeader = signedJWT.getHeader();
                payload = signedJWT.getJWTClaimsSet();
                return ApiKeyAuthenticatorUtils.isAPIKey(splitToken, decodedHeader, payload);
            } catch (ParseException e) {
                log.error("Error while parsing API Key", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext)
            throws APISecurityException {

        if (InboundWebsocketProcessorUtil.isAuthenticatorEnabled(APIConstants.API_SECURITY_API_KEY,
                inboundMessageContext)) {
            try {
                String certAlias = decodedHeader.getKeyID();
                String tokenIdentifier = payload.getJWTID();
                String tenantDomain = GatewayUtils.getTenantDomain();
                String apiContext = inboundMessageContext.getApiContext();
                String apiVersion = inboundMessageContext.getVersion();
                String matchingResource = inboundMessageContext.getMatchingResource();
                String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenIdentifier, apiContext, apiVersion,
                        matchingResource, null);
                boolean isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
                boolean isVerified = ApiKeyAuthenticatorUtils.verifyAPIKeySignatureFromTokenCache(isGatewayTokenCacheEnabled,
                        tokenIdentifier, cacheKey, apiKey, splitToken[0]);
                if (!isVerified) {
                    // Not found in cache or caching disabled
                    isVerified = ApiKeyAuthenticatorUtils.verifyAPIKeySignature(signedJWT, certAlias);
                }
                ApiKeyAuthenticatorUtils.addTokenToTokenCache(isGatewayTokenCacheEnabled, tokenIdentifier, isVerified,
                        tenantDomain);

                // If Api Key signature is verified
                if (isVerified) {
                    ExtendedJWTConfigurationDto jwtConfigurationDto = ServiceReferenceHolder.getInstance().
                            getAPIManagerConfiguration().getJwtConfigurationDto();
                    ApiKeyAuthenticatorUtils.overridePayloadFromDataCache(isGatewayTokenCacheEnabled, cacheKey, payload);
                    ApiKeyAuthenticatorUtils.checkTokenExpired(isGatewayTokenCacheEnabled, cacheKey, tokenIdentifier,
                            apiKey, tenantDomain, payload);
                    ApiKeyAuthenticatorUtils.validateAPIKeyRestrictions(payload, inboundMessageContext.getUserIP(),
                            apiContext, apiVersion, inboundMessageContext.getRequestHeaders().
                                    get(APIMgtGatewayConstants.REFERER));
                    net.minidev.json.JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion,
                            payload, splitToken[0]);
                    String endUserToken = ApiKeyAuthenticatorUtils.getEndUserToken(api, jwtConfigurationDto, apiKey,
                            signedJWT, payload, tokenIdentifier, isGatewayTokenCacheEnabled);
                    AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(
                            tokenIdentifier, payload, api, null, endUserToken, null);
                    if (!InboundWebsocketProcessorUtil.validateAuthenticationContext(authenticationContext,
                            inboundMessageContext)) {
                        return InboundWebsocketProcessorUtil.getFrameErrorDTO(
                                WebSocketApiConstants.FrameErrorConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, true);
                    }
                    log.debug("User is authorized to access the resource using Api Key.");
                    InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
                    inboundProcessorResponseDTO.setErrorCode(0);
                    inboundProcessorResponseDTO.setErrorMessage(null);
                    return inboundProcessorResponseDTO;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Api Key signature verification failure. Api Key: " +
                            GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key. Signature verification failed.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } catch (ParseException e) {
                log.error("Error while parsing API Key", e);
                return InboundWebsocketProcessorUtil.getFrameErrorDTO(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, true);
            } catch (APIManagementException e) {
                log.error("Error while setting public cert/private key for backend jwt generation", e);
                return InboundWebsocketProcessorUtil.getFrameErrorDTO(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, true);
            }
        }
        return InboundWebsocketProcessorUtil.getFrameErrorDTO(
                WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_ERROR,
                "Authentication has not enabled for the Authentication type: " + APIConstants.
                        API_SECURITY_API_KEY, true);
    }
}
