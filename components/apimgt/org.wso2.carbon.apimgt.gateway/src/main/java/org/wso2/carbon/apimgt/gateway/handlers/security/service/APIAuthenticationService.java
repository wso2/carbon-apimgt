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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class APIAuthenticationService extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(APIAuthenticationService.class);

    public void invalidateKeys(APIKeyMapping[] mappings) {

        //Previously we were clearing API key manager side cache. But actually this service deployed at gateway side.
        //Hence we will get cache from gateway cache
        Cache gatewayCache =  getCacheManager().getCache(APIConstants.GATEWAY_KEY_CACHE_NAME);
        for (APIKeyMapping mapping : mappings) {
            //According to new cache design we will use cache key to clear cache if its available in mapping
            //Later we construct key using attributes. Now cache key will pass as key
            String cacheKey = mapping.getKey();
            if(cacheKey!=null){
                gatewayCache.remove(cacheKey);
            }
        }
    }

    public void invalidateOAuthKeys(String consumerKey, String authorizedUser) {
        Cache cache = getCacheManager().getCache(APIConstants.KEY_CACHE_NAME);
        String cacheKey = consumerKey + ':' + authorizedUser;
        cache.remove(cacheKey);

    }

    public void invalidateResourceCache(String apiContext, String apiVersion, String resourceURLContext, String httpVerb) {
        boolean isTenantFlowStarted = false;
        int tenantDomainIndex = apiContext.indexOf("/t/");
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomainIndex != -1) {
            String temp = apiContext.substring(tenantDomainIndex + 3, apiContext.length());
            tenantDomain = temp.substring(0, temp.indexOf('/'));
        }

        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = startTenantFlow(tenantDomain);
            }

            String resourceVerbCacheKey =
                                          APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                                                                             resourceURLContext, httpVerb);

            String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);

            Cache cache =
                          getCacheManager()
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
                endTenantFlow();
            }
        }

    }

    protected void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    protected boolean startTenantFlow(String tenantDomain) {
        boolean isTenantFlowStarted;
        isTenantFlowStarted = true;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        return isTenantFlowStarted;
    }

    /**
     * This method is to invalidate an access token which is already in gateway cache.
     *
     * @param accessToken The access token to be remove from the cache
     */
    public void invalidateKey(String accessToken) {
        //TODO Review and fix
        Cache keyCache = getCacheManager().getCache(APIConstants.KEY_CACHE_NAME);
        keyCache.remove(accessToken);
        Iterator<Object> iterator = keyCache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry cacheEntry = (javax.cache.Cache.Entry) iterator.next();
            String cacheAccessKey = cacheEntry.getKey().toString().split(":")[0];
            if (cacheAccessKey.equals(accessToken)) {
                keyCache.remove(cacheEntry.getKey());
            }
        }
    }

    /**
     * Invalidate the cached access tokens from the API Gateway. This method is supposed to be used when you have to
     * remove a bulk of access tokens from the cache. For example, a removal of a subscription would require the
     * application's active access tokens to be removed from the cache. These access tokens can reside in different
     * tenant caches since the subscription owning tenant and API owning tenant can be different.
     * @param accessTokens String[] - The access tokens to be cleared from the cache.
     */
    public void invalidateCachedTokens(String[] accessTokens){

        //Return if no elements to remove from cache
        if(accessTokens == null || accessTokens.length == 0){
                log.debug("No access tokens received to invalidate Gateway Token Cache.");
            return;
        }

        Cache gatewayCache = getCacheManager().
                getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME);

        Map<String, String> cachedObjects = new HashMap<String, String>();
        for(String accessToken : accessTokens){
            Object cacheEntry = gatewayCache.get(accessToken);
            if(cacheEntry != null){
                //cachePut(accessToken, tenantDomain)
                cachedObjects.put(accessToken, cacheEntry.toString());
            }
        }

        //Return if no caches found
        if(cachedObjects.isEmpty()){
                log.debug("No objects found in the super tenant token cache to invalidate.");
            return;
        }

        //The map that groups access tokens by their respective tenant domains.
        Map<String, Set<String>> tenantMap = new ConcurrentHashMap<String, Set<String>>();

        //Iterate through the set of cached objects to group the cache by tenant domain
        for(Object tokenObj : cachedObjects.keySet()){
            String token = tokenObj.toString();

            //Get the tenant domain of the access token
            String tenantDomain = cachedObjects.get(token);

            if(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                //Continue to next since we won't have to delete cache entry from the tenant cache.
                continue;
            }

            //If the tenant domain exists in the map
            if(tenantMap.get(tenantDomain) != null){
                //Add to the tenant's token list
                tenantMap.get(tenantDomain).add(token);
            }
            else{
                if(log.isDebugEnabled()){
                    log.debug("Found token(s) of tenant " + tenantDomain + " to clear from cache");
                }
                //Add the tenant and his token list to the map.
                Set<String> tokensOfDomain = new HashSet<String>();
                tokensOfDomain.add(token);
                tenantMap.put(tenantDomain, tokensOfDomain);
            }
        }

        //Remove all tokens from the super tenant cache.
        getCacheManager().
                getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).removeAll(cachedObjects.keySet());

        //For each each tenant
        for(String tenantDomain : tenantMap.keySet()){
            try{
                startTenantFlow(tenantDomain);
                if(log.isDebugEnabled()){
                    log.debug("About to delete " + tenantMap.get(tenantDomain).size() + " tokens from tenant " +
                                tenantDomain + "'s cache");
                }

                Cache tenantGatewayCache = getCacheManager().
                        getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME);

                //Remove all cached tokens from the tenant's cache
                //Note: Best solution would have been to use the removeAll method of the cache. But it currently throws
                //an NPE if at least one key in the list doesn't exist in the cache.
                for(String accessToken : tenantMap.get(tenantDomain)){
                    tenantGatewayCache.remove(accessToken);
                }

                if(log.isDebugEnabled()){
                    log.debug("Removed all cached tokens of " + tenantDomain + " from cache");
                }
            }finally{
                endTenantFlow();
            }
        }
    }

    protected CacheManager getCacheManager() {
        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER);
    }
}
