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
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceStatusDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests that a policy deleted between reading an artifact's evaluated policies and reading its rulesets does not
 * cost the artifact its place in the compliance listing.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComplianceAPIUtil.class, APIMGovernanceUtil.class})
public class GetArtifactComplianceListDTOTest {

    private static final String ARTIFACT_REF_ID = "d090cf7c-d1ab-491c-9357-b55a47e49ef2";
    private static final String ORGANIZATION = "carbon.super";
    private static final String USERNAME = "admin";
    private static final String SURVIVING_POLICY_ID = "policy-surviving";
    private static final String DELETED_POLICY_ID = "policy-deleted-concurrently";

    private ComplianceManager complianceManager;
    private PolicyManager policyManager;

    @Before
    public void setUp() throws Exception {

        complianceManager = Mockito.mock(ComplianceManager.class);
        PowerMockito.whenNew(ComplianceManager.class).withNoArguments().thenReturn(complianceManager);

        policyManager = Mockito.mock(PolicyManager.class);
        PowerMockito.whenNew(PolicyManager.class).withNoArguments().thenReturn(policyManager);

        PowerMockito.mockStatic(APIMGovernanceUtil.class);
        PowerMockito.when(APIMGovernanceUtil.getAllArtifacts(ArtifactType.API, USERNAME, ORGANIZATION))
                .thenReturn(Collections.singletonList(ARTIFACT_REF_ID));
        PowerMockito.when(APIMGovernanceUtil.getArtifactName(ARTIFACT_REF_ID, ArtifactType.API, ORGANIZATION))
                .thenReturn("Test API");
        PowerMockito.when(APIMGovernanceUtil.getArtifactVersion(ARTIFACT_REF_ID, ArtifactType.API, ORGANIZATION))
                .thenReturn("1.0.0");
        PowerMockito.when(APIMGovernanceUtil.getArtifactOwner(ARTIFACT_REF_ID, ArtifactType.API, ORGANIZATION))
                .thenReturn("admin");
        PowerMockito.when(APIMGovernanceUtil.getExtendedArtifactTypeForArtifact(ARTIFACT_REF_ID, ArtifactType.API))
                .thenReturn(ExtendedArtifactType.REST_API);

        Map<String, String> applicablePolicies = new LinkedHashMap<>();
        applicablePolicies.put(SURVIVING_POLICY_ID, "Surviving Policy");
        applicablePolicies.put(DELETED_POLICY_ID, "Deleted Policy");
        PowerMockito.when(APIMGovernanceUtil.getApplicablePoliciesForArtifact(ARTIFACT_REF_ID, ArtifactType.API,
                ORGANIZATION)).thenReturn(applicablePolicies);

        List<String> evaluatedPolicies = Arrays.asList(SURVIVING_POLICY_ID, DELETED_POLICY_ID);
        Mockito.when(complianceManager.getEvaluatedPoliciesForArtifact(ARTIFACT_REF_ID, ArtifactType.API,
                ORGANIZATION)).thenReturn(evaluatedPolicies);
        Mockito.when(complianceManager.getPendingPoliciesForArtifact(ARTIFACT_REF_ID, ArtifactType.API, ORGANIZATION))
                .thenReturn(Collections.emptyList());

        RuleViolation violation = new RuleViolation();
        violation.setRuleName("api-description-check");
        violation.setRulesetId("ruleset-1");
        violation.setSeverity(RuleSeverity.WARN);
        Map<RuleSeverity, List<RuleViolation>> violationsBySeverity = new HashMap<>();
        violationsBySeverity.put(RuleSeverity.WARN, Collections.singletonList(violation));
        Mockito.when(complianceManager.getSeverityBasedRuleViolationsForArtifact(ARTIFACT_REF_ID, ArtifactType.API,
                ORGANIZATION)).thenReturn(violationsBySeverity);

        // No policy has narrowed its severities, and the surviving policy holds no ruleset the violation
        // belongs to, so the artifact reports compliant regardless of the deleted policy.
        Mockito.when(policyManager.getComplianceAffectingSeverities(ORGANIZATION))
                .thenReturn(Collections.emptyMap());
        RulesetInfo surviving = new RulesetInfo();
        surviving.setId("ruleset-unrelated");
        Mockito.when(policyManager.getRulesetsByPolicyId(SURVIVING_POLICY_ID, ORGANIZATION))
                .thenReturn(Collections.singletonList(surviving));

        // The policy was deleted after evaluatedPolicies was read
        Mockito.when(policyManager.getRulesetsByPolicyId(DELETED_POLICY_ID, ORGANIZATION))
                .thenThrow(new APIMGovernanceException(APIMGovExceptionCodes.POLICY_NOT_FOUND, DELETED_POLICY_ID));
    }

    @Test
    public void testAConcurrentlyDeletedPolicyDoesNotDropTheArtifact() throws Exception {

        ArtifactComplianceListDTO result = ComplianceAPIUtil.getArtifactComplianceListDTO(ArtifactType.API, USERNAME,
                ORGANIZATION, 25, 0);

        Assert.assertEquals("A policy deleted between reading the evaluated policies and reading its rulesets "
                        + "must not remove the artifact from the listing", 1, result.getList().size());
        ArtifactComplianceStatusDTO status = result.getList().get(0);
        Assert.assertEquals(ARTIFACT_REF_ID, status.getId());
        Mockito.verify(policyManager).getRulesetsByPolicyId(ArgumentMatchers.eq(SURVIVING_POLICY_ID),
                ArgumentMatchers.eq(ORGANIZATION));
    }
}
