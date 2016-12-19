/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.apimgt.core.exception.KeyManagementException;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;


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

    static final Logger LOG = LoggerFactory.getLogger(KeyManager.class);

    /**
     * Create a new OAuth application in the Authorization Server.
     *
     * @param oauthAppRequest - this object contains values of oAuth app properties.
     * @return OAuthApplicationInfo object with oAuthApplication properties.
     */
    OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException;


    /**
     * Update an oAuth application
     *
     * @param appInfoDTO accept an appinfoDTO object
     * @return OAuthApplicationInfo this object will  contain all the properties of updated oAuth application
     */
    OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws KeyManagementException;

    /**
     * Delete auth application
     *
     * @param consumerKey - will take consumer key as parameter
     */
    void deleteApplication(String consumerKey) throws KeyManagementException;

    /**
     * Populate auth application.this will fetch data from oAuth server and will save properties to a java object
     *
     * @param consumerKey will take consumer key as parameter
     * @return json string
     */
    OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException;

    /**
     * Store calls this method to get a new Application Access Token. This will be called when getting the token for
     * the first time and when Store needs to refresh the existing token.
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from UI.
     * @return Details of the Generated Token. AccessToken and Validity period are a must.
     * @throws KeyManagementException
     */
    AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException;

    /**
     * Get details about an access token. As a part of the response, consumer key against which token was obtained
     * must be returned.
     *
     * @return {@code AccessTokenInfo}
     */
    AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException;

    /**
     * Key manager implementation should be read from hardcoded json file
     *
     * @return {@code KeyManagerConfiguration}
     */
    KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException;

    /** 
     * This method will be used if you want to create a oAuth application in semi-manual mode
     * where you must input minimum consumer key and consumer secret.
     *
     * @param appInfoRequest
     * @return OAuthApplicationInfo with oAuth application properties.
     * @throws KeyManagementException
     */
    OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws KeyManagementException;

    

    void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException;

    /**
     * This Method will talk to APIResource registration end point  of  authorization server and creates a new resource
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return true if sucessfully registered. false if there is a error while registering a new resource.
     * @throws KeyManagementException
     */

    boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException;

    /**
     * This method will be used to retrieve registered resource by given API ID.
     *
     * @param apiId APIM api id.
     * @return It will return a Map with registered resource details.
     * @throws KeyManagementException
     */
    Map getResourceByApiId(String apiId) throws KeyManagementException;

    /**
     * This method is responsible for update given APIResource  by its resourceId.
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return TRUE|FALSE. if it is successfully updated it will return TRUE or else FALSE.
     * @throws KeyManagementException
     */
    boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagementException;

    /**
     * This method will accept API id  as a parameter  and will delete the registered resource.
     *
     * @param apiID API id.
     * @throws KeyManagementException
     */
    void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagementException;

    /**
     * This method will be used to delete mapping records of oAuth applications.
     *
     * @param consumerKey
     * @throws KeyManagementException
     */
    void deleteMappedApplication(String consumerKey) throws KeyManagementException;

   

}

