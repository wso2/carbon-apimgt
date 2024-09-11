package org.wso2.carbon.apimgt.api.model;

import java.util.Map;

public class LLMConfiguration {

    private boolean enabled;

    private Map<String, String> additionalHeaders;

    private Map<String, String> additionalQueryParameters;

    private String llmProviderName;

    private String llmProviderApiVersion;
//
//    private String setTokenDetails;

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

    public Map<String, String> getAdditionalHeaders() {

        return additionalHeaders;
    }

    public void setAdditionalHeaders(Map<String, String> additionalHeaders) {

        this.additionalHeaders = additionalHeaders;
    }

    public Map<String, String> getAdditionalQueryParameters() {

        return additionalQueryParameters;
    }

    public void setAdditionalQueryParameters(Map<String, String> additionalQueryParameters) {

        this.additionalQueryParameters = additionalQueryParameters;
    }
}
