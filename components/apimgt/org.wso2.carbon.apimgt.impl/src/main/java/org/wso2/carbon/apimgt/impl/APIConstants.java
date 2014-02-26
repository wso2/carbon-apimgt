/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl;

import javax.xml.namespace.QName;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class APIConstants {

    //key value of the provider rxt
    public static final String PROVIDER_KEY = "provider";

    //key value of the APIImpl rxt
    public static final String API_KEY = "api";
    
    //governance registry apimgt root location
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";
    
    public static final String API_CONTEXT_ID = "api.context.id";
    //This is the resource name of API
    public static final String API_RESOURCE_NAME ="/api";

    //Association between documentation and its content
    public static final String DOCUMENTATION_CONTENT_ASSOCIATION = "hasContent";

    public static final String DOCUMENTATION_KEY = "document";

    //association type between provider and APIImpl
    public static final String PROVIDER_ASSOCIATION = "provides";

    //association type between API and Documentation
    public static final String DOCUMENTATION_ASSOCIATION = "document";

    //registry location of providers
    public static final String PROVIDERS_PATH = "/providers";
    
    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION +"/applicationdata";

    //registry location of API
    public static final String API_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";
    
    public static final String API_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/tiers.xml";

    public static final String API_IMAGE_LOCATION = API_APPLICATION_DATA_LOCATION + "/icons";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";
    
    //registry location for API documentation
    public static final String API_DOC_LOCATION = API_APPLICATION_DATA_LOCATION + "/api-docs";
    
    //registry location for Custom sequences
    public static final String API_CUSTOM_SEQUENCE_LOCATION = APIMGT_REGISTRY_LOCATION+"/customsequences";
    
    public static final String API_CUSTOM_INSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/in/";
  
    public static final String API_CUSTOM_OUTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/out/";
    
    //registry location for secure vault passwords
    public static final String API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION = "/repository/components/secure-vault";
  
   //registry location for wsdl files
    public static final String API_WSDL_RESOURCE_LOCATION = API_APPLICATION_DATA_LOCATION + "/wsdls/";
    
    public static final String API_DOC_RESOURCE_NAME = "api-doc.json";
    
    public static final String API_DEFINITION_DOC_NAME = "Swagger API Definition";

    public static final String API_ICON_IMAGE = "icon";
    
    public static final String API_GLOBAL_VISIBILITY = "public";

    public static final String API_RESTRICTED_VISIBILITY = "restricted";

    public static final String API_CONTROLLED_VISIBILITY = "controlled";

    public static final String ACCESS_TOKEN_STORE_TABLE = "IDN_OAUTH2_ACCESS_TOKEN";

    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    // Those constance are used in API artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
    public static final String API_OVERVIEW_ENDPOINT_URL = "overview_endpointURL";
    public static final String API_OVERVIEW_SANDBOX_URL = "overview_sandboxURL";
    public static final String API_OVERVIEW_WSDL = "overview_wsdl";
    public static final String API_OVERVIEW_WADL = "overview_wadl";
    public static final String API_OVERVIEW_PROVIDER = "overview_provider";
    public static final String API_OVERVIEW_THUMBNAIL_URL="overview_thumbnail";
    public static final String API_OVERVIEW_STATUS="overview_status";
    public static final String API_OVERVIEW_TIER="overview_tier";
    public static final String API_OVERVIEW_IS_LATEST ="overview_isLatest";
    public static final String API_URI_TEMPLATES ="uriTemplates_entry";
    public static final String API_OVERVIEW_TEC_OWNER ="overview_technicalOwner";
    public static final String API_OVERVIEW_TEC_OWNER_EMAIL ="overview_technicalOwnerEmail";
    public static final String API_OVERVIEW_BUSS_OWNER ="overview_businessOwner";
    public static final String API_OVERVIEW_BUSS_OWNER_EMAIL ="overview_businessOwnerEmail";
    public static final String API_OVERVIEW_VISIBILITY ="overview_visibility";
    public static final String API_OVERVIEW_VISIBLE_ROLES ="overview_visibleRoles";
    public static final String API_OVERVIEW_VISIBLE_TENANTS ="overview_visibleTenants";
    public static final String API_STATUS = "STATUS";
    public static final String API_URI_PATTERN ="URITemplate_urlPattern";
    public static final String API_URI_HTTP_METHOD ="URITemplate_httpVerb";
    public static final String API_URI_AUTH_TYPE ="URITemplate_authType";
    public static final String API_OVERVIEW_ENDPOINT_SECURED = "overview_endpointSecured";
    public static final String API_OVERVIEW_ENDPOINT_USERNAME = "overview_endpointUsername";
    public static final String API_OVERVIEW_ENDPOINT_PASSWORD = "overview_endpointPpassword";
    public static final String API_OVERVIEW_TRANSPORTS = "overview_transports";
    public static final String API_OVERVIEW_INSEQUENCE = "overview_inSequence";
    public static final String API_OVERVIEW_OUTSEQUENCE = "overview_outSequence";
    
    public static final String API_OVERVIEW_RESPONSE_CACHING = "overview_responseCaching";
    public static final String API_OVERVIEW_CACHE_TIMEOUT = "overview_cacheTimeout";

    public static final String API_OVERVIEW_REDIRECT_URL = "overview_redirectURL";
    public static final String API_OVERVIEW_OWNER = "overview_apiOwner";
    public static final String API_OVERVIEW_ADVERTISE_ONLY = "overview_advertiseOnly";
    public static final String API_OVERVIEW_ENDPOINT_CONFIG = "overview_endpointConfig";
    
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABILITY = "overview_subscriptionAvailability";
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS = "overview_tenants";
    
    //Those constance are used in Provider artifact.
    public static final String PROVIDER_OVERVIEW_NAME= "overview_name";
    public static final String PROVIDER_OVERVIEW_EMAIL = "overview_email";
    public static final String PROVIDER_OVERVIEW_DESCRIPTION = "overview_description";

    //database columns for Subscriber
    public static final String SUBSCRIBER_FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String SUBSCRIBER_FIELD_USER_ID = "USER_ID";
    public static final String SUBSCRIBER_FIELD_DATE_SUBSCRIBED = "DATE_SUBSCRIBED";

    //tables columns for subscription
    public static final String SUBSCRIPTION_FIELD_SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String SUBSCRIPTION_FIELD_TIER_ID = "TIER_ID";
    public static final String SUBSCRIPTION_FIELD_API_ID = "API_ID";
    public static final String SUBSCRIPTION_FIELD_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SUBSCRIPTION_FIELD_LAST_ACCESS = "LAST_ACCESSED";
    public static final String SUBSCRIPTION_FIELD_SUB_STATUS = "SUB_STATUS";

    public static final String SUBSCRIPTION_KEY_TYPE = "KEY_TYPE";
    public static final String SUBSCRIPTION_USER_TYPE = "USER_TYPE";
    public static final String ACCESS_TOKEN_USER_TYPE_APPLICATION = "APPLICATION";
    public static final String USER_TYPE_END_USER = "END_USER";
    public static final String FIELD_API_NAME = "API_NAME";
    public static final String FIELD_CONSUMER_KEY = "CONSUMER_KEY";
    public static final String FIELD_API_PUBLISHER = "API_PROVIDER";

    //table columns for AM_APPLICATION
    public static final String APPLICATION_ID = "APPLICATION_ID";
    public static final String APPLICATION_NAME = "NAME";
    public static final String APPLICATION_SUBSCRIBER_ID = "SUBSCRIBER_ID";
    public static final String APPLICATION_TIER = "APPLICATION_TIER";

    //IDENTITY OAUTH2 table
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_STATE="TOKEN_STATE";
    public static final String IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER = "AUTHZ_USER";
    public static final String IDENTITY_OAUTH2_FIELD_TIME_CREATED = "TIME_CREATED";
    public static final String IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD = "VALIDITY_PERIOD";

    //documentation rxt

    public static final String DOC_NAME= "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_DIR = "documentation";
    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String DOCUMENT_FILE_DIR = "files";
    public static final String DOC_API_BASE_PATH="overview_apiBasePath";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_FILE_PATH = "overview_filePath";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String DOC_OTHER_TYPE_NAME = "overview_otherTypeName";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";
    public static final String DEPRECATED = "DEPRECATED";


    public static class TokenStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String BLOCKED = "BLOCKED";
        public static final String REVOKED = "REVOKED";
        public static final String INACTIVE = "INACTIVE";
    }
    public static class SubscriptionStatus {
        public static final String BLOCKED = "BLOCKED";
        public static final String PROD_ONLY_BLOCKED = "PROD_ONLY_BLOCKED";
        public static final String UNBLOCKED = "UNBLOCKED";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String REJECTED = "REJECTED";
    }

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final int TOP_TATE_MARGIN = 4;
    
    public static final class Permissions {
        public static final String API_CREATE = "/permission/admin/manage/api/create";
        public static final String API_PUBLISH = "/permission/admin/manage/api/publish";
        public static final String API_SUBSCRIBE = "/permission/admin/manage/api/subscribe";
        public static final String API_WORKFLOWADMIN = "/permission/admin/manage/workflowadmin";
        public static final String MANAGE_TIERS = "/permission/admin/manage/manage_tiers";
    }
    
    public static final String API_GATEWAY = "APIGateway.";
    public static final String API_GATEWAY_SERVER_URL = "ServerURL";
    public static final String API_GATEWAY_USERNAME = "Username";
    public static final String API_GATEWAY_PASSWORD = "Password";
    public static final String API_GATEWAY_KEY_CACHE_ENABLED = API_GATEWAY + "EnableGatewayKeyCache";
    public static final String API_GATEWAY_ENDPOINT = "GatewayEndpoint";
    public static final String API_GATEWAY_CLIENT_DOMAIN_HEADER = API_GATEWAY + "ClientDomainHeader";
    public static final String API_GATEWAY_TYPE = "GatewayType";
    public static final String API_GATEWAY_TYPE_SYNAPSE = "Synapse";
    
    public static final String API_KEY_MANAGER = "APIKeyManager.";
    public static final String API_KEY_MANAGER_URL = API_KEY_MANAGER + "ServerURL";
    public static final String API_KEY_MANAGER_TOKEN_ENDPOINT_NAME = API_KEY_MANAGER + "TokenEndPointName";
    public static final String API_KEY_MANAGER_USERNAME = API_KEY_MANAGER + "Username";
    public static final String API_KEY_MANAGER_PASSWORD = API_KEY_MANAGER + "Password";
    public static final String API_KEY_MANAGER_APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD = API_KEY_MANAGER + "ApplicationTokenDefaultValidityPeriod";
    public static final String API_KEY_MANGER_THRIFT_CLIENT_PORT = API_KEY_MANAGER + "ThriftClientPort";
    public static final String API_KEY_MANGER_THRIFT_SERVER_PORT = API_KEY_MANAGER + "ThriftServerPort";
    public static final String API_KEY_MANGER_THRIFT_SERVER_HOST = API_KEY_MANAGER + "ThriftServerHost";
    public static final String API_KEY_MANGER_CONNECTION_TIMEOUT = API_KEY_MANAGER + "ThriftClientConnectionTimeOut";
    public static final String API_KEY_MANAGER_THRIFT_SERVER_HOST = API_KEY_MANAGER + "ThriftServerHost";
    public static final String API_KEY_VALIDATOR_CLIENT_TYPE = API_KEY_MANAGER + "KeyValidatorClientType";
    public static final String API_KEY_VALIDATOR_WS_CLIENT = "WSClient";
    public static final String API_KEY_MANAGER_ENABLE_THRIFT_SERVER = API_KEY_MANAGER + "EnableThriftServer";
    public static final String API_KEY_VALIDATOR_THRIFT_CLIENT = "ThriftClient";
    public static final String API_KEY_SECURITY_CONTEXT_TTL = API_KEY_MANAGER + "SecurityContextTTL";
    public static final String API_KEY_MANAGER_ENABLE_JWT_CACHE = API_KEY_MANAGER + "EnableJWTCache";
    public static final String API_KEY_MANAGER_ENABLE_VALIDATION_INFO_CACHE = API_KEY_MANAGER + "EnableKeyMgtValidationInfoCache";
    public static final String API_KEY_MANAGER_REMOVE_USERNAME_TO_JWT_FOR_APP_TOKEN = API_KEY_MANAGER + "RemoveUserNameToJWTForApplicationToken";
    public static final String API_KEY_MANAGER_ENABLE_ASSERTIONS = API_KEY_MANAGER + "EnableAssertions.";
    public static final String API_KEY_MANAGER_ENABLE_ASSERTIONS_USERNAME = API_KEY_MANAGER_ENABLE_ASSERTIONS + "UserName";
    public static final String API_KEY_MANAGER_ENABLE_ACCESS_TOKEN_PARTITIONING = API_KEY_MANAGER + "AccessTokenPartitioning." + "EnableAccessTokenPartitioning";
    public static final String API_KEY_MANAGER_ACCESS_TOKEN_PARTITIONING_DOMAINS = API_KEY_MANAGER + "AccessTokenPartitioning." + "AccessTokenPartitioningDomains";
    public static final String API_KEY_MANAGER_ENCRYPT_TOKENS = API_KEY_MANAGER + "EncryptPersistedTokens";

    public static final String API_STORE = "APIStore.";
    public static final String API_STORE_URL = "APIStore."+"URL";
    public static final String API_STORE_DISPLAY_ALL_APIS = API_STORE + "DisplayAllAPIs";
    public static final String API_STORE_DISPLAY_MULTIPLE_VERSIONS = API_STORE + "DisplayMultipleVersions";
    public static final String API_STORE_DISPLAY_COMMENTS = API_STORE + "DisplayComments";
    public static final String API_STORE_DISPLAY_RATINGS = API_STORE + "DisplayRatings";
    public static final String API_STORE_TAG_CACHE_DURATION = API_STORE + "TagCacheDuration";

    public static final String API_PUBLISHER = "APIPublisher.";
    public static final String SHOW_API_PUBLISHER_URL_FROM_STORE = "APIPublisher."+"DisplayURL";
    public static final String API_PUBLISHER_URL = "APIPublisher."+"URL";

    public static final String WSO2_API_STORE_TYPE = "wso2";

    public static final String EXTERNAL_API_STORES = "ExternalAPIStores";
    public static final String LOGIN_CONFIGS = "LoginConfig";
    public static final String EXTERNAL_API_STORES_STORE_URL = EXTERNAL_API_STORES + ".StoreURL";
    public static final String EXTERNAL_API_STORE = "ExternalAPIStore";
    public static final String EXTERNAL_API_STORE_ID = "id";
    public static final String EXTERNAL_API_STORE_TYPE = "type";
    public static final String EXTERNAL_API_STORE_DISPLAY_NAME = "DisplayName";
    public static final String EXTERNAL_API_STORE_ENDPOINT = "Endpoint";
    public static final String EXTERNAL_API_STORE_USERNAME = "Username";
    public static final String EXTERNAL_API_STORE_PASSWORD ="Password";


 
    public static final String AUTH_MANAGER = "AuthManager.";
    public static final String AUTH_MANAGER_URL = AUTH_MANAGER + "ServerURL";
    public static final String AUTH_MANAGER_USERNAME = AUTH_MANAGER + "Username";
    public static final String AUTH_MANAGER_PASSWORD = AUTH_MANAGER + "Password";
    
    public static final String SELF_SIGN_UP = "SelfSignUp.";
    public static final String SELF_SIGN_UP_ENABLED = SELF_SIGN_UP + "Enabled";
    public static final String SELF_SIGN_UP_ROLE = SELF_SIGN_UP + "SubscriberRoleName";
    public static final String SELF_SIGN_UP_CREATE_ROLE = SELF_SIGN_UP + "CreateSubscriberRole";

    public static final String STATUS_OBSERVERS = "StatusObservers.";
    public static final String OBSERVER = STATUS_OBSERVERS + "Observer";
    
    public static final String CORS_CONFIGURATION = "CORSConfiguration.";
    public static final String CORS_CONFIGURATION_ENABLED = CORS_CONFIGURATION + "Enabled";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN = CORS_CONFIGURATION + "Access-Control-Allow-Origin";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS = CORS_CONFIGURATION + "Access-Control-Allow-Headers";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS = CORS_CONFIGURATION + "Access-Control-Allow-Methods";
    
    
    public static final String API_KEY_TYPE = "AM_KEY_TYPE";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";

    public static final String BILLING_AND_USAGE_CONFIGURATION = "EnableBillingAndUsage";
    
    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";

    public static final QName POLICY_ELEMENT = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy",
                      "Policy");
    public static final QName ASSERTION_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "MediatorThrottleAssertion");
    public static final QName THROTTLE_ID_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "ID");
    public static final QName THROTTLE_ID_DISPLAY_NAME_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
            "displayName");
    public static final String TIER_DESCRIPTION_PREFIX = "tier.desc.";
    
    public static final String TIER_MANAGEMENT = "TierManagement.";
    public static final String ENABLE_UNLIMITED_TIER = TIER_MANAGEMENT + "EnableUnlimitedTier";
    
    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String UNLIMITED_TIER_DESC = "Allows unlimited requests";

    public static final String UNAUTHENTICATED_TIER = "Unauthenticated";
    
    public static final int AM_CREATOR_APIMGT_EXECUTION_ID = 200;
    public static final int AM_CREATOR_GOVERNANCE_EXECUTION_ID = 201;
    public static final int AM_PUBLISHER_APIMGT_EXECUTION_ID = 202;
    public static final QName THROTTLE_CONTROL_ELEMENT = new QName("http://www.wso2.org/products/wso2commons/throttle",
                        "Control");
    public static final QName THROTTLE_MAXIMUM_COUNT_ELEMENT = new QName("http://www.wso2"
            +".org/products/wso2commons/throttle", "MaximumCount");
    public static final QName THROTTLE_UNIT_TIME_ELEMENT = new QName("http://www.wso2"
                                                                     + ".org/products/wso2commons/throttle", "UnitTime");
    public static final QName THROTTLE_ATTRIBUTES_ELEMENT = new QName("http://www.wso2"
                                                                      + ".org/products/wso2commons/throttle", "Attributes");
    public static final String THROTTLE_ATTRIBUTE_DISPLAY_NAME= "displayName";

    public static final String TIER_DESC_NOT_AVAILABLE = "Tire Description is not available";
    
    public static final String AUTH_TYPE_DEFAULT = "DEFAULT";
    public static final String AUTH_TYPE_NONE = "NONE";
    public static final String AUTH_TYPE_USER = "USER";
    public static final String AUTH_TYPE_APP = "APP";

    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    
    public static final String TIER_PERMISSION_ALLOW = "allow";
    
    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "current_tenant";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "all_tenants";
    public static final String SUBSCRIPTION_TO_SPECIFIC_TENANTS = "specific_tennats";

    //TODO: move this to a common place (& Enum) to be accessible by all components
    public static class KeyValidationStatus {
        public static final int API_AUTH_GENERAL_ERROR       = 900900;
        public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
        public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
        public static final int API_AUTH_ACCESS_TOKEN_EXPIRED = 900903;
        public static final int API_AUTH_ACCESS_TOKEN_INACTIVE = 900904;
        public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
        public static final int API_AUTH_INCORRECT_API_RESOURCE = 900906;
        public static final int API_BLOCKED = 900907;
        public static final int SUBSCRIPTION_INACTIVE = 900909;
    }

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";

    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";

    //API caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";
    public static final String API_CONTEXT_CACHE_MANAGER = "API_CONTEXT_CACHE_MANAGER";
    public static final String RESOURCE_CACHE_NAME = "resourceCache";
    public static final String KEY_CACHE_NAME = "keyCache";
    public static final String JWT_CACHE_NAME = "jwtCache";
    public static final String API_CONTEXT_CACHE = "apiContextCache";
    public static final int API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS = 3650 ;

    //URI Authentication Schemes
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String NO_MATCHING_AUTH_SCHEME = "noMatchedAuthScheme";

    public static final String EVERYONE_ROLE = "internal/everyone";
    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    public static final String API_ACTION="action";
    public static final String API_ADD_ACTION="addAPI";
    public static final String API_UPDATE_ACTION="updateAPI";
    public static final String API_CHANGE_STATUS_ACTION="updateStatus";
    public static final String API_REMOVE_ACTION="removeAPI";
    public static final String API_LOGIN_ACTION="login";
    public static final String API_LOGOUT_ACTION="logout";
    public static final String APISTORE_LOGIN_USERNAME="username";
    public static final String APISTORE_LOGIN_PASSWORD="password";
    public static final String APISTORE_LOGIN_URL="/site/blocks/user/login/ajax/login.jag";
    public static final String APISTORE_PUBLISH_URL="/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String APISTORE_ADD_URL="/site/blocks/item-add/ajax/add.jag";
    public static final String APISTORE_DELETE_URL="/site/blocks/item-add/ajax/remove.jag";

    public static final String SWAGGER_VERSION = "1.1";
    
    public static class OperationParameter {
    	public static final String AUTH_PARAM_NAME = "Authorization";
    	public static final String AUTH_PARAM_DESCRIPTION = "Access Token";
    	public static final String AUTH_PARAM_TYPE = "header";
    	public static final String PAYLOAD_PARAM_NAME = "Payload";
    	public static final String PAYLOAD_PARAM_DESCRIPTION = "Request Payload";
    	public static final String QUERY_PARAM_NAME = "Query Parameters";
    	public static final String QUERY_PARAM_DESCRIPTION = "Request Query Parameters";
    	public static final String PAYLOAD_PARAM_TYPE = "body";
    }
    
    public static class CORSHeaders {
    	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    	
    	public static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "authorization,Access-Control-Allow-Origin,Content-Type";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "GET,POST,PUT,DELETE,OPTIONS";
    }

    public static final String EXTENSION_HANDLER_POSITION = "ExtensionHandlerPosition";

    public static final String GATEWAY_ENV_TYPE_HYBRID = "hybrid";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";
    
    public static final String API_RESPONSE_CACHE_ENABLED = "Enabled";
    public static final String API_RESPONSE_CACHE_DISABLED = "Disabled";
    public static final int API_RESPONSE_CACHE_TIMEOUT = 300;
    
    
    public static class ApplicationStatus {
       public static final String APPLICATION_CREATED = "CREATED";
       public static final String APPLICATION_APPROVED = "APPROVED"; 
       public static final String APPLICATION_REJECTED = "REJECTED"; 
       public static final String APPLICATION_ONHOLD = "ON_HOLD"; 
          
    }
    
    //key  of the endpoint securevault
    public static final String API_SECUREVAULT_ENABLE = "EnableSecureVault";
}
