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

import org.wso2.carbon.apimgt.core.models.APISubscription;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;

import java.sql.SQLException;

import javax.annotation.CheckForNull;


/**
 * Provides access to API Subscription data layer
 */

public interface APISubscriptionDAO {
    /**
     * Retrieve a given instance of an API Subscription
     * @param subscriptionID The UUID that uniquely identifies a Subscription
     * @return valid {@link APISubscription} object or null
     * @throws SQLException
     *
     */
    @CheckForNull
    APISubscription getAPISubscription(String subscriptionID) throws SQLException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and ensuring results
     * returned are those that belong to the specified username
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     *
     */
    APISubscriptionResults getAPISubscriptionsForUser(int offset, int limit, String userName)
                                                                            throws SQLException;

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     *
     */
    APISubscriptionResults getAPISubscriptionsForGroup(int offset, int limit, String groupID)
            throws SQLException;

    /**
     * Retrieves all available API Subscriptions that match the given search criteria. This method supports
     * result pagination and ensures results returned belong to the specified username
     * @param searchAttribute The attribute of a Subscription against which the search will be performed
     * @param searchString The search string provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     *
     */
    APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                        int limit, String userName) throws SQLException;

    /**
     * Retrieves all available API Subscriptions that match the given search criteria. This method supports
     * result pagination and ensures results returned belong to the specified Group ID
     * @param searchAttribute The attribute of an Application against which the search will be performed
     * @param searchString The search string provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     *
     */
    APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                         int limit, String groupID) throws SQLException;

    /**
     * Create a new Subscription
     * @param subscription The {@link APISubscription} object to be added
     * @return The newly added {@link APISubscription} object
     * @throws SQLException
     *
     */
    APISubscription addAPISubscription(APISubscription subscription) throws SQLException;

    /**
     * Remove an existing API Subscription
     * @param subscriptionID The UUID of the API Subscription that needs to be deleted
     * @throws SQLException
     *
     */
    void deleteAPISubscription(String subscriptionID) throws SQLException;


    long getAPISubscriptionCountByAPI(String apiId) throws SQLException;

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     * @param identifier uuid of newly created version
     * @throws SQLException
     */
    void copySubscriptions(String identifier) throws SQLException;
}
