/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores configurations specific for a KeyManager implementation. Certain features will be made available based on
 * the values set in this Config.
 */
public class KeyManagerConfiguration {

    private String name;
    private String type;
    private boolean enabled;
    private String tenantDomain;

    public enum TokenType {
        EXCHANGED, DIRECT, BOTH
    }

    private TokenType tokenType = TokenType.DIRECT;

    private Map<String, Object> configuration = new HashMap<>();

    public void addParameter(String name, Object value) {

        configuration.put(name, value);
    }

    public Object getParameter(String name) {

        return configuration.get(name);
    }

    public void setConfiguration(Map<String, Object> configuration) {

        this.configuration = configuration;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public Map<String, Object> getConfiguration() {

        return configuration;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}
