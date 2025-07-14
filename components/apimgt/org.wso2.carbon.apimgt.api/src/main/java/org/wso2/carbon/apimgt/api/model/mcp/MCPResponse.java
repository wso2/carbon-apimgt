package org.wso2.carbon.apimgt.api.model.mcp;

import com.google.gson.annotations.SerializedName;

/**
 * This class is used to represent the response for the MCP.
 */
public class MCPResponse {
    public static final String JSON_RPC_VERSION = "2.0";

    @SerializedName("jsonrpc")
    private final String jsonRpcVersion = JSON_RPC_VERSION;

    // TODO: Check why was Object used instead of String
    @SerializedName("id")
    private Object id;

    public MCPResponse(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getJsonRpcVersion() {
        return jsonRpcVersion;
    }

}
