/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.APILCWorkflowStatus;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;

/**
 * Provides access to API data layer
 */
public interface ApiDAO {
    /**
     * Retrieve a given instance of an API
     *
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    API getAPI(String apiID) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of an APISummary object
     *
     * @param apiID The UUID that uniquely identifies an API
     * @return valid {@link API} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    API getAPISummary(String apiID) throws APIMgtDAOException;

    /**
     * Retrieves the summary of a Composite API. Summary contains only basic information
     * of the API. To get the complete API details, use {@link #getAPI(String)}
     *
     * @param apiID ID of the Composite API
     * @return Basic information about the Composite API with {@code apiID}
     * @throws APIMgtDAOException if failed to retrieve/create API summary
     */
    @CheckForNull
    CompositeAPI getCompositeAPISummary(String apiID) throws APIMgtDAOException;

    /**
     * Retrieve a given instance of a Composite API
     *
     * @param apiID The UUID that uniquely identifies a Composite API
     * @return valid {@link CompositeAPI} object or null
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    @CheckForNull
    CompositeAPI getCompositeAPI(String apiID) throws APIMgtDAOException;

    /**
     * Retrieves a paginated list of composite APIs. Resulting APIs will be filtered for
     * PROVIDER name <code>user</code>.
     *
     * @param roles  If provided, results will be filtered for only the APIs with these roles.
     *               If not APIs with any role will be retrieved.
     * @param user   Name of the PROVIDER who owns requested set of Composite APIs
     * @param offset Page number
     *               <p>(ex: <code>offset</code> 2 with <code>limit</code> 10 will retrieve 11-20th results)</p>
     * @param limit  Number of results to be included in the resulting list
     * @return list of Composite APIs
     * @throws APIMgtDAOException if database error occurred while querying data
     */
    @CheckForNull
    List<CompositeAPI> getCompositeAPIs(Set<String> roles, String user, int offset, int limit)
            throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of an API
     *
     * @param apiId UUID of API
     * @return Last updated time of API given its uuid
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    String getLastUpdatedTimeOfAPI(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the swagger definition of an API
     *
     * @param apiId UUID of API
     * @return Last updated time of Swagger definition given the uuid of API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    String getLastUpdatedTimeOfSwaggerDefinition(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the gateway config of an API
     *
     * @param apiId UUID of API
     * @return Last updated time of gateway configuration given the uuid of API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */ 
    String getLastUpdatedTimeOfGatewayConfig(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs.
     *
     * @param roles The set of roles of the current user
     * @param user The userName of the current user
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIs(Set<String> roles, String user) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     *
     * @param statuses A list of matching life cycle statuses
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsByStatus(List<String> statuses) throws APIMgtDAOException;

    /**
     * Retrieves summary data of all available APIs with life cycle status that matches the status list provided
     * which has role based visibility
     *
     * @param roles    role list of current user
     * @param statuses status of APIs to be returned
     * @return API list
     * @throws APIMgtDAOException if failed to fetch APIs from database
     */
    List<API> getAPIsByStatus(Set<String> roles, List<String> statuses) throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria. This will use
     * the full text search for API table
     *
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param searchString The search string provided
     * @param offset  The starting point of the formatApiSearch results.
     * @param limit   Number of formatApiSearch results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> searchAPIs(Set<String> roles, String user, String searchString, int offset, int limit)
            throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available Composite APIs that match the given search criteria.
     * This will use the full text search for API table
     *
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param searchString The search string provided
     * @param offset  The starting point of the formatApiSearch results.
     * @param limit   Number of formatApiSearch results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<CompositeAPI> searchCompositeAPIs(Set<String> roles, String user, String searchString, int offset, int limit)
            throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of all available APIs that match the given search criteria.
     *
     * @param roles     List of the roles of the user.
     * @param user      Current user.
     * @param attributeMap Map containing the attributes and search queries for those attributes
     * @param offset  The starting point of the formatApiSearch results.
     * @param limit   Number of formatApiSearch results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> attributeSearchAPIs(Set<String> roles, String user, Map<String, String> attributeMap,
                                  int offset, int limit) throws APIMgtDAOException;

    /**
     * Retrieves summary of paginated data of APIs based on visibility (in store), that match the
     * given search criteria.
     *
     * @param roles List of the roles of the user.
     * @param attributeMap Map containing the attributes to be searched
     * @param offset The starting point of the search results.
     * @param limit Number of search results that will be returned.
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<API> searchAPIsByAttributeInStore(List<String> roles, Map<String, String> attributeMap,
                                           int offset, int limit) throws APIMgtDAOException;

    /**
     * Checks if a given API which is uniquely identified by the Provider, API Name and Version combination already
     * exists
     *
     * @param apiName Name of API
     * @param providerName Provider of the API.
     * @return true if providerName, apiName, version combination already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isAPINameExists(String apiName, String providerName) throws APIMgtDAOException;

    /**
     * Checks if a given API Context already exists
     *
     * @param contextName Name of API Context
     * @return true if contextName already exists else false
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isAPIContextExists(String contextName) throws APIMgtDAOException;

    /**
     * Add a new instance of an API
     *
     * @param api The {@link API} object to be added
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void addAPI(API api) throws APIMgtDAOException;

    /**
     * Create API that is associated with an Application. This is specifically required to support the creation of
     * Composite APIs which are always associated with a specific Application.
     *
     * @param api The {@link API} object to be added
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void addApplicationAssociatedAPI(CompositeAPI api) throws APIMgtDAOException;


    /**
     * Update an existing API
     *
     * @param apiID The UUID of the API that needs to be updated
     * @param substituteAPI Substitute {@link API} object that will replace the existing API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void updateAPI(String apiID, API substituteAPI) throws APIMgtDAOException;

    /**
     * Remove an existing API
     *
     * @param apiID The UUID of the API that needs to be deleted
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void deleteAPI(String apiID) throws APIMgtDAOException;

    /**
     * Delete API that is associated with an Application. This is specifically required to support the deletion of
     * Composite APIs which are always associated with a specific Application.
     *
     * @param apiId The UUID of the API that needs to be deleted
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    void deleteCompositeApi(String apiId) throws APIMgtDAOException;


    /**
     * Get swagger definition of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getApiSwaggerDefinition(String apiID) throws APIMgtDAOException;

    /**
     * Get swagger definition of a given Composite API
     *
     * @param apiID The UUID of the respective Composite API
     * @return Swagger definition String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getCompositeApiSwaggerDefinition(String apiID) throws APIMgtDAOException;

    /**
     * Get image of a given API
     *
     * @param apiID The UUID of the respective API
     * @return Image stream
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    InputStream getImage(String apiID) throws APIMgtDAOException;

    /**
     * Update image of a given API
     *
     * @param apiID    The UUID of the respective API
     * @param image    Image stream
     * @param dataType Data Type of image
     * @param updatedBy user who adds/updates the API
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateImage(String apiID, InputStream image, String dataType, String updatedBy) throws APIMgtDAOException;

    /**
     * Change the lifecycle status of a given API
     *
     * @param apiID The UUID of the respective API
     * @param status The lifecycle status that the API must be set to
     * @throws APIMgtDAOException  if error occurs while accessing data layer
     *
     */
    void changeLifeCycleStatus(String apiID, String status) throws APIMgtDAOException;

    /**
     * Return list of all Document info belonging to a given API.
     *
     * @param apiID The UUID of the respective API
     * @return {@code List<DocumentInfo>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<DocumentInfo> getDocumentsInfoList(String apiID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link DocumentInfo} DocumentInfo meta data
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    DocumentInfo getDocumentInfo(String resourceID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link InputStream} Document File content
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    InputStream getDocumentFileContent(String resourceID) throws APIMgtDAOException;

    /**
     *
     * @param resourceID The UUID of the respective resource
     * @return {@link String} Document inline content
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @CheckForNull
    String getDocumentInlineContent(String resourceID) throws APIMgtDAOException;

    /**
     * Add document info meta data to an API
     *
     * @param apiId    UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentInfo(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException;

    /**
     * Add document info meta data to an API
     *
     * @param apiId    UUID of API
     * @param documentInfo {@link DocumentInfo}
     * @param updatedBy user who performs the action 
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateDocumentInfo(String apiId, DocumentInfo documentInfo, String updatedBy) throws APIMgtDAOException;

    /**
     * Add Document File content
     *
     * @param resourceID UUID of resource
     * @param content    File content as an InputStream
     * @param dataType File mime type
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentFileContent(String resourceID, InputStream content, String dataType, String updatedBy) throws
            APIMgtDAOException;

    /**
     * Add Document Inline content
     *
     * @param resourceID UUID of resource
     * @param content    Inline content as a String
     * @param updatedBy user who updates the resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addDocumentInlineContent(String resourceID, String content, String updatedBy) throws APIMgtDAOException;

    /**
     * Delete a document
     *
     * @param resourceID   UUID of resource
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void deleteDocument(String resourceID) throws APIMgtDAOException;

    /**
     * used to deprecate older versions of the api
     *
     * @param identifier API ID.
     */
    void deprecateOlderVersions(String identifier);

    /**
     * Check if document Exist
     *
     * @param apiId UUID of the API.
     * @param documentInfo  Document.
     * @return  TRUE or false based on the existence of document.
     * @throws APIMgtDAOException   If error occurs while checking if document exits.
     */
    boolean isDocumentExist(String apiId, DocumentInfo documentInfo) throws APIMgtDAOException;


    /**
     * Add an Endpoint
     *
     * @param endpoint  Endpoint Object.
     * @throws APIMgtDAOException   If failed to add endpoint.
     */
    void addEndpoint(Endpoint endpoint) throws APIMgtDAOException;



    /**
     * Delete an Endpoint
     *
     * @param endpointId    UUID of the endpoint.
     * @return  Success value of the endpoint delete operation
     * @throws APIMgtDAOException   If failed to delete API.
     */
    boolean deleteEndpoint(String endpointId) throws APIMgtDAOException;

    /**
     * Update an Endpoint
     *
     * @param endpoint  Endpoint object.
     * @return  Success value of the endpoint update operation
     * @throws APIMgtDAOException   If failed to update API.
     */
    boolean updateEndpoint(Endpoint endpoint) throws APIMgtDAOException;

    /**
     * Get an Endpoint
     *
     * @param endpointId uuid of endpoint
     * @return  Endpoint object.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    Endpoint getEndpoint(String endpointId) throws APIMgtDAOException;

    /**
     * Get an Endpoint
     *
     * @param name name of endpoint
     * @return  Endpoint object.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    Endpoint getEndpointByName(String name) throws APIMgtDAOException;

    /**
     * get all Endpoints
     *
     * @return  List of endpoint objects.
     * @throws APIMgtDAOException   If failed to get endpoint.
     */
    List<Endpoint> getEndpoints() throws APIMgtDAOException;

    /**
     * Update swagger definition of a given API
     *
     * @param apiID             The UUID of the respective API
     * @param swaggerDefinition Swagger definition String
     * @param updatedBy user who performs the update
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateApiDefinition(String apiID, String swaggerDefinition, String updatedBy)
            throws APIMgtDAOException;

    /**
     * Checks whether an WSDL archive exists for an API.
     * 
     * @param apiId UUID of API
     * @return true if an WSDL archive exists for an API
     * @throws APIMgtDAOException If an error occurs while accessing data layer
     */
    boolean isWSDLArchiveExists(String apiId) throws APIMgtDAOException;

    /**
     * Checks whether an WSDL exists for an API.
     *
     * @param apiId UUID of API
     * @return true if an WSDL exists for an API
     * @throws APIMgtDAOException If an error occurs while accessing data layer
     */
    boolean isWSDLExists(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves the WSDL of the API
     *
     * @param apiId UUID of the API
     * @return WSDL as {@link String}
     * @throws APIMgtDAOException If error occurs while accessing the WSDL from the data layer
     */
    String getWSDL(String apiId) throws APIMgtDAOException;

    /**
     * Gets a WSDL archive content stream for an API.
     * 
     * @param apiId UUID of API
     * @return WSDL archive content of the API
     * @throws APIMgtDAOException If error occurs while accessing the WSDL from the data layer
     */
    InputStream getWSDLArchive(String apiId) throws APIMgtDAOException;

    /**
     * Add a WSDL resource to an API
     *
     * @param apiId UUID of API
     * @param wsdlContent WSDL content as byte array
     * @param createdBy the user who adds the WSDL
     * @throws APIMgtDAOException when updating the WSDL failed in DB level
     */
    void addOrUpdateWSDL(String apiId, byte[] wsdlContent, String createdBy) throws APIMgtDAOException;

    /**
     * Add WSDL archive if not exist. If Text WSDL exists, it will be removed from the DB.
     * If a WSDL archive already exists, it will be updated with the new content. 
     *
     * @param apiID api uuid
     * @param inputStream WSDL archive input stream
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException when updating the WSDL failed in DB level
     */
    void addOrUpdateWSDLArchive(String apiID, InputStream inputStream, String updatedBy) throws APIMgtDAOException;

    /**
     * Remove WSDL text resource of a particular API
     * 
     * @param apiId UUID of API
     * @throws APIMgtDAOException if error occurs while deleting the WSDL from the data layer
     */
    void removeWSDL(String apiId) throws APIMgtDAOException;

    /**
     * Remove WSDL archive of an API
     * 
     * @param apiId UUID of API
     * @throws APIMgtDAOException if error occurs while deleting the WSDL archive from the data layer
     */
    void removeWSDLArchiveOfAPI(String apiId) throws APIMgtDAOException;

    /**
     * Get gateway configuration of a given API
     *
     * @param apiID The UUID of the respective API
     * @return gateway configuration String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    String getGatewayConfigOfAPI(String apiID) throws APIMgtDAOException;

    /**
     * Get gateway configuration of a given Composite API
     *
     * @param apiID The UUID of the respective Composite API
     * @return gateway configuration String
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    InputStream getCompositeAPIGatewayConfig(String apiID) throws APIMgtDAOException;

    /**
     * update gateway config
     *
     * @param apiID         api uuid
     * @param gatewayConfig config text
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException throws if any error occurred
     */
    void updateGatewayConfig(String apiID, String gatewayConfig, String updatedBy) throws APIMgtDAOException;

    /**
     * update gateway config
     *
     * @param apiID         api uuid
     * @param gatewayConfig config text
     * @param updatedBy user who performs the action
     * @throws APIMgtDAOException throws if any error occurred
     */
    void updateCompositeAPIGatewayConfig(String apiID, InputStream gatewayConfig, String updatedBy)
            throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of a document of an API
     *
     * @param documentId UUID of document
     * @return Last updated time of document given its uuid
     * @throws APIMgtDAOException throws if any DB level error occurred
     */
    String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the content of a document of an API
     *
     * @param apiId UUID of API
     * @param documentId UUID of document
     * @return  Last updated time of document's content
     * @throws APIMgtDAOException throws if any DB level error occurred
     */
    String getLastUpdatedTimeOfDocumentContent(String apiId, String documentId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the thumbnail image of an API
     *
     * @param apiId UUID of API
     * @return  Last updated time of document's content
     * @throws APIMgtDAOException throws if any db level error occurred
     */
    String getLastUpdatedTimeOfAPIThumbnailImage(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the endpoint given its endpointId
     *
     * @param endpointId Id of the endpoint
     * @return last updated time
     * @throws APIMgtDAOException throws if any db level error occurred
     */
    String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIMgtDAOException;
    
    /**
     * Update an existing API workflow state
     *
     * @param apiID         The {@link String} of the API that needs to be updated
     * @param workflowStatus workflow status
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateAPIWorkflowStatus(String apiID, APILCWorkflowStatus workflowStatus) throws APIMgtDAOException;

    /**
     * Returns all the available labels
     *
     * @param commentId UUID of the comment
     * @param apiId UUID of the API
     * @return Comment Object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Comment getCommentByUUID(String commentId, String apiId) throws APIMgtDAOException;

    /**
     * Adds comment for a specific Api
     *
     * @param comment the comment text
     * @param apiId UUID of the Api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addComment(Comment comment, String apiId) throws APIMgtDAOException;

    /**
     *  Deletes a comment
     *
     * @param commentId UUID of the comment
     * @param apiId UUID of the api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void deleteComment(String commentId, String apiId) throws APIMgtDAOException;

    /**
     *  Updates an already existing comment
     *
     * @param comment new comment
     * @param commentId old comment uuid
     * @param apiId uuid of the api associated with the comment
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateComment(Comment comment, String commentId, String apiId) throws APIMgtDAOException;

    /**
     * Retrieves all comments for an api
     *
     * @param apiId UUID of the api
     * @return List of comments for the api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<Comment> getCommentsForApi(String apiId) throws APIMgtDAOException;

    /**
     * Retrieves last updated time of comment
     *
     * @param commentId UUID of the comment
     * @return Last updated time of comment
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    String getLastUpdatedTimeOfComment(String commentId) throws APIMgtDAOException;

    /**
     * return list of resources associated with API
     *
     * @param apiContext context of API
     * @param apiVersion version of API
     * @return list of resources
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<UriTemplate> getResourcesOfApi(String apiContext, String apiVersion) throws APIMgtDAOException;
    /**
     * Check Endpoint is exist
     *
     * @param name name of endpoint
     * @return existence of endpoint
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isEndpointExist(String name) throws APIMgtDAOException;

    /**
     * Check endpoint use in api or operation
     *
     * @param endpointId id of endpoint
     * @return true if used
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    boolean isEndpointAssociated(String endpointId) throws APIMgtDAOException;

    /**
     * Retrieves available APIs with given life cycle status and gateway labels.
     *
     * @param gatewayLabels A list of gateway labels
     * @param status Life cycle status
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsByStatus(List<String> gatewayLabels, String status) throws APIMgtDAOException;

    /**
     * Retrieves available APIs with given gateway labels.
     * 
     * @param gatewayLabels A list of gateway labels
     * @return {@code List<API>} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     *
     */
    List<API> getAPIsByGatewayLabel(List<String> gatewayLabels) throws APIMgtDAOException;

    /**
     * Add a rating for an api.
     * By default the max rating value is 5. To update the max rating, add "ratingMaxValue" config to deployment.yaml
     * and set a suitable value.
     *
     * @param apiId UUID of the api
     * @param rating rating object
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addRating(String apiId, Rating rating) throws APIMgtDAOException;

    /**
     * Update an existing rating
     *
     * @param apiId  UUID of the api
     * @param ratingId UUID of the rating
     * @param ratingFromPayload Rating object from request payload
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateRating(String apiId, String ratingId, Rating ratingFromPayload) throws APIMgtDAOException;

    /**
     * Retrieve user rating for a given api
     *
     * @param apiId UUID of the api
     * @param userId unique userId of the user
     * @return user rating for an api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Rating getUserRatingForApiFromUser(String apiId, String userId) throws APIMgtDAOException;

    /**
     * Retrieve rating given the uuid
     *
     * @param apiId  UUID of the api
     * @param ratingId  UUID of the rating
     * @return the rating object for a given uuid
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Rating getRatingByUUID(String apiId, String ratingId) throws APIMgtDAOException;

    /**
     * Retrieve average rating for an api
     *
     * @param apiId  UUID of the api
     * @return average rating of the api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    double getAverageRating(String apiId) throws APIMgtDAOException;

    /**
     * @param apiId  UUID of the api
     * @return list of ratings for an api
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<Rating> getRatingsListForApi(String apiId) throws APIMgtDAOException;

    /**
     * Retrieve list of uuids of global endpoints
     * @return list of global endpoint uuid
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<String> getUUIDsOfGlobalEndpoints() throws APIMgtDAOException;

    /**
     * Retrieve Gateway Configuration of Endpoint
     * @param endpointId uuid of Endpoint
     * @return Gateway Configuration of Endpoint
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    String getEndpointConfig(String endpointId) throws APIMgtDAOException;
}
