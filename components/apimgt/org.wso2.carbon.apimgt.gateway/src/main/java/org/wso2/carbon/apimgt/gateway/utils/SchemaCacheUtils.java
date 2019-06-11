package org.wso2.carbon.apimgt.gateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.cache.Cache;
import javax.cache.Caching;

public class SchemaCacheUtils {
    private static Logger log = LoggerFactory.getLogger(SchemaCacheUtils.class);

    /**
     * Get the Schemas from the Cache.
     *
     * @return schema cache object.
     */
    protected static Cache getSchemaCache() {
        Cache cache = null;
        try {
            javax.cache.CacheManager manager = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER);
            cache = manager.getCache(ThreatProtectorConstants.API_SWAGGER_SCHEMA);
        } catch (NullPointerException e) {
            log.error("Did not found valid API Validation Information cache configuration: " + e.getMessage(), e);
        }
        return cache;
    }

    /**
     * Put the Schema into the Cache.
     *
     * @param key   Schema name.
     * @param value Schema content.
     */
    public static void putCache(String key, String value) {
        getSchemaCache().put(key, value);

    }

    /**
     * Get Schema for given Schema definition.
     *
     * @param key Schema name.
     * @return Schema content.
     */
    public static String getCacheSchema(String key) {
        return (String) getSchemaCache().get(key);

    }
}
