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