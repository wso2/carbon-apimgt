/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class OAuthProtectedResourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("authorization_servers")
    private List<String> authorizationServers = new java.util.ArrayList<>();

    @SerializedName("resource_scopes")
    private final List<String> resourceScopes = new java.util.ArrayList<>();

    public List<String> getAuthorizationServers() {
        return authorizationServers;
    }

    public void addAuthorizationServers(List<String> authorizationServers) {
        if (authorizationServers != null) {
            this.authorizationServers.addAll(authorizationServers);
        }
    }

    public List<String> getResourceScopes() {
        return resourceScopes;
    }

    public void addResourceScopes(List<String> resourceScopes) {
        if (resourceScopes != null) {
            this.resourceScopes.addAll(resourceScopes);
        }
    }

    public void addAuthorizationServer(String server) {
        authorizationServers.add(server);
    }

    public void addResourceScope(String scope) {
        resourceScopes.add(scope);
    }
}
