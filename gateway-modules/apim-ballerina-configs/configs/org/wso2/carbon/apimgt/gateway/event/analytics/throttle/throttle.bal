package org.wso2.carbon.apimgt.gateway.event.analytics.throttle;

import org.wso2.carbon.apimgt.gateway.event.publisher;
import org.wso2.carbon.apimgt.gateway.event.holder;
import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto;

function mediate (message m) {
    dto:AnalyticsInfoDTO analyticsConf = holder:getAnalyticsConf();
    if (!analyticsConf.enabled) {
        system:println("Analytics is Disabled");
        return;
    }
    int current_time = system:currentTimeMillis();

    dto:ThrottleEventAnalyticsHolderDTO throttleEventHolderDTO = {};
    throttleEventHolderDTO.timestamp = system:currentTimeMillis();

    dto:ThrottleAnalyticsEventDTO throttleAnalyticsEventDTO = {};
    throttleAnalyticsEventDTO.api = messages:getProperty(m, "api");
    throttleAnalyticsEventDTO.context = messages:getProperty(m, "REQUEST_URL");
    throttleAnalyticsEventDTO.version = messages:getProperty(m, "version");
    throttleAnalyticsEventDTO.publisher = messages:getProperty(m, "publisher");
    throttleAnalyticsEventDTO.subscriptionPolicy = messages:getProperty(m, "subscription_policy");
    throttleAnalyticsEventDTO.uriTemplate = messages:getProperty(m, "SUB_PATH");
    throttleAnalyticsEventDTO.httpMethod = messages:getProperty(m, "HTTP_METHOD");
    throttleAnalyticsEventDTO.consumerKey = messages:getProperty(m, "consumer_key");
    throttleAnalyticsEventDTO.applicationName = messages:getProperty(m, "application_name");
    throttleAnalyticsEventDTO.applicationId = messages:getProperty(m, "application_id");
    throttleAnalyticsEventDTO.applicationOwner = messages:getProperty(m, "application_owner");
    throttleAnalyticsEventDTO.userId = messages:getProperty(m, "user_id");
    throttleAnalyticsEventDTO.subscriber = messages:getProperty(m, "subscriber");
    throttleAnalyticsEventDTO.throttleCount = 1;
    throttleAnalyticsEventDTO.throttleTime = current_time;
    throttleAnalyticsEventDTO.gatewayDomain = messages:getProperty(m, "gateway_domain");
    throttleAnalyticsEventDTO.gatewayIp = messages:getProperty(m, "gateway_ip");
    throttleAnalyticsEventDTO.throttledReason = messages:getProperty(m, "throttled_reason");
    throttleAnalyticsEventDTO.throttledPolicy = messages:getProperty(m, "throttled_policy");
    throttleAnalyticsEventDTO.hostName = messages:getProperty(m, "host_name");

    throttleEventHolderDTO.throttleAnalyticsEventDTO = throttleAnalyticsEventDTO;

    publisher:publishThrottleAnalyticsEvent(throttleEventHolderDTO);
}
