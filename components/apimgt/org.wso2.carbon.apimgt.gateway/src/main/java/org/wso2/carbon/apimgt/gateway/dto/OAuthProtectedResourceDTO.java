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

/**
 * Holds information related to OAuth protected resources.
 * This DTO is used to store the authorization servers and resource scopes
 * associated with a protected resource in the API Gateway.
 */
public class OAuthProtectedResourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("resource")
    private String resource;

    @SerializedName("authorization_servers")
    private final List<String> authorizationServers = new java.util.ArrayList<>();

    @SerializedName("scopes_supported")
    private final List<String> scopesSupported = new java.util.ArrayList<>();

    public List<String> getAuthorizationServers() {
        return authorizationServers;
    }

    public void addAuthorizationServers(List<String> authorizationServers) {
        if (authorizationServers != null) {
            this.authorizationServers.addAll(authorizationServers);
        }
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public List<String> getScopesSupported() {
        return scopesSupported;
    }

    public void addScopesSupported(List<String> resourceScopes) {
        if (resourceScopes != null) {
            this.scopesSupported.addAll(resourceScopes);
        }
    }

    public void addAuthorizationServer(String server) {
        authorizationServers.add(server);
    }

    public void addScopeSupported(String scope) {
        scopesSupported.add(scope);
    }
}
