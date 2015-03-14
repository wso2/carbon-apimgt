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

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.*;

public class ScopesIssuer {

    private static Log log = LogFactory.getLog(ScopesIssuer.class);

    private static final String DEVICE_SCOPE_PREFIX = "device_";

    private static final String DEFAULT_SCOPE_NAME = "default";

    public boolean setScopes(OAuthTokenReqMessageContext tokReqMsgCtx){
        String[] requestedScopes = tokReqMsgCtx.getScope();
        String[] defaultScope = new String[]{DEFAULT_SCOPE_NAME};

        //If no scopes were requested.
        if(requestedScopes == null || requestedScopes.length == 0){
            tokReqMsgCtx.setScope(defaultScope);
            return true;
        }

        String consumerKey = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        String username = tokReqMsgCtx.getAuthorizedUser();

        String cacheKey = getAppUserScopeCacheKey(consumerKey, username, requestedScopes);
        Cache cache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                getCache(APIConstants.APP_USER_SCOPE_CACHE);

        List<String> reqScopeList = Arrays.asList(requestedScopes);

        try {
            Map<String, String> appScopes = null;
            Cache appCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    getCache(APIConstants.APP_SCOPE_CACHE);

            //Cache hit
            if(appCache.containsKey(consumerKey)){
                appScopes = (Map<String, String>)appCache.get(consumerKey);

                // checking the user cache only if app cache is available (since we only update app cache)
                if(cache.containsKey(cacheKey)){
                    tokReqMsgCtx.setScope((String [])cache.get(cacheKey));
                    return true;
                }
            }
            //Cache miss
            else{
                ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
                //Get all the scopes and roles against the scopes defined for the APIs subscribed to the application.
                appScopes = apiMgtDAO.getScopeRolesOfApplication(consumerKey);
                //If scopes is null, set empty hashmap to scopes so that we avoid adding a null entry to the cache.
                if(appScopes == null){
                    appScopes = new HashMap<String, String>();
                }
                appCache.put(consumerKey, appScopes);
            }

            //If no scopes can be found in the context of the application
            if(appScopes.isEmpty()){
                if(log.isDebugEnabled()){
                    log.debug("No scopes defined for the Application " +
                            tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
                }

                String[] allowedScopes = getAllowedScopes(reqScopeList);
                tokReqMsgCtx.setScope(allowedScopes);
                cache.put(cacheKey, allowedScopes);
                return true;
            }

            int tenantId;
            RealmService realmService = OAuthComponentServiceHolder.getRealmService();
            UserStoreManager userStoreManager = null;
            String[] userRoles = null;

            try {
                tenantId = IdentityUtil.getTenantIdOFUser(username);
                userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                userRoles = userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
            } catch (IdentityException e) {
                //Log and return since we do not want to stop issuing the token in case of scope validation failures.
                log.error("Error when obtaining tenant Id of user " + username, e);
                return false;
            } catch (UserStoreException e) {
                //Log and return since we do not want to stop issuing the token in case of scope validation failures.
                log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
                return false;
            }

            if(userRoles == null || userRoles.length == 0){
                if(log.isDebugEnabled()){
                    log.debug("Could not find roles of the user.");
                }
                tokReqMsgCtx.setScope(defaultScope);
                cache.put(cacheKey, defaultScope);
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
                    if(!roleList.isEmpty()){
                        authorizedScopes.add(scope);
                    }
                }
                //The requested scope is defined for the context of the App but no roles have been associated with the scope
                //OR
                //The scope string starts with 'device_'.
                else if(appScopes.containsKey(scope) || scope.startsWith(DEVICE_SCOPE_PREFIX)){
                    authorizedScopes.add(scope);
                }
            }
            if(!authorizedScopes.isEmpty()){
                String[] authScopesArr = authorizedScopes.toArray(new String[authorizedScopes.size()]);
                cache.put(cacheKey, authScopesArr);
                tokReqMsgCtx.setScope(authScopesArr);
            }
            else{
                cache.put(cacheKey, defaultScope);
                tokReqMsgCtx.setScope(defaultScope);
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage());
            return false;
        }
        return true;
    }

    private String getAppUserScopeCacheKey(String consumerKey, String username, String[] requestedScopes){
        StringBuilder reqScopesBuilder = new StringBuilder("");
        for(int i=0; i<requestedScopes.length; i++){
            reqScopesBuilder.append(requestedScopes[i]);
        }

        int reqScopesHash = reqScopesBuilder.toString().hashCode();

        StringBuilder cacheKey = new StringBuilder("");
        cacheKey.append(consumerKey);
        cacheKey.append(":");
        cacheKey.append(username);
        cacheKey.append(":");
        cacheKey.append(reqScopesHash);

        return cacheKey.toString();
    }

    /**
     * Get the set of default scopes. This will return a String array which has the scopes that are prefixed with
     * "device_" from the set of requested scopes. If no such scopes exists, it will only return the 'default' scope.
     * @param requestedScopes - The set of requested scopes
     * @return
     */
    private String[] getAllowedScopes(List<String> requestedScopes){
        List<String> authorizedScopes = new ArrayList<String>();

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            if(scope.startsWith(DEVICE_SCOPE_PREFIX)){
                authorizedScopes.add(scope);
            }
        }

        if(authorizedScopes.isEmpty()){
            authorizedScopes.add(DEFAULT_SCOPE_NAME);
        }
        return authorizedScopes.toArray(new String[authorizedScopes.size()]);
    }
}


