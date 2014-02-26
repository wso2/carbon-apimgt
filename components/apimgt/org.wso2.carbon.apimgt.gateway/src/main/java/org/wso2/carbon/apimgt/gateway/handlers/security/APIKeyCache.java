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

import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;

import java.util.Map;

/**
 * A simple in-memory cache for API keys and validation information related to API keys.
 * In order to conserve resources, this implementation imposes hard upper bounds on the
 * number of valid and invalid keys kept in the cache. When the cache is full, a simple
 * LRU algorithm is used to replace existing cache entries. This cache implementation is
 * thread safe. It supports multiple concurrent read operations. That is read operations
 * may overlap and will return the results without blocking. But write operations are
 * carried out in a mutually exclusive manner. When multiple write operations are requested,
 * they will be executed in serial manner with thread blocking. Similarly overlapping read and
 * write operations will be executed in serial fashion. In general this cache is designed
 * for many read operations and less write operations.
 */
public class APIKeyCache {

    private Map<String,APIKeyValidationInfoDTO> validKeys;
    private Map<String,APIKeyValidationInfoDTO> invalidKeys;

    APIKeyCache(int maxValidKeys, int maxInvalidKeys) {
        validKeys = new LRUCache<String, APIKeyValidationInfoDTO>(maxValidKeys);
        invalidKeys = new LRUCache<String, APIKeyValidationInfoDTO>(maxInvalidKeys);
    }

    public void addValidKey(String key, APIKeyValidationInfoDTO info) {
        validKeys.put(key, info);
    }

    public void addInvalidKey(String key, APIKeyValidationInfoDTO info) {
        invalidKeys.put(key, info);
    }

    public APIKeyValidationInfoDTO getInfo(String key) {
        APIKeyValidationInfoDTO info = validKeys.get(key);
        if (info == null) {
            info = invalidKeys.get(key);
        }
        return info;
    }
    
    public void invalidateEntry(String key) {
        validKeys.remove(key);
        invalidKeys.remove(key);
    }

    public void invalidateCache() {
        validKeys.clear();
        invalidKeys.clear();
    }
}
