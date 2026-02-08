/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.api.model;

import java.util.Map;

public class APIKeyInfo {
    private String keyDisplayName;
    private int appId;
    private String applicationId;
    private String keyType;
    private String lastUsedTime;
    private String createdTime;
    private String salt;
    private String lookupKey;
    private long validityPeriod;
    private byte[] properties;
    private Map<String, String> additionalProperties;
    private String apiKeyHash;
    private String apiKey;
    private String authUser;
    private String status;

    public String getKeyDisplayName() {
        return keyDisplayName;
    }

    public void setKeyDisplayName(String keyDisplayName) {
        this.keyDisplayName = keyDisplayName;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
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

    public String getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(String lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public byte[] getProperties() {
        return properties;
    }

    public void setProperties(byte[] properties) {
        this.properties = properties;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
