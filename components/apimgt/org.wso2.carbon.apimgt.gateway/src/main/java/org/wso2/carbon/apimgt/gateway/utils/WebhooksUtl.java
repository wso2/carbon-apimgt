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
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
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
public class WebhooksUtl {

    private static final Log log = LogFactory.getLog(WebhooksUtl.class);

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
        String context = (String) messageContext.getProperty(APIMgtGatewayConstants.CONTEXT);
        String apiVersion = (String) messageContext.getProperty(APIMgtGatewayConstants.VERSION);
        API api = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain).
                getApiByContextAndVersion(context, apiVersion );
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
        String apiKey = WebhooksUtl.generateAPIKey(messageContext, tenantDomain);
        String urlQueryParams = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(APIConstants.TRANSPORT_URL_IN);
        List<NameValuePair> queryParameter = URLEncodedUtils.parse(new URI(urlQueryParams),
                StandardCharsets.UTF_8.name());
        String topicName = null;
        for (NameValuePair nvPair : queryParameter) {
            if (nvPair.getName().equals(APIConstants.Webhooks.TOPIC_QUERY_PARAM)) {
                topicName = nvPair.getValue();
            }
        }
        String subscriptionKey = apiKey + "_" + topicName;
        messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_TOPIC_PROPERTY, topicName);
        return ServiceReferenceHolder.getInstance().getSubscriptionsDataService()
                .getSubscriptionsList(subscriptionKey, tenantDomain);
    }
}
