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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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

    public void invalidateResourceCache(String apiContext, String apiVersion, String resourceURLContext, String httpVerb) {
        boolean isTenantFlowStarted = false;
        int tenantDomainIndex = apiContext.indexOf("/t/");
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomainIndex != -1) {
            String temp = apiContext.substring(tenantDomainIndex + 3, apiContext.length());
            tenantDomain = temp.substring(0, temp.indexOf("/"));
        }

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            String resourceVerbCacheKey =
                                          APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                                                                             resourceURLContext, httpVerb);

            String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);

            Cache cache =
                          Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                                 .getCache(APIConstants.RESOURCE_CACHE_NAME);

            if (cache.containsKey(apiCacheKey)) {
                cache.remove(apiCacheKey);
            }
            // TODO this code is not needed now, can remove
            /*
             * if (keyCache.size() != 0) {
             * Set keys = keyCache.keySet();
             * for (Object cacheKey : keys) {
             * String key = cacheKey.toString();
             * if (key.contains(apiContext + ":" + apiVersion)) {
             * keyCache.remove(key);
             * }
             * }
             * }
             */

            if (cache.containsKey(resourceVerbCacheKey)) {
                cache.remove(resourceVerbCacheKey);
            }
            // TODO this code is not needed now, can remove
            /*
             * if (cache.size() != 0) {
             * if (resourceURLContext.equals("/")) {
             * Set keys = cache.keySet();
             * for (Object cacheKey : keys) {
             * String key = cacheKey.toString();
             * if (key.contains(apiContext + "/" + apiVersion +
             * resourceURLContext)) {
             * cache.remove(key);
             * }
             * }
             * } else if (cache.get(resourceCacheKey) != null) {
             * cache.remove(resourceVerbCacheKey);
             * }
             * 
             * cache.remove(resourceCacheKey);
             * }
             */

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

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
