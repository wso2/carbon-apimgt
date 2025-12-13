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

package org.wso2.carbon.apimgt.gateway.mediators;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.OAuthProtectedResourceDTO;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequest;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequestProcessor;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.MCPPayloadGenerator;
import org.wso2.carbon.apimgt.gateway.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Mediator for handling MCP (Model Context Protocol) requests and responses in the API Gateway.
 * This mediator processes MCP JSON-RPC messages, handles initialization, tool listing,
 * and tool calls, and transforms responses to MCP format.
 */
public class McpMediator extends AbstractMediator implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(McpMediator.class);
    private String mcpDirection = "";
    private static final String MCP_PROCESSED = "MCP_PROCESSED";
    private static final String IN_FLOW = "IN";
    private static final String OUT_FLOW = "OUT";
    private static final Pattern validHostHeaderPattern =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9.-]*(:\\d{1,5})?$");

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (log.isDebugEnabled()) {
            log.debug("MCPMediator: Initialized.");
        }
    }

    @Override
    public void destroy() {

    }

    public String getMcpDirection() {
        return mcpDirection;
    }

    public void setMcpDirection(String mcpDirection) {
        this.mcpDirection = mcpDirection;
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        String path = (String) messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        API matchedAPI = GatewayUtils.getAPI(messageContext);
        if (matchedAPI == null) {
            log.error("No API matched for the request: " + path + " with method: " + httpMethod);
            return false;
        }
        String subType = matchedAPI.getSubtype();
        String mcpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD);

        if (IN_FLOW.equals(mcpDirection)) {
            if (StringUtils.equals(subType, APIConstants.API_SUBTYPE_SERVER_PROXY) &&
                    !StringUtils.equals(APIConstants.MCP.METHOD_TOOL_LIST, mcpMethod)) {
                // For server proxy APIs, we do not handle MCP requests
                log.debug("Skipping MCP mediation for server proxy API: " + matchedAPI.getName() + ":" +
                        matchedAPI.getVersion());
                return true;
            }
            if (path.startsWith(APIMgtGatewayConstants.MCP_RESOURCE) && httpMethod.equals(APIConstants.HTTP_POST)) {
                handleMcpRequest(messageContext, matchedAPI);
            } else if (path.startsWith(APIMgtGatewayConstants.MCP_RESOURCE) &&
                    httpMethod.equals(APIConstants.HTTP_GET)) {
                McpResponseDto errorResponse = new McpResponseDto("Server-Sent Events (SSE) not supported",
                        405, null);
                MCPUtils.handleMCPFailure(messageContext, errorResponse);
                return false;
            } else if (path.startsWith(APIMgtGatewayConstants.MCP_WELL_KNOWN_RESOURCE) &&
                    httpMethod.equals(APIConstants.HTTP_GET)) {
                return handleProtectedResourceMetadataResponse(messageContext, matchedAPI);
            }
        } else if (OUT_FLOW.equals(mcpDirection)) {
            if (StringUtils.equals(subType, APIConstants.API_SUBTYPE_SERVER_PROXY)) {
                // For server proxy APIs, we do not handle MCP requests
                log.debug("Skipping MCP mediation for server proxy API: " + matchedAPI.getName() + ":" +
                        matchedAPI.getVersion());
                return true;
            }

            try {
                handleMcpResponse(messageContext);
            } catch (McpException e) {
                log.error("Error while handling MCP response", e);
                MCPUtils.handleMCPFailure(messageContext, new McpResponseDto(e.getErrorMessage(), e.getErrorCode(), null));
                return false;
            }
        }
        return true;
    }

    private void handleMcpRequest(MessageContext messageContext, API matchedAPI) {
        McpRequest requestBody = (McpRequest) messageContext.getProperty(APIMgtGatewayConstants.MCP_REQUEST_BODY);
        String mcpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        McpResponseDto mcpResponse = McpRequestProcessor.processRequest(messageContext, matchedAPI, requestBody);
        if (APIConstants.MCP.METHOD_INITIALIZE.equals(mcpMethod) || APIConstants.MCP.METHOD_TOOL_LIST.equals(mcpMethod)
            || APIConstants.MCP.METHOD_PING.equals(mcpMethod) || APIConstants.MCP.METHOD_PROMPTS_LIST.equals(mcpMethod)
            || (APIConstants.MCP.METHOD_TOOL_CALL.equals(mcpMethod) && mcpResponse != null)) {
            messageContext.setProperty(MCP_PROCESSED, "true");
            if (mcpResponse != null) {
                try {
                    JsonUtil.removeJsonPayload(axis2MessageContext);
                    JsonUtil.getNewJsonPayload(axis2MessageContext, mcpResponse.getResponse(), true, true);
                    axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                            APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, mcpResponse.getStatusCode());
                    axis2MessageContext.setProperty(APIMgtGatewayConstants.MCP_METHOD, mcpMethod);
                } catch (AxisFault e) {
                    log.error("Error while generating mcp payload " + axis2MessageContext.getLogIDString(), e);
                }
            } else {
                // If no response is generated, set the HTTP status to 204 No Content
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, HttpStatus.SC_NO_CONTENT);
            }
        } else if (StringUtils.equals(mcpMethod, APIConstants.MCP.METHOD_NOTIFICATION_INITIALIZED)) {
            JsonUtil.removeJsonPayload(axis2MessageContext);
            messageContext.setProperty(MCP_PROCESSED, "true");
            axis2MessageContext.setProperty(APIConstants.NO_ENTITY_BODY, true);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, HttpStatus.SC_ACCEPTED);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.MCP_METHOD, mcpMethod);
        }
    }

    private boolean handleProtectedResourceMetadataResponse(MessageContext messageContext, API matchedAPI) {
        OAuthProtectedResourceDTO oAuthProtectedResourceDTO = new OAuthProtectedResourceDTO();
        List<String> keyManagers = DataHolder.getInstance().getKeyManagersFromUUID(matchedAPI.getUuid());
        boolean skipAuthServersAttribute = false;
        if (keyManagers.isEmpty()) {
            log.error("No Key Managers found for MCP Server: " + matchedAPI.getUuid());
            skipAuthServersAttribute = true;
        }
        if (keyManagers.size() > 1) {
            log.error("Multiple Key Managers found for MCP Server: " + matchedAPI.getUuid() + ".");
            skipAuthServersAttribute = true;
        }

        // Derive the outward facing host and port from host header
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String hostHeader = headers != null ? (String) headers.get(APIMgtGatewayConstants.HOST) : null;
        if (StringUtils.isBlank(hostHeader) || !validHostHeaderPattern.matcher(hostHeader).matches()) {
            if (log.isDebugEnabled()) {
                log.debug("Missing or malformed host header in request.Extracting host header from config.");
            }
            hostHeader = APIUtil.getHostAddress();
        }


        String contextPath = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String serverURL = MCPUtils.getGatewayServerURL(hostHeader, contextPath);

        if (StringUtils.isEmpty(serverURL)) {
            log.error("Error while generating mcp payload for resource metadata");
            return false;
        }

        String resourceURL = serverURL + contextPath + APIMgtGatewayConstants.MCP_RESOURCE;
        oAuthProtectedResourceDTO.setResource(resourceURL);

        if (APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS.equals(keyManagers.get(0))) {
            Map<String, KeyManagerDto> keyManagerMap =
                    KeyManagerHolder.getTenantKeyManagers(matchedAPI.getOrganization());
            if (keyManagerMap.size() > 1) {
                log.error("Multiple Key Managers found for MCP Server: " + matchedAPI.getUuid() + ".");
            } else {
                oAuthProtectedResourceDTO.addAuthorizationServer(keyManagerMap.values().iterator().next().getIssuer());
            }
        } else if (!skipAuthServersAttribute) {
            KeyManagerDto keyManager =
                    KeyManagerHolder.getKeyManagerByName(matchedAPI.getOrganization(), keyManagers.get(0));
            if (keyManager != null) {
                oAuthProtectedResourceDTO.addAuthorizationServer(keyManager.getIssuer());
            } else {
                log.error("Key Manager: " + keyManagers.get(0) + " not found for MCP Server: " +
                        matchedAPI.getUuid() + ".");
            }
        }

        oAuthProtectedResourceDTO.addScopesSupported(getAllScopes(matchedAPI));

        messageContext.setProperty(MCP_PROCESSED, "true");
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, new Gson().toJson(oAuthProtectedResourceDTO),
                    true, true);
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, HttpStatus.SC_OK);
            axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
        } catch (AxisFault e) {
            log.error("Error while generating mcp payload " + axis2MessageContext.getLogIDString(), e);
            return false;
        }
        return true;
    }

    private void handleMcpResponse(MessageContext messageContext) throws McpException {
        String mcpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD);
        if (APIConstants.MCP.METHOD_TOOL_CALL.equals(mcpMethod)) {
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            String contentType = (String) axis2MessageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
            if (contentType != null && contentType.toLowerCase().contains(APIConstants.APPLICATION_JSON_MEDIA_TYPE)) {
                Object statusCodeObject = axis2MessageContext.getProperty(APIMgtGatewayConstants.HTTP_SC);
                int statusCode = 0;
                if (statusCodeObject instanceof String) {
                    String scString = ((String) statusCodeObject).trim();
                    if (StringUtils.isNumeric(scString)) {
                        statusCode = Integer.parseInt(scString);
                    } else {
                        log.warn("Skipping non-numeric HTTP status in axis2 context: " + scString);
                    }
                } else if (null != statusCodeObject) {
                    statusCode = (Integer) statusCodeObject;
                }
                Object id = messageContext.getProperty(APIConstants.MCP.RECEIVED_MCP_ID);

                try {
                    RelayUtils.buildMessage(axis2MessageContext);
                } catch (XMLStreamException | IOException  e) {
                    throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE, "Error while building " +
                            "message from the axis2 message context", e);
                }

                boolean isError = !isSuccessResponse(statusCode);
                String messageBody;
                if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                    messageBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
                } else {
                    messageBody = isError ? "Error occurred during tool call. HTTP Status Code: " + statusCode : "";
                }

                buildMCPResponse(messageContext, id, isError, messageBody);
            } else {
                throw new McpException(APIConstants.MCP.RpcConstants.INVALID_REQUEST_CODE,
                        APIConstants.MCP.RpcConstants.INVALID_REQUEST_MESSAGE,
                        "Unsupported content type in the response. Expected: application/json, Found: " + contentType);
            }
        }
    }

    private boolean isSuccessResponse(int statusCode) {
        return (statusCode >= 200 && statusCode < 300);
    }

    private void buildMCPResponse(MessageContext messageContext, Object id, boolean isError, String messageBody)
            throws McpException {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        try {
            String mcpResponse = MCPPayloadGenerator.generateMCPResponsePayload(id, isError, messageBody);
            JsonUtil.removeJsonPayload(axis2MessageContext);
            JsonUtil.getNewJsonPayload(axis2MessageContext, mcpResponse, true, true);
            axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE);

            // for JSON-RPC compliance, set HTTP_SC to 200 for all MCP responses
            axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, 200);
            axis2MessageContext.removeProperty(APIConstants.NO_ENTITY_BODY);
        } catch (Exception e) {
            throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE, APIConstants.MCP.RpcConstants.
                    INTERNAL_ERROR_MESSAGE, e);
        }
    }

    /**
     * Returns a list of all applicable scopes by concatenating all scopes attached to each URLMapping of the API.
     * @param api The API entity
     * @return List of all scopes
     */
    public static List<String> getAllScopes(API api) {
        List<String> allScopes = new ArrayList<>();
        if (api != null && api.getResources() != null) {
            for (URLMapping urlMapping : api.getResources()) {
                if (urlMapping.getScopes() != null) {
                    allScopes.addAll(urlMapping.getScopes());
                }
            }
        }
        return allScopes;
    }
}
