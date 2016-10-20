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
import org.apache.oltu.oauth2.common.OAuth;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClientPool;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
        if (keyType != null) {
            applicationName = applicationName + "_" + keyType;
        }

        if (log.isDebugEnabled()) {
            log.debug("Trying to create OAuth application :" + applicationName);
        }



        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String tokenScopes[] = new String[1];
        tokenScopes[0] = tokenScope;

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = null;
        SubscriberKeyMgtClient keyMgtClient = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationToCreate = new org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo();
            applicationToCreate.setIsSaasApplication(oAuthApplicationInfo.getIsSaasApplication());
            applicationToCreate.setCallBackURL(oAuthApplicationInfo.getCallBackURL());
            applicationToCreate.setClientName(applicationName);
            applicationToCreate.setAppOwner(userId);
            info = keyMgtClient.createOAuthApplicationbyApplicationInfo(applicationToCreate);
        } catch (Exception e) {
            handleException("Can not create OAuth application  : " + applicationName, e);
        } finally {
            SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
        }

        if (info == null || info.getJsonString() == null) {
            handleException("OAuth app does not contains required data  : " + applicationName,
                    new APIManagementException("OAuth app does not contains required data"));
        }

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
        SubscriberKeyMgtClient keyMgtClient = null;

        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();

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
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo applicationInfo = keyMgtClient
                    .updateOAuthApplication(userId, applicationName, oAuthApplicationInfo.getCallBackURL(),
                            oAuthApplicationInfo.getClientId(), grantTypes);
            OAuthApplicationInfo newAppInfo = new OAuthApplicationInfo();
            newAppInfo.setClientId(applicationInfo.getClientId());
            newAppInfo.setCallBackURL(applicationInfo.getCallBackURL());
            newAppInfo.setClientSecret(applicationInfo.getClientSecret());

            return newAppInfo;
        } catch (Exception e) {
            handleException("Error occurred while updating OAuth Client : ", e);
        } finally {
            if (keyMgtClient != null) {
                SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
            }
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
        //SubscriberKeyMgtClient keyMgtClient = APIUtil.getKeyManagementClient();

        SubscriberKeyMgtClient keyMgtClient = null;
        if (log.isDebugEnabled()) {
            log.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = keyMgtClient.getOAuthApplication(consumerKey);

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
        } finally {
            if (keyMgtClient != null) {
                SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
            }
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

        String tokenEndpoint = configuration.getParameter(APIConstants.TOKEN_URL);
        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = configuration.getParameter(APIConstants.REVOKE_URL);
        URL keyMgtURL = new URL(tokenEndpoint);
        int keyMgtPort = keyMgtURL.getPort();
        String keyMgtProtocol = keyMgtURL.getProtocol();

        // Call the /revoke only if there's a token to be revoked.
        try {
            if (tokenRequest.getTokenToRevoke() != null && !"".equals(tokenRequest.getTokenToRevoke())) {
                URL revokeEndpointURL = new URL(revokeEndpoint);
                String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
                int revokeEndpointPort = revokeEndpointURL.getPort();

                HttpClient revokeEPClient = APIUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);

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
                    HttpResponse revokeResponse = revokeEPClient.execute(httpRevokePost);
                    statusCode = revokeResponse.getStatusLine().getStatusCode();
                } finally {
                    httpRevokePost.reset();
                }

                if (statusCode != 200) {
                    throw new RuntimeException("Token revoke failed : HTTP error code : " + statusCode);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
                    }
                }
            }
            //get default application access token name from config.

            String applicationTokenScope = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                                            getAPIManagerConfiguration().getFirstProperty(APIConstants
                                                                            .APPLICATION_TOKEN_SCOPE);

            // When validity time set to a negative value, a token is considered never to expire.
            if (tokenRequest.getValidityPeriod() == OAuthConstants.UNASSIGNED_VALIDITY_PERIOD) {
                // Setting a different -ve value if the set value is -1 (-1 will be ignored by TokenValidator)
                tokenRequest.setValidityPeriod(-2);
            }

            //Generate New Access Token
            HttpClient tokenEPClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpPost httpTokpost = new HttpPost(tokenEndpoint);
            List<NameValuePair> tokParams = new ArrayList<NameValuePair>(3);
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_VALUE));
            tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                    Long.toString(tokenRequest.getValidityPeriod())));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
            StringBuilder builder = new StringBuilder();
            builder.append(applicationTokenScope);

            for (String scope : tokenRequest.getScope()) {
                builder.append(' ').append(scope);
            }

            tokParams.add(new BasicNameValuePair("scope", builder.toString()));

            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            try {
                HttpResponse tokResponse = tokenEPClient.execute(httpTokpost);
                HttpEntity tokEntity = tokResponse.getEntity();

                if (tokResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Error occurred while calling token endpoint: HTTP error code : " +
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
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
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

        OAuth2ClientApplicationDTO clientApplicationDTO = oAuth2TokenValidationService.findOAuthConsumerIfTokenIsValid
                (requestDTO);
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
            tokenInfo.setValidityPeriod(responseDTO.getExpiryTime() * 1000);
        }

        tokenInfo.setIssuedTime(System.currentTimeMillis());
        tokenInfo.setScope(responseDTO.getScope());

        // If token has am_application_scope, consider the token as an Application token.
        String[] scopes = responseDTO.getScope();
        String applicationTokenScope = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                                                getAPIManagerConfiguration().getFirstProperty(APIConstants.
                APPLICATION_TOKEN_SCOPE);

        if (scopes != null && applicationTokenScope != null && !applicationTokenScope.isEmpty()) {
            if (Arrays.asList(scopes).contains(applicationTokenScope)) {
                tokenInfo.setApplicationToken(true);
            }
        }

        if (APIUtil.checkAccessTokenPartitioningEnabled() &&
                APIUtil.checkUserNameAssertionEnabled()) {
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
        String tokenScopes[] = new String[1];
        tokenScopes[0] = tokenScope;
        String clientSecret = (String) oAuthApplicationInfo.getParameter("client_secret");
        oAuthApplicationInfo.setClientSecret(clientSecret);
        //for the first time we set default time period.
        oAuthApplicationInfo.addParameter(ApplicationConstants.VALIDITY_PERIOD,
                configuration.getParameter(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD));


        //check whether given consumer key and secret match or not. If it does not match throw an exception.
        SubscriberKeyMgtClient keyMgtClient = null;
        org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo info = null;
        try {
            keyMgtClient = SubscriberKeyMgtClientPool.getInstance().get();
            info = keyMgtClient.getOAuthApplication(oAuthApplicationInfo.getClientId());
            if (!clientSecret.equals(info.getClientSecret())) {
                throw new APIManagementException("The secret key is wrong for the given consumer key " + consumerKey);
            }

        } catch (Exception e) {
            handleException("Some thing went wrong while getting OAuth application for given consumer key " +
                    oAuthApplicationInfo.getClientId(), e);
        } finally {
            if (keyMgtClient != null) {
                SubscriberKeyMgtClientPool.getInstance().release(keyMgtClient);
            }
        }
        if (info != null && info.getClientId() == null) {
            return null;
        }

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
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
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            /**
             * we need to read identity.xml here because we need to get default validity time for access_token in order
             * to set in semi-manual.
             */
            IdentityConfigParser configParser;
            configParser = IdentityConfigParser.getInstance();
            OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

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
                this.configuration.addParameter(APIConstants.AUTHSERVER_URL, config.getFirstProperty(APIConstants
                        .KEYMANAGER_SERVERURL));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_USERNAME, config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME));
                this.configuration.addParameter(APIConstants.KEY_MANAGER_PASSWORD, config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD))
                ;
                this.configuration.addParameter(APIConstants.REVOKE_URL, config.getFirstProperty(APIConstants
                        .REVOKE_API_URL));
                this.configuration.addParameter(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD,validityPeriod);
                String revokeUrl = config.getFirstProperty(APIConstants.REVOKE_API_URL);

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

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException {
        return null;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

}
