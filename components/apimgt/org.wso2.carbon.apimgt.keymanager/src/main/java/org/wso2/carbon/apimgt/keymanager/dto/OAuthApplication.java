/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.keymanager.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 *The Data-holder for OAuth Applications in the Key Manager Service
 */
public class OAuthApplication implements Serializable {

    @SerializedName ("client_name")
    private String client_name;

    @SerializedName("client_id")
    private String client_id;

    @SerializedName("client_secret")
    private String client_secret;

    @SerializedName("redirect_uris")
    private String[] redirect_uris;

    @SerializedName("grant_types")
    private String[] grant_types;

    @SerializedName("access_token")
    private String access_token;

    @SerializedName("refresh_token")
    private String refresh_token;
    
    @SerializedName("app_owner")
    private String app_owner;


    private static final long serialVersionUID = 1;

    public OAuthApplication() {
        this.client_name = "";
    }

    public String getClientName() {
        return client_name;
    }

    public void setClientName(String client_name) {
        this.client_name = client_name;
    }

    public String getClientId() {
        return client_id;
    }

    public void setClientId(String client_id) {
        this.client_id = client_id;
    }

    public String getClientSecret() {
        return client_secret;
    }

    public void setClientSecret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }


    public String getAppOwner() {
        return app_owner;
    }

    public void setAppOwner(String app_owner) {
        this.app_owner = app_owner;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OAuthApplication {\n");

        sb.append("    client_name: ").append(toIndentedString(client_name)).append("\n");
        sb.append("    client_id: ").append(toIndentedString(client_id)).append("\n");
        sb.append("    client_secret: ").append(toIndentedString(client_secret)).append("\n");
        sb.append("    redirect_uris: ").append(toIndentedString(redirect_uris)).append("\n");
        sb.append("    grant_types: ").append(toIndentedString(grant_types)).append("\n");
        sb.append("    app_owner: ").append(toIndentedString(app_owner)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
