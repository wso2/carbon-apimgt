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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class APIConstants {

    public static final String STRING = "string";
    public static final String OBJECT = "object";
    //key value of the provider rxt
    public static final String PROVIDER_KEY = "provider";

    //key value of the APIImpl rxt
    public static final String API_KEY = "api";

    //governance registry apimgt root location
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";

    public static final String API_CONTEXT_ID = "api.context.id";
    //This is the resource name of API
    public static final String API_RESOURCE_NAME = "/api";

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

    public static final String API_LIFE_CYCLE_HISTORY =
            "/repository/components/org.wso2.carbon.governance/lifecycles/history";

    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION + "/applicationdata";

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

    public static final String APPLICATION_XML_MEDIA_TYPE = "application/xml";

    public static final String APPLICATION_WSDL_MEDIA_TYPE = "application/wsdl";

    public static final String WSDL_NAMESPACE_URI = "http://www.w3.org/2005/08/addressing";

    public static final String WSDL_ELEMENT_LOCAL_NAME = "Address";

    public static final String API_TENANT_CONF = "tenant-conf.json";

    public static final String API_TENANT_CONF_LOCATION = API_APPLICATION_DATA_LOCATION + "/" + API_TENANT_CONF;

    public static final String API_TENANT_CONF_MEDIA_TYPE = "tenant-config";
    public static final String TENANT_CONFIG_CACHE_NAME = "tenantConfigCache";

    public static final String RESOURCE_FOLDER_LOCATION = "repository" + File.separator + "resources";

    public static final String API_TENANT_CONF_ENABLE_MONITZATION_KEY = "EnableMonetization";

    public static final String API_TENANT_CONF_DEFAULT_ROLES = "DefaultRoles";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_ROLENAME = "RoleName";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_CREATE_ON_TENANT_LOAD = "CreateOnTenantLoad";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_PUBLISHER_ROLE = "PublisherRole";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_CREATOR_ROLE = "CreatorRole";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_SUBSCRIBER_ROLE = "SubscriberRole";
    public static final String API_TENANT_CONF_DEFAULT_ROLES_DEVOPS_ROLE = "DevOpsRole";
    public static final String ANALYTICS_ROLE = "Internal/analytics";
    public static final String API_TENANT_CONF_ENABLE_ANONYMOUS_MODE = "EnableAnonymous";

    public static final String ENDPOINT_REGISTRY_ADMIN_ROLE = "Internal/registry_admin";
    public static final String ENDPOINT_REGISTRY_USER_ROLE = "Internal/registry_user";
    public static final String ENDPOINT_REGISTRY_BROWSER_ROLE = "Internal/registry_browser";

    public static final String API_TENANT_CONF_IS_UNLIMITED_TIER_PAID = "IsUnlimitedTierPaid";

    public static final String API_TENANT_CONF_EXPOSE_ENDPOINT_PASSWORD = "ExposeEndpointPassword";

    public static final String API_CATEGORY_FREE = "Free";

    public static final String API_CATEGORY_FREEMIUM = "Freemium";

    public static final String API_CATEGORY_PAID = "Paid";

    public static final String SSL_VERIFY_CLIENT = "SSLVerifyClient";

    public static final String SSL_VERIFY_CLIENT_STATUS_REQUIRE = "require";

    //location for custom url domain mapings. "<tenant-id>" will be replaced by actual tenant name.
    public static final String API_DOMAIN_MAPPINGS = "/customurl/api-cloud/<tenant-id>/urlMapping/<tenant-id>";
    public static final String API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER = "<tenant-id>";
    //domain mapping app keys
    public static final String API_DOMAIN_MAPPINGS_GATEWAY = "gateway";
    public static final String API_DOMAIN_MAPPINGS_STORE = "store";
    public static final String API_DOMAIN_MAPPINGS_PUBLISHER = "publisher";
    public static final String API_DOMAIN_MAPPINGS_CONTEXT = "context";
    public static final String API_DOMAIN_MAPPINGS_LOGIN_CALLBACK = "login";
    public static final String API_DOMAIN_MAPPINGS_LOGOUT_CALLBACK = "logout";

    public static final String API_IMAGE_LOCATION = API_APPLICATION_DATA_LOCATION + "/icons";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = API_APPLICATION_DATA_LOCATION + "/provider";

    //registry location for API documentation
    public static final String API_DOC_LOCATION = API_APPLICATION_DATA_LOCATION + "/api-docs";

    public static final String API_DOC_1_2_LOCATION = "1.2";

    //registry location for Custom sequences
    public static final String API_CUSTOM_SEQUENCE_LOCATION = APIMGT_REGISTRY_LOCATION + "/customsequences";

    public static final String API_CUSTOM_INSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION + "/in/";

    public static final String API_CUSTOM_OUTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION + "/out/";

    public static final String API_CUSTOM_FAULTSEQUENCE_LOCATION = API_CUSTOM_SEQUENCE_LOCATION + "/fault/";

    // registry location for secure vault passwords
    public static final String API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION = "/repository/components/secure-vault";

    // registry location of the governance component
    public static final String GOVERNANCE_COMPONENT_REGISTRY_LOCATION = "/repository/components/org.wso2.carbon" +
            ".governance";

    // registry location for OpenAPI files
    public static final String OPENAPI_ARCHIVES_TEMP_FOLDER = "OPENAPI-archives";
    public static final String OPENAPI_EXTRACTED_DIRECTORY = "extracted";
    public static final String OPENAPI_ARCHIVE_ZIP_FILE = "openapi-archive.zip";
    public static final String OPENAPI_MASTER_JSON = "swagger.json";
    public static final String OPENAPI_MASTER_YAML = "swagger.yaml";

    // registry location for wsdl files
    public static final String API_WSDL_RESOURCE_LOCATION = API_APPLICATION_DATA_LOCATION + "/wsdls/";
    public static final String API_WSDL_RESOURCE = API_APPLICATION_DATA_LOCATION + "/wsdls";
    public static final String WSDL_FILE_EXTENSION = ".wsdl";
    public static final String WSDL_PROVIDER_SEPERATOR = "--";
    public static final String API_WSDL_ARCHIVE_LOCATION = "archives/";
    public static final String API_WSDL_EXTRACTED_DIRECTORY = "extracted";
    public static final String WSDL_ARCHIVES_TEMP_FOLDER = "WSDL-archives";
    public static final String WSDL_ARCHIVE_ZIP_FILE = "wsdl-archive.zip";
    public static final String WSDL_ARCHIVE_UPDATED_ZIP_FILE = "wsdl-archive-updated.zip";
    public static final String WSDL_FILE = "wsdlFile";
    public static final String UPDATED_WSDL_ZIP = "updated.zip";
    public static final String FILE_URI_PREFIX = "file://";
    public static final String API_DOC_RESOURCE_NAME = "api-doc.json";

    public static final String WSDL_VERSION_11 = "1.1";
    public static final String WSDL_VERSION_20 = "2.0";

    public static final String API_DOC_1_2_RESOURCE_NAME = "/api-doc";
    public static final String API_OAS_DEFINITION_RESOURCE_NAME = "swagger.json";

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

    public static final String EXTERNAL_API_STORES_LOCATION =
            APIMGT_REGISTRY_LOCATION + "/externalstores/external-api-stores.xml";

    public static final String GA_CONFIGURATION_LOCATION = APIMGT_REGISTRY_LOCATION + "/statistics/ga-config.xml";

    public static final String GA_CONF_KEY = "ga-config-key";
    public static final String GA_CONF_MEDIA_TYPE = "ga-config";

    public static final String WORKFLOW_EXECUTOR_LOCATION = API_APPLICATION_DATA_LOCATION + "/workflow-extensions.xml";

    public static final String WORKFLOW_MEDIA_TYPE = "workflow-config";

    // Constants used in API Security Audit feature
    // For configs
    public static final String API_SECURITY_AUDIT = "APISecurityAudit.";
    public static final String API_SECURITY_AUDIT_API_TOKEN = API_SECURITY_AUDIT + "APIToken";
    public static final String API_SECURITY_AUDIT_CID = API_SECURITY_AUDIT + "CollectionID";
    public static final String API_SECURITY_AUDIT_BASE_URL = API_SECURITY_AUDIT + "BaseUrl";
    public static final String API_SECURITY_AUDIT_GLOBAL = API_SECURITY_AUDIT + "Global";

    public static final String SECURITY_AUDIT_CONFIGURATION = "SecurityAuditConfig";
    public static final String SECURITY_AUDIT_API_TOKEN = "apiToken";
    public static final String SECURITY_AUDIT_COLLECTION_ID = "collectionId";
    public static final String SECURITY_AUDIT_BASE_URL = "baseUrl";
    public static final String SECURITY_AUDIT_OVERRIDE_GLOBAL = "overrideGlobal";
    // For HTTP requests
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_API_TOKEN = "X-API-KEY";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String MULTIPART_FORM_BOUNDARY = "X-WSO2-BOUNDARY";
    public static final String MULTIPART_LINE_FEED = "\r\n";
    public static final String BASE_AUDIT_URL = "https://platform.42crunch.com/api/v1/apis";
    public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=";
    public static final String USER_AGENT_APIM = "WSO2-APIM";
    public static final String DATA = "data";
    public static final String ATTR = "attr";
    public static final String GRADE = "grade";
    public static final String NUM_ERRORS = "numErrors";
    public static final String DESC = "desc";
    public static final String ID = "id";
    public static final String IS_VALID = "isValid";
    public static final String ASSESSMENT_REPORT = "/assessmentreport?";

    //registry resource containing the self signup user config
    public static final String SELF_SIGN_UP_CONFIG_LOCATION = API_APPLICATION_DATA_LOCATION + "/sign-up-config.xml";
    public static final String SELF_SIGN_UP_CONFIG_MEDIA_TYPE = "signup-config";

    public static final String DOCUMENTATION_SEARCH_PATH_FIELD = "path";

    public static final String DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD = "mediaType";

    public static final String DOCUMENTATION_INLINE_CONTENT_TYPE = "text/plain";

    public static final String DOCUMENTATION_RESOURCE_MAP_DATA = "Data";
    public static final String DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE = "contentType";
    public static final String DOCUMENTATION_RESOURCE_MAP_NAME = "name";

    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    // Those constance are used in API artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_TYPE = "overview_type";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_VERSION_TYPE = "overview_versionType";
    public static final String API_OVERVIEW_IS_DEFAULT_VERSION = "overview_isDefaultVersion";
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
    public static final String AUTHORIZATION_HEADER_BASIC = "Basic";
    public static final String DEFAULT_API_SECURITY_OAUTH2 = "oauth2";
    public static final String API_SECURITY_MUTUAL_SSL = "mutualssl";
    public static final String API_SECURITY_BASIC_AUTH = "basic_auth";
    public static final String API_SECURITY_API_KEY = "api_key";
    public static final String API_SECURITY_MUTUAL_SSL_MANDATORY = "mutualssl_mandatory";
    public static final String API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY = "oauth_basic_auth_api_key_mandatory";
    public static final String CERTIFICATE_COMMON_NAME = "CN";
    public static final List<String> APPLICATION_LEVEL_SECURITY = Arrays.asList("basic_auth", "api_key", "oauth2");
    public static final String API_OVERVIEW_DEPLOYMENTS = "overview_deployments";
    public static final String BEGIN_CERTIFICATE_STRING = "-----BEGIN CERTIFICATE-----\n";
    public static final String BEGIN_CERTIFICATE_STRING_SPACE = "-----BEGIN CERTIFICATE----- ";
    public static final String END_CERTIFICATE_STRING = "-----END CERTIFICATE-----";

    public static final String API_OVERVIEW_RESPONSE_CACHING = "overview_responseCaching";
    public static final String API_OVERVIEW_CACHE_TIMEOUT = "overview_cacheTimeout";

    public static final String PROTOTYPE_OVERVIEW_IMPLEMENTATION = "overview_implementation";
    public static final String API_PRODUCTION_THROTTLE_MAXTPS = "overview_productionTps";
    public static final String API_SANDBOX_THROTTLE_MAXTPS = "overview_sandboxTps";

    public static final String IMPLEMENTATION_TYPE_ENDPOINT = "ENDPOINT";
    public static final String IMPLEMENTATION_TYPE_INLINE = "INLINE";
    public static final String IMPLEMENTATION_TYPE_MARKDOWN = "MARKDOWN";
    public static final String IMPLEMENTATION_TYPE_FILE = "FILE";

    public static final String API_OVERVIEW_REDIRECT_URL = "overview_redirectURL";
    public static final String API_OVERVIEW_OWNER = "overview_apiOwner";
    public static final String API_OVERVIEW_ADVERTISE_ONLY = "overview_advertiseOnly";
    public static final String API_OVERVIEW_ENDPOINT_CONFIG = "overview_endpointConfig";

    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABILITY = "overview_subscriptionAvailability";
    public static final String API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS = "overview_tenants";

    public static final String API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED = "overview_destinationStatsEnabled";
    public static final String API_OVERVIEW_WEBSOCKET = "overview_ws";

    //This constant is used in Json schema validator
    public static final String API_OVERVIEW_ENABLE_JSON_SCHEMA = "overview_enableSchemaValidation";

    public static final String API_OVERVIEW_ENABLE_STORE = "overview_enableStore";

    public static final String API_OVERVIEW_TESTKEY = "overview_testKey";

    //Those constance are used in Provider artifact.
    public static final String PROVIDER_OVERVIEW_NAME = "overview_name";
    public static final String PROVIDER_OVERVIEW_EMAIL = "overview_email";
    public static final String PROVIDER_OVERVIEW_DESCRIPTION = "overview_description";

    public static final String API_LABELS_GATEWAY_LABELS = "labels_labelName";
    public static final String LABEL_NAME = "labelName";
    public static final String LABEL_DESCRIPTION = "labelDescription";
    public static final String LABEL_ACCESS_URLS = "accessURLs";
    public static final String LABEL = "label";

    //database columns for Subscriber
    public static final String SUBSCRIBER_FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String SUBSCRIBER_FIELD_USER_ID = "USER_ID";
    public static final String SUBSCRIBER_FIELD_DATE_SUBSCRIBED = "DATE_SUBSCRIBED";

    //tables columns for subscription
    public static final String SUBSCRIPTION_FIELD_SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String SUBSCRIPTION_FIELD_TIER_ID = "TIER_ID";
    public static final String SUBSCRIPTION_FIELD_TIER_ID_PENDING = "TIER_ID_PENDING";
    public static final String SUBSCRIPTION_FIELD_API_ID = "API_ID";
    public static final String SUBSCRIPTION_FIELD_ACCESS_TOKEN = "ACCESS_TOKEN";
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
    public static final String APPLICATION_CREATED_BY = "CREATED_BY";
    public static final String APPLICATION_SUBSCRIBER_ID = "SUBSCRIBER_ID";
    public static final String APPLICATION_TIER = "APPLICATION_TIER";
    public static final String APPLICATION_STATUS = "APPLICATION_STATUS";

    //IDENTITY OAUTH2 table
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_STATE = "TOKEN_STATE";
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_SCOPE = "TOKEN_SCOPE";
    public static final String IDENTITY_OAUTH2_FIELD_AUTHORIZED_USER = "AUTHZ_USER";
    public static final String IDENTITY_OAUTH2_FIELD_TIME_CREATED = "TIME_CREATED";
    public static final String IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD = "VALIDITY_PERIOD";
    public static final String IDENTITY_OAUTH2_FIELD_USER_DOMAIN = "USER_DOMAIN";
    public static final String DOT = ".";
    public static final String DEFAULT = "DEFAULT";
    public static final String API_KEY_AUTH_TYPE = "API_KEY";
    public static final String EXP = "exp";
    public static final String JWT = "JWT";
    public static final String JWT_DEFAULT_AUDIENCE = "http://org.wso2.apimgt/gateway";
    public static final String JWT_CONFIGS = "JWTConfiguration";
    public static final String JWT_HEADER = "JWTHeader";
    public static final String TOKEN_GENERATOR_IMPL = "JWTGeneratorImpl";
    public static final String ENABLE_JWT_GENERATION = "EnableJWTGeneration";
    public static final String CLAIMS_RETRIEVER_CLASS = "ClaimsRetrieverImplClass";
    public static final String CONSUMER_DIALECT_URI = "ConsumerDialectURI";
    public static final String JWT_SIGNATURE_ALGORITHM = "SignatureAlgorithm";
    public static final String GATEWAY_JWT_GENERATOR = "GatewayJWTGeneration";
    public static final String GATEWAY_JWT_GENERATOR_IMPL = "ImplClass";
    public static final String TOKEN_ISSUERS = "TokenIssuers";
    public static final String GATEWAY_JWT_CONFIGURATION = "Configuration";
    public static final String GATEWAY_JWT_GENERATOR_CLAIMS = "ExcludedClaims";
    public static final String GATEWAY_JWT_GENERATOR_CLAIM = "Claim";
    public static final String CONVERT_CLAIMS_TO_CONSUMER_DIALECT = JWT_CONFIGS + ".ConvertClaimsToConsumerDialect";
    public static final String ENABLE_TENANT_BASED_JWT_SIGNING = "EnableTenantBasedSigning";

    public static final String OAUTH_CONFIGS = "OAuthConfigurations.";
    public static final String AUTHORIZATION_HEADER = "AuthorizationHeader";
    public static final String API_SECURITY = "APISecurity";
    public static final String API_LEVEL_POLICY = "APILevelPolicy";
    public static final String CERTIFICATE_INFORMATION = "CertificateInformation";
    public static final String AUTHORIZATION_HEADER_DEFAULT = "Authorization";
    public static final String HEADER_TENANT = "xWSO2Tenant";
    public static final String X_WSO2_TENANT_HEADER = "X-WSO2-Tenant";
    public static final String AUTHORIZATION_QUERY_PARAM_DEFAULT = "access_token";
    public static final String API_KEY_HEADER_QUERY_PARAM = "apikey";
    public static final String REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE = "RemoveOAuthHeadersFromOutMessage";
    public static final String REMOVE_OAUTH_HEADER_FROM_OUT_MESSAGE_DEFAULT = "true";
    public static final String REMOVE_OAUTH_HEADERS_FROM_MESSAGE = OAUTH_CONFIGS + "RemoveOAuthHeadersFromOutMessage";
    public static final String APPLICATION_TOKEN_SCOPE = OAUTH_CONFIGS + "ApplicationTokenScope";
    public static final String ALLOWED_SCOPES = OAUTH_CONFIGS + "AllowedScopes.Scope";
    public static final String TOKEN_ENDPOINT_NAME = OAUTH_CONFIGS + "TokenEndPointName";
    public static final String REVOKE_API_URL = OAUTH_CONFIGS + "RevokeAPIURL";
    public static final String ENCRYPT_TOKENS_ON_PERSISTENCE = OAUTH_CONFIGS + "EncryptPersistedTokens";
    public static final String HASH_TOKENS_ON_PERSISTENCE = OAUTH_CONFIGS + "EnableTokenHashMode";
    public static final String TOKEN_ENDPOINT_CONTEXT = OAUTH_CONFIGS + "TokenEndPointName";
    public static final String REVOKE_ENDPOINT_CONTEXT = OAUTH_CONFIGS + "RevokeEndpointName";
    public static final String DEFAULT_MODIFIED_ENDPOINT_PASSWORD = "*****"; //5 stars
    public static final String REGISTRY_HIDDEN_ENDPOINT_PROPERTY = "registry.HiddenEpProperty";
    public static final String OVERVIEW_ELEMENT = "overview";
    public static final String ENDPOINT_PASSWORD_ELEMENT = "endpointPpassword";
    public static final String FEDERATED_USER = "FEDERATED";

    //documentation rxt

    public static final String DOC_NAME = "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_VISIBILITY = "overview_visibility";
    public static final String DOC_DIR = "documentation";
    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String NO_CONTENT_UPDATE = "no_content_update";
    public static final String DOCUMENT_FILE_DIR = "files";
    public static final String DOC_API_BASE_PATH = "overview_apiBasePath";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_FILE_PATH = "overview_filePath";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String DOC_OTHER_TYPE_NAME = "overview_otherTypeName";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";
    public static final String DEPRECATED = "DEPRECATED";
    public static final String PROTOTYPED = "PROTOTYPED";
    public static final String RETIRED = "RETIRED";
    public static final String BLOCKED = "BLOCKED";
    public static final String VERB_INFO_DTO = "VERB_INFO";
    public static final String RESOURCE_AUTHENTICATION_SCHEME = "ResourceAuthenticationScheme";
    public static final String GOVERNANCE = "governance";

    //Overview constants for CORS configuration
    public static final String API_OVERVIEW_CORS_CONFIGURATION = "overview_corsConfiguration";
    //Registry lifecycle related info
    public static final String API_LIFE_CYCLE = "APILifeCycle";
    public static final String LC_NEXT_STATES = "nextStates";
    public static final String LC_PROPERTY_LIFECYCLE_NAME_PREFIX = "registry.lifecycle.";
    public static final String LC_PROPERTY_CHECKLIST_PREFIX = "registry.custom_lifecycle.checklist.";
    public static final String LC_PROPERTY_STATE_SUFFIX = ".state";
    public static final String LC_PROPERTY_PERMISSION_SUFFIX = ".item.permission";
    public static final String LC_PROPERTY_ITEM_SUFFIX = ".item";
    public static final String LC_STATUS = "status:";
    public static final String LC_CHECK_ITEMS = "items";
    public static final String LC_CHECK_ITEM_NAME = "name:";
    public static final String LC_CHECK_ITEM_VALUE = "value:";
    public static final String LC_CHECK_ITEM_ORDER = "order:";
    public static final String LC_PUBLISH_LC_STATE = "Publish";

    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final String TENANT_PREFIX = "/t/";
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    public static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    public static final String OAUTH_RESPONSE_TOKEN_SCOPE = "scope";
    public static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    public static final String APP_DISPLAY_NAME = "DisplayName";
    public static final String APP_TOKEN_TYPE = "TokenType";
    public static final String APP_SKIP_CONSENT_DISPLAY = "Skip Consent";
    public static final String APP_SKIP_CONSENT_NAME = "skipConsent";
    public static final String APP_SKIP_CONSENT_VALUE = "true";
    public static final String APP_SKIP_LOGOUT_CONSENT_DISPLAY = "Skip Logout Consent";
    public static final String APP_SKIP_LOGOUT_CONSENT_NAME = "skipLogoutConsent";
    public static final String APP_SKIP_LOGOUT_CONSENT_VALUE = "true";
    public static final String RECEIVER_URL = "receiverURL";
    public static final String AUTHENTICATOR_URL = "authenticatorURL";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PROTOCOL = "protocol";
    public static final String PUBLISHING_MODE = "publishingMode";
    public static final String PUBLISHING_TIME_OUT = "publishTimeout";
    public static final String NON_BLOCKING = "non-blocking";
    public static final String BLOCKING_CONDITIONS_STREAM_ID = "org.wso2.blocking.request.stream:1.0.0";
    public static final String TOKEN_REVOCATION_STREAM_ID = "org.wso2.apimgt.token.revocation.stream:1.0.0";
    public static final String KEY_TEMPLATE_STREM_ID = "org.wso2.keytemplate.request.stream:1.0.0";
    public static final String CACHE_INVALIDATION_STREAM_ID = "org.wso2.apimgt.cache.invalidation.stream:1.0.0";
    public static final String NOTIFICATION_STREAM_ID = "org.wso2.apimgt.notification.stream:1.0.0";

    //Property for enabling scope sharing between APIs
    public static final String ENABLE_API_SCOPES_SHARING = "enable-api-scopes-sharing";

    // Constants for obtaining organization claims
    public static final String READ_ORGANIZATION_FROM_SAML_ASSERTION = "readOrganizationClaimFromSamlAssertion";
    public static final String SAML2_SSO_AUTHENTICATOR_NAME = "SAML2SSOAuthenticator";
    public static final String ORGANIZATION_CLAIM_ATTRIBUTE = "OrganizationClaimAttribute";
    public static final String DEFAULT_ORGANIZATION_CLAIM_NAME = "http://wso2.org/claims/organization";
    public static final String DEFAULT_TOKEN_TYPE = "DEFAULT";
    public static final String TOKEN_TYPE_JWT = "JWT";

    public static final String PASSWORD_RESOLVER_IMPL_CLASS = "PasswordResolverImpl";
    public static final String CACHE_INVALIDATION_TYPE = "type";
    public static final String GATEWAY_JWKS_CACHE = "JWKS_CACHE";
    public static final String BLOCKING_CONDITION_ID = "id";
    public static final String API_OVERVIEW_KEY_MANAGERS = "overview_keyManagers";
    public static final String KEY_MANAGER_CONSUMER_KEY = "consumer_key";
    public static final String KEY_MANAGER_CONSUMER_SECRET = "consumer_secret";
    public static final String REVOKED_TOKEN_TYPE = "type";
    public static final String IDENTITY_REVOKE_ENDPOINT = "/oauth2/revoke";
    public static final String IDENTITY_TOKEN_ENDPOINT_CONTEXT = "/oauth2/token";
    public static final String GATEWAY_SIGNED_JWT_CACHE = "SignedJWTParseCache";

    public static final String DEFAULT_RESERVED_USERNAME = "apim_reserved_user";

    public static final String DEFAULT_WEBSOCKET_VERSION = "defaultVersion";
    public static final String ENCRYPTED_VALUE = "encrypted";
    public static final String VALUE = "value";
    public static final String GATEWAY_INTROSPECT_CACHE_NAME = "GatewayIntrospectCache";

    public static String DELEM_COLON = ":";

    public static class TokenStatus {

        public static final String ACTIVE = "ACTIVE";
        public static final String BLOCKED = "BLOCKED";
        public static final String REVOKED = "REVOKED";
        public static final String INACTIVE = "INACTIVE";

        private TokenStatus() {

        }
    }

    public static class SubscriptionStatus {

        public static final String BLOCKED = "BLOCKED";
        public static final String PROD_ONLY_BLOCKED = "PROD_ONLY_BLOCKED";
        public static final String UNBLOCKED = "UNBLOCKED";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String TIER_UPDATE_PENDING = "TIER_UPDATE_PENDING";
        public static final String REJECTED = "REJECTED";

        private SubscriptionStatus() {

        }
    }

    public enum OAuthAppMode {
        CREATED, MAPPED
    }

    public static class SubscriptionCreatedStatus {

        public static final String SUBSCRIBE = "SUBSCRIBE";
        public static final String UN_SUBSCRIBE = "UN_SUBSCRIBE";

        private SubscriptionCreatedStatus() {

        }
    }

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final String API_RXT_MEDIA_TYPE = "application/vnd.wso2-api+xml";
    public static final String DOCUMENT_RXT_MEDIA_TYPE = "application/vnd.wso2-document+xml";
    public static final String API_PRODUCT_RXT_MEDIA_TYPE = "application/vnd.wso2-product+xml";
    public static final int TOP_TATE_MARGIN = 4;

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

    public static final String API_GATEWAY = "APIGateway.";
    public static final String API_GATEWAY_SERVER_URL = "ServerURL";
    public static final String API_GATEWAY_USERNAME = "Username";
    public static final String API_GATEWAY_PASSWORD = "Password";
    public static final String API_GATEWAY_ENDPOINT = "GatewayEndpoint";
    public static final String API_WEBSOCKET_GATEWAY_ENDPOINT = "GatewayWSEndpoint";
    public static final String API_GATEWAY_TYPE = "GatewayType";
    public static final String API_GATEWAY_TYPE_SYNAPSE = "Synapse";
    public static final String API_GATEWAY_NONE = "none";
    public static final String GATEWAY_STATS_SERVICE = "GatewayStatsUpdateService";

    public static final String CACHE_CONFIGS = "CacheConfigurations.";
    public static final String GATEWAY_TOKEN_CACHE_ENABLED = CACHE_CONFIGS + "EnableGatewayTokenCache";
    public static final String GATEWAY_RESOURCE_CACHE_ENABLED = CACHE_CONFIGS + "EnableGatewayResourceCache";
    public static final String JWT_CLAIM_CACHE_EXPIRY = CACHE_CONFIGS + "JWTClaimCacheExpiry";
    public static final String ENABLED_JWT_CLAIM_CACHE = CACHE_CONFIGS + "EnableJWTClaimCache";
    public static final String KEY_MANAGER_TOKEN_CACHE = CACHE_CONFIGS + "EnableKeyManagerTokenCache";
    public static final String TOKEN_CACHE_EXPIRY = CACHE_CONFIGS + "TokenCacheExpiry";
    public static final String REST_API_TOKEN_CACHE_ENABLED = CACHE_CONFIGS + "EnableRESTAPITokenCache";
    public static final String REST_API_TOKEN_CACHE_EXPIRY = CACHE_CONFIGS + "RESTAPITokenCacheExpiry";
    public static final String REST_API_CACHE_CONTROL_HEADERS_ENABLED = CACHE_CONFIGS
            + "EnableRESTAPICacheControlHeaders";
    public static final String REST_API_CACHE_CONTROL_HEADERS_MAX_AGE = CACHE_CONFIGS
            + "RESTAPICacheControlHeadersMaxAge";

    public static final String STORE_TAG_CACHE_DURATION = CACHE_CONFIGS + "TagCacheDuration";
    public static final String API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE =
            CACHE_CONFIGS + "EnableRecentlyAddedAPICache";
    public static final String SCOPE_CACHE_ENABLED = CACHE_CONFIGS + "EnableScopeCache";
    public static final String PUBLISHER_ROLE_CACHE_ENABLED = CACHE_CONFIGS + "EnablePublisherRoleCache";
    public static final String GATEWAY_RESOURCE_CACHE_TIMEOUT = CACHE_CONFIGS + "GatewayResourceCacheExpiry";
    public static final String DEFAULT_CACHE_TIMEOUT = "Cache.DefaultCacheTimeout";
    public static final String REST_API_SCOPE_CACHE = "REST_API_SCOPE_CACHE";
    public static final long DEFAULT_TIMEOUT = 900;

    public static final String API_KEY_VALIDATOR = "APIKeyValidator.";
    public static final String API_KEY_VALIDATOR_URL = API_KEY_VALIDATOR + "ServerURL";
    public static final String API_KEY_VALIDATOR_USERNAME = API_KEY_VALIDATOR + "Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = API_KEY_VALIDATOR + "Password";
    public static final String ENABLE_DEFAULT_KEY_MANAGER_REGISTRATION = API_KEY_VALIDATOR +
            "EnableDefaultKeyManagerRegistration";
    public static final String ENABLE_KEY_MANAGER_RETRIVAL = API_KEY_VALIDATOR +
            "EnableKeyManagerConfigurationRetriever";
    public static final String DEFAULT_KEY_MANAGER_TYPE = API_KEY_VALIDATOR + "DefaultKeyManagerType";
    public static final String KEY_VALIDATION_HANDLER_CLASSNAME = API_KEY_VALIDATOR + "KeyValidationHandlerClassName";
    // Constants needed for KeyManager section
    public static final String API_KEY_MANAGER = "APIKeyManager.";
    public static final String KEY_MANAGER_CLIENT = API_KEY_MANAGER + "KeyManagerClientImpl";
    public static final String KEY_MANAGER = "KeyManager";
    public static final String KEY_MANAGER_USERNAME = "Username";
    public static final String KEY_MANAGER_PASSWORD = "Password";
    public static final String AUTHSERVER_URL = "ServerURL";
    public static final String API_KEY_VALIDATOR_ENABLE_PROVISION_APP_VALIDATION =
            API_KEY_VALIDATOR + "EnableProvisionedAppValidation";
    public static final String KEY_MANAGER_OAUTH2_SCOPES_REST_API_BASE_PATH = "/api/identity/oauth2/v1.0/scopes";
    public static final String KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM = "{scope_name}";
    public static final String KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME = "/name/"
            + KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM;
    public static final String[] KEY_MANAGER_OAUTH2_REST_API_MGT_SCOPES = {"internal_application_mgt_create",
            "internal_application_mgt_delete", "internal_application_mgt_update", "internal_application_mgt_view",
            "internal_user_mgt_list"};
    public static final String KEY_MANAGER_CLIENT_APPLICATION_PREFIX = "wso2_apim_km_";

    public static final String TOKEN_URL = "TokenURL";
    public static final String REVOKE_URL = "RevokeURL";
    public static final String SERVICES_URL_RELATIVE_PATH = "services";

    public static final String PORT_OFFSET_SYSTEM_VAR = "portOffset";
    public static final String PORT_OFFSET_CONFIG = "Ports.Offset";

    public static final String DEVICE_SCOPE_PATTERN = "^device_.*";
    public static final String OPEN_ID_SCOPE_NAME = "openid";
    public static final String API_KEY_MANGER_VALIDATIONHANDLER_CLASS_NAME = API_KEY_VALIDATOR
            + "KeyValidationHandlerClassName";

    public static final String USER_DEFAULT_PROFILE = "default";
    public static final String USER_PROFILE_MGT_SERVICE = "UserProfileMgtService";
    public static final String USER_INFO_RECOVERY_SERVICE = "UserInformationRecoveryService";

    public static final String API_RESTAPI = "RESTAPI.";
    public static final String API_RESTAPI_ALLOWED_URI = API_RESTAPI + "AllowedURIs.AllowedURI.";
    public static final String API_RESTAPI_ALLOWED_URI_URI = API_RESTAPI_ALLOWED_URI + "URI";
    public static final String API_RESTAPI_ALLOWED_URI_HTTPMethods = API_RESTAPI_ALLOWED_URI + "HTTPMethods";
    public static final String API_RESTAPI_ETAG_SKIP_LIST = API_RESTAPI + "ETagSkipList.";
    public static final String API_RESTAPI_ETAG_SKIP_URI = API_RESTAPI_ETAG_SKIP_LIST + "ETagSkipURI.";
    public static final String API_RESTAPI_ETAG_SKIP_URI_URI = API_RESTAPI_ETAG_SKIP_URI + "URI";
    public static final String API_RESTAPI_ETAG_SKIP_URI_HTTPMETHOD = API_RESTAPI_ETAG_SKIP_URI + "HTTPMethods";

    public static final String API_KEY_VALIDATOR_CLIENT_TYPE = API_KEY_VALIDATOR + "KeyValidatorClientType";
    public static final String API_KEY_VALIDATOR_WS_CLIENT = "WSClient";

    public static final String JWT_EXPIRY_TIME = API_KEY_VALIDATOR + "JWTExpiryTime";

    public static final String API_KEY_VALIDATOR_ENABLE_ASSERTIONS = API_KEY_VALIDATOR + "EnableAssertions.";

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
    public static final String STORE_TOKEN_DISPLAY_URL = API_STORE + "StoreTokenDisplayURL";
    public static final String API_STORE_FORCE_CI_COMPARISIONS = API_STORE + "CompareCaseInsensitively";
    public static final String API_STORE_DISABLE_PERMISSION_CHECK = API_STORE + "DisablePermissionCheck";
    public static final String API_STORE_APIS_PER_PAGE = API_STORE + "APIsPerPage";
    public static final String API_STORE_FORUM_ENABLED = API_STORE + "isStoreForumEnabled";
    public static final String MULTI_TENANT_USER_ADMIN_SERVICE = "MultiTenantUserAdminService";
    public static final String API_STORE_GROUP_EXTRACTOR_CLAIM_URI = API_STORE + "DefaultGroupExtractorClaimUri";
    public static final String API_STORE_MAP_EXISTING_AUTH_APPS = API_STORE + "MapExistingAuthApps";
    public static final String API_STORE_API_KEY_ALIAS = API_STORE + "ApiKeyAlias";
    public static final String API_STORE_API_KEY_GENERATOR_IMPL = API_STORE + "ApiKeyGeneratorImpl";
    public static final String API_STORE_API_KEY_SIGN_KEY_STORE = API_STORE + "APIKeyKeystore";
    public static final String WSO2_ANONYMOUS_USER = "wso2.anonymous.user";
    public static final String API_DEVPORTAL_ANONYMOUS_MODE = API_STORE + "EnableAnonymousMode";
    public static final String API_DEVPORTAL_ENABLE_CROSS_TENANT_SUBSCRIPTION = API_STORE +
            "EnableCrossTenantSubscription";

    public static final String API_PUBLISHER = "APIPublisher.";
    public static final String SHOW_API_PUBLISHER_URL_FROM_STORE = API_PUBLISHER + "DisplayURL";
    public static final String API_PUBLISHER_URL = API_PUBLISHER + "URL";
    public static final String API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS = API_PUBLISHER
            + "EnableAPIDocVisibilityLevels";
    // Configuration that need to enable to add access control to APIs in publisher
    public static final String API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS = API_PUBLISHER
            + "EnableAccessControl";
    public static final String API_PUBLISHER_APIS_PER_PAGE = API_PUBLISHER + "APIsPerPage";
    public static final String API_PUBLISHER_SUBSCRIBER_CLAIMS = API_PUBLISHER + "SubscriberClaims";
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
    public static final String EXTERNAL_API_STORE_PASSWORD = "Password";

    public static final String AUTH_MANAGER = "AuthManager.";
    public static final String AUTH_MANAGER_URL = AUTH_MANAGER + "ServerURL";
    public static final String AUTH_MANAGER_USERNAME = AUTH_MANAGER + "Username";
    public static final String AUTH_MANAGER_PASSWORD = AUTH_MANAGER + "Password";
    public static final String ENABLE_MTLS_FOR_APIS = "EnableMTLSForAPIs";

    public static final String IDENTITY_PROVIDER = "IdentityProvider.";
    public static final String IDENTITY_PROVIDER_AUTHORIZE_ENDPOINT = IDENTITY_PROVIDER + "AuthorizeEndpoint";
    public static final String IDENTITY_PROVIDER_OIDC_LOGOUT_ENDPOINT = IDENTITY_PROVIDER + "OIDCLogoutEndpoint";
    public static final String IDENTITY_PROVIDER_SERVER_URL = IDENTITY_PROVIDER + "ServerURL";
    public static final String IDENTITY_PROVIDER_OIDC_CHECK_SESSION_ENDPOINT = IDENTITY_PROVIDER + "CheckSessionEndpoint";
    public static final String IDENTITY_PROVIDER_OIDC_CHECK_SESSION_URL = "/oidc/checksession";

    public static final String SELF_SIGN_UP = "SelfSignUp.";
    public static final String SELF_SIGN_UP_ENABLED = SELF_SIGN_UP + "Enabled";
    public static final String SELF_SIGN_UP_ROLE = SELF_SIGN_UP + "SubscriberRoleName";

    //elements in the configuration file in the registry related to self signup
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

    public static final String CORS_CONFIGURATION_ACCESS_CTL_EXPOSE_HEADERS = CORS_CONFIGURATION
            + "Access-Control-Expose-Headers";

    public static final String CORS_CONFIGURATION_ACCESS_CTL_ALLOW_CREDENTIALS = CORS_CONFIGURATION
            + "Access-Control-Allow-Credentials";

    public static final String API_KEY_TYPE = "AM_KEY_TYPE";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";

    public static final String BILLING_AND_USAGE_CONFIGURATION = "EnableBillingAndUsage";

    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";
    public static final String DEFAULT_APPLICATION_DESCRIPTION = "This is the default application";
    public static final String BASIC_AUTH_APPLICATION_NAME = "BasicAuthApplication";

    public static final QName POLICY_ELEMENT = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy",
            "Policy");

    public static final String THROTTLE_NAMESPACE = "http://www.wso2.org/products/wso2commons/throttle";

    public static final QName ASSERTION_ELEMENT = new QName(THROTTLE_NAMESPACE, "MediatorThrottleAssertion");
    public static final QName THROTTLE_ID_ELEMENT = new QName(THROTTLE_NAMESPACE, "ID");
    public static final QName THROTTLE_ID_DISPLAY_NAME_ELEMENT = new QName(THROTTLE_NAMESPACE, "displayName");

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
    public static final String BLOCKED_REASON_KEY = "BLOCKED_REASON";
    // The following properties describes the reason for the throttle out.
    public static final String THROTTLE_OUT_REASON_HARD_LIMIT_EXCEEDED = "HARD_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_SOFT_LIMIT_EXCEEDED = "SOFT_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_API_LIMIT_EXCEEDED = "API_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_RESOURCE_LIMIT_EXCEEDED = "RESOURCE_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_APPLICATION_LIMIT_EXCEEDED = "APPLICATION_LIMIT_EXCEEDED";
    public static final String THROTTLE_OUT_REASON_SUBSCRIPTION_LIMIT_EXCEEDED = "SUBSCRIPTION_LIMIT_EXCEEDED";

    public static final String API_ANALYTICS = "Analytics.";
    public static final String API_USAGE_ENABLED = API_ANALYTICS + "Enabled";
    public static final String API_USAGE_BAM_SERVER_URL_GROUPS = API_ANALYTICS + "StreamProcessorServerURL";
    public static final String API_USAGE_BAM_SERVER_AUTH_URL_GROUPS = API_ANALYTICS + "StreamProcessorAuthServerURL";
    public static final String API_USAGE_BUILD_MSG = API_ANALYTICS + "PublishResponseMessageSize";
    public static final String API_USAGE_BAM_SERVER_USER = API_ANALYTICS + "StreamProcessorUsername";
    public static final String API_USAGE_BAM_SERVER_PASSWORD = API_ANALYTICS + "StreamProcessorPassword";
    public static final String API_USAGE_SKIP_EVENT_RECEIVER_CONN = API_ANALYTICS + "SkipEventReceiverConnection";
    public static final String API_USAGE_PUBLISHER_CLASS = API_ANALYTICS + "PublisherClass";
    public static final String API_USAGE_DATA_SOURCE_NAME = "WSO2AM_STATS_DB";
    public static final String API_USAGE_STREAMS = API_ANALYTICS + "Streams.";
    public static final String STAT_PROVIDER_IMPL = API_ANALYTICS + "StatsProviderImpl";
    public static final String API_USAGE_REQUEST_STREAM = API_USAGE_STREAMS + "Request.";
    public static final String API_USAGE_RESPONSE_STREAM = API_USAGE_STREAMS + "Response.";
    public static final String API_USAGE_FAULT_STREAM = API_USAGE_STREAMS + "Fault.";
    public static final String API_USAGE_THROTTLE_STREAM = API_USAGE_STREAMS + "Throttle.";
    public static final String API_USAGE_EXECUTION_TIME_STREAM = API_USAGE_STREAMS + "ExecutionTime.";
    public static final String API_ALERT_TYPES_STREAM = API_USAGE_STREAMS + "AlertTypes.";
    public static final String API_REQUEST_STREAM_NAME = API_USAGE_REQUEST_STREAM + "Name";
    public static final String API_REQUEST_STREAM_VERSION = API_USAGE_REQUEST_STREAM + "Version";
    public static final String API_RESPONSE_STREAM_NAME = API_USAGE_RESPONSE_STREAM + "Name";
    public static final String API_RESPONSE_STREAM_VERSION = API_USAGE_RESPONSE_STREAM + "Version";
    public static final String API_FAULT_STREAM_NAME = API_USAGE_FAULT_STREAM + "Name";
    public static final String API_FAULT_STREAM_VERSION = API_USAGE_FAULT_STREAM + "Version";
    public static final String API_THROTTLE_STREAM_NAME = API_USAGE_THROTTLE_STREAM + "Name";
    public static final String API_THRORRLE_STREAM_VERSION = API_USAGE_THROTTLE_STREAM + "Version";
    public static final String API_EXECUTION_TIME_STREAM_NAME = API_USAGE_EXECUTION_TIME_STREAM + "Name";
    public static final String API_EXECUTION_TIME_STREAM_VERSION = API_USAGE_EXECUTION_TIME_STREAM + "Version";
    public static final String API_ALERT_TYPES_STREAM_NAME = API_ALERT_TYPES_STREAM + "Name";
    public static final String API_ALERT_TYPES_STREAM_VERSION = API_ALERT_TYPES_STREAM + "Version";
    public static final String API_USAGE_SKIP_WORKFLOW_EVENT_RECEIVER_CONN =
            API_ANALYTICS + "SkipWorkflowEventPublisher";

    public static final String API_USAGE_WF_STREAM = API_USAGE_STREAMS + "Workflow.";
    public static final String API_WF_STREAM_NAME = API_USAGE_WF_STREAM + "Name";
    public static final String API_WF_STREAM_VERSION = API_USAGE_WF_STREAM + "Version";
    //Rest API Config data in api-config.xml
    public static final String API_USAGE_DAS_REST_API_URL = API_ANALYTICS + "StreamProcessorRestApiURL";
    public static final String API_USAGE_DAS_REST_API_USER = API_ANALYTICS + "StreamProcessorRestApiUsername";
    public static final String API_USAGE_DAS_REST_API_PASSWORD = API_ANALYTICS + "StreamProcessorRestApiPassword";

    public static final String UNLIMITED_TIER = "Unlimited";
    public static final String UNLIMITED_TIER_DESC = "Allows unlimited requests";

    public static final String UNAUTHENTICATED_TIER = "Unauthenticated";
    public static final String BLOCKING_EVENT_PUBLISHER = "blockingEventPublisher";
    public static final String EVENT_HUB_NOTIFICATION_EVENT_PUBLISHER = "eventHubNotificationEventPublisher";
    public static final String NOTIFICATION_EVENT_PUBLISHER = "notificationPublisher";

    public static final int AM_CREATOR_APIMGT_EXECUTION_ID = 200;
    public static final int AM_CREATOR_GOVERNANCE_EXECUTION_ID = 201;
    public static final int AM_PUBLISHER_APIMGT_EXECUTION_ID = 202;
    public static final int AM_CREATOR_LIFECYCLE_EXECUTION_ID = 203;
    public static final int AM_PUBLISHER_LIFECYCLE_EXECUTION_ID = 204;

    public static final QName THROTTLE_CONTROL_ELEMENT = new QName(THROTTLE_NAMESPACE, "Control");
    public static final QName THROTTLE_MAXIMUM_COUNT_ELEMENT = new QName(THROTTLE_NAMESPACE, "MaximumCount");
    public static final QName THROTTLE_UNIT_TIME_ELEMENT = new QName(THROTTLE_NAMESPACE, "UnitTime");
    public static final QName THROTTLE_ATTRIBUTES_ELEMENT = new QName(THROTTLE_NAMESPACE, "Attributes");
    public static final QName THROTTLE_ATTRIBUTE_ELEMENT = new QName(THROTTLE_NAMESPACE, "Attribute");
    public static final QName THROTTLE_DESCRIPTION_ELEMENT = new QName(THROTTLE_NAMESPACE, "Description");
    public static final QName THROTTLE_TIER_PLAN_ELEMENT = new QName(THROTTLE_NAMESPACE, THROTTLE_TIER_PLAN_ATTRIBUTE);
    public static final String THROTTLE_ATTRIBUTE_DISPLAY_NAME = "displayName";

    public static final String TIER_DESC_NOT_AVAILABLE = "Tire Description is not available";

    public static final String AUTH_TYPE_DEFAULT = "DEFAULT";
    public static final String AUTH_TYPE_NONE = "NONE";
    public static final String AUTH_TYPE_USER = "USER";
    public static final String AUTH_TYPE_APP = "APP";

    public static final String TIER_PERMISSION_ALLOW = "allow";
    public static final String TIER_PERMISSION_DENY = "deny";

    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "current_tenant";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "all_tenants";
    public static final String SUBSCRIPTION_TO_SPECIFIC_TENANTS = "specific_tenants";
    public static final String NO_PERMISSION_ERROR = "noPermissions";
    public static final String JSON_PARSE_ERROR = "parseErrors";

    public static final String ACCOUNT_LOCKED_CLAIM = "http://wso2.org/claims/identity/accountLocked";

    //TODO: move this to a common place (& Enum) to be accessible by all components
    public static class KeyValidationStatus {

        public static final int API_AUTH_GENERAL_ERROR = 900900;
        public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
        public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
        public static final int API_BLOCKED = 900907;
        public static final int API_AUTH_RESOURCE_FORBIDDEN = 900908;
        public static final int SUBSCRIPTION_INACTIVE = 900909;
        public static final int INVALID_SCOPE = 900910;
        public static final int KEY_MANAGER_NOT_AVAILABLE = 900912;

        private KeyValidationStatus() {

        }
    }

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";

    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";

    //API caching related constants
    public static final String API_MANAGER_CACHE_MANAGER = "API_MANAGER_CACHE";
    public static final String API_CONTEXT_CACHE_MANAGER = "API_CONTEXT_CACHE_MANAGER";
    public static final String RESOURCE_CACHE_NAME = "resourceCache";
    public static final String POLICY_CACHE_CONTEXT = "POLICY:";
    public static final String GATEWAY_KEY_CACHE_NAME = "gatewayKeyCache";
    public static final String GATEWAY_USERNAME_CACHE_NAME = "gatewayUsernameCache";
    public static final String GATEWAY_INVALID_USERNAME_CACHE_NAME = "gatewayInvalidUsernameCache";
    public static final String GATEWAY_BASIC_AUTH_RESOURCE_CACHE_NAME = "gatewayBasicAuthResourceCache";
    public static final String GATEWAY_CERTIFICATE_CACHE_NAME = "gatewayCertificateCache";
    public static final String GATEWAY_API_KEY_CACHE_NAME = "gatewayApiKeyCache";
    public static final String GATEWAY_API_KEY_DATA_CACHE_NAME = "gatewayApiKeyKeyCache";
    public static final String GATEWAY_INVALID_API_KEY_CACHE_NAME = "gatewayInvalidApiKeyCache";
    public static final String GATEWAY_TOKEN_CACHE_NAME = "GATEWAY_TOKEN_CACHE";
    public static final String GATEWAY_INVALID_TOKEN_CACHE_NAME = "GATEWAY_INVALID_TOKEN_CACHE";
    public static final String REST_API_TOKEN_CACHE_NAME = "RESTAPITokenCache";
    public static final String REST_API_INVALID_TOKEN_CACHE_NAME = "RESTAPIInvalidTokenCache";
    public static final String GATEWAY_JWT_TOKEN_CACHE = "GatewayJWTTokenCache";

    public static final String KEY_CACHE_NAME = "keyCache";
    public static final String API_CONTEXT_CACHE = "apiContextCache";
    public static final String WORKFLOW_CACHE_NAME = "workflowCache";
    public static final String APP_SCOPE_CACHE = "appScopeCache";
    public static final String TIERS_CACHE = "tiersCache";
    public static final int API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS = 3650;
    public static final String CLAIMS_APIM_CACHE = "claimsLocalCache";
    public static final String APP_SUBSCRIPTION_SCOPE_CACHE = "appSubscriptionScopeCache";
    public static final String APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE = "appSubscriptionFilteredScopeCache";
    public static final String API_USER_ROLE_CACHE = "appPublisherUserRoleCache";
    public static final String API_PUBLISHER_ADMIN_PERMISSION_CACHE = "apimAdminPermissionCache";
    public static final String API_SUBSCRIBER_CACHE = "subscriberCache";
    public static final String EMAIL_CLAIM = "http://wso2.org/claims/emailaddress";

    //URI Authentication Schemes
    public static final Set<String> SUPPORTED_METHODS =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(new String[]{"get", "put", "post", "delete", "patch", "head", "options"})));
    public static final String PARAMETERS = "parameters";
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APP_AND_USER = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String NO_MATCHING_AUTH_SCHEME = "noMatchedAuthScheme";

    public static final String EVERYONE_ROLE = "internal/everyone";
    public static final String ANONYMOUS_ROLE = "system/wso2.anonymous.role";
    public static final String SUBSCRIBER_ROLE = "Internal/subscriber";

    // Anonymous end user, to be used with ACCESS_TOKEN_USER_TYPE_APPLICATION
    public static final String END_USER_ANONYMOUS = "anonymous";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    public static final String API_ACTION = "action";
    public static final String API_ADD_ACTION = "addAPI";
    public static final String API_GET_ACTION = "getAPI";
    public static final String API_UPDATE_ACTION = "updateAPI";
    public static final String API_CHANGE_STATUS_ACTION = "updateStatus";
    public static final String API_REMOVE_ACTION = "removeAPI";
    public static final String API_COPY_ACTION = "createNewAPI";
    public static final String API_LOGIN_ACTION = "login";
    public static final String API_LOGOUT_ACTION = "logout";
    public static final String APISTORE_LOGIN_USERNAME = "username";
    public static final String APISTORE_LOGIN_PASSWORD = "password";
    public static final String APISTORE_LOGIN_URL = "/site/blocks/user/login/ajax/login.jag";
    public static final String APISTORE_PUBLISH_URL = "/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String APISTORE_ADD_URL = "/site/blocks/item-add/ajax/add.jag";
    public static final String APISTORE_DELETE_URL = "/site/blocks/item-add/ajax/remove.jag";
    public static final String APISTORE_LIST_URL = "/site/blocks/listing/ajax/item-list.jag";
    public static final String APISTORE_COPY_URL = "/site/blocks/overview/ajax/overview.jag";

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

        private OperationParameter() {

        }
    }

    public static class SubscriptionValidationResources {

        public static final String APIS = "/apis";
        public static final String APPLICATIONS = "/applications";
        public static final String SUBSCRIPTIONS = "/subscriptions";
        public static final String SUBSCRIBERS = "/subscribers";
        public static final String APPLICATION_KEY_MAPPINGS = "/application-key-mappings";
        public static final String APPLICATION_POLICIES = "/application-policies";
        public static final String API_POLICIES = "/api-policies";
        public static final String SUBSCRIPTION_POLICIES = "/subscription-policies";
        public static final String SCOPES = "/scopes";

        private SubscriptionValidationResources() {

        }
    }

    public static class CORSHeaders {

        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
        public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
        public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        public static final String ALLOW_HEADERS_HANDLER_VALUE = "allowHeaders";
        public static final String ALLOW_METHODS_HANDLER_VALUE = "allowedMethods";
        public static final String ALLOW_ORIGIN_HANDLER_VALUE = "allowedOrigins";
        public static final String ALLOW_CREDENTIALS_HANDLER_VALUE = "allowCredentials";
        public static final String IMPLEMENTATION_TYPE_HANDLER_VALUE = "apiImplementationType";

        private CORSHeaders() {

        }
    }

    public static final String EXTENSION_HANDLER_POSITION = "ExtensionHandlerPosition";
    public static final String ENABLE_PER_TENANT_SERVICE_PROVIDER_CREATION = "EnablePerTenantServiceProviderCreation";
    public static final String DISABLE_DEFAULT_APPLICATION_CREATION = "DisableDefaultApplicationCreation";
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
    }

    public static class AppRegistrationStatus {

        public static final String REGISTRATION_CREATED = "CREATED";
        public static final String REGISTRATION_APPROVED = "APPROVED";
        public static final String REGISTRATION_REJECTED = "REJECTED";
        public static final String REGISTRATION_COMPLETED = "COMPLETED";
    }

    public static class FrontEndParameterNames {

        public static final String CONSUMER_KEY = "consumerKey";
        public static final String CONSUMER_SECRET = "consumerSecret";
        public static final String CLIENT_DETAILS = "appDetails";
        public static final String CALLBACK_URL = "callbackUrl";
        public static final String KEY_STATE = "keyState";
        public static final String KEY_MAPPING_ID = "keyMappingId";
    }

    public static class AccessTokenConstants {

        public static final String ACCESS_TOKEN = "accessToken";
        public static final String VALIDITY_TIME = "validityTime";
        public static final String TOKEN_SCOPES = "tokenScope";
    }

    //key  of the endpoint securevault
    public static final String API_SECUREVAULT_ENABLE = "EnableSecureVault";

    public static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
    public static final String API_ELECTED_RESOURCE = "API_ELECTED_RESOURCE";

    // GraphQL related constants
    public static final String API_TYPE = "API_TYPE";
    public static final String HTTP_VERB = "HTTP_VERB";
    public static final String GRAPHQL_API = "GRAPHQL";
    public static final String GRAPHQL_API_OPERATION_RESOURCE = "OPERATION_RESOURCE";
    public static final String GRAPHQL_API_OPERATION_TYPE = "OPERATION_TYPE";
    public static final String GRAPHQL_API_OPERATION_VERB_INFO_DTO = "OPERATION_VERB_INFO_DTO";
    public static final String[] GRAPHQL_SUPPORTED_METHODS = {"QUERY", "MUTATION", "SUBSCRIPTION"};
    public static final String API_GRAPHQL_SCHEMA_RESOURCE_LOCATION = API_APPLICATION_DATA_LOCATION + "/graphql/";
    public static final String GRAPHQL_SCHEMA_FILE_EXTENSION = ".graphql";
    public static final String GRAPHQL_LOCAL_ENTRY_EXTENSION = "_graphQL";
    public static final String GRAPHQL_SCHEMA_PROVIDER_SEPERATOR = "--";
    public static final String GRAPHQL_RESOURCE_PATH = "/*";
    public static final String GRAPHQL_SCHEMA_DEFINITION_SEPARATOR = "schemaDefinition=";
    public static final String GRAPHQL_QUERY = "Query";
    public static final String GRAPHQL_SWAGGER_QUERY = "query";
    public static final String GRAPHQL_MUTATION = "Mutation";
    public static final String GRAPHQL_SUBSCRIPTION = "Subscription";
    public static final String SCOPE_ROLE_MAPPING = "WSO2ScopeRoleMapping";
    public static final String SCOPE_OPERATION_MAPPING = "WSO2ScopeOperationMapping";
    public static final String OPERATION_THROTTLING_MAPPING = "WSO2OperationThrottlingMapping";
    public static final String OPERATION_AUTH_SCHEME_MAPPING = "WSO2OperationAuthSchemeMapping";
    public static final String OPERATION_SECURITY_ENABLED = "Enabled";
    public static final String OPERATION_SECURITY_DISABLED = "Disabled";
    public static final String GRAPHQL_PAYLOAD = "GRAPHQL_PAYLOAD";
    public static final String GRAPHQL_SCHEMA = "GRAPHQL_SCHEMA";
    public static final String GRAPHQL_ACCESS_CONTROL_POLICY = "WSO2GraphQLAccessControlPolicy";
    public static final String QUERY_ANALYSIS_COMPLEXITY = "complexity";
    public static final String MAXIMUM_QUERY_COMPLEXITY = "max_query_complexity";
    public static final String MAXIMUM_QUERY_DEPTH = "max_query_depth";
    public static final String GRAPHQL_MAX_DEPTH = "graphQLMaxDepth";
    public static final String GRAPHQL_MAX_COMPLEXITY = "graphQLMaxComplexity";
    public static final String GRAPHQL_ADDITIONAL_TYPE_PREFIX = "WSO2";

    //URI Authentication Schemes
    public static final Set<String> GRAPHQL_SUPPORTED_METHOD_LIST =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(new String[]{"QUERY", "MUTATION", "SUBSCRIPTION", "head", "options"})));

    public static final String OAUTH2_DEFAULT_SCOPE = "default";

    public static final String RECENTLY_ADDED_API_CACHE_NAME = "RECENTLY_ADDED_API";
    public static final String VELOCITY_LOGGER = "VelocityLogger";

    public static final String SHA_256 = "SHA-256";

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

        private DigestAuthConstants() {

        }
    }

    public static class OAuthConstants {

        public static final String OAUTH = "OAUTH";
        public static final String OAUTH_ID = "id";
        public static final String UNIQUE_IDENTIFIER = "uniqueIdentifier";
        public static final String TOKEN_API_URL = "tokenUrl";
        public static final String OAUTH_CLIENT_ID = "clientId";
        public static final String OAUTH_CLIENT_SECRET = "clientSecret";
        public static final String OAUTH_USERNAMEPASSWORD = "usernamePassword";
        public static final String GRANT_TYPE = "grantType";
        public static final String OAUTH_CUSTOM_PARAMETERS = "customParameters";
        public static final String CLIENT_CREDENTIALS = "CLIENT_CREDENTIALS";
        public static final String PASSWORD = "PASSWORD";

        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String CONTENT_TYPE_HEADER = "Content-Type";
        public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String CLIENT_CRED_GRANT_TYPE = "grant_type=client_credentials";
        public static final String PASSWORD_GRANT_TYPE = "grant_type=password";
        public static final String REFRESH_TOKEN_GRANT_TYPE = "grant_type=refresh_token";

        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SCOPE = "scope";
        public static final String TOKEN_TYPE = "token_type";
        public static final String EXPIRES_IN = "expires_in";

        // Properties in Endpoint Config
        public static final String ENDPOINT_SECURITY_PRODUCTION = "production";
        public static final String ENDPOINT_SECURITY_SANDBOX = "sandbox";
        public static final String ENDPOINT_SECURITY_PASSWORD = "password";
        public static final String ENDPOINT_SECURITY_TYPE = "type";
        public static final String ENDPOINT_SECURITY_ENABLED = "enabled";
        public static final String ENDPOINT_SECURITY_USERNAME = "username";

        private OAuthConstants() {

        }
    }

    // For Redis Configuration
    public static final String REDIS_CONFIG = "RedisConfig.";
    public static final String CONFIG_REDIS_HOST = REDIS_CONFIG + "RedisHost";
    public static final String CONFIG_REDIS_PORT = REDIS_CONFIG + "RedisPort";
    public static final String CONFIG_REDIS_USER = REDIS_CONFIG + "RedisUser";
    public static final String CONFIG_REDIS_PASSWORD = REDIS_CONFIG + "RedisPassword";
    public static final String CONFIG_REDIS_DATABASE_ID = REDIS_CONFIG + "RedisDatabaseId";
    public static final String CONFIG_REDIS_CONNECTION_TIMEOUT = REDIS_CONFIG + "RedisConnectionTimeout";
    public static final String CONFIG_REDIS_IS_SSL_ENABLED = REDIS_CONFIG + "RedisIsSslEnabled";

    public static final String IS_REDIS_ENABLED = "isRedisEnabled";
    public static final String REDIS_HOST = "redisHost";
    public static final String REDIS_PORT = "redisPort";
    public static final String REDIS_USER = "redisUser";
    public static final String REDIS_PASSWORD = "redisPassword";
    public static final String REDIS_DATABASE_ID = "redisDatabaseId";
    public static final String REDIS_CONNECTION_TIMEOUT = "redisConnectionTimeout";
    public static final String REDIS_IS_SSL_ENABLED = "redisIsSslEnabled";

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

    public static final String HTTP_CLIENT_MAX_TOTAL = "HttpClient.MaxTotal";
    public static final String HTTP_CLIENT_DEFAULT_MAX_PER_ROUTE = "HttpClient.DefaultMaxPerRoute";

    public static final String PROXY_ENABLE = "ProxyConfig.Enable";
    public static final String PROXY_HOST = "ProxyConfig.Host";
    public static final String PROXY_PORT = "ProxyConfig.Port";
    public static final String PROXY_USERNAME = "ProxyConfig.Username";
    public static final String PROXY_PASSWORD = "ProxyConfig.Password";
    public static final String NON_PROXY_HOSTS = "ProxyConfig.NonProxyHosts";
    public static final String PROXY_PROTOCOL = "ProxyConfig.Protocol";

    public static final String KEYMANAGER_HOSTNAME = "keyManagerHostname";
    public static final String KEYMANAGER_PORT = "keyManagerPort";
    public static final String KEYMANAGER_SERVERURL = API_KEY_VALIDATOR + "ServerURL";
    public static final String CARBON_LOCALIP = "carbon.local.ip";

    public static final String APIPROVIDER_HOSTCACHE = "apiProvideHostObjectCache";
    public static final String TENANTCOUNT_CACHEKEY = "apiProviderCacheKey";

    public static final String API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION = API_STORE + "GroupingExtractor";
    public static final String API_STORE_REST_API_GROUP_EXTRACTOR_IMPLEMENTATION =
            API_STORE + "RESTApiGroupingExtractor";
    public static final String API_CUSTOM_SEQUENCES_FOLDER_LOCATION =
            "repository" + File.separator + "resources" + File.separator + "customsequences";
    public static final String WORKFLOW_EXTENSION_LOCATION =
            "repository" + File.separator + "resources" + File.separator + "default-workflow-extensions.xml";
    public static final String API_CUSTOM_SEQUENCE_TYPE_IN = "in";
    public static final String API_CUSTOM_SEQUENCE_TYPE_OUT = "out";
    public static final String API_CUSTOM_SEQUENCE_TYPE_FAULT = "fault";
    public static final String MEDIATION_SEQUENCE_ELEM = "sequence";
    public static final String MEDIATION_CONFIG_EXT = ".xml";
    public static final String API_CUSTOM_SEQ_IN_EXT = "--In";
    public static final String API_CUSTOM_SEQ_OUT_EXT = "--Out";
    public static final String API_CUSTOM_SEQ_FAULT_EXT = "--Fault";
    public static final String API_CUSTOM_SEQ_JSON_FAULT = "json_fault.xml";

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

    public enum SupportedCustomPolicyKeys {
        APP_ID("APP_ID"),
        IP("IP"),
        STARTING_IP("STARTING_IP"),
        END_IP("END_IP"),
        ACCESS_TOKEN("ACCESS_TOKEN"),
        USERNAME("APP_ID"),
        QUERY_PARAM("QUERY_PARAM"),
        HEADER("HEADER"),
        BANDWIDTH("BANDWIDTH"),
        JWT_CLAIM("JWT_CLAIM"),
        CONTEXT("CONTEXT"),
        VERSION("VERSION");

        private final String name;

        SupportedCustomPolicyKeys(String s) {

            name = s;
        }

        public String getValue() {

            return this.name;
        }
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
    public static final String SWAGGER_X_AMZN_RESOURCE_NAME = "x-amzn-resource-name";
    public static final String SWAGGER_X_AMZN_RESOURCE_TIMEOUT = "x-amzn-resource-timeout";
    public static final String SWAGGER_X_AUTH_TYPE = "x-auth-type";
    public static final String SWAGGER_X_THROTTLING_TIER = "x-throttling-tier";
    public static final String SWAGGER_X_THROTTLING_BANDWIDTH = "x-throttling-bandwidth";
    public static final String SWAGGER_X_MEDIATION_SCRIPT = "x-mediation-script";
    public static final String SWAGGER_X_WSO2_SECURITY = "x-wso2-security";
    public static final String WSO2_APP_SECURITY_TYPES = "security-types";
    public static final String OPTIONAL = "optional";
    public static final String MANDATORY = "mandatory";
    public static final String RESPONSE_CACHING_ENABLED = "enabled";
    public static final String RESPONSE_CACHING_TIMEOUT = "cacheTimeoutInSeconds";
    public static final String SWAGGER_X_WSO2_SCOPES = "x-wso2-scopes";
    public static final String SWAGGER_X_EXAMPLES = "x-examples";
    public static final String SWAGGER_SCOPE_KEY = "key";
    public static final String SWAGGER_NAME = "name";
    public static final String SWAGGER_SCHEMES = "schemes";
    public static final String SWAGGER_DESCRIPTION = "description";
    public static final String SWAGGER_SERVERS = "servers";
    public static final String SWAGGER_SUMMARY = "summary";
    public static final String SWAGGER_ROLES = "roles";
    public static final String SWAGGER_TITLE = "title";
    public static final String SWAGGER_EMAIL = "email";
    public static final String SWAGGER_CONTACT = "contact";
    public static final String SWAGGER_SECURITY_TYPE = "type";
    public static final String SWAGGER_SECURITY_OAUTH2 = "oauth2";
    public static final String SWAGGER_SECURITY_OAUTH2_IMPLICIT = "implicit";
    public static final String SWAGGER_SECURITY_OAUTH2_PASSWORD = "password";
    public static final String SWAGGER_SECURITY_OAUTH2_AUTHORIZATION_URL = "authorizationUrl";
    public static final String SWAGGER_SECURITY_OAUTH2_TOKEN_URL = "tokenUrl";
    public static final String SWAGGER_SECURITY_OAUTH2_FLOW = "flow";
    public static final String SWAGGER_VER = "version";
    public static final String SWAGGER_OBJECT_NAME_APIM = "apim";
    public static final String SWAGGER_PATHS = "paths";
    public static final String SWAGGER_RESPONSES = "responses";
    public static final String SWAGGER = "swagger";
    public static final String SWAGGER_HOST = "host";
    public static final String SWAGGER_V2 = "2.0";
    public static final String SWAGGER_INFO = "info";
    public static final String SWAGGER_RESPONSE_200 = "200";
    public static final String SWAGGER_SECURITY_DEFINITIONS = "securityDefinitions";
    public static final String SWAGGER_APIM_DEFAULT_SECURITY = "default";
    public static final String SWAGGER_APIM_RESTAPI_SECURITY = "OAuth2Security";
    public static final String OPEN_API_V3 = "3.0.x";
    public static final String OPEN_API = "openapi";
    public static final String OPEN_API_VERSION_REGEX = "3\\.0\\.\\d{1,}";
    public static final String SWAGGER_IS_MISSING_MSG = "swagger is missing";
    public static final String OPENAPI_IS_MISSING_MSG = "openapi is missing";
    public static final String SWAGGER_X_SCOPES_BINDINGS = "x-scopes-bindings";

    //swagger v1.2 constants
    public static final String SWAGGER_RESOURCES = "resources";
    public static final String ENVIRONMENTS_NONE = "none";
    public static final String SWAGGER_BASEPATH = "basePath";
    public static final String SWAGGER_OPERATIONS = "operations";
    public static final String SWAGGER_SCOPE = "scope";
    public static final String SWAGGER_SCOPES = "scopes";
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

    //swagger MG related constants
    public static final String X_WSO2_AUTH_HEADER = "x-wso2-auth-header";
    public static final String X_THROTTLING_TIER = "x-throttling-tier";
    public static final String X_WSO2_CORS = "x-wso2-cors";
    public static final String X_WSO2_PRODUCTION_ENDPOINTS = "x-wso2-production-endpoints";
    public static final String X_WSO2_SANDBOX_ENDPOINTS = "x-wso2-sandbox-endpoints";
    public static final String X_WSO2_BASEPATH = "x-wso2-basePath";
    public static final String X_WSO2_TRANSPORTS = "x-wso2-transports";
    public static final String X_WSO2_MUTUAL_SSL = "x-wso2-mutual-ssl";
    public static final String X_WSO2_APP_SECURITY = "x-wso2-application-security";
    public static final String X_WSO2_RESPONSE_CACHE = "x-wso2-response-cache";
    public static final String X_WSO2_DISABLE_SECURITY = "x-wso2-disable-security";
    public static final String X_WSO2_THROTTLING_TIER = "x-wso2-throttling-tier";
    public static final String X_WSO2_REQUEST_INTERCEPTOR = "x-wso2-request-interceptor";
    public static final String X_WSO2_RESPONSE_INTERCEPTOR = "x-wso2-response-interceptor";
    public static final String X_WSO2_ENDPOINT_TYPE = "type";

    //API Constants
    public static final String API_DATA_NAME = "name";
    public static final String API_DATA_PROVIDER = "provider";
    public static final String API_DATA_VERSION = "version";
    public static final String API_DATA_DESCRIPTION = "description";
    public static final String API_DATA_BUSINESS_OWNER = "businessOwner";
    public static final String API_DATA_RATES = "rates";
    public static final String API_DATA_ENDPOINT = "endpoint";
    public static final String API_DATA_THUMB_URL = "thumbnailurl";
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
    public static final String API_DATA_PRODUCTION_ENDPOINTS = "production_endpoints";
    public static final String API_DATA_SANDBOX_ENDPOINTS = "sandbox_endpoints";
    public static final String API_DATA_URL = "url";
    public static final String API_UUID = "apiUUID";

    // mock response generation
    public static final String MOCK_GEN_POLICY_LIST = "policyList";

    public static final String IMPLEMENTATION_STATUS = "implementation_status";
    public static final String ENDPOINT_TYPE_DEFAULT = "default";
    public static final String ENDPOINT_TYPE_FAILOVER = "failover";
    public static final String ENDPOINT_TYPE_LOADBALANCE = "load_balance";
    public static final String ENDPOINT_CONFIG = "endpoint_config";
    public static final String ENDPOINT_TYPE_HTTP = "http";
    public static final String ENDPOINT_TYPE_ADDRESS = "address";
    public static final String ENDPOINT_TYPE_AWSLAMBDA = "awslambda";
    public static final String ENDPOINT_PRODUCTION_FAILOVERS = "production_failovers";
    public static final String ENDPOINT_SANDBOX_FAILOVERS = "sandbox_failovers";
    public static final String ENDPOINT_PRODUCTION_ENDPOINTS = "production_endpoints";
    public static final String ENDPOINT_SANDBOX_ENDPOINTS = "sandbox_endpoints";
    public static final String ENDPOINT_URLS = "urls";
    public static final String ENDPOINT_URL = "url";
    public static final String ENDPOINT_SECURITY_TYPE = "type";
    public static final String ENDPOINT_SECURITY_TYPE_BASIC = "basic";
    public static final String ENDPOINT_SECURITY_TYPE_DIGEST = "digest";
    public static final String ENDPOINT_SECURITY_TYPE_OAUTH = "oauth";
    public static final String ENDPOINT_SECURITY_USERNAME = "username";
    public static final String ENDPOINT_SECURITY_CONFIG = "securityConfig";
    public static final String ENDPOINT_SECURITY = "endpoint_security";
    public static final String ENDPOINT_SECURITY_PRODUCTION = "production";
    public static final String ENDPOINT_SECURITY_SANDBOX = "sandbox";
    public static final String ENDPOINT_SECURITY_PASSWORD = "password";
    public static final String ENDPOINT_SECURITY_CLIENT_ID = "clientId";
    public static final String ENDPOINT_SECURITY_CLIENT_SECRET = "clientSecret";
    public static final String ENDPOINT_SECURITY_ENABLED = "enabled";

    public static final String API_ENDPOINT_CONFIG_TIMEOUT = "timeout";
    public static final String API_ENDPOINT_CONFIG_PROTOCOL_TYPE = "endpoint_type";

    public static final String ACTIVITY_ID = "activityID";
    public static final String USER_AGENT = "User-Agent";

    public static final String REST_API_SCOPE = "Scope";
    public static final String REST_API_SCOPE_NAME = "Name";
    public static final String REST_API_SCOPE_ROLE = "Roles";
    public static final String REST_API_SCOPES_CONFIG = "RESTAPIScopes";
    public static final String REST_API_ROLE_MAPPINGS_CONFIG = "RoleMappings";
    public static final String APIM_SUBSCRIBE_SCOPE = "apim:subscribe";

    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTPS_PROTOCOL_URL_PREFIX = "https://";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL_URL_PREFIX = "http://";
    public static final int HTTPS_PROTOCOL_PORT = 443;
    public static final int HTTP_PROTOCOL_PORT = 80;

    public static final String EMAIL_TRANSPORT = "mailto";

    public static final long MAX_FILE_SIZE = 1024L;

    public static final String REGISTRY_RESOURCE_PREFIX = "/registry/resource";
    public static final String REGISTRY_RESOURCE_URL_PREFIX =
            "/registry/resource/_system/governance/apimgt/applicationdata/provider/";

    public enum RegistryResourceTypesForUI {
        TAG_THUMBNAIL
    }

    public static final String API_LC_ACTION_DEPRECATE = "Deprecate";
    public static final String DEPRECATE_CHECK_LIST_ITEM = "Deprecate old versions after publishing the API";
    public static final String RESUBSCRIBE_CHECK_LIST_ITEM = "Requires re-subscription when publishing the API";
    public static final String PUBLISH_IN_PRIVATE_JET_MODE = "Publish In Private-Jet Mode";

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

    //Constants for swagger-codegen client generation
    public static final String CLIENT_CODEGEN_GROUPID = "SwaggerCodegen.ClientGeneration.GroupId";
    public static final String CLIENT_CODEGEN_ARTIFACTID = "SwaggerCodegen.ClientGeneration.ArtifactId";
    public static final String CLIENT_CODEGEN_MODAL_PACKAGE = "SwaggerCodegen.ClientGeneration.ModelPackage";
    public static final String CLIENT_CODEGEN_API_PACKAGE = "SwaggerCodegen.ClientGeneration.ApiPackage";
    public static final String CLIENT_CODEGEN_SUPPORTED_LANGUAGES =
            "SwaggerCodegen.ClientGeneration.SupportedLanguages";

    public static final String TEMP_DIRECTORY_NAME = "tmp";
    public static final String SWAGGER_CODEGEN_DIRECTORY = "swaggerCodegen";
    public static final String JSON_FILE_EXTENSION = ".json";
    public static final String ZIP_FILE_EXTENSION = ".zip";

    //Starts CEP based throttling policy implementation related constants
    public static final String CPS_SERVER_URL = "CPSServerUrl";
    public static final String CPS_SERVER_USERNAME = "CPSServerUsername";
    public static final String CPS_SERVER_PASSWORD = "CPSServerPassword";
    public static final String POLICY_FILE_FOLDER = "repository" + File.separator + "deployment" + File.separator +
            "server" + File.separator + "throttle-config";
    public static final String SEQUENCE_FILE_FOLDER = "repository" + File.separator + "deployment" + File.separator +
            "server" + File.separator + "synapse-configs" + File.separator + "default" + File.separator + "sequences";
    public static final String POLICY_FILE_LOCATION = POLICY_FILE_FOLDER + File.separator;
    public static final String SEQUENCE_FILE_LOCATION = SEQUENCE_FILE_FOLDER + File.separator;

    public static final String ELIGIBILITY_QUERY_ELEM = "eligibilityQuery";
    public static final String POLICY_NAME_ELEM = "name";
    public static final String DECISION_QUERY_ELEM = "decisionQuery";
    public static final String XML_EXTENSION = ".xml";

    public static final String POLICY_TEMPLATE_KEY = "keyTemplateValue";
    public static final String TEMPLATE_KEY_STATE = "keyTemplateState";

    public static final String THROTTLE_POLICY_DEFAULT = "_default";

    //Advanced throttling related constants
    public static final String TIME_UNIT_SECOND = "sec";
    public static final String TIME_UNIT_MINUTE = "min";
    public static final String TIME_UNIT_HOUR = "hour";
    public static final String TIME_UNIT_DAY = "day";

    public static final String DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN = "50PerMin";
    public static final String DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN = "20PerMin";
    public static final String DEFAULT_APP_POLICY_TEN_REQ_PER_MIN = "10PerMin";
    public static final String DEFAULT_APP_POLICY_UNLIMITED = "Unlimited";

    public static final String DEFAULT_APP_POLICY_LARGE_DESC = "Allows 50 request per minute";
    public static final String DEFAULT_APP_POLICY_MEDIUM_DESC = "Allows 20 request per minute";
    public static final String DEFAULT_APP_POLICY_SMALL_DESC = "Allows 10 request per minute";
    public static final String DEFAULT_APP_POLICY_UNLIMITED_DESC = "Allows unlimited requests";

    public static final String DEFAULT_SUB_POLICY_GOLD = "Gold";
    public static final String DEFAULT_SUB_POLICY_SILVER = "Silver";
    public static final String DEFAULT_SUB_POLICY_BRONZE = "Bronze";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED = "Unlimited";
    public static final String DEFAULT_SUB_POLICY_UNAUTHENTICATED = "Unauthenticated";

    public static final String DEFAULT_SUB_POLICY_GOLD_DESC = "Allows 5000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_SILVER_DESC = "Allows 2000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_BRONZE_DESC = "Allows 1000 requests per minute";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED_DESC = "Allows unlimited requests";
    public static final String DEFAULT_SUB_POLICY_UNAUTHENTICATED_DESC = "Allows 500 request(s) per minute";

    public static final String DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN = "50KPerMin";
    public static final String DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN = "20KPerMin";
    public static final String DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN = "10KPerMin";
    public static final String DEFAULT_API_POLICY_UNLIMITED = "Unlimited";

    public static final String DEFAULT_API_POLICY_ULTIMATE_DESC = "Allows 50000 requests per minute";
    public static final String DEFAULT_API_POLICY_PLUS_DESC = "Allows 20000 requests per minute";
    public static final String DEFAULT_API_POLICY_BASIC_DESC = "Allows 10000 requests per minute";
    public static final String DEFAULT_API_POLICY_UNLIMITED_DESC = "Allows unlimited requests";

    public static final String API_POLICY_USER_LEVEL = "userLevel";
    public static final String API_POLICY_API_LEVEL = "apiLevel";

    public static final String BILLING_PLAN_FREE = "FREE";

    public static final String BLOCKING_EVENT_TYPE = "wso2event";
    public static final String BLOCKING_EVENT_FORMAT = "wso2event";
    public static final String THROTTLE_KEY = "throttleKey";
    public static final String BLOCKING_CONDITION_STATE = "state";
    public static final String BLOCKING_CONDITION_KEY = "blockingCondition";
    public static final String BLOCKING_CONDITION_VALUE = "conditionValue";
    public static final String BLOCKING_CONDITION_DOMAIN = "tenantDomain";
    public static final String BLOCKING_CONDITIONS_APPLICATION = "APPLICATION";
    public static final String BLOCKING_CONDITIONS_API = "API";
    public static final String BLOCKING_CONDITIONS_USER = "USER";
    public static final String BLOCKING_CONDITIONS_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String BLOCKING_CONDITIONS_IP = "IP";
    public static final String BLOCK_CONDITION_IP_RANGE = "IPRANGE";
    public static final String BLOCK_CONDITION_FIXED_IP = "fixedIp";
    public static final String BLOCK_CONDITION_START_IP = "startingIp";
    public static final String BLOCK_CONDITION_ENDING_IP = "endingIp";
    public static final String BLOCK_CONDITION_INVERT = "invert";
    public static final String BLOCK_CONDITION_IP_TYPE = "type";
    public static final String REVOKED_TOKEN_KEY = "revokedToken";
    public static final String REVOKED_TOKEN_EXPIRY_TIME = "expiryTime";
    public static final String EVENT_TYPE = "eventType";
    public static final String EVENT_TIMESTAMP = "timestamp";
    public static final String EVENT_PAYLOAD = "event";

    public static final String SEARCH_AND_TAG = "&";
    public static final String LCSTATE_SEARCH_TYPE_KEY = "lcState=";
    public static final String ENABLE_STORE_SEARCH_TYPE_KEY = "enableStore=(true OR null)";
    public static final String LCSTATE_SEARCH_KEY = "lcState";
    public static final String SUBCONTEXT_SEARCH_TYPE_PREFIX = "subcontext";
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

    //api-product related constants
    public static final String API_PRODUCT_VERSION = "1.0.0";
    public static final String API_IDENTIFIER_TYPE = "API";
    public static final String API_PRODUCT_IDENTIFIER_TYPE = "API Product";
    public static final String[] API_SUPPORTED_TYPE_LIST = {"HTTP", "WS", "SOAPTOREST", "GRAPHQL", "SOAP"};

    public static class AdvancedThrottleConstants {

        public static final String THROTTLING_CONFIGURATIONS = "ThrottlingConfigurations";
        public static final String TRAFFIC_MANAGER = "TrafficManager";

        public static final String DATA_PUBLISHER_CONFIGURATION = "DataPublisher";
        public static final String DATA_PUBLISHER_CONFIGURAION_TYPE = "Type";
        public static final String DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP = "ReceiverUrlGroup";
        public static final String DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP = "AuthUrlGroup";
        public static final String USERNAME = "Username";
        public static final String PASSWORD = "Password";

        public static final String DATA_PUBLISHER_POOL_CONFIGURATION = "DataPublisherPool";
        public static final String DATA_PUBLISHER_POOL_CONFIGURAION_MAX_IDLE = "MaxIdle";
        public static final String DATA_PUBLISHER_POOL_CONFIGURAION_INIT_IDLE = "InitIdleCapacity";

        public static final String GLOBAL_POLICY_ENGINE_WS_CONFIGURATION = "GlobalPolicyEngineWSConnectionDetails";
        public static final String SERVICE_URL = "ServiceURL";
        public static final String JMS_CONNECTION_DETAILS = "JMSConnectionDetails";
        public static final String JMS_CONNECTION_PARAMETERS = "JMSConnectionParameters";
        public static final String JMS_PUBLISHER_PARAMETERS = "JMSEventPublisherParameters";
        public static final String DEFAULT_THROTTLE_LIMITS = "DefaultLimits";
        public static final String SUBSCRIPTION_THROTTLE_LIMITS = "SubscriptionTierLimits";
        public static final String APPLICATION_THROTTLE_LIMITS = "ApplicationTierLimits";
        public static final String RESOURCE_THROTTLE_LIMITS = "ResourceLevelTierLimits";
        public static final String JMS_TASK_MANAGER = "JMSTaskManager";
        public static final String MIN_THREAD_POOL_SIZE = "MinThreadPoolSize";
        public static final String MAX_THREAD_POOL_SIZE = "MaxThreadPoolSize";
        public static final String KEEP_ALIVE_TIME_IN_MILLIS = "KeepAliveTimeInMillis";
        public static final String JOB_QUEUE_SIZE = "JobQueueSize";
        public static final String ENABLE_UNLIMITED_TIER = "EnableUnlimitedTier";
        public static final String POLICY_DEPLOYER_CONFIGURATION = "PolicyDeployer";
        public static final String BLOCK_CONDITION_RETRIEVER_CONFIGURATION = "BlockCondition";
        public static final String DATA_PUBLISHER_THREAD_POOL_CONFIGURATION = "DataPublisherThreadPool";
        public static final String DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_CORE_POOL_SIZE = "CorePoolSize";
        public static final String DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_MAXMIMUM_POOL_SIZE = "MaxmimumPoolSize";
        public static final String DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_KEEP_ALIVE_TIME = "KeepAliveTime";
        public static final String BLOCK_CONDITION_RETRIEVER_INIT_DELAY = "InitDelay";
        public static final String BLOCK_CONDITION_RETRIEVER_PERIOD = "Period";
        public static final String ENABLE_SUBSCRIPTION_SPIKE_ARREST = "EnableSubscriptionSpikeArrest";
        public static final String ENABLE_HEADER_CONDITIONS = "EnableHeaderConditions";
        public static final String ENABLE_JWT_CLAIM_CONDITIONS = "EnableJWTClaimConditions";
        public static final String ENABLE_QUERY_PARAM_CONDITIONS = "EnableQueryParamConditions";
        public static final String ENABLED = "Enabled";
        public static final String IS_THROTTLED = "isThrottled";
        public static final String THROTTLE_KEY = "throttleKey";
        public static final String EXPIRY_TIMESTAMP = "expiryTimeStamp";
        public static final String EVALUATED_CONDITIONS = "evaluatedConditions";
        public static final String TRUE = "true";
        public static final String ADD = "add";
    }

    /**
     * Parameter for adding custom attributes against application in API Store
     */
    public static class ApplicationAttributes {

        public static final String APPLICATION_CONFIGURATIONS = "ApplicationConfigs";
        public static final String APPLICATION_ATTRIBUTES = "ApplicationAttributes";
        public static final String ATTRIBUTES = "Attributes";
        public static final String ATTRIBUTE = "Attribute";
        public static final String REQUIRED = "Required";
        public static final String HIDDEN = "Hidden";
        public static final String DESCRIPTION = "Description";
        public static final String DEFAULT = "Default";
        public static final String NAME = "Name";
    }

    public static final String REGISTRY_ARTIFACT_SEARCH_DESC_ORDER = "DES";
    public static final String REGISTRY_ARTIFACT_SORT_BY_CREATED_TIME = "meta_created_date";

    public static final String MULTI_ATTRIBUTE_SEPARATOR_DEFAULT = ",";
    public static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    public static final String CUSTOM_URL = "customUrl";
    public static final String API_PRODUCT = "APIProduct";

    public static class AuditLogConstants {

        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String DELETED = "deleted";

        public static final String API = "API";
        public static final String API_PRODUCT = "APIProduct";
        public static final String APPLICATION = "Application";
        public static final String SUBSCRIPTION = "Subscription";

        public static final String NAME = "name";
        public static final String VERSION = "version";
        public static final String CONTEXT = "context";
        public static final String PROVIDER = "provider";
        public static final String OWNER = "owner";
        public static final String TIER = "tier";
        public static final String REQUESTED_TIER = "requested_tier";
        public static final String CALLBACK = "callbackURL";
        public static final String GROUPS = "groups";
        public static final String STATUS = "status";
        public static final String API_NAME = "api_name";
        public static final String API_PRODUCT_NAME = "api_product_name";
        public static final String APPLICATION_NAME = "application_name";
        public static final String APPLICATION_ID = "application_id";
    }

    public static final String API_WORKFLOW_STATE_ATTR = "overview_workflowState";

    public static class WorkflowConfigConstants {

        public static final String WORKFLOW = "WorkflowConfigurations";
        public static final String WORKFLOW_ENABLED = "Enabled";
        public static final String WORKFLOW_SERVER_URL = "ServerUrl";
        public static final String WORKFLOW_SERVER_USER = "ServerUser";
        public static final String WORKFLOW_SERVER_PASSWORD = "ServerPassword";
        public static final String WORKFLOW_CALLBACK = "WorkflowCallbackAPI";
        public static final String WORKFLOW_TOKEN_EP = "TokenEndPoint";
        public static final String WORKFLOW_DCR_EP = "DCREndPoint";
        public static final String WORKFLOW_DCR_EP_USER = "DCREndPointUser";
        public static final String WORKFLOW_DCR_EP_PASSWORD = "DCREndPointPassword";
        public static final String LIST_PENDING_TASKS = "ListPendingTasks";

    }

    public static class AccessTokenBinding {

        public static final String ACCESS_TOKEN_BINDING = "AccessTokenBinding";
        public static final String ACCESS_TOKEN_BINDING_ENABLED = ACCESS_TOKEN_BINDING + ".Enabled";
    }

    public static class JwtTokenConstants {

        public static final String APPLICATION = "application";
        public static final String APPLICATION_ID = "id";
        public static final String APPLICATION_UUID = "uuid";
        public static final String APPLICATION_NAME = "name";
        public static final String APPLICATION_TIER = "tier";
        public static final String APPLICATION_OWNER = "owner";
        public static final String KEY_TYPE = "keytype";
        public static final String CONSUMER_KEY = "consumerKey";
        public static final String AUTHORIZED_PARTY = "azp";
        public static final String KEY_ID = "kid";
        public static final String JWT_ID = "jti";
        public static final String SUBSCRIPTION_TIER = "subscriptionTier";
        public static final String SUBSCRIBER_TENANT_DOMAIN = "subscriberTenantDomain";
        public static final String TIER_INFO = "tierInfo";
        public static final String STOP_ON_QUOTA_REACH = "stopOnQuotaReach";
        public static final String SPIKE_ARREST_LIMIT = "spikeArrestLimit";
        public static final String SPIKE_ARREST_UNIT = "spikeArrestUnit";
        public static final String SCOPE = "scope";
        public static final String SCOPE_DELIMITER = " ";
        public static final String ISSUED_TIME = "iat";
        public static final String EXPIRY_TIME = "exp";
        public static final String JWT_KID = "kid";
        public static final String SIGNATURE_ALGORITHM = "alg";
        public static final String TOKEN_TYPE = "typ";
        public static final String BACKEND_TOKEN = "backendJwt";
        public static final String SUBSCRIBED_APIS = "subscribedAPIs";
        public static final String API_CONTEXT = "context";
        public static final String API_VERSION = "version";
        public static final String API_PUBLISHER = "publisher";
        public static final String API_NAME = "name";
        public static final String QUOTA_TYPE = "tierQuotaType";
        public static final String QUOTA_TYPE_BANDWIDTH = "bandwidthVolume";
        public static final String PERMITTED_IP = "permittedIP";
        public static final String PERMITTED_REFERER = "permittedReferer";
        public static final String GRAPHQL_MAX_DEPTH = "graphQLMaxDepth";
        public static final String GRAPHQL_MAX_COMPLEXITY = "graphQLMaxComplexity";
        public static final String AUTHORIZED_USER_TYPE = "aut";
    }

    public static final String SIGNATURE_ALGORITHM_RS256 = "RS256";
    public static final String SIGNATURE_ALGORITHM_SHA256_WITH_RSA = "SHA256withRSA";

    public static class APIEndpointSecurityConstants {

        public static final String BASIC_AUTH = "BasicAuth";
        public static final String DIGEST_AUTH = "DigestAuth";
        public static final String OAUTH = "OAuth";
    }

    public enum APITransportType {
        HTTP, WS, GRAPHQL
    }

    public static final String API_TYPE_SOAP = "SOAP";
    public static final String API_TYPE_SOAPTOREST = "SOAPTOREST";

    public static final String[] HTTP_DEFAULT_METHODS = {"get", "put", "post", "delete", "patch"};
    public static final String[] SOAP_DEFAULT_METHODS = {"post"};

    public static final String JSON_GRANT_TYPES = "grant_types";
    public static final String JSON_USERNAME = "username";
    public static final String REGEX_ILLEGAL_CHARACTERS_FOR_API_METADATA = "[~!@#;%^*()+={}|<>\"\',\\[\\]&/$\\\\]";
    public static final String JSON_CLIENT_ID = "client_id";
    public static final String JSON_ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String JSON_CLIENT_SECRET = "client_secret";
    public static final String JSON_CALLBACK_URL = "callbackUrl";

    /**
     * Publisher Access Control related registry properties and values.
     */
    public static final String PUBLISHER_ROLES = "publisher_roles";
    public static final String DISPLAY_PUBLISHER_ROLES = "display_publisher_roles";
    public static final String ACCESS_CONTROL = "publisher_access_control";
    public static final String NO_ACCESS_CONTROL = "all";
    public static final String NULL_USER_ROLE_LIST = "null";

    /**
     * CustomIndexer property to indicate whether it is gone through API Custom Indexer.
     */
    public static final String CUSTOM_API_INDEXER_PROPERTY = "registry.customIndexer";

    /**
     * Parameter related with accessControl support.
     */
    public static final String ACCESS_CONTROL_PARAMETER = "accessControl";
    public static final String ACCESS_CONTROL_ROLES_PARAMETER = "accessControlRoles";

    // Error message that will be shown when the user tries to access the API, that is not authorized for him.
    public static final String UN_AUTHORIZED_ERROR_MESSAGE = "User is not authorized to";

    // Prefix used for saving the custom properties related with APIs
    public static final String API_RELATED_CUSTOM_PROPERTIES_PREFIX = "api_meta.";
    // Reserved keywords for search.
    public static final String[] API_SEARCH_PREFIXES = {DOCUMENTATION_SEARCH_TYPE_PREFIX, TAGS_SEARCH_TYPE_PREFIX,
            NAME_TYPE_PREFIX, SUBCONTEXT_SEARCH_TYPE_PREFIX, PROVIDER_SEARCH_TYPE_PREFIX, CONTEXT_SEARCH_TYPE_PREFIX,
            VERSION_SEARCH_TYPE_PREFIX, LCSTATE_SEARCH_KEY.toLowerCase(), API_DESCRIPTION.toLowerCase(),
            API_STATUS.toLowerCase(), CONTENT_SEARCH_TYPE_PREFIX, TYPE_SEARCH_TYPE_PREFIX, LABEL_SEARCH_TYPE_PREFIX,
            CATEGORY_SEARCH_TYPE_PREFIX, ENABLE_STORE.toLowerCase()};
    // Prefix for registry attributes.
    public static final String OVERVIEW_PREFIX = "overview_";
    /**
     * Parameter for enabling tenant load notifications to members in the same HZ cluster
     */
    public static final String ENABLE_TENANT_LOAD_NOTIFICATION = "enableTenantLoadNotification";

    public static final String STORE_VIEW_ROLES = "store_view_roles";
    public static final String PUBLIC_STORE_VISIBILITY = "public";
    public static final String RESTRICTED_STORE_VISIBILITY = "restricted";

    public static final String CREATED_DATE = "createdDate";

    public static final String UNLIMITED_TIER_NAME = "unlimited";
    public static final String FAULT_SEQUENCE = "fault";
    public static final String OUT_SEQUENCE = "out";

    public static final String ENABLE_DUPLICATE_SCOPES = "enableDuplicateScopes";

    public static final String USER = "user";
    public static final String IS_SUPER_TENANT = "isSuperTenant";
    public static final String NULL_GROUPID_LIST = "null";

    public static final String APPLICATION_GZIP = "application/gzip";
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_X_ZIP_COMPRESSED = "application/x-zip-compressed";
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String JSON_FILENAME_EXTENSION = ".json";
    public static final String JSON_GZIP_FILENAME_EXTENSION = ".json.gz";
    public static final String MIGRATION_MODE = "migrationMode";

    /**
     * Constants for correlation logging
     */
    public static final String CORRELATION_ID = "Correlation-ID";
    public static final String ENABLE_CORRELATION_LOGS = "enableCorrelationLogs";
    public static final String CORRELATION_LOGGER = "correlation";
    public static final String LOG_ALL_METHODS = "logAllMethods";
    public static final String AM_ACTIVITY_ID = "activityid";

    public static final String PDF_EXTENSION = "pdf";
    public static final String XLS_EXTENSION = "xls";
    public static final String XLSX_EXTENSION = "xlsx";
    public static final String PPT_EXTENSION = "ppt";
    public static final String PPTX_EXTENSION = "pptx";
    public static final String DOC_EXTENSION = "doc";
    public static final String DOCX_EXTENSION = "docx";
    public static final String XML_DOC_EXTENSION = "xml";
    public static final String TXT_EXTENSION = "txt";
    public static final String WSDL_EXTENSION = "wsdl";

    public static final String API_STATE_CHANGE_INDICATOR = "registry.api.state.change.indicator";
    public static final String DOCUMENT_MEDIA_TYPE_KEY = "application/vnd.wso2-document\\+xml";
    public static final String DOCUMENT_INDEXER_INDICATOR = "document_indexed";

    public static final String KEY_SUFFIX = "_KEY";

    public static final String COLUMN_PRODUCT_DEFINITION = "DEFINITION";
    public static final String PRODUCTSCOPE_PREFIX = "productscope";
    public static final String API_PRODUCT_SUBSCRIPTION_TYPE = "APIProduct";
    public static final String API_SUBSCRIPTION_TYPE = "API";
    public static final String TYPE = "type";
    public static final String TYPE_SEARCH_TYPE_KEY = "type=";

    public static class OASResourceAuthTypes {

        public static final String APPLICATION_OR_APPLICATION_USER = "Application & Application User";
        public static final String APPLICATION_USER = "Application User";
        public static final String APPLICATION = "Application";
        public static final String NONE = "None";
    }

    public static class Analytics {

        public static final String API_NAME = "apiName";
        public static final String API_VERSION = "apiVersion";
        public static final String API_CREATOR = "apiCreator";
        public static final String API_CREATOR_TENANT_DOMAIN = "apiCreatorTenantDomain";
        public static final String APPLICATION_ID = "applicationId";
        public static final String RECORDS_DELIMITER = "records";
    }

    public static class Monetization {

        public static final String MONETIZATION_USAGE_RECORD_APP = "APIM_MONETIZATION_SUMMARY";
        public static final String MONETIZATION_USAGE_RECORD_AGG = "MonetizationAgg";
        public static final String USAGE_PUBLISH_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String USAGE_PUBLISH_TIME_ZONE = "UTC";
        public static final String COMPLETED = "COMPLETED";
        public static final String RUNNING = "RUNNING";
        public static final String INPROGRESS = "INPROGRESS";
        public static final String INITIATED = "INITIATED";
        public static final String SUCCESSFULL = "SUCCESSFULL";
        public static final String FAILED = "FAILED";
        public static final String USAGE_PUBLISH_DEFAULT_GRANULARITY = "days";
        public static final String USAGE_PUBLISH_DEFAULT_TIME_GAP_IN_DAYS = "1";
        public static final String USAGE_PUBLISHER_JOB_NAME = "USAGE_PUBLISHER";
        public static final String FROM_TIME_CONFIGURATION_PROPERTY =
                "Monetization.UsagePubliser.PublishTimeDurationInDays";
        public static final String MONETIZATION_CONFIG = "Monetization";
        public static final String MONETIZATION_IMPL = MONETIZATION_CONFIG + ".MonetizationImpl";
        public static final String USAGE_PUBLISHER = MONETIZATION_CONFIG + ".UsagePublisher";
        public static final String USAGE_PUBLISHER_GRANULARITY = USAGE_PUBLISHER + ".Granularity";
        public static final String ADDITIONAL_ATTRIBUTES = "AdditionalAttributes";
        public static final String ATTRIBUTE = "Attribute";
        public static final String IS_ATTRIBITE_REQUIRED = "Required";
        public static final String IS_ATTRIBUTE_HIDDEN = "Hidden";
        public static final String ATTRIBUTE_DESCRIPTION = "Description";
        public static final String ATTRIBUTE_DEFAULT = "Default";
        public static final String ATTRIBUTE_DISPLAY_NAME = "DisplayName";
        public static final String ATTRIBUTE_NAME = "Name";

        public static final String CURRENCY = "currencyType";
        public static final String BILLING_CYCLE = "billingCycle";
        public static final String FIXED_RATE = "fixedRate";
        public static final String DYNAMIC_RATE = "dynamicRate";
        public static final String FIXED_PRICE = "fixedPrice";
        public static final String PRICE_PER_REQUEST = "pricePerRequest";
        public static final String API_MONETIZATION_STATUS = "isMonetizationEnabled";
        public static final String API_MONETIZATION_PROPERTIES = "monetizationProperties";
    }

    // HTTP methods
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_HEAD = "HEAD";
    public static final String HTTP_OPTIONS = "OPTIONS";
    public static final String HTTP_PATCH = "PATCH";

    // Supported API Types
    public enum ApiTypes {
        API,
        PRODUCT_API
    }

    public static final String TENANT_STATE_ACTIVE = "ACTIVE";
    public static final String TENANT_STATE_INACTIVE = "INACTIVE";

    public static final String DEFAULT_API_KEY_SIGN_KEY_STORE = "KeyStore";
    public static final String GATEWAY_PUBLIC_CERTIFICATE_ALIAS = "gateway_certificate_alias";
    public static final String DEFAULT_API_KEY_GENERATOR_IMPL = "org.wso2.carbon.apimgt.impl.token" +
            ".DefaultApiKeyGenerator";

    //Constants for user API ratings
    public static final String API_ID = "apiId";
    public static final String RATING_ID = "ratingId";
    public static final String RATING = "rating";
    public static final String USER_NAME = "username";

    public static class RestApiConstants {

        public static final String REST_API_DEFAULT_VERSION = "v1";
        public static final String REST_API_OLD_VERSION = "v0.17";
        public static final String REST_API_PUBLISHER_CONTEXT = "/api/am/publisher/";
        public static final String REST_API_PUBLISHER_CONTEXT_FULL_1 =
                REST_API_PUBLISHER_CONTEXT + REST_API_DEFAULT_VERSION;
        public static final String REST_API_ADMIN_CONTEXT = "/api/am/admin/";
        public static final String REST_API_ADMIN_VERSION = "v0.17";
        public static final String REST_API_ADMIN_CONTEXT_FULL_0 = REST_API_ADMIN_CONTEXT + REST_API_ADMIN_VERSION;
        public static final String REST_API_ADMIN_IMPORT_API_RESOURCE = "/import/api";
        public static final String IMPORT_API_PRESERVE_PROVIDER = "preserveProvider";
        public static final String IMPORT_API_OVERWRITE = "overwrite";
        public static final String IMPORT_API_ARCHIVE_FILE = "file";
        public static final String IMPORT_API_SUCCESS = "API imported successfully.";
        public static final String REST_API_PUB_RESOURCE_PATH_APIS = "/apis";
        public static final String REST_API_PUB_SEARCH_API_QUERY = "query";
        public static final String PUB_SEARCH_API_QUERY_PARAMS_NAME = "name:";
        public static final String PUB_SEARCH_API_QUERY_PARAMS_VERSION = "version:";
        public static final String PUB_API_LIST_RESPONSE_PARAMS_LIST = "list";
        public static final String PUB_API_LIST_RESPONSE_PARAMS_COUNT = "count";
        public static final String PUB_API_RESPONSE_PARAMS_ID = "id";
        public static final String DYNAMIC_CLIENT_REGISTRATION_URL_SUFFIX =
                "/client-registration/" + REST_API_OLD_VERSION + "/register";
    }

    public static final int MAX_LENGTH_API_NAME = 50;
    public static final int MAX_LENGTH_VERSION = 30;
    public static final int MAX_LENGTH_PROVIDER = 50;
    public static final int MAX_LENGTH_CONTEXT = 82; //context becomes context + version + two '/'. so max context is 50
    public static final int MAX_LENGTH_MEDIATION_POLICY_NAME = 255;

    /**
     * Constants for trust store access
     */
    public static final String TRUST_STORE_PASSWORD = "Security.TrustStore.Password";
    public static final String TRUST_STORE_LOCATION = "Security.TrustStore.Location";
    public static final String INTERNAL_WEB_APP_EP = "/internal/data/v1";
    public static final String API_KEY_REVOKE_PATH = "/key/revoke";

    public static final String SKIP_ROLES_BY_REGEX = "skipRolesByRegex";

    /**
     * API categories related constants
     */
    public static final String API_CATEGORIES_CATEGORY_NAME = "apiCategories_categoryName";
    public static final String API_CATEGORY = "api-category";

    // AWS Lambda: Constants for aws lambda
    public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";
    public static final int AWS_ENCRYPTED_SECRET_KEY_LENGTH = 620;
    public static final int AWS_DEFAULT_CONNECTION_TIMEOUT = 50000;
    public static final String AMZN_ACCESS_KEY = "amznAccessKey";
    public static final String AMZN_SECRET_KEY = "amznSecretKey";
    public static final String AMZN_REGION = "amznRegion";
    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";

    public static final String JWT_AUTHENTICATION_CONFIG = "JWTAuthentication";
    public static final String JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION =
            JWT_AUTHENTICATION_CONFIG + ".EnableSubscriptionValidationViaKeyManager";
    public static final String APPLICATION_TOKEN_TYPE_JWT = "JWT";
    /**
     * Constants for the recommendation system
     */
    public static final String API_RECOMMENDATION = "APIRecommendations";
    public static final String RECOMMENDATION_ENDPOINT = "recommendationAPI";
    public static final String AUTHENTICATION_ENDPOINT = "authenticationEndpoint";
    public static final String RECOMMENDATION_API_CONSUMER_KEY = "consumerKey";
    public static final String RECOMMENDATION_API_CONSUMER_SECRET = "consumerSecret";
    public static final String MAX_RECOMMENDATIONS = "maxRecommendations";
    public static final String WAIT_DURATION = "waitDuration";
    public static final String APPLY_RECOMMENDATIONS_FOR_ALL_APIS = "applyForAllTenants";
    public static final String RECOMMENDATION_USERNAME = "userName";
    public static final String RECOMMENDATION_PASSWORD = "password";
    public static final String ADD_API = "ADD_API";
    public static final String DELETE_API = "DELETE_API";
    public static final String ADD_NEW_APPLICATION = "ADD_APPLICATION";
    public static final String UPDATED_APPLICATION = "UPDATED_APPLICATION";
    public static final String DELETE_APPLICATION = "DELETE_APPLICATION";
    public static final String ADD_USER_CLICKED_API = "ADD_USER_CLICKED_API";
    public static final String ADD_USER_SEARCHED_QUERY = "ADD_USER_SEARCHED_QUERY";
    public static final String PUBLISHED_STATUS = "PUBLISHED";
    public static final String DELETED_STATUS = "DELETED";
    public static final String ACTION_STRING = "action";
    public static final String PAYLOAD_STRING = "payload";
    public static final String API_TENANT_CONF_ENABLE_RECOMMENDATION_KEY = "EnableRecommendation";
    public static final String RECOMMENDATIONS_WSO2_EVENT_PUBLISHER = "recommendationEventPublisher";
    public static final String RECOMMENDATIONS_GET_RESOURCE = "/getRecommendations";
    public static final String RECOMMENDATIONS_PUBLISH_RESOURCE = "/publishEvents";
    public static final String RECOMMENDATIONS_USER_HEADER = "User";
    public static final String RECOMMENDATIONS_ACCOUNT_HEADER = "Account";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_FORM = "application/x-www-form-urlencoded";
    public static final String AUTHORIZATION_BASIC = "Basic ";
    public static final String AUTHORIZATION_BEARER = "Bearer ";
    public static final String TOKEN_GRANT_TYPE_KEY = "grant_type";
    public static final String SCOPES_KEY = "scopes";
    public static final String TOKEN_KEY = "token";
    public static final String GRANT_TYPE_VALUE = "client_credentials";
    public static final String RECOMMENDATIONS_CACHE_NAME = "APIRecommendationsCache";
    public static final String RECOMMENDATIONS_CACHE_KEY = "Recommendations";
    public static final String LAST_UPDATED_CACHE_KEY = "LastUpdated";

    public static class CertificateReLoaderConfiguration {

        public static final String CERTIFICATE_RELOADER_CONFIGURATION_ROOT = "CertificateReLoaderConfiguration";
        public static final String PERIOD = CERTIFICATE_RELOADER_CONFIGURATION_ROOT + ".Period";
    }

    public static class MutualSSL {

        public static final String MUTUAL_SSL_CONFIG_ROOT = "MutualSSL";
        public static final String CLIENT_CERTIFICATE_HEADER = MUTUAL_SSL_CONFIG_ROOT + ".ClientCertificateHeader";
        public static final String CLIENT_CERTIFICATE_ENCODE = MUTUAL_SSL_CONFIG_ROOT + ".ClientCertificateEncode";
        public static final String ENABLE_CLIENT_CERTIFICATE_VALIDATION = MUTUAL_SSL_CONFIG_ROOT +
                ".EnableClientCertificateValidation";
    }

    public static final String DEFAULT_SCOPE_TYPE = "OAUTH2";
    public static final String DEFAULT_BINDING_TYPE = "DEFAULT";

    public static class TokenIssuer {

        public static final String SCOPES_CLAIM = "ScopesClaim";
        public static String TOKEN_ISSUER = "TokenIssuer";
        public static final String JWKS_CONFIGURATION = "JWKSConfiguration";
        public static final String CLAIM_MAPPINGS = "ClaimMappings";
        public static final String CLAIM_MAPPING = "ClaimMapping";
        public static final String CONSUMER_KEY_CLAIM = "ConsumerKeyClaim";


        public static class JWKSConfiguration {

            public static final String URL = "URL";
        }

        public static class ClaimMapping {

            public static final String LOCAL_CLAIM = "LocalClaim";
            public static final String REMOTE_CLAIM = "RemoteClaim";

        }
    }

    public static class KeyManager {

        public static final String SERVICE_URL = "ServiceURL";
        public static final String INIT_DELAY = "InitDelay";
        public static final String INTROSPECTION_ENDPOINT = "introspection_endpoint";
        public static final String CLIENT_REGISTRATION_ENDPOINT = "client_registration_endpoint";
        public static final String KEY_MANAGER_OPERATIONS_DCR_ENDPOINT = "/keymanager-operations/dcr/register";
        public static final String KEY_MANAGER_OPERATIONS_USERINFO_ENDPOINT = "/keymanager-operations/user-info";
        public static final String TOKEN_ENDPOINT = "token_endpoint";
        public static final String REVOKE_ENDPOINT = "revoke_endpoint";
        public static final String WELL_KNOWN_ENDPOINT = "well_known_endpoint";
        public static final String SCOPE_MANAGEMENT_ENDPOINT = "scope_endpoint";
        public static final String AVAILABLE_GRANT_TYPE = "grant_types";
        public static final String ENABLE_TOKEN_GENERATION = "enable_token_generation";
        public static final String ENABLE_TOKEN_HASH = "enable_token_hash";
        public static final String ENABLE_TOKEN_ENCRYPTION = "enable_token_encryption";
        public static final String ENABLE_OAUTH_APP_CREATION = "enable_oauth_app_creation";
        public static final String DEFAULT_KEY_MANAGER = "Resident Key Manager";
        public static final String DEFAULT_KEY_MANAGER_TYPE = "default";
        public static final String DEFAULT_KEY_MANAGER_DESCRIPTION = "This is Resident Key Manager";

        public static final String ISSUER = "issuer";
        public static final String JWKS_ENDPOINT = "jwks_endpoint";
        public static final String USERINFO_ENDPOINT = "userinfo_endpoint";
        public static final String AUTHORIZE_ENDPOINT = "authorize_endpoint";
        public static final String EVENT_HUB_CONFIGURATIONS = "EventHubConfigurations";
        public static final String KEY_MANAGER = "KeyManager";
        public static final String APPLICATION_CONFIGURATIONS = "ApplicationConfigurations";
        public static final String EVENT_RECEIVER_CONFIGURATION = "EventReceiverConfiguration";

        public static final String ENABLE = "Enable";
        public static final String USERNAME = "Username";
        public static final String PASSWORD = "Password";
        public static final String SELF_VALIDATE_JWT = "self_validate_jwt";
        public static final String CLAIM_MAPPING = "claim_mappings";
        public static final String VALIDATION_TYPE = "validation_type";
        public static final String VALIDATION_JWT = "jwt";
        public static final String VALIDATION_REFERENCE = "reference";
        public static final String VALIDATION_CUSTOM = "custom";
        public static final String TOKEN_FORMAT_STRING = "token_format_string";
        public static final String ENABLE_TOKEN_VALIDATION = "validation_enable";
        public static final String VALIDATION_ENTRY_JWT_BODY = "body";
        public static final String API_LEVEL_ALL_KEY_MANAGERS = "all";
        public static final String REGISTERED_TENANT_DOMAIN = "tenantDomain";
        public static final String ENABLE_MAP_OAUTH_CONSUMER_APPS = "enable_map_oauth_consumer_apps";
        public static final String KEY_MANAGER_TYPE = "type";
        public static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F" +
                "]{3}-[0-9a-fA-F]{12}";
        public static final String CONSUMER_KEY_CLAIM = "consumer_key_claim";
        public static final String SCOPES_CLAIM = "scopes_claim";
        public static final String CERTIFICATE_TYPE = "certificate_type";
        public static final String CERTIFICATE_VALUE = "certificate_value";
        public static final String CERTIFICATE_TYPE_JWKS_ENDPOINT = "JWKS";
        public static final String CERTIFICATE_TYPE_PEM_FILE = "PEM";
        public static final String EVENT_PUBLISHER_CONFIGURATIONS = "EventPublisherConfiguration";
        public static final String KEY_MANAGER_TYPE_HEADER = "X-WSO2-KEY-MANAGER";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String AUTH_CODE = "authCode";
        public static final String CLAIM_DIALECT = "dialect";
        public static final String DEFAULT_KEY_MANAGER_OPENID_CONNECT_DISCOVERY_ENDPOINT = "/oauth2/token/.well-known/openid-configuration";
        public static final String DEFAULT_JWKS_ENDPOINT = "/oauth2/jwks";
        public static final String PRODUCTION_TOKEN_ENDPOINT = "production_token_endpoint";
        public static final String SANDBOX_TOKEN_ENDPOINT = "sandbox_token_endpoint";
        public static final String PRODUCTION_REVOKE_ENDPOINT = "production_revoke_endpoint";
        public static final String SANDBOX_REVOKE_ENDPOINT = "sandbox_revoke_endpoint";
        public static final String APPLICATION_ACCESS_TOKEN_EXPIRY_TIME = "application_access_token_expiry_time";
        public static final String USER_ACCESS_TOKEN_EXPIRY_TIME = "user_access_token_expiry_time";
        public static final String REFRESH_TOKEN_EXPIRY_TIME = "refresh_token_expiry_time";
        public static final String ID_TOKEN_EXPIRY_TIME = "id_token_expiry_time";
        public static final String NOT_APPLICABLE_VALUE = "N/A";

        public static class KeyManagerEvent {

            public static final String EVENT_TYPE = "event_type";
            public static final String KEY_MANAGER_CONFIGURATION = "key_manager_configuration";
            public static final String ACTION = "action";
            public static final String NAME = "name";
            public static final String ENABLED = "enabled";
            public static final String VALUE = "value";
            public static final String TENANT_DOMAIN = "tenantDomain";
            public static final String ACTION_ADD = "add";
            public static final String ACTION_UPDATE = "update";
            public static final String ACTION_DELETE = "delete";
            public static final String TYPE = "type";
            public static final String KEY_MANAGER_STREAM_ID = "org.wso2.apimgt.keymgt.stream:1.0.0";
        }
    }

    public static class GlobalCacheInvalidation {

        public static final String ENABLED = "Enabled";
        public static final Object GLOBAL_CACHE_INVALIDATION = "GlobalCacheInvalidation";

        public static final String Domain = "Domain";
        public static final String Stream = "Stream";
        public static final String REVEIVER_URL_GROUP = "ReceiverUrlGroup";
        public static final String AUTH_URL_GROUP = "AuthUrlGroup";
        public static final String USERNAME = "Username";
        public static final String PASSWORD = "Password";
        public static final String TOPIC_NAME = "Topic";
        public static final String EXCLUDED_CACHES = "ExcludedCaches";
        public static final String ReceiverConnectionDetails = "ReceiverConnectionDetails";
    }

    // Supported Notifier Types
    public enum NotifierType {
        API,
        GATEWAY_PUBLISHED_API,
        APPLICATION,
        APPLICATION_REGISTRATION,
        POLICY,
        SUBSCRIPTIONS,
        SCOPE
    }

    // Supported Event Types
    public enum EventType {
        API_CREATE,
        API_UPDATE,
        API_DELETE,
        API_LIFECYCLE_CHANGE,
        APPLICATION_CREATE,
        APPLICATION_UPDATE,
        APPLICATION_DELETE,
        APPLICATION_REGISTRATION_CREATE,
        POLICY_CREATE,
        POLICY_UPDATE,
        POLICY_DELETE,
        SUBSCRIPTIONS_CREATE,
        SUBSCRIPTIONS_UPDATE,
        SUBSCRIPTIONS_DELETE,
        DEPLOY_API_IN_GATEWAY,
        REMOVE_API_FROM_GATEWAY,
        REMOVE_APPLICATION_KEYMAPPING,
        SCOPE_CREATE,
        SCOPE_UPDATE,
        SCOPE_DELETE
    }

    public static class GatewayArtifactSynchronizer {

        public static final String SYNC_RUNTIME_ARTIFACTS_PUBLISHER_CONFIG = "SyncRuntimeArtifactsPublisher";
        public static final String SYNC_RUNTIME_ARTIFACTS_GATEWAY_CONFIG = "SyncRuntimeArtifactsGateway";
        public static final String ENABLE_CONFIG = "Enable";
        public static final String SAVER_CONFIG = "ArtifactSaver";
        public static final String RETRIEVER_CONFIG = "ArtifactRetriever";
        public static final String RETRY_DUARTION = "RetryDuration";
        public static final String PUBLISH_DIRECTLY_TO_GW_CONFIG = "PublishDirectlyToGW";
        public static final String GATEWAY_LABELS_CONFIG = "GatewayLabels";
        public static final String EVENT_WAITING_TIME_CONFIG = "EventWaitingTime";
        public static final String LABEL_CONFIG = "Label";
        public static final String DB_SAVER_NAME = "DBSaver";
        public static final String DB_RETRIEVER_NAME = "DBRetriever";
        public static final String GATEWAY_INSTRUCTION_PUBLISH = "Publish";
        public static final String GATEWAY_INSTRUCTION_REMOVE = "Remove";
        public static final String GATEWAY_INSTRUCTION_ANY = "ANY";
        public static final String SYNAPSE_ARTIFACTS = "/synapse-artifacts";
        public static final String SYNAPSE_ATTRIBUTES = "/synapse-attributes";
        public static final String GATEAY_SYNAPSE_ARTIFACTS = "/gateway-synapse-artifacts";
        public static final String DATA_SOURCE_NAME = "DataSourceName";
        public static final String DATA_RETRIEVAL_MODE = "DataRetrievalMode";
        public static final String GATEWAY_STARTUP_SYNC = "sync";
        public static final String GATEWAY_STARTUP_ASYNC = "async";
        public static final String API_ID = "apiId";
        public static final String LABEL = "label";
        public static final String LABELS = "labels";

    }

    public static class ContainerMgtAttributes {

        public static final String DEPLOYMENTS = "Deployments";
        public static final String CONTAINER_MANAGEMENT = "ContainerMgt";
        public static final String CONTAINER_MANAGEMENT_INFO = "ContainerMgtInfo";

    }

    public static class TopicNames {

        //APIM default topic names
        public static final String TOPIC_THROTTLE_DATA = "throttleData";
        public static final String TOPIC_TOKEN_REVOCATION = "tokenRevocation";
        public static final String TOPIC_CACHE_INVALIDATION = "cacheInvalidation";
        public static final String TOPIC_KEY_MANAGER = "keyManager";
        public static final String TOPIC_NOTIFICATION = "notification";
    }

    public enum PolicyType {
        API,
        APPLICATION,
        SUBSCRIPTION
    }

    public static class NotificationEvent {

        public static final String TOKEN_TYPE = "token_type";
        public static final String TOKEN_REVOCATION_EVENT = "token_revocation";
        public static final String CONSUMER_KEY = "consumer_key";
        public static final String EVENT_ID = "eventId";
        public static final String TENANT_ID = "tenantId";
        public static final String TENANT_DOMAIN = "tenant_domain";
    }

    //Constants related to user password
    public static final String ENABLE_CHANGE_PASSWORD = "EnableChangePassword";
    public static final String IS_PASSWORD_POLICY_ENABLED_PROPERTY = "passwordPolicy.enable";
    public static final String PASSWORD_POLICY_MIN_LENGTH_PROPERTY = "passwordPolicy.min.length";
    public static final String PASSWORD_POLICY_MAX_LENGTH_PROPERTY = "passwordPolicy.max.length";
    public static final String PASSWORD_POLICY_PATTERN_PROPERTY = "passwordPolicy.pattern";
    public static final String PASSWORD_JAVA_REGEX_PROPERTY = "PasswordJavaRegEx";

    public static class KeyStoreManagement {
        public static final String KeyStoreName = "KeyStoreName";
        public static final String SERVER_APIKEYSIGN_KEYSTORE_FILE = "Security.KeyStoreName.Location";
        public static final String SERVER_APIKEYSIGN_KEYSTORE_PASSWORD = "Security.KeyStoreName.Password";
        public static final String SERVER_APIKEYSIGN_KEYSTORE_KEY_ALIAS = "Security.KeyStoreName.KeyAlias";
        public static final String SERVER_APIKEYSIGN_KEYSTORE_TYPE = "Security.KeyStoreName.Type";
        public static final String SERVER_APIKEYSIGN_PRIVATE_KEY_PASSWORD = "Security.KeyStoreName.KeyPassword";
        public static final String KEY_STORE_EXTENSION_JKS = ".jks";
    }
}
