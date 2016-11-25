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
public class APIConstants {
    public static final String DEPRECATE_PREVIOUS_VERSIONS = "Deprecate older versions";
    public static final String REQUIRE_RE_SUBSCRIPTIONS = "Require Re-Subscriptions";
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
    public static final String POLICY_APPLICATION_TYPE = "2";

    /**
     *  Application status related constants.
     */
    public static class ApplicationStatus {
        public static final String APPLICATION_CREATED = "CREATED";
        public static final String APPLICATION_APPROVED = "APPROVED";
        public static final String APPLICATION_REJECTED = "REJECTED";
        public static final String APPLICATION_ONHOLD = "ON_HOLD";
    }

    /**
     *  Application registration status related constants.
     */
    public static class AppRegistrationStatus {
        public static final String REGISTRATION_CREATED = "CREATED";
        public static final String REGISTRATION_APPROVED = "APPROVED";
        public static final String REGISTRATION_REJECTED = "REJECTED";
        public static final String REGISTRATION_COMPLETED = "COMPLETED";
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

}
