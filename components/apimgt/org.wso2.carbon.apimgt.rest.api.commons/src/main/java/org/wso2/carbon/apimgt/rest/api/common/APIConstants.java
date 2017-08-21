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
 * Represents Constants related to an API.
 */
public class APIConstants {

    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";
    public static final String DEPRECATED = "DEPRECATED";
    public static final String PROTOTYPED = "PROTOTYPED";
    public static final String VERB_INFO_DTO = "VERB_INFO";

    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String REQUEST_URL = "REQUEST_URL";
    public static final String HTTP_OPTIONS = "OPTIONS";

    /**
     * Represents Frontend Constants related to an API.
     */
    public static class FrontEndParameterNames {
        public static final String CONSUMER_KEY = "consumerKey";
        public static final String CONSUMER_SECRET = "consumerSecret";
        public static final String CLIENT_DETAILS = "appDetails";
        public static final String CALLBACK_URL = "callbackUrl";
        public static final String KEY_STATE = "keyState";
    }

    /**
     * Represents Access Token Constants related to an API.
     */
    public static class AccessTokenConstants {
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String VALIDITY_TIME = "validityTime";
        public static final String TOKEN_SCOPES = "tokenScope";

        public static final String TOKEN_1 = "WSO2_AM_TOKEN_1";
        public static final String AM_TOKEN_MSF4J = "WSO2_AM_TOKEN_MSF4J";
    }

    public static final int POLICY_APPLICATION_TYPE = 2;
}
