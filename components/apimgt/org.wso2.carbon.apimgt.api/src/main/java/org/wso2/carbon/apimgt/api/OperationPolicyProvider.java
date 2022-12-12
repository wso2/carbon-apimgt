/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.OperationPolicyData;

import java.util.List;

public interface OperationPolicyProvider {

    /**
     * Get all common operation policies
     *
     * @param organization organization
     * @return List of OperationPolicyData
     * @throws APIManagementException failed to get common operation polices
     */
    List<OperationPolicyData> getAllCommonOperationPolicies(String organization) throws APIManagementException;

    /**
     * Get common operation policy for given name
     *
     * @param name         policyName
     * @param version      PolicyVersion
     * @param organization organization
     * @return Operation policy data
     * @throws APIManagementException failed to get common operation policy
     */
    OperationPolicyData getCommonOperationPolicyByPolicyName(
            String name, String version, String organization, boolean isWithPolicyDefinition) throws APIManagementException;

    /**
     * Get common operation policy for given id
     *
     * @param policyId     policy ID
     * @param organization organization
     * @return Operation policy data
     * @throws APIManagementException failed to get common operation policy
     */
    OperationPolicyData getCommonOperationPolicyByPolicyId(
            String policyId, String organization, boolean isWithPolicyDefinition) throws APIManagementException;

    /**
     * Add operation policy as a common policy
     *
     * @param operationPolicyData policy data to be added
     * @param organization        organization
     * @return new policy ID
     * @throws APIManagementException failed to add common operation policy
     */
    String addCommonOperationPolicy(OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;

    /**
     * Update operation policy by id
     *
     * @param policyId            policy ID
     * @param operationPolicyData Operation policy data
     * @param organization        organization
     * @throws APIManagementException failed to update operation policy
     */
    void updateOperationPolicy(String policyId, OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;

    /**
     * Delete operation policy
     *
     * @param policyId     policy ID
     * @param organization organization
     * @throws APIManagementException failed to delete operation policy
     */
    void deleteOperationPolicyById(String policyId, String organization) throws APIManagementException;

    /**
     * Get API-Specific operation policy by policy name
     *
     * @param apiUUID       API UUID
     * @param policyName    operation policy name
     * @param policyVersion operation policy version
     * @param organization  organization
     * @return Operation policy data object
     * @throws APIManagementException failed to get API-Specific operation policy
     */
    OperationPolicyData getAPISpecificOperationPolicyByPolicyName(
            String apiUUID, String policyName, String policyVersion, String organization, boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Get API-Specific operation policy by policy ID
     *
     * @param apiUUID      API UUID
     * @param policyId     policy ID
     * @param organization organization
     * @return Operation policy data object
     * @throws APIManagementException failed to get API-Specific operation policy
     */
    OperationPolicyData getAPISpecificOperationPolicyByPolicyID
    (String apiUUID, String policyId, String organization, boolean isWithPolicyDefinition)
            throws APIManagementException;

    /**
     * Get all API-Specific operation policies of an API. (these are not attached policies to the API)
     *
     * @param apiUUID      API UUID
     * @param organization organization
     * @return List of Operation policy data objects
     * @throws APIManagementException failed to get API-Specific operation policies
     */
    List<OperationPolicyData> getAllAPiSpecificOperationPolicies(String apiUUID, String organization)
            throws APIManagementException;

    /**
     * Clone an API-Specific policy from one API to another API as API-Specific policy
     *
     * @param policyId     policy ID
     * @param OldApiUUID   API UUID (from)
     * @param NewApiUUID   API UUID (to)
     * @param organization organization
     * @return cloned policy id
     * @throws APIManagementException failed to clone operation policy
     */
    String cloneAPISpecificOperationPolicy(String policyId, String OldApiUUID, String NewApiUUID, String organization)
            throws APIManagementException;

    /**
     * Clone all operation policies from one API to another API as API-Specific policies
     *
     * @param oldApiUUID   API UUID (from)
     * @param newApiUUID   API UUID (to)
     * @param organization organization
     * @throws APIManagementException failed to clone operation policies
     */
    void cloneAllApiSpecificOperationPolicies(String oldApiUUID, String newApiUUID, String organization)
            throws APIManagementException;

    /**
     * Revision API-Specific operation policy.
     *
     * @param policyId     policy ID
     * @param apiUUID      API UUID
     * @param revisionId   revision ID of policy
     * @param organization organization
     * @return revisioned policy ID
     * @throws APIManagementException failed to revision operation policy
     */
    String revisionOperationPolicy(String policyId, String apiUUID, String revisionId, String organization)
            throws APIManagementException;

    /**
     * Add operation policy to an API
     *
     * @param apiID               API UUID
     * @param operationPolicyData policy data
     * @param organization        organization
     * @return newly added policy ID
     * @throws APIManagementException failed to add API-Specific policy
     */
    String addAPISpecificOperationPolicy(String apiID, OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;

    /**
     * Default Operation Policy Provider will implement this as follows,
     * This method will be used to import Operation policy.
     * This will check existing API specific policy first and then common policy.
     * If API specific policy exists and MD5 hash matches, it will not import and will return the existing API specific policy.
     * If the existing API specific policy is different in md5, it will be updated the existing policy
     * If a common policy exists and MD5 hash match, it will return the common policy's id.
     * This policy will be imported at the API update.
     * If the common policy is different from the imported policy, a new API specific policy will be created.
     * If there aren't any existing policies, a new API specific policy will be created.
     *
     * @param operationPolicyData policy data
     * @param organization        organization
     * @return imported policy ID
     * @throws APIManagementException failed to import
     */
    String importOperationPolicy(OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException;
}
