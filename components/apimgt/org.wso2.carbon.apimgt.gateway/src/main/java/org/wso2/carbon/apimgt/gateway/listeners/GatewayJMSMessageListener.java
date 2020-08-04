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

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.EventType;
import org.wso2.carbon.apimgt.impl.APIConstants.PolicyType;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.PolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class GatewayJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayJMSMessageListener.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
            .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    private final ScheduledExecutorService artifactRetrievalScheduler = Executors.newScheduledThreadPool( 10,
            new ArtifactsRetrieverThreadFactory());

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (APIConstants.TopicNames.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.EVENT_TYPE) != null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            if (debugEnabled) {
                                log.debug("Event received from the topic of " + jmsDestination.getTopicName());
                            }
                            handleNotificationMessage((String) map.get(APIConstants.EVENT_TYPE),
                                    (Long) map.get(APIConstants.EVENT_TIMESTAMP),
                                    (String) map.get(APIConstants.EVENT_PAYLOAD));
                        }
                    }

                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    private void handleNotificationMessage(String eventType, long timestamp, String encodedEvent) {

        byte[] eventDecoded = Base64.decodeBase64(encodedEvent);
        String eventJson = new String(eventDecoded);

        if ((APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)
                || APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType))
                && gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            DeployAPIInGatewayEvent gatewayEvent = new Gson().fromJson(new String(eventDecoded), DeployAPIInGatewayEvent.class);
            gatewayEvent.getGatewayLabels().retainAll(gatewayArtifactSynchronizerProperties.getGatewayLabels());
            if (!gatewayEvent.getGatewayLabels().isEmpty()) {
                String gatewayLabel = gatewayEvent.getGatewayLabels().iterator().next();
                String tenantDomain = gatewayEvent.getTenantDomain();
                Runnable task = null;
                if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)) {
                    task = new Runnable() {

                        @Override public void run() {
                            boolean tenantFlowStarted = false;
                            try {
                                startTenantFlow(tenantDomain);
                                tenantFlowStarted = true;
                                inMemoryApiDeployer.deployAPI(gatewayEvent.getApiId(), gatewayLabel);
                            } catch (ArtifactSynchronizerException e) {
                                log.error("Error in deploying artifacts");
                            } finally {
                                if (tenantFlowStarted){
                                    endTenantFlow();
                                }
                            }
                        }
                    };
                } else if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType)) {
                    task = new Runnable() {

                        @Override public void run() {
                            boolean tenantFlowStarted = false;
                            try {
                                startTenantFlow(tenantDomain);
                                tenantFlowStarted = true;
                                inMemoryApiDeployer.unDeployAPI(gatewayEvent.getApiId(), gatewayLabel);
                            } catch (ArtifactSynchronizerException e) {
                                log.error("Error in undeploying artifacts");
                            } finally {
                                if (tenantFlowStarted){
                                    endTenantFlow();
                                }
                            }
                        }
                    };
                }
                artifactRetrievalScheduler.schedule(task, 1, TimeUnit.MILLISECONDS);
                if (debugEnabled) {
                    log.debug("Event with ID " + gatewayEvent.getEventId() + " is received and " +
                            gatewayEvent.getApiId() + " is successfully deployed/undeployed");
                }
            }
        }
        if (EventType.APPLICATION_CREATE.toString().equals(eventType)
                || EventType.APPLICATION_UPDATE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplication(event);;
        } else if (EventType.SUBSCRIPTIONS_CREATE.toString().equals(eventType)
                || EventType.SUBSCRIPTIONS_UPDATE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateSubscription(event);
        } else if (EventType.API_UPDATE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
        } else if (EventType.API_LIFECYCLE_CHANGE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            if (APIStatus.CREATED.toString().equals(event.getApiStatus())
                    || APIStatus.RETIRED.toString().equals(event.getApiStatus())) {
                ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeAPI(event);
            } else {
                ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
            }
        } else if (EventType.APPLICATION_REGISTRATION_CREATE.toString().equals(eventType)) {
            ApplicationRegistrationEvent event = new Gson().fromJson(eventJson, ApplicationRegistrationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplicationKeyMapping(event);
        } else if (EventType.API_DELETE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeAPI(event);
        } else if (EventType.SUBSCRIPTIONS_DELETE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeSubscription(event);
        } else if (EventType.APPLICATION_DELETE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
            ServiceReferenceHolder.getInstance().getKeyManagerDataService().removeApplication(event);
        } else {
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
}
