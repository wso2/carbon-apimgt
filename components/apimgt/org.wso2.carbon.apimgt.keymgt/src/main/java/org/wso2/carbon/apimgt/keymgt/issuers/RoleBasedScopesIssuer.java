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
import org.opensaml.saml2.core.Assertion;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.common.GrantType;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.cache.Caching;

/**
 * This class represents the functions related to an scope issuer which
 * issues scopes based on user roles.
 */
public class RoleBasedScopesIssuer extends AbstractScopesIssuer {

    private static Log log = LogFactory.getLog(RoleBasedScopesIssuer.class);
    private static final String DEFAULT_SCOPE_NAME = "default";

    // set role based scopes issuer as the default
    private static final String ISSUER_PREFIX = "default";

    @Override
    public String getPrefix() {
        return ISSUER_PREFIX;
    }

    @Override
    public List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> whiteListedScopes) {
        String[] requestedScopes = tokReqMsgCtx.getScope();
        List<String> defaultScope = new ArrayList<String>();
        defaultScope.add(DEFAULT_SCOPE_NAME);

        String consumerKey = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        String username = tokReqMsgCtx.getAuthorizedUser().getUserName();
        String endUsernameWithDomain = addDomainToName(username, tokReqMsgCtx.getAuthorizedUser().getUserStoreDomain());
        List<String> reqScopeList = Arrays.asList(requestedScopes);
        Map<String, String> restAPIScopesOfCurrentTenant;

        try {
            Map<String, String> appScopes;
            ApiMgtDAO apiMgtDAO = getApiMgtDAOInstance();
            //Get all the scopes and roles against the scopes defined for the APIs subscribed to the application.
            appScopes = apiMgtDAO.getScopeRolesOfApplication(consumerKey);
            //Add API Manager rest API scopes set. This list should be loaded at server start up and keep
            //in memory and add it to each and every request coming.
            String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
            restAPIScopesOfCurrentTenant = (Map) getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache("REST_API_SCOPE_CACHE")
                    .get(tenantDomain);
            if (restAPIScopesOfCurrentTenant != null) {
                appScopes.putAll(restAPIScopesOfCurrentTenant);
            } else {
                restAPIScopesOfCurrentTenant = getRESTAPIScopesFromConfig(getTenantRESTAPIScopesConfig(tenantDomain));
                //call load tenant config for rest API.
                //then put cache
                appScopes.putAll(restAPIScopesOfCurrentTenant);
                getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache("REST_API_SCOPE_CACHE")
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

            int tenantId;
            RealmService realmService = getRealmService();
            UserStoreManager userStoreManager;
            String[] userRoles;

            try {
                tenantId = realmService.getTenantManager().
                        getTenantId(tenantDomain);

                // If tenant Id is not set in the tokenReqContext, deriving it from username.
                if (tenantId == 0 || tenantId == -1) {
                    tenantId = getTenantIdOfUser(username);
                }

                String grantType = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType();
                userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();

                // If GrantType is SAML20_BEARER and CHECK_ROLES_FROM_SAML_ASSERTION is true,
                // use user roles from assertion otherwise use roles from userstore.
                String isSAML2Enabled = System.getProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION);
                if (GrantType.SAML20_BEARER.toString().equals(grantType) && Boolean.parseBoolean(isSAML2Enabled)) {
                    Assertion assertion = (Assertion) tokReqMsgCtx.getProperty(ResourceConstants.SAML2_ASSERTION);
                    userRoles = getRolesFromAssertion(assertion);
                } else {
                    userRoles = userStoreManager.getRoleListOfUser(endUsernameWithDomain);
                }

            } catch (UserStoreException e) {
                //Log and return since we do not want to stop issuing the token in case of scope validation failures.
                log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
                return null;
            }

            if (userRoles == null || userRoles.length == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find roles of the user.");
                }
                return defaultScope;
            }

            List<String> authorizedScopes = new ArrayList<String>();
            List<String> userRoleList = new ArrayList<String>(Arrays.asList(userRoles));

            //Iterate the requested scopes list.
            for (String scope : requestedScopes) {
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
                // The requested scope is defined for the context of the App but no roles have been associated with the
                // scope
                // OR
                // The scope string starts with 'device_'.
                else if (appScopes.containsKey(scope) || isWhiteListedScope(whiteListedScopes, scope)) {
                    authorizedScopes.add(scope);
                }
            }
            if (!authorizedScopes.isEmpty()) {
                return authorizedScopes;
            } else {
                return defaultScope;
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Add domain to name
     *
     * @param username   Username
     * @param domainName domain name
     * @return String
     */
    protected String addDomainToName(String username, String domainName) {
        return UserCoreUtil.addDomainToName(username, domainName);
    }

    /**
     * Get roles from assertion
     *
     * @param assertion Assertion
     * @return String[]
     */
    protected String[] getRolesFromAssertion(Assertion assertion) {
        return APIKeyMgtUtil.getRolesFromAssertion(assertion);
    }

}
