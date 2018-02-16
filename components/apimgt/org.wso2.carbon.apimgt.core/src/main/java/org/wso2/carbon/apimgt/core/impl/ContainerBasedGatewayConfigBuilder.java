/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.ContainerBasedGatewayConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * This builder can be used to build {@link
 * org.wso2.carbon.apimgt.core.configuration.models.ContainerBasedGatewayConfiguration}
 */
public class ContainerBasedGatewayConfigBuilder {
    private static final Logger log = LoggerFactory.getLogger(ContainerBasedGatewayConfigBuilder.class);
    private static ContainerBasedGatewayConfiguration containerBasedGatewayConfig;

    /**
     * Builds the {@link ContainerBasedGatewayConfiguration} object
     *
     * @param configProvider ConfigProvider instance
     */
    public static void build(ConfigProvider configProvider) {
        try {
            containerBasedGatewayConfig = configProvider.getConfigurationObject(
                    ContainerBasedGatewayConfiguration.class);
        } catch (ConfigurationException e) {
            log.error("Error while loading the configuration for container based gateway ", e);
            containerBasedGatewayConfig = new ContainerBasedGatewayConfiguration();
        }
    }

    /**
     * Clears the {@link ContainerBasedGatewayConfiguration} instance
     */
    public static void clearContainerBasedGatewayConfig() {
        containerBasedGatewayConfig = null;
    }

    /**
     * Gives the {@link ContainerBasedGatewayConfiguration} instance if already built
     *
     * @return ServiceDiscoveryConfigurations instance
     */
    public static ContainerBasedGatewayConfiguration getContainerBasedGatewayConfiguration() {
        return containerBasedGatewayConfig;
    }
}
