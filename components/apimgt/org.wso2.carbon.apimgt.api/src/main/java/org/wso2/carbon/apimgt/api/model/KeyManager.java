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
import java.util.Set;

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
     */
    OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException;

    /**
     * Update an oAuth application
     *
     * @param appInfoDTO accept an appinfoDTO object
     * @return OAuthApplicationInfo this object will  contain all the properties of updated oAuth application
     */
    OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException;

    /**
     * Update an oAuth application owner
     *
     * @param appInfoDTO accept an appinfoDTO object
     * @return OAuthApplicationInfo this object will  contain all the properties of updated oAuth application
     */
    default OAuthApplicationInfo updateApplicationOwner(OAuthAppRequest appInfoDTO, String owner)
            throws APIManagementException {

        throw new APIManagementException("This is not supported");
    }

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
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from UI.
     * @return Details of the Generated Token. AccessToken and Validity period are a must.
     * @throws APIManagementException
     */
    AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException;

    /**
     * Store calls this method to get a new Application Consumer Secret.
     *
     * @param tokenRequest AccessTokenRequest which encapsulates parameters sent from UI.
     * @return New consumer secret.
     * @throws APIManagementException This is the custom exception class for API management.
     */
    String getNewApplicationConsumerSecret(AccessTokenRequest tokenRequest) throws APIManagementException;

    /**
     * Get details about an access token. As a part of the response, consumer key against which token was obtained
     * must be returned.
     *
     * @return {@code AccessTokenInfo}
     */
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
     * @param jsonInput           this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws APIManagementException
     */
    OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo authApplicationInfo, String jsonInput) throws
            APIManagementException;

    /**
     * This method will parse the JSON input and add those additional values to AccessTokenRequest. If its needed to
     * pass parameters in addition to those specified in AccessTokenRequest, those can be provided in the JSON input.
     *
     * @param jsonInput    Input as a JSON. This is the same JSON passed from Store UI.
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
    OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws APIManagementException;

    /**
     * This method will create an AccessTokenRequest using OAuthApplicationInfo object. If tokenRequest is null,
     * this will create a new object, else will modify the provided AccessTokenRequest Object.
     *
     * @param oAuthApplication
     * @param tokenRequest
     * @return AccessTokenRequest
     */
    AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplication,
                                                           AccessTokenRequest tokenRequest)
            throws APIManagementException;

    void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException;

    /**
     * This method will initialize a HTTP Client and a connection manager for each authorization server instance.
     *
     * @throws APIManagementException If an error occurs while initializing HTTP client
     */
    void initializeHttpClient() throws APIManagementException;

    /**
     * This Method will talk to APIResource registration end point  of  authorization server and creates a new resource
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return true if sucessfully registered. false if there is a error while registering a new resource.
     * @throws APIManagementException
     */

    boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException;

    /**
     * This method will be used to retrieve registered resource by given API ID.
     *
     * @param apiId APIM api id.
     * @return It will return a Map with registered resource details.
     * @throws APIManagementException
     */
    Map getResourceByApiId(String apiId) throws APIManagementException;

    /**
     * This method is responsible for update given APIResource  by its resourceId.
     *
     * @param api                this is a API object which contains all details about a API.
     * @param resourceAttributes this param will contains additional details if required.
     * @return TRUE|FALSE. if it is successfully updated it will return TRUE or else FALSE.
     * @throws APIManagementException
     */
    boolean updateRegisteredResource(API api, Map resourceAttributes) throws APIManagementException;

    /**
     * This method will accept API id  as a parameter  and will delete the registered resource.
     *
     * @param apiID API id.
     * @throws APIManagementException
     */
    void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException;

    /**
     * This method will be used to delete mapping records of oAuth applications.
     *
     * @param consumerKey
     * @throws APIManagementException
     */
    void deleteMappedApplication(String consumerKey) throws APIManagementException;

    /**
     * When provided the ConsumerKey, this method will provide all the Active tokens issued against that Key.
     *
     * @param consumerKey ConsumerKey of the OAuthClient
     * @return {@link java.util.Set} having active access tokens.
     * @throws APIManagementException
     */
    Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException;

    /**
     * Gives details of the Access Token to be displayed on Store.
     *
     * @param consumerKey
     * @return {@link org.wso2.carbon.apimgt.api.model.AccessTokenInfo} populating all the details of the Access Token.
     * @throws APIManagementException
     */
    AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException;

    /**
     * Get Scopes of the APIs by API Ids
     *
     * @param apiIdsString String of API Ids
     * @return {@link java.util.Map} having scopes of each API
     * @throws APIManagementException
     */
    Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString) throws APIManagementException;

    /**
     * This method will be used to register a Scope in the authorization server.
     *
     * @param scope        Scope to register
     * @param tenantDomain tenant domain to add scope
     * @throws APIManagementException if an error occurs while registering scope
     */
    void registerScope(Scope scope, String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to retrieve details of a Scope in the authorization server.
     *
     * @param scopeName    Scope Name to retrieve
     * @param tenantDomain tenant domain to retrieve scope from
     * @return Scope object
     * @throws APIManagementException if an error while retrieving scope
     */
    Scope getScopeByName(String scopeName, String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to retrieve all the scopes available in the authorization server for the given tenant
     * domain.
     *
     * @param tenantDomain tenant domain to retrieve scopes from
     * @return Mapping of Scope object to scope key
     * @throws APIManagementException if an error occurs while getting scopes list
     */
    Map<String, Scope> getAllScopes(String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to attach a Scope in the authorization server to an API resource.
     *
     * @param api          API
     * @param uriTemplate  URITemplate
     * @param scope        Scope to attach
     * @param tenantDomain tenant domain
     * @throws APIManagementException
     */
    void attachScopeToResource(API api, URITemplate uriTemplate, Scope scope, String tenantDomain)
            throws APIManagementException;

    /**
     * This method will be used to detach a Scope in the authorization server from an API resource.
     *
     * @param apiIdentifier
     * @param uriTemplate
     * @param scope
     * @param tenantDomain
     * @throws APIManagementException
     */
    void detachScopeToResource(APIIdentifier apiIdentifier, URITemplate uriTemplate, Scope scope, String tenantDomain)
            throws APIManagementException;

    /**
     * This method will be used to delete a Scope in the authorization server.
     *
     * @param scope        Scope object
     * @param tenantDomain tenant domain to delete the scope from
     * @throws APIManagementException if an error occurs while deleting the scope
     */
    void deleteScope(Scope scope, String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to update a Scope in the authorization server.
     *
     * @param scope        Scope object
     * @param tenantDomain tenant domain to update the scope
     * @throws APIManagementException if an error occurs while updating the scope
     */
    void updateScope(Scope scope, String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to check whether the a Scope exists for the given scope name in the authorization
     * server.
     *
     * @param scopeName    Scope Name
     * @param tenantDomain tenant Domain to check scope existence
     * @return whether scope exists or not
     * @throws APIManagementException if an error occurs while checking the existence of the scope
     */
    Boolean isScopeExists(String scopeName, String tenantDomain) throws APIManagementException;

    /**
     * This method will be used to register a service provider application in the authorization server for the given
     * tenant.
     *
     * @param tenantDomain tenant domain to register the application
     * @throws APIManagementException if an error occurs while registering application
     */
    OAuthApplicationInfo registerKeyManagerMgtApplication(String tenantDomain) throws APIManagementException;
}
