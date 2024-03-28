package org.wso2.carbon.apimgt.impl.portalNotifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class PortalNotificationDAO {

    private static final Log log = LogFactory.getLog(PortalNotificationDAO.class);

    private static PortalNotificationDAO INSTANCE = null;

    private PortalNotificationDAO() {
    }

    public static PortalNotificationDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PortalNotificationDAO();
        }
        return INSTANCE;
    }

    public boolean addNotification(PortalNotificationDTO portalNotificationDTO) {

        String addNotificationQuery = SQLConstants.PortalNotifications.ADD_NOTIFICATION;
        String addEndUserQuery = SQLConstants.PortalNotifications.ADD_NOTIFICATION_END_USER;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(addNotificationQuery);
            String notificationId = UUID.randomUUID().toString();

            ps.setString(1, notificationId);
            ps.setString(2, portalNotificationDTO.getNotificationType().toString());
            ps.setTimestamp(3, portalNotificationDTO.getCreatedTime());

            String metadataJson = convertMetadataToJson(portalNotificationDTO.getNotificationMetadata());

            ps.setString(4, metadataJson);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                for (PortalNotificationEndUserDTO endUser : portalNotificationDTO.getEndUsers()) {
                    addEndUser(conn, addEndUserQuery, notificationId, endUser);
                }
                return true;
            }

        } catch (SQLException e) {
            log.error("Error while adding notification", e);
            log.error("SQL State: " + e.getSQLState());
            log.error("Error Code: " + e.getErrorCode());
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return false;
    }

    private void addEndUser(Connection conn, String addEndUserQuery, String notificationId,
            PortalNotificationEndUserDTO endUser) throws SQLException {

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(addEndUserQuery);
            ps.setString(1, notificationId);
            ps.setString(2, endUser.getDestinationUser());
            ps.setString(3, endUser.getOrganization());
            ps.setString(4, endUser.getPortalToDisplay());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while adding end users", e);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String convertMetadataToJson(PortalNotificationMetaData metaData) {
        JSONObject json = new JSONObject();
        json.put("apiName", metaData.getApi());
        json.put("apiVersion", metaData.getApiVersion());
        json.put("apiContext", metaData.getApiContext());
        json.put("action", metaData.getAction());
        json.put("applicationName", metaData.getApplicationName());
        json.put("requestedTier", metaData.getRequestedTier());
        json.put("revisionId", metaData.getRevisionId());
        json.put("comment", metaData.getComment());
        return json.toJSONString();
    }

    public NotificationList getNotifications(String username, String organization, String portalToDisplay, String sortOrder, Integer limit,
            Integer offset) throws APIManagementException {

        List<Notification> list = new ArrayList<Notification>();
        NotificationList notificationList = new NotificationList();
        Pagination pagination = new Pagination();
        notificationList.setPagination(pagination);
        int total = 0;
        int unreadCount = 0;

        String sqlQueryForCount = SQLConstants.PortalNotifications.GET_NOTIFICATIONS_COUNT;
        String sqlQuery;

        try (Connection conn = APIMgtDBUtil.getConnection();
                PreparedStatement psForCount = conn.prepareStatement(sqlQueryForCount)) {
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
                                notification.setComments(getCommentFromMetaData(rs.getString("NOTIFICATION_METADATA"),
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

            String comment = (String) json.get("comment");
            String apiName = (String) json.get("apiName");
            String apiVersion = (String) json.get("apiVersion");
            String apiContext = (String) json.get("apiContext");
            String action = (String) json.get("action");
            String revisionId = (String) json.get("revisionId");
            String applicationName = (String) json.get("applicationName");
            String requestedTier = (String) json.get("requestedTier");
            String status;

            if (comment.equals("APPROVED")) {
                status = "approved";
            } else {
                status = "rejected";
            }

            switch (notificationType) {
            case "API_STATE_CHANGE":
                finalComment = "API State Change request to " + action + " the API: " + apiName + ", version: " + apiVersion
                        + " that has the context: " + apiContext + " has been " + status + ".";
                break;
            case "API_PRODUCT_STATE_CHANGE":
                finalComment = "API Product State Change request of the API PRODUCT: " + apiName + ", version: "
                        + apiVersion + " that has the context: " + apiContext + " has been " + status + ".";
                break;
            case "API_REVISION_DEPLOYMENT":
                finalComment = "API Revision Deployment request of the API: " + apiName + ", version: " + apiVersion
                        + " for RevisionId: " + revisionId + " has been " + status + ".";
                break;
            case "APPLICATION_CREATION":
                finalComment = "Application Creation request for the Application: " + applicationName + " has been "
                        + status + ".";
                break;
            case "SUBSCRIPTION_CREATION":
                finalComment = "Subscription Creation request for the API: " + apiName + " version: " + apiVersion
                        + " using Application: " + applicationName + " has been " + status + ".";
                break;
            case "SUBSCRIPTION_UPDATE":
                finalComment = "Subscription Update request for the " + requestedTier + " for the API: " + apiName
                        + " version: " + apiVersion + " using Application: " + applicationName + " has been " + status
                        + ".";
                break;
            case "SUBSCRIPTION_DELETION":
                finalComment = "Subscription Deletion request for the API: " + apiName + " version: " + apiVersion
                        + " using Application: " + applicationName + " has been " + status + ".";
                break;
            case "APPLICATION_REGISTRATION_PRODUCTION":
                finalComment = "Production Key Generation request for the Application: " + applicationName
                        + " has been " + status + ".";
                break;
            case "APPLICATION_REGISTRATION_SANDBOX":
                finalComment = "Sandbox Key Generation request for the Application: " + applicationName + " has been "
                        + status + ".";
                break;
            }

            if (!comment.equals("APPROVED")) {
                finalComment = finalComment + " Reason: " + comment;
            }

        } catch (ParseException e) {
            log.error("Failed to parse notification metadata JSON", e);
        }
        return finalComment;
    }

    public boolean deleteAllNotifications(String username, String organization, String portalToDisplay) {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String sqlQuery = SQLConstants.PortalNotifications.DELETE_ALL_NOTIFICATIONS_OF_USER;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, username);
            ps.setString(2, organization);
            ps.setString(3, portalToDisplay);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                String deleteNotification = SQLConstants.PortalNotifications.DELETE_NOTIFICATIONS;
                ps = conn.prepareStatement(deleteNotification);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            log.error("Failed to delete notifications", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return false;
    }

    public Notification markNotificationAsReadById(String username, String organization, String notificationId, String portalToDisplay) {

        Connection conn = null;
        PreparedStatement ps = null;
        Notification notification;
        try {
            String sqlQuery = SQLConstants.PortalNotifications.MARK_NOTIFICATION_AS_READ;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, notificationId);
            ps.setString(2, username);
            ps.setString(3, organization);
            ps.setString(4, portalToDisplay);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                notification = getNotificationById(notificationId, username, organization, portalToDisplay, conn);
                if (notification != null) {
                    return notification;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to mark notification as read", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return null;
    }

    private Notification getNotificationById(String notificationId, String username, String organization, String portalToDisplay,
            Connection conn) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sqlQuery = SQLConstants.PortalNotifications.GET_NOTIFICATION_BY_ID;

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, notificationId);
            ps.setString(2, username);
            ps.setString(3, organization);
            ps.setString(4, portalToDisplay);
            rs = ps.executeQuery();
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
        } catch (SQLException e) {
            log.error("Failed to retrieve notification by id", e);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        return null;
    }

    public boolean deleteNotificationById(String username, String organization, String notificationId, String portalToDisplay) {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String sqlQuery = SQLConstants.PortalNotifications.DELETE_NOTIFICATION_BY_ID;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, notificationId);
            ps.setString(2, username);
            ps.setString(3, organization);
            ps.setString(4, portalToDisplay);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                String deleteNotification = SQLConstants.PortalNotifications.DELETE_NOTIFICATIONS;
                ps = conn.prepareStatement(deleteNotification);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            log.error("Failed to delete notification by id", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
        return false;
    }

    public NotificationList markAllNotificationsAsRead(String username, String organization, String portalToDisplay) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sqlQuery = SQLConstants.PortalNotifications.MARK_ALL_NOTIFICATIONS_AS_READ;
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, username);
            ps.setString(2, organization);
            ps.setString(3, portalToDisplay);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getNotifications(username, organization, portalToDisplay,null, null, null);
            }
        } catch (SQLException e) {
            log.error("Failed to mark all notifications as read", e);
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    public String getAPIUUIDUsingNameContextVersion(String apiName, String apiContext, String apiVersion, String organization) {

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
                log.error("Failed to retrieve API UUID using name, context and version", e);
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
}
