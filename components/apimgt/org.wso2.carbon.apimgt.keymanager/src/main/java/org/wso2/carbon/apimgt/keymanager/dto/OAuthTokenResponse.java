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
import java.util.List;

/**
 * The access_token response class
 */
public class OAuthTokenResponse implements Serializable {

    @SerializedName ("access_token")
    private String access_token;

    @SerializedName("refresh_token")
    private String refresh_token;

    @SerializedName("expires_in")
    private long expires_in;

    @SerializedName("scope")
    private List<String> scope;

    @SerializedName("expires_timestamp")
    private long expiresTimestamp;

    public long getExpiresTimestamp() {
        return expiresTimestamp;
    }

    public void setExpiresTimestamp(long expiresTimestamp) {
        this.expiresTimestamp = expiresTimestamp;
    }

    private static final long serialVersionUID = 2;

    public String getToken() {
        return access_token;
    }

    public void setToken(String token) {
        this.access_token = token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }

    public long getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(long expires_in) {
        this.expires_in = expires_in;
    }

    public List<String> getScopes() {
        return scope;
    }

    public void setScopes(List<String> scopes) {
        this.scope = scopes;
    }
}
