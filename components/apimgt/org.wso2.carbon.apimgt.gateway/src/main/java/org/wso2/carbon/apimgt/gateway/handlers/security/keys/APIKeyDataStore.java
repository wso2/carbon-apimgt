/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the interface used by the APIKeyValidator to interact with the API
 * key manager. Different implementations of this interface may employ different
 * techniques/protocols to communicate with the actual key management server.
 */
public interface APIKeyDataStore {

    /**
     * Validate the given API key for the specified API context and version.
     *
     * @param context Context of an API
     * @param apiVersion A valid version of the API
     * @param apiKey An API key string - Not necessarily a valid key
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    //public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
    //                                             String apiKey, String clientDomain) throws APISecurityException;

    /**
     * Validate the given API key for the specified API context and version.
     *
     * @param context Context of an API
     * @param apiVersion A valid version of the API
     * @param apiKey An API key string - Not necessarily a valid key
     * @param requiredAuthenticationLevel - type of  key. can be one of 'Application or Application_User'
     * @param tenantDomain teantDomain of API
     * @param  keyManagers list of key managers assigned to API
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
                                          String requiredAuthenticationLevel, String clientDomain,
                                          String matchingResource, String httpVerb, String tenantDomain,
                                          List<String> keyManagers) throws APISecurityException;

    /**
     * Get API Resource URI Templates
     *
     * @param context Context of an API
     * @param apiVersion A valid version of the API
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion) throws APISecurityException;

    /**
     * Get API Product Resource URI Templates
     *
     * @param context Context of an API Product
     * @param apiVersion A valid version of the API Product
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    ArrayList<URITemplate> getAPIProductURITemplates(String context, String apiVersion) throws APISecurityException;


    /**
     * Validate API subscriptions.
     *
     * @param context Context of an API
     * @param version A valid version of the API
     * @param consumerKey consumer key
     * @param keyManager
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey,
                                                 String tenantDomain, String keyManager)
            throws APISecurityException;

    /**
     * Validate API subscriptions.
     *
     * @param context Context of an API
     * @param version A valid version of the API
     * @param appId ApplicationId
     * @param keyManager
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException on error
     */
    APIKeyValidationInfoDTO validateSubscription(String context, String version, int appId,
                                                 String tenantDomain)
            throws APISecurityException;

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes of the token.
     *
     * @param tokenValidationContext Token validation context
     * @param tenantDomain           Tenant domain
     * @return <code>true</code> if scope validation is successful and
     * <code>false</code> if scope validation failed
     * @throws APISecurityException in case of scope validation failure
     */
    boolean validateScopes(TokenValidationContext tokenValidationContext, String tenantDomain)
            throws APISecurityException;

    /**
     * Clean up any resources allocated to this API key data store instance.
     */
    void cleanup();

    Map<String, Scope> retrieveScopes(String tenantDomain);
}
