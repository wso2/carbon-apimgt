/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import java.util.HashMap;
import java.util.Map;

public class WebSubHubConfig {
    private boolean isEnabled;
    private String url;
    private String type;
    private boolean topicContextPrefixDisabled;
    private Map<String, Object> hubProperties = new HashMap<>();

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isTopicContextPrefixDisabled() {
        return topicContextPrefixDisabled;
    }

    public void setTopicContextPrefixDisabled(boolean topicContextPrefixDisabled) {
        this.topicContextPrefixDisabled = topicContextPrefixDisabled;
    }

    public Map<String, Object> getHubProperties() {
        return hubProperties;
    }

    public void setHubProperties(Map<String, Object> hubProperties) {
        this.hubProperties = hubProperties;
    }

    public void addHubProperty(String key, Object value) {
        this.hubProperties.put(key, value);
    }

    public Object getHubProperty(String key) {
        return this.hubProperties.get(key);
    }
}
