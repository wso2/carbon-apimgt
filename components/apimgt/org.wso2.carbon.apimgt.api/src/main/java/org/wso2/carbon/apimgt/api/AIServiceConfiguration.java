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

package org.wso2.carbon.apimgt.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Neutral carrier for the configuration handed to an {@link AIService} implementation through
 * {@link AIService#init(AIServiceConfiguration)}.
 * <p>
 * It models the settings common to the AI-service integrations - the service {@code endpoint}, an optional
 * {@code tokenEndpoint}, and either an API {@code key} or an {@code accessToken} - as typed fields, while everything
 * that varies between the individual assistants (for example the resource paths of the WSO2-hosted service) is carried
 * in a generic {@link #getProperties() property bag}. Keeping this a plain value object in the {@code api} module (as
 * opposed to the {@code impl}-module {@code *ConfigurationDTO} classes) is what lets a custom implementation be built
 * against {@code api} alone. This follows the same "typed common fields + property map" shape as
 * {@code KeyManagerConfiguration}.
 */
public class AIServiceConfiguration {

    private boolean enabled;
    private String endpoint;
    private String tokenEndpoint;
    private String key;
    private String accessToken;
    private boolean keyProvided;
    private boolean authTokenProvided;
    private final Map<String, Object> properties = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isKeyProvided() {
        return keyProvided;
    }

    public void setKeyProvided(boolean keyProvided) {
        this.keyProvided = keyProvided;
    }

    public boolean isAuthTokenProvided() {
        return authTokenProvided;
    }

    public void setAuthTokenProvided(boolean authTokenProvided) {
        this.authTokenProvided = authTokenProvided;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}
