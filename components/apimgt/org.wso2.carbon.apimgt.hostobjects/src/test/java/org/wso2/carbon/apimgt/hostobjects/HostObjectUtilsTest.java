package org.wso2.carbon.apimgt.hostobjects;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.ProviderKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({HostObjectComponent.class, PrivilegedCarbonContext.class, ProviderKeyMgtClient.class,
        HostObjectUtils.class, MultitenantUtils.class, Caching.class})
public class HostObjectUtilsTest {
    private HostObjectUtils hostObject = new HostObjectUtils();

    @Test
    public void testGetBackendPort() throws Exception {
        //success case
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContext context = Mockito.mock(ConfigurationContext.class);
        ConfigurationContextService contextService = Mockito.mock(ConfigurationContextService.class);
        Mockito.when(contextService.getServerConfigContext()).thenReturn(context);
        Mockito.when(context.getAxisConfiguration()).thenReturn(axisConfiguration);
        hostObject.setConfigContextService(contextService);
        String returnedPortSuccess = hostObject.getBackendPort("http");
        Assert.assertNotNull(returnedPortSuccess);
        Assert.assertNotEquals("", returnedPortSuccess);

        //error case
        hostObject.setConfigContextService(null);
        String returnedPortError = hostObject.getBackendPort("http");
        Assert.assertNull(returnedPortError);

    }

    @Test
    public void testGetKeyManagementClient() throws Exception {
        PowerMockito.mockStatic(HostObjectComponent.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(hostObjectComponent.getAPIManagerConfiguration()).thenReturn(apimConfiguration);
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn("https://localhost:9443/services/");
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn("admin");
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn("admin");

        Assert.assertTrue(hostObject.getKeyManagementClient() instanceof SubscriberKeyMgtClient);
    }

    @Test
    public void testGetProviderClient() throws Exception {
        PowerMockito.mockStatic(HostObjectComponent.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ProviderKeyMgtClient pkmClient = PowerMockito.mock(ProviderKeyMgtClient.class);

        Mockito.when(hostObjectComponent.getAPIManagerConfiguration()).thenReturn(apimConfiguration);
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn("https://localhost:9443/services/");
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn("admin");
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD)).thenReturn("admin");
        PowerMockito.whenNew(ProviderKeyMgtClient.class).withAnyArguments().thenReturn(pkmClient);

        Assert.assertTrue(hostObject.getProviderClient() instanceof ProviderKeyMgtClient);

    }

    @Test
    public void testGetProviderClientWithInvalidCredentials() throws Exception {
        PowerMockito.mockStatic(HostObjectComponent.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ProviderKeyMgtClient pkmClient = PowerMockito.mock(ProviderKeyMgtClient.class);

        Mockito.when(hostObjectComponent.getAPIManagerConfiguration()).thenReturn(apimConfiguration);
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL)).thenReturn("https://localhost:9443/services/");
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME)).thenReturn(null);
        PowerMockito.whenNew(ProviderKeyMgtClient.class).withAnyArguments().thenReturn(pkmClient);

        try{
            hostObject.getProviderClient();
            Assert.fail("APIManagementExcception not thrown");
        } catch (APIManagementException e) {
            String msg = "Authentication credentials for API Provider manager unspecified";
            Assert.assertEquals(msg, e.getMessage());
        }

    }

    @Test
    public void testCheckDataPublishingEnabled() throws Exception {
        hostObject.checkDataPublishingEnabled();
    }

    @Test
    public void testInvalidateRecentlyAddedAPICacheWhenRecentlyAddedAPICacheDisabled() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(HostObjectComponent.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);

        Mockito.when(hostObjectComponent.getAPIManagerConfiguration()).thenReturn(apimConfiguration);

        hostObject.invalidateRecentlyAddedAPICache("admin@test.com");
    }

    @Test
    public void testInvalidateRecentlyAddedAPICacheWhenRecentlyAddedAPICacheEnabled() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(HostObjectComponent.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(Caching.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        APIManagerConfiguration apimConfiguration = Mockito.mock(APIManagerConfiguration.class);
        MultitenantUtils multitenantUtils = Mockito.mock(MultitenantUtils.class);
        Caching caching = Mockito.mock(Caching.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);

        Mockito.when(hostObjectComponent.getAPIManagerConfiguration()).thenReturn(apimConfiguration);
        Mockito.when(apimConfiguration.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE)).thenReturn("true");
        Mockito.when(privilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(multitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("test.com");
        Mockito.when(caching.getCacheManager(Mockito.anyString())).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(cache);

        hostObject.invalidateRecentlyAddedAPICache("admin@test.com");

        //when tenant domain from user name equals super tenant domain
        Mockito.when(multitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        hostObject.invalidateRecentlyAddedAPICache("admin@tcarbon.super");
    }

    @Test
    public void testIsUsageDataSourceSpecified() throws Exception {
        PowerMockito.mockStatic(HostObjectComponent.class);
        HostObjectComponent hostObjectComponent = Mockito.mock(HostObjectComponent.class);
        DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
        CarbonDataSource carbonDataSource = Mockito.mock(CarbonDataSource.class);

        Mockito.when(hostObjectComponent.getDataSourceService()).thenReturn(dataSourceService);
        Mockito.when(dataSourceService.getDataSource(Mockito.anyString())).thenReturn(carbonDataSource);
        Assert.assertTrue(hostObject.isUsageDataSourceSpecified());

        //when datasource is not specified
        Mockito.when(dataSourceService.getDataSource(Mockito.anyString())).thenReturn(null);
        Assert.assertFalse(hostObject.isUsageDataSourceSpecified());

        //exception path
        Mockito.when(dataSourceService.getDataSource(Mockito.anyString())).thenThrow(new DataSourceException());
        hostObject.isUsageDataSourceSpecified();
    }

    @Test
    public void testCompareRequiredUserFieldComparator() throws Exception {
        HostObjectUtils.RequiredUserFieldComparator userFieldComparator = new HostObjectUtils.RequiredUserFieldComparator();
        UserFieldDTO userFieldDTO1 = Mockito.mock(UserFieldDTO.class);
        UserFieldDTO userFieldDTO2 = Mockito.mock(UserFieldDTO.class);

        //set required false for both
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), 0);

        //set required true for both
        Mockito.when(userFieldDTO1.getRequired()).thenReturn(true);
        Mockito.when(userFieldDTO2.getRequired()).thenReturn(true);
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), 0);

        //set required true for DTO1
        Mockito.when(userFieldDTO1.getRequired()).thenReturn(true);
        Mockito.when(userFieldDTO2.getRequired()).thenReturn(false);
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), -1);

        //set required true for DTO2
        Mockito.when(userFieldDTO1.getRequired()).thenReturn(false);
        Mockito.when(userFieldDTO2.getRequired()).thenReturn(true);
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), 1);
    }

    @Test
    public void testUserFieldComparatorCompare() throws Exception {
        HostObjectUtils.UserFieldComparator userFieldComparator = new HostObjectUtils.UserFieldComparator();
        UserFieldDTO userFieldDTO1 = Mockito.mock(UserFieldDTO.class);
        UserFieldDTO userFieldDTO2 = Mockito.mock(UserFieldDTO.class);

        //when local dispay order is equal
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), 0);

        //when local dispay order for DTO1 is high
        Mockito.when(userFieldDTO1.getDisplayOrder()).thenReturn(1);
        Mockito.when(userFieldDTO2.getDisplayOrder()).thenReturn(0);
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), 1);

        //when local dispay order for DTO2 is high
        Mockito.when(userFieldDTO1.getDisplayOrder()).thenReturn(0);
        Mockito.when(userFieldDTO2.getDisplayOrder()).thenReturn(1);
        Assert.assertEquals(userFieldComparator.compare(userFieldDTO1, userFieldDTO2), -1);
    }

}