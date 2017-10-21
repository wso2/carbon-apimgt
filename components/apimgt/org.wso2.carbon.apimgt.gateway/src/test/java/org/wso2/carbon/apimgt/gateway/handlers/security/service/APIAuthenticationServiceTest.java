/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.service;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.caching.impl.CacheEntry;

import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;
import javax.cache.CacheManager;


public class APIAuthenticationServiceTest {
    @Test
    public void invalidateKeys() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.GATEWAY_KEY_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceWrapper(cacheManager);
        APIKeyMapping[] apiKeyMappings = new APIKeyMapping[1];
        APIKeyMapping apiKeyMapping = new APIKeyMapping();
        apiKeyMapping.setApiVersion("1.0.0");
        apiKeyMapping.setContext("/api1");
        apiKeyMapping.setKey("abcde");
        apiKeyMapping.setCacheKey("/api1:1.0.0");
        apiKeyMappings[0] = apiKeyMapping;
        apiAuthenticationService.invalidateKeys(apiKeyMappings);
    }

    @Test
    public void invalidateOAuthKeys() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.KEY_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceWrapper(cacheManager);
        apiAuthenticationService.invalidateOAuthKeys("abcd-efg", "admin");
    }

    @Test
    public void invalidateResourceCache() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cache.containsKey("/api1/1.0.0")).thenReturn(true);
        Mockito.when(cache.containsKey("/api1/1.0.0/*:GET")).thenReturn(true);
        Mockito.when(cacheManager.getCache(APIConstants.RESOURCE_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceWrapper(cacheManager);
        apiAuthenticationService.invalidateResourceCache("/api1","1.0.0","/*","GET");
    }
    @Test
    public void invalidateResourceCacheInTenant() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        Mockito.when(cacheManager.getCache(APIConstants.RESOURCE_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceTenantWrapper(cacheManager);
        apiAuthenticationService.invalidateResourceCache("/t/wso2.com/api1","1.0.0","/*","GET");
    }

    @Test
    public void invalidateKey() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        List<Cache.Entry> cacheEntryList = new ArrayList<>();
        cacheEntryList.add(new CacheEntry("",""));
        cacheEntryList.add(new CacheEntry("abcde-efgh",""));
        Mockito.when(cache.iterator()).thenReturn(cacheEntryList.iterator());
        Mockito.when(cacheManager.getCache(APIConstants.KEY_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceWrapper(cacheManager);
        apiAuthenticationService.invalidateKey("abcde-efgh");
    }

    @Test
    public void invalidateCachedTokens() throws Exception {
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        Cache cache = Mockito.mock(Cache.class);
        List<Cache.Entry> cacheEntryList = new ArrayList<>();
        Cache.Entry superTenantEntry= new CacheEntry("cdefg-hijk","carbon.super");
        Cache.Entry tenantEntry = new CacheEntry("abcde-efgh","wso2.com");
        Cache.Entry tenantEntry2 = new CacheEntry("abcde-efghi","wso2.com");
        cacheEntryList.add(superTenantEntry);
        cacheEntryList.add(tenantEntry);
        cacheEntryList.add(tenantEntry2);
        Mockito.when(cache.iterator()).thenReturn(cacheEntryList.iterator());
        Mockito.when(cache.containsKey("cdefg-hijk")).thenReturn(true);
        Mockito.when(cache.get("cdefg-hijk")).thenReturn("carbon.super");
        Mockito.when(cache.get("abcde-efgh")).thenReturn("wso2.com");
        Mockito.when(cache.get("abcde-efghi")).thenReturn("wso2.com");
        Mockito.when(cacheManager.getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME)).thenReturn(cache);
        APIAuthenticationService apiAuthenticationService = new ApiAuthenticationServiceWrapper(cacheManager);
        String[] tokens = new String[]{"abcde-efgh","cdefg-hijk","abcde-efghi"};
        apiAuthenticationService.invalidateCachedTokens(tokens);
    }
}