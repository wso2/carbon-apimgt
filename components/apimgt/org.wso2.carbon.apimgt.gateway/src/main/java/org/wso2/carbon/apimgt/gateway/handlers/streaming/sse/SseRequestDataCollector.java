package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.impl.GenericRequestDataCollector;

public class SseRequestDataCollector extends GenericRequestDataCollector {

    public SseRequestDataCollector(AnalyticsDataProvider provider) {
        super(provider);
    }


}
