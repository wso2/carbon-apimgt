/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.api.ApiUtils;
import org.apache.synapse.api.dispatch.URITemplateHelper;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionType;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.listener.ExtensionListenerUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;
import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE_SUBSCRIBE;

/**
 * Handler used for web hook apis. This handler retrieves the topic name, to which subscription request is coming and
 * will set it to the synapse msg context.
 * <pre>
 * {@code
 * <handler class="org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook.WebhookApiHandler">
 *     <property name="eventReceiverResourcePath" value="/webhooks_events_receiver_resource"/>
 *     <property name="topicQueryParamName" value="hub.topic"/>
 * </handler>
 * }
 * </pre>
 */
public class WebhookApiHandler extends APIAuthenticationHandler {

    private static final Log log = LogFactory.getLog(WebhookApiHandler.class);
    private static final String EMPTY_STRING = "";
    private static final String TEXT_CONTENT_TYPE = "text/plain";
    private static final String REST_API_CONTEXT_PROPERTY_KEY = "REST_API_CONTEXT";
    private static final String SANITIZED_REST_API_CONTEXT_PROPERTY_KEY = "SANITIZED_API_CONTEXT";
    private static final String WEBSUB_MATCHED_TOPIC = "WEBSUB_MATCHED_TOPIC";
    private static final String WEBSUB_REQUESTED_TOPIC = "WEBSUB_REQUESTED_TOPIC";
    private static final String WEBSUB_URL_PATTERNS = "WEBSUB_URL_PATTERNS";

    private final String type = ExtensionType.WEBSUB_TOPIC_RESOLVER.toString();
    private String eventReceiverResourcePath = APIConstants.WebHookProperties.DEFAULT_SUBSCRIPTION_RESOURCE_PATH;
    private String topicQueryParamName = APIConstants.WebHookProperties.DEFAULT_TOPIC_QUERY_PARAM_NAME;

    @Override
    public boolean handleRequest(MessageContext synCtx) {

        String requestSubPath = getRequestSubPath(synCtx);
        String apiContext = (String) synCtx.getProperty(REST_API_CONTEXT_PROPERTY_KEY);
        if (apiContext != null) {
            String sanitizedApiContext = apiContext.replace("/", "__");
            synCtx.setProperty(SANITIZED_REST_API_CONTEXT_PROPERTY_KEY, sanitizedApiContext);
        }

        // all other requests are assumed to be for subscription as there will be only 2 resources for web hook api
        if (!requestSubPath.startsWith(eventReceiverResourcePath)) {
            HashMap<String, String> hubParameters = new HashMap<>();
            org.apache.axis2.context.MessageContext axisCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
            String contentType = (String) axisCtx.getProperty(SynapseConstants.AXIS2_PROPERTY_CONTENT_TYPE);
            if (contentType != null && contentType.contains(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
                populateParamsWithFormUrlEncodedData(synCtx, hubParameters);
            } else {
                populateParamsWithQueryData(synCtx, hubParameters);
            }
            if (!hasMandatorySubscriptionParameters(hubParameters)) {
                handleFailure(synCtx,
                              "One or more mandatory parameters were not found in web hook subscription request");
                return false;
            }
            Object httpVerb = axisCtx.getProperty(HTTP_METHOD);
            axisCtx.setProperty(HTTP_METHOD, APIConstants.SubscriptionCreatedStatus.SUBSCRIBE);
            synCtx.setProperty(APIConstants.Webhooks.SUBSCRIPTION_PARAMETER_PROPERTY, hubParameters);
            synCtx.setProperty(APIConstants.API_TYPE, APIConstants.API_TYPE_WEBSUB);

            String providedTopic = hubParameters.get(APIConstants.WebHookProperties.DEFAULT_TOPIC_QUERY_PARAM_NAME);
            String electedResource = resolveElectedResource(synCtx, providedTopic);
            if (electedResource == null) {
                return false;
            }
            synCtx.setProperty(APIConstants.API_ELECTED_RESOURCE, electedResource);
            synCtx.setProperty(ASYNC_MESSAGE_TYPE, ASYNC_MESSAGE_TYPE_SUBSCRIBE);
            boolean authenticationResolved = super.handleRequest(synCtx);
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    setProperty(Constants.Configuration.HTTP_METHOD, httpVerb);
            if (authenticationResolved) {
                return ExtensionListenerUtil.postProcessRequest(synCtx, type);
            }
            return false;
        } else {
            String providedTopic = extractTopicFromQueryParams(synCtx);
            if (providedTopic == null) {
                synCtx.setProperty(APIConstants.TOPIC_VALIDITY, String.valueOf(false));
                return handleEventReceiver(synCtx);
            }

            String matchedTopic = invokeExtensionListenerForTopicMatching(synCtx, providedTopic);
            if (matchedTopic == null && isExtensionListenerAborted(synCtx)) {
                return false;
            }

            boolean isValidTopic;
            if (matchedTopic != null) {
                isValidTopic = true;
            } else {
                isValidTopic = validateTopic(synCtx, providedTopic);
            }
            synCtx.setProperty(APIConstants.TOPIC_VALIDITY, String.valueOf(isValidTopic));
            return handleEventReceiver(synCtx);
        }
    }

    public boolean validateTopic(MessageContext messageContext, String topicName) {
        if (log.isDebugEnabled()) {
            log.debug("Validating topic for webhook event");
        }

        if (topicName == null || topicName.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid topic name in the request");
            }
            return false;
        }

        API api = GatewayUtils.getAPI(messageContext);
        if (api != null) {
            List<String> urlPatterns = getUrlPatterns(api.getUrlMappings());
            String matchingTopic = getMatchingTopic(topicName, urlPatterns);
            if (matchingTopic != null) {
                log.info("Valid topic found for webhook event");
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Topic validation failed for webhook event");
        }
        return false;
    }

    private String extractTopicFromQueryParams(MessageContext synCtx) {
        String urlQueryParams = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .getProperty(APIConstants.TRANSPORT_URL_IN);
        if (urlQueryParams == null) {
            return null;
        }
        try {
            List<NameValuePair> queryParameters = URLEncodedUtils.parse(new URI(urlQueryParams),
                    StandardCharsets.UTF_8.name());
            for (NameValuePair nvPair : queryParameters) {
                if (APIConstants.Webhooks.TOPIC_QUERY_PARAM.equals(nvPair.getName())) {
                    return nvPair.getValue();
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error parsing URI for topic extraction: " + e.getMessage());
        }
        return null;
    }

    private boolean handleEventReceiver(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axisMsgContext = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axisMsgContext);
            String payload;
            String contentType = getContentType(axisMsgContext);
            if (JsonUtil.hasAJsonPayload(axisMsgContext)) {
                payload = JsonUtil.jsonPayloadToString(axisMsgContext);
            } else if (contentType != null && contentType.contains(TEXT_CONTENT_TYPE)) {
                payload = synCtx.getEnvelope().getBody().getFirstElement().getText();
            } else {
                payload = synCtx.getEnvelope().getBody().getFirstElement().toString();
            }
            synCtx.setProperty(APIConstants.Webhooks.PAYLOAD_PROPERTY, payload);
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            synCtx.setProperty(APIConstants.TENANT_DOMAIN_INFO_PROPERTY, tenantDomain);
            return ExtensionListenerUtil.postProcessRequest(synCtx, type);
        } catch (IOException | XMLStreamException e) {
            log.error("Error while building the message", e);
            return false;
        }
    }

    private String getContentType(org.apache.axis2.context.MessageContext axisMsgContext) {
        Object headers = axisMsgContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map transportHeaders = (Map) headers;
        if (transportHeaders != null) {
            return (String) transportHeaders.get(HTTP.CONTENT_TYPE);
        }
        return null;
    }

    private String getRequestSubPath(MessageContext synCtx) {

        Object requestSubPath = synCtx.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
        if (requestSubPath != null) {
            return requestSubPath.toString();
        }
        return Utils.getSubRequestPath(Utils.getSelectedAPI(synCtx), synCtx);
    }

    /**
     * This method handle the failure
     *
     * @param messageContext   message context of the request
     * @param errorDescription description of the error
     */
    private void handleFailure(MessageContext messageContext, String errorDescription) {
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        throw new SynapseException(errorDescription);
    }

    public void setEventReceiverResourcePath(String eventReceiverResourcePath) {
        this.eventReceiverResourcePath = eventReceiverResourcePath;
    }

    public void setTopicQueryParamName(String topicQueryParamName) {
        this.topicQueryParamName = topicQueryParamName;
    }

    /**
     * Populates subscriber request parameters to a provided Map
     * from form-URL-encoded data.
     *
     * @param synCtx        Request message context
     * @param hubParameters Map to be populated
     */
    private void populateParamsWithFormUrlEncodedData(MessageContext synCtx, HashMap<String, String> hubParameters) {

        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        try {
            RelayUtils.buildMessage(axis2MsgCtx, false);
            SOAPEnvelope soapEnvelope = synCtx.getEnvelope();
            if (soapEnvelope != null) {
                OMElement xFormValuesOMElement = soapEnvelope.getBody().getFirstElement();
                Iterator<OMElement> children = xFormValuesOMElement.getChildElements();
                while (children.hasNext()) {
                    // insert all available parameters to the parameter map
                    OMElement requestElement = children.next();
                    hubParameters.put(requestElement.getQName().toString(), requestElement.getText());
                }
            }
        } catch (IOException | XMLStreamException e) {
            log.error("Error building the subscription request payload", e);
        }
    }

    /**
     * Populates subscriber request parameters to a provided Map
     * from query parameters.
     *
     * @param synCtx        Request message context
     * @param hubParameters Map to be populated
     */
    private void populateParamsWithQueryData(MessageContext synCtx, HashMap<String, String> hubParameters) {
        String url = ApiUtils.getFullRequestPath(synCtx);
        try {
            List<NameValuePair> queryParameter = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8.name());
            for (NameValuePair nvPair : queryParameter) {
                hubParameters.put(nvPair.getName(), nvPair.getValue());
            }
        } catch (URISyntaxException e) {
            log.error("Error while parsing the query parameters", e);
        }
    }

    /**
     * Checks the availability of hub.topic, hub.mode and hub.callback
     * parameters in the request parameter map.
     *
     * @param hubParameters Map
     * @return boolean
     */
    private boolean hasMandatorySubscriptionParameters(HashMap<String, String> hubParameters) {
        return StringUtils.isNotEmpty(hubParameters.get(topicQueryParamName)) && StringUtils.isNotEmpty(
                hubParameters.get(APIConstants.Webhooks.HUB_CALLBACK_QUERY_PARAM)) && StringUtils.isNotEmpty(
                hubParameters.get(APIConstants.Webhooks.HUB_MODE_QUERY_PARAM));
    }

    private String resolveElectedResource(MessageContext synCtx, String providedTopic) {
        Object apiObject = synCtx.getProperty("API");
        List<String> urlPatterns = null;
        if (apiObject != null) {
            API api = (API) apiObject;
            urlPatterns = getUrlPatterns(api.getUrlMappings());
        }

        String matchedTopic = invokeExtensionListenerForTopicMatching(synCtx, providedTopic, urlPatterns);
        if (matchedTopic == null && isExtensionListenerAborted(synCtx)) {
            return null;
        }
        if (matchedTopic != null) {
            return matchedTopic;
        }

        // Fall back to default matching
        if (urlPatterns != null) {
            String fallbackMatch = getMatchingTopic(providedTopic, urlPatterns);
            if (fallbackMatch != null) {
                return fallbackMatch;
            }
        }
        return providedTopic;
    }

    private String invokeExtensionListenerForTopicMatching(MessageContext synCtx, String providedTopic) {
        Object apiObject = synCtx.getProperty("API");
        List<String> urlPatterns = null;
        if (apiObject != null) {
            API api = (API) apiObject;
            urlPatterns = getUrlPatterns(api.getUrlMappings());
        }
        return invokeExtensionListenerForTopicMatching(synCtx, providedTopic, urlPatterns);
    }

    @SuppressWarnings("unchecked")
    private String invokeExtensionListenerForTopicMatching(MessageContext synCtx, String providedTopic,
                                                           List<String> urlPatterns) {
        Map<String, Object> existingCustomProps = (Map<String, Object>) synCtx.getProperty(
                APIMgtGatewayConstants.CUSTOM_PROPERTY);

        Map<String, Object> extensionInput = new HashMap<>();
        if (existingCustomProps != null) {
            extensionInput.putAll(existingCustomProps);
        }
        extensionInput.put(WEBSUB_REQUESTED_TOPIC, providedTopic);
        extensionInput.put(WEBSUB_URL_PATTERNS, urlPatterns);
        synCtx.setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY, extensionInput);

        boolean continueFlow = ExtensionListenerUtil.preProcessRequest(synCtx, type);

        Map<String, Object> extensionOutput = (Map<String, Object>) synCtx.getProperty(
                APIMgtGatewayConstants.CUSTOM_PROPERTY);
        String matchedTopic = extensionOutput != null ? (String) extensionOutput.get(WEBSUB_MATCHED_TOPIC) : null;

        // Restore custom properties: merge original + output, removing temporary keys
        Map<String, Object> restored = new HashMap<>();
        if (existingCustomProps != null) {
            restored.putAll(existingCustomProps);
        }
        if (extensionOutput != null) {
            restored.putAll(extensionOutput);
        }
        restored.remove(WEBSUB_REQUESTED_TOPIC);
        restored.remove(WEBSUB_URL_PATTERNS);

        if (restored.isEmpty()) {
            synCtx.setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY, null);
        } else {
            synCtx.setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY, restored);
        }

        if (!continueFlow) {
            synCtx.setProperty("WEBSUB_EXTENSION_ABORTED", Boolean.TRUE);
        } else {
            synCtx.setProperty("WEBSUB_EXTENSION_ABORTED", null);
        }

        return matchedTopic;
    }

    private boolean isExtensionListenerAborted(MessageContext synCtx) {
        return Boolean.TRUE.equals(synCtx.getProperty("WEBSUB_EXTENSION_ABORTED"));
    }

    private String getMatchingTopic(String providedTopic, List<String> urlPatterns) {
        if (urlPatterns == null || urlPatterns.isEmpty()) {
            return null;
        }

        // Check for exact match
        for (String urlPattern : urlPatterns) {
            if (providedTopic.equals(urlPattern)) {
                return urlPattern;
            }
        }

        // Check for URI template match (wildcard support)
        for (String urlPattern : urlPatterns) {
            URITemplateHelper uriTemplateHelper = new URITemplateHelper(urlPattern);
            Map<String, String> variables = new HashMap<>();
            if (uriTemplateHelper.getUriTemplate().matches(providedTopic, variables)) {
                return urlPattern;
            }
        }

        return null;
    }

    private List<String> getUrlPatterns(List<URLMapping> urlMappings) {
        if (urlMappings == null) {
            return null;
        }
        List<String> urlPatterns = new ArrayList<>();
        for (URLMapping urlMapping : urlMappings) {
            urlPatterns.add(urlMapping.getUrlPattern());
        }
        return urlPatterns;
    }
}
