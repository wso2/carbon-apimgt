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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.generated.thrift.APIKeyMgtException;
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
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.wso2.carbon.apimgt.api.model.ApplicationConstants.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationServiceImpl implements RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationServiceImpl.class);
    private OAuthApplicationInfo retrievedApp;
    private String applicationName, appName, userName;
    private String errorMsg;

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
//        Response response;
//        String errorMsg;
//        ErrorDTO errorDTO;
        Response response;
        String errorMsg,userNameForSP,grantTypes,authUserName;
        ErrorDTO errorDTO;
        OAuthApplicationInfo oauthApplicationInfo;
        ServiceProvider appServiceProvider = null;
//        try {
//            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
//            OAuthAppRequest appRequest = new OAuthAppRequest();
//            OAuthApplicationInfo applicationInfo = new OAuthApplicationInfo();
//
//            String owner = profile.getOwner();
//            String authUserName = RestApiUtil.getLoggedInUsername();
//            //validates if the application owner and logged in username is same.
//            if (authUserName != null && authUserName.equals(owner)) {
//                if (!isUserAccessAllowed(authUserName)) {
//                    String msg = "You do not have enough privileges to create an OAuth app";
//                    log.error("User " + authUserName + " does not have any of subscribe/create/publish privileges "
//                            + "to create an OAuth app");
//                    errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, msg);
//                    response = Response.status(Response.Status.FORBIDDEN).entity(errorDTO).build();
//                    return response;
//                }
//
//                applicationInfo.setClientName(profile.getClientName());
//                applicationInfo.setCallBackURL(profile.getCallbackUrl());
//                applicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, owner);
//                applicationInfo.setClientId("");
//                applicationInfo.setClientSecret("");
//                applicationInfo.setIsSaasApplication(profile.isSaasApp());
//                appRequest.setOAuthApplicationInfo(applicationInfo);
//                OAuthApplicationInfo returnedAPP = keyManager.createApplication(appRequest);
//                if (returnedAPP != null) {
//                    returnedAPP.removeParameter("tokenScope");
//                    return Response.status(Response.Status.CREATED).entity(returnedAPP).build();
//                }
//
//                //returnedAPP is null
//                errorMsg = "OAuth app '" + profile.getClientName()
//                        + "' creation failed. Dynamic Client Registration Service not available.";
//                log.error(errorMsg);
//                errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 500l, errorMsg);
//                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
//            } else {
//                errorMsg = "Logged in user '" + authUserName + "' and application owner '" + owner
//                        + "' should be same.";
//                log.error(errorMsg);
//                errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, errorMsg);
//                response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
//            }
//        } catch (APIManagementException e) {
//            String msg = "Error occurred while registering client '" + profile.getClientName() + "'";
//            errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, msg);
//            response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
//            log.error(msg, e);
//        }

        try {
            OAuthAppRequest appRequest = new OAuthAppRequest();
            oauthApplicationInfo = new OAuthApplicationInfo();
            OAuthApplicationInfo returnedAPP;
            String loggedInUserTenantDomain;

            String owner = profile.getOwner();
            authUserName = RestApiUtil.getLoggedInUsername();

            //validates if the application owner and logged in username is same.
            if (authUserName != null && authUserName.equals(owner)) {
                if (!isUserAccessAllowed(authUserName)) {
                    errorMsg = "You do not have enough privileges to create an OAuth app";
                    log.error("User " + authUserName + " does not have any of subscribe/create/publish privileges "
                            + "to create an OAuth app");
                    errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT,
                            403L, errorMsg);
                    response = Response.status(Response.Status.FORBIDDEN).entity(errorDTO).build();
                    return response;
                }

                //getting client credentials from the profile
                oauthApplicationInfo.setClientName(profile.getClientName());
                oauthApplicationInfo.setCallBackURL(profile.getCallbackUrl());
                oauthApplicationInfo.addParameter(OAUTH_CLIENT_USERNAME, owner);
                oauthApplicationInfo.setClientId("");
                oauthApplicationInfo.setClientSecret("");
                oauthApplicationInfo.setIsSaasApplication(profile.isSaasApp());
                appRequest.setOAuthApplicationInfo(oauthApplicationInfo);

                loggedInUserTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

                String userId = (String) oauthApplicationInfo.getParameter(OAUTH_CLIENT_USERNAME);
                userName = MultitenantUtils.getTenantAwareUsername(userId);
                userNameForSP = userName;
                applicationName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + profile.getClientName();

                grantTypes = profile.getGrantType();

                ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();

                //Check if the application is already exists
                try {
                    appServiceProvider = applicationManagementService.getApplicationExcludingFileBasedSPs(
                            applicationName, loggedInUserTenantDomain);
                } catch (IdentityApplicationManagementException e) {
                    errorMsg = "Error occured while checking the availability of the client " + applicationName;
                    log.error(errorMsg);
                }
                if (appServiceProvider != null) {
                    //retrieving the existing application
                    retrievedApp = this.getExistingApp(applicationName, appServiceProvider.isSaasApp());

                    //check if the client request values and existing application's values are different
                    if (retrievedApp.getIsSaasApplication() != oauthApplicationInfo.getIsSaasApplication() ||
                            !retrievedApp.getCallBackURL().equals(oauthApplicationInfo.getCallBackURL())) {

                        //checking if the callback urls is different in client request
                        if (!retrievedApp.getCallBackURL().equals(oauthApplicationInfo.getCallBackURL())) {
                            this.updateCallbackUrl(oauthApplicationInfo.getCallBackURL(),grantTypes,authUserName);
                        }
                        //checking if the IsSaasApplication is different in client request
                        if (retrievedApp.getIsSaasApplication() != oauthApplicationInfo.getIsSaasApplication()) {
                            this.updateSaasApp(oauthApplicationInfo.getIsSaasApplication(),appServiceProvider,
                                    loggedInUserTenantDomain);
                        }
                        //parameter values are input insert into a map
                        HashMap<String, String> returnMap = new HashMap<String,String>();
                        returnMap.put(OAUTH_CLIENT_USERNAME, owner);

                        //mapping updated values to a OAuthApplicationInfo object
                        returnedAPP = this.settingUpdatingValues(retrievedApp.getClientId(), retrievedApp.getClientName(),
                                oauthApplicationInfo.getCallBackURL(), retrievedApp.getClientSecret(),
                                oauthApplicationInfo.getIsSaasApplication(), null, returnMap);
                    } else {
                        returnedAPP = retrievedApp;
                    }
                } else {
                    //create a new client application if the application name doesn't exists.
                    returnedAPP = this.createApplication(appRequest,userNameForSP,grantTypes);
                    if (returnedAPP != null) {
                        return Response.status(Response.Status.CREATED).entity(returnedAPP).build();
                    }
                }
                if (returnedAPP == null) {
                    errorMsg = "OAuth app '" + profile.getClientName()
                            + "' creation or updating failed. Dynamic Client Registration Service not available.";
                    log.error(errorMsg);
                    errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 500L,
                            errorMsg);
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
                } else {
                    String infoMsg = "OAuth app " + profile.getClientName() + " creation successful. ";
                    log.info(infoMsg);
                    response = Response.status(Response.Status.OK).entity(returnedAPP).build();
                }
            } else {
                errorMsg = "Logged in user '" + authUserName + "' and application owner '" + owner
                        + "' should be same.";
                errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, errorMsg);
                response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }
        } catch (APIKeyMgtException e) {
            errorMsg = "Error occured while trying to create the client application " + applicationName;
            log.error(errorMsg);
            errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 500L, errorMsg);
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
                    entity("Dynamic Client Registration Service's resource deletion not implemented.").build();
        } catch (Exception e) {
            String msg = "Error occurred while un-registering client '" + applicationName + "'";
            log.error(msg, e);
            response = Response.serverError().entity(new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
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
            log.debug("checking 'subscribe' permission for user " + username);
            APIUtil.checkPermission(username, APIConstants.Permissions.API_SUBSCRIBE);
            return true;
        } catch (APIManagementException e) {
            log.debug("user " + username + " does not have subscriber permission", e);
        }

        try {
            log.debug("checking 'api publish' permission for user " + username);
            APIUtil.checkPermission(username, APIConstants.Permissions.API_PUBLISH);
            return true;
        } catch (APIManagementException e) {
            log.debug("user " + username + " does not have 'api publish' permission", e);
        }

        try {
            log.debug("checking 'api create' permission for user " + username);
            APIUtil.checkPermission(username, APIConstants.Permissions.API_CREATE);
            return true;
        } catch (APIManagementException e) {
            log.debug("user " + username + " does not have 'api create' permission", e);
        }
        return false;
    }

    /**
     * retrieve the existing client application
     * @param applicationName client application name
     * @param saasApp value of retrived IsSaasApp attribute.
     * @return existing Application data
     */
    private OAuthApplicationInfo getExistingApp(String applicationName, boolean saasApp){

        OAuthApplicationInfo appToReturn=null;
        OAuthAdminService oAuthAdminService = new OAuthAdminService();
        try {
            OAuthConsumerAppDTO consumerAppDTO = oAuthAdminService.getOAuthApplicationDataByAppName(applicationName);
            Map<String, String> valueMap = new HashMap<String,String>();
            valueMap.put(OAUTH_CLIENT_GRANT, consumerAppDTO.getGrantTypes());

            appToReturn = this.settingUpdatingValues(consumerAppDTO.getOauthConsumerKey(),
                    consumerAppDTO.getApplicationName(),consumerAppDTO.getCallbackUrl(),
                    consumerAppDTO.getOauthConsumerSecret(),saasApp,null,valueMap);

        } catch (IdentityOAuthAdminException e) {
            errorMsg="error occur while tryng to get OAuth Application data";
            log.error(errorMsg);
        }
        return appToReturn;
    }

    /**
     *create a new client application
     * @param appRequest Client request credentials
     * @return created Application
     * @throws APIKeyMgtException
     */
    private OAuthApplicationInfo createApplication(OAuthAppRequest appRequest,String userNameForSP,
                                                   String grantType) throws APIKeyMgtException {

        OAuthApplicationInfo applicationInfo = appRequest.getOAuthApplicationInfo();

        String callbackUrl = applicationInfo.getCallBackURL();
        appName = applicationInfo.getClientName();
        String userId = (String) applicationInfo.getParameter(OAUTH_CLIENT_USERNAME);
        boolean isTenantFlowStarted = false;

        if (userId == null || userId.isEmpty()) {
            return null;
        }
        userName = MultitenantUtils.getTenantAwareUsername(userId);
        userNameForSP = userName;
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String domain = UserCoreUtil.extractDomainFromName(userNameForSP);

        try {

            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domain)) {
                userNameForSP = userNameForSP.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
            }
            // Append the username before Application name to make application name unique across two users.
            appName = APIUtil.replaceEmailDomain(userNameForSP) + "_" + appName;

            //creating the service provider
            ServiceProvider serviceprovider = new ServiceProvider();
            serviceprovider.setApplicationName(appName);
            serviceprovider.setDescription("Service Provider for application " + appName);
            serviceprovider.setSaasApp(applicationInfo.getIsSaasApplication());

            ApplicationManagementService appMgtService = ApplicationManagementService.getInstance();
            appMgtService.createApplication(serviceprovider, tenantDomain, userName);

            //retrieving the created service provider
            ServiceProvider createdServiceProvider = appMgtService.getApplicationExcludingFileBasedSPs
                    (appName, tenantDomain);
            if (createdServiceProvider == null) {
                throw new APIKeyMgtException("Error occured while creating Service Provider Application" + appName);
            }

            //creating the OAuth app
            OAuthConsumerAppDTO createdOauthApp = this.createOAuthApp(callbackUrl,grantType);

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
            inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs);
            createdServiceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

            //updating the service provider with Inbound Authentication Configs
            appMgtService.updateApplication(createdServiceProvider, tenantDomain, userName);

            Map<String, String> valueMap = new HashMap<String,String>();
            valueMap.put(OAUTH_REDIRECT_URIS, createdOauthApp.getCallbackUrl());
            valueMap.put(OAUTH_CLIENT_NAME, createdOauthApp.getApplicationName());
            valueMap.put(OAUTH_CLIENT_GRANT, createdOauthApp.getGrantTypes());

            //adjusting values to be return in to the user
            return this.settingUpdatingValues(createdOauthApp.getOauthConsumerKey(),
                    null, createdOauthApp.getCallbackUrl(), createdOauthApp.getOauthConsumerSecret(),
                    createdServiceProvider.isSaasApp(), userId, valueMap);

        } catch (IdentityApplicationManagementException e) {
            errorMsg = "Error occured while creating the client application " + appName;
            log.error(errorMsg);
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
     * @return created client App
     */
    private OAuthConsumerAppDTO createOAuthApp(String callbackUrl,String grantTypes) {

        OAuthConsumerAppDTO createdApp = null;
        OAuthAdminService oauthAdminService = new OAuthAdminService();
        OAuthConsumerAppDTO oauthConsumerAppDTO = new OAuthConsumerAppDTO();
        oauthConsumerAppDTO.setApplicationName(appName);
        oauthConsumerAppDTO.setCallbackUrl(callbackUrl);
        oauthConsumerAppDTO.setUsername(userName);
        oauthConsumerAppDTO.setOAuthVersion(OAuthConstants.OAuthVersions.VERSION_2);

        StringBuilder grantTypeString = new StringBuilder();
        grantTypeString.append(grantTypes).append(" ");

        if (!StringUtils.isBlank(callbackUrl)) {
            grantTypeString.append(ApplicationConstants.AUTHORIZATION_CODE).append(" ");
            grantTypeString.append(ApplicationConstants.IMPLICIT).append(" ");
        }

        oauthConsumerAppDTO.setGrantTypes(grantTypeString.toString().trim());

        try {
            oauthAdminService.registerOAuthApplicationData(oauthConsumerAppDTO);

            //retrieving the created OAuth application
            createdApp = oauthAdminService.getOAuthApplicationDataByAppName
                    (oauthConsumerAppDTO.getApplicationName());
        } catch (IdentityOAuthAdminException e) {
            errorMsg = "Error occured while creating the OAuth app";
            log.error(errorMsg);
        }
        log.debug("created OAuth App " + appName);
        return createdApp;
    }

    /**
     *
     * updating the existing client application if the callback url is different from client request
     */
    private void updateCallbackUrl(String callBackUrl,String grantTypes, String authUserName) {

        OAuthConsumerAppDTO oAuthConsumerAppdto = new OAuthConsumerAppDTO();

        oAuthConsumerAppdto.setOauthConsumerKey(retrievedApp.getClientId());
        oAuthConsumerAppdto.setOauthConsumerSecret(retrievedApp.getClientSecret());
        oAuthConsumerAppdto.setApplicationName(applicationName);
        oAuthConsumerAppdto.setCallbackUrl(callBackUrl);
        oAuthConsumerAppdto.setOAuthVersion(OAuthConstants.OAuthVersions.VERSION_2);
        oAuthConsumerAppdto.setGrantTypes(grantTypes);
        oAuthConsumerAppdto.setUsername(authUserName);

        OAuthAdminService oAuthAdminServices = new OAuthAdminService();

        //updating the existing application with current values.
        try {
            oAuthAdminServices.updateConsumerApplication(oAuthConsumerAppdto);
        } catch (IdentityOAuthAdminException e) {
            String errorMsg = " error occured whiile updating client application " +
                    applicationName + " Callback Url";
            log.error(errorMsg);
        }
    }

    /**
     *
     * updating the existing client application if the IsSaasApp variable is different from client request
     */
    private void updateSaasApp(boolean isSaasApp,ServiceProvider appServiceProvider,String loggedInUserTenantDomain) {

        appServiceProvider.setSaasApp(isSaasApp);
        ApplicationDAOImpl applicationDAOImpl = new ApplicationDAOImpl();
        try {
            //update application
            applicationDAOImpl.updateApplication(appServiceProvider, loggedInUserTenantDomain);
        } catch (IdentityApplicationManagementException e) {
            String errorMsg = "Error occur while updating client application " +
                    retrievedApp.getClientName() + " IsSaasApp";
            log.error(errorMsg);
        }
    }


    /**
     *
     *
     * @param clientId client id
     * @param clientName client name
     * @param callbackUrl callback url
     * @param clientSecret clientSecret
     * @param saasApp IsSaasApp
     * @param appOwner AppOwner
     * @param sampleMap Map
     * @return OAuthApplicationInfo object containing parsed values.
     */
    private OAuthApplicationInfo settingUpdatingValues(String clientId, String clientName, String callbackUrl,
                                                       String clientSecret, boolean saasApp, String appOwner,
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
            System.out.println(pair.getKey() + " = " + pair.getValue());
            updatingApp.addParameter((String) pair.getKey(), pair.getValue());
            it.remove();
        }
        return updatingApp;
    }
}
