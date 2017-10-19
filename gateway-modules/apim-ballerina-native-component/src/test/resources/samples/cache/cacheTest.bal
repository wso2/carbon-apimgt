import org.wso2.carbon.apimgt.ballerina.caching;

function testCache(string cacheName, string validation, string cacheKey , any cacheEntry) (any) {
    caching:createCache(cacheName, validation);
    caching:putCacheEntry(cacheName, cacheKey ,cacheEntry);
    return caching:getCacheEntry(cacheName, cacheKey);
}