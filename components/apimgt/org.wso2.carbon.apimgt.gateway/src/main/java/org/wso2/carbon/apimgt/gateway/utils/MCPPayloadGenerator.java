package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.*;
import org.wso2.carbon.apimgt.api.model.mcp.MCPResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

public class MCPPayloadGenerator {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    public static String getErrorResponse(Integer id, int code, String message, String data) {
        McpError error = new McpError(code, message, data);
        McpErrorResponse errorResponse = new McpErrorResponse(id, error);
        return gson.toJson(errorResponse);
    }

    public static String getErrorResponse(int code, String message, String data) {
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

    public static String generateToolListPayload(Object id, List<ExtendedOperation> extendedOperations) {
        MCPResponse response = new MCPResponse(id);
        JsonObject responseObject = gson.fromJson(gson.toJson(response), JsonObject.class);
        JsonObject result = new JsonObject();
        JsonArray toolsArray = new JsonArray();
        for (ExtendedOperation extendedOperation : extendedOperations) {
            JsonObject toolObject = new JsonObject();
            toolObject.addProperty(APIConstants.MCP.TOOL_NAME_KEY, extendedOperation.getName());
            toolObject.addProperty(APIConstants.MCP.TOOL_DESC_KEY, extendedOperation.getDescription());
            String schema = extendedOperation.getSchema();
            if (schema != null) {
                JsonObject schemaObject = gson.fromJson(schema, JsonObject.class);
                toolObject.add("inputSchema", sanitizeInputSchema(schemaObject));
            }
            toolsArray.add(toolObject);
        }
        result.add("tools", toolsArray);
        responseObject.add(APIConstants.MCP.RESULT_KEY, result);

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
        for (JsonElement element : requiredArray) {
            String requiredField = element.getAsString();
            String newRequiredField = requiredField.split("_", 2)[1];
            sanitizedArray.add(newRequiredField);
        }
        inputObject.add(APIConstants.MCP.REQUIRED_KEY, sanitizedArray);

        // remove the header, query, and path prefixes from the properties keys
        JsonObject propertiesObject = inputObject.getAsJsonObject(APIConstants.MCP.PROPERTIES_KEY);
        JsonObject sanitizedPropertiesObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : propertiesObject.entrySet()) {
            String key = entry.getKey();
            if ("requestBody".equalsIgnoreCase(key)) {
                sanitizedPropertiesObject.add("requestBody", entry.getValue());
                continue;
            }
            String sanitizedKey = key.split("_", 2)[1];
            sanitizedPropertiesObject.add(sanitizedKey, entry.getValue().getAsJsonObject());
        }
        inputObject.add(APIConstants.MCP.PROPERTIES_KEY, sanitizedPropertiesObject);
        return inputObject;
    }


    // not relevant to On prem MCP
    public static JsonObject generateTransformationRequestPayload(String toolName, String vHost, String args,
                                                                  ExtendedOperation extendedOperation,
                                                                  String authParam) {
        StringBuilder sb = new StringBuilder("https://");
        JsonObject payload = new JsonObject();
        payload.addProperty(APIConstants.MCP.PAYLOAD_TOOL_NAME, toolName);
        payload.addProperty(APIConstants.MCP.PAYLOAD_SCHEMA, extendedOperation.getSchema());

        JsonObject apiInfo = new JsonObject();
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_API_NAME, extendedOperation.getApiName());
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_CONTEXT, extendedOperation.getApiContext());
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_VERSION, extendedOperation.getApiVersion());
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_PATH, extendedOperation.getApiTarget());
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_VERB, extendedOperation.getApiVerb());
        if (!authParam.isEmpty()) {
            apiInfo.addProperty(APIConstants.MCP.PAYLOAD_AUTH, authParam);
        }
        if ("localhost".equals(vHost)) {
            sb.append("router").append(":").append("9095");
        } else {
            sb.append(vHost);
        }
        apiInfo.addProperty(APIConstants.MCP.PAYLOAD_ENDPOINT, sb.toString());
        payload.add("api", apiInfo);

        payload.addProperty(APIConstants.MCP.ARGUMENTS_KEY, args);
        return payload;
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
