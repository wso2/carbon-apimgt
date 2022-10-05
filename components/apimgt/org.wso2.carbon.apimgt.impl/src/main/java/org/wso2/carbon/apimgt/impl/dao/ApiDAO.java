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
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;


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

}
