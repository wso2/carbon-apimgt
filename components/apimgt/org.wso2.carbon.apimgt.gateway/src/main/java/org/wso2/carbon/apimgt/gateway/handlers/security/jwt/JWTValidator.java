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
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

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

    public JWTValidator() {
    }

    public AuthenticationContext authenticate(String jwtToken, MessageContext synCtx) throws APISecurityException {
        String[] splitToken = jwtToken.split("\\.");
        if (splitToken.length != 3) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
        }

        boolean isVerified = verifySignature(jwtToken);

        if (isVerified) {
            JSONObject payload = new JSONObject(new String(Base64Utils.decode(splitToken[1])));
            JSONObject applicationObj = (JSONObject) payload.get("application");
            JSONArray subscribedAPIs = (JSONArray) payload.get("subscribedAPIs");

            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

            JSONObject api = null;
            for (int i = 0; i < subscribedAPIs.length(); i++) {
                JSONObject subscribedAPIsJSONObject = subscribedAPIs.getJSONObject(i);
                if (subscribedAPIsJSONObject.getString("context").equals(apiContext) &&
                        subscribedAPIsJSONObject.getString("version").equals(apiVersion)) {
                    api = subscribedAPIsJSONObject;
                    break;
                }
            }

            if (api != null) {
                AuthenticationContext authContext = new AuthenticationContext();
                authContext.setAuthenticated(true);
                authContext.setTier(api.getString("subscriptionTier"));
                authContext.setApiKey(payload.getString("jti"));
                authContext.setKeyType(payload.getString("keytype"));
                authContext.setUsername(payload.getString("sub"));
                authContext.setApplicationId(String.valueOf(applicationObj.getInt("id")));
                authContext.setApplicationName(applicationObj.getString("name"));
                authContext.setApplicationTier(applicationObj.getString("tier"));
                authContext.setSubscriber(payload.getString("sub"));
                authContext.setConsumerKey(payload.getString("consumerKey"));
                authContext.setSubscriberTenantDomain(api.getString("subscriberTenantDomain"));
                return authContext;
            } else {
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "User is not subscribed to the API : " + apiContext + ", version: " + apiVersion);
            }
        }
        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, "Invalid JWT token");
    }

    private boolean verifySignature(String jwtToken) throws APISecurityException {
        Certificate publicCert;

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

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

}