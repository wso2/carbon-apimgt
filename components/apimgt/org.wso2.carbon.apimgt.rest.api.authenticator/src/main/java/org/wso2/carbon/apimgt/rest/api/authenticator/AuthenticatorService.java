/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.authenticator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.configuration.models.MultiEnvironmentOverview;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.APIMAppConfigurationService;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.NewCookie;

/**
 * This class is used to mock the authenticator apis.
 */
public class AuthenticatorService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorAPI.class);

    private KeyManager keyManager;
    private SystemApplicationDao systemApplicationDao;
    private APIMConfigurationService apimConfigurationService;
    private APIMAppConfigurationService apimAppConfigurationService;

    /**
     * Constructor
     *
     * @param keyManager               KeyManager object
     * @param systemApplicationDao     systemApplicationDao object
     * @param apimConfigurationService apimConfigurationService object
     * @param apimAppConfigurationService   configuration ofject for app
     */
    public AuthenticatorService(KeyManager keyManager, SystemApplicationDao systemApplicationDao,
                                APIMConfigurationService apimConfigurationService,
                                APIMAppConfigurationService apimAppConfigurationService) {
        this.keyManager = keyManager;
        this.systemApplicationDao = systemApplicationDao;
        this.apimConfigurationService = apimConfigurationService;
        this.apimAppConfigurationService = apimAppConfigurationService;
    }

    /**
     * This method returns the details of a DCR application.
     *
     * @param appName Name of the application to be created
     * @return oAuthData - A JsonObject with DCR application details, scopes, auth endpoint, and SSO is enabled or not
     * @throws APIManagementException When creating DCR application fails
     */
    public JsonObject getAuthenticationConfigurations(String appName)
            throws APIManagementException {
        JsonObject oAuthData = new JsonObject();
        MultiEnvironmentOverview multiEnvironmentOverviewConfigs = apimConfigurationService
                .getEnvironmentConfigurations().getMultiEnvironmentOverview();
        boolean isMultiEnvironmentOverviewEnabled = multiEnvironmentOverviewConfigs.isEnabled();

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.REFRESH_GRANT_TYPE);

        if (isMultiEnvironmentOverviewEnabled) {
            grantTypes.add(KeyManagerConstants.JWT_GRANT_TYPE);
        }
        APIMAppConfigurations appConfigs = apimAppConfigurationService.getApimAppConfigurations();
        String callBackURL = appConfigs.getApimBaseUrl() + AuthenticatorConstants.AUTHORIZATION_CODE_CALLBACK_URL + appName;

        // Get scopes of the application
        String scopes = getApplicationScopes(appName);
        log.debug("Set scopes for {} application using swagger definition.", appName);

        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthApplicationInfo = createDCRApplication(appName, callBackURL, grantTypes);
            if (oAuthApplicationInfo != null) {
                log.debug("Created DCR Application successfully for {}.", appName);
                String oAuthApplicationClientId = oAuthApplicationInfo.getClientId();
                String oAuthApplicationCallBackURL = oAuthApplicationInfo.getCallBackURL();
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationClientId);
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationCallBackURL);
                oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES, scopes);
                oAuthData.addProperty(KeyManagerConstants.AUTHORIZATION_ENDPOINT,
                        appConfigs.getAuthorizationEndpoint());
                oAuthData.addProperty(AuthenticatorConstants.SSO_ENABLED, appConfigs.isSsoEnabled());
                oAuthData.addProperty(AuthenticatorConstants.MULTI_ENVIRONMENT_OVERVIEW_ENABLED, isMultiEnvironmentOverviewEnabled);
            } else {
                String errorMsg = "No information available in OAuth application.";
                log.error(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        } catch (APIManagementException e) {
            String errorMsg = "Error while creating the keys for OAuth application : " + appName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
        return oAuthData;
    }

    /**
     * This method returns the access tokens for a given application.
     *
     * @param appName           Name of the application which needs to get tokens
     * @param grantType         Grant type of the application
     * @param userName          User name of the user
     * @param password          Password of the user
     * @param refreshToken      Refresh token
     * @param validityPeriod    Validity period of tokens
     * @param authorizationCode Authorization Code
     * @return AccessTokenInfo - An object with the generated access token information
     * @throws APIManagementException When receiving access tokens fails
     */
    public AccessTokenInfo getTokens(String appName, String grantType,
                                     String userName, String password, String refreshToken,
                                     long validityPeriod, String authorizationCode, String assertion,
                                     IdentityProvider identityProvider)
            throws APIManagementException {
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        MultiEnvironmentOverview multiEnvironmentOverviewConfigs = apimConfigurationService
                .getEnvironmentConfigurations().getMultiEnvironmentOverview();
        boolean isMultiEnvironmentOverviewEnabled = multiEnvironmentOverviewConfigs.isEnabled();

        // Get scopes of the application
        String scopes = getApplicationScopes(appName);
        log.debug("Set scopes for {} application using swagger definition.", appName);
        // TODO: Get Consumer Key & Secret without creating a new app, from the IS side
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        log.debug("Received consumer key & secret for {} application.", appName);
        try {
            if (KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType)) {
                // Access token for authorization code grant type
                APIMAppConfigurations appConfigs = apimAppConfigurationService.getApimAppConfigurations();
                String callBackURL = appConfigs.getApimBaseUrl() + AuthenticatorConstants.AUTHORIZATION_CODE_CALLBACK_URL + appName;

                if (authorizationCode != null) {
                    // Get Access & Refresh Tokens
                    accessTokenRequest.setClientId(consumerKeySecretMap.get("CONSUMER_KEY"));
                    accessTokenRequest.setClientSecret(consumerKeySecretMap.get("CONSUMER_SECRET"));
                    accessTokenRequest.setGrantType(grantType);
                    accessTokenRequest.setAuthorizationCode(authorizationCode);
                    accessTokenRequest.setScopes(scopes);
                    accessTokenRequest.setValidityPeriod(validityPeriod);
                    accessTokenRequest.setCallbackURI(callBackURL);
                    accessTokenInfo = getKeyManager().getNewAccessToken(accessTokenRequest);
                } else {
                    String errorMsg = "No Authorization Code available.";
                    log.error(errorMsg, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                    throw new APIManagementException(errorMsg, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
                }
            } else if (KeyManagerConstants.PASSWORD_GRANT_TYPE.equals(grantType)) {
                // Access token for password code grant type
                accessTokenRequest = AuthUtil
                        .createAccessTokenRequest(userName, password, grantType, refreshToken,
                                null, validityPeriod, scopes,
                                consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
                accessTokenInfo = getKeyManager().getNewAccessToken(accessTokenRequest);
            } else if (KeyManagerConstants.REFRESH_GRANT_TYPE.equals(grantType)) {
                accessTokenRequest = AuthUtil
                        .createAccessTokenRequest(userName, password, grantType, refreshToken,
                                null, validityPeriod, scopes,
                                consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
                accessTokenInfo = getKeyManager().getNewAccessToken(accessTokenRequest);
            } else if (isMultiEnvironmentOverviewEnabled) { // JWT or Custom grant type
                accessTokenRequest.setClientId(consumerKeySecretMap.get("CONSUMER_KEY"));
                accessTokenRequest.setClientSecret(consumerKeySecretMap.get("CONSUMER_SECRET"));
                accessTokenRequest.setAssertion(assertion);
                // Pass grant type to extend a custom grant instead of JWT grant in the future
                accessTokenRequest.setGrantType(KeyManagerConstants.JWT_GRANT_TYPE);
                accessTokenRequest.setScopes(scopes);
                accessTokenRequest.setValidityPeriod(validityPeriod);
                accessTokenInfo = getKeyManager().getNewAccessToken(accessTokenRequest);

                String usernameFromJWT = getUsernameFromJWT(accessTokenInfo.getIdToken());
                try {
                    identityProvider.getIdOfUser(usernameFromJWT);
                } catch (IdentityProviderException e) {
                    String errorMsg = "User " + usernameFromJWT + " does not exists in this environment.";
                    throw new APIManagementException(errorMsg, e, ExceptionCodes.USER_NOT_AUTHENTICATED);
                }
            }
        } catch (KeyManagementException e) {
            String errorMsg = "Error while receiving tokens for OAuth application : " + appName;
            log.error(errorMsg, e, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }

        log.debug("Received access token for {} application.", appName);
        return accessTokenInfo;
    }

    /**
     * This method revokes the access token.
     *
     * @param appName     Name of the application
     * @param accessToken Access token to be revoked
     * @throws APIManagementException When revoking access token fails
     */
    public void revokeAccessToken(String appName, String accessToken) throws APIManagementException {
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        getKeyManager().revokeAccessToken(accessToken,
                consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
    }

    /**
     * This method sets access token data.
     *
     * @param accessTokenInfo Information of the access token
     * @return AuthResponseBean - An object with access token data
     * @throws KeyManagementException When parsing JWT fails
     */
    public AuthResponseBean getResponseBeanFromTokenInfo(AccessTokenInfo accessTokenInfo)
            throws KeyManagementException {
        String authUser = null;
        if (accessTokenInfo.getIdToken() != null) {
            authUser = getUsernameFromJWT(accessTokenInfo.getIdToken());
        }
        if (authUser == null) {
            authUser = AuthenticatorConstants.ADMIN_USER;
        }

        AuthResponseBean responseBean = new AuthResponseBean();
        responseBean.setTokenValid(true);
        responseBean.setAuthUser(authUser);
        responseBean.setScopes(accessTokenInfo.getScopes());
        responseBean.setType(AuthenticatorConstants.BEARER_PREFIX);
        responseBean.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        responseBean.setIdToken(accessTokenInfo.getIdToken());

        return responseBean;
    }

    /**
     * Setup cookies and authentication response with dividing access token
     *
     * @param cookies          A map of cookies to be populated
     * @param authResponseBean Authentication response bean to be populated
     * @param accessToken      Access Token
     * @param contextPaths     Map of context paths
     */
    public void setupAccessTokenParts(Map<String, NewCookie> cookies, AuthResponseBean authResponseBean,
                                      String accessToken, Map<String, String> contextPaths, boolean isSsoEnabled) {
        String accessTokenPart1 = accessToken.substring(0, accessToken.length() / 2);
        String accessTokenPart2 = accessToken.substring(accessToken.length() / 2);

        // First part of the access token is set in payload
        authResponseBean.setPartialToken(accessTokenPart1);

        // Second part of the access token is set in cookies
        String environmentName = apimConfigurationService.getEnvironmentConfigurations().getEnvironmentLabel();
        NewCookie restAPIContextCookie = AuthUtil.cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J,
                accessTokenPart2, contextPaths.get(AuthenticatorConstants.Context.REST_API_CONTEXT), true, true,
                "", environmentName);
        // Cookie should be set to the log out context in order to revoke the token when log out happens.
        NewCookie logoutContextCookie = AuthUtil.cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2, accessTokenPart2,
                contextPaths.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT), true, true,
                "", environmentName);

        cookies.put(AuthenticatorConstants.Context.REST_API_CONTEXT, restAPIContextCookie);
        cookies.put(AuthenticatorConstants.Context.LOGOUT_CONTEXT, logoutContextCookie);

        if (isSsoEnabled) {
            String authUser = authResponseBean.getAuthUser();
            NewCookie authUserCookie = AuthUtil.cookieBuilder(AuthenticatorConstants.AUTH_USER, authUser,
                    contextPaths.get(AuthenticatorConstants.Context.APP_CONTEXT), true, false,
                    "", environmentName);
            cookies.put(AuthenticatorConstants.AUTH_USER, authUserCookie);
        }
    }

    /**
     * Setup cookies and authentication response with dividing refresh token
     *
     * @param cookies      A map of cookies to be populated
     * @param refreshToken Refresh Token
     * @param contextPaths Map of context paths
     */
    public void setupRefreshTokenParts(Map<String, NewCookie> cookies, String refreshToken,
                                       Map<String, String> contextPaths) {
        String refreshTokenPart1 = refreshToken.substring(0, refreshToken.length() / 2);
        String refreshTokenPart2 = refreshToken.substring(refreshToken.length() / 2);
                /* Note:
                Two parts of the Refresh Token should be stored in two cookies where JS accessible cookie
                is stored with `/{appName}` (i:e /publisher) path, because JS will trigger a token refresh call
                anywhere under `/publisher` context.Other part of the Refresh token cookie should set with the
                path directive as `/login/token/{appName}` (i:e /login/token/publisher) because the token request call
                will be send to `/login/token/{appName}` endpoint originated from `/{appName}`
                * */
        String environmentName = apimConfigurationService.getEnvironmentConfigurations().getEnvironmentLabel();
        NewCookie refreshTokenCookie = AuthUtil.cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_1, refreshTokenPart1,
                contextPaths.get(AuthenticatorConstants.Context.APP_CONTEXT), true, false,
                "", environmentName);
        NewCookie refreshTokenHttpOnlyCookie = AuthUtil.cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_2,
                refreshTokenPart2, contextPaths.get(AuthenticatorConstants.Context.LOGIN_CONTEXT), true, true,
                "", environmentName);

        cookies.put(AuthenticatorConstants.Context.APP_CONTEXT, refreshTokenCookie);
        cookies.put(AuthenticatorConstants.Context.LOGIN_CONTEXT, refreshTokenHttpOnlyCookie);
    }

    /**
     * Get the URI for the redirection to the UI Service
     *
     * @param appName          Name of the Application
     * @param authResponseBean Authentication response bean
     * @return URI of the UI Service
     * @throws UnsupportedEncodingException encoding format not supported
     * @throws URISyntaxException syntax of url is incorrect.
     */
    public URI getUIServiceRedirectionURI(String appName, AuthResponseBean authResponseBean)
            throws URISyntaxException,UnsupportedEncodingException {
        String uiServiceUrl;
        //The first host in the list "allowedHosts" is the host of UI-Service
        String uiServiceHost = apimConfigurationService.getEnvironmentConfigurations()
                .getAllowedHosts().get(0);

        if (StringUtils.isEmpty(uiServiceHost)) {
            uiServiceUrl = apimAppConfigurationService.getApimAppConfigurations().getApimBaseUrl();
        } else {
            uiServiceUrl = AuthenticatorConstants.HTTPS_PROTOCOL + AuthenticatorConstants.PROTOCOL_SEPARATOR +
                    uiServiceHost + AuthenticatorConstants.URL_PATH_SEPARATOR;
        }
        log.debug("Read UI Service url from configurations. value: {}", uiServiceUrl);

        if (authResponseBean == null) {
            return new URI(uiServiceUrl + appName);
        }

        String authResponseBeanData = new StringBuilder()
                .append("user_name=").append(authResponseBean.getAuthUser())
                .append("&id_token=").append(authResponseBean.getIdToken())
                .append("&partial_token=").append(authResponseBean.getPartialToken())
                .append("&scopes=").append(authResponseBean.getScopes())
                .append("&validity_period=").append(authResponseBean.getValidityPeriod()).toString();
        String Uri = new StringBuilder(uiServiceUrl).append(appName).append("/login?")
                .append(URLEncoder.encode(authResponseBeanData, "UTF-8")
                        .replaceAll("\\+", "%20").replaceAll("%26", "&")
                        .replaceAll("%3D", "=")).toString();
        return new URI(Uri);
    }

    /**
     * This method returns the consumer key & secret of a DCR application.
     *
     * @param appName Name of the DCR application
     * @return Map with consumer key & secret
     * @throws APIManagementException When creating DCR application fails
     */
    private Map<String, String> getConsumerKeySecret(String appName) throws APIManagementException {
        HashMap<String, String> consumerKeySecretMap;
        if (!AuthUtil.getConsumerKeySecretMap().containsKey(appName)) {
            consumerKeySecretMap = new HashMap<>();
            List<String> grantTypes = new ArrayList<>();
            grantTypes.add(KeyManagerConstants.PASSWORD_GRANT_TYPE);
            grantTypes.add(KeyManagerConstants.REFRESH_GRANT_TYPE);
            OAuthApplicationInfo oAuthApplicationInfo;
            oAuthApplicationInfo = createDCRApplication(appName, "http://temporary.callback/url", grantTypes);

            consumerKeySecretMap.put(AuthenticatorConstants.CONSUMER_KEY, oAuthApplicationInfo.getClientId());
            consumerKeySecretMap.put(AuthenticatorConstants.CONSUMER_SECRET, oAuthApplicationInfo.getClientSecret());

            AuthUtil.getConsumerKeySecretMap().put(appName, consumerKeySecretMap);
            return consumerKeySecretMap;
        } else {
            return AuthUtil.getConsumerKeySecretMap().get(appName);
        }
    }

    /**
     * This method returns the scopes for a given application.
     *
     * @param appName Name the application
     * @return scopes - A space separated list of scope keys
     * @throws APIManagementException When retrieving scopes from swagger definition fails
     */
    private String getApplicationScopes(String appName) throws APIManagementException {
        String scopes;
        String applicationRestAPI = null;
        if (AuthenticatorConstants.STORE_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getStoreRestAPIResource();
        } else if (AuthenticatorConstants.PUBLISHER_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getPublisherRestAPIResource();
        } else if (AuthenticatorConstants.ADMIN_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getAdminRestAPIResource();
        }
        try {
            if (applicationRestAPI != null) {
                APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
                Map<String, Scope> applicationScopesMap = apiDefinitionFromSwagger20
                        .getScopesFromSecurityDefinitionForWebApps(applicationRestAPI);
                scopes = String.join(" ", applicationScopesMap.keySet());
                // Set openid scope
                if (StringUtils.isEmpty(scopes)) {
                    scopes = KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
                } else {
                    scopes = scopes + " " + KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
                }
            } else {
                String errorMsg = "Error while getting application rest API resource.";
                log.error(errorMsg, ExceptionCodes.INTERNAL_ERROR);
                throw new APIManagementException(errorMsg, ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (APIManagementException e) {
            String errorMsg = "Error while reading scopes from swagger definition.";
            log.error(errorMsg, e, ExceptionCodes.INTERNAL_ERROR);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.INTERNAL_ERROR);
        }
        return scopes;
    }

    /**
     * This method creates a DCR application.
     *
     * @param clientName  Name of the application to be created
     * @param callBackURL Call back URL of the application
     * @param grantTypes  List of grant types of the application
     * @return OAUthApplicationInfo - An object with DCR Application information
     * @throws APIManagementException When creating DCR application fails
     */
    private OAuthApplicationInfo createDCRApplication(String clientName, String callBackURL, List<String> grantTypes)
            throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            // Here the keyType:"Application" will be passed as a default value
            // for the oAuthAppRequest constructor argument.
            // This value is not related to DCR application creation.
            OAuthAppRequest oAuthAppRequest = new OAuthAppRequest(clientName,
                    callBackURL, AuthenticatorConstants.APPLICATION_KEY_TYPE, grantTypes);
            if (systemApplicationDao.isConsumerKeyExistForApplication(clientName)) {
                String consumerKey = systemApplicationDao.getConsumerKeyForApplication(clientName);
                oAuthApplicationInfo = getKeyManager().retrieveApplication(consumerKey);
            } else {
                oAuthApplicationInfo = getKeyManager().createApplication(oAuthAppRequest);
                if (oAuthApplicationInfo != null) {
                    systemApplicationDao.addApplicationKey(clientName, oAuthApplicationInfo.getClientId());
                }
            }
        } catch (KeyManagementException | APIMgtDAOException e) {
            String errorMsg = "Error while creating the keys for OAuth application : " + clientName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
        return oAuthApplicationInfo;
    }

    private String getUsernameFromJWT(String jwt) throws KeyManagementException {
        if (jwt != null && jwt.contains(".")) {
            String[] jwtParts = jwt.split("\\.");
            JWTTokenPayload jwtHeader = new Gson().fromJson(new String(Base64.getDecoder().decode(jwtParts[1]),
                    StandardCharsets.UTF_8), JWTTokenPayload.class);

            // Removing "@carbon.super" part explicitly (until IS side is fixed to drop it)
            String username = jwtHeader.getSub();
            username = username.replace("@carbon.super", "");
            return username;
        } else {
            log.error("JWT Parsing failed. Invalid JWT: " + jwt);
            throw new KeyManagementException("JWT Parsing failed. Invalid JWT.", ExceptionCodes.JWT_PARSING_FAILED);
        }
    }

    protected KeyManager getKeyManager() {
        return keyManager;
    }

    /**
     * Represents Payload of JWT.
     */
    private class JWTTokenPayload {
        private String sub;
        private String iss;
        private String exp;
        private String iat;
        private String[] aud;

        public String getSub() {
            return sub;
        }

        public String getIss() {
            return iss;
        }

        public String getExp() {
            return exp;
        }

        public String getIat() {
            return iat;
        }

        public String[] getAud() {
            return aud;
        }
    }
}
