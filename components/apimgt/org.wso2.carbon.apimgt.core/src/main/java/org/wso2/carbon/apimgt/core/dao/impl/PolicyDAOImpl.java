/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.core.dao.impl;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.BlockConditionAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Limit;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DAO Layer implementation class for Throttling Policies
 */
public class PolicyDAOImpl implements PolicyDAO {

    private static final Logger log = LoggerFactory.getLogger(PolicyDAOImpl.class);

    public static final String FIFTY_PER_MIN_TIER = "50PerMin";
    private static final String AM_API_POLICY_TABLE_NAME = "AM_API_POLICY";
    private static final String AM_APPLICATION_POLICY_TABLE_NAME = "AM_APPLICATION_POLICY";
    private static final String AM_SUBSCRIPTION_POLICY_TABLE_NAME = "AM_SUBSCRIPTION_POLICY";
    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String GOLD_TIER = "Gold";
    public static final String SILVER_TIER = "Silver";
    public static final String BRONZE_TIER = "Bronze";
    public static final String API_TIER_LEVEL = "API";
    public static final String REQUEST_COUNT_TYPE = "requestCount";
    public static final String SECONDS_TIMUNIT = "s";
    public static final String MINUTE_TIMEUNIT = "min";
    public static final String TWENTY_PER_MIN_TIER = "20PerMin";

    @Override
    public APIPolicy getApiPolicy(String policyName) throws APIMgtDAOException {
        try {
            String sqlQuery = "SELECT UUID, NAME, DEFAULT_QUOTA_TYPE, DEFAULT_TIME_UNIT, DEFAULT_UNIT_TIME, "
                    + "DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DESCRIPTION, DISPLAY_NAME, IS_DEPLOYED, APPLICABLE_LEVEL "
                    + "from AM_API_POLICY WHERE NAME = ?";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        APIPolicy policy = new APIPolicy(policyName);
                        setCommonPolicyDetails(policy, resultSet);
                        policy.setUserLevel(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                                .COLUMN_APPLICABLE_LEVEL));
                        policy.setPipelines(getPipelines(policy.getUuid(), connection));
                        return policy;
                    } else {
                        // not found
                        String msg = "API Policy not found for name: " + policyName;
                        log.warn(msg);
                        throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving API policy with name: " + policyName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public APIPolicy getApiPolicyByUuid(String uuid) throws APIMgtDAOException {
        try {
            String sqlQuery = "SELECT UUID, NAME, DEFAULT_QUOTA_TYPE, DEFAULT_TIME_UNIT, DEFAULT_UNIT_TIME, "
                    + "DEFAULT_QUOTA, "
                    + "DEFAULT_QUOTA_UNIT, DESCRIPTION, DISPLAY_NAME, IS_DEPLOYED, APPLICABLE_LEVEL from "
                    + "AM_API_POLICY WHERE UUID = ?";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, uuid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        APIPolicy apiPolicy = new APIPolicy(resultSet
                                .getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                        setCommonPolicyDetails(apiPolicy, resultSet);
                        apiPolicy.setUserLevel(
                                resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                        apiPolicy.setPipelines(getPipelines(apiPolicy.getUuid(), connection));
                        return apiPolicy;
                    } else {
                        // not found
                        String msg = "API Policy not found for id: " + uuid;
                        log.warn(msg);
                        throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving API policy with id: " + uuid;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void addApplicationPolicy(ApplicationPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                addApplicationPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in adding Application policy, policy name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }

        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void addApiPolicy(APIPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                addApiPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in adding API policy, policy name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }

        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void addSubscriptionPolicy(SubscriptionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                addSubscriptionPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in adding Subscription policy, policy name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }

        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void updateApplicationPolicy(ApplicationPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                updateApplicationPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in updating Application policy with name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void updateSubscriptionPolicy(SubscriptionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                updateSubscriptionPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in updating Subscription policy with name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Adds an Application Policy
     *
     * @param policy     {@link ApplicationPolicy} instance
     * @param connection DB Connection instance
     * @throws SQLException if an error occurs while adding an Application Policy
     */
    private static void addApplicationPolicy(ApplicationPolicy policy, Connection connection) throws SQLException {

        final String query = "INSERT INTO AM_APPLICATION_POLICY (UUID, NAME, DISPLAY_NAME, " +
                "DESCRIPTION, QUOTA_TYPE, QUOTA, QUOTA_UNIT, UNIT_TIME, TIME_UNIT, IS_DEPLOYED, CUSTOM_ATTRIBUTES) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        Limit limit = policy.getDefaultQuotaPolicy().getLimit();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policy.getUuid());
            statement.setString(2, policy.getPolicyName());
            statement.setString(3, policy.getDisplayName());
            statement.setString(4, policy.getDescription());
            statement.setString(5, policy.getDefaultQuotaPolicy().getType());
            setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
            statement.setInt(8, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            statement.setString(9, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            statement.setBoolean(10, policy.isDeployed());
            statement.setBytes(11, policy.getCustomAttributes());
            statement.execute();
        }
    }

    /**
     * sets the default throttling policy related information to the DB query
     *
     * @param limit     {@link Limit} instance
     * @param statement DB query related {@link PreparedStatement} instance
     * @throws SQLException if any error occurs while setting default throttle policy related information
     */
    private static void setDefaultThrottlePolicyDetailsPreparedStmt(Limit limit, PreparedStatement statement)
            throws SQLException {
        limit.populateDataInPreparedStatement(statement);
    }

    private static void addApiPolicy(APIPolicy policy, Connection connection) throws SQLException, APIMgtDAOException {

        final String query = "INSERT INTO AM_API_POLICY (UUID, NAME, DISPLAY_NAME, DESCRIPTION, "
                + "DEFAULT_QUOTA_TYPE, DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DEFAULT_UNIT_TIME,"
                + " DEFAULT_TIME_UNIT, APPLICABLE_LEVEL, IS_DEPLOYED) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        Limit limit = policy.getDefaultQuotaPolicy().getLimit();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policy.getUuid());
            statement.setString(2, policy.getPolicyName());
            statement.setString(3, policy.getDisplayName());
            statement.setString(4, policy.getDescription());
            statement.setString(5, policy.getDefaultQuotaPolicy().getType());
            setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
            statement.setLong(8, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            statement.setString(9, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            statement.setString(10, API_TIER_LEVEL);
            statement.setBoolean(11, policy.isDeployed());
            statement.execute();

            if (policy.getPipelines() != null) {
                addAPIPipeline(connection, policy.getPipelines(), policy.getUuid());
            }
        }
    }

    /**
     * Adds an Subscription policy
     *
     * @param policy     {@link SubscriptionPolicy} instance
     * @param connection DB Connection instance
     * @throws SQLException if any error occurs while setting default throttle policy related information
     */
    private static void addSubscriptionPolicy(SubscriptionPolicy policy, Connection connection) throws SQLException {

        String query;

        query = "INSERT INTO AM_SUBSCRIPTION_POLICY (UUID, NAME, DISPLAY_NAME, DESCRIPTION, QUOTA_TYPE, QUOTA, "
                + "QUOTA_UNIT, UNIT_TIME, RATE_LIMIT_COUNT, RATE_LIMIT_TIME_UNIT, CUSTOM_ATTRIBUTES, "
                + "STOP_ON_QUOTA_REACH, BILLING_PLAN, TIME_UNIT, IS_DEPLOYED) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        Limit limit = policy.getDefaultQuotaPolicy().getLimit();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policy.getUuid());
            statement.setString(2, policy.getPolicyName());
            statement.setString(3, policy.getDisplayName());
            statement.setString(4, policy.getDescription());
            statement.setString(5, policy.getDefaultQuotaPolicy().getType());
            setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
            statement.setInt(8, policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            policy.populateDataInPreparedStatement(statement);
            statement.setString(14, policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            statement.setBoolean(15, policy.isDeployed());

            statement.execute();
        }
    }

    @Override
    public void updateApiPolicy(APIPolicy policy) throws APIMgtDAOException {

        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                updateAPIPolicy(policy, connection);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in updating API policy with name: " + policy.getPolicyName();
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void deletePolicy(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
                    deleteApplicationPolicy(policyName, connection);
                } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
                    deleteSubscriptionPolicy(policyName, connection);
                } else if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
                    deleteApiPolicy(policyName, connection);
                }
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in deleting throttling policy for level: " + policyLevel +
                        ", policy name: " + policyName;
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }

        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void deletePolicyByUuid(APIMgtAdminService.PolicyLevel policyLevel, String uuid)
            throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
                    deleteApplicationPolicyByUuid(uuid, connection);
                } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
                    deleteSubscriptionPolicyByUuid(uuid, connection);
                } else if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
                    deleteApiPolicyByUuid(uuid, connection);
                }
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                String errorMessage = "Error in deleting throttling policy for level: " + policyLevel +
                        ", policy id: " + uuid;
                log.error(errorMessage, e);
                throw new APIMgtDAOException(errorMessage, e);

            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            String errorMsg = "Error in obtaining DB connection";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Retrieves all API policies.
     *
     * @return List of {@link APIPolicy} instances, or an empty list if none is found
     * @throws SQLException if an error occurs while retrieving policies
     */
    private List<APIPolicy> getAllApiPolicies() throws SQLException {
        List<APIPolicy> policyList = new ArrayList<>();
        String sqlQuery = "SELECT UUID, NAME, DEFAULT_QUOTA_TYPE, DEFAULT_TIME_UNIT, DEFAULT_UNIT_TIME, "
                + "DEFAULT_QUOTA, DEFAULT_QUOTA_UNIT, DESCRIPTION, DISPLAY_NAME, IS_DEPLOYED, APPLICABLE_LEVEL "
                + "from AM_API_POLICY";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    APIPolicy apiPolicy = new APIPolicy(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                    apiPolicy.setUuid(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID));
                    setCommonPolicyDetails(apiPolicy, resultSet);
                    apiPolicy.setUserLevel(
                            resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_APPLICABLE_LEVEL));
                    apiPolicy.setPipelines(getPipelines(apiPolicy.getUuid(), connection));

                    policyList.add(apiPolicy);
                }
            }
        }
        return policyList;
    }

    @Override
    public boolean policyExists(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException {

        if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
            return apiPolicyExists(policyName);
        } else if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
            return applicationPolicyExists(policyName);
        } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
            return subscriptionPolicyExists(policyName);
        } else {
            String msg = "Invalid Policy level: " + policyLevel;
            log.warn(msg);
            throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }
    }

    /**
     * Check if the particular API Policy for the given name exists
     *
     * @param policyName policy name
     * @return true if the API policy exists, else false
     * @throws APIMgtDAOException if an error occurs while checking the policy existence
     */
    private static boolean apiPolicyExists(String policyName) throws APIMgtDAOException {

        String sqlQuery = "SELECT 1 from AM_API_POLICY WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String errorMsg = "Error in checking whether API Policy exists";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Check if the particular Application Policy for the given name exists
     *
     * @param policyName policy name
     * @return true if the Application policy exists, else false
     * @throws APIMgtDAOException if an error occurs while checking the policy existence
     */
    private static boolean applicationPolicyExists(String policyName) throws APIMgtDAOException {

        String sqlQuery = "SELECT 1 from AM_APPLICATION_POLICY WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String errorMsg = "Error in checking whether Application Policy exists";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Check if the particular Subscription Policy for the given name exists
     *
     * @param policyName policy name
     * @return true if the Subscription policy exists, else false
     * @throws APIMgtDAOException if an error occurs while checking the policy existence
     */
    private static boolean subscriptionPolicyExists(String policyName) throws APIMgtDAOException {

        String sqlQuery = "SELECT 1 from AM_SUBSCRIPTION_POLICY WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String errorMsg = "Error in checking whether Subscription Policy exists";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public List<Policy> getPoliciesByLevel(APIMgtAdminService.PolicyLevel policyLevel) throws APIMgtDAOException {

        List<Policy> policies = new ArrayList<>();
        if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
            policies.addAll(getApiPolicies());
        } else if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
            policies.addAll(getApplicationPolicies());
        } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
            policies.addAll(getSubscriptionPolicies());
        } else {
            String msg = "Invalid Policy level: " + policyLevel;
            log.warn(msg);
            throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }

        return policies;
    }

    @Override
    public Policy getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel policyLevel,
                                          String policyName) throws APIMgtDAOException {

        if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
            return getApiPolicy(policyName);
        } else if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
            return getApplicationPolicy(policyName);
        } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
            return getSubscriptionPolicy(policyName);
        } else {
            String msg = "Invalid Policy level: " + policyLevel;
            log.warn(msg);
            throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }
    }

    @Override
    public List<ApplicationPolicy> getApplicationPolicies() throws APIMgtDAOException {
        try {
            List<ApplicationPolicy> policyList = new ArrayList<>();
            String sqlQuery = "SELECT UUID, NAME, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                    + "DISPLAY_NAME, IS_DEPLOYED from AM_APPLICATION_POLICY";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        ApplicationPolicy applicationPolicy = new ApplicationPolicy(
                                resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                        setCommonPolicyDetails(applicationPolicy, resultSet);
                        policyList.add(applicationPolicy);
                    }
                }
            }
            return policyList;
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Application policies";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String policyName) throws APIMgtDAOException {
        try {
            ApplicationPolicy policy;
            String sqlQuery = "SELECT UUID, NAME, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                    + "DISPLAY_NAME, CUSTOM_ATTRIBUTES, IS_DEPLOYED from AM_APPLICATION_POLICY WHERE NAME = ?";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, policyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        policy = new ApplicationPolicy(
                                resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME));
                        setCommonPolicyDetails(policy, resultSet);
                        return policy;
                    } else {
                        // not found
                        String msg = "Application Policy not found for name: " + policyName;
                        log.warn(msg);
                        throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Application policy with name: " + policyName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUuid(String uuid) throws APIMgtDAOException {
        try {
            final String query = "SELECT UUID, NAME, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                    + "DISPLAY_NAME, CUSTOM_ATTRIBUTES, IS_DEPLOYED from AM_APPLICATION_POLICY WHERE UUID = ?";
            ApplicationPolicy applicationPolicy;
            try (Connection conn = DAOUtil.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        applicationPolicy = new ApplicationPolicy(rs.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_NAME));
                        setCommonPolicyDetails(applicationPolicy, rs);
                        InputStream inputStream = rs.getBinaryStream(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_CUSTOM_ATTRIB);
                        if (inputStream != null) {
                            applicationPolicy.setCustomAttributes(IOUtils.toByteArray(inputStream));
                        }
                        return applicationPolicy;
                    } else {
                        // not found
                        String msg = "Application Policy not found for id: " + uuid;
                        log.warn(msg);
                        throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                    }
                } catch (IOException e) {
                    // error
                    throw new APIMgtDAOException("Internal error", e);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Application Policy with id: " + uuid;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public List<SubscriptionPolicy> getSubscriptionPolicies() throws APIMgtDAOException {
        try {
            List<SubscriptionPolicy> policyList = new ArrayList<>();
            String sqlQuery = "SELECT UUID, NAME, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                    + "DISPLAY_NAME, CUSTOM_ATTRIBUTES, IS_DEPLOYED from AM_SUBSCRIPTION_POLICY";

            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        policyList.add(createSubscriptionPolicyFromResultSet(resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_NAME), resultSet));
                    }
                }
            }
            return policyList;
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Subscription policies";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String policyName) throws APIMgtDAOException {
        try {
            final String query = "SELECT UUID, NAME, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                    + "DISPLAY_NAME, CUSTOM_ATTRIBUTES, IS_DEPLOYED FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";
            try (Connection conn = DAOUtil.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, policyName);
                statement.execute();
                try (ResultSet rs = statement.getResultSet()) {
                    if (rs.next()) {
                        return createSubscriptionPolicyFromResultSet(policyName, rs);
                    } else {
                        // not found
                        String msg = "Subscription Policy not found for name: " + policyName;
                        log.warn(msg);
                        throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Subscription policy for name: " + policyName;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Populated common attributes of policy type objects to <code>policy</code>
     * from <code>resultSet</code>
     *
     * @param policy    initiallized {@link Policy} object to populate
     * @param resultSet {@link ResultSet} with data to populate <code>policy</code>
     * @throws SQLException
     */
    private void setCommonPolicyDetails(Policy policy, ResultSet resultSet) throws SQLException {
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        String prefix = "";

        if (policy instanceof APIPolicy) {
            prefix = "DEFAULT_";
        }

        quotaPolicy.setType(resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants
                .COLUMN_QUOTA_POLICY_TYPE));
        if (resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.REQUEST_COUNT_TYPE)) {
            RequestCountLimit reqLimit = new RequestCountLimit(resultSet.getString(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_TIME_UNIT), resultSet.getInt(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_UNIT_TIME), resultSet.getInt(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_QUOTA));
            quotaPolicy.setLimit(reqLimit);

        } else if (resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_POLICY_TYPE)
                .equalsIgnoreCase(PolicyConstants.BANDWIDTH_TYPE)) {
            BandwidthLimit bandLimit = new BandwidthLimit(resultSet.getString(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_TIME_UNIT), resultSet.getInt(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_UNIT_TIME), resultSet.getInt(prefix + APIMgtConstants
                    .ThrottlePolicyConstants.COLUMN_QUOTA),
                    resultSet.getString(prefix + APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT));
            quotaPolicy.setLimit(bandLimit);
        }

        policy.setUuid(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID));
        policy.setDescription(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION));
        policy.setDisplayName(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DISPLAY_NAME));
        policy.setDefaultQuotaPolicy(quotaPolicy);
        policy.setDeployed(resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DEPLOYED));
    }

    /**
     * Retrieves list of pipelines for the policy with policy Id: <code>policyId</code>
     *
     * @param policyId policy id of the pipelines
     * @return list of pipelines
     * @throws SQLException
     */
    private ArrayList<Pipeline> getPipelines(String policyId, Connection connection) throws SQLException {
        ArrayList<Pipeline> pipelines = new ArrayList<>();
        final String sqlQuery = "SELECT CONDITION_GROUP_ID,QUOTA_TYPE,QUOTA,QUOTA_UNIT,UNIT_TIME,TIME_UNIT," +
                "DESCRIPTION FROM AM_CONDITION_GROUP WHERE UUID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            int unitTime;
            int quota;
            int pipelineId;
            String timeUnit;
            String quotaUnit;
            String description;
            preparedStatement.setString(1, policyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    Pipeline pipeline = new Pipeline();
                    ArrayList<Condition> conditions;
                    QuotaPolicy quotaPolicy = new QuotaPolicy();
                    quotaPolicy.setType(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_QUOTA_POLICY_TYPE));

                    timeUnit = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_TIME_UNIT);
                    quotaUnit = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA_UNIT);
                    unitTime = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UNIT_TIME);
                    quota = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_QUOTA);
                    pipelineId = resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_CONDITION_ID);
                    description = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_DESCRIPTION);
                    if (PolicyConstants.REQUEST_COUNT_TYPE.equals(quotaPolicy.getType())) {
                        RequestCountLimit requestCountLimit = new RequestCountLimit(timeUnit, unitTime, quota);
                        quotaPolicy.setLimit(requestCountLimit);
                    } else if (PolicyConstants.BANDWIDTH_TYPE.equals(quotaPolicy.getType())) {
                        BandwidthLimit bandwidthLimit = new BandwidthLimit(timeUnit, unitTime, quota, quotaUnit);
                        quotaPolicy.setLimit(bandwidthLimit);
                    }
                    conditions = getConditions(pipelineId, connection);
                    pipeline.setConditions(conditions);
                    pipeline.setQuotaPolicy(quotaPolicy);
                    pipeline.setId(pipelineId);
                    pipeline.setDescription(description);
                    pipelines.add(pipeline);
                }
            }
        }
        return pipelines;
    }

    /**
     * Retrieves list of Conditions for a pipeline specified by <code>pipelineId</code>
     *
     * @param pipelineId pipeline Id with conditions to retrieve
     * @return list of Conditions for a pipeline
     * @throws SQLException
     */
    private ArrayList<Condition> getConditions(int pipelineId, Connection connection) throws SQLException {
        ArrayList<Condition> conditions = new ArrayList<>();
        setIPCondition(pipelineId, conditions, connection);
        setHeaderConditions(pipelineId, conditions, connection);
        setQueryParameterConditions(pipelineId, conditions, connection);
        setJWTClaimConditions(pipelineId, conditions, connection);

        return conditions;
    }

    /**
     * Retrieve IP condition from pipeline
     *
     * @param pipelineId id of the pipeline to get ip condition
     * @param conditions condition list to add each ip condition
     * @param connection connection to db
     * @throws SQLException If error occurred while getting ip condition form db
     */
    private void setIPCondition(int pipelineId, ArrayList<Condition> conditions,
                                Connection connection) throws SQLException {
        final String sqlQuery = "SELECT " + "STARTING_IP, " + "ENDING_IP, " + "SPECIFIC_IP,WITHIN_IP_RANGE " + "FROM " +
                "" + "AM_IP_CONDITION " + "WHERE " + "CONDITION_GROUP_ID = ? ";

        String startingIP;
        String endingIP;
        String specificIP;
        boolean invert;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    startingIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_STARTING_IP);
                    endingIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_ENDING_IP);
                    specificIP = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_SPECIFIC_IP);
                    invert = resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_WITHIN_IP_RANGE);

                    if (specificIP != null && !"".equals(specificIP)) {
                        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
                        ipCondition.setSpecificIP(specificIP);
                        ipCondition.setInvertCondition(invert);
                        conditions.add(ipCondition);
                    } else if (startingIP != null && !"".equals(startingIP)) {

                     /*
                     Assumes availability of starting ip means ip range is enforced.
                     Therefore availability of ending ip is not checked.
                    */
                        IPCondition ipRangeCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
                        ipRangeCondition.setStartingIP(startingIP);
                        ipRangeCondition.setEndingIP(endingIP);
                        ipRangeCondition.setInvertCondition(invert);
                        conditions.add(ipRangeCondition);
                    }
                }
            }
        }
    }

    /**
     * Add Header conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param connection Connection of the database
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setHeaderConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection)
            throws SQLException {
        final String query = "SELECT " + "HEADER_FIELD_NAME, " + "HEADER_FIELD_VALUE , IS_HEADER_FIELD_MAPPING "
                + " FROM " + "AM_HEADER_FIELD_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    HeaderCondition headerCondition = new HeaderCondition();
                    headerCondition.setHeader(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_NAME));
                    headerCondition.setValue(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_HEADER_FIELD_VALUE));
                    headerCondition.setInvertCondition(resultSet.getBoolean(
                            APIMgtConstants.ThrottlePolicyConstants.COLUMN_IS_HEADER_FIELD_MAPPING));
                    conditions.add(headerCondition);
                }
            }
        }
    }

    /**
     * Add Query parameter conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setQueryParameterConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection)
            throws SQLException {
        final String query = "SELECT " + "PARAMETER_NAME,PARAMETER_VALUE , IS_PARAM_MAPPING " + "FROM " +
                "AM_QUERY_PARAMETER_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
                    queryParameterCondition
                            .setParameter(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                                    .COLUMN_PARAMETER_NAME));
                    queryParameterCondition.setValue(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_PARAMETER_VALUE));
                    queryParameterCondition.setInvertCondition(
                            resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants.COLUMN_IS_PARAM_MAPPING));
                    conditions.add(queryParameterCondition);
                }
            }
        }
    }

    /**
     * Add JWT claim conditions of pipeline with pipeline Id: <code>pipelineId</code> to a
     * provided {@link Condition} array
     *
     * @param pipelineId Id of the pipeline
     * @param conditions condition array to populate
     * @throws SQLException
     */
    private void setJWTClaimConditions(int pipelineId, ArrayList<Condition> conditions, Connection connection) throws
            SQLException {
        final String query = "SELECT " + "CLAIM_URI, " + "CLAIM_ATTRIB , IS_CLAIM_MAPPING " + "FROM " +
                "AM_JWT_CLAIM_CONDITION " + "WHERE " + "CONDITION_GROUP_ID =?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, pipelineId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
                    jwtClaimsCondition.setClaimUrl(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_URI));
                    jwtClaimsCondition.setAttribute(resultSet.getString(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_CLAIM_ATTRIBUTE));
                    jwtClaimsCondition.setInvertCondition(resultSet.getBoolean(APIMgtConstants.ThrottlePolicyConstants
                            .COLUMN_IS_CLAIM_MAPPING));
                    conditions.add(jwtClaimsCondition);
                }
            }
        }
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUuid(String uuid) throws APIMgtDAOException {
        try {
            return getSubscriptionPolicyById(uuid);
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Subscription policy with id: " + uuid;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public List<APIPolicy> getApiPolicies() throws APIMgtDAOException {
        try {
            return getAllApiPolicies();
        } catch (SQLException e) {
            String errorMsg = "Error in retrieving Api policies";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Retrieves {@link SubscriptionPolicy} with policy uuid <code>uuid</code>
     * <p>This will retrieve complete details about the ApplicationPolicy with all pipelins and conditions.</p>
     *
     * @param uuid uuid of the policy to retrieve from the database
     * @return {@link SubscriptionPolicy}
     */
    private SubscriptionPolicy getSubscriptionPolicyById(String uuid) throws SQLException, APIMgtDAOException {
        final String query = "SELECT NAME, UUID, QUOTA_TYPE, TIME_UNIT, UNIT_TIME, QUOTA, QUOTA_UNIT, DESCRIPTION, "
                + "DISPLAY_NAME, CUSTOM_ATTRIBUTES, IS_DEPLOYED FROM AM_SUBSCRIPTION_POLICY WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, uuid);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return createSubscriptionPolicyFromResultSet(uuid, rs);
                } else {
                    // not found
                    String msg = "Subscription Policy not found for id: " + uuid;
                    log.warn(msg);
                    throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_NOT_FOUND);
                }
            }
        }
    }

    /**
     * Creates a Subscription Policy from the results set
     *
     * @param identifier policy id
     * @param rs         {@link ResultSet} instance
     * @return {@link SubscriptionPolicy} instance
     * @throws SQLException       if any error occurs while creating the Subscription Policy from the result set
     * @throws APIMgtDAOException if any error occurs while retrieving custom attributes for this Subscription Policy
     */
    private SubscriptionPolicy createSubscriptionPolicyFromResultSet(String identifier, ResultSet rs)
            throws SQLException, APIMgtDAOException {

        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(rs.getString(APIMgtConstants.
                ThrottlePolicyConstants.COLUMN_NAME));
        setCommonPolicyDetails(subscriptionPolicy, rs);
        InputStream binary = rs.getBinaryStream(APIMgtConstants.ThrottlePolicyConstants.
                COLUMN_CUSTOM_ATTRIB);
        if (binary != null) {
            byte[] customAttrib;
            try {
                customAttrib = IOUtils.toByteArray(binary);
                if (customAttrib.length > 0) {
                    subscriptionPolicy.setCustomAttributes(customAttrib);
                }
            } catch (IOException e) {
                String errorMsg = "An Error occurred while retrieving custom attributes for subscription policy with "
                        + "identifier: " + identifier;
                log.error(errorMsg, e);
                throw new APIMgtDAOException(errorMsg, e);
            }
        }
        return subscriptionPolicy;
    }

    @Override
    public String getLastUpdatedTimeOfThrottlingPolicy(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException {
        if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
            return getLastUpdatedTimeOfApiPolicy(policyName);
        } else if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
            return getLastUpdatedTimeOfApplicationPolicy(policyName);
        } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
            return getLastUpdatedTimeOfSubscriptionPolicy(policyName);
        } else {
            String msg = "Invalid Policy level: " + policyLevel;
            log.warn(msg);
            throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }
    }

    @Override
    public Set<PolicyValidationData> getAllPolicies() throws APIMgtDAOException {
        Set<PolicyValidationData> policyValidationDataList = new LinkedHashSet();
        final String apiPolicyQuery = "SELECT UUID,NAME FROM AM_API_POLICY";
        final String applicationPolicyQuery = "SELECT UUID,NAME FROM AM_APPLICATION_POLICY";
        final String subscriptionPolicyQuery = "SELECT UUID,NAME,STOP_ON_QUOTA_REACH FROM AM_SUBSCRIPTION_POLICY";
        try (Connection connection = DAOUtil.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(apiPolicyQuery)) {
                    while (resultSet.next()) {
                        policyValidationDataList.add(new PolicyValidationData(resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_NAME), false));
                    }
                }
                try (ResultSet resultSet = statement.executeQuery(applicationPolicyQuery)) {
                    while (resultSet.next()) {
                        policyValidationDataList.add(new PolicyValidationData(resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_NAME), false));
                    }
                }
                try (ResultSet resultSet = statement.executeQuery(subscriptionPolicyQuery)) {
                    while (resultSet.next()) {
                        policyValidationDataList.add(new PolicyValidationData(resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_NAME), resultSet.getBoolean(APIMgtConstants
                                .ThrottlePolicyConstants.COLUMN_STOP_ON_QUOTA_REACH)));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error while retrieving policies";
            log.error(msg, e);
            throw new APIMgtDAOException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return policyValidationDataList;
    }

    @Override
    public Policy getPolicyByLevelAndUUID(APIMgtAdminService.PolicyLevel policyLevel, String policyId) throws
            APIMgtDAOException {
        if (APIMgtAdminService.PolicyLevel.api == policyLevel) {
            return getApiPolicyByUuid(policyId);
        } else if (APIMgtAdminService.PolicyLevel.application == policyLevel) {
            return getApplicationPolicyByUuid(policyId);
        } else if (APIMgtAdminService.PolicyLevel.subscription == policyLevel) {
            return getSubscriptionPolicyByUuid(policyId);
        } else {
            String msg = "Invalid Policy level: " + policyLevel;
            log.warn(msg);
            throw new APIMgtDAOException(msg, ExceptionCodes.POLICY_LEVEL_NOT_SUPPORTED);
        }
    }

    /**
     * Returns the last updated time of an Application policy
     *
     * @param policyName name of the Application policy
     * @return last updated time
     * @throws APIMgtDAOException if an error occurs while retrieving the last updated time
     */
    private String getLastUpdatedTimeOfApplicationPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_APPLICATION_POLICY_TABLE_NAME, policyName);
    }

    /**
     * Returns the last updated time of an Subscription policy
     *
     * @param policyName name of the Subscription policy
     * @return last updated time
     * @throws APIMgtDAOException if an error occurs while retrieving the last updated time
     */
    private String getLastUpdatedTimeOfSubscriptionPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_SUBSCRIPTION_POLICY_TABLE_NAME, policyName);
    }

    /**
     * Returns the last updated time of an API policy
     *
     * @param policyName name of the API policy
     * @return last updated time
     * @throws APIMgtDAOException if an error occurs while retrieving the last updated time
     */
    private String getLastUpdatedTimeOfApiPolicy(String policyName)
            throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByName(AM_API_POLICY_TABLE_NAME, policyName);
    }

    /**
     * Adding pipelines of API policy to database
     *
     * @param connection connection to db
     * @param uuid       policy id/ uuid of the policy
     * @throws SQLException if error occurred while inserting pipeline to db
     */

    private static void addAPIPipeline(Connection connection, List<Pipeline> pipelines, String uuid)
            throws SQLException, APIMgtDAOException {

        final String query =
                "INSERT INTO AM_CONDITION_GROUP (UUID, QUOTA_TYPE, UNIT_TIME, TIME_UNIT, DESCRIPTION, QUOTA, "
                        + "QUOTA_UNIT) VALUES (?,?,?,?,?,?,?)";
        String dbProductName = connection.getMetaData().getDatabaseProductName();
        try (PreparedStatement statement = connection.prepareStatement(query, new String[] {DAOUtil
                .getConvertedAutoGeneratedColumnName(dbProductName,
                APIMgtConstants.ThrottlePolicyConstants.COLUMN_CONDITION_GROUP_ID)})) {
            for (Pipeline pipeline : pipelines) {
                statement.setString(1, uuid);
                statement.setString(2, pipeline.getQuotaPolicy().getType());
                statement.setLong(3, pipeline.getQuotaPolicy().getLimit().getUnitTime());
                statement.setString(4, pipeline.getQuotaPolicy().getLimit().getTimeUnit());
                statement.setString(5, pipeline.getDescription());
                Limit limit = pipeline.getQuotaPolicy().getLimit();
                setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    // get the auto increment id
                    int conditionId = rs.getInt(1);
                    List<Condition> conditionList = pipeline.getConditions();
                    for (Condition condition : conditionList) {
                        if (PolicyConstants.IP_CONDITION_TYPE.equals(condition.getType()) ||
                                PolicyConstants.IP_SPECIFIC_TYPE.equals(condition.getType()) ||
                                PolicyConstants.IP_RANGE_TYPE.equals(condition.getType())) {
                            addIPCondition(connection, condition, conditionId);
                        } else if (PolicyConstants.HEADER_CONDITION_TYPE.equals(condition.getType())) {
                            addHeaderCondition(connection, condition, conditionId);
                        } else if (PolicyConstants.JWT_CLAIMS_CONDITION_TYPE.equals(condition.getType())) {
                            addJWTClaimCondition(connection, condition, conditionId);
                        } else if (PolicyConstants.QUERY_PARAMS_CONDITION_TYPE.equals(condition.getType())) {
                            addParamCondition(connection, condition, conditionId);
                        } else {
                            // unsupported Condition
                            log.warn("Unsupported Condition type: " + condition.getType());
                        }
                    }
                } else {
                    String errorMsg = "Unable to retrieve auto incremented id, hence unable to add Pipeline Condition";
                    throw new APIMgtDAOException(errorMsg);
                }
            }
        }
    }

    /**
     * Adding IP Condition to DB
     *
     * @param connection  connection to db
     * @param ipCondition ip condition of the pipeline in API policy
     * @param conId       condition group id AKA pipeline id
     * @throws SQLException if error occurred while inserting ip condition to db
     */
    private static void addIPCondition(Connection connection, Condition ipCondition, int conId) throws SQLException {
        String query = "INSERT INTO AM_IP_CONDITION (STARTING_IP, ENDING_IP, SPECIFIC_IP, WITHIN_IP_RANGE, "
                + "CONDITION_GROUP_ID) VALUES (?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ipCondition.populateDataInPreparedStatement(statement);
            statement.setBoolean(4, ipCondition.isInvertCondition());
            statement.setInt(5, conId);         //Con id represents condition group id
            statement.execute();
        }
    }

    /**
     * Adding header conditions to DB
     *
     * @param connection      connection to the db
     * @param headerCondition header condition of the pipeline
     * @param conId           condition group id a.k.a pipeline id
     * @throws SQLException
     */
    private static void addHeaderCondition(Connection connection, Condition headerCondition, int conId)
            throws SQLException {
        String query = "INSERT INTO AM_HEADER_FIELD_CONDITION (HEADER_FIELD_NAME, HEADER_FIELD_VALUE, "
                + "CONDITION_GROUP_ID, IS_HEADER_FIELD_MAPPING) VALUES (?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            headerCondition.populateDataInPreparedStatement(statement);
            statement.setInt(3, conId);         //Con id represents condition group id
            statement.setBoolean(4, headerCondition.isInvertCondition());
            statement.execute();
        }
    }

    /**
     * Adding jwt claim condition to DB
     *
     * @param connection         connection to db
     * @param jwtClaimsCondition jwt claim condition of pipeline
     * @param conID              condition group id a.k.a pipeline id
     * @throws SQLException for errors occurred when inserting jwt claim condition to db
     */
    private static void addJWTClaimCondition(Connection connection, Condition jwtClaimsCondition, int conID)
            throws SQLException {
        String query =
                "INSERT INTO AM_JWT_CLAIM_CONDITION (CLAIM_URI, CLAIM_ATTRIB, CONDITION_GROUP_ID, IS_CLAIM_MAPPING) "
                        + "VALUES (?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            jwtClaimsCondition.populateDataInPreparedStatement(statement);
            statement.setInt(3, conID);         //Con id represents condition group id
            statement.setBoolean(4, jwtClaimsCondition.isInvertCondition());
            statement.execute();
        }
    }

    /**
     * Adding query parameter condition to DB
     *
     * @param connection          connection to db
     * @param queryParamCondition query parameter condition of the pipeline
     * @param conID               condition group id a.k.a pipeline id
     * @throws SQLException if error occurred when inserting query parameter condition for db
     */
    private static void addParamCondition(Connection connection, Condition queryParamCondition, int conID)
            throws SQLException {
        String query = "INSERT INTO AM_QUERY_PARAMETER_CONDITION (PARAMETER_NAME, "
                + "PARAMETER_VALUE,CONDITION_GROUP_ID, IS_PARAM_MAPPING) VALUES (?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            queryParamCondition.populateDataInPreparedStatement(statement);
            statement.setInt(3, conID);         //Con id represents condition group id
            statement.setBoolean(4, queryParamCondition.isInvertCondition());
            statement.execute();
        }
    }

    /**
     * Delete API policy from database
     *
     * @param policyName API policy name to be deleted
     * @param connection DB connection instance
     * @throws SQLException if error occurred while deleting API policy
     */
    private void deleteApiPolicy(String policyName, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_API_POLICY WHERE NAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            deletePipelines(connection, policyName);
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        }
    }

    /**
     * Delete pipelines of api policy
     *
     * @param connection connection to the database
     * @param policyName policy name to be deleted
     * @throws SQLException if error occurred when deleting the API policy
     */
    private void deletePipelines(Connection connection, String policyName) throws SQLException {
        String queryUUID = "SELECT UUID FROM AM_API_POLICY WHERE NAME = ?";
        String deleteQuery = "DELETE FROM AM_CONDITION_GROUP WHERE UUID =?";
        try (PreparedStatement statement = connection.prepareStatement(queryUUID)) {
            statement.setString(1, policyName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String uuid = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_UUID);
                    List<Integer> conditionIDs = getConditionGroupIDs(connection, uuid);
                    for (int conId : conditionIDs) {
                        deleteConditions(connection, conId);
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                        preparedStatement.setString(1, uuid);
                        preparedStatement.execute();
                    }
                }
            }
        }
    }

    /**
     * Delete condition of a particular pipeline
     *
     * @param connection connection to db
     * @param conID      condition group id
     * @throws SQLException if error occurred while deleting policy from db
     */
    private void deleteConditions(Connection connection, int conID) throws SQLException {
        deleteIPCondition(connection, conID);
        deleteHeaderCondition(connection, conID);
        deleteJWTCondition(connection, conID);
        deleteQueryParamCondition(connection, conID);
    }

    /**
     * Deleting IP condition from database
     *
     * @param connection connection to the database
     * @param conID      condition group id/ pipeline id of the ip condition
     * @throws SQLException if error occurred when ip condition is deleted
     */
    private void deleteIPCondition(Connection connection, int conID) throws SQLException {

        final String query = "DELETE FROM AM_IP_CONDITION WHERE CONDITION_GROUP_ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
    }

    /**
     * Delete header condition from database
     *
     * @param connection connection to the database
     * @param conID      pipeline id of the header condition
     * @throws SQLException if error occurred while deleting header condition
     */
    private void deleteHeaderCondition(Connection connection, int conID) throws SQLException {

        final String query = "DELETE FROM AM_HEADER_FIELD_CONDITION WHERE CONDITION_GROUP_ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
    }

    /**
     * Delete JWT claim condition from database
     *
     * @param connection connection to the database
     * @param conID      pipeline id of the jwt claim condition
     * @throws SQLException if error occurred when jwt claim is deleted
     */
    private void deleteJWTCondition(Connection connection, int conID) throws SQLException {

        final String query = "DELETE FROM AM_JWT_CLAIM_CONDITION WHERE CONDITION_GROUP_ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
    }

    /**
     * Delete query parameter condition from pipeline
     *
     * @param connection connection to the database
     * @param conID      pipeline id of query parameter condition to be deleted
     * @throws SQLException if error occurred when query parameter condition is deleted
     */
    private void deleteQueryParamCondition(Connection connection, int conID) throws SQLException {

        final String query = "DELETE FROM AM_QUERY_PARAMETER_CONDITION WHERE CONDITION_GROUP_ID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, conID);
            preparedStatement.execute();
        }
    }

    /**
     * Get condition group id / pipeline id for a particular UUID
     *
     * @param connection connection to the database
     * @param uuid       uuid of the corresponding API policy
     * @return return list of integers containing pipeline ids of an API policy
     * @throws SQLException if error occurred while retrieving condition group ids
     */
    private static List<Integer> getConditionGroupIDs(Connection connection, String uuid) throws SQLException {
        String query = "SELECT CONDITION_GROUP_ID FROM AM_CONDITION_GROUP WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Integer> conditionGroupIDs = new ArrayList<>();
                while (resultSet.next()) {
                    conditionGroupIDs
                            .add(resultSet.getInt(APIMgtConstants.ThrottlePolicyConstants.COLUMN_CONDITION_ID));
                }
                return conditionGroupIDs;
            }
        }
    }

    /**
     * Delete application policy by policyName
     *
     * @param policyName application policy name to be deleted
     * @param connection DB Connection instance
     * @throws SQLException if error occurred while deleting application policy from database
     */
    private void deleteApplicationPolicy(String policyName, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_APPLICATION_POLICY WHERE NAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        }
    }

    /**
     * Delete Subscription policy by policyName
     *
     * @param policyName subscription policy name to be deleted
     * @param connection DB Connection instance
     * @throws SQLException if error occurred when deleting subscription policy from database
     */
    private void deleteSubscriptionPolicy(String policyName, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyName);
            preparedStatement.execute();
        }
    }

    /**
     * Delete Application policy by uuid
     *
     * @param uuid       policy uuid
     * @param connection DB Connection instance
     * @throws SQLException if error occurred when deleting policy from database
     */
    private void deleteApplicationPolicyByUuid(String uuid, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_APPLICATION_POLICY WHERE UUID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.execute();
        }
    }

    /**
     * Delete Subscription policy by uuid
     *
     * @param uuid       policy uuid
     * @param connection DB Connection instance
     * @throws SQLException if error occurred when deleting policy from database
     */
    private void deleteSubscriptionPolicyByUuid(String uuid, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_SUBSCRIPTION_POLICY WHERE UUID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.execute();
        }
    }

    /**
     * Delete Api policy by uuid
     *
     * @param uuid       policy uuid
     * @param connection DB Connection instance
     * @throws SQLException if error occurred when deleting policy from database
     */
    private void deleteApiPolicyByUuid(String uuid, Connection connection) throws SQLException {
        String sqlQuery = "DELETE FROM AM_API_POLICY WHERE UUID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.execute();
        }
    }

    /**
     * Updates an existing API policy
     *
     * @param policy     {@link APIPolicy} instance
     * @param connection DB Connection instance
     * @throws SQLException       if an error occurs while updating the API policy
     * @throws APIMgtDAOException if the uuid of the policy is not available
     */
    private void updateAPIPolicy(APIPolicy policy, Connection connection) throws SQLException, APIMgtDAOException {

        String queryFindPolicyName = "SELECT NAME from AM_API_POLICY WHERE UUID = ?";

        if (policy.getUuid() == null || policy.getUuid().isEmpty()) {
            String errorMsg = "Policy uuid is not found, unable to update policy: " + policy.getPolicyName();
            throw new APIMgtDAOException(errorMsg);
        }

        try (PreparedStatement selectStatement = connection.prepareStatement(queryFindPolicyName)) {
            selectStatement.setString(1, policy.getUuid());
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                while (resultSet.next()) {
                    String policyName = resultSet.getString(APIMgtConstants.ThrottlePolicyConstants.COLUMN_NAME);
                    deleteApiPolicy(policyName, connection);
                }
            }
        }
        addApiPolicy(policy, connection);
    }

    /**
     * updates an existing Application Policy
     *
     * @param applicationPolicy {@link Policy} instance
     * @param connection        DB Connection instance
     * @throws SQLException       if an error occurs while updating the API policy
     * @throws APIMgtDAOException if the uuid of the policy is not available
     */
    private void updateApplicationPolicy(Policy applicationPolicy, Connection connection)
            throws APIMgtDAOException, SQLException {

        final String query = "UPDATE AM_APPLICATION_POLICY SET NAME = ?, DISPLAY_NAME = ?, DESCRIPTION = ?, "
                + "QUOTA_TYPE = ?, UNIT_TIME = ?, QUOTA = ?, QUOTA_UNIT = ?, TIME_UNIT = ?, LAST_UPDATED_TIME = ? "
                + "WHERE UUID = ?";

        if (applicationPolicy.getUuid() == null || applicationPolicy.getUuid().isEmpty()) {
            String errorMsg = "Policy uuid is not found, unable to update policy: " + applicationPolicy.getPolicyName();
            throw new APIMgtDAOException(errorMsg);
        }

        Limit limit = applicationPolicy.getDefaultQuotaPolicy().getLimit();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, applicationPolicy.getPolicyName());
            statement.setString(2, applicationPolicy.getDisplayName());
            statement.setString(3, applicationPolicy.getDescription());
            statement.setString(4, applicationPolicy.getDefaultQuotaPolicy().getType());
            statement.setInt(5, applicationPolicy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
            statement.setString(8, applicationPolicy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(10, applicationPolicy.getUuid());

            statement.execute();
        }
    }

    /**
     * updates an existing Subscription Policy
     *
     * @param subscriptionPolicy {@link Policy} instance
     * @param connection         DB Connection instance
     * @throws APIMgtDAOException if an error occurs while updating the Subscription policy
     */
    private void updateSubscriptionPolicy(Policy subscriptionPolicy,
                                          Connection connection) throws APIMgtDAOException, SQLException {

        final String query =
                "UPDATE AM_SUBSCRIPTION_POLICY SET NAME = ?, DISPLAY_NAME = ?, DESCRIPTION = ?, QUOTA_TYPE = ?, "
                        + "UNIT_TIME = ?, QUOTA = ?, QUOTA_UNIT = ?, TIME_UNIT = ?, RATE_LIMIT_COUNT = ?, "
                        + "RATE_LIMIT_TIME_UNIT = ?, CUSTOM_ATTRIBUTES = ?, STOP_ON_QUOTA_REACH = ?, "
                        + "BILLING_PLAN = ?, IS_DEPLOYED = ?, LAST_UPDATED_TIME = ? WHERE UUID = ?";

        if (subscriptionPolicy.getUuid() == null || subscriptionPolicy.getUuid().isEmpty()) {
            String errorMsg = "Policy uuid is not found, unable to update policy: " +
                    subscriptionPolicy.getPolicyName();
            throw new APIMgtDAOException(errorMsg);
        }

        Limit limit = subscriptionPolicy.getDefaultQuotaPolicy().getLimit();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subscriptionPolicy.getPolicyName());
            statement.setString(2, subscriptionPolicy.getDisplayName());
            statement.setString(3, subscriptionPolicy.getDescription());
            statement.setString(4, subscriptionPolicy.getDefaultQuotaPolicy().getType());
            statement.setInt(5, subscriptionPolicy.getDefaultQuotaPolicy().getLimit().getUnitTime());
            setDefaultThrottlePolicyDetailsPreparedStmt(limit, statement);
            statement.setString(8, subscriptionPolicy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            subscriptionPolicy.populateDataInPreparedStatement(statement);
            statement.setBoolean(14, subscriptionPolicy.isDeployed());
            statement.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(16, subscriptionPolicy.getUuid());

            statement.execute();
        }
    }

    @Override
    public String addBlockConditions(BlockConditions blockConditions) throws APIMgtDAOException {
        boolean status = false;
        boolean valid = false;
        String uuid = null;
        String conditionType = blockConditions.getConditionType();
        String conditionValue = blockConditions.getConditionValue();
        try {
            String query = "INSERT INTO AM_BLOCK_CONDITIONS (TYPE, VALUE, ENABLED, UUID) VALUES (?,?,?,?)";
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_API.equals(conditionType)) {
                if (isValidContext(conditionValue)) {
                    valid = true;
                } else {
                    throw new APIMgtDAOException(
                            "Couldn't Save Block Condition Due to Invalid API Context : " + conditionValue);
                }
            } else if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_APPLICATION.equals(conditionType)) {
                String appArray[] = conditionValue.split(":");
                if (appArray.length > 1) {
                    String appUuid = appArray[0];
                    String appName = appArray[1];

                    if (isValidApplication(appName, appUuid)) {
                        valid = true;
                    } else {
                        throw new APIMgtDAOException(
                                "Couldn't Save Block Condition Due to Invalid Application : " + appName + ", UUID :"
                                        + appUuid);
                    }
                }
            } else if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
                valid = true;
            } else if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP.equals(conditionType)) {
                valid = true;
            } else if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE.equals(conditionType)) {
                if (isIPRangeConditionValid(blockConditions.getStartingIP(), blockConditions.getEndingIP())) {
                    valid = true;
                } else {
                    throw new APIMgtDAOException(
                            "Couldn't Save Block Condition Due to Invalid IP Range -> Starting IP : " + blockConditions
                                    .getStartingIP() + " EndingIP : " + blockConditions.getEndingIP());
                }
            }
            if (valid) {
                try (Connection connection = DAOUtil.getConnection();
                     PreparedStatement insertPreparedStatement = connection.prepareStatement(query)) {
                    connection.setAutoCommit(false);
                    if (!isBlockConditionExist(blockConditions)) {
                        uuid = UUID.randomUUID().toString();
                        insertPreparedStatement.setString(1, conditionType);
                        insertPreparedStatement.setString(2, conditionValue);
                        insertPreparedStatement.setBoolean(3, blockConditions.isEnabled());
                        insertPreparedStatement.setString(4, uuid);
                        insertPreparedStatement.execute();
                        if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE.equals(conditionType)) {
                            String ipConditionQuery = "INSERT INTO AM_IP_RANGE_CONDITION (STARTING_IP, ENDING_IP, UUID)"
                                    + " VALUES (?, ?, ?)";
                            try (PreparedStatement ipStatement = connection.prepareStatement(ipConditionQuery)) {
                                ipStatement.setString(1, blockConditions.getStartingIP());
                                ipStatement.setString(2, blockConditions.getEndingIP());
                                ipStatement.setString(3, uuid);
                                ipStatement.execute();
                            } catch (SQLException e) {
                                connection.rollback();
                            }
                        }
                        connection.commit();
                        status = true;
                    } else {
                        throw new BlockConditionAlreadyExistsException(
                                "Condition with type: " + conditionType + ", value: " + conditionValue
                                        + " already exists");
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to add Block condition : " + conditionType + " and " + conditionValue, e);
        }
        if (status) {
            return uuid;
        } else {
            return null;
        }
    }

    @Override
    public BlockConditions getBlockConditionByUUID(String uuid) throws APIMgtDAOException {
        BlockConditions blockCondition = new BlockConditions();
        String query = "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,UUID FROM AM_BLOCK_CONDITIONS WHERE UUID =?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement selectPreparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(true);
            selectPreparedStatement.setString(1, uuid);
            try (ResultSet resultSet = selectPreparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                    blockCondition.setConditionType(resultSet.getString("TYPE"));
                    blockCondition.setConditionValue(resultSet.getString("VALUE"));
                    blockCondition.setConditionId(resultSet.getInt("CONDITION_ID"));
                    blockCondition.setUuid(resultSet.getString("UUID"));
                    if (blockCondition.getConditionType()
                            .equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE)) {
                        String ipQuery = "SELECT STARTING_IP, ENDING_IP FROM AM_IP_RANGE_CONDITION WHERE UUID = ?";
                        try (PreparedStatement selectIpStatement = connection.prepareStatement(ipQuery)) {
                            selectIpStatement.setString(1, uuid);
                            ResultSet rs = selectIpStatement.executeQuery();
                            if (rs.next()) {
                                blockCondition.setStartingIP(rs.getString("STARTING_IP"));
                                blockCondition.setEndingIP(rs.getString("ENDING_IP"));
                            }
                            rs.close();
                        } catch (SQLException e) {
                            connection.rollback();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Block condition by uuid " + uuid, e);
        }
        return blockCondition;
    }

    @Override
    public List<BlockConditions> getBlockConditions() throws APIMgtDAOException {
        List<BlockConditions> blockConditionsList = new ArrayList<BlockConditions>();
        String query = "SELECT CONDITION_ID, TYPE, VALUE, ENABLED, AM_BLOCK_CONDITIONS.UUID, STARTING_IP, ENDING_IP "
                + "FROM AM_BLOCK_CONDITIONS LEFT JOIN AM_IP_RANGE_CONDITION ON "
                + "AM_BLOCK_CONDITIONS.UUID = AM_IP_RANGE_CONDITION.UUID";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement selectPreparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(true);
            try (ResultSet resultSet = selectPreparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    BlockConditions blockConditions = new BlockConditions();
                    blockConditions.setEnabled(resultSet.getBoolean("ENABLED"));
                    blockConditions.setConditionType(resultSet.getString("TYPE"));
                    blockConditions.setConditionValue(resultSet.getString("VALUE"));
                    blockConditions.setConditionId(resultSet.getInt("CONDITION_ID"));
                    blockConditions.setUuid(resultSet.getString("UUID"));
                    blockConditions.setStartingIP(resultSet.getString("STARTING_IP"));
                    blockConditions.setEndingIP(resultSet.getString("ENDING_IP"));
                    blockConditionsList.add(blockConditions);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get Block conditions", e);
        }
        return blockConditionsList;
    }

    @Override
    public boolean updateBlockConditionStateByUUID(String uuid, Boolean state) throws APIMgtDAOException {
        boolean status = false;
        String query = "UPDATE AM_BLOCK_CONDITIONS SET ENABLED = ? WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement updateBlockConditionPreparedStatement = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement.setBoolean(1, state);
            updateBlockConditionPreparedStatement.setString(2, uuid);
            updateBlockConditionPreparedStatement.executeUpdate();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            handleException("Failed to update Block condition with condition UUID " + uuid, e);
        }
        return status;
    }

    @Override
    public boolean deleteBlockConditionByUuid(String uuid) throws APIMgtDAOException {
        boolean status = false;
        try (Connection connection = DAOUtil.getConnection()) {
            String query = "DELETE FROM AM_BLOCK_CONDITIONS WHERE UUID=?";
            String ipRangeQuery = "DELETE FROM AM_IP_RANGE_CONDITION WHERE UUID = ?";
            connection.setAutoCommit(false);
            try (PreparedStatement deleteBlockConditionPreparedStatement = connection.prepareStatement(query)) {
                deleteBlockConditionPreparedStatement.setString(1, uuid);
                deleteBlockConditionPreparedStatement.execute();
            }
            try (PreparedStatement deleteIpRangeStatement = connection.prepareStatement(ipRangeQuery)) {
                deleteIpRangeStatement.setString(1, uuid);
                deleteIpRangeStatement.execute();
                deleteIpRangeStatement.close();
            } catch (SQLException e) {
                connection.rollback();
            }
            connection.commit();
            status = true;
        } catch (SQLException e) {
            handleException("Failed to delete Block condition with condition UUID " + uuid, e);
        }
        return status;
    }

    @Override
    public String addCustomPolicy(CustomPolicy customPolicy) throws APIMgtDAOException {
        String addQuery = "INSERT INTO AM_CUSTOM_POLICY (NAME , KEY_TEMPLATE, DESCRIPTION ,SIDDHI_QUERY,"
                + "IS_DEPLOYED, UUID) VALUES (?,?,?,?,?,?)";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement policyStatement = connection.prepareStatement(addQuery)) {
            policyStatement.setString(1, customPolicy.getPolicyName());
            policyStatement.setString(2, customPolicy.getKeyTemplate());
            policyStatement.setString(3, customPolicy.getDescription());

            String uuid = UUID.randomUUID().toString();
            byte[] byteArray = customPolicy.getSiddhiQuery().getBytes(Charset.defaultCharset());
            policyStatement.setBinaryStream(4, new ByteArrayInputStream(byteArray));
            //todo:change IS_DEPLOYED status after publishing policy to gateway
            policyStatement.setBoolean(5, false);
            policyStatement.setString(6, uuid);
            policyStatement.executeUpdate();
            return uuid;
        } catch (SQLException e) {
            log.error("An Error occurred while adding custom policy with name " + customPolicy.getPolicyName(), e);
            throw new APIMgtDAOException(
                    "Error occurred while adding custom policy with name " + customPolicy.getPolicyName(), e);
        }
    }

    @Override
    public List<CustomPolicy> getCustomPolicies() throws APIMgtDAOException {
        List<CustomPolicy> customPolicyList = new ArrayList<>();
        String getQuery =
                "SELECT NAME, DESCRIPTION, UUID, KEY_TEMPLATE, IS_DEPLOYED, SIDDHI_QUERY FROM " + "AM_CUSTOM_POLICY";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(getQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String siddhiQuery = null;
                    CustomPolicy customPolicy = new CustomPolicy(resultSet.getString("NAME"));
                    customPolicy.setDescription(resultSet.getString("DESCRIPTION"));
                    customPolicy.setUuid(resultSet.getString("UUID"));
                    customPolicy.setKeyTemplate(resultSet.getString("KEY_TEMPLATE"));
                    customPolicy.setDeployed(resultSet.getBoolean("IS_DEPLOYED"));
                    InputStream siddhiQueryBlob = resultSet.getBinaryStream("SIDDHI_QUERY");
                    if (siddhiQueryBlob != null) {
                        try {
                            siddhiQuery = IOUtils.toString(siddhiQueryBlob);
                        } catch (IOException e) {
                            log.error("Error in converting siddhi query blob", e);
                            handleException("Error in converting siddhi query blob", e);
                        }
                    }
                    customPolicy.setSiddhiQuery(siddhiQuery);
                    customPolicyList.add(customPolicy);
                }
            }
            return customPolicyList;
        } catch (SQLException e) {
            log.error("An Error occurred while getting custom policies", e);
            throw new APIMgtDAOException("Error occurred while getting custom policies", e);
        }
    }

    @Override
    public CustomPolicy getCustomPolicyByUuid(String uuid) throws APIMgtDAOException {
        String query = "SELECT NAME, DESCRIPTION, UUID, KEY_TEMPLATE, IS_DEPLOYED, SIDDHI_QUERY FROM AM_CUSTOM_POLICY"
                + " WHERE UUID = ? ";
        CustomPolicy customPolicy = null;
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String siddhiQuery = null;
                    customPolicy = new CustomPolicy(resultSet.getString("NAME"));
                    customPolicy.setDescription(resultSet.getString("DESCRIPTION"));
                    customPolicy.setUuid(resultSet.getString("UUID"));
                    customPolicy.setKeyTemplate(resultSet.getString("KEY_TEMPLATE"));
                    customPolicy.setDeployed(resultSet.getBoolean("IS_DEPLOYED"));
                    InputStream siddhiQueryBlob = resultSet.getBinaryStream("SIDDHI_QUERY");
                    if (siddhiQueryBlob != null) {
                        try {
                            siddhiQuery = IOUtils.toString(siddhiQueryBlob);
                        } catch (IOException e) {
                            log.error("Error in converting siddhi query blob", e);
                            handleException("Error in converting siddhi query blob", e);
                        }
                    }
                    customPolicy.setSiddhiQuery(siddhiQuery);
                }
            }
        } catch (SQLException e) {
            log.error("An Error occurred while getting custom policy with UUID [" + uuid + "], ", e);
            throw new APIMgtDAOException("Error occurred while getting custom policy with UUID : " + uuid, e);
        }
        return customPolicy;
    }

    @Override
    public void updateCustomPolicy(CustomPolicy customPolicy) throws APIMgtDAOException {
        String query = "UPDATE AM_CUSTOM_POLICY SET DESCRIPTION = ?, SIDDHI_QUERY = ?, KEY_TEMPLATE = ?, NAME = ? "
                + "WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(query)) {
            byte[] byteArray = customPolicy.getSiddhiQuery().getBytes(Charset.defaultCharset());
            updateStatement.setString(1, customPolicy.getDescription());
            updateStatement.setBinaryStream(2, new ByteArrayInputStream(byteArray));
            updateStatement.setString(3, customPolicy.getKeyTemplate());
            updateStatement.setString(4, customPolicy.getPolicyName());
            updateStatement.setString(5, customPolicy.getUuid());
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("An Error occurred while updating custom policy with UUID [" + customPolicy.getUuid() + "], ", e);
            throw new APIMgtDAOException(
                    "Error occurred while updating custom policy with UUID : " + customPolicy.getUuid(), e);
        }
    }

    @Override
    public void deleteCustomPolicy(String uuid) throws APIMgtDAOException {
        String deleteQuery = "DELETE FROM AM_CUSTOM_POLICY WHERE UUID = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.execute();
        } catch (SQLException e) {
            log.error("An Error occurred while deleting custom policy with UUID [" + uuid + "], ", e);
            throw new APIMgtDAOException("Error occurred while deleting custom policy with UUID : " + uuid, e);
        }
    }

    @Override
    public Policy getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIMgtDAOException, APIMgtResourceNotFoundException {
        Policy policy = null;
        final String apiPolicyQuery = "SELECT UUID,NAME FROM AM_API_POLICY WHERE NAME = ?";
        final String applicationPolicyQuery = "SELECT UUID,NAME FROM AM_APPLICATION_POLICY WHERE NAME = ?";
        final String subscriptionPolicyQuery = "SELECT UUID,NAME FROM AM_SUBSCRIPTION_POLICY WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection()) {
            if (policyLevel.equals(APIMgtAdminService.PolicyLevel.api)) {
                try (PreparedStatement statement = connection.prepareStatement(apiPolicyQuery)) {
                    statement.setString(1, policyName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            policy = new APIPolicy(resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_NAME));
                        }
                    }
                }
            } else if (policyLevel.equals(APIMgtAdminService.PolicyLevel.application)) {
                try (PreparedStatement statement = connection.prepareStatement(applicationPolicyQuery)) {
                    statement.setString(1, policyName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            policy = new ApplicationPolicy(resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_NAME));
                        }
                    }
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(subscriptionPolicyQuery)) {
                    statement.setString(1, policyName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            policy = new SubscriptionPolicy(resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_UUID), resultSet.getString(APIMgtConstants
                                    .ThrottlePolicyConstants.COLUMN_NAME));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error while retrieving policies";
            log.error(msg, e);
            throw new APIMgtDAOException(msg, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        if (policy == null) {
            throw new APIMgtResourceNotFoundException("Policy " + policyLevel + "Couldn't found " + policyName,
                    ExceptionCodes.POLICY_NOT_FOUND);
        }
        return policy;
    }

    /**
     * Validating context of the API.
     *
     * @param context context of the API
     * @return true/false if context available or not
     * @throws APIMgtDAOException
     */
    private boolean isValidContext(String context) throws APIMgtDAOException {
        String query = "select count(*) COUNT from AM_API where CONTEXT=?";
        boolean status = false;
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement validateContextPreparedStatement = connection.prepareStatement(query)) {
            validateContextPreparedStatement.setString(1, context);
            try (ResultSet resultSet = validateContextPreparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt("COUNT") > 0) {
                    status = true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check Block condition with context " + context, e);
        }
        return status;
    }

    /**
     * validate the blocking application.
     *
     * @param appName name of the application
     * @param uuid    uuid of the application
     * @return return true/false depends of the success
     * @throws APIMgtDAOException if failed validating application
     */
    private boolean isValidApplication(String appName, String uuid) throws APIMgtDAOException {
        boolean status = false;
        String query = "SELECT * FROM AM_APPLICATION WHERE UUID = ? ";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement validateApplicationPreparedStatement = connection.prepareStatement(query)) {
            //todo:Decide a unique field to get applications
            connection.setAutoCommit(false);
            validateApplicationPreparedStatement.setString(1, uuid);
            try (ResultSet resultSet = validateApplicationPreparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    status = true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check Block condition with Application Name " + appName, e);
        }
        return status;
    }

    /**
     * Check if a blocking condition already exists.
     *
     * @param blockConditions BlockConditions object to be added
     * @return true/false depending on the success
     * @throws APIMgtDAOException If failed to check if block condition exist
     */
    private boolean isBlockConditionExist(BlockConditions blockConditions) throws APIMgtDAOException {
        boolean status = false;
        if (blockConditions.getConditionType()
                .equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE)) {
            String isExistQuery = "SELECT STARTING_IP, ENDING_IP FROM AM_IP_RANGE_CONDITION WHERE STARTING_IP =? "
                    + "AND ENDING_IP =?";
            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement checkIsExistPreparedStatement = connection.prepareStatement(isExistQuery)) {
                checkIsExistPreparedStatement.setString(1, blockConditions.getStartingIP());
                checkIsExistPreparedStatement.setString(2, blockConditions.getEndingIP());
                try (ResultSet checkIsResultSet = checkIsExistPreparedStatement.executeQuery()) {
                    if (checkIsResultSet.next()) {
                        status = true;
                    }
                }
            } catch (SQLException e) {
                String msg =
                        "Couldn't check the IP range blacklist condition exist with starting IP: " + blockConditions
                                .getStartingIP() + ", ending IP: " + blockConditions.getEndingIP();
                log.error(msg, e);
                handleException(msg, e);
            }
        } else {
            String isExistQuery = "SELECT CONDITION_ID,TYPE,VALUE,ENABLED,UUID FROM AM_BLOCK_CONDITIONS WHERE TYPE =? "
                    + "AND VALUE =?";
            try (Connection connection = DAOUtil.getConnection();
                 PreparedStatement checkIsExistPreparedStatement = connection.prepareStatement(isExistQuery)) {
                connection.setAutoCommit(false);
                checkIsExistPreparedStatement.setString(1, blockConditions.getConditionType());
                checkIsExistPreparedStatement.setString(2, blockConditions.getConditionValue());
                try (ResultSet checkIsResultSet = checkIsExistPreparedStatement.executeQuery()) {
                    if (checkIsResultSet.next()) {
                        status = true;
                    }
                }
            } catch (SQLException e) {
                String msg = "Couldn't check the Block Condition Exist with condition type: " + blockConditions
                        .getConditionType() + ", condition value: " + blockConditions.getConditionValue();
                log.error(msg, e);
                handleException(msg, e);
            }
        }
        return status;
    }

    /**
     * Check whether given ip address are valid.
     *
     * @param startingIp starting IP address
     * @param endingIp   ending IP address
     * @return true/false depending on the validity
     */
    private boolean isIPRangeConditionValid(String startingIp, String endingIp) {
        Boolean status = false;
        Long startingIP = ipToLong(startingIp);
        Long endingIP = ipToLong(endingIp);
        if (startingIP < endingIP) {
            status = true;
        } else {
            log.error("IR Range is not valid. starting IP :" + startingIp + ", ending IP :" + endingIp);
        }
        return status;
    }

    /**
     * Handle exception occurred within a method by throwing APIMgtDAOException.
     *
     * @param msg message to be shown in console
     * @param t   exception to throw
     * @throws APIMgtDAOException if failed to execute
     */
    private void handleException(String msg, Throwable t) throws APIMgtDAOException {
        log.error(msg, t);
        throw new APIMgtDAOException(msg, t);
    }

    /**
     * Convert IP address to long
     *
     * @param ip ip address to be converted to long
     * @return ip address in long
     */
    private long ipToLong(String ip) {
        long ipAddressinLong = 0;
        if (ip != null) {
            //convert ipaddress into a long
            String[] ipAddressArray = ip.split("\\.");    //split by "." and add to an array

            for (int i = 0; i < ipAddressArray.length; i++) {
                int power = 3 - i;
                long ipAddress = Long.parseLong(ipAddressArray[i]);   //parse to long
                ipAddressinLong += ipAddress * Math.pow(256, power);
            }
        }
        return ipAddressinLong;
    }
}
