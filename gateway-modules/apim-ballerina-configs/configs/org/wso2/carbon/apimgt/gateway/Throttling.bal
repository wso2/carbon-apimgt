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

    //Throttle Tiers
    string applicationLevelTier;
    string subscriptionLevelTier;
    string resourceLevelTier = "";
    string apiLevelTier;

    //Other Relevant parameters
    map authContext = messages:getProperty(msg,"authCtx");
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

    //todo
    boolean stopOnQuotaReach = true;

    //todo ConditionGroupDTO[] conditionGroupDTOs;

    //todo get these from authcontext
    authorizedUser = messages:getProperty(msg,"authorizedUser");
    //Throttle Tiers
    applicationLevelTier = messages:getProperty(msg,"APPLICATION_TIER");
    subscriptionLevelTier = messages:getProperty(msg,"SUBSCRIPTION_TIER");
    apiLevelTier =  messages:getProperty(msg,"API_TIER");

    string apiContext = messages:getProperty(msg,"REST_API_CONTEXT");
    string apiVersion = messages:getProperty(msg,"REST_API_VERSION");
    string applicationId = messages:getProperty(msg,"APPLICATION_ID");

    // Blocking Condition
    boolean isBlocked = throttle:isRequestBlocked(apiContext,apiContext,apiContext,apiContext);
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

    string verb = messages:getProperty(msg,"verb");
    string throttling = messages:getProperty(msg,"throttling");
    string applicableLevel = messages:getProperty(msg,"applicableLevel");
    apiLevelThrottleKey = apiContext + ":" + apiVersion;

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

    
    if ( throttling == "Unlimited" && !apiLevelThrottlingTriggered) {
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
    subscriptionLevelThrottleKey = applicationId + ":" + apiContext + ":" + apiVersion;
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

function SetProperties( message msg) {
    http:convertToResponse(msg);
    messages:setStringPayload(msg, "sasasasasas");
    messages:setHeader(msg, "sasasasasas", "xxxxxxxxxxxxxxxxxxxx");
    messages:setProperty(msg, "name", "Test");
    messages:setProperty(msg, "REST_API_CONTEXT", "/test");
    messages:setProperty(msg, "REST_API_VERSION", "1.0.0");
    messages:setProperty(msg, "subscriber", "admin");
    messages:setProperty(msg, "authorizedUser", "admin");
    messages:setProperty(msg, "APPLICATION_ID", "001");
    messages:setProperty(msg, "APPLICATION_TIER", "Unlimited");
    messages:setProperty(msg, "SUBSCRIPTION_TIER", "Unlimited");
    messages:setProperty(msg, "API_TIER", "Unlimited");
    messages:setProperty(msg, "authType", "user");
    messages:setProperty(msg, "verb", "POST");
    messages:setProperty(msg, "throttling", "Unlimited");
    messages:setProperty(msg, "applicableLevel", "user");
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
