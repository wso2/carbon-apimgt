import org.wso2.carbon.apimgt.ballerina.caching;

function testRemoveCache(string cacheName, string validation, string cacheKey , string cacheEntry) (any) {
    caching:createCache(cacheName, validation);
    caching:putCacheEntry(cacheName, cacheKey ,cacheEntry);
    caching:removeCacheEntry(cacheName, cacheKey);
    return "OK";
}