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
     * Call Introspection API + User API. This should retrieve two things including user claims.
     *
     * @return json String
     */
    Map getTokenMetaData(String accessToken) throws APIManagementException;

    /**
     * Key manager implementation should be read from hardcoded json file
     *
     * @return json String
     */
    String getKeyManagerMetaData() throws APIManagementException;

    /**
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws APIManagementException
     */
    OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException;

    /**
     * This method will be used if you want to create a oAuth application in semi-manual mode
     * where you must input minimum consumer key and consumer secret.
     *
     * @param appInfoRequest
     * @return OAuthApplicationInfo with oAuth application properties.
     * @throws APIManagementException
     */
    OAuthApplicationInfo createSemiManualAuthApplication(OauthAppRequest appInfoRequest) throws APIManagementException;

//    /**
//     * This Method will talk to APIResource registration end point  of  authorization server then will return the
//     * response as Map.
//     *
//     * @param externalResource ExternalResource object, This APIResource would be an API and it comes with APIResource attributes
//     *                  such as scopes/url_sets/auth_methods etc.
//     * @return this will return a Map with returned values of APIResource registration.
//     * @throws APIManagementException
//     */
//
//    boolean registerNewResource(ExternalResource externalResource) throws APIManagementException;
//
//    /**
//     * This method will be used to retrieve registered resource by given API ID.
//     *
//     * @param apiId APIM api id.
//     * @return It will return a Map with registered resource details.
//     * @throws APIManagementException
//     */
//    Map getResourceByApiId(String apiId) throws APIManagementException;
//
//    /**
//     * This method is responsible for update given APIResource  by its resourceId.
//     *
//     * @param externalResource this  will hold ExternalResource data that needs to be updated.
//     * @return TRUE|FALSE. if it is successfully updated it will return TRUE or else FALSE.
//     * @throws APIManagementException
//     */
//    boolean updateRegisteredResource(ExternalResource externalResource) throws APIManagementException;
//
//    /**
//     * This method will accept API id  as a parameter  and will delete the registered resource.
//     *
//     * @param apiID API id.
//     * @throws APIManagementException
//     */
//    void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException;


}
