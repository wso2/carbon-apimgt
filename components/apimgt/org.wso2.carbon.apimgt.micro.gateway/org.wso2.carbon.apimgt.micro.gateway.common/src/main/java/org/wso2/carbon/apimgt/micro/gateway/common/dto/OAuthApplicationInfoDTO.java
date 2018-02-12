package org.wso2.carbon.apimgt.micro.gateway.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an OAuth Application created via the Dynamic Client Registration in API Manager
 */
public class OAuthApplicationInfoDTO {

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("callBackURL")
    private String callBackURL;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("isSaasApplication")
    private boolean isSaasApplication;

    @JsonProperty("appOwner")
    private String appOwner;

    @JsonProperty("jsonString")
    private String jsonString;

    @JsonProperty("clientSecret")
    private String clientSecret;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setIsSaasApplication(boolean isSaasApplication) {
        this.isSaasApplication = isSaasApplication;
    }

    public boolean isIsSaasApplication() {
        return isSaasApplication;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String toString() {
        return
                "{" +
                        "\"clientId\":\"" + clientId + "\"," +
                        "\"callBackURL\":\"" + callBackURL + "\"," +
                        "\"clientName\":\"" + clientName + "\"," +
                        "\"isSaasApplication\":" + isSaasApplication + "," +
                        "\"appOwner\":\"" + appOwner + "\"," +
                        "\"jsonString\":\"" + jsonString + "\"," +
                        "\"clientSecret\":\"" + clientSecret + "\"" +
                        "}";
    }
}
