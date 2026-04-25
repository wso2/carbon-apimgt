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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Point-in-time Devportal Governance snapshot attached to an application.
 */
public class DevportalGovernanceApplicationSnapshot {

    private String snapshotId;
    private int applicationId;
    private String applicationUuid;
    private String sourceTemplateId;
    private String templateName;
    private String templateDescription;
    private Map<String, Object> formConfig = new HashMap<>();
    private String formConfigHash;
    private String organization;
    private String capturedBy;
    private String capturedAt;
    private List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots = new ArrayList<>();

    public String getSnapshotId() {

        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {

        this.snapshotId = snapshotId;
    }

    public int getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(int applicationId) {

        this.applicationId = applicationId;
    }

    public String getApplicationUuid() {

        return applicationUuid;
    }

    public void setApplicationUuid(String applicationUuid) {

        this.applicationUuid = applicationUuid;
    }

    public String getSourceTemplateId() {

        return sourceTemplateId;
    }

    public void setSourceTemplateId(String sourceTemplateId) {

        this.sourceTemplateId = sourceTemplateId;
    }

    public String getTemplateName() {

        return templateName;
    }

    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    public String getTemplateDescription() {

        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {

        this.templateDescription = templateDescription;
    }

    public Map<String, Object> getFormConfig() {

        return new HashMap<>(formConfig);
    }

    public void setFormConfig(Map<String, Object> formConfig) {

        if (formConfig == null) {
            this.formConfig = Collections.emptyMap();
        } else {
            this.formConfig = Collections.unmodifiableMap(new HashMap<>(formConfig));
        }
    }

    public String getFormConfigHash() {

        return formConfigHash;
    }

    public void setFormConfigHash(String formConfigHash) {

        this.formConfigHash = formConfigHash;
    }

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public String getCapturedBy() {

        return capturedBy;
    }

    public void setCapturedBy(String capturedBy) {

        this.capturedBy = capturedBy;
    }

    public String getCapturedAt() {

        return capturedAt;
    }

    public void setCapturedAt(String capturedAt) {

        this.capturedAt = capturedAt;
    }

    public List<DevportalGovernanceRulesetSnapshot> getRulesetSnapshots() {

        return new ArrayList<>(rulesetSnapshots);
    }

    public void setRulesetSnapshots(List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots) {

        if (rulesetSnapshots == null) {
            this.rulesetSnapshots = Collections.emptyList();
        } else {
            this.rulesetSnapshots = Collections.unmodifiableList(new ArrayList<>(rulesetSnapshots));
        }
    }
}
