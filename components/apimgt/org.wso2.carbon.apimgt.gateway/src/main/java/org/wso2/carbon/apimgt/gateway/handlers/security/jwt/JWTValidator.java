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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.util.DateUtils;
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
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;

/**
 * A Validator class to validate JWT tokens in an API request.
 */
public class JWTValidator {

    private static final Log log = LogFactory.getLog(JWTValidator.class);
    private boolean isGatewayTokenCacheEnabled;
    private APIKeyValidator apiKeyValidator;
    private boolean jwtGenerationEnabled;
    private AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator;
    JWTConfigurationDto jwtConfigurationDto;
    JWTValidationService jwtValidationService;

    public JWTValidator(APIKeyValidator apiKeyValidator) {

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
     * @param signedJWTInfo The JWT token sent with the API request
     * @param synCtx   The message to be authenticated
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticate(SignedJWTInfo signedJWTInfo, MessageContext synCtx)
            throws APISecurityException {

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
        String jwtTokenIdentifier = getJWTTokenIdentifier(signedJWTInfo);

        String jwtHeader = signedJWTInfo.getSignedJWT().getHeader().toString();
        if (StringUtils.isNotEmpty(jwtTokenIdentifier)) {
            if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(jwtTokenIdentifier)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                            getMaskedToken(jwtHeader));
                }
                log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(jwtHeader));
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }

        }

        JWTValidationInfo jwtValidationInfo = getJwtValidationInfo(signedJWTInfo, jwtTokenIdentifier);

        if (jwtValidationInfo != null) {
            if (jwtValidationInfo.isValid()) {

                // Validate subscriptions
                APIKeyValidationInfoDTO apiKeyValidationInfoDTO;

                log.debug("Begin subscription validation via Key Manager: " + jwtValidationInfo.getKeyManager());
                apiKeyValidationInfoDTO = validateSubscriptionUsingKeyManager(synCtx, jwtValidationInfo);

                if (log.isDebugEnabled()) {
                    log.debug("Subscription validation via Key Manager. Status: "
                            + apiKeyValidationInfoDTO.isAuthorized());
                }
                if (!apiKeyValidationInfoDTO.isAuthorized()){
                    log.debug(
                            "User is NOT authorized to access the Resource. API Subscription validation failed.");
                    throw new APISecurityException(apiKeyValidationInfoDTO.getValidationStatus(),
                            "User is NOT authorized to access the Resource. API Subscription validation failed.");

                }
                // Validate scopes
                validateScopes(apiContext, apiVersion, matchingResource, httpMethod, jwtValidationInfo, signedJWTInfo);
                synCtx.setProperty(APIMgtGatewayConstants.SCOPES, jwtValidationInfo.getScopes().toString());

                if (apiKeyValidationInfoDTO.isAuthorized()) {
                    /*
                     * Set api.ut.apiPublisher of the subscribed api to the message context.
                     * This is necessary for the functionality of Publisher alerts.
                     * Set API_NAME of the subscribed api to the message context.
                     * */
                    synCtx.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiKeyValidationInfoDTO.getApiPublisher());
                    synCtx.setProperty("API_NAME", apiKeyValidationInfoDTO.getApiName());
                    /* GraphQL Query Analysis Information */
                    if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                        synCtx.setProperty(APIConstants.MAXIMUM_QUERY_DEPTH,
                                apiKeyValidationInfoDTO.getGraphQLMaxDepth());
                        synCtx.setProperty(APIConstants.MAXIMUM_QUERY_COMPLEXITY,
                                apiKeyValidationInfoDTO.getGraphQLMaxComplexity());
                    }
                    log.debug("JWT authentication successful.");
                }
                log.debug("JWT authentication successful.");
                String endUserToken = null;
                if (jwtGenerationEnabled) {
                    JWTInfoDto jwtInfoDto = GatewayUtils
                            .generateJWTInfoDto(null, jwtValidationInfo, apiKeyValidationInfoDTO, synCtx);
                    endUserToken = generateAndRetrieveJWTToken(jwtTokenIdentifier, jwtInfoDto);
                }
                return GatewayUtils.generateAuthenticationContext(jwtTokenIdentifier, jwtValidationInfo, apiKeyValidationInfoDTO,
                        endUserToken, true);
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
                long exp = payload.getLong("exp") * 1000L;
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
     * @param signedJWTInfo   The JWT token sent with the API request
     * @param apiContext The context of the invoked API
     * @param apiVersion The version of the invoked API
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticateForWebSocket(SignedJWTInfo signedJWTInfo, String apiContext,
                                                          String apiVersion)
            throws APISecurityException {

        String tokenSignature = signedJWTInfo.getSignedJWT().getSignature().toString();

        JWTValidationInfo jwtValidationInfo;
        String jwtHeader = signedJWTInfo.getSignedJWT().getHeader().toString();
        String jti;
        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        jti = jwtClaimsSet.getJWTID();

        jwtValidationInfo = getJwtValidationInfo(signedJWTInfo, jti);
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
            log.debug("Begin subscription validation via Key Manager: " + jwtValidationInfo.getKeyManager());
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validateSubscriptionUsingKeyManager(apiContext,
                    apiVersion, jwtValidationInfo);
            if (APIConstants.DEFAULT_WEBSOCKET_VERSION.equals(apiVersion)) {
                apiVersion = apiKeyValidationInfoDTO.getApiVersion();
            }
            if (log.isDebugEnabled()) {
                log.debug("Subscription validation via Key Manager: " + jwtValidationInfo.getKeyManager() + ". Status: " +
                        apiKeyValidationInfoDTO.isAuthorized());
            }
            if (apiKeyValidationInfoDTO.isAuthorized()) {
                log.debug("JWT authentication successful. user: " + apiKeyValidationInfoDTO.getEndUserName());
                String endUserToken = null;
                JWTInfoDto jwtInfoDto;
                if (jwtGenerationEnabled) {
                    jwtInfoDto = GatewayUtils.generateJWTInfoDto(jwtValidationInfo,
                            apiKeyValidationInfoDTO, apiContext, apiVersion);
                    endUserToken = generateAndRetrieveJWTToken(tokenSignature, jwtInfoDto);
                }
                AuthenticationContext context = GatewayUtils
                        .generateAuthenticationContext(jti, jwtValidationInfo, apiKeyValidationInfoDTO,
                                endUserToken, true);
                context.setApiVersion(apiVersion);
                return context;
            } else {
                String message = "User is NOT authorized to access the Resource. API Subscription validation failed.";
                log.debug(message);
                throw new APISecurityException(apiKeyValidationInfoDTO.getValidationStatus(), message);
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
     * @param apiContext        API Context
     * @param apiVersion        API Version
     * @param matchingResource  Accessed API resource
     * @param httpMethod        API resource's HTTP method
     * @param jwtValidationInfo Validated JWT Information
     * @param jwtToken          JWT Token
     * @throws APISecurityException in case of scope validation failure
     */
    private void validateScopes(String apiContext, String apiVersion, String matchingResource, String httpMethod,
                                JWTValidationInfo jwtValidationInfo, SignedJWTInfo jwtToken)
            throws APISecurityException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        // Generate TokenValidationContext
        TokenValidationContext tokenValidationContext = new TokenValidationContext();

        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        Set<String> scopeSet = new HashSet<>();
        scopeSet.addAll(jwtValidationInfo.getScopes());
        apiKeyValidationInfoDTO.setScopes(scopeSet);
        tokenValidationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);

        tokenValidationContext.setAccessToken(jwtToken.getToken());
        tokenValidationContext.setHttpVerb(httpMethod);
        tokenValidationContext.setMatchingResource(matchingResource);
        tokenValidationContext.setContext(apiContext);
        tokenValidationContext.setVersion(apiVersion);

        boolean valid = this.apiKeyValidator.validateScopes(tokenValidationContext, tenantDomain);
        if (valid) {
            if (log.isDebugEnabled()) {
                log.debug("Scope validation successful for the resource: " + matchingResource
                        + ", user: " + jwtValidationInfo.getUser());
            }
        } else {
            String message = "User is NOT authorized to access the Resource: " + matchingResource
                    + ". Scope validation failed.";
            log.debug(message);
            throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, message);
        }
    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param tokenIdentifier The token Identifier of JWT.
     * @param payload        The payload of the JWT token
     * @param tenantDomain   The tenant domain from which the token cache is retrieved
     * @throws APISecurityException if the token is expired
     * @return
     */
    private JWTValidationInfo checkTokenExpiration(String tokenIdentifier, JWTValidationInfo payload,
                                                   String tenantDomain)
            throws APISecurityException {

        long timestampSkew = getTimeStampSkewInSeconds();

        Date now = new Date();
        Date exp = new Date(payload.getExpiryTime());
        if (!DateUtils.isAfter(exp, now, timestampSkew)) {
            if (isGatewayTokenCacheEnabled) {
                getGatewayTokenCache().remove(tokenIdentifier);
                getGatewayJWTTokenCache().remove(tokenIdentifier);
                getInvalidTokenCache().put(tokenIdentifier, tenantDomain);
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

    private JWTValidationInfo getJwtValidationInfo(SignedJWTInfo signedJWTInfo, String jti)
            throws APISecurityException {

        String jwtHeader = signedJWTInfo.getSignedJWT().getHeader().toString();
        String tenantDomain = GatewayUtils.getTenantDomain();
        JWTValidationInfo jwtValidationInfo = null;
        if (isGatewayTokenCacheEnabled &&
                !SignedJWTInfo.ValidationStatus.NOT_VALIDATED.equals(signedJWTInfo.getValidationStatus())) {
            String cacheToken = (String) getGatewayTokenCache().get(jti);
            if (SignedJWTInfo.ValidationStatus.VALID.equals(signedJWTInfo.getValidationStatus())
                    && cacheToken != null) {
                if (getGatewayKeyCache().get(jti) != null) {
                    JWTValidationInfo tempJWTValidationInfo = (JWTValidationInfo) getGatewayKeyCache().get(jti);
                    checkTokenExpiration(jti, tempJWTValidationInfo, tenantDomain);
                    jwtValidationInfo = tempJWTValidationInfo;
                }
            } else if (SignedJWTInfo.ValidationStatus.INVALID.equals(signedJWTInfo.getValidationStatus())
                    && getInvalidTokenCache().get(jti) != null) {
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
                jwtValidationInfo = jwtValidationService.validateJWTToken(signedJWTInfo);
                signedJWTInfo.setValidationStatus(jwtValidationInfo.isValid() ?
                        SignedJWTInfo.ValidationStatus.VALID : SignedJWTInfo.ValidationStatus.INVALID);

                if (isGatewayTokenCacheEnabled) {
                    // Add token to tenant token cache
                    if (jwtValidationInfo.isValid()) {
                        getGatewayTokenCache().put(jti, tenantDomain);
                        getGatewayKeyCache().put(jti, jwtValidationInfo);
                    } else {
                        getInvalidTokenCache().put(jti, tenantDomain);
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
                                getGatewayTokenCache().put(jti, tenantDomain);
                            } else {
                                getInvalidTokenCache().put(jti, tenantDomain);
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

    private String getJWTTokenIdentifier(SignedJWTInfo signedJWTInfo) {

        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        String jwtid = jwtClaimsSet.getJWTID();
        if (StringUtils.isNotEmpty(jwtid)) {
            return jwtid;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
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

}
