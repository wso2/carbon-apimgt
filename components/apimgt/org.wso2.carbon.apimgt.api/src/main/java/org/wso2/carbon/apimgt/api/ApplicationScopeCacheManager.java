package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.Set;

public interface ApplicationScopeCacheManager {

    void addToCache(String applicationUUID, String userName, Set<Scope> scopeSet, boolean isFiltered);

    Set<Scope> getValueFromCache(String applicationUUID, String userName, boolean isFiltered);

    void notifyOnApplicationDelete(String applicationUUID);

    void notifyUpdateOnCache(String applicationUUID);

    void notifyUpdateOnApi(APIIdentifier apiIdentifier) throws APIManagementException;
}
