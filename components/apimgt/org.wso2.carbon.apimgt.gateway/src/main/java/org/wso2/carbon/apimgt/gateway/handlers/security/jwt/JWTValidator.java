/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.jwt;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.Base64;
import java.util.HashMap;
import javax.cache.Cache;

/**
 * A Validator class to validate JWT tokens in an API request.
 */
public class JWTValidator {

    private static final Log log = LogFactory.getLog(JWTValidator.class);

    private String apiLevelPolicy;
    private boolean isGatewayTokenCacheEnabled;
    private APIKeyValidator apiKeyValidator;
    private boolean jwtGenerationEnabled;
    private AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator;

    public JWTValidator(String apiLevelPolicy, APIKeyValidator apiKeyValidator) {
        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        this.apiKeyValidator = apiKeyValidator;
        JWTConfigurationDto jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
        jwtGenerationEnabled  = jwtConfigurationDto.isEnabled();
        apiMgtGatewayJWTGenerator = ServiceReferenceHolder.getInstance().getAPIMgtGatewayJWTGenerator();

    }

    /**
     * Authenticates the given request with a JWT token to see if an API consumer is allowed to access
     * a particular API or not.
     *
     * @param jwtToken The JWT token sent with the API request
     * @param synCtx   The message to be authenticated
     * @param openAPI  The OpenAPI object of the invoked API
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticate(String jwtToken, MessageContext synCtx, OpenAPI openAPI)
            throws APISecurityException {
        String[] splitToken = jwtToken.split("\\.");

        JSONObject header = null;
        JSONObject payload = null;
        boolean isVerified = false;

        String tokenSignature = splitToken[2];

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenSignature, apiContext, apiVersion, matchingResource, httpMethod);
        String tenantDomain = GatewayUtils.getTenantDomain();

        // Validate from cache
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenSignature);
            if (cacheToken != null) {
                log.debug("Token retrieved from the token cache.");
                isVerified = true;
            } else if (getInvalidTokenCache().get(tokenSignature) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + GatewayUtils
                            .getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
            // Check revoked map.
            else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        } else {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        if (!isVerified) {
            log.debug("Token not found in the caches and revoked jwt token map.");
            try {
                header = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[0])));
                payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
            } catch (JSONException | IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid JWT token. Token: " + GatewayUtils.getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. Failed to decode the token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token. Failed to decode the token.", e);
            }
            log.debug("Verifying signature of JWT");
            String certificateAlias = APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS;
            if (header.has(APIConstants.JwtTokenConstants.KEY_ID)) {
                certificateAlias = header.getString(APIConstants.JwtTokenConstants.KEY_ID);
            }

            isVerified = GatewayUtils.verifyTokenSignature(splitToken, certificateAlias);
            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayTokenCache().put(tokenSignature, tenantDomain);
                } else {
                    getInvalidTokenCache().put(tokenSignature, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
                    //to remove the entry when the need occurs to clear this particular cache entry.
                    try {
                        // Start super tenant flow
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        // Add token to super tenant token cache
                        if (isVerified) {
                            getGatewayTokenCache().put(tokenSignature, tenantDomain);
                        } else {
                            getInvalidTokenCache().put(tokenSignature, tenantDomain);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            }
        }

        // If token signature is verified
        if (isVerified) {
            log.debug("Token signature is verified.");
            if (isGatewayTokenCacheEnabled && getGatewayKeyCache().get(cacheKey) != null) {
                // Token is found in the key cache
                payload = (JSONObject) getGatewayKeyCache().get(cacheKey);
                checkTokenExpiration(tokenSignature, payload, tenantDomain);
            } else {
                // Retrieve payload from token
                log.debug("Token payload not found in the cache.");
                if (payload == null) {
                    try {
                        payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                    } catch (JSONException | IllegalArgumentException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Token decryption failure when retrieving payload. Token: "
                                    + GatewayUtils.getMaskedToken(splitToken), e);
                        }
                        log.error("Invalid JWT token. Failed to decode the token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                }
                checkTokenExpiration(tokenSignature, payload, tenantDomain);
                validateScopes(synCtx, openAPI, payload);

                if (isGatewayTokenCacheEnabled) {
                    getGatewayKeyCache().put(cacheKey, payload);
                }
            }

            JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, true);

            /*
             * Set api.ut.apiPublisher of the subscribed api to the message context.
             * This is necessary for the functionality of Publisher alerts.
             * */
            if (api != null) {
                synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, api.get("publisher"));
            } else {
                boolean validateSubscriptionViaKM = Boolean.parseBoolean(
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                                .getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION)
                );
                if (validateSubscriptionViaKM) {
                    log.debug("Begin subscription validation via Key Manager");
                    APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validateSubscriptionUsingKeyManager(synCtx, payload);

                    if (log.isDebugEnabled()) {
                        log.debug("Subscription validation via Key Manager. Status: " +
                                apiKeyValidationInfoDTO.isAuthorized());
                    }
                    if (apiKeyValidationInfoDTO.isAuthorized()) {
                        synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiKeyValidationInfoDTO.getApiPublisher());
                        log.debug("JWT authentication successful.");
                        String endUserToken = null;
                        if (jwtGenerationEnabled) {
                            JWTInfoDto jwtInfoDto =
                                    GatewayUtils.generateJWTInfoDto(payload, api, apiKeyValidationInfoDTO, synCtx);
                            endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
                        }
                        return GatewayUtils.generateAuthenticationContext(tokenSignature, payload, null,
                                apiKeyValidationInfoDTO, getApiLevelPolicy(), endUserToken, true);
                    } else {
                        log.debug("User is NOT authorized to access the Resource. API Subscription validation failed.");
                        throw new APISecurityException(apiKeyValidationInfoDTO.getValidationStatus(),
                                "User is NOT authorized to access the Resource. API Subscription validation failed.");
                    }
                }
                log.debug("Ignored subscription validation");
            }

            log.debug("JWT authentication successful.");
            String endUserToken = null;
            if (jwtGenerationEnabled) {
                JWTInfoDto jwtInfoDto = GatewayUtils.generateJWTInfoDto(payload, api, null, synCtx);
                endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
            }
            return GatewayUtils
                    .generateAuthenticationContext(tokenSignature, payload, api, null, getApiLevelPolicy(),
                            endUserToken, true);
        }
        if (log.isDebugEnabled()) {
            log.debug("Token signature verification failure. Token: " + GatewayUtils.getMaskedToken(splitToken));
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                "Invalid JWT token. Signature verification failed.");
    }

    private String generateAndRetrieveJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto)
            throws APISecurityException {

        String endUserToken = null;
        boolean valid = false;
        if (isGatewayTokenCacheEnabled) {
            Object token = getGatewayJWTTokenCache().get(tokenSignature);
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
                    getGatewayJWTTokenCache().put(tokenSignature, endUserToken);
                } catch (APIManagementException e) {
                    log.error("Error while Generating Backend JWT", e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
                }
            }
        } else {
            try {
                endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
            } catch (APIManagementException e) {
                log.error("Error while Generating Backend JWT", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        return endUserToken;
    }

    private APIKeyValidationInfoDTO validateSubscriptionUsingKeyManager(MessageContext synCtx, JSONObject payload)
            throws APISecurityException {
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String consumerkey = null;
        if (payload.has(APIConstants.JwtTokenConstants.CONSUMER_KEY)) {
            consumerkey = payload.getString(APIConstants.JwtTokenConstants.CONSUMER_KEY);
        } else if (payload.has(APIConstants.JwtTokenConstants.AUTHORIZED_PARTY)) {
            consumerkey = payload.getString(APIConstants.JwtTokenConstants.AUTHORIZED_PARTY);
        }
        if (consumerkey != null) {
            return apiKeyValidator.validateSubscription(apiContext, apiVersion, consumerkey);
        }
        log.debug("Cannot call Key Manager to validate subscription. " +
                "Payload of the token does not contain the Authorized party - the party to which the ID Token was issued");
        throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
    }

    /**
     * Authenticates the given WebSocket handshake request with a JWT token to see if an API consumer is allowed to
     * access a particular API or not.
     *
     * @param jwtToken   The JWT token sent with the API request
     * @param apiContext The context of the invoked API
     * @param apiVersion The version of the invoked API
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticateForWebSocket(String jwtToken, String apiContext, String apiVersion)
            throws APISecurityException {

        String[] splitToken = jwtToken.split("\\.");

        JSONObject payload = null;
        boolean isVerified = false;

        String tokenSignature = splitToken[2];
        String tenantDomain = GatewayUtils.getTenantDomain();

        // Validate from cache
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenSignature);
            if (cacheToken != null) {
                log.debug("Token retrieved from the token cache.");
                isVerified = true;
            } else if (getInvalidTokenCache().get(tokenSignature) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + GatewayUtils
                            .getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
            // Check revoked map.
            else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        } else {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        if (!isVerified) {
            log.debug("Token not found in the caches and revoked jwt token map.");
            try {
                payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
            } catch (JSONException | IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid JWT token. Token: " + GatewayUtils.getMaskedToken(splitToken));
                }
                log.error("Invalid JWT token. Failed to decode the token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token. Failed to decode the token.", e);
            }
            log.debug("Verifying signature of JWT");
            isVerified = GatewayUtils.verifyTokenSignature(splitToken, APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS);
            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayTokenCache().put(tokenSignature, tenantDomain);
                } else {
                    getInvalidTokenCache().put(tokenSignature, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    //Add the tenant domain as a reference to the super tenant cache so we know from which tenant cache
                    //to remove the entry when the need occurs to clear this particular cache entry.
                    try {
                        // Start super tenant flow
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        // Add token to super tenant token cache
                        if (isVerified) {
                            getGatewayTokenCache().put(tokenSignature, tenantDomain);
                        } else {
                            getInvalidTokenCache().put(tokenSignature, tenantDomain);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            }
        }

        String cacheKey = WebsocketUtil.getAccessTokenCacheKey(tokenSignature, apiContext);

        // If token signature is verified
        if (isVerified) {
            log.debug("Token signature is verified.");
            if (isGatewayTokenCacheEnabled && getGatewayKeyCache().get(cacheKey) != null) {
                // Token is found in the key cache
                payload = (JSONObject) getGatewayKeyCache().get(cacheKey);
                checkTokenExpiration(tokenSignature, payload, tenantDomain);
            } else {
                // Retrieve payload from token
                log.debug("Token payload not found in the cache.");
                if (payload == null) {
                    try {
                        payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                    } catch (JSONException | IllegalArgumentException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Token decryption failure when retrieving payload. Token: "
                                    + GatewayUtils.getMaskedToken(splitToken), e);
                        }
                        log.error("Invalid JWT token. Failed to decode the token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                }
                checkTokenExpiration(tokenSignature, payload, tenantDomain);

                if (isGatewayTokenCacheEnabled) {
                    getGatewayKeyCache().put(cacheKey, payload);
                }
            }

            JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, true);

            log.debug("JWT authentication successful.");
            String endUserToken = null;
            if (jwtGenerationEnabled) {
                JWTInfoDto jwtInfoDto = GatewayUtils.generateJWTInfoDto(payload, api, null, apiContext, apiVersion);
                endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
            }
            return GatewayUtils.generateAuthenticationContext(tokenSignature, payload, api, null, getApiLevelPolicy()
                    , endUserToken, true);
        }
        if (log.isDebugEnabled()) {
            log.debug("Token signature verification failure. Token: " + GatewayUtils.getMaskedToken(splitToken));
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                "Invalid JWT token. Signature verification failed.");
    }

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes specified
     * in the JWT token payload.
     *
     * @param synCtx  The message to be authenticated
     * @param openAPI The OpenAPI object of the invoked API
     * @param payload The payload of the JWT token
     * @throws APISecurityException in case of scope validation failure
     */
    private void validateScopes(MessageContext synCtx, OpenAPI openAPI, JSONObject payload)
            throws APISecurityException {
        if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            HashMap<String, String>  operationScopeMappingList =
                    (HashMap<String, String>) synCtx.getProperty(APIConstants.SCOPE_OPERATION_MAPPING);
            String[] operationList = ((String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).split(",");
            for (String operation: operationList) {
                String operationScope = operationScopeMappingList.get(operation);
                checkTokenWithTheScope(operation, operationScope, payload);
            }
        } else {
            String resource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            String resourceScope = OpenAPIUtils.getScopesOfResource(openAPI, synCtx);
            checkTokenWithTheScope(resource, resourceScope, payload);
        }
    }

    private void checkTokenWithTheScope(String resource, String resourceScope, JSONObject payload) throws APISecurityException {
        if (StringUtils.isNotBlank(resourceScope)) {
            if (!payload.has(APIConstants.JwtTokenConstants.SCOPE)) {
                log.error("Scopes not found in the token.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            String[] tokenScopes = payload.getString(APIConstants.JwtTokenConstants.SCOPE)
                    .split(APIConstants.JwtTokenConstants.SCOPE_DELIMITER);

            boolean scopeFound = false;

            for (String scope : tokenScopes) {
                if (scope.trim().equals(resourceScope)) {
                    scopeFound = true;
                    break;
                }
            }
            if (!scopeFound) {
                if (log.isDebugEnabled()) {
                    log.debug("Scope validation failed. User: " +
                            payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
                }
                log.error("Scope validation failed.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            if (log.isDebugEnabled()) {
                log.debug("Scope validation successful for the resource: " + resource + ", Resource Scope: " + resourceScope
                        + ", User: " + payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
            }
        }
        log.debug("No scopes assigned to the resource: " + resource);
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param tokenSignature The signature of the JWT token
     * @param payload        The payload of the JWT token
     * @param tenantDomain   The tenant domain from which the token cache is retrieved
     * @throws APISecurityException if the token is expired
     */
    private void checkTokenExpiration(String tokenSignature, JSONObject payload, String tenantDomain) throws APISecurityException {
        // Check whether the token is expired or not.
        long issuedTime = payload.getLong(APIConstants.JwtTokenConstants.ISSUED_TIME) * 1000;
        long expiredTime = payload.getLong(APIConstants.JwtTokenConstants.EXPIRY_TIME) * 1000;
        long validityPeriod = expiredTime - issuedTime;
        long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
        long currentTime = System.currentTimeMillis();

        //If the validity period is not a never expiring value
        if (validityPeriod != Long.MAX_VALUE && (currentTime - timestampSkew) > validityPeriod) {
            if ((currentTime - timestampSkew) > expiredTime) {
                if (isGatewayTokenCacheEnabled) {
                    getGatewayTokenCache().remove(tokenSignature);
                    getGatewayJWTTokenCache().remove(tokenSignature);
                    getInvalidTokenCache().put(tokenSignature, tenantDomain);
                }
                log.error("JWT token is expired");
                throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                        "JWT token is expired");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
        }
    }

    private Cache getGatewayTokenCache() {
        return CacheProvider.getGatewayTokenCache();
    }

    private Cache getInvalidTokenCache() {
        return CacheProvider.getInvalidTokenCache();
    }

    private Cache getGatewayKeyCache() {
        return CacheProvider.getGatewayKeyCache();
    }

    private Cache getGatewayJWTTokenCache() {
        return CacheProvider.getGatewayJWTTokenCache();
    }

    private String getApiLevelPolicy() {
        return apiLevelPolicy;
    }
}
