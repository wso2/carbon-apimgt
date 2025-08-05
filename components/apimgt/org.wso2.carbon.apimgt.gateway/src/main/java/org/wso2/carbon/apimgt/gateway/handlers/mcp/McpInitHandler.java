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

import com.google.gson.JsonObject;
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
import org.wso2.carbon.apimgt.api.model.BackendAPIOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendOperation;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

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
            boolean isNoAuthMCPRequest = isNoAuthMCPRequest(buildMCPRequest(messageContext));
            messageContext.setProperty(APIMgtGatewayConstants.MCP_NO_AUTH_REQUEST, isNoAuthMCPRequest);
        } catch (McpException e) {
            // TODO: Handle MCP specific errors
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
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
        try {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            RelayUtils.buildMessage(axis2MC);
            if (JsonUtil.hasAJsonPayload(axis2MC)) {
                messageBody = JsonUtil.jsonPayloadToString(axis2MC);
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                        APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "No JSON-RPC payload found");
            }
        } catch (Exception e) {
            throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                    APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE, "Invalid or Malformed JSON-RPC payload found");
        }
        JsonObject requestObject =  MCPUtils.parseAndValidateRequest(messageBody);
        String method = requestObject.get(APIConstants.MCP.RpcConstants.METHOD).getAsString();
        messageContext.setProperty(APIMgtGatewayConstants.MCP_METHOD, method);
        messageContext.setProperty(APIMgtGatewayConstants.MCP_REQUEST_BODY, requestObject);
        messageContext.setProperty(APIMgtGatewayConstants.API_TYPE, APIConstants.API_TYPE_MCP);

        // TODO: Add elected resource - tool that is invoked
        if (StringUtils.equals(method, APIConstants.MCP.METHOD_TOOL_CALL)) {
            JsonObject params = requestObject.getAsJsonObject(APIConstants.MCP.PARAMS_KEY);
            String toolName = params.get(APIConstants.MCP.TOOL_NAME_KEY).getAsString();
            API api = GatewayUtils.getAPI(messageContext);
            URLMapping extendedOperation = api.getUrlMappings()
                    .stream()
                    .filter(operation -> operation.getUrlPattern().equals(toolName))
                    .findFirst()
                    .orElse(null);

            if (extendedOperation != null) {
                BackendAPIOperationMapping backendAPIOperationMapping = extendedOperation.getBackendOperationMapping();
                if (backendAPIOperationMapping != null) {
                    BackendOperation backendOperation = backendAPIOperationMapping.getBackendOperation();
                    messageContext.setProperty("MCP_HTTP_METHOD", backendOperation.getVerb());
                    messageContext.setProperty("MCP_API_ELECTED_RESOURCE", backendOperation.getTarget());
                }
            }
        }

        return method;
    }

    private boolean isNoAuthMCPRequest(String method) throws McpException {

        switch (method) {
            case APIConstants.MCP.METHOD_INITIALIZE:
            case APIConstants.MCP.METHOD_PING:
            case APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED:
                return true;

            case APIConstants.MCP.METHOD_TOOL_LIST:
            case APIConstants.MCP.METHOD_TOOL_CALL:
            case APIConstants.MCP.METHOD_RESOURCES_LIST:
            case APIConstants.MCP.METHOD_RESOURCE_TEMPLATE_LIST:
            case APIConstants.MCP.METHOD_PROMPTS_LIST:
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
