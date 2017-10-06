package org.wso2.carbon.apimgt.keymgt.issuers;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.user.core.service.RealmService;

import javax.cache.CacheManager;
import java.util.HashMap;
import java.util.Map;

public class PermissionBasedScopesIssuerWrapper extends PermissionBasedScopeIssuer {

    private CacheManager cacheManager;
    private RealmService realmService;
    private ApiMgtDAO apiMgtDao;

    public PermissionBasedScopesIssuerWrapper(CacheManager cacheManager, RealmService realmService,
                                              ApiMgtDAO apiMgtDao) {
        this.cacheManager = cacheManager;
        this.realmService = realmService;
        this.apiMgtDao = apiMgtDao;
    }

    @Override
    protected ApiMgtDAO getApiMgtDAOInstance() {
        return apiMgtDao;
    }

    @Override
    protected CacheManager getCacheManager(String name) {
        return cacheManager;
    }

    @Override
    protected JSONObject getTenantRESTAPIScopesConfig(String tenantDomain) throws APIManagementException {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    @Override
    protected Map<String, String> getRESTAPIScopesFromConfig(JSONObject jsonObject) {

        Map<String, String> scopes = new HashMap<String, String>();
        scopes.put("default", "default");
        return scopes;
    }

    @Override
    protected RealmService getRealmService() {
        return realmService;
    }

    @Override
    protected int getTenantIdOfUser(String username) {
        return -1234;
    }

}
