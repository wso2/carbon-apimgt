/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.axis2.AxisFault;
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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This service class exposes the functionality required by the application developers who will be
 * consuming the APIs published in the API Store.
 */
public class APIKeyMgtSubscriberService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(APIKeyMgtSubscriberService.class);
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";

    /**
     * Get the access token for a user per given API. Users/developers can use this access token
     * to consume the API by directly passing it as a bearer token as per the OAuth 2.0 specification.
     *
     * @param userId     User/Developer name
     * @param apiInfoDTO Information about the API to which the Access token will be issued.
     *                   Provider name, API name and the version should be provided to uniquely identify
     *                   an API.
     * @param tokenType  Type (scope) of the required access token
     * @return Access Token
     * @throws APIKeyMgtException Error when getting the AccessToken from the underlying token store.
     */
    public String getAccessToken(String userId, APIInfoDTO apiInfoDTO,
                                 String applicationName, String tokenType, String callbackUrl) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        String accessToken = apiMgtDAO.getAccessKeyForAPI(userId, applicationName, apiInfoDTO, tokenType);
        if (accessToken == null) {
            //get the tenant id for the corresponding domain
            String tenantAwareUserId = userId;
            int tenantId = IdentityUtil.getTenantIdOFUser(userId);

            String[] credentials = apiMgtDAO.addOAuthConsumer(tenantAwareUserId, tenantId, applicationName, callbackUrl);

            accessToken = apiMgtDAO.registerAccessToken(credentials[0], applicationName,
                    tenantAwareUserId, tenantId, apiInfoDTO, tokenType);
        }
        return accessToken;
    }

    /**
     * Get the access token for the specified application. This token can be used as an OAuth
     * 2.0 bearer token to access any API in the given application.
     *
     * @param userId          User/Developer name
     * @param applicationName Name of the application
     * @param tokenType       Type (scope) of the required access token
     * @return Access token
     * @throws APIKeyMgtException on error
     */
    public ApplicationKeysDTO getApplicationAccessToken(String userId, String applicationName, String tokenType,
                                                        String callbackUrl, String[] allowedDomains, String validityTime)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

        OAuthApplicationInfo oAuthApplicationInfo = null;
        String accessToken = apiMgtDAO.getAccessKeyForApplication(userId, applicationName, tokenType);
        Application application = apiMgtDAO.getApplicationByName(applicationName, userId);
        oAuthApplicationInfo = apiMgtDAO.getProductionClientOfApplication(application.getId(), tokenType);
        if (oAuthApplicationInfo == null) {
            throw new APIKeyMgtException("Unable to locate oAuth Application");
        } else {
            if (oAuthApplicationInfo.getClientId() == null) {
                throw new APIKeyMgtException("Consumer key value is null can not get application access token");
            } else if (oAuthApplicationInfo.getParameter("client_secret") == null) {
                throw new APIKeyMgtException("Consumer secret value is null can not get application access token");
            }

            String consumerKey = oAuthApplicationInfo.getClientId();
            String consumerSecret = (String) oAuthApplicationInfo.getParameter("client_secret");
            if (accessToken == null) {
                //get the tenant id for the corresponding domain
                String tenantAwareUserId = userId;

                String state = apiMgtDAO.getRegistrationApprovalState(application.getId(), tokenType);
                if (APIConstants.AppRegistrationStatus.REGISTRATION_APPROVED.equals(state)) {
                    //credentials = apiMgtDAO.addOAuthConsumer(tenantAwareUserId, tenantId, applicationName, callbackUrl);

                    accessToken = apiMgtDAO.registerApplicationAccessToken(oAuthApplicationInfo.getClientId(), application.getId(),
                            applicationName,
                            tenantAwareUserId, tokenType, allowedDomains, validityTime);
                }

            }

            ApplicationKeysDTO keys = new ApplicationKeysDTO();
            keys.setApplicationAccessToken(accessToken);
            keys.setConsumerKey(consumerKey);
            keys.setConsumerSecret(consumerSecret);
            keys.setValidityTime(validityTime);
            return keys;
        }

    }

    /**
     * Get the list of subscribed APIs of a user
     *
     * @param userId User/Developer name
     * @return An array of APIInfoDTO instances, each instance containing information of provider name,
     *         api name and version.
     * @throws APIKeyMgtException Error when getting the list of APIs from the persistence store.
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO ApiMgtDAO = new ApiMgtDAO();
        return ApiMgtDAO.getSubscribedAPIsOfUser(userId);
    }

    /**
     * Renew the ApplicationAccesstoken, Call Token endpoint and get parameters.
     * Revoke old token.(create a post request to getNewAccessToken with client_credentials
        grant type.)
     *
     * @param tokenType
     * @param oldAccessToken
     * @param allowedDomains
     * @param clientId
     * @param clientSecret
     * @param validityTime
     * @return
     * @throws Exception
     */

    public String renewAccessToken(String tokenType, String oldAccessToken,
                                   String[] allowedDomains, String clientId, String clientSecret,
                                   String validityTime) throws Exception {
        String newAccessToken = null;
        long validityPeriod = 0;
        

        String tokenEndpointName = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_TOKEN_ENDPOINT_NAME);
        String keyMgtServerURL = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        URL keymgtURL = new URL(keyMgtServerURL);
        int keyMgtPort = keymgtURL.getPort();
        String keyMgtProtocol= keymgtURL.getProtocol();
        String tokenEndpoint = null;

        String webContextRoot = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");

        if(webContextRoot == null || "/".equals(webContextRoot)){
            webContextRoot = "";
        }

        if (keyMgtServerURL != null) {
            String[] tmp = keyMgtServerURL.split(webContextRoot + "/services");
            tokenEndpoint = tmp[0] + tokenEndpointName;
        }
      
        //To revoke tokens we should call revoke API deployed in API gateway.
        String revokeEndpoint = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_MANAGER_REVOKE_API_URL);

		URL revokeEndpointURL = new URL(revokeEndpoint);
		String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
		int revokeEndpointPort = revokeEndpointURL.getPort();
	

        HttpClient tokenEPClient =  APIKeyMgtUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
        HttpClient revokeEPClient = APIKeyMgtUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);
        HttpPost httpTokpost = new HttpPost(tokenEndpoint);
        HttpPost httpRevokepost = new HttpPost(revokeEndpoint);

        // Request parameters.
        List<NameValuePair> tokParams = new ArrayList<NameValuePair>(3);
        List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);

        tokParams.add(new BasicNameValuePair(OAuth.OAUTH_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS));
        tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, clientId));
        tokParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, clientSecret));

        revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, clientId));
        revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, clientSecret));
        revokeParams.add(new BasicNameValuePair("token", oldAccessToken));

        try {
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

            //Generate New Access Token
            httpTokpost.setEntity(new UrlEncodedFormEntity(tokParams, "UTF-8"));
            HttpResponse tokResponse = tokenEPClient.execute(httpTokpost);
            HttpEntity tokEntity = tokResponse.getEntity();

            if (tokResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " +
                        tokResponse.getStatusLine().getStatusCode());
            } else {
                String responseStr = EntityUtils.toString(tokEntity);
                JSONObject obj = new JSONObject(responseStr);
                newAccessToken = obj.get(OAUTH_RESPONSE_ACCESSTOKEN).toString();
                validityPeriod = Long.parseLong(obj.get(OAUTH_RESPONSE_EXPIRY_TIME).toString());

                if (validityTime != null && !"".equals(validityTime)) {
                    validityPeriod = Long.parseLong(validityTime);
                }
            }
        } catch (Exception e) {
            String errMsg = "Error in getting new accessToken";
            log.error(errMsg, e);
            throw new APIKeyMgtException(errMsg, e);
        }
        
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        apiMgtDAO.updateRefreshedApplicationAccessToken(tokenType, newAccessToken,
                validityPeriod);
        return newAccessToken;

    }

    public void unsubscribeFromAPI(String userId, APIInfoDTO apiInfoDTO) {

    }

    /**
     * Revoke Access tokens by Access token string.This will change access token status to revoked and
     * remove cached access tokens from memory
     *
     * @param key Access Token String to be revoked
     * @throws APIManagementException on error in revoking
     * @throws AxisFault              on error in clearing cached key
     */
    public void revokeAccessToken(String key, String consumerKey, String authorizedUser) throws APIManagementException, AxisFault {
        ApiMgtDAO dao = new ApiMgtDAO();
        dao.revokeAccessToken(key);
        clearOAuthCache(consumerKey, authorizedUser);
    }

    /**
     * Revoke All access tokens associated with an application.This will change access tokens status to revoked and
     * remove cached access tokens from memory
     *
     * @param application Application object associated with keys to be removed
     * @throws APIManagementException on error in revoking
     * @throws AxisFault              on error in revoking cached keys
     */
    public void revokeAccessTokenForApplication(Application application) throws APIManagementException, AxisFault {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean gatewayExists = config.getApiGatewayEnvironments().size() > 0;
        Set<SubscribedAPI> apiSet = null;
        Set<String> keys = null;
        ApiMgtDAO dao;
        dao = new ApiMgtDAO();
        if (gatewayExists) {
            keys = dao.getApplicationKeys(application.getId());
            apiSet = dao.getSubscribedAPIs(application.getSubscriber());
        }
        List<APIKeyMapping> mappings = new ArrayList<APIKeyMapping>();
        for (String key : keys) {
            dao.revokeAccessToken(key);
            for (SubscribedAPI api : apiSet) {
                APIKeyMapping mapping = new APIKeyMapping();
                API apiDefinition = APIKeyMgtUtil.getAPI(api.getApiId());
                mapping.setApiVersion(api.getApiId().getVersion());
                mapping.setContext(apiDefinition.getContext());
                mapping.setKey(key);
                mappings.add(mapping);
            }
        }
        if (mappings.size() > 0) {
            List<Environment> gatewayEnvs = config.getApiGatewayEnvironments();
            for (Environment environment : gatewayEnvs) {
                APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                client.invalidateKeys(mappings);
            }
        }
    }


    /**
     * Revoke all access tokens associated by subscriber user.This will change access token status to revoked and
     * remove cached access tokens from memory
     *
     * @param subscriber Subscriber associated with the keys to be removed
     * @throws APIManagementException on error in revoking keys
     * @throws AxisFault              on error in clearing cached keys
     */
    public void revokeAccessTokenBySubscriber(Subscriber subscriber) throws
            APIManagementException, AxisFault {
        ApiMgtDAO dao;
        dao = new ApiMgtDAO();
        Application[] applications = dao.getApplications(subscriber);
        for (Application app : applications) {
            revokeAccessTokenForApplication(app);
        }
    }

    /**
     * Revoke all access tokens associated with the given tier.This will change access token status to revoked and
     * remove cached access tokens from memory
     *
     * @param tierName Tier associated with keys to be removed
     * @throws APIManagementException on error in revoking keys
     * @throws AxisFault              on error in clearing cached keys
     */
    public void revokeKeysByTier(String tierName) throws APIManagementException, AxisFault {
        ApiMgtDAO dao;
        dao = new ApiMgtDAO();
        Application[] applications = dao.getApplicationsByTier(tierName);
        for (Application application : applications) {
            revokeAccessTokenForApplication(application);
        }
    }

    public void clearOAuthCache(String consumerKey, String authorizedUser) {
        OAuthCache oauthCache;
        CacheKey cacheKey = new OAuthCacheKey(consumerKey + ":" + authorizedUser);
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            oauthCache = OAuthCache.getInstance();
            oauthCache.clearCacheEntry(cacheKey);
        }
    }
}
