/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.gateway.mcp.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ToolListResult {

    @SerializedName("tools")
    public List<ToolInfo> tools;

    @SerializedName("nextCursor")
    public String nextCursor;

    public List<ToolInfo> getTools() {
        return tools;
    }

    public void setTools(List<ToolInfo> tools) {
        this.tools = tools;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public static class ToolInfo {
        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("inputSchema")
        public JsonSchema inputSchema;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public JsonSchema getInputSchema() {
            return inputSchema;
        }

        public void setInputSchema(JsonSchema inputSchema) {
            this.inputSchema = inputSchema;
        }
    }

    public static class JsonSchema {
        @SerializedName("type")
        public String type;

        @SerializedName("properties")
        public java.util.Map<String, Object> properties;

        @SerializedName("required")
        public List<String> required;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        public void addProperty(String name, Object property) {
            if (this.properties == null) {
                this.properties = new java.util.HashMap<>();
            }
            this.properties.put(name, property);
        }

        public void removeProperty(String name) {
            if (this.properties != null) {
                this.properties.remove(name);
            }
        }

        public List<String> getRequired() {
            return required;
        }

        public void setRequired(List<String> required) {
            this.required = required;
        }
    }
}
