/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
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
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String HOST_NAME = "api.ut.hostName";
    public static final String API_PUBLISHER = "api.ut.apiPublisher";
    public static final String API_SWAGGER = "API_SWAGGER";
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
    public static final String REQUEST_EXECUTION_START_TIME ="request.execution.start.time";
    public static final String SYNAPSE_ENDPOINT_ADDRESS = "ENDPOINT_ADDRESS";

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
    public static final String OTHER_LATENCY = "other_latency";
    public static final String AM_CORRELATION_ID = "am.correlationID";

    /**
     * Constants for regex protector.
     */
    public static final String REGEX_PATTERN = "regex";
    public static final String ENABLED_CHECK_BODY = "enabledCheckBody";
    public static final String ENABLED_CHECK_PATHPARAM = "enabledCheckPathParams";
    public static final String ENABLED_CHECK_HEADERS  = "enabledCheckHeaders";
    public static final String REST_URL_POSTFIX = "REST_URL_POSTFIX";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String REGEX_THREAT_PROTECTOR_ENABLED_TENANTS = "regexThreatProtectorEnabledTenants";
    public static final String PAYLOAD_SIZE_LIMIT_FOR_REGEX_TREAT_PROTECTOR = "payloadSizeLimitForRegexThreatProtector";

    /**
     * Constants for handling threat protection exceptions.
     */
    public static final String HTTP_SC_CODE = "400";
    public static final String HTTP_HEADER_THREAT_MSG = "Threat detected in HTTP Headers";
    public static final String QPARAM_THREAT_MSG = "Threat detected in Query Parameters";
    public static final String PAYLOAD_THREAT_MSG = "Threat detected in Payload";
    public static final String THREAT_FOUND = "THREAT_FOUND";
    public static final String THREAT_CODE = "THREAT_CODE";
    public static final String THREAT_MSG = "THREAT_MSG";
    public static final String THREAT_DESC = "THREAT_DESC";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String THREAT_TYPE = "threatType";
    public static final String THREAT_FAULT = "_threat_fault_";
    public static final String XML_VALIDATION = "xmlValidation";
    public static final String SCHEMA_VALIDATION = "schemaValidation";
    public static final String XSD_URL = "xsdURL";
    public static final String UTF8 = "UTF-8";

    /**
     * Web socket header for jwt assertion.
     * */
    public static final String WS_JWT_TOKEN_HEADER = "websocket.custom.header.X-JWT-Assertion";
    
    public static final String GATEWAY_TYPE = "SYNAPSE";
    public static final String SYNAPDE_GW_LABEL = "Synapse";
    public static final String CLIENT_USER_AGENT = "clientUserAgent";
    public static final String CLIENT_IP = "clientIp";

    /**
     * Constants for Open Tracing
     * */
    public static final String SERVICE_NAME = "API:Latency";
    public static final String RESPONSE_LATENCY = "API:Response_Latency";
    public static final String BACKEND_LATENCY_SPAN = "API:Backend_Latency";
    public static final String KEY_VALIDATION = "API:Key_Validation_Latency";
    public static final String REQUEST_MEDIATION = "API:Request_Mediation_Latency";
    public static final String RESPONSE_MEDIATION = "API:Response_Mediation_Latency";
    public static final String GET_RESOURCE_AUTHENTICATION_SCHEME = "API:Get_Resource_Authentication_Scheme()";
    public static final String KEY_VALIDATION_FROM_GATEWAY_NODE = "API:Key_Validation_From_Gateway_Node";
    public static final String GET_CLIENT_DOMAIN = "API:Get_Client_Domain()";
    public static final String GET_KEY_VALIDATION_INFO = "API:Get_Key_Validation_Info()";
    public static final String FIND_MATCHING_VERB = "API:Find_matching_verb()";
    public static final String CORS_REQUEST_HANDLER = "API:CORS_Request_Handler";
    public static final String THROTTLE_LATENCY = "API:Throttle_Latency";
    public static final String DO_GET_API_INFO_DTO = "API:Do_Get_API_Info_dto()";
    public static final String API_MGT_RESPONSE_HANDLER = "API:API_MGT_Response_Handler";
    public static final String GOOGLE_ANALYTICS_HANDLER = "API:Google_Analytics_Handler";
    public static final String API_MGT_USAGE_HANDLER = "API:API_Mgt_Usage_Handler";
    public static final String GET_ALL_URI_TEMPLATES = "API:GET_ALL_URI_TEMPLATES()";
    public static final String TRACING_ENABLED = "OpenTracer.Enabled";
    public static final String SPAN_KIND = "span.kind";
    public static final String SERVER = "server";
    public static final String ERROR = "error";
    public static final String KEY_SPAN_ERROR = "API Authentication Failure";
    public static final String RESPONSE_MEDIATION_ERROR = "Error in Response Mediation";
    public static final String REQUEST_MEDIATION_ERROR = "Error in Request Mediation";
    public static final String RESOURCE_AUTH_ERROR = "Error in Resource Authentication";
    public static final String THROTTLE_HANDLER_ERROR = "Error in Throttle Handler";
    public static final String API_THROTTLE_HANDLER_ERROR = "Error in API Throttle Handler";
    public static final String CORS_REQUEST_HANDLER_ERROR = "Error in CORS_Request Handler";
    public static final String API_KEY_VALIDATOR_ERROR = "Error while accessing backend services for API key validation";
    public static final String GOOGLE_ANALYTICS_ERROR = "Error in Google Analytics Handler";
}

