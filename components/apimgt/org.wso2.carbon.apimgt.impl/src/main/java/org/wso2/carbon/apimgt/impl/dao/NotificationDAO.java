/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Pagination;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private static final Log log = LogFactory.getLog(NotificationDAO.class);
    private static final NotificationDAO INSTANCE = new NotificationDAO();

    private NotificationDAO() {
    }

    public static NotificationDAO getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves a specific notification by its UUID for a given user.
     *
     * @param notificationUUID the unique identifier of the notification to be retrieved
     * @param username the username of the user to retrieve the notification for
     * @param organization the organization to which the user belongs
     * @param portalToDisplay the portal where the notification is displayed
     * @return the notification matching the given UUID and user, or {@code null} if not found
     * @throws APIManagementException if there is an error while retrieving the notification
     */
    public Notification getNotification(String notificationUUID, String username, String organization,
                                            String portalToDisplay) throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.GET_NOTIFICATION_BY_ID;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setString(1, notificationUUID);
            ps.setString(2, username);
            ps.setString(3, organization);
            ps.setString(4, portalToDisplay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Notification notification = new Notification();
                    notification.setNotificationId(rs.getString("NOTIFICATION_ID"));
                    notification.setNotificationType(rs.getString("NOTIFICATION_TYPE"));
                    notification.setCreatedTime(rs.getTimestamp("CREATED_TIME").toString());
                    notification.setComments(rs.getString("CONTENT"));
                    notification.setIsRead(rs.getBoolean("IS_READ"));
                    return notification;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to get notification by id", e);
        }
        return null;
    }

    /**
     * Changes the status of a notification for a given user.
     *
     * @param username the username of the user whose notification status is to be changed
     * @param organization the organization to which the user belongs
     * @param notificationUUID the unique identifier of the notification
     * @param isMarkAsRead boolean flag indicating whether to mark the notification as read or unread
     * @param portalToDisplay the portal where the notification is displayed
     * @throws APIManagementException if there is an error while changing the notification status
     */
    public void changeNotificationStatus(String username, String organization, String notificationUUID,
                                         boolean isMarkAsRead, String portalToDisplay) throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.CHANGE_NOTIFICATION_STATUS;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setBoolean(1, isMarkAsRead);
                ps.setString(2, notificationUUID);
                ps.setString(3, username);
                ps.setString(4, organization);
                ps.setString(5, portalToDisplay);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback notification status change", e1);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to change notification status", e);
        }
    }

    /**
     * Deletes a notification for a given user.
     *
     * @param username the username of the user whose notification is to be deleted
     * @param organization the organization to which the user belongs
     * @param notificationUUID the unique identifier of the notification to be deleted
     * @param portalToDisplay the portal where the notification is displayed
     * @throws APIManagementException if there is an error while deleting the notification
     */
    public void deleteNotification(String username, String organization, String notificationUUID, String portalToDisplay)
            throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(SQLConstants.PortalNotifications.
                    DELETE_NOTIFICATION_BY_ID_FROM_AM_NOTIFICATION_END_USERS)) {
                ps.setString(1, notificationUUID);
                ps.setString(2, username);
                ps.setString(3, organization);
                ps.setString(4, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    try (PreparedStatement preparedStatement = conn.prepareStatement(SQLConstants.PortalNotifications.
                            DELETE_NOTIFICATION_BY_ID_FROM_AM_NOTIFICATION)) {
                        preparedStatement.setString(1, notificationUUID);
                        preparedStatement.setString(2, notificationUUID);
                        preparedStatement.executeUpdate();
                    }
                    conn.commit();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw new APIManagementException("Failed to delete notification by id", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to establish database connection", e);
        }
    }

    /**
     * Retrieves a list of notifications for a given user.
     *
     * @param username the username of the user to retrieve notifications for
     * @param organization the organization to which the user belongs
     * @param portalToDisplay the portal where the notifications are to be displayed
     * @param limit the maximum number of notifications to retrieve
     * @param offset the starting point in the list of notifications to retrieve
     * @return a list of notifications matching the criteria, including pagination information
     * @throws APIManagementException if there is an error while retrieving notifications
     */
    public NotificationList getNotifications(String username, String organization, String portalToDisplay, int limit,
                                             int offset) throws APIManagementException {

        NotificationList notificationList = new NotificationList();
        Pagination pagination = new Pagination();
        pagination.setLimit(limit);
        pagination.setOffset(offset);
        notificationList.setPagination(pagination);
        int totalNotificationCount = 0;
        int unreadNotificationCount = 0;
        int notificationCount = 0;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SQLConstants.PortalNotifications.
                    GET_TOTAL_NOTIFICATIONS_COUNT_FOR_USER)) {
                ps.setString(1, username);
                ps.setString(2, organization);
                ps.setString(3, portalToDisplay);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalNotificationCount = rs.getInt("NOTIFICATION_COUNT");
                        pagination.setTotal(totalNotificationCount);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Failed to retrieve notification count of the user " + username, e);
            }

            if (totalNotificationCount > 0) {
                try (PreparedStatement ps = conn.prepareStatement(SQLConstants.PortalNotifications.
                        GET_UNREAD_NOTIFICATION_COUNT_FOR_USER)) {
                    ps.setString(1, username);
                    ps.setString(2, organization);
                    ps.setString(3, portalToDisplay);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            unreadNotificationCount = rs.getInt("UNREAD_NOTIFICATION_COUNT");
                        }
                    }
                } catch (SQLException e) {
                    throw new APIManagementException("Failed to retrieve unread notification count of the user " + username,
                            e);
                }

                if (limit > 0) {
                    try (PreparedStatement ps =
                                 conn.prepareStatement(SQLConstants.PortalNotifications.GET_NOTIFICATIONS)) {
                        ps.setString(1, username);
                        ps.setString(2, organization);
                        ps.setString(3, portalToDisplay);
                        ps.setInt(4, offset);
                        ps.setInt(5, limit);
                        try (ResultSet rs = ps.executeQuery()) {
                            List<Notification> list = new ArrayList<>();
                            while (rs.next()) {
                                Notification notification = new Notification();
                                notification.setNotificationId(rs.getString("NOTIFICATION_ID"));
                                notification.setNotificationType(rs.getString("NOTIFICATION_TYPE"));
                                notification.setComments(rs.getString("CONTENT"));
                                notification.setCreatedTime(rs.getTimestamp("CREATED_TIME").toString());
                                notification.setIsRead(rs.getBoolean("IS_READ"));
                                list.add(notification);
                            }
                            notificationList.setList(list);
                            notificationCount = list.size();
                        } catch (SQLException e) {
                            throw new APIManagementException("Failed to retrieve notifications of the user " + username, e);
                        }
                    }
                }
            }
            notificationList.setUnreadCount(unreadNotificationCount);
            notificationList.setCount(notificationCount);
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve notifications of the user " + username, e);
        }
        return notificationList;
    }

    /**
     * Marks all notifications as read for a given user.
     *
     * @param username the username of the user whose notifications are to be marked as read
     * @param organization the organization to which the user belongs
     * @param portalToDisplay the portal where the notifications are displayed
     * @throws APIManagementException if there is an error while marking the notifications as read
     */
    public void markAllNotificationsAsRead(String username, String organization, String portalToDisplay)
            throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.MARK_ALL_NOTIFICATIONS_AS_READ;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, username);
                ps.setString(2, organization);
                ps.setString(3, portalToDisplay);
                ps.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback notification status change", e1);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to change notification status", e);
        }
    }

    /**
     * Deletes all notifications for a given user.
     *
     * @param username the username of the user whose notifications are to be deleted
     * @param organization the organization to which the user belongs
     * @param portalToDisplay the portal where the notifications are displayed
     * @throws APIManagementException if there is an error while deleting the notifications
     */
    public void deleteAllNotifications(String username, String organization, String portalToDisplay) throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.DELETE_ALL_NOTIFICATIONS_OF_USER_FROM_AM_NOTIFICATION_END_USERS;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, username);
                ps.setString(2, organization);
                ps.setString(3, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    String deleteNotifications =
                            SQLConstants.PortalNotifications.DELETE_ALL_UNUSED_NOTIFICATIONS_FROM_AM_NOTIFICATION;
                    try (PreparedStatement preparedStatement = conn.prepareStatement(deleteNotifications)) {
                        preparedStatement.executeUpdate();
                    }
                    conn.commit();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw new APIManagementException("Failed to delete notifications", e);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to establish database connection", e);
        }
    }
}
