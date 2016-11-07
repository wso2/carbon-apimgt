/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.common;

/**
 * Represents constants related to REST APIs.
 */
public class RestApiConstants {

    public static final String RESOURCE_APPLICATION = "application";
    public static final String RESOURCE = "resource";
    public static final String RESOURCE_API = "API";
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

    public static final String STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT = "Internal server error";
    public static final String STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT =
            "The server encountered " + "an internal error. Please contact administrator.";
    public static final String STATUS_NOT_FOUND_MESSAGE_DEFAULT = "Not Found";
    public static final String STATUS_FORBIDDEN_MESSAGE_DEFAULT = "Forbidden";
    public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";
    public static final String RESOURCE_PATH_APPLICATIONS = "/applications";
    public static final String STATUS_CONFLICT_MESSAGE_RESOURCE_ALREADY_EXISTS = "Resource Already Exists";
    public static final int PAGINATION_LIMIT_DEFAULT = 25;
    public static final int PAGINATION_OFFSET_DEFAULT = 0;
    public static final String PAGINATION_NEXT_OFFSET = "next_offset";
    public static final String PAGINATION_NEXT_LIMIT = "next_limit";
    public static final String PAGINATION_PREVIOUS_OFFSET = "previous_offset";
    public static final String PAGINATION_PREVIOUS_LIMIT = "previous_limit";
    public static final String LIMIT_PARAM = "{limit}";
    public static final String OFFSET_PARAM = "{offset}";
    public static final String GROUPID_PARAM = "{groupId}";
    public static final String APPLICATIONS_GET_PAGINATION_URL =
            RESOURCE_PATH_APPLICATIONS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&groupId="
                    + GROUPID_PARAM;

    public static final int TIER_APPLICATION_TYPE = 2;

    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String RESOURCE_PATH_APIS = "/apis";

}
