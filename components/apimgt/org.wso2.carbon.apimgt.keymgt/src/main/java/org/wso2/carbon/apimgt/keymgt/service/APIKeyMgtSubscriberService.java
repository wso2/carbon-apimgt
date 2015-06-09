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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service class exposes the functionality required by the application developers who will be
 * consuming the APIs published in the API Store.
 */
public class APIKeyMgtSubscriberService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(APIKeyMgtSubscriberService.class);
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    private static final String OAUTH_RESPONSE_TOKEN_SCOPE = "scope";
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
     * Register an OAuth application for the given user
     * @param userId
     * @param applicationName
     * @param callbackUrl
     * @return
     * @throws APIKeyMgtException
     * @throws APIManagementException
     * @throws IdentityException
     */
    public OAuthApplicationInfo createOAuthApplication(String userId, String applicationName, String callbackUrl)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        if (userId == null || userId.isEmpty()) {
            return null;
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String userName = MultitenantUtils.getTenantAwareUsername(userId);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        // Acting as the provided user. When creating Service Provider/OAuth App,
        // username is fetched from CarbonContext
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);

        try {

            // Append the username before Application name to make application name unique across two users.
            applicationName = userName + "_" + applicationName;

            // Create the Service Provider
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("Service Provider for application " + applicationName);

            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            appMgtService.createApplication(serviceProvider);

            ServiceProvider createdServiceProvider = appMgtService.getApplication(applicationName);

            if (createdServiceProvider == null) {
                throw new APIKeyMgtException("Couldn't create Service Provider Application " + applicationName);
            }

            // Then Create OAuthApp
            OAuthAdminService oAuthAdminService = new OAuthAdminService();

            OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

            oAuthConsumerAppDTO.setApplicationName(applicationName);
            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);

            String[] allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
            // CallbackURL is needed for authorization_code and implicit grant types. If CallbackURL is empty,
            // simply remove those grant types from the list
            StringBuilder grantTypeString = new StringBuilder();

            for (String grantType : allowedGrantTypes) {
                if (callbackUrl == null || callbackUrl.isEmpty()) {
                    if ("authorization_code".equals(grantType) || "implicit".equals(grantType)) {
                        continue;
                    }
                }
                grantTypeString.append(grantType).append(" ");
            }

            if (grantTypeString.length() > 0) {
                oAuthConsumerAppDTO.setGrantTypes(grantTypeString.toString().trim());
                log.debug("Setting Grant Type String : " + grantTypeString);
            }

            oAuthConsumerAppDTO.setOAuthVersion(OAuthConstants.OAuthVersions.VERSION_2);
            log.debug("Creating OAuth App " + applicationName);
            oAuthAdminService.registerOAuthApplicationData(oAuthConsumerAppDTO);
            // === Finished Creating OAuth App ===

            log.debug("Created OAuth App " + applicationName);
            OAuthConsumerAppDTO createdApp = oAuthAdminService.getOAuthApplicationDataByAppName(oAuthConsumerAppDTO
                                                                                                        .getApplicationName());
            log.debug("Retrieved Details for OAuth App " + createdApp.getApplicationName());

            // Set the OAuthApp in InboundAuthenticationConfig
            InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
            InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs = new
                    InboundAuthenticationRequestConfig[1];
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                    InboundAuthenticationRequestConfig();

            inboundAuthenticationRequestConfig.setInboundAuthKey(createdApp.getOauthConsumerKey());
            inboundAuthenticationRequestConfig.setInboundAuthType("oauth2");
            if (createdApp.getOauthConsumerSecret() != null && !createdApp.
                    getOauthConsumerSecret().isEmpty()) {
                Property property = new Property();
                property.setName("oauthConsumerSecret");
                property.setValue(createdApp.getOauthConsumerSecret());
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }

            inboundAuthenticationRequestConfigs[0] = inboundAuthenticationRequestConfig;
            inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs);
            createdServiceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

            // Update the Service Provider app to add OAuthApp as an Inbound Authentication Config
            appMgtService.updateApplication(createdServiceProvider);


            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            oAuthApplicationInfo.setClientId(createdApp.getOauthConsumerKey());
            oAuthApplicationInfo.setCallBackURL(createdApp.getCallbackUrl());
            oAuthApplicationInfo.setClientSecret(createdApp.getOauthConsumerSecret());

            oAuthApplicationInfo.addParameter(ApplicationConstants.
                                                      OAUTH_REDIRECT_URIS, createdApp.getCallbackUrl());
            oAuthApplicationInfo.addParameter(ApplicationConstants.
                                                      OAUTH_CLIENT_NAME, createdApp.getApplicationName());
            oAuthApplicationInfo.addParameter(ApplicationConstants.
                                                      OAUTH_CLIENT_GRANT, createdApp.getGrantTypes());

            return oAuthApplicationInfo;

        } catch (IdentityApplicationManagementException e) {
            APIUtil.handleException("Error occurred while creating ServiceProvider for app " + applicationName, e);
        } catch (Exception e) {
            APIUtil.handleException("Error occurred while creating OAuthApp " + applicationName, e);
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(baseUser);
        }
        return null;
    }

    /**
     * Retrieve OAuth application for given consumer key
     * @param consumerKey
     * @return
     * @throws APIKeyMgtException
     * @throws APIManagementException
     * @throws IdentityException
     */
    public OAuthApplicationInfo retrieveOAuthApplication(String consumerKey)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        OAuthApplicationInfo oAuthApplicationInfo = apiMgtDAO.getOAuthApplication(consumerKey);
        return oAuthApplicationInfo;
    }

    /**
     * Delete OAuth application for given consumer key
     * @param consumerKey
     * @throws APIKeyMgtException
     * @throws APIManagementException
     * @throws IdentityException
     */
    public void deleteOAuthApplication(String consumerKey)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        if (consumerKey == null || consumerKey.isEmpty()) {
            return;
        }

        Subscriber subscriber = ApiMgtDAO.getOwnerForConsumerApp(consumerKey);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantAwareUsername = subscriber.getName();

        PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(subscriber.getTenantId(), true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAwareUsername);

        try {

            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();

            log.debug("Getting OAuth App for " + consumerKey);
            String spAppName = appMgtService.getServiceProviderNameByClientId(consumerKey, "oauth2");

            if (spAppName == null) {
                log.debug("Couldn't find OAuth App for Consumer Key : " + consumerKey);
                return;
            }

            log.debug("Removing Service Provider with name : " + spAppName);
            appMgtService.deleteApplication(spAppName);


        } catch (IdentityApplicationManagementException e) {
            APIUtil.handleException("Error occurred while deleting ServiceProvider", e);
        } catch (Exception e) {
            APIUtil.handleException("Error occurred while deleting OAuthApp", e);
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(baseUser);
        }
    }

    /**
     * Get the access token for the specified application. This token can be used as an OAuth
     * 2.0 bearer token to access any API in the given application.
     *
     * @param userId          User/Developer name
     * @param applicationName Name of the application
     * @param tokenType       Type (scope) of the required access token
     * @param tokenScope      Scope of the token
     * @return Access token
     * @throws APIKeyMgtException on error
     */
    public ApplicationKeysDTO getApplicationAccessToken(String userId, String applicationName, String tokenType,
                                                        String callbackUrl, String[] allowedDomains,
                                                        String validityTime, String tokenScope)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

        OAuthApplicationInfo oAuthApplicationInfo = null;
        String accessToken = apiMgtDAO.getAccessKeyForApplication(userId, applicationName, tokenType);

        Application application = apiMgtDAO.getApplicationByName(applicationName, userId, null);
        oAuthApplicationInfo = apiMgtDAO.getClientOfApplication(application.getId(), tokenType);
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
                            tenantAwareUserId, tokenType, allowedDomains, validityTime,tokenScope);
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
        String tokenScope = null;
        long validityPeriod = 0;


        String tokenEndpointName = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_VALIDATOR_TOKEN_ENDPOINT_NAME);
        String keyMgtServerURL = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
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
                getFirstProperty(APIConstants.API_KEY_VALIDATOR_REVOKE_API_URL);

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
        tokParams.add(new BasicNameValuePair(OAuth.OAUTH_SCOPE, tokenType));

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
                tokenScope = obj.get(OAUTH_RESPONSE_TOKEN_SCOPE).toString();

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
        apiMgtDAO.updateRefreshedApplicationAccessToken(tokenScope, newAccessToken,
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
            apiSet = dao.getSubscribedAPIs(application.getSubscriber(), null);
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
            Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();
            for (Environment environment : gatewayEnvs.values()) {
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
        Application[] applications = dao.getApplications(subscriber, null);
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

