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
function authenticate (message m) (boolean, message) {
    message response = {};
    message request = {};
    boolean state = false;
    dto:SubscriptionDto subscriptionDto;
    dto:ResourceDto resourceDto;
    json userInfo;
    boolean introspectCacheHit = true;
    string apiContext = messages:getProperty(m,constants:BASE_PATH);
    //todo get this from ballerina property once they set versioning
    string version = "1.0.0";
    //todo need to have elected resource to be in properties
    string uriTemplate = "/*";
    string httpVerb = strings:toUpperCase(http:getMethod(m));
    //check api status
    string apiKey = apiContext + ":" + version;
    dto:APIDTO apiDto = holder:getFromAPICache(apiKey);

    if (apiDto != null) {
        if (constants:MAINTENANCE == apiDto.lifeCycleStatus) {
            gatewayUtil:constructAPIIsInMaintenance(response);
            return false, response;
        }

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
                        if (introspectDto.username != "") {
                            userInfo = holder:getFromUserInfoCache(introspectDto.username);
                            if ((userInfo == null) && (introspectDto.scope != "") && (strings:contains(introspectDto.scope, "openid"))) {
                                userInfo = retrieveUserInfo(authToken);
                                holder:putIntoUserInfoCache(introspectDto.username, userInfo);
                            }
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
                                messages:setProperty(m, constants:KEY_TYPE, subscriptionDto.keyEnvType);
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
    } else {
        http:setStatusCode(response, 404);
    }
    return state, response;
}

function doIntrospect (string authToken) (dto:IntrospectDto) {
    message request = {};
    dto:KeyManagerInfoDTO keyManagerConf = holder:keyManagerConf;
    dto:CredentialsDTO credentials = keyManagerConf.credentials;
    messages:setStringPayload(request, "token=" + authToken);
    messages:setHeader(request, "Content-Type", "application/x-www-form-urlencoded");
    messages:setHeader(request, constants:AUTHORIZATION, "Basic " + utils:base64encode(credentials.username + ":" + credentials.password));
    http:ClientConnector introspectConnector = create http:ClientConnector(keyManagerConf.introspectEndpoint);
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
        gatewayUtil:retrieveResources(apiContext, apiVersion);
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
    dto:ApplicationDto applicationDto = holder:getFromApplicationCache(subscriptionDto.applicationId);
    keyValidationInfoDTO.username = introspectDto.username;
    keyValidationInfoDTO.applicationPolicy = applicationDto.applicationPolicy;
    keyValidationInfoDTO.subscriptionPolicy = subscriptionDto.subscriptionPolicy;
    keyValidationInfoDTO.apiLevelPolicy = subscriptionDto.apiLevelPolicy;
    keyValidationInfoDTO.resourceLevelPolicy = resourceDto.policy;
    keyValidationInfoDTO.verb = resourceDto.httpVerb;
    keyValidationInfoDTO.apiName = subscriptionDto.apiName;
    keyValidationInfoDTO.apiProvider = subscriptionDto.apiProvider;
    keyValidationInfoDTO.apiContext = subscriptionDto.apiContext;
    keyValidationInfoDTO.apiVersion = subscriptionDto.apiVersion;
    keyValidationInfoDTO.applicationId = subscriptionDto.applicationId;
    keyValidationInfoDTO.apiKey = token;
    keyValidationInfoDTO.applicationName = applicationDto.applicationName;
    keyValidationInfoDTO.consumerKey = subscriptionDto.consumerKey;
    keyValidationInfoDTO.keyType = subscriptionDto.keyEnvType;
    keyValidationInfoDTO.subscriber = applicationDto.applicationOwner;
    keyValidationInfoDTO.resourcePath = resourceDto.uriTemplate;
    return jsons:toString(keyValidationInfoDTO);
}
function retrieveUserInfo (string token) (json) {
    message request = {};
    string keyURl = "https://localhost:9443";
    messages:setHeader(request, "Content-Type", "application/json");
    messages:setHeader(request, constants:AUTHORIZATION, "Bearer " + token);
    http:ClientConnector userInfoConnector = create http:ClientConnector(keyURl);
    message userInfoResponse = http:ClientConnector.post (userInfoConnector, constants:USER_INFO_CONTEXT + "?schema=openid", request);
    return messages:getJsonPayload(userInfoResponse);
}