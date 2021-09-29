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
    @SerializedName("ext_application_owner")
    private String application_owner;
    @SerializedName("ext_application_token_lifetime")
    private Long applicationAccessTokenLifeTime;
    @SerializedName("ext_user_token_lifetime")
    private Long userAccessTokenLifeTime;
    @SerializedName("ext_refresh_token_lifetime")
    private Long refreshTokenLifeTime;
    @SerializedName("ext_id_token_lifetime")
    private Long idTokenLifeTime;
    @SerializedName("application_display_name")
    private String applicationDisplayName;
    @SerializedName("pkceMandatory")
    private Boolean pkceMandatory;
    @SerializedName("pkceSupportPlain")
    private Boolean pkceSupportPlain;
    @SerializedName("bypassClientCredentials")
    private Boolean bypassClientCredentials;

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

    public String getApplication_owner() {

        return application_owner;
    }

    public void setApplication_owner(String application_owner) {

        this.application_owner = application_owner;
    }

    public Long getApplicationAccessTokenLifeTime() {

        return applicationAccessTokenLifeTime;
    }

    public void setApplicationAccessTokenLifeTime(Long applicationAccessTokenLifeTime) {

        this.applicationAccessTokenLifeTime = applicationAccessTokenLifeTime;
    }

    public Long getUserAccessTokenLifeTime() {

        return userAccessTokenLifeTime;
    }

    public void setUserAccessTokenLifeTime(Long userAccessTokenLifeTime) {

        this.userAccessTokenLifeTime = userAccessTokenLifeTime;
    }

    public Long getRefreshTokenLifeTime() {

        return refreshTokenLifeTime;
    }

    public void setRefreshTokenLifeTime(Long refreshTokenLifeTime) {

        this.refreshTokenLifeTime = refreshTokenLifeTime;
    }

    public Long getIdTokenLifeTime() {

        return idTokenLifeTime;
    }

    public void setIdTokenLifeTime(Long idTokenLifeTime) {

        this.idTokenLifeTime = idTokenLifeTime;
    }

    public String getApplicationDisplayName() {

        return applicationDisplayName;
    }

    public void setApplicationDisplayName(String applicationDisplayName) {

        this.applicationDisplayName = applicationDisplayName;
    }

    public Boolean getPkceMandatory() {
        return pkceMandatory;
    }

    public void setPkceMandatory(Boolean pkceMandatory) {
        this.pkceMandatory = pkceMandatory;
    }

    public Boolean getPkceSupportPlain() {
        return pkceSupportPlain;
    }

    public void setPkceSupportPlain(Boolean pkceSupportPlain) {
        this.pkceSupportPlain = pkceSupportPlain;
    }

    public Boolean getBypassClientCredentials() {
        return bypassClientCredentials;
    }

    public void setBypassClientCredentials(Boolean bypassClientCredentials) {
        this.bypassClientCredentials = bypassClientCredentials;
    }
}
