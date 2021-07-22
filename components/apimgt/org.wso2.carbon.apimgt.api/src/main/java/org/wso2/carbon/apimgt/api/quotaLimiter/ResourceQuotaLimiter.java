package org.wso2.carbon.apimgt.api.quotaLimiter;

public interface ResourceQuotaLimiter {
    public boolean GetAPIRateLimitStatus(String orgID, String userId, String resourceType);
}
