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

    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String ENCODING_UTF_8 = "UTF-8";
    public static final String DEPRECATE_PREVIOUS_VERSIONS = "Deprecate old versions after publish the API";
    public static final String REQUIRE_RE_SUBSCRIPTIONS = "Require re-subscription when publish the API";
    public static final String CHECK_LIST_ITEM_CHANGE_EVENT = "CheckListItemChange";
    public static final String IS_EXTERNAL_KEYMANAGER = "IsExternalKeyManager";
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
    public static final String GLOBAL_ENDPOINT = "GLOBAL";
    public static final String API_SPECIFIC_ENDPOINT = "API";
    public static final String RESOURCE_SPECIFIC_ENDPOINT = "RESOURCE";
    public static final String PRODUCTION_ENDPOINT = "production";
    public static final String OAUTH2SECURITY = "OAuth2Security";
    public static final String SCOPES = "scopes";

    public static final String DEFAULT_LABEL_NAME = "Default";
    public static final String DEFAULT_LABEL_ACCESS_URL = "https://localhost:9092";

    //workflow executor default executors
    public static final String WF_DEFAULT_APPCREATION_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.ApplicationCreationSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_APISTATE_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.APIStateChangeSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_PRODAPP_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.ApplicationRegistrationSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_SANDBOXAPP_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.ApplicationRegistrationSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_APPDELETE_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.ApplicationDeletionSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_SUBCREATION_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.SubscriptionCreationSimpleWorkflowExecutor";
    public static final String WF_DEFAULT_SUBDELETE_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.SubscriptionDeletionSimpleWorkflowExecutor";

    //workflow executor default executor
    public static final String WF_DEFAULT_WF_EXEC =
            "org.wso2.carbon.apimgt.core.workflow.DefaultWorkflowExecutor";

    //Store constants
    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";
    public static final String EMPTY_STRING_VALUE = " ";
    public static final String SUPPORTED_HTTP_VERBS = "GET,POST,PUT,DELETE,PATCH,HEAD,OPTIONS";

    public static final String HTTP_GET = "GET";
    public static final String OVERWRITE_LABELS = "overwrite_labels";

    public static final String TAG_SEARCH_TYPE_PREFIX = "tags";
    public static final String SUBCONTEXT_SEARCH_TYPE_PREFIX = "subcontext";
    public static final String PROVIDER_SEARCH_TYPE_PREFIX = "provider";
    public static final String VERSION_SEARCH_TYPE_PREFIX = "version";
    public static final String CONTEXT_SEARCH_TYPE_PREFIX = "context";
    public static final String DESCRIPTION_SEARCH_TYPE_PREFIX = "description";

    public static final String TAG_NAME_COLUMN = "NAME";
    public static final String URL_PATTERN_COLUMN = "URL_PATTERN";

    public static final String SANDBOX_ENDPOINT = "sandbox";
    public static final String GATEWAY_CONFIG = "GATEWAY_CONFIG";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static final String HTTP_METHOD = "HTTP_METHOD";

    public static final String NAMESPACE_STORE_API = "wso2.carbon.apimgt.store.rest.api";
    public static final String NAMESPACE_PUBLISHER_API = "wso2.carbon.apimgt.publisher.rest.api";
    public static final String NAMESPACE_ADMIN_API = "wso2.carbon.apimgt.admin.rest.api";

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
        SANDBOX_ONLY_BLOCKED,
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
     * API workflow statuses.
     */
    public enum APILCWorkflowStatus {
        APPROVED,
        REJECTED,
        PENDING
    }

    /**
     * API File Constants
     */
    public static class APIFileUtilConstants {
        public static final String API_DEFINITION_FILE_PREFIX = "api-";
        public static final String JSON_EXTENSION = ".json";
        public static final String SWAGGER_DEFINITION_FILE_PREFIX = "swagger-";
        public static final String GATEWAY_CONFIGURATION_DEFINITION_FILE = "gateway-configuration";
        public static final String THUMBNAIL_FILE_NAME = "thumbnail";
        public static final String ENDPOINTS_ROOT_DIRECTORY = "Endpoints";
    }


    /**
     * WSDL Constants
     */
    public static class WSDLConstants {
        public static final String WSDL_ARCHIVES_FOLDERNAME = "WSDL-archives";
        public static final String WSDL_ARCHIVE_FILENAME = "wsdl-archive.zip";
        public static final String EXTRACTED_WSDL_ARCHIVE_FOLDERNAME = "extracted";
        public static final String WSDL_VERSION_11 = "1.1";
        public static final String WSDL_VERSION_20 = "2.0";
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

        public static final String COLUMN_KEY_TEMPLATE = "KEY_TEMPLATE";

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
        public static final String COLUMN_CONDITION_GROUP_ID = "CONDITION_GROUP_ID";
        public static final String BLOCKING_CONDITION_STATE = "state";
        public static final String BLOCKING_CONDITION_KEY = "blockingCondition";
        public static final String BLOCKING_CONDITION_VALUE = "conditionValue";
        public static final String BLOCKING_CONDITIONS_APPLICATION = "APPLICATION";
        public static final String BLOCKING_CONDITIONS_API = "API";
        public static final String BLOCKING_CONDITIONS_USER = "USER";
        public static final String BLOCKING_CONDITIONS_IP = "IP";
        public static final String BLOCKING_CONDITION_IP_RANGE = "IP_RANGE";
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
        public static final String KEY_TYPE = "KEY_TYPE";
        public static final String GRANT_TYPES = "GRANT_TYPES";
        public static final String CALLBACK_URL = "CALLBACK_URL";
        public static final String APPLICATION_QUERY = "APPLICATION_QUERY";
        public static final String API_ID = "API_ID";
        public static final String TIER = "TIER";
        public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
        public static final String DOC_ID = "DOC_ID";
        public static final String TIER_LEVEL = "TIER_LEVEL";
        public static final String ENDPOINT_ID = "ENDPOINT_ID";
        public static final String LIFECYCLE_ID = "LIFECYCLE_ID";
        public static final String WORKFLOW_REF_ID = "WORKFLOW_REFERENCE_ID";
        public static final String WORKFLOW_CATEGORY = "WORKFLOW_CATEGORY";
        public static final String COMMENT_ID = "COMMENT_ID";
        public static final String RATING_ID = "RATING_ID";
        public static final String USERNAME = "USERNAME";
    }

    /**
     * Permission related constants
     */
    public static class Permission {
        public static final int MANAGE_SUBSCRIPTION_PERMISSION = 8; //Publisher side API permission
        public static final int SUBSCRIBE_PERMISSION = 8; //Store side permission
        public static final int READ_PERMISSION = 1;
        public static final int UPDATE_PERMISSION = 2;
        public static final int DELETE_PERMISSION = 4;
        public static final String EVERYONE_GROUP = "EVERYONE";
        public static final String GROUP_ID = "groupId";
        public static final String PERMISSION = "permission";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String SUBSCRIPTION = "SUBSCRIPTION";
        public static final String MANAGE_SUBSCRIPTION = "MANAGE_SUBSCRIPTION";
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
        public static final String HEAD = "HEAD";
        public static final String OPTIONS = "OPTIONS";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String USER_TENANT_DOMAIN = "userTenantDomain";
        public static final String RSA_SIGNED_TOKEN = "rsaSignedToken";
        public static final String SET_COOKIE = "Set-Cookie";
        public static final String COOKIE = "Cookie";
    }

    /**
     * Workflow related constants
     */
    public static class WorkflowConstants {
        public static final String WF_TYPE_AM_SUBSCRIPTION_CREATION = "AM_SUBSCRIPTION_CREATION";
        public static final String WF_TYPE_AM_SUBSCRIPTION_DELETION = "AM_SUBSCRIPTION_DELETION";
        public static final String WF_TYPE_AM_APPLICATION_CREATION = "AM_APPLICATION_CREATION";
        public static final String WF_TYPE_AM_APPLICATION_DELETION = "AM_APPLICATION_DELETION";
        public static final String WF_TYPE_AM_APPLICATION_UPDATE = "AM_APPLICATION_UPDATE";
        public static final String WF_TYPE_AM_API_STATE = "AM_API_STATE";
        public static final String WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION =
                "AM_APPLICATION_REGISTRATION_PRODUCTION";
        public static final String WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX = "AM_APPLICATION_REGISTRATION_SANDBOX";
        public static final String ATTRIBUTE_API_CUR_STATE = "apiCurrentState";
        public static final String ATTRIBUTE_API_TARGET_STATE = "apiTargetState";
        public static final String ATTRIBUTE_API_LC_INVOKER = "lcStateChangeInvoker";
        public static final String ATTRIBUTE_API_LAST_UPTIME = "lastUpdatedTime";
        public static final String ATTRIBUTE_APPLICATION_NAME = "name";
        public static final String ATTRIBUTE_APPLICATION_UPDATEDBY = "updatedUser";
        public static final String ATTRIBUTE_APPLICATION_TIER = "tier";
        public static final String ATTRIBUTE_APPLICATION_DESCRIPTION = "description";
        public static final String ATTRIBUTE_APPLICATION_PERMISSION = "permission";
        public static final String ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS = "status";
        public static final String ATTRIBUTE_APPLICATION_POLICY_ID = "policyId";

    }

    /**
     * ETags related constants
     */
    public static class ETagConstants {
        public static final String MESSAGE_DIGEST_ALGORITHM_MD5 = "MD5";
    }

    /**
     * Gateway event types
     */
    public static class GatewayEventTypes {
        public static final String API_CREATE = "API_CREATE";
        public static final String API_UPDATE = "API_UPDATE";
        public static final String API_DELETE = "API_DELETE";
        public static final String API_STATE_CHANGE = "API_STATE_CHANGE";
        public static final String ENDPOINT_CREATE = "ENDPOINT_CREATE";
        public static final String ENDPOINT_UPDATE = "ENDPOINT_UPDATE";
        public static final String ENDPOINT_DELETE = "ENDPOINT_DELETE";
        public static final String SUBSCRIPTION_CREATE = "SUBSCRIPTION_CREATE";
        public static final String SUBSCRIPTION_DELETE = "SUBSCRIPTION_DELETE";
        public static final String SUBSCRIPTION_STATUS_CHANGE = "SUBSCRIPTION_STATUS_CHANGE";
        public static final String APPLICATION_CREATE = "APPLICATION_CREATE";
        public static final String APPLICATION_UPDATE = "APPLICATION_UPDATE";
        public static final String APPLICATION_DELETE = "APPLICATION_DELETE";
        public static final String POLICY_CREATE = "POLICY_CREATE";
        public static final String POLICY_UPDATE = "POLICY_UPDATE";
        public static final String POLICY_DELETE = "POLICY_DELETE";
        public static final String BLOCK_CONDITION_ADD = "BLOCK_CONDITION_ADD";
        public static final String BLOCK_CONDITION_UPDATE = "BLOCK_CONDITION_UPDATE";
        public static final String BLOCK_CONDITION_DELETE = "BLOCK_CONDITION_DELETE";

    }

    /**
     * HTTP Status Codes
     */
    public static class HTTPStatusCodes {
        public static final int SC_100_CONTINUE = 100;
        public static final int SC_101_SWITCHING_PROTOCOLS = 101;
        public static final int SC_102_PROCESSING = 102;
        public static final int SC_200_OK = 200;
        public static final int SC_201_CREATED = 201;
        public static final int SC_202_ACCEPTED = 202;
        public static final int SC_203_NON_AUTHORITATIVE_INFORMATION = 203;
        public static final int SC_204_NO_CONTENT = 204;
        public static final int SC_205_RESET_CONTENT = 205;
        public static final int SC_206_PARTIAL_CONTENT = 206;
        public static final int SC_207_MULTI_STATUS = 207;
        public static final int SC_300_MULTIPLE_CHOICES = 300;
        public static final int SC_301_MOVED_PERMANENTLY = 301;
        public static final int SC_302_MOVED_TEMPORARILY = 302;
        public static final int SC_303_SEE_OTHER = 303;
        public static final int SC_304_NOT_MODIFIED = 304;
        public static final int SC_305_USE_PROXY = 305;
        public static final int SC_307_TEMPORARY_REDIRECT = 307;
        public static final int SC_400_BAD_REQUEST = 400;
        public static final int SC_401_UNAUTHORIZED = 401;
        public static final int SC_402_PAYMENT_REQUIRED = 402;
        public static final int SC_403_FORBIDDEN = 403;
        public static final int SC_404_NOT_FOUND = 404;
        public static final int SC_405_METHOD_NOT_ALLOWED = 405;
        public static final int SC_406_NOT_ACCEPTABLE = 406;
        public static final int SC_407_PROXY_AUTHENTICATION_REQUIRED = 407;
        public static final int SC_408_REQUEST_TIMEOUT = 408;
        public static final int SC_409_CONFLICT = 409;
        public static final int SC_410_GONE = 410;
        public static final int SC_411_LENGTH_REQUIRED = 411;
        public static final int SC_412_PRECONDITION_FAILED = 412;
        public static final int SC_413_REQUEST_TOO_LONG = 413;
        public static final int SC_414_REQUEST_URI_TOO_LONG = 414;
        public static final int SC_415_UNSUPPORTED_MEDIA_TYPE = 415;
        public static final int SC_416_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
        public static final int SC_417_EXPECTATION_FAILED = 417;
        public static final int SC_419_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
        public static final int SC_420_METHOD_FAILURE = 420;
        public static final int SC_422_UNPROCESSABLE_ENTITY = 422;
        public static final int SC_423_LOCKED = 423;
        public static final int SC_424_FAILED_DEPENDENCY = 424;
        public static final int SC_500_INTERNAL_SERVER_ERROR = 500;
        public static final int SC_501_NOT_IMPLEMENTED = 501;
        public static final int SC_502_BAD_GATEWAY = 502;
        public static final int SC_503_SERVICE_UNAVAILABLE = 503;
        public static final int SC_504_GATEWAY_TIMEOUT = 504;
        public static final int SC_505_HTTP_VERSION_NOT_SUPPORTED = 505;
        public static final int SC_507_INSUFFICIENT_STORAGE = 507;
    }

    /**
     * Type of UUF apps in system
     */
    public static class APPType {
        public static final String PUBLISHER = "publisher";
        public static final String STORE = "store";
        public static final String ADMIN = "admin";
    }

    /**
     * Label related constants
     */
    public static class LabelConstants {
        public static final String DEFAULT = "Default";
    }
    
}
