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
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.impl.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.AsyncAnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook.WebhooksAnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.utils.WebhooksUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;

import static org.wso2.carbon.apimgt.impl.APIConstants.AsyncApi.ASYNC_MESSAGE_TYPE;

/**
 * This mediator would persist delivery status of the callback urls of the subscriptions.
 */
public class DeliveryStatusUpdater extends AbstractMediator {

    private static final int deliveryDataPersisRetries = 15;

    @Override
    public boolean mediate(MessageContext messageContext) {
        try {
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext();
            int status = 2;
            Object statusCode = axis2MessageContext.getProperty(APIMgtGatewayConstants.HTTP_SC);
            messageContext.setProperty(Constants.BACKEND_RESPONSE_CODE, statusCode);
            if (statusCode != null) {
                String responseStatus = statusCode.toString();
                if (responseStatus.startsWith("2")) {
                    //handle 2XX response
                    status = 1;
                }
            }
            String topicName = (String) messageContext.getProperty(APIConstants.Webhooks.SUBSCRIBER_TOPIC_PROPERTY);
            String callback = (String) messageContext.getProperty(APIConstants.Webhooks.SUBSCRIBER_CALLBACK_PROPERTY);
            String tenantDomain = (String) messageContext.getProperty(APIConstants.TENANT_DOMAIN_INFO_PROPERTY);
            if (tenantDomain == null) {
                tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            }
            String apiKey = WebhooksUtils.generateAPIKey(messageContext, tenantDomain);
            String applicationID = (String) messageContext.getProperty(APIConstants.Webhooks.
                    SUBSCRIBER_APPLICATION_ID_PROPERTY);
            String requestBody = generateRequestBody(apiKey, applicationID, tenantDomain, callback, topicName, status);
            boolean isSubscribeRequest = messageContext.getProperty(ASYNC_MESSAGE_TYPE) != null;
            if (APIUtil.isAnalyticsEnabled() && !isSubscribeRequest) {
                WebhooksUtils.publishAnalyticsData(messageContext);
            }
            WebhooksUtils.persistData(requestBody, deliveryDataPersisRetries, APIConstants.Webhooks.DELIVERY_EVENT_TYPE);
        } catch (InterruptedException | IOException e) {
            log.error("Error while persisting delivery status", e);
        }
        return true;
    }

    /**
     * This method is used to generate the request body for the API call.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param applicationID     the application ID of the subscriber.
     * @param tenantDomain      the tenant domain.
     * @param callback          the subscriber's callback url.
     * @param topicName         the subscriber's topic name.
     * @param status            the status of the message delivery to the callback url. 1 if success: 0 if failed.
     * @return the generated body.
     */
    private String generateRequestBody(String apiKey, String applicationID, String tenantDomain, String callback,
                                       String topicName, int status) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put(APIConstants.Webhooks.API_UUID, apiKey);
        node.put(APIConstants.Webhooks.APP_ID,applicationID);
        node.put(APIConstants.Webhooks.TENANT_DOMAIN, tenantDomain);
        node.put(APIConstants.Webhooks.CALLBACK, callback);
        node.put(APIConstants.Webhooks.TOPIC, topicName);
        node.put(APIConstants.Webhooks.STATUS, status);
        return node.toString();
    }

}
