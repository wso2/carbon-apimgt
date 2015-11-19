/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.service;

import javax.cache.Cache;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.GhostResource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.caching.RegistryCacheKey;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * This service contains the methods to invalidate registry cache for a given resource
 *
 */
public class RegistryCacheInvalidationService extends AbstractAdmin {
    
    
    /**
     * This method invalidates registry cache for given resource in given tenant domain
     * @param path
     * @param tenantDomain
     * @throws APIManagementException
     */
    public void invalidateCache(String path, String tenantDomain) throws APIManagementException {
        Registry registry;
        boolean isTenantFlowStarted = false;
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                                                            .getTenantId(tenantDomain);            
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);            
            isTenantFlowStarted = true;
            
            registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
            
            Cache<RegistryCacheKey, GhostResource> cache = 
                    RegistryUtils.getResourceCache(RegistryConstants.REGISTRY_CACHE_BACKED_ID);
            RegistryCacheKey cacheKey = null ;

            //Is registry mounted
            if(registry.getRegistryContext().getRemoteInstances().size() > 0) {
                for (Mount mount : registry.getRegistryContext().getMounts()) {
                    for(RemoteConfiguration configuration : registry.getRegistryContext().getRemoteInstances()) {
                        if (path.startsWith(mount.getPath())) {
                            DataBaseConfiguration dataBaseConfiguration = 
                                    registry.getRegistryContext().getDBConfig(configuration.getDbConfig());
                            String connectionId = (dataBaseConfiguration.getUserName() != null
                               ? dataBaseConfiguration.getUserName().split("@")[0] : 
                                   dataBaseConfiguration.getUserName()) + "@" + dataBaseConfiguration.getDbUrl();
                            cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, path);

                            if (cacheKey != null && cache.containsKey(cacheKey)) {
                                cache.remove(cacheKey);
                            }
                        }
                    }
                }
            } else {
                DataBaseConfiguration dataBaseConfiguration = registry.getRegistryContext().
                        getDefaultDataBaseConfiguration();
                String connectionId = (dataBaseConfiguration.getUserName() != null
                        ? dataBaseConfiguration.getUserName().split("@")[0]:dataBaseConfiguration.getUserName()) 
                        + "@" + dataBaseConfiguration.getDbUrl();
                cacheKey = RegistryUtils.buildRegistryCacheKey(connectionId, tenantId, path);

                if (cacheKey != null && cache.containsKey(cacheKey)) {
                    cache.remove(cacheKey);
                }
            }
        } catch (RegistryException e) {
            APIUtil.handleException("Error in accessing governance registry while invalidating cache for " 
                                                    + path + "in tenant " + tenantDomain, e);
        } catch (UserStoreException e) {
            APIUtil.handleException("Error in retrieving Tenant Information while invalidating cache for " 
                                                    + path + "in tenant " + tenantDomain, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

}
