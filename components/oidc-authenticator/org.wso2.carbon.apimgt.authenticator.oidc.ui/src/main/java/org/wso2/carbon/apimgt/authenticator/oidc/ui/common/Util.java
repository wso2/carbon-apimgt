/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.authenticator.oidc.ui.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the utility methods required by OIDC Authenticator module.
 */
public class Util {

    private static Log log = LogFactory.getLog(Util.class);

    private static String serviceProviderId = null;
    private static String identityProviderURI = null;
    private static String authorizationEndpointURI = null;
    private static String tokenEndpointURI = null;
    private static String userInfoURI = null;
    private static String jwksURI = null;
    private static String clientId = null;
    private static String clientSecret = null;
    private static String responseType = null;
    private static String authorizationType = null;
    private static String scope = null;
    private static String redirectURI = null;
    private static String loginPage = "/carbon/admin/login.jsp";
    private static boolean initSuccess = false;


    /**
     * Sets the OIDC config parameters during the server start-up by reading
     * authenticators.xml
     */
    public static boolean initOIDCConfigParams() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration
                .getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(OIDCConstants.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            Map<String, String> parameters = authenticatorConfig.getParameters();

            serviceProviderId = parameters.get(OIDCConstants.SERVICE_PROVIDER_ID);
            identityProviderURI = parameters.get(OIDCConstants.IDENTITY_PROVIDER_URI);
            authorizationEndpointURI = parameters.get(OIDCConstants.AUTHORIZATION_ENDPOINT_URI);
            tokenEndpointURI = parameters.get(OIDCConstants.TOKEN_ENDPOINT_URI);
            userInfoURI = parameters.get(OIDCConstants.USER_INFO_URI);
            jwksURI = parameters.get(OIDCConstants.JWKS_URL);

            clientId = parameters.get(OIDCConstants.CLIENT_ID);
            clientSecret = parameters.get(OIDCConstants.CLIENT_SECRET);
            responseType = parameters.get(OIDCConstants.CLIENT_RESPONSE_TYPE);
            authorizationType = parameters.get(OIDCConstants.CLIENT_AUTHORIZATION_TYPE);
            scope = parameters.get(OIDCConstants.CLIENT_SCOPE);
            redirectURI = parameters.get(OIDCConstants.CLIENT_REDIRECT_URI);

            initSuccess = true;
        }
        return initSuccess;
    }

    /**
     * checks whether authenticator enable ot disable
     *
     * @return True/False
     */
    public static boolean isAuthenticatorEnabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration
                .getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(OIDCConstants.AUTHENTICATOR_NAME);
        // if the authenticator is disabled, then do not register the servlet filter.
        return (authenticatorConfig == null) ? false : !authenticatorConfig.isDisabled();
    }


    /**
     * Building authentication request
     * @param nonce cryptographically random nonce
     * @param state cryptographically random state
     * @return url
     */
    public static String buildAuthRequestUrl(String nonce, String state) {

        try {
            log.debug("Building Authentication request...");
            URIBuilder uriBuilder = new URIBuilder(authorizationEndpointURI);

            uriBuilder.addParameter(OIDCConstants.PARAM_RESPONSE_TYPE, responseType);
            uriBuilder.addParameter(OIDCConstants.PARAM_CLIENT_ID, clientId);
            uriBuilder.addParameter(OIDCConstants.PARAM_SCOPE, scope);
            uriBuilder.addParameter(OIDCConstants.PARAM_REDIRECT_URI, redirectURI);
            uriBuilder.addParameter(OIDCConstants.PARAM_NONCE, nonce);
            uriBuilder.addParameter(OIDCConstants.PARAM_STATE, state);

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            log.error("Build Auth Request Failed", e);
        }
        return null;
    }

    /**
     * Create a cryptographically random nonce/state and return
     * @return randomString
     */
    public static String createRandomString() {
        return new BigInteger(50, new SecureRandom()).toString(16);
    }

    public static String getLoginPage() {
        return loginPage;
    }

    public static String getIdentityProviderURI() {
        return identityProviderURI;
    }

    public static String getServiceProviderId() {
        return serviceProviderId;
    }

    public static String getTokenEndpointURI() {
        return tokenEndpointURI;
    }

    public static String getUserInfoURI() {
        return userInfoURI;
    }

    public static String getJwksURI() {
        return jwksURI;
    }

    public static String getClientId() {
        return clientId;
    }

    public static String getClientSecret() {
        return clientSecret;
    }

    public static String getAuthorizationType() {
        return authorizationType;
    }

}
