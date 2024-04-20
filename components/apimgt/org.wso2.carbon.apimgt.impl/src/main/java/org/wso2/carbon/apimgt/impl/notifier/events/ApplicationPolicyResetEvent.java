package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Map;
import java.util.Objects;

import org.wso2.carbon.apimgt.impl.APIConstants.PolicyType;

public class ApplicationPolicyResetEvent extends PolicyEvent {
    private String uuid;
    private String appId;
    private String userId;
    private String appTier;

    public ApplicationPolicyResetEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                                     String uuid, String appId, String userId, String appTier) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.tenantDomain = tenantDomain;
        this.appId = appId;
        this.userId = userId;
        this.appTier = appTier;
        this.policyType = PolicyType.APPLICATION;
    }

    @Override
    public String toString() {
        return "ApplicationPolicyResetEvent{" +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", appId='" + appId + '\'' +
                ", userId='" + userId + '\'' +
                ", appTier='" + appTier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationPolicyResetEvent)) return false;
        ApplicationPolicyResetEvent that = (ApplicationPolicyResetEvent) o;
        return
                getAppId().equals(that.getAppId()) &&
                getUserId().equals(that.getUserId()) &&
                getAppTier().equals(that.getAppTier());
    }

    @Override
    public int hashCode() {
        return Objects.hash( getAppId(), getUserId(), getAppTier());
    }


    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppTier() {
        return appTier;
    }

    public void setAppTier(String appTier) {
        this.appTier = appTier;
    }

}
