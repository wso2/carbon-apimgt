/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, Caching.class})
public class ClaimCacheTest {

    private Cache cache = Mockito.mock(Cache.class);

    @Before
    public void setup() {
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.mockStatic(Caching.class);
        CacheManager cacheManager = Mockito.mock(CacheManager.class);
        PowerMockito.when(Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)).thenReturn(cacheManager);
        Mockito.when(cacheManager.getCache("ClaimCache")).thenReturn(cache);
    }

    @Test
    public void testAddToCache() {
        ClaimCache claimCache = ClaimCache.getInstance();
        ClaimCacheKey claimCacheKey = new ClaimCacheKey("cache1");
        UserClaims userCaims = Mockito.mock(UserClaims.class);
        claimCache.addToCache(claimCacheKey, userCaims);
        Mockito.verify(cache, Mockito.times(1)).put(claimCacheKey, userCaims);
    }

    @Test
    public void testGetFromCacheWhenCacheKeyNotExists() {
        ClaimCache claimCache = ClaimCache.getInstance();
        ClaimCacheKey claimCacheKey = new ClaimCacheKey("cache1");
        Assert.assertNull(claimCache.getValueFromCache(claimCacheKey));
    }

    @Test
    public void testGetFromCacheWhenKeyExistsInCache() {
        ClaimCache claimCache = ClaimCache.getInstance();
        ClaimCacheKey claimCacheKey = new ClaimCacheKey("cache1");
        UserClaims userCaims = Mockito.mock(UserClaims.class);
        claimCache.addToCache(claimCacheKey, userCaims);
        Mockito.when(cache.get(claimCacheKey)).thenReturn(Mockito.mock(Cache.Entry.class));
        Assert.assertNotNull(claimCache.getValueFromCache(claimCacheKey));
    }

    @Test
    public void testClearCacheEntry() {
        ClaimCache claimCache = ClaimCache.getInstance();
        ClaimCacheKey claimCacheKey = new ClaimCacheKey("cache1");
        claimCache.clearCacheEntry(claimCacheKey);
        Mockito.verify(cache, Mockito.times(1)).removeAll();
    }
}


