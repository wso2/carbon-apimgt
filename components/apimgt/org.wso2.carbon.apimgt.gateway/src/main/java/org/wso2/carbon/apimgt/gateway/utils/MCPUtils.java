package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class MCPUtils {

    public static JsonObject parseAndValidateRequest(String requestBody) throws McpException {
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(requestBody);
        } catch (JsonSyntaxException e) {
            // TODO: Check error codes
            throw new McpException(APIConstants.MCP.RpcConstants.PARSE_ERROR_CODE,
                    APIConstants.MCP.RpcConstants.PARSE_ERROR_MESSAGE, e.getMessage());

        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has(APIConstants.MCP.RpcConstants.JSON_RPC)) {
                JsonElement jsonRpcElement = jsonObject.get(APIConstants.MCP.RpcConstants.JSON_RPC);
                if (jsonRpcElement == null || jsonRpcElement.isJsonNull()) {
                    throwMissingJsonRpcError();
                }
                if (!APIConstants.MCP.RpcConstants.JSON_RPC_VERSION.equals(jsonRpcElement.getAsString())) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                            APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Invalid JSON-RPC version");
                }
            } else {
                throwMissingJsonRpcError();
            }
            String method = "";
            if (jsonObject.has(APIConstants.MCP.RpcConstants.METHOD)) {
                JsonElement methodElement = jsonObject.get(APIConstants.MCP.RpcConstants.METHOD);
                if (methodElement == null || methodElement.isJsonNull()) {
                    throwMissingMethodError();
                }
                method = methodElement.getAsString();
                if (method.isEmpty()) {
                    throwMissingMethodError();
                }
                if (!APIConstants.MCP.ALLOWED_METHODS.contains(method)) {
                    throw new McpException(APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_CODE,
                            APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_MESSAGE, "Method not found");
                }
            } else {
                throwMissingMethodError();
            }
            if (jsonObject.has(APIConstants.MCP.RpcConstants.ID)) {
                JsonElement idElement = jsonObject.get(APIConstants.MCP.RpcConstants.ID);
                if (idElement == null || idElement.isJsonNull() || idElement.getAsString().isEmpty()) {
                    throwMissingIdError();
                }
            } else if (!APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED.equals(method)) {
                // TODO: Check if this is needed to be handled
                throwMissingIdError();
            }
            return jsonObject;
        } else {
            throw new McpException(APIConstants.MCP.RpcConstants.PARSE_ERROR_CODE,
                    APIConstants.MCP.RpcConstants.PARSE_ERROR_MESSAGE, "Invalid JSON format");
        }
    }


    private static void throwMissingJsonRpcError() throws McpException {
        throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing jsonrpc field");
    }

    private static void throwMissingIdError() throws McpException {
        throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing id field");
    }

    private static void throwMissingMethodError() throws McpException {
        throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing method field");
    }

}
