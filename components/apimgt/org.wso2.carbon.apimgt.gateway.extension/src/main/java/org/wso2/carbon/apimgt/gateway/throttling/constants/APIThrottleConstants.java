/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.throttling.constants;


/**
 * Contains throttling related constants
 */
public class APIThrottleConstants {

    public static final int API_THROTTLE_OUT_ERROR_CODE = 900800;
    public static final int HARD_LIMIT_EXCEEDED_ERROR_CODE = 900801;
    public static final int RESOURCE_THROTTLE_OUT_ERROR_CODE = 900802;
    public static final int APPLICATION_THROTTLE_OUT_ERROR_CODE = 900803;
    public static final int SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE = 900804;
    public static final int SUBSCRIPTION_BURST_THROTTLE_OUT_ERROR_CODE = 900807;
    public static final int BLOCKED_ERROR_CODE = 900805;
    public static final int CUSTOM_POLICY_THROTTLE_OUT_ERROR_CODE = 900806;

    public static final String API_LIMIT_EXCEEDED = "API_LIMIT_EXCEEDED";
    public static final String RESOURCE_LIMIT_EXCEEDED = "RESOURCE_LIMIT_EXCEEDED";
    public static final String APPLICATION_LIMIT_EXCEEDED = "APPLICATION_LIMIT_EXCEEDED";
    public static final String SUBSCRIPTION_LIMIT_EXCEEDED = "SUBSCRIPTION_LIMIT_EXCEEDED";
    public static final String CUSTOM_POLICY_LIMIT_EXCEED = "CUSTOM_POLICY_LIMIT_EXCEED";

    public static final String API_THROTTLE_NS = "http://wso2.org/apimanager/throttling";
    public static final String API_THROTTLE_NS_PREFIX = "amt";
    public static final String API_THROTTLE_OUT_HANDLER = "_throttle_out_handler_";
    public static final String HARD_THROTTLING_CONFIGURATION = "hard_throttling_limits";
    public static final String PRODUCTION_HARD_LIMIT = "PRODUCTION_HARD_LIMIT";
    public static final String SUBSCRIPTION_BURST_LIMIT = "SUBSCRIPTION_BURST_LIMIT";
    public static final String SANDBOX_HARD_LIMIT = "SANDBOX_HARD_LIMIT";
    public static final String THROTTLED_OUT_REASON = "THROTTLED_OUT_REASON";
    public static final String THROTTLED_NEXT_ACCESS_TIMESTAMP = "NEXT_ACCESS_TIME";
    public static final String THROTTLED_NEXT_ACCESS_TIME = "NEXT_ACCESS_UTC_TIME";
    public static final String HARD_LIMIT_EXCEEDED = "HARD_LIMIT_EXCEEDED";
    public static final String SUBSCRIPTON_BURST_LIMIT_EXCEEDED = "SUBSCRIPTION_BURST_LIMIT_EXCEED";
    public static final String REQUEST_BLOCKED = "REQUEST_BLOCKED";
    public static final int SC_TOO_MANY_REQUESTS = 429;
    public static final String BLOCKED_REASON = "BLOCKED_REASON";
    public static final String UTC = "UTC";
    public static final String IS_THROTTLED = "isThrottled";
    public static final String THROTTLE_KEY = "throttleKey";
    public static final String EXPIRY_TIMESTAMP = "expiryTimeStamp";
    public static final String IP = "ip";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String MESSAGE_SIZE = "messageSize";
    public static final String MIN = "min";
    public static final String WS_THROTTLE_POLICY_HEADER = "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap" +
            ".org/ws/2004/09/policy\" xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
            "    <throttle:MediatorThrottleAssertion>\n";
    public static final String WS_THROTTLE_POLICY_BOTTOM = "</throttle:MediatorThrottleAssertion>\n" + "</wsp:Policy>";

    public static final String TRUE = "true";
    public static final String ADD = "add";

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String REST_URL_POSTFIX = "REST_URL_POSTFIX";

    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String REMOTE_ADDR = "REMOTE_ADDR";

    public static final String THROTTLING_LATENCY = "throttling_latency";

    public static final String REST_API_CONTEXT = "REST_API_CONTEXT";
    public static final String SYNAPSE_REST_API_VERSION = "SYNAPSE_REST_API_VERSION";

    // HttpStatus
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_FORBIDDEN = 403;

    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    // policy constants
    public static final String IP_RANGE_TYPE = "IPRange";
    public static final String IP_SPECIFIC_TYPE = "IPSpecific";
    public static final String QUERY_PARAMETER_TYPE = "QueryParameterType";
    public static final String JWT_CLAIMS_TYPE = "JWTClaims";
    public static final String HEADER_TYPE = "Header";

    //governance registry apimgt root location
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";

    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION + "/applicationdata";

    public static final String VERB_INFO_DTO = "VERB_INFO";

    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";

    // There is a property added to the message context when such an event happens.
    public static final String API_USAGE_THROTTLE_OUT_PROPERTY_KEY = "isThrottleOutIgnored";
    public static final String AUTH_TYPE_NONE = "NONE";

    public static final String POLICY_TEMPLATE_KEY = "keyTemplateValue";
    public static final String TEMPLATE_KEY_STATE = "keyTemplateState";

    public static final String THROTTLE_POLICY_DEFAULT = "_default";

    public static final String API_POLICY_USER_LEVEL = "userLevel";
    public static final String API_POLICY_API_LEVEL = "apiLevel";


    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String BLOCKING_CONDITION_STATE = "state";
    public static final String BLOCKING_CONDITION_KEY = "blockingCondition";
    public static final String BLOCKING_CONDITION_VALUE = "conditionValue";
    public static final String BLOCKING_CONDITION_DOMAIN = "tenantDomain";
    public static final String BLOCKING_CONDITIONS_APPLICATION = "APPLICATION";
    public static final String BLOCKING_CONDITIONS_API = "API";
    public static final String BLOCKING_CONDITIONS_USER = "USER";
    public static final String BLOCKING_CONDITIONS_IP = "IP";

}
