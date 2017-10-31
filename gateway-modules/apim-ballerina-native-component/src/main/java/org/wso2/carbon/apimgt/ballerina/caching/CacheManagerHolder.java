package org.wso2.carbon.apimgt.ballerina.caching;

import org.wso2.carbon.caching.spi.CarbonCachingProvider;

import javax.cache.CacheManager;

/**
 * Cache Manager holder class to hold cache Manager
 */
public final class CacheManagerHolder {
    private static volatile CacheManagerHolder instance = null;


    /**
     * Get cache manager instance for cache access
     *
     * @return {@link CacheManager} which holds cache manager object
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    private CacheManager cacheManager;

    /**
     * Private cache manager holder constructor
     */
    private CacheManagerHolder() {
        CarbonCachingProvider carbonCachingProvider = new CarbonCachingProvider();
        this.cacheManager = carbonCachingProvider.getCacheManager();
    }

    /**
     * Static method to get cache manager
     *
     * @return {@link CacheManagerHolder} instance which holds cache manager reference.
     */
    public static CacheManagerHolder getInstance() {
        if (instance == null) {
            synchronized (CacheManagerHolder.class) {
                if (instance == null) {
                    instance = new CacheManagerHolder();
                }
            }
        }
        return instance;
    }
}
