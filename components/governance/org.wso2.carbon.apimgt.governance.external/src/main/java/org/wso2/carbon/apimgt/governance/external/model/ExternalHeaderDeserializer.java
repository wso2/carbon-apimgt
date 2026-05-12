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

package org.wso2.carbon.apimgt.governance.external.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer for headers that supports both list format (new) and map format (legacy).
 * Converts both formats to List<ExternalHeader>.
 */
public class ExternalHeaderDeserializer extends JsonDeserializer<List<ExternalHeader>> {

    @Override
    public List<ExternalHeader> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {

        List<ExternalHeader> headers = new ArrayList<>();
        JsonNode node = parser.getCodec().readTree(parser);

        if (node == null || node.isNull()) {
            return headers;
        }

        if (node.isArray()) {
            // New list format
            for (JsonNode item : node) {
                if (item.isObject()) {
                    ExternalHeader header = new ExternalHeader();
                    if (item.has("key")) {
                        header.setKey(item.get("key").asText());
                    }
                    if (item.has("value")) {
                        JsonNode valueNode = item.get("value");
                        if (valueNode.isNull()) {
                            header.setValue("");
                        } else if (valueNode.isTextual()) {
                            header.setValue(valueNode.asText());
                        } else {
                            header.setValue(valueNode);
                        }
                    }
                    if (item.has("category")) {
                        header.setCategory(item.get("category").asText());
                    }
                    headers.add(header);
                }
            }
        } else if (node.isObject()) {
            // Legacy map format: convert to list
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                ExternalHeader header = new ExternalHeader();
                header.setKey(entry.getKey());
                JsonNode valueNode = entry.getValue();
                if (valueNode.isNull()) {
                    header.setValue("");
                } else if (valueNode.isTextual()) {
                    header.setValue(valueNode.asText());
                } else {
                    header.setValue(valueNode);
                }
                header.setCategory("Standard"); // Default category for legacy headers
                headers.add(header);
            }
        }

        return headers;
    }
}
