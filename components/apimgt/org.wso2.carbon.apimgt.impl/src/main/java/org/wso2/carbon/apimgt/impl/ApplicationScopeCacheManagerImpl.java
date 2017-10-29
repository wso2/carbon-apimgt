/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ApplicationScopeCacheManager;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a default concrete implementation of ApplicationScopeCacheManager.
 */
public class ApplicationScopeCacheManagerImpl implements ApplicationScopeCacheManager{
    /**
     * This map is used to save the details of updated application list, after cache is being created.
     * Map<applicationUUID, list of cache keys that are updated after the application update>
     */
    private ConcurrentHashMap<String, List<String>> updatedApplicationList;
    /**
     * This map is used to save the details of the users, who have the scope list in cache against the particular
     * application.
     * Map <applicationUUID, UserNameList>
     */
    private ConcurrentHashMap<String, List<String>> applicationUserMappings;
    private ApiMgtDAO apiMgtDAO;

    /**
     * Initializing the needed parameters.
     */
    ApplicationScopeCacheManagerImpl() {
        updatedApplicationList = new ConcurrentHashMap<String, List<String>>();
        applicationUserMappings = new ConcurrentHashMap<String, List<String>>();
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    @Override
    public void addToCache(String applicationUUID, String userName, Set<Scope> scopeSet, boolean isFiltered) {
        if (isFiltered) {
            getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
                    .put(userName + "-" + applicationUUID, scopeSet);
            List<String> userList = applicationUserMappings.get(applicationUUID);
            if (userList == null) {
                userList = new ArrayList<String>();
            }
            if (!userList.contains(userName)) {
                userList.add(userName);
            }
            applicationUserMappings.put(applicationUUID, userList);
        } else {
            getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).put(applicationUUID, scopeSet);
        }
        List<String> updatedEntries = updatedApplicationList.get(applicationUUID);
        if (updatedEntries != null) {
            if (isFiltered) {
                updatedEntries.add(userName + "-" + applicationUUID);
            } else {
                updatedEntries.add(applicationUUID);
            }
        }
    }

    @Override
    public Set<Scope> getValueFromCache(String applicationUUID, String userName, boolean isFiltered) {
        boolean isCacheNeedToBeUpdated;
        List<String> updatedCacheList = updatedApplicationList.get(applicationUUID);
        if (updatedCacheList == null) {
            isCacheNeedToBeUpdated = false;
        } else {
            if (isFiltered) {
                isCacheNeedToBeUpdated = !updatedCacheList.contains(userName + "-" + applicationUUID);
            } else {
                isCacheNeedToBeUpdated = !updatedCacheList.contains(applicationUUID);
            }
        }
        if (!isCacheNeedToBeUpdated) {
            return isFiltered ?
                    getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
                            .get(userName + "-" + applicationUUID) :
                    getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).get(applicationUUID);
        }
        return null;
    }

    public void notifyOnApplicationDelete(String applicationUUID) {
        getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).remove(applicationUUID);
        List<String> userList = applicationUserMappings.get(applicationUUID);

        if (userList != null) {
            for (String userName : userList) {
                getApplicationScopeCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
                        .remove(userName + "-" + applicationUUID);
            }
        }
        applicationUserMappings.remove(applicationUUID);
        updatedApplicationList.remove(applicationUUID);
    }

    @Override
    public void notifyUpdateOnCache(String applicationUUID) {
        updatedApplicationList.put(applicationUUID, new ArrayList<String>());
    }

    @Override
    public void notifyUpdateOnApi(APIIdentifier apiIdentifier) throws APIManagementException {
        List<String> applicationIds = apiMgtDAO.getApplicationSubscriptions(apiIdentifier);
        if (applicationIds != null) {
            for (String applicationId : applicationIds) {
                notifyUpdateOnCache(applicationId);
            }
        }
    }

    /**
     * To get the relevant application scope Cache.
     *
     * @param cacheName - Name of the Cache
     * @return relevant cache.
     */
    private Cache<String, Set<Scope>> getApplicationScopeCache(String cacheName) {
        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName);
    }
}
