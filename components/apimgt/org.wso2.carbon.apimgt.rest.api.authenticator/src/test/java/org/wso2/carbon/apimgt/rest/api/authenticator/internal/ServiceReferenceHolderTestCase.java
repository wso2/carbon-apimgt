/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.HashMap;
import java.util.Map;

public class ServiceReferenceHolderTestCase {
    @Test
    public void testGetInstance() {
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetAPIMAppConfiguration() throws ConfigurationException {
        //// Happy Path
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);

        APIMAppConfigurations expectedConfigs = new APIMAppConfigurations();
        expectedConfigs.setApimBaseUrl("https://localhost:9443/");
        Mockito.when(configProvider.getConfigurationObject(Mockito.any(Class.class))).thenReturn(expectedConfigs);

        APIMAppConfigurations actualConfigs = instance.getAPIMAppConfiguration();
        Assert.assertNotNull(actualConfigs);
        Assert.assertEquals(expectedConfigs.getApimBaseUrl(), actualConfigs.getApimBaseUrl());

        //// Error path
        //// ConfigurationException
        Mockito.when(configProvider.getConfigurationObject(Mockito.any(Class.class)))
                .thenThrow(ConfigurationException.class);
        actualConfigs = instance.getAPIMAppConfiguration();
        Assert.assertNotNull(actualConfigs);

        //// config provider is null
        instance.setConfigProvider(null);
        actualConfigs = instance.getAPIMAppConfiguration();
        Assert.assertNotNull(actualConfigs);
    }

    @Test
    public void testGetRestAPIConfigurationMap() throws ConfigurationException {
        //// Happy Path
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);

        Map<String, String> expectedConfigMap = new HashMap<>();
        expectedConfigMap.put("apimBaseUrl", "https://localhost:9443/");
        Mockito.when(configProvider.getConfigurationObject("xxx-namespace-xxx")).thenReturn(expectedConfigMap);

        Map<String, String> actualConfigMap = instance.getRestAPIConfigurationMap("xxx-namespace-xxx");
        Assert.assertEquals(expectedConfigMap, actualConfigMap);

        //// Error path
        //// ConfigurationException
        Mockito.when(configProvider.getConfigurationObject("xxx-namespace-xxx"))
                .thenThrow(ConfigurationException.class);
        actualConfigMap = instance.getRestAPIConfigurationMap("xxx-namespace-xxx");
        Assert.assertNull(actualConfigMap);

        //// config provider is null
        instance.setConfigProvider(null);
        actualConfigMap = instance.getRestAPIConfigurationMap("xxx-namespace-xxx");
        Assert.assertNull(actualConfigMap);
    }
}
