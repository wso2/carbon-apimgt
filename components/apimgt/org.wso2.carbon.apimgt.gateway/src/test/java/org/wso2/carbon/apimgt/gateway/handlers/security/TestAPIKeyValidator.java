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


import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestAPIKeyValidator {   //extends APIKeyValidator
    /*
    private int counter = 0;
    private Map<String,APIKeyValidationInfoDTO> userInfo = new HashMap<String, APIKeyValidationInfoDTO>();

    public TestAPIKeyValidator() {
        super(new AxisConfiguration());
    }

    @Override
    protected Cache initCache() {
        return new SimpleCache();
    }

    @Override
    protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, 
                                                             String apiKey) throws APISecurityException {
        counter++;
        String key = getKey(context, apiVersion, apiKey);
        if (userInfo.containsKey(key)) {
            return userInfo.get(key);
        }
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(false);
        return info;
    }

    public void addUserInfo(String context, String apiVersion, 
                            String apiKey, APIKeyValidationInfoDTO info) {
        String key = getKey(context, apiVersion, apiKey);
        userInfo.put(key, info);
    }

    private String getKey(String context, String apiVersion, String apiKey) {
        return "{" + context + ":" + apiVersion + ":" + apiKey + "}";
    }

    public int getCounter() {
        return counter;
    }
    
    private static class SimpleCache implements Cache {
        
        private Map<Object,Object> map = new LRUCache<Object, Object>(APISecurityConstants.DEFAULT_MAX_INVALID_KEYS);
        
        public boolean containsKey(Object o) {
            return map.containsKey(o);
        }

        public boolean containsValue(Object o) {
            return map.containsValue(o);
        }

        public Set entrySet() {
            return map.entrySet();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Set keySet() {
            return map.keySet();
        }

        public void putAll(Map map) {
            map.putAll(map);
        }

        public int size() {
            return map.size();
        }

        public Collection values() {
            return map.values();
        }

        public Object get(Object o) {
            return map.get(o);
        }

        public Map getAll(Collection collection) throws CacheException {
            return null;
        }

        public void load(Object o) throws CacheException {

        }

        public void loadAll(Collection collection) throws CacheException {

        }

        public Object peek(Object o) {
            return map.get(o);
        }

        public Object put(Object o, Object o1) {
            return map.put(o, o1);
        }

        public CacheEntry getCacheEntry(Object o) {
            return null;
        }

        public CacheStatistics getCacheStatistics() {
            return null;
        }

        public Object remove(Object o) {
            return map.remove(o);
        }

        public void clear() {
            map.clear();
        }

        public void evict() {

        }

        public void addListener(CacheListener cacheListener) {

        }

        public void removeListener(CacheListener cacheListener) {

        }
    }
    */
}
