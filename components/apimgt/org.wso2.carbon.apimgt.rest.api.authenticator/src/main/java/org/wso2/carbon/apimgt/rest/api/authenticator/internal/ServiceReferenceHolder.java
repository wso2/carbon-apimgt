package org.wso2.carbon.apimgt.rest.api.authenticator.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

/**
 * Class used to hold the APIM configuration
 */

public class ServiceReferenceHolder {

    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private APIMConfigurations config = null;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public APIMConfigurations getAPIMConfiguration() {
        try {
            if (configProvider != null) {
                config = configProvider.getConfigurationObject(APIMConfigurations.class);
            } else {
                log.error("Configuration provider is null");
            }
        } catch (CarbonConfigurationException e) {
            log.error("error getting config : org.wso2.carbon.apimgt.core.internal.APIMConfiguration", e);
        }

        if (config == null) {
            config = new APIMConfigurations();
            log.info("Setting default configurations...");
        }

        return config;
    }
}