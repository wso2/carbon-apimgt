/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.XML;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AiAPIHandler handles AI-specific API requests and responses.
 * It extends the AbstractSynapseHandler to integrate with the Synapse MessageContext.
 */
public class AiApiHandler extends AbstractSynapseHandler {

    private static final Log log = LogFactory.getLog(AiApiHandler.class);

    /**
     * Handles the incoming request flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if the handling is successful, otherwise false
     */
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        try {
            return processMessage(messageContext, true);
        } catch (APIManagementException | XMLStreamException | IOException e) {
            log.error("Error occurred while processing AI API", e);
        }
        return true;
    }

    /**
     * Handles the outgoing request flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if the handling is successful, otherwise false
     */
    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {

        return true;
    }

    /**
     * Handles the incoming response flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if the handling is successful, otherwise false
     */
    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {

        try {
            return processMessage(messageContext, false);
        } catch (APIManagementException | XMLStreamException | IOException e) {
            log.error("Error occurred while processing AI API", e);
        }
        return true;
    }

    /**
     * Handles the outgoing response flow.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if the handling is successful, otherwise false
     */
    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        return true;
    }

    /**
     * Processes the message to extract and set LLM metadata.
     *
     * @param messageContext the Synapse MessageContext
     * @return true if the processing is successful, otherwise false
     */
    private boolean processMessage(MessageContext messageContext, boolean isRequest)
            throws APIManagementException, XMLStreamException, IOException {

        String llmProviderName =
                (String) messageContext.getProperty(APIConstants.AIAPIConstants.LLM_PROVIDER_NAME);
        String llmProviderApiVersion =
                (String) messageContext.getProperty(APIConstants.AIAPIConstants.LLM_PROVIDER_API_VERSION);
        String organization = (String) messageContext.getProperty(APIMgtGatewayConstants.TENANT_DOMAIN);

        LLMProvider provider = createLLMProvider(llmProviderName, llmProviderApiVersion, organization);
        String providerConfigurations = DataHolder.getInstance().getLLMProviderConfigurations(provider);

        if (providerConfigurations == null) {
            log.error("Unable to find provider configurations for provider: "
                    + provider.getName() + "_" + provider.getApiVersion() + " in tenant "
                    + "domain: " + organization);
            return true;
        }

        LLMProviderConfiguration config = parseLLMProviderConfig(providerConfigurations);
        LLMProviderService llmProviderService =
                ServiceReferenceHolder.getInstance().getLLMProviderService(config.getConnectorType());

        if (llmProviderService == null) {
            log.error("Unable to find LLM provider service for provider: "
                    + provider.getName() + "_" + provider.getApiVersion() + " in tenant "
                    + "domain: " + organization);
            return true;
        }
        String payload = extractPayloadFromContext(messageContext, config);
        Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
        Map<String, String> headers = extractHeadersFromContext(messageContext);

        Map<String, String> metadataMap = isRequest
                ? llmProviderService.getRequestMetadata(payload, headers, queryParams, config.getMetadata())
                : llmProviderService.getResponseMetadata(payload, headers, queryParams, config.getMetadata());

        if (metadataMap != null && !metadataMap.isEmpty()) {
            String metadataProperty = isRequest
                    ? APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA
                    : APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA;
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty(metadataProperty, metadataMap);
        }

        return true;
    }

    /**
     * Creates and initializes an LLM Provider with the given name, API version, and organization.
     *
     * @param name         The name of the LLM Provider.
     * @param apiVersion   The API version of the LLM Provider.
     * @param organization The organization associated with the LLM Provider.
     * @return The initialized LlmProvider object.
     */
    private LLMProvider createLLMProvider(String name, String apiVersion, String organization) {

        LLMProvider provider = new LLMProvider();
        provider.setName(name);
        provider.setApiVersion(apiVersion);
        provider.setOrganization(organization);
        return provider;
    }

    /**
     * Extracts the payload from the given MessageContext based on LLM Provider configuration.
     *
     * @param messageContext The message context containing the payload.
     * @param config         The LLM Provider configuration to check for input source.
     * @return The extracted payload as a string, or null if not found.
     * @throws XMLStreamException If an error occurs while processing XML.
     * @throws IOException        If an I/O error occurs.
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
     * Extracts query parameters from the request path in the given MessageContext.
     *
     * @param messageContext The message context containing the request path.
     * @return A map of query parameters.
     */
    private Map<String, String> extractQueryParamsFromContext(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        Object requestPathObj = axis2MessageContext.getProperties().get(RESTConstants.REST_SUB_REQUEST_PATH);
        if (requestPathObj == null || requestPathObj.toString().isEmpty()) {
            log.warn("No request path available in the message context.");
            return new HashMap<>();
        }

        String requestPath = requestPathObj.toString();
        return extractQueryParams(requestPath);
    }

    /**
     * Extracts transport headers from the given MessageContext.
     *
     * @param messageContext The message context containing transport headers.
     * @return A map of transport headers.
     */
    private Map<String, String> extractHeadersFromContext(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (Map<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Parses the given LLM Provider configuration string into an LlmProviderConfiguration object.
     *
     * @param providerConfigurations The JSON string of provider configurations.
     * @return The parsed LlmProviderConfiguration object.
     * @throws APIManagementException If parsing fails.
     */
    public LLMProviderConfiguration parseLLMProviderConfig(String providerConfigurations)
            throws APIManagementException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(providerConfigurations, LLMProviderConfiguration.class);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error occurred while parsing LLM Provider configuration", e);
        }
    }

    /**
     * Extracts query parameters from the given request path.
     *
     * @param requestPath The request path containing query parameters.
     * @return A map of query parameter names and values.
     */
    public Map<String, String> extractQueryParams(String requestPath) {

        Map<String, String> queryParams = new HashMap<>();
        if (requestPath.contains("?")) {
            String queryString = requestPath.split("\\?")[1];
            String[] pairs = queryString.split("&");

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
     * Extracts the payload from the Axis2 MessageContext based on content type.
     *
     * @param axis2MessageContext the Axis2 MessageContext
     * @return the extracted payload as a String
     * @throws IOException        if an I/O error occurs
     * @throws XMLStreamException if an XML parsing error occurs
     */
    private String getPayload(org.apache.axis2.context.MessageContext axis2MessageContext)
            throws IOException, XMLStreamException {

        RelayUtils.buildMessage(axis2MessageContext);
        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);
        if (contentType != null) {
            String normalizedContentType = contentType.toLowerCase();

            if (normalizedContentType.contains(MediaType.APPLICATION_XML) ||
                    normalizedContentType.contains(MediaType.TEXT_XML)) {
                return axis2MessageContext.getEnvelope().getBody().getFirstElement().toString();
            } else if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
                String jsonString = axis2MessageContext.getEnvelope().getBody().getFirstElement().toString();
                jsonString = jsonString
                        .substring(jsonString.indexOf(">") + 1, jsonString.lastIndexOf("</jsonObject>"));
                return XML.toJSONObject(jsonString).toString();
            } else if (normalizedContentType.contains(MediaType.TEXT_PLAIN)) {
                return axis2MessageContext.getEnvelope().getBody().getFirstElement().getText();
            }
        }
        return null;
    }
}