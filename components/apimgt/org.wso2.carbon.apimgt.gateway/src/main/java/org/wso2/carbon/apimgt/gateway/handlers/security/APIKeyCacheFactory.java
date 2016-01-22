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

package org.wso2.carbon.apimgt.gateway.handlers.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this factory implementation to construct the required API key caches. This class
 * will index all constructed cache instances by a unique identifier (context + version).
 * Therefore the same implementation can be later queried for accessing already existing
 * cache instances and to invalidate entries.
 */
public class APIKeyCacheFactory {
    
    private static final APIKeyCacheFactory instance = new APIKeyCacheFactory();
    
    private Map<String,APIKeyCache> cacheMap = new ConcurrentHashMap<String, APIKeyCache>();
    
    private APIKeyCacheFactory() {
        
    }
    
    public static APIKeyCacheFactory getInstance() {
        return instance;
    }

    /**
     * This method checks whether an APIKeyCache instance exists for the specified API
     * context and version. If a cache exists, it will be returned. If not this method
     * will construct a new APIKeyCache instance and return it. Subsequent invocations
     * of this method with the same parameters will return that APIKeyCache object.
     *
     * @param context An API context
     * @param version Version of the API
     * @return an APIKeyCache object
     */
    public APIKeyCache getAPIKeyCache(String context, String version) {
        String identifier = context + ':' + version;
        APIKeyCache cache = cacheMap.get(identifier);
        if (cache == null) {
            synchronized (this) {
                cache = cacheMap.get(identifier);
                if (cache == null) {
                    cache = new APIKeyCache(APISecurityConstants.DEFAULT_MAX_VALID_KEYS, 
                            APISecurityConstants.DEFAULT_MAX_INVALID_KEYS);
                    cacheMap.put(identifier, cache);
                }
            }
        }
        return cache;
    }

    /**
     * This method returns an existing APIKeyCache instance for the given API context, version
     * combination. If such a cache instance does not exist, this method will simply return
     * null.
     *
     * @param context An API context
     * @param version Version of the API
     * @return an APIKeyCache object or null
     */
    public APIKeyCache getExistingAPIKeyCache(String context, String version) {
        String identifier = context + ':' + version;
        return cacheMap.get(identifier);
    }

    /**
     * Cleanup all the cache instances created previously. Used for internal purposes
     * only.
     */
    void reset() {
        cacheMap.clear();
    }
}
