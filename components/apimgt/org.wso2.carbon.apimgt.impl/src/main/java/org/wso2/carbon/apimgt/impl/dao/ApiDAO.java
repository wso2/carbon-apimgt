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
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides access to API data layer
 */
public interface ApiDAO {

    int addAPI(API api, int tenantId, String organization) throws APIManagementException;

    void recordAPILifeCycleEvent(String uuid, String oldStatus, String newStatus, String userId,
                                 int tenantId) throws APIManagementException;

    int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException;

    void addUpdateAPIAsDefaultVersion(API api, Connection connection) throws APIManagementException;

    PublisherAPI getPublisherAPI(Organization organization, String apiUUID) throws APIManagementException;

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

    /* ==== OAS API Schema Definition ====
     ================================== */

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

    /* ==== Async API Definition ==========
    ============================================= */

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

    /* ==== GraphQL API Schema Definition ==========
    ============================================= */

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

    void addAPIRevision(APIRevision apiRevision) throws APIManagementException;

    APIRevision checkAPIUUIDIsARevisionUUID(String apiUUID) throws APIManagementException;

    int getAPIID(String uuid) throws APIManagementException;

    PublisherAPI updateAPI(Organization organization, PublisherAPI publisherAPI) throws APIManagementException;

    void deleteAPI(Organization organization, String apiUUID) throws APIManagementException;
    Documentation addDocumentation(Organization organization, String apiUUID, Documentation documentation) throws DocumentationPersistenceException;

    Documentation updateDocumentation(Organization organization, String s, Documentation documentation) throws DocumentationPersistenceException;

    Documentation getDocumentation(Organization organization, String apiUUID, String docUUID) throws DocumentationPersistenceException;

    DocumentContent getDocumentationContent(Organization organization, String apiUUID, String docUUID) throws DocumentationPersistenceException;

    DocumentSearchResult searchDocumentation(Organization org, String apiUUID, int start, int offset,
                                             String searchQuery, UserContext ctx) throws DocumentationPersistenceException;
    DocumentContent addDocumentationContent(Organization organization, String apiUUID, String docUUID, DocumentContent documentContent) throws DocumentationPersistenceException;
    void deleteDocumentation(Organization organization, String s, String s1) throws DocumentationPersistenceException;

    PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int i, int i1, UserContext userContext) throws APIPersistenceException;

    void saveThumbnail(Organization organization, String apiId, ResourceFile resourceFile) throws ThumbnailPersistenceException;

    ResourceFile getThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException;

    void deleteThumbnail(Organization organization, String apiId) throws ThumbnailPersistenceException;


}
