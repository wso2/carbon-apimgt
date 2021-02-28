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
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.WebhooksUtils;
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
            AuthenticationContext authenticationContext = APISecurityUtils.getAuthenticationContext(messageContext);
            String tenantDomain = (String) messageContext.getProperty(APIConstants.TENANT_DOMAIN_INFO_PROPERTY);
            int tenantID = (Integer) messageContext.getProperty(APIConstants.TENANT_ID_INFO_PROPERTY);
            String apiKey = WebhooksUtils.generateAPIKey(messageContext, tenantDomain);
            String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String applicationID = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID);
            if (APIConstants.Webhooks.SUBSCRIBE_MODE.equalsIgnoreCase(mode) &&
                    isThrottled(applicationID, apiKey, tenantDomain)) {
                WebhooksUtils.handleThrottleOutMessage(messageContext);
                return false;
            }
            String jsonString = generateRequestBody(apiKey, apiContext, apiVersion, applicationID, tenantDomain,
                    tenantID, authenticationContext);
            HttpResponse httpResponse = WebhooksUtils.persistData(jsonString, subscriptionDataPersisRetries,
                    APIConstants.Webhooks.SUBSCRIPTION_EVENT_TYPE);
            handleResponse(httpResponse, messageContext);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
                Utils.setFaultPayload(messageContext, WebhooksUtils.getFaultPayload(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Error while persisting request", "Check the request format"));
            }
            //dataCollector.collectData(messageContext);
            WebhooksUtils.sendFault(messageContext, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return true;
    }

    private boolean isThrottled(String appID, String apiUUID, String tenantDomain) {
        return ServiceReferenceHolder.getInstance().getSubscriptionsDataService().getThrottleStatus(appID, apiUUID,
                tenantDomain);
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
            //dataCollector.collectData(messageContext);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Failed to submit the request for persist subscription with status code: " + statusCode);
            }
            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (response.contains("Throttle")) {
                WebhooksUtils.handleThrottleOutMessage(messageContext);
            }
            if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
                Utils.setFaultPayload(messageContext, WebhooksUtils.getFaultPayload(statusCode, response, response));
            }
            //dataCollector.collectData(messageContext);
            WebhooksUtils.sendFault(messageContext, statusCode);
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
            if (APIConstants.Webhooks.HUB_TOPIC_QUERY_PARAM.equals(nvPair.getName())) {
                topicName = nvPair.getValue();
            }
            if (APIConstants.Webhooks.HUB_CALLBACK_QUERY_PARAM.equals(nvPair.getName())) {
                callback = nvPair.getValue();
            }
            if (APIConstants.Webhooks.HUB_SECRET_QUERY_PARAM.equals(nvPair.getName())) {
                secret = nvPair.getValue();
            }
            if (APIConstants.Webhooks.HUB_MODE_QUERY_PARAM.equals(nvPair.getName())) {
                mode = nvPair.getValue();
            }
            if (APIConstants.Webhooks.HUB_LEASE_SECONDS_QUERY_PARAM.equals(nvPair.getName())) {
                leaseSeconds = nvPair.getValue();
            }
        }
    }

    /**
     * This method is used to generate the request body for the API call.
     *
     * @param apiUUID           the API UUID to uniquely identify the API.
     * @param apiContext        the API context.
     * @param apiVersion        the API version.
     * @param applicationID     the application ID of the subscriber.
     * @param tenantDomain      the tenant domain.
     * @param tenantID          the tenant id.
     * @param authContext       the authentication context.
     * @return the generated body.
     */
    private String generateRequestBody(String apiUUID, String apiContext, String apiVersion, String applicationID,
                                       String tenantDomain, int tenantID, AuthenticationContext authContext) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put(APIConstants.Webhooks.API_UUID, apiUUID);
        node.put(APIConstants.Webhooks.API_CONTEXT, apiContext);
        node.put(APIConstants.Webhooks.API_VERSION, apiVersion);
        node.put(APIConstants.Webhooks.API_NAME, authContext.getApiName());
        node.put(APIConstants.Webhooks.APP_ID, applicationID);
        node.put(APIConstants.Webhooks.TENANT_DOMAIN, tenantDomain);
        node.put(APIConstants.Webhooks.TENANT_ID, tenantID);
        node.put(APIConstants.Webhooks.CALLBACK, callback);
        node.put(APIConstants.Webhooks.TOPIC, topicName);
        node.put(APIConstants.Webhooks.MODE, mode);
        node.put(APIConstants.Webhooks.SECRET, secret);
        node.put(APIConstants.Webhooks.LEASE_SECONDS, leaseSeconds);
        node.put(APIConstants.Webhooks.TIER, authContext.getTier());
        node.put(APIConstants.Webhooks.APPLICATION_TIER, authContext.getApplicationTier());
        node.put(APIConstants.Webhooks.API_TIER, authContext.getApiTier());
        node.put(APIConstants.Webhooks.SUBSCRIBER_NAME, authContext.getSubscriber());
        return node.toString();
    }
}
