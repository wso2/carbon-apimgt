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

package org.wso2.carbon.apimgt.governance.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

/**
 * This class represents the default policy model.
 */

public class DefaultPolicy {
    private String name;
    private String description;
    private String policyCategory;
    private String policyType;
    private String artifactType;
    private String documentationLink;
    private String provider;
    private Object policyContent;

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

    public String getDocumentationLink() {
        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {
        this.documentationLink = documentationLink;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Object getPolicyContent() {
        return policyContent;
    }

    public void setPolicyContent(Object policyContent) {
        this.policyContent = policyContent;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getPolicyContentString() throws APIMGovernanceException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        String contentString;
        try {
            contentString = objectMapper.writeValueAsString(policyContent);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_LOADING_DEFAULT_POLICY_CONTENT, e);
        }
        return contentString;
    }

    public String getPolicyCategory() {
        return policyCategory;
    }

    public void setPolicyCategory(String policyCategory) {
        this.policyCategory = policyCategory;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }
}
