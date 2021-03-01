/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
/*
This is the util class for webhooks related operations
 */
public class WebhooksUtils {

    private static final Log log = LogFactory.getLog(WebhooksUtils.class);

    /**
     * This method is used to call the eventhub rest API to persist data .
     *
     * @param jsonString        the message body.
     * @param retriesCount      the retries count if the message delivery failed to the REST API.
     * @param eventType         the event type. (subscribe event/ delivery status update event).
     * @return the http response.
     */
    public static HttpResponse persistData(String jsonString, int retriesCount, String eventType) throws InterruptedException, IOException {
        String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                "/notify");
        HttpPost method = new HttpPost(url);
        HttpEntity stringEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
        method.setEntity(stringEntity);
        byte[] credentials = Base64.encodeBase64((getEventHubConfiguration().getUsername() + ":" +
                getEventHubConfiguration().getPassword()).getBytes(StandardCharsets.UTF_8));
        method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
        method.setHeader(APIConstants.KeyManager.KEY_MANAGER_TYPE_HEADER, eventType);
        URL eventHubURL = new URL(url);
        int eventHubPort = eventHubURL.getPort();
        String eventHubProtocol = eventHubURL.getProtocol();
        HttpClient httpClient = APIUtil.getHttpClient(eventHubPort, eventHubProtocol);
        int retryCount = 0;
        HttpResponse httpResponse = null;
        boolean retry;
        do {
            try {
                httpResponse = httpClient.execute(method);
                retry = false;
            } catch (IOException ex) {
                retryCount++;
                if (retryCount < retriesCount) {
                    retry = true;
                    log.warn("Failed to persist subscription request to remote endpoint: " +
                            ex.getMessage() + ". Retrying after " + retriesCount +
                            " seconds...");
                    Thread.sleep(retriesCount * 1000);
                } else {
                    throw ex;
                }
            }
        } while (retry);
        return httpResponse;
    }

    /**
     * This method is used to get the eventhub DTO instance.
     *
     * @return the eventhub DTO.
     */
    public static EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }

    /**
     * This method is used to generate the API Key.
     *
     * @param messageContext     the message context.
     * @return the generated API Key.
     */
    public static String generateAPIKey(MessageContext messageContext, String tenantDomain) {
        String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        API api = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain).
                getApiByContextAndVersion(context, apiVersion);
        return api.getUuid();
    }

    /**
     * This method is used to get the subscribers list from the in memory map.
     *
     * @param messageContext    the message context.
     * @return the list of subscribers.
     */
    public static List<WebhooksDTO> getSubscribersListFromInMemoryMap(MessageContext messageContext)
            throws URISyntaxException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        String apiKey = WebhooksUtils.generateAPIKey(messageContext, tenantDomain);
        String urlQueryParams = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(APIConstants.TRANSPORT_URL_IN);
        List<NameValuePair> queryParameter = URLEncodedUtils.parse(new URI(urlQueryParams),
                StandardCharsets.UTF_8.name());
        String topicName = null;
        for (NameValuePair nvPair : queryParameter) {
            if (APIConstants.Webhooks.TOPIC_QUERY_PARAM.equals(nvPair.getName())) {
                topicName = nvPair.getValue();
            }
        }
        String subscriptionKey = apiKey + "_" + topicName;
        messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_TOPIC_PROPERTY, topicName);
        return ServiceReferenceHolder.getInstance().getSubscriptionsDataService()
                .getSubscriptionsList(subscriptionKey, tenantDomain);
    }

    /**
     * check if the request is throttled
     *
     * @param resourceLevelThrottleKey
     * @param subscriptionLevelThrottleKey
     * @param applicationLevelThrottleKey
     * @return true if request is throttled out
     */
    public static boolean isThrottled(String resourceLevelThrottleKey, String subscriptionLevelThrottleKey,
                                      String applicationLevelThrottleKey) {
        boolean isApiLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                .isAPIThrottled(resourceLevelThrottleKey);
        boolean isSubscriptionLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                .isThrottled(subscriptionLevelThrottleKey);
        boolean isApplicationLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                .isThrottled(applicationLevelThrottleKey);
        return (isApiLevelThrottled || isApplicationLevelThrottled || isSubscriptionLevelThrottled);
    }

    public static void handleThrottleOutMessage(MessageContext messageContext) {
        String errorMessage = "Message throttled out";
        String errorDescription = "You have exceeded your quota";
        int errorCode = APIThrottleConstants.CONNECTIONS_COUNT_THROTTLE_OUT_ERROR_CODE;
        int httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, httpErrorCode);

        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message
            // in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription));
        }

        sendFault(messageContext, httpErrorCode);
    }

    public static OMElement getFaultPayload(int throttleErrorCode, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(throttleErrorCode));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);
        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    public static void sendFault(MessageContext messageContext, int httpErrorCode) {
        Utils.sendFault(messageContext, httpErrorCode);
    }
}
