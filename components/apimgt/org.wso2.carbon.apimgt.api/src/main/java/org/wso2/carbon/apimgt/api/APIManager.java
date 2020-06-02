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

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Wsdl;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.registry.api.Resource;

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
     * @param uuid                  UUID of the API's registry artifact
     * @param requestedTenantDomain tenantDomain for the registry
     * @return An API object related to the given artifact id or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getAPIbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException;

    /**
     * Get API or APIProduct by registry artifact id
     *
     * @param uuid                  Registry artifact id
     * @param requestedTenantDomain tenantDomain for the registry
     * @return ApiTypeWrapper wrapping the API or APIProduct of the provided artifact id
     * @throws APIManagementException
     */
    ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String requestedTenantDomain)
            throws APIManagementException;

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid Registry artifact id
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    API getLightweightAPIByUUID(String uuid, String requestedTenantDomain) throws APIManagementException;

    /**
     * Get minimal details of API by API identifier
     *
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
     * Checks the Availability of given APIProductIdentifier
     *
     * @param identifier APIProductIdentifier
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API Product availability
     */
    boolean isAPIProductAvailable(APIProductIdentifier identifier) throws APIManagementException;

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
     * @throws APIManagementException if failed to check the api name availability
     */
    boolean isApiNameExist(String apiName) throws APIManagementException;

    /**
     * Checks whether a different letter case of the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @return true if a different letter case of the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the different letter case api name availability
     */
    boolean isApiNameWithDifferentCaseExist(String apiName) throws APIManagementException;

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    Set<String> getAPIVersions(String providerName, String apiName) throws APIManagementException;

    String getGraphqlSchemaDefinition(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns the swagger v2.0 definition as a string
     *
     * @param apiId id of the APIIdentifier
     * @return swagger string
     * @throws APIManagementException
     */
    String getOpenAPIDefinition(Identifier apiId) throws APIManagementException;

    /**
     * Checks whether the given document already exists for the given api/product
     *
     * @param identifier API/Product Identifier
     * @param docName    Name of the document
     * @return true if document already exists for the given api/product
     * @throws APIManagementException if failed to check existence of the documentation
     */
    boolean isDocumentationExist(Identifier identifier, String docName) throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API/API Product
     *
     * @param id Identifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    List<Documentation> getAllDocumentation(Identifier id) throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API
     *
     * @param apiId APIIdentifier
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    List<Documentation> getAllDocumentation(APIIdentifier apiId, String loggedUserName)
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
     * @param identifier,        API/Product identifier
     * @param documentationName, name of the inline documentation
     * @return if failed to get doc content
     * @throws APIManagementException if the asking documentation content is unavailable
     */
    String getDocumentationContent(Identifier identifier, String documentationName) throws APIManagementException;

    /**
     * Retrieves the subscriber from the given access token
     *
     * @param accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Subscription
     * @return SubscribedAPI object which is related to the UUID
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException;

    /**
     * Creates a new subscriber given the username and the grouping Id
     *
     * @param username   Username of the subscriber to be added
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
     * @param identifier ID representing the API
     * @param resourcePath a String representing the relative path of a resource.
     * @param resourceFile to be saved
     * @return a String URL pointing to the image that was added
     * @throws APIManagementException if an error occurs while adding the icon image
     */
    String addResourceFile(Identifier identifier, String resourcePath, ResourceFile resourceFile)
            throws APIManagementException;

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
     * Returns the corresponding application given the uuid. It will also contain keys of the application
     *
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getApplicationByUUID(String uuid) throws APIManagementException;

    /**
     * Returns the corresponding application given the uuid. The returned application will not contain key information
     *
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getLightweightApplicationByUUID(String uuid) throws APIManagementException;

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
    Map<String, String> getTenantDomainMappings(String tenantDomain, String appType) throws APIManagementException;

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
     * Check whether the given scope key is already assigned to any API under given tenant.
     *
     * @param scopeKey     Scope
     * @param tenantDomain Tenant Domain
     * @return true if scope is attached to an API
     * @throws APIManagementException if failed to check the assignment
     */
    boolean isScopeKeyAssignedToAPI(String scopeKey, String tenantDomain) throws APIManagementException;

    /**
     * Check whether the given scope key is already assigned to an API as local scope under given tenant.
     * This will return false if those APIs are different versions of the same API.
     *
     * @param apiIdentifier API Identifier
     * @param scopeKey   candidate scope key
     * @param tenantId   tenant Id
     * @return true if the scope key is already attached as a local scope in any API
     * @throws APIManagementException if failed to check the local scope availability
     */
    boolean isScopeKeyAssignedLocally(APIIdentifier apiIdentifier, String scopeKey, int tenantId)
            throws APIManagementException;

    /**
     * Check if a given context template already exists
     *
     * @param contextTemplate - The contextTemplate to be checked for
        *                        <p>
        *                        Ex: /foo/{version}/bar
        *                        </p>
     * @return boolean - true if the template exists, false otherwise.
     * @throws APIManagementException - If an error occurs while checking the value in the APIM DB.
     */
    boolean isDuplicateContextTemplate(String contextTemplate) throws APIManagementException;

    /**
     * get a set of API names that matches given context template
     *
     * @param contextTemplate context in the payload
     * @return list of API names matches the context
     * @throws APIManagementException
     */
    List<String> getApiNamesMatchingContext(String contextTemplate) throws APIManagementException;

    /**
     * Get policy object for given level and user name
     *
     * @param username
     * @param level
     * @return
     * @throws APIManagementException
     */
    Policy[] getPolicies(String username, String level) throws APIManagementException;

    /**
     * Returns API Search result based on the provided query. This search method supports '&' based concatenate
     * search in multiple fields.
     *
     * @param searchQuery     search query. Ex: provider=*admin*&version=*1*
     * @param tenantDomain    tenant domain
     * @param start           starting number
     * @param end             ending number
     * @param limitAttributes whether or not to limit attributes in the search result
     * @return API result
     * @throws APIManagementException if search is failed
     */
    Map<String,Object> searchPaginatedAPIs(String searchQuery, String tenantDomain,int start,int end,
                                           boolean limitAttributes) throws APIManagementException;


    /**
     * fetches the lastUpdated timestamp for the API swagger resource
     *
     * @param apiIdentifier
     * @return long
     * @throws APIManagementException
     */
    Map<String, String> getSwaggerDefinitionTimeStamps(APIIdentifier apiIdentifier) throws APIManagementException;

    /**
     * gets the updated timestamp for the API swagger resource
     *
     * @param apiIdentifier
     * @return long
     * @throws APIManagementException
     */
    String getThumbnailLastUpdatedTime(APIIdentifier apiIdentifier) throws APIManagementException;

    /**
     * Returns list of global mediation policies
     *
     * @return list of Mediation objects related to the given identifier or null
     * @throws APIManagementException if failed to get global mediation policies
     */
    List<Mediation> getAllGlobalMediationPolicies() throws APIManagementException;

    /**
     * Delete existing global mediation policy
     *
     * @param mediationPolicyId uuid of the global mediation policy
     * @return True is deletion successful
     * @throws APIManagementException If failed to delete mediation policy
     */
    boolean deleteGlobalMediationPolicy(String mediationPolicyId) throws APIManagementException;


    /**
     * Return mediation specify by identifier
     *
     * @param mediationPolicyId uuid of the mediation policy resource
     * @return A Mediation object related to the given identifier or null
     * @throws APIManagementException If failed to get specified mediation policy
     */
    Mediation getGlobalMediationPolicy(String mediationPolicyId) throws APIManagementException;

    /**
     * Return the uuid of the mediation policy in given registry path
     *
     * @param mediationPolicyPath path to the registry resource
     * @return uuid of the resource
     */
    String getCreatedResourceUuid(String mediationPolicyPath);

    /**
     * Returns registry resource specify by the mediation identifier
     *
     * @param mediationPolicyId uuid of the resource
     * @return Registry resource correspond to identifier or null
     */
    Resource getCustomMediationResourceFromUuid(String mediationPolicyId) throws APIManagementException;

    /**
     * Returns Registry resource matching given mediation policy identifier
     * @param identifier ID representing the API
     * @param uuid         mediation policy identifier
     * @param resourcePath registry path to the API resource
     * @return Registry resource matches given identifier or null
     * @throws APIManagementException If fails to get the resource matching given identifier
     */
    Resource getApiSpecificMediationResourceFromUuid(Identifier identifier, String uuid, String resourcePath)
            throws APIManagementException;

    /**
     * Returns list of API specific mediation policies
     *
     * @param apiIdentifier API identifier
     * @return list of mediation policy objects or null
     * @throws APIManagementException If unable to return satisfied mediation object list
     */
    List<Mediation> getAllApiSpecificMediationPolicies(APIIdentifier apiIdentifier)
            throws APIManagementException;

    /**
     * Returns the mediation policy name specify inside mediation config
     *
     * @param config mediation config content
     * @return name of the mediation policy or null
     */
    String getMediationNameFromConfig(String config);

    /**
     * Returns API specific mediation policy specified by the identifier
     * @param identifier ID representing the API
     * @param apiResourcePath   registry path to the API resource
     * @param mediationPolicyId mediation policy identifier
     * @return Mediation object of given identifier or null
     */
    Mediation getApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath, String mediationPolicyId)
            throws APIManagementException;

    /**
     * Delete a API specific mediation policy identified by the identifier
     *
     * @param apiResourcePath   registry path to the API resource
     * @param mediationPolicyId mediation policy identifier
     * @throws APIManagementException If failed to delete the given mediation policy
     */
    Boolean deleteApiSpecificMediationPolicy(Identifier identifier, String apiResourcePath, String mediationPolicyId)
            throws APIManagementException;

    /**
     * Returns true if resource already exists in registry
     *
     * @param mediationPolicyPath resource path
     * @return true, If resource exists
     */
    boolean checkIfResourceExists(String mediationPolicyPath) throws APIManagementException;

    /**
     * Returns a list of api versions that matches the given context template
     *
     * @param apiName api name in the payload
     * @return api versions that matches context template
     * @throws APIManagementException If failed to get the list of api versions
     */
    List<String> getApiVersionsMatchingApiName(String apiName,String username) throws APIManagementException;

    /**
     * Returns list of wsdls
     *
     * @return list of wsdl objects or null
     * @throws APIManagementException If unable to return satisfied wsdl object list
     */
    @Deprecated
    List<Wsdl> getAllWsdls() throws APIManagementException;

    /**
     * Return Wsdl specify by identifier
     *
     * @param wsdlId uuid of the wsdl resource
     * @return A Wsdl object related to the given identifier or null
     * @throws APIManagementException If failed to get specified wsdl
     */
    @Deprecated
    Wsdl getWsdlById(String wsdlId) throws APIManagementException;

    /**
     * Returns Registry resource matching given wsdl identifier
     *
     * @param wsdlId wsdl identifier
     * @return Registry resource matches given identifier or null
     * @throws APIManagementException If fails to get the resource matching given identifier
     */
    @Deprecated
    Resource getWsdlResourceFromUuid(String wsdlId) throws APIManagementException;

    /**
     * Delete an existing wsdl
     *
     * @param wsdlId uuid of the wsdl
     * @return true if deleted successfully
     * @throws APIManagementException If failed to delete wsdl
     */
    @Deprecated
    boolean deleteWsdl(String wsdlId) throws APIManagementException;

    /**
     * Create a wsdl in the path specified.
     *
     * @param resourcePath   Registry path of the resource
     * @param wsdlDefinition wsdl content
     */
    @Deprecated
    // Only used in old CXF REST APIS. Remove this once old CXF REST APIs are removed.
    void uploadWsdl(String resourcePath, String wsdlDefinition) throws APIManagementException;

    /**
     * Update a existing wsdl in the path specified
     *
     * @param resourcePath   Registry path of the resource
     * @param wsdlDefinition wsdl content
     */
    @Deprecated
    void updateWsdl(String resourcePath, String wsdlDefinition) throws APIManagementException;

    /**
     * Returns the wsdl content in registry specified by the wsdl name. If it is a single WSDL, the content will be
     * returned as String or if it is an archive, an InputStream pointed to the content will be returned.
     *
     * @param apiId api identifier of the API
     * @return wsdl content matching name if exist else throws an APIManagementException
     */
    ResourceFile getWSDL(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns the graphql schema content in registry specified by the schema name
     *
     * @param apiId api identifier of the API
     * @return schema content matching name if exist else null
     */
    String getGraphqlSchema(APIIdentifier apiId) throws APIManagementException;

    /**
     * Returns the corresponding application given the subscriberId and application name
     *
     * @param subscriberId subscriberId of the Application
     * @param applicationName name of the Application
     * @return it will return Application corresponds to the subscriberId and name provided.
     * @throws APIManagementException
     */
    Application getApplicationBySubscriberIdAndName(int subscriberId, String applicationName) throws APIManagementException;

    /**
     * Returns details of an APIProduct
     *
     * @param uuid                  UUID of the API Product's registry artifact
     * @param requestedTenantDomain tenantDomain for the registry
     * @return An API Product object related to the given artifact id or null
     * @throws APIManagementException if failed get APIProduct from UUID
     */
    APIProduct getAPIProductbyUUID(String uuid, String requestedTenantDomain) throws APIManagementException;

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
     * Returns resource list of the api product
     *
     * @param productIdentifier
     * @return
     * @throws APIManagementException
     */
    List<APIProductResource> getResourcesOfAPIProduct(APIProductIdentifier productIdentifier)
            throws APIManagementException;

    /**
     * Retrieves the icon image associated with a particular API Product as a stream.
     *
     * @param identifier ID representing the API Product
     * @return an Icon containing image content and content type information
     * @throws APIManagementException if an error occurs while retrieving the image
     */
    ResourceFile getProductIcon(APIProductIdentifier identifier) throws APIManagementException;

    /**
     * Associates the given product resource with the specified path.
     * @param identifier ID representing the API Product
     * @param resourcePath a String representing the relative path of a resource.
     * @param resourceFile to be saved
     * @return a String URL pointing to the image that was added
     * @throws APIManagementException if an error occurs while adding the icon image
     */
    String addProductResourceFile(APIProductIdentifier identifier, String resourcePath, ResourceFile resourceFile)
            throws APIManagementException;

    /**
     * Get an api product documentation by artifact Id
     *
     * @param docId   DocumentID
     * @param requestedTenantDomain tenant domain of the registry where the artifact is located
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
    Documentation getProductDocumentation(String docId, String requestedTenantDomain) throws APIManagementException;

    /**
     * Get open api definition for the product
     * @param  product
     * @return openapidoc
     * @throws APIManagementException
     */
    String getAPIDefinitionOfAPIProduct(APIProduct product) throws APIManagementException;
}
