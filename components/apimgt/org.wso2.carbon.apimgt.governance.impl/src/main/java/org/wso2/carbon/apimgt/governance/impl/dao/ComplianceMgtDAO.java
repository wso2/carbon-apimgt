/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.dao;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.EvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.EvaluationStatus;

import java.util.List;

/**
 * This interface represents the DAO class related assessing compliance of APIs
 */
public interface ComplianceMgtDAO {

    /**
     * Add an artifact compliance evaluation request
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param policyId     Policy ID
     * @param organization Organization
     * @throws GovernanceException If an error occurs while adding the artifact
     *                             compliance evaluation request event
     */
    void addComplianceEvaluationRequest(String artifactId, ArtifactType artifactType,
                                        String policyId,
                                        String organization) throws GovernanceException;

    /**
     * Get pending evaluation requests
     *
     * @return List of pending evaluation requests
     * @throws GovernanceException If an error occurs while getting the pending evaluation requests
     */
    List<EvaluationRequest> getPendingEvaluationRequests() throws GovernanceException;

    /**
     * Update the evaluation status of a request
     *
     * @param requestId Evaluation request ID
     * @param status    Evaluation status
     * @throws GovernanceException If an error occurs while updating the evaluation status
     */
    void updateEvaluationStatus(String requestId, EvaluationStatus status) throws GovernanceException;

    /**
     * Delete an evaluation request
     *
     * @param requestId Evaluation request ID
     * @throws GovernanceException If an error occurs while deleting the evaluation request
     */
    void deleteEvaluationRequest(String requestId) throws GovernanceException;


}
