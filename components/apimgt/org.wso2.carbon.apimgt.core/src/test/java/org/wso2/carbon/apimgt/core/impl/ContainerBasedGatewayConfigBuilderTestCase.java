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

package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.configuration.models.ContainerBasedGatewayConfiguration;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

public class ContainerBasedGatewayConfigBuilderTestCase {

    @Test(description = "Test all the methods in ContainerBasedGatewayConfigBuilder")
    public void testContainerBasedGatewayConfigBuilder() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ContainerBasedGatewayConfiguration config = Mockito.mock(ContainerBasedGatewayConfiguration.class);
        Mockito.when(configProvider.getConfigurationObject(ContainerBasedGatewayConfiguration.class))
                .thenReturn(config);

        ContainerBasedGatewayConfigBuilder.build(configProvider);
        Assert.assertNotNull(ContainerBasedGatewayConfigBuilder.getContainerBasedGatewayConfiguration());
        ContainerBasedGatewayConfigBuilder.clearContainerBasedGatewayConfig();
        Assert.assertNull(ContainerBasedGatewayConfigBuilder.getContainerBasedGatewayConfiguration());
    }

    @Test(description = "Test the flow where Config Provider throws an exception")
    public void testWhenExceptionThrownByConfigProvider() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        Mockito.doThrow(ConfigurationException.class)
                .when(configProvider).getConfigurationObject(ContainerBasedGatewayConfiguration.class);

        ContainerBasedGatewayConfigBuilder.build(configProvider);
        Assert.assertNotNull(ContainerBasedGatewayConfigBuilder.getContainerBasedGatewayConfiguration());
        Assert.assertNotNull(ContainerBasedGatewayConfigBuilder.getContainerBasedGatewayConfiguration());
    }
}
