/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import javax.cache.Cache;
import javax.cache.Caching;

/**
 * This registry handler is used to clear cache when lifecycle resource is modified.
 */
public class LifecycleModificationHandler extends Handler {
    private static final Log log = LogFactory.getLog(LifecycleModificationHandler.class);

    public void put(RequestContext requestContext) {
        clearConfigCache(requestContext);
    }

    public void delete(RequestContext requestContext) {
        clearConfigCache(requestContext);
    }

    private void clearConfigCache(RequestContext requestContext) {
        Resource resource = requestContext.getResource();
        if (resource instanceof ResourceImpl) {
            ResourceImpl resourceImpl = (ResourceImpl) resource;
            if (resourceImpl != null && APIConstants.API_LIFE_CYCLE.equals(resourceImpl.getName())) {
                Cache lcCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.LC_CACHE_NAME);
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                String cacheName = tenantDomain + "_" + APIConstants.LC_CACHE_NAME;
                if (lcCache.containsKey(cacheName)) {
                    lcCache.remove(cacheName);
                    if (log.isDebugEnabled()) {
                        log.debug("Lifecycle cache cleared for tenant domain " + tenantDomain);
                    }
                }
            }

        }
    }
}
