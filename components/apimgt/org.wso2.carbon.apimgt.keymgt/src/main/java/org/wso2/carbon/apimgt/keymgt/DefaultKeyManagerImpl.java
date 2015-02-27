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
import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.OauthAppRequest;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.AbstractKeyManager;
import org.wso2.carbon.apimgt.impl.clients.ApplicationManagementServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuth2TokenValidationServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class DefaultKeyManagerImpl extends AbstractKeyManager {

    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    private static final String GRANT_TYPE_VALUE = "open_keymanager";
    private static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";

    private static final Log log = LogFactory.getLog(DefaultKeyManagerImpl.class);

    @Override
    public OAuthApplicationInfo createApplication(OauthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        ApplicationManagementServiceClient applicationManagementServiceClient = APIUtil.
                getApplicationManagementServiceClient();

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getoAuthApplicationInfo();
        oAuthConsumerAppDTO.setApplicationName(oAuthApplicationInfo.getClientName());

        if(oAuthApplicationInfo.getParameter("callback_url") != null){
            JSONArray jsonArray = (JSONArray) oAuthApplicationInfo.getParameter("callback_url");
            String callbackUrl = null;

            for (Object callbackUrlObject : jsonArray) {
                callbackUrl = (String) callbackUrlObject;
            }

            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
        }

        try {
            oAuthAdminClient.registerOAuthApplicationData(oAuthConsumerAppDTO);

        } catch (Exception e) {
            handleException("OAuth application registration failed", e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName(oAuthApplicationInfo.getClientName());
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
            applicationManagementServiceClient.createApplication(serviceProvider);
        } catch (Exception e) {
            handleException("Service Provider creation failed", e);
        }

//        try {
//            serviceProvider = applicationManagementServiceClient.getApplication((String) oAuthApplicationInfo.getParameter("client_name"));
//        } catch (Exception e) {
//            handleException("Service Provider creation failed", e);
//        }


//
//        try {
//            applicationManagementServiceClient.updateApplicationData(serviceProvider);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OauthAppRequest appInfoDTO) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTOFromAppInfo(appInfoDTO);
        String oAuthAppName = (String) appInfoDTO.getoAuthApplicationInfo().getParameter(ApplicationConstants.
                OAUTH_CLIENT_NAME);

        try {
            oAuthAdminClient.updateOAuthApplicationData(oAuthConsumerAppDTO);
        } catch (Exception e) {
            handleException("Can not update OAuth application : " + oAuthAppName, e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName(oAuthAppName);
        } catch (Exception e) {
            handleException("Can not retrieve updated OAuth application : " + oAuthAppName, e);
        }

        return createOAuthAppFromResponse(oAuthConsumerAppDTO);

    }


    @Override
    public void deleteApplication(String consumerKey) throws APIManagementException {

        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        try {
            oAuthAdminClient.removeOAuthApplicationData(consumerKey);
        } catch (Exception e) {
            handleException("Can not remove OAuth application for the given consumer key : " + consumerKey, e);
        }

    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        try {
            oAuthConsumerAppDTO = oAuthAdminClient.getOAuthApplicationData(consumerKey);
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


        String tokenEndpointName = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_TOKEN_ENDPOINT_NAME);
        String keyMgtServerURL = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        URL keymgtURL = new URL(keyMgtServerURL);
        int keyMgtPort = keymgtURL.getPort();
        String keyMgtProtocol = keymgtURL.getProtocol();
        String tokenEndpoint = null;

        String webContextRoot = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");

        if (webContextRoot == null || "/".equals(webContextRoot)) {
            webContextRoot = "";
        }

        if (keyMgtServerURL != null) {
            String[] tmp = keyMgtServerURL.split(webContextRoot + "/services");
            tokenEndpoint = tmp[0] + tokenEndpointName;
        }

        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_REVOKE_API_URL);


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

        return tokenInfo;
    }

    @Override
    public String getKeyManagerMetaData() throws APIManagementException {
        return null;
    }

    @Override
    public OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException {
        return null;
    }

    @Override
    public OAuthApplicationInfo createSemiManualAuthApplication(OauthAppRequest appInfoRequest) throws APIManagementException {
        return null;
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
