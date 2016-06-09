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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.dcr.web.RegistrationService;
import org.wso2.carbon.apimgt.rest.api.dcr.web.dto.FaultResponse;
import org.wso2.carbon.apimgt.rest.api.dcr.web.dto.RegistrationProfile;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationServiceImpl implements RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationServiceImpl.class);


    OAuthApplicationInfo applicationInfo;
    OAuthApplicationInfo retrivedApp;
    ServiceProvider appServiceProvider;
    String loggedInUserTenantDomain, applicationName, authUserName;
    String grantTypes, consumerKey, owner;

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
        String errorMsg;
        ErrorDTO errorDTO;
        try {
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            AMDefaultKeyManagerImpl amdKeyManager = new AMDefaultKeyManagerImpl();
            OAuthAppRequest appRequest = new OAuthAppRequest();
            applicationInfo = new OAuthApplicationInfo();
            OAuthApplicationInfo returnedAPP = null;

            owner = profile.getOwner();
            authUserName = RestApiUtil.getLoggedInUsername();
            //validates if the application owner and logged in username is same.
            if (authUserName != null && authUserName.equals(owner)) {
                if (!isUserAccessAllowed(authUserName)) {
                    String msg = "You do not have enough privileges to create an OAuth app";
                    log.error("User " + authUserName + " does not have any of subscribe/create/publish privileges "
                            + "to create an OAuth app");
                    errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, msg);
                    response = Response.status(Response.Status.FORBIDDEN).entity(errorDTO).build();
                    return response;
                }

                applicationInfo.setClientName(profile.getClientName());
                applicationInfo.setCallBackURL(profile.getCallbackUrl());
                applicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, owner);
                applicationInfo.setClientId("");
                applicationInfo.setClientSecret("");
                applicationInfo.setIsSaasApplication(profile.isSaasApp());
                appRequest.setOAuthApplicationInfo(applicationInfo);


                loggedInUserTenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                applicationName = authUserName + "_" + profile.getClientName();
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
                    consumerKey = this.getConsumerKey();

                    //retirving the existing application
                    retrivedApp = amdKeyManager.retrieveApplication(consumerKey);

                    //check if the client new parameter values and existing applications' values are different
                    if (retrivedApp.getIsSaasApplication() != applicationInfo.getIsSaasApplication() ||
                            !retrivedApp.getCallBackURL().equals(applicationInfo.getCallBackURL())) {

                        //checking if the callback urls is different in clinet request
                        if (!retrivedApp.getCallBackURL().equals(applicationInfo.getCallBackURL())) {
                            this.updateCalbackUrl();
                        }
                        //checking if the IsSaasApplication is different in client request
                        if (retrivedApp.getIsSaasApplication() != applicationInfo.getIsSaasApplication()) {
                            this.updateSaasApp();
                        }
                        returnedAPP = this.settingUpdatingValues();

                    } else {
                        returnedAPP = retrivedApp;
                    }
                } else {
                    returnedAPP = keyManager.createApplication(appRequest);
                     return Response.status(Response.Status.CREATED).entity(returnedAPP).build();

                }

                if (returnedAPP == null) {
                    errorMsg = "OAuth app '" + profile.getClientName()
                            + "' creation or updating failed. Dynamic Client Registration Service not available.";
                    log.error(errorMsg);
                    errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 500l,
                            errorMsg);
                    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
                } else {
                    String infoMsg = "OAuth app " + profile.getClientName() + " creation or updating successful. ";
                    log.info(infoMsg);
                    response = Response.status(Response.Status.OK).entity(returnedAPP).build();
                }
            } else {
                errorMsg = "Logged in user '" + authUserName + "' and application owner '" + owner
                        + "' should be same.";
                errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, errorMsg);
                response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String msg = "Error occurred while registering client '" + profile.getClientName() + "'";
            errorDTO = RestApiUtil.getErrorDTO(RestApiConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400l, msg);
            response = Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            log.error(msg, e);
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

    private void updateCalbackUrl() {

        OAuthConsumerAppDTO oAuthConsumerAppdto = new OAuthConsumerAppDTO();

        oAuthConsumerAppdto.setOauthConsumerKey(consumerKey);
        oAuthConsumerAppdto.setOauthConsumerSecret(retrivedApp.getClientSecret());
        oAuthConsumerAppdto.setApplicationName(applicationName);
        oAuthConsumerAppdto.setCallbackUrl(applicationInfo.getCallBackURL());
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

    private void updateSaasApp() {

        appServiceProvider.setSaasApp(applicationInfo.getIsSaasApplication());

        ApplicationDAOImpl applicationDAOImpl = new ApplicationDAOImpl();
        try {

            applicationDAOImpl.updateApplication(appServiceProvider, loggedInUserTenantDomain);

        } catch (IdentityApplicationManagementException e) {

            String errorMsg = "Error occur while updating client application " +
                    retrivedApp.getClientName() + " IsSaasApp";
            log.error(errorMsg);
        }


    }

    private OAuthApplicationInfo settingUpdatingValues() {

        OAuthApplicationInfo updatingApp = new OAuthApplicationInfo();

        updatingApp.setClientId(retrivedApp.getClientId());
        updatingApp.setClientName(retrivedApp.getClientName());
        updatingApp.setCallBackURL(applicationInfo.getCallBackURL());
        updatingApp.setClientSecret(retrivedApp.getClientSecret());
        updatingApp.setIsSaasApplication(applicationInfo.getIsSaasApplication());
        updatingApp.addParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME, owner);

        return updatingApp;

    }

    private String getConsumerKey() {

        String key = null;

        if (appServiceProvider.getInboundAuthenticationConfig() != null &&
                appServiceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {

            InboundAuthenticationRequestConfig[] configs = appServiceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();

            //getting the consumer key of the existing application
            for (InboundAuthenticationRequestConfig config : configs) {
                if (IdentityApplicationConstants.OAuth2.NAME.equalsIgnoreCase(config.getInboundAuthType()) &&
                        config.getInboundAuthKey() != null) {

                    key = config.getInboundAuthKey();
                    break;
                }
            }
        }
        return key;
    }
}
