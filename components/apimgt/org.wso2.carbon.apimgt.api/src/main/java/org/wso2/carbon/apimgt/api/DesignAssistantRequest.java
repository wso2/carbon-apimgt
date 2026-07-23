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
 * Carries the context of an API Design Assistant request to a {@link DesignAssistant} implementation.
 * <p>
 * Passing a context object rather than positional parameters keeps the SPI stable: an implementation reads only the
 * fields it needs, and new fields can be added over time without breaking existing implementations. Any information
 * not modelled as a typed field can be supplied through {@link #getAdditionalProperties()}.
 */
public class DesignAssistantRequest {

    private String sessionId;
    private String text;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void addProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    public Object getProperty(String key) {
        return additionalProperties.get(key);
    }
}
