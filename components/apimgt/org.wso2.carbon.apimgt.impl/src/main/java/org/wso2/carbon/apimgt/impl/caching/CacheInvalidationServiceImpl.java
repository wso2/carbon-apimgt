/*
 *
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.ResourceCacheInvalidationDto;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class CacheInvalidationServiceImpl implements CacheInvalidationService {

    private static final Log log = LogFactory.getLog(CacheInvalidationServiceImpl.class);

    public void invalidateResourceCache(String apiContext, String apiVersion, String resourceURLContext,
                                        String httpVerb) {

        ResourceCacheInvalidationDto uriTemplate = new ResourceCacheInvalidationDto();
        uriTemplate.setResourceURLContext(resourceURLContext);
        uriTemplate.setHttpVerb(httpVerb);
        invalidateResourceCache(apiContext, apiVersion, new ResourceCacheInvalidationDto[]{uriTemplate});
    }

    @Override
    public void invalidateResourceCache(String apiContext, String apiVersion,
                                        ResourceCacheInvalidationDto[] uriTemplates) {

        boolean isTenantFlowStarted = false;
        int tenantDomainIndex = apiContext.indexOf("/t/");
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantDomainIndex != -1) {
            String temp = apiContext.substring(tenantDomainIndex + 3, apiContext.length());
            tenantDomain = temp.substring(0, temp.indexOf('/'));
        }

        try {
            isTenantFlowStarted = startTenantFlow(tenantDomain);
            Cache cache = CacheProvider.getResourceCache();
            if (apiContext.contains(APIConstants.POLICY_CACHE_CONTEXT)) {
                if (log.isDebugEnabled()) {
                    log.debug("Cleaning cache for policy update for tenant " + tenantDomain);
                }
                cache.removeAll();
            } else {
                String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);
                if (cache.containsKey(apiCacheKey)) {
                    cache.remove(apiCacheKey);
                }
                for (ResourceCacheInvalidationDto uriTemplate : uriTemplates) {
                    String resourceVerbCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                            uriTemplate.getResourceURLContext(), uriTemplate.getHttpVerb());
                    if (cache.containsKey(resourceVerbCacheKey)) {
                        cache.remove(resourceVerbCacheKey);
                    }
                }
            }

        } finally {
            if (isTenantFlowStarted) {
                endTenantFlow();
            }
        }
    }

    @Override
    public void invalidateCachedUsernames(String[] usernameList) {

        if (usernameList == null || usernameList.length == 0) {
            log.debug("No username received to invalidate Gateway Username Cache.");
            return;
        }
        Map<String, Set<String>> tenantDomainMap = new HashMap<>();
        for (String username : usernameList) {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            Set<String> usersInTenant = tenantDomainMap.get(tenantDomain);
            if (usersInTenant == null) {
                usersInTenant = new HashSet<>();
            }
            usersInTenant.add(username);
            tenantDomainMap.put(tenantDomain, usersInTenant);
        }

        for (Map.Entry<String, Set<String>> tenantEntry : tenantDomainMap.entrySet()) {
            boolean startTenantFlow = false;
            try {
                startTenantFlow = startTenantFlow(tenantEntry.getKey());
                Cache gatewayUsernameCache = CacheProvider.getGatewayUsernameCache();
                Cache gatewayInvalidUsernameCache = CacheProvider.getInvalidUsernameCache();
                for (String username : tenantEntry.getValue()) {
                    if (gatewayUsernameCache != null) {
                        gatewayUsernameCache.remove(username);
                    }
                    if (gatewayInvalidUsernameCache != null) {
                        BasicAuthValidationInfoDTO basicAuthValidationInfoDTO = new BasicAuthValidationInfoDTO();
                        basicAuthValidationInfoDTO.setAuthenticated(false);
                        basicAuthValidationInfoDTO.setDomainQualifiedUsername(username);
                        gatewayInvalidUsernameCache.put(username, basicAuthValidationInfoDTO);
                    }
                }
            } finally {
                if (startTenantFlow) {
                    endTenantFlow();
                }
            }
        }
    }

    @Override
    public void invalidateCachedTokens(String[] accessTokens) {

        //Return if no elements to remove from cache
        if (accessTokens == null || accessTokens.length == 0) {
            log.debug("No access tokens received to invalidate Gateway Token Cache.");
            return;
        }
        Cache gatewayCache;
        boolean isSuperTenantFlowStarted = false;
        Map<String, String> cachedObjects = new HashMap<String, String>();
        // Removing from first Level gateway Cache and add it to invalid token Cache
        try {
            isSuperTenantFlowStarted = startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            gatewayCache = CacheProvider.getGatewayTokenCache();
            Cache invalidGatewayCache = CacheProvider.getInvalidTokenCache();
            for (String accessToken : accessTokens) {
                Object cacheEntry = gatewayCache.get(accessToken);
                if (cacheEntry != null) {
                    //cachePut(accessToken, tenantDomain)
                    cachedObjects.put(accessToken, cacheEntry.toString());
                    gatewayCache.remove(accessToken);
                    invalidGatewayCache.put(accessToken, cacheEntry.toString());
                }
            }
        } finally {
            if (isSuperTenantFlowStarted) {
                endTenantFlow();
            }
        }

        //Return if no caches found
        if (cachedObjects.isEmpty()) {
            log.debug("No objects found in the super tenant token cache to invalidate.");
            return;
        }

        //The map that groups access tokens by their respective tenant domains.
        Map<String, Set<String>> tenantMap = new ConcurrentHashMap<String, Set<String>>();

        //Iterate through the set of cached objects to group the cache by tenant domain

        for (Map.Entry<String, String> tokenObj : cachedObjects.entrySet()) {
            String token = tokenObj.getKey();

            //Get the tenant domain of the access token
            String tenantDomain = tokenObj.getValue();

            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                //Continue to next since we won't have to delete cache entry from the tenant cache.
                continue;
            }

            //If the tenant domain exists in the map
            if (tenantMap.get(tenantDomain) != null) {
                //Add to the tenant's token list
                tenantMap.get(tenantDomain).add(token);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Found token(s) of tenant " + tenantDomain + " to clear from cache");
                }
                //Add the tenant and his token list to the map.
                Set<String> tokensOfDomain = new HashSet<String>();
                tokensOfDomain.add(token);
                tenantMap.put(tenantDomain, tokensOfDomain);
            }
        }

        //For each each tenant
        for (String tenantDomain : tenantMap.keySet()) {
            try {
                startTenantFlow(tenantDomain);
                if (log.isDebugEnabled()) {
                    log.debug("About to delete " + tenantMap.get(tenantDomain).size() + " tokens from tenant " +
                            tenantDomain + "'s cache");
                }

                Cache tenantGatewayCache = CacheProvider.getGatewayTokenCache();
                Cache invalidtTenantGatewayCache = CacheProvider.getInvalidTokenCache();

                //Remove all cached tokens from the tenant's cache
                //Note: Best solution would have been to use the removeAll method of the cache. But it currently throws
                //an NPE if at least one key in the list doesn't exist in the cache.
                for (String accessToken : tenantMap.get(tenantDomain)) {
                    tenantGatewayCache.remove(accessToken);
                    invalidtTenantGatewayCache.put(accessToken, tenantDomain);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Removed all cached tokens of " + tenantDomain + " from cache");
                }
            } finally {
                endTenantFlow();
            }
        }

    }

    public void invalidateResourceCache(String context, String version, String organization,
                                        List<URLMapping> urlMappings) {

        boolean isTenantFlowStarted = false;
        try {
            isTenantFlowStarted = startTenantFlow(organization);
            Cache cache = CacheProvider.getResourceCache();
            String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(context, version);
            if (cache.containsKey(apiCacheKey)) {
                cache.remove(apiCacheKey);
            }
            for (URLMapping uriTemplate : urlMappings) {
                String resourceVerbCacheKey =
                        APIUtil.getResourceInfoDTOCacheKey(context, version, uriTemplate.getUrlPattern(),
                                uriTemplate.getHttpMethod());
                if (cache.containsKey(resourceVerbCacheKey)) {
                    cache.remove(resourceVerbCacheKey);
                }
            }

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

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        return true;
    }

    protected CacheManager getCacheManager() {

        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER);
    }

}
