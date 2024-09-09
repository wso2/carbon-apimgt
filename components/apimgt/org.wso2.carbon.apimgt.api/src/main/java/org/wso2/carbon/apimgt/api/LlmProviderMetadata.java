package org.wso2.carbon.apimgt.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LlmProviderMetadata {

    @JsonProperty("attributeName")
    private String attributeName;

    @JsonProperty("inputSource")
    private String inputSource;

    @JsonProperty("attributeIdentifier")
    private String attributeIdentifier;

    public LlmProviderMetadata() {}

    public LlmProviderMetadata(@JsonProperty("attributeName") String attributeName,
                               @JsonProperty("inputSource") String inputSource,
                               @JsonProperty("attributeIdentifier") String attributeIdentifier) {

        this.attributeName = attributeName;
        this.inputSource = inputSource;
        this.attributeIdentifier = attributeIdentifier;
    }

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getInputSource() {

        return inputSource;
    }

    public void setInputSource(String inputSource) {

        this.inputSource = inputSource;
    }

    public String getAttributeIdentifier() {

        return attributeIdentifier;
    }

    public void setAttributeIdentifier(String attributeIdentifier) {

        this.attributeIdentifier = attributeIdentifier;
    }
}
