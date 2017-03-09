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
     * @throws APIMgtDAOException
     */
    @CheckForNull
    Subscription getAPISubscription(String subscriptionId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an API
     *
     * @param apiId The UUID of API
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    public List<Subscription> getAPISubscriptionsByAPI(String apiId) throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an API for validation
     *
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException
     */
    @CheckForNull
    List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(String apiContext, String apiVersion)
            throws APIMgtDAOException;

    /**
     * Retrieve the list of subscriptions of an Application
     *
     * @param applicationId The UUID of Application
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    public List<Subscription> getAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException;

    /**
     * Retrieve all API Subscriptions for validation
     *
     * @param limit Subscription Limit
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException
     */
    @CheckForNull
    List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(int limit) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and ensuring results
     * returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException
     */
    APISubscriptionResults getAPISubscriptionsForUser(int offset, int limit, String userName) throws APIMgtDAOException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException
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
     * @param userName        The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException
     */
    APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                     int limit, String userName) throws APIMgtDAOException;

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
     * @throws APIMgtDAOException
     */
    APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                      int limit, String groupID) throws APIMgtDAOException;

    /**
     * Create a new Subscription
     *
     * @param uuid   UUID of new subscription
     * @param apiId  API ID
     * @param appId  Application ID
     * @param tier   subscription tier
     * @param status {@link APIConstants.SubscriptionStatus} Subscription state
     * @throws APIMgtDAOException
     */
    void addAPISubscription(String uuid, String apiId, String appId, String tier, APIMgtConstants.SubscriptionStatus
            status) throws APIMgtDAOException;

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionId The UUID of the API Subscription that needs to be deleted
     * @throws APIMgtDAOException
     */
    void deleteAPISubscription(String subscriptionId) throws APIMgtDAOException;


    /**
     * Retrieve the number of subscriptions if a given API
     *
     * @param apiId UUID of the API
     * @return  Subscription Count
     * @throws APIMgtDAOException
     */
    long getSubscriptionCountByAPI(String apiId) throws APIMgtDAOException;

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     * @param subscriptionList {@link List<Subscription>}
     * @throws APIMgtDAOException
     */
    void copySubscriptions(List<Subscription> subscriptionList) throws APIMgtDAOException;

    /**
     * Update Subscription Status
     *
     * @param subId     ID of the Subscription
     * @param subStatus New Subscription Status
     * @throws APIMgtDAOException
     */
    void updateSubscriptionStatus(String subId, APIMgtConstants.SubscriptionStatus subStatus) throws
            APIMgtDAOException;

    /**
     * Update Subscription Policy
     *
     * @param subId  ID of the Subscription
     * @param policy New Subscription Policy
     * @throws APIMgtDAOException
     */
    void updateSubscriptionPolicy(String subId, String policy) throws APIMgtDAOException;

    /**
     * Validates a subscription
     *
     * @param apiContext Context of the API
     * @param apiVersion Version of the API
     * @param consumerKey Consumer key of the application
     * @return Subscription Validation Information
     * @throws APIManagementException
     */
    SubscriptionValidationResult validateSubscription(String apiContext, String apiVersion, String consumerKey)
            throws APIMgtDAOException;
}
