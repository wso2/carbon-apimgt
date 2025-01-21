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

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

import java.util.List;

@Component(
        name = "org.wso2.carbon.apimgt.governance.service",
        service = APIMGovernanceService.class,
        immediate = true
)
public class APIMGovernanceServiceImpl implements APIMGovernanceService {

    /**
     * Check if there are any policies with blocking actions for the artifact
     *
     * @param artifactId   Artifact ID
     * @param state        State to be governed
     * @param organization Organization
     * @return True if there are policies with blocking actions, False otherwise
     * @throws GovernanceException If an error occurs while checking for policies with blocking actions
     */
    @Override
    public boolean isPoliciesWithBlockingActionExist(String artifactId, GovernableState state, String organization)
            throws GovernanceException {
        List<String> applicablePolicyIds = GovernanceUtil.getApplicableGovPoliciesForArtifact(artifactId, state,
                organization);
        return GovernanceUtil.isBlockingActionsPresent(applicablePolicyIds, state);
    }


    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type
     * @param state        State to be governed
     * @param organization Organization
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    @Override
    public void evaluateComplianceAsync(String artifactId, ArtifactType artifactType,
                                        GovernableState state, String organization) throws
            GovernanceException {
        List<String> applicablePolicyIds = GovernanceUtil.getApplicableGovPoliciesForArtifact(artifactId,
                state, organization);
        new ComplianceManagerImpl().handleComplianceEvaluationAsync
                (artifactId, artifactType, applicablePolicyIds, organization);
    }
}
