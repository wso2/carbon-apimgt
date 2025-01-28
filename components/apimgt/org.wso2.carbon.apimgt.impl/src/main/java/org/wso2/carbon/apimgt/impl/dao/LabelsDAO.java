/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the LabelsDAO
 */
public class LabelsDAO {

    private static final Log log = LogFactory.getLog(LabelsDAO.class);
    private static LabelsDAO instance = null;

    private LabelsDAO() {}

    /**
     * Returns the single instance of LabelsDAO, creating it if necessary.
     *
     * @return the singleton instance of LabelsDAO
     */
    public static LabelsDAO getInstance() {
        if (instance == null) {
            instance = new LabelsDAO();
        }
        return instance;
    }

    /**
     * Adds an Label
     *
     * @param label label to be added
     * @param tenantDomain tenant domain
     * @return Label added label
     * @throws APIManagementException if failed to add the label
     */
    public Label addLabel(Label label, String tenantDomain) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_LABEL_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, label.getLabelId());
            prepStmt.setString(2, label.getName());
            prepStmt.setString(3, label.getDescription());
            prepStmt.setString(4, tenantDomain);
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("Failed to rollback the add Label: " + label.getLabelId(), ex);
            }
            handleException("Error while adding the Label: " + label.getLabelId() + " to the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        return label;
    }

        /**
     * Update Label
     *
     * @param label Label object with updated details
     * @throws APIManagementException if failed to update
     */
    public void updateLabel(Label label) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_LABEL_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, label.getName());
            prepStmt.setString(2, label.getDescription());
            prepStmt.setString(3, label.getLabelId());
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("Failed to rollback the update Label: " + label.getLabelId(), ex);
            }
            handleException("Error while updating the Label: " + label.getLabelId() + " in the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Get all available Labels of the Tenant Domain
     *
     * @param tenantDomain tenant domain
     * @return List<Label> list of labels
     * @throws APIManagementException if failed to get labels
     */
    public List<Label> getAllLabels(String tenantDomain) throws APIManagementException {

        List<Label> labelsList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.GET_LABELS_BY_TENANT_DOMAIN_SQL)) {
            statement.setString(1, tenantDomain);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label();
                    label.setLabelId(rs.getString("UUID"));
                    label.setName(rs.getString("NAME"));
                    label.setDescription(rs.getString("DESCRIPTION"));

                    labelsList.add(label);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve Labels for Tenant Domain " + tenantDomain, e);
        }
        return labelsList;
    }

    /**
     * Checks whether the given label name is already available under given tenant domain
     *
     * @param labelName label name
     * @param uuid label UUID
     * @param tenantDomain tenant domain
     * @return true if the given name is already exists
     * @throws APIManagementException if failed to get name existence
     */
    public boolean isLabelNameExists(String labelName, String uuid, String tenantDomain) throws APIManagementException {

        String sql = SQLConstants.IS_LABEL_NAME_EXISTS_FOR_ANOTHER_UUID_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, labelName);
            statement.setString(2, tenantDomain);
            statement.setString(3, uuid);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("LABEL_COUNT");
                    if (count > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check whether Label name : " + labelName + " exists", e);
        }
        return false;
    }

    /**
     * Checks whether the given label name is already available under given tenant domain
     *
     * @param labelName label name
     * @param tenantDomain tenant domain
     * @return true if the given name is already exists
     * @throws APIManagementException if failed to get name existence
     */
    public boolean isLabelNameExists(String labelName, String tenantDomain) throws APIManagementException {

        String sql = SQLConstants.IS_LABEL_NAME_EXISTS_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, labelName);
            statement.setString(2, tenantDomain);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("LABEL_COUNT");
                    if (count > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check whether Label name : " + labelName + " exists", e);
        }
        return false;
    }

    /**
     * Get a label by UUID
     *
     * @param labelID label UUID
     * @param tenantDomain tenant domain
     * @return Label label object
     * @throws APIManagementException if failed to get
     */
    public Label getLabelByIdAndTenantDomain(String labelID, String tenantDomain) throws APIManagementException {

        Label label = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.GET_LABEL_BY_UUID_AND_TENANT_DOMAIN__SQL)) {
            statement.setString(1, labelID);
            statement.setString(2, tenantDomain);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    label = new Label();
                    label.setName(resultSet.getString("NAME"));
                    label.setDescription(resultSet.getString("DESCRIPTION"));
                    label.setLabelId(labelID);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to fetch Label : " + labelID, e);
        }
        return label;
    }

    /**
     * Delete a label
     *
     * @param labelID label UUID
     * @throws APIManagementException if failed to delete label
     */
    public void deleteLabel(String labelID) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_LABEL_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, labelID);
            prepStmt.execute();

            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("Failed to rollback the delete Label: " + labelID, ex);
            }
            handleException("Error while deleting the Label: " + labelID + " from the database", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Checks whether APIs are mapped with the given label
     *
     * @param labelID label UUID
     * @return true if there are API mappings for the given label
     * @throws APIManagementException if failed to get mappings
     */
    public boolean hasAPIsForLabel(String labelID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SQLConstants.IS_ANY_MAPPING_EXISTS_FOR_LABEL_SQL)) {
            statement.setString(1, labelID);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("MAPPING_COUNT") > 0;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check API mappings for label ID: " + labelID, e);
        }
        return false;
    }

    /**
     * Add a label mapping to an API
     *
     * @param apiID API Id
     * @param labelIDs label UUIDs
     * @throws APIManagementException if failed to add mapping
     */
    public void addApiLabelMappings (String apiID, List<String> labelIDs) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_API_LABEL_MAPPING_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            for (String labelID : labelIDs) {
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, apiID);
                prepStmt.setString(2, labelID);
                prepStmt.execute();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("Failed to rollback the add API Label Mapping: API ID: "
                        + apiID + " Label IDs: " + labelIDs, ex);
            }
            handleException("Error while adding mapping between API ID: " + apiID + " and Label IDs: " + labelIDs, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Remove a label mapping from an API
     *
     * @param apiID   API Id
     * @param labelIDs label UUIDs
     * @throws APIManagementException if failed to remove mapping
     */
    public void deleteApiLabelMappings(String apiID, List<String> labelIDs) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_API_LABEL_MAPPING_SQL;

        try {
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);

            for (String labelID : labelIDs) {
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, apiID);
                prepStmt.setString(2, labelID);
                prepStmt.execute();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                log.error("Failed to rollback the delete API Label Mapping: API ID: "
                        + apiID + " Label IDs: " + labelIDs, ex);
            }
            handleException("Error while deleting mapping between API ID: "
                    + apiID + " and Label IDs: " + labelIDs, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
    }

    /**
     * Get all mapped label IDs of the given API
     *
     * @param apiID API Id
     * @return List<String> list of label UUIDs
     * @throws APIManagementException if failed to get mappings
     */
    public List<String> getMappedLabelIDsForApi(String apiID) throws APIManagementException {

        List<String> mappedLabelIDs = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_MAPPED_LABEL_IDS_BY_API_ID_SQL)) {
            statement.setString(1, apiID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    mappedLabelIDs.add(rs.getString("LABEL_UUID"));
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting mapped label IDs of the API ID: " + apiID, e);
        }
        return mappedLabelIDs;
    }

    /**
     * Get all mapped APIs of the given Label
     *
     * @param labelID label UUID
     * @return List<ApiResult> list of ApiResult
     * @throws APIManagementException if failed to get mappings
     */
    public List<ApiResult> getMappedApisForLabel(String labelID) throws APIManagementException {

        List<ApiResult> mappedApis = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_MAPPED_APIS_BY_LABEL_UUID_SQL)) {
            statement.setString(1, labelID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ApiResult apiResult = new ApiResult();
                    apiResult.setId(rs.getString("API_UUID"));
                    apiResult.setName(rs.getString("API_NAME"));
                    apiResult.setVersion(rs.getString("API_VERSION"));
                    apiResult.setProvider(rs.getString("API_PROVIDER"));
                    apiResult.setType(rs.getString("API_TYPE"));
                    mappedApis.add(apiResult);
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting mapped APIs of the label ID: " + labelID, e);
        }
        return mappedApis;
    }

    /**
     * Get all mapped Labels of the given API
     *
     * @param apiID API UUID
     * @return List<Label> list of labels
     * @throws APIManagementException if failed to get mappings
     */
    public List<Label> getMappedLabelsForApi(String apiID) throws APIManagementException {
        List<Label> mappedLabels = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_MAPPED_LABELS_BY_API_UUID_SQL)) {
            statement.setString(1, apiID);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Label label = new Label();
                    label.setLabelId(rs.getString("UUID"));
                    label.setName(rs.getString("NAME"));
                    label.setDescription(rs.getString("DESCRIPTION"));
                    mappedLabels.add(label);
                }
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting mapped Labels of the API ID: " + apiID, e);
        }
        return mappedLabels;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
