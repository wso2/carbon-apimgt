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
 * The resolved AI-service configuration handed to an {@link AIService} implementation through
 * {@link AIService#init(AIServiceConfiguration)}.
 * <p>
 * It carries one {@link AIServiceEndpointConfiguration} per AI capability - Design Assistant, Marketplace Assistant
 * and API Chat - so each capability can be pointed at its own backend/credentials. Anything not modelled as a typed
 * field can be supplied through the generic {@link #getProperties() property bag}. Because this is a plain
 * {@code api}-module value object, an implementation reads everything it needs from here and never has to reach into
 * the {@code impl}-module configuration ({@code APIManagerConfiguration}/{@code ServiceReferenceHolder}).
 */
public class AIServiceConfiguration {

    private final AIServiceEndpointConfiguration designAssistant = new AIServiceEndpointConfiguration();
    private final AIServiceEndpointConfiguration marketplaceAssistant = new AIServiceEndpointConfiguration();
    private final AIServiceEndpointConfiguration apiChat = new AIServiceEndpointConfiguration();
    private final Map<String, Object> properties = new HashMap<>();

    public AIServiceEndpointConfiguration getDesignAssistant() {
        return designAssistant;
    }

    public AIServiceEndpointConfiguration getMarketplaceAssistant() {
        return marketplaceAssistant;
    }

    public AIServiceEndpointConfiguration getApiChat() {
        return apiChat;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }
}
