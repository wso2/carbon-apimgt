/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util;

public final class RestApiConstants {

    public static final String APPLICATION_JSON = "application/json";

    public static final String API_ID_DELIMITER = "-";

    public static final String QUERY_PARAM = "{query}";
    public static final String LIMIT_PARAM = "{limit}";
    public static final String OFFSET_PARAM = "{offset}";
    public static final String TYPE_PARAM = "{type}";
    public static final String SUBSCRIBER_PARAM = "{subscriber}";
    public static final String GROUPID_PARAM = "{groupId}";
    public static final String APIID_PARAM = "{apiId}";
    public static final String APPLICATIONID_PARAM = "{applicationId}";
    public static final String API_VERSION_PARAM="{version}";

    //todo better to take from cxf level
    public static final String RESOURCE_PATH_APIS = "/apis";
    public static final String RESOURCE_PATH_APPLICATIONS = "/applications";
    public static final String SERVER_URL = "/applications";
    public static final String SERVER_USER_NAME = "/applications";
    public static final String SERVER_PASSWORD = "/applications";
    public static final String RESOURCE_PATH_SUBSCRIPTIONS = "/subscriptions";
    public static final String RESOURCE_PATH_TIERS = "/tiers";
    public static final String RESOURCE_PATH_TAGS = "/tags";
    public static final String RESOURCE_PATH_DOCUMENTS = RESOURCE_PATH_APIS + "/" + APIID_PARAM + "/documents";
    public static final String REST_API_STORE_CONTEXT="store_rest_api";
    public static final String REST_API_STORE_VERSION="v1";
    public static final String REST_API_PUBLISHER_VERSION="v1";
    public static final String REST_API_PUBLISHER_CONTEXT="publisher_rest_api";
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
            RESOURCE_PATH_APIS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM + "&query=" + QUERY_PARAM
                    + "&type=" + TYPE_PARAM;

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
            RESOURCE_PATH_DOCUMENTS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM  + "&query=" + QUERY_PARAM;

    public static final String TIERS_GET_PAGINATION_URL =
            RESOURCE_PATH_TIERS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;

    public static final String TAGS_GET_PAGINATION_URL =
            RESOURCE_PATH_TAGS + "?limit=" + LIMIT_PARAM + "&offset=" + OFFSET_PARAM;
}
