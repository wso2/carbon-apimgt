package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

/**
 * This builder can be used to build {@link ServiceDiscoveryConfigurations}
 */
public class ServiceDiscoveryConfigBuilder {
    private static final Logger log = LoggerFactory.getLogger(ServiceDiscoveryConfigBuilder.class);

    private static ServiceDiscoveryConfigurations serviceDiscoveryConfig;

    public static ServiceDiscoveryConfigurations getServiceDiscoveryConfiguration() {
        return serviceDiscoveryConfig;
    }

    public static void clearServiceDiscoveryConfig() {
        serviceDiscoveryConfig = null;
    }
    public static void build(ConfigProvider configProvider) {
        try {
            serviceDiscoveryConfig = configProvider.getConfigurationObject(ServiceDiscoveryConfigurations.class);
        } catch (CarbonConfigurationException e) {
            log.error("Error while loading the configuration for Service discovery ", e);
            serviceDiscoveryConfig = new ServiceDiscoveryConfigurations();
        }
    }
}
