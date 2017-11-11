/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.threatprotection;

/**
 * Holds constants used in API gateway threat protector.
 */

public class ThreatProtectorConstants {

    public static final String CONTENT_TYPE = "synapse.internal.rest.contentType";
    public static final String HTTP_REQUEST_METHOD = "HTTP_METHOD_OBJECT";
    public static final String API_CONTEXT = "REST_API_CONTEXT";
    public static final String TRANSPORT_URL = "TransportInURL";

    /** Constants for handling threat protection exceptions */
    public static final String THREAT_FAILURE_HANDLER = "_threat_fault_";
    public static final String HTTP_HEADER_THREAT_CODE = "400-002";
    public static final String HTTP_HEADER_THREAT_MSG = "Threat detected in HTTP Headers";
    public static final String HTTP_HEADER_THREAT_DESC = "Check the API documentation and send proper HTTP Headers";
    public static final String QPARAM_THREAT_CODE = "400-002";
    public static final String QPARAM_THREAT_MSG = "Threat detected in Query Parameters";
    public static final String QPARAM_THREAT_DESC = "Check the API documentation and send proper Query Parameters";
    public static final String PAYLOAD_THREAT_CODE = "400-002";
    public static final String PAYLOAD_THREAT_MSG = "Threat detected in Payload";
    public static final String PAYLOAD_THREAT_DESC = "Check the API documentation and send proper Payload";
    public static final String THREAT_FOUND = "THREAT_FOUND";
    public static final String THREAT_CODE = "THREAT_CODE";
    public static final String THREAT_MSG = "THREAT_MSG";
    public static final String THREAT_DESC = "THREAT_DESC";

    public static final String P_MAX_ATTRIBUTES_PER_ELEMENT = "com.ctc.wstx.maxAttributesPerElement";
    public static final String P_MAX_ATTRIBUTE_SIZE = "com.ctc.wstx.maxAttributeSize";
    public static final String P_MAX_CHILDREN_PER_ELEMENT = "com.ctc.wstx.maxChildrenPerElement";
    public static final String P_MAX_ELEMENT_COUNT = "com.ctc.wstx.maxElementCount";
    public static final String P_MAX_ELEMENT_DEPTH = "com.ctc.wstx.maxElementDepth";
    public static final String P_MAX_ENTITY_COUNT = "com.ctc.wstx.maxEntityCount";
    public static final String ENABLED = "enabled";

    /** Constants for regex protector */
    public static final String REGEX_PATTERN = "regex";

    /** Constants for JSON protector */
    public static final String MAX_PROPERTY_COUNT = "maxPropertyCount";
    public static final String MAX_STRING_LENGTH = "maxStringLength";
    public static final String MAX_ARRAY_ELEMENT_COUNT = "maxArrayElementCount";
    public static final String MAX_LENGTH = "maxKeyLength";
    public static final String MAX_JSON_DEPTH = "maxJsonDepth";

    /** Constants for XML protector */
    public static final String DTD_ENABLED = "dtdEnabled";
    public static final String EXTERNAL_ENTITIES_ENABLED = "externalEntitiesEnabled";
    public static final String MAX_ELEMENT_COUNT = "maxElementCount";
    public static final String MAX_ATTRIBUT_COUNT = "maxAttributeCount";
    public static final String MAX_ATTRIBUTE_LENGTH = "maxAttributeLength";
    public static final String ENTITY_EXPANSTION_LIMIT = "entityExpansionLimit";
    public static final String CHILDREN_PER_ELEMENT = "maxChildrenPerElement";



}
