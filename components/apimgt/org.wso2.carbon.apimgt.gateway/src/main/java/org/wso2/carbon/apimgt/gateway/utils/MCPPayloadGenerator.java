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
import com.google.gson.JsonObject;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.mcp.response.InitializeResult;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpError;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpErrorResponse;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponse;
import org.wso2.carbon.apimgt.gateway.mcp.response.ToolCallResult;
import org.wso2.carbon.apimgt.gateway.mcp.response.ToolListResult;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.HashMap;
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
        McpResponse<InitializeResult> initializeResponse = new McpResponse<>(id);
        InitializeResult result = new InitializeResult();
        result.setProtocolVersion(APIConstants.MCP.PROTOCOL_VERSION_2025_JUNE);

        InitializeResult.ServerInfo serverInfo = new InitializeResult.ServerInfo();
        serverInfo.setName(serverName);
        serverInfo.setVersion(serverVersion);
        serverInfo.setDescription(serverDescription);
        result.setServerInfo(serverInfo);

        InitializeResult.Capabilities capabilities = getCapabilities(toolListChangeNotified);
        
        result.setCapabilities(capabilities);
        initializeResponse.setResult(result);

        return gson.toJson(initializeResponse);
    }

    private static InitializeResult.Capabilities getCapabilities(boolean toolListChangeNotified) {
        InitializeResult.Capabilities capabilities = new InitializeResult.Capabilities();
        InitializeResult.Capabilities.Tools tools = new InitializeResult.Capabilities.Tools();
        tools.setListChanged(toolListChangeNotified);
        capabilities.setTools(tools);
        capabilities.setLogging(new HashMap<>());

        InitializeResult.Capabilities.Resources resources = new InitializeResult.Capabilities.Resources();
        resources.setSubscribe(false); // Resources are not supported at the moment
        resources.setListChanged(false); // Resources are not supported at the moment
        capabilities.setResources(resources);

        InitializeResult.Capabilities.Prompts prompts = new InitializeResult.Capabilities.Prompts();
        prompts.setListChanged(false); // Prompts are not supported at the moment
        capabilities.setPrompts(prompts);
        return capabilities;
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

    //write a method to generate the tool list payload
    public static String generateToolListPayload(Object id, List<URLMapping> extendedOperations, boolean isThirdParty) {
        McpResponse<ToolListResult> toolListResponse = new McpResponse<>(id);
        ToolListResult toolListResult = new ToolListResult();
        List<ToolListResult.ToolInfo> toolInfoList = new ArrayList<>();

        for (URLMapping extendedOperation : extendedOperations) {
            ToolListResult.ToolInfo tool = new ToolListResult.ToolInfo();
            tool.setName(extendedOperation.getUrlPattern());
            tool.setDescription(extendedOperation.getDescription());
            String schema = extendedOperation.getSchemaDefinition();
            if (schema != null) {
                ToolListResult.JsonSchema schemaObject = gson.fromJson(schema, ToolListResult.JsonSchema.class);
                if (!isThirdParty) {
                    tool.setInputSchema(sanitizeInputSchema(schemaObject));
                } else {
                    // For third-party tools, we do not sanitize the input schema
                    tool.setInputSchema(schemaObject);
                }

            }
            toolInfoList.add(tool);
        }
        toolListResult.setTools(toolInfoList);
        toolListResponse.setResult(toolListResult);
        return gson.toJson(toolListResponse);
    }

    private static ToolListResult.JsonSchema sanitizeInputSchema(ToolListResult.JsonSchema inputSchema) {
        if (inputSchema == null) {
            // Return an empty object schema if the input schema is null
            ToolListResult.JsonSchema emptySchema = new ToolListResult.JsonSchema();
            emptySchema.setType("object");
            emptySchema.setProperties(new HashMap<>());
            return emptySchema;
        }
        inputSchema.removeProperty("contentType");

        // remove the header, query, and path prefixes from the required fields
        List<String> requiredProperties = inputSchema.getRequired();
        List<String> sanitizedRequiredProperties = new ArrayList<>();
        if (requiredProperties != null && !requiredProperties.isEmpty()) {
            for (String requiredProperty : requiredProperties) {
                String sanitizedRequiredProperty;
                if (!"requestBody".equalsIgnoreCase(requiredProperty)) {
                    String[] parts = requiredProperty.split("_", 2);
                    sanitizedRequiredProperty = parts.length > 1 ? parts[1] : requiredProperty;
                } else {
                    sanitizedRequiredProperty = requiredProperty;
                }
                sanitizedRequiredProperties.add(sanitizedRequiredProperty);
            }
        }
        inputSchema.setRequired(sanitizedRequiredProperties);

        // remove the header, query, and path prefixes from the properties keys
        Map<String, Object> properties = inputSchema.getProperties();
        Map<String, Object> sanitizedProperties = new HashMap<>();
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                if ("requestBody".equalsIgnoreCase(key)) {
                    sanitizedProperties.put("requestBody", entry.getValue());
                    continue;
                }
                String[] parts = key.split("_", 2);
                String sanitizedKey = parts.length > 1 ? parts[1] : key;
                Object property = entry.getValue();
                sanitizedProperties.put(sanitizedKey, property);
            }
        }
        inputSchema.setProperties(sanitizedProperties);
        return inputSchema;
    }

    public static String generateMCPResponsePayload(Object id, boolean isError, String body) {
        McpResponse<ToolCallResult> mcpResponse = new McpResponse<>(id);
        ToolCallResult toolCallResult = new ToolCallResult();

        toolCallResult.setError(isError);
        List<ToolCallResult.ContentItem> contentItems = new ArrayList<>();
        ToolCallResult.ContentItem contentItem = new ToolCallResult.ContentItem();
        contentItem.setType("text");
        contentItem.setText(body);
        contentItems.add(contentItem);
        toolCallResult.setContent(contentItems);
        mcpResponse.setResult(toolCallResult);

        return gson.toJson(mcpResponse);
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
        McpResponse<JsonObject> response = new McpResponse<>(id);
        response.setResult(new JsonObject());
        return gson.toJson(response);
    }
}
