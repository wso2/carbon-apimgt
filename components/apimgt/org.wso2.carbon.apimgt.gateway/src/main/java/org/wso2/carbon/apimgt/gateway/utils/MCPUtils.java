package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.exception.McpExceptionWithId;
import org.wso2.carbon.apimgt.gateway.mcp.Param;
import org.wso2.carbon.apimgt.gateway.mcp.SchemaMapping;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

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
    public static McpResponseDto processInternalRequest(MessageContext messageContext, API matchedMcpApi, JsonObject requestObject, String method,
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
                return handleMcpToolList(id, matchedMcpApi, false);
            } else if (APIConstants.MCP.METHOD_TOOL_CALL.equals(method)) {
                validateToolsCallRequest(requestObject, matchedMcpApi);
                return handleMcpToolsCall(messageContext, id, matchedMcpApi, requestObject, additionalHeaders);
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

    private static void validateToolsCallRequest(JsonObject jsonObject, API matchedApi) throws McpException {
        if (jsonObject.has(APIConstants.MCP.PARAMS_KEY)) {
            JsonObject params = jsonObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
            if (params.has("name")) {
                JsonElement toolNameElement = params.get("name");
                if (toolNameElement == null || toolNameElement.isJsonNull()) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                            APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing toolName field");
                }
                String toolName = toolNameElement.getAsString();
                if (toolName == null || toolName.isEmpty()) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                            APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing toolName field");
                } else {
                    if (!validateToolName(toolName, matchedApi)) {
                        throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                                APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "The requested tool does not exist");
                    }
                }
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                        APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing toolName field");
            }
        } else {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing params field");
        }
    }

    private static boolean validateToolName(String toolName, API matchedApi) {
        return matchedApi.getUrlMappings()
                .stream()
                .anyMatch(operation -> toolName.equals(operation.getUrlPattern()));
    }


    public static McpResponseDto handleMcpInitialize(Object id, API matchedApi) {
        String name = matchedApi.getName();
        String version = matchedApi.getVersion();
        String description = "This is an MCP Server";
        return new McpResponseDto(MCPPayloadGenerator
                .getInitializeResponse(id, name, version, description, false),
                200, null);
    }

    public static McpResponseDto handleMcpToolList(Object id, API matchedApi, boolean isThirdParty) {
        return new McpResponseDto(
                MCPPayloadGenerator.generateToolListPayload(id, matchedApi.getUrlMappings(),
                        isThirdParty), 200, null);
    }

    private static McpResponseDto handleMcpToolsCall(MessageContext messageContext, Object id, API matchedApi, JsonObject jsonObject,
                                                     Map<String, String> additionalHeaders)
            throws McpException {
        JsonObject params = jsonObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
        String toolName = params.get(APIConstants.MCP.TOOL_NAME_KEY).getAsString();
        URLMapping extendedOperation = matchedApi.getUrlMappings()
                .stream()
                .filter(operation -> operation.getUrlPattern().equals(toolName))
                .findFirst()
                .orElse(null);

        String args;
        if (params.has(APIConstants.MCP.ARGUMENTS_KEY)) {
            args = params.get(APIConstants.MCP.ARGUMENTS_KEY).toString();
        } else {
            args = "{}";
        }
        params.addProperty(APIConstants.MCP.ARGUMENTS_KEY, args);

        transformMcpRequest(messageContext, id, extendedOperation, jsonObject);

        return new McpResponseDto("success", 200, null);
        // for now only supported mode is Rest API Backend

    }

    private static void transformMcpRequest(MessageContext messageContext, Object id, URLMapping extendedOperation,
                                            JsonObject jsonObject) throws McpException {
        if (extendedOperation != null) {
            BackendOperationMapping backendOperationMapping = extendedOperation.getBackendOperationMapping();
            if (backendOperationMapping != null) {
                BackendOperation backendOperation = backendOperationMapping.getBackendOperation();
                if (backendOperation != null) {
                    //process HTTP Verb
                    String verb = backendOperation.getVerb();

                    //process schema
                    String schemaDefinition = extendedOperation.getSchemaDefinition();
                    SchemaMapping schemaMapping = processMcpSchema(schemaDefinition);

                    //process endpoint URL including query and path params
                    processEndpoint(messageContext, schemaMapping, jsonObject, backendOperation);

                    //process headers
                    processHeaders(messageContext, schemaMapping, jsonObject);

                    //process request body
                    processRequestBody(messageContext, schemaMapping, jsonObject);

                    //set received id to msg context
                    messageContext.setProperty("RECEIVED_MCP_ID", id.toString());
                }
            }
        } else {
            //todo: handle error
        }
    }

    private static SchemaMapping processMcpSchema(String schemaDefinition) {
        JsonObject schemaJson = JsonParser.parseString(schemaDefinition).getAsJsonObject();

        SchemaMapping schemaMapping = new SchemaMapping();
        List<Param> queryParams = new ArrayList<>();
        List<Param> headerParams = new ArrayList<>();
        List<String> pathParams = new ArrayList<>();

        JsonObject properties = new JsonObject();
        JsonArray requiredProperties = new JsonArray();
        if (schemaJson.get(APIConstants.MCP.PROPERTIES_KEY) != null) {
            properties = schemaJson.get(APIConstants.MCP.PROPERTIES_KEY).getAsJsonObject();
        }

        if (schemaJson.get(APIConstants.MCP.REQUIRED_KEY) != null) {
            requiredProperties = schemaJson.get(APIConstants.MCP.REQUIRED_KEY).getAsJsonArray();
        }

        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if ("requestBody".equalsIgnoreCase(key)) {
                JsonObject requestBodySchema = value.getAsJsonObject();
                schemaMapping.setContentType(requestBodySchema.get("contentType").getAsString());
                schemaMapping.setHasBody(true);
                continue;
            }

            String[] meta = key.split("_", 2);
            String paramType = meta[0];
            String paramName = meta[1];

            Param param = new Param();
            param.setName(paramName);
            param.setRequired(StreamSupport.stream(requiredProperties.spliterator(), false)
                    .anyMatch(e -> e.getAsString().equals(key)));
            switch(paramType) {
                case "query":
                    queryParams.add(param);
                    break;
                case "header":
                    headerParams.add(param);
                    break;
                case "path":
                    pathParams.add(paramName);
            }
        }
        schemaMapping.setQueryParams(queryParams);
        schemaMapping.setHeaderParams(headerParams);
        schemaMapping.setPathParams(pathParams);
        return schemaMapping;
    }

    private static void processEndpoint(MessageContext messageContext, SchemaMapping schemaMapping, JsonObject jsonObject,
                                        BackendOperation backendOperation) {
        String httpMethod = backendOperation.getVerb();
        String target = backendOperation.getTarget();

        StringBuilder resourcePath = new StringBuilder();
        StringBuilder queryString = new StringBuilder();
        resourcePath.append(target);
        List<Param> queryParams = schemaMapping.getQueryParams();
        JsonObject paramsObj = jsonObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
        if (paramsObj != null && !paramsObj.isEmpty()) {
            JsonObject argumentObj = paramsObj.getAsJsonObject(APIConstants.MCP.ARGUMENTS_KEY);
            for (Param param : queryParams) {
                String paramName = param.getName();
                boolean isParamRequired = param.isRequired();

                if (argumentObj.get(paramName) != null) {
                    String paramValue = argumentObj.get(paramName).getAsString();
                    if (queryString.length() == 0) {
                        queryString.append(paramName).append("=").append(paramValue);
                    } else {
                        queryString.append("&").append(paramName).append("=").append(paramValue);
                    }
                } else {
                    if (isParamRequired) {
                        //todo: handle error
                    }
                }
            }
            if (queryString.length() > 0) {
                resourcePath.append("?").append(queryString);
            }

            List<String> pathParams = schemaMapping.getPathParams();
            String resourcePathString = resourcePath.toString();
            for (String pathParam : pathParams) {
                String paramValue = argumentObj.get(pathParam).getAsString();
                resourcePathString = resourcePathString.replace("{" + pathParam + "}", paramValue);
            }
            resourcePath.setLength(0);
            resourcePath.append(resourcePathString);

            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_URL_POSTFIX, resourcePath.toString());
            axis2MessageContext.setProperty(APIConstants.RESOURCE_METHOD, httpMethod.toUpperCase());
        }
    }

    public static void processHeaders(MessageContext messageContext, SchemaMapping schemaMapping, JsonObject jsonObject) {
        JsonObject paramsObj = jsonObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
        if (paramsObj != null && !paramsObj.isEmpty()) {
            JsonObject argumentObj = paramsObj.getAsJsonObject(APIConstants.MCP.ARGUMENTS_KEY);

            List<Param> headerParams = schemaMapping.getHeaderParams();
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            for (Param param : headerParams) {
                String paramName = param.getName();
                boolean isParamRequired = param.isRequired();

                if (argumentObj.get(paramName) != null) {
                    String paramValue = argumentObj.get(paramName).getAsString();
                    headers.put(paramName, paramValue);
                } else {
                    if (isParamRequired) {
                        //todo: handle error
                    }
                }
            }
        }
    }

    public static void processRequestBody(MessageContext messageContext, SchemaMapping schemaMapping, JsonObject jsonObject) {
        String contentType = schemaMapping.getContentType();

        JsonObject paramsObj = jsonObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
        if (paramsObj != null && !paramsObj.isEmpty()) {
            JsonObject argumentObj = paramsObj.getAsJsonObject(APIConstants.MCP.ARGUMENTS_KEY);
            if (schemaMapping.isHasBody()) {
                org.apache.axis2.context.MessageContext axis2MessageContext =
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext();

                if (APIConstants.APPLICATION_JSON_MEDIA_TYPE.equals(contentType)) {
                    JsonObject requestBody = argumentObj.get("requestBody").getAsJsonObject();

                    try {
                        JsonUtil.removeJsonPayload(axis2MessageContext);
                        JsonUtil.getNewJsonPayload(axis2MessageContext, requestBody.getAsJsonObject().toString(), true, true);
                        axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                        axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    } catch (AxisFault e) {
                        //todo: handle fault
                    }
                } else if (APIConstants.APPLICATION_XML_MEDIA_TYPE.equals(contentType)) {
                    //todo: handle xml payloads
                }
            }
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
