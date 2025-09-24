/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.Set;

public class APISpecParserUtil {

    private static final Log log = LogFactory.getLog(APISpecParserUtil.class);

    /**
     * Find scope object in a set based on the key
     *
     * @param scopes - Set of scopes
     * @param key    - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes, String key) {
        if (scopes == null) {
            if (log.isDebugEnabled()) {
                log.debug("Scopes set is null, returning null for key: " + key);
            }
            return null;
        }
        
        if (key == null) {
            if (log.isDebugEnabled()) {
                log.debug("Search key is null, returning null");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Searching for scope with key: " + key + " in " + scopes.size() + " scopes");
        }

        for (Scope scope : scopes) {
            if (scope != null && scope.getKey() != null && scope.getKey().equals(key)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found scope with key: " + key);
                }
                return scope;
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Scope not found for key: " + key);
        }
        return null;
    }
}
