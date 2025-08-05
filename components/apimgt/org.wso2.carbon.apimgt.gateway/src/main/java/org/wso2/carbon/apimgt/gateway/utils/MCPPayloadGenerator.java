/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.wso2.carbon.apimgt.api.model.mcp.MCPResponse;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpError;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpErrorResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.List;
import java.util.Map;

public class MCPPayloadGenerator {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    public static String getErrorResponse(Object id, int code, String message, Object data) {
        McpError error = new McpError(code, message, data);
        McpErrorResponse errorResponse = new McpErrorResponse(id, error);
        return gson.toJson(errorResponse);
    }

    public static String getErrorResponse(int code, String message, Object data) {
        McpError error = new McpError(code, message, data);
        McpErrorResponse errorResponse = new McpErrorResponse(null, error);
        return gson.toJson(errorResponse);
    }

    public static String getInitializeResponse(Object id, String serverName, String serverVersion,
                                               String serverDescription, boolean toolListChangeNotified) {
        // Create the response object as specified in
        // https://modelcontextprotocol.io/specification/2025-03-26/basic/lifecycle#initialization
        MCPResponse response = new MCPResponse(id);
        JsonObject responseObject = gson.fromJson(gson.toJson(response), JsonObject.class);
        JsonObject result = new JsonObject();
        result.addProperty(APIConstants.MCP.PROTOCOL_VERSION_KEY, APIConstants.MCP.PROTOCOL_VERSION_2025_MARCH);

        JsonObject capabilities = new JsonObject();
        JsonObject toolCapabilities = new JsonObject();
        toolCapabilities.addProperty("listChanged", toolListChangeNotified);
        capabilities.add("tools", toolCapabilities);
        // Add empty objects for unsupported capabilities
        capabilities.add("resources", new JsonObject());
        capabilities.add("prompts", new JsonObject());
        capabilities.add("logging", new JsonObject());
        result.add("capabilities", capabilities);

        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", serverName);
        serverInfo.addProperty("version", serverVersion);
        serverInfo.addProperty("description", serverDescription);
        result.add("serverInfo", serverInfo);

        responseObject.add(APIConstants.MCP.RESULT_KEY, result);
        return gson.toJson(responseObject);
    }

    /**
     * Generates the error body for the initialize method when the requested protocol version is not supported.
     *
     * @param requestedVersion The requested protocol version
     * @return The error body as a JSON string
     */
    public static JsonObject getInitializeErrorBody(String requestedVersion) {
        JsonObject data = new JsonObject();
        data.addProperty(APIConstants.MCP.PROTOCOL_VERSION_REQUESTED, requestedVersion);
        JsonArray supportedVersions = new JsonArray();
        for (String version : APIConstants.MCP.SUPPORTED_PROTOCOL_VERSIONS) {
            supportedVersions.add(version);
        }
        data.add(APIConstants.MCP.PROTOCOL_VERSION_SUPPORTED, supportedVersions);
        return data;
    }

    public static String generateToolListPayload(Object id, List<URLMapping> extendedOperations, boolean isThridParty) {
        MCPResponse response = new MCPResponse(id);
        JsonObject responseObject = gson.fromJson(gson.toJson(response), JsonObject.class);
        JsonObject result = new JsonObject();
        JsonArray toolsArray = new JsonArray();
        if (!isThridParty) {
            for (URLMapping extendedOperation : extendedOperations) {
                JsonObject toolObject = new JsonObject();
                toolObject.addProperty(APIConstants.MCP.TOOL_NAME_KEY, extendedOperation.getUrlPattern());
                toolObject.addProperty(APIConstants.MCP.TOOL_DESC_KEY, extendedOperation.getDescription());
                String schema = extendedOperation.getSchemaDefinition();
                if (schema != null) {
                    JsonObject schemaObject = gson.fromJson(schema, JsonObject.class);
                    toolObject.add("inputSchema", sanitizeInputSchema(schemaObject));
                }
                toolsArray.add(toolObject);
            }
            result.add("tools", toolsArray);
            responseObject.add(APIConstants.MCP.RESULT_KEY, result);
        }

        return gson.toJson(responseObject);
    }

    private static JsonObject sanitizeInputSchema(JsonObject inputObject) {
        if (inputObject == null || inputObject.isEmpty()) {
            JsonObject emptyObject = new JsonObject();
            emptyObject.addProperty("type", "object");
            emptyObject.add("properties", new JsonObject());
            return emptyObject;
        }
        inputObject.remove("contentType");

        JsonArray requiredArray = inputObject.getAsJsonArray(APIConstants.MCP.REQUIRED_KEY);
        JsonArray sanitizedArray = new JsonArray();

        // remove the header, query, and path prefixes from the required fields
        if (requiredArray != null) {
            for (JsonElement element : requiredArray) {
                String requiredField = element.getAsString();
                String sanitizedRequiredField;
                if (!"requestBody".equalsIgnoreCase(requiredField)) {
                    sanitizedRequiredField = requiredField.split("_", 2)[1];
                } else {
                    sanitizedRequiredField = requiredField;
                }
                sanitizedArray.add(sanitizedRequiredField);
            }
        }
        inputObject.add(APIConstants.MCP.REQUIRED_KEY, sanitizedArray);

        // remove the header, query, and path prefixes from the properties keys
        JsonObject propertiesObject = inputObject.getAsJsonObject(APIConstants.MCP.PROPERTIES_KEY);
        JsonObject sanitizedPropertiesObject = new JsonObject();
        if (propertiesObject != null) {
            for (Map.Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
                String key = entry.getKey();
                if ("requestBody".equalsIgnoreCase(key)) {
                    sanitizedPropertiesObject.add("requestBody", entry.getValue());
                    continue;
                }
                String sanitizedKey = key.split("_", 2)[1];
                sanitizedPropertiesObject.add(sanitizedKey, entry.getValue().getAsJsonObject());
            }
        }
        inputObject.add(APIConstants.MCP.PROPERTIES_KEY, sanitizedPropertiesObject);
        return inputObject;
    }

    public static String generateMCPResponsePayload(Object id, boolean isError, String body) {
        MCPResponse response = new MCPResponse(id);
        JsonObject responseObject = gson.fromJson(gson.toJson(response), JsonObject.class);

        JsonObject result = new JsonObject();
        result.addProperty("isError", isError);

        JsonArray content = new JsonArray();
        JsonObject contentObject = new JsonObject();
        contentObject.addProperty("type", "text");
        contentObject.addProperty("text", body);

        content.add(contentObject);
        result.add("content", content);
        responseObject.add(APIConstants.MCP.RESULT_KEY, result);

        return gson.toJson(responseObject);
    }

    public static String generatePingResponse(Object id) {
        return generateEmptyResult(id);
    }

    public static String generateResourceListResponse(Object id) {
        // Resources are not supported at the moment
        return generateEmptyResult(id);
    }

    public static String generateResourceTemplateListResponse(Object id) {
        // Resource templates are not supported at the moment
        return generateEmptyResult(id);
    }

    public static String generatePromptListResponse(Object id) {
        // Prompts are not supported at the moment
        return generateEmptyResult(id);
    }

    private static String generateEmptyResult(Object id) {
        MCPResponse response = new MCPResponse(id);
        JsonObject responseObject = gson.fromJson(gson.toJson(response), JsonObject.class);
        JsonObject result = new JsonObject();
        responseObject.add(APIConstants.MCP.RESULT_KEY, result);

        return gson.toJson(responseObject);
    }
}
