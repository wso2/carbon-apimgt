package org.wso2.carbon.apimgt.core.internal;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.securevault.SecureVault;

import java.util.Map;

/**
 * Class used to hold the APIM configuration
 */
public class ServiceReferenceHolder {
    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private APIMConfigurations config;
    private SecureVault secureVault;

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }


    /**
     * Sets the configProvider instance
     *
     * @param configProvider configProvider instance to set
     */
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Gives the APIMConfigurations explicitly set in the deployment yaml or the default configurations
     *
     * @return APIMConfigurations
     */
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

    /**
    * This method is to get configuration map of a given namespace
    *
    * @param namespace namespace defined in deployment.yaml
    * @return resource path to scope mapping
    */
    public Map<String, String> getRestAPIConfigurationMap(String namespace) {
        try {
            if (configProvider != null) {
                return configProvider.getConfigurationMap(namespace);
            } else {
                log.error("Configuration provider is null");
            }
        } catch (CarbonConfigurationException e) {
            log.error("Error while reading the configurations map of namespace : " +
                    "org.wso2.carbon.apimgt.core.internal.APIMConfiguration", e);
        }
        return null;
    }


    /**
     * Gives the secure vault instance if already set
     *
     * @return secureVault instance
     */
    public SecureVault getSecureVault() {
        return secureVault;
    }

    /**
     * Sets the secure vault instance
     *
     * @param secureVault secureVault instance to set
     */
    public void setSecureVault(SecureVault secureVault) {
        this.secureVault = secureVault;
    }
}
