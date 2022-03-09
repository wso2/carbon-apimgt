/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.apimgt.impl.issuers;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.SystemScopeUtils;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth.common.GrantType;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.model.ResourceScopeCacheEntry;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidationMessageContext;
import org.wso2.carbon.identity.oauth2.validators.scope.ScopeValidator;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.ParseException;
import java.util.*;

import static org.wso2.carbon.identity.oauth2.util.OAuth2Util.getAppInformationByClientId;
import static org.wso2.carbon.registry.core.jdbc.DumpConstants.RESOURCE;

/**
 * This class implements Scope Validator which represents the functions related to an scope issuer which
 * issues scopes based on requested system scopes.
 */
public class SystemScopesIssuer implements ScopeValidator {

    private static Log log = LogFactory.getLog(SystemScopesIssuer.class);
    private static final String DEFAULT_SCOPE_NAME = "default";
    private static final String PRESERVED_CASE_SENSITIVE_VARIABLE = "preservedCaseSensitive";
    private static final String ACCESS_TOKEN_DO = "AccessTokenDO";
    // The following constants are as same as the constants defined in
    // org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants.
    // If any changes are taking place in that these should also be updated accordingly.
    // Setting the "retrieveRolesFromUserStoreForScopeValidation" as a System property which is used when
    // skipping the scope role validation during token issuing using JWT bearer grant.
    public static final String CHECK_ROLES_FROM_SAML_ASSERTION = "checkRolesFromSamlAssertion";
    public static final String RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION =
            "retrieveRolesFromUserStoreForScopeValidation";
    private static final String SCOPE_VALIDATOR_NAME = "System scope validator";
    private IdentityProvider identityProvider = null;
    // set role based scopes issuer as the default
    private static final String ISSUER_PREFIX = "default";

    @Override
    public boolean validateScope(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) throws
            IdentityOAuth2Exception {

        List<String> authScopes = getScopes(oAuthAuthzReqMessageContext);
        oAuthAuthzReqMessageContext.setApprovedScope(authScopes.toArray(new String[authScopes.size()]));
        return true;
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext oAuthTokenReqMessageContext) throws
            IdentityOAuth2Exception {

        List<String> authScopes = getScopes(oAuthTokenReqMessageContext);
        oAuthTokenReqMessageContext.setScope(authScopes.toArray(new String[authScopes.size()]));
        return true;
    }

    @Override
    public boolean validateScope(OAuth2TokenValidationMessageContext oAuth2TokenValidationMessageContext) throws
            IdentityOAuth2Exception {

        AccessTokenDO accessTokenDO = (AccessTokenDO) oAuth2TokenValidationMessageContext.getProperty(ACCESS_TOKEN_DO);
        if (accessTokenDO == null) {
            return false;
        }
        String resource = getResourceFromMessageContext(oAuth2TokenValidationMessageContext);
        // Return true if there is no resource to validate the token against.
        if (resource == null) {
            return true;
        }
        //Get the list of scopes associated with the access token
        String[] scopes = accessTokenDO.getScope();

        //If no scopes are associated with the token
        if (scopes == null || scopes.length == 0) {
            return true;
        }

        String resourceScope = null;
        int resourceTenantId = -1;

        boolean cacheHit = false;
        // Check the cache, if caching is enabled.
        OAuthCacheKey cacheKey = new OAuthCacheKey(resource);
        CacheEntry result = OAuthCache.getInstance().getValueFromCache(cacheKey);

        //Cache hit
        if (result != null && result instanceof ResourceScopeCacheEntry) {
            resourceScope = ((ResourceScopeCacheEntry) result).getScope();
            resourceTenantId = ((ResourceScopeCacheEntry) result).getTenantId();
            cacheHit = true;
        }

        // Cache was not hit. So retrieve from database.
        if (!cacheHit) {
            Pair<String, Integer> scopeMap = OAuthTokenPersistenceFactory.getInstance()
                    .getTokenManagementDAO().findTenantAndScopeOfResource(resource);

            if (scopeMap != null) {
                resourceScope = scopeMap.getLeft();
                resourceTenantId = scopeMap.getRight();
            }

            cacheKey = new OAuthCacheKey(resource);
            ResourceScopeCacheEntry cacheEntry = new ResourceScopeCacheEntry(resourceScope);
            cacheEntry.setTenantId(resourceTenantId);
            //Store resourceScope in cache even if it is null (to avoid database calls when accessing resources for
            //which scopes haven't been defined).
            OAuthCache.getInstance().addToCache(cacheKey, cacheEntry);
        }

        //Return TRUE if - There does not exist a scope definition for the resource
        if (resourceScope == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource '" + resource + "' is not protected with a scope");
            }
            return true;
        }

        List<String> scopeList = new ArrayList<>(Arrays.asList(scopes));

        // If the access token does not bear the scope required for accessing the Resource.
        if (!scopeList.contains(resourceScope)) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.ACCESS_TOKEN)) {
                log.debug("Access token '" + accessTokenDO.getAccessToken() + "' does not bear the scope '" +
                        resourceScope + "'");
            }
            return false;
        }

        // If a federated user and CHECK_ROLES_FROM_SAML_ASSERTION system property is set to true,
        // or if a federated user and RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION system property is false,
        // avoid validating user roles.
        // This system property is set at server start using -D option, Thus will be a permanent property.
        if (accessTokenDO.getAuthzUser().isFederatedUser()
                && (Boolean.parseBoolean(System.getProperty(CHECK_ROLES_FROM_SAML_ASSERTION)) ||
                !(Boolean.parseBoolean(System.getProperty(RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION))))) {
            return true;
        }

        AuthenticatedUser authenticatedUser = OAuthUtil
                .getAuthenticatedUser(oAuth2TokenValidationMessageContext.getResponseDTO().getAuthorizedUser());
        String clientId = accessTokenDO.getConsumerKey();
        List<String> requestedScopes = Arrays.asList(scopes);
        List<String> authorizedScopes = null;

        String[] userRoles = null;
        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser, requestedScopes);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                authorizedScopes = getAllowedScopes(requestedScopes);
                oAuth2TokenValidationMessageContext.getResponseDTO().setScope(authorizedScopes.toArray(
                        new String[authorizedScopes.size()]));
                return true;
            }
            userRoles = getUserRoles(authenticatedUser);
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes);
            oAuth2TokenValidationMessageContext.getResponseDTO().setScope(authorizedScopes.toArray(
                    new String[authorizedScopes.size()]));
        }
        if (ArrayUtils.isEmpty(userRoles)) {
            if (log.isDebugEnabled()) {
                log.debug("No roles associated for the user " + authenticatedUser.getUserName());
            }
            return false;
        }
        return true;
    }

    @Override
    public String getName() {

        return SCOPE_VALIDATOR_NAME;
    }

    public String getPrefix() {

        return ISSUER_PREFIX;
    }

    /**
     * Extract the resource from the access token validation request message
     *
     * @param messageContext Message context of the token validation request
     * @return resource
     */
    private String getResourceFromMessageContext(OAuth2TokenValidationMessageContext messageContext) {

        String resource = null;
        if (messageContext.getRequestDTO().getContext() != null) {
            // Iterate the array of context params to find the 'resource' context param.
            for (OAuth2TokenValidationRequestDTO.TokenValidationContextParam resourceParam :
                    messageContext.getRequestDTO().getContext()) {
                // If the context param is the resource that is being accessed
                if (resourceParam != null && RESOURCE.equals(resourceParam.getKey())) {
                    resource = resourceParam.getValue();
                    break;
                }
            }
        }
        return resource;
    }

    public List<String> getScopes(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) {

        List<String> authorizedScopes = null;
        List<String> requestedScopes = new ArrayList<>();
        if (oAuthAuthzReqMessageContext.getApprovedScope() != null) {
            requestedScopes = Arrays.asList(oAuthAuthzReqMessageContext.getApprovedScope());
        }
        String clientId = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getConsumerKey();
        AuthenticatedUser authenticatedUser = oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getUser();

        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser, requestedScopes);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(requestedScopes);
            }
            String[] userRoles = getUserRoles(authenticatedUser);
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes);
        }
        return authorizedScopes;
    }

    /**
     * This method is used to retrieve authorized scopes with respect to an authorization callback.
     *
     * @param scopeValidationCallback Authorization callback to validate scopes
     * @return authorized scopes list
     */
    public List<String> getScopes(OAuthCallback scopeValidationCallback) {

        List<String> authorizedScopes = null;
        List<String> requestedScopes = Arrays.asList(scopeValidationCallback.getRequestedScope());
        String clientId = scopeValidationCallback.getClient();
        AuthenticatedUser authenticatedUser = scopeValidationCallback.getResourceOwner();

        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser, requestedScopes);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(requestedScopes);
            }
            String[] userRoles = getUserRoles(authenticatedUser);
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes);
        }
        return authorizedScopes;
    }

    /**
     * This method is used to retrieve the authorized scopes with respect to a token.
     *
     * @param tokReqMsgCtx token message context
     * @return authorized scopes list
     */
    public List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx) {

        List<String> authorizedScopes = null;
        List<String> requestedScopes = new ArrayList<>(Arrays.asList(tokReqMsgCtx.getScope()));
        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        AuthenticatedUser authenticatedUser = tokReqMsgCtx.getAuthorizedUser();
        Map<String, String> appScopes = getAppScopes(clientId, authenticatedUser, requestedScopes);
        if (appScopes != null) {
            //If no scopes can be found in the context of the application
            if (isAppScopesEmpty(appScopes, clientId)) {
                return getAllowedScopes(requestedScopes);
            }

            String grantType = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getGrantType();
            String[] userRoles = null;

            // If GrantType is SAML20_BEARER and CHECK_ROLES_FROM_SAML_ASSERTION is true, or if GrantType is
            // JWT_BEARER and retrieveRolesFromUserStoreForScopeValidation system property is true,
            // use user roles from assertion or jwt otherwise use roles from userstore.
            String isSAML2Enabled = System.getProperty(APIConstants.SystemScopeConstants.CHECK_ROLES_FROM_SAML_ASSERTION);
            String isRetrieveRolesFromUserStoreForScopeValidation = System
                    .getProperty(APIConstants.SystemScopeConstants.RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION);
            if (GrantType.SAML20_BEARER.toString().equals(grantType) && Boolean.parseBoolean(isSAML2Enabled)) {
                authenticatedUser.setUserStoreDomain("FEDERATED");
                tokReqMsgCtx.setAuthorizedUser(authenticatedUser);
                Assertion assertion = (Assertion) tokReqMsgCtx.getProperty(APIConstants.SystemScopeConstants.SAML2_ASSERTION);
                userRoles = getRolesFromAssertion(assertion);
            } else if (APIConstants.SystemScopeConstants.OAUTH_JWT_BEARER_GRANT_TYPE.equals(grantType) && !(Boolean
                    .parseBoolean(isRetrieveRolesFromUserStoreForScopeValidation))) {
                configureForJWTGrant(tokReqMsgCtx);
                Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();
                if (tokReqMsgCtx.getProperty(APIConstants.SystemScopeConstants.ROLE_CLAIM) != null) {
                    userRoles = getRolesFromUserAttribute(userAttributes,
                            tokReqMsgCtx.getProperty(APIConstants.SystemScopeConstants.ROLE_CLAIM).toString());
                }
            } else {
                userRoles = getUserRoles(authenticatedUser);
            }
            authorizedScopes = getAuthorizedScopes(userRoles, requestedScopes, appScopes);
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
     * @param userRoles       Roles list of user
     * @param requestedScopes Requested scopes
     * @param appScopes       Scopes of the Application
     * @return authorized scopes list
     */
    private List<String> getAuthorizedScopes(String[] userRoles, List<String> requestedScopes,
                                             Map<String, String> appScopes) {

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
                userRoleList.add(aRole.toLowerCase(Locale.ENGLISH));
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
                        roleList.add(aRole.trim().toLowerCase(Locale.ENGLISH));
                    }
                }
                //Check if user has at least one of the roles associated with the scope
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    authorizedScopes.add(scope);
                }
            } else {
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
            if (null != entry.getKey().getLocalClaim()) {
                if (roleClaim.equals(entry.getKey().getLocalClaim().getClaimUri()) && StringUtils
                        .isNotBlank(entry.getValue())) {
                    return entry.getValue().replace("\\/", "/").
                            replace("[", "").replace("]", "").
                            replace("\"", "").split(FrameworkUtils.getMultiAttributeSeparator());
                }
            }
        }
        return new String[0];
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

        return SystemScopeUtils.getRolesFromAssertion(assertion);
    }

    protected void configureForJWTGrant(OAuthTokenReqMessageContext tokReqMsgCtx) {

        SignedJWT signedJWT = null;
        JWTClaimsSet claimsSet = null;
        String[] roles = null;
        try {
            signedJWT = getSignedJWT(tokReqMsgCtx);
        } catch (IdentityOAuth2Exception e) {
            log.error("Couldn't retrieve signed JWT", e);
        }
        if (signedJWT != null) {
            claimsSet = getClaimSet(signedJWT);
        }
        String jwtIssuer = claimsSet != null ? claimsSet.getIssuer() : null;
        String tenantDomain = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getTenantDomain();

        try {
            identityProvider = IdentityProviderManager.getInstance().getIdPByName(jwtIssuer, tenantDomain);
            if (identityProvider != null) {
                if (StringUtils.equalsIgnoreCase(identityProvider.getIdentityProviderName(), "default")) {
                    identityProvider = this.getResidentIDPForIssuer(tenantDomain, jwtIssuer);
                    if (identityProvider == null) {
                        log.error("No Registered IDP found for the JWT with issuer name : " + jwtIssuer);
                    }
                }
            } else {
                log.error("No Registered IDP found for the JWT with issuer name : " + jwtIssuer);
            }
        } catch (IdentityProviderManagementException | IdentityOAuth2Exception e) {
            log.error("Couldn't initiate identity provider instance", e);
        }

        try {
            roles = claimsSet != null ?
                    claimsSet.getStringArrayClaim(identityProvider.getClaimConfig().getRoleClaimURI()) :
                    null;
        } catch (ParseException e) {
            log.error("Couldn't retrieve roles:", e);
        }

        List<String> updatedRoles = new ArrayList<>();
        if (roles != null) {
            for (String role : roles) {
                String updatedRoleClaimValue = getUpdatedRoleClaimValue(identityProvider, role);
                if (updatedRoleClaimValue != null) {
                    updatedRoles.add(updatedRoleClaimValue);
                } else {
                    updatedRoles.add(role);
                }
            }
        }
        AuthenticatedUser user = tokReqMsgCtx.getAuthorizedUser();
        Map<ClaimMapping, String> userAttributes = user.getUserAttributes();
        String roleClaim = identityProvider.getClaimConfig().getRoleClaimURI();
        if (roleClaim != null) {
            userAttributes
                    .put(ClaimMapping.build(roleClaim, roleClaim, null, false),
                            updatedRoles.toString().replace(" ", ""));
            tokReqMsgCtx.addProperty(APIConstants.SystemScopeConstants.ROLE_CLAIM, roleClaim);
        }
        user.setUserAttributes(userAttributes);
        tokReqMsgCtx.setAuthorizedUser(user);
    }

    /**
     * Method to parse the assertion and retrieve the signed JWT
     *
     * @param tokReqMsgCtx request
     * @return SignedJWT object
     * @throws IdentityOAuth2Exception exception thrown due to a parsing error
     */
    private SignedJWT getSignedJWT(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        RequestParameter[] params = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();
        String assertion = null;
        SignedJWT signedJWT;
        for (RequestParameter param : params) {
            if (param.getKey().equals(APIConstants.SystemScopeConstants.OAUTH_JWT_ASSERTION)) {
                assertion = param.getValue()[0];
                break;
            }
        }
        if (StringUtils.isEmpty(assertion)) {
            String errorMessage = "Error while retrieving assertion";
            throw new IdentityOAuth2Exception(errorMessage);
        }

        try {
            signedJWT = SignedJWT.parse(assertion);
            if (log.isDebugEnabled()) {
                log.debug(signedJWT);
            }
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the JWT.";
            throw new IdentityOAuth2Exception(errorMessage, e);
        }
        return signedJWT;
    }

    /**
     * Method to retrieve claims from the JWT
     *
     * @param signedJWT JWT token
     * @return JWTClaimsSet Object
     */
    private JWTClaimsSet getClaimSet(SignedJWT signedJWT) {

        JWTClaimsSet claimsSet = null;
        try {
            claimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error when trying to retrieve claimsSet from the JWT:", e);
        }
        return claimsSet;
    }

    private IdentityProvider getResidentIDPForIssuer(String tenantDomain, String jwtIssuer)
            throws IdentityOAuth2Exception {

        String issuer = "";
        IdentityProvider residentIdentityProvider;
        try {
            residentIdentityProvider = IdentityProviderManager.getInstance().getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException var7) {
            String errorMsg = String
                    .format("Error while getting Resident Identity Provider of '%s' tenant.", tenantDomain);
            throw new IdentityOAuth2Exception(errorMsg, var7);
        }

        FederatedAuthenticatorConfig[] fedAuthnConfigs = residentIdentityProvider.getFederatedAuthenticatorConfigs();
        FederatedAuthenticatorConfig oauthAuthenticatorConfig = IdentityApplicationManagementUtil.
                getFederatedAuthenticator(fedAuthnConfigs, "openidconnect");
        if (oauthAuthenticatorConfig != null) {
            issuer = IdentityApplicationManagementUtil.getProperty(oauthAuthenticatorConfig.
                    getProperties(), "IdPEntityId").getValue();
        }

        return jwtIssuer.equals(issuer) ? residentIdentityProvider : null;
    }

    /**
     * Check the retireved roles against the role mappings in the IDP and return the updated roles
     *
     * @param identityProvider      used to retrieve the role mappings
     * @param currentRoleClaimValue current roles received through the token
     * @return updated roles
     */
    private String getUpdatedRoleClaimValue(IdentityProvider identityProvider, String currentRoleClaimValue) {

        if (StringUtils.equalsIgnoreCase(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                identityProvider.getIdentityProviderName())) {
            return currentRoleClaimValue;
        }
        currentRoleClaimValue = currentRoleClaimValue.replace("\\/", "/")
                .replace("[", "").replace("]", "")
                .replace("\"", "");

        PermissionsAndRoleConfig permissionAndRoleConfig = identityProvider.getPermissionAndRoleConfig();
        if (permissionAndRoleConfig != null && org.apache.commons.lang3.ArrayUtils.isNotEmpty(
                permissionAndRoleConfig.getRoleMappings())) {
            String[] receivedRoles = currentRoleClaimValue.split(FrameworkUtils.getMultiAttributeSeparator());
            List<String> updatedRoleClaimValues = new ArrayList<>();
            String updatedLocalRole;
            loop:
            for (String receivedRole : receivedRoles) {
                for (RoleMapping roleMapping : permissionAndRoleConfig.getRoleMappings()) {
                    if (roleMapping.getRemoteRole().equals(receivedRole)) {
                        updatedLocalRole = StringUtils.isEmpty(roleMapping.getLocalRole().getUserStoreId())
                                ? roleMapping.getLocalRole().getLocalRoleName()
                                : roleMapping.getLocalRole().getUserStoreId() + UserCoreConstants.DOMAIN_SEPARATOR
                                + roleMapping.getLocalRole().getLocalRoleName();
                        updatedRoleClaimValues.add(updatedLocalRole);
                        continue loop;
                    }
                }
                if (!OAuthServerConfiguration.getInstance().isReturnOnlyMappedLocalRoles()) {
                    updatedRoleClaimValues.add(receivedRole);
                }
            }
            if (!updatedRoleClaimValues.isEmpty()) {
                return StringUtils.join(updatedRoleClaimValues, FrameworkUtils.getMultiAttributeSeparator());
            }
            return null;
        }
        if (!OAuthServerConfiguration.getInstance().isReturnOnlyMappedLocalRoles()) {
            return currentRoleClaimValue;
        }
        return null;
    }

    /**
     * This method is used to get the application scopes including the scopes defined for the APIs subscribed to the
     * application and the API-M REST API scopes set of the current tenant.
     *
     * @param consumerKey       Consumer Key of the application
     * @param authenticatedUser Authenticated User
     * @return Application Scope List
     */
    public Map<String, String> getAppScopes(String consumerKey, AuthenticatedUser authenticatedUser,
                                            List<String> requestedScopes) {

        //Get all the scopes and roles against the scopes defined for the APIs subscribed to the application.
        Map<String, String> appScopes = new HashMap<>();
        String tenantDomain = null;
        try {
            tenantDomain = getAppInformationByClientId(consumerKey).getAppOwner().getTenantDomain();
        } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
            log.error("Error when retrieving the tenant domain " + e.getMessage(), e);
        }
        //Add API Manager rest API scopes set. This list should be loaded at server start up and keep
        //in memory and add it to each and every request coming.
        try {
            Map<String, String> restAPIScopes = SystemScopeUtils.getRESTAPIScopesForTenant(tenantDomain);
            if (!restAPIScopes.isEmpty()) {
                appScopes.putAll(restAPIScopes);
            }
        } catch (APIManagementException e) {
            log.error("Error while getting scopes of application " + e.getMessage(), e);
        }
        return appScopes;
    }

    /**
     * This method is used to check if the application scope list empty.
     *
     * @param appScopes Application scopes list
     * @param clientId  Client ID of the application
     * @return if the scopes list is empty
     */
    public Boolean isAppScopesEmpty(Map<String, String> appScopes, String clientId) {

        if (appScopes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No scopes defined for the Application " + clientId);
            }
            return true;
        }
        return false;
    }

    /**
     * Get the set of default scopes. If a requested scope is matches with the patterns specified in the whitelist,
     * then such scopes will be issued without further validation. If the scope list is empty,
     * token will be issued for default scop1e.
     *
     * @param requestedScopes - The set of requested scopes
     * @return - The subset of scopes that are allowed
     */
    public List<String> getAllowedScopes(List<String> requestedScopes) {

        if (requestedScopes.isEmpty()) {
            requestedScopes.add(DEFAULT_SCOPE_NAME);
        }
        return requestedScopes;
    }


    /**
     * Get RealmService
     *
     * @return RealmService
     */
    protected RealmService getRealmService() {
        return ServiceReferenceHolder.getInstance().getRealmService();
    }

    /**
     * Get tenant Id of the user
     *
     * @param username Username
     * @return int
     */
    protected int getTenantIdOfUser(String username) {
        return IdentityTenantUtil.getTenantIdOfUser(username);
    }
}

