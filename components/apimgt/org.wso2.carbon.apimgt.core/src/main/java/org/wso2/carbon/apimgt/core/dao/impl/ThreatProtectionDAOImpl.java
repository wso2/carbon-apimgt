/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO Layer implementation class for Threat Protection Policies
 */
public class ThreatProtectionDAOImpl implements ThreatProtectionDAO {
    private static final String THREAT_PROTECTION_TABLE = "AM_THREAT_PROTECTION_POLICIES";

    //DB Column names
    private static final String F_UUID = "UUID";
    private static final String F_NAME = "NAME";
    private static final String F_TYPE = "TYPE";
    private static final String F_POLICY = "POLICY";

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ThreatProtectionPolicy> getPolicies() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getPolicies(connection);
        } catch (SQLException e) {
            String errorMsg = "Error getting Threat Protection policies";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreatProtectionPolicy getPolicy(String policyId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getPolicy(policyId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error getting Threat Protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            if (isPolicyExists(policy.getUuid())) {
                updatePolicy(policy, connection);
                return;
            }
            addPolicy(policy, connection);
        } catch (SQLException e) {
            String errorMsg = "Error adding Threat Protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPolicyExists(String policyId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return isPolicyExists(policyId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error checking policy existence status for PolicyId: " + policyId;
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getThreatProtectionPolicyIdsForApi(String apiId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getThreatProtectionPolicyIdsForApi(apiId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error getting threat protection policy ids for API_ID: " + apiId;
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            //handling global policy creation
            //Global policies have ids 'GLOBAL-JSON' or 'GLOBAL-XML'
            if ("GLOBAL-JSON".equalsIgnoreCase(policy.getUuid()) || "GLOBAL-XML".equalsIgnoreCase(policy.getUuid())) {
                if (!isPolicyExists(policy.getUuid())) {
                    addPolicy(policy, connection);
                    return;
                }
            }
            updatePolicy(policy, connection);
        } catch (SQLException e) {
            String errorMsg = "Error updating policy for PolicyId: " + policy.getUuid();
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePolicy(String policyId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            deletePolicy(policyId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error deleting policy for PolicyId: " + policyId;
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Get a list of all threat protection policies
     * @param connection SQL Connection
     * @return  List of threat protection policies
     * @throws APIMgtDAOException if failed to retrieve the list of policies
     */
    private List<ThreatProtectionPolicy> getPolicies(Connection connection) throws APIMgtDAOException {
        final String sqlQuery = "SELECT UUID, NAME, TYPE, POLICY " +
                " FROM " + THREAT_PROTECTION_TABLE;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<ThreatProtectionPolicy> list = new ArrayList<>();
                while (rs.next()) {
                    ThreatProtectionPolicy policy = new ThreatProtectionPolicy();
                    policy.setUuid(rs.getString(F_UUID));
                    policy.setName(rs.getString(F_NAME));
                    policy.setType(rs.getString(F_TYPE));
                    policy.setPolicy(new String(rs.getBytes(F_POLICY), "UTF-8"));
                    list.add(policy);
                }
                return list;
            } catch (UnsupportedEncodingException e) {
                String errorMsg = "Charset error in threat protection policy";
                throw new APIMgtDAOException(errorMsg, e);
            }
        } catch (SQLException e) {
            String errorMsg = "Error getting threat protection policies";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Get a single threat protection policy
     * @param policyId Threat protection policy id
     * @param connection SQL Connection
     * @return The threat protection policy with policy id. null if not found.
     * @throws APIMgtDAOException If failed to retrieve the policy
     */
    private ThreatProtectionPolicy getPolicy(String policyId, Connection connection) throws APIMgtDAOException {
        final String sqlQuery = "SELECT UUID, NAME, TYPE, POLICY " +
                " FROM " + THREAT_PROTECTION_TABLE + " WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                ThreatProtectionPolicy policy = null;
                if (rs.next()) {
                    policy = new ThreatProtectionPolicy();
                    policy.setUuid(rs.getString(F_UUID));
                    policy.setName(rs.getString(F_NAME));
                    policy.setType(rs.getString(F_TYPE));
                    policy.setPolicy(new String(rs.getBytes(F_POLICY), "UTF-8"));
                } else {
                    log.warn("No Threat Protection Policy found for PolicyId: " + policyId);
                }
                return policy;
            } catch (UnsupportedEncodingException e) {
                String errorMsg = "Charset error in threat protection policy";
                throw new APIMgtDAOException(errorMsg, e);
            }
        } catch (SQLException e) {
            String errorMsg = "Error getting threat protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Add a threat protection policy to database
     * @param policy Threat protection policy
     * @param connection SQL Connection
     * @throws APIMgtDAOException If failed to add policy
     */
    private void addPolicy(ThreatProtectionPolicy policy, Connection connection) throws APIMgtDAOException {
        final String sqlQuery = "INSERT INTO " + THREAT_PROTECTION_TABLE +
                " (UUID, NAME, TYPE, POLICY) " +
                " VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policy.getUuid());
            preparedStatement.setString(2, policy.getName());
            preparedStatement.setString(3, policy.getType());
            preparedStatement.setBytes(4, policy.getPolicy().getBytes("UTF-8"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errorMsg = "Error adding Threat Protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Charset error in threat protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Update a threat protection policy
     * @param policy Policy to be updated
     * @param connection SQL Connection
     * @throws APIMgtDAOException If failed to update policy
     */
    private void updatePolicy(ThreatProtectionPolicy policy, Connection connection) throws APIMgtDAOException {
        final String sqlQuery = "UPDATE " + THREAT_PROTECTION_TABLE +
                " SET NAME = ?, " +
                "TYPE = ?, " +
                "POLICY = ? " +
                "WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policy.getName());
            preparedStatement.setString(2, policy.getType());
            preparedStatement.setBytes(3, policy.getPolicy().getBytes("UTF-8"));
            preparedStatement.setString(4, policy.getUuid());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errorMsg = "Error updating Threat Protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Charset error in threat protection policy";
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Check whether a threat protection policy exists
     * @param policyId PolicyId to be checked
     * @param connection SQL Connection
     * @return True if policy exists, false otherwise
     * @throws APIMgtDAOException
     */
    private boolean isPolicyExists(String policyId, Connection connection) throws APIMgtDAOException {
        final String sqlQuery = "SELECT UUID FROM " + THREAT_PROTECTION_TABLE + " WHERE " +
                "UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String errorMsg = "Error querying policy status for PolicyId: " + policyId;
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    /**
     * Get a list of threat protection policyIds associated with an API
     * @param apiId ApiId to be checked
     * @param connection SQL Connection
     * @return A list of threat protection policy ids
     * @throws SQLException If failed to retrieve the list of ids
     */
    private Set<String> getThreatProtectionPolicyIdsForApi(String apiId, Connection connection) throws SQLException {
        final String query = "SELECT POLICY_ID FROM AM_THREAT_PROTECTION_MAPPING WHERE API_ID = ?";
        Set<String> policyIds = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            try (ResultSet res = statement.executeQuery()) {
                while (res.next()) {
                    policyIds.add(res.getString("POLICY_ID"));
                }
            }
        }
        return policyIds;
    }

    /**
     * Delete a threat protection policy
     * @param policyId ID of the policy to be deleted
     * @param connection SQL Connection
     * @throws SQLException If failed to delete the policy
     */
    private void deletePolicy(String policyId, Connection connection) throws SQLException {
        final String query = "DELETE FROM " + THREAT_PROTECTION_TABLE + " WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyId);
            statement.executeUpdate();
        }
    }
}
