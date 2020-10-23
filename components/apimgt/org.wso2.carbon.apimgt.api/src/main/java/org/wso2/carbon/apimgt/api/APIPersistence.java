package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.persistence.*;
import org.wso2.carbon.apimgt.api.model.persistence.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.api.model.persistence.PublisherAPISearchResult;

import java.io.InputStream;
import java.util.List;

/**
 * This Interface defines the interface methods related to API operations and functionalities which incorporate with
 * the persistence layer
 *
 * All the below methods has below two parameters as the first and last parameter
 *
 *      1. Organization org:
 *          Organization which the subject artifact(s) is owned by. Organization represents an instance that might be in
 *          the context of new term 'Organization' or 'Tenant'. It has the attributes id, name, etc. so that it is
 *          identifiable.
 *
 *      2. UserContext ctx:
 *          UserContext represents an instance of the user, that invokes the particular method. UserContext instance
 *          carries attributes username, user's organization(s) and other user context specific properties
 */
public interface APIPersistence {

    /* ======= API =======
    =========================== */

    /**
     * Add API to the persistence layer
     *
     * @param org          Organization the API is owned by
     * @param publisherAPI API to add
     * @param ctx          User context
     * @return Added API
     * @throws APIPersistenceException
     */
    PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI, UserContext ctx) throws APIPersistenceException;

    /**
     * Update API in the persistence layer
     *
     * @param org          Organization the API is owed by
     * @param publisherAPI API to update
     * @param ctx          User context
     * @return Updated API
     * @throws APIPersistenceException
     */
    PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI, UserContext ctx) throws APIPersistenceException;

    /**
     * Get the API information stored in persistence layer, that is used for publisher operations
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return API information
     * @throws APIPersistenceException
     */
    PublisherAPI getPublisherAPI(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;

    /**
     * Get the API information stored in persistence layer, that is used for DevPortal operations
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return
     * @throws APIPersistenceException
     */
    DevPortalAPI getDevPortalAPI(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;

    /**
     * Delete API
     *
     * @param org   Organization the API is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return 'true' if API deletion is successful, else 'false'
     * @throws APIPersistenceException
     */
    boolean deleteAPI(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;

    /**
     * Search APIs to be displayed on Publisher API listing
     *
     * @param org         Organization the APIs are owned by
     * @param searchQuery search query
     * @param start       starting index
     * @param offset      offset to search
     * @param ctx         User context
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
     * @param ctx         User context
     * @return Dev Portal API Search Result
     * @throws APIPersistenceException
     */
    DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start, int offset,
                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Change API Life Cycle
     *
     * @param org    Organization the API is owned by
     * @param apiId  API ID
     * @param status status to which the API is to be updated
     * @param ctx    User context
     * @throws APIPersistenceException
     */
    void changeAPILifeCycle(Organization org, String apiId, String status, UserContext ctx)
                                    throws APIPersistenceException;



    /* =========== WSDL ============
       =========================== */

    /**
     * Save the passed WSDL schema definition of the API.  This includes initial creation operation and later
     * update operations
     *
     * @param org              Organization the WSDL is owned by
     * @param apiId            API ID
     * @param wsdlResourceFile WSDL Resource File
     * @param ctx              User context
     * @throws APIPersistenceException
     */
    void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get the WSDL shema definition
     *
     * @param org   Organization the WSDL is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return WSDL schema definition
     * @throws APIPersistenceException
     */
    ResourceFile getWSDL(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;


    /* ==== OAS API Schema Definition ====
     ================================== */

    /**
     * Save OAS Schema definition
     *
     * @param org           Organization the OAS definnition is owned by
     * @param apiId         API ID
     * @param apiDefinition API OAS definition
     * @param ctx           User context
     * @throws APIPersistenceException
     */
    void saveOASDefinition(Organization org, String apiId, String apiDefinition, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get OAS Schema definition of the API
     *
     * @param org   Organization the OAS definition is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return OAS Schema definition
     * @throws APIPersistenceException
     */
    String getOASDefinition(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;


    /* ==== GraphQL API Schema Definition ==========
    ============================================= */

    /**
     * Save GraphQL schema definition. This includes initial creation operation and later update operations.
     *
     * @param org              Organization the GraphQL definition is owned by
     * @param apiId            API ID
     * @param schemaDefinition GraphQL definition of API
     * @param ctx              User context
     * @throws APIPersistenceException
     */
    void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get GraphQL schema definition
     *
     * @param org   Organization the GraphQL definition is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return GraphQL schema definition
     * @throws APIPersistenceException
     */
    String getGraphQLSchema(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;


    /* ======= Documentation  =======
    ================================ */

    /**
     * Add documentation to API
     *
     * @param org           Organization the documentation is owned by
     * @param apiId         API ID
     * @param documentation Documentation
     * @param ctx           User context
     * @throws APIPersistenceException
     */
    void addDocumentation(Organization org, String apiId, Documentation documentation, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Update API documentation
     *
     * @param org           Organization the documentation is owned by
     * @param apiId         API ID
     * @param documentation Documentation to update
     * @param ctx           User context
     * @throws APIPersistenceException
     */
    void updateDocumentation(Organization org, String apiId, Documentation documentation, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get API Documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @param ctx   User context
     * @return
     * @throws APIPersistenceException
     */
    Documentation getDocumentation(Organization org, String apiId, String docId, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get the content of API documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @param ctx   User context
     * @return Documentation Content
     * @throws APIPersistenceException
     */
    String getDocumentationContent(Organization org, String apiId, String docId, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get all documentation of the given API
     *
     * @param org   Organization the documentations are owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return Documentation listing of the API
     * @throws APIPersistenceException
     */
    DocumentationGetResult getAllDocumentation(Organization org, String apiId, int start, int offset, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Delete API documentation
     *
     * @param org   Organization the documentation is owned by
     * @param apiId API ID
     * @param docId Documentation ID
     * @param ctx   User context
     * @return 'true' if documentation deletion is successful, else 'false'
     * @throws APIPersistenceException
     */
    boolean deleteDocumentation(Organization org, String apiId, String docId, UserContext ctx)
                                    throws APIPersistenceException;


    /* ======= Mediation Policy ========
     =================================== */

    /**
     * Add mediation policy to the API
     *
     * @param org         Organization the mediation policy is owned by
     * @param apiId       API ID
     * @param type        Type of mediation policy (i.e. In, Out, Fault)
     * @param contentFile
     * @param ctx         User context
     * @throws APIPersistenceException
     */
    void addMediationPolicy(Organization org, String apiId, String type, ResourceFile contentFile, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Update mediation policy of the API
     *
     * @param org               Organization the mediation policy is owned by
     * @param apiId             API ID
     * @param mediationPolicyId Mediation policy ID
     * @param contentFile       Mediation policy contentFile
     * @param ctx               User context
     * @throws APIPersistenceException
     */
    void updateMediationPolicy(Organization org, String apiId, String mediationPolicyId, ResourceFile contentFile,
                                    UserContext ctx) throws APIPersistenceException;

    /**
     * Get mediation policy of API
     *
     * @param org               Organization the mediation policy is owned by
     * @param apiId             API ID
     * @param mediationPolicyId Mediation policy ID
     * @param ctx               User context
     * @return Mediation Policy of API
     * @throws APIPersistenceException
     */
    Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get a list of all the mediation policies of the API
     *
     * @param org   Organization the mediation policies are owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return list of all the mediation policies of the API
     * @throws APIPersistenceException
     */
    List<MediationPolicyInfo> getAllMediationPolicies(Organization org, String apiId, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Delete a mediation policy of the API
     *
     * @param org               Organization the mediation policy is owned by
     * @param apiId             API ID
     * @param mediationPolicyId Mediation policy ID
     * @param ctx               User context
     * @return 'true' if deletion of the mediation policy is successful, else 'false'
     * @throws APIPersistenceException
     */
    boolean deleteMediationPolicy(Organization org, String apiId, String mediationPolicyId, UserContext ctx)
                                    throws APIPersistenceException;


    /* ======= Thumbnail Icon =======
    ==================================== */

    /**
     * Save Thumbnail icon of the API. This includes both the initial creation and later update operations.
     *
     * @param org             Organization the thumbnail icon is owned by
     * @param apiId           API ID
     * @param fileInputStream File input stream of the thumbnail icon
     * @param ctx             User context
     * @throws APIPersistenceException
     */
    void saveAPIThumbnail(Organization org, String apiId, InputStream fileInputStream, UserContext ctx)
                                    throws APIPersistenceException;

    /**
     * Get thumbnail icon of the API
     *
     * @param org   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return Thumbnail icon resource file
     * @throws APIPersistenceException
     */
    ResourceFile getAPIThumbnail(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;

    /**
     * Delete thumbnail icon of the API
     *
     * @param org   Organization the thumbnail icon is owned by
     * @param apiId API ID
     * @param ctx   User context
     * @return 'true' if the thumbnail icon deletion is successful, else 'false'
     * @throws APIPersistenceException
     */
    boolean deleteAPIThumbnail(Organization org, String apiId, UserContext ctx) throws APIPersistenceException;
}
