package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.exception.McpExceptionWithId;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.Map;

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

    /**
     * Processes the MCP requests for existing APIs and direct backends.
     *
     * @param matchedMcpApi     matched API in the gateway
     * @param requestObject     MCP request payload
     * @param method            MCP JSON RPC method
     * @param additionalHeaders additional headers to send
     * @return the response payload as a String
     */
    public static McpResponseDto processInternalRequest(API matchedMcpApi, JsonObject requestObject, String method,
                                                        Map<String, String> additionalHeaders) {
        try {
            Object id = -1;
            if (!method.contains("notifications/")) {
                id = requestObject.get(APIConstants.MCP.RpcConstants.ID);
            }
            if (APIConstants.MCP.METHOD_INITIALIZE.equals(method)) {
                validateInitializeRequest(id, requestObject);
                return handleMcpInitialize(id, matchedMcpApi);
            } else if (APIConstants.MCP.METHOD_TOOL_LIST.equals(method)) {

            } else if (APIConstants.MCP.METHOD_TOOL_CALL.equals(method)) {

            } else if (APIConstants.MCP.METHOD_PING.equals(method)) {

            } else if (APIConstants.MCP.METHOD_RESOURCES_LIST.equals(method)) {

            } else if (APIConstants.MCP.METHOD_RESOURCE_TEMPLATE_LIST.equals(method)) {

            } else if (APIConstants.MCP.METHOD_PROMPTS_LIST.equals(method)) {

            } else if (APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED.equals(method)) {

            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_CODE,
                        APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_MESSAGE, "Method not found");
            }
            return null; //todo: remove
        } catch (McpException e) {
            return new McpResponseDto(e.toJsonRpcErrorPayload(), 200, null);
        }
    }

    public static void validateInitializeRequest(Object id, JsonObject requestObject) throws McpException {
        if (requestObject.has(APIConstants.MCP.PARAMS_KEY)) {
            JsonObject params = requestObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
            if (params.has(APIConstants.MCP.PROTOCOL_VERSION_KEY)) {
                String protocolVersion = params.get(APIConstants.MCP.PROTOCOL_VERSION_KEY).getAsString();
                if (!APIConstants.MCP.SUPPORTED_PROTOCOL_VERSIONS.contains(protocolVersion)) {
                    throw new McpExceptionWithId(id, APIConstants.MCP.RpcConstants.INVALID_PARAMS_CODE,
                            APIConstants.MCP.PROTOCOL_MISMATCH_ERROR,
                            MCPPayloadGenerator.getInitializeErrorBody(protocolVersion));
                }
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                        APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing protocolVersion field");
            }
        } else {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing params field");
        }
    }

    public static McpResponseDto handleMcpInitialize(Object id, API matchedApi) {
        String name = matchedApi.getName();
        String version = matchedApi.getVersion();
        String description = "This is an MCP Server";
        return new McpResponseDto(MCPPayloadGenerator
                .getInitializeResponse(id, name, version, description, false),
                200, null);
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
