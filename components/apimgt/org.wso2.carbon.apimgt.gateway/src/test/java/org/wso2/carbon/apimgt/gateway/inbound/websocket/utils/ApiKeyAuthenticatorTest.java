package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class,
        ServiceReferenceHolder.class, WebsocketUtil.class, APIUtil.class, DataHolder.class,
        ApiKeyAuthenticator.class, CacheProvider.class, GatewayUtils.class, JWTClaimsSet.class})

public class ApiKeyAuthenticatorTest {

    private DataHolder dataHolder;
    List<String> keyManagers;
    String apiKey;

    @Before
    public void init() throws APIManagementException {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        dataHolder = Mockito.mock(DataHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(serviceReferenceHolder.getJwtValidationService()).thenReturn(jwtValidationService);
        Mockito.when(jwtValidationService.getKeyManagerNameIfJwtValidatorExist(Mockito.anyObject()))
                .thenReturn("KeyManager");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(WebsocketUtil.class);
        PowerMockito.mockStatic(DataHolder.class);
        Mockito.when(DataHolder.getInstance()).thenReturn(dataHolder);
        keyManagers = Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS);
        apiKey = "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI" +
                "6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc" +
                "3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIs" +
                "Im5hbWUiOiJEZWZhdWx0QXBwbGljYXRpb24iLCJpZCI6MSwidXVpZCI6IjA2MzRkMGI0LWRmMGEtNGMzZS04ZmY2LWRmODNhOTAzYTl" +
                "mNiJ9LCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJBc3luY1VubGltaX" +
                "RlZCI6eyJ0aWVyUXVvdGFUeXBlIjoiZXZlbnRDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiO" +
                "jAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0" +
                "eXBlIjoiU0FOREJPWCIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWF" +
                "pbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJDaGF0cyIsImNvbnRleHQiOiJcL2NoYXRzXC8xLjAuMCIsInB1Ymxpc2hlciI6ImFkbW" +
                "luIiwidmVyc2lvbiI6IjEuMC4wIiwic3Vic2NyaXB0aW9uVGllciI6IkFzeW5jVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS" +
                "2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE2OTM5OTk3MDcsImp0aSI6IjhjY2M1YzBlLWFlODMtNGM4MS1hN2JmLTVjMmNiYTc0" +
                "ZmM1OCJ9.VYkMrt6vs82V1cVJ-ChFcgBfej3m8P22lmnG0_Q1g_fox0ZeJklWhjtsI8dxyTJ0tRx57dg1dOFQD-VbRTnoxOmWDnZdxB" +
                "_cGQfrn-2A72HBTMx-lAaMGAi0Gi9OjXBa8J2ilc7qBgL4au4HVfSyOxpAJIDgwPnjIYjDovnYQPMhZemaOfKTbfReU3g_w8MBKLN20" +
                "hjZT02gKpwyak1LnXG4ulKi-A0qZlm2VSArpwF73x9vK0rIiWT17UR43IJNoDAmXjG76lwmNeIIiDhyqRBLDdRwmxFLlac6KsqeESMh" +
                "xoklqH1_0x4VyUG-JYlJMQVFd5FmWmnjjF4eJZwSaQ==";
    }

    @Test
    public void authenticationForAPIKeyAsRequestHeader() throws Exception {

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.getRequestHeaders().put(APIConstants.API_KEY_HEADER_QUERY_PARAM, apiKey);
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto())
                .thenReturn(new ExtendedJWTConfigurationDto());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "isGatewayTokenCacheEnabled"))
                .toReturn(true);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "verifyTokenSignature", SignedJWT.class,
                String.class)).toReturn(true);
        Cache invalidGatewayApiKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayApiKeyCache()).thenReturn(invalidGatewayApiKeyCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "isJwtTokenExpired"))
                .toReturn(false);
        Cache gatewayApiKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyDataCache()).thenReturn(gatewayApiKeyDataCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "validateAPISubscription", String.class,
                String.class, JWTClaimsSet.class, String[].class, boolean.class)).toReturn(new net.minidev.json.JSONObject());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "generateAuthenticationContext", String.class,
                JWTClaimsSet.class, net.minidev.json.JSONObject.class, String.class, String.class,
                org.apache.synapse.MessageContext.class)).toReturn(new AuthenticationContext());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "validateAuthenticationContext"))
                .toReturn(true);
        Assert.assertFalse(ApiKeyAuthenticator.authenticate(inboundMessageContext).isError());
    }

    @Test
    public void authenticationForAPIKeyAsQueryParam() throws Exception {

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setApiKeyFromQueryParams(apiKey);
        inboundMessageContext.setFullRequestPath("ws://localhost:9099/chats/1.0.0/notifications?apikey=" + apiKey);
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto())
                .thenReturn(new ExtendedJWTConfigurationDto());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "isGatewayTokenCacheEnabled"))
                .toReturn(true);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "verifyTokenSignature", SignedJWT.class,
                String.class)).toReturn(true);
        Cache invalidGatewayApiKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayApiKeyCache()).thenReturn(invalidGatewayApiKeyCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "isJwtTokenExpired"))
                .toReturn(false);
        Cache gatewayApiKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyDataCache()).thenReturn(gatewayApiKeyDataCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "validateAPISubscription", String.class,
                String.class, JWTClaimsSet.class, String[].class, boolean.class)).toReturn(new net.minidev.json.JSONObject());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "generateAuthenticationContext", String.class,
                JWTClaimsSet.class, net.minidev.json.JSONObject.class, String.class, String.class,
                org.apache.synapse.MessageContext.class)).toReturn(new AuthenticationContext());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "validateAuthenticationContext"))
                .toReturn(true);
        Assert.assertFalse(ApiKeyAuthenticator.authenticate(inboundMessageContext).isError());
    }

    @Test
    public void authenticationTestForExpiredAPIKey() throws Exception {

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.getRequestHeaders().put(APIConstants.API_KEY_HEADER_QUERY_PARAM, apiKey);
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto())
                .thenReturn(new ExtendedJWTConfigurationDto());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "isGatewayTokenCacheEnabled"))
                .toReturn(true);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "verifyTokenSignature", SignedJWT.class,
                String.class)).toReturn(true);
        Cache invalidGatewayApiKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayApiKeyCache()).thenReturn(invalidGatewayApiKeyCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "isJwtTokenExpired"))
                .toReturn(true);
        Cache gatewayApiKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyDataCache()).thenReturn(gatewayApiKeyDataCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "validateAPISubscription", String.class,
                String.class, JWTClaimsSet.class, String[].class, boolean.class)).toReturn(new net.minidev.json.JSONObject());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "generateAuthenticationContext", String.class,
                JWTClaimsSet.class, net.minidev.json.JSONObject.class, String.class, String.class,
                org.apache.synapse.MessageContext.class)).toReturn(new AuthenticationContext());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "validateAuthenticationContext"))
                .toReturn(true);
        Assert.assertTrue(ApiKeyAuthenticator.authenticate(inboundMessageContext).isError());
    }

    @Test
    public void authenticationTestForRestrictedIP() throws Exception {
        //Following APIKey has been generated by providing allowed IP as 192.168.1.1
        String apiKey = "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI" +
                "6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc" +
                "3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIs" +
                "Im5hbWUiOiJEZWZhdWx0QXBwbGljYXRpb24iLCJpZCI6MSwidXVpZCI6IjA1M2IzNjEwLTI5MGEtNDM0OC05Zjc4LWMyODM4OTc3NGE" +
                "2NiJ9LCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJBc3luY1VubGltaX" +
                "RlZCI6eyJ0aWVyUXVvdGFUeXBlIjoiZXZlbnRDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiO" +
                "jAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0" +
                "eXBlIjoiU0FOREJPWCIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWF" +
                "pbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJDaGF0cyIsImNvbnRleHQiOiJcL2NoYXRzXC8xLjAuMCIsInB1Ymxpc2hlciI6ImFkbW" +
                "luIiwidmVyc2lvbiI6IjEuMC4wIiwic3Vic2NyaXB0aW9uVGllciI6IkFzeW5jVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS" +
                "2V5IiwicGVybWl0dGVkSVAiOiIxOTIuMTY4LjEuMiIsImlhdCI6MTY5NDAxOTgzNywianRpIjoiYmJhNWM3MWYtZjc0NS00NDQzLTkz" +
                "OGEtODI1YzNhMmYyMmU2In0=.vywflR4UEKA7awycXGYFqFTn0AgUSZLSL9ob6kZLEMaei6PTktsVmGT9kLjRK5sJTmaSvr9JoUtvKL" +
                "kVhkUusL9rWp8Wikj0fPE-BkmYjGQYnRdzSS-M8z_ajS1MekjWvedcl5m-ID_8iMJEilDPPJNkmE1ujeUc1Pliw9KvM2L9kuogq9j5R" +
                "7s-E8jI-2oI_xdHc4hEXkU81GGUJtjfDkXbM2OrfOUAd-OWmFmgyp3p1tdbux2GbwBnuTcrF3kgxuQRHTv86hyxz_0Ik70ypNbpJ0qs" +
                "5z8qDyU4jSMoid7gyeCoOOJxyCAwCNjGNxc-6YsQpknQjXVd3cMfbOqneQ==";

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setApiKeyFromQueryParams(apiKey);
        inboundMessageContext.setFullRequestPath("ws://localhost:9099/chats/1.0.0/notifications?apikey=" + apiKey);
        inboundMessageContext.setUserIP("192.168.1.1");
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto())
                .thenReturn(new ExtendedJWTConfigurationDto());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "isGatewayTokenCacheEnabled"))
                .toReturn(true);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "verifyTokenSignature", SignedJWT.class,
                String.class)).toReturn(true);
        Cache invalidGatewayApiKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayApiKeyCache()).thenReturn(invalidGatewayApiKeyCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "isJwtTokenExpired"))
                .toReturn(false);
        Cache gatewayApiKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyDataCache()).thenReturn(gatewayApiKeyDataCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "validateAPISubscription", String.class,
                String.class, JWTClaimsSet.class, String[].class, boolean.class)).toReturn(new net.minidev.json.JSONObject());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "generateAuthenticationContext", String.class,
                JWTClaimsSet.class, net.minidev.json.JSONObject.class, String.class, String.class,
                org.apache.synapse.MessageContext.class)).toReturn(new AuthenticationContext());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "validateAuthenticationContext"))
                .toReturn(true);
        Assert.assertTrue(ApiKeyAuthenticator.authenticate(inboundMessageContext).isError());
    }

    @Test
    public void authenticationTestForRestrictedWebSites() throws Exception {
        //Following APIKey has been generated by providing allowed URL as www.wso2.com
        String apiKey = "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI" +
                "6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc" +
                "3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIs" +
                "Im5hbWUiOiJEZWZhdWx0QXBwbGljYXRpb24iLCJpZCI6MSwidXVpZCI6IjA1M2IzNjEwLTI5MGEtNDM0OC05Zjc4LWMyODM4OTc3NGE" +
                "2NiJ9LCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJVbmxpbWl0ZWQiOn" +
                "sidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiOjAsI" +
                "nN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH0sIkFzeW5jVW5s" +
                "aW1pdGVkIjp7InRpZXJRdW90YVR5cGUiOiJldmVudENvdW50IiwiZ3JhcGhRTE1heENvbXBsZXhpdHkiOjAsImdyYXBoUUxNYXhEZXB" +
                "0aCI6MCwic3RvcE9uUXVvdGFSZWFjaCI6dHJ1ZSwic3Bpa2VBcnJlc3RMaW1pdCI6MCwic3Bpa2VBcnJlc3RVbml0IjpudWxsfX0sIm" +
                "tleXR5cGUiOiJTQU5EQk9YIiwicGVybWl0dGVkUmVmZXJlciI6Ind3dy53c28yLmNvbSIsInN1YnNjcmliZWRBUElzIjpbeyJzdWJzY" +
                "3JpYmVyVGVuYW50RG9tYWluIjoiY2FyYm9uLnN1cGVyIiwibmFtZSI6IkNoYXRzIiwiY29udGV4dCI6IlwvY2hhdHNcLzEuMC4wIiwi" +
                "cHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoiMS4wLjAiLCJzdWJzY3JpcHRpb25UaWVyIjoiQXN5bmNVbmxpbWl0ZWQifSx7InN" +
                "1YnNjcmliZXJUZW5hbnREb21haW4iOiJjYXJib24uc3VwZXIiLCJuYW1lIjoiUGl6emFTaGFja0FQSSIsImNvbnRleHQiOiJcL3Bpen" +
                "phc2hhY2tcLzEuMC4wIiwicHVibGlzaGVyIjoiYWRtaW4iLCJ2ZXJzaW9uIjoiMS4wLjAiLCJzdWJzY3JpcHRpb25UaWVyIjoiVW5sa" +
                "W1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE2OTQwMjExMDEsImp0aSI6Ijc1MDkx" +
                "MDZkLWIxYTItNGIxOS05YmY2LTM0MjY5Yzg1YTJmZSJ9.RZoEI57qNBKlHR8BhHFEBlYcqkZUyXuIoh4MB1jFlk6TQK4VgGvTiTpNs1" +
                "9igd4sU2cmTN_F7CQQnwru7QZjRsbOHlxjhxSva1w0GjOZPg8IfX4NnNV0ThZDDwwDdtvxd-nxkoYCbRozvtk2VYjorCxVOQHsw97Jl" +
                "VDt-vRAfuchtaKTS2AlKwIzajael-3-88RWsI9i6LpboDB0VGyFzvHjP2uTp_Hg7cI5xTyaYvcbcIz71kyPc4PwCCkpyJAwvmE6EHLw" +
                "HdYQK3_K3j8B0W1TWq4aTOTluTf2KTJd1Bp36zT9KkVYuMdcCguacA3-ZJ_HhEb7AvSVNv9nC5flHw==";

        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setApiKeyFromQueryParams(apiKey);
        inboundMessageContext.setFullRequestPath("ws://localhost:9099/chats/1.0.0/notifications?apikey=" + apiKey);
        inboundMessageContext.getRequestHeaders().put("Referer", "www.example.com");
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        Mockito.when(ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto())
                .thenReturn(new ExtendedJWTConfigurationDto());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "isGatewayTokenCacheEnabled"))
                .toReturn(true);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "verifyTokenSignature", SignedJWT.class,
                String.class)).toReturn(true);
        Cache invalidGatewayApiKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayApiKeyCache()).thenReturn(invalidGatewayApiKeyCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "isJwtTokenExpired"))
                .toReturn(false);
        Cache gatewayApiKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayApiKeyDataCache()).thenReturn(gatewayApiKeyDataCache);
        Mockito.when(invalidGatewayApiKeyCache.get(Mockito.anyString())).thenReturn(null);
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "validateAPISubscription", String.class,
                String.class, JWTClaimsSet.class, String[].class, boolean.class)).toReturn(new net.minidev.json.JSONObject());
        PowerMockito.stub(PowerMockito.method(GatewayUtils.class, "generateAuthenticationContext", String.class,
                JWTClaimsSet.class, net.minidev.json.JSONObject.class, String.class, String.class,
                org.apache.synapse.MessageContext.class)).toReturn(new AuthenticationContext());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "validateAuthenticationContext"))
                .toReturn(true);
        Assert.assertTrue(ApiKeyAuthenticator.authenticate(inboundMessageContext).isError());
    }

    private InboundMessageContext createWebSocketApiMessageContext() {
        API websocketAPI = new API(UUID.randomUUID().toString(), 1, "admin", "WSAPI", "1.0.0", "/wscontext",
                "Unlimited", APIConstants.API_TYPE_WS, APIConstants.PUBLISHED_STATUS, false);
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setElectedAPI(websocketAPI);
        return inboundMessageContext;
    }
}
