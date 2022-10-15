package org.wso2.apk.apimgt.impl.dao;

import org.json.simple.JSONArray;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.*;

import java.util.Set;

public interface ConsumerDAO {

    /**
     * This method used to get Subscriber from subscriberId.
     *
     * @param subscriberName id
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from subscriber id
     */
    Subscriber getSubscriber(String subscriberName) throws APIManagementException;

    /**
     * Add Rating for API
     *
     * @param id API UUID
     * @param rating Rating
     * @param user User
     * @throws APIManagementException if failed to add Rating for API
     */
    void addRating(String id, int rating, String user) throws APIManagementException;

    /**
     * Remove Rating for the API provided by user
     *
     * @param uuid API UUID
     * @param user User
     * @throws APIManagementException if failed to remove Rating for API
     */
    void removeAPIRating(String uuid, String user) throws APIManagementException;

    /**
     * Retrieve API Rating provided by User
     *
     * @param uuid API UUID
     * @param user User
     * @return Rating
     * @throws APIManagementException if failed to remove Rating for API
     */
    int getUserRating(String uuid, String user) throws APIManagementException;

    /**
     * @param apiId API uuid
     * @throws APIManagementException if failed to get API Ratings
     */
    JSONArray getAPIRatings(String apiId) throws APIManagementException;

    /**
     * Retrieve Average API Rating
     *
     * @param apiId API UUID
     * @return Average Rating
     * @throws APIManagementException if failed to retrieve average rating for API
     */
    float getAverageRating(String apiId) throws APIManagementException;

    /**
     * @param apiIdentifier
     * @param userId
     * @param applicationId
     * @return true if user app subscribed for given APIIdentifier
     * @throws APIManagementException if failed to check subscribed or not
     */
    boolean isSubscribedToApp(APIIdentifier apiIdentifier, String userId, int applicationId)
            throws APIManagementException;

    /**
     * returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException;

    /**
     * This method returns the set of APIs for given subscriber
     *
     * @param organization identifier of the organization
     * @param subscriber subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get SubscribedAPIs
     */
    Set<SubscribedAPI> getSubscribedAPIs(String organization, Subscriber subscriber, String groupingId)
            throws APIManagementException;

    /**
     * This method returns the Scopes for Application Subscription
     *
     * @param applicationId Application ID
     * @param subscriber subscriber
     * @return Set<String> Scopes
     * @throws APIManagementException if failed to get Scopes
     */
    Set<String> getScopesForApplicationSubscription(Subscriber subscriber, int applicationId)
            throws APIManagementException;

    /**
     * returns the Subscription Count
     *
     * @param subscriber subscriber
     * @param applicationName application Name
     * @param groupingId grouping ID
     * @return {@link SubscribedAPI} Object which contains the subscribed API information.
     * @throws APIManagementException
     */
    Integer getSubscriptionCount(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException;

    /**
     * @param apiIdentifier APIIdentifier
     * @param userId        User Id
     * @return true if user subscribed for given APIIdentifier
     * @throws APIManagementException if failed to check subscribed or not
     */
    boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws APIManagementException;

    /**
     * Add Subscription
     *
     * @param subscriber subscriber
     * @param application application
     * @param apiTypeWrapper API Details Object
     * @param status Status
     * @return Subscription ID
     * @throws APIManagementException if error
     */
    int addSubscription(ApiTypeWrapper apiTypeWrapper, Application application, String status, String subscriber)
            throws APIManagementException;

    /**
     * Removes a subscription by id by force without considering the subscription blocking state of the user
     *
     * @param subscription_id id of subscription
     * @throws APIManagementException
     */
    void removeSubscriptionById(int subscription_id) throws APIManagementException;

    /**
     * Add Subscription
     *
     * @param inputSubscriptionUUId subscription ID
     * @param requestedThrottlingTier new throttling tier
     * @param apiTypeWrapper API Details Object
     * @param status Status
     * @return Subscription ID
     * @throws APIManagementException if error
     */
    int updateSubscription(ApiTypeWrapper apiTypeWrapper, String inputSubscriptionUUId, String status,
                                  String requestedThrottlingTier) throws APIManagementException;

    /**
     * Retrieves subscription status for APIIdentifier and applicationId
     *
     * @param uuid    API subscribed
     * @param applicationId application with subscription
     * @return subscription status
     * @throws APIManagementException
     */
    String getSubscriptionStatus(String uuid, int applicationId) throws APIManagementException;

    /**
     * Retrieves subscription Id for APIIdentifier and applicationId
     *
     * @param uuid    API subscribed
     * @param applicationId application with subscription
     * @return subscription id
     * @throws APIManagementException
     */
    String getSubscriptionId(String uuid, int applicationId) throws APIManagementException;

    /**
     * Update subscription status for provided subscription ID
     *
     * @param subscriptionId    Subscription ID
     * @param status new status
     * @throws APIManagementException
     */
    void updateSubscriptionStatus(int subscriptionId, String status) throws APIManagementException;

    /**
     * Persist revoked jwt signatures to database.
     *
     * @param eventId
     * @param jwtSignature signature of jwt token.
     * @param expiryTime   expiry time of the token.
     * @param tenantId     tenant id of the jwt subject.
     */
    void addRevokedJWTSignature(String eventId, String jwtSignature, String type,
                                       Long expiryTime, int tenantId) throws APIManagementException;




}
