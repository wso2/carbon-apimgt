/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Map;

/**
 * This Interface is  providing functionality to  key manager specific core operations.
 * You can implement create/update/delete/retrieve  oAuth Application by implementing this interface.
 * Furthermore  this interface is providing  a method to read introspection endpoint + user api and return
 * combined map object.
 * Moreover this interface supports to retrieve key manager specific meta data such as what are the UI fields
 * needs to be implemented.
 */
public interface KeyManager {

    /**
     * Create new auth application in oAuth server.
     *
     * @param oauthAppRequest - this object contains values of oAuth app properties.
     * @return OAuthApplicationInfo object with oAuthApplication properties.
     */
    OAuthApplicationInfo createApplication(OauthAppRequest oauthAppRequest) throws APIManagementException;


    /**
     * Update an oAuth application
     *
     * @param appInfoDTO accept an appinfoDTO object
     * @return OAuthApplicationInfo this object will  contain all the properties of updated oAuth application
     */
    OAuthApplicationInfo updateApplication(OauthAppRequest appInfoDTO) throws APIManagementException;

    /**
     * Delete auth application
     *
     * @param consumerKey - will take consumer key as parameter
     */
    void deleteApplication(String consumerKey) throws APIManagementException;

    /**
     * Populate auth application.this will fetch data from oAuth server and will save properties to a java object
     *
     * @param consumerKey will take consumer key as parameter
     * @return json string
     */
    OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException;

    /**
     * Store calls this method to get a new Application Access Token. This will be called when getting the token for
     * the first time and when Store needs to refresh the existing token.
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from UI.
     * @return Details of the Generated Token. AccessToken and Validity period are a must.
     * @throws APIManagementException
     */
    AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException;

    /**
     * Call Introspection API + User API. This should retrieve two things including user claims.
     *
     * @return json String
     */
    //TODO:Change the Exception to APIKeyMgtException
    AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException;

    /**
     * Key manager implementation should be read from hardcoded json file
     *
     * @return {@code KeyManagerConfiguration}
     */
    KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException;

    /**
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws APIManagementException
     */
    OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException;

    /**
     * @param authApplicationInfo
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws APIManagementException
     *
     */
    OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo authApplicationInfo,  String jsonInput) throws
            APIManagementException;


    /**
     * This method will parse the JSON input and add those additional values to AccessTokenRequest. If its needed to
     * pass parameters in addition to those specified in AccessTokenRequest, those can be provided in the JSON input.
     * @param jsonInput Input as a JSON. This is the same JSON passed from Store UI.
     * @param tokenRequest Object encapsulating parameters sent from UI.
     * @return If input AccessTokenRequest is null, a new object will be returned,
     * else the additional parameters will be added to the input object passed.
     * @throws APIManagementException
     */
    AccessTokenRequest buildAccessTokenRequestFromJSON(String jsonInput, AccessTokenRequest tokenRequest)
            throws APIManagementException;

    /**
     * This method will be used if you want to create a oAuth application in semi-manual mode
     * where you must input minimum consumer key and consumer secret.
     *
     * @param appInfoRequest
     * @return OAuthApplicationInfo with oAuth application properties.
     * @throws APIManagementException
     */
    OAuthApplicationInfo createSemiManualAuthApplication(OauthAppRequest appInfoRequest) throws APIManagementException;

    /**
     * This method will create an AccessTokenRequest using OAuthApplicationInfo object. If tokenRequest is null,
     * this will create a new object, else will modify the provided AccessTokenRequest Object.
     * @param oAuthApplication
     * @param tokenRequest
     * @return AccessTokenRequest
     */
    AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplication,
                                                           AccessTokenRequest tokenRequest) throws APIManagementException;

    void loadConfiguration(String configuration) throws APIManagementException;
}
