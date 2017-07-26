package org.wso2.carbon.apimgt.ballerina.caching;

import ballerina.doc;

@doc:Description { value:"Put cache entry by providing cacheName, cacheKey and cache entry"}
@doc:Param { value:"cacheName: Name of the cache" }
@doc:Param { value:"cacheKey: String cache key" }
@doc:Param { value:"cacheEntry: Cache entry to be push to cache" }
@doc:Return { value:"string: Cache updated status" }
native function putCacheEntry (string cacheName, string cacheKey, any cacheEntry) (string);

@doc:Description { value:"Get cache entry by providing cacheName and cacheKey"}
@doc:Param { value:"cacheName: Name of the cache" }
@doc:Param { value:"cacheKey: String cache key" }
@doc:Return { value:"string: Cache entry object" }
native function getCacheEntry (string cacheName, string cacheKey) (any);

@doc:Description { value:"Create cache as per user requirement"}
@doc:Param { value:"cacheName: Name of the cache to be create" }
@doc:Param { value:"cacheTimeout: Cache timeout in minutes" }
@doc:Return { value:"string: created cache name" }
native function createCache (string cacheName, string cacheTimeout) (string);

@doc:Description { value:"Remove cache entry by providing cacheName, cacheKey"}
@doc:Param { value:"cacheName: Name of the cache to be create" }
@doc:Param { value:"cacheKey: String cache key" }
@doc:Return { value:"string: cache name" }
native function removeCacheEntry (string cacheName, string cacheKey) (string);