package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

public class ServiceDiscoveryConfigBuilderTestCase {

    @Test(description = "Test all the methods in ServiceDiscoveryConfigBuilder")
    public void testServiceDiscoveryConfigBuilder() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceDiscoveryConfigurations sdConfig = Mockito.mock(ServiceDiscoveryConfigurations.class);
        Mockito.when(configProvider.getConfigurationObject(ServiceDiscoveryConfigurations.class)).thenReturn(sdConfig);

        ServiceDiscoveryConfigBuilder.build(configProvider);
        Assert.assertNotNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
        ServiceDiscoveryConfigBuilder.clearServiceDiscoveryConfig();
        Assert.assertNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
    }

    @Test(description = "Test the flow where Config Provider throws an exception")
    public void testWhenExceptionThrownByConfigProvider() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        Mockito.doThrow(ConfigurationException.class)
                .when(configProvider).getConfigurationObject(ServiceDiscoveryConfigurations.class);

        ServiceDiscoveryConfigBuilder.build(configProvider);
        Assert.assertNotNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
    }
}
