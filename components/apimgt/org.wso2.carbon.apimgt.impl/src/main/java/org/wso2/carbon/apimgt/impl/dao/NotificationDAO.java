/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Pagination;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationDTO;
import org.wso2.carbon.apimgt.impl.dto.systemNotifications.NotificationEndUserDTO;
import org.wso2.carbon.apimgt.impl.systemNotifications.NotificationMetaData;
import org.wso2.carbon.apimgt.impl.systemNotifications.NotificationType;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class NotificationDAO {

    private static final Log log = LogFactory.getLog(NotificationDAO.class);
    private static final NotificationDAO INSTANCE = new NotificationDAO();

    private NotificationDAO() {
    }

    public static NotificationDAO getInstance() {
        return INSTANCE;
    }

    public boolean addNotification(NotificationDTO notificationDTO) throws APIManagementException {

        String addNotificationQuery = SQLConstants.PortalNotifications.ADD_NOTIFICATION;
        String addEndUserQuery = SQLConstants.PortalNotifications.ADD_NOTIFICATION_END_USER;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(addNotificationQuery)) {

                String notificationId = UUID.randomUUID().toString();
                ps.setString(1, notificationId);
                ps.setString(2, notificationDTO.getNotificationType().toString());
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                String metadataJson = convertMetadataToJson(notificationDTO.getNotificationMetadata());
                ps.setString(4, metadataJson);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    for (NotificationEndUserDTO endUser : notificationDTO.getEndUsers()) {
                        addEndUser(conn, addEndUserQuery, notificationId, endUser);
                    }
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("Error while adding notification", e);
            }
        } catch (SQLException e) {
            handleException("Error while establishing database connection", e);
        }
        return false;
    }

    private void addEndUser(Connection conn, String addEndUserQuery, String notificationId,
            NotificationEndUserDTO endUser) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(addEndUserQuery)) {
            ps.setString(1, notificationId);
            ps.setString(2, endUser.getDestinationUser());
            ps.setString(3, endUser.getOrganization());
            ps.setString(4, endUser.getPortalToDisplay());
            ps.executeUpdate();
        }
    }

    private String convertMetadataToJson(NotificationMetaData metaData) {
        JSONObject json = new JSONObject();
        json.put(APIConstants.PortalNotifications.API_NAME, metaData.getApi());
        json.put(APIConstants.PortalNotifications.API_VERSION, metaData.getApiVersion());
        json.put(APIConstants.PortalNotifications.API_CONTEXT, metaData.getApiContext());
        json.put(APIConstants.PortalNotifications.ACTION, metaData.getAction());
        json.put(APIConstants.PortalNotifications.APPLICATION_NAME, metaData.getApplicationName());
        json.put(APIConstants.PortalNotifications.REQUESTED_TIER, metaData.getRequestedTier());
        json.put(APIConstants.PortalNotifications.REVISION_ID, metaData.getRevisionId());
        json.put(APIConstants.PortalNotifications.COMMENT, metaData.getComment());
        return json.toJSONString();
    }

    public NotificationList getNotifications(String username, String organization, String portalToDisplay,
            String sortOrder, Integer limit, Integer offset) throws APIManagementException {

        List<Notification> list = new ArrayList<Notification>();
        NotificationList notificationList = new NotificationList();
        Pagination pagination = new Pagination();
        notificationList.setPagination(pagination);
        int total = 0;
        int unreadCount = 0;

        String sqlQueryForCount = SQLConstants.PortalNotifications.GET_NOTIFICATIONS_COUNT;
        String sqlQuery;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement psForCount = conn.prepareStatement(sqlQueryForCount)) {
                psForCount.setString(1, username);
                psForCount.setString(2, organization);
                psForCount.setString(3, portalToDisplay);
                try (ResultSet rsForCount = psForCount.executeQuery()) {
                    while (rsForCount.next()) {
                        total = rsForCount.getInt("NOTIFICATION_COUNT");
                    }
                    if (total > 0 && limit > 0) {
                        if (sortOrder != null && sortOrder.equals("asc")) {
                            sqlQuery = SQLConstants.PortalNotifications.GET_NOTIFICATIONS_ASC;
                        } else {
                            sqlQuery = SQLConstants.PortalNotifications.GET_NOTIFICATIONS_DESC;
                        }
                        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                            ps.setString(1, username);
                            ps.setString(2, organization);
                            ps.setString(3, portalToDisplay);
                            ps.setInt(4, offset);
                            ps.setInt(5, limit);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    Notification notification = new Notification();
                                    notification.setNotificationId(rs.getString("NOTIFICATION_ID"));
                                    notification.setNotificationType(rs.getString("NOTIFICATION_TYPE"));
                                    notification.setCreatedTime(rs.getTimestamp("CREATED_TIME").toString());
                                    notification.setComments(
                                            getCommentFromMetaData(rs.getString("NOTIFICATION_METADATA"),
                                                    rs.getString("NOTIFICATION_TYPE")));
                                    notification.setIsRead(rs.getBoolean("IS_READ"));
                                    list.add(notification);
                                }
                            }
                        }
                    } else {
                        notificationList.getPagination().setTotal(total);
                        notificationList.setCount(total);
                        return notificationList;
                    }
                }
                unreadCount = getUnreadNotificationCount(username, organization, portalToDisplay, conn);
            } catch (SQLException e) {
                handleException("Failed to retrieve notifications of the user " + username, e);
            }
        } catch (SQLException e) {
            handleException("Failed to establish database connection", e);
        }

        pagination.setLimit(limit);
        pagination.setOffset(offset);
        notificationList.getPagination().setTotal(total);
        notificationList.setList(list);
        notificationList.setCount(list.size());
        notificationList.setUnreadCount(unreadCount);

        return notificationList;
    }

    private String getCommentFromMetaData(String notificationMetadata, String notificationType) {
        String finalComment = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(notificationMetadata);

            String comment = (String) json.get(APIConstants.PortalNotifications.COMMENT);
            String apiName = (String) json.get(APIConstants.PortalNotifications.API_NAME);
            String apiVersion = (String) json.get(APIConstants.PortalNotifications.API_VERSION);
            String apiContext = (String) json.get(APIConstants.PortalNotifications.API_CONTEXT);
            String action = (String) json.get(APIConstants.PortalNotifications.ACTION);
            String revisionId = (String) json.get(APIConstants.PortalNotifications.REVISION_ID);
            String applicationName = (String) json.get(APIConstants.PortalNotifications.APPLICATION_NAME);
            String requestedTier = (String) json.get(APIConstants.PortalNotifications.REQUESTED_TIER);
            String status;

            if (APIConstants.PortalNotifications.APPROVED.equals(comment)) {
                status = APIConstants.PortalNotifications.STATUS_APPROVED;
            } else {
                status = APIConstants.PortalNotifications.STATUS_REJECTED;
            }

            switch (NotificationType.valueOf(notificationType)) {
            case API_STATE_CHANGE:
                finalComment = "API State Change request to " + action + " the API: " + apiName + ", version: "
                        + apiVersion + " that has the context: " + apiContext + " has been " + status + ".";
                break;
            case API_PRODUCT_STATE_CHANGE:
                finalComment = "API Product State Change request of the API PRODUCT: " + apiName + ", version: "
                        + apiVersion + " that has the context: " + apiContext + " has been " + status + ".";
                break;
            case API_REVISION_DEPLOYMENT:
                finalComment = "API Revision Deployment request of the API: " + apiName + ", version: " + apiVersion
                        + " for RevisionId: " + revisionId + " has been " + status + ".";
                break;
            case APPLICATION_CREATION:
                finalComment = "Application Creation request for the Application: " + applicationName + " has been "
                        + status + ".";
                break;
            case SUBSCRIPTION_CREATION:
                finalComment = "Subscription Creation request for the API: " + apiName + " version: " + apiVersion
                        + " using Application: " + applicationName + " has been " + status + ".";
                break;
            case SUBSCRIPTION_UPDATE:
                finalComment = "Subscription Update request for the " + requestedTier + " for the API: " + apiName
                        + " version: " + apiVersion + " using Application: " + applicationName + " has been "
                        + status + ".";
                break;
            case SUBSCRIPTION_DELETION:
                finalComment = "Subscription Deletion request for the API: " + apiName + " version: " + apiVersion
                        + " using Application: " + applicationName + " has been " + status + ".";
                break;
            case APPLICATION_REGISTRATION_PRODUCTION:
                finalComment = "Production Key Generation request for the Application: " + applicationName
                        + " has been " + status + ".";
                break;
            case APPLICATION_REGISTRATION_SANDBOX:
                finalComment = "Sandbox Key Generation request for the Application: " + applicationName + " has been "
                        + status + ".";
                break;
            }

            if (!APIConstants.PortalNotifications.APPROVED.equals(comment)) {
                finalComment = finalComment + " Reason: " + comment;
            }

        } catch (ParseException e) {
            log.error("Failed to parse notification metadata JSON", e);
        }
        return finalComment;
    }

    public boolean deleteAllNotifications(String username, String organization, String portalToDisplay)
            throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.DELETE_ALL_NOTIFICATIONS_OF_USER;
        try (Connection conn = APIMgtDBUtil.getConnection()){
            conn.setAutoCommit(false);
            try ( PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, username);
                ps.setString(2, organization);
                ps.setString(3, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    String deleteNotification = SQLConstants.PortalNotifications.DELETE_NOTIFICATIONS;
                    try (PreparedStatement preparedStatement = conn.prepareStatement(deleteNotification)) {
                        preparedStatement.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to delete notifications", e);
            }
        } catch (SQLException e) {
            handleException("Error while establishing database connection", e);
        }

        return false;
    }

    public Notification markNotificationAsReadById(String username, String organization, String notificationId,
            String portalToDisplay) throws APIManagementException {

        Notification notification;
        String sqlQuery = SQLConstants.PortalNotifications.MARK_NOTIFICATION_AS_READ;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, notificationId);
                ps.setString(2, username);
                ps.setString(3, organization);
                ps.setString(4, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    notification = getNotificationById(notificationId, username, organization, portalToDisplay, conn);
                    if (notification != null) {
                        conn.commit();
                        return notification;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to mark notification as read", e);
            }
        } catch (SQLException e) {
            handleException("Failed to establish database connection", e);
        }
        return null;
    }

    private Notification getNotificationById(String notificationId, String username, String organization,
            String portalToDisplay, Connection conn) throws SQLException {

        String sqlQuery = SQLConstants.PortalNotifications.GET_NOTIFICATION_BY_ID;
        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setString(1, notificationId);
            ps.setString(2, username);
            ps.setString(3, organization);
            ps.setString(4, portalToDisplay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Notification notification = new Notification();
                    notification.setNotificationId(rs.getString("NOTIFICATION_ID"));
                    notification.setNotificationType(rs.getString("NOTIFICATION_TYPE"));
                    notification.setCreatedTime(rs.getTimestamp("CREATED_TIME").toString());
                    notification.setComments(getCommentFromMetaData(rs.getString("NOTIFICATION_METADATA"),
                            rs.getString("NOTIFICATION_TYPE")));
                    notification.setIsRead(rs.getBoolean("IS_READ"));
                    return notification;
                }
            }
        }
        return null;
    }

    public boolean deleteNotificationById(String username, String organization, String notificationId,
            String portalToDisplay) throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.DELETE_NOTIFICATION_BY_ID;

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, notificationId);
                ps.setString(2, username);
                ps.setString(3, organization);
                ps.setString(4, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    String deleteNotification = SQLConstants.PortalNotifications.DELETE_NOTIFICATIONS;
                    try (PreparedStatement preparedStatement = conn.prepareStatement(deleteNotification)) {
                        preparedStatement.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to delete notification by id", e);
            }
        } catch (SQLException e) {
            handleException("Failed to establish database connection", e);
        }
        return false;
    }

    public NotificationList markAllNotificationsAsRead(String username, String organization, String portalToDisplay)
            throws APIManagementException {

        String sqlQuery = SQLConstants.PortalNotifications.MARK_ALL_NOTIFICATIONS_AS_READ;

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
                ps.setString(1, username);
                ps.setString(2, organization);
                ps.setString(3, portalToDisplay);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    NotificationList notifications = getNotifications(username, organization, portalToDisplay, "desc",
                            10, 0);
                    connection.commit();
                    return notifications;
                }
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to mark all notifications as read", e);
            }
        } catch (SQLException e) {
            handleException("Failed to establish database connection", e);
        }
        return null;
    }

    public String getAPIUUIDUsingNameContextVersion(String apiName, String apiContext, String apiVersion,
            String organization) throws APIManagementException {

        String apiUUID = null;
        String sqlQuery = SQLConstants.PortalNotifications.GET_API_UUID_USING_NAME_CONTEXT_VERSION;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
            ps.setString(1, apiName);
            ps.setString(2, apiContext);
            ps.setString(3, apiVersion);
            ps.setString(4, organization);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    apiUUID = rs.getString("API_UUID");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API UUID using name, context, and version", e);
        }
        return apiUUID;
    }

    public int getUnreadNotificationCount(String username, String organization, String portalToDisplay, Connection conn)
            throws APIManagementException {
        int unreadCount = 0;
        String sqlQuery = SQLConstants.PortalNotifications.GET_UNREAD_NOTIFICATION_COUNT;

        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setString(1, username);
            ps.setString(2, organization);
            ps.setString(3, portalToDisplay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unreadCount = rs.getInt("UNREAD_NOTIFICATION_COUNT");
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get unread notification count for user " + username, e);
        }

        return unreadCount;
    }

    private void handleException(String message, Throwable t) throws APIManagementException {
        log.error(message, t);
        throw new APIManagementException(message, t);
    }
}
