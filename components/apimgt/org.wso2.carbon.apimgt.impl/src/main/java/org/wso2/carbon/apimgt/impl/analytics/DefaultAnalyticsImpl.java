package org.wso2.carbon.apimgt.impl.analytics;

import org.wso2.carbon.apimgt.api.model.AnalyticsforMonetization;
import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;

public class DefaultAnalyticsImpl implements AnalyticsforMonetization {
    @Override
    public Object getUsageData(MonetizationUsagePublishInfo monetizationUsagePublishInfo) throws AnalyticsException {
        return new Object();
    }
}