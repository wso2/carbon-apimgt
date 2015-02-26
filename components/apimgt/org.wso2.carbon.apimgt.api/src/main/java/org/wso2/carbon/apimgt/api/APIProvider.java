/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * APIProvider responsible for providing helper functionality
 */
public interface APIProvider extends APIManager {

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException;

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get set of API
     */
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException;

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws APIManagementException;

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException;

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier);

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName);

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerId Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException If failed to get UserApplicationAPIUsage
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerId)
            throws APIManagementException;

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail);

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException;

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException;

    public void addTier(Tier tier) throws APIManagementException;
    
    public void updateTier(Tier tier) throws APIManagementException;
    
    public void removeTier(Tier tier) throws APIManagementException;

    public String getDefaultVersion(APIIdentifier apiid) throws APIManagementException;

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to add API
     */
    public void addAPI(API api) throws APIManagementException;

    /**
     * Updates an existing API. This method must not be used to change API status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException;

    /**
     * Change the lifecycle state of the specified API
     *
     * @param api The API whose status to be updated
     * @param status New status of the API
     * @param userId User performing the API state change
     * @param updateGatewayConfig Whether the changes should be pushed to the API gateway or not
     * @throws org.wso2.carbon.apimgt.api.APIManagementException on error
     */
    public void changeAPIStatus(API api, APIStatus status, String userId, boolean updateGatewayConfig)
            throws APIManagementException;

    /**
     * Locate any API keys issued for the previous versions of the given API, which are
     * currently in the PUBLISHED state and make those API keys compatible with this
     * version of the API
     *
     * @param api An API object with which the old API keys will be associated
     * @throws org.wso2.carbon.apimgt.api.APIManagementException on error
     */
    public void makeAPIKeysForwardCompatible(API api) throws APIManagementException;

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws org.wso2.carbon.apimgt.api.model.DuplicateAPIException  If the API trying to be created already exists
     * @throws org.wso2.carbon.apimgt.api.APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
            APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docType, String docName)
            throws APIManagementException;

    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException;

    /**
     * Checks if a given API exists in the registry
     * @param apiId
     * @return boolean result
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param api,        API
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(API api, String documentationName, String text)
            throws APIManagementException;
    
    /**
     * This method used to update the API definition content - Swagger
     *
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addAPIDefinitionContent(APIIdentifier identifier, String documentationName, String text) 
    					throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException;

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws APIManagementException;

    /**
     * Returns the details of all the life-cycle changes done per API.
     *
     * @param apiId     id of the APIIdentifier
     * @return List of life-cycle events per given API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to copy docs
     */
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId)
            throws APIManagementException;

    /**
     * Delete an API
     *
     * @param identifier APIIdentifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to remove the API
     */
    public void deleteAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Search API
     *
     * @param searchTerm  Search Term
     * @param searchType  Search Type
     * @return   Set of APIs
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException;
    /**
     * Update the subscription status
     *
     * @param apiId API Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id              *
     * @return int value with subscription id
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId) throws APIManagementException;
    
    /**
     * Update the Tier Permissions
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles          
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public void updateTierPermissions(String tierName, String permissionType, String roles)
            throws APIManagementException;
    
    /**
     * Get the list of Tier Permissions
     * 
     * @return Tier Permission Set
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public Set getTierPermissions() throws APIManagementException;
    
    /**
     * Get the list of Custom InSequences.
     * @return List of available sequences
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    
    public List<String> getCustomInSequences()  throws APIManagementException;
    
    
    /**
     * Get the list of Custom OutSequences.
     * @return List of available sequences
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    
    public List<String> getCustomOutSequences()  throws APIManagementException;

    /**
     * Get the list of Custom Fault Sequences.
     * @return List of available fault sequences
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */

    public List<String> getCustomFaultSequences()  throws APIManagementException;


    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet) throws APIManagementException;

    /**
     * Update the API to external APIStores and database
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet) throws APIManagementException;


    /**
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     * @param apiId The API Identifier which need to update in db
     * @throws APIManagementException
     *          If failed to update subscription status
     */

    public Set<APIStore> getExternalAPIStores(APIIdentifier apiId) throws APIManagementException;

    /**
     * When enabled publishing to external APIStores support,get only the published external apistore details which are
     * stored in db
     * @param apiId The API Identifier which need to update in db
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    public Set<APIStore> getPublishedExternalAPIStores(APIIdentifier apiId) throws APIManagementException;
    
    /**
     * Checks the Gateway Type
     * 
     * @return True if gateway is Synpase
     * @throws APIManagementException
     *         
     */
    public boolean isSynapseGateway() throws APIManagementException;
    
    /**
     * Search API by Document Content
     *
     * @param searchTerm  Search Term
     * @param searchType  Search Type
     * @return   Set of Documents and APIs
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public Map<Documentation, API> searchAPIsByDoc(String searchTerm, String searchType) throws APIManagementException;
    
    /**
     * This method updates Swagger 1.2 resources in the registry
     * @param fileName
     * @param jsonText
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public void updateSwagger12Definition(APIIdentifier apiId, String fileName, String jsonText) throws APIManagementException;
    
    
    /**
     * Returns the Swagger12 definition as a string
     * @param apiId
     * @return
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public String getSwagger12Definition(APIIdentifier apiId) throws APIManagementException;

}
