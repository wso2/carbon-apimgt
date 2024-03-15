package org.wso2.carbon.apimgt.impl.portalNotifications;

public class PortalNotificationEndUserDTO {

    private String notificationId;
    private String destinationUser;
    private String organization;
    private boolean isRead;
    private String portalToDisplay;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getDestinationUser() {
        return destinationUser;
    }

    public void setDestinationUser(String destinationUser) {
        this.destinationUser = destinationUser;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public String getPortalToDisplay() {
        return portalToDisplay;
    }

    public void setPortalToDisplay(String portalToDisplay) {
        this.portalToDisplay = portalToDisplay;
    }
}
