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
 * Point-in-time ruleset copy attached to an application governance snapshot.
 */
public class DevportalGovernanceRulesetSnapshot {

    private String snapshotRulesetId;
    private String snapshotId;
    private String sourceRulesetId;
    private String rulesetName;
    private String rulesetDescription;
    private String artifactType;
    private String rulesetType;
    private String yamlContent;
    private String contentSha256;
    private int bindingOrder;
    private List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes = new ArrayList<>();

    public String getSnapshotRulesetId() {

        return snapshotRulesetId;
    }

    public void setSnapshotRulesetId(String snapshotRulesetId) {

        this.snapshotRulesetId = snapshotRulesetId;
    }

    public String getSnapshotId() {

        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {

        this.snapshotId = snapshotId;
    }

    public String getSourceRulesetId() {

        return sourceRulesetId;
    }

    public void setSourceRulesetId(String sourceRulesetId) {

        this.sourceRulesetId = sourceRulesetId;
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

    public String getArtifactType() {

        return artifactType;
    }

    public void setArtifactType(String artifactType) {

        this.artifactType = artifactType;
    }

    public String getRulesetType() {

        return rulesetType;
    }

    public void setRulesetType(String rulesetType) {

        this.rulesetType = rulesetType;
    }

    public String getYamlContent() {

        return yamlContent;
    }

    public void setYamlContent(String yamlContent) {

        this.yamlContent = yamlContent;
    }

    public String getContentSha256() {

        return contentSha256;
    }

    public void setContentSha256(String contentSha256) {

        this.contentSha256 = contentSha256;
    }

    public int getBindingOrder() {

        return bindingOrder;
    }

    public void setBindingOrder(int bindingOrder) {

        this.bindingOrder = bindingOrder;
    }

    public List<DevportalGovernanceRulesetSnapshotKeyManagerScope> getKeyManagerScopes() {

        return new ArrayList<>(keyManagerScopes);
    }

    public void setKeyManagerScopes(
            List<DevportalGovernanceRulesetSnapshotKeyManagerScope> keyManagerScopes) {

        if (keyManagerScopes == null) {
            this.keyManagerScopes = Collections.emptyList();
        } else {
            this.keyManagerScopes = Collections.unmodifiableList(new ArrayList<>(keyManagerScopes));
        }
    }
}
