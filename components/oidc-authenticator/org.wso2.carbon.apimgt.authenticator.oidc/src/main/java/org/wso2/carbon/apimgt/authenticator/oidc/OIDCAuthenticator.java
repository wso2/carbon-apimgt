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
package org.wso2.carbon.apimgt.authenticator.oidc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.*;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.apimgt.authenticator.oidc.common.AuthenticationToken;
import org.wso2.carbon.apimgt.authenticator.oidc.common.ServerConfiguration;
import org.wso2.carbon.apimgt.authenticator.oidc.common.AuthClient;
import org.wso2.carbon.apimgt.authenticator.oidc.internal.OIDCAuthBEDataHolder;
import org.wso2.carbon.apimgt.authenticator.oidc.util.Util;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;


public class OIDCAuthenticator implements CarbonServerAuthenticator {

    private static final int DEFAULT_PRIORITY_LEVEL = 3;
    private static final String AUTHENTICATOR_NAME = OIDCAuthenticatorBEConstants.OIDC_AUTHENTICATOR_NAME;

    public static final Log log = LogFactory.getLog(OIDCAuthenticator.class);

    /**
     * Login method
     *
     * @param code  code value
     * @param nonce nonce value
     * @return user name of authenticated user
     */
    public String login(String code, String nonce) {

        String userName;

        try {

            HttpSession httpSession = getHttpSession();
            RealmService realmService = OIDCAuthBEDataHolder.getInstance().getRealmService();
            RegistryService registryService = OIDCAuthBEDataHolder.getInstance().getRegistryService();

            ServerConfiguration serverConfiguration = getServerConfiguration();
            AuthClient authClient = getClientConfiguration();
            String jsonResponse = getTokenFromTokenEP(serverConfiguration, authClient, code);
            AuthenticationToken oidcAuthenticationToken = getAuthenticationToken(jsonResponse);
            userName = getUserName(oidcAuthenticationToken, serverConfiguration);

            if (userName == null || userName.equals("")) {
                log.error("Authentication Request is rejected. "
                        + "User Name is Null");
                return null;
            }

            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);


            // Start Authentication
            handleAuthenticationStarted(tenantId);
            if (isResponseSignatureValidationEnabled()) {

                boolean isSignatureValid = validateSignature(serverConfiguration, authClient,
                        oidcAuthenticationToken, nonce);

                if (!isSignatureValid) {
                    log.error("Authentication Request is rejected. "
                            + " Signature validation failed.");
                    CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, userName, tenantId,
                            "OIDC Authentication",
                            "Invalid Signature");
                    handleAuthenticationCompleted(tenantId, false);
                    return null;
                }
            }

            userName = MultitenantUtils.getTenantAwareUsername(userName);
            UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService,
                    tenantDomain);

            // Starting Authorization
            PermissionUpdateUtil.updatePermissionTree(tenantId);
            boolean isAuthorized = realm.getAuthorizationManager().isUserAuthorized(userName,
                    "/permission/admin/login", CarbonConstants.UI_PERMISSION_ACTION);
            if (isAuthorized) {
                CarbonAuthenticationUtil.onSuccessAdminLogin(httpSession, userName,
                        tenantId, tenantDomain, "OIDC Authentication");
                handleAuthenticationCompleted(tenantId, true);
            } else {
                log.error("Authentication Request is rejected. Authorization Failure.");
                CarbonAuthenticationUtil.onFailedAdminLogin(httpSession, userName, tenantId,
                        "OIDC Authentication", "Invalid credential");
                handleAuthenticationCompleted(tenantId, false);
                return null;
            }
        } catch (Exception e) {
            String msg = "System error while Authenticating/Authorizing User : " + e.getMessage();
            log.error(msg, e);
            return null;
        }

        return userName;
    }

    /**
     * HTTP post against token endpoint of OIDC server.
     *
     * @param serverConfiguration ServerConfiguration
     * @param code                code
     * @return json String
     * @throws IOException
     */
    private String getTokenFromTokenEP(ServerConfiguration serverConfiguration, AuthClient authClient,
                                       String code) throws IOException {

        // Client details
        String clientId = authClient.getClientId();
        String clientSecret = authClient.getClientSecret();
        String authorizationType = authClient.getAuthorizationType();
        String redirectURI = authClient.getRedirectURI();


        HttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(serverConfiguration.getTokenEndpointUri());

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        nvps.add(new BasicNameValuePair("grant_type", authorizationType));
        nvps.add(new BasicNameValuePair("code", code));
        nvps.add(new BasicNameValuePair("redirect_uri", redirectURI));
        post.setEntity(new UrlEncodedFormEntity(nvps));


        post.setHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", Base64.
                encode(String.format("%s:%s", clientId, clientSecret))).trim());

        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        String jsonString = "";
        String line;
        while ((line = rd.readLine()) != null) {
            jsonString = jsonString + line;
            log.debug("Response from Token Endpoint : " + jsonString);
        }
        return jsonString;
    }

    /**
     * Create AuthClient bean to hold client information
     * @return AuthClient
     */
    private AuthClient getClientConfiguration() {
        AuthClient authClient = new AuthClient();

        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(
                        OIDCAuthenticatorBEConstants.OIDC_AUTHENTICATOR_NAME);

        Map<String, String> parameters = authenticatorConfig.getParameters();


        // Client details
        authClient.setClientId(parameters.get(OIDCAuthenticatorBEConstants.
                CLIENT_ID));
        authClient.setClientSecret(parameters.get(OIDCAuthenticatorBEConstants.
                CLIENT_SECRET));
        authClient.setAuthorizationType(parameters.get(OIDCAuthenticatorBEConstants.
                CLIENT_AUTHORIZATION_TYPE));
        authClient.setRedirectURI(parameters.get(OIDCAuthenticatorBEConstants.
                CLIENT_REDIRECT_URI));
        authClient.setClientAlgorithm(parameters.get(OIDCAuthenticatorBEConstants.
                CLIENT_ALGORITHM));

        return authClient;

    }


    /**
     * Get OIDC Server Configuration
     * @return ServerConfiguration
     */
    private ServerConfiguration getServerConfiguration() {

        ServerConfiguration serverConfiguration = new ServerConfiguration();

        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.
                getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration.
                getAuthenticatorConfig(OIDCAuthenticatorBEConstants.OIDC_AUTHENTICATOR_NAME);

        Map<String, String> parameters = authenticatorConfig.getParameters();
        serverConfiguration.setIssuer(parameters.
                get(OIDCAuthenticatorBEConstants.IDENTITY_PROVIDER_URI));
        serverConfiguration.setJwksUri(parameters.
                get(OIDCAuthenticatorBEConstants.JWKS_URL));
        serverConfiguration.setUserInfoUri(parameters.
                get(OIDCAuthenticatorBEConstants.USER_INFO_URI));
        serverConfiguration.setTokenEndpointUri(parameters.
                get(OIDCAuthenticatorBEConstants.TOKEN_ENDPOINT_URI));

        return serverConfiguration;
    }


    private AuthenticationToken getAuthenticationToken(String jsonTokenResponse)
            throws Exception {

        JsonElement jsonRoot = new JsonParser().parse(jsonTokenResponse);
        if (!jsonRoot.isJsonObject()) {
            throw new Exception("Token Endpoint did not return a JSON object: " + jsonRoot);
        }
        JsonObject tokenResponse = jsonRoot.getAsJsonObject();

        if (tokenResponse.get("error") != null) {

            // Handle error
            String error = tokenResponse.get("error").getAsString();
            log.error("Token Endpoint returned: " + error);
            throw new Exception("Unable to obtain Access Token.  Token Endpoint returned: " + error);

        } else {

            // get out all the token strings
            String accessTokenValue;
            String idTokenValue;
            String refreshTokenValue = null;

            if (tokenResponse.has("access_token")) {
                accessTokenValue = tokenResponse.get("access_token").getAsString();
            } else {
                throw new Exception("Token Endpoint did not return an access_token: " +
                        jsonTokenResponse);
            }

            if (tokenResponse.has("id_token")) {
                idTokenValue = tokenResponse.get("id_token").getAsString();
            } else {
                log.error("Token Endpoint did not return an id_token");
                throw new Exception("Token Endpoint did not return an id_token");
            }

            if (tokenResponse.has("refresh_token")) {
                refreshTokenValue = tokenResponse.get("refresh_token").getAsString();
            }

            return new AuthenticationToken(idTokenValue,
                    accessTokenValue, refreshTokenValue);

        }
    }


    private String getUserName(AuthenticationToken authenticationToken, ServerConfiguration serverConfiguration) throws Exception {

        String userName;


        String userInfoJson = Util.getUserInfo(serverConfiguration, authenticationToken);

        JsonElement jsonRoot = new JsonParser().parse(userInfoJson);

        if (!jsonRoot.isJsonObject()) {
            log.error("User Info Json did not return a JSON object: " + jsonRoot);
            throw new Exception("User Info Json did not return a JSON object: " + jsonRoot);
        }

        JsonObject jsonResponse = jsonRoot.getAsJsonObject();

        if (jsonResponse.has("preferred_username")) {
            userName = jsonResponse.get("preferred_username").getAsString();
            log.debug("User name taken from user info endpoint : " + userName);
        } else {
            throw new Exception("User Info JSON did not return an preferred_username");
        }
        return userName;
    }


    private boolean validateSignature(ServerConfiguration serverConfiguration, AuthClient authClient,
                                      AuthenticationToken oidcAuthenticationToken, String nonce) throws Exception {

        boolean isSignatureValid;
        JWT idToken = JWTParser.parse(oidcAuthenticationToken.getIdTokenValue());
        ReadOnlyJWTClaimsSet idClaims = idToken.getJWTClaimsSet();

        // Supports only signedJWT
        if (idToken instanceof SignedJWT) {
            SignedJWT signedIdToken = (SignedJWT) idToken;
            isSignatureValid = Util.verifySignature(signedIdToken, serverConfiguration);

        } else if (idToken instanceof PlainJWT) {
            log.error("Plain JWT not supported");
            throw new Exception("Plain JWT not supported");

        } else {
            log.error("JWT type not supported");
            throw new Exception("JWT type not supported");
        }

        boolean isValidClaimSet = Util.validateIdClaims(serverConfiguration, authClient, idToken, nonce, idClaims);
        return  isSignatureValid && isValidClaimSet;
    }


    private void handleAuthenticationStarted(int tenantId) {
        BundleContext bundleContext = OIDCAuthBEDataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                            AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        }
    }

    private void handleAuthenticationCompleted(int tenantId, boolean isSuccessful) {
        BundleContext bundleContext = OIDCAuthBEDataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                            AuthenticationObserver.class.getName(), null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).completedAuthentication(
                            tenantId, isSuccessful);
                }
            }
            tracker.close();
        }
    }

    public void logout() {
        String loggedInUser;
        String delegatedBy;
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat date = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss,SSSS']'");
        HttpSession session = getHttpSession();

        if (session != null) {
            loggedInUser = (String) session.getAttribute(ServerConstants.USER_LOGGED_IN);
            delegatedBy = (String) session.getAttribute("DELEGATED_BY");
            if (delegatedBy == null) {
                log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime));
            } else {
                log.info("'" + loggedInUser + "' logged out at " + date.format(currentTime)
                        + " delegated by " + delegatedBy);
            }
            session.invalidate();
        }
    }

    public boolean isHandle(MessageContext messageContext) {
        return false;
    }

    public boolean isAuthenticated(MessageContext messageContext) {
        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        HttpSession httpSession = request.getSession();
        String loginStatus = (String) httpSession.getAttribute(ServerConstants.USER_LOGGED_IN);

        return (loginStatus != null);
    }

    public boolean authenticateWithRememberMe(MessageContext messageContext) {
        return false;
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);

        return authenticatorConfig != null && authenticatorConfig.isDisabled();
    }

    /**
     * Check whether signature validation is enabled in the authenticators.xml configuration file
     *
     * @return false only if SSOAuthenticator configuration has the parameter
     * <Parameter name="ResponseSignatureValidationEnabled">false</Parameter>, true otherwise
     */
    private boolean isResponseSignatureValidationEnabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration
                .getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            String responseSignatureValidation = authenticatorConfig
                    .getParameters()
                    .get(OIDCAuthenticatorBEConstants.PropertyConfig.RESPONSE_SIGNATURE_VALIDATION_ENABLED);
            if (responseSignatureValidation != null
                    && responseSignatureValidation.equalsIgnoreCase("false")) {
                if (log.isDebugEnabled()) {
                    log.debug("Signature validation is disabled in the configuration");
                }
                return false;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Signature validation is enabled in the configuration");
        }
        return true;
    }


    private HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSession = null;
        if (msgCtx != null) {
            HttpServletRequest request =
                    (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSession = request.getSession();
        }
        return httpSession;
    }


}
