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

package org.wso2.carbon.apimgt.governance.impl;

import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceApplicationSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.DevportalGovernanceRulesetSnapshot;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous Devportal Governance validator backed by the Spectral validation engine.
 */
public class DevportalGovernanceValidator {

    /**
     * Validate a JSON payload against the rulesets captured on an application snapshot.
     *
     * @param targetJsonPayload target payload to validate
     * @param snapshot          hydrated application governance snapshot
     * @return rule violations returned by Spectral
     * @throws APIMGovernanceException if validation cannot be completed
     */
    public List<RuleViolation> validate(String targetJsonPayload,
                                        DevportalGovernanceApplicationSnapshot snapshot)
            throws APIMGovernanceException {

        if (snapshot == null) {
            return new ArrayList<>();
        }
        return validate(targetJsonPayload, snapshot.getRulesetSnapshots(), snapshot.getApplicationUuid(),
                snapshot.getOrganization());
    }

    /**
     * Validate a JSON payload against a list of snapshotted YAML rulesets.
     *
     * @param targetJsonPayload target payload to validate
     * @param rulesetSnapshots  snapshotted YAML rulesets
     * @param applicationUuid   application UUID used as the artifact reference
     * @param organization      application organization
     * @return rule violations returned by Spectral
     * @throws APIMGovernanceException if validation cannot be completed
     */
    public List<RuleViolation> validate(String targetJsonPayload,
                                        List<DevportalGovernanceRulesetSnapshot> rulesetSnapshots,
                                        String applicationUuid, String organization)
            throws APIMGovernanceException {

        List<RuleViolation> violations = new ArrayList<>();
        if (rulesetSnapshots == null || rulesetSnapshots.isEmpty()) {
            return violations;
        }
        ValidationEngine validationEngine = getValidationEngine();
        for (DevportalGovernanceRulesetSnapshot rulesetSnapshot : rulesetSnapshots) {
            Ruleset ruleset = toRuleset(rulesetSnapshot);
            List<RuleViolation> rulesetViolations = validationEngine.validate(targetJsonPayload, ruleset,
                    APIMGovernanceUtil.getAPIMGovernanceOptions());
            for (RuleViolation violation : rulesetViolations) {
                violation.setArtifactRefId(applicationUuid);
                violation.setOrganization(organization);
                violation.setRuleType(toRuleType(rulesetSnapshot.getRulesetType()));
                violations.add(violation);
            }
        }
        return violations;
    }

    private ValidationEngine getValidationEngine() throws APIMGovernanceException {

        if (ServiceReferenceHolder.getInstance().getValidationEngineService() == null ||
                ServiceReferenceHolder.getInstance().getValidationEngineService().getValidationEngine() == null) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INTERNAL_SERVER_ERROR,
                    "Spectral validation engine is unavailable");
        }
        return ServiceReferenceHolder.getInstance().getValidationEngineService().getValidationEngine();
    }

    private Ruleset toRuleset(DevportalGovernanceRulesetSnapshot rulesetSnapshot) {

        Ruleset ruleset = new Ruleset();
        ruleset.setId(rulesetSnapshot.getSourceRulesetId());
        ruleset.setName(rulesetSnapshot.getRulesetName());
        ruleset.setDescription(rulesetSnapshot.getRulesetDescription());
        ruleset.setArtifactType(toArtifactType(rulesetSnapshot.getArtifactType()));
        ruleset.setRuleType(toRuleType(rulesetSnapshot.getRulesetType()));

        RulesetContent rulesetContent = new RulesetContent();
        rulesetContent.setFileName(rulesetSnapshot.getRulesetName() + ".yaml");
        String yamlContent = rulesetSnapshot.getYamlContent() == null ? "" : rulesetSnapshot.getYamlContent();
        rulesetContent.setContent(yamlContent.getBytes(StandardCharsets.UTF_8));
        ruleset.setRulesetContent(rulesetContent);
        return ruleset;
    }

    private ExtendedArtifactType toArtifactType(String artifactType) {

        if (artifactType == null) {
            return null;
        }
        return ExtendedArtifactType.fromString(artifactType);
    }

    private RuleType toRuleType(String ruleType) {

        if (ruleType == null) {
            return null;
        }
        return RuleType.fromString(ruleType);
    }
}
