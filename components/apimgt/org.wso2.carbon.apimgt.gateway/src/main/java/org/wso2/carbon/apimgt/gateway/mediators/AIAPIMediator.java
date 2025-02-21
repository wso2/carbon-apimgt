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
import com.jayway.jsonpath.PathNotFoundException;
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
            LLMProviderService llmProviderService =
                    ServiceReferenceHolder.getInstance()
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

        String targetModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);
        String targetEndpoint = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);

        if (APIConstants.AIAPIConstants.REJECT_ENDPOINT.equals(targetEndpoint)) {
            return;
        }

        org.apache.axis2.context.MessageContext axis2Ctx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (targetModel != null) {
            handleLoadBalancing(axis2Ctx, providerConfiguration, targetModel);
            return;
        }

        prepareForFailoverIfNeeded(messageContext, axis2Ctx, providerConfiguration);

        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                APIConstants.AIAPIConstants.DEFAULT_ENDPOINT);
    }

    /**
     * Prepares the request for failover if a failover target model is specified.
     * It rebuilds the message, extracts the request model, and preserves request properties if needed.
     *
     * @param messageContext        The Synapse {@link MessageContext} containing API request details.
     * @param axis2Ctx              The Axis2 {@link org.apache.axis2.context.MessageContext} containing the request
     *                              payload.
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during payload handling.
     */
    private void prepareForFailoverIfNeeded(MessageContext messageContext,
                                            org.apache.axis2.context.MessageContext axis2Ctx,
                                            LLMProviderConfiguration providerConfiguration) throws XMLStreamException
            , IOException {

        Object failoverTargetModel = messageContext.getProperty(APIConstants.AIAPIConstants.FAILOVER_TARGET_MODEL);
        if (failoverTargetModel == null) {
            return;
        }

        RelayUtils.buildMessage(axis2Ctx);

        LLMProviderMetadata requestModelMetadata = getTargetModelMetadata(providerConfiguration);
        String requestModel = extractRequestModel(requestModelMetadata, axis2Ctx);

        if (requestModel != null && requestModel.equals(failoverTargetModel.toString())) {
            preserveRequestProperties(messageContext, axis2Ctx, requestModel);
        }
    }

    /**
     * Extracts the request model from the request payload and removes any surrounding quotes.
     *
     * @param requestModelMetadata The {@link LLMProviderMetadata} containing metadata for extracting the model
     *                             attribute.
     * @param axis2Ctx             The Axis2 {@link org.apache.axis2.context.MessageContext} containing the request
     *                             payload.
     * @return The extracted request model as a {@code String} with quotes removed, or {@code null} if extraction fails.
     */
    private String extractRequestModel(LLMProviderMetadata requestModelMetadata,
                                       org.apache.axis2.context.MessageContext axis2Ctx) {

        String requestModel = getRequestModel(requestModelMetadata, axis2Ctx);
        return (requestModel != null) ? requestModel.replaceAll("\"", "").replace("\"", "") : null;
    }

    /**
     * Preserves the request properties by extracting relevant information from the Axis2 message context
     * and storing it in the Synapse message context.
     *
     * @param messageContext The Synapse {@link MessageContext} where the request properties are stored.
     * @param axis2Ctx       The Axis2 {@link org.apache.axis2.context.MessageContext} containing the request details.
     * @param requestModel   The request model associated with the API request.
     */
    private void preserveRequestProperties(MessageContext messageContext,
                                           org.apache.axis2.context.MessageContext axis2Ctx, String requestModel) {

        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_PAYLOAD, JsonUtil.jsonPayloadToString(axis2Ctx));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_HEADERS,
                axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD,
                axis2Ctx.getProperty(PassThroughConstants.HTTP_METHOD));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX,
                axis2Ctx.getProperty(NhttpConstants.REST_URL_POSTFIX));
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_MODEL, requestModel);
    }

    /**
     * Handles load balancing by modifying the request payload based on the target model metadata.
     *
     * @param axis2Ctx              The Axis2 {@link org.apache.axis2.context.MessageContext} containing the request
     *                              details.
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @param targetModel           The target model for which load balancing is applied.
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during request modification.
     */
    private void handleLoadBalancing(org.apache.axis2.context.MessageContext axis2Ctx,
                                     LLMProviderConfiguration providerConfiguration, String targetModel)
            throws XMLStreamException, IOException {

        LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);
        if (targetModelMetadata == null) {
            log.error("Target model metadata is null for model: " + targetModel);
            return;
        }

        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(targetModelMetadata.getInputSource())) {
            RelayUtils.buildMessage(axis2Ctx);
            modifyRequestPayload(targetModel, targetModelMetadata, axis2Ctx);
        } else {
            log.debug("Unsupported input source for attribute: " + targetModelMetadata.getAttributeName());
        }
    }

    /**
     * Retrieves the request model from the request payload based on its content type.
     *
     * @param requestModelMetadata The {@link LLMProviderMetadata} containing metadata for extracting the model
     *                             attribute.
     * @param axis2MessageContext  The Axis2 {@link org.apache.axis2.context.MessageContext} containing the request
     *                             payload.
     * @return The extracted request model as a {@code String}, or {@code null} if the content type is unsupported
     * or the model cannot be extracted.
     */
    private String getRequestModel(LLMProviderMetadata requestModelMetadata,
                                   org.apache.axis2.context.MessageContext axis2MessageContext) {

        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);
        if (contentType == null) {
            return null;
        }

        String normalizedContentType = contentType.toLowerCase();
        if (isUnsupportedContentType(normalizedContentType)) {
            return null;
        }

        if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
            return extractRequestModelFromJson(requestModelMetadata, axis2MessageContext);
        }

        return null;
    }

    /**
     * Checks if the given content type is unsupported.
     *
     * @param contentType The content type of the request.
     * @return {@code true} if the content type is unsupported (XML or plain text), {@code false} otherwise.
     */
    private boolean isUnsupportedContentType(String contentType) {

        return contentType.contains(MediaType.APPLICATION_XML) ||
                contentType.contains(MediaType.TEXT_XML) ||
                contentType.contains(MediaType.TEXT_PLAIN);
    }

    /**
     * Extracts the request model from the JSON payload based on the provided metadata.
     *
     * @param requestModelMetadata The {@link LLMProviderMetadata} containing metadata for extracting the model
     *                             attribute.
     * @param axis2MessageContext  The Axis2 {@link org.apache.axis2.context.MessageContext} containing the JSON
     *                             payload.
     * @return The extracted request model as a {@code String}, or {@code null} if the payload is missing,
     * the input source is unsupported, or the attribute is not found.
     */
    private String extractRequestModelFromJson(LLMProviderMetadata requestModelMetadata,
                                               org.apache.axis2.context.MessageContext axis2MessageContext) {

        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            log.debug("No JSON payload found.");
            return null;
        }

        String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
        if (jsonPayload == null) {
            log.debug("Payload is null, cannot extract metadata for attribute: "
                    + requestModelMetadata.getAttributeName());
            return null;
        }

        String inputSource = requestModelMetadata.getInputSource();
        if (!APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(inputSource)) {
            log.debug("Unsupported input source: " + inputSource + " for attribute: "
                    + requestModelMetadata.getAttributeName());
            return null;
        }

        try {
            return JsonPath.read(jsonPayload, requestModelMetadata.getAttributeIdentifier()).toString();
        } catch (PathNotFoundException e) {
            log.debug("Attribute not found in the payload for identifier: "
                    + requestModelMetadata.getAttributeIdentifier());
            return null;
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
    private void processOutboundResponse(MessageContext messageContext,
                                         LLMProviderConfiguration providerConfiguration,
                                         LLMProviderService llmProviderService,
                                         Map<String, String> metadataMap) throws APIManagementException,
            XMLStreamException, IOException {

        String payload = extractPayloadFromContext(messageContext, providerConfiguration);
        Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
        Map<String, String> headers = extractHeadersFromContext(messageContext);

        llmProviderService.getResponseMetadata(payload, headers, queryParams, providerConfiguration.getMetadata(),
                metadataMap);
        messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA, metadataMap);

        String targetModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);
        String requestModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_MODEL);

        if (targetModel == null && requestModel == null) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        int statusCode = (int) ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext()
                .getProperty(APIMgtGatewayConstants.HTTP_SC);

        if (handleSuccessfulResponse(messageContext, statusCode)) {
            return;
        }

        String targetEndpoint = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);
        Long suspendDuration = (Long) messageContext.getProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION);

        if (suspendTargetEndpointIfNeeded(messageContext, targetEndpoint, targetModel, suspendDuration)) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        if (handleFailoverIfNeeded(messageContext, providerConfiguration)) {
            return;
        }
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                APIConstants.AIAPIConstants.EXIT_ENDPOINT);
    }

    /**
     * Handles a successful response by checking the HTTP status code and updating the target endpoint accordingly.
     *
     * @param messageContext The Synapse {@link MessageContext} to be updated.
     * @param statusCode     The HTTP status code of the response.
     * @return {@code true} if the response is considered successful (status code 2xx), {@code false} otherwise.
     */
    private boolean handleSuccessfulResponse(MessageContext messageContext, int statusCode) {

        if (statusCode >= 200 && statusCode < 300) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return true;
        }
        return false;
    }

    /**
     * Handles failover if needed by checking the request model and available failover endpoints.
     *
     * @param messageContext        The Synapse {@link MessageContext} containing API request details.
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @return {@code true} if failover handling was triggered, {@code false} if no failover was needed.
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during failover handling.
     */
    private boolean handleFailoverIfNeeded(MessageContext messageContext,
                                           LLMProviderConfiguration providerConfiguration)
            throws XMLStreamException, IOException {

        String requestModel = (String) messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_MODEL);
        if (requestModel == null) {
            return false;
        }

        List<ModelEndpointDTO> failoverEndpoints = (List<ModelEndpointDTO>) messageContext
                .getProperty(APIConstants.AIAPIConstants.FAILOVER_ENDPOINTS);

        if (failoverEndpoints != null) {
            handleFailover(messageContext, failoverEndpoints, providerConfiguration);
            return true;
        }
        return false;
    }

    /**
     * Suspends the target endpoint if the necessary parameters are provided.
     *
     * @param messageContext  The Synapse {@link MessageContext} containing API request details.
     * @param targetEndpoint  The ID of the target endpoint to be suspended.
     * @param targetModel     The model associated with the target endpoint.
     * @param suspendDuration The duration (in milliseconds) for which the endpoint should be suspended.
     * @return {@code true} if the endpoint was successfully suspended, {@code false} if required parameters are
     * missing.
     */
    private boolean suspendTargetEndpointIfNeeded(MessageContext messageContext, String targetEndpoint,
                                                  String targetModel, Long suspendDuration) {

        if (targetModel == null || targetEndpoint == null || suspendDuration == null) {
            return false;
        }
        DataHolder.getInstance().suspendEndpoint(
                GatewayUtils.getAPIKeyForEndpoints(messageContext),
                getEndpointId(targetEndpoint, targetModel),
                suspendDuration
        );
        return true;
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
    private void handleFailover(MessageContext messageContext,
                                List<ModelEndpointDTO> failoverEndpoints,
                                LLMProviderConfiguration providerConfiguration)
            throws XMLStreamException, IOException {

        int currentEndpointIndex = getCurrentFailoverIndex(messageContext);

        if ((currentEndpointIndex >= 0 && failoverEndpoints.isEmpty())
                || currentEndpointIndex >= failoverEndpoints.size()) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.REJECT_ENDPOINT);
            return;
        }

        if (currentEndpointIndex > 0) {
            Long suspendDuration = (Long) messageContext.getProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION);
            ModelEndpointDTO previousEndpoint = failoverEndpoints.get(currentEndpointIndex - 1);

            suspendTargetEndpointIfNeeded(messageContext, previousEndpoint.getEndpointId(),
                    previousEndpoint.getModel(), suspendDuration);
        }
        ModelEndpointDTO failoverEndpoint = failoverEndpoints.get(currentEndpointIndex);
        buildFailoverRequest(messageContext, failoverEndpoint, providerConfiguration);

        updateFailoverContext(messageContext, currentEndpointIndex + 1, failoverEndpoint);
    }

    /**
     * Retrieves the current failover endpoint index from the message context.
     * If the index is not set or is not an integer, it defaults to 0.
     *
     * @param messageContext The Synapse {@link MessageContext} containing the failover index property.
     * @return The current failover endpoint index, or 0 if not set or invalid.
     */
    private int getCurrentFailoverIndex(MessageContext messageContext) {

        Object currentEndpointObj = messageContext.getProperty(APIConstants.AIAPIConstants.CURRENT_ENDPOINT_INDEX);
        return (currentEndpointObj instanceof Integer) ? (int) currentEndpointObj : 0;
    }

    /**
     * Updates the failover context in the message by setting the next endpoint index and target endpoint ID.
     *
     * @param messageContext   The Synapse {@link MessageContext} to update with failover details.
     * @param nextIndex        The index of the next failover endpoint to be used.
     * @param failoverEndpoint The {@link ModelEndpointDTO} representing the failover endpoint.
     */
    private void updateFailoverContext(MessageContext messageContext, int nextIndex,
                                       ModelEndpointDTO failoverEndpoint) {

        messageContext.setProperty(APIConstants.AIAPIConstants.CURRENT_ENDPOINT_INDEX, nextIndex);
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, failoverEndpoint.getEndpointId());
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

        Axis2MessageContext axis2MessageContext = (Axis2MessageContext) messageContext;
        org.apache.axis2.context.MessageContext axis2Ctx = axis2MessageContext.getAxis2MessageContext();

        updateJsonPayload(messageContext, axis2Ctx);
        modifyPayloadIfNeeded(failoverEndpoint, providerConfiguration, axis2Ctx);
        updateRequestMetadata(messageContext, axis2Ctx);
    }

    /**
     * Updates the JSON payload of the request by retrieving the payload from the Synapse message context
     * and setting it in the Axis2 message context.
     *
     * @param messageContext The Synapse {@link MessageContext} containing the request payload.
     * @param axis2Ctx       The Axis2 {@link org.apache.axis2.context.MessageContext} where the updated payload is set.
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during payload modification.
     */
    private void updateJsonPayload(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2Ctx)
            throws XMLStreamException, IOException {

        String requestPayload = (String) messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_PAYLOAD);
        RelayUtils.buildMessage(axis2Ctx);
        JsonUtil.getNewJsonPayload(axis2Ctx, requestPayload, true, true);
    }

    /**
     * Modifies the request payload if needed based on the target model metadata.
     *
     * @param failoverEndpoint      The {@link ModelEndpointDTO} representing the failover endpoint.
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @param axis2Ctx              The Axis2 {@link org.apache.axis2.context.MessageContext} for the current request.
     * @throws XMLStreamException If an error occurs while processing the XML payload.
     * @throws IOException        If an I/O error occurs during payload modification.
     */
    private void modifyPayloadIfNeeded(ModelEndpointDTO failoverEndpoint,
                                       LLMProviderConfiguration providerConfiguration,
                                       org.apache.axis2.context.MessageContext axis2Ctx)
            throws XMLStreamException, IOException {

        LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);
        if (targetModelMetadata == null) {
            log.debug("Target model metadata is null, skipping request payload modification.");
            return;
        }

        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(targetModelMetadata.getInputSource())) {
            modifyRequestPayload(failoverEndpoint.getModel(), targetModelMetadata, axis2Ctx);
        } else {
            log.debug("Unsupported input source for attribute: " + targetModelMetadata.getAttributeName());
        }
    }

    /**
     * Updates the request metadata by setting relevant properties in the Axis2 message context.
     *
     * @param messageContext The Synapse {@link MessageContext} containing API request details.
     * @param axis2Ctx       The Axis2 {@link org.apache.axis2.context.MessageContext} to be updated.
     */
    private void updateRequestMetadata(MessageContext messageContext,
                                       org.apache.axis2.context.MessageContext axis2Ctx) {

        axis2Ctx.setProperty(NhttpConstants.REST_URL_POSTFIX,
                messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX));
        axis2Ctx.setProperty(PassThroughConstants.HTTP_METHOD,
                messageContext.getProperty(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD));
        axis2Ctx.setProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS,
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
            LLMProviderMetadata metadata =
                    metadataList.stream().filter(meta ->
                            attributeName.equals(meta.getAttributeName())).findFirst().orElse(null);
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
            throws IOException {

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
     */
    private static void modifyJsonPayload(org.apache.axis2.context.MessageContext axis2MessageContext,
                                          String jsonPath, String newValue) throws AxisFault {

        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return;
        }
        String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
        String updatedJson = JsonPath.parse(jsonPayload).set(jsonPath, newValue).jsonString();
        JsonUtil.getNewJsonPayload(axis2MessageContext, updatedJson, true, true);
    }

    /**
     * Modifies the text content of the first element in the SOAP body of the given
     * Axis2 message context.
     *
     * @param axis2MessageContext the Axis2 {@link org.apache.axis2.context.MessageContext}
     *                            containing the SOAP envelope
     * @param newValue            the new text value to set for the first element in the SOAP body
     */

    private static void modifyTextPayload(org.apache.axis2.context.MessageContext axis2MessageContext,
                                          String newValue) {

        OMElement bodyElement = axis2MessageContext.getEnvelope().getBody().getFirstElement();
        if (bodyElement != null) {
            bodyElement.setText(newValue);
        }

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
        return (Map<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
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
