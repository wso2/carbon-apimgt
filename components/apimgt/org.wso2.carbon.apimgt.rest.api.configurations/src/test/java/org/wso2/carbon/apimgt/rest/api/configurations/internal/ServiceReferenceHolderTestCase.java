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
import org.wso2.carbon.apimgt.rest.api.configurations.models.APIMUIConfigurations;
import org.wso2.carbon.apimgt.rest.api.configurations.models.Feature;
import org.wso2.carbon.apimgt.rest.api.configurations.utils.ConfigurationAPIConstants;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.HashMap;
import java.util.List;
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
        APIMUIConfigurations apimUIConfigurations = instance.getApimUIConfigurations();
        Assert.assertNotNull(apimUIConfigurations);

        ////ConfigProvider is null
        instance.setConfigProvider(null);
        apimUIConfigurations = instance.getApimUIConfigurations();
        Assert.assertNotNull(apimUIConfigurations);

        ////CarbonConfigurationException when reading configs
        configProvider = new ConfigProvider() {
            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws ConfigurationException {
                throw new ConfigurationException("Error while creating configuration instance");
            }

            @Override
            public Object getConfigurationObject(String namespace) throws ConfigurationException {
                throw new ConfigurationException("Error while creating configuration instance");
            }

            public <T> T getConfigurationObject(String s, Class<T> aClass) throws ConfigurationException {
                return null;
            }
        };
        instance.setConfigProvider(configProvider);
        apimUIConfigurations = instance.getApimUIConfigurations();
        Assert.assertNotNull(apimUIConfigurations);
    }

    @Test
    public void testGetAvailableFeatures() throws ConfigurationException {

        ////Happy Path
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);
        Map configs = new HashMap<>();
        configs.put(ConfigurationAPIConstants.ENABLED, true);
        Mockito.when(configProvider.getConfigurationObject(Mockito.anyString())).thenReturn(configs);
        Map<String, Feature> featureList = instance.getAvailableFeatures();
        Assert.assertNotNull(featureList);
    }

    @Test
    public void testGetAvailableFeaturesWhenConfigProviderIsNull() throws ConfigurationException {

        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        instance.setConfigProvider(null);
        Map<String, Feature> featureList = instance.getAvailableFeatures();
        Assert.assertNotNull(featureList);
    }

    @Test
    public void testGetAvailableFeaturesForException() throws ConfigurationException {

        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);
        Mockito.when(configProvider.getConfigurationObject(Mockito.anyString()))
                .thenThrow(ConfigurationException.class);
        Map<String, Feature> featureList = instance.getAvailableFeatures();
        Assert.assertNotNull(featureList);
    }
}
