/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ruleset binding configured for a Devportal Governance template.
 */
public class DevportalGovernanceRulesetBinding {

    private String bindingId;
    private String templateId;
    private String rulesetId;
    private String rulesetName;
    private String rulesetDescription;
    private String documentationLink;
    private String ruleType;
    private String artifactType;
    private int bindingOrder;
    private String createdBy;
    private String createdTime;
    private List<DevportalGovernanceKeyManagerScope> keyManagerScopes = new ArrayList<>();

    public String getBindingId() {

        return bindingId;
    }

    public void setBindingId(String bindingId) {

        this.bindingId = bindingId;
    }

    public String getTemplateId() {

        return templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public String getRulesetId() {

        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {

        this.rulesetId = rulesetId;
    }

    public String getRulesetName() {

        return rulesetName;
    }

    public void setRulesetName(String rulesetName) {

        this.rulesetName = rulesetName;
    }

    public String getRulesetDescription() {

        return rulesetDescription;
    }

    public void setRulesetDescription(String rulesetDescription) {

        this.rulesetDescription = rulesetDescription;
    }

    public String getDocumentationLink() {

        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {

        this.documentationLink = documentationLink;
    }

    public String getRuleType() {

        return ruleType;
    }

    public void setRuleType(String ruleType) {

        this.ruleType = ruleType;
    }

    public String getArtifactType() {

        return artifactType;
    }

    public void setArtifactType(String artifactType) {

        this.artifactType = artifactType;
    }

    public int getBindingOrder() {

        return bindingOrder;
    }

    public void setBindingOrder(int bindingOrder) {

        this.bindingOrder = bindingOrder;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    public String getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(String createdTime) {

        this.createdTime = createdTime;
    }

    public List<DevportalGovernanceKeyManagerScope> getKeyManagerScopes() {

        return new ArrayList<>(keyManagerScopes);
    }

    public void setKeyManagerScopes(List<DevportalGovernanceKeyManagerScope> keyManagerScopes) {

        if (keyManagerScopes == null) {
            this.keyManagerScopes = Collections.emptyList();
        } else {
            this.keyManagerScopes = Collections.unmodifiableList(new ArrayList<>(keyManagerScopes));
        }
    }
}
