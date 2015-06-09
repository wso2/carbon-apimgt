/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

/**
 * Utility class for performing Operations related to Applications, OAuth clients.
 */
public class ApplicationUtils {

    private static ApiMgtDAO dao = new ApiMgtDAO();

    private static Log log = LogFactory.getLog(ApplicationUtils.class);


    /**
     * This method will take application name and user id as parameters and will return application object.
     * @param appName APIM manager application name
     * @param userId logged in userID
     * @return APIM application object will return.
     */
    public static Application retrieveApplication(String appName, String userId, String groupingId) throws APIManagementException {

        return dao.getApplicationByName(appName, userId, groupingId);

    }

    /**
     * Get details of an Application referred by an Application Registration workflow.
     * @param workflowReference Reference ID for an Application Registration Workflow
     * @return {@code Application} Details of the Application.
     * @throws APIManagementException
     */
    public static Application populateApplication(String workflowReference)
            throws APIManagementException {
        int appId = dao.getApplicationIdForAppRegistration(workflowReference);
        Application application = dao.getApplicationById(appId);
        return application;
    }


    /**
     * This method will parse json String and set properties in  OAuthApplicationInfo object.
     * Further it will initiate new OauthAppRequest  object and set applicationInfo object as its own property.
     * @param clientName client Name.
     * @param callbackURL This is the call back URL of the application
     * @param tokenScope
     * @param clientDetails
     * @param clientId
     * @return appRequest object of OauthAppRequest.
     * @throws APIManagementException
     */
    public static OAuthAppRequest createOauthAppRequest(String clientName, String clientId, String callbackURL, String tokenScope, String
            clientDetails)
            throws
            APIManagementException {

        //initiate OauthAppRequest object.
        OAuthAppRequest appRequest = new OAuthAppRequest();
        OAuthApplicationInfo authApplicationInfo = new OAuthApplicationInfo();
        authApplicationInfo.setClientName(clientName);
        authApplicationInfo.setCallBackURL(callbackURL);
        authApplicationInfo.addParameter("tokenScope",tokenScope);
        authApplicationInfo.setClientId(clientId);

        if (clientDetails != null) {
            //parse json string and set applicationInfo parameters.
            authApplicationInfo = KeyManagerHolder.getKeyManagerInstance().buildFromJSON(authApplicationInfo, clientDetails);

            if (log.isDebugEnabled()) {
                log.debug("Additional json parameters when building OauthAppRequest =  " + clientDetails);
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("No additional json parameters when building OauthAppRequest");
            }
        }
        //set applicationInfo object
        appRequest.setOAuthApplicationInfo(authApplicationInfo);
        return appRequest;
    }


    /**
     * This method adds additional parameters specified in JSON input to TokenRequest.
     * @param jsonParams Additional Parameters required by the Authorization Server.
     * @param tokenRequest Values captured in TokenRequest.
     * @return Token Request after adding parameters in JSON input.
     * @throws APIManagementException
     */
    public static AccessTokenRequest populateTokenRequest(String jsonParams, AccessTokenRequest tokenRequest)
            throws APIManagementException {
        if (tokenRequest == null) {
            tokenRequest = new AccessTokenRequest();
        }

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        if (keyManager != null) {
            return keyManager.buildAccessTokenRequestFromJSON(jsonParams, tokenRequest);
        }
        return null;
    }

    public static AccessTokenRequest createAccessTokenRequest(OAuthApplicationInfo oAuthApplication,
                                                              AccessTokenRequest tokenRequest)
            throws APIManagementException {
        if (tokenRequest == null) {
            tokenRequest = new AccessTokenRequest();
        }

        KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
        if (keyManager != null) {
            return keyManager.buildAccessTokenRequestFromOAuthApp(oAuthApplication, tokenRequest);
        }
        return null;
    }

    /**
     * common method to throw exceptions
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
     */
    private static void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
    /**
     * common method to throw exceptions only with message.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @throws APIManagementException
     */
    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    public static void updateOAuthAppAssociation(Application application, String keyType,
                                                 OAuthApplicationInfo oAuthApplication) throws APIManagementException {
        application.addOAuthApp(keyType,oAuthApplication);
        dao.updateApplicationKeyTypeMapping(application,keyType);
    }
}
