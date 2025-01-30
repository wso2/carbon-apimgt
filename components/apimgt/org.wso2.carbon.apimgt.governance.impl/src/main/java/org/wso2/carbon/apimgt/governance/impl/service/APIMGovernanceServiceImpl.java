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
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        complianceManager = new ComplianceManagerImpl();
        policyManager = new PolicyManagerImpl();
    }

    /**
     * Check if there are any policies with blocking actions for the artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type (ArtifactType.API)
     * @param state        State to be governed
     * @param organization Organization
     * @return True if there are policies with blocking actions, False otherwise
     * @throws GovernanceException If an error occurs while checking for policies with blocking actions
     */
    @Override
    public boolean isPoliciesWithBlockingActionExist(String artifactId,
                                                     ArtifactType artifactType, GovernableState state,
                                                     String organization)
            throws GovernanceException {

        List<String> applicablePolicyIds = GovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactId,
                artifactType, state, organization);
        return GovernanceUtil.isBlockingActionsPresent(applicablePolicyIds, state);
    }

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type (ArtifactType.REST_API) , Needs to be specific
     *                     , DO NOT USE ArtifactType.API
     * @param state        State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization Organization
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public void evaluateComplianceAsync(String artifactId, ArtifactType artifactType,
                                        GovernableState state, String organization) throws
            GovernanceException {

        List<GovernableState> dependentGovernableStates = GovernableState.getDependentGovernableStates(state);

        for (GovernableState dependentState : dependentGovernableStates) {
            List<String> applicablePolicyIds = GovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactId,
                    artifactType, dependentState, organization);
            complianceManager.handleComplianceEvaluationAsync
                    (artifactId, artifactType, applicablePolicyIds, organization);
        }
    }

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactName    Artifact name
     * @param artifactVersion Artifact version
     * @param artifactType    Artifact type (ArtifactType.REST_API)
     * @param state           State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization    Organization
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public void evaluateComplianceAsync(String artifactName, String artifactVersion, ArtifactType artifactType,
                                        GovernableState state, String organization) throws GovernanceException {

        String artifactId = GovernanceUtil.getArtifactId(artifactName, artifactVersion, artifactType, organization);

        if (artifactId == null) {
            throw new GovernanceException(GovernanceExceptionCodes.ARTIFACT_NOT_FOUND_WITH_NAME_AND_VERSION,
                    artifactName, artifactVersion);
        }

        evaluateComplianceAsync(artifactId, artifactType, state, organization);

    }

    /**
     * Evaluate compliance of the artifact synchronously
     *
     * @param artifactId             Artifact ID
     * @param revisionNo             Revision number
     * @param artifactType           Artifact type (ArtifactType.REST_API) , Needs to be specific ,
     *                               DO NOT USE ArtifactType.API
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
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public ArtifactComplianceInfo evaluateComplianceSync(String artifactId, String revisionNo,
                                                         ArtifactType artifactType, GovernableState state,
                                                         Map<RuleType, String> artifactProjectContent,
                                                         String organization) throws GovernanceException {

        List<String> applicablePolicyIds = GovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactId,
                artifactType, state, organization);

        ArtifactComplianceInfo artifactComplianceInfo = complianceManager.handleComplianceEvaluationSync
                (artifactId, revisionNo,
                        artifactType, applicablePolicyIds, artifactProjectContent, state,
                        organization);

        // Though compliance is evaluated sync , we need to evaluate the compliance for all dependent states async to
        // update results in the database. Hence, calling the async method here and this won't take time as it is async
        evaluateComplianceAsync(artifactId, artifactType, state, organization);
        return artifactComplianceInfo;
    }

    /**
     * Handle artifact label attach
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type (ArtifactType.REST_API) , Needs to be specific ,
     *                     DO NOT USE ArtifactType.API
     * @param label        ID of the label to be attached
     * @param organization Organization
     * @throws GovernanceException If an error occurs while attaching the label
     */
    @Override
    public void evaluateComplianceOnLabelAttach(String artifactId, ArtifactType artifactType,
                                                String label, String organization) throws GovernanceException {

        List<String> applicablePolicyIds = new ArrayList<>(policyManager.getPoliciesByLabel(label,
                organization).keySet());

        complianceManager.handleComplianceEvaluationAsync(artifactId, artifactType, applicablePolicyIds, organization);

    }

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while clearing the compliance information
     */
    @Override
    public void clearArtifactComplianceInfo(String artifactId) throws GovernanceException {

        complianceManager.deleteArtifact(artifactId);
    }
}
