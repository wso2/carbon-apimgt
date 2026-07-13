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

package org.wso2.carbon.apimgt.impl.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * Carries the context of an API Chat request to an {@link APIChatService} implementation.
 * <p>
 * Passing a context object rather than positional parameters keeps the SPI stable: an implementation reads only the
 * fields it needs (ignoring, for example, {@link #getApiChatRequestId()} if its AI service does not track requests
 * that way), and new fields can be added over time without breaking existing implementations. Any information not
 * modelled as a typed field can be supplied through {@link #getAdditionalProperties()}.
 */
public class APIChatRequest {

    private String action;
    private String apiId;
    private String apiChatRequestId;
    private String organization;
    private String username;
    private String openAPIDefinition;
    private String requestPayload;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiChatRequestId() {
        return apiChatRequestId;
    }

    public void setApiChatRequestId(String apiChatRequestId) {
        this.apiChatRequestId = apiChatRequestId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOpenAPIDefinition() {
        return openAPIDefinition;
    }

    public void setOpenAPIDefinition(String openAPIDefinition) {
        this.openAPIDefinition = openAPIDefinition;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
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
