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

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
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
    JWTConfigurationDto jwtConfigurationDto;
    JWTValidationService jwtValidationService;

    public JWTValidator(String apiLevelPolicy, APIKeyValidator apiKeyValidator) {

        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        this.apiKeyValidator = apiKeyValidator;
        jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
        jwtGenerationEnabled = jwtConfigurationDto.isEnabled();
        apiMgtGatewayJWTGenerator =
                ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                        .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
        jwtValidationService = ServiceReferenceHolder.getInstance().getJwtValidationService();
    }

    protected JWTValidator(String apiLevelPolicy, boolean isGatewayTokenCacheEnabled,
                           APIKeyValidator apiKeyValidator, boolean jwtGenerationEnabled,
                           AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator,
                           JWTConfigurationDto jwtConfigurationDto,
                           JWTValidationService jwtValidationService) {

        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = isGatewayTokenCacheEnabled;
        this.apiKeyValidator = apiKeyValidator;
        this.jwtGenerationEnabled = jwtGenerationEnabled;
        this.apiMgtGatewayJWTGenerator = apiMgtGatewayJWTGenerator;
        this.jwtConfigurationDto = jwtConfigurationDto;
        this.jwtValidationService = jwtValidationService;
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
    public AuthenticationContext authenticate(SignedJWT jwtToken, MessageContext synCtx, OpenAPI openAPI)
            throws APISecurityException {

        String tokenSignature = jwtToken.getSignature().toString();
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        String jwtHeader = jwtToken.getHeader().toString();
        if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
            if (log.isDebugEnabled()) {
                log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                        getMaskedToken(jwtHeader));
            }
            log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(jwtHeader));
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token");
        }
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(tokenSignature, apiContext, apiVersion, matchingResource, httpMethod);

        JWTValidationInfo jwtValidationInfo = getJwtValidationInfo(jwtToken, cacheKey);

        if (jwtValidationInfo != null) {
            if (jwtValidationInfo.isValid()) {
                // validate scopes
                validateScopes(synCtx, openAPI, jwtValidationInfo);
                // Validate subscriptions


                APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
                
                log.debug("Begin subscription validation via Key Manager");
                apiKeyValidationInfoDTO = validateSubscriptionUsingKeyManager(synCtx, jwtValidationInfo);

                if (log.isDebugEnabled()) {
                    log.debug("Subscription validation via Key Manager. Status: "
                            + apiKeyValidationInfoDTO.isAuthorized());
                }
                if (apiKeyValidationInfoDTO.isAuthorized()) {
                    /*
                     * Set api.ut.apiPublisher of the subscribed api to the message context.
                     * This is necessary for the functionality of Publisher alerts.
                     * */
                    synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiKeyValidationInfoDTO.getApiPublisher());
                    log.debug("JWT authentication successful.");
                } else {
                    log.debug(
                            "User is NOT authorized to access the Resource. API Subscription validation " + "failed.");
                    throw new APISecurityException(apiKeyValidationInfoDTO.getValidationStatus(),
                            "User is NOT authorized to access the Resource. API Subscription validation " + "failed.");
                }
            
                log.debug("JWT authentication successful.");
                String endUserToken = null;
                try {
                    if (jwtGenerationEnabled) {
                        JWTInfoDto jwtInfoDto =
                                GatewayUtils
                                        .generateJWTInfoDto(jwtValidationInfo, null, apiKeyValidationInfoDTO, synCtx);
                        endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
                    }
                    return GatewayUtils
                            .generateAuthenticationContext(tokenSignature, jwtValidationInfo, null,
                                    apiKeyValidationInfoDTO,
                                    getApiLevelPolicy(), endUserToken, true);
                } catch (ParseException e) {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                }
            } else {
                throw new APISecurityException(jwtValidationInfo.getValidationCode(),
                        APISecurityConstants.getAuthenticationFailureMessage(jwtValidationInfo.getValidationCode()));
            }
        } else {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
    }

    private String generateAndRetrieveJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto)
            throws APISecurityException {

        String endUserToken = null;
        boolean valid = false;
        String jwtTokenCacheKey = jwtInfoDto.getApicontext().concat(":").concat(jwtInfoDto.getVersion()).concat(":")
                .concat(tokenSignature);
        if (isGatewayTokenCacheEnabled) {
            Object token = getGatewayJWTTokenCache().get(jwtTokenCacheKey);
            if (token != null) {
                endUserToken = (String) token;
                String[] splitToken = ((String) token).split("\\.");
                JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                long exp = payload.getLong("exp");
                long timestampSkew = getTimeStampSkewInSeconds() * 1000;
                valid = (exp - System.currentTimeMillis() > timestampSkew);
            }
            if (StringUtils.isEmpty(endUserToken) || !valid) {
                try {
                    endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
                    getGatewayJWTTokenCache().put(jwtTokenCacheKey, endUserToken);
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

    private APIKeyValidationInfoDTO validateSubscriptionUsingKeyManager(MessageContext synCtx,
                                                                        JWTValidationInfo jwtValidationInfo)
            throws APISecurityException {

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        return validateSubscriptionUsingKeyManager(apiContext, apiVersion, jwtValidationInfo);
    }

    private APIKeyValidationInfoDTO validateSubscriptionUsingKeyManager(String apiContext, String apiVersion,
                                                                        JWTValidationInfo jwtValidationInfo)
            throws APISecurityException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        String consumerKey = jwtValidationInfo.getConsumerKey();
        String keyManager = jwtValidationInfo.getKeyManager();
        if (consumerKey != null && keyManager != null) {
            return apiKeyValidator.validateSubscription(apiContext, apiVersion, consumerKey, tenantDomain, keyManager);
        }
        log.debug("Cannot call Key Manager to validate subscription. " +
                "Payload of the token does not contain the Authorized party - the party to which the ID Token was " +
                "issued");
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
    public AuthenticationContext authenticateForWebSocket(SignedJWT jwtToken, String apiContext, String apiVersion)
            throws APISecurityException {

        String tokenSignature = jwtToken.getSignature().toString();

        String cacheKey = WebsocketUtil.getAccessTokenCacheKey(tokenSignature, apiContext);
        JWTValidationInfo jwtValidationInfo = null;
        String jwtHeader = jwtToken.getHeader().toString();
        jwtValidationInfo = getJwtValidationInfo(jwtToken, cacheKey);
        if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
            if (log.isDebugEnabled()) {
                log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                        getMaskedToken(jwtHeader));
            }
            log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(jwtHeader));
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token");
        }

        if (jwtValidationInfo != null && jwtValidationInfo.isValid()) {
            net.minidev.json.JSONObject api =
                    GatewayUtils.validateAPISubscription(apiContext, apiVersion, jwtValidationInfo,
                            jwtHeader, true);
            if (api == null) {
                boolean validateSubscriptionViaKM = Boolean.parseBoolean(
                        ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                                .getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION)
                                                                        );
                if (validateSubscriptionViaKM) {
                    log.debug("Begin subscription validation via Key Manager");
                    APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validateSubscriptionUsingKeyManager(apiContext,
                            apiVersion, jwtValidationInfo);

                    if (log.isDebugEnabled()) {
                        log.debug("Subscription validation via Key Manager. Status: " +
                                apiKeyValidationInfoDTO.isAuthorized());
                    }
                    if (apiKeyValidationInfoDTO.isAuthorized()) {
                        log.debug("JWT authentication successful.");
                        String endUserToken;
                        if (jwtGenerationEnabled) {
                            JWTInfoDto jwtInfoDto;
                            try {
                                jwtInfoDto =
                                        GatewayUtils.generateJWTInfoDto(jwtValidationInfo, api, apiKeyValidationInfoDTO,
                                                apiContext, apiVersion);
                                endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
                                return GatewayUtils
                                        .generateAuthenticationContext(tokenSignature, jwtValidationInfo, null,
                                                apiKeyValidationInfoDTO, getApiLevelPolicy(), endUserToken, true);
                            } catch (ParseException e) {
                                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                            }
                        }
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
            try {
                if (jwtGenerationEnabled) {
                    JWTInfoDto jwtInfoDto =
                            GatewayUtils.generateJWTInfoDto(jwtValidationInfo, api, null, apiContext, apiVersion);
                    endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
                }
                return GatewayUtils
                        .generateAuthenticationContext(tokenSignature, jwtValidationInfo, api, null,
                                getApiLevelPolicy(),
                                endUserToken, true);
            } catch (ParseException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        } else if (!jwtValidationInfo.isValid()) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token");
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
    }

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes specified
     * in the JWT token payload.
     *
     * @param synCtx  The message to be authenticated
     * @param openAPI The OpenAPI object of the invoked API
     * @param jwtValidationInfo Validated JWT Information
     * @throws APISecurityException in case of scope validation failure
     */
    private void validateScopes(MessageContext synCtx, OpenAPI openAPI, JWTValidationInfo jwtValidationInfo)
            throws APISecurityException {

        if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
            HashMap<String, String> operationScopeMappingList =
                    (HashMap<String, String>) synCtx.getProperty(APIConstants.SCOPE_OPERATION_MAPPING);
            String[] operationList = ((String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE)).split(",");
            for (String operation : operationList) {
                String operationScope = operationScopeMappingList.get(operation);
                checkTokenWithTheScope(operation, operationScope, jwtValidationInfo);
            }
        } else {
            String resource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            String resourceScope = OpenAPIUtils.getScopesOfResource(openAPI, synCtx);
            checkTokenWithTheScope(resource, resourceScope, jwtValidationInfo);
        }
    }

    private void checkTokenWithTheScope(String resource, String resourceScope, JWTValidationInfo jwtValidationInfo)
            throws APISecurityException {

        if (StringUtils.isNotBlank(resourceScope)) {
            if (jwtValidationInfo.getScopes().isEmpty()) {
                log.error("Scopes not found in the token.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }

            boolean scopeFound = false;

            for (String scope : jwtValidationInfo.getScopes()) {
                if (scope.trim().equals(resourceScope)) {
                    scopeFound = true;
                    break;
                }
            }
            if (!scopeFound) {
                if (log.isDebugEnabled()) {
                    log.debug("Scope validation failed. User: " + jwtValidationInfo.getUser());
                }
                log.error("Scope validation failed.");
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            if (log.isDebugEnabled()) {
                log.debug("Scope validation successful for the resource: " + resource + ", Resource Scope: " +
                        resourceScope
                        + ", User: " + jwtValidationInfo.getUser());
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
     * @return
     */
    private JWTValidationInfo checkTokenExpiration(String tokenSignature, JWTValidationInfo payload,
                                                   String tenantDomain)
            throws APISecurityException {

        long timestampSkew = getTimeStampSkewInSeconds();

        Date now = new Date();
        Date exp = new Date(payload.getExpiryTime());
        if (!DateUtils.isAfter(exp, now, timestampSkew)) {
            if (isGatewayTokenCacheEnabled) {
                getGatewayTokenCache().remove(tokenSignature);
                getGatewayJWTTokenCache().remove(tokenSignature);
                getInvalidTokenCache().put(tokenSignature, tenantDomain);
            }
            payload.setValid(false);
            payload.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
            return payload;
        }
        return payload;
    }

    protected long getTimeStampSkewInSeconds() {

        return OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();
    }

    private JWTValidationInfo getJwtValidationInfo(SignedJWT jwtToken, String cacheKey)
            throws APISecurityException {

        String jwtHeader = jwtToken.getHeader().toString();
        String tenantDomain = GatewayUtils.getTenantDomain();
        String tokenSignature = jwtToken.getSignature().toString();
        JWTValidationInfo jwtValidationInfo = null;
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenSignature);
            if (cacheToken != null) {
                if (getGatewayKeyCache().get(cacheKey) != null) {
                    JWTValidationInfo tempJWTValidationInfo = (JWTValidationInfo) getGatewayKeyCache().get(cacheKey);
                    String rawPayload = tempJWTValidationInfo.getRawPayload();
                    if (rawPayload.equals(jwtToken.getParsedString())) {
                        checkTokenExpiration(tokenSignature, tempJWTValidationInfo, tenantDomain);
                        jwtValidationInfo = tempJWTValidationInfo;
                    }
                }
            } else if (getInvalidTokenCache().get(tokenSignature) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + GatewayUtils
                            .getMaskedToken(jwtHeader));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(jwtHeader));

                jwtValidationInfo = new JWTValidationInfo();
                jwtValidationInfo.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
                jwtValidationInfo.setValid(false);
            }
        }
        if (jwtValidationInfo == null) {

            try {
                jwtValidationInfo = jwtValidationService.validateJWTToken(jwtToken);
                if (isGatewayTokenCacheEnabled) {
                    // Add token to tenant token cache
                    if (jwtValidationInfo.isValid()) {
                        getGatewayTokenCache().put(tokenSignature, tenantDomain);
                        getGatewayKeyCache().put(cacheKey, jwtValidationInfo);
                    } else {
                        getInvalidTokenCache().put(tokenSignature, tenantDomain);
                    }

                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        //Add the tenant domain as a reference to the super tenant cache so we know from which tenant
                        // cache
                        //to remove the entry when the need occurs to clear this particular cache entry.
                        try {
                            // Start super tenant flow
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                            // Add token to super tenant token cache
                            if (jwtValidationInfo.isValid()) {
                                getGatewayTokenCache().put(tokenSignature, tenantDomain);
                            } else {
                                getInvalidTokenCache().put(tokenSignature, tenantDomain);
                            }
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                    }
                }
                return jwtValidationInfo;
            } catch (APIManagementException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        }
        return jwtValidationInfo;
    }

    protected Cache getGatewayTokenCache() {

        return CacheProvider.getGatewayTokenCache();
    }

    protected Cache getInvalidTokenCache() {

        return CacheProvider.getInvalidTokenCache();
    }

    protected Cache getGatewayKeyCache() {

        return CacheProvider.getGatewayKeyCache();
    }

    protected Cache getGatewayJWTTokenCache() {

        return CacheProvider.getGatewayJWTTokenCache();
    }

    private String getApiLevelPolicy() {

        return apiLevelPolicy;
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }
}
