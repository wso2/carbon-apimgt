package org.wso2.carbon.apimgt.notification.event;

/**
 * Event to notify token revocation of a user by user events.
 */
public class InternalTokenRevocationUserEvent extends Event {
    private long revocationTime;
    private String userUUID;

    public long getRevocationTime() {
        return revocationTime;
    }

    public void setRevocationTime(long revocationTime) {
        this.revocationTime = revocationTime;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }
}
