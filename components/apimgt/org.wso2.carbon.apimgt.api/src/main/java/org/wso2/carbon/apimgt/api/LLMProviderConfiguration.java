package org.wso2.carbon.apimgt.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class LLMProviderConfiguration {

    @JsonProperty("connectorType")
    private String connectorType;

    @JsonProperty("metadata")
    private List<LLMProviderMetadata> metadata;

    @JsonProperty("authHeader")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String authHeader;

    @JsonProperty("authQueryParameter")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String authQueryParameter;

    public LLMProviderConfiguration() {}

    @JsonCreator
    public LLMProviderConfiguration(
            @JsonProperty("connectorType") String connectorType,
            @JsonProperty("metadata") List<LLMProviderMetadata> metadata,
            @JsonProperty("authHeader") String authHeader,
            @JsonProperty("authQueryParameter") String authQueryParameter) {

        this.connectorType = connectorType;
        this.metadata = metadata;
        this.authHeader = authHeader;
        this.authQueryParameter = authQueryParameter;
    }

    public String getConnectorType() {

        return connectorType;
    }

    public void setConnectorType(String connectorType) {

        this.connectorType = connectorType;
    }

    public List<LLMProviderMetadata> getMetadata() {

        return metadata;
    }

    public void setMetadata(List<LLMProviderMetadata> metadata) {

        this.metadata = metadata;
    }

    public String getAuthHeader() {

        return authHeader;
    }

    public void setAuthHeader(String authHeader) {

        this.authHeader = authHeader;
    }

    public String getAuthQueryParameter() {

        return authQueryParameter;
    }

    public void setAuthQueryParam(String authQueryParameter) {

        this.authQueryParameter = authQueryParameter;
    }

    public String toJsonString() throws APIManagementException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while parsing LLM Provider configuration");
        }
    }
}
