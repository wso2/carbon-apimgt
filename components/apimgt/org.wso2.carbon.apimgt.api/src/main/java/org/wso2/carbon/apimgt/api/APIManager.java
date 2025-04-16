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
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
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
     * Returns the minimalistic information about the API given the UUID. This will only query from AM database AM_API
     * table.
     *
     * @param id UUID of the API
     * @return basic information about the API
     * @throws APIManagementException error while getting the API information from AM_API
     */
    APIInfo getAPIInfoByUUID(String id) throws APIManagementException;

    /**
     * Get API or APIProduct by registry artifact id
     *
     * @param uuid   Registry artifact id
     * @param organization  Organization
     * @return ApiTypeWrapper wrapping the API or APIProduct of the provided artifact id
     * @throws APIManagementException
     */
    ApiTypeWrapper getAPIorAPIProductByUUID(String uuid, String organization) throws APIManagementException;

    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid          Registry artifact id
     * @param organization  Identifier of an organization
     * @return API of the provided artifact id
     * @throws APIManagementException
     */
    API getLightweightAPIByUUID(String uuid, String organization) throws APIManagementException;

    /**
     * Checks the Availability of given APIIdentifier
     *
     * @param identifier APIIdentifier
     * @param organization Organization
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API availability
     */
    boolean isAPIAvailable(APIIdentifier identifier, String organization) throws APIManagementException;

    /**
     * Checks the Availability of given APIProductIdentifier
     *
     * @param identifier APIProductIdentifier
     * @param organization API Product organization
     * @return true, if already exists. False, otherwise
     * @throws APIManagementException if failed to get API Product availability
     */
    boolean isAPIProductAvailable(APIProductIdentifier identifier, String organization) throws APIManagementException;

    /**
     * Checks whether the given API context is already registered in the system
     *
     * @param context A String representing an API context
     * @param organization Organization
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isContextExist(String context, String organization) throws APIManagementException;


    /**
     * Checks whether the given API Product context is already registered in the system for API products
     *
     * @param context A String representing an API product context
     * @param contextWithVersion A String representing an API context appended with the version
     * @param organization Organization
     * @return true if the context already exists and false otherwise
     * @throws APIManagementException if failed to check the context availability
     */
    boolean isContextExistForAPIProducts(String context, String contextWithVersion, String organization)
            throws APIManagementException;

    /**

    /**
     * Checks whether the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @param organization Organization
     * @return true if the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the api name availability
     */
    boolean isApiNameExist(String apiName, String organization) throws APIManagementException;


    /**
     * Checks whether a different letter case of the given API name is already registered in the system
     *
     * @param apiName A String representing an API name
     * @param organization Organization
     * @return true if a different letter case of the api name already exists and false otherwise
     * @throws APIManagementException if failed to check the different letter case api name availability
     */
    boolean isApiNameWithDifferentCaseExist(String apiName, String organization) throws APIManagementException;

    /**
     * Returns a set of API versions for the given provider and API name
     *
     * @param providerName name of the provider (common)
     * @param apiName      name of the api
     * @param organization organization
     * @return Set of version strings (possibly empty)
     * @throws APIManagementException if failed to get version for api
     */
    Set<String> getAPIVersions(String providerName, String apiName, String organization) throws APIManagementException;

    /**
     * Get graphql schema definition
     * @param apiId  ID of the API
     * @param orgId  Identifier of an organization
     * @return
     * @throws APIManagementException
     */
    String getGraphqlSchemaDefinition(String apiId, String orgId) throws APIManagementException;

    /**
     * Returns the OpenAPI definition as a string
     *
     * @param apiId         ID of the API
     * @param organization  Identifier of an organization
     * @return swagger string
     * @throws APIManagementException
     */
    String getOpenAPIDefinition(String apiId, String organization) throws APIManagementException;

    /**
     * Returns the async-api v2.0 definition as a string
     *
     * @param apiId id of the APIIdentifier
     * @param organization  identifier of the organization
     * @return async specification string
     * @throws APIManagementException
     */
    String getAsyncAPIDefinition(String apiId, String organization) throws APIManagementException;

    /**
     * Returns a list of documentation attached to a particular API
     *
     * @param uuid id of the api
     * @param organization  Identifier of an organization
     * @return List<Documentation>
     * @throws APIManagementException if failed to get Documentations
     */
    List<Documentation> getAllDocumentation(String uuid, String organization) throws APIManagementException;

    /**
     * Get a documentation by artifact Id
     *
     * @param apiId         apiId
     * @param docId         DocumentID
     * @param organization  Identifier of the organization
     * @return Documentation
     * @throws APIManagementException if failed to get Documentation
     */
    Documentation getDocumentation(String apiId, String docId, String organization)
            throws APIManagementException;

    /**
     * Get a documentation Content by apiid and doc id
     *
     * @param apiId         ID of the API
     * @param docId         DocumentID
     * @param organization  Identifier of an organization
     * @return DocumentationContent
     * @throws APIManagementException if failed to get Documentation
     */
    DocumentationContent getDocumentationContent(String apiId, String docId, String organization)
            throws APIManagementException;

    /**
     * Returns the GraphqlComplexityInfo object for a given API ID
     *
     * @param  apiId UUID of the API
     * @return GraphqlComplexityInfo object
     * @throws APIManagementException if failed to retrieve complexity details of the given API
     */
    GraphqlComplexityInfo getComplexityDetails(String apiId) throws APIManagementException;


    /**
     * Add or Updates the complexity details given the GraphqlComplexityInfo object
     *
     * @param uuid         API uuid
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @throws APIManagementException if failed to update complexity details
     */
    void addOrUpdateComplexityDetails(String uuid, GraphqlComplexityInfo graphqlComplexityInfo)
            throws APIManagementException;


    /**
     * Retrieves the subscriber from the given access token
     *
     * @param  accessToken Subscriber key
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from access token
     */
    Subscriber getSubscriberById(String accessToken) throws APIManagementException;

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param  uuid UUID of Subscription
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
     * Creates a new subscriber without default application given the username and the grouping Id
     *
     * @param username   Username of the subscriber to be added
     * @param groupingId - the groupId to which the subscriber belongs to
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed add subscriber
     */
    void addSubscriberOnly(String username, String groupingId) throws APIManagementException;

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
     * Retrieves the icon image associated with a particular API as a stream.
     *
     * @param apiId ID representing the API
     * @param orgId  Identifier of an organization
     * @return an Icon containing image content and content type information
     * @throws APIManagementException if an error occurs while retrieving the image
     */
    ResourceFile getIcon(String apiId, String orgId) throws APIManagementException;

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
     * Returns the corresponding application given the uuid. It will also contain keys of the application
     *
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getApplicationByUUID(String uuid, String tenantDomain) throws APIManagementException;

    /**
     * Returns the corresponding application given the uuid. The returned application will not contain key information
     *
     * @param uuid uuid of the Application
     * @return it will return Application corresponds to the uuid provided.
     * @throws APIManagementException
     */
    Application getLightweightApplicationByUUID(String uuid) throws APIManagementException;


    /**
     * Return subscribed APIs per access token
     *
     * @param accessToken
     * @return
     * @throws APIManagementException
     */
    Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException;

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
     * @param apiName API name
     * @param scopeKey   candidate scope key
     * @param organization   organization
     * @return true if the scope key is already attached as a local scope in any API
     * @throws APIManagementException if failed to check the local scope availability
     */
    boolean isScopeKeyAssignedLocally(String apiName, String scopeKey, String organization) throws APIManagementException;

    /**
     * Check if a given context template already exists in an organization
     *
     * @param contextTemplate - The contextTemplate to be checked for
     *                        <p>
     *                        Ex: /foo/{version}/bar
     *                        </p>
     * @param orgId  identifier of the organization
     * @return boolean - true if the template exists, false otherwise.
     * @throws APIManagementException - If an error occurs while checking the value in the APIM DB.
     */
    boolean isDuplicateContextTemplateMatchingOrganization(String contextTemplate, String orgId) throws APIManagementException;

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
     * Returns the mediation policy name specify inside mediation config
     *
     * @param config mediation config content
     * @return name of the mediation policy or null
     */
    String getMediationNameFromConfig(String config);

    /**
     * Returns a list of api versions that matches the given context template
     *
     * @param apiName           API name in the payload
     * @param username          User Name
     * @param organization      Organization Identifier
     * @return api versions that matches context template
     * @throws APIManagementException If failed to get the list of api versions
     */
    List<String> getApiVersionsMatchingApiNameAndOrganization(String apiName, String username, String organization)
            throws APIManagementException;

    /**
     * Get the provider of a given API or set of API Revisions, given the API name and the organization
     *
     * @param name , name of the API
     * @param organization
     * @return String APIProvider
     * @throws APIManagementException if failed to get set of API
     */
    String getAPIProviderByNameAndOrganization(String name, String organization) throws APIManagementException;

    /**
     * Returns the wsdl content in registry specified by the wsdl name. If it is a single WSDL, the content will be
     * returned as String or if it is an archive, an InputStream pointed to the content will be returned.
     *
     * @param apiId         ID of the API
     * @param organization  Organization of the API
     * @return wsdl content matching name if exist else throws an APIManagementException
     */
    ResourceFile getWSDL(String apiId, String organization) throws APIManagementException;

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
     * Returns resource list of the api product
     *
     * @param productIdentifier
     * @return
     * @throws APIManagementException
     */
    List<APIProductResource> getResourcesOfAPIProduct(APIProductIdentifier productIdentifier)
            throws APIManagementException;

    /**
     * @param searchQuery search query. ex : provider:admin
     * @param organization Identifier of an organization
     * @param start starting number
     * @param end ending number
     * @return
     * @throws APIManagementException
     */
    Map<String, Object> searchPaginatedAPIs(String searchQuery, String organization, int start, int end)
            throws APIManagementException;

    /**
     * Search in content of apis, api products and documents and provide the results
     * @param searchQuery search query
     * @param orgId Identifier of an organization
     * @param start
     * @param end
     * @return
     * @throws APIManagementException
     */
    Map<String, Object> searchPaginatedContent(String searchQuery, String orgId, int start, int end)
            throws APIManagementException;
}
