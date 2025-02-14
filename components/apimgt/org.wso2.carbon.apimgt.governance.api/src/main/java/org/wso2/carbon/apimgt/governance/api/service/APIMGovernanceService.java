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

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceDryRunInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;

import java.util.List;
import java.util.Map;

/**
 * This class represents the Governance Service, which is responsible for managing governance related operations
 */
public interface APIMGovernanceService {

    /**
     * Check if there are any policies with blocking actions for the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param state         State to be governed
     * @param organization  Organization
     * @return True if there are policies with blocking actions, False otherwise
     * @throws APIMGovernanceException If an error occurs while checking for policies with blocking actions
     */
    boolean isPoliciesWithBlockingActionExist(String artifactRefId, ArtifactType artifactType,
                                              APIMGovernableState state, String organization)
            throws APIMGovernanceException;

    /**
     * Evaluate compliance of the artifact asynchronously
     *
     * @param artifactRefId Artifact Reference ID (ID of artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param state         State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    void evaluateComplianceAsync(String artifactRefId, ArtifactType artifactType,
                                 APIMGovernableState state,
                                 String organization) throws APIMGovernanceException;

    /**
     * Evaluate compliance of the artifact synchronously
     *
     * @param artifactRefId          Artifact Reference ID (ID of artifact on APIM side)
     * @param revisionNo             Revision number
     * @param artifactType           Artifact type (ArtifactType.REST_API)
     * @param state                  State at which artifact should be governed (CREATE, UPDATE, DEPLOY, PUBLISH)
     * @param artifactProjectContent This is a map of RuleType and String which contains the content of the artifact
     *                               project. This is used to evaluate the compliance of the artifact.
     *                               API_METADATA --> api.yaml content
     *                               API_DEFINITION --> api definition content
     *                               API_DOCUMENTATION --> api documentation content
     *                               If no content is specified content fetched from DB
     * @param organization           Organization
     * @return ArtifactComplianceInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    ArtifactComplianceInfo evaluateComplianceSync(String artifactRefId, String revisionNo, ArtifactType artifactType,
                                                  APIMGovernableState state,
                                                  Map<RuleType, String> artifactProjectContent,
                                                  String organization) throws APIMGovernanceException;

    /**
     * This method can be called to evaluate the compliance of the artifact without persisting the compliance data (A
     * dry run) using the provided artifact content file path and the artifact type. The artifact will be evaluated
     * against all the global policies configured in the system.
     *
     * @param artifactType Artifact type (ExtendedArtifactType.REST_API, etc)
     * @param zipArchive   File path of the artifact content (ZIP path)
     * @param organization Organization
     * @return ArtifactComplianceDryRunInfo object
     * @throws APIMGovernanceException If an error occurs while evaluating compliance
     */
    ArtifactComplianceDryRunInfo evaluateComplianceDryRunSync(ExtendedArtifactType artifactType, byte[] zipArchive,
                                                              String organization) throws APIMGovernanceException;

    /**
     * Handle artifact label attach
     *
     * @param artifactRefId Artifact Reference ID (ID of artifact on APIM side)
     * @param artifactType  Artifact type (ArtifactType.API)
     * @param labels        ID of the labels to be attached
     * @param organization  Organization
     * @throws APIMGovernanceException If an error occurs while attaching the label
     */
    void evaluateComplianceOnLabelAttach(String artifactRefId, ArtifactType artifactType, List<String> labels,
                                         String organization)
            throws APIMGovernanceException;

    /**
     * Delete governance related data for the given label
     *
     * @param label        Label id to delete governance data
     * @param organization Organization
     * @throws APIMGovernanceException If an error occurs while deleting governance data
     */
    void deleteGovernanceDataForLabel(String label, String organization) throws APIMGovernanceException;

    /**
     * Delete all governance data related to the artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of artifact on APIM side)
     * @throws APIMGovernanceException If an error occurs while clearing the compliance information
     */
    void clearArtifactComplianceInfo(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException;
}
