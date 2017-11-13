package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

public class ServiceDiscoveryConfigBuilderTestCase {

    @Test
    public void testServiceDiscoveryConfigBuilder() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceDiscoveryConfigurations sdConfig = Mockito.mock(ServiceDiscoveryConfigurations.class);
        Mockito.when(configProvider.getConfigurationObject(ServiceDiscoveryConfigurations.class)).thenReturn(sdConfig);

        ServiceDiscoveryConfigBuilder.build(configProvider);
        Assert.assertNotNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
        ServiceDiscoveryConfigBuilder.clearServiceDiscoveryConfig();
        Assert.assertNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
    }

    @Test
    public void testWhenExceptionThrownByConfigProvider() throws Exception {
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        Mockito.doThrow(CarbonConfigurationException.class)
                .when(configProvider).getConfigurationObject(ServiceDiscoveryConfigurations.class);

        ServiceDiscoveryConfigBuilder.build(configProvider);
        Assert.assertNotNull(ServiceDiscoveryConfigBuilder.getServiceDiscoveryConfiguration());
    }


}
