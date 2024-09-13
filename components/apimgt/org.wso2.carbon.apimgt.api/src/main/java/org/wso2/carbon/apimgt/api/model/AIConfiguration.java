package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.TokenBaseThrottlingCountHolder;

import java.util.List;
import java.util.Map;

public class AiConfiguration {

    private boolean enabled;
    private Map<String, String> productionEndpointAdditionalHeaders;
    private Map<String, String> sandboxEndpointAdditionalHeaders;
    private Map<String, String> productionEndpointAdditionalQueryParameters;
    private Map<String, String> sandboxEndpointAdditionalQueryParameters;
    private List<String> productionEndpointSensitiveParameters;
    private List<String> sandboxEndpointSensitiveParameters;
    private String llmProviderName;
    private String llmProviderApiVersion;
    private TokenBaseThrottlingCountHolder tokenBasedThrottlingConfiguration;

    public List<String> getProductionEndpointSensitiveParameters() {

        return productionEndpointSensitiveParameters;
    }

    public void setProductionEndpointSensitiveParameters(List<String> productionEndpointSensitiveParameters) {

        this.productionEndpointSensitiveParameters = productionEndpointSensitiveParameters;
    }

    public List<String> getSandboxEndpointSensitiveParameters() {

        return sandboxEndpointSensitiveParameters;
    }

    public void setSandboxEndpointSensitiveParameters(List<String> sandboxEndpointSensitiveParameters) {

        this.sandboxEndpointSensitiveParameters = sandboxEndpointSensitiveParameters;
    }

    public Map<String, String> getSandboxEndpointAdditionalHeaders() {

        return sandboxEndpointAdditionalHeaders;
    }

    public void setSandboxEndpointAdditionalHeaders(Map<String, String> sandboxEndpointAdditionalHeaders) {

        this.sandboxEndpointAdditionalHeaders = sandboxEndpointAdditionalHeaders;
    }

    public Map<String, String> getProductionEndpointAdditionalQueryParameters() {

        return productionEndpointAdditionalQueryParameters;
    }

    public void setProductionEndpointAdditionalQueryParameters(Map<String, String> productionEndpointAdditionalQueryParameters) {

        this.productionEndpointAdditionalQueryParameters = productionEndpointAdditionalQueryParameters;
    }

    public TokenBaseThrottlingCountHolder getTokenBasedThrottlingConfiguration() {

        return tokenBasedThrottlingConfiguration;
    }

    public void setTokenBasedThrottlingConfiguration(TokenBaseThrottlingCountHolder tokenBasedThrottlingConfiguration) {

        this.tokenBasedThrottlingConfiguration = tokenBasedThrottlingConfiguration;
    }

    public String getLlmProviderName() {

        return llmProviderName;
    }

    public void setLlmProviderName(String llmProviderName) {

        this.llmProviderName = llmProviderName;
    }

    public String getLlmProviderApiVersion() {

        return llmProviderApiVersion;
    }

    public void setLlmProviderApiVersion(String llmProviderApiVersion) {

        this.llmProviderApiVersion = llmProviderApiVersion;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public Map<String, String> getProductionEndpointAdditionalHeaders() {

        return productionEndpointAdditionalHeaders;
    }

    public void setProductionEndpointAdditionalHeaders(Map<String, String> productionEndpointAdditionalHeaders) {

        this.productionEndpointAdditionalHeaders = productionEndpointAdditionalHeaders;
    }

    public Map<String, String> getSandboxEndpointAdditionalQueryParameters() {

        return sandboxEndpointAdditionalQueryParameters;
    }

    public void setSandboxEndpointAdditionalQueryParameters(Map<String, String> sandboxEndpointAdditionalQueryParameters) {

        this.sandboxEndpointAdditionalQueryParameters = sandboxEndpointAdditionalQueryParameters;
    }
}
