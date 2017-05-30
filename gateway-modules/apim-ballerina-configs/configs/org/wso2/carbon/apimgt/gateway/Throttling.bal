package org.wso2.carbon.apimgt.gateway;

import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.jsons;

import org.wso2.carbon.apimgt.gateway.holders as throttle;
import org.wso2.carbon.apimgt.gateway.utils as util;

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

    //Other Relevant parameters
    map authContext = util:getStringProperty(msg,"authCtx");
    string authorizedUser;

    //Throttle decisions
    boolean isThrottled = false;
    boolean isResourceLevelThrottled = false;
    boolean isApplicationLevelThrottled = false;
    boolean isSubscriptionLevelThrottled =false;
    boolean isSubscriptionLevelSpikeThrottled = false;
    boolean isApiLevelThrottled = false;
    boolean apiLevelThrottlingTriggered = false;
    boolean policyLevelUserTriggered = false;

    string ipLevelBlockingKey = "";
    string appLevelBlockingKey = "";
    string apiLevelBlockingKey = "";
    string userLevelBlockingKey = "";

    //todo condition implementation ConditionGroupDTO[] conditionGroupDTOs;

    //todo get these from authcontext
    json keyValidationDto = (json)messages:getProperty(msg, "KEY_VALIDATION_INFO");

    authorizedUser = jsons:getString(keyValidationDto, "username");
    //Throttle Tiers
    string applicationLevelTier = jsons:getString(keyValidationDto, "applicationTier");
    string subscriptionLevelTier = jsons:getString(keyValidationDto, "subscriptionTier");
    string apiLevelTier =  jsons:getString(keyValidationDto, "apiTier");

    string apiContext = jsons:getString(keyValidationDto, "apiContext");
    string apiVersion = jsons:getString(keyValidationDto, "apiVersion");
    string applicationId = jsons:getString(keyValidationDto, "applicationId");

    //todo get the correct key value
    string ip = util:getStringProperty(msg, "CLIENT_IP");

    apiLevelThrottleKey = apiContext + ":" + apiVersion;
    subscriptionLevelThrottleKey = applicationId + ":" + apiContext + ":" + apiVersion;

    // Blocking Condition
    boolean isBlocked = throttle:isRequestBlocked(apiLevelThrottleKey,subscriptionLevelThrottleKey,authorizedUser,ip);
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

    json resourceDto = (json)messages:getProperty(msg, "RESOURCE_INFO");
    string verb = jsons:getString(resourceDto, "verb");
    string resourceLevelTier = jsons:getString(resourceDto, "resourceLevelTier");
    string applicableLevel = jsons:getString(resourceDto, "applicableLevel");

    //Check API Level is Applied
    if (apiLevelTier != "" && apiLevelTier != "Unlimited"){
        apiLevelThrottlingTriggered = true;
        resourceLevelThrottleKey = apiLevelThrottleKey ;
    }

    //Check if verb dto is present
    //If verbInfo is present then only we will do resource level throttling
    if( verb == ""){
        system:println("Error while getting throttling information for resource and http verb");
        return false;
    }

    
    if ( resourceLevelTier == "Unlimited" && !apiLevelThrottlingTriggered) {
        //If unlimited tier throttling will not apply at resource level and pass it
         system:println("Resource level throttling set as unlimited and request will pass resource level");
    }else{
        
        if (applicableLevel == "userLevel" ) {
            resourceLevelThrottleKey = resourceLevelThrottleKey + "_" + authorizedUser;
            policyLevelUserTriggered = true;
        }

        //todo check for conditions
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
    isApiLevelThrottled = throttle:isThrottled(applicationLevelThrottleKey);

    if(isApiLevelThrottled){
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
