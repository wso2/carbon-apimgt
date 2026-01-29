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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
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
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.LLMResponseMetaData;
import org.wso2.carbon.apimgt.api.gateway.FailoverPolicyConfigDTO;
import org.wso2.carbon.apimgt.api.gateway.FailoverPolicyDeploymentConfigDTO;
import org.wso2.carbon.apimgt.api.gateway.ModelEndpointDTO;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.util.Arrays;
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
                    ServiceReferenceHolder.getInstance().getLLMProviderService(providerConfiguration.getConnectorType());

            if (llmProviderService == null) {
                log.error("LLM provider service not found for provider ID: " + llmProviderId);
                return false;
            }

            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put(APIConstants.AIAPIConstants.NAME, provider.getName());
            metadataMap.put(APIConstants.AIAPIConstants.API_VERSION, provider.getApiVersion());
            messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA, metadataMap);
            try {
                messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_TIMEOUT,
                        APIUtil.getDefaultRequestTimeoutsForAIAPIs()
                                * APIConstants.AIAPIConstants.MILLISECONDS_IN_SECOND);
            } catch (APIManagementException e) {
                log.error("Error while retrieving REQUEST_TIMEOUT", e);
                return false;
            }

            if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_IN.equals(direction)) {
                processInboundRequest(messageContext, providerConfiguration, provider);
            } else if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_OUT.equals(direction)) {
                processOutboundResponse(messageContext, providerConfiguration, llmProviderService, metadataMap,
                        provider);
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
     * @param provider              LLM Service provider
     * @throws XMLStreamException If an error occurs while processing the XML stream.
     * @throws IOException        If an I/O error occurs.
     */
    private void processInboundRequest(MessageContext messageContext,
                                       LLMProviderConfiguration providerConfiguration,
                                       LLMProviderInfo provider)
            throws XMLStreamException, IOException, APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Processing inbound request for provider: " + provider.getName() + " with API version: " +
                    provider.getApiVersion());
        }
        String targetEndpoint = null;
        if (messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT) != null) {
            targetEndpoint = (String) messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);
        }

        if (APIConstants.AIAPIConstants.REJECT_ENDPOINT.equals(targetEndpoint)) {
            return;
        }

        Map<String, Object> roundRobinConfigs;
        if (messageContext.getProperty(APIConstants.AIAPIConstants.ROUND_ROBIN_CONFIGS) != null) {
            roundRobinConfigs =
                    (Map<String, Object>) messageContext.getProperty(APIConstants.AIAPIConstants.ROUND_ROBIN_CONFIGS);
            handleLoadBalancing(messageContext, providerConfiguration, roundRobinConfigs, provider);
            if (log.isDebugEnabled()) {
                log.debug("Load balancing configured, processing with round-robin configurations");
            }
            return;
        }

        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                APIConstants.AIAPIConstants.DEFAULT_ENDPOINT);

        Map<String, FailoverPolicyConfigDTO> failoverConfigMap = null;

        if (messageContext.getProperty(APIConstants.AIAPIConstants.FAILOVER_CONFIG_MAP) != null) {
            failoverConfigMap =
                    (Map<String, FailoverPolicyConfigDTO>) messageContext
                            .getProperty(APIConstants.AIAPIConstants.FAILOVER_CONFIG_MAP);
        }

        if (failoverConfigMap != null && !failoverConfigMap.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing failover for provider: " + provider.getName());
            }
            initFailover(messageContext, providerConfiguration, failoverConfigMap, provider);
        }

    }

    /**
     * Prepares the request for failover if a failover target model is specified.
     * It rebuilds the message, extracts the request model, and preserves request properties if needed.
     *
     * @param messageContext        The Synapse {@link MessageContext} containing API request details.
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @param failoverConfigMap     Map of failover configs
     * @param provider              LLM service provider
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during payload handling.
     */
    private void initFailover(MessageContext messageContext,
                              LLMProviderConfiguration providerConfiguration,
                              Map<String, FailoverPolicyConfigDTO> failoverConfigMap,
                              LLMProviderInfo provider)
            throws XMLStreamException, IOException, APIManagementException {

        org.apache.axis2.context.MessageContext axis2Ctx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        RelayUtils.buildMessage(axis2Ctx);
        String requestModel = extractRequestModel(getTargetModelMetadata(providerConfiguration), axis2Ctx);

        FailoverPolicyConfigDTO failoverConfig;

        failoverConfig = failoverConfigMap.get(requestModel);
        if (requestModel == null || failoverConfig == null) {
            return;
        }

        boolean isProviderAzureV1 = isAzureV1Provider(provider);

        applyFailoverConfigs(messageContext, failoverConfig, providerConfiguration, !isProviderAzureV1);
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
        return (requestModel != null) ? requestModel
                .replaceAll("\"", "").replace("\"", "") : null;
    }

    /**
     * Applies failover configuration based on the policy and provider settings.
     *
     * @param messageContext        The API request context.
     * @param policyConfig          The failover policy configuration.
     * @param providerConfiguration The LLM provider configuration.
     * @param modifyRequestPayload  Whether to modify request payload or not
     * @throws IOException            If request modification fails.
     * @throws APIManagementException If an API management error occurs.
     * @throws XMLStreamException     If an error occurs while processing the XML message.
     */
    private void applyFailoverConfigs(MessageContext messageContext, FailoverPolicyConfigDTO policyConfig,
                                      LLMProviderConfiguration providerConfiguration, boolean modifyRequestPayload)
            throws IOException, APIManagementException, XMLStreamException {

        FailoverPolicyDeploymentConfigDTO targetConfig = GatewayUtils.getTargetConfig(messageContext, policyConfig);
        if (targetConfig == null) {
            log.error("Failover configuration is null.");
            return;
        }

        ModelEndpointDTO targetModelEndpoint = targetConfig.getTargetModelEndpoint();
        if (targetModelEndpoint == null) {
            log.error("Failover configuration found, but targetModelEndpoint is null.");
            return;
        }

        List<ModelEndpointDTO> failoverEndpoints =
                GatewayUtils.filterActiveEndpoints(targetConfig.getFallbackModelEndpoints(), messageContext);

        boolean isEndpointSuspended =
                DataHolder.getInstance().isEndpointSuspended(GatewayUtils.getAPIKeyForEndpoints(messageContext),
                        GatewayUtils.getEndpointKey(targetModelEndpoint));

        if (isEndpointSuspended) {
            if (failoverEndpoints.isEmpty()) {
                messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                        APIConstants.AIAPIConstants.REJECT_ENDPOINT);
                return;
            }
            ModelEndpointDTO failoverEndpoint = failoverEndpoints.get(0);

            LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);
            if (targetModelMetadata == null) {
                log.error("Target model metadata is null. Cannot apply failover request modifications.");
                return;
            }

            if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(targetModelMetadata.getInputSource())) {
                org.apache.axis2.context.MessageContext axis2Ctx =
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext();
                RelayUtils.buildMessage(axis2Ctx);
                if (modifyRequestPayload) {
                    modifyRequestPayload(failoverEndpoint.getModel(), targetModelMetadata, axis2Ctx);
                }
            } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(
                    targetModelMetadata.getInputSource())) {
                modifyRequestPath(failoverEndpoint.getModel(), targetModelMetadata, messageContext);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Unsupported input source for attribute: " + targetModelMetadata.getAttributeName());
                }
            }

            updateTargetEndpoint(messageContext, 1, failoverEndpoint);
            log.info("Applied failover configuration with model: " + failoverEndpoint.getModel());
        }
        preserveFailoverPropertiesInMsgCtx(messageContext, policyConfig, targetModelEndpoint, failoverEndpoints);
    }

    /**
     * Preserves failover properties in the message context for further processing.
     *
     * @param messageContext      The API request context.
     * @param policyConfig        The failover policy configuration.
     * @param targetModelEndpoint The primary target model endpoint.
     * @param failoverEndpoints   The list of failover endpoints.
     * @throws APIManagementException If an API management error occurs.
     */
    private void preserveFailoverPropertiesInMsgCtx(MessageContext messageContext,
                                                    FailoverPolicyConfigDTO policyConfig,
                                                    ModelEndpointDTO targetModelEndpoint,
                                                    List<ModelEndpointDTO> failoverEndpoints)
            throws APIManagementException {

        Map<String, Object> failoverConfigurations = new HashMap<>();

        failoverConfigurations.put(APIConstants.AIAPIConstants.FAILOVER_TARGET_MODEL_ENDPOINT,
                targetModelEndpoint);
        failoverConfigurations.put(APIConstants.AIAPIConstants.FAILOVER_ENDPOINTS,
                failoverEndpoints);
        failoverConfigurations.put(APIConstants.AIAPIConstants.SUSPEND_DURATION,
                policyConfig.getSuspendDuration() * APIConstants.AIAPIConstants.MILLISECONDS_IN_SECOND);

        long requestTimeout = (policyConfig.getRequestTimeout() != null)
                ? policyConfig.getRequestTimeout()
                : APIUtil.getDefaultRequestTimeoutForFailoverConfigurations();
        messageContext.setProperty(APIConstants.AIAPIConstants.REQUEST_TIMEOUT,
                requestTimeout * APIConstants.AIAPIConstants.MILLISECONDS_IN_SECOND);

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        failoverConfigurations.put(APIConstants.AIAPIConstants.REQUEST_PAYLOAD,
                JsonUtil.jsonPayloadToString(axis2MessageContext));
        failoverConfigurations.put(APIConstants.AIAPIConstants.REQUEST_HEADERS,
                axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        failoverConfigurations.put(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD,
                axis2MessageContext.getProperty(PassThroughConstants.HTTP_METHOD));
        failoverConfigurations.put(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX,
                axis2MessageContext.getProperty(NhttpConstants.REST_URL_POSTFIX));

        messageContext.setProperty(APIConstants.AIAPIConstants.FAILOVER_CONFIGS, failoverConfigurations);
    }

    /**
     * Handles load balancing by modifying the request payload based on the target model metadata.
     *
     * @param messageContext
     * @param providerConfiguration The {@link LLMProviderConfiguration} containing provider-specific configurations.
     * @param roundRobinConfigs     The target model for which load balancing is applied.
     * @param provider              LLM service provider
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during request modification.
     */
    private void handleLoadBalancing(
            MessageContext messageContext, LLMProviderConfiguration providerConfiguration,
            Map<String, Object> roundRobinConfigs, LLMProviderInfo provider)
            throws XMLStreamException, IOException {

        LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);
        if (targetModelMetadata == null) {
            log.error("Target model metadata is null.");
            return;
        }

        ModelEndpointDTO targetModelEndpoint =
                (ModelEndpointDTO) roundRobinConfigs.get(APIConstants.AIAPIConstants.TARGET_MODEL_ENDPOINT);

        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(targetModelMetadata.getInputSource())) {
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            RelayUtils.buildMessage(axis2Ctx);
            boolean isProviderAzureV1 = isAzureV1Provider(provider);

            if (!isProviderAzureV1) {
                modifyRequestPayload(targetModelEndpoint.getModel(), targetModelMetadata, axis2Ctx);
                if (log.isDebugEnabled()) {
                    log.debug("Modified request payload with model: " + targetModelEndpoint.getModel());
                }
            }
        } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(
                targetModelMetadata.getInputSource())) {
            modifyRequestPath(targetModelEndpoint.getModel(), targetModelMetadata, messageContext);
            if (log.isDebugEnabled()) {
                log.debug("Modified request path with model: " + targetModelEndpoint.getModel());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsupported input source for attribute: " + targetModelMetadata.getAttributeName());
            }
        }

        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, targetModelEndpoint.getEndpointId());
    }

    /**
     * Modifies the request path by replacing the target model identifier with the specified model.
     *
     * @param model               The new model to set in the request path.
     * @param targetModelMetadata The {@link LLMProviderMetadata} containing metadata for extracting the model
     *                            attribute.
     * @param messageContext     The Synapse {@link MessageContext} containing the API request details.
     */
    private void modifyRequestPath(String model, LLMProviderMetadata targetModelMetadata,
                                   MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2Ctx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String requestPath = (String) axis2Ctx.getProperty(NhttpConstants.REST_URL_POSTFIX);
        if (StringUtils.isNotEmpty(requestPath)) {
            if (log.isDebugEnabled()) {
                log.debug("Modifying request path for model: " + model
                        + " with target identifier: " + targetModelMetadata.getAttributeIdentifier());
            }
            URI uri = URI.create(requestPath);
            String rawPath = uri.getRawPath();
            String rawQuery = uri.getRawQuery();

            String decodedPath = decodePathUrl(rawPath);

            // Replace the model in the decoded path
            String updatedDecodedPath = decodedPath.replaceAll(
                    targetModelMetadata.getAttributeIdentifier(),
                    java.util.regex.Matcher.quoteReplacement(model));

            // Re-encode the entire path to ensure proper URL encoding
            String updatedRawPath;
            try {
                updatedRawPath = new URI(null, null, updatedDecodedPath, null).getRawPath();
            } catch (java.net.URISyntaxException e) {
                log.warn("Failed to re-encode path with URI constructor, falling back to manual encoding");
                updatedRawPath = rawPath.replaceAll(targetModelMetadata.getAttributeIdentifier(),
                        java.util.regex.Matcher.quoteReplacement(encodePathSegmentRFC3986(model)));
            }

            StringBuilder finalPath = new StringBuilder(updatedRawPath);
            if (rawQuery != null) {
                // Preserve original query as-is to avoid changing semantics
                finalPath.append("?").append(rawQuery);
            }
            axis2Ctx.setProperty(NhttpConstants.REST_URL_POSTFIX, finalPath.toString());
            if (log.isDebugEnabled()) {
                log.debug("Updated request path from: " + requestPath +" to: " + finalPath);
            }
        }
    }

    /**
     * Decodes percent-encoded sequences in a URL path.
     *
     * @param rawPath The percent-encoded path.
     * @return The decoded path.
     */
    private String decodePathUrl(String rawPath) {
        // URLDecoder is for form-encoded data and converts + to space; escape + first to preserve it in paths
        return URLDecoder.decode(rawPath.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    /**
     * Encodes a path segment according to RFC 3986 standards.
     *
     * @param segment The path segment to encode.
     * @return The encoded path segment.
     */
    private static String encodePathSegmentRFC3986(String segment) {
        if (log.isDebugEnabled()) {
            log.debug("Encoding path segment: " + segment);
        }
        StringBuilder out = new StringBuilder();
        byte[] bytes = segment.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            char c = (char) (b & 0xFF);
            if (isUnreserved(c) || isSubDelim(c) || c == ':' || c == '@') {
                out.append(c);
            } else {
                out.append('%');
                String hx = Integer.toHexString(b & 0xFF).toUpperCase();
                if (hx.length() == 1) out.append('0');
                out.append(hx);
            }
        }
        return out.toString();
    }

    /**
     * Checks if the given character is an unreserved character as per RFC 3986.
     *
     * @param c The character to check.
     * @return {@code true} if the character is unreserved, {@code false} otherwise.
     */
    private static boolean isUnreserved(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    /**
     * Checks if the given character is a sub-delimiter as per RFC 3986.
     *
     * @param c The character to check.
     * @return {@code true} if the character is a sub-delimiter, {@code false} otherwise.
     */
    private static boolean isSubDelim(char c) {
        return c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')' ||
                c == '*' || c == '+' || c == ',' || c == ';' || c == '=';
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

        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(
                requestModelMetadata.getInputSource())) {
            String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);
            if (contentType == null) {
                log.debug("Content type is null, cannot extract request model");
                return null;
            }

            String normalizedContentType = contentType.toLowerCase();
            if (isUnsupportedContentType(normalizedContentType)) {
                return null;
            }

            if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
                return extractRequestModelFromJson(requestModelMetadata, axis2MessageContext);
            }
        } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(
                requestModelMetadata.getInputSource())) {
            String requestPath = (String) axis2MessageContext.getProperty(NhttpConstants.REST_URL_POSTFIX);

            URI uri = URI.create(requestPath);
            String rawPath = uri.getRawPath();
            // Decode the path
            String decodedPath = decodePathUrl(rawPath);

            String regex = requestModelMetadata.getAttributeIdentifier();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(decodedPath);
            if (matcher.find()) {
                String model = matcher.group();
                if (log.isDebugEnabled()) {
                    log.debug("Extracted request model from path: " + model);
                }
                return model;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unsupported input source for attribute: " + requestModelMetadata.getAttributeName());
            }
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

        return contentType.contains(MediaType.APPLICATION_XML)
                || contentType.contains(MediaType.TEXT_XML)
                || contentType.contains(MediaType.TEXT_PLAIN);
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
     * @param messageContext     The message context of the response.
     * @param providerConfigs    The configuration of the LLM provider.
     * @param llmProviderService The service handling LLM provider operations.
     * @param metadataMap        A map containing metadata information.
     * @param provider           LLM service provider
     * @throws APIManagementException If an API management error occurs.
     * @throws XMLStreamException     If an error occurs while processing the XML stream.
     * @throws IOException            If an I/O error occurs.
     */
    private void processOutboundResponse(MessageContext messageContext,
                                         LLMProviderConfiguration providerConfigs,
                                         LLMProviderService llmProviderService,
                                         Map<String, String> metadataMap,
                                         LLMProviderInfo provider)
            throws APIManagementException, XMLStreamException, IOException {

        String payload = extractPayloadFromContext(messageContext, providerConfigs);
        Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
        Map<String, String> headers = extractHeadersFromContext(messageContext);
        String requestPath = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        LLMResponseMetaData llmResponseMetaData = new LLMResponseMetaData(payload, headers, queryParams, requestPath);
        llmProviderService.getResponseMetadata(llmResponseMetaData, providerConfigs.getMetadata(), metadataMap);
        messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA, metadataMap);

        Map<String, Object> roundRobinConfigs = null;
        if (messageContext.getProperty(APIConstants.AIAPIConstants.ROUND_ROBIN_CONFIGS) != null) {
            roundRobinConfigs =
                    (Map<String, Object>) messageContext.getProperty(APIConstants.AIAPIConstants.ROUND_ROBIN_CONFIGS);
        }

        Map<String, Object> failoverConfigs = null;
        if (messageContext.getProperty(APIConstants.AIAPIConstants.FAILOVER_CONFIGS) != null) {
            failoverConfigs =
                    (Map<String, Object>) messageContext.getProperty(APIConstants.AIAPIConstants.FAILOVER_CONFIGS);
        }

        if (roundRobinConfigs == null && failoverConfigs == null) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }

        int statusCode =
                (int) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                        .getProperty(APIMgtGatewayConstants.HTTP_SC);

        if (handleSuccessfulResponse(messageContext, statusCode, providerConfigs, roundRobinConfigs, failoverConfigs)) {
            return;
        }

        if (roundRobinConfigs != null) {
            ModelEndpointDTO targetModelEndpoint =
                    (ModelEndpointDTO) roundRobinConfigs.get(APIConstants.AIAPIConstants.TARGET_MODEL_ENDPOINT);
            Long suspendDuration = (Long) roundRobinConfigs.get(APIConstants.AIAPIConstants.SUSPEND_DURATION);
            suspendTargetEndpoint(messageContext, targetModelEndpoint.getEndpointId(), targetModelEndpoint.getModel(),
                    suspendDuration);
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
            return;
        }
        if (failoverConfigs != null) {
            boolean isProviderAzureV1 = isAzureV1Provider(provider);
            handleFailover(messageContext, providerConfigs, failoverConfigs, !isProviderAzureV1);
            return;
        }
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                APIConstants.AIAPIConstants.EXIT_ENDPOINT);
    }

    /**
     * Handles the successful response by checking the status code and processing token count headers.
     * If the remaining token count is zero or below, it triggers the suspension of the target endpoint
     * based on round robin or failover configurations.
     *
     * @param messageContext        The message context containing the request and response data.
     * @param statusCode            The HTTP status code of the response.
     * @param providerConfiguration The LLM provider configuration used for fetching token metadata.
     * @param roundRobinConfigs     The configuration for round robin load balancing.
     * @param failoverConfigs       The configuration for failover handling.
     * @return True if the response is successful and further processing is done, false otherwise.
     */
    private boolean handleSuccessfulResponse(MessageContext messageContext, int statusCode,
                                             LLMProviderConfiguration providerConfiguration,
                                             Map<String, Object> roundRobinConfigs,
                                             Map<String, Object> failoverConfigs) {

        List<Integer> allowedStatusCodes = Arrays.asList(HttpStatus.SC_BAD_REQUEST,
                HttpStatus.SC_METHOD_NOT_ALLOWED, HttpStatus.SC_UNPROCESSABLE_ENTITY);
        if ((statusCode >= 200 && statusCode < 300) || allowedStatusCodes.contains(statusCode)) {
            Map<String, Object> transportHeaders = (Map<String, Object>) ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext().getProperty(
                            org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            String remainingTokenCountHeader = null;
            LLMProviderMetadata remainingTokenCountMetadata = getRemainingTokenCountMetadata(providerConfiguration);
            if (remainingTokenCountMetadata != null){
                remainingTokenCountHeader = remainingTokenCountMetadata.getAttributeIdentifier();
            }
            if (remainingTokenCountHeader != null && transportHeaders.containsKey(remainingTokenCountHeader)) {
                long remainingTokenCount = Long.parseLong((String) transportHeaders.get(remainingTokenCountHeader));
                if (remainingTokenCount <= 0) {
                    if (roundRobinConfigs != null) {

                        ModelEndpointDTO targetModelEndpoint = (ModelEndpointDTO) roundRobinConfigs
                                .get(APIConstants.AIAPIConstants.TARGET_MODEL_ENDPOINT);
                        Long suspendDuration = (Long) roundRobinConfigs
                                .get(APIConstants.AIAPIConstants.SUSPEND_DURATION);

                        suspendTargetEndpoint(messageContext, targetModelEndpoint.getEndpointId(),
                                targetModelEndpoint.getModel(),
                                suspendDuration);
                    } else if (failoverConfigs != null) {
                        int currentEndpointIndex = getCurrentFailoverIndex(messageContext);

                        if (currentEndpointIndex == 0) {
                            ModelEndpointDTO failoverTargetModelEndpoint = (ModelEndpointDTO) failoverConfigs
                                    .get(APIConstants.AIAPIConstants.FAILOVER_TARGET_MODEL_ENDPOINT);
                            Long suspendDuration = (Long) failoverConfigs
                                    .get(APIConstants.AIAPIConstants.SUSPEND_DURATION);
                            suspendTargetEndpoint(messageContext, failoverTargetModelEndpoint.getEndpointId(),
                                    failoverTargetModelEndpoint.getModel(), suspendDuration);
                        }
                        if (currentEndpointIndex > 0) {
                            List<ModelEndpointDTO> failoverEndpoints =
                                    (List<ModelEndpointDTO>) failoverConfigs
                                            .get(APIConstants.AIAPIConstants.FAILOVER_ENDPOINTS);
                            ModelEndpointDTO previousEndpoint = failoverEndpoints
                                    .get(currentEndpointIndex - 1);
                            Long suspendDuration = (Long) failoverConfigs
                                    .get(APIConstants.AIAPIConstants.SUSPEND_DURATION);

                            suspendTargetEndpoint(messageContext, previousEndpoint.getEndpointId(),
                                    previousEndpoint.getModel(), suspendDuration);
                        }
                    }
                }
            }

            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.EXIT_ENDPOINT);
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
     */
    private void suspendTargetEndpoint(MessageContext messageContext, String targetEndpoint,
                                       String targetModel, Long suspendDuration) {

        if (targetModel == null || targetEndpoint == null || suspendDuration == null) {
            return;
        }
        DataHolder.getInstance().suspendEndpoint(GatewayUtils.getAPIKeyForEndpoints(messageContext),
                getEndpointId(targetEndpoint, targetModel), suspendDuration);
    }

    /**
     * Checks if the given provider is Azure OpenAI V1 based on its name and API version.
     *
     * @param provider The LLMProviderInfo object containing provider details.
     * @return true if the provider is Azure OpenAI V1, false otherwise.
     */
    private boolean isAzureV1Provider(LLMProviderInfo provider) {
        return APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_NAME.equals(provider.getName()) &&
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_AZURE_OPENAI_VERSION.equals(provider.getApiVersion());
    }

    /**
     * Handles failover logic when an API request fails.
     *
     * @param messageContext        The message context containing request details.
     * @param providerConfiguration The configuration details of the LLM provider.
     * @param failoverConfigs       Failover Configurations
     * @param modifyRequestPayload  Whether to modify request payload or not
     * @throws XMLStreamException If an error occurs while handling XML streams.
     * @throws IOException        If an I/O error occurs during processing.
     */
    private void handleFailover(MessageContext messageContext,
                                LLMProviderConfiguration providerConfiguration,
                                Map<String, Object> failoverConfigs,
                                boolean modifyRequestPayload)
            throws XMLStreamException, IOException {

        int currentEndpointIndex = getCurrentFailoverIndex(messageContext);

        if (currentEndpointIndex == 0) {
            ModelEndpointDTO failoverTargetModelEndpoint =
                    (ModelEndpointDTO) failoverConfigs.get(APIConstants.AIAPIConstants.FAILOVER_TARGET_MODEL_ENDPOINT);
            Long suspendDuration = (Long) failoverConfigs.get(APIConstants.AIAPIConstants.SUSPEND_DURATION);
            suspendTargetEndpoint(messageContext, failoverTargetModelEndpoint.getEndpointId(),
                    failoverTargetModelEndpoint.getModel(), suspendDuration);
        }
        List<ModelEndpointDTO> failoverEndpoints =
                (List<ModelEndpointDTO>) failoverConfigs.get(APIConstants.AIAPIConstants.FAILOVER_ENDPOINTS);
        if ((currentEndpointIndex > 0 && failoverEndpoints.isEmpty())
                || currentEndpointIndex >= failoverEndpoints.size()) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.REJECT_ENDPOINT);
            return;
        }

        if (currentEndpointIndex > 0) {
            ModelEndpointDTO previousEndpoint = failoverEndpoints.get(currentEndpointIndex - 1);
            Long suspendDuration = (Long) failoverConfigs.get(APIConstants.AIAPIConstants.SUSPEND_DURATION);

            suspendTargetEndpoint(messageContext, previousEndpoint.getEndpointId(),
                    previousEndpoint.getModel(), suspendDuration);
        }
        ModelEndpointDTO failoverEndpoint = failoverEndpoints.get(currentEndpointIndex);
        updateJsonPayloadWithRequestPayload(messageContext,
                (String) failoverConfigs.get(APIConstants.AIAPIConstants.REQUEST_PAYLOAD));

        LLMProviderMetadata targetModelMetadata = getTargetModelMetadata(providerConfiguration);

        updateRequestMetadata(messageContext, failoverConfigs);

        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(targetModelMetadata.getInputSource())) {
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            RelayUtils.buildMessage(axis2Ctx);
            if (modifyRequestPayload) {
                modifyRequestPayload(failoverEndpoint.getModel(), targetModelMetadata, axis2Ctx);
            }
        } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(
                targetModelMetadata.getInputSource())) {
            modifyRequestPath(failoverEndpoint.getModel(), targetModelMetadata, messageContext);
        } else {
            if (log.isDebugEnabled()){
                log.debug("Unsupported input source for attribute: " + targetModelMetadata.getAttributeName());
            }
        }

        updateTargetEndpoint(messageContext, currentEndpointIndex + 1, failoverEndpoint);
        log.info("Failover activated, switching to endpoint with model " + failoverEndpoint.getModel());
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
    private void updateTargetEndpoint(MessageContext messageContext, int nextIndex,
                                      ModelEndpointDTO failoverEndpoint) {

        messageContext.setProperty(APIConstants.AIAPIConstants.CURRENT_ENDPOINT_INDEX, nextIndex);
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, failoverEndpoint.getEndpointId());
    }

    /**
     * Updates the JSON payload of the request by retrieving the payload from the Synapse message context
     * and setting it in the Axis2 message context.
     *
     * @param messageContext The Synapse {@link MessageContext} containing the request payload.
     * @param requestPayload Request payload.
     * @throws XMLStreamException If an error occurs while processing the XML message.
     * @throws IOException        If an I/O error occurs during payload modification.
     */
    private void updateJsonPayloadWithRequestPayload(MessageContext messageContext, String requestPayload)
            throws XMLStreamException, IOException {

        RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        JsonUtil.getNewJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext(), requestPayload,
                true, true);
    }

    /**
     * Updates the request metadata by setting relevant properties in the Axis2 message context.
     *
     * @param messageContext  The Synapse {@link MessageContext} containing API request details.
     * @param failoverConfigs
     */
    private void updateRequestMetadata(MessageContext messageContext, Map<String, Object> failoverConfigs) {

        Axis2MessageContext axis2MessageContext = (Axis2MessageContext) messageContext;
        org.apache.axis2.context.MessageContext axis2Ctx = axis2MessageContext.getAxis2MessageContext();

        axis2Ctx.setProperty(NhttpConstants.REST_URL_POSTFIX,
                failoverConfigs.get(APIConstants.AIAPIConstants.REQUEST_REST_URL_POSTFIX));
        axis2Ctx.setProperty(PassThroughConstants.HTTP_METHOD,
                failoverConfigs.get(APIConstants.AIAPIConstants.REQUEST_HTTP_METHOD));
        axis2Ctx.setProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS,
                failoverConfigs.get(APIConstants.AIAPIConstants.REQUEST_HEADERS));
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
     * Retrieves the metadata for the remaining token count of the LLM provider.
     *
     * @param providerConfiguration The LLM provider configuration.
     * @return The metadata containing the remaining token count.
     */
    private LLMProviderMetadata getRemainingTokenCountMetadata(LLMProviderConfiguration providerConfiguration) {

        return findMetadataByAttributeName(providerConfiguration.getMetadata(),
                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REMAINING_TOKEN_COUNT);
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
     * Modifies a JSON property in the given Axis2 message context by updating its value.
     * If the property does not exist but the parent object exists, it will be added.
     * <p>
     * This method does not create missing parent objects if the specified JSONPath does not exist.
     *
     * @param axis2MessageContext The Axis2 message context containing the JSON payload.
     * @param jsonPath            The JSONPath expression specifying the property to modify or add.
     * @param newValue            The new value to set for the specified property.
     * @throws AxisFault If an error occurs while modifying the JSON payload.
     */
    private static void modifyJsonPayload(org.apache.axis2.context.MessageContext axis2MessageContext,
                                          String jsonPath, String newValue) throws AxisFault {

        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return;
        }
        String jsonPayload = JsonUtil.jsonPayloadToString(axis2MessageContext);
        try {
            DocumentContext jsonContext = JsonPath.parse(jsonPayload);
            try {
                jsonContext.read(jsonPath);
                jsonContext.set(jsonPath, newValue);
            } catch (PathNotFoundException e) {
                int lastDotIndex = jsonPath.lastIndexOf('.');
                if (lastDotIndex != -1) {
                    String parentPath = jsonPath.substring(0, lastDotIndex);
                    String propertyName = jsonPath.substring(lastDotIndex + 1);
                    try {
                        jsonContext.read(parentPath);
                        jsonContext.put(parentPath, propertyName, newValue);
                    } catch (PathNotFoundException ex) {
                        log.warn("Parent path missing. Skipping addition of new property: " + jsonPath);
                    }
                }
            }

            String updatedJson = jsonContext.jsonString();
            JsonUtil.getNewJsonPayload(axis2MessageContext, updatedJson, true, true);
        } catch (Exception e) {
            log.error("Error modifying JSON payload", e);
            throw new AxisFault("Failed to modify JSON payload", e);
        }
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
