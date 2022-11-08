/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

public class WebSocketApiConstants {

    WebSocketApiConstants() {
    }

    public static final String WEBSOCKET_DUMMY_HTTP_METHOD_NAME = "WS";
    public static final String WS_ENDPOINT_NAME = "WebSocketInboundEndpoint";
    public static final String WS_SECURED_ENDPOINT_NAME = "SecureWebSocketEP";
    public static final String URL_SEPARATOR = "/";
    public static final String DEFAULT_RESOURCE_NAME = "/_default_resource_of_api_";
    public static final String WS_SSL_CHANNEL_HANDLER_NAME = "ssl";

    //Constants for Websocket frame error codes and messages
    public static class FrameErrorConstants {
        public static final int API_AUTH_GENERAL_ERROR = 4000;
        public static final String API_AUTH_GENERAL_MESSAGE = "Unclassified Authentication Failure";
        public static final int API_AUTH_INVALID_CREDENTIALS = 4001;
        public static final String API_AUTH_INVALID_CREDENTIALS_MESSAGE = "Invalid Credentials";
        public static final int RESOURCE_FORBIDDEN_ERROR = 4002;
        public static final String RESOURCE_FORBIDDEN_ERROR_MESSAGE = "User NOT authorized to access the resource";
        public static final int THROTTLED_OUT_ERROR = 4003;
        public static final String THROTTLED_OUT_ERROR_MESSAGE = "Websocket frame throttled out";
        public static final int INTERNAL_SERVER_ERROR = 4004;
        public static final int BAD_REQUEST = 4005;
        public static final int GRAPHQL_QUERY_TOO_DEEP = 4020;
        public static final String GRAPHQL_QUERY_TOO_DEEP_MESSAGE = "QUERY TOO DEEP";
        public static final int GRAPHQL_QUERY_TOO_COMPLEX = 4021;
        public static final String GRAPHQL_QUERY_TOO_COMPLEX_MESSAGE = "QUERY TOO COMPLEX";
        public static final int GRAPHQL_INVALID_QUERY = 4022;
        public static final String GRAPHQL_INVALID_QUERY_MESSAGE = "INVALID QUERY";
        public static final String ERROR_CODE = "code";
        public static final String ERROR_MESSAGE = "message";
    }

    //Constants for Websocket handshake error codes and messages
    public static class HandshakeErrorConstants {
        public static final int API_AUTH_ERROR = 401;
        public static final String API_AUTH_INVALID_CREDENTIALS_MESSAGE = "Invalid Credentials";
        public static final String API_AUTH_GENERAL_MESSAGE = "Unclassified Authentication Failure";
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int RESOURCE_NOT_FOUND_ERROR = 404;
    }
}
