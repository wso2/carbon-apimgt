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
package org.wso2.carbon.apimgt.jms.listener.utils;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.common.jms.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This Class used to properly start and Close JMS listeners.
 */
public class JMSListenerStartupShutdownListener implements ServerStartupObserver, ServerShutdownHandler,
        JMSListenerShutDownService {

    private Log log = LogFactory.getLog(JMSListenerStartupShutdownListener.class);
    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public JMSListenerStartupShutdownListener() {

        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration().getEventHubConfigurationDto()
                        .getEventHubReceiverConfiguration();
        ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties jmsTaskManagerProperties =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration().getThrottleProperties()
                        .getJmsConnectionProperties().getJmsTaskManagerProperties();

        if (eventHubReceiverConfiguration != null) {
            this.jmsTransportHandlerForEventHub = new JMSTransportHandler(
                    eventHubReceiverConfiguration.getJmsConnectionParameters(), jmsTaskManagerProperties);
        }

    }

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        log.info("JMS Listener startup process initiated");
        String migrationEnabled = System.getProperty(APIConstants.MIGRATE);
        if (migrationEnabled == null) {
            APIManagerConfiguration apimConfiguration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
            if (apimConfiguration != null) {
                String enableKeyManagerRetrieval =
                        apimConfiguration.getFirstProperty(APIConstants.ENABLE_KEY_MANAGER_RETRIVAL);
                if (JavaUtils.isTrueExplicitly(enableKeyManagerRetrieval)) {
                    log.info("Key manager retrieval enabled, subscribing to JMS topics");
                    jmsTransportHandlerForEventHub
                            .subscribeForJmsEvents(JMSConstants.TOPIC_KEY_MANAGER, new KeyManagerJMSMessageListener());
                    jmsTransportHandlerForEventHub
                            .subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_NOTIFICATION, 
                            new CorrelationConfigJMSMessageListener());
                    log.info("Successfully subscribed to JMS topics for key manager and correlation config events");
                } else {
                    log.info("Key manager retrieval disabled, skipping JMS topic subscription");
                }
            } else {
                log.warn("API Manager configuration not available, skipping JMS topic subscription");
            }
        } else {
            log.info("Running on migration enabled mode: Stopped at JMSListenerStartupShutdownListener completed");
        }

    }

    @Override
    public void invoke() {

        if (jmsTransportHandlerForEventHub != null) {
            if (log.isDebugEnabled()) {
                log.debug("Unsubscribing from JMS Events during server shutdown");
            }
            log.info("JMS Listener shutdown process initiated");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            log.info("Successfully unsubscribed from JMS events");
        }
    }

    @Override
    public void shutDownListener() {

        if (jmsTransportHandlerForEventHub != null) {
            if (log.isDebugEnabled()) {
                log.debug("Shutting down JMS Listener service");
            }
            log.info("JMS Listener service shutdown requested");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            log.info("JMS Listener service shutdown completed successfully");
        }
    }
}
