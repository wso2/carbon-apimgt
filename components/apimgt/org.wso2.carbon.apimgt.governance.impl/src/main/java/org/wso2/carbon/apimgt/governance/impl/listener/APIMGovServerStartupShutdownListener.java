/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSTransportHandler;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This listener is triggered at server startup
 */
public class APIMGovServerStartupShutdownListener implements ServerStartupObserver, ServerShutdownHandler,
        JMSListenerShutDownService {
    private static final Log log = LogFactory.getLog(APIMGovServerStartupShutdownListener.class);
    private JMSTransportHandler jmsTransportHandlerForEventHub;

    public APIMGovServerStartupShutdownListener() {

        EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                ServiceReferenceHolder.getInstance().getAPIMConfigurationService().getAPIManagerConfiguration()
                        .getEventHubConfigurationDto().getEventHubReceiverConfiguration();
        ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties jmsTaskManagerProperties =
                ServiceReferenceHolder.getInstance().getAPIMConfigurationService().getAPIManagerConfiguration()
                        .getThrottleProperties().getJmsConnectionProperties().getJmsTaskManagerProperties();
        if (eventHubReceiverConfiguration != null) {
            this.jmsTransportHandlerForEventHub = new JMSTransportHandler(
                    eventHubReceiverConfiguration.getJmsConnectionParameters(), jmsTaskManagerProperties);
        }
    }

    @Override
    public void completedServerStartup() {
        String migrationEnabled = System.getProperty(APIMGovernanceConstants.MIGRATE);
        if (migrationEnabled == null) {
            APIManagerConfiguration apimConfigService =
                    ServiceReferenceHolder.getInstance().getAPIMConfigurationService()
                            .getAPIManagerConfiguration();
            if (apimConfigService != null) {
                APIMGovernanceUtil.loadDefaultRulesets(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIMGovernanceUtil.loadDefaultPolicies(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                jmsTransportHandlerForEventHub
                        .subscribeForJmsEvents(APIConstants.TopicNames.TOPIC_NOTIFICATION,
                                new APIMGovernanceMessageListener());
            }
        }
    }

    @Override
    public void invoke() {
        if (jmsTransportHandlerForEventHub != null) {
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
        }
    }

    @Override
    public void shutDownListener() {
        if (jmsTransportHandlerForEventHub != null) {
            log.debug("Unsubscribe from JMS Events...");
            jmsTransportHandlerForEventHub.unSubscribeFromEvents();
        }
    }


    @Override
    public void completingServerStartup() {
    }
}
