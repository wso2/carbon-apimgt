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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.models.AvgRating;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

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
     * @throws APIManagementException   If failed to delete application.
     */
    void deleteApplication(String appId) throws APIManagementException;

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
     * @throws APIManagementException If an error occurs while updating the application
     */
    void updateApplication(String uuid, Application application) throws APIManagementException;

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
     * @throws APIManagementException   If failed to delete the subscription.
     */
    void deleteAPISubscription(String subscriptionId) throws APIManagementException;

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
     * @param labels List of label names
     * @return {@code List<Label>} List of Labels
     * @throws APIManagementException if failed to get labels
     */
    List<Label> getLabelInfo(List<String> labels) throws APIManagementException;

    /**
     * Retrieve Individual Comment based on Comment ID
     *
     * @param commentId UUID od the comment
     * @param apiId UUID of the API
     * @return Comment Object.
     * @throws APIManagementException if failed to get labels
     */
    Comment getCommentByUUID(String commentId, String apiId) throws APIManagementException;

    /**
     * Retrieve Average Rating based on the API ID
     *
     * @param apiId UUID of the API
     * @return AvgRating Object.
     * @throws APIManagementException if failed to get labels
     */
    AvgRating getRatingByApiId(String apiId) throws APIManagementException;

    /**
     * Retrieve Individual Rating
     *
     * @param apiId UUID of the API
     * @param subscriberName Name of the subscriber who has given the rating
     * @return Comment Object.
     * @throws APIManagementException if failed to get labels
     */
    Rating getAPIRatingBySubscriber(String apiId, String subscriberName) throws APIManagementException;

}
