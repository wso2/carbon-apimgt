/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axis2.util.URL;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.oltu.oauth2.common.OAuth;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ScopeDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClientPool;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager.
 */
public class AMDefaultKeyManagerImpl extends AbstractKeyManager {

    private static final Log log = LogFactory.getLog(AMDefaultKeyManagerImpl.class);
    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";
    private CloseableHttpClient kmHttpClient;
    private SubscriberKeyMgtClientPool subscriberKeyMgtClientPool;
    private AccessTokenGenerator accessTokenGenerator;
    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {

        // OAuthApplications are created by calling to APIKeyMgtSubscriber Service
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        // Subscriber's name should be passed as a parameter, since it's under the subscriber the OAuth App is created.
        String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.
                OAUTH_CLIENT_USERNAME);
        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);
        String callBackURL = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_CALLBACK_URL);
        if (keyType != null) {
            applicationName = applicationName + '_' + keyType;
        }

        if (log.isDebugEnabled()) {
            log.debug("Trying to create OAuth application :" + applicationName);
        }

        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String[] tokenScopes = new String[1];
        tokenScopes[0] = tokenScope;

        org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = null;

        try {
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationToCreate =
                    new org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo();
            applicationToCreate.setIsSaasApplication(oAuthApplicationInfo.getIsSaasApplication());
            applicationToCreate.setCallBackURL(callBackURL);
            applicationToCreate.setClientName(applicationName);
            applicationToCreate.setAppOwner(userId);
            applicationToCreate.setJsonString(oAuthApplicationInfo.getJsonString());
            applicationToCreate.setTokenType(oAuthApplicationInfo.getTokenType());
            info = createOAuthApplicationbyApplicationInfo(applicationToCreate);
        } catch (Exception e) {
            handleException("Can not create OAuth application  : " + applicationName, e);
        }

        if (info == null || info.getJsonString() == null) {
            handleException("OAuth app does not contains required data  : " + applicationName,
                    new APIManagementException("OAuth app does not contains required data"));
        }

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        oAuthApplicationInfo.setClientName(info.getClientName());
        oAuthApplicationInfo.setClientId(info.getClientId());
        oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
        oAuthApplicationInfo.setClientSecret(info.getClientSecret());
        oAuthApplicationInfo.setIsSaasApplication(info.getIsSaasApplication());

        try {
            JSONObject jsonObject = new JSONObject(info.getJsonString());

            if (jsonObject.has(ApplicationConstants.
                    OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_REDIRECT_URIS, jsonObject.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_NAME)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_NAME, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_GRANT, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            }
        } catch (JSONException e) {
            handleException("Can not retrieve information of the created OAuth application", e);
        }

        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();

        try {

            String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
            String[] grantTypes = null;
            if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT) != null) {
                grantTypes = ((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT))
                        .split(",");
            }
            String applicationName = oAuthApplicationInfo.getClientName();
            String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);

            if (keyType != null) {
                applicationName = applicationName + "_" + keyType;
            }
            log.debug("Updating OAuth Client with ID : " + oAuthApplicationInfo.getClientId());

            if (log.isDebugEnabled() && oAuthApplicationInfo.getCallBackURL() != null) {
                log.debug("CallBackURL : " + oAuthApplicationInfo.getCallBackURL());
            }

            if (log.isDebugEnabled() && applicationName != null) {
                log.debug("Client Name : " + applicationName);
            }
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationInfo = updateOAuthApplication(userId,
                    applicationName, oAuthApplicationInfo.getCallBackURL(), oAuthApplicationInfo.getClientId(),
                    grantTypes);
            OAuthApplicationInfo newAppInfo = new OAuthApplicationInfo();
            newAppInfo.setClientId(applicationInfo.getClientId());
            newAppInfo.setCallBackURL(applicationInfo.getCallBackURL());
            newAppInfo.setClientSecret(applicationInfo.getClientSecret());
            newAppInfo.setJsonString(applicationInfo.getJsonString());

            return newAppInfo;
        } catch (Exception e) {
            handleException("Error occurred while updating OAuth Client : ", e);
        }
        return null;
    }

    @Override
    public OAuthApplicationInfo updateApplicationOwner(OAuthAppRequest appInfoDTO, String owner)
            throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();
        String userId = oAuthApplicationInfo.getAppOwner();

        try {
            String applicationName = oAuthApplicationInfo.getClientName();
            String[] grantTypes = null;
            if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT) != null) {
                grantTypes = ((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT))
                        .split(",");
            }
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationInfo = updateOAuthApplicationOwner(
                    userId, owner, applicationName,
                    oAuthApplicationInfo.getCallBackURL(), oAuthApplicationInfo.getClientId(), grantTypes);
            OAuthApplicationInfo newAppInfo = new OAuthApplicationInfo();
            newAppInfo.setAppOwner(applicationInfo.getAppOwner());
            newAppInfo.setClientId(applicationInfo.getClientId());
            newAppInfo.setCallBackURL(applicationInfo.getCallBackURL());
            newAppInfo.setClientSecret(applicationInfo.getClientSecret());
            newAppInfo.setJsonString(applicationInfo.getJsonString());
            return newAppInfo;
        } catch (Exception e) {
            handleException("Error occurred while updating OAuth application owner to " + userId, e);
        }
        return null;
    }

    @Override
    public void deleteApplication(String consumerKey) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Trying to delete OAuth application for consumer key :" + consumerKey);
        }

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = subscriberKeyMgtClientPool.get();
            keyMgtClient.deleteOAuthApplication(consumerKey);
        } catch (Exception e) {
            handleException("Can not remove service provider for the given consumer key : " + consumerKey, e);
        } finally {
            if (keyMgtClient != null) {
                subscriberKeyMgtClientPool.release(keyMgtClient);
            }
        }
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        try {
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = getOAuthApplication(consumerKey);

            if (info == null || info.getClientId() == null) {
                return null;
            }
            oAuthApplicationInfo.setClientName(info.getClientName());
            oAuthApplicationInfo.setClientId(info.getClientId());
            oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
            oAuthApplicationInfo.setClientSecret(info.getClientSecret());

            JSONObject jsonObject = new JSONObject(info.getJsonString());

            if (jsonObject.has(ApplicationConstants.
                    OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_REDIRECT_URIS, jsonObject.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_NAME)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_NAME, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_GRANT, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            }

        } catch (Exception e) {
            handleException("Can not retrieve OAuth application for the given consumer key : " + consumerKey, e);
        }
        return oAuthApplicationInfo;
    }

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest)
            throws APIManagementException {

        String newAccessToken;
        long validityPeriod;
        AccessTokenInfo tokenInfo = null;

        if (tokenRequest == null) {
            log.warn("No information available to generate Token.");
            return null;
        }

        String tokenEndpoint = getConfigurationParamValue(APIConstants.TOKEN_URL);
        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = getConfigurationParamValue(APIConstants.REVOKE_URL);
        URL keyMgtURL = new URL(tokenEndpoint);
        int keyMgtPort = keyMgtURL.getPort();
        String keyMgtProtocol = keyMgtURL.getProtocol();

        // Call the /revoke only if there's a token to be revoked.
        try {
            if (tokenRequest.getTokenToRevoke() != null && !tokenRequest.getTokenToRevoke().isEmpty()) {
                URL revokeEndpointURL = new URL(revokeEndpoint);
                String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
                int revokeEndpointPort = revokeEndpointURL.getPort();

                HttpPost httpRevokePost = new HttpPost(revokeEndpoint);

                // Request parameters.
                List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
                revokeParams.add(new BasicNameValuePair("token", tokenRequest.getTokenToRevoke()));

                //Revoke the Old Access Token
                httpRevokePost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
                int statusCode;
                String responseBody;
                try {
                    HttpResponse revokeResponse = executeHTTPrequest(revokeEndpointPort, revokeEndpointProtocol,
                            httpRevokePost);
                    statusCode = revokeResponse.getStatusLine().getStatusCode();
                    responseBody = EntityUtils.toString(revokeResponse.getEntity());
                } finally {
                    httpRevokePost.reset();
                }

                if (statusCode != 200) {
                    String errorReason = "Token revoke failed : HTTP error code : " + statusCode + ". Reason "
                            + responseBody;
                    throw new APIManagementException(errorReason);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
                    }
                }
            }

            // When validity time set to a negative value, a token is considered never to expire.
            if (tokenRequest.getValidityPeriod() == OAuthConstants.UNASSIGNED_VALIDITY_PERIOD) {
                // Setting a different -ve value if the set value is -1 (-1 will be ignored by TokenValidator)
                tokenRequest.setValidityPeriod(-2L);
            }

            //Generate New Access Token
            HttpPost httpTokpost = new HttpPost(tokenEndpoint);
            List<NameValuePair> tokParams = new ArrayList<>(3);
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_VALUE));
            if (tokenRequest.getValidityPeriod() != 0) {
                tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                        Long.toString(tokenRequest.getValidityPeriod())));
            }
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));

            String scopes = String.join(" ", tokenRequest.getScope());
            tokParams.add(new BasicNameValuePair("scope", scopes));

            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            try {
                HttpResponse tokResponse = executeHTTPrequest(keyMgtPort, keyMgtProtocol, httpTokpost);
                HttpEntity tokEntity = tokResponse.getEntity();

                if (tokResponse.getStatusLine().getStatusCode() != 200) {
                    throw new APIManagementException("Error occurred while calling token endpoint: HTTP error code : " +
                            tokResponse.getStatusLine().getStatusCode());
                } else {
                    tokenInfo = new AccessTokenInfo();
                    String responseStr = EntityUtils.toString(tokEntity);
                    JSONObject obj = new JSONObject(responseStr);
                    newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                    validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                    if (obj.has("scope")) {
                        tokenInfo.setScope(((String) obj.get("scope")).split(" "));
                    }
                    tokenInfo.setAccessToken(newAccessToken);
                    tokenInfo.setValidityPeriod(validityPeriod);
                }
            } finally {
                httpTokpost.reset();
            }
        } catch (ClientProtocolException e) {
            handleException("Error while creating token - Invalid protocol used", e);
        } catch (UnsupportedEncodingException e) {
            handleException("Error while preparing request for token/revoke APIs", e);
        } catch (IOException e) {
            handleException("Error while creating tokens - " + e.getMessage(), e);
        } catch (JSONException e) {
            handleException("Error while parsing response from token api", e);
        }

        return tokenInfo;
    }

    @Override
    public String getNewApplicationConsumerSecret(AccessTokenRequest tokenRequest) throws APIManagementException {

        OAuthAdminService oauthAdminService = new OAuthAdminService();
        OAuthConsumerAppDTO appDTO;
        try {
            if (oauthAdminService != null) {
                appDTO = oauthAdminService.updateAndRetrieveOauthSecretKey(tokenRequest.getClientId());
                return appDTO.getOauthConsumerSecret();
            }
        } catch (IdentityOAuthAdminException e) {
            handleException("Error while generating new consumer secret", e);
        }
        return null;
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken token = requestDTO.new OAuth2AccessToken();

        token.setIdentifier(accessToken);
        token.setTokenType("bearer");
        requestDTO.setAccessToken(token);

        OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] contextParams =
                new OAuth2TokenValidationRequestDTO.TokenValidationContextParam[1];
        requestDTO.setContext(contextParams);

        OAuth2ClientApplicationDTO clientApplicationDTO = findOAuthConsumerIfTokenIsValid(requestDTO);
        OAuth2TokenValidationResponseDTO responseDTO = clientApplicationDTO.getAccessTokenValidationResponse();

        if (!responseDTO.isValid()) {
            tokenInfo.setTokenValid(responseDTO.isValid());
            log.error("Invalid OAuth Token : " + responseDTO.getErrorMsg());
            tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            return tokenInfo;
        }

        tokenInfo.setTokenValid(responseDTO.isValid());
        tokenInfo.setEndUserName(responseDTO.getAuthorizedUser());
        tokenInfo.setConsumerKey(clientApplicationDTO.getConsumerKey());

        // Convert Expiry Time to milliseconds.
        if (responseDTO.getExpiryTime() == Long.MAX_VALUE) {
            tokenInfo.setValidityPeriod(Long.MAX_VALUE);
        } else {
            tokenInfo.setValidityPeriod(responseDTO.getExpiryTime() * 1000L);
        }

        tokenInfo.setIssuedTime(System.currentTimeMillis());
        tokenInfo.setScope(responseDTO.getScope());

        // If token has am_application_scope, consider the token as an Application token.
        String[] scopes = responseDTO.getScope();
        String applicationTokenScope = getConfigurationElementValue(APIConstants.APPLICATION_TOKEN_SCOPE);

        if (scopes != null && applicationTokenScope != null && !applicationTokenScope.isEmpty()) {
            if (Arrays.asList(scopes).contains(applicationTokenScope)) {
                tokenInfo.setApplicationToken(true);
            }
        }

        if (checkAccessTokenPartitioningEnabled() &&
                checkUserNameAssertionEnabled()) {
            tokenInfo.setConsumerKey(
                    ApiMgtDAO.getInstance().getConsumerKeyForTokenWhenTokenPartitioningEnabled(accessToken));
        }

        return tokenInfo;
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {

        return configuration;
    }

    @Override
    public OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException {

        return null;
    }

    /**
     * This method will create a new record at CLIENT_INFO table by given OauthAppRequest.
     *
     * @param appInfoRequest oAuth application properties will contain in this object
     * @return OAuthApplicationInfo with created oAuth application details.
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest)
            throws APIManagementException {

        //initiate OAuthApplicationInfo
        OAuthApplicationInfo oAuthApplicationInfo = appInfoRequest.getOAuthApplicationInfo();

        String consumerKey = oAuthApplicationInfo.getClientId();
        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String[] tokenScopes = new String[1];
        tokenScopes[0] = tokenScope;
        String clientSecret = (String) oAuthApplicationInfo.getParameter("client_secret");
        oAuthApplicationInfo.setClientSecret(clientSecret);
        //for the first time we set default time period.
        oAuthApplicationInfo.addParameter(ApplicationConstants.VALIDITY_PERIOD,
                getConfigurationParamValue(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD));

        //check whether given consumer key and secret match or not. If it does not match throw an exception.
        org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = null;
        try {
            info = getOAuthApplication(oAuthApplicationInfo.getClientId());
            if (!clientSecret.equals(info.getClientSecret())) {
                throw new APIManagementException("The secret key is wrong for the given consumer key " + consumerKey);
            }

        } catch (Exception e) {
            handleException("Some thing went wrong while getting OAuth application for given consumer key " +
                    oAuthApplicationInfo.getClientId(), e);
        }
        if (info != null && info.getClientId() == null) {
            return null;
        }

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        oAuthApplicationInfo.setClientName(info.getClientName());
        oAuthApplicationInfo.setClientId(info.getClientId());
        oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
        oAuthApplicationInfo.setClientSecret(info.getClientSecret());
        oAuthApplicationInfo.setIsSaasApplication(info.getIsSaasApplication());

        try {
            JSONObject jsonObject = new JSONObject(info.getJsonString());

            if (jsonObject.has(ApplicationConstants.
                    OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_REDIRECT_URIS, jsonObject.get(ApplicationConstants.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_NAME)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_NAME, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_NAME));
            }

            if (jsonObject.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(ApplicationConstants.
                        OAUTH_CLIENT_GRANT, jsonObject.get(ApplicationConstants.OAUTH_CLIENT_GRANT));
            }
        } catch (JSONException e) {
            handleException("Can not read information from the retrieved OAuth application", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }

        return oAuthApplicationInfo;
    }

    /**
     * This method initialize the HTTP Client and Connection Manager to call services in KeyManager.
     *
     * @throws APIManagementException if an error occurs while initializing HttpClient
     */
    protected void initializeHttpClient() throws APIManagementException {

        try {
            String authServerURL = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
            java.net.URL keyManagerURL = new java.net.URL(authServerURL);
            int keyManagerPort = keyManagerURL.getPort();
            String keyManagerProtocol = keyManagerURL.getProtocol();
            this.kmHttpClient = (CloseableHttpClient) APIUtil.getHttpClient(keyManagerPort, keyManagerProtocol);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Error while initializing HttpClient due to malformed URL", e);
        }
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        this.configuration = configuration;
        subscriberKeyMgtClientPool  = new SubscriberKeyMgtClientPool();
        subscriberKeyMgtClientPool.setConfiguration(configuration);//Initialize a Http Client and Connection Manager using the ServerURL of KM
        initializeHttpClient();
        String consumerKey = (String) configuration.getParameter(APIConstants.KEY_MANAGER_CONSUMER_KEY);
        String consumerSecret = (String) configuration.getParameter(APIConstants.KEY_MANAGER_CONSUMER_SECRET);
        String keyManagerServiceUrl = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
        String tokenEndpoint = keyManagerServiceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0].concat(
                "/oauth2/token");
        String revokeEndpoint = keyManagerServiceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0].concat(
                "/oauth2/revoke");
        accessTokenGenerator = new AccessTokenGenerator(tokenEndpoint,revokeEndpoint,consumerKey,consumerSecret);
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException {
//        //Register new resource means create new API with given Scopes.
        //todo commented below code because of blocker due to API publish fail. need to find a better way of doing this
//        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
//        apiMgtDAO.addAPI(api, CarbonContext.getThreadLocalCarbonContext().getTenantId());

        return true;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws APIManagementException {

        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws APIManagementException {

        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws APIManagementException {

    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getActiveTokensOfConsumerKey(consumerKey);
    }

    /**
     * Returns the access token information of the provided consumer key.
     *
     * @param consumerKey The consumer key.
     * @return AccessTokenInfo The access token information.
     * @throws APIManagementException
     */
    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        APIKey apiKey;
        try {
            apiKey = apiMgtDAO.getAccessTokenInfoByConsumerKey(consumerKey);
            if (apiKey != null) {
                tokenInfo.setAccessToken(apiKey.getAccessToken());
                tokenInfo.setConsumerSecret(apiKey.getConsumerSecret());
                tokenInfo.setValidityPeriod(apiKey.getValidityPeriod());
                tokenInfo.setScope(apiKey.getTokenScope().split("\\s"));
            } else {
                tokenInfo.setAccessToken("");
                //set default validity period
                tokenInfo.setValidityPeriod(3600);
            }
            tokenInfo.setConsumerKey(consumerKey);

        } catch (SQLException e) {
            handleException("Cannot retrieve information for the given consumer key : "
                    + consumerKey, e);
        } catch (CryptoException e) {
            handleException("Token decryption failed of an access token for the given consumer key : "
                    + consumerKey, e);
        }
        return tokenInfo;
    }

    @Override
    public Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString)
            throws APIManagementException {

        Map<String, Set<Scope>> apiToScopeMapping = new HashMap<>();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Set<String>> apiToScopeKeyMapping = apiMgtDAO.getScopesForAPIS(apiIdsString);
        for (String apiId : apiToScopeKeyMapping.keySet()) {
            Set<Scope> apiScopes = new LinkedHashSet<>();
            Set<String> scopeKeys = apiToScopeKeyMapping.get(apiId);
            for (String scopeKey : scopeKeys) {
                Scope scope = getScopeByName(scopeKey);
                apiScopes.add(scope);
            }
            apiToScopeMapping.put(apiId, apiScopes);
        }
        return apiToScopeMapping;
    }

    /**
     * Get scope management service tenant URL for given KM endpoint.
     *
     * @return Scope Management Service host URL (Eg:https://localhost:9444/api/identity/oauth2/v1.0/scopes)
     * @throws APIManagementException If a malformed km endpoint is provided
     */
    private String getScopeManagementServiceEndpoint() throws APIManagementException {

        String authServerURL = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
        if (StringUtils.isEmpty(authServerURL)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String scopeMgtTenantEndpoint = authServerURL.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
        if (StringUtils.isNoneEmpty(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            scopeMgtTenantEndpoint += "/t/" + tenantDomain;
        }
        scopeMgtTenantEndpoint += APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_BASE_PATH;
        return scopeMgtTenantEndpoint;
    }

    /**
     * This method will be used to register a Scope in the authorization server.
     *
     * @param scope        Scope to register
     * @throws APIManagementException if there is an error while registering a new scope.
     */
    @Override
    public void registerScope(Scope scope) throws APIManagementException {

        String accessToken = getAccessTokenForScopeMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint();
        String scopeKey = scope.getKey();
        try {
            HttpPost httpPost = new HttpPost(scopeEndpoint);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(scopeKey);
            scopeDTO.setDisplayName(scope.getName());
            scopeDTO.setDescription(scope.getDescription());
            if (scope.getRoles() != null) {
                scopeDTO.setBindings(Arrays.asList(scope.getRoles().split(",")));
            }
            StringEntity payload = new StringEntity(new Gson().toJson(scopeDTO));
            httpPost.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to register scope "
                        + scopeKey);
            }
            try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpPost)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_CREATED) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    throw new APIManagementException("Error occurred while registering scope: " + scopeKey + " via "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while registering scope: " + scopeKey + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * Read response body for HTTPResponse as a string.
     *
     * @param httpResponse HTTPResponse
     * @return Response Body String
     * @throws APIManagementException If an error occurs while reading the response
     */
    protected String readHttpResponseAsString(CloseableHttpResponse httpResponse) throws APIManagementException {

        try {
            HttpEntity entity = httpResponse.getEntity();
            String responseString = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            //release all resources held by the responseHttpEntity
            EntityUtils.consume(entity);
            return responseString;
        } catch (IOException e) {
            String errorMessage = "Error occurred while reading response body as string";
            throw new APIManagementException(errorMessage, e);
        }
    }


    /**
     * Get access token with scope management scopes for the tenant using the KM Mgt OAuth Application.
     *
     * @return Access Token
     */
    private String getAccessTokenForScopeMgt() throws APIManagementException {

        return accessTokenGenerator.getAccessToken(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_MGT_SCOPES);
    }

    /**
     * Construct Bearer Authorization header for AccessTokenInfo.
     *
     * @param accessToken Access Token
     * @return Bearer Authorization header
     */
    private String getBearerAuthorizationHeader(String accessToken) {

        return APIConstants.AUTHORIZATION_BEARER + accessToken;
    }

    /**
     * This method will be used to retrieve details of a Scope in the authorization server.
     *
     * @param name    Scope Name to retrieve
     * @return Scope object
     * @throws APIManagementException if an error while retrieving scope
     */
    @Override
    public Scope getScopeByName(String name) throws APIManagementException {

        ScopeDTO scopeDTO;
        String accessToken = getAccessTokenForScopeMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, name));
        HttpGet httpGet = new HttpGet(scopeEndpoint);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to get scope "
                    + name);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpGet)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String responseString = readHttpResponseAsString(httpResponse);
            if (statusCode == HttpStatus.SC_OK && StringUtils.isNoneEmpty(responseString)) {
                scopeDTO = new Gson().fromJson(responseString, ScopeDTO.class);
            } else {
                throw new APIManagementException("Error occurred while retrieving scope: " + name + " via "
                        + scopeEndpoint + ". Error Status: " + statusCode + ". Error Response: " + responseString);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while retrieving scope: " + name + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
        return fromDTOToScope(scopeDTO);
    }

    /**
     * Get Scope object from ScopeDTO response received from authorization server.
     *
     * @param scopeDTO ScopeDTO response
     * @return Scope model object
     */
    private Scope fromDTOToScope(ScopeDTO scopeDTO) {

        Scope scope = new Scope();
        scope.setName(scopeDTO.getDisplayName());
        scope.setKey(scopeDTO.getName());
        scope.setDescription(scopeDTO.getDescription());
        scope.setRoles(String.join(",", scopeDTO.getBindings()));
        return scope;
    }

    /**
     * Get Scope object list from ScopeDTO List response received from authorization server.
     *
     * @param scopeDTOList Scope DTO List
     * @return Scope Object to Scope Name Mappings
     */
    private Map<String, Scope> fromDTOListToScopeListMapping(List<ScopeDTO> scopeDTOList) {

        Map<String, Scope> scopeListMapping = new HashMap<>();
        for (ScopeDTO scopeDTO : scopeDTOList) {
            scopeListMapping.put(scopeDTO.getName(), fromDTOToScope(scopeDTO));
        }
        return scopeListMapping;
    }

    /**
     * This method will be used to retrieve all the scopes available in the authorization server for the given tenant
     * domain.
     *
     * @return Mapping of Scope object to scope key
     * @throws APIManagementException if an error occurs while getting scopes list
     */
    @Override
    public Map<String, Scope> getAllScopes() throws APIManagementException {

        List<ScopeDTO> allScopeDTOS;
        // Get access token
        String accessToken = getAccessTokenForScopeMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint();
        HttpGet httpGet = new HttpGet(scopeEndpoint);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to get scopes");
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpGet)) {
            String responseString = readHttpResponseAsString(httpResponse);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK && StringUtils.isNoneEmpty(responseString)) {
                Type scopeListType =
                        new TypeToken<ArrayList<ScopeDTO>>() {
                        }.getType();
                allScopeDTOS = new Gson().fromJson(responseString, scopeListType);
            } else {
                throw new APIManagementException("Error occurred while retrieving scopes via: " + scopeEndpoint
                        + ". Error Status: " + statusCode + " . Error Response: " + responseString);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while getting retrieving via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
        return fromDTOListToScopeListMapping(allScopeDTOS);
    }

    /**
     * This method will be used to attach a Scope in the authorization server to a API resource.
     *
     * @param api          API
     * @param uriTemplates URITemplate set with attached scopes
     * @throws APIManagementException if an error occurs while attaching scope to resource
     */
    @Override
    public void attachResourceScopes(API api, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        //TODO: remove after scope validation from swagger completes
        ApiMgtDAO.getInstance().addResourceScopes(api, uriTemplates, tenantDomain);
    }

    /**
     * This method will be used to update the local scopes and resource to scope attachments of an API in the
     * authorization server.
     *
     * @param api               API
     * @param oldLocalScopeKeys Old local scopes of the API before update (excluding the versioned local scopes
     * @param newLocalScopes    New local scopes of the API after update
     * @param oldURITemplates   Old URI templates of the API before update
     * @param newURITemplates   New URI templates of the API after update
     * @throws APIManagementException if fails to update resources scopes
     */
    @Override
    public void updateResourceScopes(API api, Set<String> oldLocalScopeKeys, Set<Scope> newLocalScopes,
                                     Set<URITemplate> oldURITemplates, Set<URITemplate> newURITemplates)
            throws APIManagementException {

        detachResourceScopes(api, oldURITemplates);
        // remove the old local scopes from the KM
        for (String oldScope : oldLocalScopeKeys) {
            deleteScope(oldScope);
        }
        //Register scopes
        for (Scope scope : newLocalScopes) {
            String scopeKey = scope.getKey();
            // Check if key already registered in KM. Scope Key may be already registered for a different version.
            if (!isScopeExists(scopeKey)) {
                //register scope in KM
                registerScope(scope);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Scope: " + scopeKey + " already registered in KM. Skipping registering scope.");
                }
            }
        }
        attachResourceScopes(api, newURITemplates);
    }

    /**
     * This method will be used to detach the resource scopes of an API and delete the local scopes of that API from
     * the authorization server.
     *
     * @param api          API   API
     * @param uriTemplates URITemplate Set with attach scopes to detach
     * @throws APIManagementException if an error occurs while detaching resource scopes of the API.
     */
    @Override
    public void detachResourceScopes(API api, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        //TODO: remove after scope validation from swagger completes
        ApiMgtDAO.getInstance().removeResourceScopes(api.getId(), api.getContext(), uriTemplates, tenantDomain);
    }

    /**
     * This method will be used to delete a Scope in the authorization server.
     *
     * @param scopeName    Scope name
     * @throws APIManagementException if an error occurs while deleting the scope
     */
    @Override
    public void deleteScope(String scopeName) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForScopeMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeName));

        HttpDelete httpDelete = new HttpDelete(scopeEndpoint);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to delete scope "
                    + scopeName);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpDelete)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                String responseString = readHttpResponseAsString(httpResponse);
                String errorMessage = "Error occurred while deleting scope: " + scopeName + " via: " + scopeEndpoint
                        + ". Error Status: " + statusCode + " . Error Response: " + responseString;
                throw new APIManagementException(errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while deleting scope: " + scopeName + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to update a Scope in the authorization server.
     *
     * @param scope        Scope object
     * @throws APIManagementException if an error occurs while updating the scope
     */
    @Override
    public void updateScope(Scope scope) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForScopeMgt();
        String scopeKey = scope.getKey();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeKey));
        try {
            HttpPut httpPut = new HttpPut(scopeEndpoint);
            httpPut.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
            httpPut.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setDisplayName(scope.getName());
            scopeDTO.setDescription(scope.getDescription());
            if (scope.getRoles() != null) {
                scopeDTO.setBindings(Arrays.asList(scope.getRoles().split(",")));
            }
            StringEntity payload = new StringEntity(new Gson().toJson(scopeDTO));
            httpPut.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to update scope "
                        + scopeKey);
            }
            try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpPut)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    throw new APIManagementException("Error occurred while updating scope: " + scopeKey + " via: "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while updating scope: " + scopeKey + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to check whether the a Scope exists for the given scope name in the authorization
     * server.
     *
     * @param scopeName    Scope Name
     * @return whether scope exists or not
     * @throws APIManagementException if an error occurs while checking the existence of the scope
     */
    @Override
    public boolean isScopeExists(String scopeName) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForScopeMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeName));

        HttpHead httpHead = new HttpHead(scopeEndpoint);
        httpHead.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to check scope existence of "
                    + scopeName);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpHead)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (statusCode != HttpStatus.SC_NOT_FOUND) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    String errorMessage = "Error occurred while checking existence of scope: " + scopeName + " via: "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString;
                    throw new APIManagementException(errorMessage);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Scope " + scopeName + " not found in authorization server " + scopeEndpoint);
                    }
                    return false;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Scope " + scopeName + " found in authorization server " + scopeEndpoint);
            }
            return true;
        } catch (IOException e) {
            String errorMessage = "Error occurred while checking existence of scope: " + scopeName + " via "
                    + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to validate the scope set provided and populate the additional parameters
     * (description and bindings) for each Scope object.
     *
     * @param scopes Scope set to validate
     * @throws APIManagementException if an error occurs while validating and populating
     */
    @Override
    public void validateScopes(Set<Scope> scopes) throws APIManagementException {

        for (Scope scope : scopes) {
            Scope sharedScope = getScopeByName(scope.getKey());
            scope.setName(sharedScope.getName());
            scope.setDescription(sharedScope.getDescription());
            scope.setRoles(sharedScope.getRoles());
        }
    }

    @Override
    public String getType() {

        return APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
    }

    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo createOAuthApplicationbyApplicationInfo(
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationToCreate) throws Exception {

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = subscriberKeyMgtClientPool.get();
            return keyMgtClient.createOAuthApplicationbyApplicationInfo(applicationToCreate);
        } finally {
            subscriberKeyMgtClientPool.release(keyMgtClient);
        }

    }

    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo updateOAuthApplication(String userId,
                                                                                               String applicationName,
                                                                                               String callBackURL,
                                                                                               String clientId,
                                                                                               String[] grantTypes)
            throws Exception {

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = subscriberKeyMgtClientPool.get();
            return keyMgtClient
                    .updateOAuthApplication(userId, applicationName, callBackURL, clientId, grantTypes);
        } finally {
            subscriberKeyMgtClientPool.release(keyMgtClient);
        }

    }

    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo updateOAuthApplicationOwner(
            String userId, String owner, String applicationName, String callBackURL, String clientId,
            String[] grantTypes) throws Exception {

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = subscriberKeyMgtClientPool.get();
            return keyMgtClient
                    .updateOAuthApplicationOwner(userId, owner, applicationName, callBackURL, clientId, grantTypes);
        } finally {
            subscriberKeyMgtClientPool.release(keyMgtClient);
        }
    }

    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo getOAuthApplication(String consumerKey)
            throws Exception {

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = subscriberKeyMgtClientPool.get();
            return keyMgtClient.getOAuthApplication(consumerKey);
        } finally {
            subscriberKeyMgtClientPool.release(keyMgtClient);
        }
    }

    /**
     * Executes the HTTP request and returns the response.
     *
     * @param port
     * @param protocol
     * @param httpPost Post payload
     * @return response
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected HttpResponse executeHTTPrequest(int port, String protocol, HttpPost httpPost) throws IOException {

        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);
        return httpClient.execute(httpPost);
    }

    /**
     * Returns the value of the provided APIM configuration element.
     *
     * @param property APIM configuration element name
     * @return APIM configuration element value
     */
    protected String getConfigurationElementValue(String property) {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getFirstProperty(property);
    }

    /**
     * Return the value of the provided configuration parameter.
     *
     * @param parameter Parameter name
     * @return Parameter value
     */
    protected String getConfigurationParamValue(String parameter) {

        return (String) configuration.getParameter(parameter);
    }

    /**
     * Returns the OAuth application details if the token is valid.
     *
     * @param requestDTO Token validation request
     * @return
     */
    protected OAuth2ClientApplicationDTO findOAuthConsumerIfTokenIsValid(OAuth2TokenValidationRequestDTO requestDTO) {

        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
        return oAuth2TokenValidationService.findOAuthConsumerIfTokenIsValid(requestDTO);
    }

    /**
     * Check whether Token partitioning is enabled.
     *
     * @return true/false
     */
    protected boolean checkAccessTokenPartitioningEnabled() {

        return APIUtil.checkAccessTokenPartitioningEnabled();
    }

    /**
     * Check whether user name assertion is enabled.
     *
     * @return true/false
     */
    protected boolean checkUserNameAssertionEnabled() {

        return APIUtil.checkUserNameAssertionEnabled();
    }

}
