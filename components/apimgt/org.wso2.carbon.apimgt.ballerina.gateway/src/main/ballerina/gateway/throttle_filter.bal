import ballerina/http;
import ballerina/log;
import ballerina/cache;
import ballerina/config;

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
        if (context.attributes.hasKey(KEY_VALIDATION_RESPONSE)){
            if (isRquestBlocked(request, context)){
                // request Blocked
                requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
            } else {
                requestFilterResult = { canProceed: true };
                // Request not blocked go to check throttling
                APIKeyValidationDto keyvalidationResult = check <APIKeyValidationDto>context.attributes[
                KEY_VALIDATION_RESPONSE];
                boolean apiLevelThrottled = isApiLevelThrottled(keyvalidationResult);
                boolean resourceLevelThrottled = isResourceLevelThrottled(keyvalidationResult);
                if (!apiLevelThrottled){
                    if (!resourceLevelThrottled){
                        if (!isSubscriptionLevelThrottled(context, keyvalidationResult)){
                            if (!isApplicationLevelThrottled(keyvalidationResult)){
                                if (!isHardlimitThrottled(getContext(context), getVersionFromBasePath(
                                        getContext(context)))){
                                    // Send Throttle Event
                                    RequestStream throttleEvent = generateThrottleEvent(request, context,
                                        keyvalidationResult);
                                    publishNonThrottleEvent(throttleEvent);
                                }
                            } else {
                                // Application Level Throttled
                                requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
                            }
                        } else {
                            // Subscription Level Throttled
                            requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
                        }
                    } else {
                        //Resource level Throttled
                        requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
                    }
                } else {
                    //API level Throttled
                    requestFilterResult = { canProceed: false, statusCode: 429, message: "Message blocked" };
                }
            }
        } else {
            requestFilterResult = { canProceed: false, statusCode: 500, message: "Internal Error Occurred" };
        }
        return requestFilterResult;
    }
};
function isRquestBlocked(http:Request request, http:FilterContext context) returns (boolean) {
    APIKeyValidationDto keyvalidationResult = check <APIKeyValidationDto>context.attributes[
    KEY_VALIDATION_RESPONSE];
    string apiLevelBlockingKey = getContext(context);
    string ipLevelBlockingKey = SUPER_TENANT_DOMAIN_NAME + ":" + getClientIp(request);
    string appLevelBlockingKey = keyvalidationResult.subscriber + ":" + keyvalidationResult.applicationName;
    if (isBlockConditionExist(apiLevelBlockingKey) || isBlockConditionExist(ipLevelBlockingKey) ||
        isBlockConditionExist(appLevelBlockingKey)){
        return true;
    } else {
        return false;
    }
}

function isApiLevelThrottled(APIKeyValidationDto keyValidationDto) returns (boolean) {
    return false;
}

function isResourceLevelThrottled(
                                  APIKeyValidationDto keyValidationDto) returns (boolean) {
    return false;
}

function isHardlimitThrottled(string context, string apiVersion) returns (boolean) {

    return false;
}


function isSubscriptionLevelThrottled(http:FilterContext context, APIKeyValidationDto keyValidationDto) returns (
            boolean) {
    string subscriptionLevelThrottleKey = keyValidationDto.applicationId + ":" + getContext
        (context) + ":" + getVersionFromBasePath(getContext(context));
    if (isThrottled(subscriptionLevelThrottleKey)){
        return true;
    } else {
        return false;
    }
}

function isApplicationLevelThrottled(APIKeyValidationDto keyValidationDto) returns (boolean) {
    string applicationLevelThrottleKey = keyValidationDto.applicationId + ":" + keyValidationDto.endUserName;
    if (isThrottled(applicationLevelThrottleKey)){
        return true;
    } else {
        return false;
    }
}
function generateThrottleEvent(http:Request req, http:FilterContext context, APIKeyValidationDto keyValidationDto)
             returns (
                     RequestStream) {
    RequestStream requestStream;
    requestStream.apiKey = getContext(context) + ":" + getVersionFromBasePath(getContext(context));
    requestStream.appKey = keyValidationDto.applicationId + ":" + keyValidationDto.endUserName;
    requestStream.subscriptionKey = keyValidationDto.applicationId + ":" + getContext(context) + ":" +
        getVersionFromBasePath(getContext(context));
    requestStream.appTier = keyValidationDto.applicationTier;
    requestStream.apiTier = keyValidationDto.apiTier;
    requestStream.subscriptionTier = keyValidationDto.tier;
    requestStream.resourceKey = "";
    requestStream.resourceTier = "Unlimited";
    requestStream.userId = keyValidationDto.endUserName;
    requestStream.apiContext = getContext(context);
    requestStream.apiVersion = getVersionFromBasePath(getContext(context));
    requestStream.appTenant = keyValidationDto.subscriberTenantDomain;
    requestStream.apiTenant = SUPER_TENANT_DOMAIN_NAME;
    requestStream.apiName = keyValidationDto.apiName;
    return requestStream;
}