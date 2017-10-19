package org.wso2.carbon.apimgt.gateway.event.util;

import ballerina.lang.messages;

function simulate (message m) {
    messages:setProperty(m, "api", "sampleApi");
    //messages:setProperty(m, "context", "/sampleApi");
    messages:setProperty(m, "version", "1.0.0");
    messages:setProperty(m, "subscription_policy", "GOLD");
    //messages:setProperty(m, "uri_template", "/path");
    messages:setProperty(m, "consumer_key", "AKJHSJAHSIIEWW64451231");

    messages:setProperty(m, "application_id", "123456789");
    messages:setProperty(m, "user_id", "admin");
    messages:setProperty(m, "subscriber", "admin");
    //messages:setProperty(m, "event_time", 13454564556);
    messages:setProperty(m, "gateway_domain", "gw.wso2.com");
    messages:setProperty(m, "gateway_ip", "127.0.0.1");
    messages:setProperty(m, "is_throttled", "false");
    messages:setProperty(m, "throttled_reason", "SUBSCRIPTION");
    messages:setProperty(m, "throttled_policy", "LARGE");
    messages:setProperty(m, "client_ip", "127.0.0.1");
    messages:setProperty(m, "publisher", "admin");
    messages:setProperty(m, "user_agent", "curl");
    //messages:setProperty(m, "method", "GET");

    messages:setProperty(m, "application_name", "sampleApp");
    messages:setProperty(m, "application_owner", "admin");
    messages:setProperty(m, "request_count", "1");
    messages:setProperty(m, "response_count", "1");
    messages:setProperty(m, "host_name", "wso2gw");

    messages:setProperty(m, "cache_hit", "0");
    //messages:setProperty(m, "response_size", 123);
    //messages:setProperty(m, "protocol", "http");
    //messages:setProperty(m, "response_code", 200);
    messages:setProperty(m, "destination", "destination");
    //messages:setProperty(m, "response_time", "123456");
    //messages:setProperty(m, "service_time", "123456");
    //messages:setProperty(m, "backend_time", "123456");
    //messages:setProperty(m, "backend_latency", "123456");
    messages:setProperty(m, "am.security_latency", "123456");
    messages:setProperty(m, "am.throttling_latency", "123456");
    messages:setProperty(m, "am.request_mediation_latency", "123456");
    messages:setProperty(m, "am.response_mediation_latency", "123456");
    messages:setProperty(m, "am.other_latency", "123456");
}
