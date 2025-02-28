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

package org.wso2.carbon.apimgt.governance.api;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;

import java.util.List;
import java.util.Map;

/**
 * This interface acts as the base for implementing governance capabilities to different artifacts  in API Manager
 * such as APIs, Applications, etc.
 */
public interface ArtifactGovernanceHandler {

    /**
     * This method is used to get all the artifacts of a given type in a given organization
     *
     * @param organization organization name
     * @return List of artifact ids
     * @throws APIMGovernanceException if an error occurs while getting the artifacts
     */
    List<String> getAllArtifacts(String organization) throws APIMGovernanceException;

    /**
     * This method is used to get all the artifacts visible to a given user in a given organization
     *
     * @param username     username of logged-in user
     * @param organization organization name
     * @return List of artifact ids
     * @throws APIMGovernanceException if an error occurs while getting the artifacts
     */
    List<String> getAllArtifacts(String username, String organization) throws APIMGovernanceException;

    /**
     * This method is used to get all the artifacts attached to a given label in a given organization
     *
     * @param label label id
     * @return List of artifact ids
     * @throws APIMGovernanceException if an error occurs while getting the artifacts
     */
    List<String> getArtifactsByLabel(String label) throws APIMGovernanceException;

    /**
     * This method is used to get all the labels attached to a given artifact in a given organization
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @return List of label ids
     * @throws APIMGovernanceException if an error occurs while getting the labels
     */
    List<String> getLabelsForArtifact(String artifactRefId) throws APIMGovernanceException;

    /**
     * This method checks whether an artifact is available in the given organization
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param organization  organization name
     * @return true if the artifact is available, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the availability
     */
    boolean isArtifactAvailable(String artifactRefId, String organization) throws APIMGovernanceException;

    /**
     * This method checks whether an artifact is visible to a given user in the given organization
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param username      username of logged-in user
     * @param organization  organization name
     * @return true if the artifact is visible, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the visibility
     */
    boolean isArtifactVisibleToUser(String artifactRefId, String username, String organization)
            throws APIMGovernanceException;

    /**
     * Given a list of governable states, this method checks whether the artifact is governable considering
     * artifacts current state on the APIM side
     *
     * @param artifactRefId    artifact reference id (uuid on APIM side)
     * @param governableStates list of governable states
     * @return true if the artifact is governable, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the governability
     */
    boolean isArtifactGovernable(String artifactRefId, List<APIMGovernableState> governableStates)
            throws APIMGovernanceException;

    /**
     * This method checks whether the artifact is governable considering the artifacts type
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @return true if the artifact is governable, false otherwise
     * @throws APIMGovernanceException if an error occurs while checking the governability
     */
    boolean isArtifactGovernable(String artifactRefId) throws APIMGovernanceException;

    /**
     * This method is used to get the name of an artifact
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param organization  organization name
     * @return name of the artifact
     * @throws APIMGovernanceException if an error occurs while getting the name
     */
    String getName(String artifactRefId, String organization) throws APIMGovernanceException;

    /**
     * This method is used to get the version of an artifact
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param organization  organization name
     * @return version of the artifact
     * @throws APIMGovernanceException if an error occurs while getting the version
     */
    String getVersion(String artifactRefId, String organization) throws APIMGovernanceException;

    /**
     * This method is used to get the owner of an artifact
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param organization  organization name
     * @return owner of the artifact
     * @throws APIMGovernanceException if an error occurs while getting the owner
     */
    String getOwner(String artifactRefId, String organization) throws APIMGovernanceException;

    /**
     * This method is used to convert the artifacts type on APIM side to the ExtendedArtifactType
     * for the operations in the governance side
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException if an error occurs while getting the extended artifact type
     */
    ExtendedArtifactType getExtendedArtifactType(String artifactRefId) throws APIMGovernanceException;

    /**
     * This method is used to get the artifact project zip
     *
     * @param artifactRefId artifact reference id (uuid on APIM side)
     * @param revisionId     revision number of the artifact (can be null)
     * @param organization  organization name
     * @return artifact content
     * @throws APIMGovernanceException if an error occurs while getting the artifact zip
     */
    byte[] getArtifactProject(String artifactRefId, String revisionId, String organization)
            throws APIMGovernanceException;


    /**
     * This method is used to extract the artifact project in to a map of RuleType and
     * the content for governance operations
     *
     * @param artifactProject artifact project zip
     * @return Map of RuleType and the content
     * @throws APIMGovernanceException if an error occurs while extracting the artifact project
     */
    Map<RuleType, String> extractArtifactProject(byte[] artifactProject) throws APIMGovernanceException;

    /**
     * This method is used to get the extended artifact type from the artifact project
     *
     * @param artifactProject artifact project zip
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException if an error occurs while getting the extended artifact type
     */
    ExtendedArtifactType getExtendedArtifactTypeFromProject(byte[] artifactProject) throws APIMGovernanceException;
}
