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

package org.wso2.carbon.apimgt.governance.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.RulesetMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.RulesetMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Compliance Manager, which is responsible for managing compliance related operations
 */
public class ComplianceManagerImpl implements ComplianceManager {
    private static final Log log = LogFactory.getLog(ComplianceManagerImpl.class);

    private ComplianceMgtDAO complianceMgtDAO;
    private GovernancePolicyMgtDAO governancePolicyMgtDAO;

    private RulesetMgtDAO rulesetMgtDAO;

    public ComplianceManagerImpl() {
        complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        governancePolicyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
        rulesetMgtDAO = RulesetMgtDAOImpl.getInstance();
    }


    /**
     * Handle Policy Change Event
     *
     * @param policyId     Policy ID
     * @param organization Organization
     */
    @Override
    public void handlePolicyChangeEvent(String policyId, String organization) throws GovernanceException {

        // Get the policy and its labels and associated governable states

        GovernancePolicy policy = governancePolicyMgtDAO.getGovernancePolicyByID(organization, policyId);
        List<String> labels = policy.getLabels();

        List<GovernableState> governableStates = policy.getGovernableStates();

        // Get artifacts that should be governed by the policy as a Map of artifact ID and artifact type
        Map<String, String> artifacts = new HashMap<>();
        artifacts.putAll(getAPIArtifactsByLabelsAndGovernableState(labels, governableStates));

        for (Map.Entry<String, String> artifact : artifacts.entrySet()) {
            String artifactId = artifact.getKey();
            ArtifactType artifactType = ArtifactType.fromString(artifact.getValue());
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }
    }

    /**
     * Get API Artifacts by Labels and Governable State
     *
     * @param labels           List of labels
     * @param governableStates List of governable states
     * @return Map of artifact ID and artifact type
     */
    private Map<String, String> getAPIArtifactsByLabelsAndGovernableState(List<String> labels,
                                                                          List<GovernableState> governableStates) {
        Map<String, String> apiIdTypeMap = new HashMap<>();
        List<String> correspondingAPIStates =
                APIMUtil.getCorrespondingAPIStatusesForGovernableStates(governableStates);
        for (String label : labels) {
            // TODO: Get artifacts by label
            // TODO: Filter APIs from state
            //TODO: Get artifact type from APIM
        }
        return apiIdTypeMap;
    }

    /**
     * Handle Ruleset Change Event
     *
     * @param rulesetId    Ruleset ID
     * @param organization Organization
     */
    @Override
    public void handleRulesetChangeEvent(String rulesetId, String organization) throws GovernanceException {
        List<String> policies = rulesetMgtDAO.getAssociatedPoliciesForRuleset(rulesetId);

        for (String policyId : policies) {
            handlePolicyChangeEvent(policyId, organization);
        }
    }

    /**
     * Handle API Compliance Evaluation Request Async
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param govPolicies  List of governance policies to be evaluated
     * @param organization Organization
     * @throws GovernanceException If an error occurs while handling the API compliance evaluation
     */
    @Override
    public void handleComplianceEvaluationAsync(String artifactId, ArtifactType artifactType,
                                                List<String> govPolicies,
                                                String organization) throws GovernanceException {

        for (String policyId : govPolicies) {
            complianceMgtDAO.addComplianceEvaluationRequest(artifactId, artifactType,
                    policyId, organization);
        }

    }

    /**
     * Get Rule Violations
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return List of Rule Violations
     * @throws GovernanceException If an error occurs while getting the rule violations
     */
    @Override
    public List<RuleViolation> getRuleViolations(String artifactId, String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getRuleViolations(artifactId, policyId, rulesetId);
    }

    /**
     * Get Compliance Evaluation Result
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @param rulesetId  Ruleset ID
     * @return Compliance Evaluation Result
     * @throws GovernanceException If an error occurs while getting the compliance evaluation result
     */
    @Override
    public ComplianceEvaluationResult getComplianceEvaluationResult(String artifactId,
                                                                    String policyId, String rulesetId)
            throws GovernanceException {
        return complianceMgtDAO.getComplianceEvaluationResult(artifactId, policyId, rulesetId);
    }

    /**
     * Check whether the artifact evaluation results exist
     *
     * @param artifactId Artifact ID
     * @return True if the artifact evaluation results exist, False otherwise
     * @throws GovernanceException If an error occurs while checking the existence of the artifact evaluation results
     */
    @Override
    public boolean isArtifactEvaluationResultsExist(String artifactId) throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactId(artifactId);
        return complianceEvaluationResults != null && !complianceEvaluationResults.isEmpty();
    }

    /**
     * Check whether the policy evaluation results exist for a given artifact
     *
     * @param artifactId Artifact ID
     * @param policyId   Policy ID
     * @return True if the policy evaluation results exist, False otherwise
     * @throws GovernanceException If an error occurs while checking the existence of the policy evaluation results
     */
    @Override
    public boolean isPolicyEvaluationResultsExist(String artifactId, String policyId) throws GovernanceException {
        List<ComplianceEvaluationResult> complianceEvaluationResults =
                complianceMgtDAO.getComplianceEvaluationResultsByArtifactAndPolicyId(artifactId, policyId);
        return complianceEvaluationResults != null && !complianceEvaluationResults.isEmpty();
    }


    /**
     * Get a map of compliant and non-compliant artifacts
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of compliant and non-compliant artifacts
     * @throws GovernanceException If an error occurs while getting the compliant and non-compliant artifacts
     */
    @Override
    public Map<ArtifactComplianceState, List<String>> getCompliantAndNonCompliantArtifacts(
            ArtifactType artifactType, String organization) throws GovernanceException {
        List<String> allComplianceEvaluatedArtifacts =
                complianceMgtDAO.getAllComplianceEvaluatedArtifacts(artifactType, organization);
        List<String> nonCompliantArtifacts = complianceMgtDAO.getNonCompliantArtifacts(artifactType, organization);

        Map<ArtifactComplianceState, List<String>> compliantAndNonCompliantArtifacts = new HashMap<>();
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.COMPLIANT, new ArrayList<>());
        compliantAndNonCompliantArtifacts.put(ArtifactComplianceState.NON_COMPLIANT, new ArrayList<>());

        for (String artifact : allComplianceEvaluatedArtifacts) {
            if (nonCompliantArtifacts.contains(artifact)) {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).add(artifact);
            } else {
                compliantAndNonCompliantArtifacts.get(ArtifactComplianceState.COMPLIANT).add(artifact);
            }
        }

        return compliantAndNonCompliantArtifacts;
    }
}
