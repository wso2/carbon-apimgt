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

    private String customParameters = null;

    private Map additionalProperties = new HashMap();

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

    }

    public EndpointSecurity() {

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

    @Override
    public String toString() {

        return "EndpointSecurity{" + "uniqueIdentifier='" + uniqueIdentifier + '\'' + ", password='" + password + '\''
                + ", type='" + type + '\'' + ", enabled=" + enabled + ", username='" + username + '\'' + ", grantType='"
                + grantType + '\'' + ", tokenUrl='" + tokenUrl + '\'' + ", clientId='" + clientId + '\''
                + ", clientSecret='" + clientSecret + '\'' + ", customParameters='" + customParameters + '\''
                + ", additionalProperties=" + additionalProperties + '}';
    }
}
