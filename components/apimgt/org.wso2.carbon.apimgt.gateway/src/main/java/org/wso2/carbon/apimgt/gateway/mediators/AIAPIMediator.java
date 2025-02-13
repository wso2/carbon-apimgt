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
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderMetadata;
import org.wso2.carbon.apimgt.api.LLMProviderService;
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
                log.error("LLM provider service not found for the provider with ID: " + llmProviderId);
                return false;
            }

            Map<String, String> metadataMap = new HashMap<>();
            if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_IN.equals(direction)) {
                metadataMap.put(APIConstants.AIAPIConstants.NAME, provider.getName());
                metadataMap.put(APIConstants.AIAPIConstants.API_VERSION, provider.getApiVersion());
                messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA, metadataMap);

                Object targetEndpointObj = messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);
                Object targetModelObj = messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);

                String targetEndpoint = targetEndpointObj instanceof String ? (String) targetEndpointObj : null;
                String targetModel = targetModelObj instanceof String ? (String) targetModelObj : null;

                if (APIConstants.AIAPIConstants.REJECT_ENDPOINT.equals(targetEndpoint)) {
                    return true;
                }
                if (targetEndpoint != null && targetModel != null) {
                    LLMProviderMetadata targetModelMetadata = findMetadataByAttributeName(
                            providerConfiguration.getMetadata(),
                            APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_REQUEST_MODEL);

                    if (targetModelMetadata == null) {
                        targetModelMetadata = findMetadataByAttributeName(
                                providerConfiguration.getMetadata(),
                                APIConstants.AIAPIConstants.LLM_PROVIDER_SERVICE_METADATA_MODEL);
                    }

                    if (targetModelMetadata != null && APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD
                            .equalsIgnoreCase(targetModelMetadata.getInputSource())) {
                        modifyRequestPayload(targetModel, targetModelMetadata,
                                ((Axis2MessageContext) messageContext).getAxis2MessageContext());
                    } else {
                        log.debug("Unsupported input source: " + (targetModelMetadata != null ?
                                targetModelMetadata.getInputSource() : "null") + " for attribute: " +
                                (targetModelMetadata != null ? targetModelMetadata.getAttributeName() : "unknown"));
                    }
                } else {
                    messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                            APIConstants.AIAPIConstants.DEFAULT_ENDPOINT);
                }
            } else if (APIConstants.AIAPIConstants.TRAFFIC_FLOW_DIRECTION_OUT.equals(direction)) {
                String targetModel = (String)
                        messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_MODEL);
                String targetEndpoint = (String)
                        messageContext.getProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT);

                if (targetEndpoint != null && targetModel != null) {
                    int statusCode = (int) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                            .getProperty(APIMgtGatewayConstants.HTTP_SC);

                    if (statusCode < 200 || statusCode >= 300) {

                        Long suspendDuration = (Long)
                                messageContext.getProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION);
                        DataHolder.getInstance().suspendEndpoint(GatewayUtils.getAPIKeyForEndpoints(messageContext),
                                getEndpointId(targetEndpoint, targetModel),
                                suspendDuration * 1000);
                        return true;
                    }
                }

                String payload = extractPayloadFromContext(messageContext, providerConfiguration);
                Map<String, String> queryParams = extractQueryParamsFromContext(messageContext);
                Map<String, String> headers = extractHeadersFromContext(messageContext);

                llmProviderService.getResponseMetadata(payload, headers, queryParams,
                        providerConfiguration.getMetadata(), metadataMap);
                messageContext.setProperty(APIConstants.AIAPIConstants.AI_API_RESPONSE_METADATA, metadataMap);
            }
        } catch (Exception e) {
            if (e instanceof APIManagementException) {
                log.error("Error occurred while extracting metadata.", e);
            } else if (e instanceof XMLStreamException || e instanceof IOException) {
                log.error("Error occurred while reading payload.", e);
            }
            return false;
        }

        return true;
    }

    /**
     * Modifies the request payload based on its content type. Supports JSON (via JSONPath)
     * and plain text modifications; XML payloads remain unchanged.
     *
     * @param targetModel         the new value to set
     * @param targetModelMetadata metadata containing the attribute identifier
     * @param axis2MessageContext the Axis2 message context with the request payload
     * @return {@code true} if modified, {@code false} if not possible, or {@code null} for XML/undefined content types
     * @throws XMLStreamException if XML processing fails
     * @throws IOException        if an I/O error occurs
     */

    public static Boolean modifyRequestPayload(String targetModel, LLMProviderMetadata targetModelMetadata,
                                               org.apache.axis2.context.MessageContext axis2MessageContext)
            throws XMLStreamException, IOException {

        RelayUtils.buildMessage(axis2MessageContext);
        String contentType = (String) axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_CONTENT_TYPE);

        if (contentType == null) {
            return null;
        }
        String normalizedContentType = contentType.toLowerCase();
        String attributeIdentifier = targetModelMetadata.getAttributeIdentifier();

        if (normalizedContentType.contains(MediaType.APPLICATION_XML)
                || normalizedContentType.contains(MediaType.TEXT_XML)) {
            return null;
        } else if (normalizedContentType.contains(MediaType.APPLICATION_JSON)) {
            return modifyJsonPayload(axis2MessageContext, attributeIdentifier, targetModel);
        } else if (normalizedContentType.contains(MediaType.TEXT_PLAIN)) {
            return modifyTextPayload(axis2MessageContext, targetModel);
        }

        return false;
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
