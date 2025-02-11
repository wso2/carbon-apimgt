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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a compliance evaluation request
 */
public class ComplianceEvaluationRequest {

    private String id;

    private String artifactRefId;

    private ArtifactType artifactType;

    private List<String> policyAttachmentIds;

    private String organization;

    private ComplianceEvaluationStatus evaluationStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<String> getPolicyAttachmentIds() {
        return new ArrayList<>(policyAttachmentIds);
    }

    public void setPolicyAttachmentIds(List<String> policyAttachmentIds) {
        this.policyAttachmentIds = new ArrayList<>(policyAttachmentIds);
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public ComplianceEvaluationStatus getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(ComplianceEvaluationStatus evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }
}
