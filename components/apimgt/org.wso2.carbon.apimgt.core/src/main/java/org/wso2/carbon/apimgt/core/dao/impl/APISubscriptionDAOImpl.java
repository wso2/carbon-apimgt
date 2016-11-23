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
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.APISubscription;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * @throws APIMgtDAOException
     */
    @Override
    public APISubscription getAPISubscription(String subscriptionID) throws APIMgtDAOException {
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
     * @throws APIMgtDAOException
     */
    @Override
    public APISubscriptionResults getAPISubscriptionsForUser(int offset, int limit, String userName)
            throws APIMgtDAOException {
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
     * @throws APIMgtDAOException
     */
    @Override
    public APISubscriptionResults getAPISubscriptionsForGroup(int offset, int limit, String groupID)
            throws APIMgtDAOException {
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
     * @throws APIMgtDAOException
     */
    @Override
    public APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                            int limit, String userName) throws APIMgtDAOException {
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
     * @throws APIMgtDAOException
     */
    @Override
    public APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                             int limit, String groupID) throws APIMgtDAOException {
        return null;
    }

    /**
     * Create a new Subscription
     *
     * @param subscription The {@link APISubscription} object to be added
     * @throws APIMgtDAOException
     */
    @Override
    public void addAPISubscription(APISubscription subscription) throws APIMgtDAOException {
        final String CHECK_EXISTING_SUBSCRIPTION_SQL = " SELECT UUID FROM AM_SUBSCRIPTION WHERE API_ID = ? " +
                "AND APPLICATION_ID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(CHECK_EXISTING_SUBSCRIPTION_SQL)) {
            ps.setString(1, subscription.getApiId());
            ps.setString(2, subscription.getApplication().getUuid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new APIMgtDAOException("Subscription already exists for API " +
                            DAOFactory.getApiDAO().getAPI(subscription.getApiId()).getName() + " in Application " +
                            subscription.getApplication().getName());
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        final String ADD_SUBSCRIPTION_SQL = "INSERT INTO AM_SUBSCRIPTION (UUID, TIER_ID, API_ID, APPLICATION_ID," +
                "SUB_STATUS, SUBS_TYPE, CREATED_BY, CREATED_TIME) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(ADD_SUBSCRIPTION_SQL)) {
            ps.setString(1, subscription.getId());
            ps.setString(2, subscription.getApplication().getTier());
            ps.setString(3, subscription.getApplication().getUuid());
            ps.setString(4, subscription.getApplication().getUuid());
            ps.setString(5, subscription.getApplication().getUuid());
            ps.setString(6, subscription.getApplication().getUuid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new APIMgtDAOException("Subscription already exists for API " +
                            DAOFactory.getApiDAO().getAPI(subscription.getApiId()).getName() + " in Application " +
                            subscription.getApplication().getName());
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionID The UUID of the API Subscription that needs to be deleted
     * @throws APIMgtDAOException
     */
    @Override
    public void deleteAPISubscription(String subscriptionID) throws APIMgtDAOException {

    }

    @Override
    public long getAPISubscriptionCountByAPI(String apiId) throws APIMgtDAOException {
        return 0;
    }

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     *
     * @param identifier uuid of newly created version
     * @throws APIMgtDAOException
     */
    @Override
    public void copySubscriptions(String identifier) throws APIMgtDAOException {
        /* TODO: 11/12/16 Get identifiers of existing versions
        Get subscriptions of above apiid
        insert new entries with above given identifier
         */
    }
}
