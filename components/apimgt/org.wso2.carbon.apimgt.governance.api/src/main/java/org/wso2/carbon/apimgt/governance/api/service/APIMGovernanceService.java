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

package org.wso2.carbon.apimgt.governance.api.service;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;

import java.util.List;

public interface APIMGovernanceService {

    /**
     * Get IDs of applicable policies for the artifact
     *
     * @param artifactId   Artifact ID
     * @param state        State to be governed
     * @param organization Organization
     * @return List of policy IDs
     */
    List<String> getApplicablePolicies(String artifactId,
                                       GovernableState state, String organization) throws GovernanceException;

    /**
     * Check if there are any blocking actions for the artifact based on the policies
     *
     * @param policyIds       List of policy IDs to evaluate
     * @param governableState State to be governed
     * @return True if there are blocking actions, False otherwise
     */
    boolean checkForBlockingActions(List<String> policyIds, GovernableState governableState) throws GovernanceException;

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type
     * @param policyIds    List of policy IDs to evaluate
     * @param organization Organization
     */
    void evaluateComplianceAsync(String artifactId, ArtifactType artifactType,
                                 List<String> policyIds,
                                 String organization) throws GovernanceException;
}
