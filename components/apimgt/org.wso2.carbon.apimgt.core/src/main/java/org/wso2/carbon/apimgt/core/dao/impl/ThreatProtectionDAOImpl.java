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
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Override
    public List<ThreatProtectionPolicy> getPolicies() throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getPolicies(connection);
        } catch (SQLException e) {
            String errorMsg = "Error getting Threat Protection policies";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public ThreatProtectionPolicy getPolicy(String policyId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return getPolicy(policyId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error getting Threat Protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void addPolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            //check for update
            if (isPolicyExists(policy.getUuid(), connection)) {
                updatePolicy(policy, connection);
                return;
            }
            policy.setUuid(UUID.randomUUID().toString());
            addPolicy(policy, connection);
        } catch (SQLException e) {
            String errorMsg = "Error adding Threat Protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public boolean isPolicyExists(String policyId) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            return isPolicyExists(policyId, connection);
        } catch (SQLException e) {
            String errorMsg = "Error checking policy existence status for PolicyId: " + policyId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void updatePolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException {
        try (Connection connection = DAOUtil.getConnection()) {
            updatePolicy(policy, connection);
        } catch (SQLException e) {
            String errorMsg = "Error updating policy for PolicyId: " + policy.getUuid();
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    @Override
    public void deletePolicy(String policyId) throws APIMgtDAOException {
        //to be implemented
    }

    private List<ThreatProtectionPolicy> getPolicies(Connection connection) throws APIMgtDAOException {
        String sqlQuery = "SELECT `UUID`, `NAME`, `TYPE`, `POLICY` " +
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
                log.error(errorMsg, e);
                throw new APIMgtDAOException(errorMsg, e);
            }
        } catch (SQLException e) {
            String errorMsg = "Error getting threat protection policies";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    private ThreatProtectionPolicy getPolicy(String policyId, Connection connection) throws APIMgtDAOException {
        String sqlQuery = "SELECT `UUID`, `NAME`, `TYPE`, `POLICY` " +
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
                log.error(errorMsg, e);
                throw new APIMgtDAOException(errorMsg, e);
            }
        } catch (SQLException e) {
            String errorMsg = "Error getting threat protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    private void addPolicy(ThreatProtectionPolicy policy, Connection connection) throws APIMgtDAOException {
        String sqlQuery = "INSERT INTO " + THREAT_PROTECTION_TABLE +
                " (`UUID`, `NAME`, `TYPE`, `POLICY`) " +
                " VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policy.getUuid());
            preparedStatement.setString(2, policy.getName());
            preparedStatement.setString(3, policy.getType());
            preparedStatement.setBytes(4, policy.getPolicy().getBytes("UTF-8"));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errorMsg = "Error adding Threat Protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Charset error in threat protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    private void updatePolicy(ThreatProtectionPolicy policy, Connection connection) throws APIMgtDAOException {
        String sqlQuery = "UPDATE " + THREAT_PROTECTION_TABLE +
                " SET `NAME` = ?, " +
                "`TYPE` = ?, " +
                "`POLICY` = ?, " +
                "WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policy.getName());
            preparedStatement.setString(2, policy.getType());
            preparedStatement.setBytes(3, policy.getPolicy().getBytes("UTF-8"));
            preparedStatement.setString(4, policy.getUuid());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errorMsg = "Error updating Threat Protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Charset error in threat protection policy";
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

    private boolean isPolicyExists(String policyId, Connection connection) throws APIMgtDAOException {
        String sqlQuery = "SELECT UUID FROM " + THREAT_PROTECTION_TABLE + " WHERE " +
                "UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, policyId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            String errorMsg = "Error querying policy status for PolicyId: " + policyId;
            log.error(errorMsg, e);
            throw new APIMgtDAOException(errorMsg, e);
        }
    }

}
