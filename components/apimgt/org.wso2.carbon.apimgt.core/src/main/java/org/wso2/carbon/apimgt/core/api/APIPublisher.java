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
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.io.InputStream;
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
     * @return Set<Provider>
     * @throws APIManagementException if failed to get Providers
     */
    Set<Provider> getAllProviders() throws APIManagementException;

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerName  username of the the user who created the API
     * @return set of APIs
     * @throws APIManagementException if failed to get set of API
     */
    List<API> getAPIsByProvider(String providerName) throws APIManagementException;

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    Set<String> getSubscribersOfProvider(String providerId) throws APIManagementException;

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
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    Set<String> getSubscribersOfAPI(API identifier) throws APIManagementException;

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param id apiuuid
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    long getAPISubscriptionCountByAPI(String id) throws APIManagementException;

    String getDefaultVersion(String apiid) throws APIManagementException;

    /**
     * Adds a new API to the Store
     *
     * @param apiBuilder API
     * @throws APIManagementException if failed to add API
     */
    String addAPI(API.APIBuilder apiBuilder) throws APIManagementException;

    /**
     * @param api
     * @return
     * @throws APIManagementException
     */
    public boolean isAPIUpdateValid(API api) throws APIManagementException;

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status.
     * Implementations should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param apiBuilder {@link org.wso2.carbon.apimgt.core.models.API.APIBuilder}
     * @throws APIManagementException if failed to update API
     */
    void updateAPI(API.APIBuilder apiBuilder) throws APIManagementException;



    /**
     * This method used to Update the status of API
     *
     * @param api
     * @param status
     * @throws APIManagementException
     */
    void updateAPIStatus(String api, String status, Map<String, Boolean> checkListItemMap)
            throws APIManagementException;


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param apiId        The API to be copied
     * @param newVersion The version of the new API
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    String createNewAPIVersion(String apiId, String newVersion) throws APIManagementException;

    /**
     * Attach Documentation (without content) to an API
     *
     * @param apiId         UUID of API
     * @param documentInfo      Document Summary
     * @throws APIManagementException if failed to add documentation
     */
    String addDocumentationInfo(String apiId, DocumentInfo documentInfo) throws APIManagementException;

    /**
     * Add a document (of source type FILE) with a file
     *
     * @param resourceId         UUID of API
     * @param content       content of the file as an Input Stream
     * @param fileName
     * @throws APIManagementException if failed to add the file
     */
    void uploadDocumentationFile(String resourceId, InputStream content, String fileName) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param docId   Document Id
     * @throws APIManagementException if failed to remove documentation
     */
    void removeDocumentation(String docId) throws APIManagementException;

    /**
     * Checks if a given API exists in the registry
     *
     * @param apiId
     * @return boolean result
     * @throws APIManagementException
     */
    boolean checkIfAPIExists(String apiId) throws APIManagementException;

    /**
     * Checks if a given API name exists in the registry
     *
     * @param name
     * @return boolean result
     * @throws APIManagementException
     */
    boolean checkIfAPINameExists(String name) throws APIManagementException;

    /**
     * Checks if a given API context exists in the registry
     *
     * @param context
     * @return boolean result
     * @throws APIManagementException
     */
    boolean checkIfAPIContextExists(String context) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param docId name of the inline documentation
     * @param text              content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    void addDocumentationContent(String docId, String text) throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         String
     * @param documentation Documentation
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
     *
     * @param limit
     * @param offset
     * @param query
     * @return
     * @throws APIManagementException
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
     * This method updates Swagger 2.0 resources in the registry
     *
     * @param apiId    id of the String
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException
     */
    void saveSwagger20Definition(String apiId, String jsonText) throws APIManagementException;




    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return Map<String,Object> a map with lifecycle data
     */
    LifecycleState getAPILifeCycleData(String apiId) throws APIManagementException;




    /**
     * Get the current lifecycle status of the api
     *
     * @param apiIdentifier Api identifier
     * @return Current lifecycle status
     * @throws APIManagementException
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
     * Save the thumbnail icon for api
     * @param apiId apiId of api
     * @param inputStream inputStream of image
     * @throws APIManagementException
     */
   void saveThumbnailImage(String apiId, InputStream inputStream, String dataType) throws APIManagementException;


    /**
     * Get the thumbnail icon for api
     * @param apiId apiId of api
     * @throws APIManagementException
     */
    InputStream getThumbnailImage(String apiId) throws APIManagementException;

    /**
     * This method updates gateway config in the database
     *
     * @param apiId        id of the String
     * @param configString text to be saved in the registry
     * @throws APIManagementException
     */
    void updateApiGatewayConfig(String apiId, String configString) throws APIManagementException;

    /**
     * This method retrieve gateway config in the database
     *
     * @param apiId id of the String
     * @return API gateway config as a string
     * @throws APIManagementException
     */
    String getApiGatewayConfig(String apiId) throws APIManagementException;

    /**
     * Return list of endpoints
     * @return
     * @throws APIManagementException
     */
    List<Endpoint> getAllEndpoints() throws APIManagementException;


    /**
     * Get endpoint details according to the endpointId
     * @param endpointId uuid of endpoint
     * @return details of endpoint
     * @throws APIManagementException
     */
    Endpoint getEndpoint(String endpointId) throws APIManagementException;

    /**
     * Add an endpoint
     * @param endpoint
     * @throws APIManagementException
     */
    String addEndpoint(Endpoint endpoint) throws APIManagementException;

    /**
     * Update and endpoint
     * @param endpoint
     * @throws APIManagementException
     */
    void updateEndpoint(Endpoint endpoint) throws APIManagementException;

    /**
     * Delete an endpoint
     * @param endpointId
     * @throws APIManagementException
     */
    void deleteEndpoint(String endpointId) throws APIManagementException;

    /**
     * Create api from Definition
     * @param apiDefinition
     * @return
     * @throws APIManagementException
     */
    String addApiFromDefinition(InputStream apiDefinition) throws APIManagementException;

    List<Policy> getAllPoliciesByLevel(String tierLevel) throws APIManagementException;

    Policy getPolicyByName(String tierLevel, String tierName) throws APIManagementException;
}
