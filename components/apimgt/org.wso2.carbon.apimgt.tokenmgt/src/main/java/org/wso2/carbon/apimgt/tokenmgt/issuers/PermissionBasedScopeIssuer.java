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
package org.wso2.carbon.apimgt.tokenmgt.issuers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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

    @Override
    public String getPrefix(){
        return ISSUER_PREFIX;
    }

    /**
     * This method is used to retrieve the authorized scopes with respect to a token.
     *
     * @param tokReqMsgCtx      token message context
     * @param whiteListedScopes scopes to be white listed
     * @return authorized scopes list
     */
    @Override
    public List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> whiteListedScopes) {

        List<String> authorizedScopes = null;
        List<String> requestedScopes = Arrays.asList(tokReqMsgCtx.getScope());
        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        AuthenticatedUser authenticatedUser = tokReqMsgCtx.getAuthorizedUser();
        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(whiteListedScopes, requestedScopes);
            }
            authorizedScopes = getAuthorizedScopes(authenticatedUser, requestedScopes, appScopes, whiteListedScopes);
        }
        return authorizedScopes;
    }

    /**
     * This method is used to retrieve authorized scopes with respect to an authorization callback.
     *
     * @param scopeValidationCallback Authorization callback to validate scopes
     * @param whiteListedScopes       scopes to be white listed
     * @return authorized scopes list
     */
    @Override
    public List<String> getScopes(OAuthCallback scopeValidationCallback, List<String> whiteListedScopes) {

        List<String> authroizedScopes = null;
        List<String> requestedScopes = Arrays.asList(scopeValidationCallback.getRequestedScope());
        String clientId = scopeValidationCallback.getClient();
        AuthenticatedUser authenticatedUser = scopeValidationCallback.getResourceOwner();
        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(whiteListedScopes, requestedScopes);
            }
            authroizedScopes = getAuthorizedScopes(authenticatedUser, requestedScopes, appScopes, whiteListedScopes);
        }
        return authroizedScopes;
    }

    /**
     * This method is used to get the authorized scopes out of requested scopes. It checks requested scopes with app
     * scopes whether user has permissions to take actions for the requested scopes.
     *
     * @param authenticatedUser Authenticated user.
     * @param reqScopeList      Requested scope list.
     * @param appScopes         App scopes.
     * @return Returns a list of scopes.
     */
    private List<String> getAuthorizedScopes(AuthenticatedUser authenticatedUser, List<String> reqScopeList,
                                             Map<String, String> appScopes, List<String> whiteListedScopes) {

        boolean status;
        List<String> authorizedScopes = new ArrayList<>();
        int tenantId;
        String username = authenticatedUser.getUserName();
        String tenantDomain = authenticatedUser.getTenantDomain();
        RealmService realmService = getRealmService();
        List<String> defaultScope = new ArrayList<>();
        defaultScope.add(DEFAULT_SCOPE_NAME);

        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            // If tenant Id is not set in the tokenReqContext, deriving it from username.
            if (tenantId == 0 || tenantId == -1) {
                tenantId = getTenantIdOfUser(username);
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
                    List<String> permissions = new ArrayList<>(Arrays.asList(appPermissions
                            .replaceAll(" ", "").split(",")));

                    //Check if user has at least one of the permission associated with the scope
                    if (!permissions.isEmpty()) {
                        for (String permission : permissions) {
                            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                                String userStore = authenticatedUser.getUserStoreDomain();
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
        return (!authorizedScopes.isEmpty()) ? authorizedScopes : defaultScope;
    }

 }
