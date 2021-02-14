package org.wso2.carbon.apimgt.gateway.common.analytics.collectors;

import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Target;

/**
 * Data provider interface to extract request data
 */
public interface AnalyticsDataProvider {
    boolean isSuccessRequest();

    boolean isFaultRequest();

    boolean isAnonymous();

    boolean isAuthenticated();

    boolean isAuthFaultRequest();

    boolean isThrottledFaultRequest();

    boolean isTargetFaultRequest();

    boolean isResourceNotFound();

    boolean isMethodNotAllowed();

    API getApi();

    Application getApplication();

    Latencies getLatency();

    Operation getOperation();

    Target getTarget();

    Latencies getLatencies();

    MetaInfo getMetaInfo();

    int getProxyResponseCode();

    int getTargetResponseCode();

    long getRequestTime();

    Error getError();

    String getUserAgentHeader();

}
