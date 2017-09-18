package org.wso2.carbon.apimgt.gateway.throttle;

import ballerina.lang.messages;

const string THROTTLED_OUT_REASON = "THROTTLE_OUT_REASON";
const string THROTTLED_ERROR_CODE = "THROTTLED_ERROR_CODE";
const string HTTP_ERROR_CODE = "THROTTLED_ERROR_CODE";
const string THROTTLE_EXPIRE_TIME = "THROTTLE_EXPIRE_TIME";

const string THROTTLE_OUT_REASON_API_LIMIT_EXCEEDED = "API Limit Exceeded";
const string THROTTLE_OUT_REASON_RESOURCE_LIMIT_EXCEEDED = "Resource Limit Exceeded";
const string THROTTLE_OUT_REASON_APPLICATION_LIMIT_EXCEEDED = "Application Limit Exceeded";
const string THROTTLE_OUT_REASON_SUBSCRIPTION_LIMIT_EXCEEDED = "Subscription Limit Exceeded";
const string THROTTLE_OUT_REASON_REQUEST_BLOCKED = "Request is Blocked by administrator";

const string API_THROTTLE_OUT_ERROR_CODE = "900800";
const string RESOURCE_THROTTLE_OUT_ERROR_CODE = "900802";
const string APPLICATION_THROTTLE_OUT_ERROR_CODE = "900803";
const string SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE = "900804";
const string BLOCKED_ERROR_CODE = "900805";
const int HTTP_FORBIDDEN = 403;
const int HTTP_TOO_MANY_REQUESTS = 429;
const int HTTP_UNAUTHORIZED = 401;

function setThrottledResponse(message msg){
    json jsonPayload = {};
    jsonPayload.Error_Code =(string)messages:getProperty(msg, THROTTLED_ERROR_CODE);
    jsonPayload.Error_Message = (string)messages:getProperty(msg, THROTTLED_OUT_REASON);
    jsonPayload.Expiry_Time = (string)messages:getProperty(msg, THROTTLE_EXPIRE_TIME);
    messages:setJsonPayload(msg, jsonPayload);
}

