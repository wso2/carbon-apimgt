package org.wso2.carbon.apimgt.persistence.documents;

import org.wso2.carbon.apimgt.api.model.TierPermission;

import java.util.Map;

public class TiersDocument {

    private String name;
    private String displayName;
    private String description;
    private byte[] policyContent;
    private Map<String, Object> tierAttributes;

    private long requestsPerMin = 0;
    private long requestCount = 0;
    private long unitTime = 0;
    private String timeUnit = "ms";
    private String tierPlan;
    // The default value would be "true" since the default behavior is to stop when the quota is reached
    private boolean stopOnQuotaReached = true;
    private TierPermission tierPermission;
    private Map<String, String> monetizationAttributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPolicyContent() {
        return policyContent;
    }

    public void setPolicyContent(byte[] policyContent) {
        this.policyContent = policyContent;
    }

    public Map<String, Object> getTierAttributes() {
        return tierAttributes;
    }

    public void setTierAttributes(Map<String, Object> tierAttributes) {
        this.tierAttributes = tierAttributes;
    }

    public long getRequestsPerMin() {
        return requestsPerMin;
    }

    public void setRequestsPerMin(long requestsPerMin) {
        this.requestsPerMin = requestsPerMin;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public long getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(long unitTime) {
        this.unitTime = unitTime;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTierPlan() {
        return tierPlan;
    }

    public void setTierPlan(String tierPlan) {
        this.tierPlan = tierPlan;
    }

    public boolean isStopOnQuotaReached() {
        return stopOnQuotaReached;
    }

    public void setStopOnQuotaReached(boolean stopOnQuotaReached) {
        this.stopOnQuotaReached = stopOnQuotaReached;
    }

    public TierPermission getTierPermission() {
        return tierPermission;
    }

    public void setTierPermission(TierPermission tierPermission) {
        this.tierPermission = tierPermission;
    }

    public Map<String, String> getMonetizationAttributes() {
        return monetizationAttributes;
    }

    public void setMonetizationAttributes(Map<String, String> monetizationAttributes) {
        this.monetizationAttributes = monetizationAttributes;
    }
}
