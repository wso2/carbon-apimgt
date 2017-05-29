package org.wso2.carbon.apimgt.gateway.event.analytics.response;

import org.wso2.carbon.apimgt.gateway.event.publisher;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.system;
import ballerina.net.http;

function mediate (message m, message res) {

    int request_start_time = (int)messages:getProperty(m, "am.request_start_time");
    int request_end_time = (int)messages:getProperty(m, "am.request_end_time");
    int backend_start_time = (int)messages:getProperty(m, "am.backend_start_time");
    int backend_end_time = (int)messages:getProperty(m, "am.backend_end_time");
    int security_latency = (int)messages:getProperty(m, "am.security_latency");
    int throttling_latency = (int)messages:getProperty(m, "am.throttling_latency");
    int request_mediation_latency = (int)messages:getProperty(m, "am.request_mediation_latency");
    int response_mediation_latency = (int)messages:getProperty(m, "am.response_mediation_latency");
    int other_latency = (int)messages:getProperty(m, "am.other_latency");
    int current_time = system:currentTimeMillis();
    int response_time =  request_end_time - request_start_time;
    int backend_time = backend_end_time - backend_start_time;
    int service_time = response_time - backend_time;
    int backend_latency = current_time - backend_start_time;

    json event = {};

    jsons:add (event, "$", "streamName", "ResponseStream");
    jsons:add (event, "$", "executionPlanName", "ResponseExecutionPlan");
    jsons:add (event, "$", "timestamp", "123456789");

    json dataArr = [];
    jsons:add (dataArr, "$", messages:getProperty(m, "api"));
    jsons:add (dataArr, "$", messages:getProperty(m, "REQUEST_URL"));
    jsons:add (dataArr, "$", messages:getProperty(m, "version"));
    jsons:add (dataArr, "$", messages:getProperty(m, "publisher"));
    jsons:add (dataArr, "$", messages:getProperty(m, "subscription_policy"));
    jsons:add (dataArr, "$", messages:getProperty(m, "SUB_PATH"));
    jsons:add (dataArr, "$", messages:getProperty(m, "HTTP_METHOD"));
    jsons:add (dataArr, "$", messages:getProperty(m, "consumer_key"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_name"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_id"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_owner"));
    jsons:add (dataArr, "$", messages:getProperty(m, "user_id"));
    jsons:add (dataArr, "$", messages:getProperty(m, "subscriber"));
    jsons:add (dataArr, "$", messages:getProperty(m, "request_count"));
    jsons:add (dataArr, "$", messages:getProperty(m, "response_count"));
    jsons:add (dataArr, "$", request_start_time+"");   //request event type
    jsons:add (dataArr, "$", backend_end_time+"");     //response event type
    jsons:add (dataArr, "$", messages:getProperty(m, "gateway_domain"));
    jsons:add (dataArr, "$", messages:getProperty(m, "gateway_ip"));
    jsons:add (dataArr, "$", messages:getProperty(m, "is_throttled"));
    jsons:add (dataArr, "$", messages:getProperty(m, "throttled_reason"));
    jsons:add (dataArr, "$", messages:getProperty(m, "throttled_policy"));
    jsons:add (dataArr, "$", messages:getProperty(m, "client_ip"));
    jsons:add (dataArr, "$", messages:getProperty(m, "user_agent"));
    jsons:add (dataArr, "$", messages:getProperty(m, "host_name"));

    jsons:add (dataArr, "$", messages:getProperty(m, "cache_hit"));
    jsons:add (dataArr, "$", http:getContentLength(res)+"");
    jsons:add (dataArr, "$", messages:getProperty(m, "PROTOCOL"));
    jsons:add (dataArr, "$", http:getStatusCode(res)+"");
    jsons:add (dataArr, "$", messages:getProperty(m, "destination"));
    jsons:add (dataArr, "$", response_time+"");
    jsons:add (dataArr, "$", service_time+"");
    jsons:add (dataArr, "$", backend_time+"");
    jsons:add (dataArr, "$", backend_latency+"");
    jsons:add (dataArr, "$", security_latency+"");
    jsons:add (dataArr, "$", throttling_latency+"");
    jsons:add (dataArr, "$", request_mediation_latency+"");
    jsons:add (dataArr, "$", response_mediation_latency+"");
    jsons:add (dataArr, "$", other_latency+"");

    jsons:add (event, "$", "data", dataArr);

    publisher:publish(event);
}
