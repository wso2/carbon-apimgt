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

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationResult;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.util.List;
import javax.annotation.CheckForNull;


/**
 * Provides access to API Subscription data layer
 */
public interface APISubscriptionDAO {

    /**
     * Retrieve a given instance of an API Subscription
     *
     * @param subscriptionId The UUID that uniquely identifies a Subscription
     * @return valid {@link Subscription} object or null
     * @throws APIMgtDAOException If failed to get subscription.
     */
    @CheckForNull
    Subscription getAPISubscription(String subscriptionId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an API
     *
     * @param apiId The UUID of API
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    public List<Subscription> getAPISubscriptionsByAPI(String apiId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an API for validation
     *
     * @param apiContext Context of the API.
     * @param apiVersion Version of the API.
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscription validation data.
     */
    @CheckForNull
    List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(String apiContext, String apiVersion)
            throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an API for validation
     *
     * @param apiContext    Context of the API
     * @param apiVersion    Version of the API.
     * @param applicationId UUID of the application
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @CheckForNull
    List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(String apiContext, String
            apiVersion, String applicationId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions with a given application and key type for validation
     *
     * @param applicationId UUID of the application
     * @param keyType       Application key type
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    List<SubscriptionValidationData> getAPISubscriptionsOfAppForValidation(String applicationId, String keyType)
            throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an Application
     *
     * @param applicationId The UUID of Application
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException If failed to get subscription data.
     */
    List<Subscription> getAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException;

    /**
     * Retrieve list of subscriptions given the application and the API Type.
     *
     * @param applicationId The UUID of Application
     * @param apiType       API Type to filter subscriptions
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException If failed to get subscription data.
     */
    List<Subscription> getAPISubscriptionsByApplication(String applicationId, ApiType apiType)
            throws APIMgtDAOException;

    /**
     * Retrieve all API Subscriptions for validation
     *
     * @param limit Subscription Limit
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @CheckForNull
    List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(int limit) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and ensuring results
     * returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param username The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    List<Subscription> getAPISubscriptionsForUser(int offset, int limit, String username) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    APISubscriptionResults getAPISubscriptionsForGroup(int offset, int limit, String groupID) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions that match the given search criteria. This method supports
     * result pagination and ensures results returned belong to the specified username
     *
     * @param searchAttribute The attribute of a Subscription against which the search will be performed
     * @param searchString    The search string provided
     * @param offset          The number of results from the beginning that is to be ignored
     * @param limit           The maximum number of results to be returned after the offset
     * @param username        The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                     int limit, String username) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions that match the given search criteria. This method supports
     * result pagination and ensures results returned belong to the specified Group ID
     *
     * @param searchAttribute The attribute of an Application against which the search will be performed
     * @param searchString    The search string provided
     * @param offset          The number of results from the beginning that is to be ignored
     * @param limit           The maximum number of results to be returned after the offset
     * @param groupID         The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                      int limit, String groupID) throws APIMgtDAOException;

    /**
     * Create a new Subscription
     *
     * @param uuid     UUID of new subscription
     * @param apiId    API ID
     * @param appId    Application ID
     * @param policyId Subscription tier's policy id
     * @param status   {@code APIConstants.SubscriptionStatus} Subscription state
     * @throws APIMgtDAOException If failed to add subscription.
     */
    void addAPISubscription(String uuid, String apiId, String appId, String policyId, APIMgtConstants.SubscriptionStatus
            status) throws APIMgtDAOException;

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionId The UUID of the API Subscription that needs to be deleted
     * @throws APIMgtDAOException If failed to delete subscription.
     */
    void deleteAPISubscription(String subscriptionId) throws APIMgtDAOException;


    /**
     * Retrieve the number of subscriptions if a given API
     *
     * @param apiId UUID of the API
     * @return Subscription Count
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    long getSubscriptionCountByAPI(String apiId) throws APIMgtDAOException;

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     *
     * @param subscriptionList {@code List<Subscription>}
     * @throws APIMgtDAOException If filed to copy subscriptions.
     */
    void copySubscriptions(List<Subscription> subscriptionList) throws APIMgtDAOException;

    /**
     * Update Subscription Status
     *
     * @param subId     ID of the Subscription
     * @param subStatus New Subscription Status
     * @throws APIMgtDAOException If failed update subscription status.
     */
    void updateSubscriptionStatus(String subId, APIMgtConstants.SubscriptionStatus subStatus) throws
            APIMgtDAOException;

    /**
     * Update Subscription Policy
     *
     * @param subId  ID of the Subscription
     * @param policy New Subscription Policy
     * @throws APIMgtDAOException If failed to update subscription policy.
     */
    void updateSubscriptionPolicy(String subId, String policy) throws APIMgtDAOException;

    /**
     * Validates a subscription
     *
     * @param apiContext  Context of the API
     * @param apiVersion  Version of the API
     * @param consumerKey Consumer key of the application
     * @return Subscription Validation Information
     * @throws APIMgtDAOException If failed to validat subscription.
     */
    SubscriptionValidationResult validateSubscription(String apiContext, String apiVersion, String consumerKey)
            throws APIMgtDAOException;

    /**
     * Retrieves the last updated time of the subscription
     *
     * @param subscriptionId UUID of the subscription
     * @return Last updated time of the resource
     * @throws APIMgtDAOException if DB level exception occurred
     */
    String getLastUpdatedTimeOfSubscription(String subscriptionId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an Application which are in pending state
     *
     * @param applicationId The UUID of Application
     * @return A list of {@link Subscription} objects which has pending status
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    List<Subscription> getPendingAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException;

    /**
     * Delete Subscriptions in a given API.
     *
     * @param apiId UUID of the API
     * @throws APIMgtDAOException If failed to delete subscriptions
     */
    void deleteSubscriptionsByAPIId(String apiId) throws APIMgtDAOException;
}
