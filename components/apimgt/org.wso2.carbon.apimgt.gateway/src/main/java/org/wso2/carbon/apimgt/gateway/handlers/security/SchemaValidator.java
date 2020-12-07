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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This SchemaValidator handler validates the request/response messages against schema defined in the swagger.
 */
public class SchemaValidator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private String uuid;
    private String swagger = null;
    private JsonObject rootNode;
    private String requestMethod;
    private String schemaContent = null;

    public void setLocalentry(String localEntry) {
        this.uuid = localEntry;
    }

    /**
     * Handle the API request message validation.
     *
     * @param messageContext Context of the message to be validated
     * @return Continue true the handler chain
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) {
        logger.debug("Validating the API request Body content..");
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String contentType;
        Object objContentType = axis2MC.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);
        swagger = messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_STRING).toString();
        if (swagger == null) {
            return true;
        }
        try {
            JsonParser jsonParser = new JsonParser();
            rootNode = jsonParser.parse(swagger).getAsJsonObject();

            Object reqMethod = messageContext.getProperty(APIMgtGatewayConstants.
                    ELECTED_REQUEST_METHOD);
            if (reqMethod == null) {
                Object method = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD);
                if (method == null) {
                    return true;
                }
                requestMethod = method.toString();
            } else {
                requestMethod = reqMethod.toString();
            }
            if (objContentType == null) {
                return true;
            }
            contentType = objContentType.toString();
            if (logger.isDebugEnabled()) {
                logger.debug("Content type of the request message: " + contentType);
            }
            if (!APIMgtGatewayConstants.APPLICATION_JSON.equals(contentType)) {
                return true;
            }
            Pipe pipe = (Pipe)axis2MC.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
            if (pipe != null) {
                if (getMessageContent(messageContext) == null) {
                    String payloadContent = IOUtils.toString(pipe.getInputStream());
                    JsonUtil.getNewJsonPayload(axis2MC, payloadContent, true, true);
                    axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
                }
            }
            JsonElement payloadObject = getMessageContent(messageContext);
            if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(requestMethod) &&
                    payloadObject != null && !APIMgtGatewayConstants.EMPTY_ARRAY.equals(payloadObject)) {
                validateRequest(messageContext);
            }
        } catch (IOException e) {
            logger.error("Error occurred while building the API request", e);
            return false;
        } catch (APIManagementException e) {
            logger.error("Error occurred while validating the API request", e);
            return false;
        }
        return true;
    }

    /**
     * Validate the response message.
     *
     * @param messageContext Context of the API response
     * @return Continue true the handler chain
     */
    @Override
    public boolean handleResponse(MessageContext messageContext) {
        logger.debug("Validating the API response  Body content..");
        String contentType;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axis2MC);
            logger.debug("Successfully built the response message");
        } catch (IOException e) {
            logger.error("Error occurred while building the API response", e);
            return false;
        } catch (XMLStreamException e) {
            logger.error("Error occurred while validating the API response", e);
            return false;
        }
        Object objectResponse = axis2MC.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);
        if (objectResponse == null) {
            return true;
        }
        contentType = objectResponse.toString();
        if (!APIMgtGatewayConstants.APPLICATION_JSON.equals(contentType)) {
            return true;
        }
        try {
            validateResponse(messageContext);
        } catch (APIManagementException e) {
            logger.error("Error occurred while validating the API response", e);
            return false;
        }
        return true;
    }

    /**
     * Validate the Request/response content.
     *
     * @param payloadObject  Request/response payload
     * @param schemaString   Schema which uses to validate request/response messages
     * @param messageContext Message context
     * @return Weather Schema validation success or not, if schema is null return true
     */
    private boolean validateContent(JsonElement payloadObject, String schemaString, MessageContext messageContext) {
        logger.debug("Validating JSON content against the schema");
        StringBuilder finalMessage = new StringBuilder();
        List<String> errorMessages;

        JSONObject jsonSchema = new JSONObject(schemaString);
        Schema schema = SchemaLoader.load(jsonSchema);
        if (schema == null) {
            return true;
        }
        try {
            schema.validate(new JSONObject(payloadObject.toString()));
            return true;
        } catch (ValidationException e) {
            errorMessages = e.getAllMessages();
            for (String message : errorMessages) {
                finalMessage.append(message).append(", ");
            }
            if (messageContext.isResponse()) {
                String message = "Schema validation failed in the Response: ";
                logger.error(message + e.getMessage(), e);
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.INTERNAL_ERROR_CODE,
                        message + finalMessage);
            } else {
                String errMessage = "Schema validation failed in the Request: ";
                logger.error(errMessage + e.getMessage(), e);
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE,
                        errMessage + finalMessage);
            }
            return false;
        }
    }

    /**
     * Validate the API Request JSON Body.
     *
     * @param messageContext Message context to be validate the request
     */
    private void validateRequest(MessageContext messageContext) throws APIManagementException {
        //extract particular schema content.
        String schema = getSchemaContent(messageContext);
        //extract the request payload.
        JsonElement payloadObject = getMessageContent(messageContext);
        if (schema != null && !APIMgtGatewayConstants.EMPTY.equals(schema) &&
                payloadObject != null && !APIMgtGatewayConstants.EMPTY_ARRAY.equals(payloadObject)) {
            if (payloadObject.isJsonArray()) {
                for (JsonElement payloadItem : payloadObject.getAsJsonArray()) {
                    // if validation fails stop validation other items.
                    if (!validateContent(payloadItem, schema, messageContext)) {
                        return;
                    }
                }
            } else {
                validateContent(payloadObject, schema, messageContext);
            }
        }
    }

    /**
     * Validate the API Response Body  which comes from the BE.
     *
     * @param messageContext Message context to be validate the response
     */
    private void validateResponse(MessageContext messageContext) throws APIManagementException {
        String responseSchema = getSchemaContent(messageContext);
        try {
            new JSONObject(responseSchema);
        } catch (JSONException ex) {
            return;
        }
        JsonElement payloadObject = getMessageContent(messageContext);
        if (responseSchema != null && !APIMgtGatewayConstants.EMPTY_ARRAY.equals(responseSchema) &&
                payloadObject != null && !APIMgtGatewayConstants.EMPTY_ARRAY.equals(payloadObject)) {
            if (payloadObject.isJsonArray()) {
                for (JsonElement payloadItem : payloadObject.getAsJsonArray()) {
                    // if validation fails stop validation other items.
                    if (validateContent(payloadItem, responseSchema, messageContext)) {
                        return;
                    }
                }
            } else {
                validateContent(payloadObject, responseSchema, messageContext);
            }
        }
    }

    /**
     * Get the Request/Response messageContent as a JsonObject.
     *
     * @param messageContext Message context
     * @return JsonElement which contains the request/response message content
     */
    private JsonElement getMessageContent(MessageContext messageContext) {
        JsonElement payloadObject = null;
        org.apache.axis2.context.MessageContext axis2Context = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        if (JsonUtil.hasAJsonPayload(axis2Context)) {
            String jsonString = JsonUtil.jsonPayloadToString(axis2Context);
            JsonParser jsonParser = new JsonParser();
            payloadObject = jsonParser.parse(jsonString);
        } else if (messageContext.getEnvelope().getBody() != null) {
            Object objFirstElement = messageContext.getEnvelope().getBody().getFirstElement();
            if (objFirstElement != null) {
                OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
                try {
                    JsonParser jsonParser = new JsonParser();
                    payloadObject = jsonParser.parse(JsonUtil.toJsonString(xmlResponse).toString());
                } catch (AxisFault axisFault) {
                    logger.error(" Error occurred while converting the String payload to Json");
                }
            }
        }
        return payloadObject;
    }

    /**
     * Extract the schema Object.
     *
     * @param refNode JSON node to be extracted
     * @return Extracted schema
     */
    private JsonElement extractSchemaObject(JsonElement refNode) {
        String[] val = refNode.toString().split("" + APIMgtGatewayConstants.HASH);
        String path = val[1].replace("\\{^\"|\"}", APIMgtGatewayConstants.EMPTY).replace
                ("\"", APIMgtGatewayConstants.EMPTY).replace("}", APIMgtGatewayConstants.EMPTY)
                .replaceAll(APIMgtGatewayConstants.BACKWARD_SLASH, APIMgtGatewayConstants.EMPTY);
        String jsonPath = APIMgtGatewayConstants.JSON_PATH
                + StringUtils.strip(path, String.valueOf(APIMgtGatewayConstants.FORWARD_SLASH))
                .replace(APIMgtGatewayConstants.FORWARD_SLASH, APIMgtGatewayConstants.JSONPATH_SEPARATE);
        return JsonPath.read(rootNode, jsonPath);
    }

    /**
     * Get the relevant schema content for the  particular request/response messages.
     *
     * @param messageContext Message content
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
        String resourcePath = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String subPath = messageContext.getProperty(RESTConstants.REST_SUB_REQUEST_PATH).toString();
        if ("/".equals(subPath.substring(subPath.length() - 1))) {
            resourcePath = resourcePath + "/";
        }
        String requestMethod = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD).toString();
        String schema;
        String Swagger = swagger;
        String value = JsonPath.read(Swagger, APIMgtGatewayConstants.JSON_PATH +
                APIMgtGatewayConstants.OPEN_API).toString();
        StringBuilder jsonPath = new StringBuilder();
        if (value != null && !value.equals(APIMgtGatewayConstants.EMPTY_ARRAY)) {
            //refer schema
            jsonPath.append(APIMgtGatewayConstants.PATHS)
                    .append(resourcePath).append(APIMgtGatewayConstants.BODY_CONTENT);
            schema = JsonPath.read(Swagger, jsonPath.toString()).toString();
            if (schema == null | APIMgtGatewayConstants.EMPTY_ARRAY.equals(schema)) {
                // refer request bodies
                StringBuilder requestBodyPath = new StringBuilder();
                requestBodyPath.append(APIMgtGatewayConstants.PATHS).append(resourcePath).
                        append(APIMgtGatewayConstants.JSONPATH_SEPARATE).
                        append(requestMethod.toLowerCase()).append(APIMgtGatewayConstants.REQUEST_BODY);
                schema = JsonPath.read(Swagger, requestBodyPath.toString()).toString();
            }
        } else {
            jsonPath.append(APIMgtGatewayConstants.PATHS).append(resourcePath).
                    append(APIMgtGatewayConstants.JSONPATH_SEPARATE)
                    .append(requestMethod.toLowerCase()).append(APIMgtGatewayConstants.PARAM_SCHEMA);
            schema = JsonPath.read(Swagger, jsonPath.toString()).toString();
        }
        return extractReference(schema);
    }

    /**
     * Extract the response schema from swagger according to the response code.
     *
     * @param messageContext message content
     * @return response schema
     * @throws APIManagementException wrap and throw IOException
     */
    private String extractResponse(MessageContext messageContext) throws APIManagementException {
        JsonElement resourceSchema;
        ObjectMapper mapper = new ObjectMapper();
        String name;

        String value;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String reqMethod;

        Object method = messageContext.getProperty(APIMgtGatewayConstants.
                ELECTED_REQUEST_METHOD);
        if (method == null) {
            reqMethod = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_REQUEST_METHOD).toString();
        } else {
            reqMethod = method.toString();
        }

        String responseStatus = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_SC).toString();
        StringBuilder responseSchemaPath = new StringBuilder();
        responseSchemaPath.append(APIMgtGatewayConstants.PATHS).append(electedResource)
                .append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(reqMethod.toLowerCase())
                .append(APIMgtGatewayConstants.JSON_RESPONSES).append(responseStatus)
                .append(APIMgtGatewayConstants.CONTENT)
                .append(APIMgtGatewayConstants.JSON_CONTENT);
        JsonArray schema = JsonPath.read(swagger, responseSchemaPath.toString());

        if (schema.size() != 0) {
            return extractReference(schema.toString());
        } else {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(APIMgtGatewayConstants.PATHS).append(electedResource).
                    append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(reqMethod.toLowerCase()).
                    append(APIMgtGatewayConstants.JSON_RESPONSES).
                    append(responseStatus).append(APIMgtGatewayConstants.JSON_SCHEMA);

            schema = JsonPath.read(swagger, pathBuilder.toString());
            if (schema.size() != 0) {
                value = schema.getAsJsonArray().get(0).toString();
            } else {
                value = schema.toString();
            }
            if (value.contains(APIMgtGatewayConstants.ITEMS)) {
                StringBuilder requestSchemaPath = new StringBuilder();
                requestSchemaPath.append(APIMgtGatewayConstants.PATHS).append(electedResource).
                        append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(reqMethod.toLowerCase()).
                        append(APIMgtGatewayConstants.JSON_RESPONSES).append(responseStatus).
                        append(APIMgtGatewayConstants.JSON_SCHEMA).append(
                        APIMgtGatewayConstants.JSONPATH_SEPARATE).append(APIMgtGatewayConstants.ITEMS);
                name = JsonPath.read(swagger, requestSchemaPath.toString()).toString();
                if (name.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                    requestSchemaPath.append(APIMgtGatewayConstants.JSONPATH_SEPARATE).
                            append(APIMgtGatewayConstants.SCHEMA_REFERENCE);
                    extractReference(name);
                    return JsonPath.read(swagger, requestSchemaPath.toString()).toString();
                }
                return value;
            }
        }

        StringBuilder resPath = new StringBuilder();
        resPath.append(APIMgtGatewayConstants.PATHS).append(electedResource).append(
                APIMgtGatewayConstants.JSONPATH_SEPARATE).append(reqMethod.toLowerCase()).
                append(APIMgtGatewayConstants.JSON_RESPONSES).append(responseStatus).append
                (APIMgtGatewayConstants.SCHEMA);
        JsonArray resource = JsonPath.read(swagger, resPath.toString());
        if (resource.size() != 0) {
            value = resource.getAsJsonArray().get(0).toString();
        } else {
            value = resource.toString();
        }
        if (value != null && !APIMgtGatewayConstants.EMPTY_ARRAY.equals(value)) {
            if (value.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                byte[] bytes = value.getBytes();
                try {
                    JsonNode node = mapper.readTree(bytes);
                    Iterator<JsonNode> schemaNode = node.findParent(
                            APIMgtGatewayConstants.SCHEMA_REFERENCE).elements();
                    JsonNode nodeNext = schemaNode.next();
                    if (nodeNext != null) {
                        return extractReference(nodeNext.toString());
                    }
                } catch (IOException e) {
                    throw new APIManagementException("Error occurred while converting bytes from json node");
                }
            } else {
                return value;
            }
        } else {
            StringBuilder responseDefaultPath = new StringBuilder();
            responseDefaultPath.append(APIMgtGatewayConstants.PATHS).append(electedResource).
                    append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(reqMethod.toLowerCase()).
                    append(APIMgtGatewayConstants.JSON_RESPONSES).append(APIMgtGatewayConstants.DEFAULT);
            resourceSchema = JsonPath.read(swagger, responseDefaultPath.toString());
            if (resource.size() != 0) {
                value = resourceSchema.getAsJsonArray().get(0).toString();
            } else {
                value = resourceSchema.toString();
            }
            if (value.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                byte[] bytes = value.getBytes();
                try {
                    JsonNode node = mapper.readTree(bytes);
                    if (node != null) {
                        Iterator<JsonNode> schemaNode = node.findParent(
                                APIMgtGatewayConstants.SCHEMA_REFERENCE).elements();
                        return extractRef(schemaNode);
                    }
                } catch (IOException e) {
                    logger.error("Error occurred while reading the schema.", e);
                    throw new APIManagementException(e);
                }
            } else {
                return value;
            }
        }
        return value;
    }

    /**
     * Replace $ref array elements.
     *
     * @param entry Array reference to be replaced from actual value.
     */
    private void generateArraySchemas(Map.Entry<String, JsonElement> entry) {
        JsonElement entryRef;
        JsonElement ref;
        JsonElement schemaProperty;
        if (entry.getValue() != null) {
            schemaProperty = entry.getValue();
            if (schemaProperty == null) {
                return;
            }
            Iterator<JsonElement> arrayElements = schemaProperty.getAsJsonArray().iterator();
            List<JsonElement> nodeList = Lists.newArrayList(arrayElements);
            for (int i = 0; i < nodeList.size(); i++) {
                entryRef = nodeList.get(i);
                if (entryRef.isJsonObject() && entryRef.getAsJsonObject().has(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                    ref = extractSchemaObject(entryRef);
                    nodeList.remove(i);
                    nodeList.add(i, ref);
                }
            }
            Gson gson = new Gson();
            entry.setValue(gson.toJsonTree(nodeList));
        }
    }

    /**
     * Get Schema path from $ref.
     *
     * @param schemaNode Swagger schema content
     * @return $ref path
     */
    private String extractRef(Iterator<JsonNode> schemaNode) {
        while (schemaNode.hasNext()) {
            String nodeVal = schemaNode.next().toString();
            String[] val = nodeVal.split("" + APIMgtGatewayConstants.HASH);
            if (val.length > 0) {
                String path = val[1].replaceAll("^\"|\"$", APIMgtGatewayConstants.EMPTY);
                if (StringUtils.isNotEmpty(path)) {
                    int c = path.lastIndexOf(APIMgtGatewayConstants.FORWARD_SLASH);
                    return path.substring(c + 1);
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Extract the reference.
     *
     * @param schemaNode Schema node to be extracted
     * @return extracted schema
     */
    private String extractReference(String schemaNode) {
        String[] val = schemaNode.split("" + APIMgtGatewayConstants.HASH);
        String path = val[1].replaceAll("\"|}|]|\\\\", "");
        String searchLastIndex = null;
        if (StringUtils.isNotEmpty(path)) {
            int index = path.lastIndexOf(APIMgtGatewayConstants.FORWARD_SLASH);
            searchLastIndex = path.substring(index + 1);
        }

        String nodeVal = path.replaceAll("" + APIMgtGatewayConstants.FORWARD_SLASH, ".");
        String name = null;
        JsonElement jsonSchema = JsonPath.read(swagger, APIMgtGatewayConstants.JSON_PATH + nodeVal);
        String value;

        if (jsonSchema.isJsonArray()) {
            if (jsonSchema.getAsJsonArray().size() == 0) {
                value = "";
            } else {
                value = jsonSchema.getAsJsonArray().get(0).toString();
            }
        } else {
            value = jsonSchema.toString();
        }
        if (value.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE) &&
                !nodeVal.contains(APIMgtGatewayConstants.DEFINITIONS)) {
            if (nodeVal.contains(APIMgtGatewayConstants.REQUESTBODIES)) {
                StringBuilder extractRefPath = new StringBuilder();
                extractRefPath.append(APIMgtGatewayConstants.JSON_PATH).append(
                        APIMgtGatewayConstants.REQUESTBODY_SCHEMA).
                        append(searchLastIndex).append(APIMgtGatewayConstants.JSON_SCHEMA);
                String res = JsonPath.read(swagger, extractRefPath.toString()).toString();
                if (res.contains(APIMgtGatewayConstants.ITEMS)) {
                    StringBuilder requestSchemaPath = new StringBuilder();
                    requestSchemaPath.append(APIMgtGatewayConstants.JSON_PATH).
                            append(APIMgtGatewayConstants.REQUESTBODY_SCHEMA).append(
                            searchLastIndex).append(APIMgtGatewayConstants.JSON_SCHEMA).
                            append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(APIMgtGatewayConstants.ITEMS).
                            append(APIMgtGatewayConstants.JSONPATH_SEPARATE).append(APIMgtGatewayConstants.SCHEMA_REFERENCE);
                    name = JsonPath.read(swagger, requestSchemaPath.toString()).toString();
                    extractReference(name);
                } else {
                    StringBuilder jsonSchemaRef = new StringBuilder();
                    jsonSchemaRef.append(APIMgtGatewayConstants.JSON_PATH).append(
                            APIMgtGatewayConstants.REQUESTBODY_SCHEMA).append(searchLastIndex).append(
                            APIMgtGatewayConstants.CONTENT).append(APIMgtGatewayConstants.JSON_CONTENT);
                    name = JsonPath.read(swagger, jsonSchemaRef.toString()).toString();
                    if (name.contains(APIMgtGatewayConstants.COMPONENT_SCHEMA)) {
                        JsonArray componentSchema = JsonPath.read(swagger,
                                APIMgtGatewayConstants.JSONPATH_SCHEMAS + searchLastIndex);
                        try {
                            generateSchema(componentSchema);
                            if (componentSchema.size() != 0) {
                                name = componentSchema.getAsJsonArray().get(0).toString();
                            } else {
                                name = "";
                            }
                        } catch (APIManagementException e) {
                            logger.error("Error occurred while generating the schema content for " +
                                    "the particular request", e);
                        }
                        schemaContent = name;
                    } else {
                        extractReference(name);
                    }
                }
            } else if (nodeVal.contains(APIMgtGatewayConstants.SCHEMA)) {
                JsonArray componentSchema = JsonPath.read(swagger,
                        APIMgtGatewayConstants.JSONPATH_SCHEMAS + searchLastIndex);
                try {
                    generateSchema(componentSchema);
                    if (componentSchema.size() != 0) {
                        name = componentSchema.getAsJsonArray().get(0).toString();
                    } else {
                        name = "";
                    }
                } catch (APIManagementException e) {
                    logger.error("Error occurred while generating the schema content for " +
                            "the particular request", e);
                }
                schemaContent = name;
            }
        } else if (nodeVal.contains(APIMgtGatewayConstants.DEFINITIONS)) {
            StringBuilder requestSchemaPath = new StringBuilder();
            requestSchemaPath.append(APIMgtGatewayConstants.JSON_PATH).
                    append(APIMgtGatewayConstants.DEFINITIONS).append(APIMgtGatewayConstants.JSONPATH_SEPARATE
            ).append(searchLastIndex);
            JsonElement nameObj = JsonPath.read(swagger, requestSchemaPath.toString());
            try {
                generateSchema(nameObj);
                if (nameObj.isJsonArray()) {
                    if (nameObj.getAsJsonArray().size() != 0) {
                        name = nameObj.getAsJsonArray().get(0).toString();
                    } else {
                        name = "";
                    }
                } else {
                    name = nameObj.toString();
                }
            } catch (APIManagementException e) {
                logger.error("Error occurred while generating the schema content for " +
                        "the particular request", e);
            }
            schemaContent = name;
        } else {
            schemaContent = value;
            return schemaContent;
        }
        return schemaContent;
    }

    /**
     * Replace $ref references with relevant schemas and recreate the swagger definition.
     *
     * @param parent Swagger definition parent Node
     * @throws APIManagementException Throws an APIManagement exception
     */
    private void generateSchema(JsonElement parent) throws APIManagementException {
        JsonElement schemaProperty;
        Iterator<Map.Entry<String, JsonElement>> schemaNode;
        if (parent.isJsonArray()) {
            if (parent.getAsJsonArray().size() == 0) return;
            schemaNode = parent.getAsJsonArray().get(0).getAsJsonObject().entrySet().iterator();
        } else {
            schemaNode = parent.getAsJsonObject().entrySet().iterator();
        }
        while (schemaNode.hasNext()) {
            Map.Entry<String, JsonElement> entry = schemaNode.next();
            if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                JsonObject refNode = entry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> entryRef : refNode.entrySet()) {
                    if (entryRef.getKey().equals(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                        JsonElement schemaObject = extractSchemaObject(entryRef.getValue());
                        if (schemaObject != null) {
                            entry.setValue(schemaObject);
                        }
                    }
                }
            }
            schemaProperty = entry.getValue();
            if (schemaProperty.isJsonObject()) {
                generateSchema(schemaProperty);
            }
            if (schemaProperty.isJsonArray()) {
                generateArraySchemas(entry);
            }
        }
    }
}
