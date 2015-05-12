package org.wso2.carbon.throttle.core;/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class ThrottleUtil {
    private static Log log = LogFactory.getLog(ThrottleUtil.class.getName());

    public static final String THROTTLING_CACHE_MANAGER = "throttling.cache.manager";

    public static final String THROTTLING_CACHE = "throttling.cache";

    public static Cache<String,CallerContext> getThrottleCache(){
        // acquiring  cache manager.
        Cache<String, CallerContext> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(THROTTLING_CACHE_MANAGER);
        if (cacheManager != null) {
            cache = cacheManager.getCache(THROTTLING_CACHE);
        } else {
            cache = Caching.getCacheManager().getCache(THROTTLING_CACHE);
        }
        if (log.isDebugEnabled()) {
            log.debug("created throttling cache : " + cache);
        }
        return cache;
    }

}
