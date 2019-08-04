/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.exception.APIManagementException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * This SchemaValidator handler validates the request/response messages against schema defined in the swagger.
 */
public class SchemaValidator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private String uuid;
    private String swagger = null;
    private JsonNode rootNode;
    private String requestMethod;
    private String schemaContent = null;

    public void setLocalentry(String localEntry) {
        this.uuid = localEntry;
    }

    /**
     * Validate request message body.
     *
     * @param messageContext Context of the message.
     * @return Continue true the handler chain.
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.info("Validating the API request Body content..");
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String contentType;
        Object objContentType = axis2MC.getProperty(ThreatProtectorConstants.REST_CONTENT_TYPE);
        if (objContentType != null) {
            contentType = objContentType.toString();
            try {
                RelayUtils.buildMessage(axis2MC);
                if (ThreatProtectorConstants.APPLICATION_JSON.equals(contentType)) {
                    try {
                        initialize(messageContext);
                    } catch (APIManagementException e) {
                        logger.error("Error occurred while initializing the swagger elements", e);
                    }
                    if (swagger != null) {
                        requestMethod = messageContext.getProperty(
                                APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
                        if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(requestMethod)) {
                            try {
                                validateRequest(messageContext);
                            } catch (APIManagementException e) {
                                logger.error("Error occurred while validating the API request", e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error occurred while building the API request", e);
            } catch (XMLStreamException e) {
                logger.error("Error occurred while validating the API request", e);
            }
        }
        return true;
    }

    /**
     * Validate the response message.
     * @param messageContext Context of the API response.
     * @return Continue true the handler chain.
     */
    @Override
    public boolean handleResponse(MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.info("Validating the API response  Body content..");
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axis2MC);
        } catch (IOException e) {
           logger.error("Error occurred while building the API response", e);
        } catch (XMLStreamException e) {
           logger.error("Error occurred while validating the API response", e);
        }
        Object objectResponse = axis2MC.getProperty(ThreatProtectorConstants.REST_CONTENT_TYPE);
        if (objectResponse != null) {
            String contentType = objectResponse.toString();
            requestMethod = messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
            if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(requestMethod) &&
                    ThreatProtectorConstants.APPLICATION_JSON.equals(contentType)) {
                try {
                    validateResponse(messageContext);
                } catch (APIManagementException e) {
                    logger.error("Error occurred while validating the API response", e);
                }
            }
        }
        return true;
    }

    /**
     * Initialize the swagger from the Local Entry.
     *
     * @param messageContext Content of the message context.
     */
    private void initialize(MessageContext messageContext) throws APIManagementException {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing the swagger from localEntry");
        }
        Entry localEntryObj;
        ObjectMapper objectMapper = new ObjectMapper();
        if (uuid != null) {
            localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(uuid);
            if ((!messageContext.isResponse()) && (localEntryObj != null)) {
                swagger = localEntryObj.getValue().toString();
                if (swagger != null) {
                    try {
                        rootNode = objectMapper.readTree(swagger.getBytes());
                    } catch (IOException e) {
                        throw new APIManagementException("Error occurred while converting the Swagger" +
                                " into JsonNode", e);
                    }
                }
            }
        }
    }

    /**
     * Validate the Request/response content.
     *
     * @param payloadObject  Request/response payload.
     * @param schemaString   Schema which uses to validate request/response messages.
     * @param messageContext Message context.
     */
    private void validateContent(JSONObject payloadObject, String schemaString, MessageContext messageContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validating JSON content against the schema");
        }
        JSONObject jsonSchema = new JSONObject(schemaString);
        Schema schema = SchemaLoader.load(jsonSchema);
        if (schema != null) {
            try {
                schema.validate(payloadObject);
            } catch (ValidationException e) {
                if (messageContext.isResponse()) {
                    logger.error("Schema validation failed in the Response :" + e.getMessage(), e);
                } else {
                    logger.error("Schema validation failed in the Request :" + e.getMessage(), e);
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
            }
        }
    }

    /**
     * Validate the API Request JSON Body.
     *
     * @param messageContext Message context.
     */
    private void validateRequest(MessageContext messageContext) throws APIManagementException {
        String schema = getSchemaContent(messageContext);
        JSONObject payloadObject = getMessageContent(messageContext);
        if (schema != null && !schema.equals("")) {
            validateContent(payloadObject, schema, messageContext);
        }
    }

    /**
     * Validate the API Response Body  which comes from the BE.
     *
     * @param messageContext Message context.
     */
    private void validateResponse(MessageContext messageContext) throws APIManagementException {
        String responseSchema = getSchemaContent(messageContext);
        JSONObject payloadObject = getMessageContent(messageContext);
        if (responseSchema != null && !responseSchema.equals(ThreatProtectorConstants.EMPTY_ARRAY) &&
                payloadObject != null && !payloadObject.equals(ThreatProtectorConstants.EMPTY_ARRAY)) {
            validateContent(payloadObject, responseSchema, messageContext);
        }
    }

    /**
     * Get the Request/Response messageContent as a JsonObject.
     *
     * @param messageContext Message context.
     * @return JsonObject which contains the request/response message content.
     */
    private JSONObject getMessageContent(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String requestMethod;
        if (messageContext.isResponse()) {
            requestMethod = messageContext.getProperty(ThreatProtectorConstants.HTTP_RESPONSE_METHOD).toString();
        } else {
            requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();

        }
        JSONObject payloadObject = null;
        if (!APIConstants.SupportedHTTPVerbs.GET.name().equalsIgnoreCase(requestMethod) && messageContext.getEnvelope().
                getBody() != null) {
            if (messageContext.getEnvelope().getBody().getFirstElement() != null) {
                OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
                try {
                    payloadObject = new JSONObject(JsonUtil.toJsonString(xmlResponse).toString());
                } catch (AxisFault axisFault) {
                    logger.error(" Error occurred while converting the String payload to Json");
                }
            }
        }
        return payloadObject;
    }

    /**
     * Extract the schema Object.
     * @param refNode JSON node to be extracted.
     * @return Extracted schema.
     */
    private JsonNode extractSchemaObject(JsonNode refNode) {
        String[] val = refNode.toString().split("#");
        String path = val[1].replace("\\{^\"|\"}", "").replaceAll("\"", "");
        return rootNode.at(path);
    }

    /**
     * Get the relevant schema content for the  particular request/response messages.
     *
     * @param messageContext Message content.
     * @return particular schema content with its schema initialization pattern(key/schema)
     */
    private String getSchemaContent(MessageContext messageContext) throws APIManagementException {
        String schemaKey;
        if (!messageContext.isResponse()) {
            schemaKey = extractSchemaFromRequest(messageContext);
        } else {
            schemaKey = extractResponse(messageContext);
        }
        return schemaKey;
    }

    /**
     * Extract the relevant schema from the request.
     *
     * @param messageContext Message Context
     * @return String Schema
     */
    private String extractSchemaFromRequest(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        StringBuilder jsonPath = new StringBuilder();

        String resourcePath = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();
        String schema;
        String value = JsonPath.read(swagger, ThreatProtectorConstants.JSON_PATH + ".openapi").toString();
        if (value != null && !value.equals(ThreatProtectorConstants.EMPTY_ARRAY)) {
            //refer schema
            schema = JsonPath.read(swagger, jsonPath.append(ThreatProtectorConstants.JSONPATH_PATHS)
                    .append(resourcePath).append("..requestBody.content.application/json.schema").toString());

            if (schema == null | ThreatProtectorConstants.EMPTY_ARRAY.equals(schema)) {
                // refer request bodies
                schema = JsonPath.read(swagger, ThreatProtectorConstants.JSONPATH_PATHS + resourcePath +
                        ThreatProtectorConstants.JSONPATH_SEPRATER + requestMethod.toLowerCase()
                        + "..requestBody").toString();
            }
        } else {
            schema = JsonPath.read(swagger, ThreatProtectorConstants.JSONPATH_PATHS + resourcePath +
                    ThreatProtectorConstants.JSONPATH_SEPRATER + requestMethod.toLowerCase()
                    + ".parameters..schema").toString();
        }
        String content = extractReference(schema);
        return content;
    }

    /**
     * Extract the response schema from swagger according to the response code.
     *
     * @param messageContext message content
     * @return response schema
     * @throws APIManagementException wrap and throw IOException
     */
    private String extractResponse(MessageContext messageContext) throws APIManagementException {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String reqMethod = messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
        String responseStatus = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_SC).toString();
        String Swagger = swagger;
        Object resourceSchema;
        Object resource;
        String nonSchema = "NonSchema";
        String value;

        resource = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS +
                electedResource + ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                ThreatProtectorConstants.JSONPATH_RESPONSES + responseStatus);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(resource, JsonNode.class);

        if (jsonNode.get(0) != null && !ThreatProtectorConstants.EMPTY_ARRAY.equals(jsonNode)) {
            value = jsonNode.get(0).toString();
        } else {
            value = jsonNode.toString();
        }
        if (value != null) {
            resource = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS + electedResource +
                    ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                    ThreatProtectorConstants.JSONPATH_RESPONSES +
                    responseStatus + ".schema");

            JsonNode json = mapper.convertValue(resource, JsonNode.class);
            if (json.get(0) != null && !ThreatProtectorConstants.EMPTY_ARRAY.equals(json.get(0))) {
                value = json.get(0).toString();
            } else {
                value = json.toString();
            }
            if (value != null && !ThreatProtectorConstants.EMPTY_ARRAY.equals(value)) {
                if (value.contains(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                    byte[] bytes = value.getBytes();
                    try {
                        JsonNode node = mapper.readTree(bytes);
                        Iterator<JsonNode> schemaNode = node.findParent(
                                ThreatProtectorConstants.SCHEMA_REFERENCE).elements();
                        return extractRef(schemaNode);

                    } catch (IOException e) {
                        throw new APIManagementException("Error occurred while converting bytes from json node");
                    }
                } else {
                    return value;
                }

            } else {
                resourceSchema = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS
                        + electedResource + ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                        ".responses.default");

                JsonNode jnode = mapper.convertValue(resourceSchema, JsonNode.class);
                if (jnode.get(0) != null && !ThreatProtectorConstants.EMPTY_ARRAY.equals(jnode)) {
                    value = jnode.get(0).toString();
                } else {
                    value = jnode.toString();
                }
                if (resourceSchema != null) {
                    if (value.contains(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                        byte[] bytes = value.getBytes();
                        try {
                            JsonNode node = mapper.readTree(bytes);
                            if (node != null) {
                                Iterator<JsonNode> schemaNode = node.findParent(
                                        ThreatProtectorConstants.SCHEMA_REFERENCE).elements();
                                return extractRef(schemaNode);
                            }

                        } catch (IOException e) {
                            throw new APIManagementException(e);
                        }
                    } else {
                        return value;
                    }
                } else {
                    return value;
                }
            }
        }
        return nonSchema;
    }

    /**
     * Replace $ref array elements
     *
     * @param entry Swagger content
     * @throws APIManagementException
     */
    private void generateArraySchemas(Map.Entry<String, JsonNode> entry) throws APIManagementException {
        JsonNode entryRef;
        JsonNode schemaProperty;
        if (entry.getValue() != null) {
            schemaProperty = entry.getValue();
            if (schemaProperty != null) {
                Iterator<JsonNode> arrayElements = schemaProperty.elements();
                while (arrayElements.hasNext()) {
                    entryRef = arrayElements.next();
                    if (entryRef != null) {
                        if (entryRef.has(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                            entryRef = extractSchemaObject(entryRef);
                            ObjectMapper mapper = new ObjectMapper();
                            String str[] = schemaProperty.toString().split(",");
                            if ( str.length > 0) {
                                List<String> schemaItems = Arrays.asList(str);
                                ArrayList<String> convertedSchemaItems = new ArrayList(schemaItems);
                                for (int x = 0; x < convertedSchemaItems.size(); x++) {
                                    String refItem = convertedSchemaItems.get(x);
                                    if (refItem.contains(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                                        convertedSchemaItems.remove(refItem);
                                        convertedSchemaItems.add(entryRef.toString());
                                    }
                                }
                                try {
                                    JsonNode actualObj = mapper.readTree(convertedSchemaItems.toString());
                                    entry.setValue(actualObj);
                                } catch (IOException e) {
                                    throw new APIManagementException(
                                            "Error occurred while converting string to json elements", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Schema path from $ref
     *
     * @param schemaNode Swagger schema content
     * @return $ref path
     */
    private String extractRef(Iterator<JsonNode> schemaNode) {
        while (schemaNode.hasNext()) {
            String nodeVal = schemaNode.next().toString();
            String[] val = nodeVal.split("#");
            if (val.length > 0) {
                String path = val[1].replaceAll("^\"|\"$", "");
                if (StringUtils.isNotEmpty(path)) {
                    int c = path.lastIndexOf('/');
                    return path.substring(c + 1);
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Extract the reference.
     * @param schemaNode Schema node to be extracted.
     * @return extracted schema.
     */
    private String extractReference(String schemaNode) {
        String[] val = schemaNode.split("#");
        String path = val[1].replaceAll("[\"}\\]\\\\]", "");
        String searchLastIndex = null;
        if (StringUtils.isNotEmpty(path)) {
            int index = path.lastIndexOf('/');
            searchLastIndex = path.substring(index + 1);
        }
        String nodeVal = path.replaceAll("/", ".");
        String Swagger = swagger;
        String name;

        Object object = JsonPath.read(Swagger, "$." + nodeVal);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNo = mapper.convertValue(object, JsonNode.class);
        String value = jsonNo.get(0).toString();

        if (value.contains(ThreatProtectorConstants.SCHEMA_REFERENCE)) {

            String res = JsonPath.read(Swagger, "$.components.requestBodies." +
                    searchLastIndex + ".content.application/json.schema").toString();
            if (res.contains("items")) {
                name = JsonPath.read(Swagger, "$.components.requestBodies." +
                        searchLastIndex + ".content.application/json.schema.items.$ref").toString();
                extractReference(name);
            } else {
                name = JsonPath.read(Swagger, "$.components.requestBodies." + searchLastIndex +
                        ".content.application/json.schema.$ref").toString();
                if (name.contains("components/schemas")) {
                    Object ob = JsonPath.read(Swagger, "$..components.schemas." + searchLastIndex);
                    mapper = new ObjectMapper();
                    try {
                        JsonNode jsonNode = mapper.convertValue(ob, JsonNode.class);
                        generateSchema(jsonNode);
                        name = jsonNode.get(0).toString();

                    } catch (APIManagementException e) {
                        e.printStackTrace();
                    }
                    schemaContent = name;
                } else {
                    extractReference(name);
                }
            }
        } else {
            schemaContent = value;
            return schemaContent;

        }
        return schemaContent;
    }

    /**
     * Replace $ref references with relevant schemas and recreate the swagger definition.
     *
     * @param parent Swagger definition parent Node.
     * @throws APIManagementException Throws an APIManagement exception.
     */
    private void generateSchema(JsonNode parent) throws APIManagementException {
        JsonNode schemaProperty;
        Iterator<Map.Entry<String, JsonNode>> schemaNode;
        if (parent.get(0) != null) {
            schemaNode = parent.get(0).fields();
        } else {
            schemaNode = parent.fields();
        }
        while (schemaNode.hasNext()) {
            Map.Entry<String, JsonNode> entry = schemaNode.next();
            if (entry.getValue().has(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                JsonNode refNode = entry.getValue();
                Iterator<Map.Entry<String, JsonNode>> refItems = refNode.fields();
                while (refItems.hasNext()) {
                    Map.Entry<String, JsonNode> entryRef = refItems.next();
                    if (entryRef.getKey().equals(ThreatProtectorConstants.SCHEMA_REFERENCE)) {
                        JsonNode schemaObject = extractSchemaObject(entryRef.getValue());
                        if (schemaObject != null) {
                            entry.setValue(schemaObject);
                        }
                    }
                }
            }
            schemaProperty = entry.getValue();
            if (JsonNodeType.OBJECT == schemaProperty.getNodeType()) {
                generateSchema(schemaProperty);
            }
            if (JsonNodeType.ARRAY == schemaProperty.getNodeType()) {
                generateArraySchemas(entry);
            }
        }
    }
}
