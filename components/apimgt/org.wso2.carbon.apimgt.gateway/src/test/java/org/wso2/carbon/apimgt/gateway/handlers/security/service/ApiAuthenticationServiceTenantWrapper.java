package org.wso2.carbon.apimgt.gateway.handlers.security.service;


import javax.cache.CacheManager;

public class ApiAuthenticationServiceTenantWrapper extends APIAuthenticationService{
    private CacheManager cacheManager;

    public ApiAuthenticationServiceTenantWrapper(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    protected CacheManager getCacheManager() {
        return cacheManager;
    }

    @Override
    protected boolean startTenantFlow(String tenantDomain) {
        return true;
    }

    @Override
    protected void endTenantFlow() {
    }
}
