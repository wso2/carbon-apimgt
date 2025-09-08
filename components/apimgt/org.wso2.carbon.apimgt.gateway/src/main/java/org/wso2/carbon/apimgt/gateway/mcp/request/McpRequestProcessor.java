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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.mcp.response.McpResponseDto;
import org.wso2.carbon.apimgt.gateway.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

public class McpRequestProcessor {
    private static final Log log = LogFactory.getLog(McpRequestProcessor.class);

    public static McpResponseDto processRequest(MessageContext messageContext, API matchedMcpApi,
                                                McpRequest requestBody) {

        if (log.isDebugEnabled()) {
            log.debug("Processing MCP request: " + requestBody.getMethod() + " for API: " +
                    matchedMcpApi.getName() + "-" + matchedMcpApi.getVersion());
        }

        String method = requestBody.getMethod();
        McpResponseDto dto = MCPUtils.processInternalRequest(messageContext, matchedMcpApi, requestBody, method);

        if (log.isDebugEnabled()) {
            log.debug("Successfully processed MCP request: " + requestBody.getMethod() + " for API: " +
                    matchedMcpApi.getName() + "-" + matchedMcpApi.getVersion());
        }

        return dto;
    }

}
