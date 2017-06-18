package org.wso2.carbon.apimgt.ballerina.maps;

import ballerina.doc;

@doc:Description {value:"Put cache entry by providing cacheName, cacheKey and cache entry"}
@doc:Param {value:"key: String cache key"}
@doc:Param {value:"value: Map entry to be push to Map"}
@doc:Return {value:"string: Cache updated status"}
native function putMapEntry (string key, any cacheEntry) (string);

@doc:Description {value:"Get cache entry by providing cacheName and cacheKey"}
@doc:Param {value:"key: String cache key"}
@doc:Return {value:"any: Map Entry object"}
native function getMapEntry (string key) (any);

@doc:Description {value:"Remove cache entry by providing cacheName, cacheKey"}
@doc:Param {value:"key: String key"}
@doc:Return {value:"string: key"}
native function removeMapEntry (string key) (string);