/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.Collections;
import java.util.Map;

public class WebSocketTopicMappingConfiguration {
    private Map<String, Map<String, String>> mappings;

    public WebSocketTopicMappingConfiguration(Map<String, Map<String, String>> mappings) {
        this.mappings = mappings;
    }

    public Map<String, Map<String, String>> getMappings() {
        if (mappings != null) {
            return mappings;
        }
        return Collections.emptyMap();
    }

    public void setResourceKey(String topic, String resourceKey) {
        if (mappings != null) {
            Map<String, String> topicData = mappings.get(topic);
            if (topicData != null) {
                topicData.put("resourceKey", resourceKey);
            }
        }
    }
}
