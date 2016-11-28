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
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the APISubscriptionDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class APISubscriptionDAOImpl implements APISubscriptionDAO {

    /**
     * Retrieve a given instance of an API Subscription
     *
     * @param subscriptionId The UUID that uniquely identifies a Subscription
     * @return valid {@link Subscription} object or null
     * @throws APIMgtDAOException
     */
    @Override
    public Subscription getAPISubscription(String subscriptionId) throws APIMgtDAOException {
        final String getSubscriptionCountOfApiSql = "SELECT TIER_ID, API_ID, APPLICATION_ID, SUB_STATUS, " +
                "SUB_TYPE, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME FROM AM_SUBSCRIPTION WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionCountOfApiSql)) {
            ps.setString(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an API
     *
     * @param apiId The UUID of API
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    @Override
    public Subscription getAPISubscriptionsByAPI(String apiId) throws APIMgtDAOException {
        final String getSubscriptionCountOfApiSql = "SELECT TIER_ID, API_ID, APPLICATION_ID, SUB_STATUS, " +
                "SUB_TYPE, CREATED_BY, CREATED_TIME, UPDATED_BY, UPDATED_TIME FROM AM_SUBSCRIPTION WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionCountOfApiSql)) {
            ps.setString(1, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                createSubscriptionsWithApiInformation(rs);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
        return null;
    }

    /**
     * Retrieve the list of subscriptions of an Application
     *
     * @param applicationId The UUID of Application
     * @return List of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    @Override
    public Subscription getAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException {
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
     * @param uuid   UUID of new subscription
     * @param apiId  API ID
     * @param appId  Application ID
     * @param tier   Subscription tier
     * @param status {@link APIConstants.SubscriptionStatus} Subscription state
     * @throws APIMgtDAOException
     */
    @Override
    public void addAPISubscription(String uuid, String apiId, String appId, String tier, String status)
            throws APIMgtDAOException {
        //check for existing subscriptions
        final String checkExistingSubscriptionSql = " SELECT UUID FROM AM_SUBSCRIPTION WHERE API_ID = ? " +
                "AND APPLICATION_ID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkExistingSubscriptionSql)) {
            ps.setString(1, apiId);
            ps.setString(2, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new APIMgtDAOException("Subscription already exists for API " +
                            DAOFactory.getApiDAO().getAPI(apiId).getName() + " in Application " +
                            DAOFactory.getApplicationDAO().getApplication(appId).getName());
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        //add new subscription
        final String addSubscriptionSql = "INSERT INTO AM_SUBSCRIPTION (UUID, TIER_ID, API_ID, APPLICATION_ID," +
                "SUB_STATUS, SUBS_TYPE, CREATED_BY, CREATED_TIME) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = DAOUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(addSubscriptionSql)) {
                conn.setAutoCommit(false);
                ps.setString(1, uuid);
                ps.setString(2, tier);
                ps.setString(3, apiId);
                ps.setString(4, appId);
                ps.setString(5, status != null ? status : APIMgtConstants.SubscriptionStatus.ACTIVE);
                ps.setString(6, APIMgtConstants.SubscriptionType.SUBSCRIBE);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionId The UUID of the API Subscription that needs to be deleted
     * @throws APIMgtDAOException
     */
    @Override
    public void deleteAPISubscription(String subscriptionId) throws APIMgtDAOException {
        final String deleteSubscriptionSql = "DELETE FROM AM_SUBSCRIPTION WHERE UUID = ? ";
        try (Connection conn = DAOUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteSubscriptionSql)) {
                conn.setAutoCommit(false);
                ps.setString(1, subscriptionId);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the number of subscriptions if a given API
     *
     * @param apiId UUID of the API
     * @return Subscription Count
     * @throws APIMgtDAOException
     */
    @Override
    public long getSubscriptionCountByAPI(String apiId) throws APIMgtDAOException {
        final String getSubscriptionCountOfApiSql = "SELECT COUNT(*) AS API_COUNT FROM AM_SUBSCRIPTION WHERE API_ID=?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionCountOfApiSql)) {
            ps.setString(1, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("API_COUNT");
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
        return 0L;
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

    private Subscription[] createSubscriptionsWithApiInformation(ResultSet rs) throws SQLException, APIMgtDAOException {
        List<Subscription> subscriptionList = new ArrayList<>();
        Subscription subscription;
        while ((subscription = createSubscriptionFromResultSet(rs)) != null) {
            subscriptionList.add(subscription);
        }
        Subscription[] subscriptions = new Subscription[subscriptionList.size()];
        return subscriptionList.toArray(subscriptions);
    }

    private Subscription createSubscriptionFromResultSet(ResultSet rs) throws APIMgtDAOException, SQLException {
        Subscription subscription = null;
        if (rs.next()) {
            String subscriptionId = rs.getString("UUID");
            String apiId = rs.getString("API_ID");
            String appId = rs.getString("APPLICATION_ID");
            String subscriptionTier = rs.getString("TIER");
            subscription = new Subscription(subscriptionId, DAOFactory.getApplicationDAO().getApplication(appId),
                    DAOFactory.getApiDAO().getAPI(apiId), subscriptionTier);
        }
        return subscription;
    }
}
