/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.util;

/**
 * On Premise Gateway Common constants
 */
public class OnPremiseGatewayConstants {

    public static final String CONFIG_FILE_NAME = "on-premise-gateway.properties";

    public static final String API_GATEWAY_URL_PROPERTY_KEY = "api.gateway.url";
    public static final String API_PUBLISHER_URL_PROPERTY_KEY = "api.publisher.url";
    public static final String DEFAULT_API_PUBLISHER_URL = "https://localhost:9443";
    public static final String API_ADMIN_URL_PROPERTY_KEY = "api.admin.url";
    public static final int DEFAULT_PORT = 9443;
    public static final int DEFAULT_GATEWAY_PORT = 8243;
    public static final String UPDATED_API_INFO_RETRIEVAL_DURATION = "updated.api.info.retrieval.duration";
    public static final int DEFAULT_UPDATED_API_INFO_RETRIEVAL_DURATION = 60 * 15 * 1000;

    public static final String TOKEN_API_SUFFIX = "/token";
    public static final String DYNAMIC_CLIENT_REGISTRATION_URL_SUFFIX = "/client-registration/{version}/register";

    public static final String API_REQUEST_UNIQUE_IDENTIFIER = "unique.identifier";
    public static final String API_REQUEST_UNIQUE_IDENTIFIER_HOLDER = "$token";
    public static final String APT_REQUEST_TOKEN_HEADER = "Request-Token";
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String ACCESS_TOKEN = "access_token";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_BEARER = "Bearer ";
    public static final String AUTHORIZATION_BASIC = "Basic ";

    public static final String TOKEN_SCOPE = "scope";
    public static final String TOKEN_GRANT_TYPE_KEY = "grant_type";
    public static final String TOKEN_GRANT_TYPE = "password";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    public static final String DEFAULT_DCR_CALLBACK_URL = "https://wso2.com";
    public static final String DEFAULT_DCR_CLIENT_NAME = "on-premise-gateway";
    public static final String DEFAULT_DCR_SCOPE = "Production";
    public static final String DEFAULT_DCR_GRANT_TYPE = "password refresh_token";

    public static final String EMPTY_STRING = "";
    public static final String API_VERSION_PARAM = "{version}";
    public static final String CLOUD_API = "cloud";
    public static final String API_VERSION_PROPERTY = "rest.api.version";
    public static final String API_DEFAULT_VERSION = "v0.14";
    public static final String URL_PATH_SEPARATOR = "/";
    public static final String USERNAME_SEPARATOR = "@";
    public static final String UNIQUE_IDENTIFIER_HOLDER = "$token";

    public static final int DEFAULT_RETRY_COUNT = 3;

}
