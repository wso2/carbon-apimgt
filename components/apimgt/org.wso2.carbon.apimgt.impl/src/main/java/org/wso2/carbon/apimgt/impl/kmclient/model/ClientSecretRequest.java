package org.wso2.carbon.apimgt.impl.kmclient.model;

import com.google.gson.annotations.SerializedName;

public class ClientSecretRequest {
    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("description")
    private String description;

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
