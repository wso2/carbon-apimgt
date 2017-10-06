package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class, ConfigurationContextFactory.class })
public class CEPPolicyManagementServiceClientTest {
    private final String USER_NAME = "admin";

    @Test
    public void testShouldInitializeServiceClient() throws AxisFault {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ConfigurationContextFactory.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration config = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService configService = Mockito.mock(APIManagerConfigurationService.class);
        ConfigurationContextFactory configFactory = Mockito.mock(ConfigurationContextFactory.class);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configService);
        Mockito.when(configService.getAPIManagerConfiguration()).thenReturn(config);
        Mockito.when(config.getFirstProperty(Mockito.anyString()))
                .thenReturn("")
                .thenReturn(USER_NAME)
                .thenReturn("")
                .thenReturn(USER_NAME)
                .thenReturn(null)
                .thenReturn(USER_NAME);
        Mockito.when(configFactory.createConfigurationContextFromFileSystem(null, null))
                .thenReturn(configurationContext)
                .thenThrow(AxisFault.class)
                .thenReturn(configurationContext);

        try {
            new CEPPolicyManagementServiceClient();
        } catch (APIManagementException e) {
            Assert.fail("Should not throw an exception");
        }

        try {
            // should throw exception this time
            new CEPPolicyManagementServiceClient();
            Assert.fail("Should throw an exception");
        } catch (APIManagementException e) {
        }

        try {
            // should throw exception this time
            new CEPPolicyManagementServiceClient();
            Assert.fail("Should throw an exception");
        } catch (APIManagementException e) {
        }
    }

}
