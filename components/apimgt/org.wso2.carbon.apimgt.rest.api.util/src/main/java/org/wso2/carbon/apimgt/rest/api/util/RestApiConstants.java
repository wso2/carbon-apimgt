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

package org.wso2.carbon.apimgt.rest.api.util;

import java.io.File;

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
    public static final String AUTHENTICATION_REQUIRED = "authentication_required";

    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;
    
    public static final String RESOURCE = "resource";
    public static final String RESOURCE_API = "API";
    public static final String RESOURCE_APPLICATION = "application";
    public static final String RESOURCE_SUBSCRIPTION = "subscription";
    public static final String RESOURCE_DOCUMENTATION = "documentation";
    public static final String RESOURCE_POLICY = "policy";
    public static final String RESOURCE_APP_POLICY = "application policy";
    public static final String RESOURCE_ADVANCED_POLICY = "advanced policy";
    public static final String RESOURCE_SUBSCRIPTION_POLICY = "subcription policy";
    public static final String RESOURCE_CUSTOM_RULE = "custom rule";
    public static final String RESOURCE_BLOCK_CONDITION = "block condition";
    public static final String RESOURCE_TIER = "tier";
    public static final String RESOURCE_TIER_UPDATE_PERMISSION = RESOURCE_TIER + "/update-permission";
    public static final String RESOURCE_TAG = "tag";
    public static final String RESOURCE_WORKFLOW = "workflow";

    public static final String API_ID_DELIMITER = "-";
    public static final String QUERY_PARAM = "{query}";
    public static final String LIMIT_PARAM = "{limit}";
    public static final String OFFSET_PARAM = "{offset}";
    public static final String TYPE_PARAM = "{type}";
    public static final String TIER_LEVEL_PARAM = "{tierLevel}";
    public static final String SUBSCRIBER_PARAM = "{subscriber}";
    public static final String GROUPID_PARAM = "{groupId}";
    public static final String APIID_PARAM = "{apiId}";
    public static final String APPLICATIONID_PARAM = "{applicationId}";
    public static final String DOCUMENTID_PARAM = "{documentId}";
    public static final String API_VERSION_PARAM="{version}";
    public static final String URL_ENCODED_API_ID_DELIMITER = "%2D";
    public static final String CHARSET = "UTF-8";

    //todo better to take from cxf level
    public static final String RESOURCE_PATH_APIS = "/apis";
    public static final String RESOURCE_PATH_APPLICATIONS = "/applications";
    public static final String RESOURCE_PATH_THROTTLING = "/throttling";
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

    public static final String SERVER_URL = "/applications";
    public static final String SERVER_USER_NAME = "/applications";
    public static final String SERVER_PASSWORD = "/applications";
    public static final String RESOURCE_PATH_SUBSCRIPTIONS = "/subscriptions";
    public static final String RESOURCE_PATH_TIERS = "/tiers";
    public static final String RESOURCE_PATH_TIERS_API = RESOURCE_PATH_TIERS + "/api";
    public static final String RESOURCE_PATH_TIERS_APPLICATION = RESOURCE_PATH_TIERS + "/application";
    public static final String RESOURCE_PATH_TIERS_RESOURCE = RESOURCE_PATH_TIERS + "/resource";
    public static final String RESOURCE_PATH_TAGS = "/tags";
    public static final String RESOURCE_PATH_THUMBNAIL = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/thumbnail";
    public static final String RESOURCE_PATH_DOCUMENTS = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/documents";
    public static final String RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID = RESOURCE_PATH_DOCUMENTS + "/" + DOCUMENTID_PARAM;
    public static final String RESOURCE_PATH_DOCUMENT_CONTENT = RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID + "/content";
    public static final String REST_API_STORE_CONTEXT="/api/am/store/";
    public static final String REST_API_STORE_VERSION="v0.11";
    public static final String REST_API_PUBLISHER_VERSION="v0.11";
    public static final String REST_API_PUBLISHER_CONTEXT="/api/am/publisher/";
    public static final String REST_API_ADMIN_CONTEXT="/api/am/admin";
    public static final String REST_API_ADMIN_VERSION="v0.11";
    public static final String REST_API_PROVIDER = "admin";
    public static final String REST_API_WEB_APP_AUTHENTICATOR_IMPL_CLASS_NAME = "org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl";
    public static final String AUTH_HEADER_NAME = "Authorization";

    public static final int PAGINATION_LIMIT_DEFAULT = 25;
    public static final int PAGINATION_OFFSET_DEFAULT = 0;
    public static final String PAGINATION_NEXT_OFFSET = "next_offset";
    public static final String PAGINATION_NEXT_LIMIT = "next_limit";
    public static final String PAGINATION_PREVIOUS_OFFSET = "previous_offset";
    public static final String PAGINATION_PREVIOUS_LIMIT = "previous_limit";

    public static final String APIS_GET_PAGINATION_URL =
            RESOURCE_PATH_APIS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM;

    public static final String APPLICATIONS_GET_PAGINATION_URL =
            RESOURCE_PATH_APPLICATIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&groupId="
                    + GROUPID_PARAM;

    public static final String SUBSCRIPTIONS_GET_PAGINATION_URL_APIID =
            RESOURCE_PATH_SUBSCRIPTIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&apiId="
                    + APIID_PARAM + "&groupId=" + GROUPID_PARAM;

    public static final String SUBSCRIPTIONS_GET_PAGINATION_URL_APPLICATIONID =
            RESOURCE_PATH_SUBSCRIPTIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&applicationId="
                    + APPLICATIONID_PARAM;

    public static final String DOCUMENTS_GET_PAGINATION_URL =
            RESOURCE_PATH_DOCUMENTS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String TIERS_GET_PAGINATION_URL =
            RESOURCE_PATH_TIERS + "/" + TIER_LEVEL_PARAM + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String TAGS_GET_PAGINATION_URL =
            RESOURCE_PATH_TAGS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;


    public static final String THROTTLING_CUSTOM_ATTRIBUTE_NAME = "name";
    public static final String THROTTLING_CUSTOM_ATTRIBUTE_VALUE = "value";

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
}
