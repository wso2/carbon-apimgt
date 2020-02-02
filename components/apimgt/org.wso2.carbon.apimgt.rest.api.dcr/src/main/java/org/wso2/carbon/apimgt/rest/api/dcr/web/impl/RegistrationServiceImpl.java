/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.dcr.web.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.dcr.web.RegistrationService;
import org.wso2.carbon.apimgt.rest.api.dcr.web.dto.FaultResponse;
import org.wso2.carbon.apimgt.rest.api.dcr.web.dto.RegistrationProfile;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.api.model.ApplicationConstants.OAUTH_CLIENT_GRANT;
import static org.wso2.carbon.apimgt.api.model.ApplicationConstants.OAUTH_CLIENT_NAME;
import static org.wso2.carbon.apimgt.api.model.ApplicationConstants.OAUTH_CLIENT_USERNAME;
import static org.wso2.carbon.apimgt.api.model.ApplicationConstants.OAUTH_REDIRECT_URIS;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationServiceImpl implements RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationServiceImpl.class);
    private static final String APP_DISPLAY_NAME = "DisplayName";

    @POST
    @Override
    public Response register(RegistrationProfile profile) {
        /**
         * sample message to this method
         * {
         * "callbackUrl": "www.google.lk",
         * "clientName": "mdm",
         * "tokenScope": "Production",
         * "owner": "admin",
         * "grantType": "password refresh_token",
         * "saasApp": true
         *}
         */
        Response response;
        String applicationName = null;
        ErrorDTO errorDTO;
        try {
            OAuthAppRequest appRequest = new OAuthAppRequest();
            OAuthApplicationInfo oauthApplicationInfo = new OAuthApplicationInfo();
            OAuthApplicationInfo returnedAPP;
            String loggedInUserTenantDomain;
            String owner = profile.getOwner();
            String authUserName = RestApiUtil.getLoggedInUsername();

            //Validates if the application owner and logged in username is same.
            if (authUserName != null && ((authUserName.equals(owner))|| isUserSuperAdmin(authUserName))) {
                if (!isUserAccessAllowed(authUserName)) {
                    String errorMsg = "You do not have enough privileges to create an OAuth app";
                    log.error("User " + authUserName +
                            " does not have any of subscribe/create/publish privileges " +
                            "to create an OAuth app");
                    errorDTO =
                            RestApiUtil.getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT,
                                    403L, errorMsg);
                    response = Response.status(Response.Status.FORBIDDEN).entity(errorDTO).build();
                    return response;
                }
                //Getting client credentials from the profile
                String grantTypes = profile.getGrantType();
                oauthApplicationInfo.setClientName(profile.getClientName());
                if (StringUtils.isNotBlank(profile.getCallbackUrl())) {
                    oauthApplicationInfo.setCallBackURL(profile.getCallbackUrl());
                } else {
                    String[] grantTypeArr = grantTypes.split(" ");
                    for (String grantType : grantTypeArr) {
                        if ((grantType.equalsIgnoreCase(ApplicationConstants.AUTHORIZATION_CODE)) ||
                                (grantType.equalsIgnoreCase(ApplicationConstants.IMPLICIT_CONST))) {
                            grantTypes = grantTypes.replace(grantType, "");
                        }
                    }
                }

                String tokenType = APIConstants.DEFAULT_TOKEN_TYPE;
                String profileTokenType = profile.getTokenType();
                if (StringUtils.isNotEmpty(profileTokenType)) {
                    tokenType = profileTokenType;
                }
                oauthApplicationInfo.addParameter(OAUTH_CLIENT_USERNAME, owner);
                oauthApplicationInfo.setClientId("");
                oauthApplicationInfo.setClientSecret("");
                oauthApplicationInfo.setIsSaasApplication(profile.isSaasApp());
                oauthApplicationInfo.setTokenType(tokenType);
                appRequest.setOAuthApplicationInfo(oauthApplicationInfo);
                if (!authUserName.equals(owner)){
                    loggedInUserTenantDomain = MultitenantUtils.getTenantDomain(owner);
                }else{
                    loggedInUserTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                }
                String userId = (String) oauthApplicationInfo.getParameter(OAUTH_CLIENT_USERNAME);
                String userNameForSP = MultitenantUtils.getTenantAwareUsername(userId);
                // Replace domain separator by "_" if user is coming from a secondary userstore.
                String domain = UserCoreUtil.extractDomainFromName(userNameForSP);
                if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals
                        (domain)) {
                    userNameForSP = userNameForSP.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
                }
                applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + profile.getClientName();

                ApplicationManagementService applicationManagementService =
                        ApplicationManagementService.getInstance();

                //Check if the application is already exists
                ServiceProvider appServiceProvider = null;
                try {
                    appServiceProvider =
                            applicationManagementService.getApplicationExcludingFileBasedSPs(
                                    applicationName, loggedInUserTenantDomain);
                } catch (IdentityApplicationManagementException e) {
                    log.error("Error occurred while checking the existence of the application " +
                            applicationName, e);
                }
                //Retrieving the existing application
                if (appServiceProvider != null) {
                    returnedAPP = this.getExistingApp(applicationName, appServiceProvider.isSaasApp());
                } else {
                    //create a new application if the application doesn't exists.
                    returnedAPP = this.createApplication(applicationName, appRequest, grantTypes);
                }
                //ReturnedAPP is null
                if (returnedAPP == null) {
                    String errorMsg = "OAuth app '" + profile.getClientName() +
                            "' creation or updating failed." +
                            " Dynamic Client Registration Service not available.";
                    log.error(errorMsg);
                    errorDTO = RestApiUtil.getErrorDTO
                            (RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 500L, errorMsg);
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                            entity(errorDTO).build();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("OAuth app " + profile.getClientName() + " creation successful.");
                    }
                    response = Response.status(Response.Status.OK).entity(returnedAPP).build();
                }
            } else {
                String errorMsg = "Logged in user '" + authUserName + "' and application owner '" +
                        owner + "' should be same.";
                errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT,
                        400L, errorMsg);
                response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMsg = "Error occurred while trying to create the client application " +
                    applicationName;
            log.error(errorMsg, e);
            errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT,
                    500L, errorMsg);
            response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
        }
        return response;
    }

    @DELETE
    @Override
    public Response unRegister(@QueryParam("applicationName") String applicationName,
            @QueryParam("userId") String userId,
            @QueryParam("consumerKey") String consumerKey) {
        Response response;
        try {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("Dynamic Client Registration Service's resource deletion not implemented.").
                    build();
        } catch (Exception e) {
            String msg = "Error occurred while un-registering client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.serverError().
                    entity(new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
        }
        return response;
    }

    /**
     * Check whether user have any of create, publish or subscribe permissions
     *
     * @param username username
     * @return true if user has any of create, publish or subscribe permissions
     */
    private boolean isUserAccessAllowed(String username) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking 'subscribe' permission for user " + username);
            }
            APIUtil.checkPermission(username, APIConstants.Permissions.API_SUBSCRIBE);
            return true;
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " does not have subscriber permission", e);
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking 'api publish' permission for user " + username);
            }
            APIUtil.checkPermission(username, APIConstants.Permissions.API_PUBLISH);
            return true;
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " does not have 'api publish' permission", e);
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking 'api create' permission for user " + username);
            }
            APIUtil.checkPermission(username, APIConstants.Permissions.API_CREATE);
            return true;
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("User " + username + " does not have 'api create' permission", e);
            }
        }
        return false;
    }

    /**
     * Retrieve the existing application of given name
     *
     * @param applicationName application name
     * @param saasApp         value of IsSaasApp attribute of application.
     * @return existing Application
     */
    private OAuthApplicationInfo getExistingApp(String applicationName, boolean saasApp) {

        OAuthApplicationInfo appToReturn = null;
        OAuthAdminService oAuthAdminService = new OAuthAdminService();
        try {
            OAuthConsumerAppDTO consumerAppDTO = oAuthAdminService.
                    getOAuthApplicationDataByAppName(applicationName);
            Map<String, String> valueMap = new HashMap<String, String>();
            valueMap.put(OAUTH_CLIENT_GRANT, consumerAppDTO.getGrantTypes());

            appToReturn = this.fromAppDTOToApplicationInfo(consumerAppDTO.getOauthConsumerKey(),
                    consumerAppDTO.getApplicationName(), consumerAppDTO.getCallbackUrl(),
                    consumerAppDTO.getOauthConsumerSecret(), saasApp, null, valueMap);

        } catch (IdentityOAuthAdminException e) {
            log.error("error occurred while trying to get OAuth Application data", e);
        }
        return appToReturn;
    }

    /**
     * Create a new client application
     *
     * @param appRequest OAuthAppRequest object with client's payload content
     * @return created Application
     * @throws APIKeyMgtException if failed to create the a new application
     */
    private OAuthApplicationInfo createApplication(String applicationName, OAuthAppRequest appRequest,
            String grantType) throws APIManagementException {
        String userName;
        OAuthApplicationInfo applicationInfo = appRequest.getOAuthApplicationInfo();
        String appName = applicationInfo.getClientName();
        String userId = (String) applicationInfo.getParameter(OAUTH_CLIENT_USERNAME);
        boolean isTenantFlowStarted = false;

        if (userId == null || userId.isEmpty()) {
            return null;
        }
        userName = MultitenantUtils.getTenantAwareUsername(userId);
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.
                    equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            //Creating the service provider
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            serviceProvider.setDescription("Service Provider for application " + appName);
            serviceProvider.setSaasApp(applicationInfo.getIsSaasApplication());
            ServiceProviderProperty[] serviceProviderProperties = new ServiceProviderProperty[3];
            ServiceProviderProperty serviceProviderProperty = new ServiceProviderProperty();
            serviceProviderProperty.setName(APP_DISPLAY_NAME);
            serviceProviderProperty.setValue(applicationName);
            serviceProviderProperties[0] = serviceProviderProperty;
            ServiceProviderProperty tokenTypeProviderProperty = new ServiceProviderProperty();
            tokenTypeProviderProperty.setName(APIConstants.APP_TOKEN_TYPE);
            tokenTypeProviderProperty.setValue(applicationInfo.getTokenType());
            serviceProviderProperties[1] = tokenTypeProviderProperty;
            ServiceProviderProperty consentProperty = new ServiceProviderProperty();
            consentProperty.setDisplayName(APIConstants.APP_SKIP_CONSENT_DISPLAY);
            consentProperty.setName(APIConstants.APP_SKIP_CONSENT_NAME);
            consentProperty.setValue(APIConstants.APP_SKIP_CONSENT_VALUE);
            serviceProviderProperties[2] = consentProperty;
            serviceProvider.setSpProperties(serviceProviderProperties);
            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            appMgtService.createApplication(serviceProvider, tenantDomain, userName);

            //Retrieving the created service provider
            ServiceProvider createdServiceProvider =
                    appMgtService.getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
            if (createdServiceProvider == null) {
                throw new APIManagementException("Error occurred while creating Service Provider " +
                        "Application" + appName);
            }

            //creating the OAuth app
            OAuthConsumerAppDTO createdOauthApp =
                    this.createOAuthApp(applicationName, applicationInfo, grantType, userName);

            // Set the OAuthApp in InboundAuthenticationConfig
            InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
            InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs =
                    new InboundAuthenticationRequestConfig[1];
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                    new InboundAuthenticationRequestConfig();

            String oAuthType = APIConstants.SWAGGER_12_OAUTH2;
            inboundAuthenticationRequestConfig.setInboundAuthType(oAuthType);
            inboundAuthenticationRequestConfig.setInboundAuthKey(createdOauthApp.getOauthConsumerKey());
            String oauthConsumerSecret = createdOauthApp.getOauthConsumerSecret();

            if (oauthConsumerSecret != null && !oauthConsumerSecret.isEmpty()) {
                Property property = new Property();
                property.setName(ApplicationConstants.INBOUNT_AUTH_CONSUMER_SECRET);
                property.setValue(oauthConsumerSecret);
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }

            inboundAuthenticationRequestConfigs[0] = inboundAuthenticationRequestConfig;
            inboundAuthenticationConfig.
                    setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs);
            createdServiceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

            //Setting the SaasApplication attribute to created service provider
            createdServiceProvider.setSaasApp(applicationInfo.getIsSaasApplication());
            createdServiceProvider.setSpProperties(serviceProviderProperties);

            //Updating the service provider with Inbound Authentication Configs and SaasApplication
            appMgtService.updateApplication(createdServiceProvider, tenantDomain, userName);

            Map<String, String> valueMap = new HashMap<String, String>();
            valueMap.put(OAUTH_REDIRECT_URIS, createdOauthApp.getCallbackUrl());
            valueMap.put(OAUTH_CLIENT_NAME, createdOauthApp.getApplicationName());
            valueMap.put(OAUTH_CLIENT_GRANT, createdOauthApp.getGrantTypes());

            return this.fromAppDTOToApplicationInfo(createdOauthApp.getOauthConsumerKey(),
                    applicationName, createdOauthApp.getCallbackUrl(), createdOauthApp.getOauthConsumerSecret(),
                    createdServiceProvider.isSaasApp(), userId, valueMap);

        } catch (IdentityApplicationManagementException e) {
            log.error("Error occurred while creating the client application " + appName,e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
            }
        }
        return null;
    }

    /**
     * Method to create a OAuth App with client credentials
     *
     * @param appName    application name
     * @param grantTypes grant types
     * @param userName   username of the application
     * @return created Oauth App
     */
    private OAuthConsumerAppDTO createOAuthApp(String appName, OAuthApplicationInfo applicationInfo,
            String grantTypes, String userName) {
        OAuthConsumerAppDTO createdApp = null;
        OAuthAdminService oauthAdminService = new OAuthAdminService();
        OAuthConsumerAppDTO oauthConsumerAppDTO = new OAuthConsumerAppDTO();
        oauthConsumerAppDTO.setApplicationName(appName);
        if (StringUtils.isNotBlank(applicationInfo.getCallBackURL())) {
            oauthConsumerAppDTO.setCallbackUrl(applicationInfo.getCallBackURL());
        }
        oauthConsumerAppDTO.setUsername(userName);
        oauthConsumerAppDTO.setOAuthVersion(OAuthConstants.OAuthVersions.VERSION_2);
        oauthConsumerAppDTO.setGrantTypes(grantTypes.trim());
        try {
            boolean isHashDisabled = OAuth2Util.isHashDisabled();
            if (isHashDisabled) {
                //Creating the Oauth app
                oauthAdminService.registerOAuthApplicationData(oauthConsumerAppDTO);

                //Retrieving the created OAuth application
                createdApp = oauthAdminService.getOAuthApplicationDataByAppName
                        (oauthConsumerAppDTO.getApplicationName());
            } else {
                createdApp = oauthAdminService.registerAndRetrieveOAuthApplicationData(oauthConsumerAppDTO);
            }
        } catch (IdentityOAuthAdminException e) {
            log.error("Error occurred while creating the OAuth app", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Created OAuth App " + appName);
        }
        return createdApp;
    }

    /**
     * Creating a OAuthApplicationInfo type object to return
     *
     * @param clientId     client id
     * @param clientName   client name
     * @param callbackUrl  callback url
     * @param clientSecret clientSecret
     * @param saasApp      IsSaasApp
     * @param appOwner     AppOwner
     * @param sampleMap    Map
     * @return OAuthApplicationInfo object containing parsed values.
     */
    private OAuthApplicationInfo fromAppDTOToApplicationInfo(String clientId, String clientName,
            String callbackUrl, String clientSecret,
            boolean saasApp, String appOwner,
            Map<String, String> sampleMap) {

        OAuthApplicationInfo updatingApp = new OAuthApplicationInfo();
        updatingApp.setClientId(clientId);
        updatingApp.setClientName(clientName);
        updatingApp.setCallBackURL(callbackUrl);
        updatingApp.setClientSecret(clientSecret);
        updatingApp.setIsSaasApplication(saasApp);
        updatingApp.setAppOwner(appOwner);

        Iterator it = sampleMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            updatingApp.addParameter((String) pair.getKey(), pair.getValue());
            it.remove();
        }
        return updatingApp;
    }

    private boolean isUserSuperAdmin(String username) {

        try {
            RealmConfiguration realmConfig = new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
            String adminUserName = realmConfig.getAdminUserName();
            return adminUserName.equalsIgnoreCase(username);
        } catch (UserStoreException e) {
            log.error("Error while retrieving super admin username", e);
            return false;
        }
    }
}
