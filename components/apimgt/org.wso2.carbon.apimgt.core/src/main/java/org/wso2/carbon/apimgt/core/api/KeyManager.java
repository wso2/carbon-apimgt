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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wso2.carbon.apimgt.core.exception.KeyManagementException;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;

import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.util.Arrays;
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
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws KeyManagementException
     */
    OAuthApplicationInfo buildFromJSON(String jsonInput) throws KeyManagementException;

    /**
     * @param authApplicationInfo
     * @param jsonInput           this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will return after parsed jsonInput
     * @throws KeyManagementException
     */
    default OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo authApplicationInfo, String jsonInput) throws
            KeyManagementException {
        //initiate json parser.
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try {
            //parse json String
            jsonObject = (JSONObject) parser.parse(jsonInput);
            if (jsonObject != null) {
                //create a map to hold json parsed objects.
                Map<String, Object> params = (Map) jsonObject;

                //set client Id
                if (params.get(KeyManagerConstants.OAUTH_CLIENT_ID) != null) {
                    authApplicationInfo.setClientId((String) params.get(KeyManagerConstants.OAUTH_CLIENT_ID));
                }
                //copy all params map in to OAuthApplicationInfo's Map object.
                authApplicationInfo.putAll(params);
                return authApplicationInfo;
            }
        } catch (ParseException e) {
            throw new KeyManagementException("Error occurred while parsing JSON String", e);
        }
        return null;
    }


    /**
     * This method will parse the JSON input and add those additional values to AccessTokenRequest. If its needed to
     * pass parameters in addition to those specified in AccessTokenRequest, those can be provided in the JSON input.
     *
     * @param jsonInput    Input as a JSON. This is the same JSON passed from Store UI.
     * @param tokenRequest Object encapsulating parameters sent from UI.
     * @return If input AccessTokenRequest is null, a new object will be returned,
     * else the additional parameters will be added to the input object passed.
     * @throws KeyManagementException
     */
    default AccessTokenRequest buildAccessTokenRequestFromJSON(String jsonInput, AccessTokenRequest tokenRequest)
            throws KeyManagementException {
        if (jsonInput == null || jsonInput.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("JsonInput is null or Empty.");
            }
            return tokenRequest;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        if (tokenRequest == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Input request is null. Creating a new Request Object.");
            }
            tokenRequest = new AccessTokenRequest();
        }

        try {
            jsonObject = (JSONObject) parser.parse(jsonInput);
            // Getting parameters from input string and setting in TokenRequest.
            if (jsonObject != null && !jsonObject.isEmpty()) {
                Map<String, Object> params = (Map<String, Object>) jsonObject;

                if (null != params.get(KeyManagerConstants.OAUTH_CLIENT_ID)) {
                    tokenRequest.setClientId((String) params.get(KeyManagerConstants.OAUTH_CLIENT_ID));
                }

                if (null != params.get(KeyManagerConstants.OAUTH_CLIENT_SECRET)) {
                    tokenRequest.setClientSecret((String) params.get(KeyManagerConstants.OAUTH_CLIENT_SECRET));
                }

                if (null != params.get(KeyManagerConstants.VALIDITY_PERIOD)) {
                    tokenRequest.setValidityPeriod(Long.parseLong((String) params.get(
                            KeyManagerConstants.VALIDITY_PERIOD)));
                }

                return tokenRequest;
            }
        } catch (ParseException e) {
            throw new KeyManagementException("Error occurred while parsing JSON String", e);
        }
        return null;

    }

    /**
     * This method will be used if you want to create a oAuth application in semi-manual mode
     * where you must input minimum consumer key and consumer secret.
     *
     * @param appInfoRequest
     * @return OAuthApplicationInfo with oAuth application properties.
     * @throws KeyManagementException
     */
    OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest) throws KeyManagementException;

    /**
     * This method will create an AccessTokenRequest using OAuthApplicationInfo object. If tokenRequest is null,
     * this will create a new object, else will modify the provided AccessTokenRequest Object.
     *
     * @param oAuthApplication
     * @param tokenRequest
     * @return AccessTokenRequest
     */
    default AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplication,
                                                                   AccessTokenRequest tokenRequest)
            throws KeyManagementException {
        if (oAuthApplication == null) {
            return tokenRequest;
        }
        if (tokenRequest == null) {
            tokenRequest = new AccessTokenRequest();
        }

        if (oAuthApplication.getClientId() == null || oAuthApplication.getClientSecret() == null) {
            throw new KeyManagementException("Consumer key or Consumer Secret missing.");
        }
        tokenRequest.setClientId(oAuthApplication.getClientId());
        tokenRequest.setClientSecret(oAuthApplication.getClientSecret());


        if (oAuthApplication.getParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE) != null) {
            String[] tokenScopes = (String[]) oAuthApplication.getParameter(KeyManagerConstants.
                    OAUTH_CLIENT_TOKEN_SCOPE);
            tokenRequest.setScopes(tokenScopes);
            oAuthApplication.addParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE, Arrays.toString(tokenScopes));
        }

        if (oAuthApplication.getParameter(KeyManagerConstants.VALIDITY_PERIOD) != null) {
            tokenRequest.setValidityPeriod(Long.parseLong((String) oAuthApplication.getParameter(KeyManagerConstants
                    .VALIDITY_PERIOD)));
        }

        return tokenRequest;
    }

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

    /**
     * When provided the ConsumerKey, this method will provide all the Active tokens issued against that Key.
     *
     * @param consumerKey ConsumerKey of the OAuthClient
     * @return {@link java.util.Set} having active access tokens.
     * @throws KeyManagementException
     */
    Set<String> getActiveTokensByConsumerKey(String consumerKey) throws KeyManagementException;


    /**
     * Gives details of the Access Token to be displayed on Store.
     *
     * @param consumerKey
     * @return {@link org.wso2.carbon.apimgt.core.models.AccessTokenInfo} populating all the details of the AccessToken.
     * @throws KeyManagementException
     */
    AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws KeyManagementException;

}

