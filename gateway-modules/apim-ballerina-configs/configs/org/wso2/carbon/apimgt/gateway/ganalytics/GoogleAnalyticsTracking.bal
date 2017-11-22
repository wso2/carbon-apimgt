package org.wso2.carbon.apimgt.gateway.ganalytics;
import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.ballerina.util;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.gateway.constants;
import ballerina.lang.errors;
import ballerina.lang.strings;
import ballerina.net.uri;
import ballerina.utils;

errors:TypeCastError err;
dto:GAnalyticsTrackingInfoDTO gAnalyticsInfo;

function requestInterceptor (message m) (boolean, message) {
    system:println("invoking Google Analytics interceptor");

    gAnalyticsInfo = holder:getGAnalyticsTrackingConf();
    boolean isEnabled = (boolean)gAnalyticsInfo.enabled;
    if (isEnabled) {
        publishHit(m);
    }
    return true, m;
}

function publishHit (message m) (boolean, message){
    message request = {};
    message response = {};
    http:ClientConnector client = create http:ClientConnector(constants:GOOGLE_ANALYTICS_HTTP_ENDPOINT);
    string query = buildPayload(m);
    response = http:ClientConnector.get (client, query, request);
    return true, response;
}

function buildPayload (message m) (string){
    string user;
    string userIP;
    errors:Error error = null;
    dto:KeyValidationDto keyValidationDto;
    keyValidationDto, err = (dto:KeyValidationDto)util:getProperty(m, "KEY_VALIDATION_INFO");
    string trackingID = (string)gAnalyticsInfo.trackingID;
    if (keyValidationDto.username != "") {
        user = keyValidationDto.username;
    } else {
        user = constants:ANONYMOUS_USER_ID;
    }
    string clientID = utils:getHash(user, "MD5");
    string sessionControl = "end";
    string hitType = constants:GOOGLE_ANALYTICS_HIT_TYPE_PAGEVIEW;
    string host = messages:getHeader(m, constants:HTTP_HOST_HEADER);
    string documentHostName = "";
    if (strings:indexOf(host, ":") != - 1) {
        documentHostName = strings:subString(host, 0, strings:indexOf(host, ":"));
    }
    string xForwardedFor;
    xForwardedFor = messages:getHeader(m, constants:X_FORWARDED_FOR_HEADER);

    if (xForwardedFor != "") {
        string[] xForwardedForList = strings:split(xForwardedFor, ",");
        userIP = xForwardedForList[0];
    } else {
        userIP = documentHostName;
    }
    string documentPath = messages:getProperty(m, constants:BASE_PATH);
    string documentTitle = strings:toUpperCase(http:getMethod(m));
    string cacheBuster = utils:getRandomString(); //Generate random UUID as cache buster


    string payload = "?v=" + constants:GOOGLE_ANALYTICS_VERSION
                     + "&tid=" + trackingID
                     + "&cid=" + clientID
                     + "&sc=" + sessionControl
                     + "&uip=" + userIP
                     + "&t=" + constants:GOOGLE_ANALYTICS_HIT_TYPE_PAGEVIEW
                     + "&dh=" + documentHostName
                     + "&dp=" + uri:encode(documentPath)
                     + "&dt=" + documentTitle
                     + "&z=" + cacheBuster;
    return payload;
}

function responseInterceptor (message m) (boolean, message) {
    system:println("invoking response auth interceptor");
    return true, m;
}


