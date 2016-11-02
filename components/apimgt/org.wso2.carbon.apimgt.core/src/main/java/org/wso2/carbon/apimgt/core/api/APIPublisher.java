package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.LifeCycleEvent;
import org.wso2.carbon.apimgt.core.models.Provider;
import org.wso2.carbon.apimgt.core.models.Subscriber;

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
     * @param providerId , provider id
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    List<API> getAPIsByProvider(String providerId) throws APIManagementException;

    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribed APIs of given provider
     */
    Set<Subscriber> getSubscribersOfProvider(String providerId) throws APIManagementException;

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
    Set<Subscriber> getSubscribersOfAPI(API identifier) throws APIManagementException;

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
     * @param api API
     * @throws APIManagementException if failed to add API
     */
    void addAPI(API api) throws APIManagementException;

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
     * @param api API
     * @throws APIManagementException if failed to update API
     */
    void updateAPI(API api) throws APIManagementException;



    /**
     * This method used to Update the status of API
     *
     * @param api
     * @param status
     * @param deprecateOldVersions
     * @param makeKeysForwardCompatible
     * @return
     * @throws APIManagementException
     */
    boolean updateAPIStatus(API api, String status, boolean deprecateOldVersions,
                            boolean makeKeysForwardCompatible)
            throws APIManagementException;


    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the API
     */
    void createNewAPIVersion(API api, String newVersion) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param id   String
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws APIManagementException if failed to remove documentation
     */
    void removeDocumentation(String id, String docType, String docName) throws APIManagementException;

    /**
     * Removes a given documentation
     *
     * @param apiId String
     * @param docId UUID of the doc
     * @throws APIManagementException if failed to remove documentation
     */
    public void removeDocumentation(String apiId, String docId) throws APIManagementException;

    /**
     * Adds Documentation to an API
     *
     * @param apiId         String
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    void addDocumentation(String apiId, DocumentInfo documentation) throws APIManagementException;

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
    void addFileToDocumentation(String apiId, DocumentInfo documentation, String filename, InputStream content,
                                String contentType) throws APIManagementException;

    /**
     * Checks if a given API exists in the registry
     *
     * @param apiId
     * @return boolean result
     * @throws APIManagementException
     */
    boolean checkIfAPIExists(String apiId) throws APIManagementException;

    /**
     * This method used to save the documentation content
     *
     * @param api               API
     * @param documentationName name of the inline documentation
     * @param text              content of the inline documentation
     * @throws APIManagementException if failed to add the document as a resource to registry
     */
    void addDocumentationContent(API api, String documentationName, String text) throws APIManagementException;

    /**
     * Updates a given documentation
     *
     * @param apiId         String
     * @param documentation Documentation
     * @throws APIManagementException if failed to update docs
     */
    void updateDocumentation(String apiId, DocumentInfo documentation) throws APIManagementException;

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
     * Search API
     *
     * @param searchTerm Search Term
     * @param searchType Search Type
     * @param providerId
     * @return Set of APIs
     * @throws APIManagementException
     */
    List<API> searchAPIs(String searchTerm, String searchType, String providerId) throws APIManagementException;

    /**
     * Update the subscription status
     *
     * @param apiId     API Identifier
     * @param subStatus Subscription Status
     * @param appId     Application Id              *
     * @throws APIManagementException If failed to update subscription status
     */
    void updateSubscription(String apiId, String subStatus, int appId) throws APIManagementException;


    /**
     * This method updates Swagger 2.0 resources in the registry
     *
     * @param apiId    id of the String
     * @param jsonText json text to be saved in the registry
     * @throws APIManagementException
     */
    void saveSwagger20Definition(String apiId, String jsonText) throws APIManagementException;


    /**
     * This method is to change registry lifecycle states for an API artifact
     *
     * @param apiIdentifier apiIdentifier
     * @param action        Action which need to execute from registry lifecycle
     */
    boolean changeLifeCycleStatus(String apiIdentifier, String action)
            throws APIManagementException;

    /**
     * This method is to set checklist item values for a particular life-cycle state of an API
     *
     * @param apiIdentifier  apiIdentifier
     * @param checkItem      Order of the checklist item
     * @param checkItemValue Value of the checklist item
     */
    boolean changeAPILCCheckListItems(String apiIdentifier, int checkItem, boolean checkItemValue)
            throws APIManagementException;

    /**
     * This method is to set a lifecycle check list item given the String and the checklist item name.
     * If the given item not in the allowed lifecycle check items list or item is already checked, this will stay
     * silent and return false. Otherwise, the checklist item will be updated and returns true.
     *
     * @param apiIdentifier  String
     * @param checkItemName  Name of the checklist item
     * @param checkItemValue Value to be set to the checklist item
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    boolean checkAndChangeAPILCCheckListItem(String apiIdentifier, String checkItemName, boolean checkItemValue)
            throws APIManagementException;

    /**
     * This method returns the lifecycle data for an API including current state,next states.
     *
     * @param apiId String
     * @return Map<String,Object> a map with lifecycle data
     */
    Map<String, Object> getAPILifeCycleData(String apiId) throws APIManagementException;

    /**
     * Push api related state changes to the gateway. Api related configurations will be deployed or destroyed
     * according to the new state.
     *
     * @param identifier Api identifier
     * @param newStatus  new state of the lifecycle
     * @return collection of failed gateways. Map contains gateway name as the key and the error as the value
     * @throws APIManagementException
     */
    Map<String, String> propergateAPIStatusChangeToGateways(String identifier, APIStatus newStatus)
            throws APIManagementException;

    /**
     * Update api related information such as database entries, registry updates for state change.
     *
     * @param identifier
     * @param newStatus  accepted if changes are not pushed to a gateway
     * @return boolean value representing success not not
     * @throws APIManagementException
     */
    boolean updateAPIForStateChange(String identifier, APIStatus newStatus) throws APIManagementException;

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

}
