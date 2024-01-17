package org.wso2.carbon.apimgt.impl.portalNotifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

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
            ps.setString(5, portalNotificationDTO.getOrganization());

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
        json.put("applicationName", metaData.getApplicationName());
        json.put("requestedTier", metaData.getRequestedTier());
        json.put("revisionId", metaData.getRevisionId());
        json.put("comment", metaData.getComment());
        return json.toJSONString();
    }
}
