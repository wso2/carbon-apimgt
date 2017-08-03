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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISubscriptionResults;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationResult;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus;

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

    private static final String AM_SUBSCRIPTION_TABLE_NAME = "AM_SUBSCRIPTION";
    private static final Logger log = LoggerFactory.getLogger(APISubscriptionDAOImpl.class);

    /**
     * Retrieve a given instance of an API Subscription
     *
     * @param subscriptionId The UUID that uniquely identifies a Subscription
     * @return valid {@link Subscription} object or null
     * @throws APIMgtDAOException If failed to get subscription.
     */
    @Override
    public Subscription getAPISubscription(String subscriptionId) throws APIMgtDAOException {
        final String getSubscriptionSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.API_ID AS API_ID, " +
                "SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, " +
                "API.NAME AS API_NAME, API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERSION, APP.NAME AS APP_NAME, " +
                "APP.APPLICATION_STATUS AS APP_STATUS, " +
                "APP.CREATED_BY AS APP_OWNER, POLICY.NAME AS SUBS_POLICY, POLICY.UUID AS SUBS_POLICY_ID " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_APPLICATION APP, AM_SUBSCRIPTION_POLICY POLICY " +
                "WHERE SUBS.UUID = ? AND SUBS.API_ID = API.UUID AND SUBS.APPLICATION_ID = APP.UUID AND " +
                "SUBS.TIER_ID = POLICY.UUID";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionSql)) {
            ps.setString(1, subscriptionId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionWithApiAndAppInformation(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an API
     *
     * @param apiId The UUID of API
     * @return A list of {@link Subscription} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<Subscription> getAPISubscriptionsByAPI(String apiId) throws APIMgtDAOException {
        final String getSubscriptionsByAPISql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, APP.NAME AS APP_NAME, APP.APPLICATION_POLICY_ID AS APP_POLICY_ID, " +
                "APP.APPLICATION_STATUS AS APP_STATUS, " +
                "APP.CREATED_BY AS APP_OWNER, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_APPLICATION APP, AM_SUBSCRIPTION_POLICY POLICY " +
                "WHERE SUBS.API_ID = ? AND SUBS.APPLICATION_ID = APP.UUID AND SUBS.TIER_ID = POLICY.UUID " +
                "AND SUBS.SUB_STATUS NOT IN (?,?)";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAPISql)) {
            ps.setString(1, apiId);
            ps.setString(2, SubscriptionStatus.ON_HOLD.name());
            ps.setString(3, SubscriptionStatus.REJECTED.name());
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsWithAppInformationOnly(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an Application
     *
     * @param applicationId The UUID of Application
     * @return A list of {@link Subscription} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<Subscription> getAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException {
        final String getSubscriptionsByAppSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, " +
                "API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERSION, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_SUBSCRIPTION_POLICY POLICY  " +
                "WHERE SUBS.APPLICATION_ID = ? AND SUBS.API_ID = API.UUID AND SUBS.TIER_ID = POLICY.UUID";

        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAppSql)) {
            ps.setString(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsWithApiInformationOnly(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    @Override
    public List<Subscription> getAPISubscriptionsByApplication(String applicationId, ApiType apiType)
            throws APIMgtDAOException {
        final String getSubscriptionsByAppSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, " +
                "API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERSION, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_SUBSCRIPTION_POLICY POLICY  " +
                "WHERE SUBS.APPLICATION_ID = ? AND SUBS.API_ID = API.UUID AND SUBS.TIER_ID = POLICY.UUID " +
                "AND API.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAppSql)) {
            ps.setString(1, applicationId);
            ps.setString(2, apiType.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsWithApiInformationOnly(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an Application which are in pending state
     *
     * @param applicationId The UUID of Application
     * @return A list of {@link Subscription} objects which has pendig status
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<Subscription> getPendingAPISubscriptionsByApplication(String applicationId) throws APIMgtDAOException {
        final String getSubscriptionsByAppSql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, " +
                "API.CONTEXT AS API_CONTEXT, API.VERSION AS API_VERSION, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_SUBSCRIPTION_POLICY POLICY  " +
                "WHERE SUBS.APPLICATION_ID = ? AND SUBS.API_ID = API.UUID AND SUBS.TIER_ID = POLICY.UUID " +
                "AND SUBS.SUB_STATUS=?";

        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAppSql)) {
            ps.setString(1, applicationId);
            ps.setString(2, SubscriptionStatus.ON_HOLD.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsWithApiInformationOnly(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve all API Subscriptions for validation
     *
     * @param limit Subscription Limit
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(int limit) throws APIMgtDAOException {
        if (limit == 0) {
            return new ArrayList<>();
        }
        final String getSubscriptionsSql = "SELECT SUBS.API_ID AS API_ID,SUBS.APPLICATION_ID AS APP_ID,SUBS" +
                ".SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME,API.CONTEXT AS " +
                "API_CONTEXT, API.VERSION AS API_VERSION,SUBS.TIER_ID AS SUBS_POLICY , KEY_MAP.CLIENT_ID AS " +
                "CLIENT_ID,KEY_MAP.KEY_TYPE AS KEY_ENV_TYPE FROM AM_SUBSCRIPTION SUBS, AM_API API,AM_APP_KEY_MAPPING " +
                "KEY_MAP WHERE SUBS.API_ID = API.UUID AND KEY_MAP.APPLICATION_ID = SUBS.APPLICATION_ID";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsSql)) {
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionValidationDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an API for validation
     *
     * @param apiContext Context of the API
     * @param apiVersion Version of the API.
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(String apiContext, String apiVersion)
            throws APIMgtDAOException {
        final String getSubscriptionsByAPISql = "SELECT SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID,SUBS" +
                ".SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, API.CONTEXT AS " +
                "API_CONTEXT, API.VERSION AS API_VERSION, SUBS.TIER_ID AS SUBS_POLICY , KEY_MAP.CLIENT_ID AS " +
                "CLIENT_ID,KEY_MAP.KEY_TYPE AS KEY_ENV_TYPE FROM AM_SUBSCRIPTION SUBS, AM_API API,AM_APP_KEY_MAPPING " +
                "KEY_MAP WHERE SUBS.API_ID = API.UUID AND KEY_MAP.APPLICATION_ID = SUBS.APPLICATION_ID AND API" +
                ".CONTEXT = ? AND API.VERSION = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAPISql)) {
            ps.setString(1, apiContext);
            ps.setString(2, apiVersion);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionValidationDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions of an API for validation
     *
     * @param apiContext    Context of the API
     * @param apiVersion    Version of the API.
     * @param applicationId UUID of the application
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public List<SubscriptionValidationData> getAPISubscriptionsOfAPIForValidation(String apiContext, String
            apiVersion, String applicationId) throws APIMgtDAOException {
        final String getSubscriptionsByAPISql = "SELECT SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID,SUBS" +
                ".SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, API.CONTEXT AS " +
                "API_CONTEXT, API.VERSION AS API_VERSION, SUBS.TIER_ID AS SUBS_POLICY , KEY_MAP.CLIENT_ID AS " +
                "CLIENT_ID,KEY_MAP.KEY_TYPE AS KEY_ENV_TYPE FROM AM_SUBSCRIPTION SUBS, AM_API API,AM_APP_KEY_MAPPING " +
                "KEY_MAP WHERE SUBS.API_ID = API.UUID AND KEY_MAP.APPLICATION_ID = SUBS.APPLICATION_ID AND API" +
                ".CONTEXT = ? AND API.VERSION = ? AND SUBS.APPLICATION_ID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAPISql)) {
            ps.setString(1, apiContext);
            ps.setString(2, apiVersion);
            ps.setString(3, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionValidationDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the list of subscriptions with a given application and key type for validation
     *
     * @param applicationId UUID of the application
     * @param keyType       Application key type
     * @return A list of {@link SubscriptionValidationData} objects
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    public List<SubscriptionValidationData> getAPISubscriptionsOfAppForValidation(String applicationId, String keyType)
            throws APIMgtDAOException {
        final String getSubscriptionsByAPISql = "SELECT SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID,SUBS" +
                ".SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, API.CONTEXT AS " +
                "API_CONTEXT, API.VERSION AS API_VERSION, SUBS.TIER_ID AS SUBS_POLICY , KEY_MAP.CLIENT_ID AS " +
                "CLIENT_ID,KEY_MAP.KEY_TYPE AS KEY_ENV_TYPE FROM AM_SUBSCRIPTION SUBS, AM_API API,AM_APP_KEY_MAPPING " +
                "KEY_MAP WHERE SUBS.API_ID = API.UUID AND KEY_MAP.APPLICATION_ID = SUBS.APPLICATION_ID AND SUBS" +
                ".APPLICATION_ID = ? AND KEY_MAP.KEY_TYPE = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAPISql)) {
            ps.setString(1, applicationId);
            ps.setString(2, keyType);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionValidationDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

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
    @Override
    public List<Subscription> getAPISubscriptionsForUser(int offset, int limit, String username)
            throws APIMgtDAOException {
        final String getSubscriptionsByAPISql = "SELECT SUBS.UUID AS SUBS_UUID, SUBS.TIER_ID AS SUBS_TIER, " +
                "SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, SUBS.SUB_STATUS AS SUB_STATUS, " +
                "SUBS.SUB_TYPE AS SUB_TYPE, APP.NAME AS APP_NAME, APP.APPLICATION_POLICY_ID AS APP_POLICY_ID, " +
                "APP.APPLICATION_STATUS AS APP_STATUS, " +
                "APP.CREATED_BY AS APP_OWNER, POLICY.NAME AS SUBS_POLICY, POLICY.UUID AS SUBS_POLICY_ID, " +
                "API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, API.CONTEXT AS API_CONTEXT, " +
                "API.VERSION AS API_VERSION " +
                "FROM AM_SUBSCRIPTION SUBS, AM_APPLICATION APP, AM_SUBSCRIPTION_POLICY POLICY, AM_API API " +
                "WHERE  SUBS.APPLICATION_ID = APP.UUID AND SUBS.TIER_ID = POLICY.UUID " +
                "AND API.UUID = SUBS.API_ID AND API.PROVIDER = ? ";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSubscriptionsByAPISql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return createSubscriptionsFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

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
     * @param username        The username to filter results by
     * @return {@link APISubscriptionResults} matching results
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public APISubscriptionResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                            int limit, String username) throws APIMgtDAOException {
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
     * @throws APIMgtDAOException If failed to get subscriptions.
     */
    @Override
    public APISubscriptionResults searchApplicationsForGroup(String searchAttribute, String searchString, int offset,
                                                             int limit, String groupID) throws APIMgtDAOException {
        return null;
    }

    /**
     * Create a new Subscription
     *
     * @param uuid     UUID of new subscription
     * @param apiId    API ID
     * @param appId    Application ID
     * @param policyId Subscription tier's policy id
     * @param status   {@link  org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus} Subscription state
     * @throws APIMgtDAOException If failed to add subscription.
     */
    @Override
    public void addAPISubscription(String uuid, String apiId, String appId, String policyId, APIMgtConstants
            .SubscriptionStatus status) throws APIMgtDAOException {
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                createSubscription(apiId, appId, uuid, policyId, status, conn);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Remove an existing API Subscription
     *
     * @param subscriptionId The UUID of the API Subscription that needs to be deleted
     * @throws APIMgtDAOException If failed to delete subscription.
     */
    @Override
    public void deleteAPISubscription(String subscriptionId) throws APIMgtDAOException {
        final String deleteSubscriptionSql = "DELETE FROM AM_SUBSCRIPTION WHERE UUID = ? ";
        try (Connection conn = DAOUtil.getConnection()) {
            boolean originalAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(deleteSubscriptionSql)) {
                ps.setString(1, subscriptionId);
                ps.execute();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(originalAutoCommitState);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Retrieve the number of subscriptions if a given API
     *
     * @param apiId UUID of the API
     * @return Subscription Count
     * @throws APIMgtDAOException If failed to get subscription count.
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
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return 0L;
    }

    /**
     * Copy existing subscriptions on one of the API versions into latest version
     *
     * @param subscriptionList uuid of newly created version
     * @throws APIMgtDAOException If failed to get copy subscriptions.
     */
    @Override
    public void copySubscriptions(List<Subscription> subscriptionList) throws APIMgtDAOException {
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Subscription subscription : subscriptionList) {
                    createSubscription(subscription.getApi().getId(), subscription.getApplication().getId(),
                            subscription.getId(), subscription.getPolicy().getUuid(), subscription.getStatus(),
                            conn);
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Update Subscription Status
     *
     * @param subId     ID of the Subscription
     * @param subStatus New Subscription Status
     * @throws APIMgtDAOException If failed to update subscriptions.
     */
    @Override
    public void updateSubscriptionStatus(String subId, APIMgtConstants.SubscriptionStatus subStatus)
            throws APIMgtDAOException {
        final String updateSubscriptionSql = "UPDATE AM_SUBSCRIPTION SET SUB_STATUS = ?, LAST_UPDATED_TIME = ? "
                + "WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn.prepareStatement(updateSubscriptionSql)) {
                preparedStatement.setString(1, subStatus.toString());
                preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setString(3, subId);
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * @see APISubscriptionDAO#getLastUpdatedTimeOfSubscription(String)
     */
    @Override
    public String getLastUpdatedTimeOfSubscription(String subscriptionId) throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_SUBSCRIPTION_TABLE_NAME, subscriptionId);
    }

    /**
     * Update Subscription Policy
     *
     * @param subId  ID of the Subscription
     * @param policy New Subscription Policy
     * @throws APIMgtDAOException If failed to update subscriptions.
     */
    @Override
    public void updateSubscriptionPolicy(String subId, String policy) throws APIMgtDAOException {
        final String updateSubscriptionSql = "UPDATE AM_SUBSCRIPTION SET TIER_ID = " +
                "(SELECT UUID FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?) WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn.prepareStatement(updateSubscriptionSql)) {
                preparedStatement.setString(1, policy);
                preparedStatement.setString(2, subId);
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    /**
     * Validates a subscription
     *
     * @param apiContext  Context of the API
     * @param apiVersion  Version of the API
     * @param clientId    Client id of the application
     * @return Subscription Validation Information
     * @throws APIMgtDAOException   If failed to get subscription validation results.
     */
    public SubscriptionValidationResult validateSubscription(String apiContext, String apiVersion, String clientId)
            throws APIMgtDAOException {
        final String validateSubscriptionSql = "SELECT SUBS.API_ID AS API_ID, SUBS.APPLICATION_ID AS APP_ID, " +
                "SUBS.SUB_STATUS AS SUB_STATUS, API.PROVIDER AS API_PROVIDER, API.NAME AS API_NAME, " +
                "APP.NAME AS APP_NAME, APP.CREATED_BY AS APP_OWNER, POLICY.NAME AS SUBS_POLICY " +
                "FROM AM_SUBSCRIPTION SUBS, AM_API API, AM_APPLICATION APP, AM_SUBSCRIPTION_POLICY POLICY, " +
                "AM_APP_KEY_MAPPING KEYS " +
                "WHERE API.CONTEXT = ? AND API.VERSION = ? AND KEYS.CLIENT_ID= ? " +
                "AND APP.AND SUBS.API_ID = API.UUID AND SUBS.APPLICATION_ID = APP.UUID " +
                "AND SUBS.TIER_ID = POLICY.UUID AND KEYS.APPLICATION_ID = APP.UUID";
        SubscriptionValidationResult validationInfo = new SubscriptionValidationResult(false);
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(validateSubscriptionSql)) {
            ps.setString(1, apiContext);
            ps.setString(2, apiVersion);
            ps.setString(3, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    validationInfo.setValid(true);
                    validationInfo.setApiId(rs.getString("API_ID"));
                    validationInfo.setApplicationId(rs.getString("APP_ID"));
                    validationInfo.setSubscriptionStatus(
                            APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUB_STATUS")));
                    validationInfo.setApiProvider(rs.getString("API_PROVIDER"));
                    validationInfo.setApiName(rs.getString("API_NAME"));
                    validationInfo.setApplicationName(rs.getString("APP_NAME"));
                    validationInfo.setApplicationOwner(rs.getString("APP_OWNER"));
                    validationInfo.setSubscriptionStatus(
                            APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUBS_POLICY")));
                }
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return validationInfo;
    }

    private List<SubscriptionValidationData> createSubscriptionValidationDataFromResultSet(ResultSet rs)
            throws APIMgtDAOException {
        List<SubscriptionValidationData> subscriptionList = new ArrayList<>();
        if (rs == null) {
            return new ArrayList<>();
        }
        try {
            while (rs.next()) {
                SubscriptionValidationData subValidationData = new SubscriptionValidationData(
                        rs.getString("API_CONTEXT"), rs.getString("API_VERSION"), rs.getString("CLIENT_ID"));
                subValidationData.setSubscriptionPolicy(rs.getString("SUBS_POLICY"));
                subValidationData.setApiName(rs.getString("API_NAME"));
                subValidationData.setApiProvider(rs.getString("API_PROVIDER"));
                subValidationData.setKeyEnvType(rs.getString("KEY_ENV_TYPE"));
                subValidationData.setApplicationId(rs.getString("APP_ID"));
                subValidationData.setStatus(rs.getString("SUB_STATUS"));
                subscriptionList.add(subValidationData);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return subscriptionList;
    }

    private List<Subscription> createSubscriptionsFromResultSet(ResultSet rs) throws APIMgtDAOException {
        List<Subscription> subscriptionList = new ArrayList<>();
        Subscription subscription;
        if (rs == null) {
            return new ArrayList<>();
        }
        while ((subscription = createSubscriptionWithApiAndAppInformation(rs)) != null) {
            subscriptionList.add(subscription);
        }
        return subscriptionList;
    }

    private Subscription createSubscriptionWithApiAndAppInformation(ResultSet rs) throws APIMgtDAOException {
        Subscription subscription = null;
        try {
            if (rs.next()) {
                String subscriptionId = rs.getString("SUBS_UUID");
                String subscriptionTier = rs.getString("SUBS_POLICY");
                String subscriptionPolicyId = rs.getString("SUBS_POLICY_ID");

                API.APIBuilder apiBuilder = new API.APIBuilder(rs.getString("API_PROVIDER"),
                        rs.getString("API_NAME"), rs.getString("API_VERSION"));
                apiBuilder.id(rs.getString("API_ID"));
                apiBuilder.context(rs.getString("API_CONTEXT"));
                API api = apiBuilder.build();

                Application app = new Application(rs.getString("APP_NAME"), rs.getString("APP_OWNER"));
                app.setId(rs.getString("APP_ID"));
                app.setStatus(rs.getString("APP_STATUS"));

                subscription = new Subscription(subscriptionId, app, api, new SubscriptionPolicy(subscriptionPolicyId,
                        subscriptionTier));
                subscription.setStatus(APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUB_STATUS")));
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return subscription;
    }

    private List<Subscription> createSubscriptionsWithApiInformationOnly(ResultSet rs) throws APIMgtDAOException {
        List<Subscription> subscriptionList = new ArrayList<>();
        try {
            Subscription subscription;
            while (rs.next()) {
                String subscriptionId = rs.getString("SUBS_UUID");
                String subscriptionTier = rs.getString("SUBS_POLICY");

                API.APIBuilder apiBuilder = new API.APIBuilder(rs.getString("API_PROVIDER"),
                        rs.getString("API_NAME"), rs.getString("API_VERSION"));
                apiBuilder.id(rs.getString("API_ID"));
                apiBuilder.context(rs.getString("API_CONTEXT"));
                API api = apiBuilder.build();

                subscription = new Subscription(subscriptionId, null, api, new SubscriptionPolicy(subscriptionTier));
                subscription.setStatus(APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUB_STATUS")));
                subscriptionList.add(subscription);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return subscriptionList;
    }

    private List<Subscription> createSubscriptionsWithAppInformationOnly(ResultSet rs) throws APIMgtDAOException {
        List<Subscription> subscriptionList = new ArrayList<>();
        try {
            Subscription subscription;
            while (rs.next()) {
                String subscriptionId = rs.getString("SUBS_UUID");
                String subscriptionTier = rs.getString("SUBS_POLICY");

                Application app = new Application(rs.getString("APP_NAME"), rs.getString("APP_OWNER"));
                app.setId(rs.getString("APP_ID"));
                app.setStatus(rs.getString("APP_STATUS"));

                subscription = new Subscription(subscriptionId, app, null, new SubscriptionPolicy(subscriptionTier));
                subscription.setStatus(APIMgtConstants.SubscriptionStatus.valueOf(rs.getString("SUB_STATUS")));
                subscriptionList.add(subscription);
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
        return subscriptionList;
    }

    void createSubscription(String apiId, String appId, String uuid, String policyId, APIMgtConstants
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
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }

        //add new subscription
        final String addSubscriptionSql = "INSERT INTO AM_SUBSCRIPTION (UUID, TIER_ID, API_ID, APPLICATION_ID," +
                "SUB_STATUS, CREATED_TIME) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(addSubscriptionSql)) {
            conn.setAutoCommit(false);
            ps.setString(1, uuid);
            ps.setString(2, policyId);
            ps.setString(3, apiId);
            ps.setString(4, appId);
            ps.setString(5, status != null ? status.toString() : APIMgtConstants.SubscriptionStatus.ACTIVE.toString());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.execute();
        }
    }
}
