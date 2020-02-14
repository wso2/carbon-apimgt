/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.output.adapter.http.internal.util;

public class ExtendedHTTPEventAdapterConstants {

    public static final String ADAPTER_TYPE_HTTP = "http-extended";
    public static final String ADAPTER_MESSAGE_URL = "http.url";
    public static final String ADAPTER_MESSAGE_URL_HINT = "http.url.hint";
    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_PROXY_HOST = "http.proxy.host";
    public static final String ADAPTER_PROXY_HOST_HINT = "http.proxy.host.hint";
    public static final String ADAPTER_PROXY_PORT = "http.proxy.port";
    public static final String ADAPTER_PROXY_PORT_HINT = "http.proxy.port.hint";
    public static final String ADAPTER_USERNAME = "http.username";
    public static final String ADAPTER_USERNAME_HINT = "http.username.hint";
    public static final String ADAPTER_PASSWORD = "http.password";
    public static final String ADAPTER_PASSWORD_HINT = "http.password.hint";
    public static final String ADAPTER_HEADERS = "http.headers";
    public static final String ADAPTER_HEADERS_HINT = "http.headers.hint";
    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";
    public static final String ADAPTER_HTTP_CLIENT_METHOD = "http.client.method";
    public static final String CONSTANT_HTTP_POST = "HttpPost";
    public static final String CONSTANT_HTTP_PUT = "HttpPut";
    public static final String ADAPTER_OAUTH_URL = "oauth.url";
    public static final String ADAPTER_OAUTH_URL_HINT = "oauth.url.hint";
    public static final String ADAPTER_OAUTH_CONSUMER_KEY = "oauth.consumer.key";
    public static final String ADAPTER_OAUTH_CONSUMER_KEY_HINT = "oauth.consumer.key.hint";
    public static final String ADAPTER_OAUTH_CONSUMER_SECRET = "oauth.consumer.secret";
    public static final String ADAPTER_OAUTH_CONSUMER_SECRET_HINT = "oauth.consumer.secret.hint";
    public static final String JSON_STRING = "json";
    public static final String TEXT_STRING = "text";
    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json";
    public static final String UTF_EIGHT_CONTENT_TYPE = "UTF-8";
    public static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    public static final String AUTHORIZATION_HEADER_DEFAULT = "Authorization";
    public static final String AUTHORIZATION_BASIC = "Basic ";
    public static final String AUTHORIZATION_BEARER = "Bearer ";


    //configurations for the httpConnectionManager
    public static final String DEFAULT_MAX_CONNECTIONS_PER_HOST = "defaultMaxConnectionsPerHost";
    public static final int DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final String MAX_TOTAL_CONNECTIONS = "maxTotalConnections";
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;
}
