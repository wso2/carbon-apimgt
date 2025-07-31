package org.wso2.carbon.apimgt.gateway.handlers.mcp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.mcp.request.McpRequestProcessor;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.MCPPayloadGenerator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.HashMap;
import java.util.Map;

/**
 * Not Used yet
 */

public class McpHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(McpHandler.class);

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing MCP handler instance");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        String path = (String) messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);

        if (path.equals(APIMgtGatewayConstants.MCP_RESOURCE) && httpMethod.equals(APIConstants.HTTP_POST)) {
            handleMcpRequest(messageContext);
        } else if (path.equals(APIMgtGatewayConstants.MCP_WELL_KNOWN_RESOURCE) && httpMethod.equals(APIConstants.HTTP_GET)) {

        } else {
            //TODO: handle unsupported MCP Method
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        String mcpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD);
        if (APIConstants.MCP.METHOD_TOOL_CALL.equals(mcpMethod)) {
            try {
                handleMcpResponse(messageContext);
            } catch (McpException e) {
                //todo: handle exception
            }
        }
        return true;
    }

    private void handleMcpRequest(MessageContext messageContext) {
        API matchedAPI = GatewayUtils.getAPI(messageContext);
        JsonObject requestBody = (JsonObject) messageContext.getProperty(APIMgtGatewayConstants.MCP_REQUEST_BODY);
        String mcpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.MCP_METHOD);

        // TODO : Check application of below
        Map<String, String> additionalHeaders = new HashMap<>();

        McpResponseDto mcpResponse = McpRequestProcessor.processRequest(messageContext, matchedAPI, new Gson().toJson(requestBody),
                additionalHeaders);

        //loopback MCP response in case of initialize or tools/list request
        if (APIConstants.MCP.METHOD_INITIALIZE.equals(mcpMethod) || APIConstants.MCP.METHOD_TOOL_LIST.equals(mcpMethod)) {
            messageContext.setProperty("MCP_PROCESSED", "true");
            org.apache.axis2.context.MessageContext axis2MessageContext =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            if (mcpResponse != null) {
                try {
                    JsonUtil.removeJsonPayload(axis2MessageContext);
                    JsonUtil.getNewJsonPayload(axis2MessageContext, mcpResponse.getResponse(), true, true);
                    axis2MessageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, mcpResponse.getStatusCode());
                } catch (AxisFault e) {
                    log.error("Error while generating mcp payload " + axis2MessageContext.getLogIDString(), e);
                }
            } else {
                //todo : check further
                JsonUtil.removeJsonPayload(axis2MessageContext);
                axis2MessageContext.setProperty(APIMgtGatewayConstants.HTTP_SC, HttpStatus.SC_NO_CONTENT);
            }
        } else {
            messageContext.setProperty("MCP_PROCESSED", "false");
        }
    }

    private void handleMcpResponse(MessageContext messageContext) throws McpException {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String contentType = (String) axis2MessageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        if (APIConstants.APPLICATION_JSON_MEDIA_TYPE.equals(contentType)) {
            buildMCPResponse(messageContext);
        }
    }

    private void buildMCPResponse(MessageContext messageContext) throws McpException {
        if (log.isDebugEnabled()) {
            log.debug("Handling MCP response");
        }
        String messageBody;
        try {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            RelayUtils.buildMessage(axis2MC);
            if (JsonUtil.hasAJsonPayload(axis2MC)) {
                messageBody = JsonUtil.jsonPayloadToString(axis2MC);

                //todo: handle mcp response id properly
                String mcpResponse = MCPPayloadGenerator.generateMCPResponsePayload(1, false, messageBody);

                JsonUtil.removeJsonPayload(axis2MC);
                JsonUtil.getNewJsonPayload(axis2MC, mcpResponse, true, true);
            } else {
                //todo: handle other response content types
            }
        } catch (Exception e) {
            throw new McpException(APIConstants.MCP.RpcConstants.INTERNAL_ERROR_CODE,
                    APIConstants.MCP.RpcConstants.INTERNAL_ERROR_MESSAGE, "Internal error while processing response");
        }
    }
}
