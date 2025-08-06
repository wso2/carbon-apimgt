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

    public static McpResponseDto processRequest(MessageContext messageContext, API matchedMcpApi, McpRequest requestBody,
                                                Map<String, String> additionalHeaders) {

        // Todo: check subtype, implement MCP Server option
        String method = requestBody.getMethod();
        return MCPUtils.processInternalRequest(messageContext, matchedMcpApi, requestBody, method, additionalHeaders);
    }

}
