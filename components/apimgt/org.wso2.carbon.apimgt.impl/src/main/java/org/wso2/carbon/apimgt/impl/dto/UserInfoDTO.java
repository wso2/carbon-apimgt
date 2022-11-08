/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @SerializedName("username")
    private String username;
    @SerializedName("domain")
    private String domain;
    @SerializedName("dialect")
    private String dialectURI;
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("bindFederatedUserClaims")
    private boolean bindFederatedUserClaims;
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getDialectURI() {
        return dialectURI;
    }
    public void setDialectURI(String dialectURI) {
        this.dialectURI = dialectURI;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public boolean isBindFederatedUserClaims() { return bindFederatedUserClaims; }
    public void setBindFederatedUserClaims(Boolean bindFederatedUserClaims) {
        this.bindFederatedUserClaims = bindFederatedUserClaims;
    }
}
