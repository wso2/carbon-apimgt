package org.wso2.carbon.apimgt.impl.ai;

public class MarketplaceAssistantConfigurationDto {

    private String accessToken;
    private String endpoint;
    private String apiPublishResource;
    private String chatResource;
    private String apiDeleteResource;
    private String apiCountResource;
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

    public String getApiPublishResource() {
        return apiPublishResource;
    }

    public void setApiPublishResource(String apiPublishResource) {
        this.apiPublishResource = apiPublishResource;
    }

    public String getChatResource() {
        return chatResource;
    }

    public void setChatResource(String chatResource) {
        this.chatResource = chatResource;
    }

    public String getApiDeleteResource() {
        return apiDeleteResource;
    }

    public void setApiDeleteResource(String apiDeleteResource) {
        this.apiDeleteResource = apiDeleteResource;
    }

    public String getApiCountResource() {
        return apiCountResource;
    }

    public void setApiCountResource(String apiCountResource) {
        this.apiCountResource = apiCountResource;
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