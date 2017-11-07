/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.configurations.internal;


import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.rest.api.configurations.models.EnvironmentConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.util.Map;

public class ServiceReferenceHolderTestCase {
    @Test
    public void testGetInstance() {
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetEnvironmentConfigurations() {
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();

        ////Happy Path
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);
        EnvironmentConfigurations environmentConfigurations = instance.getEnvironmentConfigurations();
        Assert.assertNotNull(environmentConfigurations);

        ////ConfigProvider is null
        instance.setConfigProvider(null);
        environmentConfigurations = instance.getEnvironmentConfigurations();
        Assert.assertNotNull(environmentConfigurations);

        ////CarbonConfigurationException when reading configs
        configProvider = new ConfigProvider() {
            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
                throw new CarbonConfigurationException("Error while creating configuration instance");
            }

            @Override
            public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
                return null;
            }
        };
        instance.setConfigProvider(configProvider);
        environmentConfigurations = instance.getEnvironmentConfigurations();
        Assert.assertNotNull(environmentConfigurations);
    }
}
