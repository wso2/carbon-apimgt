package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ApplicationScopeCacheManager;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ApplicationScopeCacheManagerImpl implements ApplicationScopeCacheManager{
    /**
     * This map is used to save the details of updated application list, after cache is being created.
     * Map<applicationUUID, true/false to indicate the update>
     */
    private ConcurrentHashMap<String, Boolean> updatedApplicationList;
    /**
     * This map is used to save the details of the users, who have the scope list in cache against the particular
     * application.
     * Map <applicationUUID, UserNameList>
     */
    private ConcurrentHashMap<String, List<String>> applicationUserMappings;
    private ApiMgtDAO apiMgtDAO;

    ApplicationScopeCacheManagerImpl() {
        long defaultCacheTimeout =
                Long.valueOf(ServerConfiguration.getInstance().getFirstProperty(APIConstants.DEFAULT_CACHE_TIMEOUT))
                        * 60;
        updatedApplicationList = new ConcurrentHashMap<String, Boolean>();
        applicationUserMappings = new ConcurrentHashMap<String, List<String>>();
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    @Override
    public void addToCache(String applicationUUID, String userName, Set<Scope> scopeSet, boolean isFiltered) {
        if (isFiltered) {
            getApplicationCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
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
            getApplicationCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).put(applicationUUID, scopeSet);
        }
        if (updatedApplicationList.get(applicationUUID) != null) {
            updatedApplicationList.put(applicationUUID, false);
        }
    }

    @Override
    public Set<Scope> getValueFromCache(String applicationUUID, String userName, boolean isFiltered) {
        boolean isApplicationSubscriptionUpdated = updatedApplicationList.get(applicationUUID) == null ? false :
                updatedApplicationList.get(applicationUUID);
        if (!isApplicationSubscriptionUpdated) {
            return isFiltered ?
                    getApplicationCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
                            .get(userName + "-" + applicationUUID) :
                    getApplicationCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).get(applicationUUID);
        }
        return null;
    }

    /**
     * To remove the cache when the application is deleted.
     *
     * @param applicationUUID UUID of the application.
     */
    public void notifyOnApplicationDelete(String applicationUUID) {
        getApplicationCache(APIConstants.APP_SUBSCRIPTION_SCOPE_CACHE).remove(applicationUUID);
        List<String> userList = applicationUserMappings.get(applicationUUID);

        for (String userName : userList) {
            getApplicationCache(APIConstants.APP_SUBSCRIPTION_FILTERED_SCOPE_CACHE)
                    .remove(userName + "-" + applicationUUID);
        }
        applicationUserMappings.remove(applicationUUID);
        updatedApplicationList.remove(applicationUUID);
    }

    /**
     * To update the scope cache when there is an update.
     *
     * @param applicationUUID UUID of the application.
     */
    public void notifyUpdateOnCache(String applicationUUID) {
        updatedApplicationList.put(applicationUUID, true);
    }

    @Override
    public void notifyUpdateOnApi(APIIdentifier apiIdentifier) throws APIManagementException {
        List<String> applicationIds = apiMgtDAO.getApplicationSubscriptions(apiIdentifier);
        if(applicationIds != null) {
            for (String applicationId : applicationIds) {
                notifyUpdateOnCache(applicationId);
            }
        }
    }

    /**
     *
     * To get the relevant cache
     *
     * @param cacheName        - Name of the Cache
     *
     */
    private  Cache<String, Set<Scope>> getApplicationCache(String cacheName) {
        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                .getCache(cacheName);

    }
}
