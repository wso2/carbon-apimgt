/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.validators;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.ResourceScopeCacheEntry;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class APIResourceScopeValidator extends OAuth2ScopeValidator {
    private static final Log log = LogFactory.getLog(APIResourceScopeValidator.class);
    public static final String CHECK_ROLES_FROM_SAML_ASSERTION = "checkRolesFromSamlAssertion";
    private static final String SCOPE_VALIDATOR_NAME = "Role based api and product scope validator";

    @Override
    public boolean validateScope(AccessTokenDO accessTokenDO, String resource) throws IdentityOAuth2Exception {

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

        String resourceScopes = null;
        int resourceTenantId = -1;

        boolean cacheHit = false;
        // Check the cache, if caching is enabled.
        OAuthCacheKey cacheKey = new OAuthCacheKey(resource);
        CacheEntry result = OAuthCache.getInstance().getValueFromCache(cacheKey);

        //Cache hit
        if (result != null && result instanceof ResourceScopeCacheEntry) {
            resourceScopes = ((ResourceScopeCacheEntry) result).getScope();
            resourceTenantId = ((ResourceScopeCacheEntry) result).getTenantId();
            cacheHit = true;
        }

        // Cache was not hit. So retrieve from database.
        if (!cacheHit) {
            Pair<String, Integer> scopeMap = OAuthTokenPersistenceFactory.getInstance().getTokenManagementDAO()
                    .findTenantAndScopeOfResource(resource);

            if (scopeMap != null) {
                resourceScopes = scopeMap.getLeft();
                resourceTenantId = scopeMap.getRight();
            }

            cacheKey = new OAuthCacheKey(resource);
            ResourceScopeCacheEntry cacheEntry = new ResourceScopeCacheEntry(resourceScopes);
            cacheEntry.setTenantId(resourceTenantId);
            //Store resourceScope in cache even if it is null (to avoid database calls when accessing resources for
            //which scopes haven't been defined).
            OAuthCache.getInstance().addToCache(cacheKey, cacheEntry);

        }

        String[] subscriptionDetails =  accessTokenDO.getTokenType().split(":");
        String subscriptionType = "API";
        if (subscriptionDetails.length > 1) {
            subscriptionType = subscriptionDetails[1];
        } else {
            log.info("Subscription Type was not set properly in accesstoken data object. So type is considered as "
                    + "default  'API'.");
        }

        List<String> tokenScopeList = new ArrayList<>(Arrays.asList(scopes));
        List<String> resourceScopeList = Arrays.asList(resourceScopes.split(","));
        String apiResourceScope = null;
        List<String> productResourceScopes = new ArrayList<String>();

        //filter resource and product scopes
        for (String scope : resourceScopeList) {
            if (scope.startsWith("productscope")) {
                productResourceScopes.add(scope);
            } else {
                apiResourceScope = scope;
            }
        }

        //if requset type is APIProduct add an additional scope validation step
        if (subscriptionType.equals("APIProduct")) {
            //extract the api product identified in subscription validation phase and carry out scope validation for the
            //scope relevant to that product
            String apiProductName = "";
            String apiProductProvider = "";
            String productScope = "";
            if (subscriptionDetails.length == 3) {
                String apiProductID = subscriptionDetails[2];

                String[] productNameProviderPair = apiProductID.split("-");
                if (productNameProviderPair.length == 2) {
                    apiProductProvider = productNameProviderPair[0];
                    apiProductName = productNameProviderPair[1];
                }

                productScope = APIUtil.getProductScope(new APIProductIdentifier(apiProductProvider, apiProductName));
            }

            if (StringUtils.isNotEmpty(productScope)) {
                if (resourceScopeList.contains(productScope) ) {
                    if (!tokenScopeList.contains(productScope)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Access token does not bear the required product scope");
                        }
                        return false;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Requested resource is not allowed in api product " + apiProductName + " by " +
                        apiProductProvider);
                    }
                    return false;
                }
            }
        }

        //Return TRUE if - There does not exist a scope definition for the resource
        if (apiResourceScope == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource '" + resource + "' is not protected with a scope");
            }
            return true;
        }

        //If the access token does not bear the scope required for accessing the Resource.
        if (!tokenScopeList.contains(apiResourceScope)) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.ACCESS_TOKEN)) {
                log.debug("Access token '" + accessTokenDO.getAccessToken() + "' does not bear the scope '"
                        + apiResourceScope + "'");
            }
            return false;
        }

        // If a federated user and CHECK_ROLES_FROM_SAML_ASSERTION system property is set to true,
        // avoid validating user roles.
        // This system property is set at server start using -D option, Thus will be a permanent property.
        if (accessTokenDO.getAuthzUser().isFederatedUser() && Boolean.parseBoolean(System.getProperty(CHECK_ROLES_FROM_SAML_ASSERTION))) {
            return true;
        }

        try {
            User authzUser = accessTokenDO.getAuthzUser();
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(authzUser.getUserName());
            int tenantId = APIUtil.getTenantId(tenantAwareUserName);

            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            String[] userRoles = userStoreManager.getRoleListOfUser(tenantAwareUserName);
            //String[] userRoles = APIUtil.getListOfRoles(authzUser.getUserName());

            if (ArrayUtils.isEmpty(userRoles)) {
                if (log.isDebugEnabled()) {
                    log.debug("No roles associated for the user " + authzUser.getUserName());
                }
                return false;
            }

            //return true;
            return isUserAuthorizedForScope(apiResourceScope, authzUser.getUserName(), userRoles, tenantId);

        } catch (UserStoreException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public String getValidatorName() {
        return SCOPE_VALIDATOR_NAME;
    }

    private boolean isUserAuthorizedForScope(String scopeName, String provider, String[] userRoles, int tenantId)
            throws IdentityOAuth2Exception {

        Set<String> rolesOfScope = OAuthTokenPersistenceFactory.getInstance().getOAuthScopeDAO().
                getBindingsOfScopeByScopeName(scopeName, tenantId);

        if (CollectionUtils.isEmpty(rolesOfScope)) {
            if (log.isDebugEnabled()) {
                log.debug("Did not find any roles associated to the scope " + scopeName);
            }
            return true;
        }

        if (log.isDebugEnabled()) {
            StringBuilder logMessage = new StringBuilder("Found roles of scope '" + scopeName + "' ");

            logMessage.append(StringUtils.join(rolesOfScope, ","));
            log.debug(logMessage.toString());
        }

        if (ArrayUtils.isEmpty(userRoles)) {
            if (log.isDebugEnabled()) {
                log.debug("User does not have required roles for scope " + scopeName);
            }
            return false;
        }
        //Check if the user still has a valid role for this scope.
        rolesOfScope.retainAll(Arrays.asList(userRoles));

        if (rolesOfScope.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("User does not have required roles for scope " + scopeName);
            }
            return false;
        }

        return true;
    }

}
