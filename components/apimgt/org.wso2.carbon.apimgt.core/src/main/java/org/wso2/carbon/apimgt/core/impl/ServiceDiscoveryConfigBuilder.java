/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

/**
 * This builder can be used to build {@link ServiceDiscoveryConfigurations}
 */
public class ServiceDiscoveryConfigBuilder {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryConfigBuilder.class);
    private static ServiceDiscoveryConfigurations serviceDiscoveryConfig;

    /**
     * Builds the {@link ServiceDiscoveryConfigurations} object
     *
     * @param configProvider  ConfigProvider instance
     */
    public static void build(ConfigProvider configProvider) {
        try {
            serviceDiscoveryConfig = configProvider.getConfigurationObject(ServiceDiscoveryConfigurations.class);
        } catch (ConfigurationException e) {
            log.error("Error while loading the configuration for Service discovery ", e);
            serviceDiscoveryConfig = new ServiceDiscoveryConfigurations();
        }
    }

    /**
     * Clears the {@link ServiceDiscoveryConfigurations} instance
     */
    public static void clearServiceDiscoveryConfig() {
        serviceDiscoveryConfig = null;
    }

    /**
     * Gives the {@link ServiceDiscoveryConfigurations} instance if already built
     *
     * @return ServiceDiscoveryConfigurations instance
     */
    public static ServiceDiscoveryConfigurations getServiceDiscoveryConfiguration() {
        return serviceDiscoveryConfig;
    }
}
