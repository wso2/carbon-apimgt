package org.wso2.carbon.apimgt.gateway.handlers.security.service;


import javax.cache.CacheManager;

public class ApiAuthenticationServiceWrapper extends APIAuthenticationService{
    private CacheManager cacheManager;

    public ApiAuthenticationServiceWrapper(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    protected CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    protected boolean startTenantFlow(String tenantDomain) {
        return false;
    }

    @Override
    protected void endTenantFlow() {
    }
}
