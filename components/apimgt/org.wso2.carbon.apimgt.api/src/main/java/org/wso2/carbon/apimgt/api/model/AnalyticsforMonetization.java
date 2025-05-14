package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;

public interface AnalyticsforMonetization {
    /**
     * Gets Usage Data from Analytics Provider
     *
     * @param monetizationUsagePublishInfo monetization publish info
     * @return usage data from analytics provider
     * @throws AnalyticsException if the action failed
     */
    Object getUsageData(MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws AnalyticsException;
}