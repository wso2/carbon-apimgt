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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationSummaryResults;
import org.wso2.carbon.apimgt.core.util.APIConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Default implementation of the ApplicationDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class ApplicationDAOImpl implements ApplicationDAO {

    ApplicationDAOImpl() {}

    /**
     * Retrieve a given instance of an Application
     *
     * @param appID The UUID that uniquely identifies an Application
     * @return valid {@link Application} object or null
     * @throws SQLException
     */
    @Override
    public Application getApplication(String appID) throws SQLException {
        final String getAppQuery = "SELECT NAME, APPLICATION_POLICY_ID, CALLBACK_URL, " +
        "DESCRIPTION, APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
        "LAST_UPDATED_TIME, UUID FROM AM_APPLICATION";
        Application application = null;
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(getAppQuery)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String createdUser = rs.getString("CREATED_BY");
                    application = new Application(rs.getString("NAME"), createdUser);
                    application.setUUID(rs.getString("UUID"));
                    application.setCallbackUrl(rs.getString("CALLBACK_URL"));
                    application.setDescription(rs.getString("DESCRIPTION"));
                    application.setGroupId(rs.getString("GROUP_ID"));
                    application.setStatus(rs.getString("APPLICATION_STATUS"));
                    application.setCreatedTime(rs.getTimestamp("CREATED_TIME").toLocalDateTime());
                    application.setUpdatedUser(rs.getString("UPDATED_BY"));
                    application.setUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME").toLocalDateTime());
                    application.setTier(getSubscriptionTierName(conn, rs.getInt("APPLICATION_POLICY_ID")));
                }
            }
        }
        return application;
    }

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified username
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws SQLException
     *
     */
    @Override
    public ApplicationSummaryResults getApplicationsForUser(int offset, int limit, String userName)
                                                                            throws SQLException {
        return null;
    }

    /**
     * Retrieves summary data of all available Applications. This method supports result pagination and
     * ensures results returned are those that belong to the specified Group ID
     *
     * @param offset  The number of results from the beginning that is to be ignored
     * @param limit   The maximum number of results to be returned after the offset
     * @param groupID The Group ID to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws SQLException
     */
    @Override
    public ApplicationSummaryResults getApplicationsForGroup(int offset, int limit, String groupID)
                                                                            throws SQLException {
        return null;
    }

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified username
     * @param searchAttribute The attribute of an Application against which the search will be performed
     * @param searchString The search string provided
     * @param offset The number of results from the beginning that is to be ignored
     * @param limit The maximum number of results to be returned after the offset
     * @param userName The username to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws SQLException
     *
     */
    @Override
    public ApplicationSummaryResults searchApplicationsForUser(String searchAttribute, String searchString, int offset,
                                                         int limit, String userName) throws SQLException {
        return null;
    }

    /**
     * Retrieves summary data of all available Applications that match the given search criteria. This method supports
     * result pagination and ensuring results returned are for Apps belonging to the specified Group ID
     *
     * @param searchAttribute The attribute of an Application against which the search will be performed
     * @param searchString    The search string provided
     * @param offset          The number of results from the beginning that is to be ignored
     * @param limit           The maximum number of results to be returned after the offset
     * @param groupID         The Group ID to filter results by
     * @return {@link ApplicationSummaryResults} matching results
     * @throws SQLException
     */
    @Override
    public ApplicationSummaryResults searchApplicationsForGroup(String searchAttribute, String searchString,
                                              int offset, int limit, String groupID) throws SQLException {
        return null;
    }

    /**
     * Add a new instance of an Application
     *
     * @param application The {@link Application} object to be added
     * @return UUID of created {@link Application}
     * @throws SQLException
     */
    @Override
    public void addApplication(Application application) throws SQLException {
        final String addAppQuery = "INSERT INTO AM_APPLICATION (NAME, APPLICATION_POLICY_ID, CALLBACK_URL, " +
                             "DESCRIPTION, APPLICATION_STATUS, GROUP_ID, CREATED_BY, CREATED_TIME, UPDATED_BY, " +
                             "LAST_UPDATED_TIME, UUID) VALUES (?, (SELECT POLICY_ID FROM AM_POLICY_APPLICATION " +
                             "WHERE NAME = ?),?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(addAppQuery)) {
            conn.setAutoCommit(false);
            ps.setString(1, application.getName());
            ps.setString(2, application.getTier());
            ps.setString(3, application.getCallbackUrl());
            ps.setString(4, application.getDescription());

            if (APIConstants.DEFAULT_APPLICATION_NAME.equals(application.getName())) {
                ps.setString(5, APIConstants.ApplicationStatus.APPLICATION_APPROVED);
            } else {
                ps.setString(5, APIConstants.ApplicationStatus.APPLICATION_CREATED);
            }

            ps.setString(6, application.getGroupId());
            ps.setString(7, application.getCreatedUser());
            ps.setTimestamp(8, Timestamp.valueOf(application.getCreatedTime()));
            ps.setString(9, application.getCreatedUser());
            ps.setTimestamp(10, Timestamp.valueOf(application.getCreatedTime()));
            ps.setString(11, application.getUUID());
            ps.executeUpdate();
            conn.commit();
        }
    }

    /**
     * Update an existing Application
     *
     * @param appID                 The UUID of the Application that needs to be updated
     * @param updatedApp Substitute {@link Application} object that will replace the existing Application
     * @throws SQLException
     */
    @Override
    public void updateApplication(String appID, Application updatedApp)
                                                                        throws SQLException {
        final String updateAppQuery = "UPDATE AM_APPLICATION SET NAME=?, APPLICATION_POLICY_ID=" +
                                "(SELECT POLICY_ID FROM AM_POLICY_APPLICATION WHERE NAME=?), " +
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
        }
    }

    /**
     * Remove an existing Application
     *
     * @param appID The UUID of the Application that needs to be deleted
     * @throws SQLException
     */
    @Override
    public void deleteApplication(String appID) throws SQLException {
        final String query = "DELETE FROM AM_APPLICATION WHERE UUID = ?";
        try (Connection conn = DAOUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            conn.setAutoCommit(false);
            statement.setString(1, appID);
            statement.execute();
            conn.commit();
        }
    }

    /**
     * Fetches an Application by name.
     *
     * @param applicationName Name of the Application
     * @param userId          Name of the User.
     * @throws SQLException
     */
    @Override
    public Application getApplicationByName(String userId, String applicationName, String groupId)
            throws SQLException {
        return null;
    }

    /**
     * Retrieves the Application which is corresponding to the given UUID String
     *
     * @param uuid UUID of Application
     * @return
     * @throws SQLException
     */
    @Override
    public Application getApplicationByUUID(String uuid) throws SQLException {
        return null;
    }

    @Override
    public boolean isApplicationExists(String appName, String username, String groupId) throws SQLException {
        return false;
    }

    @Override
    public Application[] getApplications(String subscriber, String groupingId) throws SQLException {
        return new Application[0];
    }

    private String getSubscriptionTierName(Connection connection, int policyID) throws SQLException {
        final String query = "SELECT NAME FROM AM_POLICY_APPLICATION WHERE POLICY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, policyID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }
        return null;
    }
}
