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



import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.Iterator;
import java.util.Set;

public class APIAuthenticationService extends AbstractServiceBusAdmin {

    public void invalidateKeys(APIKeyMapping[] mappings) {

        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.KEY_CACHE_NAME);
        for (APIKeyMapping mapping : mappings) {
            String cacheKey = mapping.getKey() + ":" + mapping.getContext() + ":" + mapping.getApiVersion();
            if(cache.containsKey(cacheKey)){
                cache.remove(cacheKey);
            }
            //TODO Review and fix
           /* Set keys = cache.keySet();
            for (Object cKey : keys) {
                String key = cKey.toString();
                if (key.contains(cacheKey)) {
                    cache.remove(key);

                }
            }*/
        }
    }

    public void invalidateOAuthKeys(String consumerKey, String authorizedUser) {
        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.KEY_CACHE_NAME);
        String cacheKey = consumerKey + ":" + authorizedUser;
        cache.remove(cacheKey);

    }

    public void invalidateResourceCache(String apiContext, String apiVersion,
                                        String resourceURLContext, String httpVerb) {
        String resourceVerbCacheKey = apiContext + "/" + apiVersion +
                                      resourceURLContext + ":" + httpVerb;
        String resourceCacheKey = apiContext + ":" + apiVersion;
        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.RESOURCE_CACHE_NAME);
        Cache keyCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.KEY_CACHE_NAME);

        if(keyCache.containsKey(apiContext + ":" + apiVersion))  {
            keyCache.remove(apiContext + ":" + apiVersion);
        }
        //TODO Review and fix
       /* if (keyCache.size() != 0) {
            Set keys = keyCache.keySet();
            for (Object cacheKey : keys) {
                String key = cacheKey.toString();
                if (key.contains(apiContext + ":" + apiVersion)) {
                    keyCache.remove(key);
                }
            }
        }*/


        if(keyCache.containsKey(apiContext + "/" + apiVersion + resourceURLContext))  {
            keyCache.remove(apiContext + "/" + apiVersion + resourceURLContext);
        }
        //TODO Review and fix
        /*if (cache.size() != 0) {
            if (resourceURLContext.equals("/")) {
                Set keys = cache.keySet();
                for (Object cacheKey : keys) {
                    String key = cacheKey.toString();
                    if (key.contains(apiContext + "/" + apiVersion + resourceURLContext)) {
                        cache.remove(key);
                    }
                }
            } else if (cache.get(resourceCacheKey) != null) {
                cache.remove(resourceVerbCacheKey);
            }

            cache.remove(resourceCacheKey);
        } */

    }

    /**
     * This method is to invalidate an access token which is already in gateway cache.
     *
     * @param accessToken The access token to be remove from the cache
     */
    public void invalidateKey(String accessToken) {
        //TODO Review and fix
        Cache keyCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.KEY_CACHE_NAME);
        keyCache.remove(accessToken);
        Iterator<Object> iterator = keyCache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry cacheEntry = (javax.cache.Cache.Entry) iterator.next();
            String cacheAccessKey = cacheEntry.getKey().toString().split(":")[0];
            if (cacheAccessKey.equals(accessToken)) {
                keyCache.remove(cacheEntry.getKey());
            }
        }

        /*Cache cache = PrivilegedCarbonContext.getCurrentContext(getAxisConfig()).getCache("keyCache");
        for (int i = 0; i < cache.keySet().size(); i++) {
            String cacheAccessKey = cache.keySet().toArray()[i].toString().split(":")[0];
            if (cacheAccessKey.equals(accessToken)) {
                cache.remove(cache.keySet().toArray()[i]);
            }

        } */
    }
}
