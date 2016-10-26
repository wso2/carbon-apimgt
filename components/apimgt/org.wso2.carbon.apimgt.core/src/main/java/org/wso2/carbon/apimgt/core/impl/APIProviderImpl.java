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
package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.APIProvider;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIIdentifier;
import org.wso2.carbon.apimgt.core.models.APIManagementException;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscriber;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIProviderImpl extends AbstractAPIManager implements APIProvider {
    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.core.models.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws APIManagementException if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException {
        return null;
    }

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {
        return null;
    }

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException {
        return null;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws APIManagementException if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException {
        return null;
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier) throws APIManagementException {
        return null;
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws APIManagementException if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier) throws APIManagementException {
        return 0;
    }

    public String getDefaultVersion(APIIdentifier apiid) throws APIManagementException {
        return null;
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws APIManagementException if failed to add API
     */
    public void addAPI(API api) throws APIManagementException {

    }

    /**
     * @param api
     * @return
     * @throws APIManagementException
     */
    public boolean isAPIUpdateValid(API api) throws APIManagementException {
        return false;
    }

    /**
     * Updates design and implementation of an existing API. This method must not be used to change API status.
     * Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @throws APIManagementException if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException {

    }

    /**
     * Updates manage of an existing API. This method must not be used to change API status. Implementations
     * should throw an exceptions when such attempts are made. All life cycle state changes
     * should be carried out using the changeAPIStatus method of this interface.
     *
     * @param api API
     * @throws APIManagementException failed environments during gateway operation
     */
    public void manageAPI(API api) throws APIManagementException {

    }

    /**
     * Change the lifecycle state of the specified API
     *
     * @param api                 The API whose status to be updated
     * @param status              New status of the API
     * @param userId              User performing the API state change
     * @param updateGatewayConfig Whether the changes should be pushed to the API gateway or not
     * @throws APIManagementException on error
     */
    public void changeAPIStatus(API api, APIStatus status, String userId, boolean updateGatewayConfig) throws APIManagementException {

    }

    public boolean updateAPIStatus(APIIdentifier apiId, String status, boolean publishToGateway, boolean deprecateOldVersions, boolean makeKeysForwardCompatible) throws APIManagementException {
        return false;
    }

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws APIManagementException {

    }

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docType, String docName) throws APIManagementException {

    }

    /**
     * Removes a given documentation
     *
     * @param apiId APIIdentifier
     * @param docId UUID of the doc
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docId) throws APIManagementException {

    }

    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId, DocumentInfo documentation) throws APIManagementException {

    }

    /**
     * Add a file to a document of source type FILE
     *
     * @param apiId         API identifier the document belongs to
     * @param documentation document
     * @param filename      name of the file
     * @param content       content of the file as an Input Stream
     * @param contentType   content type of the file
     * @throws APIManagementException if failed to add the file
     */
    public void addFileToDocumentation(APIIdentifier apiId, DocumentInfo documentation, String filename, InputStream
            content, String contentType) throws APIManagementException {

    }

    /**
     * Checks if a given API exists in the registry
     *
     * @param apiId
     * @return boolean result
     * @throws APIManagementException
     */
    public boolean checkIfAPIExists(APIIdentifier apiId) throws APIManagementException {
        return false;
    }

    /**
     * This method used to save the documentation content
     *
     * @param api
     * @param documentationName
     * @param text              @throws APIManagementException if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(API api, String documentationName, String text) throws APIManagementException {

    }

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, DocumentInfo documentation) throws APIManagementException {

    }

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param apiId     id of the APIIdentifier
     * @param toVersion Version to which Documentation should be copied.
     * @throws APIManagementException if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {

    }

    /**
     * Returns the details of all the life-cycle changes done per API.
     *
     * @param apiId id of the APIIdentifier
     * @return List of life-cycle events per given API
     * @throws APIManagementException if failed to copy docs
     */
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException {
        return null;
    }

    /**
     * Delete an API
     *
     * @param identifier APIIdentifier
     * @throws APIManagementException if failed to remove the API
     */
    public void deleteAPI(APIIdentifier identifier) throws APIManagementException {

    }

    /**
     * Search API
     *
     * @param searchTerm Search Term
     * @param searchType Search Type
     * @param providerId Provider Id
     * @return Set of APIs
     * @throws APIManagementException
     */
    public List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException {
        return null;
    }

    /**
     * Update the subscription status
     *
     * @param apiId     API Identifier
     * @param subStatus Subscription Status
     * @param appId     Application Id
     * @throws APIManagementException If failed to update subscription status
     */
    public void updateSubscription(APIIdentifier apiId, String subStatus, int appId) throws APIManagementException {

    }

    /**
     * This method updates Swagger 2.0 resources in the registry
     *
     * @param apiId    id of the APIIdentifier
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException
     */
    public void saveSwagger20Definition(APIIdentifier apiId, String jsonText) throws APIManagementException {

    }

    /**
     * This method is to change registry lifecycle states for an API artifact
     *
     * @param apiIdentifier apiIdentifier
     * @param action        Action which need to execute from registry lifecycle
     */
    public boolean changeLifeCycleStatus(APIIdentifier apiIdentifier, String action) throws APIManagementException {
        return false;
    }

    /**
     * This method is to set checklist item values for a particular life-cycle state of an API
     *
     * @param apiIdentifier  apiIdentifier
     * @param checkItem      Order of the checklist item
     * @param checkItemValue Value of the checklist item
     */
    public boolean changeAPILCCheckListItems(APIIdentifier apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException {
        return false;
    }

    /**
     * This method is to set a lifecycle check list item given the APIIdentifier and the checklist item name.
     * If the given item not in the allowed lifecycle check items list or item is already checked, this will stay
     * silent and return false. Otherwise, the checklist item will be updated and returns true.
     *
     * @param apiIdentifier  APIIdentifier
     * @param checkItemName  Name of the checklist item
     * @param checkItemValue Value to be set to the checklist item
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    public boolean checkAndChangeAPILCCheckListItem(APIIdentifier apiIdentifier, String checkItemName, boolean
            checkItemValue) throws APIManagementException {
        return false;
    }

    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId APIIdentifier
     * @return Map<String, Object> a map with lifecycle data
     */
    public Map<String, Object> getAPILifeCycleData(APIIdentifier apiId) throws APIManagementException {
        return null;
    }

    /**
     * Push api related state changes to the gateway. Api related configurations will be deployed or destroyed
     * according to the new state.
     *
     * @param identifier Api identifier
     * @param newStatus  new state of the lifecycle
     * @return collection of failed gateways. Map contains gateway name as the key and the error as the value
     * @throws APIManagementException
     */
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier identifier, APIStatus newStatus)
            throws APIManagementException {
        return null;
    }

    /**
     * Update api related information such as database entries, registry updates for state change.
     *
     * @param identifier Api identifier
     * @param newStatus  accepted if changes are not pushed to a gateway
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    public boolean updateAPIforStateChange(APIIdentifier identifier, APIStatus newStatus) throws
            APIManagementException {
        return false;
    }

    /**
     * Get the current lifecycle status of the api
     *
     * @param apiIdentifier Api identifier
     * @return Current lifecycle status
     * @throws APIManagementException
     */
    public String getAPILifeCycleStatus(APIIdentifier apiIdentifier) throws APIManagementException {
        return null;
    }

    /**
     * Get the paginated APIs from publisher
     *
     * @param start starting number
     * @param end   ending number
     * @return set of API
     * @throws APIManagementException if failed to get Apis
     */
    public Map<String, Object> getAllPaginatedAPIs(int start, int end) throws APIManagementException {
        return null;
    }
}
