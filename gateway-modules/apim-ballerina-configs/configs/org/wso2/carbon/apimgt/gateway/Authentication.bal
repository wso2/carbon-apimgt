package org.wso2.carbon.apimgt.gateway;
import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.utils;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.holders as holder;
@http:BasePath{value: "/api"}
service Service1 {
    dto:APIKeyValidationInfoDTO keyValidationDto;
    @http:GET{}
    @http:Path{value: "/"}
    resource Resource1( message m) {
        message var1 = authenticate(m);
        reply var1;
    }
}
function authenticate( message m)( message ) {
    message response={};
    message request={};

    try{
    //extract key
        string authToken = messages:getHeader(m, "Authorization");
        authToken = strings:replace(authToken, "Bearer ","");
    //check key exist in cache
        dto:IntrospectDto introspectDto = holder:getFromTokenCache(authToken);
           if(introspectDto == null){
               // if not exist
               if(strings:length(authToken) != 0){
                   introspectDto = doIntrospect(m,authToken);
               //put into cache
                    holder:putIntoTokenCache(authToken,introspectDto);
               }else{
                   gatewayUtil:constructAccessTokenNotFoundPayload(response);
                   http:setStatusCode(response,401);
               }
           }
    //check token is active
        if(introspectDto.active){
            // validating subscription
            dto:SubscriptionDto subscriptionDto = validateSubscription(introspectDto);
            if(subscriptionDto != null){
                //if subscription is not null
            }
        }else{
            gatewayUtil:constructAccessTokenExpiredPayload(response);
            return response;
        }
        return response;
    }catch(exception e){
        messages:setHeader(request, "Content-Type", "application/json");
        http:setStatusCode(response,401);
        gatewayUtil:constructAccessTokenNotFoundPayload(response);
        return response;
    }
}
function doIntrospect (message m,string authToken)(dto:IntrospectDto) {
    message request={};
    string keyURl = "https://localhost:9443/oauth2";
    messages:setStringPayload(request,"token=" + authToken);
    messages:setHeader(request, "Content-Type", "application/x-www-form-urlencoded");
    messages:setHeader(request, "Authorization", "Basic "+utils:base64encode("admin:admin"));
    http:ClientConnector introspectConnector = create http:ClientConnector(keyURl);
    message introspectResponse = http:ClientConnector.post(introspectConnector,"/introspect",request);
    dto:IntrospectDto introspectDto = gatewayUtil:fromJsonToIntrospectDto(messages:getJsonPayload(introspectResponse));
    return introspectDto;
}
function validateSubscription (dto:IntrospectDto introspectDto)(dto:SubscriptionDto) {
    //todo: need to read from messages:getProperty(m,"")
    string apiContext = "/api1";
    string version = "1.0.0";
    string cacheKey = apiContext+":"+version+":"+introspectDto.client_id;
    dto:SubscriptionDto subscriptionDto = holder:getFromSubscriptionCache(cacheKey);
    if(subscriptionDto == null){
        message response = {};
        gatewayUtil:constructAccessTokenNotFoundPayload(response);
    }
    return subscriptionDto;
}
function validateResource (dto:SubscriptionDto subscriptionDto)(dto:ResourceDto) {
    string cachedKey = subscriptionDto.apiContext+":"+subscriptionDto.apiVersion+"/*:GET";
    dto:ResourceDto resourceDto = holder:getFromResourceCache(cachedKey);
    if(resourceDto != null){
    }
}