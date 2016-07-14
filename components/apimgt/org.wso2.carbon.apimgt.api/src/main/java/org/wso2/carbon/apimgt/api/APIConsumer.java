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

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.*;

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
     * Returns a list of all published APIs. If a given API has multiple APIs,
     * only the latest version will be included
     * in this list.
     *
     * @return set of API
     * @throws APIManagementException if failed to API set
     */
    Set<API> getAllPublishedAPIs(String tenantDomain) throws APIManagementException;
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
     * @return a list of all Tags applied to all APIs published.
     * @throws APIManagementException if failed to get All the tags
     */
    Set<Tag> getAllTags(String tenantDomain) throws APIManagementException;

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
    void rateAPI(APIIdentifier apiId, APIRating rating, String user) throws APIManagementException;
    /**
     * Remove an user rating of a particular API. This will be called when subscribers remove their rating on an API
     *
     * @param apiId  The API identifier
     * @param user Username of the subscriber providing the rating
     * @throws APIManagementException If an error occurs while rating the API
     */
    void removeAPIRating(APIIdentifier apiId, String user) throws APIManagementException;

    /** returns the SubscribedAPI object which is related to the subscriptionId
     *
     * @param subscriptionId subscription id
     * @return
     * @throws APIManagementException
     */
    SubscribedAPI getSubscriptionById(int subscriptionId) throws APIManagementException;


    /**
     * Returns a set of SubscribedAPI purchased by the given Subscriber
     *
     * @param subscriber Subscriber
     * @return Set<API>
     * @throws APIManagementException if failed to get API for subscriber
     */
    Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException;
    
    /**
     * @param subscriber the subscriber to be subscribed to the API
     * @param groupingId the groupId of the subscriber
     * @return the subscribed API's
     * @throws APIManagementException
     */
    Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber, String groupingId) throws APIManagementException;


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
     * @return
     * @throws APIManagementException
     */
    Map<String,Object> mapExistingOAuthClient(String jsonString, String userName, String clientId,
                                                     String applicationName, String keyType) throws APIManagementException;

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
     * Returns a set of SubscribedAPIs filtered by the given application name and in between starting and ending indexes.
     *
     * @param subscriber Subscriber
     * @param applicationName Application needed to find subscriptions
     * @param startSubIndex Starting index of subscriptions to be listed
     * @param endSubIndex Ending index of Subscriptions to be listed
     * @param groupingId the group id of the application
     * @return
     * @throws APIManagementException
     */
    Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber subscriber, String applicationName, int startSubIndex, int endSubIndex, String groupingId)
            throws APIManagementException;

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
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @return SubscriptionResponse subscription response object
     * @throws APIManagementException if failed to add subscription details to database
     */
    SubscriptionResponse addSubscription(APIIdentifier identifier, String userId, int applicationId)
            throws APIManagementException;

    /**
     * 
     * @param subscriptionId id of the subscription
     * @return
     * @throws APIManagementException if failed to get subscription detail from database
     */
    String getSubscriptionStatusById(int subscriptionId) throws APIManagementException;
 
    /**
     * Unsubscribe the specified user from the specified API in the given application
     *
     * @param identifier    APIIdentifier
     * @param userId        id of the user
     * @param applicationId Application Id
     * @throws APIManagementException if failed to remove subscription details from database
     */
    void removeSubscription(APIIdentifier identifier, String userId, int applicationId) throws APIManagementException;

    /** Removes a subscription specified by SubscribedAPI object
     * 
     * @param subscription SubscribedAPI object which contains the subscription information
     * @throws APIManagementException
     */
    void removeSubscription(SubscribedAPI subscription) throws APIManagementException;

    /**
     * Remove a Subscriber
     *
     * @param identifier APIIdentifier
     * @param userId     id of the user
     * @throws APIManagementException if failed to add subscription details to database
     */
    void removeSubscriber(APIIdentifier identifier, String userId) throws APIManagementException;

    /**
     * This method is to update the subscriber.
     *
     * @param identifier    APIIdentifier
     * @param userId        user id
     * @param applicationId Application Id
     * @throws APIManagementException if failed to update subscription
     */
    void updateSubscriptions(APIIdentifier identifier, String userId, int applicationId) throws APIManagementException;

    /**
     * @param identifier Api identifier
     * @param comment comment text
     * @param user Username of the comment author                        
     * @throws APIManagementException if failed to add comment for API
     */
    void addComment(APIIdentifier identifier, String comment, String user) throws APIManagementException;

    /**
     * @param identifier Api identifier
     * @return Comments
     * @throws APIManagementException if failed to get comments for identifier
     */
    Comment[] getComments(APIIdentifier identifier) throws APIManagementException;

    /**
     * Adds an application
     *
     * @param application Application
     * @param userId      User Id
     * @return Id of the newly created application
     * @throws APIManagementException if failed to add Application
     */
    int addApplication(Application application, String userId) throws APIManagementException;

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
     * @throws APIManagementException
     */
    void removeApplication(Application application) throws APIManagementException;

    /** get the status of the Application creation process given the application Id
     *
     * @param applicationId Id of the Application
     * @return
     * @throws APIManagementException
     */
    String getApplicationStatusById(int applicationId) throws APIManagementException;

    /**
     * Creates a request for getting Approval for Application Registration.
     *
     * @param userId Subsriber name.
     * @param applicationName of the Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime validity time period.
     * @param groupingId APIM application id.
     * @param jsonString Callback URL for the Application.
     * @param tokenScope Scopes for the requested tokens.
     *
     * @throws APIManagementException if failed to applications for given subscriber
     */
    Map<String,Object> requestApprovalForApplicationRegistration(String userId, String applicationName,
                                                                        String tokenType,
                                                                        String callbackUrl, String[] allowedDomains,
                                                                        String validityTime,
                                                                        String tokenScope, String groupingId, String jsonString)
        throws APIManagementException;


    /**
     * Creates a request for application update.
     *
     * @param userId Subsriber name.
     * @param applicationName of the Application.
     * @param tokenType Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl callback URL
     * @param allowedDomains allowedDomains for token.
     * @param validityTime validity time period.
     * @param groupingId APIM application id.
     * @param jsonString Callback URL for the Application.
     * @param tokenScope Scopes for the requested tokens.
     * @throws APIManagementException if failed to applications for given subscriber
     */
    OAuthApplicationInfo updateAuthClient(String userId, String applicationName,
                                               String tokenType,
                                               String callbackUrl, String[] allowedDomains,
                                               String validityTime,
                                               String tokenScope,
                                               String groupingId,
                                               String jsonString)
            throws APIManagementException;

    /**
     * Delete oAuth application from Key manager and remove key manager mapping from APIM.
     * @param consumerKey Client id of oAuthApplication.
     * @throws APIManagementException
     */
    void deleteOAuthApplication(String consumerKey) throws APIManagementException;



    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @param groupingId the groupId to which the applications must belong.
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    Application[] getApplications(Subscriber subscriber, String groupingId) throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *  @param subscriber Subscriber
     * @param search
     * @param start
     * @param offset
     * @param groupingId the groupId to which the applications must belong.  @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    Application[] getApplicationsWithPagination(Subscriber subscriber, String groupingId,int start , int offset ,
            String search, String sortColumn, String sortOrder)
            throws APIManagementException;


    /**
     * This will return APIM application by giving name and subscriber
     * @param userId APIM subscriber ID.
     * @param ApplicationName APIM application name.
     * @param groupId Group id.
     * @return it will return Application.
     * @throws APIManagementException
     */
    Application getApplicationsByName(String userId , String ApplicationName , String groupId) throws APIManagementException;

    /**
     * Returns the corresponding application given the Id
     * @param id Id of the Application
     * @return it will return Application corresponds to the id.
     * @throws APIManagementException
     */
    Application getApplicationById(int id) throws APIManagementException;
 
    /**
     * @param subscriber the subscriber in relation to the identifiers
     * @param identifier the identifiers of the API's the subscriber is subscribed to
     * @param groupingId the grouping Id the subscriber.
     * @return the set of subscribed API's.
     * @throws APIManagementException
     */
    Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber subscriber,
                                                       APIIdentifier identifier, String groupingId) throws APIManagementException;
    
    Set<APIIdentifier> getAPIByConsumerKey(String accessToken) throws APIManagementException;

    Set<API> searchAPI(String searchTerm, String searchType,String tenantDomain) throws APIManagementException;

    Map<String,Object> searchPaginatedAPIs(String searchTerm, String searchType,String tenantDomain,int start,int end, boolean limitAttributes) throws APIManagementException;
    
    int getUserRating(APIIdentifier apiId, String user) throws APIManagementException;

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
     * Check whether an application access token is already persist in database.
     * @param accessToken
     * @return
     * @throws APIManagementException
     */
    boolean isApplicationTokenExists(String accessToken) throws APIManagementException;

    /**
     * Returns a list of Tiers denied for the current user
     *
     * @return Set<String>
     * @throws APIManagementException if failed to get the tiers
     */
    Set<String> getDeniedTiers()throws APIManagementException;
    
    /**
     * Check whether given Tier is denied for the user
     * @param tierName
     * @return 
     * @throws APIManagementException if failed to get the tiers
     */
    boolean isTierDeneid(String tierName)throws APIManagementException;


    /**
     * Complete Application Registration process.If the Application registration fails before
     * generating the Access Tokens, this method should be used to resume registration.
     * @param userId Tenant Aware userID
     * @param applicationName Name of the Application
     * @param tokenType Type of the Token (PRODUCTION | SANDBOX)
     * @param tokenScope scope of the token
     * @param groupingId the application belongs to.
     * @return a Map containing the details of the OAuth application.
     * @throws APIManagementException if failed to get the tiers
     */
    Map<String, String> completeApplicationRegistration(String userId,
                                                               String applicationName,
                                                               String tokenType, String tokenScope,
															   String groupingId)
		    throws APIManagementException;


    /**
     * Returns details of an API information in low profile
     *
     * @param identifier APIIdentifier
     * @return An API object related to the given identifier or null
     * @throws APIManagementException if failed get API from APIIdentifier
     */
    API getLightweightAPI(APIIdentifier identifier) throws APIManagementException;
    
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
     * Revokes the oldAccessToken generating a new one.
     *
     * @param oldAccessToken          Token to be revoked
     * @param clientId                Consumer Key for the Application
     * @param clientSecret            Consumer Secret for the Application
     * @param validityTime            Desired Validity time for the token
     * @param jsonInput               Additional parameters if Authorization server needs any.
     * @return Details of the newly generated Access Token.
     * @throws APIManagementException
     */
    AccessTokenInfo renewAccessToken(String oldAccessToken, String clientId, String clientSecret, String validityTime,
                                     String[] requestedScopes, String jsonInput) throws
            APIManagementException;

	/**
	 * Returns a set of scopes associated with a list of API identifiers.
	 *
	 * @param identifiers list of API identifiers
	 * @return set of scopes.
	 * @throws APIManagementException
	 */
	Set<Scope> getScopesBySubscribedAPIs(List<APIIdentifier> identifiers) throws APIManagementException;

	/**
	 * Returns the scopes of an access token as a string
	 *
	 * @param accessToken access token you want to receive scopes for
	 * @return scopes of the access token as a string
	 * @throws APIManagementException
	 */
	String getScopesByToken(String accessToken) throws APIManagementException;

	/**
	 * Returns a set of scopes for a given space seperated scope key string
	 *
	 * @param scopeKeys a space seperated string of scope keys
	 * @param tenantId  tenant id
	 * @return set of scopes
	 * @throws APIManagementException
	 */
	Set<Scope> getScopesByScopeKeys(String scopeKeys, int tenantId) throws APIManagementException;

    String getGroupIds(String response) throws APIManagementException;

	JSONObject resumeWorkflow(Object[] args);

    boolean isMonetizationEnabled(String tenantDomain) throws APIManagementException;

}
