/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.configurations;

import org.wso2.carbon.apimgt.rest.api.configurations.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.configurations.models.APIMUIConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.models.Feature;

import java.util.Map;

/**
 * Service class for get configurations
 */
public class ConfigurationService {
    private static ConfigurationService instance = new ConfigurationService();
    private APIMUIConfigurations apimUIConfigurations;
    private Map<String, Feature> availableFeatures;

    private ConfigurationService() {
        apimUIConfigurations = ServiceReferenceHolder.getInstance().getApimUIConfigurations();
        availableFeatures = ServiceReferenceHolder.getInstance().getAvailableFeatures();
    }

    public static ConfigurationService getInstance() {
        return instance;
    }

    public APIMUIConfigurations getApimUIConfigurations() {
        return apimUIConfigurations;
    }

    public Map<String, Feature> getAvailableFeatures() {
        return availableFeatures;
    }
}