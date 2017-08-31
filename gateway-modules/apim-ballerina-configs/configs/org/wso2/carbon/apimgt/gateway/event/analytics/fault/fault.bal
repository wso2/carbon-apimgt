package org.wso2.carbon.apimgt.gateway.event.analytics.fault;

import org.wso2.carbon.apimgt.gateway.event.publisher;
import org.wso2.carbon.apimgt.gateway.event.holder;
import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto;
import ballerina.lang.errors;

function mediate (message m, errors:Error e) {
    dto:AnalyticsInfoDTO analyticsConf = holder:getAnalyticsConf();
    if (!analyticsConf.enabled) {
        system:println("Analytics is Disabled");
        return;
    }

    int current_time = system:currentTimeMillis();

    dto:FaultEventHolderDTO faultEventHolderDTO = {};
    faultEventHolderDTO.timestamp = system:currentTimeMillis();

    dto:RequestEventDTO requestEventDTO = {};
    requestEventDTO.api = messages:getProperty(m, "api");
    requestEventDTO.context = messages:getProperty(m, "REQUEST_URL");
    requestEventDTO.version = messages:getProperty(m, "version");
    requestEventDTO.publisher = messages:getProperty(m, "publisher");
    requestEventDTO.subscriptionPolicy = messages:getProperty(m, "subscription_policy");
    requestEventDTO.uriTemplate = messages:getProperty(m, "SUB_PATH");
    requestEventDTO.httpMethod = messages:getProperty(m, "HTTP_METHOD");
    requestEventDTO.consumerKey = messages:getProperty(m, "consumer_key");
    requestEventDTO.applicationName = messages:getProperty(m, "application_name");
    requestEventDTO.applicationId = messages:getProperty(m, "application_id");
    requestEventDTO.applicationOwner = messages:getProperty(m, "application_owner");
    requestEventDTO.userId = messages:getProperty(m, "user_id");
    requestEventDTO.subscriber = messages:getProperty(m, "subscriber");
    requestEventDTO.requestCount = 1;
    requestEventDTO.requestTime = current_time;
    requestEventDTO.gatewayDomain = messages:getProperty(m, "gateway_domain");
    requestEventDTO.gatewayIp = messages:getProperty(m, "gateway_ip");
    requestEventDTO.isThrottled = false; //(boolean)messages:getProperty(m, "is_throttled");
    requestEventDTO.throttledReason = messages:getProperty(m, "throttled_reason");
    requestEventDTO.throttledPolicy = messages:getProperty(m, "throttled_policy");
    requestEventDTO.clientIp = messages:getProperty(m, "client_ip");
    requestEventDTO.userAgent = messages:getProperty(m, "user_agent");
    requestEventDTO.hostName = messages:getProperty(m, "host_name");

    dto:FaultEventDTO faultEventDTO = {};
    faultEventDTO.faultCount = 1;
    faultEventDTO.faultTime = current_time;
    faultEventDTO.faultReason = e.msg;

    faultEventHolderDTO.requestEventDTO = requestEventDTO;
    faultEventHolderDTO.faultEventDTO = faultEventDTO;

    publisher:publishFaultEvent(faultEventHolderDTO);
}
