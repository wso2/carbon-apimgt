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

package org.wso2.carbon.apimgt.gateway.handlers.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.mcp.request.MCPRequestDeserializer;
import org.wso2.carbon.apimgt.gateway.mcp.request.Params;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequest;
import org.wso2.carbon.apimgt.gateway.mcp.request.ParamsDeserializer;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class McpInitHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(McpInitHandler.class);

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing MCP Init Handler instance");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        try {
            String path = (String) messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
            String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);

            String httpsPort = System.getProperty(APIMgtGatewayConstants.HTTPS_NIO_PORT);
            if (!StringUtils.isEmpty(httpsPort)) {
                messageContext.setProperty("uri.var.httpsPort", httpsPort);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("https.nio.port could not be resolved from System properties, hence default " +
                            "value was set");
                }
                messageContext.setProperty("uri.var.httpsPort", "8243");
            }

            if (StringUtils.startsWith(path, APIMgtGatewayConstants.MCP_WELL_KNOWN_RESOURCE) &&
                    StringUtils.equals(APIConstants.HTTP_GET, httpMethod)) {
                messageContext.setProperty(APIMgtGatewayConstants.MCP_NO_AUTH_REQUEST, true);
            } else if (StringUtils.startsWith(path, APIMgtGatewayConstants.MCP_RESOURCE) &&
                    StringUtils.equals(APIConstants.HTTP_GET, httpMethod)) {
                // No JSON-RPC payload in GET requests to /mcp resource, hence hardcoding no auth to false
                messageContext.setProperty(APIMgtGatewayConstants.MCP_NO_AUTH_REQUEST, false);
            } else {
                boolean isNoAuthMCPRequest = isNoAuthMCPRequest(buildMCPRequest(messageContext));
                messageContext.setProperty(APIMgtGatewayConstants.MCP_NO_AUTH_REQUEST, isNoAuthMCPRequest);
            }
        } catch (McpException e) {
            log.error("MCP init failed: " + String.valueOf(e.getData()), e);
            MCPUtils.handleMCPFailure(messageContext, new McpResponseDto(e.getErrorMessage(), e.getErrorCode(), null));
            return false;
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers == null) {
            headers = new HashMap<>();
            axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }

        Object exposeHeadersList = headers.get(APIConstants.CORSHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
        String exposeHeaders = APIConstants.MCP.HEADER_MCP_SESSION_ID;
        if (exposeHeadersList instanceof String) {
            exposeHeaders = (String) exposeHeadersList;
            if (!StringUtils.isEmpty(exposeHeaders) && !exposeHeaders.contains(APIConstants.MCP.HEADER_MCP_SESSION_ID)) {
                exposeHeaders += "," + APIConstants.MCP.HEADER_MCP_SESSION_ID;
            }
        }
        headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
        return true;
    }

    /**
     * This method is used to set API related parameters to the message context.
     *
     * @param messageContext The message context to which the parameters are set
     * @return MCP Method involved with the request
     */
    private String buildMCPRequest(MessageContext messageContext) throws McpException {
        if (log.isDebugEnabled()) {
            log.debug("Handling MCP request");
        }
        String messageBody;
        String method;
        try {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            RelayUtils.buildMessage(axis2MC);
            if (JsonUtil.hasAJsonPayload(axis2MC)) {
                messageBody = JsonUtil.jsonPayloadToString(axis2MC);

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(McpRequest.class, new MCPRequestDeserializer())
                        .registerTypeAdapter(Params.class, new ParamsDeserializer())
                        .create();
                McpRequest request = gson.fromJson(messageBody, McpRequest.class);
                if (log.isDebugEnabled()) {
                    log.debug("Deserialized MCP request: " + request);
                }
                if (!MCPUtils.validateRequest(request)) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                            APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Invalid Request");
                }

                method = request.getMethod();
                messageContext.setProperty(APIMgtGatewayConstants.MCP_METHOD, method);
                messageContext.setProperty(APIMgtGatewayConstants.MCP_REQUEST_BODY, request);

                if (StringUtils.equals(method, APIConstants.MCP.METHOD_TOOL_CALL)) {
                    Params params = request.getParams();
                    String toolName = params.getToolName();
                    messageContext.setProperty(APIMgtGatewayConstants.MCP_TOOL_NAME, toolName);
                    API api = GatewayUtils.getAPI(messageContext);
                    URLMapping extendedOperation = api.getUrlMappings()
                            .stream()
                            .filter(operation -> operation.getUrlPattern().equals(toolName))
                            .findFirst()
                            .orElse(null);

                    BackendOperation backendOperation = null;
                    if (extendedOperation != null) { //direct_endpoint
                        BackendOperationMapping backendAPIOperationMapping = extendedOperation.getBackendOperationMapping();
                        if (backendAPIOperationMapping != null) {
                            backendOperation = backendAPIOperationMapping.getBackendOperation();
                        } else { //existing_api
                            APIOperationMapping existingAPIOperationMapping = extendedOperation.getApiOperationMapping();
                            if (existingAPIOperationMapping != null) {
                                backendOperation = existingAPIOperationMapping.getBackendOperation();
                            }
                        }
                        if (backendOperation != null) {
                            messageContext.setProperty("MCP_HTTP_METHOD", backendOperation.getVerb());
                            messageContext.setProperty("MCP_API_ELECTED_RESOURCE", backendOperation.getTarget());
                        }
                    }
                }
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                        APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "No JSON-RPC payload found");
            }
        } catch (IOException | JsonSyntaxException | XMLStreamException e) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Invalid or Malformed JSON-RPC payload found");
        }
        return method;
    }

    private boolean isNoAuthMCPRequest(String method) throws McpException {

        switch (method) {
            case APIConstants.MCP.METHOD_INITIALIZE:
            case APIConstants.MCP.METHOD_PING:
            case APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED:
            case APIConstants.MCP.METHOD_RESOURCES_LIST:
            case APIConstants.MCP.METHOD_RESOURCE_TEMPLATE_LIST:
            case APIConstants.MCP.METHOD_PROMPTS_LIST:
                return true;

            case APIConstants.MCP.METHOD_TOOL_LIST:
            case APIConstants.MCP.METHOD_TOOL_CALL:
                return false;

            default:
                throw new McpException(
                        APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_CODE,
                        APIConstants.MCP.RpcConstants.METHOD_NOT_FOUND_MESSAGE,
                        "Method not found"
                );
        }
    }
}
