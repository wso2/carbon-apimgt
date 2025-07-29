package org.wso2.carbon.apimgt.gateway.mcp.request;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.exception.McpException;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.gateway.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import java.util.Map;

public class McpRequestProcessor {

    public static McpResponseDto processRequest(MessageContext messageContext, API matchedMcpApi, String requestBody,
                                                Map<String, String> additionalHeaders) {
        try {
            // Todo: is this needed? Req Body is already validated in MCP handler
            MCPUtils.parseAndValidateRequest(requestBody);
            // Todo: check subtype, implement MCP Server option
            JsonObject requestObject = JsonParser.parseString(requestBody).getAsJsonObject();
            String method = requestObject.get(APIConstants.MCP.RpcConstants.METHOD).getAsString();
            return MCPUtils.processInternalRequest(messageContext, matchedMcpApi, requestObject, method, additionalHeaders);
        } catch (McpException e) {
            return new McpResponseDto(e.toJsonRpcErrorPayload(), 200, null);
        }
    }

}
