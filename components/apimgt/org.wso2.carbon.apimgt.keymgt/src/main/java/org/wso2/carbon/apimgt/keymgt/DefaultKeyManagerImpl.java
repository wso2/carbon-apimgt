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
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.clients.ApplicationManagementServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import javax.xml.stream.XMLStreamException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class DefaultKeyManagerImpl extends AbstractKeyManager {

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    //private static final String GRANT_TYPE_VALUE = "open_keymanager";
    private static final String GRANT_TYPE_VALUE = "client_credentials";
    //private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";

    private KeyManagerConfiguration configuration;

    private static final Log log = LogFactory.getLog(DefaultKeyManagerImpl.class);

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        ApplicationManagementServiceClient applicationManagementServiceClient = APIUtil.
                getApplicationManagementServiceClient();

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();
        oAuthConsumerAppDTO.setApplicationName(oAuthApplicationInfo.getClientName());

        String username = (String)oAuthApplicationInfo.getParameter(ApplicationConstants.
                                                                            OAUTH_CLIENT_USERNAME);


        if(oAuthApplicationInfo.getParameter("callback_url") != null){
            JSONArray jsonArray = (JSONArray) oAuthApplicationInfo.getParameter("callback_url");
            String callbackUrl = null;

            for (Object callbackUrlObject : jsonArray) {
                callbackUrl = (String) callbackUrlObject;
            }

            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
        }

        try {
            oAuthAdminClient.registerOAuthApplicationData(oAuthConsumerAppDTO,username);

        } catch (Exception e) {
            handleException("OAuth application registration failed", e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName(oAuthApplicationInfo.getClientName(),username);
        } catch (Exception e) {
            handleException("Can not retrieve registered OAuth application information ", e);
        }

        oAuthApplicationInfo = createOAuthAppFromResponse(oAuthConsumerAppDTO);

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName((String) oAuthApplicationInfo.getParameter("client_name"));
        serviceProvider.setDescription("Service Provider for application " + oAuthApplicationInfo.getParameter("client_name"));


        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs = new
                InboundAuthenticationRequestConfig[1];
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                InboundAuthenticationRequestConfig();

        inboundAuthenticationRequestConfig.setInboundAuthKey(oAuthConsumerAppDTO.getOauthConsumerKey());
        inboundAuthenticationRequestConfig.setInboundAuthType("oauth2");
        if (oAuthConsumerAppDTO.getOauthConsumerSecret()!= null && !oAuthConsumerAppDTO.
                getOauthConsumerSecret().isEmpty()) {
            Property property = new Property();
            property.setName("oauthConsumerSecret");
            property.setValue(oAuthConsumerAppDTO.getOauthConsumerSecret());
            Property[] properties = {property};
            inboundAuthenticationRequestConfig.setProperties(properties);
        }

        inboundAuthenticationRequestConfigs[0] = inboundAuthenticationRequestConfig;
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs);
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        try {
            applicationManagementServiceClient.createApplication(serviceProvider, username);
        } catch (Exception e) {
            handleException("Service Provider creation failed", e);
        }

        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTOFromAppInfo(appInfoDTO);
        String oAuthAppName = (String) appInfoDTO.getOAuthApplicationInfo().getParameter(ApplicationConstants.
                OAUTH_CLIENT_NAME);
        String username = (String) appInfoDTO.getOAuthApplicationInfo().getParameter(ApplicationConstants.
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
            //tokParams.add(new BasicNameValuePair(GRANT_TYPE_PARAM_VALIDITY,
            //                                     Long.toString(tokenRequest.getValidityPeriod())));
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
/*
        OAuth2ClientApplicationDTO oAuth2ClientApplicationDTO;
        OAuth2TokenValidationServiceClient oAuth2TokenValidationServiceClient = new
                OAuth2TokenValidationServiceClient();
        oAuth2ClientApplicationDTO = oAuth2TokenValidationServiceClient.
                validateAuthenticationRequest(accessToken);
        org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO oAuth2TokenValidationResponseDTO = oAuth2ClientApplicationDTO.
                getAccessTokenValidationResponse();
*/
        OAuth2TokenValidationService oAuth2TokenValidationService = new OAuth2TokenValidationService();
        OAuth2TokenValidationRequestDTO requestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken token = requestDTO. new OAuth2AccessToken();

        token.setIdentifier(accessToken);
        token.setTokenType("bearer");
        requestDTO.setAccessToken(token);

        //TODO: If these values are not set, validation will fail giving an NPE. Need to see why that happens
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam contextParam = requestDTO. new
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
            log.error("Invalid OAuth Token : "+responseDTO.getErrorMsg());
            throw new APIManagementException("Invalid OAuth Token : "+responseDTO.getErrorMsg());
        }

        tokenInfo.setTokenValid(responseDTO.isValid());
        tokenInfo.setEndUserName(responseDTO.getAuthorizedUser());
        tokenInfo.setConsumerKey(clientApplicationDTO.getConsumerKey());
        tokenInfo.setValidityPeriod(responseDTO.getExpiryTime());
        tokenInfo.setIssuedTime(System.currentTimeMillis());
        tokenInfo.setScope(responseDTO.getScope());

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
     * @throws APIManagementException
     */
    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest)
            throws APIManagementException {

        //initiate OAuthApplicationInfo
        OAuthApplicationInfo oAuthApplicationInfo = appInfoRequest.getOAuthApplicationInfo();
        if (log.isDebugEnabled()) {
            log.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }
        
        return oAuthApplicationInfo;
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException{
        return;
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException {
        return false;
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

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
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
    private OAuthConsumerAppDTO getOAuthConsumerAppDTOFromAppInfo(OAuthAppRequest appInfoDTO) {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();

        oAuthConsumerAppDTO.setApplicationName((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_NAME));
        oAuthConsumerAppDTO.setOauthConsumerKey(oAuthApplicationInfo.getClientId());
        oAuthConsumerAppDTO.setOauthConsumerSecret((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_SECRET));
        oAuthConsumerAppDTO.setCallbackUrl((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_REDIRECT_URIS));
        oAuthConsumerAppDTO.setGrantTypes((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT));

        return oAuthConsumerAppDTO;
    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        Set<String> activeTokens = apiMgtDAO.getActiveTokensOfConsumerKey(consumerKey);
        return activeTokens;
    }


}
