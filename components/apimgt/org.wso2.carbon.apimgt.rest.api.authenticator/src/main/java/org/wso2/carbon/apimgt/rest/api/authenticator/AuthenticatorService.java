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
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to mock the authenticator apis.
 */
public class AuthenticatorService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorAPI.class);

    private KeyManager keyManager;

    /**
     * Constructor.
     * @param keyManager KeyManager object
     */
    public AuthenticatorService(KeyManager keyManager) {
        this.keyManager = keyManager;
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
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.REFRESH_GRANT_TYPE);
        APIMAppConfigurations appConfigs = ServiceReferenceHolder.getInstance().getAPIMAppConfiguration();
        String callBackURL = appConfigs.getApimBaseUrl() + AuthenticatorConstants.AUTHORIZATION_CODE_CALLBACK_URL + appName;
        // Get scopes of the application
        String scopes = getApplicationScopes(appName);
        if (log.isDebugEnabled()) {
            log.debug("Set scopes for " + appName + " application using swagger definition.");
        }
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthApplicationInfo = createDCRApplication(appName, callBackURL, grantTypes);
            if (oAuthApplicationInfo != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Created DCR Application successfully for " + appName + ".");
                }
                String oAuthApplicationClientId = oAuthApplicationInfo.getClientId();
                String oAuthApplicationCallBackURL = oAuthApplicationInfo.getCallBackURL();
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationClientId);
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationCallBackURL);
                oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES, scopes);
                oAuthData.addProperty(KeyManagerConstants.AUTHORIZATION_ENDPOINT,
                        appConfigs.getAuthorizationEndpoint());
                oAuthData.addProperty(AuthenticatorConstants.SSO_ENABLED, appConfigs.isSsoEnabled());
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
     * @param appName Name of the application which needs to get tokens
     * @param requestURL Request URL with the authorization code
     * @param grantType Grant type of the application
     * @param userName User name of the user
     * @param password Password of the user
     * @param refreshToken Refresh token
     * @param validityPeriod Validity period of tokens
     * @return AccessTokenInfo - An object with the generated access token information
     * @throws APIManagementException When receiving access tokens fails
     */
    public AccessTokenInfo getTokens(String appName, String requestURL, String grantType,
                                     String userName, String password, String refreshToken, long validityPeriod)
            throws APIManagementException {
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        // Get scopes of the application
        String scopes = getApplicationScopes(appName);
        if (log.isDebugEnabled()) {
            log.debug("Set scopes for " + appName + " application using swagger definition.");
        }
        // TODO: Get Consumer Key & Secret without creating a new app, from the IS side
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        if (log.isDebugEnabled()) {
            log.debug("Received consumer key & secret for " + appName + " application.");
        }
        try {
            if (KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType)) {
                // Access token for authorization code grant type
                APIMAppConfigurations appConfigs = ServiceReferenceHolder.getInstance()
                        .getAPIMAppConfiguration();
                String callBackURL = appConfigs.getApimBaseUrl() + AuthenticatorConstants.AUTHORIZATION_CODE_CALLBACK_URL + appName;
                // Get the Authorization Code
                if (requestURL.contains("code=")) {
                    String requestURLQueryParameters = requestURL.split("\\?")[1];
                    String authorizationCode = requestURLQueryParameters.split("=")[1].split("&")[0];
                    if (log.isDebugEnabled()) {
                        log.debug("Authorization Code for the app " + appName + ": " + authorizationCode);
                    }
                    // Get Access & Refresh Tokens
                    accessTokenRequest.setClientId(consumerKeySecretMap.get("CONSUMER_KEY"));
                    accessTokenRequest.setClientSecret(consumerKeySecretMap.get("CONSUMER_SECRET"));
                    accessTokenRequest.setGrantType(grantType);
                    accessTokenRequest.setAuthorizationCode(authorizationCode);
                    accessTokenRequest.setScopes(scopes);
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
            }
        } catch (KeyManagementException e) {
            String errorMsg = "Error while receiving tokens for OAuth application : " + appName;
            log.error(errorMsg, e, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.ACCESS_TOKEN_GENERATION_FAILED);
        }
        return accessTokenInfo;
    }

    /**
     * This method revokes the access token.
     *
     * @param appName Name of the application
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
     * @param responseBean Contains access token data
     * @param accessTokenInfo Information of the access token
     * @return AuthResponseBean - An object with access token data
     * @throws KeyManagementException When parsing JWT fails
     */
    public AuthResponseBean setAccessTokenData(AuthResponseBean responseBean, AccessTokenInfo accessTokenInfo)
            throws KeyManagementException {
        responseBean.setTokenValid(true);
        if (accessTokenInfo.getIdToken() != null) {
            responseBean.setAuthUser(getUsernameFromJWT(accessTokenInfo.getIdToken()));
        }
        responseBean.setScopes(accessTokenInfo.getScopes());
        responseBean.setType(AuthenticatorConstants.BEARER_PREFIX);
        responseBean.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        responseBean.setIdToken(accessTokenInfo.getIdToken());
        return responseBean;
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
        String scopes = "";
        String applicationRestAPI = null;
        if (AuthenticatorConstants.STORE_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getStoreRestAPIResource();
        } else if (AuthenticatorConstants.STORE_NEW_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getStoreRestAPIResource();
        } else if (AuthenticatorConstants.PUBLISHER_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getPublisherRestAPIResource();
        } else if (AuthenticatorConstants.ADMIN_APPLICATION.equals(appName)) {
            applicationRestAPI = RestApiUtil.getAdminRestAPIResource();
        }
        //Todo: when all swaggers modified with no vendor extension, following swagger parser should be modified.
        //todo: for now only publisher swagger have been modified for no vendor extensions
        try {
            if (applicationRestAPI != null) {
                APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
                if (AuthenticatorConstants.PUBLISHER_APPLICATION.equals(appName)) {
                    apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
                    Map<String, String> scopesPub = apiDefinitionFromSwagger20.getScope(applicationRestAPI);
                    scopes = String.join(" ", scopesPub.keySet());
                } else {
                    Map<String, Scope> applicationScopesMap = null;
                    applicationScopesMap = apiDefinitionFromSwagger20.getScopes(applicationRestAPI);
                    scopes = String.join(" ", applicationScopesMap.keySet());
                }
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
     * @param clientName Name of the application to be created
     * @param callBackURL Call back URL of the application
     * @param grantTypes List of grant types of the application
     * @return OAUthApplicationInfo - An object with DCR Application information
     * @throws APIManagementException When creating DCR application fails
     */
    private OAuthApplicationInfo createDCRApplication(String clientName, String callBackURL, List grantTypes)
            throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            // Here the keyType:"Application" will be passed as a default value
            // for the oAuthAppRequest constructor argument.
            // This value is not related to DCR application creation.
            OAuthAppRequest oAuthAppRequest = new OAuthAppRequest(clientName,
                    callBackURL, AuthenticatorConstants.APPLICATION_KEY_TYPE, grantTypes);
            oAuthApplicationInfo = getKeyManager().createApplication(oAuthAppRequest);
        } catch (KeyManagementException e) {
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
            throw new KeyManagementException("JWT Parsing failed. Invalid JWT.");
        }
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

    protected KeyManager getKeyManager() {
        return keyManager;
    }
}
