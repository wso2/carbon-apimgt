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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.models.Swagger;
import org.apache.axiom.util.base64.Base64Utils;
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
import org.wso2.carbon.apimgt.gateway.utils.SwaggerUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

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
     * @param jwtToken             The JWT token sent with the API request
     * @param synCtx               The message to be authenticated
     * @param swagger              The swagger object of the invoked API
     * @param authenticationScheme The resource authentication scheme of the invoked API resource
     * @return an AuthenticationContext object which contains the authentication information
     * @throws APISecurityException in case of authentication failure
     */
    @MethodStats
    public AuthenticationContext authenticate(String jwtToken, MessageContext synCtx, Swagger swagger,
                                              String authenticationScheme)
            throws APISecurityException {

        String[] splitToken = jwtToken.split("\\.");
        if (splitToken.length != 3) {
            log.debug("Invalid JWT token.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
        }

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
                isVerified = true;
            } else if (getInvalidTokenCache().get(tokenSignature) != null) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
        }

        // Not found in cache
        if (!isVerified) {
            log.debug("Token not found in the cache.");
            try {
                payload = new JSONObject(new String(Base64Utils.decode(splitToken[1])));
            } catch (JSONException e) {
                log.debug("Token decryption failure when retrieving payload.", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token");
            }
            isVerified = verifyTokenSignature(jwtToken, payload.getString("sub"));
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
                        payload = new JSONObject(new String(Base64Utils.decode(splitToken[1])));
                    } catch (JSONException e) {
                        log.debug("Token decryption failure when retrieving payload.", e);
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                }
                checkTokenExpiration(tokenSignature, payload, tenantDomain);
                validateTokenGrantType(payload.getString("grantType"), authenticationScheme);
                validateScopes(synCtx, swagger, payload);

                if (isGatewayTokenCacheEnabled) {
                    getGatewayKeyCache().put(cacheKey, payload);
                }
            }

            JSONObject api = validateAPISubscription(apiContext, apiVersion, payload);

            log.debug("JWT authentication passed.");
            return generateAuthenticationContext(payload, api, getApiLevelPolicy());
        }
        log.debug("Token signature verification failure.");
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
    }

    /**
     * Populate the authentication context given the token payload and API tier information.
     *
     * @param payload        The payload of the JWT token
     * @param api            A JSON object containing the API information of the subscribed API
     * @param apiLevelPolicy The API level throttling tier
     * @return an AuthenticationContext object generated using the token payload and API tier information
     */
    private AuthenticationContext generateAuthenticationContext(JSONObject payload, JSONObject api,
                                                                String apiLevelPolicy) {
        JSONObject applicationObj = (JSONObject) payload.get("application");

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setApiKey(payload.getString("jti"));
        authContext.setKeyType(payload.getString("keytype"));
        authContext.setUsername(payload.getString("sub"));
        authContext.setApiTier(apiLevelPolicy);
        authContext.setApplicationId(String.valueOf(applicationObj.getInt("id")));
        authContext.setApplicationName(applicationObj.getString("name"));
        authContext.setApplicationTier(applicationObj.getString("tier"));
        authContext.setSubscriber(payload.getString("sub"));
        authContext.setConsumerKey(payload.getString("consumerKey"));

        if (api != null) {
            // If the user is subscribed to the API
            authContext.setTier(api.getString("subscriptionTier"));
            authContext.setSubscriberTenantDomain(api.getString("subscriberTenantDomain"));
            JSONObject tierInfo = (JSONObject) payload.get("tierInfo");
            JSONObject subscriptionTierObj = (JSONObject) tierInfo.get(api.getString("subscriptionTier"));
            if (subscriptionTierObj != null) {
                authContext.setStopOnQuotaReach(subscriptionTierObj.getBoolean("stopOnQuotaReach"));
                authContext.setSpikeArrestLimit(subscriptionTierObj.getInt("spikeArrestLimit"));
                if (!JSONObject.NULL.equals(subscriptionTierObj.get("spikeArrestUnit"))) {
                    authContext.setSpikeArrestUnit(subscriptionTierObj.getString("spikeArrestUnit"));
                }
            }
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

        if (payload.get("subscribedAPIs") != null) {
            // Subscription validation
            JSONArray subscribedAPIs = (JSONArray) payload.get("subscribedAPIs");
            for (int i = 0; i < subscribedAPIs.length(); i++) {
                JSONObject subscribedAPIsJSONObject = subscribedAPIs.getJSONObject(i);
                if (subscribedAPIsJSONObject.getString("context").equals(apiContext) &&
                        subscribedAPIsJSONObject.getString("version").equals(apiVersion)) {
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
     * @param swagger The swagger object of the invoked API
     * @param payload The payload of the JWT token
     * @throws APISecurityException in case of scope validation failure
     */
    private void validateScopes(MessageContext synCtx, Swagger swagger, JSONObject payload) throws APISecurityException {
        String resourceScope = SwaggerUtils.getScopesOfResource(swagger, synCtx);

        if (StringUtils.isNotBlank(resourceScope) && payload.getString("scope") != null) {
            String[] tokenScopes = payload.getString("scope").split(" ");
            boolean scopeFound = false;
            for (String scope : tokenScopes) {
                if (scope.trim().equals(resourceScope)) {
                    scopeFound = true;
                    break;
                }
            }
            if (!scopeFound) {
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
        }
        log.debug("Scope validation passed.");
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
        long currentTime = System.currentTimeMillis() / 1000;
        long expiredTime = payload.getLong("exp");
        if (currentTime > expiredTime) {
            // Expired token is moved from valid token cache to the invalid token cache
            if (isGatewayTokenCacheEnabled) {
                getGatewayTokenCache().remove(tokenSignature);
                getInvalidTokenCache().put(tokenSignature, tenantDomain);
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED,
                    "JWT token is expired");
        }
        log.debug("Token is not expired.");
    }

    /**
     * Validate the JWT token grant type against the resource authentication scheme.
     *
     * @param grantType            The JWT token grant type
     * @param authenticationScheme The resource authentication scheme of the invoked API resource
     * @throws APISecurityException in case of token grant type validation failure
     */
    private void validateTokenGrantType(String grantType, String authenticationScheme) throws APISecurityException {
        if ((APIConstants.GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType) &&
                APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authenticationScheme)) ||
                (APIConstants.GRANT_TYPE_PASSWORD.equals(grantType) &&
                        APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authenticationScheme))) {
            // 1) tokens of client credentials grant type are not allowed to access when
            // the resource authentication is set to Application User only
            // 2) tokens of password grant type are not allowed to access when
            // the resource authentication is set to Application only
            if (log.isDebugEnabled()) {
                log.debug("Token grant type(" + grantType + ") does not allow to access the resource.");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE,
                    APISecurityConstants.getAuthenticationFailureMessage(APISecurityConstants.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE));
        }
        log.debug("Token grant type validation passed.");
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwtToken The JWT token sent with the API request
     * @param username The username of the user to whom the token is issued
     * @return whether the signature is verified or or not
     * @throws APISecurityException in case of signature verification failure
     */
    private boolean verifyTokenSignature(String jwtToken, String username) throws APISecurityException {
        Certificate publicCert;

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = APIUtil.getTenantId(username);

        //get tenant's key store manager
        try {
            APIUtil.loadTenantRegistry(tenantId);
        } catch (RegistryException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error in loading tenant registry", e);
        }
        KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

        KeyStore keyStore;
        // Retrieve the public certificate from the key store
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            //derive key store name
            String ksName = tenantDomain.trim().replace('.', '-');
            String jksName = ksName + ".jks";
            try {
                keyStore = tenantKSM.getKeyStore(jksName);
            } catch (Exception e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "Error in retrieving key store", e);
            }
            try {
                publicCert = keyStore.getCertificate(tenantDomain);
            } catch (KeyStoreException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "Error in retrieving public certificate from key store", e);
            }
        } else {
            try {
                publicCert = tenantKSM.getDefaultPrimaryCertificate();
            } catch (Exception e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "Error in retrieving public certificate from key store", e);
            }
        }

        if (publicCert != null) {
            // Retrieve public key from the certificate
            RSAPublicKey publicKey = (RSAPublicKey) publicCert.getPublicKey();
            try {
                SignedJWT signedJWT = SignedJWT.parse(jwtToken);
                JWSVerifier verifier = new RSASSAVerifier(publicKey);
                // Verify the signature
                return signedJWT.verify(verifier);
            } catch (ParseException | JOSEException e) {
                log.debug("Signature verification failed.", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token", e);
            }
        } else {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Couldn't find a public certificate to verify signature");
        }
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
}