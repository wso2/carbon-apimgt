package org.wso2.carbon.apimgt.impl.dto;

/**
 * Configuration DTO for Gateway Notification/Heartbeat settings
 */
public class GatewayNotificationConfiguration {
    private boolean enabled = true;
    private int notifyIntervalSeconds = 30; // Default 30 seconds
    private String configuredGWID = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getNotifyIntervalSeconds() {
        return notifyIntervalSeconds;
    }

    public void setNotifyIntervalSeconds(int notifyIntervalSeconds) {
        this.notifyIntervalSeconds = notifyIntervalSeconds;
    }

    public String getConfiguredGWID() {
        return configuredGWID;
    }

    public void setConfiguredGWID(String configuredGWID) {
        this.configuredGWID = configuredGWID;
    }
} 