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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.models.APISubscription;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;

import java.sql.SQLException;

/**
 * Default implementation of the APISubscriptionDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class APISubscriptionDAOImpl implements APISubscriptionDAO {
    /**
     * Retrieve a given instance of an API Subscription
     *
     * @param subscriptionID The UUID that uniquely identifies a Subscription
     * @return valid {@link APISubscription} object or null
     * @throws SQLException
     */
    @Override
    public APISubscription getAPISubscription(String subscriptionID) throws SQLException {
        return null;
    }

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and ensuring results
     * returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     */
    @Override
    public APISubscriptionResults getAPISubscriptionsForUser(int offset, int limit, String userName)
                                                                            throws SQLException {
        return null;
    }

    /**
     * Retrieves all available API Subscriptions. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws SQLException
     */
    @Override
    public APISubscriptionResults getAPISubscriptionsForGroup(int offset, int limit, String groupID)
                                                                            throws SQLException {
        return null;
    }

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
     * @throws SQLException
     */
    @Override
    public APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                        int limit, String userName) throws SQLException {
        return null;
    }

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
     * @throws SQLException
     */
    @Override
    public APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                          int limit, String groupID) throws SQLException {
        return null;
    }

    /**
     * Create a new Subscription
     *
     * @param subscription The {@link APISubscription} object to be added
     * @return The newly added {@link APISubscription} object
     * @throws SQLException
     */
    @Override
    public APISubscription addAPISubscription(APISubscription subscription) throws SQLException {
        return null;
    }

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionID The UUID of the API Subscription that needs to be deleted
     * @throws SQLException
     */
    @Override
    public void deleteAPISubscription(String subscriptionID) throws SQLException {

    }

    @Override
    public long getAPISubscriptionCountByAPI(String apiId) throws SQLException {
        return 0;
    }

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     *
     * @param identifier uuid of newly created version
     * @throws SQLException
     */
    @Override
    public void copySubscriptions(String identifier) throws SQLException {
        /* TODO: 11/12/16 Get identifiers of existing versions
        Get subscriptions of above apiid
        insert new entries with above given identifier
         */
    }
}
