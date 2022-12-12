/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.OperationPolicyProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;

public class DefaultOperationPolicyProviderImpl implements OperationPolicyProvider {

    private static final Log log = LogFactory.getLog(DefaultOperationPolicyProviderImpl.class);

    private final ApiMgtDAO apiMgtDAO;

    public DefaultOperationPolicyProviderImpl() {
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    /**
     * Get all common operation policies from database
     *
     * @param organization Organization
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    @Override
    public List<OperationPolicyData> getAllCommonOperationPolicies(String organization) throws APIManagementException {
        return apiMgtDAO.getLightWeightVersionOfAllOperationPolicies(null, organization);
    }

    /**
     * Get common operation policy from database by name
     *
     * @param policyName    policy name
     * @param policyVersion policy version
     * @param organization  Organization
     * @return Operation policy
     * @throws APIManagementException
     */
    @Override
    public OperationPolicyData getCommonOperationPolicyByPolicyName
    (String policyName, String policyVersion, String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {
        return apiMgtDAO.getCommonOperationPolicyByPolicyName(policyName, policyVersion, organization,
                isWithPolicyDefinition);
    }

    /**
     * Get common operation policy from database by ID
     *
     * @param policyId     policy ID
     * @param organization Organization name
     * @return Operation policy
     * @throws APIManagementException
     */
    @Override
    public OperationPolicyData getCommonOperationPolicyByPolicyId
    (String policyId, String organization, boolean isWithPolicyDefinition) throws APIManagementException {
        return apiMgtDAO.getCommonOperationPolicyByPolicyID(policyId, organization, isWithPolicyDefinition);
    }

    /**
     * Add a new common operation policy to the database.
     *
     * @param operationPolicyData Operation policy data.
     * @param organization        organization
     * @return new policy ID
     * @throws APIManagementException
     */
    @Override
    public String addCommonOperationPolicy(OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException {
        return apiMgtDAO.addCommonOperationPolicy(operationPolicyData);
    }

    /**
     * Update an existing operation policy with given data
     *
     * @param policyId            Shared policy UUID
     * @param operationPolicyData Updated policy definition
     * @throws APIManagementException
     */
    @Override
    public void updateOperationPolicy(String policyId, OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException {
        apiMgtDAO.updateOperationPolicy(policyId, operationPolicyData);
    }

    /**
     * Delete an operation policy by policy ID
     *
     * @param policyId UUID of the policy to be deleted
     * @throws APIManagementException
     */
    @Override
    public void deleteOperationPolicyById(String policyId, String organization) throws APIManagementException {
        apiMgtDAO.deleteOperationPolicyByPolicyId(policyId);
    }

    /**
     * Get API-Specific operation policy by name
     *
     * @param policyName    Policy name
     * @param apiUUID       apiID
     * @param policyVersion policy version
     * @param organization  Organization
     * @return Operation policy
     * @throws APIManagementException
     */
    @Override
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyName
    (String apiUUID, String policyName, String policyVersion, String organization, boolean isWithPolicyDefinition)
            throws APIManagementException {
        return apiMgtDAO.getAPISpecificOperationPolicyByPolicyName(policyName, policyVersion, apiUUID,
                null, organization, isWithPolicyDefinition);
    }

    /**
     * Get API-Specific operation policy by ID
     *
     * @param policyId     Policy id
     * @param apiUUID      apiID
     * @param organization Organization
     * @return Operation policy
     * @throws APIManagementException
     */
    @Override
    public OperationPolicyData getAPISpecificOperationPolicyByPolicyID
    (String apiUUID, String policyId, String organization, boolean isWithPolicyDefinition) throws APIManagementException {
        return apiMgtDAO.getAPISpecificOperationPolicyByPolicyID(policyId, apiUUID, organization, isWithPolicyDefinition);
    }

    /**
     * Get the list of all API-Specific operation policies.
     * This list will include policy specification of each policy but, It will not contain the
     * policy definition (light weight).
     *
     * @param apiUUID      UUID of the API if exists. Null for common operation policies
     * @param organization Organization name
     * @return List of Operation Policies
     * @throws APIManagementException
     */
    @Override
    public List<OperationPolicyData> getAllAPiSpecificOperationPolicies(String apiUUID, String organization)
            throws APIManagementException {
        return apiMgtDAO.getLightWeightVersionOfAllOperationPolicies(apiUUID, organization);
    }

    /**
     * Revision API specific operation policy
     *
     * @param policyId     policy Id
     * @param apiUUID      api Id
     * @param revisionId   revision Id
     * @param organization Organization
     * @return revisioned policy ID
     * @throws APIManagementException
     */
    @Override
    public String revisionOperationPolicy(String policyId, String apiUUID, String revisionId, String organization)
            throws APIManagementException {
        OperationPolicyData policyData = apiMgtDAO.getAPISpecificOperationPolicyByPolicyID(policyId, apiUUID,
                organization, true);
        if (policyData != null) {
            return apiMgtDAO.addAPISpecificOperationPolicy(apiUUID, revisionId, policyData);
        } else {
            throw new APIManagementException("Cannot revision policy with ID " + policyId + " as it does not exists.");
        }
    }

    /**
     * Add new operation policy
     *
     * @param apiID               apiID
     * @param operationPolicyData Data
     * @param organization        Organization
     * @return new policy ID
     * @throws APIManagementException
     */
    @Override
    public String addAPISpecificOperationPolicy(String apiID, OperationPolicyData operationPolicyData,
                                                String organization) throws APIManagementException {
        return apiMgtDAO.addAPISpecificOperationPolicy(apiID, null, operationPolicyData);
    }

    /**
     * Create a clone of an API-Specific operation policy; from one API to another API
     *
     * @param policyId     policy ID in old API
     * @param oldApiUUID   old API ID (from)
     * @param newApiUUID   new API ID (to)
     * @param organization org ID
     * @return cloned policy ID
     * @throws APIManagementException
     */
    @Override
    public String cloneAPISpecificOperationPolicy
    (String policyId, String oldApiUUID, String newApiUUID, String organization) throws APIManagementException {

        OperationPolicyData apiSpecificOperationPolicy = apiMgtDAO.getAPISpecificOperationPolicyByPolicyID
                (policyId, oldApiUUID, organization, true);
        return apiMgtDAO.cloneOperationPolicy(newApiUUID, apiSpecificOperationPolicy);
    }

    /**
     * Clone all operation policies of one API to another API
     *
     * @param oldApiUUID   apiId (from)
     * @param newApiUUID   apiId (to)
     * @param organization Organization
     * @throws APIManagementException
     */
    @Override
    public void cloneAllApiSpecificOperationPolicies(String oldApiUUID, String newApiUUID, String organization)
            throws APIManagementException {
        List<OperationPolicyData> operationPolicyDataList = getAllAPiSpecificOperationPolicies(oldApiUUID, organization);
        for (OperationPolicyData policyData : operationPolicyDataList) {
            apiMgtDAO.cloneOperationPolicy(newApiUUID, policyData);
        }

    }

    /**
     * Import Operation policy. This will check existing API specific policy first and
     * then common policy.
     * If API specific policy exists and MD5 hash matches, it will not import and will return the existing API specific policy.
     * If the existing API specific policy is different in md5, it will be updated the existing policy
     * If a common policy exists and MD5 hash match, it will return the common policy's id.
     * This policy will be imported at the API update.
     * If the common policy is different from the imported policy, a new API specific policy will be created.
     * If there aren't any existing policies, a new API specific policy will be created.
     *
     * @param importedPolicyData Imported policy
     * @param organization       Organization name
     * @return corresponding policy ID for imported data
     * @throws APIManagementException if failed to delete APIRevision
     */
    @Override
    public String importOperationPolicy(OperationPolicyData importedPolicyData, String organization)
            throws APIManagementException {

        OperationPolicySpecification importedSpec = importedPolicyData.getSpecification();
        OperationPolicyData existingOperationPolicy = getAPISpecificOperationPolicyByPolicyName(
                importedPolicyData.getApiUUID(), importedSpec.getName(), importedSpec.getVersion(), organization,
                false);
        String policyId = null;
        if (existingOperationPolicy != null) {
            if (existingOperationPolicy.getMd5Hash().equals(importedPolicyData.getMd5Hash())) {
                if (log.isDebugEnabled()) {
                    log.debug("Matching API specific policy found for imported policy and MD5 hashes match.");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Even though existing API specific policy name match with imported policy, "
                            + "the MD5 hashes does not match in the policy " + existingOperationPolicy.getPolicyId()
                            + ".Therefore updating the existing policy");
                }
                updateOperationPolicy(existingOperationPolicy.getPolicyId(), importedPolicyData, organization);
            }
            policyId = existingOperationPolicy.getPolicyId();
        } else {
            existingOperationPolicy = getCommonOperationPolicyByPolicyName(importedSpec.getName(),
                    importedSpec.getVersion(), organization, false);
            if (existingOperationPolicy != null) {
                if (existingOperationPolicy.getMd5Hash().equals(importedPolicyData.getMd5Hash())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Matching common policy found for imported policy and Md5 hashes match.");
                    }
                    policyId = existingOperationPolicy.getPolicyId();
                } else {
                    importedSpec.setName(importedSpec.getName() + "_imported");
                    importedSpec.setDisplayName(importedSpec.getDisplayName() + " Imported");
                    importedPolicyData.setSpecification(importedSpec);
                    importedPolicyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(importedPolicyData));
                    policyId = apiMgtDAO.addAPISpecificOperationPolicy(importedPolicyData.getApiUUID(), null,
                            importedPolicyData);
                    if (log.isDebugEnabled()) {
                        log.debug("Even though existing common policy name match with imported policy, " +
                                "the MD5 hashes does not match in the policy " + existingOperationPolicy.getPolicyId()
                                + ". A new policy created with ID " + policyId);
                    }
                }
            } else {
                policyId = apiMgtDAO.addAPISpecificOperationPolicy(importedPolicyData.getApiUUID(),
                        null, importedPolicyData);
                if (log.isDebugEnabled()) {
                    log.debug("There aren't any existing policies for the imported policy. A new policy created with ID "
                            + policyId);
                }
            }
        }
        return policyId;
    }
}
