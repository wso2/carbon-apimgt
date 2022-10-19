/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.impl.caching;

import com.google.common.cache.Cache;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static Map<String, Cache<?, ?>> caches = new HashMap<>();

    private CacheManager() {
        //To hide the default constructor
    }

    public static Cache<?, ?> getCache(String cacheName) {

        return caches.get(cacheName);
    }

    public static void addCache(String cacheName, Cache cache) {

        caches.put(cacheName, cache);
    }
}
