/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.kmclient.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ClientInfo {

    public ClientInfo () {}

    @SerializedName("client_id")
    private String clientId;
    @SerializedName("client_name")
    private String clientName;
    @SerializedName("client_secret")
    private String clientSecret;
    @SerializedName("token_type_extension")
    private String tokenType;
    @SerializedName("client_secret_expires_at")
    private Long clientSecretExpiredTime;
    @SerializedName("grant_types")
    private List<String> grantTypes;
    @SerializedName("redirect_uris")
    private List<String> redirectUris;
    @SerializedName("ext_param_client_id")
    private String presetClientId;
    @SerializedName("ext_param_client_secret")
    private String presetClientSecret;

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public String getClientName() {

        return clientName;
    }

    public void setClientName(String clientName) {

        this.clientName = clientName;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public Long getClientSecretExpiredTime() {

        return clientSecretExpiredTime;
    }

    public void setClientSecretExpiredTime(Long clientSecretExpiredTime) {

        this.clientSecretExpiredTime = clientSecretExpiredTime;
    }

    public List<String> getGrantTypes() {

        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {

        this.grantTypes = grantTypes;
    }

    public List<String> getRedirectUris() {

        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {

        this.redirectUris = redirectUris;
    }

    public String getTokenType() {

        return tokenType;
    }

    public void setTokenType(String tokenType) {

        this.tokenType = tokenType;
    }

    public String getPresetClientId() {

        return presetClientId;
    }

    public void setPresetClientId(String presetClientId) {

        this.presetClientId = presetClientId;
    }

    public String getPresetClientSecret() {

        return presetClientSecret;
    }

    public void setPresetClientSecret(String presetClientSecret) {

        this.presetClientSecret = presetClientSecret;
    }
}
