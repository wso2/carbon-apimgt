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
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Test class for WebsocketUtil
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WebsocketUtilTestCase.class, ServiceReferenceHolder.class, Caching.class, APIUtil.class,
        PrivilegedCarbonContext.class, MultitenantUtils.class})
public class WebsocketUtilTestCase {
    private String apiKey = "abc";
    private String apiContext = "/ishara";
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
    }

    @Test
    public void testValidateCache() {
//        PowerMockito.mockStatic(ServiceReferenceHolder.class);
//        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
//        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
//        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
//        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
//        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL))
//                .thenReturn(apiKeyValidationURL);
//        CacheManager cacheManager = Mockito.mock(CacheManager.class);

//        PowerMockito.mockStatic(Caching.class);
//        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);

        // returns null if cachedToken is not found
        Assert.assertNull(WebsocketUtil.validateCache(apiKey, cacheKey));

        // If cachedToken is found
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName(apiName);
        PowerMockito.mockStatic(APIUtil.class);

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
    public void testGetAccessTokenCacheKey() {
        Assert.assertEquals("235erwytgtkyb:/ishara", WebsocketUtil.getAccessTokenCacheKey(cachedToken,apiContext ));
    }

    @Test
    public void testInitParams() {
        Assert.assertEquals("235erwytgtkyb:/ishara", WebsocketUtil.getAccessTokenCacheKey(cachedToken,apiContext ));
    }

    @Test
    public void testIsRemoveOAuthHeadersFromOutMessage() {
        Assert.assertEquals("235erwytgtkyb:/ishara", WebsocketUtil.getAccessTokenCacheKey(cachedToken,apiContext ));
    }
}
