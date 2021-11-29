
/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * APIConsumer responsible for providing helper functionality
 */
public interface APIConsumer extends APIManager {

    /**
     * @param subscriberId id of the Subscriber
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber
     */
    Subscriber getSubscriber(String subscriberId) throws APIManagementException;

    /**
     * Returns a list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @return set of API having the given tag name
     * @throws APIManagementException if failed to get set of API
     */
    Set<API> getAPIsWithTag(String tag, String tenantDomain) throws APIManagementException;

    /**
     * Returns a paginated list of #{@link org.wso2.carbon.apimgt.api.model.API} bearing the selected tag
     *
     * @param tag name of the tag
     * @param start starting number
     * @param end ending number
     * @return set of API having the given tag name
     * @throws APIManagementException if failed to get set of API
     */
    Map<String,Object> getPaginatedAPIsWithTag(String tag, int start, int end, String tenantDomain) throws APIManagementException;

    /**
     * Returns a paginated list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending number
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Map<String,Object> getAllPaginatedPublishedAPIs(String tenantDomain, int start, int end) throws APIManagementException;

    /**
     * Returns a paginated list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     * Light weight implementation of getAllPaginatedPublishesAPIs.
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending number
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Map<String,Object> getAllPaginatedPublishedLightWeightAPIs(String tenantDomain, int start, int end)
            throws APIManagementException;

    /**
     * Returns top rated APIs
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return Set of API
     * @throws APIManagementException if failed to get top rated APIs
     */
    Set<API> getTopRatedAPIs(int limit) throws APIManagementException;

    /**
     * Get recently added APIs to the store
     *
     * @param limit if -1, no limit. Return everything else, limit the return list to specified value.
     * @return set of API
     * @throws APIManagementException if failed to get recently added APIs
     */
    Set<API> getRecentlyAddedAPIs(int limit,String tenantDomain) throws APIManagementException;

    /**
     * Get all tags of published APIs
     *
     * @param organization organization of the API
     * @return a list of all Tags applied to all APIs published.
     * @throws APIManagementException if failed to get All the tags
     */
    Set<Tag> getAllTags(String organization) throws APIManagementException;

    /**
     * Returns all tags with their descriptions.
     *
     * NOTE : The reason for having a separate method to get the tags with their attributes is,
     * because of the implementation of addition tag attributes.
     * Tag attributes are saved in a registry location with convention.
     * e.g.  governance/apimgt/applicationdata/tags/{tag_name}/description.txt.
     * In most of the use cases these attributes are not needed.
     * So not fetching the description if it is not needed is healthy for performance.
     *
     * @param tenantDomain Tenant domain.
     * @return The description of the tag.
     * @throws APIManagementException if there is a failure in getting the description.
     */
    Set<Tag> getTagsWithAttributes(String tenantDomain)throws APIManagementException;

    /**
     * Rate a particular API. This will be called when subscribers rate an API
     *
     * @param apiId  The API identifier
     * @param rating The rating provided by the subscriber
     * @param user Username of the subscriber providing the rating
     * @throws APIManagementException If an error occurs while rating the API
     */
    void rateAPI(String apiId, APIRating rating, String user) throws APIManagementException;
    /**
     * Remove an user rating of a particular API. This will be called when subscribers remove their rating on an API
     *
     * @param id  The identifier
     * @param user Username of the subscriber providing the rating
     * @throws APIManagementException If an error occurs while rating the API
     */
    void removeAPIRating(String id, String user) throws APIManagementException;

    /**
     * Remove an user rating of a particular API. This will be called when subscribers remove their rating on an API
     *
     * @param apiId         The api identifier
     * @param organization  Identifier of an organization
     * @throws APIManagementException If an error occurs while rating the API
     */
    void checkAPIVisibility(String apiId, String organization) throws APIManagementException;

    /** returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException;

    /**
     * Returns a set of SubscribedAPIs filtered by the given application name.
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String applicationName, String groupingId)
            throws APIManagementException;

    /**
     *
     * @param jsonString this string will contain oAuth app details
     * @param userName user name of logged in user.
     * @param clientId this is the consumer key of oAuthApplication
     * @param applicationName this is the APIM appication name.
     * @param keyType
     * @param tokenType this is theApplication Token Type. This can be either default or jwt.
     * @param keyManagerName
     * @return
     * @throws APIManagementException
     */
    Map<String, Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                               String applicationName, String keyType, String tokenType,
                                               String keyManagerName,String tenantDomain) throws APIManagementException;

    /**
     *This method will delete from application key mapping table and application registration table.
     *@param applicationName application Name
     *@param tokenType Token Type.
     *@param groupId group id.
     *@param userName user name.
     *@return
     *@throws APIManagementException
     */
    void cleanUpApplicationRegistration(String applicationName, String tokenType, String groupId, String userName)
            throws APIManagementException;

    /**
     *This method will delete from application key mapping table and application registration table.
     *@param applicationId application id
     *@param tokenType Token Type.
     *@return
     *@throws APIManagementException
     */
    void cleanUpApplicationRegistrationByApplicationId(int applicationId, String tokenType) throws APIManagementException;

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier APIIdentifier
     * @param userId        user id
     * @return true, if giving api identifier is already subscribed
     * @throws APIManagementException if failed to check the subscribed state
     */
    boolean isSubscribed(APIIdentifier apiIdentifier, String userId) throws APIManagementException;

    /**
     * Returns true if a given user has subscribed to the API
     *
     * @param apiIdentifier APIIdentifier
     * @param applicationId application Id
     * @param userId userId
     * @return true, if giving api identifier is already subscribed
     * @throws APIManagementException if failed to check the subscribed state
     */
    boolean isSubscribedToApp(APIIdentifier apiIdentifier, String userId, int applicationId) throws APIManagementException;

    /**
     * Returns the number of subscriptions for the given subscriber and app.
     *
     * @param subscriber Subscriber
     * @param applicationName Application
     * @return The number of subscriptions
     * @throws APIManagementException if failed to count the number of subscriptions.
     */
    Integer getSubscriptionCount(Subscriber subscriber,String applicationName,String groupingId) throws APIManagementException;

    /**
     * Add new Subscriber
     *
     * @param apiTypeWrapper    Identifier
     * @param userId        id of the user
     * @param application Application Id
     * @return SubscriptionResponse subscription response object
     * @throws APIManagementException if failed to add subscription details to database
     */
    SubscriptionResponse addSubscription(ApiTypeWrapper apiTypeWrapper, String userId, Application application)
            throws APIManagementException;

    /**
     * Update Existing Subscription
     *
     * @param apiTypeWrapper    Identifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @return SubscriptionResponse subscription response object
     * @throws APIManagementException if failed to add subscription details to database
     */
    SubscriptionResponse updateSubscription(ApiTypeWrapper apiTypeWrapper, String userId, Application applicationId,
                                            String subscriptionId, String currentThrottlingPolicy,
                                            String requestedThrottlingPolicy) throws APIManagementException;

    /**
     * Unsubscribe the specified user from the specified API in the given application
     *
     * @param identifier    Identifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @param organization  Organization
     * @throws APIManagementException if failed to remove subscription details from database
     */
    void removeSubscription(Identifier identifier, String userId, int applicationId, String organization) throws APIManagementException;

    /**
     * Unsubscribe the specified user from the specified API in the given application with GroupId
     *
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @param groupId       groupId of user
     * @param organization  Organization
     * @throws APIManagementException if failed to remove subscription details from database
     */
    void removeSubscription(APIIdentifier identifier, String userId, int applicationId, String groupId, String organization) throws
            APIManagementException;

    /** Removes a subscription specified by SubscribedAPI object
     *
     * @param subscription SubscribedAPI object which contains the subscription information
     * @param organization Organization
     * @throws APIManagementException
     */
    void removeSubscription(SubscribedAPI subscription, String organization) throws APIManagementException;

    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    void removeSubscriber(APIIdentifier identifier, String userId) throws APIManagementException;

    /**
     * @param identifier Api identifier
     * @param comment    comment text
     * @param user       Username of the comment author
     * @throws APIManagementException if failed to add comment for API
     * @deprecated This method needs to be removed once the Jaggery web apps are removed.
     */
    void addComment(APIIdentifier identifier, String comment, String user) throws APIManagementException;

    /**
     * This method is to add a comment.
     *
     * @param uuid Api uuid
     * @param comment    comment object
     * @param user       Username of the comment author
     * @throws APIManagementException if failed to add comment for API
     */
    String addComment(String uuid, Comment comment, String user) throws APIManagementException;

    /**
     * @param uuid      Api uuid
     * @param parentCommentID
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    Comment[] getComments(String uuid, String parentCommentID) throws APIManagementException;

    /**
     * This method is to get a comment of an API.
     *
     * @param apiTypeWrapper Api Type Wrapper
     * @param commentId      Comment ID
     * @param replyLimit
     * @param replyOffset
     * @return Comment
     * @throws APIManagementException if failed to get comments for identifier
     */
    Comment getComment(ApiTypeWrapper apiTypeWrapper, String commentId, Integer replyLimit, Integer replyOffset) throws
            APIManagementException;

    /**
     * @param apiTypeWrapper  Api type wrapper
     * @param parentCommentID
     * @param replyLimit
     * @param replyOffset
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    CommentList getComments(ApiTypeWrapper apiTypeWrapper, String parentCommentID, Integer replyLimit, Integer replyOffset) throws APIManagementException;

    /**
     * @param apiTypeWrapper Api Type Wrapper
     * @param commentId      comment ID
     * @param comment        Comment object
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    boolean editComment(ApiTypeWrapper apiTypeWrapper, String commentId, Comment comment) throws APIManagementException;

    /**
     * This method is to delete a comment.
     *
     * @param uuid API uuid
     * @param commentId  Comment ID
     * @throws APIManagementException if failed to delete comment for api uuid
     */
    void deleteComment(String uuid, String commentId) throws APIManagementException;

    /**
     * This method is to delete a comment.
     *
     * @param apiTypeWrapper API Type Wrapper
     * @param commentId      Comment ID
     * @return boolean
     * @throws APIManagementException if failed to delete comment for identifier
     */
    boolean deleteComment(ApiTypeWrapper apiTypeWrapper, String commentId) throws APIManagementException;

    /**
     * Adds an application
     *
     * @param application  Application
     * @param userId       User Id
     * @param organization Identifier of an organization
     * @return Id of the newly created application
     * @throws APIManagementException if failed to add Application
     */
    int addApplication(Application application, String userId, String organization) throws APIManagementException;

    /**
     * Updates the details of the specified user application.
     *
     * @param application Application object containing updated data
     * @throws APIManagementException If an error occurs while updating the application
     */
    void updateApplication(Application application) throws APIManagementException;

    /**
     * Function to remove an Application from the API Store
     * @param application - The Application Object that represents the Application
     * @param username
     * @throws APIManagementException
     */
    void removeApplication(Application application, String username) throws APIManagementException;

    /**
     * Creates a request for getting Approval for Application Registration.
     *
     * @param userId Subscriber name.
     * @param application The Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime validity time period.
     * @param tokenScope Scopes for the requested tokens.
     *
     * @param jsonString Callback URL for the Application.
     * @param keyManagerName key manager name
     * @param tenantDomain tenant domain for the app registration request
     * @param isImportMode whether Application is being imported from controller or not
     * @throws APIManagementException if failed to applications for given subscriber
     */
    Map<String,Object> requestApprovalForApplicationRegistration(String userId, Application application,
                                                                 String tokenType,
                                                                 String callbackUrl, String[] allowedDomains,
                                                                 String validityTime,
                                                                 String tokenScope,
                                                                 String jsonString, String keyManagerName,
                                                                 String tenantDomain, boolean isImportMode)
            throws APIManagementException;

    /**
     * Creates a request for getting Approval for Application Registration.
     *
     * @param userId          Subscriber name.
     * @param application    The Application.
     * @param tokenType       Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl     callback URL
     * @param allowedDomains  allowedDomains for token.
     * @param validityTime    validity time period.
     * @param tokenScope      Scopes for the requested tokens.
     * @param groupingId      APIM application id.
     * @param jsonString      Callback URL for the Application.
     * @param keyManagerName  name of the key manager
     * @param tenantDomain    tenant domain for the app registration request
     * @throws APIManagementException if failed to applications for given subscriber
     * @deprecated Use {@link #requestApprovalForApplicationRegistration(String, Application, String, String, String[],
     * String, String, String, String, String, boolean)} instead
     */
    @Deprecated
    Map<String, Object> requestApprovalForApplicationRegistration(String userId, Application application,
                                                                 String tokenType,
                                                                 String callbackUrl, String[] allowedDomains,
                                                                 String validityTime,
                                                                 String tokenScope, String groupingId,
                                                                 String jsonString, String keyManagerName,
                                                                 String tenantDomain)
            throws APIManagementException;

    /**
     * Creates a request for application update.
     *
     * @param userId Subscriber name.
     * @param application The Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime validity time period.
     * @param tokenScope Scopes for the requested tokens.
     * @param groupingId APIM application id.
     * @param jsonString Callback URL for the Application.
     * @param keyManagerName
     * @throws APIManagementException if failed to applications for given subscriber
     */
    OAuthApplicationInfo updateAuthClient(String userId, Application application,
                                          String tokenType,
                                          String callbackUrl, String[] allowedDomains,
                                          String validityTime,
                                          String tokenScope,
                                          String groupingId,
                                          String jsonString, String keyManagerName)
            throws APIManagementException;

    /**
     * Updates the application owner of a given application
     * @param newUserId the new user ID which will be updated
     * @param application the application which should be updated
     * @return
     * @throws APIManagementException
     */
    boolean updateApplicationOwner(String newUserId , String organization, Application application ) throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber   Subscriber
     * @param search
     * @param start
     * @param offset
     * @param groupingId   the groupId to which the applications must belong.
     * @param organization Identifier of an organization
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId, int start, int offset,
                                                String search, String sortColumn, String sortOrder, String organization)
            throws APIManagementException;

    /**
     * Returns the corresponding application given the Id
     * @param id Id of the Application
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    Application getApplicationById(int id) throws APIManagementException;

    /**
     * Returns the corresponding application given the Id, user id and groups
     *
     * @param id      Id of the Application
     * @param userId  APIM subscriber ID.
     * @param groupId Group id.
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    Application getApplicationById(int id, String userId, String groupId) throws APIManagementException;

    /**
     * @param subscriber the subscriber in relation to the identifiers
     * @param identifier the identifiers of the API's the subscriber is subscribed to
     * @param groupingId the grouping Id the subscriber.
     * @param organization  organization of the API
     * @return the set of subscribed API's.
     * @throws APIManagementException
     */
    Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber,
                                                Identifier identifier, String groupingId, String organization) throws APIManagementException;

    Set<API> searchAPI(String searchTerm, String searchType,String tenantDomain) throws APIManagementException;

    Map<String,Object> searchPaginatedAPIs(String searchTerm, String searchType,String tenantDomain,int start,int end, boolean limitAttributes) throws APIManagementException;

    int getUserRating(String apiId, String user) throws APIManagementException;

    JSONObject getUserRatingInfo(String id, String user) throws APIManagementException;

    float getAverageAPIRating(String apiId) throws APIManagementException;

    JSONArray getAPIRatings(String apiId) throws APIManagementException;

    /**
     * Get a list of published APIs by the given provider.
     *
     * @param providerId , provider id
     * @param loggedUser logged user
     * @param limit Maximum number of results to return. Pass -1 to get all.
     * @param apiOwner Owner name which is used to filter APIs
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    Set<API> getPublishedAPIsByProvider(String providerId, String loggedUser, int limit, String apiOwner,
                                        String apiBizOwner) throws APIManagementException;

    /** Get a list of published APIs by the given provider.
     *
     * @param providerId , provider id
     * @param limit Maximum number of results to return. Pass -1 to get all.
     * @return set of API
     * @throws APIManagementException if failed to get set of API
     */
    Set<API> getPublishedAPIsByProvider(String providerId, int limit) throws APIManagementException;

    /**
     * Returns a list of Tiers denied for the current user
     *
     * @return Set<String>
     * @throws APIManagementException if failed to get the tiers
     */
    Set<String> getDeniedTiers()throws APIManagementException;

    /**
     * Returns a list of Tiers denied based on restrictions defined for API provider tenant domain
     *
     * @param providerTenantId tenant id of API provider
     * @return Set<String>
     * @throws APIManagementException if failed to get the tiers
     */
    @Deprecated
    Set<String> getDeniedTiers(int providerTenantId) throws APIManagementException;

    /**
     * Returns a list of Tiers denied based on restrictions defined for API provider tenant domain
     *
     * @param organization organization of API provider
     * @return Set<String>
     * @throws APIManagementException if failed to get the tiers
     */
    Set<String> getDeniedTiers(String organization) throws APIManagementException;

    /**
     * Returns a list of TierPermissions
     *
     * @return Set<TierPermission>
     */
    Set<TierPermission> getTierPermissions() throws APIManagementException;

    /**
     * Check whether given Tier is denied for the user
     * @param tierName
     * @return
     * @throws APIManagementException if failed to get the tiers
     */
    boolean isTierDeneid(String tierName)throws APIManagementException;

    /**
     * Returns details of an API information in low profile
     *
     * @param identifier APIIdentifier
     * @param orgId  Identifier of an organization
     * @return An API object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getLightweightAPI(APIIdentifier identifier, String orgId) throws APIManagementException;

    /**
     * Returns a paginated list of all APIs in given Status. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending number
     * @param returnAPITags If true, tags of each API is returned
     * @return set of API
     * @throws APIManagementException if failed to API set
     */

    Map<String,Object> getAllPaginatedAPIsByStatus(String tenantDomain,int start,int end, String Status,
                                                   boolean returnAPITags) throws APIManagementException;

    /**
     * Returns a paginated list of all APIs in given Status list. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending number
     * @param Status One or more Statuses
     * @param returnAPITags If true, tags of each API is returned
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Map<String,Object> getAllPaginatedAPIsByStatus(String tenantDomain,int start,int end, String[] Status,
                                                   boolean returnAPITags) throws APIManagementException;
    /**
     * Returns a paginated list of all APIs in given Status list. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     * Light wieght implementation of getAllPaginatedAPIsByStatus
     * @param tenantDomain tenant domain
     * @param start starting number
     * @param end ending numbeer
     * @param returnAPITags If true, tags of each API is returned
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Map<String,Object> getAllPaginatedLightWeightAPIsByStatus(String tenantDomain,int start,int end, String[] Status,
                                                              boolean returnAPITags) throws APIManagementException;

    /**
     * Returns the swagger definition of the API for the given gateway environment as a string
     *
     * @param api
     * @param environmentName API Gateway environment name
     * @return swagger string
     * @throws APIManagementException if error occurred while obtaining the swagger definition
     */
    String getOpenAPIDefinitionForEnvironment(API api, String environmentName)
            throws APIManagementException;

    /**
     * Revokes the oldAccessToken generating a new one.
     *
     * @param oldAccessToken  Token to be revoked
     * @param clientId        Consumer Key for the Application
     * @param clientSecret    Consumer Secret for the Application
     * @param validityTime    Desired Validity time for the token
     * @param requestedScopes Requested Scopes
     * @param jsonInput       Additional parameters if Authorization server needs any.
     * @param keyManagerName  Configured Key Manager
     * @param grantType       Grant Type
     * @return AccessTokenInfo
     * @throws APIManagementException Error when renewing access token
     */
    AccessTokenInfo renewAccessToken(String oldAccessToken, String clientId, String clientSecret, String validityTime,
                                     String[] requestedScopes, String jsonInput, String keyManagerName,
                                     String grantType) throws APIManagementException;

    /**
     * Generates a new api key
     *
     * @param application          The Application Object that represents the Application.
     * @param userName             Username of the user requesting the api key.
     * @param validityPeriod       Requested validity period for the api key.
     * @param permittedIP          Permitted IP addresses for the api key.
     * @param permittedReferer     Permitted referrers for the api key.
     * @return Generated api key.
     * @throws APIManagementException
     */
    String generateApiKey(Application application, String userName, long validityPeriod, String permittedIP,
                          String permittedReferer)
            throws APIManagementException;

    /**
     * Regenerate new consumer secret.
     *
     * @param clientId For which consumer key we need to regenerate consumer secret.
     * @param keyManagerName
     * @return New consumer secret.
     * @throws APIManagementException This is the custom exception class for API management.
     */
    String renewConsumerSecret(String clientId, String keyManagerName) throws APIManagementException;

    /**
     * Returns a set of scopes associated with a list of API uuids.
     *
     * @param uuids list of API uuids
     * @return set of scopes.
     * @throws APIManagementException
     */
    Set<Scope> getScopesBySubscribedAPIs(List<String> uuids) throws APIManagementException;

    /**
     * Returns a set of scopes associated with an application subscription.
     *
     * @param username    subscriber of the application
     * @param applicationId applicationId of the application
     * @param organization Organization
     * @return set of scopes.
     * @throws APIManagementException
     */
    Set<Scope> getScopesForApplicationSubscription(String username, int applicationId, String organization)
            throws APIManagementException;

    /**
     * Returns the groupId of a specific Application when the Id is provided
     *
     * @param appId applicationId
     * @return groupId of the application
     * @throws APIManagementException If failed to fetch the groupId
     */
    String getGroupId (int appId) throws APIManagementException;

    String[] getGroupIds(String response) throws APIManagementException;

    JSONObject resumeWorkflow(Object[] args);

    boolean isMonetizationEnabled(String tenantDomain) throws APIManagementException;

    /**
     * This methods loads the monetization implementation class
     *
     * @return monetization implementation class
     * @throws APIManagementException if failed to load monetization implementation class
     */
    Monetization getMonetizationImplClass() throws APIManagementException;

    /**
     * Returns wsdl document resource to be downloaded from the API store for a SOAP api
     *
     * @param username           user name of the logged in user
     * @param tenantDomain       tenant domain
     * @param resourceUrl        registry resource url to the wsdl
     * @param environmentDetails map of gateway names and types
     * @param apiDetails         api details
     * @return converted wsdl document to be download
     * @throws APIManagementException
     */
    String getWSDLDocument(String username, String tenantDomain, String resourceUrl, Map environmentDetails,
                           Map apiDetails) throws APIManagementException;

    /**
     * Returns the WSDL ResourceFile (Single WSDL or ZIP) for the provided API and environment details
     *
     * @param api               API
     * @param environmentName   environment name
     * @param environmentType   environment type
     * @param organization      Identifier of an organization
     * @return WSDL of the API
     * @throws APIManagementException when error occurred while getting the WSDL
     */
    ResourceFile getWSDL(API api, String environmentName, String environmentType, String organization)
            throws APIManagementException;
    /**
     * Returns application attributes defined in configuration
     *
     * @param userId  userId of the logged in user
     * @return Array of JSONObjects of key values from configuration
     * @throws APIManagementException
     */
    JSONArray getAppAttributesFromConfig(String userId) throws APIManagementException;

    Set<SubscribedAPI> getLightWeightSubscribedIdentifiers(String organization, Subscriber subscriber, APIIdentifier apiIdentifier, String groupingId) throws APIManagementException;

    Set<APIKey> getApplicationKeysOfApplication(int applicationId) throws APIManagementException;

    Set<APIKey> getApplicationKeysOfApplication(int applicationId, String xWso2Tenant) throws APIManagementException;

    void revokeAPIKey(String apiKey, long expiryTime, String tenantDomain) throws APIManagementException;

    /**
     * Updates the details of the specified user application.
     *
     * @param query Search query typed by the user at the devportal
     * @param username Name of the user typing the search query
     * @throws APIManagementException If an error occurs while updating the application
     */
    void publishSearchQuery(String query, String username) throws APIManagementException;

    /**
     * Publish the clicked APIs for the use of API recommendation system.
     *
     * @param api API clicked by the user
     * @param username Name of the user who clicked the API
     * @throws APIManagementException If an error occurs while publishing clicked API
     */
    void publishClickedAPI(ApiTypeWrapper api, String username) throws APIManagementException;

    /**
     * Checks whether the API recommendation feature is enabled.
     *
     * @param tenantDomain       tenant domain
     * @throws APIManagementException if an error occurs while reading configs
     */
    boolean isRecommendationEnabled(String tenantDomain) throws APIManagementException;

    /**
     * Get API recommendations for a given user..
     *
     * @param userName API clicked by the user
     * @param tenantDomain Tenant Domain
     * @throws APIManagementException If an error occurs while publishing clicked API
     */
    String getApiRecommendations(String userName, String tenantDomain) throws APIManagementException;

    /**
     * Get the requested tenant Domain from consumer.
     *
     */
    String getRequestedTenant();

    /**
     *
     * @param apiId API UUID
     * @return Set of Topics defined in a specified Async API
     * @throws APIManagementException if an error occurs while retrieving data
     */
    Set<Topic> getTopics(String apiId) throws APIManagementException;

    /**
     * Retrieves webhook subscriptions for a webhook API
     *
     * @param applicationId Application UUID
     * @param apiId         API UUID
     * @return Set of Subscriptions of application to a API
     * @throws APIManagementException if an error occurs while retrieving data
     */
    Set<Subscription> getTopicSubscriptions(String applicationId, String apiId) throws APIManagementException;

    void cleanUpApplicationRegistrationByApplicationIdAndKeyMappingId(int applicationId, String keyMappingId)
            throws APIManagementException;

    APIKey getApplicationKeyByAppIDAndKeyMapping(int applicationId, String keyMappingId)
            throws APIManagementException;

    void changeUserPassword(String currentPassword, String newPassword) throws APIManagementException;

    /**
     * Returns the AsyncAPI definition of the API for the given microgateway gateway label as a string
     *
     * @param apiId id of the APIIdentifier
     * @param labelName name of the microgateway label
     * @return AsyncAPI definition string
     * @throws APIManagementException if error occurred while obtaining the AsyncAPI definition
     */
    String getAsyncAPIDefinitionForLabel(Identifier apiId, String labelName)
            throws APIManagementException;

    /**
     * Get an API Revisions Deployment mapping details of API by providing API uuid
     *
     * @param apiUUID API UUID
     * @return List<APIRevisionDeployment> Object
     * @throws APIManagementException if failed to get the related API revision Deployment Mapping details
     */
    List<APIRevisionDeployment> getAPIRevisionDeploymentListOfAPI(String apiUUID) throws APIManagementException;

    /**
     * Retrieve Subscribed APIS by Application.
     *
     * @param application The Application Object that represents the Application.
     * @param offset starting index.
     * @param limit no of entries to retrieve.
     * @param organization organization to retrieve.
     * @return SubscribedAPI set of application.
     * @throws APIManagementException if failed to retrieve Subscriptions of Application.
     */
    Set<SubscribedAPI> getPaginatedSubscribedAPIsByApplication(Application application, Integer offset, Integer limit
            , String organization) throws APIManagementException;

    /**
     *Retrieves the ThrottlePolicies From organization.
     *
     * @param policyType type of Policies to retrieve
     * @param organization organization to retrieve.
     * @return List of {@link Tier}
     * @throws APIManagementException if failed to Retrieve throttling Policies.
     */
    List<Tier> getThrottlePolicies(int policyType, String organization) throws APIManagementException;

    /**
     * Retrieve the Policy by Id,type and organization.
     * @param name name of policy.
     * @param policyType type of Policy.
     * @param organization organization organization to retrieve.
     * @return Tier.
     * @throws APIManagementException if failed to retrieve policy.
     */
    Tier getThrottlePolicyByName(String name, int policyType, String organization) throws APIManagementException;
}
