/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.api.model;

import java.util.HashMap;
import java.util.Map;

public class EndpointSecurity {

    private String uniqueIdentifier = null;

    private String password = null;

    private String type = null;

    private boolean enabled = false;

    private String username = null;

    private String grantType = null;

    private String tokenUrl = null;

    private String clientId = null;

    private String clientSecret = null;

    private String apiKeyIdentifier = null;

    private String apiKeyValue = null;

    private String apiKeyIdentifierType = null;

    private String customParameters = null;

    private Map additionalProperties = new HashMap();

    private int connectionTimeoutDuration = -1;

    private int connectionRequestTimeoutDuration = -1;

    private int socketTimeoutDuration = -1;

    private ProxyConfigs proxyConfigs;

    public EndpointSecurity(EndpointSecurity endpointSecurity) {

        this.uniqueIdentifier = endpointSecurity.uniqueIdentifier;
        this.password = endpointSecurity.password;
        this.type = endpointSecurity.type;
        this.enabled = endpointSecurity.enabled;
        this.username = endpointSecurity.username;
        this.grantType = endpointSecurity.grantType;
        this.tokenUrl = endpointSecurity.tokenUrl;
        this.clientId = endpointSecurity.clientId;
        this.clientSecret = endpointSecurity.clientSecret;
        this.customParameters = endpointSecurity.customParameters;
        this.additionalProperties = endpointSecurity.additionalProperties;
        this.connectionTimeoutDuration = endpointSecurity.connectionTimeoutDuration;
        this.connectionRequestTimeoutDuration = endpointSecurity.connectionRequestTimeoutDuration;
        this.socketTimeoutDuration = endpointSecurity.socketTimeoutDuration;
        this.proxyConfigs = endpointSecurity.proxyConfigs;
    }

    public EndpointSecurity() {

    }
    public ProxyConfigs getProxyConfigs() {
        return proxyConfigs;
    }

    public void setProxyConfigs(ProxyConfigs proxyConfigs) {
        this.proxyConfigs = proxyConfigs;
    }

    public String getUniqueIdentifier() {

        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {

        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getGrantType() {

        return grantType;
    }

    public void setGrantType(String grantType) {

        this.grantType = grantType;
    }

    public String getTokenUrl() {

        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {

        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public String getCustomParameters() {

        return customParameters;
    }

    public void setCustomParameters(String customParameters) {

        this.customParameters = customParameters;
    }

    public Map getAdditionalProperties() {

        return additionalProperties;
    }

    public void setAdditionalProperties(Map additionalProperties) {

        this.additionalProperties = additionalProperties;
    }
    public String getApiKeyIdentifier() {

        return apiKeyIdentifier;
    }

    public void setApiKeyIdentifier(String apiKeyIdentifier) {

        this.apiKeyIdentifier = apiKeyIdentifier;
    }

    public String getApiKeyValue() {

        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {

        this.apiKeyValue = apiKeyValue;
    }

    public String getApiKeyIdentifierType() {

        return apiKeyIdentifierType;
    }

    public void setApiKeyIdentifierType(String apiKeyIdentifierType) {

        this.apiKeyIdentifierType = apiKeyIdentifierType;
    }

    public int getConnectionTimeoutDuration() {
        return connectionTimeoutDuration;
    }

    public void setConnectionTimeoutDuration(int connectionTimeoutDuration) {
        this.connectionTimeoutDuration = connectionTimeoutDuration;
    }

    public int getConnectionRequestTimeoutDuration() {
        return connectionRequestTimeoutDuration;
    }

    public void setConnectionRequestTimeoutDuration(int connectionRequestTimeoutDuration) {
        this.connectionRequestTimeoutDuration = connectionRequestTimeoutDuration;
    }

    public int getSocketTimeoutDuration() {
        return socketTimeoutDuration;
    }

    public void setSocketTimeoutDuration(int socketTimeoutDuration) {
        this.socketTimeoutDuration = socketTimeoutDuration;
    }

    public static class ProxyConfigs {
        private boolean proxyEnabled;
        private String proxyHost;
        private String proxyPort;
        private String proxyProtocol;
        private String proxyUsername;
        private String proxyPassword;

        public boolean isProxyEnabled() {
            return proxyEnabled;
        }

        public void setProxyEnabled(boolean proxyEnabled) {
            this.proxyEnabled = proxyEnabled;
        }

        public String getProxyHost() {
            return proxyHost;
        }

        public void setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
        }

        public String getProxyPort() {
            return proxyPort;
        }

        public void setProxyPort(String proxyPort) {
            this.proxyPort = proxyPort;
        }

        public String getProxyProtocol() {
            return proxyProtocol;
        }

        public void setProxyProtocol(String proxyProtocol) {
            this.proxyProtocol = proxyProtocol;
        }

        public String getProxyUsername() {
            return proxyUsername;
        }

        public void setProxyUsername(String proxyUsername) {
            this.proxyUsername = proxyUsername;
        }

        public String getProxyPassword() {
            return proxyPassword;
        }

        public void setProxyPassword(String proxyPassword) {
            this.proxyPassword = proxyPassword;
        }
    }

    @Override
    public String toString() {

        return "EndpointSecurity{" +
                "uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                ", enabled=" + enabled +
                ", username='" + username + '\'' +
                ", grantType='" + grantType + '\'' +
                ", tokenUrl='" + tokenUrl + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", apiKeyIdentifier='" + apiKeyIdentifier + '\'' +
                ", apiKeyValue='" + apiKeyValue + '\'' +
                ", apiKeyIdentifierType='" + apiKeyIdentifierType + '\'' +
                ", customParameters='" + customParameters + '\'' +
                ", additionalProperties=" + additionalProperties +
                ", connectionTimeoutDuration=" + connectionTimeoutDuration +
                ", connectionRequestTimeoutDuration=" + connectionRequestTimeoutDuration +
                ", socketTimeoutDuration=" + socketTimeoutDuration +
                '}';
    }
}
