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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the ApplicationDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApplicationDAOImpl implements ApplicationDAO {

    private static final String GET_APPS_QUERY = "SELECT NAME, APPLICATION_POLICY_ID, CALLBACK_URL, DESCRIPTION, " +
            "APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME, UUID " +
            "FROM AM_APPLICATION";

    ApplicationDAOImpl() {
    }

    /**
     * Retrieve a given instance of an Application
     *
     * @param appId The UUID that uniquely identifies an Application
     * @return valid {@link Application} object or null
     * @throws APIMgtDAOException
     */
    @Override
    public Application getApplication(String appId) throws APIMgtDAOException {
        final String completeGetAppQuery = GET_APPS_QUERY + " WHERE UUID = ? AND CREATED_BY = ?";
        Application application;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(completeGetAppQuery)) {
            ps.setString(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                application = this.createApplicationFromResultSet(rs);
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
     * @throws APIMgtDAOException
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
     * @throws APIMgtDAOException
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
     * @return {@link ApplicationSummaryResults} matching results
     * @throws APIMgtDAOException
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
     * @return {@link ApplicationSummaryResults} matching results
     * @throws APIMgtDAOException
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
     * @throws APIMgtDAOException
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
     * @throws APIMgtDAOException
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
     * @throws APIMgtDAOException
     */
    @Override
    public void addApplication(Application application) throws APIMgtDAOException {
        final String addAppQuery = "INSERT INTO AM_APPLICATION (UUID, NAME, APPLICATION_POLICY_ID, CALLBACK_URL, " +
                "DESCRIPTION, APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
                "LAST_UPDATED_TIME) VALUES (?, ?, (SELECT UUID FROM AM_APPLICATION_POLICY " +
                "WHERE NAME = ?),?,?,?,?,?,?,?,?)";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(addAppQuery)) {
            conn.setAutoCommit(false);
            ps.setString(1, application.getId());
            ps.setString(2, application.getName());
            ps.setString(3, application.getTier());
            ps.setString(4, application.getCallbackUrl());
            ps.setString(5, application.getDescription());

            if (APIMgtConstants.DEFAULT_APPLICATION_NAME.equals(application.getName())) {
                ps.setString(6, APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
            } else {
                ps.setString(6, APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
            }

            ps.setString(7, application.getGroupId());
            ps.setString(8, application.getCreatedUser());
            ps.setTimestamp(9, Timestamp.valueOf(application.getCreatedTime()));
            ps.setString(10, application.getCreatedUser());
            ps.setTimestamp(11, Timestamp.valueOf(application.getCreatedTime()));
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Update an existing Application
     *
     * @param appID      The UUID of the Application that needs to be updated
     * @param updatedApp Substitute {@link Application} object that will replace the existing Application
     * @throws APIMgtDAOException
     */
    @Override
    public void updateApplication(String appID, Application updatedApp)
            throws APIMgtDAOException {
        final String updateAppQuery = "UPDATE AM_APPLICATION SET NAME=?, APPLICATION_POLICY_ID=" +
                "(SELECT UUID FROM AM_APPLICATION_POLICY WHERE NAME=?), " +
                "CALLBACK_URL=?, DESCRIPTION=?, APPLICATION_STATUS=?, GROUP_ID=?, UPDATED_BY=?, " +
                "LAST_UPDATED_TIME=?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateAppQuery)) {
            conn.setAutoCommit(false);
            ps.setString(1, updatedApp.getName());
            ps.setString(2, updatedApp.getTier());
            ps.setString(3, updatedApp.getCallbackUrl());
            ps.setString(4, updatedApp.getDescription());
            ps.setString(5, updatedApp.getStatus());
            ps.setString(6, updatedApp.getGroupId());
            ps.setString(7, updatedApp.getUpdatedUser());
            ps.setTimestamp(8, Timestamp.valueOf(updatedApp.getUpdatedTime()));
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Remove an existing Application
     *
     * @param appID The UUID of the Application that needs to be deleted
     * @throws APIMgtDAOException
     */
    @Override
    public void deleteApplication(String appID) throws APIMgtDAOException {
        final String query = "DELETE FROM AM_APPLICATION WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            conn.setAutoCommit(false);
            statement.setString(1, appID);
            statement.execute();
            conn.commit();
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
            application.setUuid(rs.getString("UUID"));
            application.setCallbackUrl(rs.getString("CALLBACK_URL"));
            application.setDescription(rs.getString("DESCRIPTION"));
            application.setGroupId(rs.getString("GROUP_ID"));
            application.setStatus(rs.getString("APPLICATION_STATUS"));
            application.setCreatedTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime());
            application.setUpdatedUser(rs.getString("UPDATED_BY"));
            application.setUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime());
            application.setTier(DAOFactory.getPolicyDAO().getSubscriptionTierName(
                    rs.getString("APPLICATION_POLICY_ID")));
        }
        return application;
    }
}
