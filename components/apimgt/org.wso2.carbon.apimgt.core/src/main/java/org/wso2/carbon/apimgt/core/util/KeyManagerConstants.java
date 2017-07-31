/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.util;

/**
 * This class will hold constants related to key manager de-coupling
 */
public class KeyManagerConstants {

    public static final String OAUTH_CLIENT_ID = "client_id"; //  this means consumer key
    public static final String OAUTH_CLIENT_ID_ISSUED_AT = "client_id_issued_at";
    public static final String OAUTH_CLIENT_SECRET = "client_secret";
    public static final String OAUTH_CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
    public static final String OAUTH_REDIRECT_URIS = "redirect_uris";
    public static final String OAUTH_CALLBACK_URIS = "callback_url";
    public static final String OAUTH_CLIENT_NAME = "client_name";
    public static final String OAUTH_CLIENT_TYPE = "client_type";
    public static final String OAUTH_TOKEN = "token";
    public static final String APP_KEY_TYPE = "key_type";
    public static final String APP_CALLBACK_URL = "callback_url";
    public static final String APP_HOME_PAGE = "homepage";
    public static final String OAUTH_CLIENT_CONTACT = "contact";
    public static final String OAUTH_CLIENT_SCOPE = "scope";
    public static final String OAUTH_CLIENT_TOKEN_SCOPE = "tokenScope";
    public static final String OAUTH_CLIENT_GRANT = "grant_type";
    public static final String OAUTH_CLIENT_GRANTS = "grant_types";
    public static final String OAUTH_CLIENT_RESPONSE_TYPES = "response_types";
    public static final String OAUTH_CLIENT_TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
    public static final String OAUTH_CLIENT_REGISTRATION_ACCESS_TOKEN = "registration_access_token";
    public static final String OAUTH_CLIENT_REGISTRATION_CLIENT_URI = "registration_client_uri";
    public static final String OAUTH_LOGO_URI = "logo_uri";
    public static final String OAUTH_JWKS_URI = "jwks_uri";
    public static final String OAUTH_CLIENT_CONTACTS = "contacts";
    public static final String OAUTH_CLIENT_MANUAL = "MANUAL";
    public static final String OAUTH_CLIENT_PRODUCTION = "PRODUCTION";
    public static final String OAUTH_CLIENT_SANDBOX = "SANDBOX";
    public static final String OAUTH_CLIENT_NOACCESSTOKEN = "NO ACCESS TOKEN";
    public static final String OAUTH_CLIENT_JSONPARAMSTRING = "jsonParams";
    public static final String OAUTH_CLIENT_USERNAME = "username";
    public static final String OAUTH_CLIENT_APPLICATION = "application";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String TOKEN_SCOPES = "scopes";
    public static final String TOKEN_STATE = "tokenState";
    public static final String OAUTH_APP_DETAILS = "appDetails";
    public static final String IMPLICIT_CONST = "implicit";
    public static final String INBOUNT_AUTH_CONSUMER_SECRET = "oauthConsumerSecret";
    public static final String USERNAME = "username";
    public static final String OAUTH2_TOKEN_EXP_TIME = "exp";
    public static final String OAUTH2_TOKEN_ISSUED_TIME = "iat";
    public static final String DCR_ENDPOINT = "dcrEndpoint";
    public static final String TOKEN_ENDPOINT = "TokenEndpoint";
    public static final String REVOKE_ENDPOINT = "RevokeEndpoint";
    public static final String INTROSPECT_ENDPOINT = "introspectEndpoint";
    public static final String AUTHORIZATION_ENDPOINT = "authorizationEndpoint";
    public static final String OAUTH2_DEFAULT_SCOPE = "default";
    public static final String OPEN_ID_CONNECT_SCOPE = "openid";
    public static final String OAUTH_RESPONSE_ACCESSTOKEN = "access_token";
    public static final String OAUTH_RESPONSE_REFRESH_TOKEN = "refresh_token";
    public static final String OAUTH_RESPONSE_EXPIRY_TIME = "expires_in";
    public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    public static final String REFRESH_GRANT_TYPE = "refresh_token";
    public static final String GRANT_TYPE_PARAM_VALIDITY = "validity_period";

    /**
     * Status codes used in key-validation process
     */
    public static class KeyValidationStatus {

        public static final int API_AUTH_GENERAL_ERROR = 900900;
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

        private KeyValidationStatus() {
        }
    }

    /**
     * Key details related constants
     */
    public static class KeyDetails {
        public static final String VALIDITY_TIME = "validityTime";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String CONSUMER_KEY = "consumerKey";
        public static final String CONSUMER_SECRET = "consumerSecret";
        public static final String APP_DETAILS = "appDetails";
        public static final String SUPPORTED_GRANT_TYPES = "supportedGrantTypes";
        public static final String TOKEN_SCOPES = "tokenScopes";
    }

}
