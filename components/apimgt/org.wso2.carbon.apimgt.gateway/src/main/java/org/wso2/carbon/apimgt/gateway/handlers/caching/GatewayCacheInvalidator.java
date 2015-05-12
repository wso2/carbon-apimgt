/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.common.GatewayKeyInfoCache;
import org.wso2.carbon.apimgt.gateway.handlers.security.service.APIKeyMapping;
import org.wso2.carbon.apimgt.impl.dto.APICacheEntry;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Runs at Gateway, clearing Cache entries associated with APIs and Tokens.
 */
public class GatewayCacheInvalidator {

    private static final Log log = LogFactory.getLog(GatewayCacheInvalidator.class);

    /**
     * APIMapping entries related to changed APIs are stored in this Set*
     */
    private ConcurrentSkipListSet<APIKeyMapping> keyMappings = new ConcurrentSkipListSet<APIKeyMapping>();

    /** Variable for Holding Singleton */
    private static GatewayCacheInvalidator cacheInvalidator;

    /** Keeps the access tokens that are scheduled to be removed.**/
    private ConcurrentSkipListSet<String> accessTokenSet = new ConcurrentSkipListSet<String>();

    /** Keeps the timestamps, cacheKeys for each domain was last retrieved.**/
    private Map<String, Long> timeStampMap = new ConcurrentHashMap<String, Long>();

    /** Keeps cacheKeys against tenant domain.**/
    private Map<String, Set<String>> cacheKeyMap = new ConcurrentHashMap<String, Set<String>>();

    public static final int MAXIMUM_MAPPINGS_TO_REMOVE = 1000;
    private static final int MAXIMUM_ACCESS_TOKENS_TO_REMOVE = 1000;
    private static final int CACHE_KEY_EXPIRATION_DURATION = 60000;
    public static final int MAXIMUM_ENTRIES_TO_ADD = 10000;

    /**
     * Call this when the component starts up, to create an instance of {@code GatewayCacheInvalidator}
     */
    public static void loadInstance() {
        cacheInvalidator = new GatewayCacheInvalidator();
    }

    /**
     * Gives a reference to the singleton.
     * @return
     */
    public static GatewayCacheInvalidator getInstance() {
        return cacheInvalidator;
    }

    private GatewayCacheInvalidator() {
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(2,
                                                 new ThreadFactory() {
                                                     public Thread newThread(Runnable r) {
                                                         Thread t = new Thread(r);
                                                         t.setName("CacheInvalidationTask" +
                                                                   " - Gateway");
                                                         return t;
                                                     }
                                                 });


        // Scheduling the task to execute every 10 seconds.
        executor.scheduleAtFixedRate(new CacheInvalidationTask(), 500,
                                     10000, TimeUnit.MILLISECONDS);
    }

    /**
     * Add APIMapping element that should be removed from Cache. CacheEntries related to those APIs will be removed
     * from GatewayCache.
     * @param mappings
     */
    public void addMappingForRemoval(APIKeyMapping[] mappings) {
        if (keyMappings.size() < MAXIMUM_ENTRIES_TO_ADD) {
            for (APIKeyMapping mapping : mappings) {
                keyMappings.add(mapping);
            }
        }
    }

    /**
     * Access tokens to be deleted from cache. Cache entries related those tokens will be removed from the cache.
     * @param revokedToken
     */
    public void addTokenForRemoval(String revokedToken) {
        if (accessTokenSet.size() < MAXIMUM_ENTRIES_TO_ADD) {
            accessTokenSet.add(revokedToken);
        }
    }


    /**
     * This task will run periodically removing entries from GatewayCache.
     * Entries can be deleted either by specifying the APIs against which they are generated,
     * or by specifying the access token they are created for.
     */
    private class CacheInvalidationTask implements Runnable {

        public void run() {

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

                // Code is only going to proceed if there are any entries to be removed.
                if (cacheInvalidator != null && (!cacheInvalidator.keyMappings.isEmpty() || !cacheInvalidator.accessTokenSet
                        .isEmpty())) {

                    if (!cacheInvalidator.keyMappings.isEmpty()) {

                        // Copying the Mappings to be deleted to a local Set.
                        HashSet<APIKeyMapping> mappings = new HashSet<APIKeyMapping>();
                        int addedMappings = 0;
                        for (APIKeyMapping mapping : cacheInvalidator.keyMappings) {
                            mappings.add(mapping);
                            addedMappings++;
                            if (addedMappings > MAXIMUM_MAPPINGS_TO_REMOVE) {
                                break;
                            }
                        }

                        // Removing copied entries.
                        cacheInvalidator.keyMappings.removeAll(mappings);


                        // Remove from GatewayCache.
                        for (APIKeyMapping mapping : mappings) {
                            if (mapping.getApiVersion() != null && mapping.getContext() != null) {
                                if(log.isDebugEnabled()){
                                    log.debug("Removing cache entry for APIKeyMapping "+mapping.toString());
                                }
                                removeMappingFromCache(mapping);
                            }
                        }
                    }

                    if (!cacheInvalidator.accessTokenSet.isEmpty()) {

                        HashSet<String> removedTokens = new HashSet<String>();
                        int addedTokens = 0;
                        for (String token : cacheInvalidator.accessTokenSet) {
                            removedTokens.add(token);
                            addedTokens++;
                            if (addedTokens > MAXIMUM_ACCESS_TOKENS_TO_REMOVE) {
                                break;
                            }
                        }

                        cacheInvalidator.accessTokenSet.removeAll(removedTokens);

                        for (String token : removedTokens) {
                            removeTokenFromCache(token, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                        }
                    }

                }
            } catch (IllegalStateException e) {
                log.error("IllegalStateException occurred while clearing GatewayCache in thread " + Thread
                        .currentThread()
                        .getName(), e);
            } catch (Throwable e) {
                log.error("Error occurred while clearing GatewayCache in thread " + Thread.currentThread()
                        .getName(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        }

        private void removeTokenFromCache(String accessToken, String domain) {

            // Switching the tenant domain. Key will be cached in the Tenant's cache from where API is coming from.
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain, true);

            Set<String> cacheKeys = getCacheKeySetForDomain(domain);
            if (cacheKeys != null && !cacheKeys.isEmpty()) {
                for (String cacheKey : cacheKeys) {
                    if (cacheKey.contains(accessToken)) {
                        GatewayKeyInfoCache.getInstance().removeFromCache(cacheKey);
                    }
                }
            }

            PrivilegedCarbonContext.endTenantFlow();
        }


        private void removeMappingFromCache(APIKeyMapping mapping) {
            String domain = mapping.getDomain();

            if (domain == null) {
                domain = APIUtil.getTenantDomainFromContext(mapping.getContext());
            }


            // Switching the tenant domain. Key will be cached in the Tenant's cache from where API is coming from.
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain, true);

            Set<String> cacheKeys = getCacheKeySetForDomain(domain);

            // This means we have to invalidate all the Cache entries related to that API
            for (String cacheKey : cacheKeys) {
                if (cacheKey.contains(APICacheEntry.createCacheKey(mapping.getContext(),
                                                                   mapping.getApiVersion()))) {
                    if(log.isDebugEnabled()){
                        log.debug("Removing entry for cacheKey : "+cacheKey);
                    }
                    GatewayKeyInfoCache.getInstance().removeFromCache(cacheKey);
                }
            }

            PrivilegedCarbonContext.endTenantFlow();
        }

        /**
         * Get cacheKeys from gatewayCache for provided domain.
         * @param domain
         * @return CacheKeys as a Set
         */
        private Set<String> getCacheKeySetForDomain(String domain) {
            Set<String> cacheKeys = cacheInvalidator.cacheKeyMap.get(domain);
            if (cacheKeys == null) {
                cacheKeys = new ConcurrentSkipListSet<String>();
                cacheKeys.addAll(GatewayKeyInfoCache.getInstance().getCacheKeys());
                cacheInvalidator.cacheKeyMap.put(domain, cacheKeys);
                cacheInvalidator.timeStampMap.put(domain, new Long(System.currentTimeMillis()));
            } else {
                Long lastPolledTimeStamp = cacheInvalidator.timeStampMap.get(domain);
                if (lastPolledTimeStamp != null) {
                    lastPolledTimeStamp = new Long(0);
                }

                // If stored CacheKeys are stale, we'll be re-fetching cacheKeys from the cache.
                if ((System.currentTimeMillis() - lastPolledTimeStamp.longValue()) >
                    CACHE_KEY_EXPIRATION_DURATION) {
                    cacheKeys.clear();
                    cacheKeys.addAll(GatewayKeyInfoCache.getInstance().getCacheKeys());
                    cacheInvalidator.cacheKeyMap.put(domain, cacheKeys);
                    cacheInvalidator.timeStampMap.put(domain, new Long(System.currentTimeMillis()));
                }
            }
            return cacheKeys;

        }
    }
}
