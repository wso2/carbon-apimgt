package org.wso2.carbon.apimgt.api.model;

import java.util.Collections;
import java.util.Map;

public class WebSocketTopicMappingConfiguration {
    private Map<String, Map<String, String>> mappings;
    // topic -> {resourceKey -> cleansed_resource(key), production -> URL, sandbox -> URL}

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
