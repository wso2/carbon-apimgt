package org.wso2.carbon.apimgt.impl.kmclient.model;

import com.google.gson.annotations.SerializedName;

public class ClientSecret {
    @SerializedName("id")
    private String secretId;
    @SerializedName("description")
    private String description;
    @SerializedName("client_secret")
    private String clientSecret;
    @SerializedName("client_secret_expires_at")
    private Long clientSecretExpiresAt;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Long getClientSecretExpiresAt() {
        return clientSecretExpiresAt;
    }

    public void setClientSecretExpiresAt(Long clientSecretExpiresAt) {
        this.clientSecretExpiresAt = clientSecretExpiresAt;
    }
}
