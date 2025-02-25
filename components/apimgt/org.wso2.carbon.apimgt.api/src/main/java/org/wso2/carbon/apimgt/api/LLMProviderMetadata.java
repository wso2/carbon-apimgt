/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LLMProviderMetadata {

    @JsonProperty("attributeName")
    private String attributeName;

    @JsonProperty("inputSource")
    private String inputSource;

    @JsonProperty("attributeIdentifier")
    private String attributeIdentifier;

    @JsonProperty("required")
    private boolean required = true;

    public LLMProviderMetadata() {}

    /**
     * @deprecated This constructor is deprecated. Use {@link #LLMProviderMetadata(String, String, String, boolean)}
     *         instead.
     */
    @Deprecated
    public LLMProviderMetadata(@JsonProperty("attributeName") String attributeName,
                               @JsonProperty("inputSource") String inputSource,
                               @JsonProperty("attributeIdentifier") String attributeIdentifier) {

        this.attributeName = attributeName;
        this.inputSource = inputSource;
        this.attributeIdentifier = attributeIdentifier;
    }

    public LLMProviderMetadata(@JsonProperty("attributeName") String attributeName,
            @JsonProperty("inputSource") String inputSource,
            @JsonProperty("attributeIdentifier") String attributeIdentifier,
            @JsonProperty("required") boolean required) {
        this.attributeName = attributeName;
        this.inputSource = inputSource;
        this.attributeIdentifier = attributeIdentifier;
        this.required = required;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
