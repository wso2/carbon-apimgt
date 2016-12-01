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
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        final String getSubscriptionCountOfApiSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.API_ID AS API_ID, " +
                "SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, " +
                "API.NAME AS API_NAME, API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERSION, APP.NAME AS APP_NAME, " +
                "APP.CALLBACK_URL AS APP_CALLBACK_URL, APP.APPLICATION_STATUS AS APP_STATUS, " +
                "APP.CREATED_BY AS APP_OWNER, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_APPLICATION APP, AM_SUBSCRIPTION_POLICY POLICY " +
                "WHERE SUBS.UUID = ? AND SUBS.API_ID = API.UUID AND SUBS.APPLICATION_ID = APP.UUID AND " +
                "SUBS.TIER_ID = POLICY.UUID";
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
     * @return A list of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    @Override
    public List<Subscription> getAPISubscriptionsByAPI(String apiId) throws APIMgtDAOException {
        final String getSubscriptionCountOfApiSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, APP.NAME AS APP_NAME, APP.APPLICATION_POLICY_ID AS APP_POLICY_ID, " +
                "APP.CALLBACK_URL AS APP_CALLBACK_URL, APP.APPLICATION_STATUS AS APP_STATUS, " +
                "APP.CREATED_BY AS APP_OWNER " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_APPLICATION APP " +
                "WHERE SUBS.API_ID = ? AND SUBS.APPLICATION_ID = APP.UUID";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionCountOfApiSql)) {
            ps.setString(1, apiId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an Application
     *
     * @param applicationId The UUID of Application
     * @return A list of {@link Subscription} objects
     * @throws APIMgtDAOException
     */
    @Override
    public List<Subscription> getAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException {
        final String getSubscriptionCountOfApiSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, " +
                "API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERTION, APP.NAME AS APP_NAME, " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_APPLICATION APP " +
                "WHERE SUBS.APPLICATION_ID = ? AND SUBS.APPLICATION_ID = APP.UUID";
        ;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionCountOfApiSql)) {
            ps.setString(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
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
     * @param status {@link  org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus} Subscription state
     * @throws APIMgtDAOException
     */
    @Override
    public void addAPISubscription(String uuid, String apiId, String appId, String tier, APIMgtConstants
            .SubscriptionStatus status) throws APIMgtDAOException {
        try (Connection conn = DAOUtil.getConnection()) {
            try {
                createSubscription(apiId, appId, uuid, tier, status, conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new APIMgtDAOException(e);
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
     * @param subscriptionList uuid of newly created version
     * @throws APIMgtDAOException
     */
    @Override
    public void copySubscriptions(List<Subscription> subscriptionList) throws APIMgtDAOException {
        for (Subscription subscription : subscriptionList) {
            try (Connection conn = DAOUtil.getConnection()) {
                try {
                    createSubscription(subscription.getApi().getId(), subscription.getApplication().getId(),
                            subscription.getId(), subscription.getSubscriptionTier(), subscription.getStatus(), conn);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new APIMgtDAOException(e);
                }
            } catch (SQLException e) {
                throw new APIMgtDAOException(e);
            }
        }
    }

    private List<Subscription> createSubscriptionsFromResultSet(ResultSet rs) throws APIMgtDAOException {
        List<Subscription> subscriptionList = new ArrayList<>();
        Subscription subscription;
        while ((subscription = createSubscriptionFromResultSet(rs)) != null) {
            subscriptionList.add(subscription);
        }
        return subscriptionList;
    }

    private Subscription createSubscriptionFromResultSet(ResultSet rs) throws APIMgtDAOException {
        Subscription subscription = null;
        try {
            if (rs.next()) {
                String subscriptionId = rs.getString("SUBS_UUID");
                String subscriptionTier = rs.getString("SUBS_POLICY");

                API api = null;
                //if API information is available
                if (rs.getString("API_NAME") != null) {
                    API.APIBuilder apiBuilder = new API.APIBuilder(rs.getString("API_PROVIDER"),
                            rs.getString("API_NAME"), rs.getString("API_VERSION"));
                    apiBuilder.id(rs.getString("API_ID"));
                    apiBuilder.context(rs.getString("API_CONTEXT"));
                    api = apiBuilder.buildApi();
                }

                Application app = null;
                //if Application information is available
                if (rs.getString("APP_NAME") != null) {
                    app = new Application(rs.getString("APP_NAME"), rs.getString("APP_OWNER"));
                    app.setUuid(rs.getString("APPLICATION_ID"));
                    app.setCallbackUrl(rs.getString("APP_CALLBACK_URL"));
                    app.setStatus(rs.getString("APP_STATUS"));
                }

                subscription = new Subscription(subscriptionId, app, api, subscriptionTier);
                subscription.setStatus(APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUB_STATUS")));
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
        return subscription;
    }

    private void createSubscription(String apiId, String appId, String uuid, String tier, APIMgtConstants
            .SubscriptionStatus status, Connection conn) throws APIMgtDAOException, SQLException {
        //check for existing subscriptions
        final String checkExistingSubscriptionSql = " SELECT UUID FROM AM_SUBSCRIPTION WHERE API_ID = ? " +
                "AND APPLICATION_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkExistingSubscriptionSql)) {
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
                "SUB_STATUS, CREATED_TIME) VALUES (?,(SELECT UUID FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?),?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(addSubscriptionSql)) {
            conn.setAutoCommit(false);
            ps.setString(1, uuid);
            ps.setString(2, tier);
            ps.setString(3, apiId);
            ps.setString(4, appId);
            ps.setString(5, status != null ? status.getStatus() : APIMgtConstants.SubscriptionStatus
                    .ACTIVE.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.execute();
        }
    }

    /**
     * Update Subscription Status
     *
     * @param subId
     * @param subStatus
     * @param policy
     * @throws APIMgtDAOException
     */
    @Override
    public void updateSubscription(String subId, APIMgtConstants.SubscriptionStatus subStatus, String policy)
            throws APIMgtDAOException {
        final String updateSubscriptionSql = "UPDATE AM_SUBSCRIPTION SET TIER_ID = ?,SUB_STATUS = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSubscriptionSql)) {
                preparedStatement.setString(1, policy);
                preparedStatement.setString(2, subStatus.getStatus());
                preparedStatement.setString(3, subId);
                preparedStatement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new APIMgtDAOException(e);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }
}
