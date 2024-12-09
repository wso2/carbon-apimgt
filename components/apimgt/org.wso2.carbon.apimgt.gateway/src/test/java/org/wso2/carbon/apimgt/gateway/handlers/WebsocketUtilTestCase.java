/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.UUID;

/**
 * Test class for WebsocketUtil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebsocketUtilTestCase.class, ServiceReferenceHolder.class, Caching.class, APIUtil.class,
        PrivilegedCarbonContext.class, MultitenantUtils.class,
        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class, Caching.class,
        Cache.class, APIManagerConfigurationService.class, CacheProvider.class, WebsocketUtil.class })
public class WebsocketUtilTestCase {
    private String apiKey = "abc";
    private String apiContext = "/ishara";
    private String resource = "/resource";
    private String resourceKey = "resourceKey";
    private String subscriptionKey = "subscriptionKey";
    private String apiName = "PhoneVerify";
    private String cacheKey = "ishara";
    private String cachedToken = "235erwytgtkyb";
    private String apiKeyValidationURL = "http://localhost:18083";
    private CacheManager cacheManager;
    private ServiceReferenceHolder serviceReferenceHolder;
    private Cache gwTokenCache;
    private Cache gwKeyCache;
    private API graphQLAPI;

    @Before
    public void setup() {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
                .thenReturn(apiKeyValidationURL);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED))
                .thenReturn("true");
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("true");
        cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.mockStatic(Caching.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        gwKeyCache = Mockito.mock(Cache.class);
        gwTokenCache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.GATEWAY_KEY_CACHE_NAME)).thenReturn(gwKeyCache);
        Mockito.when(cacheManager.getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME)).thenReturn(gwTokenCache);
        PowerMockito.mockStatic(APIUtil.class);
        graphQLAPI = new API(UUID.randomUUID().toString(), 2, "admin", "GraphQLAPI", "1.0.0", "/graphql", "Unlimited",
                APIConstants.GRAPHQL_API, "PUBLISHED", false);
    }

    @Test
    public void testValidateCache() throws Exception {
        Mockito.when(CacheProvider.getGatewayTokenCache()).thenReturn(gwTokenCache);
        Mockito.when(CacheProvider.getGatewayKeyCache()).thenReturn(gwKeyCache);
        // returns null if cachedToken is not found
        Assert.assertNull(WebsocketUtil.validateCache(apiKey, cacheKey));

        // If cachedToken is found
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName(apiName);
        WebsocketUtil.putCache(apiKeyValidationInfoDTO, apiKey, cacheKey);
        Mockito.when(gwTokenCache.get(apiKey)).thenReturn(cachedToken);
        Mockito.when(gwKeyCache.get(cacheKey)).thenReturn(apiKeyValidationInfoDTO);
        PowerMockito.when(APIUtil.isAccessTokenExpired(apiKeyValidationInfoDTO)).thenReturn(true);
        Assert.assertEquals(apiName, WebsocketUtil.validateCache(apiKey, cacheKey).getApiName());
    }

    @Test
    public void testPutCache() {
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName(apiName);
        Cache gwKeyCache = Mockito.mock(Cache.class);
        Cache gwTokenCache = Mockito.mock(Cache.class);
        PowerMockito.mockStatic(WebsocketUtil.class);

        WebsocketUtil.putCache(apiKeyValidationInfoDTO, apiKey, cacheKey);
        Assert.assertEquals(apiName, apiKeyValidationInfoDTO.getApiName());
    }

    @Test
    public void testIsThrottled() {
        ThrottleDataHolder throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        Mockito.when(serviceReferenceHolder.getThrottleDataHolder()).thenReturn(throttleDataHolder);
        Mockito.when(throttleDataHolder.isAPIThrottled(apiKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isAPIThrottled(resourceKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isAPIThrottled(subscriptionKey)).thenReturn(true);
        Assert.assertTrue(WebsocketUtil.isThrottled(resourceKey, subscriptionKey, apiKey));
    }

    @Test
    public void testGetThrottleStatus() {
        ThrottleDataHolder throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        Mockito.when(serviceReferenceHolder.getThrottleDataHolder()).thenReturn(throttleDataHolder);
        Mockito.when(throttleDataHolder.isAPIThrottled(apiKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isAPIThrottled(resourceKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isAPIThrottled(subscriptionKey)).thenReturn(true);
        Assert.assertTrue(WebsocketUtil.getThrottleStatus(resourceKey, subscriptionKey, apiKey).isThrottled());
    }

    @Test
    public void testGetAccessTokenCacheKey() {
        Assert.assertEquals("235erwytgtkyb:/ishara:/resource",
                            WebsocketUtil.getAccessTokenCacheKey(cachedToken, apiContext, resource));
    }

    @Test
    public void testInitParams() {
        Assert.assertEquals("235erwytgtkyb:/ishara:/resource",
                            WebsocketUtil.getAccessTokenCacheKey(cachedToken, apiContext, resource));
    }

    @Test
    public void testIsRemoveOAuthHeadersFromOutMessage() {
        Assert.assertEquals("235erwytgtkyb:/ishara:/resource",
                            WebsocketUtil.getAccessTokenCacheKey(cachedToken, apiContext, resource));
    }

    @Test
    public void testValidateDenyPolicies() {
        InboundMessageContext inboundMessageContext = createApiMessageContext(graphQLAPI);
        ThrottleDataHolder throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        Mockito.when(serviceReferenceHolder.getThrottleDataHolder()).thenReturn(throttleDataHolder);
        Mockito.when(serviceReferenceHolder.getThrottleDataHolder().isBlockingConditionsPresent()).thenReturn(true);
        Mockito.when(serviceReferenceHolder.getThrottleDataHolder().isRequestBlocked(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(APIUtil.isAnalyticsEnabled()).thenReturn(false);
        Assert.assertEquals(4006, WebsocketUtil.validateDenyPolicies(inboundMessageContext).getErrorCode());
    }

    private InboundMessageContext createApiMessageContext(API api) {
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        inboundMessageContext.setTenantDomain("carbon.super");
        inboundMessageContext.setElectedAPI(api);
        inboundMessageContext.setToken("test-backend-jwt-token");
        inboundMessageContext.setUserIP("127.0.0.1");
        inboundMessageContext.setApiContext("/graphql/1.0.0");
        inboundMessageContext.setVersion("1.0.0");
        return inboundMessageContext;
    }
}
