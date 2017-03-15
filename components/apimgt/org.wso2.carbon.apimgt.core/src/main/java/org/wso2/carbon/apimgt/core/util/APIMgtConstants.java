/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core.util;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public class APIMgtConstants {

    public static final String DEPRECATE_PREVIOUS_VERSIONS = "Deprecate old versions after publish the API";
    public static final String REQUIRE_RE_SUBSCRIPTIONS = "Require re-subscription when publish the API";
    public static final String CHECK_LIST_ITEM_CHANGE_EVENT = "CheckListItemChange";

    //Swagger v2.0 constants
    public static final String SWAGGER_X_SCOPE = "x-scope";
    public static final String SWAGGER_X_AUTH_TYPE = "x-auth-type";
    public static final String SWAGGER_X_THROTTLING_TIER = "x-throttling-tier";
    public static final String SWAGGER_X_MEDIATION_SCRIPT = "x-mediation-script";
    public static final String SWAGGER_X_WSO2_SECURITY = "x-wso2-security";
    public static final String SWAGGER_X_WSO2_SCOPES = "x-wso2-scopes";
    public static final String SWAGGER_SCOPE_KEY = "key";
    public static final String SWAGGER_NAME = "name";
    public static final String SWAGGER_DESCRIPTION = "description";
    public static final String SWAGGER_ROLES = "roles";
    public static final String SWAGGER_TITLE = "title";
    public static final String SWAGGER_EMAIL = "email";
    public static final String SWAGGER_CONTACT = "contact";
    public static final String SWAGGER_VER = "version";
    public static final String SWAGGER_OBJECT_NAME_APIM = "apim";
    public static final String SWAGGER_PATHS = "paths";
    public static final String SWAGGER_RESPONSES = "responses";
    public static final String SWAGGER = "swagger";
    public static final String SWAGGER_V2 = "2.0";
    public static final String SWAGGER_INFO = "info";
    public static final String SWAGGER_RESPONSE_200 = "200";
    public static final String API = "API";
    public static final String API_LIFECYCLE = "API_LIFECYCLE";
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String DEFAULT_API_POLICY = "Unlimited";

    //Store constants
    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";
    public static final String EMPTY_STRING_VALUE = " ";
    public static final String SUPPORTED_HTTP_VERBS = "GET,POST,PUT,DELETE,PATCH,HEAD,OPTIONS";

    public static final String HTTP_GET = "GET";
    public static final String OVERWRITE_LABELS = "overwrite_labels";
    /**
     * Application statuses.
     */
    public static class ApplicationStatus {
        public static final String APPLICATION_CREATED = "CREATED";
        public static final String APPLICATION_APPROVED = "APPROVED";
        public static final String APPLICATION_REJECTED = "REJECTED";
        public static final String APPLICATION_ONHOLD = "ON_HOLD";
    }

    /**
     * Application registration statuses.
     */
    public enum AppRegistrationStatus {
        CREATED,
        APPROVED,
        REJECTED,
        COMPLETED
    }

    /**
     * Subscription statuses.
     */
    public enum SubscriptionStatus {
        BLOCKED,
        PROD_ONLY_BLOCKED,
        ACTIVE,
        ON_HOLD,
        REJECTED
    }

    /**
     * Subscription types
     */
    public enum SubscriptionType {
        SUBSCRIBE,
        UN_SUBSCRIBE
    }

    /**
     * Throttle policy related constants
     */
    public static class ThrottlePolicyConstants {
        public static final String COLUMN_POLICY_ID = "POLICY_ID";

        public static final String COLUMN_UUID = "UUID";

        public static final String COLUMN_NAME = "NAME";

        public static final String COLUMN_DESCRIPTION = "DESCRIPTION";

        public static final String COLUMN_DISPLAY_NAME = "DISPLAY_NAME";

        public static final String COLUMN_TENANT_ID = "TENANT_ID";

        public static final String COLUMN_RATE_LIMIT_COUNT = "RATE_LIMIT_COUNT";

        public static final String COLUMN_RATE_LIMIT_TIME_UNIT = "RATE_LIMIT_TIME_UNIT";

        public static final String COLUMN_QUOTA_POLICY_TYPE = "QUOTA_TYPE";

        public static final String COLUMN_QUOTA = "QUOTA";

        public static final String COLUMN_QUOTA_UNIT = "QUOTA_UNIT";

        public static final String COLUMN_UNIT_TIME = "UNIT_TIME";

        public static final String COLUMN_TIME_UNIT = "TIME_UNIT";

        public static final String COLUMN_APPLICABLE_LEVEL = "APPLICABLE_LEVEL";

        public static final String COLUMN_DEFAULT_QUOTA_POLICY_TYPE = "DEFAULT_QUOTA_TYPE";

        public static final String COLUMN_DEFAULT_UNIT_TIME = "DEFAULT_UNIT_TIME";

        public static final String COLUMN_DEFAULT_TIME_UNIT = "DEFAULT_TIME_UNIT";

        public static final String COLUMN_DEFAULT_QUOTA = "DEFAULT_QUOTA";

        public static final String COLUMN_DEFAULT_QUOTA_UNIT = "DEFAULT_QUOTA_UNIT";

        public static final String COLUMN_SPECIFIC_DATE = "SPECIFIC_DATE";

        public static final String COLUMN_STARTING_DATE = "STARTING_DATE";

        public static final String COLUMN_ENDING_DATE = "ENDING_DATE";

        public static final String COLUMN_SPECIFIC_IP = "SPECIFIC_IP";

        public static final String COLUMN_STARTING_IP = "STARTING_IP";

        public static final String COLUMN_ENDING_IP = "ENDING_IP";

        public static final String COLUMN_HTTP_VERB = "HTTP_VERB";

        public static final String COLUMN_CONDITION_ID = "CONDITION_GROUP_ID";

        public static final String COLUMN_PARAMETER_NAME = "PARAMETER_NAME";

        public static final String COLUMN_PARAMETER_VALUE = "PARAMETER_VALUE";

        public static final String COLUMN_CLAIM_URI = "CLAIM_URI";

        public static final String COLUMN_CLAIM_ATTRIBUTE = "CLAIM_ATTRIB";

        public static final String COLUMN_HEADER_FIELD_NAME = "HEADER_FIELD_NAME";

        public static final String COLUMN_HEADER_FIELD_VALUE = "HEADER_FIELD_VALUE";

        public static final String COLUMN_SIDDHI_QUERY = "SIDDHI_QUERY";

        public static final String COLUMN_KEY_TEMPLATE  = "KEY_TEMPLATE";

        public static final String COLUMN_DEPLOYED = "IS_DEPLOYED";

        public static final String COLUMN_WITHIN_IP_RANGE = "WITHIN_IP_RANGE";

        public static final String COLUMN_IS_HEADER_FIELD_MAPPING = "IS_HEADER_FIELD_MAPPING";

        public static final String COLUMN_IS_CLAIM_MAPPING = "IS_CLAIM_MAPPING";

        public static final String COLUMN_IS_PARAM_MAPPING = "IS_PARAM_MAPPING";

        public static final String COLUMN_STOP_ON_QUOTA_REACH = "STOP_ON_QUOTA_REACH";

        public static final String COLUMN_BILLING_PLAN = "BILLING_PLAN";

        public static final String COLUMN_CUSTOM_ATTRIB = "CUSTOM_ATTRIBUTES";
        public static final String API_LEVEL = "api";
        public static final String APPLICATION_LEVEL = "application";
        public static final String SUBSCRIPTION_LEVEL = "subscription";
        public static final String RESOURCE_LEVEL = "resource";

    }

    /**
     * Exceptions related constance will be put here
     */
    public static class ExceptionsConstants {

        public static final String API_NAME = "API_NAME";
        public static final String API_CONTEXT = "API_CONTEXT";
        public static final String API_VERSION = "API_VERSION";
        public static final String APPLICATION_NAME = "APPLICATION_NAME";
        public static final String CONSUMER_KEY = "CONSUMER_KEY";
        public static final String APPLICATION_ID = "APPLICATION_ID";
        public static final String APPLICATION_QUERY = "APPLICATION_QUERY";
        public static final String API_ID = "API_ID";
        public static final String TIER = "TIER";
        public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
        public static final String DOC_ID = "DOC_ID";
        public static final String TIER_LEVEL = "TIER_LEVEL";
        public static final String ENDPOINT_ID = "ENDPOINT_ID";
        public static final String LIFECYCLE_ID = "LIFECYCLE_ID";
    }

    /**
     * Permission related constants
     */
    public static class Permission {
        public static final int SUBSCRIBE_PERMISSION = 8;
        public static final int READ_PERMISSION = 4;
        public static final int UPDATE_PERMISSION = 2;
        public static final int DELETE_PERMISSION = 1;
        public static final String EVERYONE_GROUP = "EVERYONE";
        public static final String GROUP_ID = "groupId";
        public static final String PERMISSION = "permission";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String SUBSCRIPTION = "SUBSCRIPTION";

    }

    /**
     * Functions related constants
     */
    public static class FunctionsConstants {
        public static final String API_ID = "apiId";
        public static final String API_NAME = "apiName";
        public static final String API_VERSION = "apiVersion";
        public static final String API_DESCRIPTION = "apiDescription";
        public static final String API_CONTEXT = "apiContext";
        public static final String API_LC_STATUS = "apiStatus";
        public static final String API_PERMISSION = "apiPermission";
        public static final String API_PROVIDER = "apiProvider";
        public static final String EVENT = "event";
        public static final String COMPONENT = "component";
        public static final String EVENT_TIME = "eventTime";
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String USER_TENANT_DOMAIN = "userTenantDomain";
        public static final String RSA_SIGNED_TOKEN = "rsaSignedToken";
        public static final String SET_COOKIE = "Set-Cookie";
        public static final String COOKIE = "Cookie";
    }
}
