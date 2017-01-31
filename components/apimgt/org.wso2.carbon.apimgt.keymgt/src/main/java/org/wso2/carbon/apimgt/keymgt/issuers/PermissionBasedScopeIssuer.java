/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class represents the functions related to an scope issuer which
 * issues scopes based on user permissions.
 */
public class PermissionBasedScopeIssuer extends AbstractScopesIssuer {

    private static Log log = LogFactory.getLog(PermissionBasedScopeIssuer.class);
    private static final String DEFAULT_SCOPE_NAME = "default";
    private static final String ISSUER_PREFIX = "perm";
    private static final String UI_EXECUTE = "ui.execute";
    private static final String REST_API_SCOPE_CACHE = "REST_API_SCOPE_CACHE";

    @Override
    public String getPrefix(){
        return ISSUER_PREFIX;
    }

    @Override
    public List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> whiteListedScopes) {
        String[] requestedScopes = tokReqMsgCtx.getScope();
        List<String> defaultScope = new ArrayList<String>();
        defaultScope.add(DEFAULT_SCOPE_NAME);

        String consumerKey = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        List<String> reqScopeList = Arrays.asList(requestedScopes);
        Map<String, String> restAPIScopesOfCurrentTenant;

        try {

            Map<String, String> appScopes;
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

            //Get all the scopes and permissions against the scopes defined for the APIs subscribed to the application.
            appScopes = apiMgtDAO.getScopeRolesOfApplication(consumerKey);

            //Add API Manager rest API scopes set. This list should be loaded at server start up and keep
            //in memory and add it to each and every request coming.
            String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
            restAPIScopesOfCurrentTenant = (Map) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache(REST_API_SCOPE_CACHE)
                    .get(tenantDomain);
            if (restAPIScopesOfCurrentTenant != null) {
                appScopes.putAll(restAPIScopesOfCurrentTenant);
            } else {
                restAPIScopesOfCurrentTenant = APIUtil.
                        getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain));

                //then put cache
                appScopes.putAll(restAPIScopesOfCurrentTenant);
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(REST_API_SCOPE_CACHE)
                        .put(tenantDomain, restAPIScopesOfCurrentTenant);
            }
            //If no scopes can be found in the context of the application
            if (appScopes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No scopes defined for the Application " +
                            tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
                }

                List<String> allowedScopes = getAllowedScopes(whiteListedScopes, reqScopeList);
                return allowedScopes;
            }

            // check for authorized scopes
            List<String> authorizedScopes =
                    getAuthorizedScopes(tokReqMsgCtx, reqScopeList, appScopes, whiteListedScopes);

            if (!authorizedScopes.isEmpty()) {
                return authorizedScopes;
            } else {
                return defaultScope;
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage());
            return null;
        }
    }

    /**
     * This method is used to get the authorized scopes out of requested scopes. It checks requested scopes with app
     * scopes whether user has permissions to take actions for the requested scopes.
     *
     * @param tokReqMsgCtx OAuth token request message context.
     * @param reqScopeList Requested scope list.
     * @param appScopes    App scopes.
     * @return Returns a list of scopes.
     */
    private List<String> getAuthorizedScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> reqScopeList,
                                                    Map<String, String> appScopes, List<String> whiteListedScopes) {

        boolean status;
        List<String> authorizedScopes = new ArrayList<String>();

        int tenantId;
        String username = tokReqMsgCtx.getAuthorizedUser().getUserName();
        String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
        RealmService realmService = APIKeyMgtDataHolder.getRealmService();

        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            // If tenant Id is not set in the tokenReqContext, deriving it from username.
            if (tenantId == 0 || tenantId == -1) {
                tenantId = IdentityTenantUtil.getTenantIdOfUser(username);
            }

            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            //Iterate the requested scopes list.
            for (String scope : reqScopeList) {
                status = false;

                //Get the set of roles associated with the requested scope.
                String appPermissions = appScopes.get(scope);

                //If the scope has been defined in the context of the App and if permissions have been defined for
                // the scope
                if (appPermissions != null && appPermissions.length() != 0) {
                    List<String> permissions = new ArrayList<String>(Arrays.asList(appPermissions.replaceAll(" ", "")
                            .split(
                            ",")));

                    //Check if user has at least one of the permission associated with the scope
                    if (!permissions.isEmpty()) {
                        for (String permission : permissions) {
                            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                                String userStore = tokReqMsgCtx.getAuthorizedUser().getUserStoreDomain();
                                username = MultitenantUtils.getTenantAwareUsername(username);
                                if (userStore != null) {
                                    status = userRealm.getAuthorizationManager()
                                            .isUserAuthorized(userStore + "/" + username, permission, UI_EXECUTE);
                                } else {
                                    status = userRealm.getAuthorizationManager()
                                            .isUserAuthorized(username, permission, UI_EXECUTE);
                                }
                                if (status) {
                                    break;
                                }
                            }
                        }
                        if (status) {
                            authorizedScopes.add(scope);
                        }
                    }
                }

                //The scope string starts with 'device_'.
                else if (appScopes.containsKey(scope) || isWhiteListedScope(whiteListedScopes, scope)) {
                    authorizedScopes.add(scope);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while initializing user store.", e);
        }
        return authorizedScopes;
    }

}
