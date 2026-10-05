/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.throttling.siddhi.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

@Component(
        name = "org.wso2.carbon.apimgt.throttling.siddhi.extension.component",
        immediate = true)
public class APIMSiddhiExtensionsComponent {

    private static Log log = LogFactory.getLog(APIMSiddhiExtensionsComponent.class);

    private static final String PORT_OFFSET_SYSTEM_VAR = "portOffset";
    private static final String PORT_OFFSET_CONFIG = "Ports.Offset";
    private static final String JMS_PORT = "jms.port";

    @Activate
    protected void activate(ComponentContext ctxt) {
        if (System.getProperty(JMS_PORT) == null) {
            int jmsPort = 5672 + getPortOffset();
            System.setProperty(JMS_PORT, String.valueOf(jmsPort));
        }
    }

    private static int getPortOffset() {
        ServerConfiguration carbonConfig = CarbonUtils.getServerConfiguration();
        String portOffset = System.getProperty(PORT_OFFSET_SYSTEM_VAR,
                carbonConfig.getFirstProperty(PORT_OFFSET_CONFIG));
        try {
            if ((portOffset != null)) {
                return Integer.parseInt(portOffset.trim());
            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid Port Offset: " + portOffset + ". Default value 0 will be used.", e);
            return 0;
        }
    }
    /**
     * Sets the API Manager Configuration Service. This method is called  when the API Manager Configuration Service
     * becomes available.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
    }

    /**
     * Unsets the API Manager Configuration Service.
     *
     * @param configurationService The API Manager Configuration Service instance.
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService configurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIM Configuration Service");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}

