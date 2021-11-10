package org.wso2.carbon.apimgt.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class DefaultKeyManagerConnectorConfigurationTest {

    @InjectMocks
    DefaultKeyManagerConnectorConfiguration defaultKeyManagerConnectorConfiguration
            = new DefaultKeyManagerConnectorConfiguration();

    @Test
    public void getApplicationConfigurationsTestCase() {

        List<ConfigurationDto> configurationDtos =
                defaultKeyManagerConnectorConfiguration.getApplicationConfigurations();
        Assert.assertEquals(configurationDtos.get(0).getName(),
                APIConstants.KeyManager.APPLICATION_ACCESS_TOKEN_EXPIRY_TIME);
        Assert.assertEquals(configurationDtos.get(1).getName(), APIConstants.KeyManager.USER_ACCESS_TOKEN_EXPIRY_TIME);
        Assert.assertEquals(configurationDtos.get(2).getName(), APIConstants.KeyManager.REFRESH_TOKEN_EXPIRY_TIME);
        Assert.assertEquals(configurationDtos.get(3).getName(), APIConstants.KeyManager.ID_TOKEN_EXPIRY_TIME);
        Assert.assertEquals(configurationDtos.get(4).getName(), APIConstants.KeyManager.PKCE_MANDATORY);
        Assert.assertEquals(configurationDtos.get(5).getName(), APIConstants.KeyManager.PKCE_SUPPORT_PLAIN);
        Assert.assertEquals(configurationDtos.get(6).getName(), APIConstants.KeyManager.BYPASS_CLIENT_CREDENTIALS);
    }

}
