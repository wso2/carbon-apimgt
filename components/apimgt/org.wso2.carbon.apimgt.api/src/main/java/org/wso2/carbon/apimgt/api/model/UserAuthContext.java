package org.wso2.carbon.apimgt.api.model;

public class UserAuthContext {
    private String token;

    public UserAuthContext() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
