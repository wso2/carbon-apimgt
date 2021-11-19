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

import org.wso2.carbon.apimgt.cache.invalidation.internal.DataHolder;
import org.wso2.carbon.apimgt.common.jms.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This class used to initialize and stop Message listening capability.
 */
public class APIMgtServerStartupListener implements ServerStartupObserver, ServerShutdownHandler,
        JMSListenerShutDownService {

    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public APIMgtServerStartupListener() {

        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                DataHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto().getEventHubReceiverConfiguration();
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

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            jmsTransportHandlerForEventHub.subscribeForJmsEvents(CachingConstants.TOPIC_NAME,
                    new APIMgtCacheInvalidationListener(DataHolder.getInstance().getCacheInvalidationConfiguration()));
            DataHolder.getInstance().setStarted(true);
        }
    }

    @Override
    public void invoke() {

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            DataHolder.getInstance().setStarted(false);
        }
    }

    @Override
    public void shutDownListener() {

        if (DataHolder.getInstance().getCacheInvalidationConfiguration() != null &&
                DataHolder.getInstance().getCacheInvalidationConfiguration().isEnabled() &&
                jmsTransportHandlerForEventHub != null) {
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            DataHolder.getInstance().setStarted(false);
        }
    }
}
