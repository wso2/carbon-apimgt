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
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service class exposes the functionality required by the application developers who will be
 * consuming the APIs published in the API Store.
 */
public class APIKeyMgtSubscriberService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(APIKeyMgtSubscriberService.class);


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
            String displayName;
            if (applicationName.endsWith("_" + APIConstants.API_KEY_TYPE_PRODUCTION) || applicationName.endsWith("_"
                    + APIConstants.API_KEY_TYPE_SANDBOX)) {
                displayName = applicationName.substring(0, applicationName.lastIndexOf("_"));
            } else {
                displayName = applicationName;
            }
            applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + applicationName;

            // Create the Service Provider
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("Service Provider for application " + applicationName);
            ServiceProviderProperty[] serviceProviderProperties = new ServiceProviderProperty[1];
            ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
            serviceProviderProperty.setName(APIConstants.APP_DISPLAY_NAME);
            serviceProviderProperty.setValue(displayName);
            serviceProviderProperties[0] = serviceProviderProperty;
            serviceProvider.setSpProperties(serviceProviderProperties);
            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            appMgtService.createApplication(serviceProvider, tenantDomain, userName);
            ServiceProvider serviceProviderCreated = appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
            serviceProviderCreated.setSaasApp(oauthApplicationInfo.getIsSaasApplication());

            if (serviceProviderCreated == null) {
                throw new APIKeyMgtException("Couldn't create Service Provider Application " + applicationName);
            }

            // Then Create OAuthApp
            OAuthAdminService oAuthAdminService = new OAuthAdminService();
            OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
            oAuthConsumerAppDTO.setApplicationName(applicationName);
            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
            //set username to avoid issues with email user name login
            oAuthConsumerAppDTO.setUsername(userName);
            String[] audienceStringArray = new String[1];
            audienceStringArray[0] = APIConstants.JWT_DEFAULT_AUDIENCE;
            oAuthConsumerAppDTO.setAudiences(audienceStringArray);

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
                if (jsonObj != null && jsonObj.has(APIConstants.JSON_CLIENT_ID)) {
                    String clientId = (String) jsonObj.get(APIConstants.JSON_CLIENT_ID);
                    if (!clientId.isEmpty()) {
                        oAuthConsumerAppDTO.setOauthConsumerKey(clientId);
                        if (jsonObj.has(APIConstants.JSON_CLIENT_SECRET)) {
                            String clientSecret = (String) jsonObj.get(APIConstants.JSON_CLIENT_SECRET);
                            if (!clientSecret.isEmpty()) {
                                oAuthConsumerAppDTO.setOauthConsumerSecret(clientSecret);
                            }
                        }
                    }
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
            OAuthConsumerAppDTO createdApp;
            boolean isHashingDiabled = OAuth2Util.isHashDisabled();
            if (isHashingDiabled) {
                oAuthAdminService.registerOAuthApplicationData(oAuthConsumerAppDTO);
                createdApp = oAuthAdminService.getOAuthApplicationDataByAppName(oAuthConsumerAppDTO
                        .getApplicationName());
            } else {
                createdApp = oAuthAdminService.registerAndRetrieveOAuthApplicationData(oAuthConsumerAppDTO);
            }
            // === Finished Creating OAuth App ===

            log.debug("Created OAuth App " + applicationName);
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
            serviceProviderCreated.setInboundAuthenticationConfig(inboundAuthenticationConfig);

            // Update the Service Provider app to add OAuthApp as an Inbound Authentication Config
            appMgtService.updateApplication(serviceProviderCreated,tenantDomain,userName);


            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            oAuthApplicationInfo.setClientId(createdApp.getOauthConsumerKey());
            oAuthApplicationInfo.setCallBackURL(createdApp.getCallbackUrl());
            oAuthApplicationInfo.setClientSecret(createdApp.getOauthConsumerSecret());
            oAuthApplicationInfo.setIsSaasApplication(serviceProviderCreated.isSaasApp());

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
    public OAuthApplicationInfo createOAuthApplication(String userId, String applicationName, String callbackUrl,
            String tokenType) throws APIKeyMgtException, APIManagementException {

        OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
        oauthApplicationInfo.setClientName(applicationName);
        oauthApplicationInfo.setCallBackURL(callbackUrl);
        oauthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, userId);
        oauthApplicationInfo.setTokenType(tokenType);
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
                String displayName;
                if (applicationName.endsWith("_" + APIConstants.API_KEY_TYPE_PRODUCTION) || applicationName.endsWith("_"
                        + APIConstants.API_KEY_TYPE_SANDBOX)) {
                    displayName = applicationName.substring(0, applicationName.lastIndexOf("_"));
                } else {
                    displayName = applicationName;
                }

                applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + applicationName;
                log.debug("Application Name has changed, hence updating Service Provider Name..");

                // Get ServiceProvider Name by consumer Key.
                ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
                String appName = appMgtService.getServiceProviderNameByClientId(consumerKey, "oauth2", tenantDomain);
                ServiceProvider serviceProvider =
                                        appMgtService.getApplicationExcludingFileBasedSPs(appName, tenantDomain);
                ServiceProvider serviceProviderUpdate = new ServiceProvider();
                if (serviceProvider != null) {
                    serviceProviderUpdate.setAccessUrl(serviceProvider.getAccessUrl());
                    serviceProviderUpdate.setApplicationID(serviceProvider.getApplicationID());
                    serviceProviderUpdate.setApplicationResourceId(serviceProvider.getApplicationResourceId());
                    serviceProviderUpdate.setCertificateContent(serviceProvider.getCertificateContent());
                    serviceProviderUpdate.setClaimConfig(serviceProvider.getClaimConfig());
                    serviceProviderUpdate.setDiscoverable(serviceProvider.isDiscoverable());
                    serviceProviderUpdate.setImageUrl(serviceProvider.getImageUrl());
                    serviceProviderUpdate.setInboundAuthenticationConfig(serviceProvider.getInboundAuthenticationConfig());
                    serviceProviderUpdate.setInboundProvisioningConfig(serviceProvider.getInboundProvisioningConfig());
                    serviceProviderUpdate.setJwksUri(serviceProvider.getJwksUri());
                    serviceProviderUpdate.setLocalAndOutBoundAuthenticationConfig(serviceProvider.getLocalAndOutBoundAuthenticationConfig());
                    serviceProviderUpdate.setOutboundProvisioningConfig(serviceProvider.getOutboundProvisioningConfig());
                    serviceProviderUpdate.setOwner(serviceProvider.getOwner());
                    serviceProviderUpdate.setPermissionAndRoleConfig(serviceProvider.getPermissionAndRoleConfig());
                    serviceProviderUpdate.setRequestPathAuthenticatorConfigs(serviceProvider.getRequestPathAuthenticatorConfigs());
                    serviceProviderUpdate.setSaasApp(serviceProvider.isSaasApp());

                    ServiceProviderProperty[] serviceProviderPropertiesArray = serviceProvider.getSpProperties();
                    ArrayList<ServiceProviderProperty> serviceProviderProperties = new ArrayList<>();
                    if (serviceProviderPropertiesArray != null) {
                        serviceProviderProperties = new ArrayList<>(Arrays.asList(serviceProviderPropertiesArray));
                    }
                    boolean displayNameExist = false;
                    //check displayName property and modify if found
                    for (ServiceProviderProperty serviceProviderProperty : serviceProviderProperties) {
                        if (APIConstants.APP_DISPLAY_NAME.equals(serviceProviderProperty.getName())) {
                            serviceProviderProperty.setValue(displayName);
                            displayNameExist = true;
                            break;
                        }
                    }
                    //if displayName not found add new property
                    if (!displayNameExist) {
                        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
                        serviceProviderProperty.setName(APIConstants.APP_DISPLAY_NAME);
                        serviceProviderProperty.setValue(displayName);
                        serviceProviderProperties.add(serviceProviderProperty);
                    }
                    serviceProviderUpdate.setSpProperties(serviceProviderProperties.toArray(new ServiceProviderProperty[0]));
                    serviceProviderUpdate.setApplicationName(applicationName);
                    serviceProviderUpdate.setDescription("Service Provider for application " + applicationName);
                    appMgtService.updateApplication(serviceProviderUpdate, tenantDomain, userName);
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
     * Update an OAuth application with the given user
     *
     * @param userId
     * @param applicationName
     * @param callbackUrl
     * @return
     * @throws APIKeyMgtException
     * @throws APIManagementException
     * @throws IdentityException
     */
    public OAuthApplicationInfo updateOAuthApplicationOwner(String userId, String ownerId, String applicationName,
                                                            String callbackUrl, String consumerKey, String[] grantTypes)
            throws APIKeyMgtException, APIManagementException, IdentityException {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String userName = MultitenantUtils.getTenantAwareUsername(userId);
        String ownerName = MultitenantUtils.getTenantAwareUsername(ownerId);
        String userNameForSP = userName;
        String authorizationCodeGrantType = "authorization_code";
        String implicitGrantType = "implicit";
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
                String displayName;
                if (applicationName.endsWith("_" + APIConstants.API_KEY_TYPE_PRODUCTION) || applicationName.endsWith("_"
                        + APIConstants.API_KEY_TYPE_SANDBOX)) {
                    displayName = applicationName.substring(0, applicationName.lastIndexOf("_"));
                } else {
                    displayName = applicationName;
                }
                // Get ServiceProvider Name by consumer Key.
                ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
                String appName = appMgtService.getServiceProviderNameByClientId(consumerKey, "oauth2", tenantDomain);
                ServiceProvider serviceProvider =
                        appMgtService.getApplicationExcludingFileBasedSPs(appName, tenantDomain);
                if (serviceProvider != null) {
                    serviceProvider.setApplicationName(applicationName);
                    serviceProvider.setDescription("Service Provider for application " + applicationName);
                    ServiceProviderProperty[] serviceProviderPropertiesArray = serviceProvider.getSpProperties();
                    ArrayList<ServiceProviderProperty> serviceProviderProperties = new ArrayList<>();
                    if (serviceProviderPropertiesArray != null) {
                        serviceProviderProperties = new ArrayList<>(Arrays.asList(serviceProviderPropertiesArray));
                    }
                    boolean displayNameExist = false;
                    //check displayName property and modify if found
                    for (ServiceProviderProperty serviceProviderProperty : serviceProviderProperties) {
                        if (APIConstants.APP_DISPLAY_NAME.equals(serviceProviderProperty.getName())) {
                            serviceProviderProperty.setValue(displayName);
                            displayNameExist = true;
                            break;
                        }
                    }
                    //if displayName not found add new property
                    if (!displayNameExist) {
                        ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
                        serviceProviderProperty.setName(APIConstants.APP_DISPLAY_NAME);
                        serviceProviderProperty.setValue(displayName);
                        serviceProviderProperties.add(serviceProviderProperty);
                    }
                    serviceProvider.setSpProperties(serviceProviderProperties.toArray(new ServiceProviderProperty[0]));
                    serviceProvider.setApplicationName(applicationName);
                    serviceProvider.setOwner(User.getUserFromUserName(userName));
                    serviceProvider.setDescription("Service Provider for application " + applicationName);
                    appMgtService.updateApplication(serviceProvider, tenantDomain, ownerName);
                    log.debug("Service Provider Name Updated to : " + applicationName);
                }
            }
            OAuthAdminService oAuthAdminService = new OAuthAdminService();
            OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminService.getOAuthApplicationData(consumerKey);
            if (oAuthConsumerAppDTO != null) {
                if (callbackUrl != null && !callbackUrl.isEmpty()) {
                    oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
                    log.debug("CallbackURL is set to : " + callbackUrl);
                }
                oAuthConsumerAppDTO.setOauthConsumerKey(consumerKey);
                if (applicationName != null && !applicationName.isEmpty()) {
                    oAuthConsumerAppDTO.setApplicationName(applicationName);
                    log.debug("Name of the OAuthApplication is set to : " + applicationName);
                }
                if (userId != null && !userId.isEmpty()) {
                    oAuthConsumerAppDTO.setUsername(userName);
                    log.debug("Username of the OAuthApplication is set to : " + userName);
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
                            if (authorizationCodeGrantType.equals(grantType) || implicitGrantType.equals(grantType)) {
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
     * Service method to revoke all access tokens issued for given user under the given application. This will change
     * access token status to revoked and remove cached access tokens from memory of all gateway nodes.
     *
     * @param userName end user name
     * @param appName  application name
     * @param appOwner application owner username
     * @return if operation is success
     * @throws APIManagementException in case of revoke failure.
     */
    public boolean revokeTokensOfUserByApp(String userName, String appName, String appOwner)
            throws APIManagementException {
        List<AccessTokenInfo> accessTokens;
        String baseUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String baseUserTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String baseUserNameWithoutTenant = MultitenantUtils.getTenantAwareUsername(baseUsername);
        //Check if the username returned from CarbonContext.getThreadLocalCarbonContext() already contains domain name
        //Otherwise append the domain name to the username
        String baseUserNameWithTenant = (!baseUserNameWithoutTenant.equals(baseUsername)) ?
                baseUsername : baseUsername.concat(APIConstants.EMAIL_DOMAIN_SEPARATOR).concat(baseUserTenantDomain);
        userName = MultitenantUtils.getTenantAwareUsername(userName);
        try {
            if (appOwner != null) {
                if (log.isDebugEnabled()) {
                    log.debug("appOwner parameter present in the request to revoke tokens of user=" + userName +
                            " for application=" + appName);
                }
                String appOwnerTenantDomain = MultitenantUtils.getTenantDomain(appOwner);
                String appOwnerUserNameWithTenant = MultitenantUtils.getTenantAwareUsername(appOwner)
                        .concat(APIConstants.EMAIL_DOMAIN_SEPARATOR)
                        .concat(appOwnerTenantDomain);
                //If app owner is given as appowner@carbon.super get the tenant aware username 'admin'
                String appOwnerUserName = appOwnerTenantDomain.equals(APIConstants.SUPER_TENANT_DOMAIN) ?
                        MultitenantUtils.getTenantAwareUsername(appOwner) : appOwner;
                //If both app owner and logged in user in both tenants
                if (appOwnerTenantDomain.equals(baseUserTenantDomain)) {
                    if (log.isDebugEnabled()) {
                        log.debug("appOwner=" + appOwner + " and the logged in user=" + baseUserNameWithTenant +
                                " both exist in the same tenant");
                    }
                    UserStoreManager userstoremanager =
                            CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                    //Get the role list of logged in user
                    Collection<String> baseUserRoles = Arrays.asList(userstoremanager.
                            getRoleListOfUser((baseUserNameWithoutTenant)));
                    //Get admin role name of the current domain
                    String adminRoleName = CarbonContext
                            .getThreadLocalCarbonContext()
                            .getUserRealm()
                            .getRealmConfiguration()
                            .getAdminRoleName();
                    //If logged in user an admin or same as the app owner
                    if (baseUserRoles != null && (baseUserRoles.contains(adminRoleName) ||
                            baseUserNameWithTenant.equals(appOwnerUserNameWithTenant))) {
                        if (log.isDebugEnabled()) {
                            log.debug("Logged in user=" + baseUserNameWithTenant +
                                    " is either the tenant admin or the same app owner of application=" + appName);
                        }
                        //find access tokens for the given user for given the application owned by given owner
                        accessTokens = ApiMgtDAO.getAccessTokenListForUser(userName, appName, appOwnerUserName);
                    } else {
                        //Tokens can only be invoked by the tenant admin or the app owner, hence return error
                        String errorMessage = "Insufficient permission to revoke token for user=" + userName +
                                " for app=" + appName + " owned by=" + appOwner + " by logged in user=" +
                                baseUserNameWithTenant;
                        log.error(errorMessage);
                        throw new APIManagementException(errorMessage);
                    }
                } else {
                    //Tokens cannot be invoked by a user in another domain than the application owner
                    String errorMessage = "Logged in user=" + baseUserNameWithTenant +
                            " does not have access to revoke token for user=" + userName + " for app=" +
                            appName + " owned by=" + appOwner + " in tenant domain=" + appOwnerTenantDomain;
                    log.error(errorMessage);
                    throw new APIManagementException(errorMessage);
                }
            } else {
                //If appOwner field not present in the request, assume the logged in user as the application owner
                if (log.isDebugEnabled()) {
                    log.debug("appOwner parameter not present in the request to revoke tokens of user=" + userName +
                            " for application=" + appName);
                }
                String appOwnerUserName = baseUserTenantDomain.equals(APIConstants.SUPER_TENANT_DOMAIN) ?
                        baseUserNameWithoutTenant : baseUserNameWithTenant;
                accessTokens = ApiMgtDAO.getAccessTokenListForUser(userName, appName, appOwnerUserName);
            }
            //find revoke urls
            List<String> APIGatewayURLs = getAPIGatewayURLs();
            List<String> APIRevokeURLs = new ArrayList<String>(APIGatewayURLs.size());

            for (String apiGatewayURL : APIGatewayURLs) {
                String[] apiGatewayURLs = apiGatewayURL.split(",");
                if (apiGatewayURL.length() > 1) {
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
            if (!accessTokens.isEmpty()) {
                log.info("Successfully revoked all tokens by logged in user=" +
                        baseUserNameWithTenant + " issued for user= " + userName + " for application=" + appName +
                        " owned by=" + appOwner);
            } else {
                log.info("No active tokens to revoke by logged in user=" +
                        baseUserNameWithTenant + " for user= " + userName + " for application=" + appName +
                        " owned by=" + appOwner);
            }
            return true;
        } catch (SQLException e) {
            String errorMessage = "Error while revoking token for user=" + userName + " app=" + appName + " owned by=" +
                    appOwner + " by logged in user=" + baseUserNameWithTenant;
            log.error(errorMessage);
            throw new APIManagementException(errorMessage, e);
        } catch (UserStoreException e) {
            String errorMessage = "Error while authenticating the logged in user=" + baseUserNameWithTenant +
                    " while revoking token for user=" + userName + " app=" + appName + " owned by=" + appOwner;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
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

