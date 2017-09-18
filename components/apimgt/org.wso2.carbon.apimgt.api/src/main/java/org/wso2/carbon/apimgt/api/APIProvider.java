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
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.io.InputStream;
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
     * @throws APIManagementException if failed to get Providers
     */
    Set<Provider> getAllProviders() throws APIManagementException;

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    List<API> getAPIsByProvider(String providerId) throws APIManagementException;

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException;

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    Provider getProvider(String providerName) throws APIManagementException;

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    Usage getUsageByAPI(APIIdentifier apiIdentifier);

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    Usage getAPIUsageByUsers(String providerId, String apiName);

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerId Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws APIManagementException If failed to get UserApplicationAPIUsage
     */
    UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerId) throws APIManagementException;

    /**
     * Returns usage details of a particular published by a provider
     *
     * @param apiId API identifier
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    List<SubscribedAPI> getAPIUsageByAPIId(APIIdentifier apiId) throws APIManagementException;

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail);

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException;

    void addTier(Tier tier) throws APIManagementException;

    void addPolicy(Policy policy) throws APIManagementException;


    /**
     * Get api throttling policy by name
     * @param username name of the user
     * @param policyName name of the policy
     * @throws APIManagementException
     */
    APIPolicy getAPIPolicy(String username, String policyName) throws APIManagementException;

    /**
     * Get api throttling policy by uuid
     * @param uuid UUID of the policy
     * @throws APIManagementException
     */
    APIPolicy getAPIPolicyByUUID(String uuid) throws APIManagementException;


    /**
     * Get application throttling policy by name
     * @param username name of the user
     * @param policyName name of the policy
     * @throws APIManagementException
     */
    ApplicationPolicy getApplicationPolicy(String username, String policyName) throws APIManagementException;

    /**
     * Get application throttling policy by uuid
     * @param uuid UUID of the policy
     * @throws APIManagementException
     */
    ApplicationPolicy getApplicationPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Get subscription throttling policy by name
     * @param username name of the user
     * @param policyName name of the policy
     * @throws APIManagementException
     */
    SubscriptionPolicy getSubscriptionPolicy(String username, String policyName) throws APIManagementException;

    /**
     * Get subscription throttling policy by uuid
     * @param uuid UUID of the policy
     * @throws APIManagementException
     */
    SubscriptionPolicy getSubscriptionPolicyByUUID(String uuid) throws APIManagementException;

    /**
     * Get global throttling policy by name
     * @param policyName name of the policy
     * @throws APIManagementException
     */
    GlobalPolicy getGlobalPolicy(String policyName) throws APIManagementException;

    /**
     * Get global throttling policy by uuid
     * @param uuid UUID of the policy
     * @throws APIManagementException
     */
    GlobalPolicy getGlobalPolicyByUUID(String uuid) throws APIManagementException;


    /**
     * Returns true if key template given by the global policy already exists.
     * But this check will exclude the policy represented by the policy name 
     *
     * @param policy Global policy
     * @return true if Global policy key template already exists
     */
    boolean isGlobalPolicyKeyTemplateExists (GlobalPolicy policy) throws APIManagementException;

    /**
     * Updates throttle policy in global CEP, gateway and database.
     * <p>
     * Database transactions and deployements are not rolledback on failiure.
     * A flag will be inserted into the database whether the operation was
     * successfull or not.
     * </p>
     *
     * @param policy updated {@link Policy} object
     * @throws APIManagementException
     */
    void updatePolicy(Policy policy) throws APIManagementException;

    void updateTier(Tier tier) throws APIManagementException;
    
    void removeTier(Tier tier) throws APIManagementException;

    String getDefaultVersion(APIIdentifier apiid) throws APIManagementException;

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws APIManagementException if failed to add API
     */
    void addAPI(API api) throws APIManagementException;
    
    public boolean isAPIUpdateValid(API api) throws APIManagementException;

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update API
     * @throws org.wso2.carbon.apimgt.api.FaultGatewaysException on Gateway Failure
     */
    void updateAPI(API api) throws APIManagementException, FaultGatewaysException;

    /**
     * Updates manage of an existing API. This method must not be used to change API status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @return failed environments during gateway operation
     * @throws APIManagementException failed environments during gateway operation
     */
    void manageAPI(API api) throws APIManagementException, FaultGatewaysException;

    /**
     * Change the lifecycle state of the specified API
     *
     * @param api The API whose status to be updated
     * @param status New status of the API
     * @param userId User performing the API state change
     * @param updateGatewayConfig Whether the changes should be pushed to the API gateway or not
     * @throws org.wso2.carbon.apimgt.api.APIManagementException on error
     * @throws org.wso2.carbon.apimgt.api.FaultGatewaysException on Gateway Failure
     * */
    void changeAPIStatus(API api, APIStatus status, String userId, boolean updateGatewayConfig)
            throws APIManagementException, FaultGatewaysException;


    boolean updateAPIStatus(APIIdentifier apiId, String status,boolean publishToGateway,boolean deprecateOldVersions,
                                boolean makeKeysForwardCompatible)
            throws APIManagementException, FaultGatewaysException;

    /**
     * Locate any API keys issued for the previous versions of the given API, which are
     * currently in the PUBLISHED state and make those API keys compatible with this
     * version of the API
     *
     * @param api An API object with which the old API keys will be associated
     * @throws APIManagementException on error
     */
    void makeAPIKeysForwardCompatible(API api) throws APIManagementException;

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws DuplicateAPIException  If the API trying to be created already exists
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException, APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    void removeDocumentation(APIIdentifier apiId, String docType, String docName) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docId UUID of the doc
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docId)throws APIManagementException;
    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    void addDocumentation(APIIdentifier apiId, Documentation documentation) throws APIManagementException;

    /**
     * Add a file to a document of source type FILE 
     *
     * @param apiId API identifier the document belongs to
     * @param documentation document
     * @param filename name of the file
     * @param content content of the file as an Input Stream
     * @param contentType content type of the file
     * @throws APIManagementException if failed to add the file
     */
    void addFileToDocumentation(APIIdentifier apiId, Documentation documentation, String filename, InputStream content,
            String contentType) throws APIManagementException;

    /**
     * Checks if a given API exists in the registry
     * @param apiId
     * @return boolean result
     * @throws APIManagementException
     */
    boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param api,        API
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    void addDocumentationContent(API api, String documentationName, String text) throws APIManagementException;
   
    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    void updateDocumentation(APIIdentifier apiId, Documentation documentation) throws APIManagementException;

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws APIManagementException if failed to copy docs
     */
    void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException;

    /**
     * Returns the details of all the life-cycle changes done per API.
     *
     * @param apiId     id of the APIIdentifier
     * @return List of life-cycle events per given API
     * @throws APIManagementException if failed to copy docs
     */
    List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException;

    /**
     * Delete an API
     *
     * @param identifier APIIdentifier
     * @throws APIManagementException if failed to remove the API
     */
    void deleteAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Search API
     *
     * @param searchTerm  Search Term
     * @param searchType  Search Type
     * @return   Set of APIs
     * @throws APIManagementException
     */
    List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException;
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
    void updateSubscription(APIIdentifier apiId, String subStatus, int appId) throws APIManagementException;


    /**
     * This method is used to update the subscription
     *
     * @param subscribedAPI subscribedAPI object that represents the new subscription detals
     * @throws APIManagementException if failed to update subscription
     */
    void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException;
    
    /**
     * Update the Tier Permissions
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles          
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    void updateTierPermissions(String tierName, String permissionType, String roles) throws APIManagementException;
    
    /**
     * Get the list of Tier Permissions
     * 
     * @return Tier Permission Set
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    Set getTierPermissions() throws APIManagementException;
    
    /**
     * Get the list of Custom InSequences.
     * @return List of available sequences
     * @throws APIManagementException
     */


    /**
     * Update Throttle Tier Permissions
     *
     * @param tierName Tier Name
     * @param permissionType Permission Type
     * @param roles Roles
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    void updateThrottleTierPermissions(String tierName, String permissionType, String roles) throws
            APIManagementException;

    /**
     * Get the list of Throttle Tier Permissions
     *
     * @return Tier Permission Set
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    Set getThrottleTierPermissions() throws APIManagementException;

    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    List<String> getCustomInSequences()  throws APIManagementException;


    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    List<String> getCustomOutSequences()  throws APIManagementException;

    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    List<String> getCustomInSequences(APIIdentifier apiIdentifier)  throws APIManagementException;
    
    
    /**
     * Get the list of Custom InSequences including API defined in sequences.
     * @return List of available sequences
     * @throws APIManagementException
     */
    List<String> getCustomOutSequences(APIIdentifier apiIdentifier)  throws APIManagementException;

    /**
     * Get the list of Custom Fault Sequences.
     * @return List of available fault sequences
     * @throws APIManagementException
     */

    List<String> getCustomFaultSequences()  throws APIManagementException;
    
    /**
     * Get the list of Custom Fault Sequences including per API sequences.
     * @return List of available fault sequences
     * @throws APIManagementException
     */

    List<String> getCustomFaultSequences(APIIdentifier apiIdentifier)  throws APIManagementException;


    /**
     * Get the list of Custom in sequences inclusing api identifier.
     * @return List of in sequences
     * @throws APIManagementException
     */

    List<String> getCustomApiInSequences(APIIdentifier apiIdentifier)  throws APIManagementException;

    /**
     * Get the list of Custom out Sequences including given api
     * @return List of available out sequences
     * @throws APIManagementException
     */

    List<String> getCustomApiOutSequences(APIIdentifier apiIdentifier)  throws APIManagementException;

    /**
     * Get the list of Custom Fault Sequences including per API sequences.
     * @return List of available fault sequences
     * @throws APIManagementException
     */

    List<String> getCustomApiFaultSequences(APIIdentifier apiIdentifier)  throws APIManagementException;


    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException;

    /**
     * Update the API to external APIStores and database
     * @param api The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @param apiOlderVersionExist The api contained older versions
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException;


    /**
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     * @param apiId The API Identifier which need to update in db
     * @throws APIManagementException
     *          If failed to update subscription status
     */

    Set<APIStore> getExternalAPIStores(APIIdentifier apiId) throws APIManagementException;

    /**
     * When enabled publishing to external APIStores support,get only the published external apistore details which are
     * stored in db
     * @param apiId The API Identifier which need to update in db
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    Set<APIStore> getPublishedExternalAPIStores(APIIdentifier apiId) throws APIManagementException;
    
    /**
     * Checks the Gateway Type
     * 
     * @return True if gateway is Synpase
     * @throws APIManagementException
     *         
     */
    boolean isSynapseGateway() throws APIManagementException;
    
    /**
     * Search APIs by swagger document content. This method searches the given search term in the registry and returns
     * a set of APIs which satisfies the given search term
     *
     * @param searchTerm  Search Term
     * @param searchType  Search Type
     * @return   Set of Documents and APIs
     * @throws APIManagementException
     */
    Map<Documentation, API> searchAPIsByDoc(String searchTerm, String searchType) throws APIManagementException;

    /**
     * Returns all the Consumer keys of applications which are subscribed to given API
     *
     * @param apiIdentifier APIIdentifier
     * @return a String array of ConsumerKeys
     * @throws APIManagementException
     */
    String[] getConsumerKeys(APIIdentifier apiIdentifier) throws APIManagementException;


    /**
     * This method updates Swagger 2.0 resources in the registry
     *
     * @param apiId    id of the APIIdentifier
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException
     */
    void saveSwagger20Definition(APIIdentifier apiId, String jsonText) throws APIManagementException;

    /**
     * This method is used to initiate the web service calls and cluster messages related to stats publishing status
     *
     * @param receiverUrl   event receiver url
     * @param user          username of the event receiver
     * @param password      password of the event receiver
     * @param updatedStatus status of the stat publishing state
     */
    void callStatUpdateService(String receiverUrl, String user, String password, boolean updatedStatus);
    
    /**
     * This method is to change registry lifecycle states for an API artifact
     *
     * @param  apiIdentifier apiIdentifier
     * @param  action  Action which need to execute from registry lifecycle
     * @return APIStateChangeResponse API workflow state and WorkflowResponse
     * */
    APIStateChangeResponse changeLifeCycleStatus(APIIdentifier apiIdentifier, String action)
             throws APIManagementException, FaultGatewaysException;

    /**
    * This method is to set checklist item values for a particular life-cycle state of an API
    *
    * @param  apiIdentifier apiIdentifier
    * @param  checkItem  Order of the checklist item
    * @param  checkItemValue Value of the checklist item
    *
    * */
    boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException;

    /** 
     * This method is to set a lifecycle check list item given the APIIdentifier and the checklist item name.
     * If the given item not in the allowed lifecycle check items list or item is already checked, this will stay 
     * silent and return false. Otherwise, the checklist item will be updated and returns true.
     * 
     * @param apiIdentifier APIIdentifier
     * @param checkItemName Name of the checklist item
     * @param checkItemValue Value to be set to the checklist item
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    boolean checkAndChangeAPILCCheckListItem(APIIdentifier apiIdentifier, String checkItemName, boolean checkItemValue)
            throws APIManagementException;

     /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId APIIdentifier
     * @return Map<String,Object> a map with lifecycle data
     */
     Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException;
     
     /**
      * Push api related state changes to the gateway. Api related configurations will be deployed or destroyed
      * according to the new state.
      * @param identifier Api identifier
      * @param newStatus new state of the lifecycle
      * @return collection of failed gateways. Map contains gateway name as the key and the error as the value
      * @throws APIManagementException
      */
     Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, APIStatus newStatus) 
             throws APIManagementException;
     
     /**
      * Update api related information such as database entries, registry updates for state change.
      * @param identifier
      * @param newStatus
      * @param failedGatewaysMap Map of failed gateways. Gateway name is the key and error message is value. Null is
      * accepted if changes are not pushed to a gateway
      * @return boolean value representing success not not
      * @throws APIManagementException
      * @throws FaultGatewaysException
      */
     boolean updateAPIforStateChange(APIIdentifier identifier, APIStatus newStatus, 
             Map<String, String> failedGatewaysMap) throws APIManagementException, FaultGatewaysException;
     
     /**
      * Get the current lifecycle status of the api
      * @param apiIdentifier Api identifier
      * @return Current lifecycle status
      * @throws APIManagementException
      */
     String getAPILifeCycleStatus(APIIdentifier apiIdentifier) throws APIManagementException;

    /**
     * Get the paginated APIs from publisher
     *
     * @param tenantDomain tenant domain
     * @param start        starting number
     * @param end          ending number
     * @return set of API
     * @throws APIManagementException if failed to get Apis
     */
    Map<String, Object> getAllPaginatedAPIs(String tenantDomain, int start, int end) throws APIManagementException;
    
    
    /**
     * Get a policy names for given policy level and user name
     * @param username
     * @param level
     * @return
     * @throws APIManagementException
     */
    String[] getPolicyNames(String username, String level) throws APIManagementException;

    /**
     * Delete throttling policy
     * @param username
     * @param policyLevel
     * @param policyName
     * @throws APIManagementException
     */
    void deletePolicy(String username, String policyLevel, String policyName) throws APIManagementException;
    
    boolean hasAttachments(String username, String policyName, String policyLevel)throws APIManagementException;

    /**
     *
     * @return List of block Conditions
     * @throws APIManagementException
     */
    List<BlockConditionsDTO> getBlockConditions() throws APIManagementException;

    /**
     *
     * @return Retrieve a block Condition
     * @throws APIManagementException
     */
    BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException;

    /**
     * Retrieves a block condition by its UUID
     * 
     * @param uuid uuid of the block condition
     * @return Retrieve a block Condition
     * @throws APIManagementException
     */
    BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException;

    /**
     * Updates a block condition given its id
     * 
     * @param conditionId id of the condition
     * @param state state of condition
     * @return state change success or not
     * @throws APIManagementException
     */
    boolean updateBlockCondition(int conditionId,String state) throws APIManagementException;

    /**
     * Updates a block condition given its UUID
     * 
     * @param uuid uuid of the block condition
     * @param state state of condition
     * @return state change success or not
     * @throws APIManagementException
     */
    boolean updateBlockConditionByUUID(String uuid,String state) throws APIManagementException;

    /**
     *  Add a block condition
     * 
     * @param conditionType type of the condition (IP, Context .. )
     * @param conditionValue value of the condition
     * @return UUID of the new Block Condition
     * @throws APIManagementException
     */
    String addBlockCondition(String conditionType, String conditionValue) throws APIManagementException;

    /**
     * Deletes a block condition given its Id
     * 
     * @param conditionId Id of the condition
     * @return true if successfully deleted
     * @throws APIManagementException
     */
    boolean deleteBlockCondition(int conditionId) throws APIManagementException;

    /**
     * Deletes a block condition given its UUID
     * 
     * @param uuid uuid of the block condition
     * @return true if successfully deleted
     * @throws APIManagementException
     */
    boolean deleteBlockConditionByUUID(String uuid) throws APIManagementException;

    /**
     *  Get the lifecycle configuration for a tenant
     * @param tenantDomain
     * @return lifecycle
     * @throws APIManagementException
     */
    String getLifecycleConfiguration(String tenantDomain) throws APIManagementException;

    /**
     * Get the external workflow reference id for a subscription
     *
     * @param subscriptionId subscription id
     * @return external workflow reference id if exists, else null
     * @throws APIManagementException
     */
    String getExternalWorkflowReferenceId (int subscriptionId) throws APIManagementException;
    
}
