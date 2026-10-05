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

        @SerializedName("tools")
        public Tools tools;

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

        public Tools getTools() {
            return tools;
        }

        public void setTools(Tools tools) {
            this.tools = tools;
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
