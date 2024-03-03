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

package org.wso2.carbon.apimgt.gateway.utils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
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
import java.util.Base64;
import java.util.HashMap;

/**
 * This class contains the common utility methods required for API Key authentication.
 */
public class ApiKeyAuthenticatorUtils {
    private static final Log log = LogFactory.getLog(ApiKeyAuthenticatorUtils.class);

    /**
     * This method is used to validate the format of the API Key.
     *
     * @param splitToken The String array of split API Key.
     * @throws APISecurityException If the API Key does not have the format {header}.{payload}.{signature}
     */
    public static void validateAPIKeyFormat(String[] splitToken) throws APISecurityException {
        if (splitToken.length != 3) {
            log.error("Api Key does not have the format {header}.{payload}.{signature} ");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
    }

    /**
     * This method is used to check whether the received token is an APIKey or not.
     *
     * @param splitToken    The String array of split API Key.
     * @param decodedHeader The decoded header of the API Key.
     * @param payload       The payload of the API Key.
     * @return true if the received token is an API Key.
     */
    public static boolean isAPIKey(String[] splitToken, JWSHeader decodedHeader, JWTClaimsSet payload) {

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

    /**
     * This method is used to verify the signature of the API Key. It uses the API Key cache to check the received APIKey
     * is in it. If the APIKey is in the cache, then the signature of the APIKey is verified.
     *
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @param tokenIdentifier            The token identifier.
     * @param cacheKey                   The cache key.
     * @param apiKey                     The API Key.
     * @param header                     The API Key header.
     * @return true if the API Key is valid.
     * @throws APISecurityException If the key is not valid and found in the invalid key cache or revoke map.
     */
    public static boolean verifyAPIKeySignatureFromTokenCache(boolean isGatewayTokenCacheEnabled, String tokenIdentifier,
                                                              String cacheKey, String apiKey, String header)
            throws APISecurityException {

        boolean isVerified = false;
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayApiKeyCache().get(tokenIdentifier);
            if (cacheToken != null) {
                log.debug("Api Key retrieved from the Api Key cache.");
                if (getGatewayApiKeyDataCache().get(cacheKey) != null) {
                    // Token is found in the key cache
                    JWTTokenPayloadInfo payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
                    String accessToken = payloadInfo.getAccessToken();
                    isVerified = accessToken.equals(apiKey);
                }
            } else if (getInvalidGatewayApiKeyCache().get(tokenIdentifier) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key retrieved from the invalid Api Key cache. Api Key: " +
                            GatewayUtils.getMaskedToken(header));
                }
                log.error("Invalid Api Key." + GatewayUtils.getMaskedToken(header));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } else {
                checkJWTTokenSignatureExistsInRevokedMap(tokenIdentifier, header);
            }
        } else {
            checkJWTTokenSignatureExistsInRevokedMap(tokenIdentifier, header);
        }
        return isVerified;
    }

    /**
     * This method is used to verify the signature of the API Key. This method will be called if the signature verification
     * is not successful using the API Key cache.
     *
     * @param signedJWT The signed JWT.
     * @param certAlias The key ID of the decoded header.
     * @return true if the signature of the API Key is verified.
     * @throws APISecurityException If an error occurs while verifying the signature of the API Key.
     */
    public static boolean verifyAPIKeySignature(SignedJWT signedJWT, String certAlias) throws APISecurityException {

        log.debug("Api Key not found in the cache.");
        boolean isVerified;
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
        return isVerified;
    }

    /**
     * This method is used to add the API Key to the gateway API Key cache after signature verification.
     * If the API Key is not valid, then it is added to the invalid gateway API Key cache.
     *
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @param tokenIdentifier            The token identifier.
     * @param isVerified                 Whether the signature of the API Key is verified or not.
     * @param tenantDomain               The tenant domain.
     */
    public static void addTokenToTokenCache(boolean isGatewayTokenCacheEnabled, String tokenIdentifier,
                                            boolean isVerified, String tenantDomain) {

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

    /**
     * This method is used to override the payload of the API Key from the cache.
     * If the token is found in the cache, then the payload is retrieved from the cache and override the existing payload.
     *
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @param cacheKey                   The cache key.
     * @param payload                    The payload of the API Key.
     */
    public static void overridePayloadFromDataCache(boolean isGatewayTokenCacheEnabled, String cacheKey,
                                                    JWTClaimsSet payload) {

        if (isGatewayTokenCacheEnabled && getGatewayApiKeyDataCache().get(cacheKey) != null) {
            JWTTokenPayloadInfo payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
            if (payloadInfo != null) {
                payload = payloadInfo.getPayload();
            }
        }
    }

    /**
     * This method is used to check whether the token is expired or not.
     *
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @param cacheKey                   The cache key.
     * @param tokenIdentifier            The token identifier.
     * @param apiKey                     The API Key.
     * @param tenantDomain               The tenant domain.
     * @param payload                    The payload of the API Key.
     * @throws APISecurityException If the token is expired.
     */
    public static void checkTokenExpired(boolean isGatewayTokenCacheEnabled, String cacheKey, String tokenIdentifier,
                                         String apiKey, String tenantDomain, JWTClaimsSet payload)
            throws APISecurityException {

        log.debug("Api Key signature is verified and started checking whether the token is expired or not.");
        if (isJwtTokenExpired(payload)) {
            if (isGatewayTokenCacheEnabled) {
                getGatewayApiKeyCache().remove(tokenIdentifier);
                getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
            }
            log.error("Api Key is expired");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        }
        if (isGatewayTokenCacheEnabled) {
            JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
            jwtTokenPayloadInfo.setPayload(payload);
            jwtTokenPayloadInfo.setAccessToken(apiKey);
            getGatewayApiKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
        }
    }

    /**
     * This method is used to validate the API Key against the given restrictions.
     *
     * @param payload    The payload of the API Key.
     * @param clientIP   The client IP.
     * @param apiContext The API context.
     * @param apiVersion The API version.
     * @param referer    The http referer.
     * @throws APISecurityException If the API Key is not allowed to access the API.
     */
    public static void validateAPIKeyRestrictions(JWTClaimsSet payload, String clientIP, String apiContext,
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

    /**
     * This method is used to get the end user token for the API Key when backend JWT token generation is needed.
     *
     * @param api                        The API object returned after validating API subscription.
     * @param jwtConfigurationDto        The JWT configuration DTO.
     * @param apiKey                     The API Key.
     * @param signedJWT                  The signed JWT.
     * @param payload                    The payload of the API Key.
     * @param tokenIdentifier            The token identifier.
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @return The end user token.
     * @throws APIManagementException If an error occurs while getting Public Certificate or Signing Key.
     * @throws APISecurityException   If an error occurs while generating the backend JWT token.
     */
    public static String getEndUserToken(net.minidev.json.JSONObject api,
                                         ExtendedJWTConfigurationDto jwtConfigurationDto, String apiKey,
                                         SignedJWT signedJWT, JWTClaimsSet payload, String tokenIdentifier,
                                         boolean isGatewayTokenCacheEnabled) throws APIManagementException,
            APISecurityException {

        AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator = null;
        boolean jwtGenerationEnabled = false;
        int tenantId = APIUtil.getTenantIdFromTenantDomain(api.getAsString(APIConstants.JwtTokenConstants.
                SUBSCRIBER_TENANT_DOMAIN));
        if (jwtConfigurationDto != null) {
            apiMgtGatewayJWTGenerator = ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                    .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
            jwtGenerationEnabled = jwtConfigurationDto.isEnabled();
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
                    api.getAsString(APIConstants.JwtTokenConstants.API_CONTEXT),
                    api.getAsString(APIConstants.JwtTokenConstants.API_VERSION));
            endUserToken = generateAndRetrieveBackendJWTToken(tokenIdentifier, jwtInfoDto, isGatewayTokenCacheEnabled,
                    apiMgtGatewayJWTGenerator);
        }
        return endUserToken;
    }

    /**
     * This method is used to generate and retrieve the backend JWT token.
     *
     * @param tokenSignature             The token signature/identifier.
     * @param jwtInfoDto                 The JWT info DTO.
     * @param isGatewayTokenCacheEnabled Whether the gateway token cache is enabled or not.
     * @param apiMgtGatewayJWTGenerator  The API Manager Gateway JWT generator.
     * @return The backend JWT token.
     * @throws APISecurityException If an error occurs while generating the backend JWT token.
     */
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
            jwtClaimsSetVerifier.verify(payload, null);
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

    /**
     * This method is used to get the JWT validation info from the signed JWT info.
     *
     * @param signedJWTInfo The signed JWT info.
     * @return The JWT validation info.
     */
    private static JWTValidationInfo getJwtValidationInfo(SignedJWTInfo signedJWTInfo) {
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setClaims(new HashMap<>(signedJWTInfo.getJwtClaimsSet().getClaims()));
        jwtValidationInfo.setUser(signedJWTInfo.getJwtClaimsSet().getSubject());
        return jwtValidationInfo;
    }

    /**
     * This method is used to check whether the token signature exists in the revoked JWT token map.
     *
     * @param tokenIdentifier The token identifier.
     * @param header          The API Key header.
     * @throws APISecurityException If the token signature exists in the revoked JWT token map.
     */
    private static void checkJWTTokenSignatureExistsInRevokedMap(String tokenIdentifier, String header) throws
            APISecurityException {
        if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
            if (log.isDebugEnabled()) {
                log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                        getMaskedToken(header));
            }
            log.error("Invalid API Key. " + GatewayUtils.getMaskedToken(header));
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid API Key");
        }
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
