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
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    
    public static final String DOCUMENTATION_FILE_ASSOCIATION = "hasFile";

    public static final String DOCUMENTATION_KEY = "document";

    //association type between provider and APIImpl
    public static final String PROVIDER_ASSOCIATION = "provides";

    //association type between API and Documentation
    public static final String DOCUMENTATION_ASSOCIATION = "document";

    //registry location of providers
    public static final String PROVIDERS_PATH = "/providers";
    
    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION +"/applicationdata";

	// Registry location where descriptions and thumbnails of the tags are
	// stored.
	public static final String TAGS_INFO_ROOT_LOCATION = API_APPLICATION_DATA_LOCATION + "/tags";
    
    //registry location of API
    public static final String API_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";

    public static final String API_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/tiers.xml";

    public static final String APP_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/app-tiers.xml";

    public static final String RES_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/res-tiers.xml";

    public static final String COMMERCIAL_TIER_PLAN = "COMMERCIAL";

    public static final int TIER_API_TYPE = 0;

    public static final int TIER_RESOURCE_TYPE = 1;

    public static final int TIER_APPLICATION_TYPE = 2;

    public static final String DEFAULT_API_TIER_FILE_NAME = "default-tiers.xml";

    public static final String DEFAULT_APP_TIER_FILE_NAME = "default-app-tiers.xml";

    public static final String DEFAULT_RES_TIER_FILE_NAME = "default-res-tiers.xml";

    public static final String APPLICATION_JSON_MEDIA_TYPE = "application/json";

    public static final String API_TENANT_CONF = "tenant-conf.json";

    public static final String API_TENANT_CONF_LOCATION = API_APPLICATION_DATA_LOCATION + "/" + API_TENANT_CONF;

    public static final String RESOURCE_FOLDER_LOCATION = "repository"+ File.separator + "resources";

    public static final String API_TENANT_CONF_ENABLE_MONITZATION_KEY = "EnableMonetization";

    public static final String API_TENANT_CONF_IS_UNLIMITED_TIER_PAID = "IsUnlimitedTierPaid";

    public static final String API_CATEGORY_FREE = "Free";

    public static final String API_CATEGORY_FREEMIUM = "Freemium";

    public static final String API_CATEGORY_PAID = "Paid";

    public static final String SSL_VERIFY_CLIENT = "SSLVerifyClient";

    public static final String SSL_VERIFY_CLIENT_STATUS_REQUIRE = "require";

    //location for custom url domain mapings. "<tenant-id>" will be replaced by actual tenant name.
    public static final String API_DOMAIN_MAPPINGS = "/customurl/api-cloud/<tenant-id>/urlMapping/<tenant-id>";
    //domain mapping app keys
    public static final String API_DOMAIN_MAPPINGS_GATEWAY = "gateway";
    public static final String API_DOMAIN_MAPPINGS_STORE = "store";


    public static final String API_IMAGE_LOCATION = API_APPLICATION_DATA_LOCATION + "/icons";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";
    
    //registry location for API documentation
    public static final String API_DOC_LOCATION = API_APPLICATION_DATA_LOCATION + "/api-docs";
    
    public static final String API_DOC_1_2_LOCATION = "1.2";
    
    //registry location for Custom sequences
    public static final String API_CUSTOM_SEQUENCE_LOCATION = APIMGT_REGISTRY_LOCATION+"/customsequences";
    
    public static final String API_CUSTOM_INSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/in/";
  
    public static final String API_CUSTOM_OUTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/out/";

    public static final String API_CUSTOM_FAULTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION +"/fault/";
    
    // registry location for secure vault passwords
    public static final String API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION = "/repository/components/secure-vault";

    // registry location of the governance component
    public static final String GOVERNANCE_COMPONENT_REGISTRY_LOCATION = "/repository/components/org.wso2.carbon" +
                                                                     ".governance";
  
    // registry location for wsdl files
    public static final String API_WSDL_RESOURCE_LOCATION = API_APPLICATION_DATA_LOCATION + "/wsdls/";
    
    public static final String API_DOC_RESOURCE_NAME = "api-doc.json";
    
    public static final String API_DOC_1_2_RESOURCE_NAME = "/api-doc";
    public static final String API_DOC_2_0_RESOURCE_NAME = "swagger.json";

    public static final String API_ICON_IMAGE = "icon";
    
    public static final String API_GLOBAL_VISIBILITY = "public";
    public static final String VISIBILITY = "visibility";

    public static final String API_RESTRICTED_VISIBILITY = "restricted";
    
    public static final String API_PRIVATE_VISIBILITY = "private";

    public static final String API_CONTROLLED_VISIBILITY = "controlled";
    public static final String DOC_API_BASED_VISIBILITY = "API_LEVEL";
    public static final String DOC_OWNER_VISIBILITY = "OWNER_ONLY";
    public static final String DOC_SHARED_VISIBILITY = "PRIVATE";

    public static final String ACCESS_TOKEN_STORE_TABLE = "IDN_OAUTH2_ACCESS_TOKEN";
    public static final String TOKEN_SCOPE_ASSOCIATION_TABLE = "IDN_OAUTH2_ACCESS_TOKEN_SCOPE";
    public static final String CONSUMER_KEY_SECRET_TABLE = "IDN_OAUTH_CONSUMER_APPS";

    public static final String CONSUMER_KEY_ACCESS_TOKEN_MAPPING_TABLE = "CONSUMER_KEY_ACCESS_TOKEN_MAPPING";
    
    public static final String EXTERNAL_API_STORES_LOCATION = APIMGT_REGISTRY_LOCATION + "/externalstores/external-api-stores.xml";
    
    public static final String GA_CONFIGURATION_LOCATION = APIMGT_REGISTRY_LOCATION + "/statistics/ga-config.xml";

    public static final String WORKFLOW_EXECUTOR_LOCATION = API_APPLICATION_DATA_LOCATION + "/workflow-extensions.xml";

    public static final String WORKFLOW_MEDIA_TYPE = "workflow-config";
    
    //registry resource containing the self signup user config
    public static final String SELF_SIGN_UP_CONFIG_LOCATION = API_APPLICATION_DATA_LOCATION + "/sign-up-config.xml";
    public static final String SELF_SIGN_UP_CONFIG_MEDIA_TYPE =  "signup-config";

    public static final String BAM_SERVER_PROFILE_LOCATION = "bamServerProfiles/bam-profile";
    
    public static final String DOCUMENTATION_SEARCH_PATH_FIELD = "path";
    
    public static final String DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD = "mediaType";
    
    public static final String DOCUMENTATION_INLINE_CONTENT_TYPE = "text/plain";
    
    public static final String DOCUMENTATION_SEARCH_TYPE_PREFIX = "Doc";

    public static final String DOCUMENTATION_RESOURCE_MAP_DATA = "Data";
    public static final String DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE = "contentType";
    public static final String DOCUMENTATION_RESOURCE_MAP_NAME = "name";

    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    // Those constance are used in API artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_VERSION_TYPE = "overview_versionType";
    public static final String API_OVERVIEW_IS_DEFAULT_VERSION = "overview_isDefaultVersion";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_OVERVIEW_CONTEXT_TEMPLATE = "overview_contextTemplate";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
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
    public static final String API_OVERVIEW_ENVIRONMENTS = "overview_environments";
    public static final String API_PROVIDER = "Provider";
    public static final String API_NAME = "Name";
    public static final String API_VERSION_LABEL = "Version";
    public static final String API_CONTEXT = "Context";
    public static final String API_DESCRIPTION = "Description";
    public static final String API_OVERVIEW_TAG = "tags";
    public static final String API_TAG = "Tag";
    public static final String API_STATUS = "STATUS";
    public static final String API_URI_PATTERN ="URITemplate_urlPattern";
    public static final String API_URI_HTTP_METHOD ="URITemplate_httpVerb";
    public static final String API_URI_AUTH_TYPE ="URITemplate_authType";
    public static final String API_URI_MEDIATION_SCRIPT ="URITemplate_mediationScript";
    public static final String API_OVERVIEW_ENDPOINT_SECURED = "overview_endpointSecured";
    public static final String API_OVERVIEW_ENDPOINT_AUTH_DIGEST = "overview_endpointAuthDigest";
    public static final String API_OVERVIEW_ENDPOINT_USERNAME = "overview_endpointUsername";
    public static final String API_OVERVIEW_ENDPOINT_PASSWORD = "overview_endpointPpassword";
    public static final String API_OVERVIEW_TRANSPORTS = "overview_transports";
    public static final String API_OVERVIEW_INSEQUENCE = "overview_inSequence";
    public static final String API_OVERVIEW_OUTSEQUENCE = "overview_outSequence";
    public static final String API_OVERVIEW_FAULTSEQUENCE = "overview_faultSequence";
    
    public static final String API_OVERVIEW_RESPONSE_CACHING = "overview_responseCaching";
    public static final String API_OVERVIEW_CACHE_TIMEOUT = "overview_cacheTimeout";

    public static final String PROTOTYPE_OVERVIEW_IMPLEMENTATION = "overview_implementation";
    public static final String API_PRODUCTION_THROTTLE_MAXTPS = "overview_productionTps";
    public static final String API_SANDBOX_THROTTLE_MAXTPS = "overview_sandboxTps";

    public static final String IMPLEMENTATION_TYPE_ENDPOINT = "ENDPOINT";
    public static final String IMPLEMENTATION_TYPE_INLINE = "INLINE";

    public static final String API_OVERVIEW_REDIRECT_URL = "overview_redirectURL";
    public static final String API_OVERVIEW_OWNER = "overview_apiOwner";
    public static final String API_OVERVIEW_ADVERTISE_ONLY = "overview_advertiseOnly";
    public static final String API_OVERVIEW_ENDPOINT_CONFIG = "overview_endpointConfig";
    
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABILITY = "overview_subscriptionAvailability";
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS = "overview_tenants";
    
    public static final String API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED = "overview_destinationStatsEnabled";
    
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
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE="TOKEN_SCOPE";
    public static final String IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER = "AUTHZ_USER";
    public static final String IDENTITY_OAUTH2_FIELD_TIME_CREATED = "TIME_CREATED";
    public static final String IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD = "VALIDITY_PERIOD";
    public static final String IDENTITY_OAUTH2_FIELD_USER_DOMAIN = "USER_DOMAIN";

    public static final String API_CONSUMER_AUTHENTICATION = "APIConsumerAuthentication.";
    public static final String TOKEN_GENERATOR_IMPL = API_CONSUMER_AUTHENTICATION+"TokenGeneratorImpl";
    public static final String ENABLE_JWT_GENERATION = API_CONSUMER_AUTHENTICATION+"EnableTokenGeneration";
    public static final String CLAIM_CACHE_EXPIRY = API_CONSUMER_AUTHENTICATION+ "APIMClaimCacheExpiry";

    //documentation rxt

    public static final String DOC_NAME= "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_VISIBILITY = "overview_visibility";
    public static final String DOC_DIR = "documentation";
    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String NO_CONTENT_UPDATE = "no_content_update";
    public static final String DOCUMENT_FILE_DIR = "files";
    public static final String DOC_API_BASE_PATH="overview_apiBasePath";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_FILE_PATH = "overview_filePath";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String DOC_OTHER_TYPE_NAME = "overview_otherTypeName";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";
    public static final String DEPRECATED = "DEPRECATED";
    public static final String PROTOTYPED = "PROTOTYPED";
    public static final String VERB_INFO_DTO = "VERB_INFO";

    //Registry lifecycle related info
    public static final String API_LIFE_CYCLE="APILifeCycle";
    public static final String LC_NEXT_STATES="nextStates";
    public static final String LC_PROPERTY_LIFECYCLE_NAME_PREFIX="registry.lifecycle.";
    public static final String LC_PROPERTY_CHECKLIST_PREFIX="registry.custom_lifecycle.checklist.";
    public static final String LC_PROPERTY_STATE_SUFFIX=".state";
    public static final String LC_PROPERTY_PERMISSION_SUFFIX=".item.permission";
    public static final String LC_PROPERTY_ITEM_SUFFIX=".item";
    public static final String LC_STATUS="status:";
    public static final String LC_CHECK_ITEMS="items";
    public static final String LC_CHECK_ITEM_NAME="name:";
    public static final String LC_CHECK_ITEM_VALUE="value:";
    public static final String LC_CHECK_ITEM_ORDER="order:";

    public static class TokenStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String BLOCKED = "BLOCKED";
        public static final String REVOKED = "REVOKED";
        public static final String INACTIVE = "INACTIVE";
        
        private TokenStatus(){
            
        }
    }
    public static class SubscriptionStatus {
        public static final String BLOCKED = "BLOCKED";
        public static final String PROD_ONLY_BLOCKED = "PROD_ONLY_BLOCKED";
        public static final String UNBLOCKED = "UNBLOCKED";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String REJECTED = "REJECTED";
        
        private SubscriptionStatus(){
            
        }
    }

    public enum OAuthAppMode {
        CREATED, MAPPED
    }

    public static class SubscriptionCreatedStatus   {
        public static final String SUBSCRIBE = "SUBSCRIBE";
        public static final String UN_SUBSCRIBE = "UN_SUBSCRIBE";
        
        private SubscriptionCreatedStatus(){
            
        }
    }

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final int TOP_TATE_MARGIN = 4;
    
    public static final class Permissions {
        public static final String API_CREATE = "/permission/admin/manage/api/create";
        public static final String API_PUBLISH = "/permission/admin/manage/api/publish";
        public static final String API_SUBSCRIBE = "/permission/admin/manage/api/subscribe";
        public static final String API_WORKFLOWADMIN = "/permission/admin/manage/workflowadmin";
        public static final String MANAGE_TIERS = "/permission/admin/manage/manage_tiers";
        
        private Permissions(){
            
        }
    }
    
    public static final String API_GATEWAY = "APIGateway.";
    public static final String API_GATEWAY_SERVER_URL = "ServerURL";
    public static final String API_GATEWAY_USERNAME = "Username";
    public static final String API_GATEWAY_PASSWORD = "Password";
    public static final String API_GATEWAY_KEY_CACHE_ENABLED = API_GATEWAY + "EnableGatewayKeyCache";
    public static final String API_GATEWAY_RESOURCE_CACHE_ENABLED = API_GATEWAY + "EnableGatewayResourceCache";
    public static final String API_GATEWAY_ENDPOINT = "GatewayEndpoint";
    public static final String API_GATEWAY_CLIENT_DOMAIN_HEADER = API_GATEWAY + "ClientDomainHeader";
    public static final String API_GATEWAY_TYPE = "GatewayType";
    public static final String API_GATEWAY_TYPE_SYNAPSE = "Synapse";
    public static final String API_GATEWAY_NONE = "none";
    public static final String GATEWAY_STATS_SERVICE = "GatewayStatsUpdateService";

    public static final String API_KEY_VALIDATOR = "APIKeyValidator.";
    public static final String API_KEY_VALIDATOR_URL = API_KEY_VALIDATOR + "ServerURL";
    public static final String API_KEY_VALIDATOR_REVOKE_API_URL = API_KEY_VALIDATOR + "RevokeAPIURL";
    public static final String API_KEY_VALIDATOR_TOKEN_ENDPOINT_NAME = API_KEY_VALIDATOR + "TokenEndPointName";
    public static final String API_KEY_VALIDATOR_USERNAME = API_KEY_VALIDATOR + "Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = API_KEY_VALIDATOR + "Password";
    public static final String API_KEY_VALIDATOR_APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD = API_KEY_VALIDATOR
            + "ApplicationTokenDefaultValidityPeriod";
    public static final String API_KEY_VALIDATOR_THRIFT_CLIENT_PORT = API_KEY_VALIDATOR + "ThriftClientPort";
    public static final String API_KEY_VALIDATOR_THRIFT_SERVER_PORT = API_KEY_VALIDATOR + "ThriftServerPort";
    public static final String API_KEY_VALIDATOR_THRIFT_SERVER_HOST = API_KEY_VALIDATOR + "ThriftServerHost";
    public static final String API_KEY_VALIDATOR_CONNECTION_TIMEOUT = API_KEY_VALIDATOR + "ThriftClientConnectionTimeOut";
    public static final String API_KEY_VALIDATOR_KEY_CACHE_EXPIRY = API_KEY_VALIDATOR + "APIMKeyCacheExpiry";

    // Constants needed for KeyManager section
    public static final String API_KEY_MANAGER = "APIKeyManager.";
    public static final String KEY_MANAGER_CLIENT = API_KEY_MANAGER+"KeyManagerClientImpl";
    public static final String KEY_MANAGER = "KeyManager";
    public static final String KEY_MANAGER_USERNAME = "Username";
    public static final String KEY_MANAGER_PASSWORD = "Password";
    public static final String AUTHSERVER_URL = "ServerURL";

    public static final String TOKEN_URL = "TokenURL";
    public static final String REVOKE_URL = "RevokeURL";


    public static final String DEVICE_SCOPE_PATTERN = "^device_.*";
    public static final String OPEN_ID_SCOPE_NAME = "openid";
    public static final String API_KEY_MANGER_VALIDATIONHANDLER_CLASS_NAME = API_KEY_VALIDATOR
            + "KeyValidationHandlerClassName";
    public static final String API_KEY_MANGER_SCOPE_WHITELIST = API_KEY_VALIDATOR + "ScopeWhitelist.Scope";

    public static final String API_RESTAPI = "RESTAPI.";
    public static final String API_RESTAPI_WHITELISTED_URI = API_RESTAPI + "WhiteListedURIs.WhiteListedURI.";
    public static final String API_RESTAPI_WHITELISTED_URI_URI = API_RESTAPI_WHITELISTED_URI + "URI";
    public static final String API_RESTAPI_WHITELISTED_URI_HTTPMethods = API_RESTAPI_WHITELISTED_URI + "HTTPMethods";

    public static final String API_KEY_MANAGER_THRIFT_SERVER_HOST = API_KEY_VALIDATOR + "ThriftServerHost";
    public static final String API_KEY_VALIDATOR_CLIENT_TYPE = API_KEY_VALIDATOR + "KeyValidatorClientType";
    public static final String API_KEY_VALIDATOR_WS_CLIENT = "WSClient";
    public static final String API_KEY_VALIDATOR_ENABLE_THRIFT_SERVER = API_KEY_VALIDATOR + "EnableThriftServer";
    public static final String API_KEY_VALIDATOR_THRIFT_CLIENT = "ThriftClient";
    public static final String API_KEY_SECURITY_CONTEXT_TTL = API_KEY_VALIDATOR + "SecurityContextTTL";
    public static final String API_KEY_VALIDATOR_ENABLE_VALIDATION_INFO_CACHE = API_KEY_VALIDATOR
            + "EnableKeyMgtValidationInfoCache";
    public static final String API_KEY_VALIDATOR_REMOVE_USERNAME_TO_JWT_FOR_APP_TOKEN = API_KEY_VALIDATOR
            + "RemoveUserNameFromJWTForApplicationToken";
    public static final String API_KEY_VALIDATOR_ENABLE_ASSERTIONS = API_KEY_VALIDATOR + "EnableAssertions.";
    public static final String API_KEY_MANAGER_ENABLE_ASSERTIONS_USERNAME = API_KEY_VALIDATOR_ENABLE_ASSERTIONS
            + "UserName";
    public static final String API_KEY_MANAGER_ENABLE_ACCESS_TOKEN_PARTITIONING = API_KEY_VALIDATOR
            + "AccessTokenPartitioning." + "EnableAccessTokenPartitioning";
    public static final String API_KEY_MANAGER_ACCESS_TOKEN_PARTITIONING_DOMAINS = API_KEY_VALIDATOR
            + "AccessTokenPartitioning." + "AccessTokenPartitioningDomains";
    public static final String API_KEY_VALIDATOR_ENCRYPT_TOKENS = API_KEY_VALIDATOR + "EncryptPersistedTokens";
    public static final String API_KEY_VALIDATOR_APPLICATION_TOKEN_SCOPE = API_KEY_VALIDATOR + "ApplicationTokenScope";


    public static final String API_STORE = "APIStore.";
    public static final String SHOW_API_STORE_URL_FROM_PUBLISHER = API_STORE + "DisplayURL";
    public static final String API_STORE_URL = API_STORE + "URL";
    public static final String API_STORE_SERVER_URL = API_STORE + "ServerURL";
    public static final String API_STORE_USERNAME = API_STORE + "Username";
    public static final String API_STORE_PASSWORD = API_STORE + "Password";
    public static final String API_STORE_DISPLAY_ALL_APIS = API_STORE + "DisplayAllAPIs";
    public static final String API_STORE_DISPLAY_MULTIPLE_VERSIONS = API_STORE + "DisplayMultipleVersions";
    public static final String API_STORE_DISPLAY_COMMENTS = API_STORE + "DisplayComments";
    public static final String API_STORE_DISPLAY_RATINGS = API_STORE + "DisplayRatings";
    public static final String API_STORE_FORCE_CI_COMPARISIONS = API_STORE + "CompareCaseInsensitively";
    public static final String API_STORE_TAG_CACHE_DURATION = API_STORE + "TagCacheDuration";
    public static final String API_STORE_REUSE_APP_NAME = API_STORE + "ReuseAppName";
    public static final String API_STORE_DISABLE_PERMISSION_CHECK = API_STORE + "DisablePermissionCheck";
    public static final String API_STORE_APIS_PER_PAGE = API_STORE + "APIsPerPage";


    public static final String API_PUBLISHER = "APIPublisher.";
    public static final String SHOW_API_PUBLISHER_URL_FROM_STORE = API_PUBLISHER + "DisplayURL";
    public static final String API_PUBLISHER_URL =  API_PUBLISHER + "URL";
    public static final String API_PUBLISHER_SERVER_URL = API_PUBLISHER + "ServerURL";
    public static final String API_PUBLISHER_USERNAME = API_PUBLISHER + "Username";
    public static final String API_PUBLISHER_PASSWORD = API_PUBLISHER + "Password";
    public static final String API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS = API_PUBLISHER
            + "EnableAPIDocVisibilityLevels";
    public static final String API_PUBLISHER_APIS_PER_PAGE = API_PUBLISHER + "APIsPerPage";
    public static final String WSO2_API_STORE_TYPE = "wso2";

    public static final String EXTERNAL_API_STORES = "ExternalAPIStores";
    public static final String LOGIN_CONFIGS = "LoginConfig";
    public static final String EXTERNAL_API_STORES_STORE_URL = "StoreURL";
    public static final String EXTERNAL_API_STORE = "ExternalAPIStore";
    public static final String EXTERNAL_API_STORE_ID = "id";
    public static final String EXTERNAL_API_STORE_TYPE = "type";
    public static final String EXTERNAL_API_STORE_CLASS_NAME = "className";
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
    
    //elements in the configuration file in the registry related to self signup
    public static final String SELF_SIGN_UP_REG_ROOT = "SelfSignUp";
    public static final String SELF_SIGN_UP_REG_DOMAIN_ELEM = "SignUpDomain";
    public static final String SELF_SIGN_UP_REG_ROLES_ELEM = "SignUpRoles";
    public static final String SELF_SIGN_UP_REG_ROLE_ELEM = "SignUpRole";
    public static final String SELF_SIGN_UP_REG_USERNAME = "AdminUserName";
    public static final String SELF_SIGN_UP_REG_PASSWORD = "AdminPassword";
    public static final String SELF_SIGN_UP_REG_ENABLED = "EnableSignup";
    public static final String SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT = "RoleName";
    public static final String SELF_SIGN_UP_REG_ROLE_IS_EXTERNAL = "IsExternalRole";

    public static final String STATUS_OBSERVERS = "StatusObservers.";
    public static final String OBSERVER = STATUS_OBSERVERS + "Observer";
    
    public static final String CORS_CONFIGURATION = "CORSConfiguration.";
    public static final String CORS_CONFIGURATION_ENABLED = CORS_CONFIGURATION + "Enabled";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN = CORS_CONFIGURATION
            + "Access-Control-Allow-Origin";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS = CORS_CONFIGURATION
            + "Access-Control-Allow-Headers";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS = CORS_CONFIGURATION
            + "Access-Control-Allow-Methods";
    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_CREDENTIALS = CORS_CONFIGURATION
            + "Access-Control-Allow-Credentials";
    
    public static final String API_KEY_TYPE = "AM_KEY_TYPE";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";

    public static final String BILLING_AND_USAGE_CONFIGURATION = "EnableBillingAndUsage";
    
    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";

    public static final QName POLICY_ELEMENT = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy",
                      "Policy");

    public static final String THROTTLE_NAMESPACE = "http://www.wso2.org/products/wso2commons/throttle";

    public static final QName ASSERTION_ELEMENT = new QName(THROTTLE_NAMESPACE, "MediatorThrottleAssertion");
    public static final QName THROTTLE_ID_ELEMENT = new QName(THROTTLE_NAMESPACE,"ID");
    public static final QName THROTTLE_ID_DISPLAY_NAME_ELEMENT = new QName(THROTTLE_NAMESPACE, "displayName");
    public static final String TIER_DESCRIPTION_PREFIX = "tier.desc.";

    public static final String THROTTLE_TIER_DESCRIPTION_ATTRIBUTE = "Description";

    //"Billing plan" and "Stop on quota reach" are considered as x-wso2 type attributes
    public static final String THROTTLE_TIER_PLAN_ATTRIBUTE = "x-wso2-BillingPlan";
    public static final String THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE = "x-wso2-StopOnQuotaReach";

    public static final String TIER_MANAGEMENT = "TierManagement.";
    public static final String ENABLE_UNLIMITED_TIER = TIER_MANAGEMENT + "EnableUnlimitedTier";
    public static final String THROTTLE_POLICY_TEMPLATE =
            "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:throttle=\"http://www.wso2" +
                ".org/products/wso2commons/throttle\">" +
                "<throttle:ID throttle:type=\"ROLE\">%s</throttle:ID>" +
                "<wsp:Policy>" +
                    "<throttle:Control>" +
                        "<wsp:Policy>" +
                            "<throttle:MaximumCount>%d</throttle:MaximumCount>" +
                            "<throttle:UnitTime>%d</throttle:UnitTime>" +
                            "<wsp:Policy>" +
                                "<throttle:Attributes>%s</throttle:Attributes>" +
                            "</wsp:Policy>" +
                        "</wsp:Policy>" +
                    "</throttle:Control>" +
                "</wsp:Policy>" +
            "</wsp:Policy>";

    public static final String THROTTLE_POLICY_ATTRIBUTE_TEMPLATE =
            "<throttle:%s xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">%s</throttle:%s>";

    // This property is used to indicate whether a throttle out event has happened
    // There is a property added to the message context when such an event happens.
    public static final String API_USAGE_THROTTLE_OUT_PROPERTY_KEY = "isThrottleOutIgnored";

    public static final String THROTTLE_OUT_REASON_KEY = "THROTTLED_OUT_REASON";

// The following properties describes the reason for the throttle out.
    public static final String THROTTLE_OUT_REASON_HARD_LIMIT_EXCEEDED = "HARD_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_SOFT_LIMIT_EXCEEDED = "SOFT_LIMIT_EXCEEDED";

    public static final String API_USAGE_TRACKING = "APIUsageTracking.";
    public static final String API_USAGE_ENABLED = "APIUsageTracking.Enabled";
    public static final String API_USAGE_BAM_SERVER_URL_GROUPS = API_USAGE_TRACKING + "DASServerURL";
    public static final String API_USAGE_BUILD_MSG = API_USAGE_TRACKING + "PublishResponseMessageSize";
    public static final String API_USAGE_BAM_SERVER_USER = API_USAGE_TRACKING + "DASUsername";
    public static final String API_USAGE_BAM_SERVER_PASSWORD = API_USAGE_TRACKING + "DASPassword";
    public static final String API_USAGE_SKIP_EVENT_RECEIVER_CONN = API_USAGE_TRACKING + "SkipEventReceiverConnection";
    public static final String API_USAGE_PUBLISHER_CLASS = API_USAGE_TRACKING + "PublisherClass";
    public static final String API_USAGE_DATA_SOURCE_NAME = "WSO2AM_STATS_DB";
    public static final String API_USAGE_STREAMS = API_USAGE_TRACKING + "Streams.";
    public static final String API_USAGE_REQUEST_STREAM = API_USAGE_STREAMS + "Request.";
    public static final String API_USAGE_RESPONSE_STREAM = API_USAGE_STREAMS + "Response.";
    public static final String API_USAGE_FAULT_STREAM = API_USAGE_STREAMS + "Fault.";
    public static final String API_USAGE_THROTTLE_STREAM = API_USAGE_STREAMS + "Throttle.";
    public static final String API_REQUEST_STREAM_NAME = API_USAGE_REQUEST_STREAM + "Name";
    public static final String API_REQUEST_STREAM_VERSION = API_USAGE_REQUEST_STREAM + "Version";
    public static final String API_RESPONSE_STREAM_NAME = API_USAGE_RESPONSE_STREAM + "Name";
    public static final String API_RESPONSE_STREAM_VERSION = API_USAGE_RESPONSE_STREAM + "Version";
    public static final String API_FAULT_STREAM_NAME = API_USAGE_FAULT_STREAM + "Name";
    public static final String API_FAULT_STREAM_VERSION = API_USAGE_FAULT_STREAM + "Version";
    public static final String API_THROTTLE_STREAM_NAME = API_USAGE_THROTTLE_STREAM + "Name";
    public static final String API_THRORRLE_STREAM_VERSION = API_USAGE_THROTTLE_STREAM + "Version";
    public static final String API_USAGE_WF_STREAM = API_USAGE_STREAMS + "Workflow.";
    public static final String API_WF_STREAM_NAME = API_USAGE_WF_STREAM + "Name";
    public static final String API_WF_STREAM_VERSION = API_USAGE_WF_STREAM + "Version";
    //Rest API Config data in api-config.xml
    public static final String API_USAGE_DAS_REST_API_URL = API_USAGE_TRACKING + "DASRestApiURL";
    public static final String API_USAGE_DAS_REST_API_USER = API_USAGE_TRACKING + "DASRestApiUsername";
    public static final String API_USAGE_DAS_REST_API_PASSWORD = API_USAGE_TRACKING + "DASRestApiPassword";
    
    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String UNLIMITED_TIER_DESC = "Allows unlimited requests";

    public static final String UNAUTHENTICATED_TIER = "Unauthenticated";
    
    public static final int AM_CREATOR_APIMGT_EXECUTION_ID = 200;
    public static final int AM_CREATOR_GOVERNANCE_EXECUTION_ID = 201;
    public static final int AM_PUBLISHER_APIMGT_EXECUTION_ID = 202;
    public static final QName THROTTLE_CONTROL_ELEMENT = new QName(THROTTLE_NAMESPACE, "Control");
    public static final QName THROTTLE_MAXIMUM_COUNT_ELEMENT = new QName(THROTTLE_NAMESPACE, "MaximumCount");
    public static final QName THROTTLE_UNIT_TIME_ELEMENT = new QName(THROTTLE_NAMESPACE, "UnitTime");
    public static final QName THROTTLE_ATTRIBUTES_ELEMENT = new QName(THROTTLE_NAMESPACE, "Attributes");
    public static final QName THROTTLE_ATTRIBUTE_ELEMENT = new QName(THROTTLE_NAMESPACE, "Attribute");
    public static final QName THROTTLE_DESCRIPTION_ELEMENT = new QName(THROTTLE_NAMESPACE, "Description");
    public static final QName THROTTLE_TIER_PLAN_ELEMENT = new QName(THROTTLE_NAMESPACE, THROTTLE_TIER_PLAN_ATTRIBUTE);
    public static final String THROTTLE_ATTRIBUTE_DISPLAY_NAME= "displayName";

    public static final String TIER_DESC_NOT_AVAILABLE = "Tire Description is not available";
    
    public static final String AUTH_TYPE_DEFAULT = "DEFAULT";
    public static final String AUTH_TYPE_NONE = "NONE";
    public static final String AUTH_TYPE_USER = "USER";
    public static final String AUTH_TYPE_APP = "APP";

    public static final String TIER_PERMISSION_ALLOW = "allow";
    
    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "current_tenant";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "all_tenants";
    public static final String SUBSCRIPTION_TO_SPECIFIC_TENANTS = "specific_tenants";
    public static final String NO_PERMISSION_ERROR="noPermissions";
    public static final String JSON_PARSE_ERROR="parseErrors";

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
        public static final int API_AUTH_RESOURCE_FORBIDDEN = 900908;
        public static final int SUBSCRIPTION_INACTIVE = 900909;
        public static final int INVALID_SCOPE = 900910;
        
        private KeyValidationStatus(){            
        }
    }

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";

    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";

    //API caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";
    public static final String API_CONTEXT_CACHE_MANAGER = "API_CONTEXT_CACHE_MANAGER";
    public static final String RESOURCE_CACHE_NAME = "resourceCache";
    public static final String GATEWAY_KEY_CACHE_NAME = "gatewayKeyCache";
    public static final String GATEWAY_TOKEN_CACHE_NAME = "GATEWAY_TOKEN_CACHE";
    public static final String KEY_CACHE_NAME = "keyCache";
    public static final String JWT_CACHE_NAME = "jwtCache";
    public static final String API_CONTEXT_CACHE = "apiContextCache";
    public static final String WORKFLOW_CACHE_NAME = "workflowCache";
    public static final String APP_USER_SCOPE_CACHE = "appUserScopeCache";
    public static final String APP_SCOPE_CACHE = "appScopeCache";
    public static final String SELF_SIGN_UP_CONFIG_CACHE = "selfSignupCache";
    public static final String TIERS_CACHE = "tiersCache";
    public static final int API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS = 3650 ;
    public static final String CLAIMS_APIM_CACHE = "claimsLocalCache";

    //URI Authentication Schemes
    public static final Set<String> SUPPORTED_METHODS =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(new String[]{"get","put","post","delete","patch","head","options"})));
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APP_AND_USER ="Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String NO_MATCHING_AUTH_SCHEME = "noMatchedAuthScheme";

    public static final String EVERYONE_ROLE = "internal/everyone";
    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";

    // Anonymous end user, to be used with ACCESS_TOKEN_USER_TYPE_APPLICATION
    public static final String END_USER_ANONYMOUS = "anonymous";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    public static final String API_ACTION="action";
    public static final String API_ADD_ACTION="addAPI";
    public static final String API_GET_ACTION="getAPI";
    public static final String API_UPDATE_ACTION="updateAPI";
    public static final String API_CHANGE_STATUS_ACTION="updateStatus";
    public static final String API_REMOVE_ACTION="removeAPI";
    public static final String API_COPY_ACTION="createNewAPI";
    public static final String API_LOGIN_ACTION="login";
    public static final String API_LOGOUT_ACTION="logout";
    public static final String APISTORE_LOGIN_USERNAME="username";
    public static final String APISTORE_LOGIN_PASSWORD="password";
    public static final String APISTORE_LOGIN_URL="/site/blocks/user/login/ajax/login.jag";
    public static final String APISTORE_PUBLISH_URL="/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String APISTORE_ADD_URL="/site/blocks/item-add/ajax/add.jag";
    public static final String APISTORE_DELETE_URL="/site/blocks/item-add/ajax/remove.jag";
    public static final String APISTORE_LIST_URL="/site/blocks/listing/ajax/item-list.jag";
    public static final String APISTORE_COPY_URL="/site/blocks/overview/ajax/overview.jag";

    public static final String SWAGGER_VERSION = "1.1";
    
    public static class OperationParameter {
    	public static final String AUTH_PARAM_NAME = "Authorization";
    	public static final String AUTH_PARAM_DESCRIPTION = "OAuth2 Authorization Header";
    	public static final String AUTH_PARAM_TYPE = "header";
    	public static final String PAYLOAD_PARAM_NAME = "Payload";
    	public static final String PAYLOAD_PARAM_DESCRIPTION = "Request Payload";
    	public static final String QUERY_PARAM_NAME = "Query Parameters";
    	public static final String QUERY_PARAM_DESCRIPTION = "Request Query Parameters";
    	public static final String PAYLOAD_PARAM_TYPE = "body";
    	
    	private OperationParameter(){    	    
    	}
    }
    
    public static class CORSHeaders {
    	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    	
    	public static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "authorization,Access-Control-Allow-Origin,Content-Type";
    	public static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "GET,POST,PUT,DELETE,PATCH,OPTIONS";
    	private CORSHeaders(){    	    
    	}
    }

    public static final String EXTENSION_HANDLER_POSITION = "ExtensionHandlerPosition";

    public static final String GATEWAY_ENV_TYPE_HYBRID = "hybrid";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";
    
    public static final String ENABLED = "Enabled";
    public static final String DISABLED = "Disabled";
    public static final int API_RESPONSE_CACHE_TIMEOUT = 300;
    
    
    public static class ApplicationStatus {
       public static final String APPLICATION_CREATED = "CREATED";
       public static final String APPLICATION_APPROVED = "APPROVED"; 
       public static final String APPLICATION_REJECTED = "REJECTED"; 
       public static final String APPLICATION_ONHOLD = "ON_HOLD"; 
       
       private ApplicationStatus(){           
       }
          
    }

    public static class AppRegistrationStatus {
        public static final String REGISTRATION_CREATED = "CREATED";
        public static final String REGISTRATION_APPROVED = "APPROVED";
        public static final String REGISTRATION_REJECTED = "REJECTED";
        public static final String REGISTRATION_COMPLETED = "COMPLETED";
        private AppRegistrationStatus(){
            
        }

    }

    public interface KeyValidationRequestConstants {
        String RESOURCE_CONTEXT = "context";
        String RESOURCE_VERSION = "apiVersion";
        String RESOURCE_AUTH_LEVEL = "authLevel";
        String RESOURCE_PATH = "resource";
        String RESOURCE_VERB = "httpVerb";
        String KEY_DOMAIN = "clientDomain";
        String CACHE_KEY = "cacheKey";

        String RESOURCE_PARAMS = "keymgt_resource_params";
        String VALIDATED_DTO = "validatedDTO";
        String KM_CACHE_HIT = "km_cacheHit";
    }

    public static class FrontEndParameterNames {
        public static final String CONSUMER_KEY = "consumerKey";
        public static final String CONSUMER_SECRET = "consumerSecret";
        public static final String CLIENT_DETAILS = "appDetails";
        public static final String CALLBACK_URL = "callbackUrl";
        public static final String KEY_STATE = "keyState";
        
        private FrontEndParameterNames(){            
        }
    }

    public static class AccessTokenConstants {
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String VALIDITY_TIME = "validityTime";
        public static final String TOKEN_DETAILS = "tokenDetails";
        public static final String TOKEN_STATE = "tokenState";
        public static final String TOKEN_SCOPES = "tokenScope";
        
        private AccessTokenConstants(){            
        }
    }
    
    //key  of the endpoint securevault
    public static final String API_SECUREVAULT_ENABLE = "EnableSecureVault";

    public static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
    public static final String API_ELECTED_RESOURCE = "API_ELECTED_RESOURCE";

    public static final String OAUTH2_DEFAULT_SCOPE = "default";
    
    public static final String  API_MANAGER_DESTINATION_STATS_BAM_PROFILE_NAME = "bam-profile";
    public static final String  API_MANAGER_DESTINATION_REQUESTS_STREAM_NAME = "org_wso2_apimgt_statistics_destination";
    public static final String  API_MANAGER_DESTINATION_REQUESTS_STREAM_VERSION = "1.0.0";

    public static final String RECENTLY_ADDED_API_CACHE_NAME = "RECENTLY_ADDED_API";
    public static final String API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE = API_STORE + "EnableRecentlyAddedAPICache";
    public static final String VELOCITY_LOGGER = "VelocityLogger";


    public static class DigestAuthConstants {
        public static final String REALM = "realm";
        public static final String NONCE = "nonce";
        public static final String ALGORITHM = "algorithm";
        public static final String OPAQUE = "opaque";
        public static final String QOP = "qop";

        public static final String AUTH = "auth";
        public static final String AUTH_INT = "auth-int";
        public static final String MD5 = "MD5";
        public static final String MD5_SESS = "MD5-sess";

        public static final String POSTFIX = "POSTFIX";
        public static final String HTTP_METHOD = "HTTP_METHOD";
        public static final String MESSAGE_BODY = "MessageBody";
        public static final String UNAMEPASSWORD = "UNAMEPASSWORD";
        public static final String NONCE_COUNT = "NonceCount";
        public static final String INIT_NONCE_COUNT = "00000000";
        public static final String AUTH_HEADER = "AuthHeader";
        public static final String BACKEND_URL = "BACKEND_URL";
        public static final String CHARSET = "UTF-8";
        
        private DigestAuthConstants(){            
        }
    }
    
    // Primary/Secondary Login configuration
    public static final String USERID_LOGIN = "UserIdLogin";
    public static final String EMAIL_LOGIN = "EmailLogin";
    public static final String PRIMARY_LOGIN = "primary";
    public static final String CLAIM_URI = "ClaimUri";

    public static final String DEFAULT_VERSION_PREFIX = "_default_";
    public static final String OAUTH_HEADER_SPLITTER = ",";
    public static final String CONSUMER_KEY_SEGMENT = "Bearer";
    public static final String CONSUMER_KEY_SEGMENT_DELIMITER = " ";

    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    

    public static final String KEYMANAGER_HOSTNAME = "keyManagerHostname";
    public static final String KEYMANAGER_PORT = "keyManagerPort";
    public static final String KEYMANAGER_SERVERURL = API_KEY_VALIDATOR+"ServerURL";
    public static final String CARBON_LOCALIP = "carbon.local.ip";

    public static final String APIPROVIDER_HOSTCACHE = "apiProvideHostObjectCache";
    public static final String TENANTCOUNT_CACHEKEY = "apiProviderCacheKey";
    
    public static final String API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION = API_STORE + "GroupingExtractor";

    public static final String API_CUSTOM_SEQUENCES_FOLDER_LOCATION =
            "repository" + File.separator + "resources" + File.separator + "customsequences";
    public static final String API_CUSTOM_SEQUENCE_TYPE_IN = "in";
    public static final String API_CUSTOM_SEQUENCE_TYPE_OUT = "out";
    public static final String API_CUSTOM_SEQUENCE_TYPE_FAULT = "fault";

    public static final String API_MANAGER_HOSTNAME = "HostName";
    public static final String API_MANAGER_HOSTNAME_UNKNOWN = "UNKNOWN_HOST";

    public static final String VERSION_PLACEHOLDER = "{version}";

    public enum SupportedHTTPVerbs {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS
    }

    public static class ConfigParameters {
        public static final String CHECK_PERMISSIONS_REMOTELY = AUTH_MANAGER + "CheckPermissionsRemotely";
        private ConfigParameters() {
            throw new AssertionError();
        }        
    }

    public static final String CORS_SEQUENCE_NAME = "_cors_request_handler_";
    public static final String CUSTOM_HTTP_STATUS_CODE = "CUSTOM_HTTP_SC";
    public static final String CUSTOM_ERROR_CODE = "ERROR_CODE";
    public static final String CUSTOM_ERROR_MESSAGE = "ERROR_MESSAGE";
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

    //swagger v1.2 constants
    public static final String SWAGGER_RESOURCES = "resources";
    public static final String ENVIRONMENTS_NONE = "none";
    public static final String SWAGGER_BASEPATH = "basePath";
    public static final String SWAGGER_OPERATIONS = "operations";
    public static final String SWAGGER_SCOPE = "scope";
    public static final String SWAGGER_AUTH_TYPE = "auth_type";
    public static final String API_THROTTLING_TIER = "throttling_tier";
    public static final String API_MEDIATION_SCRIPT = "mediation_script";
    public static final String API_SWAGGER_DOC = "api_doc";
    public static final String SWAGGER_12_AUTH = "authorizations";
    public static final String SWAGGER_12_OAUTH2 = "oauth2";
    public static final String SWAGGER_12_SCOPES = "scopes";
    public static final String API_ARRAY_NAME = "apis";
    public static final String SWAGGER_HTTP_METHOD = "method";
    public static final String SWAGGER_FILE = "file";
    public static final String SWAGGER_RESOURCE_PATH = "resourcePath";
    public static final String API_VERSION = "apiVersion";


    //API Constants
    public static final String API_DATA_NAME = "name";
    public static final String API_DATA_PROVIDER = "provider";
    public static final String API_DATA_VERSION = "version";
    public static final String API_DATA_DESCRIPTION = "description";
    public static final String API_DATA_BUSINESS_OWNER = "businessOwner";
    public static final String API_DATA_RATES = "rates";
    public static final String API_DATA_ENDPOINT = "endpoint";
    public static final String API_DATA_THUMB_URL= "thumbnailurl";
    public static final String API_DATA_VISIBILITY = "visibility";
    public static final String API_DATA_VISIBLE_ROLES = "visibleRoles";
    public static final String DOC_DATA_NAME = "docName";
    public static final String DOC_DATA_SUMMARY = "docSummary";
    public static final String DOC_DATA_SOURCEURL = "docSourceURL";
    public static final String DOC_DATA_FILEPATH = "docFilePath";
    public static final String API_DATA_DEFAULT_THUMB = "images/api-default.png";
    public static final String API_DATA_APIS = "apis";
    public static final String API_DATA_TOT_LENGTH = "totalLength";
    public static final String API_DATA_LENGTH = "length";
    public static final String API_DATA_ISMORE = "isMore";

    public static final String ACTIVITY_ID = "activityID";
    public static final String USER_AGENT = "User-Agent";

    public static final String REST_API_SCOPE_NAME = "Name";
    public static final String REST_API_SCOPE_ROLE = "Roles";
    public static final String REST_API_SCOPES_CONFIG = "RESTAPIScopes";

    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final int HTTPS_PROTOCOL_PORT = 443;
    public static final int HTTP_PROTOCOL_PORT = 80;
    

    public static final long MAX_FILE_SIZE = 1024L;

    public static final String REGISTRY_RESOURCE_PREFIX = "/registry/resource";

    public enum RegistryResourceTypesForUI {
        TAG_THUMBNAIL
    }
    
    public static final String API_LC_ACTION_DEPRECATE = "Deprecate";

    public static final String METRICS_PREFIX = "org.wso2.am";
    
    public static final String MSG_JSON_PARSE_ERROR = "Unable to parse endpoint config JSON";
    public static final String MSG_TIER_RET_ERROR = "Error while retrieving API tiers from registry";
    public static final String MSG_MALFORMED_XML_ERROR = "Malformed XML found in the API tier policy resource";

    //Doc search related constants

    public static final String PUBLISHER_CLIENT = "Publisher";
    public static final String STORE_CLIENT = "Store";

    public static final String WSDL_REGISTRY_LOCATION_PREFIX = "/registry/resource";
    public static final String HOST_NAME = "HostName";
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final String PROXY_CONTEXT_PATH = "ProxyContextPath";

}
