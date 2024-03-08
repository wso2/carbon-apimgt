package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Objects;

public class ResourcePolicyResetEvent extends PolicyResetEvent{
    private String uuid;
    private String apiContext;
    private String apiVersion;
    private String resourceTier;
    private String resource;

    public ResourcePolicyResetEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                               String uuid, String apiContext, String apiVersion, String resourceTier, String resource) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.uuid = uuid;
        this.tenantDomain = tenantDomain;
        this.apiContext = apiContext;
        this.apiVersion = apiVersion;
        this.resourceTier = resourceTier;
        this.resource = resource;
        this.policyType = APIConstants.PolicyType.API;
        this.isResourceLevel = true;
    }

    @Override
    public String toString() {
        return "ResourcePolicyResetEvent{" +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", apiContext='" + apiContext + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", resourceTier='" + resourceTier + '\'' +
                ", resource='" + resourceTier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourcePolicyResetEvent)) return false;
        ResourcePolicyResetEvent that = (ResourcePolicyResetEvent) o;
        return
                getApiContext().equals(that.getApiContext()) &&
                        getApiVersion().equals(that.getApiVersion()) &&
                        getResourceTier().equals(that.getResourceTier()) &&
                        getResource().equals(that.getResource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiContext(), getApiVersion(), getResourceTier(), getResource());
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

    public String getResourceTier() {
        return resourceTier;
    }

    public void setResourceTier(String resourceTier) {
        this.resourceTier = resourceTier;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
