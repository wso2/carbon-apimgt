/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.common;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class RestApiConstants {

    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String DOC_UPLOAD_TMPDIR = "restAPI" + File.separator + "documentUpload";
    public static final String DOC_NAME_DEFAULT = "DEFAULT_DOC_";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FILENAME = "filename";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_XML = "application/xml";
    public static final String AUTHENTICATION_REQUIRED = "authentication_required";
    public static final String HEADER_X_WSO2_TENANT = "x-wso2-tenant";
    public static final String ORGANIZATION = "organization";
    public static final String SUB_ORGANIZATION = "sub_organization";
    public static final String MASKED_TOKEN = "maskedToken";

    public static final String REQUEST_AUTHENTICATION_SCHEME = "request_authentication_scheme";
    public static final String OPAQUE_AUTHENTICATION = "oauth2";
    public static final String BASIC_AUTHENTICATION = "basic_auth";
    public static final String JWT_AUTHENTICATION = "jwt";
    public static final String JWT_TOKEN = "JWT_TOKEN";
    public static final String DOT = ".";
    public static final long TIMESTAMP_SKEW_INSECONDS = 0;

    public static final String USER_REST_API_SCOPES = "user_rest_api_scopes";

    public static final String API_IMPORT_EXPORT_SCOPE = "apim:api_import_export";
    public static final String API_MANAGE_SCOPE = "apim:api_manage";
    public static final String CREATOR_SCOPE = "apim:api_create";
    public static final String ADMIN_SCOPE = "apim:admin";
    public static final String PUBLISHER_SCOPE = "apim:api_publish";


    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;

    public static final String RESOURCE = "resource";
    public static final String RESOURCE_API = "API";
    public static final String RESOURCE_API_PRODUCT = "API Product";
    public static final String RESOURCE_PRODUCT_DOCUMENTATION = "product documentation";
    public static final String RESOURCE_RATING = "Rating";
    public static final String RESOURCE_APPLICATION = "application";
    public static final String RESOURCE_SUBSCRIPTION = "subscription";
    public static final String RESOURCE_DOCUMENTATION = "documentation";
    public static final String RESOURCE_POLICY = "policy";
    public static final String RESOURCE_APP_POLICY = "application policy";
    public static final String RESOURCE_APP_CONSUMER_KEY = "application consumer key";
    public static final String RESOURCE_ADVANCED_POLICY = "advanced policy";
    public static final String RESOURCE_KEY_MANAGER = "key manager";
    public static final String RESOURCE_SUBSCRIPTION_POLICY = "subscription policy";
    public static final String RESOURCE_CUSTOM_RULE = "custom rule";
    public static final String RESOURCE_BLOCK_CONDITION = "block condition";
    public static final String RESOURCE_TIER = "tier";
    public static final String RESOURCE_THROTTLING_POLICY = "throttling policy";
    public static final String RESOURCE_TIER_UPDATE_PERMISSION = RESOURCE_TIER + "/update-permission";
    public static final String RESOURCE_TAG = "tag";
    public static final String RESOURCE_WORKFLOW = "workflow";
    public static final String RESOURCE_COMMENTS = "comments";
    public static final String RESOURCE_MEDIATION_POLICY = "mediation-policy";
    public static final String RESOURCE_API_CATEGORY = "API Category";

    public static final String API_ID_DELIMITER = "-";
    public static final String QUERY_PARAM = "{query}";
    public static final String ALIAS_PARAM = "{alias}";
    public static final String LIMIT_PARAM = "{limit}";
    public static final String OFFSET_PARAM = "{offset}";
    public static final String SORTBY_PARAM = "{sortBy}";
    public static final String SORTORDER_PARAM = "{sortOrder}";
    public static final String TYPE_PARAM = "{type}";
    public static final String TIER_LEVEL_PARAM = "{tierLevel}";
    public static final String SUBSCRIBER_PARAM = "{subscriber}";
    public static final String GROUPID_PARAM = "{groupId}";
    public static final String APIID_PARAM = "{apiId}";
    public static final String APIPRODUCTID_PARAM = "{apiProductId}";
    public static final String APPLICATIONID_PARAM = "{applicationId}";
    public static final String DOCUMENTID_PARAM = "{documentId}";
    public static final String APICATEGORYID_PARAM = "{apiCategoryId}";
    public static final String API_VERSION_PARAM="{version}";
    public static final String SHARED_SCOPE_ID_PARAM = "{scopeId}";
    public static final String URL_ENCODED_API_ID_DELIMITER = "%2D";
    public static final String CHARSET = "UTF-8";

    //todo better to take from cxf level
    public static final String RESOURCE_PATH_APIS = "/apis";
    public static final String RESOURCE_PATH_ENDPOINT_CERTIFICATE_USAGE = "/endpoint-certificates/"
            + ALIAS_PARAM + "/usage";
    public static final String RESOURCE_PATH_API_PRODUCTS = "/api-products";
    public static final String RESOURCE_PATH_APPLICATIONS = "/applications";
    public static final String RESOURCE_PATH_THROTTLING = "/throttling";
    public static final String RESOURCE_PATH_ENVIRONMENT = "/environments";
    public static final String RESOURCE_PATH_SHARED_SCOPES =  "/scopes";
    public static final String RESOURCE_PATH_REVISIONS = "/revisions";
    public static final String RESOURCE_PATH_SHARED_SCOPES_SCOPE_ID =
            RESOURCE_PATH_SHARED_SCOPES + "/" + SHARED_SCOPE_ID_PARAM;
    public static final String RESOURCE_PATH_THROTTLING_POLICIES = RESOURCE_PATH_THROTTLING + "/policies";
    public static final String RESOURCE_PATH_THROTTLING_BLOCK_CONDITIONS = RESOURCE_PATH_THROTTLING
            + "/blacklist";
    public static final String RESOURCE_PATH_THROTTLING_POLICIES_ADVANCED = RESOURCE_PATH_THROTTLING_POLICIES
            + "/advanced";
    public static final String RESOURCE_PATH_THROTTLING_POLICIES_APPLICATION = RESOURCE_PATH_THROTTLING_POLICIES
            + "/application";
    public static final String RESOURCE_PATH_THROTTLING_POLICIES_SUBSCRIPTION = RESOURCE_PATH_THROTTLING_POLICIES
            + "/subscription";
    public static final String RESOURCE_PATH_THROTTLING_POLICIES_GLOBAL = RESOURCE_PATH_THROTTLING_POLICIES
            + "/custom";
    public static final String RESOURCE_PATH_CATEGORY = "/categories";
    public static final String KEY_MANAGERS = "/key-managers";

    // Used in XACML authentication interceptor: Deprecated
    public static final String SERVER_URL = "server_url";
    public static final String SERVER_USER_NAME = "username";
    public static final String SERVER_PASSWORD = "password";

    public static final String RESOURCE_PATH_SUBSCRIPTIONS = "/subscriptions";
    public static final String RESOURCE_PATH_TIERS = "/throttling-policies";
    public static final String RESOURCE_PATH_TIERS_API = RESOURCE_PATH_TIERS + "/api";
    public static final String RESOURCE_PATH_TIERS_APPLICATION = RESOURCE_PATH_TIERS + "/application";
    public static final String RESOURCE_PATH_TIERS_RESOURCE = RESOURCE_PATH_TIERS + "/resource";
    public static final String RESOURCE_PATH_TAGS = "/tags";
    public static final String RESOURCE_PATH_RATINGS = "/ratings";
    public static final String RESOURCE_PATH_THUMBNAIL = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/thumbnail";
    public static final String RESOURCE_PATH_API_MEDIATION = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/mediation-policies";
    public static final String RESOURCE_PATH_DOCUMENTS = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/documents";
    public static final String RESOURCE_PATH_PRODUCT_DOCUMENTS = RESOURCE_PATH_API_PRODUCTS + "/" + APIPRODUCTID_PARAM + "/documents";
    public static final String RESOURCE_PATH_THUMBNAIL_API_PRODUCT = RESOURCE_PATH_API_PRODUCTS + "/"
            + APIPRODUCTID_PARAM + "/thumbnail";
    public static final String RESOURCE_PATH_DOCUMENTS_API_PRODUCT = RESOURCE_PATH_API_PRODUCTS + "/"
            + APIPRODUCTID_PARAM + "/documents";
    public static final String RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID = RESOURCE_PATH_DOCUMENTS + "/" + DOCUMENTID_PARAM;
    public static final String RESOURCE_PATH_PRODUCT_DOCUMENTS_DOCUMENT_ID = RESOURCE_PATH_PRODUCT_DOCUMENTS + "/" + DOCUMENTID_PARAM;
    public static final String RESOURCE_PATH_DOCUMENT_CONTENT = RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID + "/content";
    public static final String RESOURCE_PATH_PRODUCT_DOCUMENT_CONTENT = RESOURCE_PATH_PRODUCT_DOCUMENTS_DOCUMENT_ID + "/content";
    public static final String RESOURCE_PATH_RESOURCE_PATHS = "/resource-paths";
    public static final String RESOURCE_PATH_COMMENTS = "/comments";
    public static final String RESOURCE_PATH_SWAGGER= "/swagger.yaml";
    public static final String REST_API_STORE_VERSION_0 ="v0.16";
    public static final String RESOURCE_PATH_API_CATEGORIES = "/api-categories";
    public static final String RESOURCE_PATH_CATEGORY_THUMBNAIL = RESOURCE_PATH_API_CATEGORIES + "/" + APICATEGORYID_PARAM + "/thumbnail";
    public static final String REST_API_DEVELOPER_PORTAL_VERSION ="v3";
    public static final String REST_API_STORE_CONTEXT="/api/am/store/";
    public static final String REST_API_DEVELOPER_PORTAL_CONTEXT = "api/am/devportal";
    public static final String REST_API_DEVELOPER_PORTAL_RESOURCE_PATH_SETTINGS = "/settings";
    public static final String REST_API_DEVELOPER_PORTAL_RESOURCE_PATH_TENANTS = "/tenants";
    public static final String REST_API_STORE_CONTEXT_FULL_0 = REST_API_STORE_CONTEXT + REST_API_STORE_VERSION_0;
    public static final String REST_API_DEVELOPER_PORTAL_CONTEXT_FULL = REST_API_DEVELOPER_PORTAL_CONTEXT + "/" +
            REST_API_DEVELOPER_PORTAL_VERSION;
    public static final String REST_API_PUBLISHER_VERSION_0 ="v0.16";
    public static final String REST_API_PUBLISHER_VERSION = "v4";
    public static final String REST_API_PUBLISHER_CONTEXT = "/api/am/publisher/";
    public static final String REST_API_PUBLISHER_CONTEXT_FULL_0 =
            REST_API_PUBLISHER_CONTEXT + REST_API_PUBLISHER_VERSION_0;
    public static final String REST_API_PUBLISHER_CONTEXT_FULL =
            REST_API_PUBLISHER_CONTEXT + REST_API_PUBLISHER_VERSION;
    public static final String REST_API_ADMIN_CONTEXT = "/api/am/admin/";
    public static final String REST_API_ADMIN_VERSION_0 = "v0.16";
    public static final String REST_API_ADMIN_VERSION = "v4";
    public static final String REST_API_DCR_CONTEXT_FULL = "/client-registration/v0.17";
    public static final String REST_API_DCR_CONTEXT = "/client-registration";
    public static final String REST_API_DCR_VERSION = "v0.17";
    public static final String REST_API_ADMIN_CONTEXT_FULL_0 = REST_API_ADMIN_CONTEXT + REST_API_ADMIN_VERSION_0;
    public static final String REST_API_ADMIN_CONTEXT_FULL = REST_API_ADMIN_CONTEXT + REST_API_ADMIN_VERSION;
    public static final String REST_API_SERVICE_CATALOG_CONTEXT_FULL = "/api/am/service-catalog";
    public static final String REST_API_PROVIDER = "admin";
    public static final String REST_API_WEB_APP_AUTHENTICATOR_IMPL_CLASS_NAME = "org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl";
    public static final String AUTH_HEADER_NAME = "Authorization";
    public static final String BACKEND_JWT_HEADER_NAME = "X-JWT-Assertion";
    public static final Pattern REGEX_BEARER_PATTERN = Pattern.compile("Bearer\\s");
    public static final String COOKIE_HEADER_NAME = "cookie";
    public static final String AUTH_COOKIE_NAME = "AM_ACC_TOKEN_DEFAULT_P2"; // This cookie name should be used when setting the cookie for SPA app in SPA app user authentication response to REST API context as path directive

    public static final String API_VERSION = "API_VERSION";
    public static final String REQUEST_URL = "org.apache.cxf.request.uri";
    public static final String REQUEST_METHOD = "org.apache.cxf.request.method";
    public static final String BASE_PATH = "org.apache.cxf.message.Message.BASE_PATH";
    public static final String PROTOCOL_HEADERS = "org.apache.cxf.message.Message.PROTOCOL_HEADERS";
    public static final String URI_TEMPLATES = "TEMPLATES";
    public static final String ORG_ID = "ORG_ID";

    public static final int PAGINATION_LIMIT_DEFAULT = 25;
    public static final int PAGINATION_OFFSET_DEFAULT = 0;
    public static final String PAGINATION_NEXT_OFFSET = "next_offset";
    public static final String PAGINATION_NEXT_LIMIT = "next_limit";
    public static final String PAGINATION_PREVIOUS_OFFSET = "previous_offset";
    public static final String PAGINATION_PREVIOUS_LIMIT = "previous_limit";
    public static final String DEFAULT_SORT_CRITERION = "createdTime";
    public static final String DEFAULT_SORT_ORDER = "asc";
    public static final String DESCENDING_SORT_ORDER = "desc";

    public static final String APIS_GET_PAGINATION_URL =
            RESOURCE_PATH_APIS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM;

    public static final String ENDPOINT_CERTIFICATE_USAGE_GET_PAGINATION_URL =
            RESOURCE_PATH_ENDPOINT_CERTIFICATE_USAGE + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String APPLICATIONS_GET_PAGINATION_URL =
            RESOURCE_PATH_APPLICATIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&groupId="
                    + GROUPID_PARAM;

    public static final String APPLICATIONS_GET_PAGINATION_URL_WITH_SORTBY_SORTORDER  =
            RESOURCE_PATH_APPLICATIONS + "?sortBy=" + SORTBY_PARAM + "&sortOrder=" + SORTORDER_PARAM +
                    "&limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&groupId=" + GROUPID_PARAM;

    public static final String SUBSCRIPTIONS_GET_PAGINATION_URL_APIID =
            RESOURCE_PATH_SUBSCRIPTIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&apiId="
                    + APIID_PARAM + "&groupId=" + GROUPID_PARAM;

    public static final String SUBSCRIPTIONS_GET_PAGINATION_URL_APPLICATIONID =
            RESOURCE_PATH_SUBSCRIPTIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&applicationId="
                    + APPLICATIONID_PARAM;

    public static final String DOCUMENTS_GET_PAGINATION_URL =
            RESOURCE_PATH_DOCUMENTS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String RATINGS_GET_PAGINATION_URL =
            RESOURCE_PATH_RATINGS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String TIERS_GET_PAGINATION_URL =
            RESOURCE_PATH_TIERS + "/" + TIER_LEVEL_PARAM + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String TAGS_GET_PAGINATION_URL =
            RESOURCE_PATH_TAGS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String SCOPES_GET_PAGINATION_URL =
            RESOURCE_PATH_SHARED_SCOPES + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String RESOURCE_PATH_PAGINATION_URL =
            RESOURCE_PATH_RESOURCE_PATHS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String API_PRODUCTS_GET_PAGINATION_URL =
            RESOURCE_PATH_API_PRODUCTS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM;

    public static final String PRODUCT_DOCUMENTS_GET_PAGINATION_URL =
            RESOURCE_PATH_PRODUCT_DOCUMENTS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String THROTTLING_CUSTOM_ATTRIBUTE_NAME = "name";
    public static final String THROTTLING_CUSTOM_ATTRIBUTE_VALUE = "value";

    //Constants for application sortBy
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_OWNER = "owner";
    public static final String SORT_BY_THROTTLING_TIER = "throttlingTier";
    public static final String SORT_BY_STATUS = "status";

    //default error messages
    public static final String STATUS_FORBIDDEN_MESSAGE_DEFAULT = "Forbidden";
    public static final String STATUS_NOT_FOUND_MESSAGE_DEFAULT = "Not Found";
    public static final String STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT = "Internal server error";
    public static final String STATUS_METHOD_NOT_ALLOWED_MESSAGE_DEFAULT = "Method Not Allowed";
    public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";
    public static final String STATUS_CONFLICT_MESSAGE_RESOURCE_ALREADY_EXISTS = "Resource Already Exists";
    public static final String STATUS_CONFLICT_MESSAGE_DEFAULT = "Conflict";

    public static final String STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT = "The server encountered "
            + "an internal error. Please contact administrator.";

    // common attributes
    public static final String PRODUCTION_ENDPOINTS = "production_endpoints";
    public static final String SANDBOX_ENDPOINTS = "sandbox_endpoints";


    public static final String GET_LAST_UPDATED = "GetLastUpdatedTime";
    public static final String GET = "GET";
    public static final String ETAG = "ETag";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    public static final String FREE = "FREE";
    public static final String COMMERCIAL = "COMMERCIAL";
    public static final String PAID = "PAID";
    public static final String FREEMIUM = "FREEMIUM";

    //System property set at server startup
    public static final String MIGRATION_MODE = "migrationMode";

    public static final String CERTS_BASE_PATH = "/certificates";
    public static final String CLIENT_CERTS_BASE_PATH = "/clientCertificates";
    public static final String CERTS_GET_PAGINATED_URL =
            CERTS_BASE_PATH + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + QUERY_PARAM;
    public static final String CLIENT_CERTS_GET_PAGINATED_URL =
            CLIENT_CERTS_BASE_PATH + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + QUERY_PARAM;

    public static final String IN_SEQUENCE = "in";
    public static final String OUT_SEQUENCE = "out";
    public static final String SEQUENCE_CONTENT = "content";
    public static final String SEQUENCE_ARTIFACT_ID = "id";
    public static final String HTTP_METHOD = "method";

    public static final String GET_API_PRODUCT_QUERY  = "type=APIProduct";

    public static final String RETURN_MODEL = "model";
    public static final String RETURN_MODEL_BEFORE_ADDED = "model_before_added";
    public static final String RETURN_DTO = "dto";

    public static final String OAS_VERSION_2 = "v2";
    public static final String OAS_VERSION_3 = "v3";

    public static final String MESSAGE_EXCHANGE_TOKEN_INFO = "message_exchange_token_info";

    public static final String ERROR_TOKEN_INVALID = "Provided access token is invalid";
    public static final String ERROR_TOKEN_EXPIRED = "Access token is expired";
    public static final String ERROR_SCOPE_VALIDATION_FAILED = "You cannot access API as scope validation failed";

    public static final String TENANT_DOMAIN = "LoggedInUserTenantDomain";

    public static final String AUTHENTICATION_ADMIN_SERVICE_ENDPOINT = "AuthenticationAdmin";

    public static final Set<String> ALLOWED_THUMBNAIL_EXTENSIONS = new HashSet<String>(
            Arrays.asList("jpg", "png", "jpeg", "gif", "svg"));

    public static final Set<String> ALLOWED_THUMBNAIL_MEDIA_TYPES = new HashSet<String>(
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/svg+xml"));

    public static final String SVG_MEDIA_TYPE = "image/svg+xml";

    public static final int TAG_LIMIT_DEFAULT = 1000;
    public static final int TAG_OFFSET_DEFAULT = 0;

    public static final String RESOURCE_PATH_OPERATION_POLICIES = "operation-policies";

    public static final String AUTH_TOKEN_INFO = "AUTH_TOKEN_INFO";
}
