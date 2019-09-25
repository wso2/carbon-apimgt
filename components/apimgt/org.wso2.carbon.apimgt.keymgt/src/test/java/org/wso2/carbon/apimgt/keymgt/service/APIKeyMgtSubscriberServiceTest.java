package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.axis2.AxisFault;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.*;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Matchers.any;
import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MultitenantUtils.class, PrivilegedCarbonContext.class, CarbonContext.class, OAuthApplicationInfo.class, ApplicationManagementService.class,
        APIKeyMgtSubscriberService.class, ApiMgtDAO.class, OAuthServerConfiguration.class, OAuthCache.class,
        ServiceReferenceHolder.class, CarbonUtils.class, ServerConfiguration.class, APIUtil.class,
        APIKeyMgtUtil.class, OAuthServerConfiguration.class })
public class APIKeyMgtSubscriberServiceTest {
    private final int TENANT_ID = 1234;
    private final String TENANT_DOMAIN = "foo.com";
    private final String USER_NAME = "admin";
    private final String USER_NAME_WITH_TENANT = "admin@foo.com";
    private final String SECONDARY_USER_NAME = "secondary/admin@foo.com";
    private final String APPLICATION_NAME = "foo_PRODUCTION";
    private final String APPLICATION_NAME_1 = "sample_app";
    private final String APPLICATION_OWNER = "admin@foo.com";
    private final String CALLBACK_URL = "http://localhost";
    private final String TOKEN_TYPE = "DEFAULT";
    private final String CONSUMER_KEY = "Har2MjbxeMg3ysWEudjOKnXb3pAa";
    private final String CONSUMER_SECRET = "Ha52MfbxeFg3HJKEud156Y5GnAa";
    private final String[] GRANT_TYPES = {"password"};
    private final String REFRESH_GRANT_TYPE = "refresh_token";
    private final String IMPLICIT_GRANT_TYPE = "implicit";
    private final String ACCESS_TOKEN = "ca19a540f544777860e44e75f605d927";
    private APIKeyMgtSubscriberService apiKeyMgtSubscriberService = new APIKeyMgtSubscriberService();

    @Mock
    private OAuthServerConfiguration mockOAuthServerConfiguration;

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

        mockStatic(OAuthServerConfiguration.class);
        when(OAuthServerConfiguration.getInstance()).thenReturn(mockOAuthServerConfiguration);
        when(mockOAuthServerConfiguration.isClientSecretHashEnabled()).thenReturn(false);

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
        apiKeyMgtSubscriberService.createOAuthApplication(USER_NAME, APPLICATION_NAME, CALLBACK_URL, TOKEN_TYPE);
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
    public void testUpdateOAuthApplicationWithSpProperties() throws Exception {
        ApplicationManagementService appMgtService = Mockito.mock(ApplicationManagementService.class);
        ServiceProvider serviceProvider = new ServiceProvider();
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
        //validating void method arguments
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                String arg2 = invocation.getArgumentAt(2, String.class);
                ServiceProvider serviceProvider = invocation.getArgumentAt(0, ServiceProvider.class);
                Assert.assertEquals(MultitenantUtils.getTenantAwareUsername(SECONDARY_USER_NAME), arg2);
                ServiceProviderProperty[] serviceProviderPropertiesResult = serviceProvider.getSpProperties();
                Assert.assertEquals(2, serviceProviderPropertiesResult.length);
                for (ServiceProviderProperty serviceProviderProperty : serviceProviderPropertiesResult) {
                    if (APIConstants.APP_DISPLAY_NAME.equals(serviceProviderProperty.getName())) {
                        Assert.assertEquals(APPLICATION_NAME.substring(0, APPLICATION_NAME.lastIndexOf("_")),
                                serviceProviderProperty.getValue());
                        return null;
                    }
                }
                Assert.fail(APIConstants.APP_DISPLAY_NAME + " cannot be empty");
                // unused return
                return null;
            }
        }).when(appMgtService).updateApplication(any(ServiceProvider.class), any(String.class), any(String.class));
        //adding certificate property
        ServiceProviderProperty[] serviceProviderPropertiesArray = new ServiceProviderProperty[1];
        ServiceProviderProperty serviceProviderProperty1 = new ServiceProviderProperty();
        serviceProviderProperty1.setName("certificate");
        serviceProviderProperty1.setValue("certificate");
        serviceProviderPropertiesArray[0] = serviceProviderProperty1;
        serviceProvider.setSpProperties(serviceProviderPropertiesArray);
        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(serviceProvider);
        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(APPLICATION_NAME);
        OAuthApplicationInfo dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME, CALLBACK_URL, CONSUMER_KEY, GRANT_TYPES);
        Assert.assertEquals("Consumer Key should be same", CONSUMER_KEY, dto.getClientId());
        //adding certificate and displayName property
        serviceProviderPropertiesArray = new ServiceProviderProperty[2];
        serviceProviderProperty1 = new ServiceProviderProperty();
        serviceProviderProperty1.setName(APIConstants.APP_DISPLAY_NAME);
        serviceProviderProperty1.setValue(APPLICATION_NAME);
        ServiceProviderProperty serviceProviderProperty2 = new ServiceProviderProperty();
        serviceProviderProperty2.setName("certificate");
        serviceProviderProperty2.setValue("certificate");
        serviceProviderPropertiesArray[0] = serviceProviderProperty1;
        serviceProviderPropertiesArray[1] = serviceProviderProperty2;
        serviceProvider.setSpProperties(serviceProviderPropertiesArray);
        Mockito.when(appMgtService.getApplicationExcludingFileBasedSPs(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(serviceProvider);
        Mockito.when(appMgtService.getServiceProviderNameByClientId(CONSUMER_KEY, "oauth2", TENANT_DOMAIN))
                .thenReturn(APPLICATION_NAME);
        dto = apiKeyMgtSubscriberService
                .updateOAuthApplication(SECONDARY_USER_NAME, APPLICATION_NAME, CALLBACK_URL, CONSUMER_KEY, GRANT_TYPES);
        Assert.assertEquals("Consumer Key should be same", CONSUMER_KEY, dto.getClientId());
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

    @Test
    public void testGetSubscribedAPIsOfUser() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        APIInfoDTO[] apiInfoDTOS = new APIInfoDTO[1];
        apiInfoDTOS[0] = new APIInfoDTO();
        Mockito.when(apiMgtDAO.getSubscribedAPIsOfUser(USER_NAME)).thenReturn(apiInfoDTOS);
        Assert.assertEquals(1, apiKeyMgtSubscriberService.getSubscribedAPIsOfUser(USER_NAME).length);
    }

    @Test
    public void testRenewAccessToken() throws Exception {
        String tokenType = "production";
        String oldAccessToken = "s5d8v8d8f8ds5d9e7w53a1a7e5g5";
        String[] allowedDomains = new String[] {"wso2.com"};
        String validityTime = "3600";

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);

        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.TOKEN_ENDPOINT_NAME))
                .thenReturn("/oauth2/token");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                .thenReturn("https://localhost:9443/services/");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REVOKE_API_URL))
                .thenReturn("https://localhost:8280/revoke/");
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.mockStatic(ServerConfiguration.class);
        PowerMockito.mockStatic(APIUtil.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        Mockito.when(serverConfiguration.getFirstProperty("WebContextRoot")).thenReturn("/");
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);

        String jsonResponse =
                "{\"scope\":\"\",\"token_type\":\"Bearer\",\"expires_in\":2061,\"access_token\":\"" + ACCESS_TOKEN
                        + "\"}";
        Mockito.when(httpResponse.getEntity()).thenReturn(new StringEntity(jsonResponse));

        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        String newAccessToken = apiKeyMgtSubscriberService
                .renewAccessToken(tokenType, oldAccessToken, allowedDomains, CONSUMER_KEY, CONSUMER_SECRET,
                        validityTime);
        Assert.assertEquals(ACCESS_TOKEN, newAccessToken);

        Mockito.when(statusLine.getStatusCode()).thenReturn(500);
        try {
            newAccessToken = apiKeyMgtSubscriberService
                    .renewAccessToken(tokenType, oldAccessToken, allowedDomains, CONSUMER_KEY, CONSUMER_SECRET,
                            validityTime);
        } catch (APIKeyMgtException e) {
            Assert.assertEquals("Error in getting new accessToken", e.getMessage());
        }
    }

    @Test
    public void testRevokeAccessToken() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        PowerMockito.when(oAuthServerConfiguration.isCacheEnabled()).thenReturn(true);

        OAuthCache oAuthCache = Mockito.mock(OAuthCache.class);
        PowerMockito.mockStatic(OAuthCache.class);
        PowerMockito.when(OAuthCache.getInstance()).thenReturn(oAuthCache);

        apiKeyMgtSubscriberService.revokeAccessToken("1", CONSUMER_KEY, USER_NAME);
    }

    @Test
    public void testRevokeAccessTokenForApplication() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);
        Set<String> apiKeys = new HashSet<String>();
        Set<SubscribedAPI> apiSet = new HashSet<SubscribedAPI>();
        APIIdentifier apiIdentifier = new APIIdentifier(USER_NAME, "API_NAME", "1.0.0");
        SubscribedAPI subscribedAPI = new SubscribedAPI(new Subscriber(USER_NAME), apiIdentifier);
        Application application = new Application("app_name", new Subscriber(USER_NAME));
        application.setId(1);
        subscribedAPI.setApplication(application);
        apiSet.add(subscribedAPI);
        apiKeys.add(ACCESS_TOKEN);
        Mockito.when(apiMgtDAO.getApplicationKeys(1)).thenReturn(apiKeys);

        Mockito.when(apiMgtDAO.getSubscribedAPIs(Mockito.any(Subscriber.class), Mockito.anyString()))
                .thenReturn(apiSet);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        environmentMap.put("hybrid", new Environment());
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        PowerMockito.mockStatic(APIKeyMgtUtil.class);
        API api = new API(apiIdentifier);
        PowerMockito.when(APIKeyMgtUtil.getAPI(apiIdentifier)).thenReturn(api);

        APIAuthenticationAdminClient apiAuthenticationAdminClient = Mockito.mock(APIAuthenticationAdminClient.class);
        PowerMockito.whenNew(APIAuthenticationAdminClient.class).withAnyArguments()
                .thenReturn(apiAuthenticationAdminClient);
        apiKeyMgtSubscriberService.revokeAccessTokenForApplication(application);

        Application[] applications = new Application[1];
        applications[0] = application;
        Mockito.when(apiMgtDAO.getApplications(Mockito.any(Subscriber.class), Mockito.anyString()))
                .thenReturn(applications);
        testRevokeAccessTokenBySubscriber((new Subscriber(USER_NAME)));

        Mockito.when(apiMgtDAO.getApplicationsByTier(Mockito.anyString())).thenReturn(applications);
        testRevokeKeysByTier("GOLD");
        Mockito.reset(apiMgtDAO);
    }

    public void testRevokeAccessTokenBySubscriber(Subscriber subscriber) {
        try {
            apiKeyMgtSubscriberService.revokeAccessTokenBySubscriber(subscriber);
        } catch (AxisFault e) {
            Assert.fail("AxisFault should not be throw");
        } catch (APIManagementException e) {
            Assert.fail("APIManagementException should not be throw");
        }
    }

    public void testRevokeKeysByTier(String tierName) {
        try {
            apiKeyMgtSubscriberService.revokeKeysByTier(tierName);
        } catch (AxisFault e) {
            Assert.fail("AxisFault should not be throw");
        } catch (APIManagementException e) {
            Assert.fail("APIManagementException should not be throw");
        }
    }

    @Test
    public void testClearOAuthCache() throws Exception {
        OAuthServerConfiguration oAuthServerConfiguration = Mockito.mock(OAuthServerConfiguration.class);
        PowerMockito.mockStatic(OAuthServerConfiguration.class);
        PowerMockito.when(OAuthServerConfiguration.getInstance()).thenReturn(oAuthServerConfiguration);
        PowerMockito.when(oAuthServerConfiguration.isCacheEnabled()).thenReturn(true);

        OAuthCache oAuthCache = Mockito.mock(OAuthCache.class);
        PowerMockito.mockStatic(OAuthCache.class);
        PowerMockito.when(OAuthCache.getInstance()).thenReturn(oAuthCache);
        apiKeyMgtSubscriberService.clearOAuthCache(CONSUMER_KEY, USER_NAME);
    }

    @Test
    public void testRevokeTokensOfUserByApp() throws Exception {
        PowerMockito.mockStatic(ApiMgtDAO.class);
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.when(ApiMgtDAO.getInstance()).thenReturn(apiMgtDAO);

        List<AccessTokenInfo> accessTokens = new ArrayList<AccessTokenInfo>();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken(ACCESS_TOKEN);
        accessTokenInfo.setConsumerKey(CONSUMER_KEY);
        accessTokenInfo.setConsumerSecret(CONSUMER_SECRET);
        accessTokens.add(accessTokenInfo);
        Mockito.when(apiMgtDAO.getAccessTokenListForUser(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER))
                .thenReturn(accessTokens);
        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(CarbonContext.getThreadLocalCarbonContext().getUsername()).thenReturn(USER_NAME);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn(TENANT_DOMAIN);
        RealmConfiguration realmConfiguration = Mockito.mock(RealmConfiguration.class);
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        Mockito.doReturn("admin").when(realmConfiguration).getAdminRoleName();
        String[] userRoles = new String[2];
        userRoles[0] = "admin";
        userRoles[1] = "Internal/subscriber";
        Mockito.doReturn(userRoles).when(userStoreManager).getRoleListOfUser(any(String.class));
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(MultitenantUtils.getTenantDomain(APPLICATION_OWNER)).thenReturn(TENANT_DOMAIN);
        PowerMockito.when(MultitenantUtils.getTenantAwareUsername(APPLICATION_OWNER)).thenReturn(USER_NAME);
        PowerMockito.when(MultitenantUtils.getTenantAwareUsername(USER_NAME)).thenReturn(USER_NAME);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        Map<String, Environment> environmentMap = new HashMap<String, Environment>();
        Environment environment = new Environment();
        environment.setApiGatewayEndpoint("http://localhost:8280,https://localhost:8243");
        environmentMap.put("hybrid", environment);
        Mockito.when(apiManagerConfiguration.getApiGatewayEnvironments()).thenReturn(environmentMap);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REVOKE_API_URL))
                .thenReturn("https://localhost:8280/revoke/");
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService())
                .thenReturn(apiManagerConfigurationService);

        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenReturn(httpResponse);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        PowerMockito.when(APIUtil.getHttpClient(Mockito.anyInt(), Mockito.anyString())).thenReturn(httpClient);
        boolean status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME,
                APPLICATION_OWNER);
        Assert.assertEquals(true, status);

        Mockito.when(statusLine.getStatusCode()).thenReturn(500);
        try {
            status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER);
            Assert.fail("APIManagementException should be thrown");
        } catch (RuntimeException e) {
            Assert.assertEquals("Token revoke failed : HTTP error code : " + 500, e.getMessage());
        }

        Mockito.when(httpClient.execute(Mockito.any(HttpPost.class))).thenThrow(new IOException("Connection Error"));
        try {
            status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error while creating tokens - Connection Error", e.getMessage());
        }

        PowerMockito.whenNew(UrlEncodedFormEntity.class).withArguments(Matchers.anyObject(), Matchers.anyString())
                .thenThrow(new UnsupportedEncodingException("Unsupported Encoding"));
        try {
            status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error while preparing request for token/revoke APIs", e.getMessage());
        }

        Mockito.when(apiMgtDAO.getAccessTokenListForUser(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER))
                .thenThrow(new SQLException("Error getting AccessToken List For User"));
        try {
            status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER);
            Assert.fail("APIManagementException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error while revoking token for user=" + USER_NAME + " app=" + APPLICATION_NAME
                    + " owned by=" + APPLICATION_OWNER + " by logged in user=" + USER_NAME_WITH_TENANT, e.getMessage());
        }
        Mockito.when(userStoreManager.getRoleListOfUser(USER_NAME))
                .thenThrow(new UserStoreException("Error getting user store information"));
        try {
            status = apiKeyMgtSubscriberService.revokeTokensOfUserByApp(USER_NAME, APPLICATION_NAME, APPLICATION_OWNER);
            Assert.fail("UserStoreException should be thrown");
        } catch (APIManagementException e) {
            Assert.assertEquals("Error while authenticating the logged in user=" + USER_NAME_WITH_TENANT +
                    " while revoking token for user=" + USER_NAME + " app=" + APPLICATION_NAME + " owned by=" +
                    APPLICATION_OWNER, e.getMessage());
        }
    }
}