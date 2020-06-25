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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This Class used to properly start and Close JMS listeners
 */
public class JMSListenerStartupShutdownListener implements ServerStartupObserver, ServerShutdownHandler {

    private Log log = LogFactory.getLog(JMSListenerStartupShutdownListener.class);
    private JMSTransportHandler jmsTransportHandlerForTrafficManager;
    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public JMSListenerStartupShutdownListener() {

        ThrottleProperties.JMSConnectionProperties jmsConnectionProperties =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration().getThrottleProperties()
                        .getJmsConnectionProperties();
        this.jmsTransportHandlerForTrafficManager =
                new JMSTransportHandler(jmsConnectionProperties.getJmsConnectionProperties());
        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration().getEventHubConfigurationDto()
                        .getEventHubReceiverConfiguration();
        if (eventHubReceiverConfiguration != null) {
            this.jmsTransportHandlerForEventHub =
                    new JMSTransportHandler(eventHubReceiverConfiguration.getJmsConnectionParameters());
        }

    }

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        jmsTransportHandlerForTrafficManager
                .subscribeForJmsEvents(JMSConstants.TOPIC_THROTTLE_DATA, new JMSMessageListener());
        jmsTransportHandlerForEventHub.subscribeForJmsEvents(JMSConstants.TOPIC_TOKEN_REVOCATION,
                new GatewayTokenRevocationMessageListener());
        jmsTransportHandlerForEventHub
                .subscribeForJmsEvents(JMSConstants.TOPIC_CACHE_INVALIDATION, new APIMgtGatewayCacheMessageListener());
        jmsTransportHandlerForEventHub
                .subscribeForJmsEvents(JMSConstants.TOPIC_KEY_MANAGER, new KeyManagerJMSMessageListener());
        jmsTransportHandlerForEventHub
                .subscribeForJmsEvents(JMSConstants.TOPIC_NOTIFICATION, new GatewayJMSMessageListener());
    }

    @Override
    public void invoke() {

        if (jmsTransportHandlerForTrafficManager != null) {
            // This method will make shutdown the Listener.
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForTrafficManager.unSubscribeFromEvents();
        }
        if (jmsTransportHandlerForEventHub != null) {
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
        }
    }
}
