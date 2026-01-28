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
    public static final String ENDPOINT_CONFIG_SESSION_TIMEOUT = "sessionTimeOut";
    public static final String ENDPOINT_SECURITY_TYPE_AWS = "aws";
    public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_SERVICE_NAME = "bedrock";

    public enum SupportedHTTPVerbs {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS,
        TOOL;

        /**
         * Returns the SupportedHTTPVerbs enum constant corresponding to the given HTTP method string.
         *
         * @param method The HTTP method string (e.g., "GET", "POST").
         * @return The corresponding SupportedHTTPVerbs enum constant.
         * @throws IllegalArgumentException If the method does not match any of the enum constants.
         */
        public static SupportedHTTPVerbs fromValue(String method) {

            for (SupportedHTTPVerbs verb : values()) {
                if (verb.name().equalsIgnoreCase(method)) {
                    return verb;
                }
            }
            throw new IllegalArgumentException("Invalid HTTP verb: " + method);
        }
    }


    public static class AIAPIConstants {
        public static final String API_KEY_AUTHENTICATION_TYPE = "apikey";
        public static final String API_KEY_HEADER_ENABLED = "headerEnabled";
        public static final String API_KEY_QUERY_PARAMETER_ENABLED = "queryParameterEnabled";
        public static final String API_KEY_HEADER_NAME = "headerName";
        public static final String API_KEY_QUERY_PARAMETER_NAME = "queryParameterName";
        public static final String AWS_AUTHENTICATION_TYPE = "aws";
        public static final String AWS_AUTHENTICATION_ACCESS_KEY_ID = "accessKey";
        public static final String AWS_AUTHENTICATION_SECRET_KEY = "secretKey";
        public static final String AWS_AUTHENTICATION_REGION = "region";
        public static final String AWS_AUTHENTICATION_SERVICE_NAME = "service";
        public static final String AI_API_SUB_TYPE = "AIAPI";
        public static final int MILLISECONDS_IN_SECOND = 1000;
        public static final String LLM_PROVIDERS = "llmProviders";
        public static final String API_KEY_IDENTIFIER_TYPE_HEADER = "HEADER";
        public static final String API_KEY_IDENTIFIER_TYPE_QUERY_PARAMETER = "QUERY_PARAMETER";
        public static final String AI_API_REQUEST_METADATA = "AI_API_REQUEST_METADATA";
        public static final String AI_API_RESPONSE_METADATA = "AI_API_RESPONSE_METADATA";
        public static final String INPUT_SOURCE_PAYLOAD = "payload";
        public static final String INPUT_SOURCE_HEADER = "header";
        public static final String CONNECTOR_TYPE = "connectorType";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String API_VERSION = "apiVersion";
        public static final String LLM_CONFIGS_ENDPOINT = "/llm-providers";
        public static final String CONFIGURATIONS = "configurations";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_NAME = "AzureOpenAI";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_VERSION_V2 = "2.0.0";
        public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_CONNECTOR = "azureOpenAi_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_CONNECTOR_V2 = "azureOpenAi_2.0.0";
        public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_CONNECTOR = "awsBedrock_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_NAME = "AWSBedrock";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_KEY = "api-key";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_API_DEFINITION_FILE_NAME = "azure_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_API_DEFINITION_FILE_NAME_V2 = "azure_openai_api_v2.yaml";
        public static final String LLM_PROVIDER_SERVICE_AZURE_OPENAI_DESCRIPTION = "Azure OpenAI service";
        public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_DESCRIPTION = "AWS Bedrock service";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_NAME = "OpenAI";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_VERSION = "2.0.0";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_CONNECTOR = "openAi_2.0.0";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_KEY = "Authorization";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_API_DEFINITION_FILE_NAME = "openai_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_OPENAI_DESCRIPTION = "OpenAI service";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_NAME = "MistralAI";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_CONNECTOR = "mistralAi_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_KEY = "Authorization";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_API_DEFINITION_FILE_NAME = "mistral_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_MISTRALAI_DESCRIPTION = "Mistral AI service";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_NAME = "Anthropic";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_CONNECTOR = "anthropic_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_KEY = "x-api-key";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_API_DEFINITION_FILE_NAME = "anthropic_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_ANTHROPIC_CLAUDE_DESCRIPTION = "Anthropic Claude LLM Service";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_NAME = "AzureAIFoundry";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_CONNECTOR = "azureAiFoundry_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_DESCRIPTION = "Azure AI Foundry service";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_API_DEFINITION_FILE_NAME = "azure_ai_foundry_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_AZURE_AI_FOUNDRY_KEY = "api-key";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_NAME = "Gemini";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_VERSION = "1.0.0";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_CONNECTOR = "gemini_1.0.0";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_DESCRIPTION = "Gemini service";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_API_DEFINITION_FILE_NAME = "gemini_api.yaml";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_KEY = "X-goog-api-key";
        public static final String LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL = "requestModel";
        public static final String LLM_PROVIDER_SERVICE_METADATA_RESPONSE_MODEL = "responseModel";
        public static final String LLM_PROVIDER_SERVICE_METADATA_MODEL = "model";
        public static final String LLM_PROVIDER_SERVICE_METADATA_PROMPT_TOKEN_COUNT = "promptTokenCount";
        public static final String LLM_PROVIDER_SERVICE_METADATA_COMPLETION_TOKEN_COUNT = "completionTokenCount";
        public static final String LLM_PROVIDER_SERVICE_METADATA_TOTAL_TOKEN_COUNT = "totalTokenCount";
        public static final String LLM_PROVIDER_SERVICE_METADATA_REMAINING_TOKEN_COUNT = "remainingTokenCount";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_MODEL = "$.model";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT = "$.usage" +
                ".prompt_tokens";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_INPUT_TOKEN = "$.usage" +
                ".input_tokens";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_OUTPUT_TOKEN = "$.usage" +
                ".output_tokens";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_ANTHROPIC_REMAINING_TOKEN_COUNT = "anthropic-ratelimit-tokens-remaining";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_COMPLETION_TOKEN_COUNT = "$.usage" +
                ".completion_tokens";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT = "$.usage.total_tokens";
        public static final String LLM_PROVIDER_SERVICE_METADATA_IDENTIFIER_REMAINING_TOKEN_COUNT = "x-ratelimit-remaining-tokens";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_REQUEST_METADATA_IDENTIFIER_MODEL =
                "(?<=models/)[a-zA-Z0-9.\\-]+";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT =
                "$.usageMetadata.promptTokenCount";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_CANDIDATES_TOKEN_COUNT =
                "$.usageMetadata.candidatesTokenCount";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT =
                "$.usageMetadata.totalTokenCount";
        public static final String LLM_PROVIDER_SERVICE_GEMINI_RESPONSE_METADATA_IDENTIFIER_MODEL =
                "$.modelVersion";
        public static final String LLM_PROVIDER_SERVICE_DEFAULT = "default";
        public static final String NULL = "null";
        public static final String LLM_PROVIDER = "LLM_PROVIDER";
        public static final String LLM_PROVIDER_TENANT_ALL = "ALL";

        public static final String TRAFFIC_FLOW_DIRECTION_IN = "IN";
        public static final String TRAFFIC_FLOW_DIRECTION_OUT = "OUT";
        public static final String API_LLM_ENDPOINT = "_API_LLMEndpoint_";
        public static final String ROUND_ROBIN_CONFIGS = "ROUND_ROBIN_CONFIGS";
        public static final String INTELLIGENT_MODEL_ROUTING_CONFIGS = "INTELLIGENT_MODEL_ROUTING_CONFIGS";
        public static final String SEMANTIC_ROUTING_CONFIGS = "SEMANTIC_ROUTING_CONFIGS";
        public static final String ROUTING_CONFIGS = "ROUTING_CONFIGS";
        public static final String FAILOVER_CONFIGS = "FAILOVER_CONFIGS";
        public static final String TARGET_MODEL_ENDPOINT = "TARGET_MODEL_ENDPOINT";
        public static final String TARGET_ENDPOINT = "TARGET_ENDPOINT";
        public static final String FAILOVER_TARGET_MODEL_ENDPOINT = "FAILOVER_TARGET_MODEL_ENDPOINT";
        public static final String FAILOVER_CONFIG_MAP = "FAILOVER_CONFIG_MAP";
        public static final String SUSPEND_DURATION = "SUSPEND_DURATION";
        public static final String FAILOVER_ENDPOINTS = "FAILOVER_ENDPOINTS";
        public static final String REJECT_ENDPOINT = "REJECT";
        public static final String DEFAULT_ENDPOINT = "DEFAULT";
        public static final String EXIT_ENDPOINT = "EXIT";
        public static final String REQUEST_PAYLOAD = "REQUEST_PAYLOAD";
        public static final String REQUEST_HEADERS = "REQUEST_HEADERS";
        public static final String REQUEST_HTTP_METHOD = "REQUEST_HTTP_METHOD";
        public static final String REQUEST_REST_URL_POSTFIX = "REQUEST_REST_URL_POSTFIX";
        public static final String CURRENT_ENDPOINT_INDEX = "CURRENT_ENDPOINT_INDEX";
        public static final String DEFAULT_PRODUCTION_ENDPOINT_NAME = "DEFAULT PRODUCTION ENDPOINT";
        public static final String DEFAULT_SANDBOX_ENDPOINT_NAME = "DEFAULT SANDBOX ENDPOINT";
        public static final String ENDPOINT_SEQUENCE = "_EndpointsSeq";
        public static final String REQUEST_TIMEOUT = "REQUEST_TIMEOUT";
        public static final String HTTP_PROTOCOL_TYPE = "HTTP";
        public static final String LLM_PROVIDER_SERVICE_AWSBEDROCK_OPENAI_API_DEFINITION_FILE_NAME =
                "aws_bedrock_api.yaml";
        public static final String INPUT_SOURCE_PATH = "pathParams";
        public static final String LLM_PROVIDER_SERVICE_AWS_BEDROCK_METADATA_IDENTIFIER_MODEL =
                "(?<=model/)[a-zA-Z0-9.:-]+(?=/)";
        public static final String LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_PROMPT_TOKEN_COUNT =
                "$.usage.inputTokens";
        public static final String LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_COMPLETION_TOKEN_COUNT =
                "$.usage.outputTokens";
        public static final String LLM_PROVIDER_SERVICE_AWS_METADATA_IDENTIFIER_TOTAL_TOKEN_COUNT =
                "$.usage.totalTokens";
        public static final String LLM_MODEL_PROVIDER_AWS_BEDROCK_ANTHROPIC = "Anthropic";
        public static final String LLM_MODEL_PROVIDER_AWS_BEDROCK_DEEPSEEK = "DeepSeek";
        public static final String LLM_MODEL_PROVIDER_AWS_BEDROCK_META = "Meta";
        public static final String LLM_MODEL_PROVIDER_AZURE_FOUNDRY_OPENAI = "OpenAI";
        public static final String LLM_MODEL_PROVIDER_AZURE_FOUNDRY_COHERE = "Cohere";
        public static final String LLM_MODEL_PROVIDER_AZURE_FOUNDRY_XAI = "xAI";
    }

    public static class UnifiedSearchConstants {
        public static final String QUERY_API_TYPE_APIS_PUBLISHER = "type:HTTP type:WS type:SOAPTOREST type:GRAPHQL " +
                "type:SOAP type:SSE type:WEBSUB type:WEBHOOK type:ASYNC";
        public static final String QUERY_API_TYPE_APIS_DEVPORTAL = "type:HTTP type:WS type:SOAPTOREST type:GRAPHQL " +
                "type:SOAP type:SSE type:WEBSUB type:WEBHOOK type:ASYNC type:APIProduct";
        public static final String QUERY_API_TYPE_MCP = "type:MCP";
    }
}
