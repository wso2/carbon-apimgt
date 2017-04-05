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
import org.wso2.carbon.apimgt.core.APIMConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

/**
 * Class used to hold the APIM configuration
 * TODO refactor class when kernal is updated to 5.2.0
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

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public APIMConfigurations getAPIMConfiguration() {
        try {
            config = ServiceReferenceHolder.getInstance().getConfigProvider()
                    .getConfigurationObject(APIMConfigurations.class);
        } catch (CarbonConfigurationException e) {
            log.error("error getting config", e);
        }

        if (config == null) {
            config = new APIMConfigurations();
            log.info("Setting default configurations");
        }

        return config;
    }
}
