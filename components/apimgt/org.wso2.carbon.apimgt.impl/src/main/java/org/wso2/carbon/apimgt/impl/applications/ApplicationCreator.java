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

package org.wso2.carbon.apimgt.impl.applications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;


import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Map;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class ApplicationCreator {

    private static ApiMgtDAO dao = new ApiMgtDAO();
    private static Log log = LogFactory.getLog(ApplicationCreator.class);

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
     * This method will take hardcoded class name from APImanager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     * @return keyManager instance.
     */
    public static KeyManager getKeyManager() {

        KeyManager keyManager = null;
        try {
            keyManager = (KeyManager) Class.forName(ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_KEY_MANGER_IMPLEMENTATION_CLASS_NAME)).newInstance();
            log.info("Created instance successfully");
        } catch (InstantiationException e) {
            log.error("Error while instantiating class" + e.toString());
        } catch (IllegalAccessException e) {
            log.error("Error while accessing class" + e.toString());
        } catch (ClassNotFoundException e) {
            log.error("Error while creating keyManager instance" + e.toString());
        }
        return keyManager;
    }

    public static ResourceManager getResourceManager() {
        ResourceManager resourceManager = null;
        try {
            resourceManager = (ResourceManager) Class.forName(ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration().
                    getFirstProperty(APIConstants.API_RESOURCE_MANGER_IMPLEMENTATION_CLASS_NAME)).newInstance();
            log.info("Created instance successfully");
        } catch (InstantiationException e) {
            log.error("Error while instantiating class" + e.toString());
        } catch (IllegalAccessException e) {
            log.error("Error while accessing class" + e.toString());
        } catch (ClassNotFoundException e) {
            log.error("Error while creating keyManager instance" + e.toString());
        }
        return resourceManager;
    }

    /**
     * common method to throw exceptions
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
     * This method will take application name and user id as parameters and will return application object.
     * @param appName APIM manager application name
     * @param userId logged in userID
     * @return APIM application object will return.
     */
    public static Application retrieveApplication(String appName, String userId) {
        Application application = getNewApplication(appName, userId);
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
     * @param clientDetails
     * @return appRequest object of OauthAppRequest.
     * @throws APIManagementException
     */
    public static OauthAppRequest createOauthAppRequest(String clientDetails) throws APIManagementException {
        //parse json string and set applicationInfo parameters.
        OAuthApplicationInfo applicationInfo = getKeyManager().buildFromJSON(clientDetails);
        //initiate OauthAppRequest object.
        OauthAppRequest appRequest = new OauthAppRequest();
        //set applicationInfo object
        appRequest.setoAuthApplicationInfo(applicationInfo);
        //return appRequest.
        return appRequest;
    }
}
