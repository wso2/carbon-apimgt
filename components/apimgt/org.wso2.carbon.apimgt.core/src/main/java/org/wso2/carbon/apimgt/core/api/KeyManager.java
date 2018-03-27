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

import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;

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
     * Create a new OAuth application in the Authorization Server.
     *
     * @param oauthAppRequest - this object contains values of oAuth app properties.
     * @return OAuthApplicationInfo object with oAuthApplication properties.
     * @throws KeyManagementException Error while creating application.
     */
    OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws KeyManagementException;

    /**
     * Update an oAuth application
     *
     * @param oAuthApplicationInfo Up-to-date information of OAuth Application
     * @return OAuthApplicationInfo this object will  contain all the properties of updated oAuth application
     * @throws KeyManagementException Error while updating application.
     */
    OAuthApplicationInfo updateApplication(OAuthApplicationInfo oAuthApplicationInfo) throws KeyManagementException;

    /**
     * Delete auth application
     *
     * @param consumerKey - will take consumer key as parameter
     * @throws KeyManagementException Error while deleting application.
     */
    void deleteApplication(String consumerKey) throws KeyManagementException;

    /**
     * Populate auth application.this will fetch data from oAuth server and will save properties to a java object
     *
     * @param consumerKey will take consumer key as parameter
     * @return json string
     * @throws KeyManagementException Error while retrieving application.
     */
    OAuthApplicationInfo retrieveApplication(String consumerKey) throws KeyManagementException;

    /**
     * Generate a new OAuth2 Access Token by a given grant type. Supported grant types are
     * Password, Authorization Code, Client Credentials and Refresh
     *
     * @param tokenRequest parameters required to generate an access token.
     * @return AccessTokenInfo object with details of the token. AccessToken and Validity period are must to have.
     * @throws KeyManagementException if error occurred while generating token
     */
    AccessTokenInfo getNewAccessToken(AccessTokenRequest tokenRequest) throws KeyManagementException;

    /**
     * Get details about an access token. As a part of the response, consumer key against which token was obtained
     * must be returned.
     *
     * @param accessToken Access token which needs to be check.
     * @return {@code AccessTokenInfo}
     * @throws KeyManagementException Error while retrieving token meta data.
     */
    AccessTokenInfo getTokenMetaData(String accessToken) throws KeyManagementException;

    /**
     * Key manager implementation should be read from hardcoded json file
     *
     * @return {@code KeyManagerConfiguration}
     * @throws KeyManagementException error while getting key manager configuration.
     */
    KeyManagerConfiguration getKeyManagerConfiguration() throws KeyManagementException;

    /**
     * Revoke an active access tokens
     *
     * @param accessToken  Access token that is required to be revoked.
     * @param clientId     Consumer Key of the application
     * @param clientSecret Consumer Secret of the application
     * @throws KeyManagementException if error occurred while revoking the access token
     */
    void revokeAccessToken(String accessToken, String clientId, String clientSecret) throws KeyManagementException;

    /**
     * Load the key manager configuration
     *
     * @param configuration configuration object.
     * @throws KeyManagementException Error while loading configs.
     */
    void loadConfiguration(KeyManagerConfiguration configuration) throws KeyManagementException;

    /**
     * This Method will talk to APIResource registration end point  of  authorization server and creates a new resource
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return true if sucessfully registered. false if there is a error while registering a new resource.
     * @throws KeyManagementException Error while registering new resource.
     */
    boolean registerNewResource(API api, Map resourceAttributes) throws KeyManagementException;

    /**
     * This method will be used to retrieve registered resource by given API ID.
     *
     * @param apiId APIM api id.
     * @return It will return a Map with registered resource details.
     * @throws KeyManagementException Error while retrieving resource by ID.
     */
    Map getResourceByApiId(String apiId) throws KeyManagementException;

    /**
     * This method is responsible for update given APIResource  by its resourceId.
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return TRUE|FALSE. if it is successfully updated it will return TRUE or else FALSE.
     * @throws KeyManagementException Error while updating resource.
     */
    boolean updateRegisteredResource(API api, Map resourceAttributes) throws KeyManagementException;

    /**
     * This method will accept API id  as a parameter  and will delete the registered resource.
     *
     * @param apiID API id.
     * @throws KeyManagementException Error while deleting resource.
     */
    void deleteRegisteredResourceByAPIId(String apiID) throws KeyManagementException;

    /**
     * This method will be used to delete mapping records of oAuth applications.
     *
     * @param consumerKey Key of the application
     * @throws KeyManagementException Error while deleting mapped application.
     */
    void deleteMappedApplication(String consumerKey) throws KeyManagementException;

    /**
     * This method will be used to create scope on resource Registration Server
     *
     * @param scope Scope Object to be created
     * @return true if scope created
     * @throws KeyManagementException Error while creating scope
     */
    boolean registerScope(Scope scope) throws KeyManagementException;

    /**
     * This method will be used to retrieve the detail of the scope
     *
     * @param name name of scope
     * @return Scope object contains the binding and other information
     * @throws KeyManagementException Error while retrieving Scope information
     */
    Scope retrieveScope(String name) throws KeyManagementException;

    /**
     * This method will be used to update the scopee information
     *
     * @param scope scope to be update
     * @throws KeyManagementException Error while updating scope
     */
    boolean updateScope(Scope scope) throws KeyManagementException;

    /**
     * This method will used to delete the scope
     *
     * @param name name of the scope to be delete
     * @return true if scope is delete
     * @throws KeyManagementException Error while deleting scope
     */
    boolean deleteScope(String name) throws KeyManagementException;
}

