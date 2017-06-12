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

import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Default implementation of the ApplicationDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApplicationDAOImpl implements ApplicationDAO {

    private static final String GET_APPS_QUERY = "SELECT NAME, APPLICATION_POLICY_ID, DESCRIPTION, " +
            "APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME, UUID " +
            "FROM AM_APPLICATION";
    private static final String AM_APPLICATION_TABLE_NAME = "AM_APPLICATION";

    ApplicationDAOImpl() {
    }

    /**
     * Retrieve a given instance of an Application
     *
     * @param appId The UUID that uniquely identifies an Application
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException   If failed to retrieve application.
     */
    @Override
    public Application getApplication(String appId) throws APIMgtDAOException {
        final String completeGetAppQuery = GET_APPS_QUERY + " WHERE UUID = ?";
        Application application;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(completeGetAppQuery)) {
            ps.setString(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                application = this.createApplicationFromResultSet(rs);
                setApplicationKeys(conn, application, appId);
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
        return application;
    }

    /**
     * Retrieve a given instance of an Application
     *
     * @param appName Name of the Application
     * @param ownerId ID of the application owner.
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException   If failed to retrieve application.
     */
    @Override
    public Application getApplicationByName(String appName, String ownerId) throws APIMgtDAOException {
        final String completeGetAppQuery = GET_APPS_QUERY + " WHERE NAME = ? AND CREATED_BY = ?";
        Application application;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(completeGetAppQuery)) {
            ps.setString(1, appName);
            ps.setString(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                application = this.createApplicationFromResultSet(rs);
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
        return application;
    }

    /**
     * Retrieves all available Applications that belong to a user.
     *
     * @param ownerId Username of user
     * @return A list of {@link Application}
     * @throws APIMgtDAOException   If failed to retrieve applications.
     */
    @Override
    public List<Application> getApplications(String ownerId) throws APIMgtDAOException {
        final String completeGetAppsQuery = GET_APPS_QUERY + " WHERE CREATED_BY = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(completeGetAppsQuery)) {
            ps.setString(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                return this.createApplicationsFromResultSet(rs);
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified username
     *
     * @param offset   The number of results from the beginning that is to be ignored
     * @param limit    The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@code Application[]} matching results
     * @throws APIMgtDAOException   If failed to retrieve applications.
     */
    @Override
    public Application[] getApplicationsForUser(int offset, int limit, String userName) throws APIMgtDAOException {
        return new Application[0];
    }

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@code Application[]} matching results
     * @throws APIMgtDAOException   If failed to retrieve applications.
     */
    @Override
    public Application[] getApplicationsForGroup(int offset, int limit, String groupID) throws APIMgtDAOException {
        return new Application[0];
    }

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified username
     *
     * @param searchString The search string provided
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException   If failed to retrieve applications.
     */
    @Override
    public Application[] searchApplicationsForUser(String searchString, String userId)
            throws APIMgtDAOException {
        //TODO
        return new Application[0];
    }

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified Group ID
     *
     * @param searchString The search string provided
     * @param groupID      The Group ID to filter results by
     * @return An array of matching {@link Application} objects
     * @throws APIMgtDAOException   If failed to retrieve applications.
     */
    @Override
    public Application[] searchApplicationsForGroup(String searchString, String groupID) throws APIMgtDAOException {
        //TODO
        return new Application[0];
    }

    /**
     * Add a new instance of an Application
     *
     * @param application The {@link Application} object to be added
     * @throws APIMgtDAOException   If failed to add application.
     */
    @Override
    public void addApplication(Application application) throws APIMgtDAOException {
        final String addAppQuery = "INSERT INTO AM_APPLICATION (UUID, NAME, APPLICATION_POLICY_ID, " +
                "DESCRIPTION, APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
                "LAST_UPDATED_TIME) VALUES (?, ?, (SELECT UUID FROM AM_APPLICATION_POLICY " +
                "WHERE NAME = ?),?,?,?,?,?,?,?)";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(addAppQuery)) {
                ps.setString(1, application.getId());
                ps.setString(2, application.getName());
                ps.setString(3, application.getTier());
                ps.setString(4, application.getDescription());

                if (APIMgtConstants.DEFAULT_APPLICATION_NAME.equals(application.getName())) {
                    ps.setString(5, APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
                } else {
                    ps.setString(5, APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
                }

                ps.setString(6, application.getGroupId());
                ps.setString(7, application.getCreatedUser());
                ps.setTimestamp(8, Timestamp.valueOf(application.getCreatedTime()));
                ps.setString(9, application.getCreatedUser());
                ps.setTimestamp(10, Timestamp.valueOf(application.getCreatedTime()));
                ps.executeUpdate();
                addApplicationPermission(conn, application.getPermissionMap(), application.getId());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * This method will save permission in to AM_APPS_GROUP_PERMISSION table.
     *
     * @param connection Database connection
     * @param permissionMap Permission Data
     * @param applicationId Application Id
     * @throws SQLException If failed to add application.
     */
    private void addApplicationPermission(Connection connection, HashMap permissionMap, String applicationId)
            throws SQLException {
        final String query = "INSERT INTO AM_APPS_GROUP_PERMISSION (APPLICATION_ID, GROUP_ID, PERMISSION) " +
                "VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, applicationId);
                        statement.setString(2, entry.getKey());
                        //if permission value is UPDATE or DELETE we by default give them read permission also.
                        if (entry.getValue() < APIMgtConstants.Permission.READ_PERMISSION && entry.getValue() != 0) {
                            statement.setInt(3, entry.getValue() + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, entry.getValue());
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, applicationId);
                statement.setString(2, APIMgtConstants.Permission.EVERYONE_GROUP);
                statement.setInt(3, 7);
                statement.execute();
            }
        }

    }

    private void updateApplicationPermission(Connection connection, HashMap permissionMap, String applicationId)
            throws SQLException {
        final String query = "INSERT INTO AM_APPS_GROUP_PERMISSION (APPLICATION_ID, GROUP_ID, PERMISSION) " +
                "VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, applicationId);
                        statement.setString(2, entry.getKey());
                        //if permission value is UPDATE or DELETE we by default give them read permission also.
                        if (entry.getValue() < APIMgtConstants.Permission.READ_PERMISSION && entry.getValue() != 0) {
                            statement.setInt(3, entry.getValue() + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, entry.getValue());
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        }
    }

    /**
     * Update an existing Application
     *
     * @param appID      The UUID of the Application that needs to be updated
     * @param updatedApp Substitute {@link Application} object that will replace the existing Application
     * @throws APIMgtDAOException   If failed to update applications.
     */
    @Override
    public void updateApplication(String appID, Application updatedApp)
            throws APIMgtDAOException {
        final String updateAppQuery = "UPDATE AM_APPLICATION SET NAME=?, APPLICATION_POLICY_ID=" +
                "(SELECT UUID FROM AM_APPLICATION_POLICY WHERE NAME=?), " +
                "DESCRIPTION=?, APPLICATION_STATUS=?, GROUP_ID=?, UPDATED_BY=?, " +
                "LAST_UPDATED_TIME=? WHERE UUID=?";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(updateAppQuery)) {
                ps.setString(1, updatedApp.getName());
                ps.setString(2, updatedApp.getTier());
                ps.setString(3, updatedApp.getDescription());
                ps.setString(4, updatedApp.getStatus());
                ps.setString(5, updatedApp.getGroupId());
                ps.setString(6, updatedApp.getUpdatedUser());
                ps.setTimestamp(7, Timestamp.valueOf(updatedApp.getUpdatedTime()));
                ps.setString(8, appID);
                ps.executeUpdate();
                updateApplicationPermission(conn, updatedApp.getPermissionMap(), updatedApp.getId());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Remove an existing Application
     *
     * @param appID The UUID of the Application that needs to be deleted
     * @throws APIMgtDAOException   If failed to delete application.
     */
    @Override
    public void deleteApplication(String appID) throws APIMgtDAOException {
        final String appDeleteQuery = "DELETE FROM AM_APPLICATION WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection()) {
            boolean originalAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(appDeleteQuery)) {
                ps.setString(1, appID);
                ps.execute();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(originalAutoCommitState);
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Check whether given application name is already available in the system
     *
     * @param appName application name
     * @return true if application name is already available
     * @throws APIMgtDAOException if failed to get applications for given subscriber
     */
    @Override
    public boolean isApplicationNameExists(String appName) throws APIMgtDAOException {
        final String query = "SELECT UUID FROM AM_APPLICATION WHERE NAME = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, appName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
        return false;
    }

    @Override
    public void addApplicationKeys(String appId, OAuthApplicationInfo oAuthAppDetails)
            throws APIMgtDAOException {
        final String addApplicationKeysQuery = "INSERT INTO AM_APP_KEY_MAPPING (APPLICATION_ID, CLIENT_ID, KEY_TYPE,"
                + "STATE, CREATE_MODE) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(addApplicationKeysQuery)) {
                ps.setString(1, appId);
                ps.setString(2, oAuthAppDetails.getClientId());
                ps.setString(3, oAuthAppDetails.getParameter(KeyManagerConstants.APP_KEY_TYPE).toString());
                ps.setString(4, "COMPLETED"); //temporary fix
                ps.setString(5, "CREATED"); //temporary fix
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * @see ApplicationDAO#getLastUpdatedTimeOfApplication(String)
     */
    @Override
    public String getLastUpdatedTimeOfApplication(String applicationId) throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_APPLICATION_TABLE_NAME, applicationId);
    }

    private void setApplicationKeys(Connection conn, Application application, String applicationId)
            throws APIMgtDAOException {
        final String getApplicationKeysQuery =
                "SELECT CLIENT_ID, KEY_TYPE, STATE FROM AM_APP_KEY_MAPPING WHERE " + "APPLICATION_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(getApplicationKeysQuery)) {
            ps.setString(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    APIKey apiKey = new APIKey();
                    apiKey.setConsumerKey(rs.getString("CLIENT_ID"));
                    apiKey.setType(rs.getString("KEY_TYPE"));
                    apiKey.setState(rs.getString("STATE"));
                    application.addKey(apiKey);
                }
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    private List<Application> createApplicationsFromResultSet(ResultSet rs) throws SQLException, APIMgtDAOException {
        List<Application> appList = new ArrayList<>();
        Application application;
        while ((application = createApplicationFromResultSet(rs)) != null) {
            appList.add(application);
        }
        return appList;
    }

    private Application createApplicationFromResultSet(ResultSet rs) throws APIMgtDAOException, SQLException {
        Application application = null;
        if (rs.next()) {
            String createdUser = rs.getString("CREATED_BY");
            application = new Application(rs.getString("NAME"), createdUser);
            application.setId(rs.getString("UUID"));
            application.setDescription(rs.getString("DESCRIPTION"));
            application.setGroupId(rs.getString("GROUP_ID"));
            application.setStatus(rs.getString("APPLICATION_STATUS"));
            application.setCreatedTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime());
            application.setUpdatedUser(rs.getString("UPDATED_BY"));
            application.setUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime());
            application.setTier(DAOFactory.getPolicyDAO().getPolicyByUuid("application",
                    rs.getString("APPLICATION_POLICY_ID")).getPolicyName());
        }
        return application;
    }

    @Override
    public void updateApplicationState(String appID, String state) throws APIMgtDAOException {
        final String updateAppQuery = "UPDATE AM_APPLICATION SET APPLICATION_STATUS=?, LAST_UPDATED_TIME=?  "
                + "WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(updateAppQuery)) {
                ps.setString(1, state);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, appID);               
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                conn.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
        
    }
}
