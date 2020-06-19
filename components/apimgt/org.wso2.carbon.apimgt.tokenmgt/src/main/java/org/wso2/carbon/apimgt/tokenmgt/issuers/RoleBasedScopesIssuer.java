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

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.wso2.carbon.apimgt.tokenmgt.handlers.ResourceConstants;
import org.wso2.carbon.apimgt.tokenmgt.util.TokenMgtUtil;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth.common.GrantType;
import org.wso2.carbon.identity.oauth2.grant.jwt.JWTConstants;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents the functions related to an scope issuer which
 * issues scopes based on user roles.
 */
public class RoleBasedScopesIssuer extends AbstractScopesIssuer {

    private static Log log = LogFactory.getLog(RoleBasedScopesIssuer.class);
    private static final String DEFAULT_SCOPE_NAME = "default";
    private static final String PRESERVED_CASE_SENSITIVE_VARIABLE = "preservedCaseSensitive";

    // set role based scopes issuer as the default
    private static final String ISSUER_PREFIX = "default";

    @Override
    public String getPrefix() {
        return ISSUER_PREFIX;
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

        List<String> authorizedScopes = null;
        String[] requestedScopes = scopeValidationCallback.getRequestedScope();
        String clientId = scopeValidationCallback.getClient();
        AuthenticatedUser authenticatedUser = scopeValidationCallback.getResourceOwner();

        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(whiteListedScopes, Arrays.asList(requestedScopes));
            }
            String[] userRoles = getUserRoles(authenticatedUser);
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes, whiteListedScopes);
        }
        return authorizedScopes;
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
        String[] requestedScopes = tokReqMsgCtx.getScope();
        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        AuthenticatedUser authenticatedUser = tokReqMsgCtx.getAuthorizedUser();

        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(whiteListedScopes, Arrays.asList(requestedScopes));
            }

            String grantType = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType();
            String[] userRoles = null;

            // If GrantType is SAML20_BEARER and CHECK_ROLES_FROM_SAML_ASSERTION is true, or if GrantType is
            // JWT_BEARER and retrieveRolesFromUserStoreForScopeValidation system property is true,
            // use user roles from assertion or jwt otherwise use roles from userstore.
            String isSAML2Enabled = System.getProperty(ResourceConstants.CHECK_ROLES_FROM_SAML_ASSERTION);
            String isRetrieveRolesFromUserStoreForScopeValidation = System
                    .getProperty(ResourceConstants.RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION);
            if (GrantType.SAML20_BEARER.toString().equals(grantType) && Boolean.parseBoolean(isSAML2Enabled)) {
                Assertion assertion = (Assertion) tokReqMsgCtx.getProperty(ResourceConstants.SAML2_ASSERTION);
                userRoles = getRolesFromAssertion(assertion);
            } else if (JWTConstants.OAUTH_JWT_BEARER_GRANT_TYPE.equals(grantType) && !(Boolean
                    .parseBoolean(isRetrieveRolesFromUserStoreForScopeValidation))) {
                AuthenticatedUser user = tokReqMsgCtx.getAuthorizedUser();
                Map<ClaimMapping, String> userAttributes = user.getUserAttributes();
                if (tokReqMsgCtx.getProperty(ResourceConstants.ROLE_CLAIM) != null) {
                    userRoles = getRolesFromUserAttribute(userAttributes,
                            tokReqMsgCtx.getProperty(ResourceConstants.ROLE_CLAIM).toString());
                }
            } else {
                userRoles = getUserRoles(authenticatedUser);
            }
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes, whiteListedScopes);
        }
        return authorizedScopes;
    }

    /**
     * This method is used to get roles list of the user.
     *
     * @param authenticatedUser Authenticated user
     * @return roles list
     */
    private String[] getUserRoles(AuthenticatedUser authenticatedUser) {

        String[] userRoles = null;
        String tenantDomain;
        String username;
        if (authenticatedUser.isFederatedUser()) {
            tenantDomain = MultitenantUtils.getTenantDomain(authenticatedUser.getAuthenticatedSubjectIdentifier());
            username = MultitenantUtils.getTenantAwareUsername(authenticatedUser.getAuthenticatedSubjectIdentifier());
        } else {
            tenantDomain = authenticatedUser.getTenantDomain();
            username = authenticatedUser.getUserName();
        }
        String userStoreDomain = authenticatedUser.getUserStoreDomain();
        RealmService realmService = getRealmService();
        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            // If tenant Id is not set in the tokenReqContext, deriving it from username.
            if (tenantId == 0 || tenantId == -1) {
                tenantId = getTenantIdOfUser(username);
            }
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            String endUsernameWithDomain = addDomainToName(username, userStoreDomain);
            userRoles = userStoreManager.getRoleListOfUser(endUsernameWithDomain);

        } catch (UserStoreException e) {
            //Log and return since we do not want to stop issuing the token in case of scope validation failures.
            log.error("Error when getting the tenant's UserStoreManager or when getting roles of user ", e);
        }
        return userRoles;
    }

    /**
     * This method is used to get authorized scopes for user from the requested scopes based on roles.
     *
     * @param userRoles         Roles list of user
     * @param requestedScopes   Requested scopes
     * @param appScopes         Scopes of the Application
     * @param whiteListedScopes Scopes to be whitelisted
     * @return authorized scopes list
     */
    private List<String> getAuthorizedScopes(String[] userRoles, String[] requestedScopes,
                                             Map<String, String> appScopes, List<String> whiteListedScopes) {

        List<String> defaultScope = new ArrayList<>();
        defaultScope.add(DEFAULT_SCOPE_NAME);

        if (userRoles == null || userRoles.length == 0) {
            userRoles = new String[0];
        }

        List<String> authorizedScopes = new ArrayList<>();
        String preservedCaseSensitiveValue = System.getProperty(PRESERVED_CASE_SENSITIVE_VARIABLE);
        boolean preservedCaseSensitive = JavaUtils.isTrueExplicitly(preservedCaseSensitiveValue);
        List<String> userRoleList;
        if (preservedCaseSensitive) {
            userRoleList = Arrays.asList(userRoles);
        } else {
            userRoleList = new ArrayList<>();
            for (String aRole : userRoles) {
                userRoleList.add(aRole.toLowerCase());
            }
        }

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            //Get the set of roles associated with the requested scope.
            String roles = appScopes.get(scope);
            //If the scope has been defined in the context of the App and if roles have been defined for the scope
            if (roles != null && roles.length() != 0) {
                List<String> roleList = new ArrayList<>();
                for (String aRole : roles.split(",")) {
                    if (preservedCaseSensitive) {
                        roleList.add(aRole.trim());
                    } else {
                        roleList.add(aRole.trim().toLowerCase());
                    }
                }
                //Check if user has at least one of the roles associated with the scope
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    authorizedScopes.add(scope);
                }
            }
            //The requested scope is defined for the context of the App but no roles have been associated with the
            //scope OR the scope string starts with 'device_'
            else if (appScopes.containsKey(scope) || isWhiteListedScope(whiteListedScopes, scope)) {
                authorizedScopes.add(scope);
            }
        }
        return (!authorizedScopes.isEmpty()) ? authorizedScopes : defaultScope;
    }

    /**
     * Extract the roles from the user attributes.
     *
     * @param userAttributes retrieved from the token
     * @return roles
     */
    private String[] getRolesFromUserAttribute(Map<ClaimMapping, String> userAttributes, String roleClaim) {

        for (Iterator<Map.Entry<ClaimMapping, String>> iterator = userAttributes.entrySet().iterator(); iterator
                .hasNext(); ) {
            Map.Entry<ClaimMapping, String> entry = iterator.next();
            if (roleClaim.equals(entry.getKey().getLocalClaim().getClaimUri()) && StringUtils
                    .isNotBlank(entry.getValue())) {
                return entry.getValue().replace("\\/", "/").
                        replace("[", "").replace("]", "").
                        replace("\"", "").split(FrameworkUtils.getMultiAttributeSeparator());
            }
        }
        return null;
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
        return TokenMgtUtil.getRolesFromAssertion(assertion);
    }

}
