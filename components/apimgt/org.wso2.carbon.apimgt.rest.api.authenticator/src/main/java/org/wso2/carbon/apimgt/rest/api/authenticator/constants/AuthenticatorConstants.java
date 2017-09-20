/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.authenticator.constants;

/**
 * This class holds the constants for the authenticator module for uuf apps .
 *
 */
public class AuthenticatorConstants {

    public static final String ACCESS_TOKEN_1 = "WSO2_AM_TOKEN_1";
    public static final String ACCESS_TOKEN_2 = "WSO2_AM_TOKEN_2";
    public static final String REFRESH_TOKEN_1 = "WSO2_AM_REFRESH_TOKEN_1";
    public static final String REFRESH_TOKEN_2 = "WSO2_AM_REFRESH_TOKEN_2";
    public static final String REFERER_HEADER = "Referer";
    public static final String X_ALT_REFERER_HEADER = "X-Alt-Referer";
    public static final String HTTP_ONLY_COOKIE = "HttpOnly";
    public static final String SECURE_COOKIE = "Secure";
    public static final String PASSWORD_GRANT = "password";
    public static final String REFRESH_GRANT = "refresh_token";
    public static final String COOKIE_HEADER = "Cookie";
    public static final String AUTHORIZATION_HTTP_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "bearer";
    public static final String COOKIE_EXPIRE_TIME = "Expires=Thu, 01-Jan-1970 00:00:01 GMT";
    public static final String REST_CONTEXT = "/api/am";
    public static final String AUTH_USER = "LOGGED_IN_USER";
    public static final String CONSUMER_KEY = "CONSUMER_KEY";
    public static final String CONSUMER_SECRET = "CONSUMER_SECRET";
    public static final String SSO_ENABLED = "is_sso_enabled";
    public static final String APPLICATION_KEY_TYPE = "Application";
    public static final String REQUEST_URL = "REQUEST_URL";
    public static final String STORE_APPLICATION = "store";
    public static final String STORE_NEW_APPLICATION = "store_new";
    public static final String PUBLISHER_APPLICATION = "publisher";
    public static final String ADMIN_APPLICATION = "admin";
    public static final String EDITOR_APPLICATION = "editor";
    public static final String AUTHORIZATION_CODE_CALLBACK_URL = "login/callback/";
}
