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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.ApiKeyAuthenticatorUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private boolean isJwtApiKey;

    @Override
    public boolean validateToken(InboundMessageContext inboundMessageContext) throws APISecurityException {

        if (InboundWebsocketProcessorUtil.isAuthenticatorEnabled(APIConstants.API_SECURITY_API_KEY,
                inboundMessageContext)) {
            log.debug("ApiKey Authentication initialized");
            try {
                apiKey = inboundMessageContext.getRequestHeaders().get(APIConstants.API_KEY_HEADER_QUERY_PARAM);
                if (StringUtils.isNotEmpty(apiKey) && apiKey.contains(APIConstants.DOT)) {
                    splitToken = apiKey.split("\\.");
                    if (splitToken.length == 3) {
                        ApiKeyAuthenticatorUtils.validateAPIKeyFormat(splitToken);
                        signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                        decodedHeader = signedJWT.getHeader();
                        payload = signedJWT.getJWTClaimsSet();
                        isJwtApiKey = ApiKeyAuthenticatorUtils.isAPIKey(splitToken, decodedHeader, payload);
                        return true; // Proceed to authenticate(); it decides JWT vs opaque path
                    }
                }
                isJwtApiKey = false;
                return StringUtils.isNotBlank(apiKey); // Opaque candidate
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
                String tenantDomain = GatewayUtils.getTenantDomain();
                String apiContext = inboundMessageContext.getApiContext();
                String apiVersion = inboundMessageContext.getVersion();
                String matchingResource = inboundMessageContext.getMatchingResource();
                boolean isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
                if (isJwtApiKey) {
                    String certAlias = decodedHeader.getKeyID();
                    String tokenIdentifier = payload.getJWTID();
                    String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenIdentifier, apiContext, apiVersion,
                            matchingResource, null);
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
                        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = GatewayUtils.validateAPISubscription(apiContext, apiVersion,
                                payload, splitToken[0]);
                        String endUserToken = ApiKeyAuthenticatorUtils.getEndUserToken(apiKeyValidationInfoDTO, jwtConfigurationDto,
                                apiKey, signedJWT, payload, tokenIdentifier, apiContext, apiVersion, isGatewayTokenCacheEnabled);
                        AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(tokenIdentifier,
                                payload, apiKeyValidationInfoDTO, endUserToken);
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
                } else {
                    AuthenticationContext opaqueApiKeyAuthenticationContext = validateOpaqueApiKey(apiKey, apiContext,
                            apiVersion, tenantDomain, inboundMessageContext.getUserIP(),
                            inboundMessageContext.getRequestHeaders().get(APIMgtGatewayConstants.REFERER));
                    if (!InboundWebsocketProcessorUtil.validateAuthenticationContext(opaqueApiKeyAuthenticationContext,
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

    /**
     * Validate an opaque API key and check subscription to the given API.
     *
     * @param apiKey     The opaque API key from the request
     * @param apiContext The API context
     * @param apiVersion The API version
     * @param tenantDomain Tenant domain
     * @param ip Ip
     * @param referrer Referer
     * @return AuthenticationContext Authentication context with the API key validation info
     * @throws APIManagementException if an error occurs
     */
    private AuthenticationContext validateOpaqueApiKey(String apiKey, String apiContext, String apiVersion, String tenantDomain,
                                                       String ip, String referrer)
            throws APISecurityException, APIManagementException {

        // Hash the provided API key
        String apiKeyHash = APIUtil.sha256Hash(apiKey);
        String lookupKey = apiKeyHash;
        String endUserToken = null;
        ExtendedJWTConfigurationDto jwtConfigurationDto = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration().getJwtConfigurationDto();
        APIKeyInfo apiKeyInfo;
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
        if (RevokedJWTDataHolder.getInstance().isApiKeyExistsInRevokedMap(apiKeyHash)) {
            if (log.isDebugEnabled()) {
                log.debug("Api Key is revoked already.");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE,
                    APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE_MESSAGE);
        }
        boolean isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        boolean isVerified = ApiKeyAuthenticatorUtils.verifyAPIKeyHashFromTokenCache(isGatewayTokenCacheEnabled,
                apiKeyHash, apiKey);
        apiKeyInfo = DataHolder.getInstance().getOpaqueAPIKeyInfo(lookupKey);
        // Not found in cache or caching disabled
        if (apiKeyInfo == null || apiKeyInfo.getApiKeyHash() == null ||
                !APIConstants.NotificationEvent.ACTIVE.equals(apiKeyInfo.getStatus())) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid Api Key. Active API key information not available.");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                    APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
        }
        if (!isVerified) {
            // Check whether the provided API key is already there in the stored list and return false otherwise
            if (!MessageDigest.isEqual(
                    apiKeyHash.getBytes(StandardCharsets.UTF_8),
                    apiKeyInfo.getApiKeyHash().getBytes(StandardCharsets.UTF_8))) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key. API key hash is not matched.");
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
            isVerified = true;
        }
        // If Api Key is verified
        if (isVerified) {
            // Check Api key expiry
            ApiKeyAuthenticatorUtils.checkApiKeyExpired(isGatewayTokenCacheEnabled, apiKeyHash, tenantDomain, apiKeyInfo);
            // Validate subscriptions
            apiKeyValidationInfoDTO = GatewayUtils.validateAPISubscription(apiContext, apiVersion, apiKeyInfo.getKeyType(),
                    apiKeyInfo.getAppId(), apiKey);
            ApiKeyAuthenticatorUtils.validateAPIKeyRestrictions(ip, apiContext, apiVersion, referrer,
                    apiKeyInfo.getAdditionalProperties());
            endUserToken = ApiKeyAuthenticatorUtils.getEndUserToken(apiKeyValidationInfoDTO,
                    jwtConfigurationDto, apiKey, null, null, apiKeyHash, apiContext, apiVersion,
                    isGatewayTokenCacheEnabled);
            if (apiKeyValidationInfoDTO != null && apiKeyValidationInfoDTO.isAuthorized()) {
                if (log.isDebugEnabled()) {
                    log.debug("User is subscribed to the API: " + apiContext + ", " +
                            "version: " + apiVersion + ". Token: " + apiKeyInfo.getKeyName());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("User is not subscribed to access the API: " + apiContext +
                            ", version: " + apiVersion + ". Token: " + apiKeyInfo.getKeyName());
                }
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        }
        ApiKeyAuthenticatorUtils.addTokenToTokenCache(isGatewayTokenCacheEnabled, apiKeyHash, isVerified,
                tenantDomain);
        ApiKeyAuthenticatorUtils.updateApiKeyLastUsedTime(apiKeyHash, tenantDomain);
        // Set and return auth context
        return GatewayUtils.generateAuthenticationContext(apiKey, null, apiKeyValidationInfoDTO, endUserToken);
    }
}
