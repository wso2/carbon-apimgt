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

    @JsonProperty("jsonAppAttribute")
    private String jsonAppAttribute;

    @JsonProperty("tokenType")
    private String tokenType;

    public boolean isSaasApplication() {
        return isSaasApplication;
    }

    public void setSaasApplication(boolean saasApplication) {
        isSaasApplication = saasApplication;
    }

    public String getJsonAppAttribute() {
        return jsonAppAttribute;
    }

    public void setJsonAppAttribute(String jsonAppAttribute) {
        this.jsonAppAttribute = jsonAppAttribute;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

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
