/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.impl.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.cache.Cache;

import javax.cache.Caching;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Wraps the Key Info main cache and Index cache providing users a single Cache.
 * Main cache keeps the mapping between combination of Access Token, API Version, Context and KeyValidationInfoDTO.
 * Index cache maintains mapping between Consumer Key and Cache Key for the main cache.
 */
public class KeyValidationInfoCache {


    private String mainCacheName;
    private static final Log log = LogFactory.getLog(KeyValidationInfoCache.class);

    public KeyValidationInfoCache(String cacheName) {
        mainCacheName = cacheName;
    }

    public void addToCache(KeyValidationInfoCacheKey cacheKey, APIKeyValidationInfoDTO validationInfo) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        mainCache.put(cacheKey.cacheKey, validationInfo);
    }

    public APIKeyValidationInfoDTO getFromCache(KeyValidationInfoCacheKey cacheKey) {
        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        return (APIKeyValidationInfoDTO) cache.get(cacheKey.cacheKey);
    }


    public boolean removeFromCache(KeyValidationInfoCacheKey cacheKey) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        return mainCache.remove(cacheKey.cacheKey);
    }

    public Collection<? extends String> getCacheKeys() {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        Iterator keyIterator = mainCache.keys();
        Set<String> set = new HashSet<String>();
        while (keyIterator.hasNext()) {
            set.add((String) keyIterator.next());
        }
        return set;
    }

    public void removeFromCache(String cacheKey) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        boolean state = mainCache.remove(cacheKey);
        if (log.isDebugEnabled()) {
            log.debug("Removed entry for " + cacheKey + "from cache , state : " + state);
        }
    }

    public class KeyValidationInfoCacheKey {
        private String accessToken;
        private String apiContext;
        private String apiVersion;
        private String matchingResource;
        private String httpVerb;
        private String authLevel;
        private String cacheKey;
        private String applicationId;

        public KeyValidationInfoCacheKey(String accessToken, String apiContext, String apiVersion,
                                         String matchingResource, String httpVerb, String authLevel) {
            this.accessToken = accessToken;
            this.apiContext = apiContext;
            this.apiVersion = apiVersion;
            this.matchingResource = matchingResource;
            this.httpVerb = httpVerb;
            this.authLevel = authLevel;
            cacheKey = APIUtil.getAccessTokenCacheKey(accessToken, apiContext, apiVersion, matchingResource,
                                                      httpVerb, authLevel);
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        @Override
        public String toString() {
            return cacheKey;
        }
    }
}
