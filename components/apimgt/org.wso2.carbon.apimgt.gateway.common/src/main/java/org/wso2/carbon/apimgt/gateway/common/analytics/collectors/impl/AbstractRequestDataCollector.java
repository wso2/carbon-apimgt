package org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl;

import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.AnalyticsDataProvider;

/**
 * Abstract request data collector
 */
public abstract class AbstractRequestDataCollector {
    private AnalyticsDataProvider provider;

    public AbstractRequestDataCollector(AnalyticsDataProvider provider) {
        this.provider = provider;
    }

    public AnalyticsDataProvider getProvider() {
        return provider;
    }
}
