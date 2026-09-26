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

package org.wso2.carbon.apimgt.governance.rest.api.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.RulesetManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;

import java.util.Collections;
import java.util.List;

/**
 * Tests what {@code getRulesetValidationResultDTO} reports as the ruleset's status.
 * <p>
 * This screen has no policy in its path, yet the severities a ruleset is judged on are declared per policy, so a
 * single status is only answerable here when the feature is off: every policy is then judged on every severity, so
 * the status is just whether any violation was reported. With the feature on, the same ruleset can legitimately
 * pass under one governing policy and fail under another, so no status is reported at all.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComplianceAPIUtil.class, APIMGovernanceUtil.class})
public class GetRulesetValidationResultDTOTest {

    private static final String ARTIFACT_REF_ID = "d090cf7c-d1ab-491c-9357-b55a47e49ef2";
    private static final String RULESET_ID = "7ca1bcae-6feb-4c4b-9252-78e034b8b89e";
    private static final String ORGANIZATION = "carbon.super";
    private static final String USERNAME = "admin";

    private ComplianceManager complianceManager;
    private RulesetManager rulesetManager;
    private PolicyManager policyManager;

    @Before
    public void setUp() throws Exception {

        complianceManager = Mockito.mock(ComplianceManager.class);
        PowerMockito.whenNew(ComplianceManager.class).withNoArguments().thenReturn(complianceManager);

        rulesetManager = Mockito.mock(RulesetManager.class);
        PowerMockito.whenNew(RulesetManager.class).withNoArguments().thenReturn(rulesetManager);

        policyManager = Mockito.mock(PolicyManager.class);
        PowerMockito.whenNew(PolicyManager.class).withNoArguments().thenReturn(policyManager);

        PowerMockito.mockStatic(APIMGovernanceUtil.class);
        PowerMockito.when(APIMGovernanceUtil.isArtifactVisibleToUser(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(ArtifactType.class), ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(true);

        RulesetInfo rulesetInfo = new RulesetInfo();
        rulesetInfo.setId(RULESET_ID);
        rulesetInfo.setName("Severity_Test_Ruleset");
        rulesetInfo.setRuleType(RuleType.API_DEFINITION);
        Mockito.when(rulesetManager.getRulesetById(RULESET_ID, ORGANIZATION)).thenReturn(rulesetInfo);
        Mockito.when(complianceManager.isRulesetEvaluatedForArtifact(ARTIFACT_REF_ID, ArtifactType.API, RULESET_ID,
                ORGANIZATION)).thenReturn(true);

        Rule rule = new Rule();
        rule.setId("rule-1");
        rule.setName("api-description-check");
        rule.setSeverity(RuleSeverity.INFO);
        List<Rule> rules = Collections.singletonList(rule);
        Mockito.when(rulesetManager.getRulesByRulesetId(RULESET_ID, ORGANIZATION)).thenReturn(rules);

        RuleViolation violation = new RuleViolation();
        violation.setRuleName(rule.getName());
        violation.setRulesetId(RULESET_ID);
        violation.setSeverity(RuleSeverity.INFO);
        Mockito.when(complianceManager.getRuleViolations(ARTIFACT_REF_ID, ArtifactType.API, RULESET_ID, ORGANIZATION))
                .thenReturn(Collections.singletonList(violation));
    }

    @Test
    public void testStatusIsReportedWhenTheFeatureIsOff() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(false);

        RulesetValidationResultDTO result = ComplianceAPIUtil.getRulesetValidationResultDTO(ARTIFACT_REF_ID,
                ArtifactType.API, RULESET_ID, USERNAME, ORGANIZATION);

        Assert.assertEquals("With the feature off every severity affects compliance, so a reported violation "
                        + "must fail the ruleset", RulesetValidationResultDTO.StatusEnum.FAILED, result.getStatus());
    }

    @Test
    public void testNoStatusIsReportedWhenTheFeatureIsOn() throws Exception {

        Mockito.when(policyManager.isComplianceAffectingSeverityFilteringEnabled()).thenReturn(true);

        RulesetValidationResultDTO result = ComplianceAPIUtil.getRulesetValidationResultDTO(ARTIFACT_REF_ID,
                ArtifactType.API, RULESET_ID, USERNAME, ORGANIZATION);

        Assert.assertNull("This ruleset can legitimately pass under one governing policy and fail under another, "
                        + "so no single status must be invented", result.getStatus());
        Assert.assertFalse("Every violation must still be reported to the user regardless of the status",
                result.getViolatedRules().isEmpty());
    }
}
