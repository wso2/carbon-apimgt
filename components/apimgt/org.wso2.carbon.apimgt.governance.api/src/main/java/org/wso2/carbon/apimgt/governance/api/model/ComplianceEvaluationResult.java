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
 * This class represents the result of a compliance evaluation
 */
public class ComplianceEvaluationResult {
    private String artifactId;
    private ArtifactType artifactType;
    private String policyId;
    private String rulesetId;
    private boolean isEvaluationSuccess;
    private String organization;

    public ComplianceEvaluationResult(String artifactId, ArtifactType artifactType, String policyId,
                                      String rulesetId, boolean inEvaluationSuccess, String organization) {
        this.artifactId = artifactId;
        this.artifactType = artifactType;
        this.policyId = policyId;
        this.rulesetId = rulesetId;
        this.isEvaluationSuccess = inEvaluationSuccess;
        this.organization = organization;
    }

    public ComplianceEvaluationResult() {
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getRulesetId() {
        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {
        this.rulesetId = rulesetId;
    }

    public boolean isEvaluationSuccess() {
        return isEvaluationSuccess;
    }

    public void setEvaluationSuccess(boolean evaluationSuccess) {
        this.isEvaluationSuccess = evaluationSuccess;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
