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
    private String policyId;
    private boolean isEvaluationSuccess;

    public ComplianceEvaluationResult(String artifactId, String policyId, boolean inEvaluationSuccess) {
        this.artifactId = artifactId;
        this.policyId = policyId;
        this.isEvaluationSuccess = inEvaluationSuccess;
    }

    public ComplianceEvaluationResult() {
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public boolean isEvaluationSuccess() {
        return isEvaluationSuccess;
    }

    public void setEvaluationSuccess(boolean evaluationSuccess) {
        this.isEvaluationSuccess = evaluationSuccess;
    }
}
