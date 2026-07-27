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
 * Settings for a single AI capability's backend - the service {@code endpoint}, an optional {@code tokenEndpoint},
 * and either an API {@code key} or an {@code accessToken} - together with the capability's named resource paths.
 * <p>
 * {@link AIServiceConfiguration} holds one of these per capability (Design Assistant, Marketplace Assistant, API
 * Chat), so each capability can point to a different backend. It is a plain {@code api}-module value object, so a
 * custom {@code AIService} implementation reads its settings from here without depending on the {@code impl}-module
 * configuration classes.
 */
public class AIServiceEndpointConfiguration {

    // Well-known resource keys, used with {@link #getResource(String)} / {@link #addResource(String, String)}.
    public static final String CHAT_RESOURCE = "chatResource";
    public static final String GEN_API_PAYLOAD_RESOURCE = "genApiPayloadResource";
    public static final String API_PUBLISH_RESOURCE = "apiPublishResource";
    public static final String API_DELETE_RESOURCE = "apiDeleteResource";
    public static final String API_COUNT_RESOURCE = "apiCountResource";
    public static final String PREPARE_RESOURCE = "prepareResource";
    public static final String EXECUTE_RESOURCE = "executeResource";

    private boolean enabled;
    private String endpoint;
    private String tokenEndpoint;
    private String key;
    private String accessToken;
    private boolean keyProvided;
    private boolean authTokenProvided;
    private final Map<String, String> resources = new HashMap<>();

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

    public Map<String, String> getResources() {
        return resources;
    }

    public String getResource(String key) {
        return resources.get(key);
    }

    public void addResource(String key, String value) {
        resources.put(key, value);
    }
}
