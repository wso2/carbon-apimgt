/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.jms.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService;
import org.wso2.carbon.apimgt.jms.listener.utils.JMSThrottleDataRetriever;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;


/**
 * This components start the JMS listeners
 */

/**
 * @scr.component name="org.wso2.apimgt.jms.listener" immediate="true"
 * @scr.reference name="throttle.data.service"
 * interface="org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService"
 * cardinality="1..1" policy="dynamic" bind="setAPIThrottleDataService" unbind="unsetAPIThrottleDataService"
 * org.wso2.andes.wso2.service.QpidNotificationService
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */

public class JMSListenerComponent {

    private static final Log log = LogFactory.getLog(JMSListenerComponent.class);

    protected void activate(ComponentContext context) {
        log.debug("Activating component...");

        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();

        if (configuration != null) {
            if (configuration.getThrottleProperties().getJmsConnectionProperties().isEnabled()) {
                JMSThrottleDataRetriever jmsThrottleDataRetriever = new JMSThrottleDataRetriever();
                jmsThrottleDataRetriever.startJMSThrottleDataRetriever();
            }

        } else {
            log.warn("API Manager Configuration not properly set.");
        }

        return;
    }


    protected void setAPIThrottleDataService(APIThrottleDataService throttleDataService) {
        log.debug("Setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(throttleDataService);
    }

    protected void unsetAPIThrottleDataService(APIThrottleDataService throttleDataService) {
        log.debug("Un-setting APIThrottleDataService");
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(null);
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        log.debug("Setting APIM Configuration Service");
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }

    }
}
