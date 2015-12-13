/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScopesIssuer {

    private static Log log = LogFactory.getLog(ScopesIssuer.class);

    private static final String DEFAULT_SCOPE_NAME = "default";

    private List<String> scopeSkipList = new ArrayList<String>();

    /**
     * Singleton of ScopeIssuer.*
     */
    private static ScopesIssuer scopesIssuer;

    public static void loadInstance(List<String> whitelist) {
        scopesIssuer = new ScopesIssuer();
        if (whitelist != null && !whitelist.isEmpty()) {
            scopesIssuer.scopeSkipList.addAll(whitelist);
        }
    }

    private ScopesIssuer() {
    }

    public static ScopesIssuer getInstance() {
        return scopesIssuer;
    }


    public boolean setScopes(OAuthTokenReqMessageContext tokReqMsgCtx) {
        String[] requestedScopes = tokReqMsgCtx.getScope();
        String[] defaultScope = new String[]{DEFAULT_SCOPE_NAME};
        //If no scopes were requested.
        if (requestedScopes == null || requestedScopes.length == 0) {
            tokReqMsgCtx.setScope(defaultScope);
            return true;
        }

        String consumerKey = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        String username = tokReqMsgCtx.getAuthorizedUser().getUserName();
        String endUsernameWithDomain = UserCoreUtil.addDomainToName(username,
                        tokReqMsgCtx.getAuthorizedUser().getUserStoreDomain());
        List<String> reqScopeList = Arrays.asList(requestedScopes);
        Map<String, String> restAPIScopesOfCurrentTenant;
        try {
            Map<String, String> appScopes;
            ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
            //Get all the scopes and roles against the scopes defined for the APIs subscribed to the application.
            appScopes = apiMgtDAO.getScopeRolesOfApplication(consumerKey);
            //Add API Manager rest API scopes set. This list should be loaded at server start up and keep
            //in memory and add it to each and every request coming.
            String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
            restAPIScopesOfCurrentTenant = (Map)Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache("REST_API_SCOPE_CACHE")
                    .get(tenantDomain);
            if (restAPIScopesOfCurrentTenant!= null) {
                appScopes.putAll(restAPIScopesOfCurrentTenant);
            }else {
                restAPIScopesOfCurrentTenant = APIUtil.getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain));
                //call load tenant config for rest API.
                //then put cache
                appScopes.putAll(restAPIScopesOfCurrentTenant);
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache("REST_API_SCOPE_CACHE")
                        .put(tenantDomain, restAPIScopesOfCurrentTenant);
            }
            //If no scopes can be found in the context of the application
            if (appScopes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No scopes defined for the Application " +
                              tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
                }

                String[] allowedScopes = getAllowedScopes(reqScopeList);
                tokReqMsgCtx.setScope(allowedScopes);
                return true;
            }

            int tenantId;
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserStoreManager userStoreManager;
            String[] userRoles;

            try {
                tenantId = realmService.getTenantManager().
                                    getTenantId(tenantDomain);

                // If tenant Id is not set in the tokenReqContext, deriving it from username.
                if (tenantId == 0 || tenantId == -1) {
                    tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
                }
                userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.
                                                               getTenantAwareUsername(endUsernameWithDomain));
            } catch (UserStoreException e) {
                //Log and return since we do not want to stop issuing the token in case of scope validation failures.
                log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
                return false;
            }

            if (userRoles == null || userRoles.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find roles of the user.");
                }
                tokReqMsgCtx.setScope(defaultScope);
                return true;
            }

            List<String> authorizedScopes = new ArrayList<String>();
            List<String> userRoleList = new ArrayList<String>(Arrays.asList(userRoles));

            //Iterate the requested scopes list.
            for (String scope : reqScopeList) {
                //Get the set of roles associated with the requested scope.
                String roles = appScopes.get(scope);
                //If the scope has been defined in the context of the App and if roles have been defined for the scope
                if (roles != null && roles.length() != 0) {
                    List<String> roleList = new ArrayList<String>(Arrays.asList(roles.replaceAll(" ", "").split(",")));
                    //Check if user has at least one of the roles associated with the scope
                    roleList.retainAll(userRoleList);
                    if (!roleList.isEmpty()) {
                        authorizedScopes.add(scope);
                    }
                }
                //The requested scope is defined for the context of the App but no roles have been associated with the scope
                //OR
                //The scope string starts with 'device_'.
                else if (appScopes.containsKey(scope) || isWhiteListedScope(scope)) {
                    authorizedScopes.add(scope);
                }
            }
            if (!authorizedScopes.isEmpty()) {
                String[] authScopesArr = authorizedScopes.toArray(new String[authorizedScopes.size()]);
                tokReqMsgCtx.setScope(authScopesArr);
            } else {
                tokReqMsgCtx.setScope(defaultScope);
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Determines if the scope is specified in the whitelist.
     *
     * @param scope - The scope key to check
     * @return - 'true' if the scope is white listed. 'false' if not.
     */
    public boolean isWhiteListedScope(String scope) {
        for (String scopeTobeSkipped : scopeSkipList) {
            if (scope.matches(scopeTobeSkipped)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the set of default scopes. If a requested scope is matches with the patterns specified in the whitelist,
     * then such scopes will be issued without further validation. If the scope list is empty,
     * token will be issued for default scope.
     *
     * @param requestedScopes - The set of requested scopes
     * @return - The subset of scopes that are allowed
     */
    private String[] getAllowedScopes(List<String> requestedScopes) {
        List<String> authorizedScopes = new ArrayList<String>();

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            if (isWhiteListedScope(scope)) {
                authorizedScopes.add(scope);
            }
        }

        if (authorizedScopes.isEmpty()) {
            authorizedScopes.add(DEFAULT_SCOPE_NAME);
        }

        return authorizedScopes.toArray(new String[authorizedScopes.size()]);
    }
}


