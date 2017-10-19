import org.wso2.carbon.apimgt.ballerina.caching;
import ballerina.lang.system;
function testCacheForNonExistence (string cacheName, string validation, string cacheKey) (boolean) {
    caching:createCache(cacheName, validation);
    any cacheEntry = caching:getCacheEntry(cacheName, cacheKey);
    if (cacheEntry != null) {
        system:println("cache entry not null");
        return true;
    } else {
        system:println("cache entry null");
        return false;
    }
}