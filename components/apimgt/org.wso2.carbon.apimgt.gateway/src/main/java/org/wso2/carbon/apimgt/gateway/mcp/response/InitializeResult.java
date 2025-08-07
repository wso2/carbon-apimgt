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

import java.util.Map;

public class InitializeResult {

    @SerializedName("protocolVersion")
    public String protocolVersion;

    @SerializedName("capabilities")
    public Capabilities capabilities;

    @SerializedName("serverInfo")
    public ServerInfo serverInfo;

    public static class Capabilities {
        @SerializedName("logging")
        public Map<String, Object> logging;

        @SerializedName("prompts")
        public Prompts prompts;

        @SerializedName("resources")
        public Resources resources;

        @SerializedName("tools")
        public Tools tools;

        @SerializedName("completions")
        public Map<String, Object> completions;

        @SerializedName("experimental")
        public Map<String, Object> experimental;

        public static class Prompts {
            @SerializedName("listChanged")
            public Boolean listChanged;

            public Boolean getListChanged() {
                return listChanged;
            }

            public void setListChanged(Boolean listChanged) {
                this.listChanged = listChanged;
            }
        }

        public static class Resources {
            @SerializedName("subscribe")
            public Boolean subscribe;

            @SerializedName("listChanged")
            public Boolean listChanged;

            public Boolean getSubscribe() {
                return subscribe;
            }

            public void setSubscribe(Boolean subscribe) {
                this.subscribe = subscribe;
            }

            public Boolean getListChanged() {
                return listChanged;
            }

            public void setListChanged(Boolean listChanged) {
                this.listChanged = listChanged;
            }
        }

        public static class Tools {
            @SerializedName("listChanged")
            public Boolean listChanged;

            public Boolean getListChanged() {
                return listChanged;
            }

            public void setListChanged(Boolean listChanged) {
                this.listChanged = listChanged;
            }
        }

        public Map<String, Object> getLogging() {
            return logging;
        }

        public void setLogging(Map<String, Object> logging) {
            this.logging = logging;
        }

        public Prompts getPrompts() {
            return prompts;
        }

        public void setPrompts(Prompts prompts) {
            this.prompts = prompts;
        }

        public Resources getResources() {
            return resources;
        }

        public void setResources(Resources resources) {
            this.resources = resources;
        }

        public Tools getTools() {
            return tools;
        }

        public void setTools(Tools tools) {
            this.tools = tools;
        }

        public Map<String, Object> getCompletions() {
            return completions;
        }

        public void setCompletions(Map<String, Object> completions) {
            this.completions = completions;
        }

        public Map<String, Object> getExperimental() {
            return experimental;
        }

        public void setExperimental(Map<String, Object> experimental) {
            this.experimental = experimental;
        }
    }

    public static class ServerInfo {
        @SerializedName("name")
        public String name;

        @SerializedName("version")
        public String version;

        @SerializedName("description")
        public String description;

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

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}