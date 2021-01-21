package org.wso2.carbon.apimgt.rest.api.common;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.h2.osgi.utils.CarbonConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonContext.class, APIManagerFactory.class, RestApiCommonUtil.class})
public class RestApiCommonUtilTest {

    @Test
    public void testGetLoggedInUserProvider() throws Exception {

        System.setProperty(CARBON_HOME, "");
        String providerName = "admin";

        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        APIProvider testApiProvider = Mockito.mock(APIProvider.class);
        when(apiManagerFactory.getAPIProvider(providerName)).thenReturn(testApiProvider);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(providerName);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenCallRealMethod();

        APIProvider loggedInUserProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Assert.assertEquals(testApiProvider, loggedInUserProvider);
    }

    @Test
    public void testGetLoggedInUserTenantDomain() {

        String defaultTenantDomain = "wso2.com";
        System.setProperty(CarbonBaseConstants.CARBON_HOME, "");

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(defaultTenantDomain);

        String loggedInUsername = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Assert.assertEquals(defaultTenantDomain, loggedInUsername);
    }

    @Test
    public void testGetConsumer() throws APIManagementException {

        String userName = "TEST_USER";

        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        when(apiManagerFactory.getAPIConsumer(userName)).thenReturn(apiConsumer);

        Assert.assertEquals(apiConsumer, RestApiCommonUtil.getConsumer(userName));
    }

}
