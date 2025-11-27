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

package org.wso2.carbon.apimgt.spec.parser.definitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents the constants used in the API Specification Parser.
 */
public final class APISpecParserConstants {
    public static final String STRING = "string";
    public static final String OBJECT = "object";
    public static final String PATH = "path";
    public static final String CONTENT_TYPE = "contentType";
    public static final String REQUEST_BODY = "requestBody";
    public static final String PROPERTIES = "properties";
    public static final String REQUIRED = "required";
    public static final String FORMAT = "format";
    public static final String ENUM = "enum";
    public static final String DEFAULT = "default";
    public static final String DESCRIPTION = "description";
    public static final String SCHEMAS = "schemas";
    public static final String PARAMETERS = "parameters";
    public static final String REQUEST_BODIES = "requestBodies";
    public static final String AUTH_TYPE_ANY = "Any";
    public static final String APPLICATION_JSON_MEDIA_TYPE = "application/json";
    public static final String APPLICATION_XML_MEDIA_TYPE = "application/xml";
    public static final String OPENAPI_ARCHIVES_TEMP_FOLDER = "OPENAPI-archives";
    public static final String OPENAPI_EXTRACTED_DIRECTORY = "extracted";
    public static final String OPENAPI_ARCHIVE_ZIP_FILE = "openapi-archive.zip";
    public static final String OPENAPI_MASTER_JSON = "swagger.json";
    public static final String OPENAPI_MASTER_YAML = "swagger.yaml";
    public static final String DEFAULT_API_SECURITY_OAUTH2 = "oauth2";
    public static final String API_SECURITY_MUTUAL_SSL = "mutualssl";
    public static final String API_SECURITY_BASIC_AUTH = "basic_auth";
    public static final String SWAGGER_API_SECURITY_BASIC_AUTH_TYPE = "basic";
    public static final String API_SECURITY_API_KEY = "api_key";
    public static final String API_SECURITY_MUTUAL_SSL_MANDATORY = "mutualssl_mandatory";
    public static final String API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY = "oauth_basic_auth_api_key_mandatory";
    public static final String API_KEY_HEADER_QUERY_PARAM = "apikey";
    public static final String API_TYPE_WS = "WS";
    public static final String AUTH_APPLICATION_LEVEL_TOKEN = "Application";
    public static final String AUTH_APPLICATION_USER_LEVEL_TOKEN = "Application_User";
    public static final String AUTH_APPLICATION_OR_USER_LEVEL_TOKEN = "Any";
    public static final String GATEWAY_ENV_TYPE_PRODUCTION = "production";
    public static final String GATEWAY_ENV_TYPE_SANDBOX = "sandbox";
    public static final String ENABLED = "Enabled";
    public static final String GRAPHQL_API = "GRAPHQL";
    public static final String MCP_API = "MCP";
    public static final String HTTP_VERB_TOOL = "TOOL";
    public static final String HTTP_VERB_PUBLISH = "PUBLISH";
    public static final String HTTP_VERB_SUBSCRIBE = "SUBSCRIBE";
    public static final String OAUTH2_DEFAULT_SCOPE = "default";
    public static final String SWAGGER_X_SCOPE = "x-scope";
    public static final String SWAGGER_X_AMZN_RESOURCE_NAME = "x-amzn-resource-name";
    public static final String SWAGGER_X_AMZN_RESOURCE_TIMEOUT = "x-amzn-resource-timeout";
    public static final String SWAGGER_X_AMZN_RESOURCE_CONTENT_ENCODED = "x-amzn-resource-content-encode";
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
    public static final String SWAGGER_PATHS = "paths";
    public static final String SWAGGER = "swagger";
    public static final String SWAGGER_RESPONSE_200 = "200";
    public static final String SWAGGER_APIM_DEFAULT_SECURITY = "default";
    public static final String OPEN_API = "openapi";
    public static final String OAS_V31 = "v31";
    public static final String OAS_V30 = "v30";
    public static final String OPEN_API_V31_VERSION = "3.1.0";
    public static final String SWAGGER_IS_MISSING_MSG = "swagger is missing";
    public static final String OPENAPI_IS_MISSING_MSG = "openapi is missing";
    public static final String SWAGGER_X_SCOPES_BINDINGS = "x-scopes-bindings";
    public static final String SWAGGER_X_BASIC_AUTH_SCOPES = "x-scopes";
    public static final String SWAGGER_X_BASIC_AUTH_RESOURCE_SCOPES = "x-basic-auth-scopes";
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
    public static final String API_DATA_URL = "url";
    public static final String MOCK_GEN_POLICY_LIST = "policyList";
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
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTPS_PROTOCOL_URL_PREFIX = "https://";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTP_PROTOCOL_URL_PREFIX = "http://";
    public static final String WS_PROTOCOL = "ws";
    public static final String WS_PROTOCOL_URL_PREFIX = "ws://";
    public static final String WSS_PROTOCOL = "wss";
    public static final String WSS_PROTOCOL_URL_PREFIX = "wss://";
    public static final String DEFAULT_SUB_POLICY_UNLIMITED = "Unlimited";
    public static final String DEFAULT_API_POLICY_UNLIMITED = "Unlimited";
    public static final String API_TYPE_WEBSUB = "WEBSUB";
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final String TYPE = "type";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String WSO2_GATEWAY_ENVIRONMENT = "wso2";
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
    public static final String SWAGGER_RELAXED_VALIDATION = "swaggerRelaxedValidation";
    public static final String SWAGGER_APIM_RESTAPI_SECURITY = "OAuth2Security";
    public static final String OPENAPIV31_SCHEMA_TYPE_NULLABLE = "null";
    public static final String OPENAPI_OBJECT_DATA_TYPE = "object";
    public static final String OPENAPI_STRING_DATA_TYPE = "string";
    public static final String OPENAPI_ARRAY_DATA_TYPE = "array";

    // GraphQL specific constants
    public static final String GRAPHQL_QUERY = "Query";
    public static final String GRAPHQL_MUTATION = "Mutation";
    public static final String GRAPHQL_SUBSCRIPTION = "Subscription";
    public static final String SCOPE_OPERATION_MAPPING = "WSO2ScopeOperationMapping";
    public static final String SCOPE_ROLE_MAPPING = "WSO2ScopeRoleMapping";
    public static final String OPERATION_THROTTLING_MAPPING = "WSO2OperationThrottlingMapping";
    public static final String AUTH_NO_AUTHENTICATION = "None";
    public static final String OPERATION_SECURITY_DISABLED = "Disabled";
    public static final String OPERATION_SECURITY_ENABLED = "Enabled";
    public static final String OPERATION_AUTH_SCHEME_MAPPING = "WSO2OperationAuthSchemeMapping";
    public static final String GRAPHQL_ACCESS_CONTROL_POLICY = "WSO2GraphQLAccessControlPolicy";
    public static final String QUERY_ANALYSIS_COMPLEXITY = "complexity";

    public static final List<String> APPLICATION_LEVEL_SECURITY = Arrays.asList("basic_auth", "api_key", "oauth2");

    public static final Set<String> SUPPORTED_METHODS =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(new String[]{"get", "put", "post", "delete", "patch", "head", "options", "tool"})));
    public static final String MCP_RESOURCES_MCP = "/mcp";
    public static final String MCP_RESOURCES_WELL_KNOWN = "/.well-known/oauth-protected-resource";

    public static class OperationParameter {
        public static final String PAYLOAD_PARAM_NAME = "Payload";
    }

    public static class DigestAuthConstants {
        public static final String CHARSET = "UTF-8";
    }

    public static class OASResourceAuthTypes {
        public static final String APPLICATION_OR_APPLICATION_USER = "Application & Application User";
        public static final String APPLICATION_USER = "Application User";
        public static final String APPLICATION = "Application";
    }

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

    // ASYNC API specific constants
    public static final String ASYNCAPI_URI_MAPPING = "x-uri-mapping";
    public static final String ASYNCAPI_ACTION_SEND = "send";
    public static final String ASYNCAPI_ACTION_RECEIVE = "receive";
    public static final String ASYNCAPI_ACTION_SEND_OPS = "send_";
    public static final String ASYNCAPI_ACTION_RECEIVE_OPS = "receive_";
    public static final String ASYNCAPI_CHANNELS_PATH = "#/channels/";
    public static final String WS_URI_MAPPING_PUBLISH = "PUBLISH_";
    public static final String WS_URI_MAPPING_SUBSCRIBE = "SUBSCRIBE_";

    public static class AsyncApi {
        public static final String ASYNC_API = "asyncapi";
        public static final String ASYNC_API_V20 = "2.0";
        public static final String ASYNC_API_V21 = "2.1";
        public static final String ASYNC_API_V22 = "2.2";
        public static final String ASYNC_API_V23 = "2.3";
        public static final String ASYNC_API_V24 = "2.4";
        public static final String ASYNC_API_V25 = "2.5";
        public static final String ASYNC_API_V26 = "2.6";
        public static final String ASYNC_API_V30 = "3.0";
        public static final String ASYNC_API_V2 = "2.0.0";
        public static final String ASYNC_API_V3 = "3.0.0";
    }

    public static class AsyncApiSchemas {
        public static final String METASCHEMA = "{\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"$id\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"title\": \"Core schema meta-schema\",\n" +
                "    \"definitions\": {\n" +
                "        \"schemaArray\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": { \"$ref\": \"#\" }\n" +
                "        },\n" +
                "        \"nonNegativeInteger\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"minimum\": 0\n" +
                "        },\n" +
                "        \"nonNegativeIntegerDefault0\": {\n" +
                "            \"allOf\": [\n" +
                "                { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
                "                { \"default\": 0 }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"simpleTypes\": {\n" +
                "            \"enum\": [\n" +
                "                \"array\",\n" +
                "                \"boolean\",\n" +
                "                \"integer\",\n" +
                "                \"null\",\n" +
                "                \"number\",\n" +
                "                \"object\",\n" +
                "                \"string\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"stringArray\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": { \"type\": \"string\" },\n" +
                "            \"uniqueItems\": true,\n" +
                "            \"default\": []\n" +
                "        }\n" +
                "    },\n" +
                "    \"type\": [\"object\", \"boolean\"],\n" +
                "    \"properties\": {\n" +
                "        \"$id\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"format\": \"uri-reference\"\n" +
                "        },\n" +
                "        \"$schema\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"$ref\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"format\": \"uri-reference\"\n" +
                "        },\n" +
                "        \"$comment\": {\n" +
                "            \"type\": \"string\"\n" +
                "        },\n" +
                "        \"title\": {\n" +
                "            \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "            \"type\": \"string\"\n" +
                "        },\n" +
                "        \"default\": true,\n" +
                "        \"readOnly\": {\n" +
                "            \"type\": \"boolean\",\n" +
                "            \"default\": false\n" +
                "        },\n" +
                "        \"writeOnly\": {\n" +
                "            \"type\": \"boolean\",\n" +
                "            \"default\": false\n" +
                "        },\n" +
                "        \"examples\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": true\n" +
                "        },\n" +
                "        \"multipleOf\": {\n" +
                "            \"type\": \"number\",\n" +
                "            \"exclusiveMinimum\": 0\n" +
                "        },\n" +
                "        \"maximum\": {\n" +
                "            \"type\": \"number\"\n" +
                "        },\n" +
                "        \"exclusiveMaximum\": {\n" +
                "            \"type\": \"number\"\n" +
                "        },\n" +
                "        \"minimum\": {\n" +
                "            \"type\": \"number\"\n" +
                "        },\n" +
                "        \"exclusiveMinimum\": {\n" +
                "            \"type\": \"number\"\n" +
                "        },\n" +
                "        \"maxLength\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
                "        \"minLength\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
                "        \"pattern\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"format\": \"regex\"\n" +
                "        },\n" +
                "        \"additionalItems\": { \"$ref\": \"#\" },\n" +
                "        \"items\": {\n" +
                "            \"anyOf\": [\n" +
                "                { \"$ref\": \"#\" },\n" +
                "                { \"$ref\": \"#/definitions/schemaArray\" }\n" +
                "            ],\n" +
                "            \"default\": true\n" +
                "        },\n" +
                "        \"maxItems\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
                "        \"minItems\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
                "        \"uniqueItems\": {\n" +
                "            \"type\": \"boolean\",\n" +
                "            \"default\": false\n" +
                "        },\n" +
                "        \"contains\": { \"$ref\": \"#\" },\n" +
                "        \"maxProperties\": { \"$ref\": \"#/definitions/nonNegativeInteger\" },\n" +
                "        \"minProperties\": { \"$ref\": \"#/definitions/nonNegativeIntegerDefault0\" },\n" +
                "        \"required\": { \"$ref\": \"#/definitions/stringArray\" },\n" +
                "        \"additionalProperties\": { \"$ref\": \"#\" },\n" +
                "        \"definitions\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
                "            \"default\": {}\n" +
                "        },\n" +
                "        \"properties\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
                "            \"default\": {}\n" +
                "        },\n" +
                "        \"patternProperties\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"additionalProperties\": { \"$ref\": \"#\" },\n" +
                "            \"propertyNames\": { \"format\": \"regex\" },\n" +
                "            \"default\": {}\n" +
                "        },\n" +
                "        \"dependencies\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"additionalProperties\": {\n" +
                "                \"anyOf\": [\n" +
                "                    { \"$ref\": \"#\" },\n" +
                "                    { \"$ref\": \"#/definitions/stringArray\" }\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        \"propertyNames\": { \"$ref\": \"#\" },\n" +
                "        \"const\": true,\n" +
                "        \"enum\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": true,\n" +
                "            \"uniqueItems\": true\n" +
                "        },\n" +
                "        \"type\": {\n" +
                "            \"anyOf\": [\n" +
                "                { \"$ref\": \"#/definitions/simpleTypes\" },\n" +
                "                {\n" +
                "                    \"type\": \"array\",\n" +
                "                    \"items\": { \"$ref\": \"#/definitions/simpleTypes\" },\n" +
                "                    \"uniqueItems\": true\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        \"format\": { \"type\": \"string\" },\n" +
                "        \"contentMediaType\": { \"type\": \"string\" },\n" +
                "        \"contentEncoding\": { \"type\": \"string\" },\n" +
                "        \"if\": { \"$ref\": \"#\" },\n" +
                "        \"then\": { \"$ref\": \"#\" },\n" +
                "        \"else\": { \"$ref\": \"#\" },\n" +
                "        \"allOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
                "        \"anyOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
                "        \"oneOf\": { \"$ref\": \"#/definitions/schemaArray\" },\n" +
                "        \"not\": { \"$ref\": \"#\" }\n" +
                "    },\n" +
                "    \"default\": true\n" +
                "}";

        public static final String ASYNCAPI_JSON_HYPERSCHEMA = "{\n" +
                "  \"title\": \"AsyncAPI 2.0.0 schema.\",\n" +
                "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"required\": [\n" +
                "    \"asyncapi\",\n" +
                "    \"info\",\n" +
                "    \"channels\"\n" +
                "  ],\n" +
                "  \"additionalProperties\": false,\n" +
                "  \"patternProperties\": {\n" +
                "    \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "      \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"asyncapi\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\n" +
                "        \"2.0.0\"\n" +
                "      ],\n" +
                "      \"description\": \"The AsyncAPI specification version of this document.\"\n" +
                "    },\n" +
                "    \"id\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"A unique id representing the application.\",\n" +
                "      \"format\": \"uri\"\n" +
                "    },\n" +
                "    \"info\": {\n" +
                "      \"$ref\": \"#/definitions/info\"\n" +
                "    },\n" +
                "    \"servers\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/server\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"defaultContentType\": {\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"channels\": {\n" +
                "      \"$ref\": \"#/definitions/channels\"\n" +
                "    },\n" +
                "    \"components\": {\n" +
                "      \"$ref\": \"#/definitions/components\"\n" +
                "    },\n" +
                "    \"tags\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"$ref\": \"#/definitions/tag\"\n" +
                "      },\n" +
                "      \"uniqueItems\": true\n" +
                "    },\n" +
                "    \"externalDocs\": {\n" +
                "      \"$ref\": \"#/definitions/externalDocs\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"definitions\": {\n" +
                "    \"Reference\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"$ref\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"$ref\": {\n" +
                "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"ReferenceObject\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"format\": \"uri-reference\"\n" +
                "    },\n" +
                "    \"info\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"General information about the API.\",\n" +
                "      \"required\": [\n" +
                "        \"version\",\n" +
                "        \"title\"\n" +
                "      ],\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"title\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A unique and precise title of the API.\"\n" +
                "        },\n" +
                "        \"version\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A semantic version number of the API.\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A longer description of the API. Should be different from the title. CommonMark is allowed.\"\n" +
                "        },\n" +
                "        \"termsOfService\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A URL to the Terms of Service for the API. MUST be in the format of a URL.\",\n" +
                "          \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"contact\": {\n" +
                "          \"$ref\": \"#/definitions/contact\"\n" +
                "        },\n" +
                "        \"license\": {\n" +
                "          \"$ref\": \"#/definitions/license\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"contact\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"Contact information for the owners of the API.\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"properties\": {\n" +
                "        \"name\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The identifying name of the contact person/organization.\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The URL pointing to the contact information.\",\n" +
                "          \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"email\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The email address of the contact person/organization.\",\n" +
                "          \"format\": \"email\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"license\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"name\"\n" +
                "      ],\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"properties\": {\n" +
                "        \"name\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The name of the license type. It's encouraged to use an OSI compatible license.\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The URL pointing to the license.\",\n" +
                "          \"format\": \"uri\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"server\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"An object representing a Server.\",\n" +
                "      \"required\": [\n" +
                "        \"url\",\n" +
                "        \"protocol\"\n" +
                "      ],\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"url\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"protocol\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"The transfer protocol.\"\n" +
                "        },\n" +
                "        \"protocolVersion\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"variables\": {\n" +
                "          \"$ref\": \"#/definitions/serverVariables\"\n" +
                "        },\n" +
                "        \"security\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"$ref\": \"#/definitions/SecurityRequirement\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"bindings\": {\n" +
                "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"serverVariables\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/serverVariable\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"serverVariable\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"An object representing a Server Variable for server URL template substitution.\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"enum\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"uniqueItems\": true\n" +
                "        },\n" +
                "        \"default\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"examples\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"channels\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"propertyNames\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"format\": \"uri-template\",\n" +
                "        \"minLength\": 1\n" +
                "      },\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/channelItem\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"components\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"description\": \"An object to hold a set of reusable objects for different aspects of the AsyncAPI Specification.\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"properties\": {\n" +
                "        \"schemas\": {\n" +
                "          \"$ref\": \"#/definitions/schemas\"\n" +
                "        },\n" +
                "        \"messages\": {\n" +
                "          \"$ref\": \"#/definitions/messages\"\n" +
                "        },\n" +
                "        \"securitySchemes\": {\n" +
                "          \"type\": \"object\",\n" +
                "        },\n" +
                "        \"parameters\": {\n" +
                "          \"$ref\": \"#/definitions/parameters\"\n" +
                "        },\n" +
                "        \"correlationIds\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"patternProperties\": {\n" +
                "            \"^[\\\\w\\\\d\\\\.\\\\-_]+$\": {\n" +
                "              \"oneOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/Reference\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/correlationId\"\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"operationTraits\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/operationTrait\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"messageTraits\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/messageTrait\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"serverBindings\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelBindings\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"operationBindings\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"messageBindings\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"schemas\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/schema\"\n" +
                "      },\n" +
                "      \"description\": \"JSON objects describing schemas the API uses.\"\n" +
                "    },\n" +
                "    \"messages\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/message\"\n" +
                "      },\n" +
                "      \"description\": \"JSON objects describing the messages being consumed and produced by the API.\"\n" +
                "    },\n" +
                "    \"parameters\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"$ref\": \"#/definitions/parameter\"\n" +
                "      },\n" +
                "      \"description\": \"JSON objects describing re-usable channel parameters.\"\n" +
                "    },\n" +
                "    \"schema\": {\n" +
                "      \"allOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"http://json-schema.org/draft-07/schema#\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"object\",\n" +
                "          \"patternProperties\": {\n" +
                "            \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "              \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"properties\": {\n" +
                "            \"additionalProperties\": {\n" +
                "              \"anyOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/schema\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"boolean\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"default\": {}\n" +
                "            },\n" +
                "            \"items\": {\n" +
                "              \"anyOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/schema\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"minItems\": 1,\n" +
                "                  \"items\": {\n" +
                "                    \"$ref\": \"#/definitions/schema\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"default\": {}\n" +
                "            },\n" +
                "            \"allOf\": {\n" +
                "              \"type\": \"array\",\n" +
                "              \"minItems\": 1,\n" +
                "              \"items\": {\n" +
                "                \"$ref\": \"#/definitions/schema\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"oneOf\": {\n" +
                "              \"type\": \"array\",\n" +
                "              \"minItems\": 2,\n" +
                "              \"items\": {\n" +
                "                \"$ref\": \"#/definitions/schema\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"anyOf\": {\n" +
                "              \"type\": \"array\",\n" +
                "              \"minItems\": 2,\n" +
                "              \"items\": {\n" +
                "                \"$ref\": \"#/definitions/schema\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"not\": {\n" +
                "              \"$ref\": \"#/definitions/schema\"\n" +
                "            },\n" +
                "            \"properties\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"additionalProperties\": {\n" +
                "                \"$ref\": \"#/definitions/schema\"\n" +
                "              },\n" +
                "              \"default\": {}\n" +
                "            },\n" +
                "            \"patternProperties\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"additionalProperties\": {\n" +
                "                \"$ref\": \"#/definitions/schema\"\n" +
                "              },\n" +
                "              \"default\": {}\n" +
                "            },\n" +
                "            \"propertyNames\": {\n" +
                "              \"$ref\": \"#/definitions/schema\"\n" +
                "            },\n" +
                "            \"contains\": {\n" +
                "              \"$ref\": \"#/definitions/schema\"\n" +
                "            },\n" +
                "            \"discriminator\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"externalDocs\": {\n" +
                "              \"$ref\": \"#/definitions/externalDocs\"\n" +
                "            },\n" +
                "            \"deprecated\": {\n" +
                "              \"type\": \"boolean\",\n" +
                "              \"default\": false\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"externalDocs\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"description\": \"information about external documentation\",\n" +
                "      \"required\": [\n" +
                "        \"url\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"url\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"uri\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"channelItem\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"$ref\": {\n" +
                "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
                "        },\n" +
                "        \"parameters\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"additionalProperties\": {\n" +
                "            \"$ref\": \"#/definitions/parameter\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A description of the channel.\"\n" +
                "        },\n" +
                "        \"publish\": {\n" +
                "          \"$ref\": \"#/definitions/operation\"\n" +
                "        },\n" +
                "        \"subscribe\": {\n" +
                "          \"$ref\": \"#/definitions/operation\"\n" +
                "        },\n" +
                "        \"deprecated\": {\n" +
                "          \"type\": \"boolean\",\n" +
                "          \"default\": false\n" +
                "        },\n" +
                "        \"bindings\": {\n" +
                "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"parameter\": {\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A brief description of the parameter. This could contain examples of use. GitHub Flavored Markdown is allowed.\"\n" +
                "        },\n" +
                "        \"schema\": {\n" +
                "          \"$ref\": \"#/definitions/schema\"\n" +
                "        },\n" +
                "        \"location\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A runtime expression that specifies the location of the parameter value\",\n" +
                "          \"pattern\": \"^\\\\$message\\\\.(header|payload)\\\\#(\\\\/(([^\\\\/~])|(~[01]))*)*\"\n" +
                "        },\n" +
                "        \"$ref\": {\n" +
                "          \"$ref\": \"#/definitions/ReferenceObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"operation\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"traits\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"oneOf\": [\n" +
                "              {\n" +
                "                \"$ref\": \"#/definitions/Reference\"\n" +
                "              },\n" +
                "              {\n" +
                "                \"$ref\": \"#/definitions/operationTrait\"\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"array\",\n" +
                "                \"items\": [\n" +
                "                  {\n" +
                "                    \"oneOf\": [\n" +
                "                      {\n" +
                "                        \"$ref\": \"#/definitions/Reference\"\n" +
                "                      },\n" +
                "                      {\n" +
                "                        \"$ref\": \"#/definitions/operationTrait\"\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"type\": \"object\",\n" +
                "                    \"additionalItems\": true\n" +
                "                  }\n" +
                "                ]\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        \"summary\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"tags\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"$ref\": \"#/definitions/tag\"\n" +
                "          },\n" +
                "          \"uniqueItems\": true\n" +
                "        },\n" +
                "        \"externalDocs\": {\n" +
                "          \"$ref\": \"#/definitions/externalDocs\"\n" +
                "        },\n" +
                "        \"operationId\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"bindings\": {\n" +
                "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "        },\n" +
                "        \"message\": {\n" +
                "          \"$ref\": \"#/definitions/message\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"message\": {\n" +
                "      \"oneOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/Reference\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"oneOf\": [\n" +
                "            {\n" +
                "              \"type\": \"object\",\n" +
                "              \"required\": [\n" +
                "                \"oneOf\"\n" +
                "              ],\n" +
                "              \"additionalProperties\": false,\n" +
                "              \"properties\": {\n" +
                "                \"oneOf\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"$ref\": \"#/definitions/message\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"object\",\n" +
                "              \"additionalProperties\": false,\n" +
                "              \"patternProperties\": {\n" +
                "                \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "                  \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"properties\": {\n" +
                "                \"schemaFormat\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                \"contentType\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                \"headers\": {\n" +
                "                  \"allOf\": [\n" +
                "                    { \"$ref\": \"#/definitions/schema\" },\n" +
                "                    { \"properties\": {\n" +
                "                      \"type\": { \"const\": \"object\" }\n" +
                "                    }\n" +
                "                    }\n" +
                "                  ]\n" +
                "                },\n" +
                "                \"payload\": {},\n" +
                "                \"correlationId\": {\n" +
                "                  \"oneOf\": [\n" +
                "                    {\n" +
                "                      \"$ref\": \"#/definitions/Reference\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"$ref\": \"#/definitions/correlationId\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                },\n" +
                "                \"tags\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"$ref\": \"#/definitions/tag\"\n" +
                "                  },\n" +
                "                  \"uniqueItems\": true\n" +
                "                },\n" +
                "                \"summary\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A brief summary of the message.\"\n" +
                "                },\n" +
                "                \"name\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"Name of the message.\"\n" +
                "                },\n" +
                "                \"title\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A human-friendly title for the message.\"\n" +
                "                },\n" +
                "                \"description\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A longer description of the message. CommonMark is allowed.\"\n" +
                "                },\n" +
                "                \"externalDocs\": {\n" +
                "                  \"$ref\": \"#/definitions/externalDocs\"\n" +
                "                },\n" +
                "                \"deprecated\": {\n" +
                "                  \"type\": \"boolean\",\n" +
                "                  \"default\": false\n" +
                "                },\n" +
                "                \"examples\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"type\": \"object\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"bindings\": {\n" +
                "                  \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "                },\n" +
                "                \"traits\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"oneOf\": [\n" +
                "                      {\n" +
                "                        \"$ref\": \"#/definitions/Reference\"\n" +
                "                      },\n" +
                "                      {\n" +
                "                        \"$ref\": \"#/definitions/messageTrait\"\n" +
                "                      },\n" +
                "                      {\n" +
                "                        \"type\": \"array\",\n" +
                "                        \"items\": [\n" +
                "                          {\n" +
                "                            \"oneOf\": [\n" +
                "                              {\n" +
                "                                \"$ref\": \"#/definitions/Reference\"\n" +
                "                              },\n" +
                "                              {\n" +
                "                                \"$ref\": \"#/definitions/messageTrait\"\n" +
                "                              }\n" +
                "                            ]\n" +
                "                          },\n" +
                "                          {\n" +
                "                            \"type\": \"object\",\n" +
                "                            \"additionalItems\": true\n" +
                "                          }\n" +
                "                        ]\n" +
                "                      }\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bindingsObject\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": true,\n" +
                "      \"properties\": {\n" +
                "        \"http\": {},\n" +
                "        \"ws\": {},\n" +
                "        \"amqp\": {},\n" +
                "        \"amqp1\": {},\n" +
                "        \"mqtt\": {},\n" +
                "        \"mqtt5\": {},\n" +
                "        \"kafka\": {},\n" +
                "        \"nats\": {},\n" +
                "        \"jms\": {},\n" +
                "        \"sns\": {},\n" +
                "        \"sqs\": {},\n" +
                "        \"stomp\": {},\n" +
                "        \"redis\": {},\n" +
                "        \"mercure\": {}\n" +
                "      }\n" +
                "    },\n" +
                "    \"correlationId\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"location\"\n" +
                "      ],\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A optional description of the correlation ID. GitHub Flavored Markdown is allowed.\"\n" +
                "        },\n" +
                "        \"location\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A runtime expression that specifies the location of the correlation ID\",\n" +
                "          \"pattern\": \"^\\\\$message\\\\.(header|payload)\\\\#(\\\\/(([^\\\\/~])|(~[01]))*)*\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"specificationExtension\": {\n" +
                "      \"description\": \"Any property starting with x- is valid.\",\n" +
                "      \"additionalProperties\": true,\n" +
                "      \"additionalItems\": true\n" +
                "    },\n" +
                "    \"tag\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"required\": [\n" +
                "        \"name\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"name\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"externalDocs\": {\n" +
                "          \"$ref\": \"#/definitions/externalDocs\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"operationTrait\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"summary\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"tags\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"$ref\": \"#/definitions/tag\"\n" +
                "          },\n" +
                "          \"uniqueItems\": true\n" +
                "        },\n" +
                "        \"externalDocs\": {\n" +
                "          \"$ref\": \"#/definitions/externalDocs\"\n" +
                "        },\n" +
                "        \"operationId\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"bindings\": {\n" +
                "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"messageTrait\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": false,\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"schemaFormat\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"contentType\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"headers\": {\n" +
                "          \"oneOf\": [\n" +
                "            {\n" +
                "              \"$ref\": \"#/definitions/Reference\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"$ref\": \"#/definitions/schema\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"correlationId\": {\n" +
                "          \"oneOf\": [\n" +
                "            {\n" +
                "              \"$ref\": \"#/definitions/Reference\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"$ref\": \"#/definitions/correlationId\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"tags\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"$ref\": \"#/definitions/tag\"\n" +
                "          },\n" +
                "          \"uniqueItems\": true\n" +
                "        },\n" +
                "        \"summary\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A brief summary of the message.\"\n" +
                "        },\n" +
                "        \"name\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"Name of the message.\"\n" +
                "        },\n" +
                "        \"title\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A human-friendly title for the message.\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"description\": \"A longer description of the message. CommonMark is allowed.\"\n" +
                "        },\n" +
                "        \"externalDocs\": {\n" +
                "          \"$ref\": \"#/definitions/externalDocs\"\n" +
                "        },\n" +
                "        \"deprecated\": {\n" +
                "          \"type\": \"boolean\",\n" +
                "          \"default\": false\n" +
                "        },\n" +
                "        \"examples\": {\n" +
                "          \"type\": \"array\",\n" +
                "          \"items\": {\n" +
                "            \"type\": \"object\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"bindings\": {\n" +
                "          \"$ref\": \"#/definitions/bindingsObject\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"SecurityScheme\": {\n" +
                "      \"oneOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/userPassword\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/apiKey\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/X509\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/symmetricEncryption\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/asymmetricEncryption\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/HTTPSecurityScheme\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/oauth2Flows\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/openIdConnect\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"userPassword\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"userPassword\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"apiKey\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\",\n" +
                "        \"in\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"apiKey\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"in\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"user\",\n" +
                "            \"password\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"X509\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"X509\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"symmetricEncryption\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"symmetricEncryption\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"asymmetricEncryption\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"asymmetricEncryption\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"HTTPSecurityScheme\": {\n" +
                "      \"oneOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/NonBearerHTTPSecurityScheme\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/BearerHTTPSecurityScheme\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"$ref\": \"#/definitions/APIKeyHTTPSecurityScheme\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"NonBearerHTTPSecurityScheme\": {\n" +
                "      \"not\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"scheme\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\n" +
                "              \"bearer\"\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"scheme\",\n" +
                "        \"type\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"scheme\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"http\"\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"BearerHTTPSecurityScheme\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\",\n" +
                "        \"scheme\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"scheme\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"bearer\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"bearerFormat\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"http\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"APIKeyHTTPSecurityScheme\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\",\n" +
                "        \"name\",\n" +
                "        \"in\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"httpApiKey\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"name\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"in\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"header\",\n" +
                "            \"query\",\n" +
                "            \"cookie\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"oauth2Flows\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\",\n" +
                "        \"flows\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"flows\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"implicit\": {\n" +
                "              \"allOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"required\": [\n" +
                "                    \"authorizationUrl\",\n" +
                "                    \"scopes\"\n" +
                "                  ]\n" +
                "                },\n" +
                "                {\n" +
                "                  \"not\": {\n" +
                "                    \"required\": [\n" +
                "                      \"tokenUrl\"\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"password\": {\n" +
                "              \"allOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"required\": [\n" +
                "                    \"tokenUrl\",\n" +
                "                    \"scopes\"\n" +
                "                  ]\n" +
                "                },\n" +
                "                {\n" +
                "                  \"not\": {\n" +
                "                    \"required\": [\n" +
                "                      \"authorizationUrl\"\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"clientCredentials\": {\n" +
                "              \"allOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"required\": [\n" +
                "                    \"tokenUrl\",\n" +
                "                    \"scopes\"\n" +
                "                  ]\n" +
                "                },\n" +
                "                {\n" +
                "                  \"not\": {\n" +
                "                    \"required\": [\n" +
                "                      \"authorizationUrl\"\n" +
                "                    ]\n" +
                "                  }\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"authorizationCode\": {\n" +
                "              \"allOf\": [\n" +
                "                {\n" +
                "                  \"$ref\": \"#/definitions/oauth2Flow\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"required\": [\n" +
                "                    \"authorizationUrl\",\n" +
                "                    \"tokenUrl\",\n" +
                "                    \"scopes\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"additionalProperties\": false\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"oauth2Flow\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"authorizationUrl\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"tokenUrl\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"refreshUrl\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"uri\"\n" +
                "        },\n" +
                "        \"scopes\": {\n" +
                "          \"$ref\": \"#/definitions/oauth2Scopes\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"oauth2Scopes\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"openIdConnect\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"type\",\n" +
                "        \"openIdConnectUrl\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"type\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"openIdConnect\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"openIdConnectUrl\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"uri\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"patternProperties\": {\n" +
                "        \"^x-[\\\\w\\\\d\\\\.\\\\-\\\\_]+$\": {\n" +
                "          \"$ref\": \"#/definitions/specificationExtension\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"additionalProperties\": false\n" +
                "    },\n" +
                "    \"SecurityRequirement\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"uniqueItems\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        public static final String JSONSCHEMA = "http://json-schema.org/draft-07/schema#";
    }

    public enum SupportedHTTPVerbs {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS,
        TOOL
    }

    public static final String API_SUBTYPE_DIRECT_BACKEND = "DIRECT_BACKEND";
    public static final String API_SUBTYPE_EXISTING_API = "EXISTING_API";
}
