/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.generic.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.generic.dao.SuccessorMappingDAO;
import org.wso2.carbon.apimgt.governance.generic.dao.SuccessorMappingSQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Implementation of SuccessorMappingDAO for database operations.
 * Manages AM_API_SUCCESSOR_MAPPING rows that persist "The Guide" engine's
 * user-confirmed successor selection across Deprecate / Retire transitions.
 */
public class SuccessorMappingDAOImpl implements SuccessorMappingDAO {

    private static final Log log = LogFactory.getLog(SuccessorMappingDAOImpl.class);

    private static final class SingletonHelper {
        private static final SuccessorMappingDAO INSTANCE = new SuccessorMappingDAOImpl();
    }

    /**
     * Private constructor for singleton.
     */
    private SuccessorMappingDAOImpl() {
    }

    /**
     * Gets the singleton instance.
     *
     * @return SuccessorMappingDAO instance
     */
    public static SuccessorMappingDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void addSuccessorMapping(String apiUuid, String successorUuid, String organization)
            throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(SuccessorMappingSQLConstants.UPSERT_MAPPING);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            stmt.setString(1, apiUuid);
            stmt.setString(2, successorUuid);
            stmt.setString(3, organization);
            stmt.setTimestamp(4, now);

            stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Persisted successor mapping: " + apiUuid + " -> " + successorUuid
                        + " [org=" + organization + "]");
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back successor mapping transaction", rollbackEx);
                }
            }
            String msg = "Error persisting successor mapping for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            closeQuietly(stmt, connection);
        }
    }

    @Override
    public String getSuccessorId(String apiUuid, String organization) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            stmt = connection.prepareStatement(SuccessorMappingSQLConstants.GET_SUCCESSOR);
            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("SUCCESSOR_UUID");
            }
            return null;

        } catch (SQLException e) {
            String msg = "Error retrieving successor mapping for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            closeQuietly(rs, stmt, connection);
        }
    }

    @Override
    public void deleteSuccessorMapping(String apiUuid, String organization)
            throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(SuccessorMappingSQLConstants.DELETE_MAPPING);
            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);
            stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Deleted successor mapping for API: " + apiUuid
                        + " [org=" + organization + "]");
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back delete transaction", rollbackEx);
                }
            }
            String msg = "Error deleting successor mapping for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            closeQuietly(stmt, connection);
        }
    }

    @Override
    public boolean mappingExists(String apiUuid, String organization) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            stmt = connection.prepareStatement(SuccessorMappingSQLConstants.CHECK_MAPPING_EXISTS);
            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);
            rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            String msg = "Error checking successor mapping existence for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            closeQuietly(rs, stmt, connection);
        }
    }

    @Override
    public void deleteAllReferences(String apiUuid, String organization) throws APIMGovernanceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = APIMGovernanceDBUtil.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(SuccessorMappingSQLConstants.DELETE_ALL_REFERENCES);
            stmt.setString(1, apiUuid);
            stmt.setString(2, apiUuid);
            stmt.setString(3, organization);
            int deleted = stmt.executeUpdate();
            connection.commit();

            if (log.isDebugEnabled()) {
                log.debug("Deleted " + deleted + " successor mapping row(s) referencing API: "
                        + apiUuid + " [org=" + organization + "]");
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    log.error("Error rolling back deleteAllReferences transaction", rollbackEx);
                }
            }
            String msg = "Error deleting successor mappings referencing API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        } finally {
            closeQuietly(stmt, connection);
        }
    }

    // ---- Helpers ----

    private void closeQuietly(PreparedStatement stmt, Connection connection) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { log.error("Error closing statement", e); }
        }
        if (connection != null) {
            try { connection.close(); } catch (SQLException e) { log.error("Error closing connection", e); }
        }
    }

    private void closeQuietly(ResultSet rs, PreparedStatement stmt, Connection connection) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { log.error("Error closing result set", e); }
        }
        closeQuietly(stmt, connection);
    }
}
