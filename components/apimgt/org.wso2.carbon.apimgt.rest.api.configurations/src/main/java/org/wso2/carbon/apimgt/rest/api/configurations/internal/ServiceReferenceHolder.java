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

package org.wso2.carbon.apimgt.rest.api.configurations.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.configurations.models.APIMUIConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.models.Feature;
import org.wso2.carbon.apimgt.rest.api.configurations.utils.ConfigurationAPIConstants;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to hold the APIM store/publisher configurations.
 */
public class ServiceReferenceHolder {
    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private APIMUIConfigurations apimUIConfigurations = null;

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public APIMUIConfigurations getApimUIConfigurations() {
        try {
            if (configProvider != null) {
                apimUIConfigurations = configProvider.getConfigurationObject(APIMUIConfigurations.class);
            } else {
                log.error("Configuration provider is null");
            }
        } catch (ConfigurationException e) {
            log.error("Error getting config : org.wso2.carbon.apimgt.rest.api.configurations.models." +
                    "EnvironmentConfigurations", e);
        }

        if (apimUIConfigurations == null) {
            apimUIConfigurations = new APIMUIConfigurations();
            log.info("Setting default configurations...");
        }

        return apimUIConfigurations;
    }

    /**
     * Get available features
     *
     * @return featureList List of features
     */
    public Map<String, Feature> getAvailableFeatures() {

        Map<String, Feature> featureMap = new HashMap<>();
        featureMap.put(ConfigurationAPIConstants.PRIVATE_JET_MODE_ID, getFeature(ConfigurationAPIConstants
                .CONTAINER_BASED_GATEWAY_NAMESPACE, ConfigurationAPIConstants.PRIVATE_JET_MODE_NAME));

        return featureMap;
    }

    /**
     * Get feature details
     *
     * @param namespace   Namespace
     * @param featureName Name of the feature
     * @return feature returns the feature details
     */
    private Feature getFeature(String namespace, String featureName) {

        Feature feature;
        try {
            if (configProvider != null) {
                Map configs = (Map) configProvider.getConfigurationObject(namespace);
                boolean enabled = false;
                if (configs != null) {
                    enabled = (Boolean) configs.get(ConfigurationAPIConstants.ENABLED);
                }
                feature = new Feature(featureName, enabled);
                return feature;
            } else {
                log.error("Configuration provider is null");
            }
        } catch (ConfigurationException e) {
            log.error("Error getting configuration for namespace " + namespace, e);
        }

        feature = new Feature(featureName, false);
        log.info("Setting default configurations for [feature] " + featureName + " and [namespace] " + namespace);
        return feature;
    }
}
