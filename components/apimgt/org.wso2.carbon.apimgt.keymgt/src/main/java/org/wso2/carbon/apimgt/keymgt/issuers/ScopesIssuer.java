/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.util.ArrayList;
import java.util.List;

public abstract class ScopesIssuer {

    private static final String DEFAULT_SCOPE_NAME = "default";

    public abstract List<String> getScopes(OAuthTokenReqMessageContext tokReqMsgCtx, List<String> whiteListedScopes);

    public abstract String getPrefix();

    /**
     * Get the set of default scopes. If a requested scope is matches with the patterns specified in the whitelist,
     * then such scopes will be issued without further validation. If the scope list is empty,
     * token will be issued for default scop1e.
     *
     * @param requestedScopes - The set of requested scopes
     * @return - The subset of scopes that are allowed
     */
    public List<String> getAllowedScopes(List<String> scopeSkipList, List<String> requestedScopes) {
        List<String> authorizedScopes = new ArrayList<String>();

        //Iterate the requested scopes list.
        for (String scope : requestedScopes) {
            if (isWhiteListedScope(scopeSkipList, scope)) {
                authorizedScopes.add(scope);
            }
        }

        if (authorizedScopes.isEmpty()) {
            authorizedScopes.add(DEFAULT_SCOPE_NAME);
        }
        return authorizedScopes;
    }

    /**
     * Determines if the scope is specified in the whitelist.
     *
     * @param scope - The scope key to check
     * @return - 'true' if the scope is white listed. 'false' if not.
     */
    public boolean isWhiteListedScope(List<String> scopeSkipList, String scope) {
        for (String scopeTobeSkipped : scopeSkipList) {
            if (scope.matches(scopeTobeSkipped)) {
                return true;
            }
        }
        return false;
    }
}


