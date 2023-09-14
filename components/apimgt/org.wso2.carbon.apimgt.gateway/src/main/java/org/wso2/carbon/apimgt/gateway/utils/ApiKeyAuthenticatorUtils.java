/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com/).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.cache.Cache;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;

public class ApiKeyAuthenticatorUtils {
    private static final Log log = LogFactory.getLog(ApiKeyAuthenticatorUtils.class);

    /**
     * Get Websocket API Key data from websocket client.
     *
     * @param key           API key
     * @param domain        tenant domain
     * @param apiContextUri API context
     * @param apiVersion    API version
     * @return APIKeyValidationInfoDTO
     * @throws APISecurityException if validation fails
     */
    public static boolean validateAPIKeyFormat(String[] splitToken, JWSHeader decodedHeader, JWTClaimsSet payload)
            throws APISecurityException, ParseException {

        if (splitToken.length != 3) {
            log.error("Api Key does not have the format {header}.{payload}.{signature} ");
            return false;
        }
        // Check if the decoded header contains type as 'JWT'.
        if (!JOSEObjectType.JWT.equals(decodedHeader.getType())) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid Api Key token type. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]));
            }
            log.error("Invalid Api Key token type.");
            return false;
        }
        if (!GatewayUtils.isAPIKey(payload)) {
            log.error("Invalid Api Key. Internal Key Sent");
            return false;
        }
        if (decodedHeader.getKeyID() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid Api Key. Could not find alias in header. Api Key: " +
                        GatewayUtils.getMaskedToken(splitToken[0]));
            }
            log.error("Invalid Api Key. Could not find alias in header");
            return false;
        }
        return true;
    }

    public static boolean verifyAPIKeySignature(boolean isGatewayTokenCacheEnabled, String tokenIdentifier,
                                                String cacheKey, String apiKey, String token, String certAlias,
                                                String tenantDomain) throws APISecurityException {

        boolean isVerified = false;

        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayApiKeyCache().get(tokenIdentifier);
            if (cacheToken != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key retrieved from the Api Key cache.");
                }
                if (getGatewayApiKeyDataCache().get(cacheKey) != null) {
                    // Token is found in the key cache
                    JWTTokenPayloadInfo payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
                    String accessToken = payloadInfo.getAccessToken();
                    isVerified = accessToken.equals(apiKey);
                }
            } else if (getInvalidGatewayApiKeyCache().get(tokenIdentifier) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key retrieved from the invalid Api Key cache. Api Key: " +
                            GatewayUtils.getMaskedToken(token));
                }
                log.error("Invalid Api Key." + GatewayUtils.getMaskedToken(token));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(token));
                }
                log.error("Invalid API Key. " + GatewayUtils.getMaskedToken(token));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid API Key");
            }
        } else {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(token));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(token));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        SignedJWT signedJWT;

        // Not found in cache or caching disabled
        if (!isVerified) {

            if (log.isDebugEnabled()) {
                log.debug("Api Key not found in the cache.");
            }

            try {
                signedJWT = (SignedJWT) JWTParser.parse(apiKey);
            } catch (JSONException | IllegalArgumentException | ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(token), e);
                }
                log.error("Invalid JWT token. Failed to decode the Api Key body.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
            }

            try {
                isVerified = GatewayUtils.verifyTokenSignature(signedJWT, certAlias);
            } catch (APISecurityException e) {
                if (e.getErrorCode() == APISecurityConstants.API_AUTH_INVALID_CREDENTIALS) {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                } else {
                    throw e;
                }
            }

            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                } else {
                    getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    try {
                        // Start super tenant flow
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        // Add token to super tenant token cache
                        if (isVerified) {
                            getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        } else {
                            getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        }
        return isVerified;
    }

    public static AuthenticationContext authenticateUsingAPIKey(boolean isGatewayTokenCacheEnabled, String cacheKey,
                                                       String tokenIdentifier, String tenantDomain, String clientIP,
                                                       String apiContext, String apiVersion, String referer,
                                                       String apiKey, String token, boolean jwtGenerationEnabled,
                                                       ExtendedJWTConfigurationDto jwtConfigurationDto,
                                                       int tenantId, String apiLevelPolicy,
                                                       org.apache.synapse.MessageContext synCtx)
            throws APISecurityException, ParseException, APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Api Key signature is verified.");
        }
        JWTClaimsSet payload;
        SignedJWT signedJWT = null;
        JWTTokenPayloadInfo payloadInfo = null;

        if (isGatewayTokenCacheEnabled && getGatewayApiKeyDataCache().get(cacheKey) != null) {
            payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
        }

        if (isGatewayTokenCacheEnabled && payloadInfo != null) {
            // Api Key is found in the key cache
            payload = payloadInfo.getPayload();
            if (isJwtTokenExpired(payload)) {
                getGatewayApiKeyCache().remove(tokenIdentifier);
                getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                log.error("Api Key is expired");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            validateAPIKeyRestrictions(payload, clientIP, apiContext, apiVersion, referer);
        } else {
            // Retrieve payload from ApiKey
            if (log.isDebugEnabled()) {
                log.debug("ApiKey payload not found in the cache.");
            }
            try {
                signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                payload = signedJWT.getJWTClaimsSet();
            } catch (JSONException | IllegalArgumentException | ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid ApiKey. ApiKey: " + GatewayUtils.getMaskedToken(token));
                }
                log.error("Invalid Api Key. Failed to decode the Api Key body.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
            }
            if (isJwtTokenExpired(payload)) {
                if (isGatewayTokenCacheEnabled) {
                    getGatewayApiKeyCache().remove(tokenIdentifier);
                    getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                }
                log.error("Api Key is expired");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            validateAPIKeyRestrictions(payload, clientIP, apiContext, apiVersion, referer);
            if (isGatewayTokenCacheEnabled) {
                JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                jwtTokenPayloadInfo.setPayload(payload);
                jwtTokenPayloadInfo.setAccessToken(apiKey);
                getGatewayApiKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
            }
        }
        net.minidev.json.JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload,
                token, false);
        if (log.isDebugEnabled()) {
            log.debug("Api Key authentication successful.");
        }

        AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator = null;
        if (jwtConfigurationDto != null) {
            apiMgtGatewayJWTGenerator = ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                    .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
        }

        if (jwtGenerationEnabled) {
            // Set certificate to jwtConfigurationDto
            if (jwtConfigurationDto.isTenantBasedSigningEnabled()) {
                jwtConfigurationDto.setPublicCert(SigningUtil.getPublicCertificate(tenantId));
                jwtConfigurationDto.setPrivateKey(SigningUtil.getSigningKey(tenantId));
            } else {
                jwtConfigurationDto.setPublicCert(ServiceReferenceHolder.getInstance().getPublicCert());
                jwtConfigurationDto.setPrivateKey(ServiceReferenceHolder.getInstance().getPrivateKey());
            }
            // Set ttl to jwtConfigurationDto
            jwtConfigurationDto.setTtl(org.wso2.carbon.apimgt.impl.utils.GatewayUtils.getTtl());

            //setting the jwt configuration dto
            if (apiMgtGatewayJWTGenerator != null) {
                apiMgtGatewayJWTGenerator.setJWTConfigurationDto(jwtConfigurationDto);
            }
        }

        String endUserToken = null;
        if (jwtGenerationEnabled) {
            SignedJWTInfo signedJWTInfo = new SignedJWTInfo(apiKey, signedJWT, payload);
            JWTValidationInfo jwtValidationInfo = getJwtValidationInfo(signedJWTInfo);
            JWTInfoDto jwtInfoDto = GatewayUtils.generateJWTInfoDto(api, jwtValidationInfo,
                    null, apiContext, apiVersion);
            endUserToken = generateAndRetrieveBackendJWTToken(tokenIdentifier, jwtInfoDto, isGatewayTokenCacheEnabled,
                    apiMgtGatewayJWTGenerator);
        }

        return GatewayUtils
                .generateAuthenticationContext(tokenIdentifier, payload, api, apiLevelPolicy, endUserToken,
                        synCtx);
    }

    private static String generateAndRetrieveBackendJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto,
                                                             boolean isGatewayTokenCacheEnabled,
                                                             AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator)
            throws APISecurityException {

        String endUserToken = null;
        boolean valid = false;
        String jwtTokenCacheKey = jwtInfoDto.getApiContext().concat(":").concat(jwtInfoDto.getVersion()).
                concat(":").concat(tokenSignature);
        if (isGatewayTokenCacheEnabled) {
            Object token = getGatewayApiKeyCache().get(jwtTokenCacheKey);
            if (token != null) {
                endUserToken = (String) token;
                String[] splitToken = ((String) token).split("\\.");
                JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                long exp = payload.getLong("exp");
                long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                valid = (exp - System.currentTimeMillis() > timestampSkew);
            }
            if (StringUtils.isEmpty(endUserToken) || !valid) {
                try {
                    endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
                    getGatewayApiKeyCache().put(jwtTokenCacheKey, endUserToken);
                } catch (JWTGeneratorException e) {
                    log.error("Error while Generating Backend JWT", e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
                }
            }
        } else {
            try {
                endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
            } catch (JWTGeneratorException e) {
                log.error("Error while Generating Backend JWT", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        return endUserToken;
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param payload The payload of the JWT token
     * @return returns true if the JWT token is expired
     */
    private static boolean isJwtTokenExpired(JWTClaimsSet payload) {

        int timestampSkew = (int) OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();

        DefaultJWTClaimsVerifier jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier();
        jwtClaimsSetVerifier.setMaxClockSkew(timestampSkew);
        try {
            jwtClaimsSetVerifier.verify(payload);
            if (log.isDebugEnabled()) {
                log.debug("Token is not expired. User: " + payload.getSubject());
            }
        } catch (BadJWTException e) {
            if ("Expired JWT".equals(e.getMessage())) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getSubject());
        }
        return false;
    }

    private static void validateAPIKeyRestrictions(JWTClaimsSet payload, String clientIP, String apiContext,
                                                  String apiVersion, String referer) throws APISecurityException {

        String permittedIPList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP) != null) {
            permittedIPList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP);
        }

        if (StringUtils.isNotEmpty(permittedIPList)) {
            // Validate client IP against permitted IPs
            if (StringUtils.isNotEmpty(clientIP)) {
                for (String restrictedIP : permittedIPList.split(",")) {
                    if (APIUtil.isIpInNetwork(clientIP, restrictedIP.trim())) {
                        // Client IP is allowed
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    if (StringUtils.isNotEmpty(clientIP)) {
                        log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                " is not permitted for client with IP: " + clientIP);
                    }
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "Access forbidden for the invocations");
            }
        }

        String permittedRefererList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER) != null) {
            permittedRefererList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER);
        }

        if (StringUtils.isNotEmpty(permittedRefererList)) {
            // Validate http referer against the permitted referrers
            if (StringUtils.isNotEmpty(referer)) {
                for (String restrictedReferer : permittedRefererList.split(",")) {
                    String restrictedRefererRegExp = restrictedReferer.trim()
                            .replace("*", "[^ ]*");
                    if (referer.matches(restrictedRefererRegExp)) {
                        // Referer is allowed
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    if (StringUtils.isNotEmpty(referer)) {
                        log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                " is not permitted for referer: " + referer);
                    }
                }
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                    "Access forbidden for the invocations");
        }
    }

    private static JWTValidationInfo getJwtValidationInfo(SignedJWTInfo signedJWTInfo) {
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setClaims(new HashMap<>(signedJWTInfo.getJwtClaimsSet().getClaims()));
        jwtValidationInfo.setUser(signedJWTInfo.getJwtClaimsSet().getSubject());
        return jwtValidationInfo;
    }

    //first level cache
    private static Cache getGatewayApiKeyCache() {
        return CacheProvider.getGatewayApiKeyCache();
    }

    //second level cache
    private static Cache getGatewayApiKeyDataCache() {
        return CacheProvider.getGatewayApiKeyDataCache();
    }

    private static Cache getInvalidGatewayApiKeyCache() {
        return CacheProvider.getInvalidGatewayApiKeyCache();
    }
}
