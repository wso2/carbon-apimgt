package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.ApplicationInfo;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;

import java.util.Map;

public interface ApplicationDAO {

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
     * @param applicationId AppID
     * @param keyManagerName KeyManager Name
     * @param keyManagerId KeyManager Id
     * @param keyType KeyType
     * @param consumerKey   Consumer Key
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
     * @param keyType         key type.
     * @param applicationId   apim application id.
     * @param clientId        consumer key.
     * @param keyMappingId    key mapping id.
     * @throws APIManagementException   if an error occurs while creation key mappings.
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

}
