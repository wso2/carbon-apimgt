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

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface used to write Publisher specific methods
 */
public interface APIPublisher extends APIManager {
    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.core.models.Provider} available on the system.
     *
     * @return {@code Set<Provider>}
     * @throws APIManagementException if failed to get Providers
     */
    Set<Provider> getAllProviders() throws APIManagementException;

    /**
     * Get a list of subscriptions for provider's APIs
     *
     * @param offset Starting index of the search results
     * @param limit Number of search results returned
     * @param providerName if of the provider
     * @return {@code List<Subscriber>} List of subscriptions for provider's APIs
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    List<Subscription> getSubscribersOfProvider(int offset, int limit, String providerName)
            throws APIManagementException;

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    Provider getProvider(String providerName) throws APIManagementException;

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier String
     * @return {@code Set<Subscriber>}
     * @throws APIManagementException if failed to get Subscribers
     */
    Set<String> getSubscribersOfAPI(API identifier) throws APIManagementException;

    /**
     * this method returns the {@code Set<APISubscriptionCount>} for given provider and api
     *
     * @param id apiuuid
     * @return {@code Set<APISubscriptionCount>}
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    long getAPISubscriptionCountByAPI(String id) throws APIManagementException;

    String getDefaultVersion(String apiid) throws APIManagementException;

    /**
     * Adds a new API to the Store
     *
     * @param apiBuilder API
     * @return Details of the added API.
     * @throws APIManagementException if failed to add API
     */
    String addAPI(API.APIBuilder apiBuilder) throws APIManagementException;

    /**
     * @param api API Object
     * @return Validity of the API update.
     * @throws APIManagementException If failed to check validity of API update.
     */
    public boolean isAPIUpdateValid(API api) throws APIManagementException;

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status.
     * Implementations should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param apiBuilder {@code org.wso2.carbon.apimgt.core.models.API.APIBuilder}
     * @throws APIManagementException if failed to update API
     */
    void updateAPI(API.APIBuilder apiBuilder) throws APIManagementException;


    /**
     * This method used to Update the status of API
     *
     * @param api              API UUID
     * @param status           New lifecycle status
     * @param checkListItemMap Map containing values of check list items.
     * @return WorkflowResponse workflow response related to LC state change.
     * @throws APIManagementException If failed to update API lifecycle status
     */
    WorkflowResponse updateAPIStatus(String api, String status, Map<String, Boolean> checkListItemMap)
            throws APIManagementException;


    /**
     * @param apiId            API UUID
     * @param status           New lifecycle status
     * @param checkListItemMap Check List Items map
     * @throws APIManagementException If failed update check list item status.
     */
    void updateCheckListItem(String apiId, String status, Map<String, Boolean> checkListItemMap)
            throws APIManagementException;

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiId      The API to be copied
     * @param newVersion The version of the new API
     * @return Details of the newly created version of the API.
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    String createNewAPIVersion(String apiId, String newVersion) throws APIManagementException;

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId        UUID of API
     * @param documentInfo Document Summary
     * @return Details of the added document.
     * @throws APIManagementException if failed to add documentation
     */
    String addDocumentationInfo(String apiId, DocumentInfo documentInfo) throws APIManagementException;

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param resourceId UUID of API
     * @param content    content of the file as an Input Stream
     * @param dataType   File mime type
     * @throws APIManagementException if failed to add the file
     */
    void uploadDocumentationFile(String resourceId, InputStream content, String dataType) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param docId Document Id
     * @throws APIManagementException if failed to remove documentation
     */
    void removeDocumentation(String docId) throws APIManagementException;

    /**
     * Checks if a given API name exists in the registry
     *
     * @param name Name of the API.
     * @return boolean result
     * @throws APIManagementException If failed to check ia API exist.
     */
    boolean checkIfAPINameExists(String name) throws APIManagementException;

    /**
     * Checks if a given API context exists in the registry
     *
     * @param context Context of the API.
     * @return boolean result
     * @throws APIManagementException If failed to check ia API exist.
     */
    boolean checkIfAPIContextExists(String context) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param docId name of the inline documentation
     * @param text  content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    void addDocumentationContent(String docId, String text) throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         UUID of the API.
     * @param documentation Documentation
     * @return Details of the updated document.
     * @throws APIManagementException if failed to update docs
     */
    String updateDocumentation(String apiId, DocumentInfo documentation) throws APIManagementException;

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the String
     * @throws APIManagementException if failed to copy docs
     */
    void copyAllDocumentation(String apiId, String toVersion) throws APIManagementException;

    /**
     * Returns the details of all the life-cycle changes done per API.
     *
     * @param apiId id of the String
     * @return List of life-cycle events per given API
     * @throws APIManagementException if failed to copy docs
     */
    List<LifeCycleEvent> getLifeCycleEvents(String apiId) throws APIManagementException;

    /**
     * Delete an API
     *
     * @param identifier String
     * @throws APIManagementException if failed to remove the API
     */
    void deleteAPI(String identifier) throws APIManagementException;

    /**
     * @param limit  Number of search results returned
     * @param offset Starting index of the search results
     * @param query  Search query
     * @return List of APIs
     * @throws APIManagementException If failed to search for apis with given query.
     */
    List<API> searchAPIs(Integer limit, Integer offset, String query) throws APIManagementException;

    /**
     * Update the subscription status
     *
     * @param subId     Subscription ID
     * @param subStatus Subscription Status
     * @throws APIManagementException If failed to update subscription status
     */
    void updateSubscriptionStatus(String subId, APIMgtConstants.SubscriptionStatus subStatus) throws
            APIManagementException;

    /**
     * Update the subscription Policy
     *
     * @param subId     Subscription ID
     * @param newPolicy New Subscription Policy
     * @throws APIManagementException If failed to update subscription policy
     */
    void updateSubscriptionPolicy(String subId, String newPolicy) throws APIManagementException;


    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return {@code Map<String,Object>} a map with lifecycle data
     * @throws APIManagementException If failed to get life cycle state data.
     */
    LifecycleState getAPILifeCycleData(String apiId) throws APIManagementException;


    /**
     * Get the current lifecycle status of the api
     *
     * @param apiIdentifier Api identifier
     * @return Current lifecycle status
     * @throws APIManagementException If failed to get life cycle state.
     */
    String getAPILifeCycleStatus(String apiIdentifier) throws APIManagementException;

    /**
     * Get the paginated APIs from publisher
     *
     * @param start starting number
     * @param end   ending number
     * @return set of API
     * @throws APIManagementException if failed to get Apis
     */
    Map<String, Object> getAllPaginatedAPIs(int start, int end) throws APIManagementException;

    /**
     * Return list of endpoints
     *
     * @return {@code List<Endpoint>}
     * @throws APIManagementException If failed to get list of endpoints.
     */
    List<Endpoint> getAllEndpoints() throws APIManagementException;


    /**
     * Get endpoint details according to the endpointId
     *
     * @param endpointId uuid of endpoint
     * @return details of endpoint
     * @throws APIManagementException If failed to get endpoint data.
     */
    Endpoint getEndpoint(String endpointId) throws APIManagementException;

    /**
     * Retrieves the {@link Endpoint} object for given endpoint name
     *
     * @param endpointName name of the Endpoint
     * @return {@link Endpoint} instance
     * @throws APIManagementException if an error occurs while retrieving the information
     */
    Endpoint getEndpointByName(String endpointName) throws APIManagementException;

    /**
     * Add an endpoint
     *
     * @param endpoint Endpoint object to be added.
     * @return Details of the added API.
     * @throws APIManagementException If failed to add endpoint.
     */
    String addEndpoint(Endpoint endpoint) throws APIManagementException;

    /**
     * Update and endpoint
     *
     * @param endpoint Endpoint object to be updated.
     * @throws APIManagementException If failed to update endpoint.
     */
    void updateEndpoint(Endpoint endpoint) throws APIManagementException;

    /**
     * Delete an endpoint
     *
     * @param endpointId Id of the endpoint to be deleted.
     * @throws APIManagementException If failed to delete endpoint.
     */
    void deleteEndpoint(String endpointId) throws APIManagementException;

    /**
     * Create api from Definition
     *
     * @param apiDefinition content of the API.
     * @return Details of the added API.
     * @throws APIManagementException If failed to add endpoint.
     */
    String addApiFromDefinition(InputStream apiDefinition) throws APIManagementException;

    /**
     * Create api using HttpUrlConnection
     *
     * @param httpURLConnection httpUrlConnection constructed by a url
     * @return details of the added API.
     * @throws APIManagementException If failed to add the API.
     */
    String addApiFromDefinition(HttpURLConnection httpURLConnection) throws APIManagementException;

    /**
     * This method updates gateway config in the database
     *
     * @param apiId        id of the String
     * @param configString text to be saved in the registry
     * @throws APIManagementException If failed to update gateway config.
     */
    void updateApiGatewayConfig(String apiId, String configString) throws APIManagementException;

    /**
     * This method retrieve gateway config in the database
     *
     * @param apiId id of the String
     * @return API gateway config as a string
     * @throws APIManagementException If failed to get gateway config of the API.
     */
    String getApiGatewayConfig(String apiId) throws APIManagementException;

    /**
     * This method updates Swagger 2.0 resource in the DB
     *
     * @param apiId    id of the String
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException If failed to save swagger definition.
     */
    void saveSwagger20Definition(String apiId, String jsonText) throws APIManagementException;

    /**
     * Returns the WSDL of a given API UUID as {@link String}.
     * 
     * @param apiId API UUID
     * @return WSDL of the API as {@link String}
     * @throws APIMgtDAOException if error occurs while accessing the WSDL from the data layer
     */
    String getAPIWSDL(String apiId) throws APIMgtDAOException;

    /**
     * Returns an stream from the ZIP wsdl archive of a particular API stored in DB.
     * 
     * @param apiId UUID of API
     * @return an stream from the ZIP wsdl archive of a particular API stored in DB
     * @throws APIMgtDAOException if error occurs while accessing the WSDL from the data layer
     */
    InputStream getAPIWSDLArchive(String apiId) throws APIMgtDAOException;

    /**
     * Creates an API using a WSDL archive stream.
     * 
     * @param apiBuilder {@code APIBuilder} instance
     * @param inputStream WSDL archive stream
     * @param isHttpBinding states whether http binding operations should be used to create resources
     * @return UUID of the created API
     * @throws APIManagementException If fails to add the API
     */
    String addAPIFromWSDLArchive(API.APIBuilder apiBuilder, InputStream inputStream, boolean isHttpBinding)
            throws APIManagementException;

    /**
     * Creates an API using a WSDL file.
     *
     * @param apiBuilder {@code APIBuilder} instance
     * @param inputStream WSDL archive stream
     * @param isHttpBinding states whether http binding operations should be used to create resources
     * @return UUID of the created API
     * @throws APIManagementException If fails to add the API
     */
    String addAPIFromWSDLFile(API.APIBuilder apiBuilder, InputStream inputStream, boolean isHttpBinding)
            throws APIManagementException;

    /**
     * Creates an API using a WSDL URL.
     *
     * @param apiBuilder {@code APIBuilder} instance
     * @param wsdlUrl WSDL URL
     * @param isHttpBinding states whether http binding operations should be used to create resources
     * @return UUID of the created API
     * @throws APIManagementException If fails to add the API
     * @throws IOException Error occurs while accessing the URL
     */
    String addAPIFromWSDLURL(API.APIBuilder apiBuilder, String wsdlUrl, boolean isHttpBinding)
            throws APIManagementException, IOException;

    /**
     * Updates a WSDL (single file) of an API.
     * 
     * @param apiId UUID of API
     * @param inputStream WSDL file stream
     * @return updated WSDL content
     * @throws APIMgtDAOException If data layer error occurs while updating the WSDL
     * @throws APIMgtWSDLException If WSDL content error occurs while updating the WSDL
     */
    String updateAPIWSDL(String apiId, InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException;

    /**
     * Updates a WSDL archive of an API.
     *
     * @param apiId UUID of API
     * @param inputStream WSDL file stream
     * @throws APIMgtDAOException If data layer error occurs while updating the WSDL
     * @throws APIMgtWSDLException If WSDL content error occurs while updating the WSDL
     */
    void updateAPIWSDLArchive(String apiId, InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException;

    /**
     * Get list of policies of an particular tier level.
     *
     * @param tierLevel Tier level.
     * @return List of policy objects.
     * @throws APIManagementException If failed to retrieve policies..
     */
    List<Policy> getAllPoliciesByLevel(APIMgtAdminService.PolicyLevel tierLevel) throws APIManagementException;

    /**
     * Get the policy when name is provided.
     *
     * @param tierLevel Tier level.
     * @param tierName  Name of the policy.
     * @return List of policy objects.
     * @throws APIManagementException If failed to retrieve policies..
     */
    Policy getPolicyByName(APIMgtAdminService.PolicyLevel tierLevel, String tierName) throws APIManagementException;

    /**
     * Get LifeCycle State Chanage History of API
     *
     * @param uuid of lifecycle
     * @return {@code List<LifecycleHistoryBean>} Life cycle history details
     * @throws APIManagementException If failed to get lifecycle history details.
     */
    List<LifecycleHistoryBean> getLifeCycleHistoryFromUUID(String uuid) throws APIManagementException;

    /**
     * Returns the list of Labels.
     *
     * @return List of labels
     * @throws LabelException if failed to get labels
     */
    List<Label> getAllLabels() throws LabelException;

    /**
     * Retrieves the last updated time of the endpoint given its endpointId
     * 
     * @param endpointId Id of the endpoint
     * @return last updated time 
     * @throws APIManagementException if API Manager core level exception occurred
     */
    String getLastUpdatedTimeOfEndpoint(String endpointId) throws APIManagementException;

    /**
     * Remove pending lifecycle state change task for the given api. 
     * 
     * @param apiId apiId of api
     * @throws APIManagementException if API Manager core level exception occurred
     */
    void removePendingLifecycleWorkflowTaskForAPI(String apiId) throws APIManagementException;

    /**
     * Check Endpoint is exist
     * @param name name of endpoint
     * @return existence of endpoint
     * @throws APIManagementException if API Manager core level exception occurred
     */
    boolean isEndpointExist(String name) throws APIManagementException;

    /**
     * Extract the WSDL archive and validate
     *
     * @param inputStream WSDL archive input stream
     * @return {@link WSDLArchiveInfo} object with WSDL information
     * @throws APIMgtDAOException  If an error occurred from DAO layer
     * @throws APIMgtWSDLException If an error occurred while processing WSDL files
     */
    WSDLArchiveInfo extractAndValidateWSDLArchive(InputStream inputStream)
            throws APIMgtDAOException, APIMgtWSDLException;
}
