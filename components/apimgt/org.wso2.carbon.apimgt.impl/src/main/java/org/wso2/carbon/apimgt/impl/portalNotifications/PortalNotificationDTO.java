package org.wso2.carbon.apimgt.impl.portalNotifications;

import java.sql.Timestamp;
import java.util.List;

public class PortalNotificationDTO {

    private String notificationId;
    private PortalNotificationType notificationType;
    private Timestamp createdTime;
    private PortalNotificationMetaData notificationMetadata;
    private String organization;
    private List<PortalNotificationEndUserDTO> endUsers;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public PortalNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(PortalNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public PortalNotificationMetaData getNotificationMetadata() {
        return notificationMetadata;
    }

    public void setNotificationMetadata(PortalNotificationMetaData notificationMetadata) {
        this.notificationMetadata = notificationMetadata;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<PortalNotificationEndUserDTO> getEndUsers() {
        return endUsers;
    }

    public void setEndUsers(List<PortalNotificationEndUserDTO> endUsers) {
        this.endUsers = endUsers;
    }
}
