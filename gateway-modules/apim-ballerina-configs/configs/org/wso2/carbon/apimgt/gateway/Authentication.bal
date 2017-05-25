package org.wso2.carbon.apimgt.gateway;
import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.lang.errors;
import ballerina.utils;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.gateway.constants;
import ballerina.lang.jsons;
@http:BasePath {value:"/api1"}
service Service1 {
    dto:APIKeyValidationInfoDTO keyValidationDto;
    @http:GET {}
    @http:Path {value:"/"}
    resource Resource1 (message m) {
        int i = - 1;
        boolean valid;
        valid, m = authenticate(m);
        if(valid){
            system:println("KeyValidationInfo : " + messages:getProperty(m, constants:KEY_VALIDATION_INFO));
            http:ClientConnector client = create http:ClientConnector("http://www.mocky.io");
            message response = http:ClientConnector.get (client, "/v2/58e253c6250000fa0096ae4d", m);
            reply response;
        }else{
            reply m;
        }
    }
}
function authenticate (message m) (boolean, message) {
    message response = {};
    message request = {};
    boolean state = false;
    dto:SubscriptionDto subscriptionDto;
    dto:ResourceDto resourceDto;
    boolean introspectCacheHit = true;
    string apiContext = "/api1";
    string version = "1.0.0";
    string uriTemplate = "/*";
    string httpVerb = strings:toUpperCase(http:getMethod(m));
    //check resource exist in cache
    resourceDto = validateResource(apiContext, version, uriTemplate, httpVerb);

    if (resourceDto != null) {
        if (resourceDto.authType != constants:AUTHENTICATION_TYPE_NONE) {
            try {
                //extract key
                string authToken = messages:getHeader(m, constants:AUTHORIZATION);
                authToken = strings:replace(authToken, constants:BEARER, "");
                if (strings:length(authToken) == 0) {
                    // token incorrect
                    gatewayUtil:constructAccessTokenNotFoundPayload(response);
                    http:setStatusCode(response, 401);
                    return false, response;
                }
                //check key exist in cache
                dto:IntrospectDto introspectDto = holder:getFromTokenCache(authToken);
                if (introspectDto == null) {
                    introspectCacheHit = false;
                    // if not exist
                    introspectDto = doIntrospect(authToken);
                }

                //check token is active
                if (introspectDto.active) {
                    // if token had exp
                    if (introspectDto.exp != - 1) {
                        //put into cache
                        holder:putIntoTokenCache(authToken, introspectDto);
                    }
                    // if token come from cache hit
                    if (introspectCacheHit) {

                        if (introspectDto.exp < system:currentTimeMillis() / 1000) {
                            holder:removeFromTokenCache(authToken);
                            gatewayUtil:constructAccessTokenExpiredPayload(response);
                            return false, response;
                        }
                    }
                    // validating subscription
                    subscriptionDto = validateSubscription(apiContext, version, introspectDto);
                    if (subscriptionDto != null) {
                        if (validateScopes(resourceDto, introspectDto)) {
                            string keyValidationInfo = constructKeyValidationDto(authToken, introspectDto, subscriptionDto, resourceDto);
                            messages:setProperty(m, "KEY_VALIDATION_INFO", jsons:toString(keyValidationInfo));
                        system:println("hello");
                            state = true;
                            response = m;
                        }
                    } else {
                        //subscription missing
                        gatewayUtil:constructSubscriptionNotFound(response);
                        return false, response;
                    }
                } else {
                    // access token expired
                    gatewayUtil:constructAccessTokenExpiredPayload(response);
                    return false, response;
                }
            }
            catch (errors:Error e) {
                messages:setHeader(response, "Content-Type", "application/json");
                http:setStatusCode(response, 401);
                gatewayUtil:constructAccessTokenNotFoundPayload(response);
                return false, response;
            }
        } else {
            // set user as anonymous
            // set throttling tier as unauthenticated
        }
    } else {
        http:setStatusCode(response, 404);

    }
    return state, response;
}

function doIntrospect (string authToken) (dto:IntrospectDto) {
    message request = {};
    string keyURl = "https://localhost:9443";
    messages:setStringPayload(request, "token=" + authToken);
    messages:setHeader(request, "Content-Type", "application/x-www-form-urlencoded");
    messages:setHeader(request, constants:AUTHORIZATION, "Basic " + utils:base64encode("admin:admin"));
    http:ClientConnector introspectConnector = create http:ClientConnector(keyURl);
    message introspectResponse = http:ClientConnector.post (introspectConnector, constants:INTROSPECT_CONTEXT, request);
    dto:IntrospectDto introspectDto = gatewayUtil:fromJsonToIntrospectDto(messages:getJsonPayload(introspectResponse));
    return introspectDto;
}
function validateSubscription (string apiContext, string version, dto:IntrospectDto introspectDto) (dto:SubscriptionDto) {
    string cacheKey = apiContext + ":" + version + ":" + introspectDto.client_id;
    dto:SubscriptionDto subscriptionDto = holder:getFromSubscriptionCache(apiContext, version, introspectDto.client_id);
    return subscriptionDto;
}
function validateResource (string apiContext, string apiVersion, string uriTemplate, string verb) (dto:ResourceDto) {
    dto:ResourceDto resourceDto = holder:getFromResourceCache(apiContext, apiVersion, uriTemplate, verb);
    if (resourceDto == null) {
        //cachedKey doesn't exist in cache
        gatewayUtil:retrieveAPIInformation(apiContext, apiVersion);
        resourceDto = holder:getFromResourceCache(apiContext, apiVersion, uriTemplate, verb);
    }
    return resourceDto;
}
function validateScopes (dto:ResourceDto resourceDto, dto:IntrospectDto introspectDto) (boolean){
    boolean state = false;
    if (resourceDto.scope != "") {
        if (strings:contains(introspectDto.scope, resourceDto.scope)) {
            state = true;
        } else {
            state = false;
        }
    } else {
        state = true;
    }
    return state;
}
function constructKeyValidationDto (string token, dto:IntrospectDto introspectDto, dto:SubscriptionDto subscriptionDto, dto:ResourceDto resourceDto) (string){
    json keyValidationInfoDTO = {};
    keyValidationInfoDTO.username = introspectDto.username;
    keyValidationInfoDTO.apiKey = token;
    keyValidationInfoDTO.apiTier = subscriptionDto.subscriptionPolicy;
    keyValidationInfoDTO.applicationName = subscriptionDto.applicationName;
    keyValidationInfoDTO.consumerKey = subscriptionDto.consumerKey;
    keyValidationInfoDTO.keyType = subscriptionDto.keyEnvType;
    keyValidationInfoDTO.subscriber = subscriptionDto.applicationOwner;
    keyValidationInfoDTO.applicationId = subscriptionDto.applicationId;
    keyValidationInfoDTO.applicationTier = subscriptionDto.applicationTier;
    return jsons:toString(keyValidationInfoDTO);
}
function retrieveUserInfo () {
    
}