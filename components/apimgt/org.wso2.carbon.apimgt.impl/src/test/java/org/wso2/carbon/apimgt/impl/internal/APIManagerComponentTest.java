package org.wso2.carbon.apimgt.impl.internal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.util.APIManagerComponentWrapper;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;

import java.io.FileNotFoundException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUtil.class })
public class APIManagerComponentTest {

    @Before
    public void setup() {
        System.setProperty("carbon.home", "");
    }

    @Test
    public void testShouldNotContinueWhenConfigurationUnAvailable() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.doNothing().when(APIUtil.class, "loadTenantExternalStoreConfig", Mockito.anyInt());
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Registry registry = Mockito.mock(Registry.class);
        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(registry.resourceExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(configuration.getFirstProperty(Mockito.anyString())).thenThrow(FileNotFoundException.class);

        APIManagerComponent apiManagerComponent = new APIManagerComponentWrapper(registry);
        try {
            apiManagerComponent.activate(componentContext);
        } catch (FileNotFoundException f) {
            // Exception thrown hear means that method was continued without the configuration file
            Assert.fail("Should not throw an exception");
        }
    }



    @After
    public void destroy() {
        System.clearProperty("carbon.home");
    }
}
