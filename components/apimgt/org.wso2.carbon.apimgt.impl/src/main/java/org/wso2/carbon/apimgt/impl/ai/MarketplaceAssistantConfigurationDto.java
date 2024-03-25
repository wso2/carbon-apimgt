package org.wso2.carbon.apimgt.impl.ai;

public class MarketplaceAssistantConfigurationDto {

    private String AccessToken;
    private String Endpoint;
    private String ApiPublishResource;
    private String ChatResource;
    private String ApiDeleteResource;
    private String ApiCountResource;
    private boolean isEnabled;



    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String AccessToken) {
        this.AccessToken = AccessToken;
    }

    public String getEndpoint() {
        return Endpoint;
    }

    public void setEndpoint(String Endpoint) {
        this.Endpoint = Endpoint;
    }

    public String getApiPublishResource() {
        return ApiPublishResource;
    }

    public void setApiPublishResource(String ApiPublishResource) {
        this.ApiPublishResource = ApiPublishResource;
    }

    public String getChatResource() {
        return ChatResource;
    }

    public void setChatResource(String ChatResource) {
        this.ChatResource = ChatResource;
    }

    public String getApiDeleteResource() {
        return ApiDeleteResource;
    }

    public void setApiDeleteResource(String ApiDeleteResource) {
        this.ApiDeleteResource = ApiDeleteResource;
    }

    public String getApiCountResource() {
        return ApiCountResource;
    }

    public void setApiCountResource(String ApiCountResource) {
        this.ApiCountResource = ApiCountResource;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean Enabled) {
        this.isEnabled = Enabled;
    }


}