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

package org.wso2.carbon.apimgt.governance.impl.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the API Governance Service Implementation
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.service",
        service = APIMGovernanceService.class,
        immediate = true
)
public class APIMGovernanceServiceImpl implements APIMGovernanceService {

    private static final Log log = LogFactory.getLog(APIMGovernanceServiceImpl.class);
    private final ComplianceManager complianceManager;
    private final PolicyManager policyManager;

    public APIMGovernanceServiceImpl() {

        complianceManager = new ComplianceManager();
        policyManager = new PolicyManager();
    }

    /**
     * Check if there are any policies with blocking actions for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param state         State to be governed
     * @param organization  Organization
     * @return True if there are policies with blocking actions, False otherwise
     * @throws APIMGovernanceException If an error occurs while checking for policies with blocking actions
     */
    @Override
    public boolean isPoliciesWithBlockingActionExist(String artifactRefId,
                                                     ArtifactType artifactType, APIMGovernableState state,
                                                     String organization)
            throws APIMGovernanceException {

        List<String> applicablePolicyIds = APIMGovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactRefId,
                artifactType, state, organization);
        return APIMGovernanceUtil.isBlockingActionsPresent(applicablePolicyIds, state, organization);
    }

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     *                      , DO NOT USE ArtifactType.API
     * @param state         State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public void evaluateComplianceAsync(String artifactRefId, ArtifactType artifactType,
                                        APIMGovernableState state, String organization) throws
            APIMGovernanceException {

        List<APIMGovernableState> dependentAPIMGovernableStates =
                APIMGovernableState.getDependentGovernableStates(state);

        for (APIMGovernableState dependentState : dependentAPIMGovernableStates) {
            List<String> applicablePolicyIds = APIMGovernanceUtil
                    .getApplicablePoliciesForArtifactWithState(artifactRefId,
                            artifactType, dependentState, organization);
            complianceManager.handleComplianceEvalAsync
                    (artifactRefId, artifactType, applicablePolicyIds, organization);
        }
    }

    /**
     * Evaluate compliance of the artifact synchronously
     *
     * @param artifactRefId          Artifact Reference ID (ID of the artifact on APIM side)
     * @param revisionNo             Revision number
     * @param artifactType           Artifact type ArtifactType.API
     * @param state                  State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param artifactProjectContent This is a map of RuleType and String which contains the content of the artifact
     *                               project. This is used to evaluate the compliance of the artifact.
     *                               API_METADATA --> api.yaml content
     *                               API_DEFINITION --> api definition content
     *                               API_DOCUMENTATION --> api documentation content.
     *                               <p>
     *                               If not provided the details will be taken from DB
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public ArtifactComplianceInfo evaluateComplianceSync(String artifactRefId, String revisionNo,
                                                         ArtifactType artifactType, APIMGovernableState state,
                                                         Map<RuleType, String> artifactProjectContent,
                                                         String organization) throws APIMGovernanceException {

        List<String> applicablePolicyIds = APIMGovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactRefId,
                artifactType, state, organization);

        ArtifactComplianceInfo artifactComplianceInfo = complianceManager.handleComplianceEvalSync
                (artifactRefId, revisionNo, artifactType, applicablePolicyIds,
                        artifactProjectContent, state, organization);

        // Though compliance is evaluated sync , we need to evaluate the compliance for all dependent states async to
        // update results in the database. Hence, calling the async method here and this won't take time as it is async
        evaluateComplianceAsync(artifactRefId, artifactType, state, organization);
        return artifactComplianceInfo;
    }


    /**
     * This method can be called to evaluate the compliance of the artifact without persisting the compliance data (A
     * dry run) using the provided artifact content file path and the artifact type.
     *
     * @param artifactType Artifact type (ExtendedArtifactType.REST_API, etc)
     *                     project
     * @param organization Organization
     * @return ArtifactComplianceDryRunInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public ArtifactComplianceDryRunInfo evaluateComplianceDryRunSync(ExtendedArtifactType artifactType,
                                                                     byte[] zipArchive, String organization)
            throws APIMGovernanceException {

        if (log.isDebugEnabled()) {
            log.debug("Evaluating compliance for the artifact with the file path ");
        }

        Map<String, String> policies = policyManager.getOrganizationWidePolicies(organization);

        List<String> applicablePolicyIds = new ArrayList<>(policies.keySet());

        // Only extract content if the artifact type requires it.
        if (ExtendedArtifactType.isArtifactAPI(artifactType)) {
            Map<RuleType, String> contentMap = APIMGovernanceUtil
                    .extractArtifactProjectContent(zipArchive, ArtifactType.API);
            return complianceManager.handleComplianceEvalDryRun(artifactType, applicablePolicyIds,
                    contentMap, organization);
        } else {
            throw new APIMGovernanceException(APIMGovExceptionCodes.INVALID_ARTIFACT_TYPE, artifactType.toString());
        }


    }

    /**
     * Handle artifact label attach
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     * @param labels        List of label IDs
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while attaching the label
     */
    @Override
    public void evaluateComplianceOnLabelAttach(String artifactRefId, ArtifactType artifactType,
                                                List<String> labels,
                                                String organization) throws APIMGovernanceException {


        Set<String> allPoliciesForLabel = new HashSet<>();
        for (String label : labels) {
            allPoliciesForLabel.addAll(new ArrayList<>(policyManager
                    .getPoliciesByLabel(label, organization).keySet()));
        }

        List<String> applicablePolicyIds = new ArrayList<>();

        if (ArtifactType.API.equals(artifactType)) {
            // Find the state API is in and get the policies that should be evaluated
            String apiStatus = APIMUtil.getAPIStatus(artifactRefId);
            boolean isDeployed = APIMUtil.isAPIDeployed(artifactRefId);

            for (String policyId : allPoliciesForLabel) {
                APIMGovernancePolicy policy = policyManager.getGovernancePolicyByID(policyId, organization);
                boolean isAPIGovernable = APIMUtil.isAPIGovernable(apiStatus, isDeployed, policy.getGovernableStates());
                // If the API should be governed by the policy
                if (isAPIGovernable) {
                    applicablePolicyIds.add(policyId);
                }
            }
        }

        complianceManager.handleComplianceEvalAsync(artifactRefId, artifactType, applicablePolicyIds, organization);
    }

    /**
     * Delete governance related data for the given label
     *
     * @param label Label id to delete governance data
     * @throws APIMGovernanceException If an error occurs while deleting governance data
     */
    @Override
    public void deleteGovernanceDataForLabel(String label, String organization) throws APIMGovernanceException {
        policyManager.deleteLabelPolicyMappings(label, organization);
    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact type ArtifactType.API
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while clearing the compliance information
     */
    @Override
    public void clearArtifactComplianceInfo(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {

        complianceManager.deleteArtifact(artifactRefId, artifactType, organization);
    }
}
