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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.external.model;

import java.util.Collections;
import java.util.Map;

/**
 * Top-level model for an external governance ruleset definition.
 */
public class ExternalRulesetDefinition {

    private String name;
    private String description;
    private String ruleCategory;
    private String ruleType;
    private String artifactType;
    private String documentationLink;
    private String provider;
    private ExternalRulesetContentDefinition rulesetContent;

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

    public String getRuleCategory() {

        return ruleCategory;
    }

    public void setRuleCategory(String ruleCategory) {

        this.ruleCategory = ruleCategory;
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

    public ExternalRulesetContentDefinition getRulesetContent() {

        return rulesetContent;
    }

    public void setRulesetContent(ExternalRulesetContentDefinition rulesetContent) {

        this.rulesetContent = rulesetContent;
    }

    public Map<String, ExternalRuleDefinition> getRules() {

        if (rulesetContent == null || rulesetContent.getRules() == null) {
            return Collections.emptyMap();
        }
        return rulesetContent.getRules();
    }
}
