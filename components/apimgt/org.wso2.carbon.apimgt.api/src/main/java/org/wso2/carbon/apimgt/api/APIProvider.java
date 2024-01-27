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

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateMetadataDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * APIProvider responsible for providing helper functionality
 */
public interface APIProvider extends APIManager {

    /**
     * This method is to add a comment.
     *
     * @param uuid Api uuid
     * @param comment    comment object
     * @param user       Username of the comment author
     * @throws APIManagementException if failed to add comment for API
     */
    String addComment(String uuid, Comment comment, String user) throws APIManagementException;

    /**
     * This method is to get a comment of an API.
     *
     * @param apiTypeWrapper Api Type Wrapper
     * @param commentId      Comment ID
     * @param replyLimit
     * @param replyOffset
     * @return Comment
     * @throws APIManagementException if failed to get comments for identifier
     */
    Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit, Integer replyOffset) throws
            APIManagementException;

    /**
     * @param apiTypeWrapper  Api type wrapper
     * @param parentCommentID
     * @param replyLimit
     * @param replyOffset
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID, Integer replyLimit, Integer replyOffset) throws APIManagementException;

    /**
     * @param apiTypeWrapper Api Type Wrapper
     * @param commentId      comment ID
     * @param comment        Comment object
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws APIManagementException;

    /**
     * This method is to delete a comment.
     *
     * @param apiTypeWrapper API Type Wrapper
     * @param commentId      Comment ID
     * @return boolean
     * @throws APIManagementException if failed to delete comment for identifier
     */
    boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException;


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException;

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
     * @param uuid API uuid
     * @param organization
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    List<SubscribedAPI> getAPIUsageByAPIId(String uuid, String organization) throws APIManagementException;

    /**
     * Returns usage details of a particular api product published by a provider
     *
     * @param apiProductId API Product identifier
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    List<SubscribedAPI> getAPIProductUsageByAPIProductId(APIProductIdentifier apiProductId) throws APIManagementException;

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail);

    /**
     * Returns full list of subscriptions of an API
     *
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param provider Name of API creator
     * @return Set<UserApplicationAPIUsage>
     * @throws APIManagementException if failed to get Subscribers
     */
    List<SubscribedAPI> getSubscriptionsOfAPI(String apiName, String apiVersion, String provider)
            throws APIManagementException;

    /**
     * Returns the subscriber name for the given subscription id.
     *
     * @param subscriptionId The subscription id of the subscriber to be returned
     * @return The subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    String getSubscriber(String subscriptionId) throws APIManagementException;

    /**
     * Returns the claims of subscriber for the given subscriber.
     *
     * @param subscriber The name of the subscriber to be returned
     * @return The looked up claims of the subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    Map getSubscriberClaims(String subscriber) throws APIManagementException;

    void addPolicy(Policy policy) throws APIManagementException;

    /**
     * Deletes a subscription block condition when the condition key is given
     *
     * @param conditionValue condition key ex: /api/1.0:admin-testApplication:SANDBOX
     * @throws APIManagementException
     */
    void deleteSubscriptionBlockCondition(String conditionValue) throws APIManagementException;

    /**
     * Get the context of API identified by the given APIIdentifier
     *
     * @param uuid api uuid
     * @return apiContext
     * @throws APIManagementException if failed to fetch the context for apiID
     */
    String getAPIContext(String uuid) throws APIManagementException;


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

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @return added api
     * @throws APIManagementException if failed to add API
     */
    API addAPI(API api) throws APIManagementException;

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @param existingAPI existing api
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update API
     * @throws org.wso2.carbon.apimgt.api.FaultGatewaysException on Gateway Failure
     * @return updated API
     */
    API updateAPI(API api, API existingAPI) throws APIManagementException, FaultGatewaysException;

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiId          The id of the API to be copied
     * @param newVersion     The version of the new API
     * @param defaultVersion whether this version is default or not
     * @param organization          Identifier of an organization
     * @return api created api
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    API createNewAPIVersion(String apiId, String newVersion, Boolean defaultVersion, String organization)
            throws APIManagementException;

    /**
     * Create a new version of the <code>apiProduct</code>, with version <code>newVersion</code>
     *
     * @param apiProductId The id of the API Product to be copied
     * @param newVersion The version of the new API Product
     * @param defaultVersion whether this version is default or not
     * @param organization Identifier of an organization
     * @return apiProduct created apiProduct
     * @throws APIManagementException If an error occurs while trying to create
     *      *                                the new version of the API Product
     */
    APIProduct createNewAPIProductVersion(String apiProductId, String newVersion, Boolean defaultVersion,
            String organization) throws APIManagementException;
    /**
     * Retrieve the Key of the Service used in the API
     * @param apiId Unique Identifier of the API
     * @param tenantId Logged-in tenant domain
     * @return Unique key of the service
     * @throws APIManagementException
     */
    String retrieveServiceKeyByApiId(int apiId, int tenantId) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId   api uuid
     * @param documentId ID of the documentation
     * @param organization  Identifier of an organization
     * @throws APIManagementException if failed to remove documentation
     */
    void removeDocumentation(String apiId, String documentId, String organization) throws APIManagementException;

    /**
     * Adds Documentation to an API/Product
     *
     * @param uuid                API/Product Identifier
     * @param documentation       Documentation
     * @param organization        Identifier of an organization
     * @return Documentation      created documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    Documentation addDocumentation(String uuid, Documentation documentation, String organization) throws APIManagementException;


    /**
     * Adds Document content to an API/Product
     *
     * @param uuid          API/Product Identifier
     * @param content       Documentation content
     * @param docId         doc uuid
     * @param organization  Identifier of an organization
     * @throws APIManagementException if failed to add documentation
     */
    void addDocumentationContent(String uuid, String docId, String organization, DocumentationContent content)
            throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         id of the document
     * @param documentation Documentation
     * @param organization  Identifier of an organization
     * @return updated documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    Documentation updateDocumentation(String apiId, Documentation documentation, String organization) throws APIManagementException;

    /**
     * Returns the details of all the life-cycle changes done per API or API Product
     *
     * @param uuid     Unique UUID of the API or API Product
     * @return List of life-cycle events per given API or API Product
     * @throws APIManagementException if failed to copy docs
     */
    List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException;

    /**
     * Update the subscription status
     *
     * @param apiId API Identifier
     * @param subStatus Subscription Status
     * @param appId Application Id
     * @param organization organization
     * @return int value with subscription id
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    void updateSubscription(APIIdentifier apiId, String subStatus, int appId, String organization)
            throws APIManagementException;


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
     * Delete the Tier Permissions
     * @param tierName  Tier Name
     * @throws APIManagementException
     */
    void deleteTierPermissions(String tierName) throws APIManagementException;

    /**
     * Get the list of Tier Permissions
     *
     * @return Tier Permission Set
     * @throws APIManagementException
     *          If failed to update subscription status
     */
    Set getTierPermissions() throws APIManagementException;

    /**
     * Get the given Subscription Throttle Policy Permission
     *
     * @return Subscription Throttle Policy
     * @throws APIManagementException If failed to retrieve Subscription Throttle Policy Permission
     */
    Object getThrottleTierPermission(String tierName) throws APIManagementException;

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
     * Publish API to external stores given by external store Ids
     *
     * @param api              API which need to published
     * @param externalStoreIds APIStore Ids which need to publish API
     * @throws APIManagementException If failed to publish to external stores
     */
    boolean publishToExternalAPIStores(API api, List<String> externalStoreIds) throws APIManagementException;

    /**
     * When enabled publishing to external APIStores support,publish the API to external APIStores
     *
     * @param api         The API which need to published
     * @param apiStoreSet The APIStores set to which need to publish API
     * @throws APIManagementException If failed to publish to external stores
     */
    void publishToExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException;

    /**
     * Update the API to external APIStores and database
     *
     * @param api                  The API which need to published
     * @param apiStoreSet          The APIStores set to whsich need to publish API
     * @param apiOlderVersionExist The api contained older versions
     * @throws APIManagementException If failed to update the APIs  in externals stores
     */
    boolean updateAPIsInExternalAPIStores(API api, Set<APIStore> apiStoreSet, boolean apiOlderVersionExist)
            throws APIManagementException;

    /**
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     *
     * @param apiId The API uuid which need to update in db
     * @throws APIManagementException If failed to get all external stores for the API
     */
    Set<APIStore> getExternalAPIStores(String apiId) throws APIManagementException;

    /**
     * When enabled publishing to external APIStores support,get only the published external apistore details which are
     * stored in db
     *
     * @param apiId The API uuid which need to update in db
     * @throws APIManagementException If failed to get all the published external stores for the API
     */
    Set<APIStore> getPublishedExternalAPIStores(String apiId) throws APIManagementException;

    /**
     * Checks the Gateway Type
     *
     * @return True if gateway is Synpase
     * @throws APIManagementException
     *
     */
    boolean isSynapseGateway() throws APIManagementException;

    /**
     * This method updates the swagger definition in registry
     *
     * @param api           API
     * @param jsonText      openAPI definition
     * @param organization  Identifier of an organization
     * @throws APIManagementException
     */
    void saveSwaggerDefinition(API api, String jsonText, String organization) throws APIManagementException;

    /**
     * This method updates the swagger definition in registry
     *
     * @param apiId   uuid of the api
     * @param jsonText    openAPI definition
     * @param orgId  Identifier of an organization
     * @throws APIManagementException
     */
    void saveSwaggerDefinition(String apiId, String jsonText, String orgId) throws APIManagementException;

    /**
     * This method adds the swagger definition of an API Product in registry
     *
     * @param apiToProductResourceMapping   List of API Product resource mappings
     * @param apiProduct   API Product
     * @throws APIManagementException
     */
    void addAPIProductSwagger(String apiProductId, Map<API, List<APIProductResource>> apiToProductResourceMapping,
            APIProduct apiProduct, String orgId) throws APIManagementException;

    /**
     * This method updates the swagger definition of an API Product in registry
     *
     * @param apiToProductResourceMapping   List of API Product resource mappings
     * @param apiProduct   API Product
     * @throws APIManagementException
     */
    void updateAPIProductSwagger(String apiProductId, Map<API, List<APIProductResource>> apiToProductResourceMapping,
            APIProduct apiProduct, String orgId) throws APIManagementException, FaultGatewaysException;

    /**
     * This method validates the existence of all the resource level throttling tiers in URI templates of API
     *
     * @param api           api
     * @param tenantDomain  tenant domain
     * @throws APIManagementException
     */
    void validateResourceThrottlingTiers(API api, String tenantDomain) throws APIManagementException;

    /**
     * This method validates the existence of all the resource level throttling tiers in URI templates of API
     * when the swagger file is provided
     *
     * @param swaggerContent swagger file
     * @param tenantDomain   tenant domain
     * @throws APIManagementException
     */
    void validateResourceThrottlingTiers(String swaggerContent, String tenantDomain) throws APIManagementException;

    /**
     * This method validates the existence of the API level throttling tier of API
     *
     * @param api           api
     * @param tenantDomain  tenant domain
     * @throws APIManagementException
     */
    void validateAPIThrottlingTier(API api, String tenantDomain) throws APIManagementException;

    /**
     * This method validates the existence of the API level throttling tier of API
     *
     * @param apiProduct   api product
     * @param tenantDomain tenant domain
     * @throws APIManagementException
     */
    void validateProductThrottlingTier(APIProduct apiProduct, String tenantDomain) throws APIManagementException;

    /**
     * This method is used to configure monetization for a given API
     *
     * @param api API to be updated with monetization
     * @throws APIManagementException if it failed to update the monetization status and data
     */
    void configureMonetizationInAPIArtifact(API api) throws APIManagementException;

    /**
     * This method is used to get the implementation class for monetization
     *
     * @return implementation class for monetization
     * @throws APIManagementException if failed to get implementation class for monetization
     */
    Monetization getMonetizationImplClass() throws APIManagementException;

    /**
     * This method is to change registry lifecycle states for an API artifact
     *
     * @param orgId UUID of the organization
     * @param  apiTypeWrapper API Type Wrapper
     * @param  action  Action which need to execute from registry lifecycle
     * @param  checklist checklist items
     * @return APIStateChangeResponse API workflow state and WorkflowResponse
     * */
    APIStateChangeResponse changeLifeCycleStatus(String orgId, ApiTypeWrapper apiTypeWrapper, String action,
                                                 Map<String, Boolean> checklist) throws APIManagementException;

    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId id of the api
     * @param orgId  Identifier of an organization
     * @return Map<String,Object> a map with lifecycle data
     */
    Map<String, Object> getAPILifeCycleData(String apiId, String orgId) throws APIManagementException;


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

    boolean hasAttachments(String username, String policyName, String policyLevel, String organization) throws APIManagementException;

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
     *  Add a block condition with condition status
     *
     * @param conditionType type of the condition (IP, Context .. )
     * @param conditionValue value of the condition
     * @param conditionStatus status of the condition
     * @return UUID of the new Block Condition
     * @throws APIManagementException
     */
    String addBlockCondition(String conditionType, String conditionValue, boolean conditionStatus)
            throws APIManagementException;

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
     * Get the external workflow reference id for a subscription
     *
     * @param subscriptionId subscription id
     * @return external workflow reference id if exists, else null
     * @throws APIManagementException
     */
    String getExternalWorkflowReferenceId (int subscriptionId) throws APIManagementException;

    /**
     * Method to add a Certificate to publisher and gateway nodes.
     *
     * @param userName : The user name of the logged in user.
     * @param certificate : Base64 encoded certificate string.
     * @param alias : Alias for the certificate.
     * @param endpoint : Endpoint which the certificate should be mapped to.
     * @return Integer which represents the operation status.
     * @throws APIManagementException
     */
    int addCertificate(String userName, String certificate, String alias, String endpoint) throws APIManagementException;

    /**
     * Method to add client certificate to gateway nodes to support mutual SSL based authentication.
     *
     * @param userName      : User name of the logged in user.
     * @param apiTypeWrapper : API Type Wrapper.
     * @param certificate   : Relevant public certificate.
     * @param alias         : Alias of the certificate.
     * @param organization  : Organization
     * @return SUCCESS : If operation succeeded,
     * INTERNAL_SERVER_ERROR : If any internal error occurred,
     * ALIAS_EXISTS_IN_TRUST_STORE : If alias is already present in the trust store,
     * CERTIFICATE_EXPIRED : If the certificate is expired.
     * @throws APIManagementException API Management Exception.
     */
    int addClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String certificate, String alias,
                             String tierName, String organization) throws APIManagementException;

    /**
     * Method to remove the certificate which mapped to the given alias, endpoint from publisher and gateway nodes.
     * @param userName : UserName of the logged in user.
     * @param alias    : Alias of the certificate which needs to be deleted.
     * @param endpoint : Endpoint which the certificate is mapped to.
     * @return Integer which represents the operation status.
     * @throws APIManagementException
     */
    int deleteCertificate(String userName, String alias, String endpoint) throws APIManagementException;

    /**
     * Method to remove the client certificates which is mapped to given alias and api identifier from database.
     *
     * @param userName      : Name of the logged in user.
     * @param alias         : Alias of the certificate which needs to be deleted.
     * @return 1: If delete succeeded,
     * 2: If delete failed, due to an un-expected error.
     * 4 : If certificate is not found in the trust store.
     * @throws APIManagementException API Management Exception.
     */
    int deleteClientCertificate(String userName, ApiTypeWrapper apiTypeWrapper, String alias)
            throws APIManagementException;

    /**
     * Method to get the server is configured to Dynamic SSL Profile feature.
     * @return : TRUE if all the configurations are met, FALSE otherwise.
     */
    boolean isConfigured();

    /**
     * Method to retrieve certificate metadata uploaded for the tenant represent by the user.
     * @param alias : The alias of the certificate.
     * @return : CertificateMetadata
     * @throws APIManagementException
     */
    CertificateMetadataDTO getCertificate(String alias) throws APIManagementException;

    /**
     * Method to retrieve all the certificates uploaded for the tenant represent by the user.
     * @param userName : User name of the logged in user.
     * @return : List of CertificateMetadata
     * @throws APIManagementException
     */
    List<CertificateMetadataDTO> getCertificates(String userName) throws APIManagementException;

    /**
     * Method to search the certificate metadata database for the provided alias and endpoints.
     *
     * @param tenantId : The id of the tenant which the certificates are belongs to.
     * @param alias : The alias of the certificate.
     * @param endpoint : Endpoint which the certificate is applied to
     * @return : Results as a CertificateMetadataDTO list.
     * @throws APIManagementException :
     */
    List<CertificateMetadataDTO> searchCertificates(int tenantId, String alias, String endpoint) throws
            APIManagementException;

    /**
     * Method to search the client certificates for the provided tenant id, alias and api identifier.
     *
     * @param tenantId      : ID of the tenant.
     * @param alias         : Alias of the certificate.
     * @param apiIdentifier : Identifier of the API.
     * @param organization  : Organization
     * @return list of client certificates that match search criteria.
     * @throws APIManagementException API Management Exception.
     */
    List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias, APIIdentifier apiIdentifier,
            String organization) throws APIManagementException;

    /**
     * Method to search the client certificates for the provided tenant id, alias and api product identifier.
     *
     * @param tenantId      : ID of the tenant.
     * @param alias         : Alias of the certificate.
     * @param apiProductIdentifier : Identifier of the API Product.
     * @param organization  : Organization
     * @return list of client certificates that match search criteria.
     * @throws APIManagementException API Management Exception.
     */
    List<ClientCertificateDTO> searchClientCertificates(int tenantId, String alias,
            APIProductIdentifier apiProductIdentifier, String organization) throws APIManagementException;

    /**
     * Retrieve the total number of certificates which a specified tenant has.
     *
     * @param tenantId : The id of the tenant
     * @return : The certificate count.
     */
    int getCertificateCountPerTenant(int tenantId) throws APIManagementException;

    /**
     * Retrieve the total number client certificates which the specified tenant has.
     *
     * @param tenantId : ID of the tenant.
     * @return count of client certificates that exists for a particular tenant.
     * @throws APIManagementException API Management Exception.
     */
    int getClientCertificateCount(int tenantId) throws APIManagementException;

    /**
     * Method to check whether an certificate for the given alias is present in the trust store and the database.
     *
     * @param alias : The alias of the certificate.
     * @return : True if a certificate is present, false otherwise.
     * @throws APIManagementException :
     */
    boolean isCertificatePresent(int tenantId, String alias) throws APIManagementException;

    /**
     * Method to check whether a client certificate for the given alias is present in trust store and whether it can
     * be modified by current user.
     *
     * @param alias    : Relevant alias.
     * @param apiTypeWrapper : The identifier of the api.
     * @param organization : Organization
     * @return Instance of {@link ClientCertificateDTO} if the client certificate is present and
     * modifiable by current user.
     * @throws APIManagementException API Management Exception.
     */
    ClientCertificateDTO getClientCertificate(String alias, ApiTypeWrapper apiTypeWrapper,
            String organization) throws APIManagementException;


    /**
     * Method to get the status of the certificate which matches the given alias.
     * This method can me modified to get other necessary information as well. Such as CN etc.
     *
     * @param tenantDomain tenant domain
     * @param alias : The alias of the certificate.
     * @return : The status and the expiry date as a parameter map.
     * @throws APIManagementException :
     */
    CertificateInformationDTO getCertificateStatus(String tenantDomain, String alias) throws APIManagementException;

    /**
     * Method to update an existing certificate.
     *
     * @param certificateString : The base64 encoded string of the uploaded certificate.
     * @param alias : Alias of the certificate that should be updated.
     * @return : Integer value which represent the operation status.
     * @throws APIManagementException :
     */
    int updateCertificate(String certificateString, String alias) throws APIManagementException;

    /**
     * Method to update the existing client certificate.
     *
     * @param certificate   : Relevant certificate that need to be updated.
     * @param alias         : Alias of the certificate.
     * @param apiTypeWrapper : API Identifier of the certificate.
     * @param tier          : tier name.
     * @param tenantId      : Id of tenant.
     * @param organization  : organization
     * @return : 1 : If client certificate update is successful,
     * 2 : If update failed due to internal error,
     * 4 : If provided certificate is empty,
     * 6 : If provided certificate is expired
     * @throws APIManagementException API Management Exception.
     */
    int updateClientCertificate(String certificate, String alias, ApiTypeWrapper apiTypeWrapper, String tier,
                                int tenantId, String organization) throws APIManagementException;

    /**
     * Retrieve the certificate which matches the given alias.
     *
     * @param tenantDomain tenant domain
     * @param alias : The alias of the certificate.
     * @return : The certificate input stream.
     * @throws APIManagementException :
     */
    ByteArrayInputStream getCertificateContent(String tenantDomain, String alias) throws APIManagementException;

    /**
     * Create API product
     * @param product product object containing details of the product
     * @return Map of APIs as keys and respective APIProductResources as values
     * @throws APIManagementException exception
     */
    Map<API, List<APIProductResource>> addAPIProductWithoutPublishingToGateway(APIProduct product) throws APIManagementException;

    /**
     * Publish API Product to Gateway
     * @param product product object containing details of the product
     * @throws FaultGatewaysException
     */
    void saveToGateway(APIProduct product) throws FaultGatewaysException, APIManagementException;

    /**
     * Delete an API Product
     *
     * @param identifier APIProductIdentifier
     * @param apiProductUUID
     * @param organization
     * @throws APIManagementException if failed to remove the API Product
     */
    void deleteAPIProduct(APIProductIdentifier identifier, String apiProductUUID, String organization)
            throws APIManagementException;

    /**
     * Update API Product
     * @param product
     * @return Map of APIs as keys and respective APIProductResources as values
     * @throws APIManagementException
     */
    Map<API, List<APIProductResource>> updateAPIProduct(APIProduct product) throws APIManagementException, FaultGatewaysException;

    List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException;

    /**
     * Remove pending lifecycle state change task for the given api or api product.
     *
     * @param  identifier Identifier object of api or api product
     * @throws APIManagementException if API Manager core level exception occurred
     */
    void deleteWorkflowTask(Identifier identifier) throws APIManagementException;

    /**
     * This method returns the security audit properties
     *
     * @param userId user id
     * @return JSONObject object with security audit properties
     * @throws APIManagementException
     */
    JSONObject getSecurityAuditAttributesFromConfig(String userId) throws APIManagementException;

    /**
     * Finds resources that have been removed in the updated API URITemplates,
     * that are currently reused by API Products.
     *
     * @param updatedUriTemplates Updated URITemplates
     * @param existingAPI         Existing API
     * @return List of removed resources that are reused among API Products
     */
    List<APIResource> getRemovedProductResources(Set<URITemplate> updatedUriTemplates, API existingAPI);

    /**
     * Check whether the given scope name exists as a shared scope in the tenant domain.
     *
     * @param scopeName    Shared Scope name
     * @param tenantId Tenant Id
     * @return Scope availability
     * @throws APIManagementException if failed to check the availability
     */
    boolean isSharedScopeNameExists(String scopeName, int tenantId) throws APIManagementException;

    /**
     * Add a shared scope.
     *
     * @param scope        Shared Scope
     * @param tenantDomain Tenant domain
     * @return UUID of the added Shared Scope
     * @throws APIManagementException if failed to add a scope
     */
    String addSharedScope(Scope scope, String tenantDomain) throws APIManagementException;

    /**
     * Get all available shared scopes.
     *
     * @param tenantDomain tenant domain
     * @return Shared Scope list
     * @throws APIManagementException if failed to get the scope list
     */
    List<Scope> getAllSharedScopes(String tenantDomain) throws APIManagementException;

    /**
     * Get all available shared scope keys.
     *
     * @param tenantDomain tenant domain
     * @return Shared Scope Keyset
     * @throws APIManagementException if failed to get the scope key set
     */
    Set<String> getAllSharedScopeKeys(String tenantDomain) throws APIManagementException;

    /**
     * Get shared scope by UUID.
     *
     * @param sharedScopeId Shared scope Id
     * @param tenantDomain  tenant domain
     * @return Shared Scope
     * @throws APIManagementException If failed to get the scope
     */
    Scope getSharedScopeByUUID(String sharedScopeId, String tenantDomain) throws APIManagementException;

    /**
     * Delete shared scope.
     *
     * @param scopeName     Shared scope name
     * @param tenantDomain  tenant domain
     * @throws APIManagementException If failed to delete the scope
     */
    void deleteSharedScope(String scopeName, String tenantDomain) throws APIManagementException;

    /**
     * Update a shared scope.
     *
     * @param sharedScope  Shared Scope
     * @param tenantDomain tenant domain
     * @throws APIManagementException If failed to update
     */
    void updateSharedScope(Scope sharedScope, String tenantDomain) throws APIManagementException;

    /**
     * Validate a shared scopes set. Add the additional attributes (scope description, bindings etc).
     *
     * @param scopes       Shared scopes set
     * @param tenantDomain Tenant domain
     * @throws APIManagementException If failed to validate
     */
    void validateSharedScopes(Set<Scope> scopes, String tenantDomain) throws APIManagementException;

    /**
     * Get the API and URI usages of the given shared scope
     *
     * @param uuid       UUID of the shared scope
     * @param tenantId ID of the Tenant domain
     * @throws APIManagementException If failed to validate
     */
    SharedScopeUsage getSharedScopeUsage(String uuid, int tenantId) throws APIManagementException;

    /**
     * Retrieve list of resources of the provided api that are used in other API products
     * @param uuid UUID of the API
     * @return APIResource list of resources
     * @throws APIManagementException
     */
    List<APIResource> getUsedProductResources(String uuid) throws APIManagementException ;

    /**
     * Delete API
     * @param apiUuid API uuid to delete
     * @param organization organization id of the deleting API
     * @throws APIManagementException
     */
    void deleteAPI(String apiUuid, String organization) throws APIManagementException;
    /**
     * Checks whether the given document already exists for the given api/product
     *
     * @param uuid       API/Product id
     * @param docName    Name of the document
     * @param organization  Identifier of the organization
     * @return true if document already exists for the given api/product
     * @throws APIManagementException if failed to check existence of the documentation
     */
    boolean isDocumentationExist(String uuid, String docName, String organization) throws APIManagementException;

    /**
     * Add WSDL to the api. wsdl can be provided either as a url or a resource file
     * @param apiId         ID of the API
     * @param resource      Resource
     * @param organization  Identifier of an organization
     * @param url           wsdl url
     * @throws APIManagementException
     */
    void addWSDLResource(String apiId, ResourceFile resource, String url, String organization) throws APIManagementException;

    /**
     * Add or update thumbnail image of an api
     * @param apiId    ID of the API
     * @param resource Image resource
     * @param orgId    Identifier of an organization
     * @throws APIManagementException
     */
    void setThumbnailToAPI(String apiId, ResourceFile resource, String orgId) throws APIManagementException;

    /**
     * Add or update graphql definition
     * @param apiId       ID of the API
     * @param definition  API Definition
     * @param orgId       Identifier of an organization
     * @throws APIManagementException
     */
    void saveGraphqlSchemaDefinition(String apiId, String definition, String orgId) throws APIManagementException;

    /**
     * Get API product by uuid
     * @param uuid Id of the api product
     * @param requestedTenantDomain tenant domain requested
     * @return APIProduct product
     * @throws APIManagementException
     */
    APIProduct getAPIProductbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException;
    /**
     * Delete API Product
     * @param apiProduct
     */
    void deleteAPIProduct(APIProduct apiProduct) throws APIManagementException;

    /**
     * Adds a new APIRevision to an existing API
     *
     * @param apiRevision    APIRevision
     * @param organization   Identifier of an organization
     * @throws APIManagementException if failed to add APIRevision
     */
    String addAPIRevision(APIRevision apiRevision, String organization) throws APIManagementException;

    /**
     * Get a Revision Object related to provided revision UUID
     *
     * @param revisionUUID API Revision UUID
     * @return API Revision
     * @throws APIManagementException if failed to get the related API revision
     */
    APIRevision getAPIRevision(String revisionUUID) throws APIManagementException;

    /**
     * Get the revision UUID from the Revision no and API UUID
     *
     * @param revisionNum   No of the revision
     * @param apiUUID       API  UUID
     * @return UUID of the revision
     * @throws APIManagementException if failed to get the API revision uuid
     */
    String getAPIRevisionUUID(String revisionNum, String apiUUID) throws APIManagementException;

    /**
     * Get the revision UUID from the Revision no, API UUID and organization
     *
     * @param revisionNum   No of the revision
     * @param apiUUID       API  UUID
     * @param organization  organization ID of the API
     * @return UUID of the revision
     * @throws APIManagementException if failed to get the API revision uuid
     */
    String getAPIRevisionUUIDByOrganization(String revisionNum, String apiUUID, String organization)
            throws APIManagementException;

    /**
     * Get the earliest revision UUID from the revision list for a given API
     *
     * @param apiUUID API UUID
     * @return Earliest revision's UUID
     * @throws APIManagementException if failed to get the revision
     */
    String getEarliestRevisionUUID(String apiUUID) throws APIManagementException;

    /**
     * Get the latest revision UUID from the revision list for a given API
     *
     * @param apiUUID API UUID
     * @return latest revision's UUID
     * @throws APIManagementException if failed to get the revision
     */
    String getLatestRevisionUUID(String apiUUID) throws APIManagementException;

    /**
     * Get a List of API Revisions related to provided API UUID
     *
     * @param apiUUID API  UUID
     * @return API Revision List
     * @throws APIManagementException if failed to get the related API revision
     */
    List<APIRevision> getAPIRevisions(String apiUUID) throws APIManagementException;

    /**
     * Adds a new APIRevisionDeployment to an existing API
     *
     * @param apiId                     API UUID
     * @param apiRevisionId             API Revision UUID
     * @param apiRevisionDeployments    List of APIRevisionDeployment objects
     * @param organization              Identifier of an organization
     * @throws APIManagementException if failed to add APIRevision
     */
    void deployAPIRevision(String apiId, String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments, String organization) throws APIManagementException;

    /**
     * Adds a new DeployedAPIRevision to an existing API
     *
     * @param apiId API UUID
     * @param apiRevisionUUID API Revision UUID
     * @param deployedAPIRevisions List of DeployedAPIRevision objects
     * @throws APIManagementException if failed to add APIRevision
     */
    void addDeployedAPIRevision(String apiId, String apiRevisionUUID, List<DeployedAPIRevision>
            deployedAPIRevisions) throws APIManagementException;

    /**
     * Adds a new DeployedAPIRevision to an existing API
     *
     * @param apiId API UUID
     * @param apiRevisionUUID API Revision UUID
     * @param environment - Un-deployed environment
     * @throws APIManagementException if failed to add APIRevision
     */
    void removeUnDeployedAPIRevision(String apiId, String apiRevisionUUID, String environment) throws APIManagementException;

    /**
     * Update the displayOnDevportal field in an existing deployments of an API
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployment APIRevisionDeployment objects
     * @throws APIManagementException if failed to add APIRevision
     */
    void updateAPIDisplayOnDevportal(String apiId, String apiRevisionId, APIRevisionDeployment apiRevisionDeployment) throws APIManagementException;

    /**
     * Update the displayOnDevportal field in an existing deployments of an API Product
     *
     * @param apiProductId API Product UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployment APIRevisionDeployment objects
     * @throws APIManagementException if failed to add APIRevision
     */
    void updateAPIProductDisplayOnDevportal(String apiProductId, String apiRevisionId, APIRevisionDeployment apiRevisionDeployment) throws APIManagementException;

    /**
     * Get an API Revisions Deployment mapping details by providing deployment name and revision id
     *
     * @param name Deployment Name
     * @param revisionId Revision UUID
     * @return APIRevisionDeployment Object
     * @throws APIManagementException if failed to get the related API revision Deployment Mapping details
     */
    APIRevisionDeployment getAPIRevisionDeployment(String name, String revisionId) throws APIManagementException;

    /**
     * Get an API Revisions Deployment mapping details by providing revision uuid
     *
     * @param revisionUUID Revision UUID
     * @return List<APIRevisionDeployment> Object
     * @throws APIManagementException if failed to get the related API revision Deployment Mapping details
     */
    List<APIRevisionDeployment> getAPIRevisionDeploymentList(String revisionUUID) throws APIManagementException;

    /**
     * Adds a new APIRevisionDeployment to an existing API
     *
     * @param apiId API UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployments List of APIRevisionDeployment objects
     * @param organization identifier of the organization
     * @throws APIManagementException if failed to add APIRevision
     */
    void undeployAPIRevisionDeployment(String apiId, String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments, String organization) throws APIManagementException;

    /**
     * Restore a provided API Revision as the working copy of the API
     *
     * @param apiId          API UUID
     * @param apiRevisionId  API Revision UUID
     * @param orgId          Identifier of an organization
     * @throws APIManagementException if failed to restore APIRevision
     */
    void restoreAPIRevision(String apiId, String apiRevisionId, String orgId) throws APIManagementException;

    /**
     * Delete an API Revision
     *
     * @param apiId         API UUID
     * @param apiRevisionId API Revision UUID
     * @param organization  Identifier of an organization
     * @throws APIManagementException if failed to delete APIRevision
     */
    void deleteAPIRevision(String apiId, String apiRevisionId, String organization) throws APIManagementException;

    /**
     * Delete all API Revision
     *
     * @param apiId         API UUID
     * @param organization  Identifier of an organization
     * @throws APIManagementException if failed to delete APIRevision
     */
    void deleteAPIRevisions(String apiId, String organization) throws APIManagementException;

    /**
     * This method updates the AsyncApi definition in registry
     *
     * @param api   API
     * @param jsonText    AsyncApi definition
     * @throws APIManagementException
     */
    void saveAsyncApiDefinition(API api, String jsonText) throws APIManagementException;
    /**
    * Adds a new APIRevision to an existing API Product
     *
     * @param apiRevision APIRevision
     * @param organization Organization
     * @throws APIManagementException if failed to add APIRevision
     */
    String addAPIProductRevision(APIRevision apiRevision, String organization) throws APIManagementException;

    /**
     * Adds a new APIRevisionDeployment to an existing API Product
     *
     * @param apiProductId API Product UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployments List of APIRevisionDeployment objects
     * @throws APIManagementException if failed to add APIRevision
     */
    void deployAPIProductRevision(String apiProductId, String apiRevisionId, List<APIRevisionDeployment>
            apiRevisionDeployments) throws APIManagementException;

    /**
     * Undeploy revision from provided gateway environments
     *
     * @param apiProductId API Product UUID
     * @param apiRevisionId API Revision UUID
     * @param apiRevisionDeployments List of APIRevisionDeployment objects
     * @throws APIManagementException if failed to add APIRevision
     */
    void undeployAPIProductRevisionDeployment(String apiProductId, String apiRevisionId,
                                              List<APIRevisionDeployment> apiRevisionDeployments) throws APIManagementException;

    /**
     * Restore a provided API Product Revision as the working copy of the API Product
     *
     * @param apiProductId API Product UUID
     * @param apiRevisionId API Revision UUID
     * @param organization organization of the API
     * @throws APIManagementException if failed to restore APIRevision
     */
    void restoreAPIProductRevision(String apiProductId, String apiRevisionId, String organization)
            throws APIManagementException;
    /**
     * Delete an API Product Revision
     *
     * @param apiProductId API Product UUID
     * @param organization Organization
     * @throws APIManagementException if failed to delete APIRevision
     */
    void deleteAPIProductRevisions(String apiProductId, String organization)
            throws APIManagementException;


    /**
     * Delete an API Product Revision
     *
     * @param apiProductId API Product UUID
     * @param apiRevisionId API Revision UUID
     * @param organization Organization
     * @throws APIManagementException if failed to delete APIRevision
     */
    void deleteAPIProductRevision(String apiProductId, String apiRevisionId, String organization)
            throws APIManagementException;

    String generateApiKey(String apiId, String organization) throws APIManagementException;

    List<APIRevisionDeployment> getAPIRevisionsDeploymentList(String apiId) throws APIManagementException;

    void addEnvironmentSpecificAPIProperties(String apiUuid, String envUuid,
            EnvironmentPropertiesDTO environmentPropertyDTO) throws APIManagementException;

    EnvironmentPropertiesDTO getEnvironmentSpecificAPIProperties(String apiUuid, String envUuid)
            throws APIManagementException;

    /**
     * Returns environment of a given uuid
     *
     * @param organization Organization
     * @return List of environments related to the given tenant
     */
    Environment getEnvironment(String organization, String uuid) throws APIManagementException;

    /**
     * Set existing operation policy mapping to the URI Templates
     *
     * @param apiId        API UUID
     * @param uriTemplates Set of URI Templates
     * @throws APIManagementException
     */
    void setOperationPoliciesToURITemplates(String apiId, Set<URITemplate> uriTemplates) throws APIManagementException;

    /**
     * Import an operation policy from the API CTL project. This will either create a new API specific policy,
     * update existing API specific policy or return the policyID of existing policy if policy content is not changed.
     *
     * @param operationPolicyData Operation Policy Data
     * @param organization              Organization name
     * @return UUID of the imported operation policy
     * @throws APIManagementException
     */
    String importOperationPolicy(OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;

    /**
     * Add an API specific operation policy
     *
     * @param apiUUID                   UUID of the API which the policy should be added to
     * @param operationPolicyData Operation Policy Data that includes policy specification and policy definition
     * @param organization              Organization name
     * @return status of the policy storage
     * @throws APIManagementException
     */
    String addAPISpecificOperationPolicy(String apiUUID, OperationPolicyData operationPolicyData,
                                         String organization)
            throws APIManagementException;

    /**
     * Add common operation policy.
     *
     * @param operationPolicyData Operation Policy Data that includes policy specification and policy definition
     * @param organization              Organization name
     * @return status of the policy storage
     * @throws APIManagementException
     */
    String addCommonOperationPolicy(OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;

    /**
     * Get API specific operation policy for a given policy name and API UUID. This will only return the policy data
     * if such policy with name is created as a API specific operation policy. This policy can be either a policy
     * created only for API, a cloned policy from a common policy or a revisioned API specific operation policy.
     * Policy data contains methods to identify whether returned policies is a cloned policy or a revisioned policy
     * or not.
     *
     * @param policyName             API specific policy name
     * @param policyVersion          API specific policy version
     * @param apiUUID                Unique identifier for API
     * @param revisionUUID           Unique identifier for API revision
     * @param organization           Organization name
     * @param isWithPolicyDefinition This will decide whether to return policy definition or not as policy definition
     *                               is bit bulky
     * @return Operation Policy
     * @throws APIManagementException
     */
    OperationPolicyData getAPISpecificOperationPolicyByPolicyName(String policyName, String policyVersion, String apiUUID,
                                                                  String revisionUUID,
                                                                  String organization,
                                                                  boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Get the common operation policy for a given policy name. This will only return the policy data if there is
     * a matching policy created as a common policy. If not, it will return null
     *
     * @param policyName             Common Policy name
     * @param policyVersion          Common Policy version
     * @param organization           Organization
     * @param isWithPolicyDefinition This will decide whether to return policy definition or not as policy definition
     *                               is bit bulky
     * @return Common Operation Policy
     * @throws APIManagementException
     */
    OperationPolicyData getCommonOperationPolicyByPolicyName(String policyName, String policyVersion, String organization,
                                                             boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Get API specific operation policy for a given Policy UUID. Even though a policy ID is provided, this will only
     * return policy if the policy is created for API. Otherwise it will return a null.
     *
     * @param policyId               Policy UUID
     * @param apiUUID                Policy UUID
     * @param organization           Organization name
     * @param isWithPolicyDefinition This will decide whether to return policy definition or not as policy definition
     *                               is bit bulky
     * @return Operation Policy
     * @throws APIManagementException
     */
    OperationPolicyData getAPISpecificOperationPolicyByPolicyId(String policyId, String apiUUID,
                                                                String organization,
                                                                boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Get common operation policy for a given Policy UUID. Even though a policy ID is provided, this will only
     * return policy if the policy is created as a common policy. Otherwise it will return a null.
     *
     * @param policyId               Policy UUID
     * @param organization           Organization name
     * @param isWithPolicyDefinition This will decide whether to return policy definition or not as policy definition
     *                               is bit bulky
     * @return Operation Policy
     * @throws APIManagementException
     */
    OperationPolicyData getCommonOperationPolicyByPolicyId(String policyId, String organization,
                                                           boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Update an existing operation policy
     *
     * @param operationPolicyId   Unique identifier of the operation policy
     * @param operationPolicyData Operation Policy Data that needs to be updated.
     * @param organization        Organization name
     * @return status of the policy update
     * @throws APIManagementException
     */
    void updateOperationPolicy(String operationPolicyId, OperationPolicyData operationPolicyData,
                               String organization) throws APIManagementException;

    /**
     * Get a light weight version of all the common policies for the tenant domain. This will not include the policy
     * definition as it is bulky. Policy specification and policy UUID will be included in the policyData object.
     *
     * @param organization Organization name
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    List<OperationPolicyData> getAllCommonOperationPolicies(String organization)
            throws APIManagementException;

    /**
     * Get a light weight version of all the API Specific Operation policies. This will not include the policy
     * definition as it is bulky. Policy specification and policy UUID will be included in the policyData object.
     *
     * @param apiUUID      UUID of the API
     * @param organization Organization name
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    List<OperationPolicyData> getAllAPISpecificOperationPolicies(String apiUUID, String organization)
            throws APIManagementException;

    /**
     * Delete an operation policy by providing the policy ID
     *
     * @param policyId     Operation Policy UUID
     * @param organization Organization name
     * @throws APIManagementException
     */
    void deleteOperationPolicyById(String policyId, String organization) throws APIManagementException;

    /**
     * Load the mediation policies if exists to the API. If a mediation policy is defined in the object under keys
     * inSequence, outSequence or faultSequence, this method will search in the registry for a such sequence and
     * populate the inSequenceMediation, outSequenceMediation or faultSequenceMediation attributes with a mediation
     * object.
     *
     * @param api     API object
     * @param organization Organization name
     * @throws APIManagementException
     */
    void loadMediationPoliciesToAPI(API api, String organization) throws APIManagementException;

    /**
     * Check whether the provided api uuid is a revisioned API's uuid or not.
     *
     * @param apiUUID    API UUID
     * @throws APIManagementException
     */
    APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException;

    /**
     * Returns details of an APIProduct
     *
     * @param identifier APIProductIdentifier
     * @return An APIProduct object related to the given identifier or null
     * @throws APIManagementException if failed get APIProduct from APIProductIdentifier
     */
    APIProduct getAPIProduct(APIProductIdentifier identifier) throws APIManagementException;

    /**
     * Returns APIProduct Search result based on the provided query.
     *
     * @param searchQuery     search query. Ex: provider=*admin*
     * @param tenantDomain    tenant domain
     * @param start           starting number
     * @param end             ending number
     * @return APIProduct result
     * @throws APIManagementException if search is failed
     */
    Map<String,Object> searchPaginatedAPIProducts(String searchQuery, String tenantDomain,int start,int end) throws
            APIManagementException;

    /**
     * Returns security scheme of an API
     *
     * @param uuid         UUID of the API's registry artifact
     * @param organization Identifier of an organization
     * @return A String containing security scheme of the API
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    String getSecuritySchemeOfAPI(String uuid, String organization) throws APIManagementException;

    /**
     * Returns details of an API
     * @param uuid   UUID of the API's registry artifact
     * @param organization  Identifier of an organization
     * @return An API object related to the given artifact id or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getAPIbyUUID(String uuid, String organization) throws APIManagementException;

    /**
     * Returns API Search result based on fqdn of the provided endpoint.
     * Returns empty API Search result if endpoint is invalid.
     *
     * @param endpoint        endpoint Ex: https://api.wso2.com
     * @param tenantDomain    tenant domain
     * @param start           starting number
     * @param end             ending number
     * @return APIProduct result
     * @throws APIManagementException if search is failed
     */
    APISearchResult searchPaginatedAPIsByFQDN(String endpoint, String tenantDomain, int start, int end) throws
            APIManagementException;

    /**
     * This method checks if the contextTemplate of the API matches its previous versions.
     *
     * @param providerName    Name of the provider
     * @param apiName         Name of the API
     * @param contextTemplate Context template of the API
     * @param userName        Logged in user
     * @param organization    Organization
     * @return true if the contextTemplate of the API matches its previous versions. Otherwise, return false.
     * @throws APIManagementException if an exception occurs while querying the APIs.
     */
    boolean isValidContext(String providerName, String apiName, String contextTemplate, String userName,
                           String organization) throws APIManagementException;
    /***
     * Validate the policies with spec
     * @param policySpecification policy spec
     * @param appliedPolicy policyID
     * @param apiType API Type
     * @return validation status
     * @throws APIManagementException
     */
    boolean validateAppliedPolicyWithSpecification(OperationPolicySpecification policySpecification, OperationPolicy
            appliedPolicy, String apiType) throws APIManagementException;

    /**
     * Resume API revision deployment process
     *
     * @param apiId        API Id using for the revision deployment
     * @param organization organization identifier
     * @param revisionUUID revision UUID
     * @param revisionId   revision number
     * @param environment  environment the deployment is happening
     */
    void resumeDeployedAPIRevision(String apiId, String organization, String revisionUUID, String revisionId,
            String environment);

    /***
     * Cleanup pending or rejected revision workflows
     *
     * @param apiId Id of the API
     * @param externalRef external Id of the revision
     * @throws APIManagementException if an exception occurs while cleaning up revision deployment
     */
    void cleanupAPIRevisionDeploymentWorkflows(String apiId, String externalRef) throws APIManagementException;


    /**
     * Apply globally added policies to the flows.
     *
     * @param gatewayGlobalPolicyList   List of Gateway Policy objects
     * @param orgId                     Organization ID
     * @return Policy Mapping ID
     * @throws APIManagementException
     */
    String applyGatewayGlobalPolicies(List<OperationPolicy> gatewayGlobalPolicyList, String description, String name,
            String orgId) throws APIManagementException;

    /**
     * Engage globally added policies to the gateways.
     *
     * @param gatewayPolicyDeploymentMap Policy mapping deployment metadata map
     * @param orgId                      Organization ID
     * @throws APIManagementException
     */
    void engageGatewayGlobalPolicies(Map<Boolean, List<GatewayPolicyDeployment>> gatewayPolicyDeploymentMap,
            String orgId, String gatewayPolicyMappingId) throws APIManagementException;

    /**
     * Get gateway policy list for a given Policy Mapping UUID.
     *
     * @param policyMappingUUID      Policy mapping UUID
     * @param isWithPolicyDefinition This will decide whether to return policy definition or not as policy definition
     *                               is a bit bulky
     * @return Gateway Policy Data List
     * @throws APIManagementException
     */
    List<OperationPolicyData> getGatewayPolicyDataListByPolicyId(String policyMappingUUID,
            boolean isWithPolicyDefinition) throws APIManagementException;

    /**
     * Get gateway policies attached to the policy mapping.
     *
     * @param policyMappingUUID Policy mapping UUID
     * @return List of gateway Policies
     * @throws APIManagementException
     */
    List<OperationPolicy> getOperationPoliciesOfPolicyMapping(String policyMappingUUID) throws APIManagementException;

    /**
     * Get gateway policies mapping UUID attached to the gateway.
     *
     * @param gatewayLabel Gateway label
     * @param orgId        Organization ID
     * @return Policy mapping UUID list
     * @throws APIManagementException
     */
    List<String> getAllPolicyMappingUUIDsByGatewayLabels(String[] gatewayLabel, String orgId)
            throws APIManagementException;

    /**
     * This method is to delete a gateway policy mapping.
     *
     * @param gatewayPolicyMappingId Gateway policy mapping UUID
     * @throws APIManagementException if failed to delete comment for identifier
     */
    void deleteGatewayPolicyMappingByPolicyMappingId(String gatewayPolicyMappingId, String tenantDomain)
            throws APIManagementException;

    /**
     * Update globally added policies to the flows.
     *
     * @param gatewayGlobalPolicyList List of Gateway Policy objects to be updated
     * @param orgId                   Organization ID
     * @param policyMappingId         Policy mapping UUID
     * @return Policy Mapping ID
     * @throws APIManagementException
     */
    String updateGatewayGlobalPolicies(List<OperationPolicy> gatewayGlobalPolicyList, String description, String name,
            String orgId, String policyMappingId) throws APIManagementException;

    /**
     * Get a lightweight version of all the gateway policies for the tenant domain. This will not include the policy
     * definition as it is bulky.
     *
     * @param organization Organization name
     * @return List of Gateway Policies
     * @throws APIManagementException
     */
    List<GatewayPolicyData> getAllLightweightGatewayPolicyMappings(String organization) throws APIManagementException;

    /**
     * Get a lightweight version of deployment information for the gateway policy mapping associated with the provided
     * gateway label within the tenant domain. This will not include the policy definition as it is bulky.
     *
     * @param organization Organization name
     * @param gatewayLabel Gateway label
     * @return List of Gateway Policies
     * @throws APIManagementException
     */
    GatewayPolicyData getLightweightGatewayPolicyMappings(String organization, String gatewayLabel) throws APIManagementException;

    /**
     * Get a lightweight policy mapping data for a particular mapping ID. This will not include the policy
     * definition as it is bulky.
     *
     * @param policyMappingUUID Policy mapping UUID
     * @param tenantDomain      Tenant domain
     * @return Gateway Policy Data
     * @throws APIManagementException
     */
    GatewayPolicyData getGatewayPolicyMappingDataByPolicyMappingId(String policyMappingUUID, String tenantDomain)
            throws APIManagementException;

    /**
     * Checks whether a policy mapping deployment exists for a given policy mapping ID.
     *
     * @param gatewayPolicyMappingId Policy mapping UUID
     * @param tenantDomain           Tenant domain
     * @return true if a policy mapping deployment exists for a given policy mapping ID
     * @throws APIManagementException
     */
    boolean isPolicyMappingDeploymentExists(String gatewayPolicyMappingId, String tenantDomain)
            throws APIManagementException;

    /**
     * Checks whether a policy mapping deployment exists for a given gateway label.
     *
     * @param gatewayLabel           Gateway label
     * @param tenantDomain           Tenant domain
     * @return true if a policy mapping deployment exists for a given policy mapping ID and gateway label
     * @throws APIManagementException
     */
    boolean hasExistingDeployments(String tenantDomain, String gatewayLabel) throws APIManagementException;

    /**
     * Checks whether a policy mapping metadata exists for a given policy mapping ID.
     *
     * @param gatewayPolicyMappingId Policy mapping UUID
     * @return true if a policy mapping metadata exists for a given policy mapping ID
     * @throws APIManagementException
     */
    boolean isPolicyMetadataExists(String gatewayPolicyMappingId)
            throws APIManagementException;

    /**
     * Checks whether a common policy exists based on the provided common policy UUID within gateway policy mappings.
     *
     * @param commonPolicyUUID Common policy UUID
     * @return count of the common policy usage
     * @throws APIManagementException
     */
    int getPolicyUsageByPolicyUUIDInGatewayPolicies(String commonPolicyUUID) throws APIManagementException;
}
