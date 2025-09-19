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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.apimgt.throttle.policy.deployer.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This Class used to properly start and Close Throttle Policy JMS listeners
 */
public class ThrottlePolicyStartupListener implements ServerStartupObserver, ServerShutdownHandler, Runnable,
        JMSListenerShutDownService {

    private final Log log = LogFactory.getLog(ThrottlePolicyStartupListener.class);
    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public ThrottlePolicyStartupListener() {

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

        log.info("Server startup completed. Initiating throttle policy deployment and JMS subscription.");
        deployPoliciesInAsyncMode();
        jmsTransportHandlerForEventHub
                .subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_NOTIFICATION,
                        new ThrottlePolicyJMSMessageListener());
        log.info("Throttle policy startup process completed successfully.");
    }

    @Override
    public void invoke() {

        if (jmsTransportHandlerForEventHub != null) {
            log.info("Server shutdown initiated. Unsubscribing from JMS events.");
            if (log.isDebugEnabled()) {
                log.debug("Unsubscribing from JMS Events...");
            }
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            log.info("JMS event unsubscription completed.");
        } else {
            log.warn("JMS transport handler is null during shutdown.");
        }
    }

    @Override
    public void run() {

        log.info("Starting asynchronous deployment of all throttle policies.");
        try {
            PolicyUtil.deployAllPolicies();
            log.info("Asynchronous policy deployment completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during asynchronous policy deployment", e);
        }
    }

    private void deployPoliciesInAsyncMode() {

        if (log.isDebugEnabled()) {
            log.debug("Starting policy deployment in asynchronous mode");
        }
        new Thread(this).start();
    }

    @Override
    public void shutDownListener() {

        if (jmsTransportHandlerForEventHub != null) {
            log.info("Shutting down JMS listener. Unsubscribing from JMS events.");
            if (log.isDebugEnabled()) {
                log.debug("Unsubscribing from JMS Events...");
            }
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
            log.info("JMS listener shutdown completed.");
        } else {
            log.warn("JMS transport handler is null during listener shutdown.");
        }
    }
}
