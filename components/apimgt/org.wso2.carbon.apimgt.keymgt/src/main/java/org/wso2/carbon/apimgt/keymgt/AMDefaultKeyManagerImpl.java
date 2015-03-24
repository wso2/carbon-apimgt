/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.OauthAppRequest;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.AbstractKeyManager;
import org.wso2.carbon.apimgt.impl.clients.ApplicationManagementServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuth2TokenValidationServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.stub.types.carbon.*;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class AMDefaultKeyManagerImpl extends AbstractKeyManager {

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "open_keymanager";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";

    private KeyManagerConfiguration configuration;

    private static final Log log = LogFactory.getLog(AMDefaultKeyManagerImpl.class);

    @Override
    public OAuthApplicationInfo createApplication(OauthAppRequest oauthAppRequest) throws APIManagementException {

        SubscriberKeyMgtClient keyMgtClient = APIUtil.getKeyManagementClient();

        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getoAuthApplicationInfo();

        String userId = (String)oAuthApplicationInfo.getParameter(ApplicationConstants.
                OAUTH_CLIENT_USERNAME);
        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = "PRODUCTION";
        String callBackURL = "";
        if(oAuthApplicationInfo.getParameter("callback_url") != null){
            JSONArray jsonArray = (JSONArray) oAuthApplicationInfo.getParameter("callback_url");
            for (Object callbackUrlObject : jsonArray) {
                callBackURL = (String) callbackUrlObject;
            }

        }

        String[] allowedDomains = null;
        String validityTime = "3600";
        String tokenScope = (String)oAuthApplicationInfo.getParameter("tokenScope");

        String tokenScopes[] = new String[1];
        tokenScopes[0]= tokenScope;

        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        org.wso2.carbon.apimgt.keymgt.stub.types.carbon.ApplicationKeysDTO keysDTO = null;
        try {
            keysDTO = keyMgtClient.getApplicationAccessKey(userId, applicationName, keyType, callBackURL, allowedDomains, validityTime, tokenScope);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set client ID.
        Object clientId = keysDTO.getConsumerKey();
        oAuthApplicationInfo.setClientId((String) clientId);

        Object clientSecret = keysDTO.getConsumerSecret();
        oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_SECRET, clientSecret);

//        Object grantType = keysDTO.get
        oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, null);


        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OauthAppRequest appInfoDTO) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTOFromAppInfo(appInfoDTO);
        String oAuthAppName = (String) appInfoDTO.getoAuthApplicationInfo().getParameter(ApplicationConstants.
                OAUTH_CLIENT_NAME);
        String username = (String) appInfoDTO.getoAuthApplicationInfo().getParameter(ApplicationConstants.
                OAUTH_CLIENT_USERNAME);

        try {
            oAuthAdminClient.updateOAuthApplicationData(oAuthConsumerAppDTO, username);
        } catch (Exception e) {
            handleException("Can not update OAuth application : " + oAuthAppName, e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName(oAuthAppName, username);
        } catch (Exception e) {
            handleException("Can not retrieve updated OAuth application : " + oAuthAppName, e);
        }

        return createOAuthAppFromResponse(oAuthConsumerAppDTO);

    }


    @Override
    public void deleteApplication(String consumerKey) throws APIManagementException {

        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        String username = ApiMgtDAO.getUserFromOauthToken(consumerKey);

        ApplicationManagementServiceClient applicationManagementServiceClient = APIUtil.
                getApplicationManagementServiceClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        String serviceProviderApplicationName = null;
        try {
            oAuthConsumerAppDTO = oAuthAdminClient.getOAuthApplicationData(consumerKey, username);
            serviceProviderApplicationName = oAuthConsumerAppDTO.getApplicationName();
        } catch (Exception e) {
            handleException("Something wrong while get OAuth Application data for the given consumer key : " +
                    consumerKey, e);
        }
        if (serviceProviderApplicationName != null) {
            if (serviceProviderApplicationName.endsWith("_PRODUCTION") ||
                    serviceProviderApplicationName.endsWith("_SANDBOX")) {

                if (log.isDebugEnabled()) {
                    log.debug("Trying to delete apim created service provider.");
                }

                try {
                    applicationManagementServiceClient.deleteApplication(serviceProviderApplicationName, username);
                } catch (Exception e) {
                    handleException("Can not remove service provider for the given consumer key : " + consumerKey, e);
                }
            }
        }


        try {
            oAuthAdminClient.removeOAuthApplicationData(consumerKey, username);
        } catch (Exception e) {
            handleException("Can not remove OAuth application for the given consumer key : " + consumerKey, e);
        }

    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

        String username = ApiMgtDAO.getUserFromOauthToken(consumerKey);
        try {
            oAuthConsumerAppDTO = oAuthAdminClient.getOAuthApplicationData(consumerKey, username);
        } catch (Exception e) {
            handleException("Can not retrieve OAuth application information from given key: " + consumerKey, e);
        }

        return createOAuthAppFromResponse(oAuthConsumerAppDTO);
    }

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest)
            throws APIManagementException {

        String newAccessToken = null;
        long validityPeriod = 0;
        AccessTokenInfo tokenInfo = null;

        if(tokenRequest == null){
            log.warn("No information available to generate Token.");
            return null;
        }

        String tokenEndpoint = configuration.getParameter(APIConstants.TOKEN_URL);
        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = configuration.getParameter(APIConstants.REVOKE_URL);
        URL keymgtURL = new URL(tokenEndpoint);
        int keyMgtPort = keymgtURL.getPort();
        String keyMgtProtocol = keymgtURL.getProtocol();

        // Call the /revoke only if there's a token to be revoked.
        try {
            if (tokenRequest.getTokenToRevoke() != null) {
                URL revokeEndpointURL = new URL(revokeEndpoint);
                String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
                int revokeEndpointPort = revokeEndpointURL.getPort();

                HttpClient revokeEPClient = APIKeyMgtUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);

                HttpPost httpRevokepost = new HttpPost(revokeEndpoint);

                // Request parameters.
                List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
                revokeParams.add(new BasicNameValuePair("token", tokenRequest.getTokenToRevoke()));


                //Revoke the Old Access Token
                httpRevokepost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
                HttpResponse revokeResponse = revokeEPClient.execute(httpRevokepost);

                if (revokeResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Token revoke failed : HTTP error code : " +
                                               revokeResponse.getStatusLine().getStatusCode());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully submitted revoke request for old application token. HTTP status : 200");
                    }
                }
            }
            //get default application access token name from config.

            String applicationScope = APIKeyMgtDataHolder.getApplicationTokenScope();


            //Generate New Access Token
            HttpClient tokenEPClient = APIKeyMgtUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpPost httpTokpost = new HttpPost(tokenEndpoint);
            List<NameValuePair> tokParams = new ArrayList<NameValuePair>(3);
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_VALUE));
            tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
                                                 Long.toString(tokenRequest.getValidityPeriod())));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, tokenRequest.getClientId()));
            tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, tokenRequest.getClientSecret()));
            tokParams.add(new BasicNameValuePair("scope", applicationScope));


            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            HttpResponse tokResponse = tokenEPClient.execute(httpTokpost);
            HttpEntity tokEntity = tokResponse.getEntity();

            if (tokResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " +
                                           tokResponse.getStatusLine().getStatusCode());
            } else {
                tokenInfo = new AccessTokenInfo();
                String responseStr = EntityUtils.toString(tokEntity);
                JSONObject obj = new JSONObject(responseStr);
                newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());
                tokenInfo.setAccessToken(newAccessToken);
                tokenInfo.setValidityPeriod(validityPeriod);

            }
        } catch (Exception e) {
            String errMsg = "Error in getting new accessToken";
            log.error(errMsg, e);
            throw new APIManagementException(errMsg, e);
        }

        return tokenInfo;
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();

        OAuth2ClientApplicationDTO oAuth2ClientApplicationDTO;
        OAuth2TokenValidationServiceClient oAuth2TokenValidationServiceClient = new
                OAuth2TokenValidationServiceClient();
        oAuth2ClientApplicationDTO = oAuth2TokenValidationServiceClient.
                validateAuthenticationRequest(accessToken);
        org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO oAuth2TokenValidationResponseDTO = oAuth2ClientApplicationDTO.
                getAccessTokenValidationResponse();

        if (!oAuth2TokenValidationResponseDTO.getValid()) {
            log.error("Invalid OAuth Token : "+oAuth2TokenValidationResponseDTO.getErrorMsg());
            throw new APIManagementException("Invalid OAuth Token : "+oAuth2TokenValidationResponseDTO.getErrorMsg());
        }

        tokenInfo.setTokenValid(oAuth2TokenValidationResponseDTO.getValid());
        tokenInfo.setEndUserName(oAuth2TokenValidationResponseDTO.getAuthorizedUser());
        tokenInfo.setConsumerKey(oAuth2ClientApplicationDTO.getConsumerKey());
        tokenInfo.setValidityPeriod(oAuth2TokenValidationResponseDTO.getExpiryTime());
        tokenInfo.setIssuedTime(System.currentTimeMillis());
        tokenInfo.setScope(oAuth2TokenValidationResponseDTO.getScope());

        if(APIUtil.checkAccessTokenPartitioningEnabled() &&
           APIUtil.checkUserNameAssertionEnabled()){
            tokenInfo.setConsumerKey(ApiMgtDAO.getConsumerKeyForTokenWhenTokenPartitioningEnabled(accessToken));
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
    public OAuthApplicationInfo createSemiManualAuthApplication(OauthAppRequest appInfoRequest)
            throws APIManagementException {

        //initiate OAuthApplicationInfo
        OAuthApplicationInfo oAuthApplicationInfo = appInfoRequest.getoAuthApplicationInfo();
        if (log.isDebugEnabled()) {
            log.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }
        //Insert a record to CLIENT_INFO table.
        //oidcDao.createSemiManualClient(oAuthApplicationInfo);
        return oAuthApplicationInfo;
    }

    @Override
    public void loadConfiguration(String configuration) throws APIManagementException{
        if(configuration != null && !configuration.isEmpty()){
            StAXOMBuilder builder = null;
            try {
                builder = new StAXOMBuilder(new ByteArrayInputStream(configuration.getBytes()));
                OMElement document = builder.getDocumentElement();
                if(this.configuration == null) {
                    synchronized (this) {
                        this.configuration = new KeyManagerConfiguration();
                        this.configuration.setManualModeSupported(true);
                        this.configuration.setResourceRegistrationEnabled(true);
                        this.configuration.setTokenValidityConfigurable(true);
                        Iterator<OMElement> elementIterator = document.getChildElements();
                        while (elementIterator.hasNext()){
                            OMElement element = elementIterator.next();
                            this.configuration.addParameter(element.getLocalName(),element.getText());
                        }
                    }
                }

            } catch (XMLStreamException e) {
                e.printStackTrace();
            }

        }
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


    /**
     * Converts OAuthConsumerAppDTO to a OAuthApplicationInfo object
     *
     * @param oAuthConsumerAppDTO - OAuthApplicationInfo
     * @return OAuthApplicationInfo object
     */
    private OAuthApplicationInfo createOAuthAppFromResponse(OAuthConsumerAppDTO oAuthConsumerAppDTO) {
        OAuthApplicationInfo info = new OAuthApplicationInfo();
        //set client ID.
        Object clientId = oAuthConsumerAppDTO.getOauthConsumerKey();
        info.setClientId((String) clientId);

        Object clientSecret = oAuthConsumerAppDTO.getOauthConsumerSecret();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_SECRET, clientSecret);

        //set client Name.
        Object clientName = oAuthConsumerAppDTO.getApplicationName();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_NAME, clientName);

        Object redirectURI = oAuthConsumerAppDTO.getCallbackUrl();
        info.addParameter(ApplicationConstants.OAUTH_REDIRECT_URIS, redirectURI);

        Object grantType = oAuthConsumerAppDTO.getGrantTypes();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, grantType);


        return info;
    }


    /**
     * Converts AppInfoDTO object into a OAuthConsumerAppDTO object
     *
     * @param appInfoDTO - AppInfoDTO
     * @return OAuthConsumerAppDTO object
     */
    private OAuthConsumerAppDTO getOAuthConsumerAppDTOFromAppInfo(OauthAppRequest appInfoDTO) {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getoAuthApplicationInfo();

        oAuthConsumerAppDTO.setApplicationName((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_NAME));
        oAuthConsumerAppDTO.setOauthConsumerKey(oAuthApplicationInfo.getClientId());
        oAuthConsumerAppDTO.setOauthConsumerSecret((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_SECRET));
        oAuthConsumerAppDTO.setCallbackUrl((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_REDIRECT_URIS));
        oAuthConsumerAppDTO.setGrantTypes((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT));

        return oAuthConsumerAppDTO;
    }


}
