/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.mediators.webhooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.utils.WebhooksUtl;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This mediator would persist webhooks subscription data.
 */
public class SubscribersPersistMediator extends AbstractMediator {
    private static final int subscriptionDataPersisRetries = 15;
    private String topicName;
    private String callback;
    private String secret;
    private String mode;
    private String leaseSeconds;

    @Override
    public boolean mediate(MessageContext messageContext) {
        try {
            populateQueryParamData(messageContext);
            if (StringUtils.isEmpty(callback)) {
                handleException("Callback URL cannot be empty", messageContext);
            }
            if (StringUtils.isEmpty(topicName)) {
                handleException("Topic name cannot be empty", messageContext);
            }
            if (StringUtils.isEmpty(mode)) {
                handleException("Mode cannot be empty", messageContext);
            } else if (!(APIConstants.Webhooks.SUBSCRIBE_MODE.equalsIgnoreCase(mode.trim()) || APIConstants.Webhooks.
                    UNSUBSCRIBE_MODE.equalsIgnoreCase(mode.trim()))) {
                handleException("Invalid Entry for hub.mode", messageContext);
            }
            String tenantDomain = (String) messageContext.getProperty(APIConstants.TENANT_DOMAIN_INFO_PROPERTY);
            String apiKey = WebhooksUtl.generateAPIKey(messageContext, tenantDomain);
            String applicationID = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID);
            String jsonString = generateRequestBody(apiKey, applicationID, tenantDomain, callback,
                    topicName, secret, mode, leaseSeconds);
            HttpResponse httpResponse = WebhooksUtl.persistData(jsonString, subscriptionDataPersisRetries,
                    APIConstants.Webhooks.SUBSCRIPTION_EVENT_TYPE);
            handleResponse(httpResponse, messageContext);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            handleException("Error while publishing event data ", e, messageContext);
        }
        return true;
    }

    /**
     * This method is used to handle the response of the REST API request to persist data.
     *
     * @param httpResponse      the http response of the persist request.
     * @param messageContext    the message context.
     */
    private void handleResponse(HttpResponse httpResponse, MessageContext messageContext) throws IOException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully submitted the request for persist subscription with status code: "
                        + statusCode);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Failed to submit the request for persist subscription with status code: " + statusCode);
            }
            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            handleException(response, messageContext);
        }
    }

    /**
     * This method is used to populate query param data of the subscription request.
     *
     * @param messageContext    the message context.
     */
    private void populateQueryParamData(MessageContext messageContext) throws URISyntaxException {
        String urlQueryParams = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(APIConstants.TRANSPORT_URL_IN);
        if (StringUtils.isEmpty(urlQueryParams)) {
            handleException("Invalid subscription request: URL params are missing", messageContext);
        }
        List<NameValuePair> queryParameter = URLEncodedUtils.parse(new URI(urlQueryParams),
                StandardCharsets.UTF_8.name());
        for (NameValuePair nvPair : queryParameter) {
            if (nvPair.getName().equals(APIConstants.Webhooks.HUB_TOPIC_QUERY_PARAM)) {
                topicName = nvPair.getValue();
            }
            if (nvPair.getName().equals(APIConstants.Webhooks.HUB_CALLBACK_QUERY_PARAM)) {
                callback = nvPair.getValue();
            }
            if (nvPair.getName().equals(APIConstants.Webhooks.HUB_SECRET_QUERY_PARAM)) {
                secret = nvPair.getValue();
            }
            if (nvPair.getName().equals(APIConstants.Webhooks.HUB_MODE_QUERY_PARAM)) {
                mode = nvPair.getValue();
            }
            if (nvPair.getName().equals(APIConstants.Webhooks.HUB_LEASE_SECONDS_QUERY_PARAM)) {
                leaseSeconds = nvPair.getValue();
            }
        }
    }

    /**
     * This method is used to generate the request body for the API call.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param applicationID     the application ID of the subscriber.
     * @param tenantDomain      the tenant domain.
     * @param callback          the subscriber's callback url.
     * @param topicName         the subscriber's topic name.
     * @param secret            the subscriber's secret key.
     * @param mode              the mode of the subscription.
     * @param leaseSeconds      the lease seconds value of the subscription.
     * @return the generated body.
     */
    private String generateRequestBody(String apiKey, String applicationID, String tenantDomain, String callback,
                                       String topicName, String secret, String mode, String leaseSeconds) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put(APIConstants.Webhooks.API_KEY_PROPERTY, apiKey);
        node.put(APIConstants.Webhooks.APP_ID_PROPERTY,applicationID);
        node.put(APIConstants.Webhooks.TENANT_DOMAIN_PROPERTY,tenantDomain);
        node.put(APIConstants.Webhooks.CALLBACK_PROPERTY, callback);
        node.put(APIConstants.Webhooks.TOPIC_PROPERTY, topicName);
        node.put(APIConstants.Webhooks.MODE_PROPERTY, mode);
        node.put(APIConstants.Webhooks.SECRET_PROPERTY, secret);
        node.put(APIConstants.Webhooks.LEASE_SECONDS_PROPERTY, leaseSeconds);
        return node.toString();
    }
}
