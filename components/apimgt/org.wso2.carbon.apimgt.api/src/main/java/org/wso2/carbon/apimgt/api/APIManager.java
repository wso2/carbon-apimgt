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

import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.policy.Policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core API management interface which provides functionality related to APIs, API metadata
 * and API subscribers (consumers).
 */
public interface APIManager {

    /**
     * Returns a list of all existing APIs by all providers. The API objects returned by this
     * method may be partially initialized (due to performance reasons). Each API instance
     * is guaranteed to have the API name, version, provider name, context, status and icon URL.
     * All other fields may not be initialized. Therefore, the objects returned by this method
     * must not be used to access any metadata item related to an API, other than the ones listed
     * above. For that purpose a fully initialized API object instance should be acquired by
     * calling the getAPI(APIIdentifier) method.
     *
     * @return a List of API objects (partially initialized), possibly empty
     * @throws APIManagementException on error
     */
    List<API> getAllAPIs() throws APIManagementException;
    /**
     * Returns details of an API
     *
     * @param apiPath APIIdentifier
     * @return An API object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getAPI(String apiPath) throws APIManagementException;
    /**
     * Returns details of an API
     *
     * @param uuid UUID of the API's registry artifact
     * @param requestedTenantDomain tenantDomain for the registry
     * @return An API object related to the given artifact id or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getAPIbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException;

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid  Registry artifact id
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    API getLightweightAPIByUUID(String uuid, String requestedTenantDomain) throws APIManagementException;

    /** 
     * Get minimal details of API by API identifier
     * @param identifier APIIdentifier object
     * @return API of the provided APIIdentifier
     * @throws APIManagementException
     */
    API getLightweightAPI(APIIdentifier identifier) throws APIManagementException;
    /**
     * Returns details of an API
     *
     * @param identifier APIIdentifier
     * @return An API object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Checks the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException;

    /**
     * Checks whether the given API context is already registered in the system
     *
     * @param context A String representing an API context
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isContextExist(String context) throws APIManagementException;

    /**
     * Checks whether the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @return true if the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isApiNameExist(String apiName) throws APIManagementException;

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    Set<String> getAPIVersions(String providerName, String apiName) throws APIManagementException;

    /**
     * Returns the swagger v2.0 definition as a string
     *
     * @param apiId id of the APIIdentifier
     * @return swagger string
     * @throws APIManagementException
     */
    String getSwagger20Definition(APIIdentifier apiId) throws APIManagementException;

    /**
     * Checks whether the given document already exists for the given api
     *
     * @param identifier API Identifier
     * @param docName Name of the document
     * @return true if document already exists for the given api
     * @throws APIManagementException if failed to check existence of the documentation
     */
    boolean isDocumentationExist(APIIdentifier identifier, String docName) throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    List<Documentation> getAllDocumentation(APIIdentifier apiId,String loggedUserName)
            throws APIManagementException;

    /**
     * Returns the specified document attached to the given API
     *
     * @param apiId   APIIdentifier
     * @param docType type of the documentation
     * @param docName name of the doc
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
    Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType, String docName) throws APIManagementException;

    /**
     * Get a documentation by artifact Id
     *
     * @param docId   DocumentID
     * @param requestedTenantDomain tenant domain of the registry where the artifact is located
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
     Documentation getDocumentation(String docId, String requestedTenantDomain) throws APIManagementException;

    /**
     * This method used to get the content of a documentation
     *
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @return if failed to get doc content
     * @throws APIManagementException if the asking documentation content is unavailable
     */
    String getDocumentationContent(APIIdentifier identifier, String documentationName) throws APIManagementException;

    /**
     * Retrieves the subscriber from the given access token
     *
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /** returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Subscription
     * @return SubscribedAPI object which is related to the UUID
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException;

    /**
     * Creates a new subscriber given the username and the grouping Id
     *
     * @param username Username of the subscriber to be added
     * @param groupingId - the groupId to which the subscriber belongs to
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed add subscriber
     */
    void addSubscriber(String username, String groupingId) throws APIManagementException;

    /**
     * Updates the details of the given subscriber.
     *
     * @param subscriber The subscriber to be updated
     * @throws APIManagementException if failed to update subscriber
     */
    void updateSubscriber(Subscriber subscriber) throws APIManagementException;

    /**
     * Returns the subscriber for the given subscriber id.
     *
     * @param subscriberId The subscriber id of the subscriber to be returned
     * @return The looked up subscriber or null if the requested subscriber does not exist
     * @throws APIManagementException if failed to get Subscriber
     */
    Subscriber getSubscriber(int subscriberId) throws APIManagementException;

    /**
     * Returns a set of APIs purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException;

    /**
     * Associates the given icon image with the specified path.
     *
     * @param resourcePath a String representing the relative path of a resource.
     * @param resourceFile         to be saved
     * @return a String URL pointing to the image that was added
     * @throws APIManagementException if an error occurs while adding the icon image
     */
    String addResourceFile(String resourcePath, ResourceFile resourceFile) throws APIManagementException;

    /**
     * Retrieves the icon image associated with a particular API as a stream.
     *
     * @param identifier ID representing the API
     * @return an Icon containing image content and content type information
     * @throws APIManagementException if an error occurs while retrieving the image
     */
    ResourceFile getIcon(APIIdentifier identifier) throws APIManagementException;

    /**
     * Cleans up any resources acquired by this APIManager instance. It is recommended
     * to call this method once the APIManager instance is no longer required.
     *
     * @throws APIManagementException if an error occurs while cleaning up
     */
    void cleanup() throws APIManagementException;

    /**
     * Returns the corresponding application given the uuid
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getApplicationByUUID(String uuid) throws APIManagementException;

    /**
     * Check whether an application access token is already persist in database.
     *
     * @param accessToken
     * @return
     * @throws APIManagementException
     */
    boolean isApplicationTokenExists(String accessToken) throws APIManagementException;

    /**
     * Check whether an application access token is already revoked.
     *
     * @param accessToken
     * @return
     * @throws APIManagementException
     */
    boolean isApplicationTokenRevoked(String accessToken) throws APIManagementException;

    /**
     * Return information related to a specific access token
     *
     * @param accessToken AccessToken
     * @return
     * @throws APIManagementException
     */
    APIKey getAccessTokenData(String accessToken) throws APIManagementException;

    /**
    /**
     * Return information related to access token by a searchTerm and searchType       *
     *
     *
     * @param searchType
     * @param searchTerm
     * @param loggedInUser
     * @return
     * @throws APIManagementException
     */
    Map<Integer, APIKey> searchAccessToken(String searchType, String searchTerm, String loggedInUser)
            throws APIManagementException;

    /**
     * Return subscribed APIs per access token
     *
     * @param accessToken
     * @return
     * @throws APIManagementException
     */
    Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException;

    /**
     * Retrieves all predefined {@link org.wso2.carbon.apimgt.api.model.Tier} in the system
     *
     * @return Set of tiers
     * @throws APIManagementException if failed to get the predefined tiers
     */
    Set<Tier> getAllTiers() throws APIManagementException;

    /**
     * Retrieves all predefined {@link org.wso2.carbon.apimgt.api.model.Tier} for the tenant in the system
     *
     * @return Set of tiers
     * @throws APIManagementException if failed to get the predefined tiers
     */
    Set<Tier> getAllTiers(String tenantDomain) throws APIManagementException;

    /**
    * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
    *
    * @return Set<Tier>
    * @throws APIManagementException if failed to get the predefined tiers
    */
    Set<Tier> getTiers() throws APIManagementException;

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     * @throws APIManagementException if failed to get the predefined tiers
     */
    Set<Tier> getTiers(String tenantDomain) throws APIManagementException;

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @param tierType     type of the tiers (api,resource ot application)
     * @param tenantDomain tenant domain to get the tiers
     * @return Set<Tier> return list of tier names
     * @throws APIManagementException APIManagementException if failed to get the predefined tiers
     */
    Set<Tier> getTiers(int tierType, String tenantDomain) throws APIManagementException;

    /**
     * Returns a list of domain name mappings store / gateway.
     *
     * @return Set<Tier>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get the predefined tiers
     */
    Map<String,String> getTenantDomainMappings(String tenantDomain, String appType) throws APIManagementException;

    /**
     * Check whether the given scope key is already available under given tenant
     *
     * @param scopeKey candidate scope key
     * @param tenantid tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isScopeKeyExist(String scopeKey, int tenantid) throws APIManagementException;
    
    /**
     * Check whether the given scope key is already assigned to an API under given tenant
     *
     * @param identifier API Identifier 
     * @param scopeKey candidate scope key
     * @param tenantid tenant id
     * @return true if the scope key is already available
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isScopeKeyAssigned(APIIdentifier identifier, String scopeKey, int tenantid) throws APIManagementException;

    /**
     * Check if a given context template already exists
     * @param contextTemplate - The contextTemplate to be checked for
        *                        <p>
        *                        Ex: /foo/{version}/bar
        *                        </p>
     * @return boolean - true if the template exists, false otherwise.
     * @throws APIManagementException - If an error occurs while checking the value in the APIM DB.
     */
    boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException;
    

    /**
     * Get policy object for given level and user name
     * @param username
     * @param level
     * @return
     * @throws APIManagementException
     */
    Policy[] getPolicies(String username, String level) throws APIManagementException;
    
    /**
     * Returns API Search result based on the provided query. This search method supports '&' based concatenate 
     * search in multiple fields. 
     * @param searchQuery search query. Ex: provider=*admin*&version=*1*
     * @param tenantDomain tenant domain 
     * @param start starting number
     * @param end ending number
     * @param limitAttributes whether or not to limit attributes in the search result
     * @return API result
     * @throws APIManagementException if search is failed
     */
    Map<String,Object> searchPaginatedAPIs(String searchQuery, String tenantDomain,int start,int end, 
                                           boolean limitAttributes) throws APIManagementException;

    /**
     * Checks the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    boolean isAPIProductAvailable(APIProductIdentifier identifier) throws APIManagementException;

    /**
     * Returns details of an APIProduct
     *
     * @param identifier APIProductIdentifier
     * @return An APIProduct object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIProductIdentifier
     */
    APIProduct getAPIProduct(APIProductIdentifier identifier) throws APIManagementException;
}
