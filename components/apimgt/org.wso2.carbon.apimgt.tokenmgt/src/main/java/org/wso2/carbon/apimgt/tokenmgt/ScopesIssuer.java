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

package org.wso2.carbon.apimgt.tokenmgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tokenmgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.tokenmgt.util.TokenMgtDataHolder;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.*;

/**
 * This is the scope issuing delegation class where it picks the matching issuer class
 * with respect to a prefix.
 */
public class ScopesIssuer {

    private static Log log = LogFactory.getLog(ScopesIssuer.class);
    private List<String> scopeSkipList = new ArrayList<>();
    private static Map<String, AbstractScopesIssuer> scopesIssuers;
    private static final String DEFAULT_SCOPE_NAME = "default";
    private static final String CONFIG_ELEM_SCOPE_ISSUER = "OAuthConfigurations.ScopeIssuer";
    /**
     * Singleton of ScopeIssuer.*
     */
    private static ScopesIssuer scopesIssuer;

    public ScopesIssuer() {

    }

    public static void loadInstance(List<String> whitelist) throws TokenMgtException {
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String scopeIssuerClass = apiManagerConfiguration.getFirstProperty(CONFIG_ELEM_SCOPE_ISSUER);
            if (scopeIssuerClass != null) {
                try {
                    scopesIssuer = (ScopesIssuer) APIUtil.getClassForName(scopeIssuerClass).newInstance();
                } catch (ClassNotFoundException ex) {
                    throw new TokenMgtException("Class " + scopeIssuerClass + " could not be found", ex);
                } catch (InstantiationException ex) {
                    throw new TokenMgtException("Class " + scopeIssuerClass + " could not be instantiated", ex);
                } catch (IllegalAccessException ex) {
                    throw new TokenMgtException("Class " + scopeIssuerClass + " could not be accessed", ex);
                }
            } else {
                scopesIssuer = new ScopesIssuer();
            }
        } else {
            scopesIssuer = new ScopesIssuer();
        }
        if (whitelist != null && !whitelist.isEmpty()) {
            scopesIssuer.scopeSkipList.addAll(whitelist);
        }
        scopesIssuers = TokenMgtDataHolder.getScopesIssuers();
    }

    public static ScopesIssuer getInstance() {

        return scopesIssuer;
    }

    /**
     * This method is used to validate the scopes in OAuthCallback and set the authorized scopes back to the
     * callback object.
     *
     * @param scopeValidationCallback OAuthCallback
     * @return true if the requested scopes are authorized, false if no scopes requested or scopes issuers are empty.
     */
    public boolean setScopes(OAuthCallback scopeValidationCallback) {

        List<String> authorizedScopes;
        List<String> sortedScopes;
        Map<String, List<String>> scopeSets;
        boolean isAllAuthorized = false;
        Set<String> authorizedAllScopes = new HashSet<>();

        String[] requestedScopes = scopeValidationCallback.getRequestedScope();
        String[] defaultScope = new String[]{DEFAULT_SCOPE_NAME};

        // if no issuers are defined
        if (scopesIssuers == null || scopesIssuers.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Scope Issuers are not loaded");
            }
            scopeValidationCallback.setApprovedScope(defaultScope);
            return true;
        }

        //If no scopes were requested.
        if (requestedScopes == null || requestedScopes.length == 0) {
            scopeValidationCallback.setApprovedScope(defaultScope);
            return true;
        }

        scopeSets = initializeScopeSets(requestedScopes);
        for (Map.Entry<String, List<String>> entry : scopeSets.entrySet()) {
            sortedScopes = entry.getValue();
            if (!sortedScopes.isEmpty()) {
                scopeValidationCallback.setRequestedScope(sortedScopes.toArray(new String[sortedScopes.size()]));
                authorizedScopes = scopesIssuers.get(entry.getKey()).getScopes(scopeValidationCallback, scopeSkipList);
                authorizedAllScopes.addAll(authorizedScopes);
                isAllAuthorized = true;
            }
        }

        if (isAllAuthorized) {
            scopeValidationCallback
                    .setApprovedScope(authorizedAllScopes.toArray(new String[authorizedAllScopes.size()]));
            return true;
        }
        return false;
    }

    /**
     * This method is used to validate the scopes in OAuthToken Request and set the authorized scopes back to the
     * context.
     *
     * @param tokReqMsgCtx OAuthTokenReqMessageContext
     * @return true if the requested scopes are authorized, false if no scopes requested or scopes issuers are empty.
     */
    public boolean setScopes(OAuthTokenReqMessageContext tokReqMsgCtx) {

        Map<String, List<String>> scopeSets;
        List<String> authorizedScopes;
        List<String> sortedScopes;
        Set<String> authorizedAllScopes = new HashSet<>();
        boolean isAllAuthorized = false;
        String[] requestedScopes = tokReqMsgCtx.getScope();
        String[] defaultScope = new String[]{DEFAULT_SCOPE_NAME};

        // if no issuers are defined
        if (scopesIssuers == null || scopesIssuers.isEmpty()) {

            if (log.isDebugEnabled()) {
                log.debug("Scope Issuers are not loaded");
            }
            tokReqMsgCtx.setScope(defaultScope);
            return true;
        }

        //If no scopes were requested.
        if (requestedScopes == null || requestedScopes.length == 0) {
            tokReqMsgCtx.setScope(defaultScope);
            return true;
        }

        scopeSets = initializeScopeSets(requestedScopes);
        for (Map.Entry<String, List<String>> entry : scopeSets.entrySet()) {
            sortedScopes = entry.getValue();
            if (!sortedScopes.isEmpty()) {
                tokReqMsgCtx.setScope(sortedScopes.toArray(new String[sortedScopes.size()]));
                authorizedScopes = scopesIssuers.get(entry.getKey()).getScopes(tokReqMsgCtx, scopeSkipList);
                authorizedAllScopes.addAll(authorizedScopes);
                isAllAuthorized = true;
            }
        }

        if (isAllAuthorized) {
            tokReqMsgCtx.setScope(authorizedAllScopes.toArray(new String[authorizedAllScopes.size()]));
            return true;
        }
        return false;
    }

    /**
     * Initialize scope sets for the requested scopes with respect to scope issuer prefix
     *
     * @param requestedScopes requested scopes
     * @return initialized scope sets
     */
    private Map<String, List<String>> initializeScopeSets(String[] requestedScopes) {

        Map<String, List<String>> scopeSets = new HashMap<>();
        // initializing scope sets with respect to prefixes
        for (String prefix : scopesIssuers.keySet()) {
            scopeSets.put(prefix, new ArrayList<String>());
        }
        for (String scope : requestedScopes) {
            boolean scopeAssigned = false;
            for (String prefix : scopesIssuers.keySet()) {
                if (scope.startsWith(prefix + ":")) {
                    scopeSets.get(prefix).add(scope);
                    scopeAssigned = true;
                    break;
                }
            }
            if (!scopeAssigned) {
                scopeSets.get(DEFAULT_SCOPE_NAME).add(scope);
            }
        }
        return scopeSets;
    }

}


