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

import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.WebhooksUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

/**
 * This mediator would load the subscriber's information from the subscribers list according to the index of the list.
 */
public class SubscriberInfoLoader extends AbstractMediator {

    //private final GenericRequestDataCollector dataCollector = null;

    @Override
    public boolean mediate(MessageContext messageContext) {
        List<WebhooksDTO> subscribersList = (List<WebhooksDTO>) messageContext.
                getProperty(APIConstants.Webhooks.SUBSCRIBERS_LIST_PROPERTY);
        int index = (Integer) messageContext.getProperty(APIConstants.CLONED_ITERATION_INDEX_PROPERTY);
        WebhooksDTO subscriber = subscribersList.get(index - 1);
        if (subscriber != null) {
            if (subscriber.isThrottled()) {
                return false;
            }
            if (doThrottle(subscriber, messageContext)) {
                messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_CALLBACK_PROPERTY,
                        subscriber.getCallbackURL());
                String errorMessage = "Message throttled out";
                String errorDescription = "You have exceeded your quota";
                int errorCode = APIThrottleConstants.EVENTS_COUNT_THROTTLE_OUT_ERROR_CODE;
                int httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
                messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
                messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
                messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDescription);
                messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, httpErrorCode);
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
                    Utils.setFaultPayload(messageContext, WebhooksUtils.getFaultPayload(errorCode, errorMessage, errorDescription));
                }
            }
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_CALLBACK_PROPERTY, subscriber.getCallbackURL());
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_SECRET_PROPERTY, subscriber.getSecret());
            messageContext.setProperty(APIConstants.Webhooks.SUBSCRIBER_APPLICATION_ID_PROPERTY, subscriber.getAppID());
        }
        return true;
    }

    private boolean doThrottle(WebhooksDTO subscriber, MessageContext messageContext) {
        String applicationLevelTier = subscriber.getApplicationTier();
        String apiLevelTier = subscriber.getApiTier();
        String subscriptionLevelTier = subscriber.getTier();
        String resourceLevelTier = apiLevelTier;
        String apiVersion = subscriber.getApiVersion();
        String apiContext = subscriber.getApiContext();
        String appTenant = subscriber.getTenantDomain();
        String apiTenant = subscriber.getTenantDomain();
        String appId = subscriber.getAppID();
        String authorizedUser;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(subscriber.getTenantDomain())) {
            authorizedUser = subscriber.getSubscriberName() + "@" + subscriber.getTenantDomain();
        } else {
            authorizedUser = subscriber.getSubscriberName();
        }
        String applicationLevelThrottleKey = appId + ":" + authorizedUser;
        String apiLevelThrottleKey = apiContext + ":" + apiVersion;
        String resourceLevelThrottleKey = apiLevelThrottleKey;
        String subscriptionLevelThrottleKey = appId + ":" + apiContext + ":" + apiVersion;
        AuthenticationContext authContext = new AuthenticationContext();
        boolean isThrottled = WebhooksUtils.isThrottled(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
                applicationLevelThrottleKey);
        if (isThrottled) {
            if (APIUtil.isAnalyticsEnabled()) {
                //dataCollector.collectData();
            }
            subscriber.setThrottled(true);
            return true;
        }
        ServiceReferenceHolder.getInstance().getThrottleDataPublisher().
                publishNonThrottledEvent(applicationLevelThrottleKey,
                        applicationLevelTier, apiLevelThrottleKey, apiLevelTier,
                        subscriptionLevelThrottleKey, subscriptionLevelTier,
                        resourceLevelThrottleKey, resourceLevelTier,
                        authorizedUser, apiContext,
                        apiVersion, appTenant, apiTenant,
                        appId,
                        messageContext, authContext);
        //dataCollector.collectData();
        return false;
    }

}
