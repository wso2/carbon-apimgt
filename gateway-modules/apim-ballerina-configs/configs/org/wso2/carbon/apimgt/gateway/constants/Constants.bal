package org.wso2.carbon.apimgt.gateway.constants;

const string API_CORE_URL = "API_CORE_URL";
const string GW_HOME = "GW_HOME";
const string API_CREATE = "API_CREATE";
const string API_UPDATE = "API_UPDATE";
const string API_DELETE = "API_DELETE";
const string API_STATE_CHANGE = "API_STATE_CHANGE";
const string EVENT_TYPE = "eventType";
const string INTROSPECT_CONTEXT = "/oauth2/introspect";
const string USER_INFO_CONTEXT = "/oauth2/userinfo";
const string AUTHORIZATION = "Authorization";
const string BEARER = "Bearer ";
const string AUTHENTICATION_TYPE_NONE = "None";
const string KEY_VALIDATION_INFO = "KEY_VALIDATION_INFO";
const string TOKEN_CACHE = "TOKEN_CACHE";
const string USER_INFO_CACHE = "USER_INFO_CACHE";
const string SUBSCRIPTION_CACHE = "SUBSCRIPTION_CACHE";
const string APPLICATION_CACHE = "APPLICATION_CACHE";
const string RESOURCE_CACHE = "RESOURCE_CACHE";
const string POLICY_CACHE = "POLICY_CACHE";

const string THROTTLE_KEY = "throttleKey";
const string POLICY_TEMPLATE_KEY = "keyTemplateValue";
const string IS_THROTTLED = "isThrottled";
const string EXPIRY_TIMESTAMP = "expiryTimeStamp";
const string TRUE = "true";
const string ADD = "add";

const string BLOCKING_EVENT_TYPE = "jms";
const string BLOCKING_EVENT_FORMAT = "map";
const string BLOCKING_CONDITION_STATE = "state";
const string BLOCKING_CONDITION_KEY = "blockingCondition";
const string BLOCKING_CONDITION_VALUE = "conditionValue";
const string BLOCKING_CONDITION_DOMAIN = "tenantDomain";
const string BLOCKING_CONDITIONS_APPLICATION = "APPLICATION";
const string BLOCKING_CONDITIONS_API = "API";
const string BLOCKING_CONDITIONS_USER = "USER";
const string BLOCKING_CONDITIONS_IP = "IP";
const string KEY_TEMPLATE_KEY = "keyTemplateValue";
const string KEY_TEMPLATE_KEY_STATE = "keyTemplateState";
const string UNLIMITED_TIER = "Unlimited";

const string THROTTLED_OUT_REASON = "THROTTLE_OUT_REASON";
const string THROTTLED_ERROR_CODE = "THROTTLED_ERROR_CODE";
const string HTTP_ERROR_CODE = "THROTTLED_ERROR_CODE";

const string THROTTLE_OUT_REASON_API_LIMIT_EXCEEDED = "API Limit Exceeded";
const string THROTTLE_OUT_REASON_RESOURCE_LIMIT_EXCEEDED = "Resource Limit Exceeded";
const string THROTTLE_OUT_REASON_APPLICATION_LIMIT_EXCEEDED = "Application Limit Exceeded";
const string THROTTLE_OUT_REASON_SUBSCRIPTION_LIMIT_EXCEEDED = "Subscription Limit Exceeded";
const string THROTTLE_OUT_REASON_REQUEST_BLOCKED = "Request is Blocked by administrator";

const string API_THROTTLE_OUT_ERROR_CODE = "900800";
const string RESOURCE_THROTTLE_OUT_ERROR_CODE = "900802";
const string APPLICATION_THROTTLE_OUT_ERROR_CODE = "900803";
const string SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE = "900804";
const string BLOCKED_ERROR_CODE = "900805";
const int HTTP_FORBIDDEN = 403;
const int HTTP_TOO_MANY_REQUESTS = 429;
const int HTTP_UNAUTHORIZED = 401;

const string STOP_ON_QUOTA_REACH = "STOP_ON_QUOTA_REACH";
const string MAINTENANCE = "Maintenance";
const string BASE_PATH = "BASE_PATH";