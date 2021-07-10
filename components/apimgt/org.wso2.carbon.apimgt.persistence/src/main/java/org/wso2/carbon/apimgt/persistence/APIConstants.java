/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence;

import java.io.File;

public final class APIConstants {

    //Registry lifecycle related info
    public static final String API_LIFE_CYCLE = "APILifeCycle";
    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";
    public static final String EMAIL_DOMAIN_SEPARATOR = "@";
    public static final String XML_EXTENSION = ".xml";


    // Those constance are used in API artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_TYPE = "overview_type";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_VERSION_TYPE = "overview_versionType";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_OVERVIEW_CONTEXT_TEMPLATE = "overview_contextTemplate";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
    public static final String API_OVERVIEW_WSDL = "overview_wsdl";
    public static final String API_OVERVIEW_WADL = "overview_wadl";
    public static final String API_OVERVIEW_PROVIDER = "overview_provider";
    public static final String API_OVERVIEW_THUMBNAIL_URL = "overview_thumbnail";
    public static final String API_OVERVIEW_STATUS = "overview_status";
    public static final String API_OVERVIEW_TIER = "overview_tier";
    public static final String API_OVERVIEW_SUB_POLICY = "overview_subPolicy";
    public static final String API_OVERVIEW_API_POLICY = "overview_apiPolicy";
    public static final String API_OVERVIEW_IS_LATEST = "overview_isLatest";
    public static final String API_URI_TEMPLATES = "uriTemplates_entry";
    public static final String API_OVERVIEW_TEC_OWNER = "overview_technicalOwner";
    public static final String API_OVERVIEW_TEC_OWNER_EMAIL = "overview_technicalOwnerEmail";
    public static final String API_OVERVIEW_BUSS_OWNER = "overview_businessOwner";
    public static final String API_OVERVIEW_BUSS_OWNER_EMAIL = "overview_businessOwnerEmail";
    public static final String API_OVERVIEW_VISIBILITY = "overview_visibility";
    public static final String API_OVERVIEW_VISIBLE_ROLES = "overview_visibleRoles";
    public static final String API_OVERVIEW_VISIBLE_TENANTS = "overview_visibleTenants";
    public static final String API_OVERVIEW_ENVIRONMENTS = "overview_environments";
    public static final String API_OVERVIEW_AUDIENCE = "overview_audience";
    public static final String API_PROVIDER = "Provider";
    public static final String API_NAME = "Name";
    public static final String API_VERSION_LABEL = "Version";
    public static final String API_CONTEXT = "Context";
    public static final String API_DESCRIPTION = "Description";
    public static final String API_OVERVIEW_TAG = "tags";
    public static final String API_TAG = "Tag";
    public static final String API_STATUS = "STATUS";
    public static final String API_URI_PATTERN = "URITemplate_urlPattern";
    public static final String API_URI_HTTP_METHOD = "URITemplate_httpVerb";
    public static final String API_URI_AUTH_TYPE = "URITemplate_authType";
    public static final String API_URI_MEDIATION_SCRIPT = "URITemplate_mediationScript";
    public static final String API_OVERVIEW_ENDPOINT_SECURED = "overview_endpointSecured";
    public static final String API_OVERVIEW_ENDPOINT_AUTH_DIGEST = "overview_endpointAuthDigest";
    public static final String API_OVERVIEW_ENDPOINT_USERNAME = "overview_endpointUsername";
    public static final String API_OVERVIEW_ENDPOINT_PASSWORD = "overview_endpointPpassword";
    public static final String API_OVERVIEW_ENDPOINT_OAUTH = "overview_endpointOAuth";
    public static final String API_OVERVIEW_ENDPOINT_GRANT_TYPE = "overview_grantType";
    public static final String API_OVERVIEW_ENDPOINT_HTTP_METHOD = "overview_httpMethod";
    public static final String API_OVERVIEW_ENDPOINT_TOKEN_URL = "overview_endpointTokenUrl";
    public static final String API_OVERVIEW_ENDPOINT_CLIENT_ID = "overview_clientId";
    public static final String API_OVERVIEW_ENDPOINT_CLIENT_SECRET = "overview_clientSecret";
    public static final String API_OVERVIEW_ENDPOINT_CUSTOM_PARAMETERS = "overview_customParameters";
    public static final String API_OVERVIEW_TRANSPORTS = "overview_transports";
    public static final String API_OVERVIEW_INSEQUENCE = "overview_inSequence";
    public static final String API_OVERVIEW_OUTSEQUENCE = "overview_outSequence";
    public static final String API_OVERVIEW_FAULTSEQUENCE = "overview_faultSequence";
    public static final String API_OVERVIEW_AUTHORIZATION_HEADER = "overview_authorizationHeader";
    public static final String API_OVERVIEW_API_SECURITY = "overview_apiSecurity";
    public static final String API_OVERVIEW_RESPONSE_CACHING = "overview_responseCaching";
    public static final String API_OVERVIEW_CACHE_TIMEOUT = "overview_cacheTimeout";

    public static final String PROTOTYPE_OVERVIEW_IMPLEMENTATION = "overview_implementation";
    public static final String API_OVERVIEW_REDIRECT_URL = "overview_redirectURL";
    public static final String API_OVERVIEW_OWNER = "overview_apiOwner";
    public static final String API_OVERVIEW_ADVERTISE_ONLY = "overview_advertiseOnly";
    public static final String API_OVERVIEW_ADVERTISE_ONLY_API_VENDOR = "overview_vendor";
    public static final String API_OVERVIEW_ENDPOINT_CONFIG = "overview_endpointConfig";

    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABILITY = "overview_subscriptionAvailability";
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS = "overview_tenants";

    public static final String API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED = "overview_destinationStatsEnabled";
    public static final String API_OVERVIEW_WEBSOCKET = "overview_ws";

    //This constant is used in Json schema validator
    public static final String API_OVERVIEW_ENABLE_JSON_SCHEMA = "overview_enableSchemaValidation";

    public static final String API_OVERVIEW_ENABLE_STORE = "overview_enableStore";

    public static final String API_OVERVIEW_TESTKEY = "overview_testKey";
    public static final String API_PRODUCTION_THROTTLE_MAXTPS = "overview_productionTps";
    public static final String API_SANDBOX_THROTTLE_MAXTPS = "overview_sandboxTps";
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final String VERSION_PLACEHOLDER = "{version}";
    public static final String TENANT_PREFIX = "/t/";

    public static final String PUBLISHED = "PUBLISHED";
    public static final String API_OVERVIEW_KEY_MANAGERS = "overview_keyManagers";
    public static final String DEFAULT_API_SECURITY_OAUTH2 = "oauth2";
    public static final String API_OVERVIEW_DEPLOYMENTS = "overview_deployments";

    //Overview constants for CORS configuration
    public static final String API_OVERVIEW_CORS_CONFIGURATION = "overview_corsConfiguration";
    public static final String API_OVERVIEW_WEBSUB_SUBSCRIPTION_CONFIGURATION
            = "overview_websubSubscriptionConfiguration";

    public static final String WSO2_ANONYMOUS_USER = "wso2.anonymous.user";

    /**
     * API categories related constants
     */
    public static final String API_CATEGORIES_CATEGORY_NAME = "apiCategories_categoryName";
    public static final String API_CATEGORY = "api-category";
    public static final String API_SECURITY_API_KEY = "api_key";

    //key value of the APIImpl rxt
    public static final String API_KEY = "api";
    public static final String API_OVERVIEW_WS_URI_MAPPING = "overview_wsUriMapping";

    public static class Monetization {
        public static final String API_MONETIZATION_PROPERTIES = "monetizationProperties";
        public static final String API_MONETIZATION_STATUS = "isMonetizationEnabled";

    }

    //registry location of API
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";

    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION + "/applicationdata";
    public static final String API_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";

    public static final String API_REVISION_LOCATION = API_APPLICATION_DATA_LOCATION + "/apis";

    public static final String API_LABELS_GATEWAY_LABELS = "labels_labelName";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";

    //This is the resource name of API
    public static final String API_RESOURCE_NAME = "/api";

    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";
    public static final String EVERYONE_ROLE = "internal/everyone";

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final String API_RXT_MEDIA_TYPE = "application/vnd.wso2-api+xml";
    public static final String DOCUMENT_RXT_MEDIA_TYPE = "application/vnd.wso2-document+xml";
    public static final String API_PRODUCT_RXT_MEDIA_TYPE = "application/vnd.wso2-product+xml";

    // registry location of the governance component
    public static final String GOVERNANCE_COMPONENT_REGISTRY_LOCATION = "/repository/components/org.wso2.carbon" +
                                    ".governance";

    public static final String API_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/tiers.xml";

    public static final String APP_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/app-tiers.xml";

    public static final String RES_TIER_LOCATION = API_APPLICATION_DATA_LOCATION + "/res-tiers.xml";

    public static final String DEFAULT_API_TIER_FILE_NAME = "default-tiers.xml";

    public static final String DEFAULT_APP_TIER_FILE_NAME = "default-app-tiers.xml";

    public static final String DEFAULT_RES_TIER_FILE_NAME = "default-res-tiers.xml";

    public static final String APPLICATION_JSON_MEDIA_TYPE = "application/json";

    public static final String DOCUMENTATION_KEY = "document";

    public static final String DOC_NAME = "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_VISIBILITY = "overview_visibility";
    public static final String DOC_DIR = "documentation";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_FILE_PATH = "overview_filePath";
    public static final String DOC_OTHER_TYPE_NAME = "overview_otherTypeName";

    public static final String PROVIDER_ASSOCIATION = "provides";

    // registry location for wsdl files
    public static final String API_WSDL_RESOURCE_LOCATION = API_APPLICATION_DATA_LOCATION + "/wsdls/";

    /**
     * Publisher Access Control related registry properties and values.
     */
    public static final String PUBLISHER_ROLES = "publisher_roles";
    public static final String DISPLAY_PUBLISHER_ROLES = "display_publisher_roles";
    public static final String ACCESS_CONTROL = "publisher_access_control";
    public static final String NO_ACCESS_CONTROL = "all";
    public static final String NULL_USER_ROLE_LIST = "null";
    public static final String API_RESTRICTED_VISIBILITY = "restricted";
    public static final String API_PRIVATE_VISIBILITY = "private";
    public static final String API_CONTROLLED_VISIBILITY = "controlled";
    public static final String DOC_OWNER_VISIBILITY = "OWNER_ONLY";
    public static final String API_GLOBAL_VISIBILITY = "public";
    public static final String DOC_SHARED_VISIBILITY = "PRIVATE";
    public static final String VISIBILITY = "visibility";
    public static final String GOVERNANCE = "governance";

    public static final String STORE_VIEW_ROLES = "store_view_roles";

    public static final String WSDL_FILE_EXTENSION = ".wsdl";
    public static final String WSDL_PROVIDER_SEPERATOR = "--";
    public static final String API_WSDL_ARCHIVE_LOCATION = "archives/";

    public static final String ZIP_FILE_EXTENSION = ".zip";

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String APPLICATION_ZIP = "application/zip";

    // Prefix used for saving the custom properties related with APIs
    public static final String API_RELATED_CUSTOM_PROPERTIES_PREFIX = "api_meta.";

    /**
     * CustomIndexer property to indicate whether it is gone through API Custom Indexer.
     */
    public static final String CUSTOM_API_INDEXER_PROPERTY = "registry.customIndexer";

    public static final String SEARCH_AND_TAG = "&";
    public static final String LCSTATE_SEARCH_TYPE_KEY = "lcState=";
    public static final String ENABLE_STORE_SEARCH_TYPE_KEY = "enableStore=(true OR null)";
    public static final String LCSTATE_SEARCH_KEY = "lcState";
    public static final String DOCUMENTATION_SEARCH_TYPE_PREFIX = "doc";
    public static final String DOCUMENTATION_SEARCH_TYPE_PREFIX_WITH_EQUALS = "doc=";
    public static final String TAGS_SEARCH_TYPE_PREFIX = "tags";
    public static final String TAGS_EQ_SEARCH_TYPE_PREFIX = "tags=";
    public static final String TAG_SEARCH_TYPE_PREFIX = "tag";
    public static final String TAG_COLON_SEARCH_TYPE_PREFIX = "tag:";
    public static final String NAME_TYPE_PREFIX = "name";
    private static final String PROVIDER_SEARCH_TYPE_PREFIX = "provider";
    private static final String VERSION_SEARCH_TYPE_PREFIX = "version";
    private static final String CONTEXT_SEARCH_TYPE_PREFIX = "context";
    public static final String CONTENT_SEARCH_TYPE_PREFIX = "content";
    public static final String TYPE_SEARCH_TYPE_PREFIX = "type";
    public static final String LABEL_SEARCH_TYPE_PREFIX = "label";
    public static final String CATEGORY_SEARCH_TYPE_PREFIX = "api-category";
    public static final String ENABLE_STORE = "enableStore";

    // Reserved keywords for search.
    public static final String[] API_SEARCH_PREFIXES = {DOCUMENTATION_SEARCH_TYPE_PREFIX, TAGS_SEARCH_TYPE_PREFIX,
                                    NAME_TYPE_PREFIX, PROVIDER_SEARCH_TYPE_PREFIX, CONTEXT_SEARCH_TYPE_PREFIX,
                                    VERSION_SEARCH_TYPE_PREFIX, LCSTATE_SEARCH_KEY.toLowerCase(), API_DESCRIPTION.toLowerCase(),
                                    API_STATUS.toLowerCase(), CONTENT_SEARCH_TYPE_PREFIX, TYPE_SEARCH_TYPE_PREFIX, LABEL_SEARCH_TYPE_PREFIX,
                                    CATEGORY_SEARCH_TYPE_PREFIX, ENABLE_STORE.toLowerCase()};

    public static final String STORE_CLIENT = "Store";

    public static final String DOCUMENTATION_SEARCH_PATH_FIELD = "path";

    public static final String DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD = "mediaType";

    //association type between API and Documentation
    public static final String DOCUMENTATION_ASSOCIATION = "document";

    public static final String API_STORE = "APIStore.";
    public static final String API_STORE_APIS_PER_PAGE = API_STORE + "APIsPerPage";

    public static final class Permissions {

        public static final String API_CREATE = "/permission/admin/manage/api/create";
        public static final String API_PUBLISH = "/permission/admin/manage/api/publish";
        public static final String API_SUBSCRIBE = "/permission/admin/manage/api/subscribe";
        public static final String API_WORKFLOWADMIN = "/permission/admin/manage/workflowadmin";
        public static final String APIM_ADMIN = "/permission/admin/manage/apim_admin";
        public static final String MANAGE_TIERS = "/permission/admin/manage/manage_tiers";

        public static final String LOGIN = "/permission/admin/login";
        public static final String CONFIGURE_GOVERNANCE = "/permission/admin/configure/governance";
        public static final String RESOURCE_GOVERN = "/permission/admin/manage/resources/govern";

        private Permissions() {

        }
    }

    public static final String API_USER_ROLE_CACHE = "appPublisherUserRoleCache";

    //API caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";

    public static final String SKIP_ROLES_BY_REGEX = "skipRolesByRegex";
    public static final String DOCUMENT_MEDIA_TYPE_KEY = "application/vnd.wso2-document\\+xml";

    public static final String TYPE = "type";
    public static final String TYPE_SEARCH_TYPE_KEY = "type=";

    public static final String[] API_SUPPORTED_TYPE_LIST =
            {"HTTP", "WS", "SOAPTOREST", "GRAPHQL", "SOAP", "SSE", "WEBSUB", "WEBHOOK"};

    public static class AuditLogConstants {
        public static final String API_PRODUCT = "APIProduct";
    }

    public static final String DEFAULT_MODIFIED_ENDPOINT_PASSWORD = "*****"; //5 stars
    public static final String REGISTRY_HIDDEN_ENDPOINT_PROPERTY = "registry.HiddenEpProperty";
    public static final int API_RESPONSE_CACHE_TIMEOUT = 300;

    public static final String API_PRODUCT = "APIProduct";

    public static final String DEPRECATED = "DEPRECATED";
    public static final String PROTOTYPED = "PROTOTYPED";


    public static final String API_PUBLISHER_ADMIN_PERMISSION_CACHE = "apimAdminPermissionCache";
    public static final String API_DATA_URL = "url";
    public static final String API_DATA_PRODUCTION_ENDPOINTS = "production_endpoints";
    public static final String API_DATA_SANDBOX_ENDPOINTS = "sandbox_endpoints";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";
    public static final String API_LEVEL_ALL_KEY_MANAGERS = "all";
    public static final String API_OAS_DEFINITION_RESOURCE_NAME = "swagger.json";
    public static final String LC_CACHE_NAME = "lcCache";
    public static final String DOC_API_BASED_VISIBILITY = "API_LEVEL";
    
    public enum APITransportType {
        HTTP, WS, GRAPHQL
    }
    public static final String API_STATE_CHANGE_INDICATOR = "registry.api.state.change.indicator";

    public static final String API_ICON_IMAGE = "icon";
    public static final String API_IMAGE_LOCATION = API_APPLICATION_DATA_LOCATION + "/icons";
    //registry location for API documentation
    public static final String API_DOC_LOCATION = API_APPLICATION_DATA_LOCATION + "/api-docs";
    
    public static final String DOC_API_BASE_PATH = "overview_apiBasePath";

    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String DOCUMENT_FILE_DIR = "files";
    
    public static final String DOCUMENTATION_INLINE_CONTENT_TYPE = "text/plain";
    public static final String NO_CONTENT_UPDATE = "no_content_update";

    public static final String DOCUMENTATION_DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument" +
            ".wordprocessingml.document";
    public static final String DOCUMENTATION_DOC_CONTENT_TYPE = "application/msword";
    public static final String DOCUMENTATION_PDF_CONTENT_TYPE = "application/pdf";
    public static final String DOCUMENTATION_TXT_CONTENT_TYPE = "text/plain";

    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String DOC_UPLOAD_TMPDIR = "restAPI" + File.separator + "documentUpload";
    public static final String DOC_NAME_DEFAULT = "DEFAULT_DOC_";
      
    public static final String USER_CTX_PROPERTY_ISADMIN = "isAdmin";
    public static final String USER_CTX_PROPERTY_SKIP_ROLES = "skipRoles";
    public static final String API = "API";
    
    public static final String API_CUSTOM_SEQUENCE_TYPE_IN = "in";
    public static final String API_CUSTOM_SEQUENCE_TYPE_OUT = "out";
    public static final String API_CUSTOM_SEQUENCE_TYPE_FAULT = "fault";
    
    public static final String GRAPHQL_SCHEMA_FILE_EXTENSION = ".graphql";
    public static final String GRAPHQL_LOCAL_ENTRY_EXTENSION = "_graphQL";
    public static final String GRAPHQL_SCHEMA_PROVIDER_SEPERATOR = "--";
    public static final String ALLOW_MULTIPLE_STATUS = "allowMultipleStatus";
    
    public static final String API_TYPE_SOAPTOREST = "SOAPTOREST";

    public static final String API_ASYNC_API_DEFINITION_RESOURCE_NAME = "asyncapi.json";
}
