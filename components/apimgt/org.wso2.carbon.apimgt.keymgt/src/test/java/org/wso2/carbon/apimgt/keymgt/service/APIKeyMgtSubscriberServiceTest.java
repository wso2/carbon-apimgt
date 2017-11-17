package org.wso2.carbon.apimgt.keymgt.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PrivilegedCarbonContext.class, OAuthApplicationInfo.class, ApplicationManagementService.class,
        APIKeyMgtSubscriberService.class, ApiMgtDAO.class, OAuthServerConfiguration.class, OAuthCache.class })
public class APIKeyMgtSubscriberServiceTest {
    private final int TENANT_ID = 1234;
    private final String TENANT_DOMAIN = "foo.com";
    private final String USER_NAME = "admin";
    private final String SECONDARY_USER_NAME = "secondary/admin@foo.com";
    private final String APPLICATION_NAME = "foo_PRODUCTION";
    private final String APPLICATION_NAME_1 = "sample_app";
    private final String CALLBACK_URL = "http://localhost";
    private final String CONSUMER_KEY = "Har2MjbxeMg3ysWEudjOKnXb3pAa";
    private final String CONSUMER_SECRET = "Ha52MfbxeFg3HJKEud156Y5GnAa";
    private final String[] GRANT_TYPES = { "password" };
    private final String REFRESH_GRANT_TYPE = "refresh_token";
    private final String IMPLICIT_GRANT_TYPE = "implicit";
    private APIKeyMgtSubscriberService apiKeyMgtSubscriberService = new APIKeyMgtSubscriberService();

    @Test
    public void createOAuthApplicationByApplicationInfo() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn(USER_NAME);

        OAuthApplicationInfo oauthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        Mockito.when(oauthApplicationInfo.getAppOwner()).thenReturn(USER_NAME);
        Mockito.when(oauthApplicationInfo.getClientName()).thenReturn(APPLICATION_NAME);
        Mockito.when(oauthApplicationInfo.getCallBackURL()).thenReturn(CALLBACK_URL);
        Mockito.when(oauthApplicationInfo.getIsSaasApplication()).thenReturn(Boolean.FALSE);

        ApplicationManagementService appMgtService = Mockito.mock(ApplicationManagementService.class);
        ServiceProvider serviceProvider = Mockito.mock(ServiceProvider.class);
        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(serviceProvider);
        PowerMockito.mockStatic(ApplicationManagementService.class);
        BDDMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);
        PowerMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);

        //Mocking OAuthConsumerAppDTO
        OAuthConsumerAppDTO oAuthConsumerAppDTO = Mockito.mock(OAuthConsumerAppDTO.class);
        PowerMockito.whenNew(OAuthConsumerAppDTO.class).withNoArguments().thenReturn(oAuthConsumerAppDTO);

        //Mocking OAuthAdminService
        OAuthAdminService oAuthAdminService = Mockito.mock(OAuthAdminService.class);
        PowerMockito.whenNew(OAuthAdminService.class).withNoArguments().thenReturn(oAuthAdminService);

        Mockito.when(oAuthAdminService.getAllowedGrantTypes()).thenReturn(GRANT_TYPES);
        Mockito.when(oAuthAdminService.getOAuthApplicationDataByAppName(Mockito.anyString()))
                .thenReturn(oAuthConsumerAppDTO);

        //Invoke createOAuthApplicationByApplicationInfo method
        apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);

        //Again run the same method with jsonPayload
        String jsonPayload = "{\"username\":\"" + SECONDARY_USER_NAME + "\"" +
                ",\"validityPeriod\":\"3600\"" +
                ",\"tokenScope\":\"default\"" +
                ",\"key_type\":\"PRODUCTION\"" +
                ",\"grant_types\":\"urn:ietf:params:oauth:grant-type:saml2-bearer,iwa:ntlm" +
                ",implicit,refresh_token,client_credentials,authorization_code,password\"}";
        Mockito.when(oauthApplicationInfo.getJsonString()).thenReturn(jsonPayload);
        Mockito.when(oauthApplicationInfo.getAppOwner()).thenReturn(SECONDARY_USER_NAME);
        apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);

        Assert.assertEquals(APPLICATION_NAME, oauthApplicationInfo.getClientName());

        Mockito.when(oAuthAdminService.getOAuthApplicationDataByAppName(Mockito.anyString()))
                .thenReturn(oAuthConsumerAppDTO);
        Mockito.when(oAuthConsumerAppDTO.getOauthConsumerKey()).thenReturn(CONSUMER_KEY);
        Mockito.when(oauthApplicationInfo.getClientName()).thenReturn(APPLICATION_NAME_1);
        OAuthApplicationInfo info = apiKeyMgtSubscriberService
                .createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
        Assert.assertEquals(CONSUMER_KEY, info.getClientId());

        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(null);
        try {
            apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error occurred while creating OAuthApp secondary_admin_sample_app", e.getMessage());
        }

        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(serviceProvider);
        jsonPayload = "{\"username\":\"" + SECONDARY_USER_NAME + "\"" + ",\"validityPeriod\":\"3600\""
                + ",\"tokenScope\":\"default\"" + ",\"key_type\":\"PRODUCTION\"" + "}";
        Mockito.when(oauthApplicationInfo.getJsonString()).thenReturn(jsonPayload);
        info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
        Assert.assertEquals(CONSUMER_KEY, info.getClientId());

        Mockito.when(oauthApplicationInfo.getAppOwner()).thenReturn(null);
        info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
        Assert.assertNull(info);

        jsonPayload = "{\"username\":\"" + SECONDARY_USER_NAME + "\"" + ",\"validityPeriod\":\"3600\""
                + ",\"tokenScope\":\"default\"" + ",\"key_type\":\"PRODUCTION\""
                + ",\"grant_types\":\"implicit,refresh_token\"}";
        Mockito.when(oauthApplicationInfo.getAppOwner()).thenReturn(SECONDARY_USER_NAME);
        Mockito.when(oauthApplicationInfo.getJsonString()).thenReturn(jsonPayload);
        Mockito.when(oauthApplicationInfo.getCallBackURL()).thenReturn(null);
        Mockito.when(oAuthConsumerAppDTO.getGrantTypes()).thenReturn(REFRESH_GRANT_TYPE);
        info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
        Assert.assertEquals(REFRESH_GRANT_TYPE, info.getParameter(ApplicationConstants.
                OAUTH_CLIENT_GRANT));

        Mockito.when(oAuthConsumerAppDTO.getOauthConsumerSecret()).thenReturn(CONSUMER_SECRET);
        info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
        Assert.assertEquals(REFRESH_GRANT_TYPE, info.getParameter(ApplicationConstants.
                OAUTH_CLIENT_GRANT));

        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new IdentityApplicationManagementException(
                        "Error getting Application Excluding File Based SPs"));
        try {
            info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error occurred while creating ServiceProvider for app secondary_admin_sample_app",
                    e.getMessage());
        }

        Mockito.when(oAuthAdminService.getOAuthApplicationDataByAppName(Mockito.anyString()))
                .thenThrow(new IdentityOAuthAdminException("Error getting OAuthApplication Data By AppName"));
        try {
            info = apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error occurred while creating ServiceProvider for app secondary_admin_sample_app",
                    e.getMessage());
        }
    }

    @Test
    public void createOAuthApplication() throws Exception {
        //Mocking OAuthConsumerAppDTO
        OAuthApplicationInfo oauthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        PowerMockito.whenNew(OAuthApplicationInfo.class).withNoArguments().thenReturn(oauthApplicationInfo);

        //Invoke createOAuthApplicationByApplicationInfo method
        apiKeyMgtSubscriberService.createOAuthApplication(USER_NAME, APPLICATION_NAME, CALLBACK_URL);
        Mockito.reset(oauthApplicationInfo);
    }

    @Test
    public void testUpdateOAuthApplication() throws Exception {
        ApplicationManagementService appMgtService = Mockito.mock(ApplicationManagementService.class);
        ServiceProvider serviceProvider = Mockito.mock(ServiceProvider.class);
        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(serviceProvider);
        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(APPLICATION_NAME);
        PowerMockito.mockStatic(ApplicationManagementService.class);
        BDDMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);
        PowerMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn(USER_NAME);

        //Mocking OAuthAdminService
        OAuthAdminService oAuthAdminService = Mockito.mock(OAuthAdminService.class);
        PowerMockito.whenNew(OAuthAdminService.class).withNoArguments().thenReturn(oAuthAdminService);

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setOauthConsumerKey(CONSUMER_KEY);
        Mockito.when(oAuthAdminService.getOAuthApplicationData(CONSUMER_KEY)).thenReturn(oAuthConsumerAppDTO);
        OAuthApplicationInfo dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME, CALLBACK_URL, CONSUMER_KEY, GRANT_TYPES);
        Assert.assertEquals("Consumer Key should be same", CONSUMER_KEY, dto.getClientId());

        dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(null, APPLICATION_NAME, CALLBACK_URL, CONSUMER_KEY, GRANT_TYPES);
        Assert.assertNull(dto);

        Mockito.when(oAuthAdminService.getAllowedGrantTypes())
                .thenReturn(new String[] { IMPLICIT_GRANT_TYPE, REFRESH_GRANT_TYPE });
        dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME, CALLBACK_URL, CONSUMER_KEY, null);
        Assert.assertEquals(IMPLICIT_GRANT_TYPE + " " + REFRESH_GRANT_TYPE, dto.getParameter(ApplicationConstants.
                OAUTH_CLIENT_GRANT));

        dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME, null, CONSUMER_KEY, null);
        Assert.assertEquals(REFRESH_GRANT_TYPE, dto.getParameter(ApplicationConstants.
                OAUTH_CLIENT_GRANT));

        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN)).thenThrow(
                new IdentityApplicationManagementException("Error while getting ServiceProvider Name By ClientId"));

        try {
            dto = apiKeyMgtSubscriberService
                    .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME_1, CALLBACK_URL, CONSUMER_KEY,
                            GRANT_TYPES);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error occurred while creating ServiceProvider for app secondary_admin_sample_app",
                    e.getMessage());
        }
    }

    @Test
    public void testRetrieveOAuthApplication() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId(CONSUMER_KEY);
        Mockito.when(apiMgtDAO.getOAuthApplication(CONSUMER_KEY)).thenReturn(oAuthApplicationInfo);
        Assert.assertEquals(apiKeyMgtSubscriberService.retrieveOAuthApplication(CONSUMER_KEY).getClientId(),
                CONSUMER_KEY);
    }

    @Test
    public void testDeleteOAuthApplication() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn(USER_NAME);

        apiKeyMgtSubscriberService.deleteOAuthApplication(null);
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Subscriber subscriber = new Subscriber(USER_NAME);
        Mockito.when(apiMgtDAO.getOwnerForConsumerApp(CONSUMER_KEY)).thenReturn(subscriber);

        ApplicationManagementService appMgtService = Mockito.mock(ApplicationManagementService.class);
        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(APPLICATION_NAME);
        PowerMockito.mockStatic(ApplicationManagementService.class);
        BDDMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);
        PowerMockito.when(ApplicationManagementService.getInstance()).thenReturn(appMgtService);

        apiKeyMgtSubscriberService.deleteOAuthApplication(CONSUMER_KEY);

        PowerMockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

        //Mocking OAuthAdminService
        OAuthAdminService oAuthAdminService = Mockito.mock(OAuthAdminService.class);
        PowerMockito.whenNew(OAuthAdminService.class).withNoArguments().thenReturn(oAuthAdminService);

        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        PowerMockito.when(oAuthServerConfiguration.isCacheEnabled()).thenReturn(true);

        OAuthCache oAuthCache = Mockito.mock(OAuthCache.class);
        PowerMockito.mockStatic(OAuthCache.class);
        PowerMockito.when(OAuthCache.getInstance()).thenReturn(oAuthCache);

        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
        apiKeyMgtSubscriberService.deleteOAuthApplication(CONSUMER_KEY);

        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(APPLICATION_NAME);
        PowerMockito.when(oAuthServerConfiguration.isCacheEnabled()).thenReturn(false);
        apiKeyMgtSubscriberService.deleteOAuthApplication(CONSUMER_KEY);

        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN)).thenThrow(
                new IdentityApplicationManagementException("Error while getting ServiceProvider Name By ClientID"));
        try {
            apiKeyMgtSubscriberService.deleteOAuthApplication(CONSUMER_KEY);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error occurred while deleting ServiceProvider", e.getMessage());
        }
    }
}