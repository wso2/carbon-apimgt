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
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;

import java.util.Map;

/**
 * This class represents the Governance Service, which is responsible for managing governance related operations
 */
public interface APIMGovernanceService {

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
    boolean isPoliciesWithBlockingActionExist(String artifactId, ArtifactType artifactType,
                                              GovernableState state, String organization) throws GovernanceException;

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type (ArtifactType.REST_API)
     * @param state        State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization Organization
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    void evaluateComplianceAsync(String artifactId, ArtifactType artifactType,
                                 GovernableState state,
                                 String organization) throws GovernanceException;

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
    void evaluateComplianceAsync(String artifactName, String artifactVersion, ArtifactType artifactType,
                                 GovernableState state,
                                 String organization) throws GovernanceException;

    /**
     * Evaluate compliance of the artifact synchronously
     *
     * @param artifactId             Artifact ID
     * @param revisionNo             Revision number
     * @param artifactType           Artifact type (ArtifactType.REST_API)
     * @param state                  State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param artifactProjectContent This is a map of RuleType and String which contains the content of the artifact
     *                               project. This is used to evaluate the compliance of the artifact.
     *                               API_METADATA --> api.yaml content
     *                               API_DEFINITION --> api definition content
     *                               API_DOCUMENTATION --> api documentation content
     *                               <p>
     *                               If no content is specified content fetched from DB
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws GovernanceException If an error occurs while evaluating compliance
     */
    ArtifactComplianceInfo evaluateComplianceSync(String artifactId, String revisionNo, ArtifactType artifactType,
                                                  GovernableState state, Map<RuleType, String> artifactProjectContent,
                                                  String organization) throws GovernanceException;

    /**
     * Handle artifact label attach
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact type (ArtifactType.REST_API, etc)
     * @param label        ID of the label to be attached
     * @param organization Organization
     * @throws GovernanceException If an error occurs while attaching the label
     */
    void evaluateComplianceOnLabelAttach(String artifactId, ArtifactType artifactType, String label,
                                         String organization)
            throws GovernanceException;

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactId Artifact ID
     * @throws GovernanceException If an error occurs while clearing the compliance information
     */
    void clearArtifactComplianceInfo(String artifactId) throws GovernanceException;
}
