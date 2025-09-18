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

package org.wso2.carbon.apimgt.cache.invalidation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.cache.invalidation.internal.DataHolder;
import org.wso2.carbon.apimgt.common.jms.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This class used to initialize and stop Message listening capability.
 */
public class APIMgtServerStartupListener implements ServerStartupObserver, ServerShutdownHandler,
        JMSListenerShutDownService {

    private static final Log log = LogFactory.getLog(APIMgtServerStartupListener.class);
    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public APIMgtServerStartupListener() {

        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                DataHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto().getEventHubReceiverConfiguration();
        ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties jmsTaskManagerProperties =
                DataHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getThrottleProperties().getJmsConnectionProperties().getJmsTaskManagerProperties();
        if (eventHubReceiverConfiguration != null) {
            this.jmsTransportHandlerForEventHub = new JMSTransportHandler(
                    eventHubReceiverConfiguration.getJmsConnectionParameters(), jmsTaskManagerProperties);
            if (log.isDebugEnabled()) {
                log.debug("JMS transport handler initialized for cache invalidation");
            }
        } else {
            log.warn("EventHub receiver configuration not available for cache invalidation");
        }
    }

    @Override
    public void completingServerStartup() {
       //Not doing anything since transport not start in this method.
    }

    @Override
    public void completedServerStartup() {

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            log.info("Starting cache invalidation JMS listener");
            jmsTransportHandlerForEventHub.subscribeForJmsEvents(CachingConstants.TOPIC_NAME,
                    new APIMgtCacheInvalidationListener(DataHolder.getInstance().getCacheInvalidationConfiguration()));
            DataHolder.getInstance().setStarted(true);
            log.info("Cache invalidation JMS listener started successfully");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache invalidation JMS listener not started - configuration enabled: " +
                          (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                           DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled()) +
                          ", transport handler available: " + (jmsTransportHandlerForEventHub != null));
            }
        }
    }

    @Override
    public void invoke() {

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            log.info("Stopping cache invalidation JMS listener due to server shutdown");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            DataHolder.getInstance().setStarted(false);
            log.info("Cache invalidation JMS listener stopped successfully");
        }
    }

    @Override
    public void shutDownListener() {

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            log.info("Shutting down cache invalidation JMS listener");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            DataHolder.getInstance().setStarted(false);
            log.info("Cache invalidation JMS listener shutdown completed");
        }
    }
}
