package org.wso2.carbon.apimgt.rest.api.authenticator.internal;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMAppConfigurations;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.Map;

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
