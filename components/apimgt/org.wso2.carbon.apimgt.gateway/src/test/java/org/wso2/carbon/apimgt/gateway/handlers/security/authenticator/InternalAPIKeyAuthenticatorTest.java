package org.wso2.carbon.apimgt.gateway.handlers.security.authenticator;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.models.OpenAPI;
import net.minidev.json.JSONObject;
import org.apache.axis2.Constants;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GatewayUtils.class, OpenAPIUtils.class, CacheProvider.class, APISecurityUtils.class,
        PrivilegedCarbonContext.class})
public class InternalAPIKeyAuthenticatorTest {
    String internalKey = "eyJraWQiOiJnYXRld2F5X2NlcnRpZmljYXRlX2FsaWFzIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbkBjYXJ" +
            "ib24uc3VwZXIiLCJpc3MiOiJodHRwczpcL1wvMTcyLjE3LjAuMTo5OTQzXC9vYXV0aDJcL3Rva2VuIiwia2V5dHlwZSI6IlBST0RVQ1" +
            "RJT04iLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWFpbiI6bnVsbCwibmFtZSI6Im11dHVhbFNTTFdpdGhP" +
            "QXV0aEFQSSIsImNvbnRleHQiOiJcL3RcL3dzbzIuY29tXC9tdXR1YWxTU0xXaXRoT0F1dGhBUElcLzEuMC4wIiwicHVibGlzaGVyIjo" +
            "iYWRtaW5Ad3NvMi5jb20iLCJ2ZXJzaW9uIjoiMS4wLjAiLCJzdWJzY3JpcHRpb25UaWVyIjpudWxsfV0sImV4cCI6MTYxNDMyOTkzNC" +
            "widG9rZW5fdHlwZSI6IkludGVybmFsS2V5IiwiaWF0IjoxNjE0MjY5OTM0LCJqdGkiOiIyOGY4ZDdiMC05ZTYyLTQzNDEtYmYxNy0wO" +
            "TQ0NTNkNWZmYTQifQ.c6ERysb7mTvWxGx_4B-2SRO3uF1GPhvDob6kQCE4EliLt9jCBW2TQiM3vgjMmRtHBxpxNCbsPdBk6srkz1qu6K" +
            "S14rPfdEqKRwmTEWkMrnv5_A6wE3AReILuUP5_5x48rVzzfYar4p9VkxkzBf5-0PRC3ROv-x_Btx-d3_oDe96dz0esQuTpwuvPYJ12gh" +
            "qQEBsEXOYJmiCjXk6Hx1StZKy5wdx0sCA7GAY1H5J2k5kna8NDY6YDYh2zAzcp5K0U6Qd2MJVyiCd2Fj5bHF1WC-GTfUVWcQktxzajGE" +
            "8fh56R-Ta3ishwWa3x0n72snuTononOkwbWUYFVJ0whFc4pA";
    SignedJWT signedJWT;
    PrivilegedCarbonContext privilegedCarbonContext;

    @Before
    public void init() throws ParseException {
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.mockStatic(OpenAPIUtils.class);
        PowerMockito.mockStatic(CacheProvider.class);
        PowerMockito.mockStatic(APISecurityUtils.class);
        System.setProperty(CARBON_HOME, "");
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        signedJWT = SignedJWT.parse(internalKey);
    }

    @Test
    public void testAuthenticateNoCache() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        Mockito.when(invalidCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Mockito.when(GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET")).thenCallRealMethod();
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(false);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertTrue(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), 0);
        Assert.assertNull(authenticate.getErrorMessage());
        Mockito.verify(internalKeyCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(invalidCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(internalKeyCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "carbon.super");
        Mockito.verify(internalKeyDataCache, Mockito.times(1)).put(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void testAuthenticateNoCacheTokenTenant() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("abc.com");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        Cache superTenantInternalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache)
                .thenReturn(internalKeyCache).thenReturn(superTenantInternalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        Mockito.when(invalidCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Mockito.when(GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET")).thenCallRealMethod();
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(false);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.doNothing().when(PrivilegedCarbonContext.class, "startTenantFlow");
        PowerMockito.doNothing().when(privilegedCarbonContext).setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertTrue(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), 0);
        Assert.assertNull(authenticate.getErrorMessage());
        Mockito.verify(internalKeyCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(invalidCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(internalKeyCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "abc.com");
        Mockito.verify(superTenantInternalKeyCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "abc.com");
        Mockito.verify(internalKeyDataCache, Mockito.times(1)).put(Mockito.anyString(), Mockito.any());
    }


    @Test
    public void testAuthenticateNoCacheExpiredToken() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        Mockito.when(invalidCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        String cacheKey = GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET");
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(true);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        Mockito.verify(internalKeyCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(invalidCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(internalKeyCache, Mockito.times(0)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "carbon.super");
        Mockito.verify(invalidCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "carbon.super");
        Mockito.verify(internalKeyDataCache, Mockito.times(0)).put(Mockito.anyString(),
                Mockito.any(AuthenticationContext.class));
    }
    @Test
    public void testAuthenticateNoCacheInvalidSignatureToken() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        Mockito.when(invalidCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        String cacheKey = GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET");
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(false);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(true);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        Mockito.verify(internalKeyCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(invalidCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(internalKeyCache, Mockito.times(0)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "carbon.super");
        Mockito.verify(invalidCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "carbon.super");
        Mockito.verify(internalKeyDataCache, Mockito.times(0)).put(Mockito.anyString(),
                Mockito.any(AuthenticationContext.class));
    }

    @Test
    public void testAuthenticateNoCacheExpiredTokenTenant() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("abc.com");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        Mockito.when(invalidCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn(null);
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(true);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.doNothing().when(PrivilegedCarbonContext.class, "startTenantFlow");
        PowerMockito.doNothing().when(privilegedCarbonContext).setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        Mockito.verify(internalKeyCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(invalidCache, Mockito.times(1)).get("28f8d7b0-9e62-4341-bf17-094453d5ffa4");
        Mockito.verify(internalKeyCache, Mockito.times(0)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "abc.com");
        Mockito.verify(invalidCache, Mockito.times(1)).put("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "abc.com");
        Mockito.verify(internalKeyDataCache, Mockito.times(0)).put(Mockito.anyString(),
                Mockito.any(AuthenticationContext.class));
    }

    @Test
    public void testAuthenticate() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn("carbon.super");
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
        jwtTokenPayloadInfo.setPayload(signedJWT.getJWTClaimsSet());
        jwtTokenPayloadInfo.setAccessToken(internalKey);
        String cacheKey = GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET");
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        Mockito.when(internalKeyDataCache.get(cacheKey)).thenReturn(jwtTokenPayloadInfo);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(false);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertTrue(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), 0);
        Assert.assertNull(authenticate.getErrorMessage());
    }

    @Test
    public void testAuthenticateWhenSecondCacheNotAvailable() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn("carbon.super");
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        String cacheKey = GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET");
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        Mockito.when(internalKeyDataCache.get(cacheKey)).thenReturn(null);
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString())).thenReturn(true);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(false);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertTrue(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), 0);
        Assert.assertNull(authenticate.getErrorMessage());
    }

    @Test
    public void testAuthenticateWithExpiredToken() throws Exception {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        Mockito.when(internalKeyCache.get("28f8d7b0-9e62-4341-bf17-094453d5ffa4")).thenReturn("carbon.super");
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        Cache invalidCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidCache);
        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
        jwtTokenPayloadInfo.setPayload(signedJWT.getJWTClaimsSet());
        jwtTokenPayloadInfo.setAccessToken(internalKey);
        String cacheKey = GatewayUtils.getAccessTokenCacheKey("28f8d7b0-9e62-4341-bf17-094453d5ffa4", "/api1/1.0.0",
                "1.0.0", "/resource", "GET");
        JSONObject subscribedAPI = Mockito.mock(JSONObject.class);
        Mockito.when(internalKeyDataCache.get(cacheKey)).thenReturn(jwtTokenPayloadInfo);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(signedJWT.getJWTClaimsSet())).thenReturn(true);
        PowerMockito.when(GatewayUtils.validateAPISubscription("/api1/1.0.0", "1.0.0", signedJWT.getJWTClaimsSet(),
                internalKey.split("\\."), false)).thenReturn(subscribedAPI);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext("28f8d7b0-9e62-4341-bf17-094453d5ffa4",
                signedJWT.getJWTClaimsSet(), subscribedAPI, api.getApiTier())).thenReturn(authenticationContext);
        PowerMockito.doNothing().when(APISecurityUtils.class, "setAuthenticationContext", messageContext,
                authenticationContext);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void testAuthenticateInvalidToken() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/resource");
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        PowerMockito.when(OpenAPIUtils.getResourceThrottlingTier(openAPI, messageContext)).thenReturn("GOLD");
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn("carbon.super");
        Cache internalKeyCache = Mockito.mock(Cache.class);
        Cache invalidInternalKeyCache = Mockito.mock(Cache.class);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(invalidInternalKeyCache);
        Mockito.when(invalidInternalKeyCache.get(Mockito.anyString())).thenReturn(Mockito.anyString());
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void testAuthenticateMissingToken() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertFalse(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertTrue(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }
    @Test
    public void testAuthenticateMissingHeaders() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(null);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertFalse(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertTrue(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void testAuthenticateMissingAPI() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_GENERAL_ERROR);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
    }

    @Test
    public void testAuthenticateWithNo2DotToken() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, "abcdefgrffhfsfdsfdfgfgsfsfsfsfdgszfhsafyau");
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void testAuthenticateWith2DotInvalidToken() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, "abcdefgrffh.fsfdsfdfgfgsfsfsfs.fdgszfhsafyau");
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_GENERAL_ERROR);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
    }

    @Test
    public void testAuthenticateWithInvalidTokenType() {
        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(false);
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Assert.assertEquals(authenticate.getErrorMessage(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void testAuthenticateNoOpenAPIDefinition() {
        InternalAPIKeyAuthenticator internalAPIKeyAuthenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        API api = new API();
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, internalKey);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        AuthenticationResponse authenticate = internalAPIKeyAuthenticator.authenticate(messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertTrue(authenticate.isMandatoryAuthentication());
        Assert.assertFalse(authenticate.isAuthenticated());
        Assert.assertFalse(authenticate.isContinueToNextAuthenticator());
        Assert.assertEquals(authenticate.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF);
        Assert.assertEquals(authenticate.getErrorMessage(),
                APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF_ERROR_MESSAGE);
    }

    private static final String MCP_UPSTREAM_TOKEN_ID = "4b9e0f6a-1c2d-4e5f-8a9b-mcp-upstream";
    private static final String CLIENT_TOKEN = "Bearer eyJ4NXQiOiJjbGllbnQifQ.eyJzdWIiOiJhZG1pbiJ9.Y2xpZW50";
    private static final String BASIC_CREDENTIALS = "Basic YWRtaW46YWRtaW4=";

    // Client token sent with an Internal Key minted by an MCP server (EXISTING_API), at the existing API

    @Test
    public void testMCPClientTokenRemovedAtExistingAPIWhenOutboundAuthHeaderDisabled() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", null), null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    @Test
    public void testMCPClientTokenForwardedAtExistingAPIWhenOutboundAuthHeaderEnabled() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("false", null), null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));

        Assert.assertEquals(CLIENT_TOKEN, headers.get(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    @Test
    public void testMCPClientTokenAtExistingAPIIndependentOfMCPOutboundAuthHeader() throws Exception {
        // Only [apim.oauth_config] enable_outbound_auth_header decides; [apim.mcp] enable_outbound_auth_header = true
        APIManagerConfiguration stripConfig = config("true", null);
        Mockito.when(stripConfig.isMCPOutboundAuthHeaderEnabled()).thenReturn(true);
        Map<String, String> stripped = authenticateWithInternalKey(buildInternalKey(true), new API(),
                stripConfig, null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));
        Assert.assertFalse(stripped.containsKey(HttpHeaders.AUTHORIZATION));

        // [apim.mcp] enable_outbound_auth_header = false
        APIManagerConfiguration forwardConfig = config("false", null);
        Mockito.when(forwardConfig.isMCPOutboundAuthHeaderEnabled()).thenReturn(false);
        Map<String, String> forwarded = authenticateWithInternalKey(buildInternalKey(true), new API(),
                forwardConfig, null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));
        Assert.assertEquals(CLIENT_TOKEN, forwarded.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPClientTokenRemovedWhenOutboundAuthHeaderNotConfigured() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config(null, null), null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPClientTokenRemovedWhenConfigurationUnavailable() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                null, null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPClientTokenRemovalKeepsOtherAuthorizationSegments() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", null), null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN + ", " + BASIC_CREDENTIALS));

        Assert.assertEquals(BASIC_CREDENTIALS, headers.get(HttpHeaders.AUTHORIZATION).trim());
    }

    @Test
    public void testNonBearerAuthorizationHeaderKeptForMCPInternalKey() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", null), null, headers(HttpHeaders.AUTHORIZATION, BASIC_CREDENTIALS));

        Assert.assertEquals(BASIC_CREDENTIALS, headers.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPInternalKeyWithoutAuthorizationHeader() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", null), null, headers());

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    @Test
    public void testMCPClientTokenRemovedFromAPILevelAuthorizationHeader() throws Exception {
        String apiLevelHeader = "X-Custom-Authorization";
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", "X-Configured-Authorization"), apiLevelHeader,
                headers(apiLevelHeader, CLIENT_TOKEN, HttpHeaders.AUTHORIZATION, BASIC_CREDENTIALS));

        Assert.assertFalse(headers.containsKey(apiLevelHeader));
        Assert.assertEquals(BASIC_CREDENTIALS, headers.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPClientTokenRemovedFromConfiguredAuthorizationHeader() throws Exception {
        String configuredHeader = "X-Configured-Authorization";
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(true), new API(),
                config("true", configuredHeader), null,
                headers(configuredHeader, CLIENT_TOKEN, HttpHeaders.AUTHORIZATION, BASIC_CREDENTIALS));

        Assert.assertFalse(headers.containsKey(configuredHeader));
        Assert.assertEquals(BASIC_CREDENTIALS, headers.get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testMCPClientTokenRemovedWhenInternalKeyPayloadIsCached() throws Exception {
        String key = buildInternalKey(true);
        JWTTokenPayloadInfo cachedPayload = new JWTTokenPayloadInfo();
        cachedPayload.setPayload(SignedJWT.parse(key).getJWTClaimsSet());
        cachedPayload.setAccessToken(key);

        // Signature verification fails in this setup, so authentication only succeeds through the cache
        Map<String, String> headers = authenticateWithInternalKey(key, new API(), config("true", null), null,
                headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN), cachedPayload);

        Assert.assertFalse(headers.containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void testAuthorizationHeaderUntouchedForNonMCPInternalKey() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(false), new API(),
                config("true", null), null, headers(HttpHeaders.AUTHORIZATION, CLIENT_TOKEN));

        Assert.assertEquals(CLIENT_TOKEN, headers.get(HttpHeaders.AUTHORIZATION));
    }

    // Internal-Key header forwarding

    @Test
    public void testInternalKeyRemovedForMCPServerProxy() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(false),
                mcpServer(APIConstants.API_SUBTYPE_SERVER_PROXY), config("true", null), null, headers());

        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    @Test
    public void testInternalKeyRemovedForMCPDirectBackend() throws Exception {
        Map<String, String> headers = authenticateWithInternalKey(buildInternalKey(false),
                mcpServer(APIConstants.API_SUBTYPE_DIRECT_BACKEND), config("true", null), null, headers());

        Assert.assertFalse(headers.containsKey(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    @Test
    public void testInternalKeyKeptForMCPExistingAPI() throws Exception {
        String key = buildInternalKey(false);
        Map<String, String> headers = authenticateWithInternalKey(key,
                mcpServer(APIConstants.API_SUBTYPE_EXISTING_API), config("true", null), null, headers());

        Assert.assertEquals(key, headers.get(APIMgtGatewayConstants.INTERNAL_KEY));
    }

    private static API mcpServer(String subtype) {
        API api = new API();
        api.setApiType(APIConstants.API_TYPE_MCP);
        api.setSubtype(subtype);
        return api;
    }

    /**
     * @param removeOAuthHeaders   value of RemoveOAuthHeadersFromOutMessage (null when not configured)
     * @param configuredAuthHeader value of [apim.oauth_config] auth_header (null when not configured)
     */
    private static APIManagerConfiguration config(String removeOAuthHeaders, String configuredAuthHeader) {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn(removeOAuthHeaders);
        Mockito.when(apiManagerConfiguration.getFirstProperty(
                APIConstants.OAUTH_CONFIGS + APIConstants.AUTHORIZATION_HEADER)).thenReturn(configuredAuthHeader);
        return apiManagerConfiguration;
    }

    private static Map<String, String> headers(String... namesAndValues) {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i + 1 < namesAndValues.length; i += 2) {
            headers.put(namesAndValues[i], namesAndValues[i + 1]);
        }
        return headers;
    }

    /**
     * Builds an Internal Key. Signature verification is mocked, so the signature segment is a placeholder.
     *
     * @param mcpAuthenticated whether the key is the one minted by an MCP server for its upstream call
     */
    private String buildInternalKey(boolean mcpAuthenticated) {
        String header = "{\"kid\":\"gateway_certificate_alias\",\"alg\":\"RS256\"}";
        String payload = "{\"sub\":\"admin@carbon.super\",\"keytype\":\"PRODUCTION\",\"token_type\":\"InternalKey\","
                + (mcpAuthenticated ? "\"" + APIMgtGatewayConstants.MCP_AUTH_CLAIM + "\":\"true\"," : "")
                + "\"exp\":" + (System.currentTimeMillis() / 1000 + 600) + ","
                + "\"jti\":\"" + MCP_UPSTREAM_TOKEN_ID + "\"}";
        return base64Url(header) + "." + base64Url(payload) + ".c2lnbmF0dXJl";
    }

    private static String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> authenticateWithInternalKey(String key, API api, APIManagerConfiguration config,
                                                            String apiLevelAuthHeader,
                                                            Map<String, String> requestHeaders) throws Exception {
        return authenticateWithInternalKey(key, api, config, apiLevelAuthHeader, requestHeaders, null);
    }

    /**
     * Authenticates a request carrying the given Internal Key and request headers at an API, asserts that it is
     * authenticated, and returns the transport headers that would be sent to the backend.
     *
     * @param cachedPayload when set, the Internal Key is served from the gateway caches and signature
     *                      verification fails, so only the cached path can authenticate it
     */
    private Map<String, String> authenticateWithInternalKey(String key, API api, APIManagerConfiguration config,
                                                            String apiLevelAuthHeader,
                                                            Map<String, String> requestHeaders,
                                                            JWTTokenPayloadInfo cachedPayload) throws Exception {
        InternalAPIKeyAuthenticator authenticator =
                new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY, apiLevelAuthHeader) {
                    @Override
                    protected APIManagerConfiguration getApiManagerConfiguration() {
                        return config;
                    }
                };

        PowerMockito.when(GatewayUtils.isInternalKey(Mockito.any(JWTClaimsSet.class))).thenReturn(true);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/items/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/items/{itemId}");
        PowerMockito.when(GatewayUtils.getAPI(messageContext)).thenReturn(api);

        Map<String, String> transportHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        transportHeaders.putAll(requestHeaders);
        transportHeaders.put(APIMgtGatewayConstants.INTERNAL_KEY, key);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(transportHeaders);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT)).thenReturn(openAPI);

        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Cache internalKeyCache = Mockito.mock(Cache.class);
        Cache internalKeyDataCache = Mockito.mock(Cache.class);
        if (cachedPayload != null) {
            Mockito.when(internalKeyCache.get(MCP_UPSTREAM_TOKEN_ID))
                    .thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Mockito.when(internalKeyDataCache.get(Mockito.any())).thenReturn(cachedPayload);
        }
        PowerMockito.when(CacheProvider.getGatewayInternalKeyCache()).thenReturn(internalKeyCache);
        PowerMockito.when(CacheProvider.getGatewayInternalKeyDataCache()).thenReturn(internalKeyDataCache);
        PowerMockito.when(CacheProvider.getInvalidGatewayInternalKeyCache()).thenReturn(Mockito.mock(Cache.class));
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(SignedJWT.class), Mockito.anyString()))
                .thenReturn(cachedPayload == null);
        PowerMockito.when(GatewayUtils.isJwtTokenExpired(Mockito.any(JWTClaimsSet.class))).thenReturn(false);
        AuthenticationContext authenticationContext = Mockito.mock(AuthenticationContext.class);
        PowerMockito.when(GatewayUtils.generateAuthenticationContext(Mockito.eq(MCP_UPSTREAM_TOKEN_ID),
                Mockito.any(JWTClaimsSet.class), Mockito.<JSONObject>any(), Mockito.<String>any()))
                .thenReturn(authenticationContext);

        AuthenticationResponse response = authenticator.authenticate(messageContext);
        Assert.assertTrue(response.isAuthenticated());
        return transportHeaders;
    }
}
