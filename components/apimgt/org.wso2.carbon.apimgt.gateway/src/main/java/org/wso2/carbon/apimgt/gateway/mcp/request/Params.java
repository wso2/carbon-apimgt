package org.wso2.carbon.apimgt.gateway.mcp.request;

import com.google.gson.annotations.SerializedName;

public class Params {
    @SerializedName("protocolVersion")
    public String protocolVersion;

    @SerializedName("capabilities")
    public Capabilities capabilities;

    @SerializedName("clientInfo")
    public ClientInfo clientInfo;

    @SerializedName("name")
    public String toolName;

    @SerializedName("arguments")
    public Object arguments;

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

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Object getArguments() {
        return arguments;
    }

    public void setArguments(Object arguments) {
        this.arguments = arguments;
    }

    public static class Capabilities {
        @SerializedName("roots")
        public Object roots;

        @SerializedName("sampling")
        public Object sampling;

        @SerializedName("elicitation")
        public Object elicitation;

        public Object getRoots() {
            return roots;
        }

        public void setRoots(Object roots) {
            this.roots = roots;
        }

        public Object getSampling() {
            return sampling;
        }

        public void setSampling(Object sampling) {
            this.sampling = sampling;
        }

        public Object getElicitation() {
            return elicitation;
        }

        public void setElicitation(Object elicitation) {
            this.elicitation = elicitation;
        }
    }

    public static class ClientInfo {
        @SerializedName("name")
        public String name;

        @SerializedName("title")
        public String title;

        @SerializedName("version")
        public String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
