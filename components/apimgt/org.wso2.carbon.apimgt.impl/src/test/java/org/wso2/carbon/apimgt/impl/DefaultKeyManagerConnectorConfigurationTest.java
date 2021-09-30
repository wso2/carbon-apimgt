package org.wso2.carbon.apimgt.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceReferenceHolder.class)
public class DefaultKeyManagerConnectorConfigurationTest {

    @Test
    public void getApplicationConfigurationsTestCase() {

        ConfigurationDto configurationDtoPkceMandatory = new ConfigurationDto(APIConstants.KeyManager.PKCE_MANDATORY,
                "Enable PKCE", "checkbox", "Enable PKCE", String.valueOf(false), false, false,
                Collections.EMPTY_LIST, false);

        ConfigurationDto configurationDtoPkcePlainText = new ConfigurationDto(APIConstants.KeyManager.PKCE_SUPPORT_PLAIN,
                "Support PKCE Plain text", "checkbox", "S256 is recommended, plain text too can be used."
                , String.valueOf(false), false, false, Collections.EMPTY_LIST, false);

        ConfigurationDto configurationDtoBypassClientCredentials = new ConfigurationDto(APIConstants.KeyManager.BYPASS_CLIENT_CREDENTIALS,
                "Public client", "checkbox", "Allow authentication without the client secret."
                , String.valueOf(false), false, false, Collections.EMPTY_LIST, false);

        DefaultKeyManagerConnectorConfiguration defaultKeyManagerConnectorConfiguration
                = new DefaultKeyManagerConnectorConfiguration();

        List<ConfigurationDto> configurationDtos =
                defaultKeyManagerConnectorConfiguration.getApplicationConfigurations();
        Assert.assertTrue(configurationDtos.contains(configurationDtoPkceMandatory));
        Assert.assertTrue(configurationDtos.contains(configurationDtoPkcePlainText));
        Assert.assertTrue(configurationDtos.contains(configurationDtoBypassClientCredentials));

    }

}
