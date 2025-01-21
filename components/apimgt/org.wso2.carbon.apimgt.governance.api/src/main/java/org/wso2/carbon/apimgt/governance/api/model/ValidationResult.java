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
 * This class represents the result of a validation
 */
public class ValidationResult {

    private String resultId; // Contains the id of the result
    private String organization; // Contains the organization that was validated
    private String artifactId; // Contains the id of the artifact that was validated
    private String policyId; // Contains the id of the policy that was validated
    private String rulesetId; // Contains the id of the ruleset that was validated
    private String ruleCode; // Contains the code of the rule that was validated
    private String validatedPath; // Contains the path that was validated
    private boolean isRuleValid; // Contains the result of the validation

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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

    public String getRulesetId() {
        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {
        this.rulesetId = rulesetId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getValidatedPath() {
        return validatedPath;
    }

    public void setValidatedPath(String validatedPath) {
        this.validatedPath = validatedPath;
    }

    public boolean isRuleValid() {
        return isRuleValid;
    }

    public void setRuleValid(boolean ruleValid) {
        isRuleValid = ruleValid;
    }

}
