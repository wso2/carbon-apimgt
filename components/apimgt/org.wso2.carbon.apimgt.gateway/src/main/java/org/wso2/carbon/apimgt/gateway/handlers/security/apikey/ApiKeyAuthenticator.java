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

package org.wso2.carbon.apimgt.gateway.handlers.security.apikey;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.Caching;

public class ApiKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(ApiKeyAuthenticator.class);
    private boolean isGatewayTokenCacheEnabled;
    private static boolean gatewayApiKeyCacheInit = false;
    private static boolean gatewayApiKeyKeyCacheInit = false;
    private static boolean gatewayInvalidApiKeyCacheInit = false;
    private String securityParam;
    private String apiLevelPolicy;
    private boolean isMandatory;

    public ApiKeyAuthenticator(String authorizationHeader, String apiLevelPolicy, boolean isApiKeyMandatory) {
        this.securityParam = authorizationHeader;
        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        this.isMandatory = isApiKeyMandatory;
    }

    @Override
    public void init(SynapseEnvironment env) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public AuthenticationResponse authenticate(MessageContext synCtx) {
        if (log.isDebugEnabled()) {
            log.info("ApiKey Authentication initialized");
        }

        try {
            // Extract apikey from the request while removing it from the msg context.
            String apiKey = extractApiKey(synCtx);

            String splitToken[] = apiKey.split("\\.");
            JSONObject decodedHeader;
            JSONObject payload = null;
            String tokenSignature, certAlias;
            if (splitToken.length != 3) {
                log.error("Api Key does not have the format {header}.{payload}.{signature} ");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            tokenSignature = splitToken[2];
            try {
                decodedHeader = new JSONObject(new String(decode(splitToken[0])));
            } catch (IllegalArgumentException | JSONException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken), e);
                }
                log.error("Invalid JWT token. Failed to decode the Api Key header.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE , e);
            }
            // Check if the decoded header contains type as 'JWT'.
            if (!APIConstants.JWT.equals(decodedHeader.getString(APIConstants.JwtTokenConstants.TOKEN_TYPE))) {
                if (log.isDebugEnabled()){
                    log.debug("Invalid Api Key token type. Api Key: " + GatewayUtils.getMaskedToken(splitToken));
                }
                log.error("Invalid Api Key token type.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }

            if (decodedHeader.getString(APIConstants.JwtTokenConstants.JWT_KID) == null) {
                if (log.isDebugEnabled()){
                    log.debug("Invalid Api Key. Could not find alias in header. Api Key: " +
                            GatewayUtils.getMaskedToken(splitToken));
                }
                log.error("Invalid Api Key. Could not find alias in header");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } else {
                certAlias = decodedHeader.getString(APIConstants.JwtTokenConstants.JWT_KID);
            }

            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    getProperty(Constants.Configuration.HTTP_METHOD);
            String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

            OpenAPI openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
            if (openAPI == null) {
                log.error("Swagger is missing in the gateway. " +
                        "Therefore, Api Key authentication cannot be performed.");
                return new AuthenticationResponse(false, isMandatory, true,
                        APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF,
                        APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF_ERROR_MESSAGE);
            }
            String resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                                                                                    matchingResource, httpMethod);
            VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
            verbInfoDTO.setHttpVerb(httpMethod);
            //Not doing resource level authentication
            verbInfoDTO.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
            verbInfoDTO.setRequestKey(resourceCacheKey);
            verbInfoDTO.setThrottling(OpenAPIUtils.getResourceThrottlingTier(openAPI, synCtx));
            List<VerbInfoDTO> verbInfoList = new ArrayList<>();
            verbInfoList.add(verbInfoDTO);
            synCtx.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);

            String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenSignature, apiContext, apiVersion,
                    matchingResource, httpMethod);
            String tenantDomain = GatewayUtils.getTenantDomain();
            boolean isVerified = false;

            // Validate from cache
            if (isGatewayTokenCacheEnabled) {
                String cacheToken = (String) getGatewayApiKeyCache().get(tokenSignature);
                if (cacheToken != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the Api Key cache.");
                    }
                    isVerified = true;
                } else if (getInvalidGatewayApiKeyCache().get(tokenSignature) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the invalid Api Key cache. Api Key: " +
                                GatewayUtils.getMaskedToken(splitToken));
                    }
                    log.error("Invalid Api Key." + GatewayUtils.getMaskedToken(splitToken));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                } else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenSignature)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                                getMaskedToken(splitToken));
                    }
                    log.error("Invalid API Key. " + GatewayUtils.getMaskedToken(splitToken));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            "Invalid API Key");
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

            // Not found in cache or caching disabled
            if (!isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key not found in the cache.");
                }
                try {
                    payload = new JSONObject(new String(decode(splitToken[1])));
                } catch (JSONException | IllegalArgumentException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken), e);
                    }
                    log.error("Invalid JWT token. Failed to decode the Api Key body.");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                }
                try {
                    isVerified = GatewayUtils.verifyTokenSignature(splitToken, certAlias);
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
                        getGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                    } else {
                        getInvalidGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                    }

                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        try {
                            // Start super tenant flow
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                            // Add token to super tenant token cache
                            if (isVerified) {
                                getGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                            } else {
                                getInvalidGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                            }
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                    }
                }
            }

            // If Api Key signature is verified
            if (isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key signature is verified.");
                }
                if (isGatewayTokenCacheEnabled && getGatewayApiKeyKeyCache().get(cacheKey) != null) {
                    // Api Key is found in the key cache
                    payload = (JSONObject) getGatewayApiKeyKeyCache().get(cacheKey);
                    if (isJwtTokenExpired(payload)) {
                        getGatewayApiKeyCache().remove(tokenSignature);
                        getInvalidGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                                APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE);
                    }
                } else {
                    // Retrieve payload from ApiKey
                    if (log.isDebugEnabled()) {
                        log.debug("ApiKey payload not found in the cache.");
                    }
                    if (payload == null) {
                        try {
                            payload = new JSONObject(new String(decode(splitToken[1])));
                        } catch (JSONException | IllegalArgumentException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid ApiKey. ApiKey: " + GatewayUtils.getMaskedToken(splitToken));
                            }
                            log.error("Invalid Api Key. Failed to decode the Api Key body.");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                        }
                    }
                    if (isJwtTokenExpired(payload)) {
                        if (isGatewayTokenCacheEnabled) {
                            getGatewayApiKeyCache().remove(tokenSignature);
                            getInvalidGatewayApiKeyCache().put(tokenSignature, tenantDomain);
                        }
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                                APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE);
                    }
                    if (isGatewayTokenCacheEnabled) {
                        getGatewayApiKeyKeyCache().put(cacheKey, payload);
                    }
                }
                JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, false);
                if (log.isDebugEnabled()) {
                    log.debug("Api Key authentication successful.");
                }
                AuthenticationContext authenticationContext;
                authenticationContext = GatewayUtils.generateAuthenticationContext(tokenSignature, payload, api, null, getApiLevelPolicy(), false);
                APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext, null);
                if (log.isDebugEnabled()) {
                    log.debug("User is authorized to access the resource using Api Key.");
                }
                return new AuthenticationResponse(true, isMandatory, false, 0, null);
            }

            if (log.isDebugEnabled()) {
                log.debug("Api Key signature verification failure. Api Key: " + GatewayUtils.getMaskedToken(splitToken));
            }
            log.error("Invalid Api Key. Signature verification failed.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

        } catch (APISecurityException e) {
            return new AuthenticationResponse(false, isMandatory, true, e.getErrorCode(), e.getMessage());
        }
    }

    private String extractApiKey(MessageContext mCtx) throws APISecurityException {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mCtx).getAxis2MessageContext();
        String apiKey;

        //check headers to get apikey
        Map headers = (Map) (axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        if (headers != null) {
            apiKey = (String) headers.get(securityParam);
            if (apiKey != null) {
                //Remove apikey header from the request
                headers.remove(securityParam);
                return apiKey.trim();
            }
        }
        //check query params to get apikey
        try {
            apiKey = new SynapseXPath("$url:apikey").stringValueOf(mCtx);
            if (StringUtils.isNotBlank(apiKey)) {
                String rest_url_postfix = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
                rest_url_postfix = removeApiKeyFromQueryParameters(rest_url_postfix, URLEncoder.encode(apiKey));
                axis2MC.setProperty(NhttpConstants.REST_URL_POSTFIX, rest_url_postfix);
                return apiKey.trim();
            } else {
                if (log.isDebugEnabled()){
                    log.debug("Api Key Authentication failed: Header or Query parameter with the name '"
                            .concat(securityParam).concat("' was not found."));
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
            }
        } catch (JaxenException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving apikey from the request query params.", e);
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
        }
    }

    private String removeApiKeyFromQueryParameters(String queryParam, String apiKey) {
        queryParam = queryParam.replace("?apikey=" + apiKey, "?");
        queryParam = queryParam.replace("&apikey=" + apiKey, "");
        queryParam = queryParam.replace("?&", "?");
        if (queryParam.lastIndexOf("?") == (queryParam.length() - 1)) {
            queryParam = queryParam.replace("?", "");
        }
        return queryParam;

    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param payload The payload of the JWT token
     * @return returns true if the JWT token is expired
     */
    private static boolean isJwtTokenExpired(JSONObject payload) {
        //if exp claim not in the token, we treat it as infinite validity token.
        if (!payload.has(APIConstants.JwtTokenConstants.EXPIRY_TIME)) {
            return false;
        }
        // Check whether the token is expired or not.
        long issuedTime = payload.getLong(APIConstants.JwtTokenConstants.ISSUED_TIME) * 1000;
        long expiryTime = payload.getLong(APIConstants.JwtTokenConstants.EXPIRY_TIME) * 1000;
        long validityPeriod = expiryTime - issuedTime;
        long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds()*1000;
        long currentTime = System.currentTimeMillis();

        //If the expiry time is not a never expiring value
        if (expiryTime != Long.MAX_VALUE && (currentTime - timestampSkew) > validityPeriod) {
            if ((currentTime - timestampSkew) > expiryTime) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
        }
        return false;
    }

    private byte[] decode(String payload) throws IllegalArgumentException {
        return java.util.Base64.getUrlDecoder().decode(payload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns the API Manager Configuration.
     *
     * @return the API Manager Configuration
     */
    private APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    //first level cache
    private Cache getGatewayApiKeyCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if (!gatewayApiKeyCacheInit) {
            gatewayApiKeyCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_API_KEY_CACHE_NAME, Long.parseLong(apimGWCacheExpiry),
                        Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout = getDefaultCacheTimeout();
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_API_KEY_CACHE_NAME, defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_API_KEY_CACHE_NAME);
    }

    private Cache getInvalidGatewayApiKeyCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if (!gatewayInvalidApiKeyCacheInit) {
            gatewayInvalidApiKeyCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_INVALID_API_KEY_CACHE_NAME, Long.parseLong(apimGWCacheExpiry),
                        Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout = getDefaultCacheTimeout();
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_INVALID_API_KEY_CACHE_NAME, defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_INVALID_API_KEY_CACHE_NAME);
    }

    //second level cache
    private Cache getGatewayApiKeyKeyCache() {
        String apimGWCacheExpiry = getApiManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if (!gatewayApiKeyKeyCacheInit) {
            gatewayApiKeyKeyCacheInit = true;
            if (apimGWCacheExpiry != null) {
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_API_KEY_KEY_CACHE_NAME, Long.parseLong(apimGWCacheExpiry),
                        Long.parseLong(apimGWCacheExpiry));
            } else {
                long defaultCacheTimeout = getDefaultCacheTimeout();
                return APIUtil.getCache(APIConstants.API_MANAGER_CACHE_MANAGER,
                        APIConstants.GATEWAY_API_KEY_KEY_CACHE_NAME, defaultCacheTimeout, defaultCacheTimeout);
            }
        }
        return getCacheFromCacheManager(APIConstants.GATEWAY_API_KEY_KEY_CACHE_NAME);
    }

    /**
     * Returns the Cache object of the given name.
     *
     * @param cacheName name of the Cache
     * @return the cache object
     */
    private Cache getCacheFromCacheManager(String cacheName) {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }

    /**
     * Returns the default cache timeout.
     *
     * @return the default cache timeout
     */
    private long getDefaultCacheTimeout() {
        return Long.valueOf(ServerConfiguration.getInstance().getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT))
                * 60;
    }

    private String getApiLevelPolicy() {
        return apiLevelPolicy;
    }

    @Override
    public String getChallengeString() {
        return null;
    }

    @Override
    public String getRequestOrigin() {
        return null;
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
