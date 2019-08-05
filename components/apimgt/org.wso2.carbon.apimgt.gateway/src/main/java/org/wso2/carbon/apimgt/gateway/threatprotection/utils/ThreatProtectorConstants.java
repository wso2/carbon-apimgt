/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.threatprotection.utils;

/**
 * Holds constants used in API gateway threat protector.
 */
public class ThreatProtectorConstants {

    public static final String CONTENT_TYPE = "synapse.internal.rest.contentType";
    public static final String SOAP_CONTENT_TYPE = "ContentType";
    public static final String HTTP_REQUEST_METHOD = "HTTP_METHOD_OBJECT";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String API_CONTEXT = "REST_API_CONTEXT";
    public static final String TEXT_XML = "text/xml";
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_JSON = "text/json";
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Constants for handling threat protection exceptions
     */
    public static final String STATUS = "STATUS";
    public static final String HTTP_SC = "HTTP_SC";
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String HTTP_HEADER_THREAT_CODE = "400-002";
    public static final String HTTP_SC_CODE = "400";
    public static final String P_MAX_ATTRIBUTES_PER_ELEMENT = "com.ctc.wstx.maxAttributesPerElement";
    public static final String P_MAX_ATTRIBUTE_SIZE = "com.ctc.wstx.maxAttributeSize";
    public static final String P_MAX_CHILDREN_PER_ELEMENT = "com.ctc.wstx.maxChildrenPerElement";
    public static final String P_MAX_ELEMENT_COUNT = "com.ctc.wstx.maxElementCount";
    public static final String P_MAX_ELEMENT_DEPTH = "com.ctc.wstx.maxElementDepth";
    public static final String P_MAX_ENTITY_COUNT = "com.ctc.wstx.maxEntityCount";
    public static final String REQUEST_BUFFER_SIZE = "RequestMessageBufferSize";
    public static final String XML = "XML";
    public static final String ORIGINAL = "Original";
    public static final String SCHEMA = "Schema";
    public static final String JSON = "JSON";

    /**
     * Constants for JSON protector
     */
    public static final String MAX_PROPERTY_COUNT = "maxPropertyCount";
    public static final String MAX_STRING_LENGTH = "maxStringLength";
    public static final String MAX_ARRAY_ELEMENT_COUNT = "maxArrayElementCount";
    public static final String MAX_KEY_LENGTH = "maxKeyLength";
    public static final String MAX_JSON_DEPTH = "maxJsonDepth";

    /**
     * Constants for XML protector
     */
    public static final String DTD_ENABLED = "dtdEnabled";
    public static final String EXTERNAL_ENTITIES_ENABLED = "externalEntitiesEnabled";
    public static final String MAX_XML_DEPTH = "maxXMLDepth";
    public static final String MAX_ELEMENT_COUNT = "maxElementCount";
    public static final String MAX_ATTRIBUTE_COUNT = "maxAttributeCount";
    public static final String MAX_ATTRIBUTE_LENGTH = "maxAttributeLength";
    public static final String ENTITY_EXPANSION_LIMIT = "entityExpansionLimit";
    public static final String CHILDREN_PER_ELEMENT = "maxChildrenPerElement";

    /**
     * Constants for schema validator
     */
    public static final String REST_CONTENT_TYPE = "ContentType";
    public static final String HTTP_RESPONSE_METHOD = "api.ut.HTTP_METHOD";
    public static final String SCHEMA_REFERENCE = "$ref";
    public static final String API_SWAGGER_SCHEMA = "swaggerSchemaCache";
    public static final String JSONPATH_PATHS = "$..paths..";
    public static final String JSON_PATH = "$.";
    public static final String JSONPATH_SEPRATER = ".";
    public static final String JSONPATH_RESPONSES = ".responses";
    public static final String COMPONENT_SCHEMAS = "components.schemas";
    public static final String EMPTY_ARRAY = "[]";
    public static final String HASH = "#";
    public static final String EMPTY = "";
    public static final String BACKWARD_SLASH = "\"";
    public static final String FORWARD_SLASH = "/";
    public static final String REQUESTBODY_SCHEMA = "components.requestBodies.";

}
