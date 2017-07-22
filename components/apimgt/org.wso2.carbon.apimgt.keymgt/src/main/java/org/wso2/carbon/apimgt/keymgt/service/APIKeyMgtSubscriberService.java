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
import org.apache.oltu.oauth2.common.OAuth;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
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
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
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
     * Register an OAuth application for the given user
     * @param oauthApplicationInfo - An OAuthApplicationInfo object that holds the application details.
     * @return OAuthApplicationInfo containing the details of the created App.
     * @throws APIKeyMgtException
     * @throws APIManagementException
     */
    public OAuthApplicationInfo createOAuthApplicationByApplicationInfo(OAuthApplicationInfo oauthApplicationInfo)
            throws APIKeyMgtException, APIManagementException {

        String userId = oauthApplicationInfo.getAppOwner();
        String applicationName = oauthApplicationInfo.getClientName();
        String callbackUrl = oauthApplicationInfo.getCallBackURL();

        if (userId == null || userId.isEmpty()) {
            return null;
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String userName = MultitenantUtils.getTenantAwareUsername(userId);
        String userNameForSP = userName;

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        // Acting as the provided user. When creating Service Provider/OAuth App,
        // username is fetched from CarbonContext
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);

        try {

            // Replace domain separator by "_" if user is coming from a secondary userstore.
            String domain = UserCoreUtil.extractDomainFromName(userNameForSP);
            if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domain)) {
                userNameForSP = userNameForSP.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
            }

            // Append the username before Application name to make application name unique across two users.
            applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + applicationName;

            // Create the Service Provider
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("Service Provider for application " + applicationName);

            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            appMgtService.createApplication(serviceProvider, tenantDomain, userName);
            ServiceProvider serviceProviderCreated = appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
            serviceProviderCreated.setSaasApp(oauthApplicationInfo.getIsSaasApplication());
            appMgtService.updateApplication(serviceProviderCreated, tenantDomain, userName);

            ServiceProvider createdServiceProvider = appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);

            if (createdServiceProvider == null) {
                throw new APIKeyMgtException("Couldn't create Service Provider Application " + applicationName);
            }

            // Then Create OAuthApp
            OAuthAdminService oAuthAdminService = new OAuthAdminService();
            OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
            oAuthConsumerAppDTO.setApplicationName(applicationName);
            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
            //set username to avoid issues with email user name login
            oAuthConsumerAppDTO.setUsername(userName);

            //check whether grant types are provided
            String[] allowedGrantTypes = null;
            String jsonPayload = oauthApplicationInfo.getJsonString();
            if(jsonPayload != null){
                
                String grantTypesString = null;
                JSONObject jsonObj = new JSONObject(jsonPayload);
                if(jsonObj != null && jsonObj.has("grant_types")){
                    grantTypesString = (String) jsonObj.get("grant_types");
                }
                if(grantTypesString !=  null){
                    allowedGrantTypes = grantTypesString.split(",");
                } else {
                  //set allowed grant types if grant types are not provided
                    allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
                }
                
            } else {
                //set allowed grant types if grant types are not provided
                allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
            }
            
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
            appMgtService.updateApplication(createdServiceProvider,tenantDomain,userName);


            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            oAuthApplicationInfo.setClientId(createdApp.getOauthConsumerKey());
            oAuthApplicationInfo.setCallBackURL(createdApp.getCallbackUrl());
            oAuthApplicationInfo.setClientSecret(createdApp.getOauthConsumerSecret());
            oAuthApplicationInfo.setIsSaasApplication(createdServiceProvider.isSaasApp());

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
     * Register an OAuth application for the given user
     * @param userId - username of the Application owner
     * @param applicationName - name of the Application
     * @param callbackUrl - callback url of the Application
     * @return OAuthApplicationInfo containing the details of the created App.
     * @throws APIKeyMgtException
     * @throws APIManagementException
     */
    public OAuthApplicationInfo createOAuthApplication(String userId, String applicationName, String callbackUrl)
            throws APIKeyMgtException, APIManagementException {

        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthApplicationInfo.setClientName(applicationName);
        oauthApplicationInfo.setCallBackURL(callbackUrl);
        oauthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, userId);
        return createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
    }

    /**
     * Register an OAuth application for the given user
     *
     * @param userId
     * @param applicationName
     * @param callbackUrl
     * @return
     * @throws APIKeyMgtException
     * @throws APIManagementException
     * @throws IdentityException
     */
    public OAuthApplicationInfo updateOAuthApplication(String userId, String applicationName, String callbackUrl,
                                                       String consumerKey, String[] grantTypes)
            throws APIKeyMgtException, APIManagementException, IdentityException {

        if (userId == null || userId.isEmpty()) {
            return null;
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String userName = MultitenantUtils.getTenantAwareUsername(userId);
        String userNameForSP = userName;

        if (log.isDebugEnabled()) {

            StringBuilder message = new StringBuilder();
            message.append("Updating OAuthApplication for ").append(userId).append(" with details : ");
            if (consumerKey != null) {
                message.append(" consumerKey = ").append(consumerKey);
            }

            if (callbackUrl != null) {
                message.append(", callbackUrl = ").append(callbackUrl);
            }

            if (applicationName != null) {
                message.append(", applicationName = ").append(applicationName);
            }

            if (grantTypes != null && grantTypes.length > 0) {
                message.append(", grant Types = ");
                for (String grantType : grantTypes) {
                    message.append(grantType).append(" ");
                }
            }
            log.debug(message.toString());
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        // Acting as the provided user. When creating Service Provider/OAuth App,
        // username is fetched from CarbonContext
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);

        try {

            // Replace domain separator by "_" if user is coming from a secondary userstore.
            String domain = UserCoreUtil.extractDomainFromName(userNameForSP);
            if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domain)) {
                userNameForSP = userNameForSP.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
            }

            if (applicationName != null && !applicationName.isEmpty()) {
                // Append the username before Application name to make application name unique across two users.
                applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + applicationName;
                log.debug("Application Name has changed, hence updating Service Provider Name..");

                // Get ServiceProvider Name by consumer Key.
                ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
                String appName = appMgtService.getServiceProviderNameByClientId(consumerKey, "oauth2", tenantDomain);
                ServiceProvider serviceProvider =
                                        appMgtService.getApplicationExcludingFileBasedSPs(appName, tenantDomain);
                if (serviceProvider != null && !appName.equals(applicationName)) {
                    serviceProvider.setApplicationName(applicationName);
                    serviceProvider.setDescription("Service Provider for application " + applicationName);
                    appMgtService.updateApplication(serviceProvider, tenantDomain, userName);
                    log.debug("Service Provider Name Updated to : " + applicationName);
                }

            }

            OAuthAdminService oAuthAdminService = new OAuthAdminService();
            OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminService.getOAuthApplicationData(consumerKey);

            if (oAuthConsumerAppDTO != null) {
                // TODO: Make sure that App is only updated by the user who created it.
                //if(userName.equals(oAuthConsumerAppDTO.getUsername()))
                if (callbackUrl != null && !callbackUrl.isEmpty()) {
                    oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
                    log.debug("CallbackURL is set to : " + callbackUrl);
                }
                oAuthConsumerAppDTO.setOauthConsumerKey(consumerKey);
                if (applicationName != null && !applicationName.isEmpty()) {
                    oAuthConsumerAppDTO.setApplicationName(applicationName);
                    log.debug("Name of the OAuthApplication is set to : " + applicationName);
                }

                if (grantTypes != null && grantTypes.length > 0) {
                    StringBuilder builder = new StringBuilder();
                    for (String grantType : grantTypes) {
                        builder.append(grantType + " ");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    oAuthConsumerAppDTO.setGrantTypes(builder.toString());
                } else {
                    //update the grant type with respect to callback url
                    String[] allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
                    StringBuilder grantTypeString = new StringBuilder();

                    for (String grantType : allowedGrantTypes) {
                        if (callbackUrl == null || callbackUrl.isEmpty()) {
                            if ("authorization_code".equals(grantType) || "implicit".equals(grantType)) {
                                continue;
                            }
                        }
                        grantTypeString.append(grantType).append(" ");
                    }
                    oAuthConsumerAppDTO.setGrantTypes(grantTypeString.toString().trim());
                }               
                
                
                oAuthAdminService.updateConsumerApplication(oAuthConsumerAppDTO);
                log.debug("Updated the OAuthApplication...");

                oAuthConsumerAppDTO = oAuthAdminService.getOAuthApplicationData(consumerKey);
                OAuthApplicationInfo oAuthApplicationInfo = createOAuthAppInfoFromDTO(oAuthConsumerAppDTO);
                return oAuthApplicationInfo;
            }

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

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
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

        Subscriber subscriber = ApiMgtDAO.getInstance().getOwnerForConsumerApp(consumerKey);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantAwareUsername = subscriber.getName();

        PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(subscriber.getTenantId(), true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAwareUsername);

        try {

            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();

            log.debug("Getting OAuth App for " + consumerKey);
            String spAppName = appMgtService.getServiceProviderNameByClientId(consumerKey, "oauth2", tenantDomain);

            if (spAppName == null) {
                log.debug("Couldn't find OAuth App for Consumer Key : " + consumerKey);
                return;
            }

            // Skip deleting the default app or role. Only delete records from IDN_OAUTH_CONSUMER_APPS
            if (IdentityApplicationConstants.DEFAULT_SP_CONFIG.equals(spAppName)) {
                log.debug("Avoided removing the default app : " + spAppName);
                log.debug("However, OAuth details for the default app will be removed.");
                OAuthAdminService oAuthAdminService = new OAuthAdminService();
                oAuthAdminService.removeOAuthApplicationData(consumerKey);
            } else {
                log.debug("Removing Service Provider with name : " + spAppName);
                appMgtService.deleteApplication(spAppName, tenantDomain, tenantAwareUsername);
            }
            if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
                OAuthCache oAuthCache = OAuthCache.getInstance();
                oAuthCache.clearCacheEntry(new OAuthCacheKey(consumerKey));
            }
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
     * Get the list of subscribed APIs of a user
     *
     * @param userId User/Developer name
     * @return An array of APIInfoDTO instances, each instance containing information of provider name,
     *         api name and version.
     * @throws APIKeyMgtException Error when getting the list of APIs from the persistence store.
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getSubscribedAPIsOfUser(userId);
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
                getFirstProperty(APIConstants.TOKEN_ENDPOINT_NAME);
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
                getFirstProperty(APIConstants.REVOKE_API_URL);

		URL revokeEndpointURL = new URL(revokeEndpoint);
		String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
		int revokeEndpointPort = revokeEndpointURL.getPort();
	

        HttpClient tokenEPClient =  APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
        HttpClient revokeEPClient = APIUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);
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
        
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
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
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
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
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        if (gatewayExists) {
            keys = dao.getApplicationKeys(application.getId());
            apiSet = dao.getSubscribedAPIs(application.getSubscriber(), null);
        }
        List<APIKeyMapping> mappings = new ArrayList<APIKeyMapping>();
        if(keys != null) {
            for (String key : keys) {
                dao.revokeAccessToken(key);
                if (apiSet != null) {
                    for (SubscribedAPI api : apiSet) {
                        APIKeyMapping mapping = new APIKeyMapping();
                        API apiDefinition = APIKeyMgtUtil.getAPI(api.getApiId());
                        mapping.setApiVersion(api.getApiId().getVersion());
                        mapping.setContext(apiDefinition.getContext());
                        mapping.setKey(key);
                        mappings.add(mapping);
                    }
                }
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
        dao = ApiMgtDAO.getInstance();
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
        dao = ApiMgtDAO.getInstance();
        Application[] applications = dao.getApplicationsByTier(tierName);
        for (Application application : applications) {
            revokeAccessTokenForApplication(application);
        }
    }

    public void clearOAuthCache(String consumerKey, String authorizedUser) {
        OAuthCache oauthCache;
        OAuthCacheKey cacheKey = new OAuthCacheKey(consumerKey + ":" + authorizedUser);
        if (OAuthServerConfiguration.getInstance().isCacheEnabled()) {
            oauthCache = OAuthCache.getInstance();
            oauthCache.clearCacheEntry(cacheKey);
        }
    }


    /**
     * Service method to revoke all access tokens issued for given user under the given application. This will change
     * access token status to revoked and remove cached access tokens from memory of all gateway nodes.
     * @param userName end user name
     * @param appName application name
     * @return if operation is success
     * @throws APIManagementException in case of revoke failure.
     */
    public boolean revokeTokensOfUserByApp(String userName, String appName)
            throws APIManagementException {

        try {

            //find access tokens for user
            List<AccessTokenInfo> accessTokens = ApiMgtDAO.getAccessTokenListForUser(userName,appName);
            //find revoke urls
            List<String> APIGatewayURLs = getAPIGatewayURLs();
            List<String> APIRevokeURLs = new ArrayList<String>(APIGatewayURLs.size());

            for (String apiGatewayURL : APIGatewayURLs) {
                String [] apiGatewayURLs = apiGatewayURL.split(",");
                if(apiGatewayURL.length()> 1) {
                    //get https url
                    String apiHTTPSURL = apiGatewayURLs[1];
                    String revokeURL = apiHTTPSURL + getRevokeURLPath();
                    APIRevokeURLs.add(revokeURL);
                }
            }

            //for each access token call revoke
            for (AccessTokenInfo accessToken : accessTokens) {
                for (String apiRevokeURL : APIRevokeURLs) {
                    revokeAccessToken(accessToken.getAccessToken(), accessToken.getConsumerKey(), accessToken
                            .getConsumerSecret(), apiRevokeURL);
                }
            }

            log.info("Successfully revoked all tokens issued for user=" + userName + "for application " + appName);
            return true;

        } catch (SQLException e) {
            throw new APIManagementException("Error while revoking token for user=" + userName + " app="+ appName, e);
        }

    }

    /**
     * Get API gateway URLs defined on apiManager.xml
     * @return list of gateway urls
     */
    private  List<String> getAPIGatewayURLs() {
        APIManagerConfiguration apiConfig = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> APIEnvironments = apiConfig.getApiGatewayEnvironments();
        List<String> gatewayURLs = new ArrayList<String>(2);
        for (Environment environment : APIEnvironments.values()) {
            gatewayURLs.add(environment.getApiGatewayEndpoint());
        }
        return gatewayURLs;
    }

    /**
     * Get file name part of revoke url configured in APIMgt.xml file.
     * (i.e revoke in https://${carbon.local.ip}:${https.nio.port}/revoke)
     * @return file name part as a string
     */
    private String getRevokeURLPath() {
        APIManagerConfiguration apiConfig = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String revokeURL = apiConfig.getFirstProperty(APIConstants.REVOKE_API_URL);
        URL revokeEndpointURL = new URL(revokeURL);
        return revokeEndpointURL.getFileName();
    }

    /**
     * Revoke the given access token. This call will reach gateway and clear token caches there as well
     * @param accessToken access token to revoke
     * @param consumerKey consumer key
     * @param consumerSecret consumer secret
     * @param revokeEndpoint revoke endpoint of the gateway
     * @throws APIManagementException
     */
    private void revokeAccessToken(String accessToken, String consumerKey, String consumerSecret, String
            revokeEndpoint) throws APIManagementException {
        try {
            if (accessToken != null) {
                URL revokeEndpointURL = new URL(revokeEndpoint);
                String revokeEndpointProtocol = revokeEndpointURL.getProtocol();
                int revokeEndpointPort = revokeEndpointURL.getPort();

                HttpClient revokeEPClient = APIUtil.getHttpClient(revokeEndpointPort, revokeEndpointProtocol);

                HttpPost httpRevokePost = new HttpPost(revokeEndpoint);

                // Request parameters.
                List<NameValuePair> revokeParams = new ArrayList<NameValuePair>(3);
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_ID, consumerKey));
                revokeParams.add(new BasicNameValuePair(OAuth.OAUTH_CLIENT_SECRET, consumerSecret));
                revokeParams.add(new BasicNameValuePair("token", accessToken));


                //Revoke the Old Access Token
                httpRevokePost.setEntity(new UrlEncodedFormEntity(revokeParams, "UTF-8"));
                HttpResponse revokeResponse = revokeEPClient.execute(httpRevokePost);

                if (revokeResponse.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Token revoke failed : HTTP error code : " +
                            revokeResponse.getStatusLine().getStatusCode());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully submitted revoke request for user token " + accessToken+ ". HTTP " +
                                "status : 200");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            handleException("Error while preparing request for token/revoke APIs", e);
        } catch (IOException e) {
            handleException("Error while creating tokens - " + e.getMessage(), e);
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
     * Convert {@link org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO} to an
     * {@link org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo}
     *
     * @param createdApp Response from OAuthAdminService
     * @return Converted {@link org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo}
     */
    private OAuthApplicationInfo createOAuthAppInfoFromDTO(OAuthConsumerAppDTO createdApp) {
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
    }
}

