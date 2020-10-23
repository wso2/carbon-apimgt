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
package org.wso2.carbon.apimgt.throttle.policy.deployer.utils;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.PolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.GlobalPolicyEvent;
import org.wso2.carbon.apimgt.throttle.policy.deployer.PolicyRetriever;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.JMSException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ThrottlePolicyJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(ThrottlePolicyJMSMessageListener.class);

    private PolicyRetriever policyRetriever = new PolicyRetriever();

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
                            if (log.isDebugEnabled()) {
                                log.debug("Event received from the topic of " + jmsDestination.getTopicName());
                            }
                            handleNotificationMessage((String) map.get(APIConstants.EVENT_TYPE),
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

    private void handleNotificationMessage(String eventType, String encodedEvent) {

        byte[] eventDecoded = Base64.decodeBase64(encodedEvent);
        String eventJson = new String(eventDecoded);

        if (APIConstants.EventType.POLICY_CREATE.toString().equals(eventType)
                || APIConstants.EventType.POLICY_UPDATE.toString().equals(eventType)
                || APIConstants.EventType.POLICY_DELETE.toString().equals(eventType)
        ) {

            boolean updatePolicy = APIConstants.EventType.POLICY_CREATE.toString().equals(eventType)
                    || APIConstants.EventType.POLICY_UPDATE.toString().equals(eventType);
            boolean deletePolicy = APIConstants.EventType.POLICY_DELETE.toString().equals(eventType);

            PolicyEvent event = new Gson().fromJson(eventJson, PolicyEvent.class);

            if (event.getPolicyType() == APIConstants.PolicyType.SUBSCRIPTION) {
                SubscriptionPolicyEvent policyEvent = new Gson().fromJson(eventJson, SubscriptionPolicyEvent.class);
                try {
                    if (updatePolicy) {
                        SubscriptionPolicy subscriptionPolicy = policyRetriever.getSubscriptionPolicy(
                                policyEvent.getPolicyName(), policyEvent.getTenantDomain());
                        PolicyUtil.deployPolicy(subscriptionPolicy, policyEvent);
                    } else if (deletePolicy) {
                        PolicyUtil.undeployPolicy(policyEvent);
                    }
                } catch (ThrottlePolicyDeployerException e) {
                    log.error("Error in retrieving subscription policy metadata from the database", e);
                }

            } else if (event.getPolicyType() == APIConstants.PolicyType.APPLICATION) {
                ApplicationPolicyEvent policyEvent = new Gson().fromJson(eventJson, ApplicationPolicyEvent.class);
                try {
                    if (updatePolicy) {
                        ApplicationPolicy applicationPolicy = policyRetriever.getApplicationPolicy(
                                policyEvent.getPolicyName(), policyEvent.getTenantDomain());
                        PolicyUtil.deployPolicy(applicationPolicy, policyEvent);
                    } else if (deletePolicy) {
                        PolicyUtil.undeployPolicy(policyEvent);
                    }
                } catch (ThrottlePolicyDeployerException e) {
                    log.error("Error in retrieving application policy metadata from the database", e);
                }

            } else if (event.getPolicyType() == APIConstants.PolicyType.API) {
                APIPolicyEvent policyEvent = new Gson().fromJson(eventJson, APIPolicyEvent.class);
                    try {
                        if (updatePolicy) {
                            ApiPolicy apiPolicy = policyRetriever.getApiPolicy(
                                    policyEvent.getPolicyName(), policyEvent.getTenantDomain());
                            PolicyUtil.deployPolicy(apiPolicy, policyEvent);
                        } else if (deletePolicy) {
                            PolicyUtil.undeployPolicy(policyEvent);
                        }
                    } catch (ThrottlePolicyDeployerException e) {
                        log.error("Error in retrieving API policy metadata from the database", e);
                    }

            } else if (event.getPolicyType() == APIConstants.PolicyType.GLOBAL) {
                GlobalPolicyEvent policyEvent = new Gson().fromJson(eventJson, GlobalPolicyEvent.class);
                    try {
                        if (updatePolicy) {
                            GlobalPolicy globalPolicy = policyRetriever.getGlobalPolicy(
                                    policyEvent.getPolicyName(), policyEvent.getTenantDomain());
                            PolicyUtil.deployPolicy(globalPolicy, policyEvent);
                        } else if (deletePolicy) {
                            PolicyUtil.undeployPolicy(policyEvent);
                        }
                    } catch (ThrottlePolicyDeployerException e) {
                        log.error("Error in retrieving Global policy metadata from the database", e);
                    }

            }
        }
    }
}

