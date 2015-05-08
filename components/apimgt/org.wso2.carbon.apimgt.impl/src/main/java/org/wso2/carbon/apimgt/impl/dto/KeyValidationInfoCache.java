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
import java.util.Set;

/**
 * Wraps the Key Info main cache and Index cache providing users a single Cache.
 * Main cache keeps the mapping between combination of Access Token, API Version, Context and KeyValidationInfoDTO.
 * Index cache maintains mapping between Consumer Key and Cache Key for the main cache.
 */
public class KeyValidationInfoCache {


    private String cacheName;
    private String mainCacheName;
    private String apiIndexCacheName;
    private String tokenIndexCacheName;
    private static final Log log = LogFactory.getLog(KeyValidationInfoCache.class);

    public KeyValidationInfoCache(String cacheName) {
        mainCacheName = cacheName + "_main_cache";
        apiIndexCacheName = cacheName + "_api_index_cache";
        tokenIndexCacheName = cacheName + "_token_index";
    }

    public void addToCache(KeyValidationInfoCacheKey cacheKey, APIKeyValidationInfoDTO validationInfo) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        Cache apiIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(apiIndexCacheName);
        Cache tokenIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache
                (tokenIndexCacheName);

        String apiCacheKey = APICacheEntry.createCacheKey(cacheKey.apiContext, cacheKey.apiVersion);
        APICacheEntry apiCacheEntry = (APICacheEntry) apiIndexCache.get(apiCacheKey);

        if (apiCacheEntry == null) {
            apiCacheEntry = new APICacheEntry(cacheKey.apiContext, cacheKey.apiVersion);
        }

        if (validationInfo.getApplicationId() != null) {
            ApplicationCacheEntry applicationCacheEntry = apiCacheEntry.getApplicationCacheEntry(validationInfo
                                                                                                         .getApplicationId());
            if (applicationCacheEntry == null) {
                applicationCacheEntry = new ApplicationCacheEntry(validationInfo.getApplicationId());
            }

            applicationCacheEntry.addCacheKey(cacheKey.cacheKey);

        }

        TokenCacheEntry tokenCacheEntry = (TokenCacheEntry) tokenIndexCache.get(cacheKey.accessToken);

        if (tokenCacheEntry == null) {
            tokenCacheEntry = new TokenCacheEntry(cacheKey.accessToken);
        }

        tokenCacheEntry.addCacheKey(cacheKey.cacheKey);

        tokenIndexCache.put(tokenCacheEntry.getAccessToken(), tokenCacheEntry);
        apiIndexCache.put(apiCacheKey, apiCacheEntry);
        mainCache.put(cacheKey.cacheKey, validationInfo);
    }

    public APIKeyValidationInfoDTO getFromCache(KeyValidationInfoCacheKey cacheKey) {
        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        return (APIKeyValidationInfoDTO) cache.get(cacheKey.toString());
    }

    public void removeFromCache(String accessToken) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        Cache tokenIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache
                (tokenIndexCacheName);
        TokenCacheEntry tokenCacheEntry = (TokenCacheEntry) tokenIndexCache.get(accessToken);
        if (tokenCacheEntry != null) {
            Set<String> cacheKeys = tokenCacheEntry.getCacheKeys();
            mainCache.removeAll(cacheKeys);
            tokenCacheEntry.removeAll();
            tokenIndexCache.remove(tokenCacheEntry.getAccessToken());
            if (log.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Deleted Keys : ");
                for (String cacheKey : cacheKeys) {
                    builder.append(cacheKey + ", ");
                }
                log.debug(builder.toString());
            }
        }
    }

    public void removeFromCache(String context, String version) {
        String cacheKey = APICacheEntry.createCacheKey(context, version);
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);
        Cache apiIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(apiIndexCacheName);

        APICacheEntry apiCacheEntry = (APICacheEntry) apiIndexCache.get(cacheKey);
        if (apiCacheEntry != null) {
            for (ApplicationCacheEntry applicationCacheEntry : apiCacheEntry.getApplicationEntries()) {
                Set<String> cacheKeys = applicationCacheEntry.getCacheKeys();
                if (cacheKeys != null) {
                    mainCache.removeAll(cacheKeys);
                }
            }
            apiIndexCache.remove(apiCacheEntry.getCacheKey());

        }

    }

    public boolean removeFromCache(KeyValidationInfoCacheKey cacheKey) {
        Cache mainCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(mainCacheName);

        removeFromAPIIndex(cacheKey);
        removeFromTokenIndex(cacheKey);

        return mainCache.remove(cacheKey.toString());
    }

    private void removeFromTokenIndex(KeyValidationInfoCacheKey cacheKey) {
        Cache tokenIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache
                (tokenIndexCacheName);
        if (cacheKey.accessToken != null) {
            TokenCacheEntry tokenCacheEntry = (TokenCacheEntry) tokenIndexCache.get(cacheKey.accessToken);
            if (tokenCacheEntry != null) {
                if (tokenCacheEntry.removeCacheKey(cacheKey.cacheKey)) {
                    tokenIndexCache.put(cacheKey.accessToken, tokenCacheEntry);
                }
            }
        }
    }

    private void removeFromAPIIndex(KeyValidationInfoCacheKey cacheKey) {
        Cache apiIndexCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(apiIndexCacheName);
        String apiCacheKey = APICacheEntry.createCacheKey(cacheKey.apiContext, cacheKey.apiVersion);
        APICacheEntry apiCacheEntry = (APICacheEntry) apiIndexCache.get(apiCacheKey);
        if (apiCacheEntry != null) {
            if (cacheKey.applicationId != null) {
                ApplicationCacheEntry applicationCacheEntry = apiCacheEntry.getApplicationCacheEntry(cacheKey
                                                                                                             .applicationId);

                if (applicationCacheEntry != null) {
                    applicationCacheEntry.removeCacheKey(cacheKey.cacheKey);
                    if (applicationCacheEntry.getCacheKeys().size() == 0) {
                        apiCacheEntry.removeApplicationEntry(applicationCacheEntry.getApplicationId());
                        if (apiCacheEntry.isEmpty()) {
                            // If Cache Entry doesn't have any elements. Remove it.
                            apiIndexCache.remove(apiCacheEntry.getCacheKey());
                        } else {
                            // One Application Entry has been removed from APICacheEntry. We need to put the
                            // APICacheEntry to make the changes visible to other nodes.
                            apiIndexCache.put(apiCacheEntry.getCacheKey(), apiCacheEntry);
                        }
                    } else {
                        // A cacheKey has been removed from ApplicationEntry. So we have to put the updated
                        // APICacheEntry.
                        apiIndexCache.put(apiCacheEntry.getCacheKey(), apiCacheEntry);
                    }

                }

            }
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
