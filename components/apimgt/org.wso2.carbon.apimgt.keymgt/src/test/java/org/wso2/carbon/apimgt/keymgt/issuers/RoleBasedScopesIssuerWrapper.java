/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.json.simple.JSONObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.user.core.service.RealmService;

import javax.cache.CacheManager;
import java.util.HashMap;
import java.util.Map;

public class RoleBasedScopesIssuerWrapper extends RoleBasedScopesIssuer {

    private CacheManager cacheManager;
    private RealmService realmService;
    private ApiMgtDAO apiMgtDao;

    public RoleBasedScopesIssuerWrapper(CacheManager cacheManager, RealmService realmService,
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
    protected Map<String, String> getRESTAPIScopesFromConfig(JSONObject scopesConfig, JSONObject roleMappings) {

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

    @Override
    protected String addDomainToName(String username, String domainName) {
        return username + "@" + domainName;
    }

    @Override
    protected String[] getRolesFromAssertion(Assertion assertion) {
        return new String[]{"role 1"};
    }

}
