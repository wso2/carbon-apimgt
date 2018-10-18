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
    public static final String RESOURCE = "api.ut.resource";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String HOST_NAME = "api.ut.hostName";
    public static final String API_PUBLISHER = "api.ut.apiPublisher";
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
    
}

