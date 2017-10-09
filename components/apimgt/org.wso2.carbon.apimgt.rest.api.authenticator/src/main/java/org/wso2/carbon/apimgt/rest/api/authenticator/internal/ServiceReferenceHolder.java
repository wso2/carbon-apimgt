/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.authenticator.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.util.Map;

/**
 * Class used to hold the APIM store configurations.
 */
public class ServiceReferenceHolder {
    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private APIMAppConfigurations config = null;
    private APIMConfigurations config1 = null;


    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public APIMAppConfigurations getAPIMAppConfiguration() {
        try {
            if (configProvider != null) {
                config = configProvider.getConfigurationObject(APIMAppConfigurations.class);
            } else {
                log.error("Configuration provider is null");
            }
        } catch (CarbonConfigurationException e) {
            log.error("Error getting config : org.wso2.carbon.apimgt.rest.api.authenticator.internal.APIMAppConfiguration", e);
        }

        if (config == null) {
            config = new APIMAppConfigurations();
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
                    "org.wso2.carbon.apimgt.rest.api.authenticator.internal.APIMAppConfiguration", e);
        }
        return null;
    }

    public APIMConfigurations getAPIMConfiguration() {
        try {
            if (configProvider != null) {
                config1 = configProvider.getConfigurationObject(APIMConfigurations.class);
            } else {
                log.error("Configuration provider is null");
            }
        } catch (CarbonConfigurationException e) {
            log.error("error getting config : org.wso2.carbon.apimgt.core.internal.APIMConfiguration", e);
        }

        if (config1 == null) {
            config1 = new APIMConfigurations();
            log.info("Setting default configurations...");
        }

        return config1;
    }
}