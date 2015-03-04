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
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.OauthAppRequest;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.applications.ApplicationImpl;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.Map;

/**
 * Utility class for performing Operations related to Applications, OAuth clients.
 */
public class ApplicationUtils {

    private static ApiMgtDAO dao = new ApiMgtDAO();

    private static Log log = LogFactory.getLog(ApplicationUtils.class);

    /**
     * This method will return object Application object by application name and Subscriber.
     * When initiating ApplicationImpl, it will call it's super class's(Application) constructor
     * @param appName APIM application name
     * @param userId Logged in user.
     * @return APIM application.
     */
    public static Application getNewApplication(String appName, String userId) {
        //initiate ApplicationImpl
        Application application = new ApplicationImpl(appName, new Subscriber(userId));
        return application;
    }

    /**
     * This method will return object Application  object by application Id.
     * When initiating ApplicationImpl, it will call it's super class's(Application) constructor
     * @param appId APIM application ID
     * @return APIM application.
     */
    public static Application getNewApplication(int appId) {
        //initiate ApplicationImpl
        Application application = new ApplicationImpl(appId);
        return application;
    }

    /**
     * This method will take application name and user id as parameters and will return application object.
     * @param appName APIM manager application name
     * @param userId logged in userID
     * @return APIM application object will return.
     */
    public static Application retrieveApplication(String appName, String userId) throws APIManagementException {
        Application application = getNewApplication(appName, userId);
        if(application == null){
            handleException("Application " + appName + " is not found.. ");
        }
        ((ApplicationImpl) application).populateApplication();
        return application;
    }

    /**
     *
     * @param workflowReference
     * @return
     * @throws APIManagementException
     */
    public static Application populateApplication(String workflowReference)
            throws APIManagementException {
        int appId = dao.getApplicationIdForAppRegistration(workflowReference);
        Application application = getNewApplication(appId);
        ((ApplicationImpl) application).populateApplication();
        return application;
    }


    public static OauthAppRequest createAppInfoDTO(Map<String, Object> params) {
//        OauthAppRequest appInfoDTO = new OIDCOauthAppRequest();
//        appInfoDTO.initialiseDTO(params);
        return null;
    }

    /**
     * This method will parse json String and set properties in  OAuthApplicationInfo object.
     * Further it will initiate new OauthAppRequest  object and set applicationInfo object as its own property.
     * @param clientName This consumer key of the application
     * @param callbackURL This is the call back URL of the application
     * @param clientDetails
     * @return appRequest object of OauthAppRequest.
     * @throws APIManagementException
     */
    public static OauthAppRequest createOauthAppRequest(String clientName, String callbackURL, String clientDetails)
            throws
            APIManagementException {

        //initiate OauthAppRequest object.
        OauthAppRequest appRequest = new OauthAppRequest();
        OAuthApplicationInfo authApplicationInfo = new OAuthApplicationInfo();
        authApplicationInfo.setClientName(clientName);
        authApplicationInfo.setCallBackURL(callbackURL);

        if (clientDetails != null) {
            //parse json string and set applicationInfo parameters.
            authApplicationInfo = KeyManagerFactory.getKeyManager().buildFromJSON(authApplicationInfo, clientDetails);

            if (log.isDebugEnabled()) {
                log.debug("Additional json parameters when building OauthAppRequest =  " + clientDetails);
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("No additional json parameters when building OauthAppRequest");
            }
        }
        //set applicationInfo object
        appRequest.setoAuthApplicationInfo(authApplicationInfo);
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

        KeyManager keyManager = KeyManagerFactory.getKeyManager();
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

        KeyManager keyManager = KeyManagerFactory.getKeyManager();
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
}
