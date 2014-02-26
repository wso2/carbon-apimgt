/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

/**
 * Contains some context information related to an authenticated request. This can be used
 * to access API keys and tier information related to already authenticated requests.
 */
public class AuthenticationContext {

    private boolean authenticated;
    private String username;
    private String applicationTier;
    private String tier;
    private String apiKey;
    private String keyType;
    private String callerToken;
    private String applicationId;
    private String applicationName;
    private String consumerKey;

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return username;
    }

    public String getTier() {
        return tier;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getCallerToken() {
        return callerToken;
    }

    public void setCallerToken(String callerToken) {
        this.callerToken = callerToken;
    }

    public String getApplicationTier() {
        return applicationTier;
    }

    public void setApplicationTier(String applicationTier) {
        this.applicationTier = applicationTier;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }
}
