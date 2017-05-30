package org.wso2.carbon.apimgt.gateway;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;

import org.wso2.carbon.apimgt.gateway.holders as throttle;

function doThrottling( message msg) (boolean){

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

    authorizedUser = getJsonString(keyValidationDto, "username");
    //Throttle Policies
    string applicationLevelPolicy = getJsonString(keyValidationDto, "applicationPolicy");
    string subscriptionLevelPolicy = getJsonString(keyValidationDto, "subscriptionPolicy");
    string apiLevelPolicy =  getJsonString(keyValidationDto, "apiLevelPolicy");

    string apiContext = getJsonString(keyValidationDto, "apiContext");
    string apiVersion = getJsonString(keyValidationDto, "apiVersion");
    string applicationId = getJsonString(keyValidationDto, "applicationId");

    //todo get the correct key value
    ipLevelBlockingKey = getStringProperty(msg, "CLIENT_IP_ADDRESS");
    apiLevelThrottleKey = apiContext + ":" + apiVersion;
    subscriptionLevelThrottleKey = applicationId + ":" + apiContext + ":" + apiVersion;

    // Blocking Condition
    boolean isBlocked = throttle:isRequestBlocked(apiLevelThrottleKey,subscriptionLevelThrottleKey,authorizedUser,ipLevelBlockingKey);
    // strings:equalsIgnoreCase(apiContext,"")
    if (apiContext == ""){
        http:setStatusCode( msg, 400 );
        messages:setStringPayload (msg,"Meesage Blocked please contact Administrator");
        system:println ("Context is Null....");
        return true;
    }
    
    if (isBlocked) {
        system:println ("Request Blocked....");
        SetAPIBlockedResponse(msg);
        return true;
    }
   
    string httpMethod = getJsonString(keyValidationDto, "verb");
    string resourceLevelPolicy = getJsonString(keyValidationDto, "resourceLevelPolicy");
    string resourceUri = getJsonString(keyValidationDto, "resourcePath");
    resourceLevelThrottleKey = apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;

    //Check API Level is Applied
    if (apiLevelPolicy != "" && apiLevelPolicy != "Unlimited"){
        apiLevelThrottlingTriggered = true;
        resourceLevelThrottleKey = apiLevelThrottleKey ;
    }

    //Check if verb dto is present
    //If verbInfo is present then only we will do resource level throttling
    if( httpMethod == ""){
        system:println("Error while getting throttling information for resource and http verb");
        return false;
    }

    
    if ( resourceLevelPolicy == "Unlimited" && !apiLevelThrottlingTriggered) {
        //If unlimited Policy throttling will not apply at resource level and pass it
         system:println("Resource level throttling set as unlimited and request will pass resource level");
    }else{

        // todo check for conditions
        // resource level condition checking
        if (throttle:isThrottled(resourceLevelThrottleKey)) {
            setAPIThrottledResponse(msg);
            return true;
        }
    }

    // IF no blocking conditions / API LEVEL / Resource Level
    // Subscription Level throttling
    isSubscriptionLevelThrottled = throttle:isThrottled(subscriptionLevelThrottleKey);

    if(isSubscriptionLevelThrottled){
        setAPIThrottledResponse(msg);
        return true;
    }

    //TODO Spike Arrest

    // Application Level Throttling
    applicationLevelThrottleKey = applicationId + ":" + authorizedUser;
    isApplicationLevelThrottled = throttle:isThrottled(applicationLevelThrottleKey);

    if(isApplicationLevelThrottled){
        setAPIThrottledResponse(msg);
        return true;
    }

    //todo data publisher to CEP

    return false;
}

function SetAPIBlockedResponse(message msg){
    http:setStatusCode( msg, 403 );
    messages:setStringPayload(msg, "API Is Blocked Please Contact Administrator");
}

function setAPIThrottledResponse(message msg){
    messages:setStringPayload(msg, "API is Throttled Out");
}

function setInvalidUser(message msg){
    messages:setStringPayload(msg, "API is Throttled Out");
}
