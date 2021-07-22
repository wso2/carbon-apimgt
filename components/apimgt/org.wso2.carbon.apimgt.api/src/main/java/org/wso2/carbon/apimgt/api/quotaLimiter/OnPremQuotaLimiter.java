package org.wso2.carbon.apimgt.api.quotaLimiter;

public class OnPremQuotaLimiter implements ResourceQuotaLimiter {

    @Override
    public boolean GetAPIRateLimitStatus(String orgID, String userId, String resourceType) {
        return false;
    }
}
