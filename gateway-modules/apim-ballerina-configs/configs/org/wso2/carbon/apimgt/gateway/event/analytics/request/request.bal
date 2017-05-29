package org.wso2.apim.eventPublisher.request;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.jsons;

function mediate (message m) {
    http:ClientConnector client = create http:ClientConnector("http://localhost:9091");
    message request = {};
    message response = {};

    json event = {};

    jsons:add (event, "$", "streamName", "RequestStream");
    jsons:add (event, "$", "executionPlanName", "TestExecutionPlan");
    jsons:add (event, "$", "timestamp", "123456789");

    json dataArr = [];
    jsons:add (dataArr, "$", messages:getProperty(m, "api"));
    jsons:add (dataArr, "$", messages:getProperty(m, "context"));
    jsons:add (dataArr, "$", messages:getProperty(m, "version"));
    jsons:add (dataArr, "$", messages:getProperty(m, "publisher"));
    jsons:add (dataArr, "$", messages:getProperty(m, "subscription_policy"));
    jsons:add (dataArr, "$", messages:getProperty(m, "uri_template"));
    jsons:add (dataArr, "$", messages:getProperty(m, "method"));
    jsons:add (dataArr, "$", messages:getProperty(m, "consumer_key"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_name"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_id"));
    jsons:add (dataArr, "$", messages:getProperty(m, "application_owner"));
    jsons:add (dataArr, "$", messages:getProperty(m, "user_id"));
    jsons:add (dataArr, "$", messages:getProperty(m, "subscriber"));
    jsons:add (dataArr, "$", messages:getProperty(m, "request_count"));
    jsons:add (dataArr, "$", messages:getProperty(m, "request_time"));
    jsons:add (dataArr, "$", messages:getProperty(m, "gateway_domain"));
    jsons:add (dataArr, "$", messages:getProperty(m, "gateway_ip"));
    jsons:add (dataArr, "$", messages:getProperty(m, "is_throttled"));
    jsons:add (dataArr, "$", messages:getProperty(m, "throttled_reason"));
    jsons:add (dataArr, "$", messages:getProperty(m, "throttled_policy"));
    jsons:add (dataArr, "$", messages:getProperty(m, "client_ip"));
    jsons:add (dataArr, "$", messages:getProperty(m, "user_agent"));
    jsons:add (dataArr, "$", messages:getProperty(m, "host_name"));

    jsons:add (event, "$", "data", dataArr);

    system:println(event);


    messages:setJsonPayload (request, event);
    messages:addHeader(request, "content-type", "text/plain");
    response = http:ClientConnector.post (client, "/simulation/single", request);
    string payload = messages:getStringPayload(response);
    system:println(payload);
}
