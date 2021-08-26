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
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

public class OrganizationPurgeDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);
    private static OrganizationPurgeDAO INSTANCE = null;
    private boolean multiGroupAppSharingEnabled = false;

    private OrganizationPurgeDAO() {
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

    /**
     * Remove pending subscriptions for a given application id list
     *
     * @param organization Organization
     * @throws APIManagementException if failed to remove pending subscriptions
     */
    public void removePendingSubscriptions(String organization) throws APIManagementException {

        String query = OrganizationPurgeSQLConstants.DELETE_PENDING_SUBSCRIPTIONS;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, organization);

                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback remove pending subscriptions for organization: " + organization, ex);
                }
                handleException("Error occurred while removing pending subscriptions for organization: " + organization,
                        e);
            }
        } catch (SQLException e) {
            handleException("Error occurred while removing pending subscriptions for organization: " + organization, e);
        }
    }

    /**
     * Remove application creation workflows
     *
     * @param organization Organization
     * @throws APIManagementException if failed to remove application creation workflows
     */
    public void removeApplicationCreationWorkflows(String organization) throws APIManagementException {

        String query = OrganizationPurgeSQLConstants.DELETE_APPLICATION_CREATION_WORKFLOWS;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, organization);

                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback remove pending application creation workflows for organization: "
                            + organization, ex);
                }
                handleException("Error occurred while removing application creation workflows for organization: "
                        + organization, e);
            }
        } catch (SQLException e) {
            handleException("Error occurred while removing application creation workflows for organization: " +
                            organization, e);
        }
    }

    /**
     * Remove pending Application Registrations
     *
     * @param organization Organization
     * @throws APIManagementException when failed to delete pending application registrations
     */
    public void deletePendingApplicationRegistrations(String organization) throws APIManagementException {
        String query = OrganizationPurgeSQLConstants.REMOVE_PENDING_APPLICATION_REGISTRATIONS;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, organization);
                preparedStatement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback remove pending application registrations for organization: "
                            + organization, ex);
                }
                handleException(
                        "Error while deleting pending application registrations for organization: " + organization, e);
            }
        } catch (SQLException e) {
            handleException("Error while deleting pending application registrations for organization: " + organization,
                    e);
        }
    }

    /**
     * Deletes Applications along with subscriptions, keys and registration data
     *
     * @param organization Organization
     * @throws APIManagementException if failed to delete applications for organization
     */
    public void deleteApplicationList(String organization) throws APIManagementException {

        String getConsumerKeyQuery = OrganizationPurgeSQLConstants.GET_CONSUMER_KEYS_OF_APPLICATION_LIST_SQL;

        String deleteDomainAppQuery = SQLConstants.REMOVE_APPLICATION_FROM_DOMAIN_MAPPINGS_SQL;

        String deleteApplicationQuery = OrganizationPurgeSQLConstants.REMOVE_APPLICATION_LIST_FROM_APPLICATIONS_SQL;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);

            if (multiGroupAppSharingEnabled) {
                updateGroupIDMappingsBulk(connection, organization);
            }

            try (PreparedStatement prepStmtGetConsumerKey = connection.prepareStatement(getConsumerKeyQuery);
                    PreparedStatement deleteDomainApp = connection.prepareStatement(deleteDomainAppQuery)) {
                prepStmtGetConsumerKey.setString(1, organization);

                try (ResultSet rs = prepStmtGetConsumerKey.executeQuery()) {
                    while (rs.next()) {
                        String consumerKey = rs.getString(APIConstants.FIELD_CONSUMER_KEY);
                        String keyManagerName = rs.getString("NAME");
                        String keyManagerOrganization = rs.getString("ORGANIZATION");

                        // This is true when OAuth App has been created by pasting consumer key/secret in the screen.
                        String mode = rs.getString("CREATE_MODE");
                        if (consumerKey != null) {

                            deleteDomainApp.setString(1, consumerKey);
                            deleteDomainApp.addBatch();
                            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(keyManagerOrganization,
                                    keyManagerName);

                            if (keyManager != null) {
                                try {
                                    keyManager.deleteMappedApplication(consumerKey);
                                    log.info("Mapped application deleted for consumer key: " + consumerKey
                                            + " and organization: " + organization);
                                } catch (APIManagementException e) {
                                    handleException(
                                            "Error while Deleting Client Application for consumer key: " + consumerKey
                                                    + " and organization: " + organization, e);
                                }
                            }

                            // OAuth app is deleted if only it has been created from API Store. For mapped clients we don't
                            // call delete.
                            if (!APIConstants.OAuthAppMode.MAPPED.name().equals(mode)) {
                                //delete on oAuthorization server.
                                if (log.isDebugEnabled()) {
                                    log.debug(
                                            "Deleting Oauth application with consumer key " + consumerKey + " from the "
                                                    + "Oauth server for organization: " + organization);
                                }
                                if (keyManager != null) {
                                    try {
                                        keyManager.deleteApplication(consumerKey);
                                        log.info("Client application deleted for consumer key: " + consumerKey
                                                + " and organization: " + organization);
                                    } catch (APIManagementException e) {
                                        handleException("Error while Deleting Client Application for organization: "
                                                + organization, e);
                                    }

                                }
                            }
                        }
                    }
                }
                deleteDomainApp.executeBatch();
            } catch (SQLException domainAppsException) {
                connection.rollback();
                log.error("Failed to rollback removing domain applications for organization: " + organization,
                        domainAppsException);
            }

            if (log.isDebugEnabled()) {
                log.debug("Subscription Key mapping details are deleted successfully for Applications for "
                        + "organization: " + organization);
            }

            try (PreparedStatement deleteApp = connection.prepareStatement(deleteApplicationQuery)) {
                deleteApp.setString(1, organization);
                deleteApp.execute();
            } catch (SQLException appDeletionException) {
                connection.rollback();
                log.error("Failed to rollback removing applications for organization: " + organization,
                        appDeletionException);
            }

            if (log.isDebugEnabled()) {
                log.debug("Applications are deleted successfully for organization: " + organization);
            }

            connection.commit();

        } catch (SQLException e) {
            handleException(
                    "Error while removing application details from the database for organization: " + organization, e);
        }
    }

    /**
     * Purge records in Application Group Mappings
     *
     * @param conn         Connection
     * @param organization Organization
     * @return
     * @throws APIManagementException when failed to execute the application groups update
     */
    private void updateGroupIDMappingsBulk(Connection conn, String organization) throws APIManagementException {

        String deleteQuery = OrganizationPurgeSQLConstants.REMOVE_GROUP_ID_MAPPING_BULK_SQL;
        String removeGroupIdsQuery = OrganizationPurgeSQLConstants.REMOVE_MIGRATED_GROUP_ID_SQL_BULK;

        try (PreparedStatement removeMigratedGroupIdsStatement = conn.prepareStatement(removeGroupIdsQuery);
                PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery)) {

            removeMigratedGroupIdsStatement.setString(1, organization);
            removeMigratedGroupIdsStatement.executeUpdate();

            deleteStatement.setString(1, organization);
            deleteStatement.executeUpdate();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                log.error("Failed to rollback update application group id mappings bulk for organization: "
                        + organization, ex);
            }
            handleException("Failed to update bulk groupId mappings for organization: " + organization, e);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
