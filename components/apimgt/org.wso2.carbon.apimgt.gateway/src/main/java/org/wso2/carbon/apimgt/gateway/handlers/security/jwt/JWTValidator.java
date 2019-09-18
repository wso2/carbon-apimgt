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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.cache.Cache;
import javax.cache.Caching;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * A Validator class to validate JWT tokens in an API request.
 */
public class JWTValidator {

    private static final Log log = LogFactory.getLog(JWTValidator.class);

    private String apiLevelPolicy;
    private boolean isGatewayTokenCacheEnabled;

    public JWTValidator(String apiLevelPolicy) {
        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = isGatewayTokenCacheEnabled();
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

        JSONObject payload = null;
        boolean isVerified = false;

        String tokenSignature = splitToken[2];

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        String cacheKey = getAccessTokenCacheKey(tokenSignature, apiContext, apiVersion, matchingResource, httpMethod);
        String tenantDomain = getTenantDomain();

        // Validate from cache
        if (isGatewayTokenCacheEnabled) {
            String cacheToken = (String) getGatewayTokenCache().get(tokenSignature);
            if (cacheToken != null) {
                log.debug("Token retrieved from the token cache.");
                isVerified = true;
            } else if (getInvalidTokenCache().get(tokenSignature) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Token retrieved from the invalid token cache. Token: " + getMaskedToken(splitToken));
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        // Not found in cache
        if (!isVerified) {
            log.debug("Token not found in the cache.");
            try {
                payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
            } catch (JSONException | IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid JWT token. Token: " + getMaskedToken(splitToken));
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token. Failed to decode the token.", e);
            }
            isVerified = verifyTokenSignature(splitToken);
            if (isGatewayTokenCacheEnabled) {
                // Add token to tenant token cache
                if (isVerified) {
                    getGatewayTokenCache().put(tokenSignature, tenantDomain);
                } else {
                    getInvalidTokenCache().put(tokenSignature, tenantDomain);
                }

                if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
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
                                    + getMaskedToken(splitToken), e);
                        }
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

            JSONObject api = validateAPISubscription(apiContext, apiVersion, payload);

            log.debug("JWT authentication successful.");
            return generateAuthenticationContext(tokenSignature, payload, api, getApiLevelPolicy());
        }
        if (log.isDebugEnabled()) {
            log.debug("Token signature verification failure. Token: " + getMaskedToken(splitToken));
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                "Invalid JWT token. Signature verification failed.");
    }

    /**
     * Populate the authentication context given the token payload and API tier information.
     *
     * @param tokenSignature The signature of the JWT token
     * @param payload        The payload of the JWT token
     * @param api            A JSON object containing the API information of the subscribed API
     * @param apiLevelPolicy The API level throttling tier
     * @return an AuthenticationContext object generated using the token payload and API tier information
     */
    private AuthenticationContext generateAuthenticationContext(String tokenSignature, JSONObject payload, JSONObject api,
                                                                String apiLevelPolicy) {
        JSONObject applicationObj = payload.getJSONObject(APIConstants.JwtTokenConstants.APPLICATION);

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setApiKey(tokenSignature);
        if (payload.has(APIConstants.JwtTokenConstants.KEY_TYPE)) {
            authContext.setKeyType(payload.getString(APIConstants.JwtTokenConstants.KEY_TYPE));
        } else {
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        }
        authContext.setUsername(payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
        authContext.setApiTier(apiLevelPolicy);
        authContext.setApplicationId(String.valueOf(applicationObj.getInt(APIConstants.JwtTokenConstants.APPLICATION_ID)));
        authContext.setApplicationName(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_NAME));
        authContext.setApplicationTier(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_TIER));
        authContext.setSubscriber(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_OWNER));
        authContext.setConsumerKey(payload.getString(APIConstants.JwtTokenConstants.CONSUMER_KEY));

        if (api != null) {
            // If the user is subscribed to the API
            String subscriptionTier = api.getString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER);
            authContext.setTier(subscriptionTier);
            authContext.setSubscriberTenantDomain(
                    api.getString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN));
            JSONObject tierInfo = (JSONObject) payload.get(APIConstants.JwtTokenConstants.TIER_INFO);

            if (tierInfo.has(subscriptionTier)) {
                JSONObject subscriptionTierObj = (JSONObject) tierInfo.get(subscriptionTier);
                authContext.setStopOnQuotaReach(
                        subscriptionTierObj.getBoolean(APIConstants.JwtTokenConstants.STOP_ON_QUOTA_REACH));
                authContext.setSpikeArrestLimit
                        (subscriptionTierObj.getInt(APIConstants.JwtTokenConstants.SPIKE_ARREST_LIMIT));
                if (!JSONObject.NULL.equals(
                        subscriptionTierObj.get(APIConstants.JwtTokenConstants.SPIKE_ARREST_UNIT))) {
                    authContext.setSpikeArrestUnit(
                            subscriptionTierObj.getString(APIConstants.JwtTokenConstants.SPIKE_ARREST_UNIT));
                }
            }
        }
        // Set JWT token sent to the backend
        if (payload.has(APIConstants.JwtTokenConstants.BACKEND_TOKEN)) {
            authContext.setCallerToken(payload.getString(APIConstants.JwtTokenConstants.BACKEND_TOKEN));
        }

        return authContext;
    }

    /**
     * Validate whether the user is subscribed to the invoked API. If subscribed, return a JSON object containing
     * the API information.
     *
     * @param apiContext API context
     * @param apiVersion API version
     * @param payload    The payload of the JWT token
     * @return an JSON object containing subscribed API information retrieved from token payload.
     * If the subscription information is not found, return a null object.
     * @throws APISecurityException if the user is not subscribed to the API
     */
    private JSONObject validateAPISubscription(String apiContext, String apiVersion, JSONObject payload)
            throws APISecurityException {
        JSONObject api = null;

        if (payload.has(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS)) {
            // Subscription validation
            JSONArray subscribedAPIs = payload.getJSONArray(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS);
            for (int i = 0; i < subscribedAPIs.length(); i++) {
                JSONObject subscribedAPIsJSONObject = subscribedAPIs.getJSONObject(i);
                if (apiContext.equals(subscribedAPIsJSONObject.getString(APIConstants.JwtTokenConstants.API_CONTEXT)) &&
                        apiVersion.equals(subscribedAPIsJSONObject.getString(APIConstants.JwtTokenConstants.API_VERSION)
                        )) {
                    api = subscribedAPIsJSONObject;
                    if (log.isDebugEnabled()) {
                        log.debug("User is subscribed to the API: " + apiContext + ", version: " + apiVersion);
                    }
                    break;
                }
            }
            if (api == null) {
                if (log.isDebugEnabled()) {
                    log.debug("User is not subscribed to access the API: " + apiContext +
                            ", version: " + apiVersion);
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "User is not subscribed to access the API: " + apiContext + ", version: " + apiVersion);
            }
        } else {
            log.debug("No subscription information found in the token.");
        }
        return api;
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
        long expiredTime = payload.getLong(APIConstants.JwtTokenConstants.EXPIRED_TIME) * 1000;
        long validityPeriod = expiredTime - issuedTime;
        long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
        long currentTime = System.currentTimeMillis();

        //If the validity period is not a never expiring value
        if (validityPeriod != Long.MAX_VALUE && (currentTime - timestampSkew) > validityPeriod) {
            if ((currentTime - timestampSkew) > expiredTime) {
                if (isGatewayTokenCacheEnabled) {
                    getGatewayTokenCache().remove(tokenSignature);
                    getInvalidTokenCache().put(tokenSignature, tenantDomain);
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                        "JWT token is expired");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getString(APIConstants.JwtTokenConstants.SUBJECT));
        }
    }

    /**
     * Verify the JWT token signature.
     *
     * @param splitToken The JWT token which is split into [header, payload, signature]
     * @return whether the signature is verified or or not
     * @throws APISecurityException in case of signature verification failure
     */
    private boolean verifyTokenSignature(String[] splitToken) throws APISecurityException {
        // Retrieve signature algorithm from token header
        String signatureAlgorithm = getSignatureAlgorithm(splitToken);

        Certificate publicCert = null;
        //Read the client-truststore.jks into a KeyStore
        try {
            KeyStore trustStore = ServiceReferenceHolder.getInstance().getTrustStore();
            if (trustStore != null) {
                // Read public certificate from trust store
                publicCert = trustStore.getCertificate(APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS);
            }
        } catch (KeyStoreException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error in retrieving public certificate from the trust store with alias : "
                            + APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS, e);
        }

        if (publicCert != null) {
            // Retrieve public key from the certificate
            PublicKey publicKey = publicCert.getPublicKey();

            try {
                // Verify token signature
                Signature signatureInstance = Signature.getInstance(signatureAlgorithm);
                signatureInstance.initVerify(publicKey);
                String assertion = splitToken[0] + "." + splitToken[1];
                signatureInstance.update(assertion.getBytes());
                byte[] decodedSignature = Base64.getUrlDecoder().decode(splitToken[2]);
                return signatureInstance.verify(decodedSignature);
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IllegalArgumentException e) {
                log.error("Signature verification failed. Token: " + getMaskedToken(splitToken), e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token", e);
            }
        } else {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Couldn't find a public certificate to verify signature");
        }
    }

    /**
     * Retrieve the signature algorithm specified in the token header.
     *
     * @param splitToken The JWT token which is split into [header, payload, signature]
     * @return whether the signature algorithm
     * @throws APISecurityException in case of signature algorithm extraction failure
     */
    private String getSignatureAlgorithm(String[] splitToken) throws APISecurityException {
        String signatureAlgorithm;
        JSONObject header;
        try {
            header = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[0])));
        } catch (JSONException | IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug("Token decryption failure when retrieving header. Token: " +
                        getMaskedToken(splitToken), e);
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token", e);
        }
        signatureAlgorithm = header.getString(APIConstants.JwtTokenConstants.SIGNATURE_ALGORITHM);
        if (StringUtils.isBlank(signatureAlgorithm)) {
            if (log.isDebugEnabled()) {
                log.debug("Signature algorithm not found in the token. Token: " + getMaskedToken(splitToken));
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
        }
        if (APIConstants.SIGNATURE_ALGORITHM_RS256.equals(signatureAlgorithm)) {
            signatureAlgorithm = APIConstants.SIGNATURE_ALGORITHM_SHA256_WITH_RSA;
        }
        return signatureAlgorithm;
    }

    /**
     * Return tenant domain of the API being invoked.
     *
     * @return tenant domain
     */
    private String getTenantDomain() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private boolean isGatewayTokenCacheEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(cacheEnabled);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. " +
                    "Use default configuration.", e);
        }
        return true;
    }

    private Cache getGatewayTokenCache() {
        return getCacheFromCacheManager(APIConstants.GATEWAY_TOKEN_CACHE_NAME);
    }

    private Cache getInvalidTokenCache() {
        return getCacheFromCacheManager(APIConstants.GATEWAY_INVALID_TOKEN_CACHE_NAME);
    }

    private Cache getGatewayKeyCache() {
        return getCacheFromCacheManager(APIConstants.GATEWAY_KEY_CACHE_NAME);
    }

    private Cache getCacheFromCacheManager(String cacheName) {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }

    private String getAccessTokenCacheKey(String accessToken, String apiContext, String apiVersion,
                                          String resourceUri, String httpVerb) {
        return accessToken + ":" + apiContext + ":" + apiVersion + ":" + resourceUri + ":" + httpVerb;
    }

    private String getApiLevelPolicy() {
        return apiLevelPolicy;
    }
    
    private String getMaskedToken(String[] splitToken) {
        String concatToken = String.join(".", splitToken);
        if (concatToken.length() >= 10) {
            return "XXXXX" + concatToken.substring(concatToken.length() - 10);
        } else {
            return "XXXXX" + concatToken.substring(concatToken.length() / 2);
        }
    }
}