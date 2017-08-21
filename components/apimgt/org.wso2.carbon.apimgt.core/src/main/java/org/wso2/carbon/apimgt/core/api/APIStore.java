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

import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.exception.APICommentException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.APINotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIRatingException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.models.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;

import java.io.InputStream;
import java.util.List;

/**
 * This interface used to write Store specific methods.
 *
 */
public interface APIStore extends APIManager {

    /**
     * Returns details of an API
     *
     * @param id ID of the API
     * @return An CompositeAPI object for the given id or null
     * @throws APIManagementException if failed get CompositeAPI for given id
     */
    CompositeAPI getCompositeAPIbyId(String id) throws APIManagementException;

    /**
     * Returns a paginated list of all APIs in given Status list. If a given API has multiple APIs,
     * only the latest version will be included in this list.
     *
     * @param offset offset
     * @param limit  limit
     * @param status One or more Statuses
     * @return {@code List<API>}
     * @throws APIManagementException if failed to API set
     */
    List<API> getAllAPIsByStatus(int offset, int limit, String[] status) throws APIManagementException;

    /**
     * Returns a paginated list of all APIs which match the given search criteria.
     *
     * @param query searchType
     * @param limit limit
     * @param offset offset
     * @return {@code List<API>}
     * @throws APIManagementException   If failed to search apis.
     */
    List<API> searchAPIs(String query, int offset, int limit) throws APIManagementException;

    /**
     * Returns a paginated list of all Composite APIs which match the given search criteria.
     *
     * @param query searchType
     * @param limit limit
     * @param offset offset
     * @return {@code List<CompositeAPI>}
     * @throws APIManagementException   If failed to search apis.
     */
    List<CompositeAPI> searchCompositeAPIs(String query, int offset, int limit) throws APIManagementException;


    /**
     * Returns Swagger definition of a Composite API
     *
     * @param id ID of the API
     * @return The CompositeAPI Swagger definition
     * @throws APIManagementException if failed get CompositeAPI implementation for given id
     */
    String getCompositeApiDefinition(String id) throws APIManagementException;

    /**
     * Checks the existence of a Composite API with {@code apiId}.
     *
     * @param apiId API id of the Composite API
     * @return {@code true} if Composite API with {@code apiId} exists
     * {@code false} otherwise.
     * @throws APIManagementException if failed to retrieve summary of Composite API with {@code apiId}
     */
    boolean isCompositeAPIExist(String apiId) throws APIManagementException;

    /**
     * Update Swagger definition of a Composite API
     *
     * @param id ID of the API
     * @param apiDefinition  CompositeAPI Swagger definition
     * @throws APIManagementException if failed get CompositeAPI implementation for given id
     */
    void updateCompositeApiDefinition(String id, String apiDefinition) throws APIManagementException;

    /**
     * Returns Ballerina implementation of a Composite API
     *
     * @param id ID of the API
     * @return File of the CompositeAPI implementation
     * @throws APIManagementException if failed get CompositeAPI implementation for given id
     */
    InputStream getCompositeApiImplementation(String id) throws APIManagementException;

    /**
     * Update Ballerina implementation of a Composite API
     *
     * @param id ID of the API
     * @param implementation  CompositeAPI Ballerina implementation file
     * @throws APIManagementException if failed get CompositeAPI implementation for given id
     */
    void updateCompositeApiImplementation(String id, InputStream implementation) throws APIManagementException;

    /**
     * Function to remove an Application from the API Store
     *
     * @param appId - The Application id of the Application
     * @return WorkflowResponse workflow response
     * @throws APIManagementException   If failed to delete application.
     */
    WorkflowResponse deleteApplication(String appId) throws APIManagementException;

    /**
     * Adds an application
     *
     * @param application Application
     * @return ApplicationCreationResponse
     * @throws APIManagementException if failed to add Application
     */
    ApplicationCreationResponse addApplication(Application application) throws APIManagementException;

    /**
     * This will return APIM application by giving name and subscriber
     *
     * @param applicationName APIM application name
     * @param ownerId          Application owner ID.
     * @return it will return Application.
     * @throws APIManagementException Failed to get application by name.
     */
    Application getApplicationByName(String applicationName, String ownerId) throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    List<Application> getApplications(String subscriber) throws APIManagementException;

    /**
     * Updates the details of the specified user application.
     * @param uuid Uuid of the existing application
     * @param application Application object containing updated data
     * @return WorkflowResponse workflow status
     * @throws APIManagementException If an error occurs while updating the application
     */
    WorkflowResponse updateApplication(String uuid, Application application) throws APIManagementException;

    /**
     * Generates oAuth keys for an application.
     *
     * @param applicationId Id of the Application.
     * @param keyType       Key type (PRODUCTION | SANDBOX)
     * @param callbackUrl   Callback URL
     * @param grantTypes    List of grant types to be supported by the application
     * @return {@link OAuthApplicationInfo}  Generated OAuth client information
     * @throws APIManagementException If oauth application creation was failed
     */
    OAuthApplicationInfo generateApplicationKeys(String applicationId, String keyType,
                                                 String callbackUrl, List<String> grantTypes)
            throws APIManagementException;

    /**
     * Provision out-of-band OAuth clients (Semi-manual client registration)
     *
     * @param applicationId Application ID
     * @param keyType       Key type (PRODUCTION | SANDBOX)
     * @param clientId      Client ID of the OAuth application
     * @param clientSecret  Client secret of the OAuth application
     * @return {@link OAuthApplicationInfo}  Existing OAuth client information
     * @throws APIManagementException If oauth application mapping was failed
     */
    OAuthApplicationInfo mapApplicationKeys(String applicationId, String keyType, String clientId,
                                            String clientSecret) throws APIManagementException;

    /**
     * Get application key information
     *
     * @param applicationId Application Id
     * @return {@link OAuthApplicationInfo}  Application key information list
     * @throws APIManagementException if error occurred while retrieving application keys
     */
    List<OAuthApplicationInfo> getApplicationKeys(String applicationId) throws APIManagementException;

    /**
     * Get application key information of a given key type
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @return {@link OAuthApplicationInfo}  Application key information
     * @throws APIManagementException if error occurred while retrieving application keys
     */
    OAuthApplicationInfo getApplicationKeys(String applicationId, String keyType) throws APIManagementException;

    /**
     * Update grantTypes and callback URL of an application
     *
     * @param applicationId Application Id
     * @param keyType       Key Type (Production | Sandbox)
     * @param grantTypes    New Grant Type list
     * @param callbackURL   New callback URL
     * @return {@link OAuthApplicationInfo}  Application key information list
     * @throws APIManagementException if error occurred while retrieving application keys
     */
    OAuthApplicationInfo updateGrantTypesAndCallbackURL(String applicationId, String keyType, List<String> grantTypes,
                                                        String callbackURL) throws APIManagementException;

    /**
     * Generate an application access token (and revoke current token, if any)
     *
     * @param clientId         Consumer Key
     * @param clientSecret     Consumer Secret
     * @param scopes           Scope of the token
     * @param validityPeriod   Token validity period
     * @param tokenToBeRevoked Current access token which needs to be revoked
     * @return {@link ApplicationToken} object which contains access token, scopes and validity period
     * @throws APIManagementException if error occurred while generating access token
     */
    ApplicationToken generateApplicationToken(String clientId, String clientSecret, String scopes, long validityPeriod,
                                              String tokenToBeRevoked) throws APIManagementException;

    /**
     * Retrieve an application given the uuid.
     *
     * @param uuid  UUID of the application.
     * @return Application object of the given uuid
     * @throws APIManagementException   If failed to get the application.
     */
    Application getApplicationByUuid(String uuid) throws APIManagementException;

    /**
     * Retrieve list of subscriptions given the application.
     *
     * @param application   Application Object.
     * @return List of subscriptions objects of the given application.
     * @throws APIManagementException If failed to get the subscriptions for the application.
     */
    List<Subscription> getAPISubscriptionsByApplication(Application application) throws APIManagementException;


    /**
     * Retrieve list of subscriptions given the application and the API Type.
     *
     * @param application   Application Object.
     * @param apiType   API Type to filter subscriptions
     * @return List of subscriptions objects of the given application made for the specified API Type.
     * @throws APIManagementException If failed to get the subscriptions for the application.
     */
    List<Subscription> getAPISubscriptionsByApplication(Application application, ApiType apiType)
                                                                                throws APIManagementException;

    /**
     * Add an api subscription.
     *
     * @param apiId             UUID of the API.
     * @param applicationId     UUID of the Application
     * @param tier              Tier level.
     * @return SubscriptionResponse  Id and the workflow response
     * @throws APIManagementException   If failed to add the subscription
     */
    SubscriptionResponse addApiSubscription(String apiId, String applicationId, String tier)
            throws APIManagementException;

    /**
     * Delete an API subscription.
     *
     * @param subscriptionId    Id of the subscription to be deleted.
     * @return WorkflowResponse  workflow response
     * @throws APIManagementException   If failed to delete the subscription.
     */
    WorkflowResponse deleteAPISubscription(String subscriptionId) throws APIManagementException;

    /**
     * Retrieve all tags
     *
     * @return  List of Tag objects
     * @throws APIManagementException   If failed to retrieve tags
     */
    List<Tag> getAllTags() throws APIManagementException;

    /**
     * Retrieve all policies of given tier level.
     * @param tierLevel Tier level.
     * @return  List of policies for the given tier level.
     * @throws APIManagementException   If failed to get policies.
     */
    List<Policy> getPolicies(APIMgtAdminService.PolicyLevel tierLevel) throws APIManagementException;

    /**
     * Retrieve all policies of given tier level.
     * @param tierLevel Level of the tier.
     * @param tierName  Name of the tier.
     * @return  Policy object.
     * @throws APIManagementException   If failed to get the policy.
     */
    Policy getPolicy(APIMgtAdminService.PolicyLevel tierLevel, String tierName) throws APIManagementException;

    /**
     * Retrieve Label information based on the label name
     *
     * @param labels    List of label names
     * @param username  Username of the user
     * @return {@code List<Label>} List of Labels
     * @throws LabelException if failed to get labels
     */
    List<Label> getLabelInfo(List<String> labels, String username) throws LabelException;

    /**
     * Retrieve List of all user Ratings based on API ID
     *
     * @param apiId UUID of the API
     * @return List of Rating Objects
     * @throws APIManagementException if failed to get labels
     */
    List<Rating> getRatingsListForApi(String apiId) throws APIManagementException;

    /**
     * Add comment for an API
     *
     * @param comment the comment text
     * @param apiId   UUID of the API
     * @return String UUID of the created comment
     * @throws APICommentException if failed to add a comment
     * @throws APIMgtResourceNotFoundException if api not found
     */
    String addComment(Comment comment, String apiId) throws APICommentException, APIMgtResourceNotFoundException;

    /**
     * Delete a comment from an API given the commentId and apiId
     *
     * @param commentId UUID of the comment to be deleted
     * @param apiId     UUID of the api
     * @param username username of the consumer
     * @throws APICommentException if failed to delete a comment
     * @throws  APIMgtResourceNotFoundException if api or comment not found
     */
    void deleteComment(String commentId, String apiId, String username) throws APICommentException,
            APIMgtResourceNotFoundException;

    /**
     * Update a comment
     *
     * @param comment   new Comment object
     * @param commentId the id of the comment which needs to be updated
     * @param apiId     UUID of the api the comment belongs to
     * @param username  username of the consumer
     * @throws APICommentException if failed to update a comment
     * @throws APIMgtResourceNotFoundException if api or comment not found
     */
    void updateComment(Comment comment, String commentId, String apiId, String username) throws APICommentException,
            APIMgtResourceNotFoundException;

    /**
     * Retrieve list of comments for a given apiId
     *
     * @param apiId UUID of the api
     * @return a list of comments for the api
     * @throws APICommentException if failed to retrieve all comments for an api
     * @throws APIMgtResourceNotFoundException if api not found
     */
    List<Comment> getCommentsForApi(String apiId) throws APICommentException, APIMgtResourceNotFoundException;

    /**
     * Retrieve Individual Comment based on Comment ID
     *
     * @param commentId UUID od the comment
     * @param apiId     UUID of the API
     * @return Comment Object.
     * @throws APICommentException if failed to retrieve comment from data layer
     * @throws APIMgtResourceNotFoundException if api or comment was not found
     */
    Comment getCommentByUUID(String commentId, String apiId) throws APICommentException,
            APIMgtResourceNotFoundException;

    /**
     * Creates a new rating
     *
     * @param apiId  UUID of the api
     * @param rating the rating object
     * @return UUID of the newly created or updated rating
     * @throws APIRatingException if failed to add rating
     * @throws  APIMgtResourceNotFoundException if api not found
     */
    String addRating(String apiId, Rating rating) throws APIRatingException, APIMgtResourceNotFoundException;

    /**
     * Retrieves a rating given its UUID
     *
     * @param apiId    UUID of the api
     * @param ratingId UUID of the rating
     * @return rating object
     * @throws APIRatingException if failed to get rating
     * @throws  APIMgtResourceNotFoundException if api or rating not found
     */
    Rating getRatingByUUID(String apiId, String ratingId) throws APIRatingException, APIMgtResourceNotFoundException;

    /**
     * Retrieve Average Rating based of an api, up to one decimal point.
     *
     * @param apiId UUID of the API
     * @return Average Rating value
     * @throws APIRatingException if failed to get average rating
     * @throws  APIMgtResourceNotFoundException if api not found
     */
    double getAvgRating(String apiId) throws APIRatingException, APIMgtResourceNotFoundException;

    /**
     * Get user rating for an api
     *
     * @param apiId  UUID of the api
     * @param userId unique id of the user
     * @return Rating of the user
     * @throws APIRatingException if failed to retrieve user rating for the given api
     * @throws APIMgtResourceNotFoundException if api or rating not found
     */
    Rating getRatingForApiFromUser(String apiId, String userId) throws APIRatingException,
            APIMgtResourceNotFoundException;

    /**
     * Updates an already existing api
     *
     * @param apiId             UUID of the api
     * @param ratingId          UUID of the rating
     * @param ratingFromPayload Rating object created from the request payload
     * @throws APIRatingException if failed to update a rating
     * @throws  APIMgtResourceNotFoundException if api or rating not found
     */
    void updateRating(String apiId, String ratingId, Rating ratingFromPayload) throws APIRatingException,
            APIMgtResourceNotFoundException;
    /**
     * Adds a new Composite API
     *
     * @param apiBuilder API Builder object
     * @return Details of the added Composite API.
     * @throws APIManagementException if failed to add Composite API
     */
    String addCompositeApi(CompositeAPI.Builder apiBuilder) throws APIManagementException;

    /**
     * Updates design and implementation of an existing Composite API.
     *
     * @param apiBuilder {@code org.wso2.carbon.apimgt.core.models.API.APIBuilder}
     * @throws APIManagementException if failed to update Composite API
     */
    void updateCompositeApi(CompositeAPI.Builder apiBuilder) throws APIManagementException;

    /**
     * Delete an existing Composite API.
     *
     * @param apiId API Id
     * @throws APIManagementException if failed to delete Composite API
     */
    void deleteCompositeApi(String apiId) throws APIManagementException;

    /**
     * Create a new version of the <code>Composite API</code>, with version <code>newVersion</code>
     *
     * @param apiId      The Composite API to be copied
     * @param newVersion The version of the new Composite API
     * @return Details of the newly created version of the Composite API.
     * @throws APIManagementException If an error occurs while trying to create
     *                                the new version of the Composite API
     */
    String createNewCompositeApiVersion(String apiId, String newVersion) throws APIManagementException;

    /**
     * Create Composite API from Swagger Definition
     *
     * @param apiDefinition Swagger content of the Composite API.
     * @return Details of the added Composite API.
     * @throws APIManagementException If failed to add Composite API.
     */
    String addCompositeApiFromDefinition(InputStream apiDefinition) throws APIManagementException;

    /**
     * Create Composite API from Swagger definition located by a given url
     *
     * @param swaggerResourceUrl url of the Swagger resource
     * @return details of the added Composite API.
     * @throws APIManagementException If failed to add the Composite API.
     */
    String addCompositeApiFromDefinition(String swaggerResourceUrl) throws APIManagementException;

    /**
     * Returns the WSDL of a given API UUID and gateway label name
     * 
     * @param apiId API Id
     * @param labelName gateway label name
     * @return WSDL of the API as {@link String}
     * @throws APIMgtDAOException if error occurs while accessing the WSDL from the data layer
     * @throws APIMgtWSDLException if error occurs while parsing/manipulating the WSDL
     * @throws APINotFoundException If API cannot be found
     * @throws LabelException If Label related error occurs
     */
    String getAPIWSDL(String apiId, String labelName)
            throws APIMgtDAOException, APIMgtWSDLException, APINotFoundException, LabelException;

    /**
     * Returns the WSDL archive info of a given API UUID and gateway label name
     *
     * @param apiId API Id
     * @param labelName gateway label name
     * @return WSDL archive information {@link WSDLArchiveInfo}
     * @throws APIMgtDAOException if error occurs while accessing the WSDL from the data layer
     * @throws APIMgtWSDLException if error occurs while parsing/manipulating the WSDL
     * @throws APINotFoundException If API cannot be found
     * @throws LabelException If Label related error occurs
     */
    WSDLArchiveInfo getAPIWSDLArchive(String apiId, String labelName)
            throws APIMgtDAOException, APIMgtWSDLException, APINotFoundException, LabelException;

    /**
     * Store user self signup
     *
     * @param user User information object
     * @throws APIManagementException if error occurred while registering the new user
     */
    void selfSignUp(User user) throws APIManagementException;
}
