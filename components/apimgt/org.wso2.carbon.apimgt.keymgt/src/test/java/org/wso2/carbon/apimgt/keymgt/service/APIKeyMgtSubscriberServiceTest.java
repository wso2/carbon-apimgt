package org.wso2.carbon.apimgt.keymgt.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, OAuthApplicationInfo.class, ApplicationManagementService.class, APIKeyMgtSubscriberService.class})
public class APIKeyMgtSubscriberServiceTest {
    private final int TENANT_ID = 1234;
    private final String TENANT_DOMAIN = "foo.com";
    private final String USER_NAME = "admin";
    private final String APPLICATION_NAME = "foo_PRODUCTION";
    private final String CALLBACK_URL = "http://localhost";
    private final String CONSUMER_KEY = "Har2MjbxeMg3ysWEudjOKnXb3pAa";
    private final String[] GRANT_TYPES = { "password" };
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
        String jsonPayload = "{\"username\":\"admin\"" +
                ",\"validityPeriod\":\"3600\"" +
                ",\"tokenScope\":\"default\"" +
                ",\"key_type\":\"PRODUCTION\"" +
                ",\"grant_types\":\"urn:ietf:params:oauth:grant-type:saml2-bearer,iwa:ntlm" +
                ",implicit,refresh_token,client_credentials,authorization_code,password\"}";
        Mockito.when(oauthApplicationInfo.getJsonString()).thenReturn(jsonPayload);
        apiKeyMgtSubscriberService.createOAuthApplicationByApplicationInfo(oauthApplicationInfo);

        Assert.assertEquals(APPLICATION_NAME, oauthApplicationInfo.getClientName());
    }

    @Test
    public void createOAuthApplication() throws Exception {
        //Mocking OAuthConsumerAppDTO
        OAuthApplicationInfo oauthApplicationInfo = Mockito.mock(OAuthApplicationInfo.class);
        PowerMockito.whenNew(OAuthApplicationInfo.class).withNoArguments().thenReturn(oauthApplicationInfo);

        //Invoke createOAuthApplicationByApplicationInfo method
        apiKeyMgtSubscriberService.createOAuthApplication(USER_NAME, APPLICATION_NAME, CALLBACK_URL);
    }

}