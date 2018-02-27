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

public class ServiceReferenceHolderTestCase {
    @Test
    public void testGetInstance() {
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        Assert.assertNotNull(instance);
    }

    public void testGetAPIMAppConfiguration() throws ConfigurationException {
        ////Happy Path
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        instance.setConfigProvider(configProvider);
        APIMAppConfigurations apimAppConfigurations = new APIMAppConfigurations();
        apimAppConfigurations.setApimBaseUrl("https://localhost:9292/");
        Mockito.when(configProvider.getConfigurationObject(Mockito.anyString())).thenReturn(apimAppConfigurations);
        instance.getAPIMAppConfiguration();
        Assert.assertNotNull(apimAppConfigurations);
    }

    @Test
    public void testGetAPIMAppConfigurationWhenConfigProviderIsNull() throws ConfigurationException {
        ServiceReferenceHolder instance = ServiceReferenceHolder.getInstance();
        instance.setConfigProvider(null);
        APIMAppConfigurations apimAppConfigurations = instance.getAPIMAppConfiguration();
        Assert.assertNotNull(apimAppConfigurations);
    }
}
