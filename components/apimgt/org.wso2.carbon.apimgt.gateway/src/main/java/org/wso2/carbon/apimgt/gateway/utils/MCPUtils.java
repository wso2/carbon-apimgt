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
import com.google.gson.JsonObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.exception.McpExceptionWithId;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequest;
import org.wso2.carbon.apimgt.gateway.mcp.request.Params;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.mcp.transformer.exception.MCPRequestResolverException;
import org.wso2.carbon.mcp.transformer.impl.PrefixBasedSchemaMappingParser;
import org.wso2.carbon.mcp.transformer.impl.RequestResolver;
import org.wso2.carbon.mcp.transformer.model.ResolvedRequest;
import org.wso2.carbon.mcp.transformer.model.SchemaMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCPUtils {
    private static final Log log = LogFactory.getLog(MCPUtils.class);

    public static boolean validateRequest(McpRequest request) throws McpException {
        String jsonRpcVersion = request.getJsonRpcVersion();
        if (StringUtils.isEmpty(jsonRpcVersion)) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing jsonrpc field");
        }
        if (!APIConstants.MCP.RpcConstants.JSON_RPC_VERSION.equals(jsonRpcVersion)) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Invalid JSON-RPC version");
        }

        String method = request.getMethod();
        if (StringUtils.isEmpty(method)) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing method field");
        }
        if (!APIConstants.MCP.ALLOWED_METHODS.contains(method)) {
            throw new McpException(APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_CODE,
                    APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_MESSAGE, "Method " + method + " not found");
        }

        Object id = request.getId();
        if (id == null && !APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED.equals(method)) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Missing id field");
        }
        return true;
    }

    /**
     * Processes the MCP requests for existing APIs and direct backends.
     *
     * @param matchedMcpApi     matched API in the gateway
     * @param requestObject     MCP request payload
     * @param method            MCP JSON RPC method
     * @return the response payload as a String
     */
    public static McpResponseDto processInternalRequest(MessageContext messageContext, API matchedMcpApi,
            McpRequest requestObject, String method) {
        try {
            Object id = -1;
            if (!method.contains("notifications/")) {
                id = requestObject.getId();
            }

            switch (method) {
                case APIConstants.MCP.METHOD_INITIALIZE:
                    validateInitializeRequest(id, requestObject);
                    return handleMcpInitialize(id, matchedMcpApi);
                case APIConstants.MCP.METHOD_TOOL_LIST:
                    return handleMcpToolList(id, matchedMcpApi, false);
                case APIConstants.MCP.METHOD_TOOL_CALL:
                    validateToolsCallRequest(requestObject, matchedMcpApi);
                    return handleMcpToolsCall(messageContext, id, matchedMcpApi, requestObject);
                case APIConstants.MCP.METHOD_PING:
                    return handleMcpPing(id);
                case APIConstants.MCP.METHOD_RESOURCES_LIST:
                    return new McpResponseDto(MCPPayloadGenerator.generateResourceListResponse(id), 200, null);
                case APIConstants.MCP.METHOD_RESOURCE_TEMPLATE_LIST:
                    return new McpResponseDto(MCPPayloadGenerator.generateResourceTemplateListResponse(id), 200, null);
                case APIConstants.MCP.METHOD_PROMPTS_LIST:
                    return new McpResponseDto(MCPPayloadGenerator.generatePromptListResponse(id), 200, null);
                case APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED:
                    // We don't need to send a reply when it's a notification
                    return null;
                default:
                    throw new McpException(APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_CODE,
                            APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_MESSAGE, "Method not found");
            }
        } catch (McpException e) {
            return new McpResponseDto(e.toJsonRpcErrorPayload(), 200, null);
        }
    }

    public static void validateInitializeRequest(Object id, McpRequest requestObject) throws McpException {
        if (requestObject.getParams() != null) {
            Params params = requestObject.getParams();
            String protocolVersion = params.getProtocolVersion();
            if (!StringUtils.isEmpty(protocolVersion)) {
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

    private static void validateToolsCallRequest(McpRequest mcpRequest, API matchedApi) throws McpException {
        Params params = mcpRequest.getParams();
        if (params != null) {
            String toolName = params.getToolName();
            if (StringUtils.isEmpty(toolName)) {
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

    private static McpResponseDto handleMcpToolsCall(MessageContext messageContext, Object id, API matchedApi,
        McpRequest mcpRequest) throws McpException {
        Params params = mcpRequest.getParams();
        if (params != null) {
            String toolName = params.getToolName();
            URLMapping extendedOperation = matchedApi.getUrlMappings()
                    .stream()
                    .filter(operation -> operation.getUrlPattern().equals(toolName))
                    .findFirst()
                    .orElse(null);
            String subType = matchedApi.getSubtype();
            transformMcpRequest(messageContext, id, extendedOperation, mcpRequest, subType);
        }
        return null;
    }

    private static McpResponseDto handleMcpPing(Object id) {
        return new McpResponseDto(MCPPayloadGenerator.generatePingResponse(id), 200, null);
    }

    private static void transformMcpRequest(MessageContext messageContext, Object id, URLMapping extendedOperation,
                                            McpRequest mcpRequest, String subType) throws McpException {
        if (extendedOperation != null) {
            BackendOperation backendOperation = null;
            if (APIConstants.API_SUBTYPE_EXISTING_API.equals(subType)) {
                APIOperationMapping apiOperationMapping = extendedOperation.getApiOperationMapping();
                if (apiOperationMapping != null) {
                    backendOperation = apiOperationMapping.getBackendOperation();
                }
            } else if (APIConstants.API_SUBTYPE_DIRECT_BACKEND.equals(subType)) {
                BackendOperationMapping backendOperationMapping = extendedOperation.getBackendOperationMapping();
                if (backendOperationMapping != null) {
                    backendOperation = backendOperationMapping.getBackendOperation();
                }
            }

            if (backendOperation != null) {
                //process schema
                String schemaDefinition = extendedOperation.getSchemaDefinition();

                PrefixBasedSchemaMappingParser parser = new PrefixBasedSchemaMappingParser();
                RequestResolver requestResolver = new RequestResolver(parser);
                Map<String, Object> arguments = new HashMap<>();
                if (mcpRequest.getParams() != null && mcpRequest.getParams().getArguments() != null) {
                    arguments = mcpRequest.getParams().getArguments();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No parameter arguments found in the MCP request");
                    }
                }

                try {
                    ResolvedRequest resolvedRequest = requestResolver.resolve(schemaDefinition, arguments);

                    //process resource path including query and path params
                    processResource(messageContext, resolvedRequest, backendOperation);

                    //process headers
                    processHeaders(messageContext, resolvedRequest);

                    //process request body
                    SchemaMapping parsedSchemaDefinition = parser.parse(schemaDefinition);
                    processRequestBody(messageContext, resolvedRequest, parsedSchemaDefinition.isHasBody(), parsedSchemaDefinition.getContentType());

                    //set received id to msg context
                    messageContext.setProperty("RECEIVED_MCP_ID", id);
                } catch (MCPRequestResolverException e) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE,
                            APIConstants.MCP.RpcConstants.INTERNAL_ERROR_MESSAGE, e.getMessage());
                }
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE,
                        APIConstants.MCP.RpcConstants.INTERNAL_ERROR_MESSAGE, "No matched tool found");
            }
        }
    }

    private static void processResource(MessageContext messageContext, ResolvedRequest resolvedRequest,
                                        BackendOperation backendOperation) throws McpException {
        //remove fully qualified path
        org.wso2.carbon.apimgt.api.APIConstants.SupportedHTTPVerbs httpMethod = backendOperation.getVerb();
        String target = backendOperation.getTarget();

        StringBuilder resourcePath = new StringBuilder();
        StringBuilder queryString = new StringBuilder();
        resourcePath.append(target);
        Map<String, Object> queryParams = resolvedRequest.getQueryParams();

        if (queryParams != null && !queryParams.isEmpty()) {
            for (Object key : queryParams.keySet()) {
                String paramName = key.toString();
                Object paramValueObj = queryParams.get(paramName);
                String paramValue = paramValueObj != null ? paramValueObj.toString() : "";
                if (!StringUtils.isEmpty(paramValue)) {
                    if (queryString.length() == 0) {
                        queryString.append(paramName).append("=").append(paramValue);
                    } else {
                        queryString.append("&").append(paramName).append("=").append(paramValue);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Query param " + paramName + " is defined but has no value, hence skipped");
                    }
                }
            }
        }

        if (queryString.length() > 0) {
            resourcePath.append("?").append(queryString);
        }

        Map<String, Object> pathParams = resolvedRequest.getPathParams();
        String resourcePathString = resourcePath.toString();

        if (pathParams != null && !queryParams.isEmpty()) {
            for (Object key : queryParams.keySet()) {
                Object paramValueObj = queryParams.get(key);
                if (paramValueObj == null) {
                    //param value obj cannot be null at this point
                    throw new McpException(APIConstants.MCP.RpcConstants.INVALID_PARAMS_CODE,
                            APIConstants.MCP.RpcConstants.INVALID_PARAMS_MESSAGE,
                            "Required path param " + key + " is not defined");
                }
                String paramValue = paramValueObj.toString();
                resourcePathString = resourcePathString.replace("{" + key + "}", paramValue);
            }
        }

        resourcePath.setLength(0);
        resourcePath.append(resourcePathString);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        axis2MessageContext.setProperty(APIMgtGatewayConstants.REST_URL_POSTFIX, resourcePath.toString());
        axis2MessageContext.setProperty(APIConstants.RESOURCE_METHOD, httpMethod.toString().toUpperCase());
    }

    public static void processHeaders(MessageContext messageContext, ResolvedRequest resolvedRequest) {
        Map<String, Object> headerParams = resolvedRequest.getHeaderParams();
        if (headerParams != null && !headerParams.isEmpty()) {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            for (Object key : headerParams.keySet()) {
                String paramName = (String) key;
                Object paramValueObj = headerParams.get(paramName);
                if (paramValueObj != null) {
                    String paramValue = paramValueObj.toString();
                    headers.put(paramName, paramValue);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Header param " + paramName + " is defined but has no value, hence skipped");
                    }
                }
            }
        }
    }

    public static void processRequestBody(MessageContext messageContext, ResolvedRequest resolvedRequest,
                                          boolean hasBody, String contentType) throws McpException {

        if (hasBody) {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            JsonObject payload = resolvedRequest.getBody();

            if (APIConstants.APPLICATION_JSON_MEDIA_TYPE.equals(contentType)) {
                try {
                    JsonUtil.removeJsonPayload(axis2MessageContext);
                    JsonUtil.getNewJsonPayload(axis2MessageContext, new Gson().toJson(payload), true, true);
                    axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                            APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                } catch (AxisFault e) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE,
                            APIConstants.MCP.RpcConstants.INTERNAL_ERROR_MESSAGE,
                            "Failed to process JSON request body: " + e.getMessage());
                }
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE,
                        APIConstants.MCP.RpcConstants.INTERNAL_ERROR_MESSAGE,
                        "Unsupported content type: " + contentType);
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

    /**
     * This method handles failures
     *
     * @param messageContext    message context of the request
     * @param responseDto      payload of the error
     */
    public static void handleMCPFailure(MessageContext messageContext, McpResponseDto responseDto) {
        messageContext.setProperty("MCP_ID", null);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, responseDto.getResponse());
        messageContext.setProperty("MCP_ERROR_CODE", responseDto.getStatusCode());
        Mediator sequence = messageContext.getSequence(APIConstants.MCP.MCP_FAILURE_HANDLER);
        if (sequence != null && !sequence.mediate(messageContext)) {
            return;
        }

        // Fallback in case failure sequence is not defined
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            JsonUtil.removeJsonPayload(axis2MC);
            JsonUtil.getNewJsonPayload(axis2MC, MCPPayloadGenerator.getErrorResponse(null,
                    responseDto.getStatusCode(), "MCP Failure", responseDto.getResponse()), true, true);
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MC.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
        } catch (AxisFault e) {
            log.warn("Failed to set MCP error JSON payload", e);
        }
        Utils.sendFault(messageContext, responseDto.getStatusCode());
    }

    /**
     * Get the Gateway server URL from the supported vhosts of the API, prioritizing the value passed in
     * host header of the request
     *
     * @param hostHeader  Host header of the request
     * @param contextPath Context path of the matched API
     * @return Gateway server URL
     */
    public static String getGatewayServerURL(String hostHeader, String contextPath) {
        String serverURL = null;
        String tenantDomain = GatewayUtils.getTenantDomain();
        Map<String, Map<String, API>> tenantAPIMap = DataHolder.getInstance().getTenantAPIMap();
        Map<String, API> contextApiMap = tenantAPIMap.get(tenantDomain);
        if (contextApiMap != null) {
            API api = contextApiMap.get(contextPath);
            if (api == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No matching API found for context: " + contextPath + " in tenant: " + tenantDomain);
                }
                return null;
            }

            List<VHost> gwVhosts = api.getVhosts();

            if (!StringUtils.isEmpty(hostHeader)) {
                for (VHost vHost: gwVhosts) {
                    if (vHost.getHost().equals(hostHeader)) {
                        serverURL = vHost.getHttpsUrl();
                        break;
                    }
                }
            }

            // If server URL is not resolved using host header, pick the first vhost as the resource endpoint
            if (StringUtils.isEmpty(serverURL)) {
                if (gwVhosts != null && !gwVhosts.isEmpty()) {
                    serverURL = gwVhosts.get(0).getHttpsUrl();
                }
            }
        }
        return serverURL;
    }

}
