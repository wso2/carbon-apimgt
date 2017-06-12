package org.wso2.carbon.apimgt.gateway.event.analytics.response;

import org.wso2.carbon.apimgt.gateway.event.publisher;
import org.wso2.carbon.apimgt.gateway.event.holder;
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.dto;
import ballerina.lang.errors;

function mediate (message m, message res) {
    dto:AnalyticsInfoDTO analyticsConf = holder:getAnalyticsConf();
    if (!analyticsConf.enabled) {
        system:println("Analytics is Disabled");
        return;
    }
    errors:TypeCastError err;
    int request_start_time;
    int request_end_time;
    int backend_start_time;
    int backend_end_time;
    int security_latency;
    int throttling_latency;
    int request_mediation_latency;
    int response_mediation_latency;
    int other_latency;

    request_start_time, _ = <int> messages:getProperty(m, "am.request_start_time");
    request_end_time , _ = <int>messages:getProperty(m, "am.request_end_time");
    backend_start_time , _ = <int>messages:getProperty(m, "am.backend_start_time");
    backend_end_time , _ = <int>messages:getProperty(res, "am.backend_end_time");
    security_latency , _ = <int>messages:getProperty(m, "am.security_latency");
    throttling_latency , _ = <int>messages:getProperty(m, "am.throttling_latency");
    request_mediation_latency , _ = <int>messages:getProperty(m, "am.request_mediation_latency");
    response_mediation_latency , _ = <int>messages:getProperty(m, "am.response_mediation_latency");
    other_latency , _ = <int>messages:getProperty(m, "am.other_latency");
    int current_time = system:currentTimeMillis();
    int response_time = request_end_time - request_start_time;
    int backend_time = backend_end_time - backend_start_time;
    int service_time = response_time - backend_time;
    int backend_latency = current_time - backend_start_time;

    dto:EventHolderDTO eventHolderDTO = {};
    eventHolderDTO.timestamp = system:currentTimeMillis();

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
    requestEventDTO.requestTime = request_start_time;
    requestEventDTO.gatewayDomain = messages:getProperty(m, "gateway_domain");
    requestEventDTO.gatewayIp = messages:getProperty(m, "gateway_ip");
    requestEventDTO.isThrottled = false; //(boolean)messages:getProperty(m, "is_throttled");
    requestEventDTO.throttledReason = messages:getProperty(m, "throttled_reason");
    requestEventDTO.throttledPolicy = messages:getProperty(m, "throttled_policy");
    requestEventDTO.clientIp = messages:getProperty(m, "client_ip");
    requestEventDTO.userAgent = messages:getProperty(m, "user_agent");
    requestEventDTO.hostName = messages:getProperty(m, "host_name");

    dto:ResponseEventDTO responseEventDTO = {};
    responseEventDTO.responseCount = 1;
    responseEventDTO.responseTime = backend_end_time;
    responseEventDTO.cacheHit = false; //messages:getProperty(m, "cache_hit");
    responseEventDTO.contentLength = http:getContentLength(res);
    responseEventDTO.protocol = messages:getProperty(m, "PROTOCOL");
    responseEventDTO.statusCode = http:getStatusCode(res);
    responseEventDTO.destination = messages:getProperty(m, "destination");

    dto:LatencyEventDTO latencyEventDTO = {};
    latencyEventDTO.responseTime = response_time;
    latencyEventDTO.serviceTime = service_time;
    latencyEventDTO.backendTime = backend_time;
    latencyEventDTO.backendLatency = backend_latency;
    latencyEventDTO.securityLatency = security_latency;
    latencyEventDTO.throttlingLatency = throttling_latency;
    latencyEventDTO.request_mediationLatency = request_mediation_latency;
    latencyEventDTO.response_mediationLatency = response_mediation_latency;
    latencyEventDTO.otherLatency = other_latency;

    eventHolderDTO.requestEventDTO = requestEventDTO;
    eventHolderDTO.responseEventDTO = responseEventDTO;
    eventHolderDTO.latencyEventDTO = latencyEventDTO;

    publisher:publishRequestEvent(eventHolderDTO);
}
