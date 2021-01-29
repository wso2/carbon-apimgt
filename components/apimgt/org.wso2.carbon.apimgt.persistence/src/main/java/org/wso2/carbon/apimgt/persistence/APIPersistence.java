/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProduct;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIProductSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.PublisherContentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.ThumbnailPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.WSDLPersistenceException;

import java.util.List;

/**
 * This Interface defines the interface methods related to API operations and functionalities which incorporate with
 * the persistence layer
 * <p>
 * All the below methods has the 'Organization org' parameters as the first parameter. This implies the Organization
 * which the subject artifact(s) is owned by. Organization represents an instance that might be in the context of new
 * term 'Organization' or 'Tenant'. It has the attributes id, name, etc. so that it is
 * identifiable.
 */
public interface APIPersistence {

    /* ======= API =======
    =========================== */

    /**
     * Add API to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param publisherAPI API to add
     * @return ID of Added API
     * @throws APIPersistenceException
     */
    PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException;

    /**
     * Add API Revision to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param apiUUID API UUID
     * @param revisionId API Revision ID
     * @return ID of Added API
     * @throws APIPersistenceException
     */
    String addAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException;

    /**
     * Add API Revision to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param apiUUID API UUID
     * @param revisionId API Revision ID
     * @throws APIPersistenceException
     */
    void restoreAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException;

    /**
     * Add API Revision to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param apiUUID API UUID
     * @param revisionId API Revision ID
     * @throws APIPersistenceException
     */
    void deleteAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException;

    /**
     * Update API in the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param publisherAPI API to update
     * @throws APIPersistenceException
     */
    PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException;

    /**
     * Get the API information stored in persistence layer, that is used for publisher operations
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @return API information
     * @throws APIPersistenceException
     */
    PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException;

    /**
     * Get the API information stored in persistence layer, that is used for DevPortal operations
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @return
     * @throws APIPersistenceException
     */
    DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException;

    /**
     * Delete API
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @throws APIPersistenceException
     */
    void deleteAPI(Organization org, String apiId) throws APIPersistenceException;

    /**
     * Search APIs to be displayed on Publisher API listing
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @return Publisher API Search Result
     * @throws APIPersistenceException
     */
    PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start, int offset,
                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Search APIs to be displayed on Dev Portal API listing
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      search offset
     * @return Dev Portal API Search Result
     * @throws APIPersistenceException
     */
    DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start, int offset,
                                    UserContext ctx) throws APIPersistenceException;
    
    /**
     * Search based on content to display on publisher
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @return Publisher  Search Result
     * @throws APIPersistenceException
     */
    PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start, int offset,
                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Search based on content to display on dev portal
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      search offset
     * @return Dev Portal API Search Result
     * @throws APIPersistenceException
     */
    DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start, int offset,
                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Change API Life Cycle
     *
     * @param org    Organization the API is owned by
     * @param apiId  API ID
     * @param status status to which the API is to be updated
     * @throws APIPersistenceException
     */
    void changeAPILifeCycle(Organization org, String apiId, String status) throws APIPersistenceException;

    /* =========== WSDL ============
       =========================== */

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

    /* ======= Documentation  =======
    ================================ */

    /**
     * Add documentation to API
     *
     * @param org           Organization the documentation is owned by
     * @param apiId         API ID
     * @param documentation Documentation
     * @return ID of the documentation added
     * @throws DocumentationPersistenceException
     */
    Documentation addDocumentation(Organization org, String apiId, Documentation documentation)
                                    throws DocumentationPersistenceException;

    /**
     * Update API documentation
     *
     * @param org           Organization the documentation is owned by
     * @param apiId         API ID
     * @param documentation Documentation to update
     * @throws DocumentationPersistenceException
     */
    Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
                                    throws DocumentationPersistenceException;

    /**
     * Get API Documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @return Documentation
     * @throws DocumentationPersistenceException
     */
    Documentation getDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException;

    /**
     * Get the content (Inline text/Markdown content text/ Resource file) of API documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @return Documentation Content
     * @throws DocumentationPersistenceException
     */
    DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
                                    throws DocumentationPersistenceException;

    /**
     * Add the content (Inline text/Markdown content text/ Resource file) of API documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @param Documentation Content
     * @return Documentation Content
     * @throws DocumentationPersistenceException
     */
    DocumentContent addDocumentationContent(Organization org, String apiId, String docId, DocumentContent content)
            throws DocumentationPersistenceException;

    /**
     * Search documentation of the given API
     *
     * @param org   Organization the documentations are owned by
     * @param apiId API ID
     * @return Documentation search result
     * @throws DocumentationPersistenceException
     */
    DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset, String searchQuery,
                                    UserContext ctx) throws DocumentationPersistenceException;
    /**
     * Delete API documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @throws DocumentationPersistenceException
     */
    void deleteDocumentation(Organization org, String apiId, String docId) throws DocumentationPersistenceException;


    /* ======= Mediation Policy ========
     =================================== */

    /**
     * Add mediation policy to the API
     *
     * @param org   Organization the mediation policy is owned by
     * @param apiId API ID
     * @return Mediation policy Id
     * @throws MediationPolicyPersistenceException
     */
    Mediation addMediationPolicy(Organization org, String apiId, Mediation mediation) throws
                                    MediationPolicyPersistenceException;

    /**
     * Update mediation policy of the API
     *
     * @param org       Organization the mediation policy is owned by
     * @param apiId     API ID
     * @param mediation Mediation policy
     * @throws MediationPolicyPersistenceException
     */
    Mediation updateMediationPolicy(Organization org, String apiId, Mediation mediation) throws MediationPolicyPersistenceException;

    /**
     * Get mediation policy of API
     *
     * @param org               Organization the mediation policy is owned by
     * @param apiId             API ID
     * @param mediationPolicyId Mediation policy ID
     * @return Mediation Policy of API
     * @throws MediationPolicyPersistenceException
     */
    Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId)
                                    throws MediationPolicyPersistenceException;
    /**
     * Get a list of all the mediation policies of the API
     *
     * @param org   Organization the mediation policies are owned by
     * @param apiId API ID
     * @return list of all the mediation policies of the API
     * @throws MediationPolicyPersistenceException
     */
    List<MediationInfo> getAllMediationPolicies(Organization org, String apiId) throws MediationPolicyPersistenceException;
    /**
     * Delete a mediation policy of the API
     *
     * @param org               Organization the mediation policy is owned by
     * @param apiId             API ID
     * @param mediationPolicyId Mediation policy ID
     * @throws MediationPolicyPersistenceException
     */
    void deleteMediationPolicy(Organization org, String apiId, String mediationPolicyId) throws MediationPolicyPersistenceException;


    /* ======= Thumbnail Icon =======
    ==================================== */

    /**
     * Save Thumbnail icon of the API. This includes both the initial creation and later update operations.
     *
     * @param org          Organization the thumbnail icon is owned by
     * @param apiId        API ID
     * @param resourceFile
     * @throws ThumbnailPersistenceException
     */
    void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile) throws ThumbnailPersistenceException;

    /**
     * Get thumbnail icon of the API
     *
     * @param org   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @return Thumbnail icon resource file
     * @throws ThumbnailPersistenceException
     */
    ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException;
    /**
     * Delete thumbnail icon of the API
     *
     * @param org   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @throws ThumbnailPersistenceException
     */
    void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException;
    
    
    /**
     * Add API product to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param publisherAPIProduct API product to add
     * @return ID of Added API Product
     * @throws APIPersistenceException
     */
    PublisherAPIProduct addAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException;
    

    /**
     * Update API product to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param publisherAPIProduct API product to add
     * @return ID of Added API Product
     * @throws APIPersistenceException
     */
    PublisherAPIProduct updateAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException;
    
    /**
     * Get the API product information stored in persistence layer, that is used for publisher operations
     *
     * @param org   Organization the API is owned by
     * @param apiProductId API product ID
     * @return API information
     * @throws APIPersistenceException
     */
    PublisherAPIProduct getPublisherAPIProduct(Organization org, String apiProductId) throws APIPersistenceException;
    
    /**
     * Search API Products to be displayed on Publisher API product listing
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @return Publisher API product Search Result
     * @throws APIPersistenceException
     */
    PublisherAPIProductSearchResult searchAPIProductsForPublisher(Organization org, String searchQuery, int start,
            int offset, UserContext ctx) throws APIPersistenceException;
    
    /**
     * Delete API Product
     *
     * @param org   Organization the API product is owned by
     * @param apiId API ID
     * @throws APIPersistenceException
     */
    void deleteAPIProduct(Organization org, String apiId) throws APIPersistenceException;

}