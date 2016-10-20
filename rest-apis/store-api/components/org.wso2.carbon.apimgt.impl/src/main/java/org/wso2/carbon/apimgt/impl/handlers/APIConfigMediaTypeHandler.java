package org.wso2.carbon.apimgt.impl.handlers;

/*
* Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import javax.cache.Cache;
import javax.cache.Caching;

public class APIConfigMediaTypeHandler extends Handler {

    public void put(RequestContext requestContext) {
        clearConfigCache();
    }

    public void delete(RequestContext requestContext) {
        clearConfigCache();
    }

    private void clearConfigCache() {
        Cache workflowCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                getCache(APIConstants.WORKFLOW_CACHE_NAME);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cacheName = tenantDomain + "_" + APIConstants.WORKFLOW_CACHE_NAME;
        if (workflowCache.containsKey(cacheName)) {
            workflowCache.remove(cacheName);
        }

    }
}
