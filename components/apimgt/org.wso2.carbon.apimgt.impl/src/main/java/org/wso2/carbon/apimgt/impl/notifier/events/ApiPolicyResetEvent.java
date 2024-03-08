package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Objects;

public class ApiPolicyResetEvent extends PolicyResetEvent{
    private String uuid;
    private String apiContext;
    private String apiVersion;
    private String apiTier;

    public ApiPolicyResetEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                                        String uuid, String apiContext, String apiVersion, String apiTier) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.tenantDomain = tenantDomain;
        this.apiContext = apiContext;
        this.apiVersion = apiVersion;
        this.apiTier = apiTier;
        this.policyType = APIConstants.PolicyType.API;
    }

    @Override
    public String toString() {
        return "ApiPolicyResetEvent{" +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", apiContext='" + apiContext + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", apiTier='" + apiTier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiPolicyResetEvent)) return false;
        ApiPolicyResetEvent that = (ApiPolicyResetEvent) o;
        return
                        getApiContext().equals(that.getApiContext()) &&
                        getApiVersion().equals(that.getApiVersion()) &&
                        getApiTier().equals(that.getApiTier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiContext(), getApiVersion(), getApiTier());
    }


    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
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

    public String getApiTier() {
        return apiTier;
    }

    public void setApiTier(String apiTier) {
        this.apiTier = apiTier;
    }

}
