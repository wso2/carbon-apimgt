/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.dto.*;
import org.wso2.carbon.apimgt.impl.dao.dto.Documentation;
import org.wso2.carbon.apimgt.impl.dao.dto.ResourceFile;
import org.wso2.carbon.apimgt.impl.dao.exceptions.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provides access to API data layer
 */
public interface ApiDAO {

    /**
     * Add API metadata.
     *
     * @param organization Organization the API Owned
     * @param api      API to add
     * @return API Id of the successfully added API
     * @throws APIManagementException if fails to add API
     */
    int addAPI(Organization organization, API api) throws APIManagementException;

    /**
     * Update API metadata.
     *
     * @param api API Object to be updated
     * @param username  User who is updating API
     * @throws APIManagementException if fails to update API Metadata
     */
    void updateAPI(API api, String username) throws APIManagementException;

    /**
     * Update API Artifact.
     *
     * @param organization Organization the API Owned
     * @param publisherAPI      API to be updated
     * @return API Object of the successfully added API
     * @throws APIManagementException if fails to update API Artifact
     */
    PublisherAPI updateAPIArtifact(Organization organization, PublisherAPI publisherAPI) throws APIManagementException;

    /**
     * Delete API.
     *
     * @param organization Organization the API Owned
     * @param apiUUID      API UUID of the API to be deleted
     * @throws APIManagementException if fails to add API
     */
    void deleteAPI(Organization organization, String apiUUID) throws APIManagementException;

    /**
     * Get the API information stored in database which is used for publisher operations
     *
     * @param organization   Organization the API is owned by
     * @param apiUUID API ID
     * @return API information
     * @throws APIManagementException
     */
    PublisherAPI getPublisherAPI(Organization organization, String apiUUID) throws APIManagementException;

    /**
     * Search APIs to be displayed on Publisher API listing
     *
     * @param organization         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @param sortBy      sort criteria
     * @param sortOrder       sort order
     * @return Publisher API Search Result
     * @throws APIManagementException
     */
    PublisherAPISearchResult searchAPIsForPublisher(Organization organization, String searchQuery, int start,
                                                    int offset, UserContext ctx, String sortBy, String sortOrder) throws APIManagementException;

    /**
     * Save the passed WSDL schema definition of the API.  This includes initial creation operation and later
     * update operations
     *
     * @param org              Organization the WSDL is owned by
     * @param apiId            API ID
     * @param wsdlResourceFile WSDL Resource File
     * @throws WSDLPersistenceException
     */
    void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile) throws WSDLPersistenceException;

    /**
     * Get the WSDL schema definition
     *
     * @param org   Organization the WSDL is owned by
     * @param apiId API ID
     * @return WSDL schema definition
     * @throws WSDLPersistenceException
     */
    ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException;

    /**
     * Save OAS Schema definition
     *
     * @param org           Organization the OAS definnition is owned by
     * @param apiId         API ID
     * @param apiDefinition API OAS definition
     * @throws OASPersistenceException
     */
    void saveOASDefinition(Organization org, String apiId, String apiDefinition) throws OASPersistenceException;
    /**
     * Get OAS Schema definition of the API
     *
     * @param org   Organization the OAS definition is owned by
     * @param apiId API ID
     * @return OAS Schema definition
     * @throws OASPersistenceException
     */
    String getOASDefinition(Organization org, String apiId) throws OASPersistenceException;

    /**
     * Save Async API definition
     *
     * @param org           Organization the Async API definition is owned by
     * @param apiId         API ID
     * @param apiDefinition Async API definition
     * @throws AsyncSpecPersistenceException
     */
    void saveAsyncDefinition(Organization org, String apiId, String apiDefinition) throws AsyncSpecPersistenceException;

    /**
     * Get Async API definition
     *
     * @param org   Organization the definition is owned by
     * @param apiId API ID
     * @return Async definition
     * @throws AsyncSpecPersistenceException
     */
    String getAsyncDefinition(Organization org, String apiId) throws AsyncSpecPersistenceException;

    /**
     * Save GraphQL schema definition. This includes initial creation operation and later update operations.
     *
     * @param org              Organization the GraphQL definition is owned by
     * @param apiId            API ID
     * @param schemaDefinition GraphQL definition of API
     * @throws GraphQLPersistenceException
     */
    void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition)
            throws GraphQLPersistenceException;

    /**
     * Get GraphQL schema definition
     *
     * @param org   Organization the GraphQL definition is owned by
     * @param apiId API ID
     * @return GraphQL schema definition
     * @throws GraphQLPersistenceException
     */
    String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException;

    /**
     * Add API Revision
     *
     * @param apiRevision API Revision Object to be added
     * @throws APIManagementException
     */
    void addAPIRevision(APIRevision apiRevision) throws APIManagementException;

    /**
     * Restore API Revision
     *
     * @param apiRevision API Revision Object to be restored
     * @throws APIManagementException
     */
    void restoreAPIRevision(APIRevision apiRevision) throws APIManagementException;

    /**
     * Delete API Revision
     *
     * @param apiRevision API Revision Object to be deleted
     * @throws APIManagementException
     */
    void deleteAPIRevision(APIRevision apiRevision) throws APIManagementException;

    /**
     * Add documentation to API
     *
     * @param organization           Organization the documentation is owned by
     * @param apiUUID         API ID
     * @param documentation Documentation
     * @return ID of the documentation added
     * @throws DocumentationPersistenceException
     */
    Documentation addDocumentation(Organization organization, String apiUUID, Documentation documentation)
            throws DocumentationPersistenceException;

    /**
     * Update API documentation
     *
     * @param organization           Organization the documentation is owned by
     * @param apiUUID         API ID
     * @param documentation Documentation to update
     * @throws DocumentationPersistenceException
     */
    Documentation updateDocumentation(Organization organization, String apiUUID, Documentation documentation)
            throws DocumentationPersistenceException;

    /**
     * Get API Documentation
     *
     * @param organization   Organization the documentation is owned by
     * @param apiUUID API ID
     * @param docUUID Documentation ID
     * @return Documentation
     * @throws DocumentationPersistenceException
     */
    Documentation getDocumentation(Organization organization, String apiUUID, String docUUID)
            throws DocumentationPersistenceException;

    /**
     * Get the content (Inline text/Markdown content text/ Resource file) of API documentation
     *
     * @param organization   Organization the documentation is owned by
     * @param apiUUID API ID
     * @param docUUID Documentation ID
     * @return Documentation Content
     * @throws DocumentationPersistenceException
     */
    DocumentContent getDocumentationContent(Organization organization, String apiUUID, String docUUID)
            throws DocumentationPersistenceException;

    /**
     * Search documentation of the given API
     *
     * @param organization   Organization the documentations are owned by
     * @param apiUUID API ID
     * @return Documentation search result
     * @throws DocumentationPersistenceException
     */
    DocumentSearchResult searchDocumentation(Organization organization, String apiUUID, int start, int offset,
                                             String searchQuery, UserContext ctx) throws DocumentationPersistenceException;

    /**
     * Add the content (Inline text/Markdown content text/ Resource file) of API documentation
     *
     * @param organization       Organization the documentation is owned by
     * @param apiUUID     API ID
     * @param docUUID     Documentation ID
     * @param documentContent   Content
     * @return Documentation Content
     * @throws DocumentationPersistenceException
     */
    DocumentContent addDocumentationContent(Organization organization, String apiUUID, String docUUID, DocumentContent documentContent)
            throws DocumentationPersistenceException;

    /**
     * Delete API documentation
     *
     * @param organization   Organization the documentation is owned by
     * @param apiUUID API ID
     * @param docUUID Documentation ID
     * @throws DocumentationPersistenceException
     */
    void deleteDocumentation(Organization organization, String apiUUID, String docUUID) throws DocumentationPersistenceException;

    /**
     * Search based on content to display on publisher
     *
     * @param organization         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @return Publisher  Search Result
     * @throws APIPersistenceException
     */
    PublisherContentSearchResult searchContentForPublisher(Organization organization, String searchQuery, int start, int offset,
                                                           UserContext userContext) throws APIPersistenceException;

    /**
     * Save Thumbnail icon of the API. This includes both the initial creation and later update operations.
     *
     * @param organization          Organization the thumbnail icon is owned by
     * @param apiUUID        API ID
     * @param resourceFile
     * @throws ThumbnailPersistenceException
     */
    void saveThumbnail(Organization organization, String apiUUID, ResourceFile resourceFile) throws ThumbnailPersistenceException;

    /**
     * Get thumbnail icon of the API
     *
     * @param organization   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @return Thumbnail icon resource file
     * @throws ThumbnailPersistenceException
     */
    ResourceFile getThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException;

    /**
     * Delete thumbnail icon of the API
     *
     * @param organization   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @throws ThumbnailPersistenceException
     */
    void deleteThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException;

    /**
     * Search APIs to be displayed on Dev Portal API listing
     *
     * @param organization         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      search offset
     * @return Dev Portal API Search Result
     * @throws APIPersistenceException
     */
    DevPortalAPISearchResult searchAPIsForDevPortal(Organization organization, String searchQuery, int start, int offset,
                                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Search based on content to display on publisher
     *
     * @param organization         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @return Publisher  Search Result
     * @throws APIPersistenceException
     */
    DevPortalContentSearchResult searchContentForDevPortal(Organization organization, String searchQuery, int start,
                                                           int offset, UserContext ctx) throws APIPersistenceException;

    /**
     * Get the API information stored which is used for DevPortal operations
     *
     * @param organization   Organization the API is owned by
     * @param apiId API ID
     * @return
     * @throws APIPersistenceException
     */
    DevPortalAPI getDevPortalAPI(Organization organization, String apiId) throws APIPersistenceException;

    /**
     * Add URI Templates to database with resource scope mappings.
     *
     * @param apiId    API Id
     * @param api      API to add URI templates of
     * @param tenantId Tenant ID
     * @throws APIManagementException If an error occurs while adding URI templates.
     */
    void addURITemplates(int tenantId, int apiId, API api) throws APIManagementException;

    /**
     * Get API Context using a new DB connection.
     *
     * @param uuid API uuid
     * @return API Context
     * @throws APIManagementException if an error occurs
     */
    String getAPIContext(String uuid) throws APIManagementException;

    /**
     * Get API Default Version using a new DB connection.
     *
     * @param apiId API Identifier
     * @return API Default Version
     * @throws APIManagementException if an error occurs
     */
    String getDefaultVersion(APIIdentifier apiId) throws APIManagementException;

    /**
     * Get Published API Default Version using a new DB connection.
     *
     * @param apiId API Identifier
     * @return API Default Version
     * @throws APIManagementException if an error occurs
     */
    String getPublishedDefaultVersion(APIIdentifier apiId) throws APIManagementException;

    /**
     * Get Lightweight API Information.
     *
     * @param organization Organization of the API
     * @param apiIdentifier API Identifier
     * @return API Object
     * @throws APIManagementException if an error occurs
     */
    API getLightWeightAPIInfoByAPIIdentifier(String organization, APIIdentifier apiIdentifier)
            throws APIManagementException;

    /**
     * Get API ID by API UUID.
     *
     * @param uuid API UUID
     * @return API ID
     * @throws APIManagementException if an error occurs
     */
    int getAPIID(String uuid) throws APIManagementException;

    /**
     * Get count of the revisions created for a particular API.
     *
     * @return revision count
     * @throws APIManagementException if an error occurs while retrieving revision count
     */
    int getRevisionCountByAPI(String apiUUID) throws APIManagementException;

    /**
     * Get most recent revision id of the revisions created for a particular API.
     *
     * @return revision id
     * @throws APIManagementException if an error occurs while retrieving revision id
     */
    int getMostRecentRevisionId(String apiUUID) throws APIManagementException;

    /**
     * Get revision details by providing revision UUID
     *
     * @return revision object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    APIRevision getRevisionByRevisionUUID(String revisionUUID) throws APIManagementException;

    /**
     * Get revision UUID providing revision number
     *
     * @param revisionNum Revision number
     * @param apiUUID     UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    String getRevisionUUID(String revisionNum, String apiUUID) throws APIManagementException;

    /**
     * Get revision UUID providing revision number and organization
     *
     * @param revisionNum   Revision number
     * @param apiUUID       UUID of the API
     * @param organization  organization ID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    String getRevisionUUIDByOrganization(String revisionNum, String apiUUID, String organization) throws APIManagementException;

    /**
     * Get the earliest revision UUID from the revision list for a given API
     *
     * @param apiUUID UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    String getEarliestRevision(String apiUUID) throws APIManagementException;

    /**
     * Get the latest revision UUID from the revision list for a given API
     *
     * @param apiUUID UUID of the API
     * @return UUID of the revision
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    String getLatestRevisionUUID(String apiUUID) throws APIManagementException;

    /**
     * Get revision details by providing revision UUID
     *
     * @return revisions List object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    List<APIRevision> getRevisionsListByAPIUUID(String apiUUID) throws APIManagementException;

    /**
     * Get APIRevisionDeployment details by providing API uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    List<APIRevisionDeployment> getAPIRevisionDeploymentByApiUUID(String apiUUID) throws APIManagementException;

    /**
     * Get APIRevisionDeployment details by providing ApiUUID
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    List<APIRevisionDeployment> getAPIRevisionDeploymentsByApiUUID(String apiUUID) throws APIManagementException;

    /**
     * Remove an API revision Deployment mapping record to the database
     *
     * @param apiUUID          uuid of the revision
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void removeAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException;

    /**
     * Adds an API revision Deployment mapping record to the database
     *
     * @param apiRevisionId          uuid of the revision
     * @param apiRevisionDeployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void addAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException;

    /**
     * Update Default API Published Version
     *
     * @param identifier APIIdentifier
     * @throws APIManagementException if an error occurs when updating
     */
    void updateDefaultAPIPublishedVersion(APIIdentifier identifier)
            throws APIManagementException;

    /**
     * Get DeployedAPIRevision details by providing ApiUUID
     *
     * @return List<DeployedAPIRevision> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    List<DeployedAPIRevision> getDeployedAPIRevisionByApiUUID(String apiUUID) throws APIManagementException;

    /**
     * Remove an deployed API revision in the database
     *
     * @param apiUUID     uuid of the revision
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void removeDeployedAPIRevision(String apiUUID, Set<DeployedAPIRevision> deployments)
            throws APIManagementException;

    /**
     * Adds an deployed API revision to the database
     *
     * @param deployedAPIRevisionList content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void addDeployedAPIRevision(String apiRevisionId, List<DeployedAPIRevision> deployedAPIRevisionList)
            throws APIManagementException;

    /**
     * Update API revision Deployment mapping record
     *
     * @param apiUUID     API UUID
     * @param deployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void updateAPIRevisionDeployment(String apiUUID, Set<APIRevisionDeployment> deployments)
            throws APIManagementException;

    /**
     * Get APIRevisionDeployment details by providing deployment name and revision uuid
     *
     * @return APIRevisionDeployment object
     * @throws APIManagementException if an error occurs while retrieving revision details
     */
    APIRevisionDeployment getAPIRevisionDeploymentByNameAndRevsionID(String name, String revisionId) throws APIManagementException;

    /**
     * Get APIRevisionDeployment details by providing revision uuid
     *
     * @return List<APIRevisionDeployment> object
     * @throws APIManagementException if an error occurs while retrieving revision deployment mapping details
     */
    List<APIRevisionDeployment> getAPIRevisionDeploymentByRevisionUUID(String revisionUUID) throws APIManagementException;

    /**
     * Remove an API revision Deployment mapping record to the database
     *
     * @param apiRevisionId          uuid of the revision
     * @param apiRevisionDeployments content of the revision deployment mapping objects
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void removeAPIRevisionDeployment(String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeployments)
            throws APIManagementException;

    /**
     * Retrieve basic information about the given API by the UUID quering only from AM_API
     *
     * @param apiId UUID of the API
     * @return basic information about the API
     * @throws APIManagementException error while getting the API information from AM_API
     */
    APIInfo getAPIInfoByUUID(String apiId) throws APIManagementException;

    /**
     * Get the set of URI templates that have Operation policies
     *
     * @param apiUUID Unique Identifier of API
     * @return URITemplate set
     * @throws APIManagementException
     */
    Set<URITemplate> getURITemplatesWithOperationPolicies(String apiUUID) throws APIManagementException;

    /**
     * Add a new API specific operation policy to the database
     *
     * @param apiUUID      Unique Identifier of API
     * @param revisionUUID Unique Identifier of API revision
     * @param policyData   Unique Identifier of API
     * @return UUID of the newly created shared policy
     * @throws APIManagementException
     */
    String addAPISpecificOperationPolicy(String apiUUID, String revisionUUID, OperationPolicyData policyData)
            throws APIManagementException;

    /**
     * Add a new common operation policy to the database. This will first add the operation policy content to the
     * AM_OPERATION_POLICY table and another entry to AM_COMMON_OPERATION_POLICY table.
     *
     * @param policyData Operation policy data.
     * @return UUID of the newly created shared policy
     * @throws APIManagementException
     */
    String addCommonOperationPolicy(OperationPolicyData policyData) throws APIManagementException;

    /**
     * Retrieve an API Specific operation policy by providing the policy name. In order to narrow down the specific policy
     * this needs policy name, apiUUID, api revision UUID (if exists) and organization. If revision UUID is not provided,
     * that means the policy is not a revisioned policy.
     *
     * @param policyName             Policy name
     * @param apiUUID                UUID of API
     * @param revisionUUID           UUID of API revision
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    OperationPolicyData getAPISpecificOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                                  String apiUUID, String revisionUUID,
                                                                  String organization, boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Retrieve a common operation policy by providing the policy name and organization
     *
     * @param policyName             Policy name
     * @param policyVersion          Policy version
     * @param organization           Organization name
     * @param isWithPolicyDefinition Include the policy definition to the output or not
     * @return operation policy
     * @throws APIManagementException
     */
    OperationPolicyData getCommonOperationPolicyByPolicyName(String policyName, String policyVersion,
                                                             String organization, boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Update an existing operation policy
     *
     * @param policyId   Shared policy UUID
     * @param policyData Updated policy definition
     * @throws APIManagementException
     */
    void updateOperationPolicy(String policyId, OperationPolicyData policyData) throws APIManagementException;

    /**
     * Get the list of all operation policies. If the API UUID is provided, this will return all the operation policies
     * for that API. If not, it will return the common operation policies which are not bound to any API.
     * This list will include policy specification of each policy and policy ID. It will not contain the
     * policy definition as it is not useful for the operation.
     *
     * @param apiUUID      UUID of the API if exists. Null for common operation policies
     * @param organization Organization name
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    List<OperationPolicyData> getLightWeightVersionOfAllOperationPolicies(String apiUUID,
                                                                          String organization)
            throws APIManagementException;

    /**
     * Delete an operation policy by providing the policy UUID
     *
     * @param policyId UUID of the policy to be deleted
     * @return True if deleted successfully
     * @throws APIManagementException
     */
    void deleteOperationPolicyByPolicyId(String policyId) throws APIManagementException;

    /**
     * Get a provided api uuid is in the revision db table
     *
     * @return String apiUUID
     * @throws APIManagementException if an error occurs while checking revision table
     */
    APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException;

    /**
     * Get All API Usage by given provider
     *
     * @param providerName Name of the provider
     * @return UserApplicationAPIUsage of given provider
     * @throws APIManagementException if failed to get UserApplicationAPIUsage for given provider
     */
    UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException;

    /**
     * Get API Identifier by the API UUID.
     *
     * @param uuid uuid of the API
     * @return API Identifier
     * @throws APIManagementException if an error occurs
     */
    APIIdentifier getAPIIdentifierFromUUID(String uuid) throws APIManagementException;

    /**
     * Get All API Usage by given provider and API UUID
     *
     * @param uuid API uuid
     * @param organization Organization of the API
     * @return UserApplicationAPIUsage of given provider
     * @throws APIManagementException if failed to get UserApplicationAPIUsage for given provider
     */
    UserApplicationAPIUsage[] getAllAPIUsageByProviderAndApiId(String uuid, String organization)
            throws APIManagementException;

    /**
     * Get All API Product Usage by given provider
     *
     * @param providerName Name of the provider
     * @return UserApplicationAPIUsage of given provider
     * @throws APIManagementException if failed to get UserApplicationAPIUsage for given provider
     */
    UserApplicationAPIUsage[] getAllAPIProductUsageByProvider(String providerName) throws APIManagementException;

    /**
     * Get All Subscribers per given API
     *
     * @param identifier API Identifier
     * @return Subscribers of given API
     * @throws APIManagementException
     */
    Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException;

    /**
     * Get all subscriptions of a given API
     *
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param provider   Name of API creator
     * @return All subscriptions of a given API
     * @throws APIManagementException
     */
    List<SubscribedAPI> getSubscriptionsOfAPI(String apiName, String apiVersion, String provider)
            throws APIManagementException;

    /**
     * Get All Subscribers count per given API
     *
     * @param identifier API Identifier
     * @return Subscribers Count of given API
     * @throws APIManagementException
     */
    long getAPISubscriptionCountByAPI(Identifier identifier) throws APIManagementException;

    /**
     * Get details of the subscription block condition by condition value and tenant domain
     *
     * @param conditionValue condition value of the block condition
     * @param tenantDomain   tenant domain of the block condition
     * @return Block condition
     * @throws APIManagementException
     */
    BlockConditionsDTO getSubscriptionBlockCondition(String conditionValue, String tenantDomain)
            throws APIManagementException;

    /**
     * Get Product Mappings by given API
     *
     * @param api API
     * @return List of mapped API product resources
     * @throws APIManagementException
     */
    List<APIProductResource> getProductMappingsForAPI(API api) throws APIManagementException;

    /**
     * Get Key Manager Configurations by given Organization
     *
     * @param organization Organization name
     * @return List of Key Manager Configurations DTOs
     * @throws APIManagementException
     */
    List<KeyManagerConfigurationDTO> getKeyManagerConfigurationsByOrganization(String organization)
            throws APIManagementException;


    /**
     * Get the local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    Set<String> getAllLocalScopeKeysForAPI(String uuid, int tenantId) throws APIManagementException;

    /**
     * Get the versioned local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    Set<String> getVersionedLocalScopeKeysForAPI(String uuid, int tenantId) throws APIManagementException;

    /**
     * Update URI templates define for an API.
     *
     * @param api      API to update
     * @param tenantId tenant Id
     * @throws APIManagementException if fails to update URI template of the API.
     */
    void updateURITemplates(API api, int tenantId) throws APIManagementException;

    /**
     * Add Operation Policy Mapping.
     *
     * @param uriTemplates URITemplates objects
     * @throws APIManagementException if fails
     */
    void addOperationPolicyMapping(Set<URITemplate> uriTemplates) throws APIManagementException;

    /**
     * Get subscriber name using subscription ID
     *
     * @param subscriptionId
     * @return subscriber name
     * @throws APIManagementException
     */
    String getSubscriberName(String subscriptionId) throws APIManagementException;

    /**
     * Clone an operation policy to the API. This method is used to clone policy to a newly created api version.
     * Cloning a common policy to API.
     * Cloning a dependent policy of a product
     * Each of these scenarios, original APIs' policy ID will be recorded as the cloned policy ID.
     *
     * @param apiUUID      UUID of the API
     * @param operationPolicyData
     * @return cloned policyID
     * @throws APIManagementException
     **/
    String cloneOperationPolicy(String apiUUID, OperationPolicyData operationPolicyData)
            throws APIManagementException;

    /**
     * Retrieve the Unique Identifier of the Service used in API
     *
     * @param apiId    Unique Identifier of API
     * @param tenantId Tenant ID
     * @return Service Key
     * @throws APIManagementException
     */
    String retrieveServiceKeyByApiId(int apiId, int tenantId) throws APIManagementException;

    /**
     * Returns the details of all the life-cycle changes done per API or API Product
     *
     * @param uuid Unique UUID of the API or API Product
     * @return List of lifecycle events per given API or API Product
     * @throws APIManagementException if failed to copy docs
     */
    List<LifeCycleEvent> getLifeCycleEvents(String uuid) throws APIManagementException;

    /**
     * This method is used to update the subscription
     *
     * @param identifier    APIIdentifier
     * @param subStatus     Subscription Status[BLOCKED/UNBLOCKED]
     * @param applicationId Application id
     * @param organization  Organization
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to update subscriber
     */
    void updateSubscription(APIIdentifier identifier, String subStatus, int applicationId, String organization)
            throws APIManagementException;

    /**
     * This method is used to update the subscription
     *
     * @param subscribedAPI subscribedAPI object that represents the new subscription detals
     * @throws APIManagementException if failed to update subscription
     */
    void updateSubscription(SubscribedAPI subscribedAPI) throws APIManagementException;

    /**
     * Get the unversioned local scope keys set of the API.
     *
     * @param uuid API uuid
     * @param tenantId      Tenant Id
     * @return Local Scope keys set
     * @throws APIManagementException if fails to get local scope keys for API
     */
    Set<String> getUnversionedLocalScopeKeysForAPI(String uuid, int tenantId)
            throws APIManagementException;

    /**
     * Delete API.
     *
     * @param uuid API uuid
     * @throws APIManagementException if fails to delete API
     */
    void deleteAPI(String uuid) throws APIManagementException;

    /**
     * Get external APIStores details which are stored in database
     *
     * @param apiIdentifier API Identifier
     * @throws APIManagementException if failed to get external APIStores
     */
    String getLastPublishedAPIVersionFromAPIStore(APIIdentifier apiIdentifier, String storeName)
            throws APIManagementException;

    /**
     * Delete the records of external APIStore details.
     *
     * @param uuid       API uuid
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    boolean deleteExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException;

    /**
     * Update the records of external APIStore details.
     *
     * @param uuid       API uuid
     * @param apiStoreSet APIStores set
     * @throws APIManagementException
     */
    void updateExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException;

    /**
     * Store external APIStore details to which APIs successfully published
     *
     * @param uuid       API uuid
     * @param apiStoreSet APIStores set
     * @return added/failed
     * @throws APIManagementException
     */
    boolean addExternalAPIStoresDetails(String uuid, Set<APIStore> apiStoreSet)
            throws APIManagementException;

    /**
     * Return external APIStore details on successfully APIs published
     *
     * @param uuid API uuid
     * @return Set of APIStore
     * @throws APIManagementException
     */
    Set<APIStore> getExternalAPIStoresDetails(String uuid) throws APIManagementException;

    /**
     * Get API UUID by the API Identifier.
     *
     * @param identifier API Identifier
     * @param organization identifier of the organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    String getUUIDFromIdentifier(APIIdentifier identifier, String organization) throws APIManagementException;

    /**
     * Get API Product ID by the API Product Identifier.
     *
     * @param identifier API Product Identifier
     * @return int ID
     * @throws APIManagementException if an error occurs
     */
    int getAPIProductId(APIProductIdentifier identifier) throws APIManagementException;

    /**
     * Retrieve the gateway vendor of an API by providing the UUID
     *
     * @param apiId UUID of the API
     * @return gatewayVendor of the API
     * @throws APIManagementException
     */
    String getGatewayVendorByAPIUUID(String apiId) throws APIManagementException;

    /**
     * Return ids of the versions for the given name for the given provider
     *
     * @param apiName     api name
     * @param apiProvider provider
     * @return set ids
     * @throws APIManagementException
     */
    List<API> getAllAPIVersions(String apiName, String apiProvider) throws APIManagementException;

    /**
     * Retrieve URI Templates for the given API
     *
     * @param api API
     * @return Map of URITemplate with key as Method:resourcepath
     * @throws APIManagementException exception
     */
    Map<String, URITemplate> getURITemplatesForAPI(API api) throws APIManagementException;

    /**
     * Add API Product
     *
     * @param apiProduct API Product Object
     * @param organization Organization
     * @return added/failed
     * @throws APIManagementException
     */
    void addAPIProduct(APIProduct apiProduct, String organization) throws APIManagementException;

    /**
     * Add api product url mappings to DB
     * - url templeates to product mappings (resource bundling) - AM_API_PRODUCT_MAPPING
     *
     * @param productResources
     * @param organization
     * @param connection
     * @throws APIManagementException
     */
    void addAPIProductResourceMappings(List<APIProductResource> productResources, String organization,
                                       Connection connection) throws APIManagementException;

    /**
     * Delete API product and its related scopes
     *
     * @param productIdentifier product ID
     * @throws APIManagementException
     */
    void deleteAPIProduct(APIProductIdentifier productIdentifier) throws APIManagementException;

    /**
     * Get API Product UUID by the API Product Identifier and organization.
     *
     * @param identifier API Product Identifier
     * @param organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization)
            throws APIManagementException;

    /**
     * Update API Product
     *
     * @param product API Product Object
     * @param username Username
     * @return added/failed
     * @throws APIManagementException
     */
    void updateAPIProduct(APIProduct product, String username) throws APIManagementException;

    /**
     * Get Resource Paths of an API.
     *
     * @param apiId API Product Identifier
     * @return List of Resource Paths
     * @throws APIManagementException if an error occurs
     */
    List<ResourcePath> getResourcePathsOfAPI(APIIdentifier apiId) throws APIManagementException;

    /**
     * Get API UUID by passed parameters.
     *
     * @param provider Provider of the API
     * @param apiName  Name of the API
     * @param version  Version of the API
     * @param organization identifier of the organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    String getUUIDFromIdentifier(String provider, String apiName, String version, String organization)
            throws APIManagementException;

    /**
     * Get API Tier
     *
     * @param apiUUID  API UUID
     * @param revisionUUID  Revision UUID
     * @return String Tier
     * @throws APIManagementException if an error occurs
     */
    String getAPILevelTier(String apiUUID, String revisionUUID) throws APIManagementException;

    /**
     * Get API Status
     *
     * @param uuid  API UUID
     * @return String Status
     * @throws APIManagementException if an error occurs
     */
    String getAPIStatusFromAPIUUID(String uuid) throws APIManagementException;

    /**
     * Get URI Templates of API with Product Mapping
     *
     * @param uuid  API UUID
     * @return URI Templates of each mapping
     * @throws APIManagementException if an error occurs
     */
    Map<Integer, URITemplate> getURITemplatesOfAPIWithProductMapping(String uuid) throws APIManagementException;

    /**
     * Adds an API Product revision record to the database
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when adding a new API revision
     */
    void addAPIProductRevision(APIRevision apiRevision) throws APIManagementException;

    /**
     * Restore API Product revision database records as the Current API Product of an API Product
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    void restoreAPIProductRevision(APIRevision apiRevision) throws APIManagementException;

    /**
     * Delete API Product revision database records
     *
     * @param apiRevision content of the revision
     * @throws APIManagementException if an error occurs when restoring an API revision
     */
    void deleteAPIProductRevision(APIRevision apiRevision) throws APIManagementException;



}
