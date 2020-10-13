package org.wso2.carbon.apimgt.persistence.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceReferenceHolder {
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private RealmService realmService;
    private TenantIndexingLoader indexLoader;
    private static UserRealm userRealm;

    private RegistryService registryService;

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public TenantIndexingLoader getIndexLoaderService(){
        return indexLoader;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public static void setUserRealm(UserRealm realm) {
        userRealm = realm;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }
}
