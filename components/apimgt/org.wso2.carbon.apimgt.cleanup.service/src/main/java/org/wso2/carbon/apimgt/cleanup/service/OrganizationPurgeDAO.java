/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cleanup.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrganizationPurgeDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
    private static OrganizationPurgeDAO INSTANCE = null;
    private boolean multiGroupAppSharingEnabled = false;

    public OrganizationPurgeDAO() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
    }

    /**
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link OrganizationPurgeDAO} instance
     */
    public static OrganizationPurgeDAO getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new OrganizationPurgeDAO();
        }

        return INSTANCE;
    }

    /**
     * Get API data for given organization
     *
     * @param orgId organization Id
     * @return ArrayList<APIIdentifier>
     * @throws APIManagementException
     */
    public ArrayList<APIIdentifier> getAPIIdList(String orgId) throws APIManagementException {

        ArrayList<APIIdentifier> apiList = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(OrganizationPurgeSQLConstants.GET_API_LIST_SQL_BY_ORG)) {
            ps.setString(1, orgId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int apiId = rs.getInt("API_ID");
                    String apiUuid = rs.getString("API_UUID");
                    String apiName = rs.getString("API_Name");
                    String apiProvider = rs.getString("API_Provider");
                    String apiVersion = rs.getString("API_Version");

                    APIIdentifier apiIdentifier = new APIIdentifier(apiProvider, apiName, apiVersion, apiUuid);
                    apiIdentifier.setId(apiId);
                    apiList.add(apiIdentifier);
                }
            }
        } catch (SQLException e) {
            log.error("Error while getting apiUuid list of organization" + orgId, e);
            handleException("Failed to get API apiUuid list of organization " + orgId, e);
        }
        return apiList;
    }

    /**
     * Delete all organization API data
     *
     * @param organization organization
     * @throws APIManagementException
     */
    public void deleteOrganizationAPIList(String organization) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            // Remove records from AM_API table and associated data through cascade delete
            String deleteAPIQuery = OrganizationPurgeSQLConstants.REMOVE_BULK_APIS_DATA_FROM_AM_API_SQL;
            deleteOrganizationAPIData(connection, deleteAPIQuery, organization);

            String deleteAPIDefaultVersionQuery = OrganizationPurgeSQLConstants.REMOVE_BULK_APIS_DEFAULT_VERSION_SQL;
            deleteAPIsFromDefaultVersion(connection, deleteAPIDefaultVersionQuery, organization);

            //Remove API Cleanup tasks
            String deleteCleanUpTasksQuery = OrganizationPurgeSQLConstants.DELETE_BULK_API_WORKFLOWS_REQUEST_SQL;
            deleteAPICleanupTasks(connection, deleteCleanUpTasksQuery, organization);

            connection.commit();
        } catch (SQLException e) {
            handleException("Error while removing the  API data of organization " + organization + " from the database",
                    e);
        }
    }

    private void deleteOrganizationAPIData(Connection conn, String deleteAPIQuery, String organization)
            throws APIManagementException {

        try (PreparedStatement prepStmt = conn.prepareStatement(deleteAPIQuery)) {
            prepStmt.setString(1, organization);
            prepStmt.execute();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error("Error while rolling back the failed operation", e1);
            }
            handleException("Failed to remove API data of organization " + organization + " from the database", e);
        }
    }

    private void deleteAPICleanupTasks(Connection conn, String deleteCleanUpTasksQuery, String organization)
            throws APIManagementException {

        try (PreparedStatement prepStmt = conn.prepareStatement(deleteCleanUpTasksQuery)) {
            prepStmt.setString(1, organization);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error("Error while rolling back the failed operation", e1);
            }
            handleException("Failed to remove API cleanup tasks of organization " + organization + " from the database",
                    e);
        }
    }

    private void deleteAPIsFromDefaultVersion(Connection conn, String deleteAPIDefaultVersionQuery, String organization)
            throws APIManagementException {

        try (PreparedStatement prepStmt = conn.prepareStatement(deleteAPIDefaultVersionQuery)) {
            prepStmt.setString(1, organization);
            prepStmt.execute();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.error("Error while rolling back the failed operation", e1);
            }
            handleException("Failed to remove API data of organization " + organization + " from the database", e);
        }
    }

    public void deleteKeyManagerConfigurationList(List<KeyManagerConfigurationDTO> kmList, String organization)
            throws APIManagementException {

        List<String> kmIdList = kmList.stream().map(KeyManagerConfigurationDTO::getUuid).collect(Collectors.toList());
        List<String> collectionList = Collections.nCopies(kmIdList.size(), "?");

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String deleteKMQuery = OrganizationPurgeSQLConstants.DELETE_BULK_KEY_MANAGER_LIST;
            deleteKMQuery = deleteKMQuery.replaceAll(OrganizationPurgeSQLConstants.KM_UUID_REGEX, String.join(",",
                    collectionList));
            try (PreparedStatement preparedStatement = conn.prepareStatement(deleteKMQuery)) {
                preparedStatement.setString(1, organization);
                int index = 1;
                for (String uuid : kmIdList) {
                    preparedStatement.setString(index + 1, uuid);
                    index++;
                }
                preparedStatement.execute();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while deleting key managers:  " + kmIdList + " in organization "
                    + organization,e);
        }

    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
