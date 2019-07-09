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
import io.swagger.models.Path;
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
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
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

    public AuthenticationContext authenticate(String jwtToken, MessageContext synCtx, Swagger swagger)
            throws APISecurityException {
        String[] splitToken = jwtToken.split("\\.");
        if (splitToken.length != 3) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
        }
        JSONObject payload = null;
        boolean isVerified = false;
        String tokenSignature = splitToken[2];

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

        String cacheKey = getAccessTokenCacheKey(tokenSignature, apiContext, apiVersion, matchingResource, httpMethod);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

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
            payload = new JSONObject(new String(Base64Utils.decode(splitToken[1])));
            isVerified = verifyToken(jwtToken, payload.getString("sub"));
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
            if (isGatewayTokenCacheEnabled && getGatewayKeyCache().get(cacheKey) != null) {
                // Token is found in the key cache
                payload = (JSONObject) getGatewayKeyCache().get(cacheKey);
                checkTokenExpiration(tokenSignature, payload, tenantDomain);
            } else {
                // Retrieve payload from token
                if (payload == null) {
                    payload = new JSONObject(new String(Base64Utils.decode(splitToken[1])));
                }
                checkTokenExpiration(tokenSignature, payload, tenantDomain);

                // Scope validation
                String resourceScope = null;
                Map<String, Object> vendorExtensions = getVendorExtensions(synCtx, swagger);

                if (vendorExtensions != null) {
                    resourceScope = (String) vendorExtensions.get(APIConstants.SWAGGER_X_SCOPE);
                }

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

                if (isGatewayTokenCacheEnabled) {
                    getGatewayKeyCache().put(cacheKey, payload);
                }
            }

            JSONObject applicationObj = (JSONObject) payload.get("application");

            JSONObject api = null;

            if (payload.get("subscribedAPIs") != null) {
                // Subscription validation
                JSONArray subscribedAPIs = (JSONArray) payload.get("subscribedAPIs");
                for (int i = 0; i < subscribedAPIs.length(); i++) {
                    JSONObject subscribedAPIsJSONObject = subscribedAPIs.getJSONObject(i);
                    if (subscribedAPIsJSONObject.getString("context").equals(apiContext) &&
                            subscribedAPIsJSONObject.getString("version").equals(apiVersion)) {
                        api = subscribedAPIsJSONObject;
                        break;
                    }
                }
                if (api == null) {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                            "User is not subscribed to access the API: " + apiContext + ", version: " + apiVersion);
                }
            }

            // Generate authentication context
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
                authContext.setSpikeArrestLimit(api.getInt("spikeArrestLimit"));
                if (!JSONObject.NULL.equals(api.get("spikeArrestUnit"))) {
                    authContext.setSpikeArrestUnit(api.getString("spikeArrestUnit"));
                }
                authContext.setStopOnQuotaReach(api.getBoolean("stopOnQuotaReach"));
            }

            return authContext;
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
    }
    
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
    }

    private boolean verifyToken(String jwtToken, String username) throws APISecurityException {
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
            return verifySignature(jwtToken, publicCert);
        } else {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Couldn't find a public certificate to verify signature");
        }
    }

    private boolean verifySignature(String jwtToken, Certificate publicCert) throws APISecurityException {
        RSAPublicKey publicKey = (RSAPublicKey) publicCert.getPublicKey();
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtToken);
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            try {
                return signedJWT.verify(verifier);
            } catch (JOSEException e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        "Invalid JWT token", e);
            }
        } catch (ParseException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Invalid JWT token", e);
        }
    }

    private Map<String, Object> getVendorExtensions(MessageContext synCtx, Swagger swagger) {
        if (swagger != null) {
            String apiElectedResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            String httpMethod = (String) axis2MessageContext.getProperty(APIConstants.DigestAuthConstants.HTTP_METHOD);
            Path path = swagger.getPath(apiElectedResource);
            if (path != null) {
                switch (httpMethod) {
                    case APIConstants.HTTP_GET:
                        return path.getGet().getVendorExtensions();
                    case APIConstants.HTTP_POST:
                        return path.getPost().getVendorExtensions();
                    case APIConstants.HTTP_PUT:
                        return path.getPut().getVendorExtensions();
                    case APIConstants.HTTP_DELETE:
                        return path.getDelete().getVendorExtensions();
                    case APIConstants.HTTP_HEAD:
                        return path.getHead().getVendorExtensions();
                    case APIConstants.HTTP_OPTIONS:
                        return path.getOptions().getVendorExtensions();
                    case APIConstants.HTTP_PATCH:
                        return path.getPatch().getVendorExtensions();
                }
            }
        }
        return null;
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
}