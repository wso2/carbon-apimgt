package org.wso2.carbon.apimgt.gateway.event.publisher;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;

function publish (json event) {
    http:ClientConnector client = create http:ClientConnector("http://localhost:9091");
    message request = {};
    message response = {};

    system:println(event);

    messages:setJsonPayload (request, event);
    messages:addHeader(request, "content-type", "text/plain");
    response = http:ClientConnector.post (client, "/simulation/single", request);
    string payload = messages:getStringPayload(response);
    system:println(payload);
}
