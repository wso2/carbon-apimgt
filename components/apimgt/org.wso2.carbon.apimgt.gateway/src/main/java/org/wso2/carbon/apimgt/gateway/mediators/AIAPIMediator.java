/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.mediators;

import com.jayway.jsonpath.JsonPath;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.gateway.ModelEndpointDTO;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

/**
 * AIAPIMediator is responsible for extracting payload metadata from AI API requests/response
 * and setting it in the message context for further processing.
 */
public class AIAPIMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(AIAPIMediator.class);
    private String llmProviderId;
    private String direction;

    /**
     * Initializes the AIAPIMediator.
     *
     * @param synapseEnvironment The Synapse environment instance.
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIMediator: Initialized.");
        }
    }

    /**
     * Destroys the AIAPIMediator instance and releases any allocated resources.
     */
    @Override
    public void destroy() {

    }

    /**
     * Mediates the AI API request and response by processing metadata, modifying the request payload,
     * and handling response metadata extraction.
     *
     * @param messageContext the Synapse {@link MessageContext} containing the API request/response
     * @return {@code true} if mediation is successful, {@code false} if an error occurs
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIMediator: Mediation started.");
        }

        try {
            LLMProviderInfo provider = DataHolder.getInstance().getLLMProviderConfigurations(this.llmProviderId);
            if (provider == null) {
                log.error("No LLM provider found for provider ID: " + llmProviderId);
                return false;
            }

            LLMProviderConfiguration providerConfiguration = provider.getConfigurations();
            LLMProviderService llmProviderService = ServiceReferenceHolder.getInstance()
                    .getLLMProviderService(providerConfiguration.getConnectorType());

            if (llmProviderService == null) {
                log.error("LLM provider service not found for provider ID: " + llmProviderId);
                return false;
            }

            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put(APIConstants.AIAPIConstants.NAME, provider.getName());
            metadataMap.put(APIConstants.AIAPIConstants.API_VERSION, provider.getApiVersion());
            messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA, metadataMap);

            if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_IN.equals(direction)) {
                processInboundRequest(messageContext, providerConfiguration);
            } else if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_OUT.equals(direction)) {
                processOutboundResponse(messageContext, providerConfiguration, llmProviderService, metadataMap);
            }

        } catch (Exception e) {
            log.error("Error during mediation.", e);
            return false;
        }

        return true;
    }

    /**
     * Processes an inbound request by extracting relevant information from the message context,
     * such as payload, headers, and HTTP method, and setting appropriate properties.
     *
     * @param messageContext        The message context of the request.
     * @param providerConfiguration The configuration of the LLM provider.
     * @throws XMLStreamException If an error occurs while processing the XML stream.
     * @throws IOException        If an I/O error occurs.
     */
    private void processInboundRequest(MessageContext messageContext, LLMProviderConfiguration providerConfiguration)
            throws XMLStreamException, IOException {

        Axis2MessageContext axis2MessageContext = (Axis2MessageContext) messageContext;
        RelayUtils.buildMessage(axis2MessageContext.getAxis2MessageContext());

        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_PAYLOAD,
                JsonUtil.jsonPayloadToString(axis2MessageContext.getAxis2MessageContext()));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_HEADERS,
                axis2MessageContext.getAxis2MessageContext()
                        .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD,
                axis2MessageContext.getAxis2MessageContext()
                        .getProperty(PassThroughConstants.HTTP_METHOD));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX,
                axis2MessageContext.getAxis2MessageContext()
                        .getProperty(NhttpConstants.REST_URL_POSTFIX));

        String targetEndpoint = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);
        String targetModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);

        if (APIConstants.AIAPIConstants.REJECT_ENDPOINT.equals(targetEndpoint)) {
            return;
        }

        if (targetEndpoint != null && targetModel != null) {
            LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);

            if (targetModelMetadata != null
                    && APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(
                    targetModelMetadata.getInputSource())) {
                modifyRequestPayload(targetModel, targetModelMetadata, axis2MessageContext.getAxis2MessageContext());
            } else {
                log.debug("Unsupported input source for attribute: " + (targetModelMetadata != null ?
                        targetModelMetadata.getAttributeName() : "unknown"));
            }
        } else {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.DEFAULT_ENDPOINT);
        }
    }

    /**
     * Processes the outbound response, extracts payload, headers, and query parameters,
     * updates response metadata, and handles failover scenarios if necessary.
     *
     * @param messageContext        The message context of the response.
     * @param providerConfiguration The configuration of the LLM provider.
     * @param llmProviderService    The service handling LLM provider operations.
     * @param metadataMap           A map containing metadata information.
     * @throws APIManagementException If an API management error occurs.
     * @throws XMLStreamException     If an error occurs while processing the XML stream.
     * @throws IOException            If an I/O error occurs.
     */
    private void processOutboundResponse(MessageContext messageContext, LLMProviderConfiguration providerConfiguration,
                                         LLMProviderService llmProviderService, Map<String, String> metadataMap)
            throws APIManagementException, XMLStreamException, IOException {

        String payload = extractPayloadFromContext(messageContext, providerConfiguration);
        Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
        Map<String, String> headers = extractHeadersFromContext(messageContext);

        llmProviderService.getResponseMetadata(payload, headers, queryParams, providerConfiguration.getMetadata(),
                metadataMap);
        messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA, metadataMap);

        String targetModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);
        String targetEndpoint = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);
        List<ModelEndpointDTO> failoverEndpoints =
                (List<ModelEndpointDTO>) messageContext.getProperty(APIConstants.AIAPIConstants.FAILOVER_ENDPOINTS);

        if (targetEndpoint == null || targetModel == null) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        int statusCode = (int) ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext().getProperty(APIMgtGatewayConstants.HTTP_SC);
        if (statusCode >= 200 && statusCode < 300) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        Long suspendDuration = (Long) messageContext.getProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION);
        DataHolder.getInstance().suspendEndpoint(GatewayUtils.getAPIKeyForEndpoints(messageContext),
                getEndpointId(targetEndpoint, targetModel), suspendDuration * 1000);

        handleFailover(messageContext, failoverEndpoints, providerConfiguration);
    }

    /**
     * Handles failover logic when an API request fails.
     *
     * @param messageContext        The message context containing request details.
     * @param failoverEndpoints     A list of failover endpoints to attempt.
     * @param providerConfiguration The configuration details of the LLM provider.
     * @throws XMLStreamException If an error occurs while handling XML streams.
     * @throws IOException        If an I/O error occurs during processing.
     */
    private void handleFailover(MessageContext messageContext, List<ModelEndpointDTO> failoverEndpoints,
                                LLMProviderConfiguration providerConfiguration) throws XMLStreamException, IOException {

        if (failoverEndpoints == null || failoverEndpoints.isEmpty()) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        Object currentEndpointObj = messageContext.getProperty(APIConstants.AIAPIConstants.CURRENT_ENDPOINT_INDEX);
        int currentEndpointIndex = (currentEndpointObj instanceof Integer) ? (int) currentEndpointObj : 0;

        if (currentEndpointIndex >= failoverEndpoints.size()) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.REJECT_ENDPOINT);
            return;
        }

        ModelEndpointDTO failoverEndpoint = failoverEndpoints.get(currentEndpointIndex);
        messageContext.setProperty(APIConstants.AIAPIConstants.CURRENT_ENDPOINT_INDEX, ++currentEndpointIndex);
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, failoverEndpoint.getEndpointId());
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_MODEL, failoverEndpoint.getModel());

        buildFailoverRequest(messageContext, failoverEndpoint, providerConfiguration);
    }

    /**
     * Builds a failover request by modifying the request payload and setting necessary properties.
     *
     * @param messageContext        The message context containing request details.
     * @param failoverEndpoint      The endpoint to which the request will be redirected.
     * @param providerConfiguration The configuration details of the LLM provider.
     * @throws XMLStreamException If an error occurs while handling XML streams.
     * @throws IOException        If an I/O error occurs during processing.
     */
    private void buildFailoverRequest(MessageContext messageContext,
                                      ModelEndpointDTO failoverEndpoint,
                                      LLMProviderConfiguration providerConfiguration)
            throws XMLStreamException, IOException {

        String requestPayload = (String) messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_PAYLOAD);
        Axis2MessageContext axis2MessageContext = (Axis2MessageContext) messageContext;
        RelayUtils.buildMessage(axis2MessageContext.getAxis2MessageContext());
        JsonUtil.getNewJsonPayload(axis2MessageContext.getAxis2MessageContext(), requestPayload, true, true);

        LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);
        if (targetModelMetadata != null &&
                APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD
                        .equalsIgnoreCase(targetModelMetadata.getInputSource())) {
            modifyRequestPayload(failoverEndpoint.getModel(), targetModelMetadata,
                    axis2MessageContext.getAxis2MessageContext());
        } else {
            log.debug("Unsupported input source for attribute: " + (targetModelMetadata != null ?
                    targetModelMetadata.getAttributeName() : "unknown"));
        }

        axis2MessageContext.getAxis2MessageContext().setProperty(NhttpConstants.REST_URL_POSTFIX,
                messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX));
        axis2MessageContext.getAxis2MessageContext().setProperty(PassThroughConstants.HTTP_METHOD,
                messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD));
        axis2MessageContext.getAxis2MessageContext()
                .setProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS,
                        messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_HEADERS));
    }

    /**
     * Retrieves metadata for the target model based on the provider configuration.
     *
     * @param providerConfiguration The configuration of the LLM provider.
     * @return The metadata of the target model or null if not found.
     */
    private LLMProviderMetadata getTargetModelMetadata(LLMProviderConfiguration providerConfiguration) {

        return findMetadataByAttributeName(providerConfiguration.getMetadata(),
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL,
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_MODEL);
    }

    /**
     * Finds metadata by matching attribute names in the provided metadata list.
     *
     * @param metadataList   The list of metadata objects.
     * @param attributeNames The attribute names to search for.
     * @return The matching metadata object or null if not found.
     */
    private LLMProviderMetadata findMetadataByAttributeName(List<LLMProviderMetadata> metadataList,
                                                            String... attributeNames) {

        for (String attributeName : attributeNames) {
            LLMProviderMetadata metadata = metadataList.stream()
                    .filter(meta -> attributeName.equals(meta.getAttributeName()))
                    .findFirst()
                    .orElse(null);
            if (metadata != null) {
                return metadata;
            }
        }
        return null;
    }

    /**
     * Modifies the request payload based on its content type. Supports JSON (via JSONPath)
     * and plain text modifications; XML payloads remain unchanged.
     *
     * @param targetModel         the new value to set
     * @param targetModelMetadata metadata containing the attribute identifier
     * @param axis2MessageContext the Axis2 message context with the request payload
     * @throws XMLStreamException if XML processing fails
     * @throws IOException        if an I/O error occurs
     */

    public static void modifyRequestPayload(String targetModel, LLMProviderMetadata targetModelMetadata,
                                            org.apache.axis2.context.MessageContext axis2MessageContext)
            throws XMLStreamException, IOException {

        RelayUtils.buildMessage(axis2MessageContext);
        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);

        if (contentType == null) {
            return;
        }
        String normalizedContentType = contentType.toLowerCase();
        String attributeIdentifier = targetModelMetadata.getAttributeIdentifier();

        if (normalizedContentType.contains(MediaType.APPLICATION_XML)
                || normalizedContentType.contains(MediaType.TEXT_XML)) {
        } else if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
            modifyJsonPayload(axis2MessageContext, attributeIdentifier, targetModel);
        } else if (normalizedContentType.contains(MediaType.TEXT_PLAIN)) {
            modifyTextPayload(axis2MessageContext, targetModel);
        }

    }

    /**
     * Modifies the value of a specified JSON field in the payload of the given
     * Axis2 message context using a JSONPath expression.
     *
     * @param axis2MessageContext the Axis2 {@link org.apache.axis2.context.MessageContext}
     *                            containing the JSON payload
     * @param jsonPath            the JSONPath expression specifying the field to modify
     * @param newValue            the new value to set for the specified JSON field
     * @return {@code true} if the JSON payload was successfully modified, {@code false}
     * if the message context does not contain a valid JSON payload
     */
    private static Boolean modifyJsonPayload(org.apache.axis2.context.MessageContext axis2MessageContext,
                                             String jsonPath, String newValue) throws AxisFault {

        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return false;
        }
        String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
        String updatedJson = JsonPath.parse(jsonPayload).set(jsonPath, newValue).jsonString();
        JsonUtil.getNewJsonPayload(axis2MessageContext, updatedJson, true, true);
        return true;
    }

    /**
     * Modifies the text content of the first element in the SOAP body of the given
     * Axis2 message context.
     *
     * @param axis2MessageContext the Axis2 {@link org.apache.axis2.context.MessageContext}
     *                            containing the SOAP envelope
     * @param newValue            the new text value to set for the first element in the SOAP body
     * @return {@code true} if the text was successfully modified, {@code false} if
     * no first element was found in the SOAP body
     */

    private static Boolean modifyTextPayload(org.apache.axis2.context.MessageContext axis2MessageContext,
                                             String newValue) {

        OMElement bodyElement = axis2MessageContext.getEnvelope().getBody().getFirstElement();
        if (bodyElement != null) {
            bodyElement.setText(newValue);
            return true;
        }
        return false;

    }

    /**
     * Indicates whether this mediator is content-aware.
     *
     * @return false as this mediator does not alter the message content.
     */
    @Override
    public boolean isContentAware() {

        return false;
    }

    /**
     * Retrieves the LLM provider ID associated with this mediator.
     *
     * @return The LLM provider ID.
     */
    public String getLlmProviderId() {

        return llmProviderId;
    }

    /**
     * Sets the LLM provider ID for this mediator.
     *
     * @param llmProviderId The LLM provider ID to set.
     */
    public void setLlmProviderId(String llmProviderId) {

        this.llmProviderId = llmProviderId;
    }

    /**
     * Retrieves the direction of traffic flow.
     *
     * @return The traffic direction as a string.
     */
    public String getDirection() {

        return direction;
    }

    /**
     * Sets the direction of traffic flow.
     *
     * @param direction The traffic direction to be set (e.g., inbound or outbound).
     */
    public void setDirection(String direction) {

        this.direction = direction;
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
    private String getPayload(org.apache.axis2.context.MessageContext axis2MessageContext) throws IOException,
            XMLStreamException {

        RelayUtils.buildMessage(axis2MessageContext);
        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);

        if (contentType == null) {
            return null;
        }

        String normalizedContentType = contentType.toLowerCase();
        if (normalizedContentType.contains(MediaType.APPLICATION_XML)
                || normalizedContentType.contains(MediaType.TEXT_XML)) {
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
        return (Map<String, String>)
                axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Finds and returns the {@link LLMProviderMetadata} object from the given list
     * that matches the specified attribute name.
     *
     * @param metadataList  the list of {@link LLMProviderMetadata} objects to search
     * @param attributeName the attribute name to match
     * @return the first {@link LLMProviderMetadata} with the given attribute name,
     * or {@code null} if no match is found
     */

    public static LLMProviderMetadata findMetadataByAttributeName(List<LLMProviderMetadata> metadataList,
                                                                  String attributeName) {

        return metadataList.stream().filter(metadata ->
                attributeName.equals(metadata.getAttributeName())).findFirst().orElse(null);
    }

    /**
     * Generates an endpoint ID by combining the target endpoint and target mode.
     *
     * @param targetEndpoint The target endpoint.
     * @param targetMode     The target mode.
     * @return The generated endpoint ID in the format "targetEndpoint_targetMode".
     */
    public String getEndpointId(String targetEndpoint, String targetMode) {

        return targetEndpoint + "_" + targetMode;
    }

}
