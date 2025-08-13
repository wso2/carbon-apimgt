/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Initializes an MCP server and fetches the available tools via JSON-RPC.
 */
public class MCPInitializerAndToolFetcher {

    private static final Log log = LogFactory.getLog(MCPInitializerAndToolFetcher.class);

    private final String mcpServerUrl;
    private final String authHeaderName;
    private final String authHeaderValue;
    private final boolean secure;

    public MCPInitializerAndToolFetcher(String mcpServerURL, String header, String value, boolean isSecure) {

        this.mcpServerUrl = mcpServerURL;
        this.authHeaderName = header;
        this.authHeaderValue = value;
        this.secure = isSecure;
    }

    /**
     * Initializes the server and returns its tool list.
     *
     * @return non-null array of tools (empty if none)
     * @throws APIManagementException on URL, I/O, or protocol errors
     */
    public JSONObject initializeAndFetchTools() throws APIManagementException {

        URL endpoint;
        try {
            endpoint = new URL(mcpServerUrl);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Invalid MCP server URL: " + mcpServerUrl, e);
        }

        try (CloseableHttpClient httpClient =
                     (CloseableHttpClient) APIUtil.getHttpClient(endpoint.getPort(), endpoint.getProtocol())) {

            // 1) initialize
            JSONObject initializePayload = buildInitializePayload();
            JSONObject initializeResponse = sendJsonRpcRequest(httpClient, mcpServerUrl, initializePayload, null);
            JSONObject initializeResult = parseJsonRpcResult(initializeResponse.getString(APIConstants.MCP.BODY_KEY));

            if (initializeResult == null) {
                throw new APIManagementException("Failed to initialize MCP server: result is null");
            }

            String sessionId = initializeResponse.optString(APIConstants.MCP.SESSION_ID_KEY, null);
            if (log.isDebugEnabled()) {
                log.debug("MCP initialization succeeded; sessionId=" + sessionId);
            }

            // 2) tools/list
            JSONObject toolsPayload = buildToolsListPayload(sessionId);
            JSONObject toolsResponse = sendJsonRpcRequest(httpClient, mcpServerUrl, toolsPayload, sessionId);

            return parseJsonRpcResult(toolsResponse.getString(APIConstants.MCP.BODY_KEY));
        } catch (APIManagementException e) {
            throw e;
        } catch (Exception e) {
            throw new APIManagementException("Error during MCP interaction: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the JSON-RPC initialize payload.
     */
    private JSONObject buildInitializePayload() {

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.MCP.RpcConstants.JSON_RPC, APIConstants.MCP.RpcConstants.JSON_RPC_VERSION);
        payload.put(APIConstants.MCP.RpcConstants.ID, 1);
        payload.put(APIConstants.MCP.RpcConstants.METHOD, APIConstants.MCP.METHOD_INITIALIZE);

        JSONObject params = new JSONObject();
        params.put(APIConstants.MCP.PROTOCOL_VERSION_KEY, APIConstants.MCP.PROTOCOL_VERSION_2025_MARCH);

        JSONObject capabilities = new JSONObject();
        JSONObject roots = new JSONObject().put(APIConstants.MCP.LIST_CHANGED_KEY, true);
        capabilities.put(APIConstants.MCP.ROOTS_KEY, roots);
        capabilities.put(APIConstants.MCP.SAMPLING_KEY, new JSONObject());
        params.put(APIConstants.MCP.CAPABILITIES_KEY, capabilities);

        JSONObject clientInfo = new JSONObject();
        clientInfo.put(APIConstants.MCP.CLIENT_NAME_KEY, APIConstants.MCP.CLIENT_NAME);
        clientInfo.put(APIConstants.MCP.CLIENT_VERSION_KEY, APIConstants.MCP.CLIENT_VERSION);
        params.put(APIConstants.MCP.CLIENT_INFO_KEY, clientInfo);

        payload.put(APIConstants.MCP.PARAMS_KEY, params);
        return payload;
    }

    /**
     * Builds the JSON-RPC tools/list payload.
     */
    private JSONObject buildToolsListPayload(String sessionId) {

        JSONObject payload = new JSONObject();
        payload.put(APIConstants.MCP.RpcConstants.JSON_RPC, APIConstants.MCP.RpcConstants.JSON_RPC_VERSION);
        payload.put(APIConstants.MCP.RpcConstants.ID, 2);
        payload.put(APIConstants.MCP.RpcConstants.METHOD, APIConstants.MCP.METHOD_TOOL_LIST);

        JSONObject params = new JSONObject();
        if (sessionId != null && !sessionId.isEmpty()) {
            params.put(APIConstants.MCP.SESSION_ID_KEY, sessionId);
        }
        payload.put(APIConstants.MCP.PARAMS_KEY, params);
        return payload;
    }

    /**
     * Sends a JSON-RPC request; returns wrapper with raw body and optional session id.
     */
    private JSONObject sendJsonRpcRequest(CloseableHttpClient httpClient, String targetUrl, JSONObject jsonBody,
                                          String sessionId) throws Exception {

        HttpPost request = new HttpPost(targetUrl);
        request.setHeader(APIConstants.MCP.HEADER_CONTENT_TYPE,
                ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8).toString());
        request.setHeader(APIConstants.MCP.HEADER_ACCEPT, APIConstants.MCP.ACCEPT_JSON_AND_SSE);

        if (secure && authHeaderName != null && !authHeaderName.isEmpty()) {
            request.setHeader(authHeaderName, authHeaderValue == null ? StringUtils.EMPTY : authHeaderValue);
        }
        if (sessionId != null && !sessionId.isEmpty()) {
            request.setHeader(APIConstants.MCP.HEADER_MCP_SESSION_ID, sessionId);
        }

        request.setEntity(new StringEntity(jsonBody.toString(), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            final int status = response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : 0;
            if (status < 200 || status >= 300) {
                String reason = response.getStatusLine() != null ?
                        response.getStatusLine().getReasonPhrase() : "Unknown";
                String bodySnippet = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : StringUtils.EMPTY;
                throw new APIManagementException("MCP request failed: HTTP " + status + " " + reason + " Body: "
                        + bodySnippet);
            }
            String body = response.getEntity() != null ?
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : StringUtils.EMPTY;
            Header sessionHeader = response.getFirstHeader(APIConstants.MCP.HEADER_MCP_SESSION_ID);
            String returnedSessionId = sessionHeader != null ? sessionHeader.getValue() : null;

            JSONObject result = new JSONObject();
            result.put(APIConstants.MCP.BODY_KEY, body);
            if (returnedSessionId != null) {
                result.put(APIConstants.MCP.SESSION_ID_KEY, returnedSessionId);
            }
            return result;
        }
    }

    /**
     * Parses a JSON-RPC response and returns the {@code result} object.
     *
     * @throws APIManagementException if the response contains an error or is invalid
     */
    private JSONObject parseJsonRpcResult(String responseText) throws APIManagementException {

        try {
            String candidate = extractJsonCandidate(responseText);
            JSONObject json = new JSONObject(candidate);

            if (json.has(APIConstants.MCP.RESULT_KEY)) {
                return json.getJSONObject(APIConstants.MCP.RESULT_KEY);
            }
            if (json.has(APIConstants.MCP.ERROR_KEY)) {
                Object errorObj = json.get(APIConstants.MCP.ERROR_KEY);
                throw new APIManagementException("MCP server returned error: " + String.valueOf(errorObj));
            }
            throw new APIManagementException("Unexpected JSON-RPC format: missing 'result'/'error'");
        } catch (APIManagementException e) {
            throw e;
        } catch (Exception e) {
            throw new APIManagementException("Failed to parse JSON-RPC response: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts JSON from plain JSON or SSE-style response (last non-empty {@code data:} line).
     */
    private String extractJsonCandidate(String responseText) {

        if (responseText == null || responseText.isEmpty()) {
            return "{}";
        }
        String[] lines = responseText.split("\n");
        String lastData = null;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith(APIConstants.MCP.SSE_DATA_PREFIX)) {
                String data = trimmed.substring(APIConstants.MCP.SSE_DATA_PREFIX.length()).trim();
                if (!data.isEmpty()) {
                    lastData = data;
                }
            }
        }
        return (lastData != null) ? lastData : responseText.trim();
    }

    /**
     * Extract the tools array from a tools/list response.
     *
     * @param toolsJson Raw JSON returned by tools/list
     * @return Non-empty tools array
     * @throws APIManagementException If the JSON is missing or invalid
     */
    public static org.json.JSONArray extractToolsArray(org.json.JSONObject toolsJson)
            throws APIManagementException {

        if (toolsJson == null) {
            throw new APIManagementException("No response received from MCP server (tools/list).",
                    ExceptionCodes.MCP_SERVER_VALIDATION_FAILED);
        }
        if (!toolsJson.has(APIConstants.MCP.TOOLS_KEY)) {
            throw new APIManagementException("Missing 'tools' field in tools/list response.",
                    ExceptionCodes.MCP_SERVER_VALIDATION_FAILED);
        }

        org.json.JSONArray toolsArray = toolsJson.optJSONArray(APIConstants.MCP.TOOLS_KEY);
        if (toolsArray == null) {
            throw new APIManagementException("Unexpected 'tools' format: expected an array.",
                    ExceptionCodes.MCP_SERVER_VALIDATION_FAILED);
        }
        if (toolsArray.length() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieved 0 tool(s) from MCP server.");
            }
            throw new APIManagementException("MCP server returned an empty tool list.",
                    ExceptionCodes.MCP_SERVER_VALIDATION_FAILED);
        }

        return toolsArray;
    }
}
