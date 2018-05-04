import ballerina/http;
import ballerina/log;
import ballerina/cache;
import ballerina/config;
import org.wso2.carbon.apimgt.gateway.constants as constants;
import org.wso2.carbon.apimgt.gateway.utils as utils;
import org.wso2.carbon.apimgt.gateway.caching as caching;
import org.wso2.carbon.apimgt.gateway.dto as dto;

@Description { value: "Representation of the Throttle filter" }
@Field { value: "filterRequest: request filter method which attempts to throttle the request" }
@Field { value: "filterRequest: response filter method (not used this scenario)" }
public type ThrottleFilter object {

    @Description { value: "Filter function implementation which tries to throttle the request" }
    @Param { value: "request: Request instance" }
    @Param { value: "context: FilterContext instance" }
    @Return { value: "FilterResult: Authorization result to indicate if the request can proceed or not" }
    public function filterRequest(http:Request request, http:FilterContext context) returns http:FilterResult {
        http:FilterResult requestFilterResult;
        if (context.attributes.hasKey(constants:KEY_VALIDATION_RESPONSE)){
            if (isRquestBlocked(request, context)){
                // request Blocked
                requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
            } else {
                // Request not blocked go to check throttling
                dto:APIKeyValidationDto keyvalidationResult = check <dto:APIKeyValidationDto>context.attributes[
                constants:KEY_VALIDATION_RESPONSE];
                boolean apiLevelThrottled = isApiLevelThrottled(keyvalidationResult);
                boolean resourceLevelThrottled = isResourceLevelThrottled(keyvalidationResult);
                if (!apiLevelThrottled){
                    if (!resourceLevelThrottled){
                        if (!isSubscriptionLevelThrottled(context, keyvalidationResult)){
                            if (!isApplicationLevelThrottled(keyvalidationResult)){
                                if (!isHardlimitThrottled(utils:getContext(context), utils:getVersionFromBasePath(utils:
                                        getContext(context)))){
                                    // Send Throttle Event
                                    dto:RequestStream throttleEvent = generateThrottleEvent(request, context,
                                        keyvalidationResult);
                                    utils:publishNonThrottleEvent(throttleEvent);
                                }
                            } else {
                                // Application Level Throttled
                            }
                        } else {
                            // Subscription Level Throttled
                        }

                    } else {

                    }
                } else {

                }

            }
        }
        return requestFilterResult;
    }
};
function isRquestBlocked(http:Request request, http:FilterContext context) returns (boolean) {
    dto:APIKeyValidationDto keyvalidationResult = check <dto:APIKeyValidationDto>context.attributes[constants:
    KEY_VALIDATION_RESPONSE];
    string apiLevelBlockingKey = utils:getContext(context);
    string ipLevelBlockingKey = constants:SUPER_TENANT_DOMAIN_NAME + ":" + utils:getClientIp(request);
    string appLevelBlockingKey = keyvalidationResult.subscriber + ":" + keyvalidationResult.applicationName;
    if (utils:isBlockConditionExist(apiLevelBlockingKey) || utils:isBlockConditionExist(ipLevelBlockingKey) || utils:
        isBlockConditionExist(appLevelBlockingKey)){
        return true;
    } else {
        return false;
    }
}

function isApiLevelThrottled(dto:APIKeyValidationDto keyValidationDto) returns (boolean) {
    return false;
}

function isResourceLevelThrottled(dto:
                                  APIKeyValidationDto keyValidationDto) returns (boolean) {
    return false;
}

function isHardlimitThrottled(string context, string apiVersion) returns (boolean) {

    return false;
}


function isSubscriptionLevelThrottled(http:FilterContext context, dto:APIKeyValidationDto keyValidationDto) returns (
            boolean) {
    string subscriptionLevelThrottleKey = keyValidationDto.applicationId + ":" + utils:getContext
        (context) + ":" + utils:getVersionFromBasePath(utils:getContext(context));
    if (utils:isThrottled(subscriptionLevelThrottleKey)){
        return true;
    } else {
        return false;
    }
}

function isApplicationLevelThrottled(dto:APIKeyValidationDto keyValidationDto) returns (boolean) {
    string applicationLevelThrottleKey = keyValidationDto.applicationId + ":" + keyValidationDto.endUserName;
    if (utils:isThrottled(applicationLevelThrottleKey)){
        return true;
    } else {
        return false;
    }
}
function generateThrottleEvent(http:Request req, http:FilterContext context, dto:APIKeyValidationDto keyValidationDto)
             returns (dto:
                     RequestStream) {
    dto:RequestStream requestStream;
    requestStream.apiKey = utils:getContext(context) + ":" + utils:getVersionFromBasePath(utils:getContext(context));
    requestStream.appKey = keyValidationDto.applicationId + ":" + keyValidationDto.endUserName;
    requestStream.subscriptionKey = keyValidationDto.applicationId + ":" + utils:getContext(context) + ":" + utils:
        getVersionFromBasePath(utils:getContext(context));
    requestStream.appTier = keyValidationDto.applicationTier;
    requestStream.apiTier = keyValidationDto.apiTier;
    requestStream.subscriptionTier = keyValidationDto.tier;
    requestStream.resourceKey = "";
    requestStream.resourceTier = "Unlimited";
    requestStream.userId = keyValidationDto.endUserName;
    requestStream.apiContext = utils:getContext(context);
    requestStream.apiVersion = utils:getVersionFromBasePath(utils:getContext(context));
    requestStream.appTenant = keyValidationDto.subscriberTenantDomain;
    requestStream.apiTenant = constants:SUPER_TENANT_DOMAIN_NAME;
    requestStream.apiName = keyValidationDto.apiName;
    return requestStream;
}