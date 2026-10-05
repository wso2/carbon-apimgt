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

/**
 * This class represents a rule violation.
 */
public class RuleViolation {
    private String artifactRefId; // Artifact which contains the violation
    private ArtifactType artifactType; // Type of the artifact (API)
    private String rulesetId; // Ruleset which contains the violated rule
    private RuleType ruleType; // Type of the violated rule
    private String ruleName;  // Code of the violated rule
    private String violatedPath; // Path in which the violation occurred
    private RuleSeverity severity; // Severity of the violation
    private String ruleMessage; // Message of the violated rule
    private String organization; // Organization of the artifact

    public String getArtifactRefId() {
        return artifactRefId;
    }

    public void setArtifactRefId(String artifactRefId) {
        this.artifactRefId = artifactRefId;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }

    public String getRulesetId() {
        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {
        this.rulesetId = rulesetId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getViolatedPath() {
        return violatedPath;
    }

    public void setViolatedPath(String violatedPath) {
        this.violatedPath = violatedPath;
    }

    public RuleSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RuleSeverity severity) {
        this.severity = severity;
    }

    public String getRuleMessage() {
        return ruleMessage;
    }

    public void setRuleMessage(String ruleMessage) {
        this.ruleMessage = ruleMessage;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
}
