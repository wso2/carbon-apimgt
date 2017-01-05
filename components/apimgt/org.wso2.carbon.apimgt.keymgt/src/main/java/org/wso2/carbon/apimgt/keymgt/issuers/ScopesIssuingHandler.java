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
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.*;

/**
 * This is the scope issuing delegation class where it picks the matching issuer class
 * with respect to a prefix.
 */
public class ScopesIssuingHandler {

    private static Log log = LogFactory.getLog(ScopesIssuingHandler.class);
    private List<String> scopeSkipList = new ArrayList<String>();
    private static Map<String, ScopesIssuer> scopesIssuers;
    private static final String DEFAULT_SCOPE_NAME = "default";
    /**
     * Singleton of ScopeIssuer.*
     */
    private static ScopesIssuingHandler scopesIssuingHandler;
    
    private ScopesIssuingHandler() {
    }

    public static void loadInstance(List<String> whitelist) {
        scopesIssuingHandler = new ScopesIssuingHandler();
        if (whitelist != null && !whitelist.isEmpty()) {
            scopesIssuingHandler.scopeSkipList.addAll(whitelist);
        }
        scopesIssuers = APIKeyMgtDataHolder.getScopesIssuers();
    }  

    public static ScopesIssuingHandler getInstance() {
        return scopesIssuingHandler;
    }

    public boolean setScopes(OAuthTokenReqMessageContext tokReqMsgCtx) {

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

        Map<String, List<String>> scopeSets = new HashMap<String, List<String>>();

        // initializing scope sets with respect to prefixes
        for (String prefix : scopesIssuers.keySet()) {
            scopeSets.put(prefix, new ArrayList<String>());
        }

        for (String scope : requestedScopes) {
            boolean scopeAssigned = false;
            for (String prefix : scopesIssuers.keySet()) {
                if (scope.startsWith(prefix)) {
                    scopeSets.get(prefix).add(scope);
                    scopeAssigned = true;
                    break;
                }
            }
            if (!scopeAssigned) {
                scopeSets.get(DEFAULT_SCOPE_NAME).add(scope);
            }
        }

        Set<String> authorizedAllScopes = new HashSet<String>();
        List<String> authorizedScopes;
        List<String> sortedScopes;
        boolean isAllAuthorized = false;
        for (String prefix : scopeSets.keySet()) {
            sortedScopes = scopeSets.get(prefix);
            if (sortedScopes.size() > 0) {
                tokReqMsgCtx.setScope(sortedScopes.toArray(new String[sortedScopes.size()]));
                authorizedScopes = scopesIssuers.get(prefix).getScopes(tokReqMsgCtx, scopeSkipList);
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

}


