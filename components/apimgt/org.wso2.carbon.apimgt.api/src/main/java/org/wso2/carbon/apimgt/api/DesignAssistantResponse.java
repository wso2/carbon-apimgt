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
 * Carries the result of a {@link DesignAssistant} invocation.
 * <p>
 * {@code payload} holds the raw JSON body returned by the generate-payload call, and {@code chatResponse} the raw
 * JSON body returned by the chat call. Consumers map the relevant field onto the REST response DTO. Returning a
 * response object (rather than a bare string) keeps the SPI stable - additional fields can be added over time, and
 * anything not modelled as a typed field can be supplied through {@link #getAdditionalProperties()}.
 */
public class DesignAssistantResponse {

    private String payload;
    private String chatResponse;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getChatResponse() {
        return chatResponse;
    }

    public void setChatResponse(String chatResponse) {
        this.chatResponse = chatResponse;
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
