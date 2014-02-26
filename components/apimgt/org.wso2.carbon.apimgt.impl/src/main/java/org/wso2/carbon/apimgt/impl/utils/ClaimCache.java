/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.Cache;
import javax.cache.Caching;

public class ClaimCache {
    //TODO refactor caching implementation

    private static final String CLAIM_CACHE_NAME = "ClaimCache";

    private static final ClaimCache instance = new ClaimCache(CLAIM_CACHE_NAME);

    private ClaimCache(String cacheName) {

    }

    public static ClaimCache getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }


    public void addToCache(ClaimCacheKey key, UserClaims entry) {
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(CLAIM_CACHE_NAME).put(key,entry);
    }


    public Cache.Entry getValueFromCache(ClaimCacheKey key) {
        return (Cache.Entry)Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(CLAIM_CACHE_NAME).get(key);
    }


    public void clearCacheEntry(ClaimCacheKey key) {
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(CLAIM_CACHE_NAME).removeAll();
    }
}
