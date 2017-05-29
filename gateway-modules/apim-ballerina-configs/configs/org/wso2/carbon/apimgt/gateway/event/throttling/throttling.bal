package org.wso2.carbon.apimgt.gateway.event.throttling;

import org.wso2.carbon.apimgt.gateway.event.publisher;
import ballerina.lang.messages;
import ballerina.lang.jsons;

function mediate (message m) {

    json event = {};

    jsons:add (event, "$", "streamName", "PreRequestStream");
    jsons:add (event, "$", "executionPlanName", "requestPreProcessorExecutionPlan");
    jsons:add (event, "$", "timestamp", "123456789");

    json dataArr = [];

    string messageID = "messageID";
    string appKey = messages:getProperty(m, "application_id") + ":" + messages:getProperty(m, "user_id");
    string appTier = "Unlimited";
    string subscriptionKey = messages:getProperty(m, "application_id") + ":" + messages:getProperty(m, "REQUEST_URL")
                             + ":" + messages:getProperty(m, "version");
    string apiKey = messages:getProperty(m, "REQUEST_URL") + ":" + messages:getProperty(m, "version");
    string apiTier ="";
    string subscriptionTier = "Silver";
    string resourceKey = "";
    string resourceTier = "Unlimited";
    string userId = messages:getProperty(m, "user_id");
    string apiContext = messages:getProperty(m, "REQUEST_URL");
    string apiVersion = messages:getProperty(m, "version");
    string appTenant = "carbon.super";
    string apiTenant = "carbon.super";
    string appId = messages:getProperty(m, "application_id");
    string apiName = messages:getProperty(m, "api");
    string properties = "{\"ip\":174327626}";

    jsons:add (dataArr, "$", messageID);
    jsons:add (dataArr, "$", appKey);
    jsons:add (dataArr, "$", appTier);
    jsons:add (dataArr, "$", subscriptionKey);
    jsons:add (dataArr, "$", apiKey);
    jsons:add (dataArr, "$", apiTier);
    jsons:add (dataArr, "$", subscriptionTier);
    jsons:add (dataArr, "$", resourceKey);
    jsons:add (dataArr, "$", resourceTier);
    jsons:add (dataArr, "$", userId);
    jsons:add (dataArr, "$", apiContext);
    jsons:add (dataArr, "$", apiVersion);
    jsons:add (dataArr, "$", appTenant);
    jsons:add (dataArr, "$", apiTenant);
    jsons:add (dataArr, "$", appId);
    jsons:add (dataArr, "$", apiName);
    jsons:add (dataArr, "$", properties);

    jsons:add (event, "$", "data", dataArr);

    publisher:publish(event);

}
