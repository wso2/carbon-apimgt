/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

/**
 * This class represent the API Key DTO.
 */
public class APIKeyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String apiKey;
    private String keyDisplayName;
    private String applicationId;
    private String keyType;
    private byte[] apiKeyProperties;
    private String authUser;
    private long validityPeriod;
    private String lastUsedTime;
    private String permittedIP;
    private String permittedReferer;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getKeyDisplayName() {
        return keyDisplayName;
    }

    public void setKeyDisplayName(String keyDisplayName) {
        this.keyDisplayName = keyDisplayName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public byte[] getApiKeyProperties() {
        return apiKeyProperties;
    }

    public void setApiKeyProperties(byte[] apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(String lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getPermittedIP() {
        return permittedIP;
    }

    public void setPermittedIP(String permittedIP) {
        this.permittedIP = permittedIP;
    }

    public String getPermittedReferer() {
        return permittedReferer;
    }

    public void setPermittedReferer(String permittedReferer) {
        this.permittedReferer = permittedReferer;
    }
}
