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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClientPool;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class AMDefaultKeyManagerImpl extends AbstractKeyManager {

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";
    private static final String CONFIG_ELEM_OAUTH = "OAuth";

    private KeyManagerConfiguration configuration;

    private static final Log log = LogFactory.getLog(AMDefaultKeyManagerImpl.class);

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
                grantTypes = ((String)oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT))
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
                            applicationName, oAuthApplicationInfo.getCallBackURL(),oAuthApplicationInfo.getClientId(), 
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
    public void deleteApplication(String consumerKey) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Trying to delete OAuth application for consumer key :" + consumerKey);
        }

        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            keyMgtClient.deleteOAuthApplication(consumerKey);
        } catch (Exception e) {
            handleException("Can not remove service provider for the given consumer key : " + consumerKey, e);
        } finally {
            if (keyMgtClient != null) {
                SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
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
                try {
                    HttpResponse revokeResponse = executeHTTPrequest(revokeEndpointPort, revokeEndpointProtocol, 
                                                                     httpRevokePost);
                    statusCode = revokeResponse.getStatusLine().getStatusCode();
                } finally {
                    httpRevokePost.reset();
                }

                if (statusCode != 200) {
                    throw new APIManagementException("Token revoke failed : HTTP error code : " + statusCode);
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
            tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                    Long.toString(tokenRequest.getValidityPeriod())));
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

        //TODO: If these values are not set, validation will fail giving an NPE. Need to see why that happens
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam contextParam = requestDTO.new
                TokenValidationContextParam();
        contextParam.setKey("dummy");
        contextParam.setValue("dummy");

        OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] contextParams =
                new OAuth2TokenValidationRequestDTO.TokenValidationContextParam[1];
        contextParams[0] = contextParam;
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
            tokenInfo.setConsumerKey(ApiMgtDAO.getInstance().getConsumerKeyForTokenWhenTokenPartitioningEnabled(accessToken));
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

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        if (configuration != null) {
            this.configuration = configuration;
        } else {
            // If the provided configuration is null, read the Server-URL and other properties from
            // APIKeyValidator section.            

            /*
             * we need to read identity.xml here because we need to get default validity time for access_token in order
             * to set in semi-manual.
             */
            OMElement oauthElem = getOAuthConfigElement();

            String validityPeriod = null;

            if (oauthElem != null) {
                if (log.isDebugEnabled()) {
                    log.debug("identity configs have loaded. ");
                }
                // Primary/Secondary supported login mechanisms
                OMElement loginConfigElem =  oauthElem.getFirstChildWithName(getQNameWithIdentityNS("AccessTokenDefaultValidityPeriod"));
                validityPeriod = loginConfigElem.getText();
            }
            
            if (this.configuration == null) {
                this.configuration = new KeyManagerConfiguration();
                this.configuration.setManualModeSupported(true);
                this.configuration.setResourceRegistrationEnabled(true);
                this.configuration.setTokenValidityConfigurable(true);
                this.configuration.addParameter(APIConstants.AUTHSERVER_URL, getConfigurationElementValue(APIConstants
                        .KEYMANAGER_SERVERURL));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_USERNAME, 
                                                getConfigurationElementValue(APIConstants.API_KEY_VALIDATOR_USERNAME));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_PASSWORD, 
                                                getConfigurationElementValue(APIConstants.API_KEY_VALIDATOR_PASSWORD))
                ;
                this.configuration.addParameter(APIConstants.REVOKE_URL, getConfigurationElementValue(APIConstants
                        .REVOKE_API_URL));
                this.configuration.addParameter(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD,validityPeriod);
                String revokeUrl = getConfigurationElementValue(APIConstants.REVOKE_API_URL);

                // Read the revoke url and replace revoke part to get token url.
                String tokenUrl = revokeUrl != null ? revokeUrl.replace("revoke", "token") : null;
                this.configuration.addParameter(APIConstants.TOKEN_URL, tokenUrl);
            }
        }

        SubscriberKeyMgtClientPool.getInstance().setConfiguration(this.configuration);

    }

    private QName getQNameWithIdentityNS(String localPart) {
        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
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
    public Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString) throws APIManagementException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Set<Scope>> scopes = apiMgtDAO.getScopesForAPIS(apiIdsString);
        return scopes;
    }

    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo createOAuthApplicationbyApplicationInfo(
                      org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationToCreate) throws Exception {
        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            return keyMgtClient.createOAuthApplicationbyApplicationInfo(applicationToCreate);
        } finally {
            SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
        }

    }
    
    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo updateOAuthApplication(String userId, 
                  String applicationName, String callBackURL, String clientId, String[] grantTypes) throws Exception {
        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            return keyMgtClient
                    .updateOAuthApplication(userId, applicationName, callBackURL, clientId, grantTypes);
        } finally {
            SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
        }

    }
    
    protected org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo getOAuthApplication(String consumerKey)
                                                                                                    throws Exception {
        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            return keyMgtClient.getOAuthApplication(consumerKey);
        } finally {
            SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
        }
    }
    
    /**
     * Executes the HTTP request and returns the response
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
     * Returns the value of the provided APIM configuration element
     * @param property APIM configuration element name
     * @return APIM configuration element value
     */
    protected String getConfigurationElementValue(String property) {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(property);
    }
    
    /**
     * Return the value of the provided configuration parameter
     * @param parameter Parameter name
     * @return Parameter value
     */
    protected String getConfigurationParamValue(String parameter) {
        return configuration.getParameter(parameter);
    }
    
    
    /**
     * Returns the OAuth application details if the token is valid
     * @param requestDTO Token validation request
     * @return
     */
    protected OAuth2ClientApplicationDTO findOAuthConsumerIfTokenIsValid(OAuth2TokenValidationRequestDTO requestDTO) {
        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
        return oAuth2TokenValidationService.findOAuthConsumerIfTokenIsValid(requestDTO);
    }
    
    /**
     * Check whether Token partitioning is enabled
     * @return true/false
     */
    protected boolean checkAccessTokenPartitioningEnabled() {
        return APIUtil.checkAccessTokenPartitioningEnabled(); 
    }
    
    /**
     * Check whether user name assertion is enabled
     * @return true/false
     */
    protected boolean checkUserNameAssertionEnabled() {
        return APIUtil.checkUserNameAssertionEnabled();
    }
    
    /**
     * Returns OAuth Configuration from identity.xml
     * @return OAuth Configuration
     */
    protected OMElement getOAuthConfigElement() {
        return IdentityConfigParser.getInstance().getConfigElement(CONFIG_ELEM_OAUTH);
    }

}
