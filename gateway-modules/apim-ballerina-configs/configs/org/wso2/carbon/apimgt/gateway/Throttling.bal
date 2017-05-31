package org.wso2.carbon.apimgt.gateway;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;

import org.wso2.carbon.apimgt.gateway.holders as throttle;
import org.wso2.carbon.apimgt.gateway.utils as util;
import org.wso2.carbon.apimgt.gateway.constants as constants;


function doThrottling( message msg) (boolean){
    // will return true if the request is throttled

    //Throttle Keys
    //applicationLevelThrottleKey = {applicationId}:{authorizedUser}
    string applicationLevelThrottleKey;

    //subscriptionLevelThrottleKey = {applicationId}:{apiContext}:{apiVersion}
    string subscriptionLevelThrottleKey;

    // resourceLevelThrottleKey = {apiContext}/{apiVersion}{resourceUri}:{httpMethod}
    // if policy is user level then authorized user will append at end
    string resourceLevelThrottleKey;

    //apiLevelThrottleKey key = {apiContext}:{apiVersion}
    string apiLevelThrottleKey;

    string authorizedUser;

    //Throttle decisions
    boolean isApplicationLevelThrottled = false;
    boolean isSubscriptionLevelThrottled =false;
    boolean isApiLevelThrottled = false;
    boolean apiLevelThrottlingTriggered = false;

    string ipLevelBlockingKey = "";
    string appLevelBlockingKey = "";
    string apiLevelBlockingKey = "";
    string userLevelBlockingKey = "";

    json keyValidationDto = (json)messages:getProperty(msg, "KEY_VALIDATION_INFO");

    authorizedUser = util:getJsonString(keyValidationDto, "username");
    //Throttle Policies
    string applicationLevelPolicy = util:getJsonString(keyValidationDto, "applicationPolicy");
    string subscriptionLevelPolicy = util:getJsonString(keyValidationDto, "subscriptionPolicy");
    string apiLevelPolicy =  util:getJsonString(keyValidationDto, "apiLevelPolicy");

    string apiContext = util:getJsonString(keyValidationDto, "apiContext");
    string apiVersion = util:getJsonString(keyValidationDto, "apiVersion");
    string applicationId = util:getJsonString(keyValidationDto, "applicationId");

    if (authorizedUser == ""){
        http:setStatusCode( msg, constants:HTTP_UNAUTHORIZED);
        messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:BLOCKED_ERROR_CODE);
        messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_REQUEST_BLOCKED);
        setThrottledResponse(msg);
        return true;
    }

    //todo get the correct key value
    ipLevelBlockingKey = util:getStringProperty(msg, "CLIENT_IP_ADDRESS");
    apiLevelThrottleKey = apiContext + ":" + apiVersion;
    subscriptionLevelThrottleKey = applicationId + ":" + apiContext + ":" + apiVersion;

    // Blocking Condition
    boolean isBlocked = throttle:isRequestBlocked(apiLevelThrottleKey,subscriptionLevelThrottleKey,authorizedUser,ipLevelBlockingKey);
    
    if (isBlocked) {
        http:setStatusCode( msg, constants:HTTP_FORBIDDEN);
        messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:BLOCKED_ERROR_CODE);
        messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_REQUEST_BLOCKED);
        setThrottledResponse(msg);
        return true;
    }
   
    string httpMethod = util:getJsonString(keyValidationDto, "verb");
    string resourceLevelPolicy = util:getJsonString(keyValidationDto, "resourceLevelPolicy");
    string resourceUri = util:getJsonString(keyValidationDto, "resourcePath");
    resourceLevelThrottleKey = apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;

    //Check API Level is Applied
    if (apiLevelPolicy != "" && apiLevelPolicy != constants:UNLIMITED_TIER){
        apiLevelThrottlingTriggered = true;
        resourceLevelThrottleKey = apiLevelThrottleKey ;
    }

    //Check if verb dto is present
    //If verbInfo is present then only we will do resource level throttling
    if( httpMethod == ""){
        system:println("Error while getting throttling information for resource and http verb");
        return false;
    }

    
    if ( resourceLevelPolicy == constants:UNLIMITED_TIER && !apiLevelThrottlingTriggered) {
        //If unlimited Policy throttling will not apply at resource level and pass it
         system:println("Resource level throttling set as unlimited and request will pass resource level");
    }else{

        // todo check for conditions
        // resource level + API level condition checking
        if (throttle:isThrottled(resourceLevelThrottleKey)) {

            if(apiLevelThrottlingTriggered){
                messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:API_THROTTLE_OUT_ERROR_CODE);
                messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_API_LIMIT_EXCEEDED);
            }else{
                messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:RESOURCE_THROTTLE_OUT_ERROR_CODE);
                messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_RESOURCE_LIMIT_EXCEEDED);
            }

            http:setStatusCode( msg, constants:HTTP_TOO_MANY_REQUESTS);
            setThrottledResponse(msg);

            return true;
        }
    }

    // Subscription Level throttling
    isSubscriptionLevelThrottled = throttle:isThrottled(subscriptionLevelThrottleKey);

    if(isSubscriptionLevelThrottled){
        http:setStatusCode( msg, constants:HTTP_TOO_MANY_REQUESTS );
        messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE);
        messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_SUBSCRIPTION_LIMIT_EXCEEDED);
        setThrottledResponse(msg);
        return true;
    }

    //TODO Spike Arrest

    // Application Level Throttling
    applicationLevelThrottleKey = applicationId + ":" + authorizedUser;
    isApplicationLevelThrottled = throttle:isThrottled(applicationLevelThrottleKey);

    if(isApplicationLevelThrottled){
        http:setStatusCode( msg, constants:HTTP_TOO_MANY_REQUESTS );
        messages:setProperty(msg, constants:THROTTLED_ERROR_CODE, constants:APPLICATION_THROTTLE_OUT_ERROR_CODE);
        messages:setProperty(msg, constants:THROTTLED_OUT_REASON, constants:THROTTLE_OUT_REASON_APPLICATION_LIMIT_EXCEEDED);
        setThrottledResponse(msg);
        return true;
    }

    //todo data publisher to CEP

    return false;
}

function setThrottledResponse(message msg){
    json jsonPayload = {};
    jsonPayload.Error_Code =(string)messages:getProperty(msg, constants:THROTTLED_ERROR_CODE);
    jsonPayload.Error_Message = (string)messages:getProperty(msg, constants:THROTTLED_OUT_REASON);
    messages:setJsonPayload(msg, jsonPayload);
}

function setInvalidUser(message msg){
    messages:setStringPayload(msg, "API is Throttled Out");
}
