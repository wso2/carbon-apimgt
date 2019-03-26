/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent a request for Dynamic Client Registration API in API Manager
 */
public class OAuthApplicationRequestDTO {

    @JsonProperty("tokenScope")
    private String tokenScope;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("clientName")
    private String clientName;

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    @JsonProperty("grantType")
    private String grantType;

    @JsonProperty("saasApp")
    private boolean saasApp;

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    public String getTokenScope() {
        return tokenScope;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setAppCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getAppCallbackUrl() {
        return callbackUrl;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setSaasApp(boolean saasApp) {
        this.saasApp = saasApp;
    }

    public boolean isSaasApp() {
        return saasApp;
    }

    @Override
    public String toString() {
        return
                "{" +
                        "\"callbackUrl\":\"" + callbackUrl + "\"," +
                        "\"clientName\":\"" + clientName + "\"," +
                        "\"tokenScope\":\"" + tokenScope + "\"," +
                        "\"owner\":\"" + owner + "\"," +
                        "\"grantType\":\"" + grantType + "\"," +
                        "\"saasApp\":" + saasApp +
                "}";
    }
}
