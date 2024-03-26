package org.wso2.carbon.apimgt.impl.ai;

public class ApiChatConfigurationDto {

    private String accessToken;
    private String endpoint;
    private String prepareResource;
    private String executeResource;
    private boolean isEnabled;
    private boolean isAuthTokenProvided;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPrepareResource() {
        return prepareResource;
    }

    public void setPrepareResource(String prepareResource) {
        this.prepareResource = prepareResource;
    }

    public String getExecuteResource() {
        return executeResource;
    }

    public void setExecuteResource(String executeResource) {
        this.executeResource = executeResource;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isAuthTokenProvided() {
        return isAuthTokenProvided;
    }

    public void setAuthTokenProvided(boolean authTokenProvided) {
        this.isAuthTokenProvided = authTokenProvided;
    }
}
