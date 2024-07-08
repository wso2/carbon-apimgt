/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway;

public class APIMgtGatewayConstants {

    public static final String CONSUMER_KEY = "api.ut.consumerKey";
    public static final String USER_ID = "api.ut.userId";
    public static final String CONTEXT = "api.ut.context";
    public static final String API_VERSION = "api.ut.api_version";
    public static final String API = "api.ut.api";
    public static final String VERSION = "api.ut.version";
    public static final String API_TYPE = "api.ut.api_type";
    public static final String RESOURCE = "api.ut.resource";
    public static final String API_STATUS = "api.ut.status";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String HOST_NAME = "api.ut.hostName";
    public static final String API_PUBLISHER = "api.ut.apiPublisher";
    public static final String OPEN_API_OBJECT = "OPEN_API_OBJECT";
    public static final String OPEN_API_STRING = "OPEN_API_STRING";
    public static final String APPLICATION_NAME = "api.ut.application.name";
    public static final String APPLICATION_ID = "api.ut.application.id";
    public static final String REQUEST_START_TIME = "api.ut.requestTime";
    public static final String BACKEND_REQUEST_START_TIME = "api.ut.backendRequestTime";
    public static final String BACKEND_REQUEST_END_TIME = "api.ut.backendRequestEndTime";
    public static final String END_USER_NAME = "api.ut.userName";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String REQUEST_RECEIVED_TIME = "wso2statistics.request.received.time";
    public static final String AUTHORIZATION = "Authorization";
    public static final String REVOKED_ACCESS_TOKEN = "RevokedAccessToken";
    public static final String DEACTIVATED_ACCESS_TOKEN = "DeactivatedAccessToken";
    public static final String SCOPES = "Scopes";
    public static final String JWT_CLAIMS = "jwt_token_claims";
    public static final String REQUEST_EXECUTION_START_TIME = "request.execution.start.time";
    public static final String SYNAPSE_ENDPOINT_ADDRESS = "ENDPOINT_ADDRESS";
    public static final String DUMMY_ENDPOINT_ADDRESS = "dummy_endpoint_address";
    public static final String CUSTOM_PROPERTY = "customProperty";
    public static final String RESOURCE_PATTERN = "^/.+?/.+?([/?].+)$";

    public static final String METHOD_NOT_FOUND_ERROR_MSG = "Method not allowed for given API resource";
    public static final String RESOURCE_NOT_FOUND_ERROR_MSG = "No matching resource found for given API Request";
    public static final String REQUEST_TYPE_FAIL_MSG = "Neither request method nor content type is matched with" +
            " the validator.";

    public static final String BACKEND_LATENCY = "backend_latency";
    public static final String SECURITY_LATENCY = "security_latency";
    public static final String THROTTLING_LATENCY = "throttling_latency";
    public static final String REQUEST_MEDIATION_LATENCY = "request_mediation_latency";
    public static final String RESPONSE_MEDIATION_LATENCY = "response_mediation_latency";
    public static final String AM_CORRELATION_ID = "am.correlationID";
    public static final String REFERER = "Referer";

    /**
     * Constants for regex protector.
     */
    public static final String REGEX_PATTERN = "regex";
    public static final String ENABLED_CHECK_BODY = "enabledCheckBody";
    public static final String ENABLED_CHECK_PATHPARAM = "enabledCheckPathParams";
    public static final String ENABLED_CHECK_HEADERS = "enabledCheckHeaders";
    public static final String REST_URL_POSTFIX = "REST_URL_POSTFIX";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String REGEX_THREAT_PROTECTOR_ENABLED_TENANTS = "regexThreatProtectorEnabledTenants";
    public static final String PAYLOAD_SIZE_LIMIT_FOR_REGEX_TREAT_PROTECTOR = "payloadSizeLimitForRegexThreatProtector";

    /**
     * Constants for handling threat protection exceptions.
     */
    public static final String HTTP_SC_CODE = "400";
    public static final String HTTP_SC = "HTTP_SC";
    public static final String HTTP_HEADER_THREAT_MSG = "Threat detected in HTTP Headers";
    public static final String QPARAM_THREAT_MSG = "Threat detected in Query Parameters";
    public static final String PAYLOAD_THREAT_MSG = "Threat detected in Payload";
    public static final String THREAT_FOUND = "THREAT_FOUND";
    public static final String THREAT_CODE = "THREAT_CODE";
    public static final String THREAT_MSG = "THREAT_MSG";
    public static final String THREAT_DESC = "THREAT_DESC";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String BAD_RESPONSE = "Bad Response";
    public static final String THREAT_TYPE = "threatType";
    public static final String THREAT_FAULT = "_threat_fault_";
    public static final String XML_VALIDATION = "xmlValidation";
    public static final String SCHEMA_VALIDATION = "schemaValidation";
    public static final String XSD_URL = "xsdURL";
    public static final String UTF8 = "UTF-8";

    /**
     * Web socket header for jwt assertion.
     */
    public static final String WS_JWT_TOKEN_HEADER = "websocket.custom.header.X-JWT-Assertion";

    public static final String WS_CUSTOM_HEADER = "ws.custom.header";
    public static final String WS_NOT_SECURED = "ws";
    public static final String WS_SECURED = "wss";

    public static final String GATEWAY_TYPE = "SYNAPSE";

    /**
     * Constants for Open Tracing
     */
    public static final String SERVICE_NAME = "API:Gateway";
    public static final String RESPONSE_LATENCY = "API:Response_Latency";
    public static final String BACKEND_LATENCY_SPAN = "API:Backend_Latency";
    public static final String KEY_VALIDATION = "API:Key_Validation_Latency";
    public static final String REQUEST_MEDIATION = "API:Request_Mediation_Latency";
    public static final String RESPONSE_MEDIATION = "API:Response_Mediation_Latency";
    public static final String CORS_REQUEST_HANDLER = "API:CORS_Request_Latency";
    public static final String THROTTLE_LATENCY = "API:Throttle_Latency";
    public static final String GOOGLE_ANALYTICS_HANDLER = "API:Google_Analytics_Latency";
    public static final String SPAN_KIND = "span.kind";
    public static final String SPAN_REQUEST_PATH = "span.request.path";
    public static final String SPAN_REQUEST_METHOD = "span.request.method";
    public static final String SERVER = "server";
    public static final String ERROR = "error";
    public static final String KEY_SPAN_ERROR = "API Authentication Failure";
    public static final String RESPONSE_MEDIATION_ERROR = "Error in Response Mediation";
    public static final String REQUEST_MEDIATION_ERROR = "Error in Request Mediation";
    public static final String THROTTLE_HANDLER_ERROR = "Error in Throttle Handler";
    public static final String API_THROTTLE_HANDLER_ERROR = "Error in API Throttle Handler";
    public static final String CORS_REQUEST_HANDLER_ERROR = "Error in CORS_Request Handler";
    public static final String CORS_FORBID_BLOCKED_REQUESTS = "corsForbidBlockedRequests";
    public static final String CORS_SET_STATUS_CODE_FROM_MSG_CONTEXT = "corsSetStatusCodeFromMsgContext";
    public static final String GOOGLE_ANALYTICS_ERROR = "Error in Google Analytics Handler";
    public static final String CUSTOM_ANALYTICS_REQUEST_PROPERTIES = "apim.analytics.request.properties";
    public static final String CUSTOM_ANALYTICS_RESPONSE_PROPERTIES = "apim.analytics.response.properties";
    public static final String CUSTOM_ANALYTICS_PROPERTY_SEPARATOR = ",";
    public static final String API_UUID_PROPERTY = "API_UUID";
    public static final String TENANT_DOMAIN = "tenant.info.domain";

    /**
     * Constants for swagger schema validator
     */
    public static final String API_ELECTED_RESOURCE = "API_ELECTED_RESOURCE";
    public static final String ELECTED_REQUEST_METHOD = "api.ut.HTTP_METHOD";
    public static final String HTTP_REQUEST_METHOD = "HTTP_METHOD_OBJECT";
    public static final String REST_CONTENT_TYPE = "ContentType";
    public static final String REST_MESSAGE_TYPE = "messageType";
    public static final String SCHEMA_VALIDATION_REPORT = "schema-validation-report";


    /**
     * Constants for trust store access
     */
    public static final String HTTP_RESPONSE_STATUS_CODE = "HTTP_RESPONSE_STATUS_CODE";
    public static final String BASE64_ENCODED_CLIENT_CERTIFICATE_HEADER = "X-WSO2-CLIENT-CERTIFICATE";
    public static final String SPAN_ACTIVITY_ID = "span.activity.id";
    public static final String SPAN_RESOURCE = "span.resource";
    public static final String SPAN_API_NAME = "span.api.name";
    public static final String SPAN_API_VERSION = "span.api.version";
    public static final String SPAN_APPLICATION_CONSUMER_KEY = "span.consumerkey";
    public static final String SPAN_ENDPOINT = "span.endpoint";
    public static final String SPAN_HTTP_RESPONSE_STATUS_CODE = "span.http.response.status.code";
    public static final String SPAN_HTTP_RESPONSE_STATUS_CODE_DESCRIPTION =
            "span.http.response.status.code.description";

    public static final String INTERNAL_KEY = "Internal-Key";

    /**
     * Synapse Properties related Constants
     */
    public static final String HOST = "Host";
    public static final String HOST_HEADER = "HostHeader";
    public static final String API_OBJECT = "API";
    public static final String OAUTH_ENDPOINT_INSTANCE = "oauth.instance";
    public static final String VALIDATED_X509_CERT = "ValidatedX509Cert";
    public static final String RESOURCE_SPAN = "API:Resource";

    /**
     * Web-sub related properties
     */
    public static final String SUBSCRIBER_LINK_HEADER_HUB = "; rel=\"hub\", ";
    public static final String SUBSCRIBER_LINK_HEADER_SELF = "; rel=\"self\" ";

    public static final String SUBSCRIBER_LINK_HEADER_PROPERTY = "SUBSCRIBER_LINK_HEADER";

    //This will be a reserved name for the synapse message context properties.
    public static final String ADDITIONAL_ANALYTICS_PROPS = "ADDITIONAL_ANALYTICS_PROPS_TO_PUBLISH";

    public static final String AZP_JWT_CLAIM = "azp";
    public static final String ENTITY_ID_JWT_CLAIM = "entity_id";

    public static final String ACCESS_GRANT_CLAIM_NAME = "grantVerificationClaim";
    public static final String ACCESS_GRANT_CLAIM_VALUE = "grantVerificationClaimValue";
    public static final String SHOULD_ALLOW_ACCESS_VALIDATION = "shouldAllowValidation";
}

