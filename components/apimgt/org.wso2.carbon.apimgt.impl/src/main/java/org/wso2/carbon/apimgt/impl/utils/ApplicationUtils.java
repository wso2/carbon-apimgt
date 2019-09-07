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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Utility class for performing Operations related to Applications, OAuth clients.
 */
public class ApplicationUtils {

    private static Log log = LogFactory.getLog(ApplicationUtils.class);


    /**
     * This method will take application name and user id as parameters and will return application object.
     * @param appName APIM manager application name
     * @param userId logged in userID
     * @return APIM application object will return.
     */
    public static Application retrieveApplication(String appName, String userId, String groupingId)
            throws APIManagementException {
        return ApiMgtDAO.getInstance().getApplicationByName(appName, userId, groupingId);
    }

    /**
     * This method will take application name and user id as parameters and will return application object.
     *
     * @param applicationId APIM manager application id
     * @return APIM application object will return.
     */
    public static Application retrieveApplicationById(int applicationId) throws APIManagementException {
        return ApiMgtDAO.getInstance().getApplicationById(applicationId);
    }

    /**
     * This method will parse json String and set properties in  OAuthApplicationInfo object.
     * Further it will initiate new OauthAppRequest  object and set applicationInfo object as its own property.
     * @param clientName client Name.
     * @param callbackURL This is the call back URL of the application
     * @param tokenScope The token scope
     * @param clientDetails The client details
     * @param clientId The ID of the client
     * @return appRequest object of OauthAppRequest.
     * @throws APIManagementException
     */
    public static OAuthAppRequest createOauthAppRequest(String clientName, String clientId, String callbackURL,
            String tokenScope, String clientDetails, String tokenType)
            throws
            APIManagementException {

        //initiate OauthAppRequest object.
        OAuthAppRequest appRequest = new OAuthAppRequest();
        OAuthApplicationInfo authApplicationInfo = new OAuthApplicationInfo();
        authApplicationInfo.setClientName(clientName);
        authApplicationInfo.setCallBackURL(callbackURL);
        authApplicationInfo.addParameter("tokenScope",tokenScope);
        authApplicationInfo.setClientId(clientId);
        authApplicationInfo.setTokenType(tokenType);

        if (clientDetails != null) {
            //parse json string and set applicationInfo parameters.
            authApplicationInfo = KeyManagerHolder.getKeyManagerInstance().buildFromJSON(authApplicationInfo,
                    clientDetails);

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


    public static void updateOAuthAppAssociation(Application application, String keyType,
                                                 OAuthApplicationInfo oAuthApplication) throws APIManagementException {
        application.addOAuthApp(keyType,oAuthApplication);
        ApiMgtDAO.getInstance().updateApplicationKeyTypeMapping(application,keyType);
    }

    /**
     * check whether current logged in user is the owner of the application
     *
     * @param application Application object
     * @param username    loged in user
     * @return true if current logged in consumer is the owner of the specified application
     */
    public static boolean isUserOwnerOfApplication(Application application, String username) {
        if (application.getSubscriber().getName().equals(username)) {
            return true;
        } else if (application.getSubscriber().getName().toLowerCase().equals(username.toLowerCase())) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String comparisonConfig = configuration
                    .getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
            return (StringUtils.isNotEmpty(comparisonConfig) && Boolean.valueOf(comparisonConfig));
        }
        return false;
    }
    /**
     * check whether current logged in user is the owner of the application
     *
     * @param applicationId Application id
     * @param username      loged in user
     * @return true if current logged in consumer is the owner of the specified application
     */
    public static boolean isUserOwnerOfApplication(int applicationId, String username) throws APIManagementException {
        APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
        Application application = apiConsumer.getApplicationById(applicationId);
        return isUserOwnerOfApplication(application, username);
    }
}
