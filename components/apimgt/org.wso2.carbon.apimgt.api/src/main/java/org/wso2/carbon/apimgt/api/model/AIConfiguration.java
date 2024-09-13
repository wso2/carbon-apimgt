package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.TokenBaseThrottlingCountHolder;

import java.util.List;
import java.util.Map;

public class AIConfiguration {

    private boolean enabled;
    private AIEndpointConfiguration aiEndpointConfiguration;
    private String llmProviderName;
    private String llmProviderApiVersion;
    private TokenBaseThrottlingCountHolder tokenBasedThrottlingConfiguration;

    public AIEndpointConfiguration getAiEndpointConfiguration() {

        return aiEndpointConfiguration;
    }

    public void setAiEndpointConfiguration(AIEndpointConfiguration aiEndpointConfiguration) {

        this.aiEndpointConfiguration = aiEndpointConfiguration;
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
}
