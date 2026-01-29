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

package org.wso2.carbon.apimgt.governance.gatekeeper.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.MinHashDAO;
import org.wso2.carbon.apimgt.governance.gatekeeper.dao.MinHashSQLConstants;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.APISignature;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MinHashDAO for database operations.
 */
public class MinHashDAOImpl implements MinHashDAO {

    private static final Log log = LogFactory.getLog(MinHashDAOImpl.class);

    private static final class SingletonHelper {
        private static final MinHashDAO INSTANCE = new MinHashDAOImpl();
    }

    /**
     * Private constructor for singleton.
     */
    private MinHashDAOImpl() {
    }

    /**
     * Gets the singleton instance.
     *
     * @return MinHashDAO instance
     */
    public static MinHashDAO getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public void storeSignature(APISignature apiSignature) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.INSERT_SIGNATURE)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            stmt.setString(1, apiSignature.getApiUuid());
            stmt.setBytes(2, apiSignature.getSignatureBlob());
            stmt.setString(3, apiSignature.getOrganization());
            stmt.setTimestamp(4, now);
            stmt.setTimestamp(5, now);

            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Stored MinHash signature for API: " + apiSignature.getApiUuid());
            }

        } catch (SQLException e) {
            String msg = "Error storing MinHash signature for API: " + apiSignature.getApiUuid();
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public void updateSignature(APISignature apiSignature) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.UPDATE_SIGNATURE)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            stmt.setBytes(1, apiSignature.getSignatureBlob());
            stmt.setTimestamp(2, now);
            stmt.setString(3, apiSignature.getApiUuid());
            stmt.setString(4, apiSignature.getOrganization());

            int updatedRows = stmt.executeUpdate();

            if (updatedRows == 0) {
                log.warn("No signature found to update for API: " + apiSignature.getApiUuid());
            } else if (log.isDebugEnabled()) {
                log.debug("Updated MinHash signature for API: " + apiSignature.getApiUuid());
            }

        } catch (SQLException e) {
            String msg = "Error updating MinHash signature for API: " + apiSignature.getApiUuid();
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public APISignature getSignature(String apiUuid, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.GET_SIGNATURE)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSignature(rs);
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving MinHash signature for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return null;
    }

    @Override
    public List<APISignature> getAllSignatures(String organization) throws APIMGovernanceException {
        List<APISignature> signatures = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.GET_ALL_SIGNATURES_BY_ORG)) {

            stmt.setString(1, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    signatures.add(mapResultSetToSignature(rs));
                }
            }

        } catch (SQLException e) {
            String msg = "Error retrieving all MinHash signatures for organization: " + organization;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return signatures;
    }

    @Override
    public List<APISignature> getAllSignatures() throws APIMGovernanceException {
        List<APISignature> signatures = new ArrayList<>();

        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.GET_ALL_SIGNATURES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                signatures.add(mapResultSetToSignature(rs));
            }

        } catch (SQLException e) {
            String msg = "Error retrieving all MinHash signatures";
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }

        return signatures;
    }

    @Override
    public void deleteSignature(String apiUuid, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.DELETE_SIGNATURE)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);

            int deletedRows = stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Deleted " + deletedRows + " MinHash signature(s) for API: " + apiUuid);
            }

        } catch (SQLException e) {
            String msg = "Error deleting MinHash signature for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public boolean signatureExists(String apiUuid, String organization) throws APIMGovernanceException {
        try (Connection connection = APIMGovernanceDBUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(MinHashSQLConstants.CHECK_SIGNATURE_EXISTS)) {

            stmt.setString(1, apiUuid);
            stmt.setString(2, organization);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            String msg = "Error checking if MinHash signature exists for API: " + apiUuid;
            log.error(msg, e);
            throw new APIMGovernanceException(msg, e);
        }
    }

    @Override
    public void upsertSignature(APISignature apiSignature) throws APIMGovernanceException {
        if (signatureExists(apiSignature.getApiUuid(), apiSignature.getOrganization())) {
            updateSignature(apiSignature);
        } else {
            storeSignature(apiSignature);
        }
    }

    /**
     * Maps a ResultSet row to an APISignature object.
     *
     * @param rs The ResultSet
     * @return APISignature object
     * @throws SQLException If mapping fails
     */
    private APISignature mapResultSetToSignature(ResultSet rs) throws SQLException {
        APISignature signature = new APISignature();
        signature.setApiUuid(rs.getString("API_UUID"));
        signature.setSignatureBlob(rs.getBytes("SIGNATURE_BLOB"));
        signature.setOrganization(rs.getString("ORGANIZATION"));
        signature.setCreatedTime(rs.getTimestamp("CREATED_TIME"));
        signature.setUpdatedTime(rs.getTimestamp("UPDATED_TIME"));
        return signature;
    }
}
