/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators.oauth.client;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Token Response data object designed to store and transfer
 * the essential data returned from an OAuth-protected backend
 */
public class TokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    private String scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private String expiresIn;

    private Long validTill;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getValidTill() {
        return validTill;
    }

    public void setValidTill(Long validTill) {
        this.validTill = validTill;
    }

    /**
     * TokenResponse constructor to set properties from Map
     * @param tokenMap Map containing the TokenResponse properties
     */
    public TokenResponse(Map<String, String> tokenMap) {
        this.setAccessToken(tokenMap.get("access_token"));
        this.setRefreshToken(tokenMap.get("refresh_token"));
        this.setScope(tokenMap.get("scope"));
        this.setTokenType(tokenMap.get("token_type"));
        this.setExpiresIn(tokenMap.get("expires_in"));
        this.setValidTill(Long.valueOf(tokenMap.get("valid_till")));
    }

    /**
     * Method to convert TokenResponse Object to a HashMap
     * @return HashMap with TokenResponse properties
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", getAccessToken());
        map.put("refresh_token", getRefreshToken());
        map.put("scope", getScope());
        map.put("token_type", getTokenType());
        map.put("expires_in", getExpiresIn());
        if (getValidTill() != null) {
            map.put("valid_till", String.valueOf(getValidTill()));
        }
        return map;
    }

    /**
     * toString method for TokenResponse object
     * @return String of TokenResponse object
     */
    @Override public String toString() {
        return "TokenResponse{" + "accessToken='" + accessToken + '\'' + ", refreshToken='" + refreshToken + '\''
                + ", scope='" + scope + '\'' + ", tokenType='" + tokenType + '\'' + ", expiresIn='" + expiresIn + '\''
                + ", validTill=" + validTill + '}';
    }
}
