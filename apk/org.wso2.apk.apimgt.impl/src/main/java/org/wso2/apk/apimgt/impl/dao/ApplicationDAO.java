package org.wso2.apk.apimgt.impl.dao;

import org.apache.commons.lang3.tuple.Pair;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.*;
import org.wso2.apk.apimgt.api.model.webhooks.Subscription;

import java.util.Map;
import java.util.Set;

public interface ApplicationDAO {

    /**
     * Add a new Application from the store.
     *
     * @param application  Application Object
     * @param userId       User ID
     * @param organization Organization
     * @return Application ID
     * @throws APIManagementException if error
     */
    int addApplication(Application application, String userId, String organization)
            throws APIManagementException;

    /**
     * Retrieves the Application which is corresponding to the given UUID String
     *
     * @param uuid UUID of Application
     * @return Application Object
     * @throws APIManagementException
     */
    Application getApplicationByUUID(String uuid) throws APIManagementException;

    /**
     * Retrieves the Application which is corresponding to the given UUID String
     *
     * @param applicationId ID of Application
     * @return Application Object
     * @throws APIManagementException
     */
    Application getApplicationById(int applicationId) throws APIManagementException;

    /**
     * Updates an Application identified by its id
     *
     * @param application Application object to be updated
     * @throws APIManagementException
     */
    void updateApplication(Application application) throws APIManagementException;

    /**
     * Deletes an Application along with subscriptions, keys and registration data
     *
     * @param application Application object to be deleted from the database which has the application Id
     * @throws APIManagementException
     */
    void deleteApplication(Application application) throws APIManagementException;

    /**
     * returns the SubscribedAPI object which is related to the UUID
     *
     * @param uuid UUID of Application
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionByUUID(String uuid) throws APIManagementException;

    /**
     * Retrieve the applications by user/application name
     *
     * @param user
     * @param owner
     * @param tenantId
     * @param limit
     * @param offset
     * @param sortBy
     * @param sortOrder
     * @param appName
     * @return
     * @throws APIManagementException
     */
    Application[] getApplicationsWithPagination(String user, String owner, int tenantId, int limit,
                                                int offset, String sortBy, String sortOrder, String appName)
            throws APIManagementException;

    /**
     * returns application for Organization
     *
     * @param organization Organization Name
     * @return Application List
     * @throws APIManagementException
     */
    Application[] getAllApplicationsOfTenantForMigration(String organization) throws
            APIManagementException;

    /**
     * Get count of the applications for the tenantId.
     *
     * @param tenantId          content to get application count based on tenant_id
     * @param searchOwner       content to search applications based on owners
     * @param searchApplication content to search applications based on application
     * @throws APIManagementException if failed to get application
     */
    int getApplicationsCount(int tenantId, String searchOwner, String searchApplication) throws
            APIManagementException;

    /**
     * Get Application Details by Consumer Key.
     *
     * @param consumerKey Consumer Key of the application
     * @return ApplicationInfo object
     * @throws APIManagementException if failed to get application details
     */
    ApplicationInfo getLightweightApplicationByConsumerKey(String consumerKey) throws APIManagementException;

    /**
     * Fetches an Application by name.
     *
     * @param applicationName Name of the Application
     * @param userId          Name of the User.
     * @param groupId         Group ID
     * @throws APIManagementException
     */
    Application getApplicationByName(String applicationName, String userId, String groupId)
            throws APIManagementException;

    /**
     * Delete a record from AM_APPLICATION_REGISTRATION table by application ID and token type.
     *
     * @param applicationId  APIM application ID.
     * @param tokenType      Token type (PRODUCTION || SANDBOX)
     * @param keyManagerName
     * @throws APIManagementException if failed to delete the record.
     */
    void deleteApplicationRegistration(int applicationId, String tokenType, String keyManagerName) throws APIManagementException;

    /**
     * This method will delete a record from AM_APPLICATION_REGISTRATION
     *
     * @param applicationId
     * @param tokenType
     */
    void deleteApplicationKeyMappingByApplicationIdAndType(int applicationId, String tokenType)
            throws APIManagementException;

    /**
     * This method will delete a record from AM_APPLICATION_REGISTRATION
     *
     * @param keyMappingId
     */
    void deleteApplicationKeyMappingByMappingId(String keyMappingId) throws APIManagementException;

    /**
     * @param applicationId
     * @param keyType
     * @return
     */
    Map<String, String> getConsumerkeyByApplicationIdAndKeyType(int applicationId, String keyType)
            throws APIManagementException;

    /**
     * @param username Subscriber
     * @return ApplicationId for given appname.
     * @throws APIManagementException if failed to get Applications for given subscriber.
     */
    int getApplicationId(String appName, String username) throws APIManagementException;

    /**
     * Check if key mapping exists for (app ID, key type and key manager) or (consumer key and key manager) values.
     *
     * @param applicationId  AppID
     * @param keyManagerName KeyManager Name
     * @param keyManagerId   KeyManager Id
     * @param keyType        KeyType
     * @param consumerKey    Consumer Key
     * @return true if key mapping exists
     * @throws APIManagementException if an error occurs.
     */
    boolean isKeyMappingExistsForConsumerKeyOrApplication(int applicationId, String keyManagerName,
                                                          String keyManagerId, String keyType,
                                                          String consumerKey) throws APIManagementException;

    /**
     * This method will create a new client at key-manager side.further it will add new record to
     * the AM_APPLICATION_KEY_MAPPING table
     *
     * @param keyType       key type.
     * @param applicationId apim application id.
     * @param clientId      consumer key.
     * @param keyMappingId  key mapping id.
     * @throws APIManagementException if an error occurs while creation key mappings.
     */
    void createApplicationKeyTypeMappingForManualClients(String keyType, int applicationId,
                                                         String clientId, String keyManagerId,
                                                         String keyMappingId) throws APIManagementException;

    /**
     * Checks whether application is accessible to the specified user
     *
     * @param applicationID ID of the Application
     * @param userId        Name of the User.
     * @param groupId       Group IDs
     * @throws APIManagementException
     */
    boolean isAppAllowed(int applicationID, String userId, String groupId) throws APIManagementException;

    /**
     * Find the name of the application by Id
     *
     * @param applicationId - application id
     * @return - application name
     * @throws APIManagementException
     */
    String getApplicationNameFromId(int applicationId) throws APIManagementException;

    /**
     * Retrieves the consumer keys and keyManager in a given application
     *
     * @param appId application id
     * @return Map<ConsumerKey, Pair < keyManagerName, keyManagerTenantDomain>
     * @throws APIManagementException
     */
    Map<String, Pair<String, String>> getConsumerKeysForApplication(int appId) throws APIManagementException;

    /**
     * Update the status of the Application creation process
     *
     * @param applicationId
     * @param status
     * @throws APIManagementException
     */
    void updateApplicationStatus(int applicationId, String status) throws APIManagementException;

    /**
     * Cleans the pending approval tasks associated with the given application subjected to be deleted
     * Pending approvals for Application creation, Subscription Creation, Subscription Deletion, Subscription Update will be deleted
     *
     * @param applicationId ID of the application which the associated pending tasks should be removed
     * @throws APIManagementException IF any issue occurred in retrieving workflow references for the given applicationId
     */
    Map<String, Set<Integer>> getPendingSubscriptionsByAppId(int applicationId) throws APIManagementException;

    /**
     * Retrieve Registration Approval State
     *
     * @param appId   Application ID
     * @param keyType Key Type
     * @throws APIManagementException
     */
    Map<String, String> getRegistrationApprovalState(int appId, String keyType) throws APIManagementException;

    /**
     * Check if key mappings already exists for app ID, key manager name or ID and key type.
     *
     * @param applicationId  app ID
     * @param keyManagerName key manager name
     * @param keyManagerId   key manager ID
     * @param keyType        key type
     * @return true if key mapping exists
     * @throws APIManagementException if an error occurs
     */
    boolean isKeyMappingExistsForApplication(int applicationId, String keyManagerName,
                                             String keyManagerId, String keyType) throws APIManagementException;

    /**
     * Retrieve Key Mapping ID from Application and Key Type and Key Manager
     *
     * @param applicationId  app ID
     * @param keyManagerName key manager name
     * @param tokenType      token type
     * @return Key Mapping ID
     * @throws APIManagementException if an error occurs
     */
    String getKeyMappingIdFromApplicationIdKeyTypeAndKeyManager(int applicationId, String tokenType,
                                                                String keyManagerName)
            throws APIManagementException;

    /**
     * Retrieve Application
     *
     * @param applicationId app ID
     * @param userId        User Id
     * @param groupId       Group ID
     * @return Application
     * @throws APIManagementException if an error occurs
     */
    Application getApplicationById(int applicationId, String userId, String groupId) throws APIManagementException;

    /**
     * Fetches groups id for a given application
     *
     * @param applicationId Application ID
     * @return group ID
     * @throws APIManagementException if error
     */
    String getGroupId(int applicationId) throws APIManagementException;

    /**
     * #TODO later we might need to use only this method.
     *
     * @param subscriber   The subscriber.
     * @param groupingId   The groupId to which the applications must belong.
     * @param start        The start index.
     * @param offset       The offset.
     * @param search       The search string.
     * @param sortOrder    The sort order.
     * @param sortColumn   The sort column.
     * @param organization Identifier of an organization
     * @return Application[] The array of applications.
     * @throws APIManagementException
     */
    Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start,
                                                int offset, String search, String sortColumn, String sortOrder, String organization)
            throws APIManagementException;

    /**
     * This method used to update Application metadata according to oauth app info
     *
     * @param applicationId
     * @param keyType
     * @param keyManagerName
     * @param updatedAppInfo
     * @throws APIManagementException
     */
    void updateApplicationKeyTypeMetaData(int applicationId, String keyType, String keyManagerName,
                                          OAuthApplicationInfo updatedAppInfo) throws APIManagementException;

    /**
     * This method used tot get Subscriber from subscriberId.
     *
     * @param subscriberName id
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from subscriber id
     */
    Subscriber getSubscriber(String subscriberName) throws APIManagementException;

    /**
     * Update Application Owner
     *
     * @param userName    Owner
     * @param application Application
     * @return
     * @throws APIManagementException
     */
    boolean updateApplicationOwner(String userName, Application application) throws
            APIManagementException;

    /**
     * Delete certain attribute stored against an Application
     *
     * @param attributeKey  User defined key of attribute
     * @param applicationId
     * @throws APIManagementException
     */
    void deleteApplicationAttributes(String attributeKey, int applicationId) throws APIManagementException;

    /**
     * Add new attributes against an Application in API Store
     *
     * @param applicationAttributes Map of key, value pair of attributes
     * @param applicationId         Id of Application against which attributes are getting stored
     * @param tenantId              Id of tenant
     * @throws APIManagementException
     */
    void addApplicationAttributes(Map<String, String> applicationAttributes, int applicationId, int tenantId)
            throws APIManagementException;


    /**
     * Retrieve API Key
     *
     * @param applicationId Application ID
     * @param keyMappingId  Key Mapping ID
     * @return
     * @throws APIManagementException
     */
    APIKey getAPIKeyFromApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
            throws APIManagementException;

    /**
     * Retrieves set of web hook topic subscriptions for a application.
     *
     * @param applicationId application UUID
     * @return set of web hook subscriptions.
     * @throws APIManagementException if failed to retrieve web hook topc subscriptions
     */
    Set<Subscription> getTopicSubscriptions(String applicationId) throws APIManagementException;

    /**
     * Retrieves paginated Subscribed APIs by Application.
     *
     * @param application  application object
     * @param organization Organization
     * @return set of Subscribed APIs
     * @throws APIManagementException if failed to retrieve APIs
     */
    Set<SubscribedAPI> getPaginatedSubscribedAPIsByApplication(Application application, Integer offset,
                                                               Integer limit, String organization)
            throws APIManagementException;

    /**
     * Retrieves Consumer Key.
     *
     * @param applicationId application Id
     * @param keyType       KeyType
     * @param keyManager    KeyManager Name
     * @return Consumer Key
     * @throws APIManagementException if failed to retrieve Consumer Key
     */
    String getConsumerKeyByApplicationIdKeyTypeKeyManager(int applicationId, String keyType, String keyManager)
            throws APIManagementException;


}
