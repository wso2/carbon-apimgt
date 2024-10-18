/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import com.google.common.net.HttpHeaders;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.XML;
import org.wso2.carbon.apimgt.api.model.AIConfiguration;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handles AI-specific API requests and responses.
 */
public class AiApiHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(AiApiHandler.class);
    private String llmProviderId;

    /**
     * Processes the request flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if handling is successful, false otherwise
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) {

        return handleMessage(messageContext, true);
    }

    /**
     * Processes the response flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if handling is successful, false otherwise
     */
    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return handleMessage(messageContext, false);
    }

    /**
     * Handles both request and response messages.
     *
     * @param messageContext the Synapse MessageContext
     * @param isRequest      true if handling request, false for response
     * @return true if processing is successful, false otherwise
     */
    private boolean handleMessage(MessageContext messageContext, boolean isRequest) {

        return processMessage(messageContext, isRequest);
    }

    /**
     * Processes the message context, extracting and setting LLM metadata.
     *
     * @param messageContext the Synapse MessageContext
     * @param isRequest      true if processing a request, false for response
     * @return true if processing is successful, false otherwise
     */
    private boolean processMessage(MessageContext messageContext, boolean isRequest) {

        try {
            if (this.llmProviderId == null) {
                log.error("No LLM provider ID provided by TemplateBuilderUtil");
                return true;
            }
            LLMProviderInfo provider = DataHolder.getInstance().getLLMProviderConfigurations(this.llmProviderId);
            if (provider == null) {
                log.error("No LLM provider found for provider ID: " + llmProviderId);
                return false;
            }
            if (isRequest) {
                Map<String, String> transportHeaders =
                        (Map<String, String>) ((Axis2MessageContext) messageContext)
                                .getAxis2MessageContext()
                                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                transportHeaders.remove(HttpHeaders.ACCEPT_ENCODING);
            }
            LLMProviderConfiguration providerConfiguration = provider.getConfigurations();
            LLMProviderService llmProviderService = ServiceReferenceHolder.getInstance()
                    .getLLMProviderService(providerConfiguration.getConnectorType());
            if (llmProviderService == null) {
                log.error("LLM provider service not found for the provider with ID: " + llmProviderId);
                return false;
            }
            Map<String, String> metadataMap = new HashMap<>();
            String payload = extractPayloadFromContext(messageContext, providerConfiguration);
            Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
            Map<String, String> headers = extractHeadersFromContext(messageContext);

            metadataMap.put(APIConstants.AIAPIConstants.NAME, provider.getName());
            metadataMap.put(APIConstants.AIAPIConstants.API_VERSION, provider.getApiVersion());
            if (isRequest) {
                llmProviderService.getRequestMetadata(payload, headers, queryParams,
                        providerConfiguration.getMetadata(), metadataMap);
            } else {
                llmProviderService.getResponseMetadata(payload, headers, queryParams,
                        providerConfiguration.getMetadata(), metadataMap);
            }
            String metadataProperty = isRequest
                    ? APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA
                    : APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA;
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty(metadataProperty,
                    metadataMap);
            return true;
        } catch (Exception e) {
            if (e instanceof APIManagementException) {
                log.error("Error occurred while extracting metadata.", e);
            } else if (e instanceof XMLStreamException || e instanceof IOException) {
                log.error("Error occurred while reading payload.", e);
            }
            return false;
        }
    }

    /**
     * Extracts the payload from the message context.
     *
     * @param messageContext the Synapse MessageContext
     * @param config         LLM provider configuration
     * @return extracted payload
     */
    private String extractPayloadFromContext(MessageContext messageContext, LLMProviderConfiguration config)
            throws XMLStreamException, IOException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        for (LLMProviderMetadata metadata : config.getMetadata()) {
            if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equals(metadata.getInputSource())) {
                return getPayload(axis2MessageContext);
            }
        }
        return null;
    }

    /**
     * Extracts the payload from the Axis2 message context.
     *
     * @param axis2MessageContext the Axis2 message context
     * @return the extracted payload
     */
    private String getPayload(org.apache.axis2.context.MessageContext axis2MessageContext)
            throws IOException, XMLStreamException {

        RelayUtils.buildMessage(axis2MessageContext);
        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);

        if (contentType == null) {
            return null;
        }

        String normalizedContentType = contentType.toLowerCase();
        if (normalizedContentType.contains(MediaType.APPLICATION_XML) || normalizedContentType.
                contains(MediaType.TEXT_XML)) {
            return axis2MessageContext.getEnvelope().getBody().getFirstElement().toString();
        } else if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
            if (JsonUtil.hasAJsonPayload(axis2MessageContext)) {
                return JsonUtil.jsonPayloadToString(axis2MessageContext);
            }
            return null;
        } else if (normalizedContentType.contains(MediaType.TEXT_PLAIN)) {
            return axis2MessageContext.getEnvelope().getBody().getFirstElement().getText();
        }
        return null;
    }

    /**
     * Extracts query parameters from the request.
     *
     * @param messageContext the Synapse MessageContext
     * @return map of query parameters
     */
    private Map<String, String> extractQueryParamsFromContext(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String requestPath = (String) axis2MessageContext.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
        if (requestPath == null || requestPath.isEmpty()) {
            log.debug("No request path available in the message context.");
            return new HashMap<>();
        }

        return extractQueryParams(requestPath);
    }

    /**
     * Parses the query parameters from the request path.
     *
     * @param requestPath the request path
     * @return map of query parameters
     */
    private Map<String, String> extractQueryParams(String requestPath) {

        Map<String, String> queryParams = new HashMap<>();
        if (requestPath.contains("?")) {
            String[] pairs = requestPath.split("\\?")[1].split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    queryParams.put(keyValue[0], "");
                }
            }
        }
        return queryParams;
    }

    /**
     * Extracts headers from the message context.
     *
     * @param messageContext the Synapse MessageContext
     * @return map of headers
     */
    private Map<String, String> extractHeadersFromContext(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (Map<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Sets the LLM provider ID.
     *
     * @param llmProviderId the LLM provider ID
     */
    public void setLlmProviderId(String llmProviderId) {
        this.llmProviderId = llmProviderId;
    }
}
