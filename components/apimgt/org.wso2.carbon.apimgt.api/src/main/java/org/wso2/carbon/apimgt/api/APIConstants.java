/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

/**
 * This class contains common constants for APIs.
 */
public class APIConstants {
    public static final String GATEWAY_ENV_TYPE_HYBRID = "hybrid";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";

    public static final String HTTPS_PROTOCOL_URL_PREFIX = "https://";
    public static final String HTTP_PROTOCOL_URL_PREFIX = "http://";

    public static final String WS_PROTOCOL_URL_PREFIX = "ws://";
    public static final String WSS_PROTOCOL_URL_PREFIX = "wss://";

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";
    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";
    public static final String DEFAULT_KEY_MANAGER_HOST = "https://localhost:9443";

    public static final String ENDPOINT_SECURITY_TYPE = "type";
    public static final String ENDPOINT_SECURITY_TYPE_BASIC = "BASIC";
    public static final String ENDPOINT_SECURITY_TYPE_DIGEST = "DIGEST";
    public static final String ENDPOINT_SECURITY_TYPE_OAUTH = "oauth";
    public static final String ENDPOINT_SECURITY = "endpoint_security";
    public static final String ENDPOINT_SECURITY_PRODUCTION = "production";
    public static final String ENDPOINT_SECURITY_SANDBOX = "sandbox";
}
