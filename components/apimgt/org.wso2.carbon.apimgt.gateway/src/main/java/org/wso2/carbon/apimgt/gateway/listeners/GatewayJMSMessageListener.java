/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.gateway.APILoggerManager;
import org.wso2.carbon.apimgt.gateway.EndpointCertificateDeployer;
import org.wso2.carbon.apimgt.gateway.GoogleAnalyticsConfigDeployer;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.EventType;
import org.wso2.carbon.apimgt.impl.APIConstants.PolicyType;
import org.wso2.carbon.apimgt.impl.certificatemgt.CertificateManagerImpl;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.CertificateEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.GoogleAnalyticsConfigEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.PolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ScopeEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.KeyTemplateEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class GatewayJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayJMSMessageListener.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
    private EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration().getEventHubConfigurationDto();
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
            .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "DeploymentThread"));

    public void onMessage(Message message) {

        try {
            if (eventHubConfigurationDto.hasEventWaitingTime()) {
                long timeLeft = message.getJMSTimestamp() + eventHubConfigurationDto.getEventWaitingTime()
                        - System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Event Hub waiting time: " + timeLeft);
                }
                if (timeLeft > 0) {
                    Thread.sleep(timeLeft);
                }
            }
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData =  new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);

                    if (APIConstants.TopicNames.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.EVENT_TYPE).asText() != null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            if (debugEnabled) {
                                log.debug("Event received from the topic of " + jmsDestination.getTopicName());
                            }
                            handleNotificationMessage(payloadData.get(APIConstants.EVENT_TYPE).asText(),
                                    payloadData.get(APIConstants.EVENT_TIMESTAMP).asLong(),
                                    payloadData.get(APIConstants.EVENT_PAYLOAD).asText());
                        }
                    } else if (APIConstants.TopicNames.TOPIC_ASYNC_WEBHOOKS_DATA.equalsIgnoreCase
                            (jmsDestination.getTopicName())) {
                        String mode = payloadData.get(APIConstants.Webhooks.MODE).asText();
                        if (APIConstants.Webhooks.SUBSCRIBE_MODE.equalsIgnoreCase(mode)) {
                            handleAsyncWebhooksSubscriptionMessage(payloadData);
                        } else if (APIConstants.Webhooks.UNSUBSCRIBE_MODE.equalsIgnoreCase(mode)) {
                            handleAsyncWebhooksUnSubscriptionMessage(payloadData);
                        }
                    }

                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (InterruptedException e) {
            log.error("Error occurred while waiting to retrieve artifacts from event hub", e);
        }
    }

    private void handleNotificationMessage(String eventType, long timestamp, String encodedEvent) {

        byte[] eventDecoded = Base64.decodeBase64(encodedEvent);
        String eventJson = new String(eventDecoded);

        if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)
                || APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType)) {
            executor.submit(new Runnable() {
                @Override
                public void run() {

                    DeployAPIInGatewayEvent gatewayEvent = new Gson().fromJson(new String(eventDecoded),
                            DeployAPIInGatewayEvent.class);
                    String tenantDomain = gatewayEvent.getTenantDomain();
                    boolean tenantLoaded = ServiceReferenceHolder.getInstance().isTenantLoaded(tenantDomain);
                    if (!tenantLoaded) {
                        String syncKey = tenantDomain.concat("__").concat(this.getClass().getName());
                        synchronized (syncKey.intern()) {
                            tenantLoaded = ServiceReferenceHolder.getInstance().isTenantLoaded(tenantDomain);
                            if (!tenantLoaded) {
                                APIUtil.loadTenantConfigBlockingMode(tenantDomain);
                            }
                        }
                    }

                    if (tenantLoaded) {
                        Set<String> systemConfiguredGatewayLabels = new HashSet(gatewayEvent.getGatewayLabels());
                        systemConfiguredGatewayLabels.retainAll(gatewayArtifactSynchronizerProperties.getGatewayLabels());
                        if (!systemConfiguredGatewayLabels.isEmpty()) {
                            ServiceReferenceHolder.getInstance().getKeyManagerDataService().updateDeployedAPIRevision(gatewayEvent);
                            if (EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)) {
                                boolean tenantFlowStarted = false;
                                try {
                                    startTenantFlow(tenantDomain);
                                    tenantFlowStarted = true;
                                    inMemoryApiDeployer.deployAPI(gatewayEvent);
                                } catch (ArtifactSynchronizerException e) {
                                    log.error("Error in deploying artifacts for " + gatewayEvent.getUuid() +
                                            "in the Gateway");
                                } finally {
                                    if (tenantFlowStarted) {
                                        endTenantFlow();
                                    }
                                }
                            }
                            if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType)) {
                                boolean tenantFlowStarted = false;
                                try {
                                    startTenantFlow(tenantDomain);
                                    tenantFlowStarted = true;
                                    inMemoryApiDeployer.unDeployAPI(gatewayEvent);
                                } catch (ArtifactSynchronizerException e) {
                                    log.error("Error in undeploying artifacts");
                                } finally {
                                    if (tenantFlowStarted) {
                                        endTenantFlow();
                                    }
                                }
                                DataHolder.getInstance().removeAPIFromAllTenantMap(gatewayEvent.getContext(),
                                        gatewayEvent.getTenantDomain());
                            }
                        }

                        if (debugEnabled) {
                            log.debug("Event with ID " + gatewayEvent.getEventId() + " is received and " +
                                    gatewayEvent.getUuid() + " is successfully deployed/undeployed");
                        }
                    }
                }
            });
        }
        if (EventType.APPLICATION_CREATE.toString().equals(eventType)
                || EventType.APPLICATION_UPDATE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplication(event);
        } else if (EventType.SUBSCRIPTIONS_CREATE.toString().equals(eventType)
                || EventType.SUBSCRIPTIONS_UPDATE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateSubscription(event);
        } else if (EventType.API_UPDATE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
            DataHolder.getInstance().addAPIMetaData(event);
        } else if (EventType.API_LIFECYCLE_CHANGE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            if (APIStatus.RETIRED.toString().equals(event.getApiStatus())) {
                ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeAPI(event);
                DataHolder.getInstance().removeAPIFromAllTenantMap(event.getApiContext(), event.getTenantDomain());
            } else {
                ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
            }
        } else if (EventType.APPLICATION_REGISTRATION_CREATE.toString().equals(eventType)) {
            ApplicationRegistrationEvent event = new Gson().fromJson(eventJson, ApplicationRegistrationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplicationKeyMapping(event);
        } else if (EventType.SUBSCRIPTIONS_DELETE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeSubscription(event);
        } else if (EventType.APPLICATION_DELETE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeApplication(event);
        } else if (EventType.REMOVE_APPLICATION_KEYMAPPING.toString().equals(eventType)) {
            ApplicationRegistrationEvent event = new Gson().fromJson(eventJson, ApplicationRegistrationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeApplicationKeyMapping(event);
        } else if (EventType.SCOPE_CREATE.toString().equals(eventType)) {
            ScopeEvent event = new Gson().fromJson(eventJson, ScopeEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addScope(event);
        } else if (EventType.SCOPE_UPDATE.toString().equals(eventType)) {
            ScopeEvent event = new Gson().fromJson(eventJson, ScopeEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addScope(event);
        } else if (EventType.SCOPE_DELETE.toString().equals(eventType)) {
            ScopeEvent event = new Gson().fromJson(eventJson, ScopeEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().deleteScope(event);
        } else if (EventType.POLICY_CREATE.toString().equals(eventType) ||
                EventType.POLICY_DELETE.toString().equals(eventType) ||
                EventType.POLICY_UPDATE.toString().equals(eventType)) {
            PolicyEvent event = new Gson().fromJson(eventJson, PolicyEvent.class);
            boolean updatePolicy = false;
            boolean deletePolicy = false;
            if (EventType.POLICY_CREATE.toString().equals(eventType)
                    || EventType.POLICY_UPDATE.toString().equals(eventType)) {
                updatePolicy = true;
            } else if (EventType.POLICY_DELETE.toString().equals(eventType)) {
                deletePolicy = true;
            }
            if (event.getPolicyType() == PolicyType.API) {
                APIPolicyEvent policyEvent = new Gson().fromJson(eventJson, APIPolicyEvent.class);
                if (updatePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .addOrUpdateAPIPolicy(policyEvent);
                } else if (deletePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .removeAPIPolicy(policyEvent);
                }
            } else if (event.getPolicyType() == PolicyType.SUBSCRIPTION) {
                SubscriptionPolicyEvent policyEvent = new Gson().fromJson(eventJson, SubscriptionPolicyEvent.class);
                if (updatePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .addOrUpdateSubscriptionPolicy(policyEvent);
                } else if (deletePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .removeSubscriptionPolicy(policyEvent);
                }
            } else if (event.getPolicyType() == PolicyType.APPLICATION) {
                ApplicationPolicyEvent policyEvent = new Gson().fromJson(eventJson, ApplicationPolicyEvent.class);
                if (updatePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .addOrUpdateApplicationPolicy(policyEvent);
                } else if (deletePolicy) {
                    ServiceReferenceHolder.getInstance().getKeyManagerDataService()
                            .removeApplicationPolicy(policyEvent);
                }
            }
        } else if (EventType.ENDPOINT_CERTIFICATE_ADD.toString().equals(eventType) ||
                EventType.ENDPOINT_CERTIFICATE_REMOVE.toString().equals(eventType)) {
            CertificateEvent certificateEvent = new Gson().fromJson(eventJson, CertificateEvent.class);
            if (EventType.ENDPOINT_CERTIFICATE_ADD.toString().equals(eventType)) {
                try {
                    new EndpointCertificateDeployer(certificateEvent.getTenantDomain())
                            .deployCertificate(certificateEvent.getAlias());
                } catch (APIManagementException e) {
                    log.error(e);
                }
            } else if (EventType.ENDPOINT_CERTIFICATE_REMOVE.toString().equals(eventType)) {
                boolean tenantFlowStarted = false;
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(certificateEvent.getTenantDomain(), true);
                    tenantFlowStarted = true;
                    CertificateManagerImpl.getInstance().deleteCertificateFromGateway(certificateEvent.getAlias());
                } finally {
                    if (tenantFlowStarted) {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        } else if (EventType.GA_CONFIG_UPDATE.toString().equals(eventType)) {
            GoogleAnalyticsConfigEvent googleAnalyticsConfigEvent =
                    new Gson().fromJson(eventJson, GoogleAnalyticsConfigEvent.class);
            try {
                new GoogleAnalyticsConfigDeployer(googleAnalyticsConfigEvent.getTenantDomain()).deploy();
            } catch (APIManagementException e) {
                log.error(e);
            }
        } else if (EventType.UDATE_API_LOG_LEVEL.toString().equals(eventType)) {
            APIEvent apiEvent = new Gson().fromJson(eventJson, APIEvent.class);
            APILoggerManager.getInstance().updateLoggerMap(apiEvent.getApiContext(), apiEvent.getLogLevel());
        } else if (EventType.CUSTOM_POLICY_ADD.toString().equals(eventType)) {
            KeyTemplateEvent keyTemplateEvent = new Gson().fromJson(eventJson, KeyTemplateEvent.class);
            String key = keyTemplateEvent.getKeyTemplate();
            String keyTemplateValue = keyTemplateEvent.getKeyTemplate();
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                    .addKeyTemplate(key, keyTemplateValue);
        } else if (EventType.CUSTOM_POLICY_DELETE.toString().equals(eventType)) {
            KeyTemplateEvent keyTemplateEvent = new Gson().fromJson(eventJson, KeyTemplateEvent.class);
            String key = keyTemplateEvent.getKeyTemplate();
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                    .removeKeyTemplate(key);
        } else if (EventType.CUSTOM_POLICY_UPDATE.toString().equals(eventType)) {
            KeyTemplateEvent keyTemplateEvent = new Gson().fromJson(eventJson, KeyTemplateEvent.class);
            String oldKey = keyTemplateEvent.getOldKeyTemplate();
            String newKey = keyTemplateEvent.getNewKeyTemplate();
            String newTemplateValue = newKey;
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                    .removeKeyTemplate(oldKey);
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                    .addKeyTemplate(newKey, newTemplateValue);
        }
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                setTenantDomain(tenantDomain, true);
    }

    private synchronized void handleAsyncWebhooksSubscriptionMessage(JsonNode payloadData) {
        if (log.isDebugEnabled()) {
            log.debug("Received event for -  Async Webhooks API subscription for : " + payloadData.
                    get(APIConstants.Webhooks.API_UUID).asText());
        }
        String apiUUID = payloadData.get(APIConstants.Webhooks.API_UUID).textValue();
        String appID = payloadData.get(APIConstants.Webhooks.APP_ID).textValue();
        String tenantDomain = payloadData.get(APIConstants.Webhooks.TENANT_DOMAIN).textValue();
        boolean isThrottled = payloadData.get(APIConstants.Webhooks.IS_THROTTLED).asBoolean();
        ServiceReferenceHolder.getInstance().getSubscriptionsDataService().updateThrottleStatus(appID, apiUUID,
                tenantDomain, isThrottled);
        if (!isThrottled) {
            String topicName = payloadData.get(APIConstants.Webhooks.TOPIC).textValue();
            WebhooksDTO subscriber = new WebhooksDTO();
            subscriber.setApiUUID(apiUUID);
            subscriber.setApiContext(payloadData.get(APIConstants.Webhooks.API_CONTEXT).textValue());
            subscriber.setApiName(payloadData.get(APIConstants.Webhooks.API_NAME).textValue());
            subscriber.setApiVersion(payloadData.get(APIConstants.Webhooks.API_VERSION).textValue());
            subscriber.setAppID(appID);
            subscriber.setCallbackURL(payloadData.get(APIConstants.Webhooks.CALLBACK).textValue());
            subscriber.setTenantDomain(tenantDomain);
            subscriber.setTenantId(payloadData.get(APIConstants.Webhooks.TENANT_ID).intValue());
            subscriber.setSecret(payloadData.get(APIConstants.Webhooks.SECRET).textValue());
            subscriber.setExpiryTime(payloadData.get(APIConstants.Webhooks.EXPIRY_AT).asLong());
            subscriber.setTopicName(topicName);
            subscriber.setApiTier(payloadData.get(APIConstants.Webhooks.API_TIER).textValue());
            subscriber.setApplicationTier(payloadData.get(APIConstants.Webhooks.APPLICATION_TIER).textValue());
            subscriber.setTier(payloadData.get(APIConstants.Webhooks.TIER).textValue());
            subscriber.setSubscriberName(payloadData.get(APIConstants.Webhooks.SUBSCRIBER_NAME).textValue());
            ServiceReferenceHolder.getInstance().getSubscriptionsDataService()
                    .addSubscription(apiUUID, topicName, tenantDomain, subscriber);
        }
    }

    private synchronized void handleAsyncWebhooksUnSubscriptionMessage(JsonNode payloadData) {
        if (log.isDebugEnabled()) {
            log.debug("Received event for -  Async Webhooks API unsubscription for : " + payloadData.
                    get(APIConstants.Webhooks.API_UUID).asText());
        }
        String apiKey = payloadData.get(APIConstants.Webhooks.API_UUID).textValue();
        String tenantDomain = payloadData.get(APIConstants.Webhooks.TENANT_DOMAIN).textValue();
        String topicName = payloadData.get(APIConstants.Webhooks.TOPIC).textValue();
        WebhooksDTO subscriber = new WebhooksDTO();
        subscriber.setCallbackURL(payloadData.get(APIConstants.Webhooks.CALLBACK).textValue());
        subscriber.setAppID(payloadData.get(APIConstants.Webhooks.APP_ID).textValue());
        subscriber.setSecret(payloadData.get(APIConstants.Webhooks.SECRET).textValue());
        ServiceReferenceHolder.getInstance().getSubscriptionsDataService()
                .removeSubscription(apiKey, topicName, tenantDomain, subscriber);
    }
}
