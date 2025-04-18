/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.oas.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class APIParserConstants {

    public static final String STRING = "string";
    public static final String OBJECT = "object";
    public static final String APPLICATION_JSON_MEDIA_TYPE = "application/json";
    public static final String APPLICATION_XML_MEDIA_TYPE = "application/xml";
    public static final String OPENAPI_MASTER_JSON = "swagger.json";
    public static final String OPENAPI_MASTER_YAML = "swagger.yaml";
    public static final String DEFAULT_API_SECURITY_OAUTH2 = "oauth2";
    public static final String API_SECURITY_MUTUAL_SSL = "mutualssl";
    public static final String API_SECURITY_BASIC_AUTH = "basic_auth";
    public static final String SWAGGER_API_SECURITY_BASIC_AUTH_TYPE = "basic";
    public static final String API_SECURITY_API_KEY = "api_key";
    public static final String API_SECURITY_MUTUAL_SSL_MANDATORY = "mutualssl_mandatory";
    public static final String API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY = "oauth_basic_auth_api_key_mandatory";
    public static final List<String> APPLICATION_LEVEL_SECURITY = Arrays.asList("basic_auth", "api_key", "oauth2");
    public static final String API_KEY_HEADER_QUERY_PARAM = "apikey";
    public static final String API_TYPE_WS = "WS";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";
    public static final String ENABLED = "Enabled";
    public static final String GRAPHQL_API = "GRAPHQL";
    public static final String HTTP_VERB_PUBLISH = "PUBLISH";
    public static final String HTTP_VERB_SUBSCRIBE = "SUBSCRIBE";
    public static final String OAUTH2_DEFAULT_SCOPE = "default";
    public static final String API_DATA_URL = "url";
    public static final String IMPLEMENTATION_STATUS = "implementation_status";
    public static final String ENDPOINT_TYPE_DEFAULT = "default";
    public static final String ENDPOINT_TYPE_FAILOVER = "failover";
    public static final String ENDPOINT_TYPE_LOADBALANCE = "load_balance";
    public static final String ENDPOINT_TYPE_HTTP = "http";
    public static final String ENDPOINT_TYPE_SERVICE = "service";
    public static final String ENDPOINT_TYPE_ADDRESS = "address";
    public static final String ENDPOINT_PRODUCTION_FAILOVERS = "production_failovers";
    public static final String ENDPOINT_SANDBOX_FAILOVERS = "sandbox_failovers";
    public static final String ENDPOINT_PRODUCTION_ENDPOINTS = "production_endpoints";
    public static final String ENDPOINT_SANDBOX_ENDPOINTS = "sandbox_endpoints";
    public static final String ENDPOINT_URLS = "urls";
    public static final String ENDPOINT_URL = "url";
    public static final String ENDPOINT_SECURITY_TYPE = "type";
    public static final String ENDPOINT_SECURITY_TYPE_BASIC = "basic";
    public static final String ENDPOINT_SECURITY_TYPE_DIGEST = "digest";
    public static final String ENDPOINT_SECURITY_USERNAME = "username";
    public static final String ENDPOINT_SECURITY_CONFIG = "securityConfig";
    public static final String API_ENDPOINT_CONFIG_PROTOCOL_TYPE = "endpoint_type";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED = "Unlimited";
    public static final String DEFAULT_API_POLICY_UNLIMITED = "Unlimited";
    public static final String API_TYPE_WEBSUB = "WEBSUB";
    public static final String TYPE = "type";
    public static final String HTTP_POST = "POST";
    public static final String WSO2_GATEWAY_ENVIRONMENT = "wso2";
    public static final String SWAGGER_RELAXED_VALIDATION = "swaggerRelaxedValidation";
    public static final String PAYLOAD_PARAM_NAME = "Payload";
    public static final String CHARSET = "UTF-8";

    //URI Authentication Schemes
    public static final Set<String> SUPPORTED_METHODS =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList("get", "put", "post", "delete", "patch", "head", "options")));
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";

    //Swagger v2.0 constants
    public static final String SWAGGER_X_SCOPE = "x-scope";
    public static final String SWAGGER_X_AMZN_RESOURCE_NAME = "x-amzn-resource-name";
    public static final String SWAGGER_X_AMZN_RESOURCE_TIMEOUT = "x-amzn-resource-timeout";
    public static final String SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED = "x-amzn-resource-content-encode";
    public static final String SWAGGER_X_AUTH_TYPE = "x-auth-type";
    public static final String SWAGGER_X_THROTTLING_TIER = "x-throttling-tier";
    public static final String SWAGGER_X_THROTTLING_BANDWIDTH = "x-throttling-bandwidth";
    public static final String SWAGGER_X_MEDIATION_SCRIPT = "x-mediation-script";
    public static final String SWAGGER_X_WSO2_SECURITY = "x-wso2-security";
    public static final String WSO2_APP_SECURITY_TYPES = "security-types";
    public static final String OPTIONAL = "optional";
    public static final String MANDATORY = "mandatory";
    public static final String RESPONSE_CACHING_ENABLED = "enabled";
    public static final String RESPONSE_CACHING_TIMEOUT = "cacheTimeoutInSeconds";
    public static final String SWAGGER_X_WSO2_SCOPES = "x-wso2-scopes";
    public static final String SWAGGER_X_EXAMPLES = "x-examples";
    public static final String SWAGGER_SCOPE_KEY = "key";
    public static final String SWAGGER_NAME = "name";
    public static final String SWAGGER_DESCRIPTION = "description";
    public static final String SWAGGER_ROLES = "roles";
    public static final String SWAGGER = "swagger";
    public static final String SWAGGER_RESPONSE_200 = "200";
    public static final String SWAGGER_APIM_DEFAULT_SECURITY = "default";
    public static final String SWAGGER_APIM_RESTAPI_SECURITY = "OAuth2Security";
    public static final String OPEN_API = "openapi";
    public static final String OAS_V31 = "v31";
    public static final String OPEN_API_V31_VERSION = "3.1.0";
    public static final String SWAGGER_IS_MISSING_MSG = "swagger is missing";
    public static final String OPENAPI_IS_MISSING_MSG = "openapi is missing";
    public static final String SWAGGER_X_SCOPES_BINDINGS = "x-scopes-bindings";
    public static final String SWAGGER_X_BASIC_AUTH_SCOPES = "x-scopes";
    public static final String SWAGGER_X_BASIC_AUTH_RESOURCE_SCOPES = "x-basic-auth-scopes";

    //swagger MG related constants
    public static final String X_WSO2_AUTH_HEADER = "x-wso2-auth-header";
    public static final String X_WSO2_API_KEY_HEADER = "x-wso2-api-key-header";
    public static final String X_THROTTLING_TIER = "x-throttling-tier";
    public static final String X_WSO2_CORS = "x-wso2-cors";
    public static final String X_WSO2_PRODUCTION_ENDPOINTS = "x-wso2-production-endpoints";
    public static final String X_WSO2_SANDBOX_ENDPOINTS = "x-wso2-sandbox-endpoints";
    public static final String X_WSO2_BASEPATH = "x-wso2-basePath";
    public static final String X_WSO2_TRANSPORTS = "x-wso2-transports";
    public static final String X_WSO2_MUTUAL_SSL = "x-wso2-mutual-ssl";
    public static final String X_WSO2_APP_SECURITY = "x-wso2-application-security";
    public static final String X_WSO2_RESPONSE_CACHE = "x-wso2-response-cache";
    public static final String X_WSO2_DISABLE_SECURITY = "x-wso2-disable-security";
    public static final String X_WSO2_THROTTLING_TIER = "x-wso2-throttling-tier";
    public static final String X_WSO2_ENDPOINT_TYPE = "type";
    public static final String ADVANCE_ENDPOINT_CONFIG = "advanceEndpointConfig";

    // mock response generation
    public static final String MOCK_GEN_POLICY_LIST = "policyList";

    // Protocol variables
    public static final String HTTP_TRANSPORT_PROTOCOL_NAME = "http";
    public static final String WS_TRANSPORT_PROTOCOL_NAME = "ws";
    public static final String KAFKA_TRANSPORT_PROTOCOL_NAME = "kafka";
    public static final String AMQP_TRANSPORT_PROTOCOL_NAME = "amqp";
    public static final String AMQP1_TRANSPORT_PROTOCOL_NAME = "amqp1";
    public static final String MQTT_TRANSPORT_PROTOCOL_NAME = "mqtt";
    public static final String MQTT5_TRANSPORT_PROTOCOL_NAME = "mqtt5";
    public static final String NATS_TRANSPORT_PROTOCOL_NAME = "nats";
    public static final String JMS_TRANSPORT_PROTOCOL_NAME = "jms";
    public static final String SNS_TRANSPORT_PROTOCOL_NAME = "sns";
    public static final String SQS_TRANSPORT_PROTOCOL_NAME = "sqs";
    public static final String STOMP_TRANSPORT_PROTOCOL_NAME = "stomp";
    public static final String REDIS_TRANSPORT_PROTOCOL_NAME = "redis";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTPS_PROTOCOL_URL_PREFIX = "https://";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL_URL_PREFIX = "http://";
    public static final String WS_PROTOCOL = "ws";
    public static final String WS_PROTOCOL_URL_PREFIX = "ws://";
    public static final String WSS_PROTOCOL = "wss";
    public static final String WSS_PROTOCOL_URL_PREFIX = "wss://";

    public static class KeyManager {

        public static final String TOKEN_ENDPOINT = "token_endpoint";
        public static final String DEFAULT_KEY_MANAGER = "Resident Key Manager";
        public static final String DEFAULT_KEY_MANAGER_TYPE = "default";

        public static final String AUTHORIZE_ENDPOINT = "authorize_endpoint";

        public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
        public static final String IMPLICIT_GRANT_TYPE = "implicit";
        public static final String PASSWORD_GRANT_TYPE = "password";
        public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
        public static final String APPLICATION_GRANT_TYPE = "application";
        public static final String ACCESS_CODE_GRANT_TYPE = "accessCode";
    }

    public static class OASResourceAuthTypes {

        public static final String APPLICATION_OR_APPLICATION_USER = "Application & Application User";
        public static final String APPLICATION_USER = "Application User";
        public static final String APPLICATION = "Application";
    }

    public enum SupportedHTTPVerbs {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS
    }
}
