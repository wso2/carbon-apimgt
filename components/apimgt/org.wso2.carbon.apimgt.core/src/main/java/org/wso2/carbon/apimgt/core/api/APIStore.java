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

import org.wso2.carbon.apimgt.core.exception.APICommentException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIRatingException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This interface used to write Store specific methods.
 *
 */
public interface APIStore extends APIManager {

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
     * @param groupId         Group id.
     * @return it will return Application.
     * @throws APIManagementException Failed to get application by name.
     */
    Application getApplicationByName(String applicationName, String ownerId, String groupId)
            throws APIManagementException;

    /**
     * Returns a list of applications for a given subscriber
     *
     * @param subscriber Subscriber
     * @param groupId    the groupId to which the applications must belong.
     * @return Applications
     * @throws APIManagementException if failed to applications for given subscriber
     */

    List<Application> getApplications(String subscriber, String groupId) throws APIManagementException;

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
     * @param userId          Subsriber name.
     * @param applicationName name of the Application.
     * @param applicationId   id of the Application.
     * @param tokenType       Token type (PRODUCTION | SANDBOX)
     * @param callbackUrl     callback URL
     * @param allowedDomains  allowedDomains for token.
     * @param validityTime    validity time period.
     * @param groupingId      APIM application id.
     * @param tokenScope      Scopes for the requested tokens.
     * @return                {@code Map<String, Object>}  Map of generated keys.
     * @throws APIManagementException if failed to applications for given subscriber
     */
    Map<String, Object> generateApplicationKeys(String userId, String applicationName, String applicationId,
            String tokenType, String callbackUrl, String[] allowedDomains, String validityTime,
            String tokenScope, String groupingId) throws APIManagementException;

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
    List<Policy> getPolicies(String tierLevel) throws APIManagementException;

    /**
     * Retrieve all policies of given tier level.
     * @param tierLevel Level of the tier.
     * @param tierName  Name of the tier.
     * @return  Policy object.
     * @throws APIManagementException   If failed to get the policy.
     */
    Policy getPolicy(String tierLevel, String tierName) throws APIManagementException;

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
     * Retrieve Average Rating based on the API ID
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
    String addCompositeApi(API.APIBuilder apiBuilder) throws APIManagementException;

    /**
     * Updates design and implementation of an existing Composite API.
     *
     * @param apiBuilder {@code org.wso2.carbon.apimgt.core.models.API.APIBuilder}
     * @throws APIManagementException if failed to update Composite API
     */
    void updateCompositeApi(API.APIBuilder apiBuilder) throws APIManagementException;

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
     * Store user self signup
     *
     * @param user User information object
     * @throws APIManagementException if error occurred while registering the new user
     */
    void selfSignUp(User user) throws APIManagementException;
}
