package org.wso2.carbon.apimgt.gateway.auth;
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
import org.wso2.carbon.apimgt.ballerina.util;

function main(string[] args) {
    system:println("Hello, World!");
}

function requestInterceptor (message m) (boolean, message) {
    system:println("invoking auth interceptor");
    message res;
    boolean authenticated;
    authenticated, res = authenticate(m);
    system:println("auth authenticated " + authenticated);
    return authenticated, res;
}

function responseInterceptor (message m) (boolean, message) {
    system:println("invoking response auth interceptor");
    return true, m;
}

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
    string uriTemplate = "/";
    string httpVerb = strings:toUpperCase(http:getMethod(m));

    string authHeader;
    string apikeyHeader;
    errors:Error authErr = null;
    errors:Error apikeyErr = null;

    //check api status
    string apiIdentifier = apiContext + ":" + version;
    system:println("[API-GW] Checking API available in cache");
    dto:APIDTO apiDto = holder:getFromAPICache(apiIdentifier);

    if (apiDto == null){
        system:println("[API-GW] API not found in cache");
        http:setStatusCode(response, 404);
        return false, response;
    }
    system:println("[API-GW] API is available in cache");
    if (constants:MAINTENANCE == apiDto.lifeCycleStatus) {
        system:println("[API-GW] API is in MAINTENANCE state");
        gatewayUtil:constructAPIIsInMaintenance(response);
        return false, response;
    }

    system:println("[API-GW] Validating resources");
    resourceDto = validateResource(apiContext, version, uriTemplate, httpVerb);
    
    if (resourceDto == null) {
        system:println("[API-GW] Resource validation failed");
        http:setStatusCode(response, 404);
        return false, response;
    }
    system:println("[API-GW] Validating resources : success");
    if (resourceDto.authType == constants:AUTHENTICATION_TYPE_NONE) {
        system:println("[API-GW] API's Auth Type is NONE");
        // set user as anonymous
        // set throttling tier as unauthenticated
    }
    system:println("[API-GW] Security Scheme : " + apiDto.securityScheme);
    if (apiDto.securityScheme == 0) {
        //pass request without authentication
        //and return method
    }

    authHeader, authErr = extractHeaderWithName(constants:AUTHORIZATION, m);
    apikeyHeader, apikeyErr = extractHeaderWithName("apikey", m);

    if (authErr != null && apikeyErr != null) {
        http:setStatusCode(response, 400);
        return false, response;
    }
    system:println("[API-GW] Processing Security Schemes");
    if (authErr == null && ((apiDto.securityScheme == 1) || (apiDto.securityScheme == 3) 
            || (apiDto.securityScheme == -1))) {
        string authToken = strings:replace(authHeader, constants:BEARER, ""); //use split method instead
        system:println("[API-GW] Auth Type is OAuth");
        if (strings:length(authToken) == 0) {
            system:println("[API-GW] Access token not found");
            // token incorrect
            gatewayUtil:constructAccessTokenNotFoundPayload(response);
            http:setStatusCode(response, 401);
            return false, response;
        }
        system:println("[API-GW] Retrieving OAuth token info from cache");
        dto:IntrospectDto introspectDto = holder:getFromTokenCache(authToken);
        if (introspectDto == null) {
            system:println("[API-GW] OAuth token cache miss");
            introspectCacheHit = false;
            system:println("[API-GW] Introspecting token");
            // if not exist
            introspectDto = doIntrospect(authToken);
        }

        if (!introspectDto.active) {
            system:println("[API-GW] OAuth token is not active");
            // access token expired
            gatewayUtil:constructAccessTokenExpiredPayload(response);
            return false, response;
        }
        system:println("[API-GW] OAuth token is active");
        // if token had exp
        if (introspectDto.exp != -1) {
            //put into cache
            holder:putIntoTokenCache(authToken, introspectDto);
        }
        if (introspectDto.username != "") {
            userInfo = holder:getFromUserInfoCache(introspectDto.username);
            if ((userInfo == null) && (introspectDto.scope != "")
                && (strings:contains(introspectDto.scope, "openid"))) {
                userInfo = retrieveUserInfo(authToken);
                holder:putIntoUserInfoCache(introspectDto.username, userInfo);
            }
        }
        // if token come from cache hit
        if (introspectCacheHit) {
            if (introspectDto.exp < system:currentTimeMillis() / 1000) {
                system:println("[API-GW] introspectCacheHit token expired");
                holder:removeFromTokenCache(authToken);
                gatewayUtil:constructAccessTokenExpiredPayload(response);
                return false, response;
            }
        }
        system:println("[API-GW] validating subscription");
        // validating subscription
        subscriptionDto = validateSubscription(apiContext, version, introspectDto);

        if (subscriptionDto != null) {
            boolean subscriptionBlocked = false;
            system:println("[API-GW] Checking subscription blocked");
            subscriptionBlocked, response = isSubscriptionBlocked(subscriptionDto, response, apiDto);
            if (subscriptionBlocked) {
                system:println("[API-GW] Subscription is blocked");
                state = false;
                return state, response;
            }
            system:println("[API-GW] Validating subscription : success");
            system:println("[API-GW] Validating scopes");
            if (validateScopes(resourceDto, introspectDto)) {
                system:println("[API-GW] Validating scopes : success");
                dto:KeyValidationDto keyValidationInfo =
                constructKeyValidationDto(authToken, introspectDto, subscriptionDto, resourceDto);
                util:setProperty(m, "KEY_VALIDATION_INFO", keyValidationInfo);
                messages:setProperty(m, constants:KEY_TYPE, subscriptionDto.keyEnvType);
                state = true;
                response = m;
            } else {
                system:println("[API-GW] Validating scopes : failed");
            }
        } else {
            system:println("[API-GW] Subscription not found");
            //subscription missing
            gatewayUtil:constructSubscriptionNotFound(response);
            return false, response;
        }

    } else if (apikeyErr == null && ((apiDto.securityScheme == 2) || (apiDto.securityScheme == 3))) {
        system:println("Api key check..");
        string apiKey = apikeyHeader;
        subscriptionDto = holder:getFromSubscriptionCache(apiContext, version, apiKey);
        if (subscriptionDto != null) {
            boolean subscriptionBlocked = false;
            subscriptionBlocked, response = isSubscriptionBlocked(subscriptionDto, response, apiDto);
            if (subscriptionBlocked) {
                state = false;
                return state, response;
            }
            //scope validation does not apply for apikey
            dto:KeyValidationDto keyValidationInfo =
            constructAPIKeyValidationDto(subscriptionDto, resourceDto);
            util:setProperty(m, "KEY_VALIDATION_INFO", keyValidationInfo);
            messages:setProperty(m, constants:KEY_TYPE, subscriptionDto.keyEnvType);
            state = true;
            response = m;
        } else {
            gatewayUtil:constructSubscriptionNotFound(response);
            return false, response;
        }
    } else {
        system:println("[API-GW] No security schemes matched for the API");
    }

    return state, response;
}

function doIntrospect (string authToken) (dto:IntrospectDto) {
    message request = {};
    dto:KeyManagerInfoDTO keyManagerConf = holder:getKeyManagerConf();
    dto:CredentialsDTO credentials = keyManagerConf.credentials;
    messages:setStringPayload(request, "token=" + authToken);
    messages:setHeader(request, "Content-Type", "application/x-www-form-urlencoded");
    messages:setHeader(request, constants:AUTHORIZATION, "Basic " + utils:base64encode(credentials.username + ":"
                                                                                       + credentials.password));
    http:ClientConnector introspectConnector = create http:ClientConnector(keyManagerConf.introspectEndpoint);
    message introspectResponse = introspectConnector.post("/", request);
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

function constructAPIKeyValidationDto (dto:SubscriptionDto subscriptionDto, dto:ResourceDto resourceDto) (dto:KeyValidationDto ) {
    dto:KeyValidationDto keyValidationInfoDTO = {};
    dto:ApplicationDto applicationDto = holder:getFromApplicationCache(subscriptionDto.applicationId);
    keyValidationInfoDTO.username = applicationDto.applicationOwner;
    dto:PolicyDto applicationPolicy = holder:getFromPolicyCache(applicationDto.applicationPolicy);
    keyValidationInfoDTO.applicationPolicy = applicationPolicy.name;
    dto:PolicyDto subscriptionPolicy = holder:getFromPolicyCache(subscriptionDto.subscriptionPolicy);
    keyValidationInfoDTO.subscriptionPolicy = subscriptionPolicy.name;
    keyValidationInfoDTO.stopOnQuotaReach = subscriptionPolicy.stopOnQuotaReach;
    dto:PolicyDto apiLevelPolicy = holder:getFromPolicyCache(subscriptionDto.apiLevelPolicy);
    keyValidationInfoDTO.apiLevelPolicy = subscriptionDto.apiLevelPolicy;
    dto:PolicyDto resourceLevelPolicy = holder:getFromPolicyCache(resourceDto.policy);
    keyValidationInfoDTO.resourceLevelPolicy = resourceLevelPolicy.name;
    keyValidationInfoDTO.verb = resourceDto.httpVerb;
    keyValidationInfoDTO.apiName = subscriptionDto.apiName;
    keyValidationInfoDTO.apiProvider = subscriptionDto.apiProvider;
    keyValidationInfoDTO.apiContext = subscriptionDto.apiContext;
    keyValidationInfoDTO.apiVersion = subscriptionDto.apiVersion;
    keyValidationInfoDTO.applicationId = subscriptionDto.applicationId;
    keyValidationInfoDTO.applicationName = applicationDto.applicationName;
    keyValidationInfoDTO.keyType = subscriptionDto.keyEnvType;
    keyValidationInfoDTO.subscriber = applicationDto.applicationOwner;
    keyValidationInfoDTO.resourcePath = resourceDto.uriTemplate;
    return keyValidationInfoDTO;
}

function constructKeyValidationDto (string token, dto:IntrospectDto introspectDto, dto:SubscriptionDto subscriptionDto,
                                    dto:ResourceDto resourceDto) (dto:KeyValidationDto ){
    dto:KeyValidationDto keyValidationInfoDTO = {};
    dto:ApplicationDto applicationDto = holder:getFromApplicationCache(subscriptionDto.applicationId);
    keyValidationInfoDTO.username = introspectDto.username;
    dto:PolicyDto applicationPolicy = holder:getFromPolicyCache(applicationDto.applicationPolicy);
    keyValidationInfoDTO.applicationPolicy = applicationPolicy.name;
    dto:PolicyDto subscriptionPolicy = holder:getFromPolicyCache(subscriptionDto.subscriptionPolicy);
    keyValidationInfoDTO.subscriptionPolicy = subscriptionPolicy.name;
    keyValidationInfoDTO.stopOnQuotaReach = subscriptionPolicy.stopOnQuotaReach;
    dto:PolicyDto apiLevelPolicy = holder:getFromPolicyCache(subscriptionDto.apiLevelPolicy);
    keyValidationInfoDTO.apiLevelPolicy = subscriptionDto.apiLevelPolicy;
    dto:PolicyDto resourceLevelPolicy = holder:getFromPolicyCache(resourceDto.policy);
    keyValidationInfoDTO.resourceLevelPolicy = resourceLevelPolicy.name;
    keyValidationInfoDTO.verb = resourceDto.httpVerb;
    keyValidationInfoDTO.apiName = subscriptionDto.apiName;
    keyValidationInfoDTO.apiProvider = subscriptionDto.apiProvider;
    keyValidationInfoDTO.apiContext = subscriptionDto.apiContext;
    keyValidationInfoDTO.apiVersion = subscriptionDto.apiVersion;
    keyValidationInfoDTO.applicationId = subscriptionDto.applicationId;
    keyValidationInfoDTO.applicationName = applicationDto.applicationName;
    keyValidationInfoDTO.keyType = subscriptionDto.keyEnvType;
    keyValidationInfoDTO.subscriber = applicationDto.applicationOwner;
    keyValidationInfoDTO.resourcePath = resourceDto.uriTemplate;
    return keyValidationInfoDTO;
}
function retrieveUserInfo (string token) (json) {
    message request = {};
    string keyURl = "https://localhost:9443";
    messages:setHeader(request, "Content-Type", "application/json");
    messages:setHeader(request, constants:AUTHORIZATION, "Bearer " + token);
    http:ClientConnector userInfoConnector = create http:ClientConnector(keyURl);
    message userInfoResponse = userInfoConnector.post (constants:USER_INFO_CONTEXT
                                                                             + "?schema=openid", request);
    return messages:getJsonPayload(userInfoResponse);
}

function isSubscriptionBlocked (dto:SubscriptionDto subscriptionDto, message response, dto:APIDTO apiDto)
                                                                                                (boolean, message) {
    if (subscriptionDto.status == constants:SUBSCRIPTION_STATUS_BLOCKED) {
        gatewayUtil:constructSubscriptionBlocked(response, apiDto.context, apiDto.version);
        return true, response;
    } else if ((subscriptionDto.status == constants:SUBSCRIPTION_STATUS_PROD_ONLY_BLOCKED) &&
    (subscriptionDto.keyEnvType == constants:ENV_TYPE_PRODUCTION)) {
            gatewayUtil:constructSubscriptionBlocked(response, apiDto.context, apiDto.version);
            return true, response;
    } else if ((subscriptionDto.status == constants:SUBSCRIPTION_STATUS_SANDBOX_ONLY_BLOCKED)
      && (subscriptionDto.keyEnvType == constants:ENV_TYPE_SANDBOX)) {
            gatewayUtil:constructSubscriptionBlocked(response, apiDto.context, apiDto.version);
            return true, response;
    }
    return false, response;
}

function extractHeaderWithName (string headerName, message msg) (string, errors:Error) {
    string headerValue = "";
    errors:Error err = null;
    try {
        headerValue = messages:getHeader(msg, headerName);
    } catch (errors:Error err){
        headerValue = "";
        return headerValue, err;
    }
    return headerValue, err;
}
