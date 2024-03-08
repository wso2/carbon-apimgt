package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Objects;

public class SubscriptionPolicyResetEvent extends PolicyResetEvent{
    private String uuid;
    private String appId;
    private String apiContext;
    private String apiVersion;
    private String subscriptionTier;

    public SubscriptionPolicyResetEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                                       String uuid, String appId, String apiContext, String apiVersion, String subscriptionTier) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.tenantDomain = tenantDomain;
        this.appId = appId;
        this.apiContext = apiContext;
        this.apiVersion = apiVersion;
        this.subscriptionTier = subscriptionTier;
        this.policyType = APIConstants.PolicyType.SUBSCRIPTION;
    }

    @Override
    public String toString() {
        return "SubscriptionPolicyResetEvent{" +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", appId='" + appId + '\'' +
                ", apiContext='" + apiContext + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", subscriptionTier='" + subscriptionTier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPolicyResetEvent)) return false;
        SubscriptionPolicyResetEvent that = (SubscriptionPolicyResetEvent) o;
        return
                getAppId().equals(that.getAppId()) &&
                        getApiContext().equals(that.getApiContext()) &&
                        getApiVersion().equals(that.getApiVersion()) &&
                        getSubscriptionTier().equals(that.getSubscriptionTier());
    }

    @Override
    public int hashCode() {
        return Objects.hash( getAppId(), getApiContext(), getApiVersion(), getSubscriptionTier());
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

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSubscriptionTier() {
        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

}
