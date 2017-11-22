/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.core.service.RealmService;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This abstract class represents the basic requirements of a scope issuer.
 */
public abstract class AbstractScopesIssuer {

    private static final String DEFAULT_SCOPE_NAME = "default";

    /**
     * This method is used to retrieve the authorized scopes with respect to a token.
     *
     * @param tokReqMsgCtx      token message context
     * @param whiteListedScopes scopes to be white listed
     * @return returns authorized scopes list
     */
    public abstract List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> whiteListedScopes);

    /**
     * This method is used to get the prefix of the scope issuer.
     *
     * @return returns the prefix with respect to an issuer.
     */
    public abstract String getPrefix();

    /**
     * Get the set of default scopes. If a requested scope is matches with the patterns specified in the whitelist,
     * then such scopes will be issued without further validation. If the scope list is empty,
     * token will be issued for default scop1e.
     *
     * @param requestedScopes - The set of requested scopes
     * @return - The subset of scopes that are allowed
     */
    public List<String> getAllowedScopes(List<String> scopeSkipList, List<String> requestedScopes) {
        List<String> authorizedScopes = new ArrayList<String>();

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            if (isWhiteListedScope(scopeSkipList, scope)) {
                authorizedScopes.add(scope);
            }
        }

        if (authorizedScopes.isEmpty()) {
            authorizedScopes.add(DEFAULT_SCOPE_NAME);
        }
        return authorizedScopes;
    }

    /**
     * Determines if the scope is specified in the whitelist.
     *
     * @param scope - The scope key to check
     * @return - 'true' if the scope is white listed. 'false' if not.
     */
    public boolean isWhiteListedScope(List<String> scopeSkipList, String scope) {
        for (String scopeTobeSkipped : scopeSkipList) {
            if (scope.matches(scopeTobeSkipped)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get ApiMgtDAO instance
     *
     * @return ApiMgtDAO
     */
    protected ApiMgtDAO getApiMgtDAOInstance() {
        return ApiMgtDAO.getInstance();
    }

    /**
     * Get CacheManager instance
     *
     * @param name The name of the Cache
     * @return CacheManager
     */
    protected CacheManager getCacheManager(String name) {
        return Caching.getCacheManager(name);
    }

    /**
     * Get tenant REST API scopes configuration as Json Object
     *
     * @param tenantDomain The tenant domain of the tenant
     * @return JSONObject
     */
    protected JSONObject getTenantRESTAPIScopesConfig(String tenantDomain) throws APIManagementException {
        return APIUtil.getTenantRESTAPIScopesConfig(tenantDomain);
    }

    /**
     * Get REST API scopes from the configuration
     *
     * @param jsonObject Scopes config as a Json object
     * @return JSONObject
     */
    protected Map<String, String> getRESTAPIScopesFromConfig(JSONObject jsonObject) {
        return APIUtil.getRESTAPIScopesFromConfig(jsonObject);
    }

    /**
     * Get RealmService
     *
     * @return RealmService
     */
    protected RealmService getRealmService() {
        return APIKeyMgtDataHolder.getRealmService();
    }

    /**
     * Get tenant Id of the user
     *
     * @param username Username
     * @return JSONObject
     */
    protected int getTenantIdOfUser(String username) {
        return IdentityTenantUtil.getTenantIdOfUser(username);
    }


}


