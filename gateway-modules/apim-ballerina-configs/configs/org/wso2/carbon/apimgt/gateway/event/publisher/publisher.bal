package org.wso2.carbon.apimgt.gateway.event.publisher;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.event.analytics.util;

function publish (json event) {
    http:ClientConnector client = create http:ClientConnector("http://localhost:9091");
    message request = {};
    message response = {};
    //system:println(event);

    messages:setJsonPayload (request, event);
    messages:addHeader(request, "content-type", "text/plain");
    response = http:ClientConnector.post (client, "/simulation/single", request);
    int statusCode = http:getStatusCode(response);
    if (statusCode != 200) {
        string responseMsg = messages:getStringPayload(response);
        system:println("Error publishing DAS event. code: " + statusCode + ", msg: " + responseMsg);
    }
}

function publishRequestEvent (dto:EventHolderDTO event) {
    json payload = util:getRequestEventPayload(event);
    publish(payload);
}

function publishThrottleEvent (dto:ThrottleEventHolderDTO event) {
    json payload = util:getThrottleEventPayload(event);
    publish(payload);
}