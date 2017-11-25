/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;

import java.util.List;
import java.util.Set;

/**
 * Provides access to Threat Protection data layer
 */
public interface ThreatProtectionDAO {

    /**
     * Get all threat protection policies as a list
     * @return A list containing {@link ThreatProtectionPolicy} objects.
     * An Empty list will be returned if no policies found.
     * @throws APIMgtDAOException if policy retrieval fails
     */
    List<ThreatProtectionPolicy> getPolicies() throws APIMgtDAOException;

    /**
     * Get a policy associated with policyId
     * @param policyId API_ID
     * @return  {@link ThreatProtectionPolicy}, if no policy is found for policyId, a null will be returned.
     * @throws APIMgtDAOException if policy retrieval fails
     */
    ThreatProtectionPolicy getPolicy(String policyId) throws APIMgtDAOException;

    /**
     * Adds a policy to the database
     * @param policy {@link ThreatProtectionPolicy}
     * @throws APIMgtDAOException if fails to add the policy
     */
    void addPolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException;

    /**
     * Updates a policy associated with policyId
     * @param policy {@link ThreatProtectionPolicy}
     * @throws APIMgtDAOException if fails to update the policy
     */
    void updatePolicy(ThreatProtectionPolicy policy) throws APIMgtDAOException;

    /**
     * Deletes the policy identified by policyId
     * @param policyId Threat Protection Policy ID
     * @throws APIMgtDAOException if failed to delete the policy
     */
    void deletePolicy(String policyId) throws APIMgtDAOException;

    /**
     * Checks whether a policy exists for the policyId
     * @param policyId API_ID
     * @return true if policy exists, false otherwise
     * @throws APIMgtDAOException if fails to get the existence status of the policy
     */
    boolean isPolicyExists(String policyId) throws APIMgtDAOException;

    /**
     * Get a Set of threat protection policy ids associated to an API
     * @param apiId API_ID
     * @return  Set of threat protection policy ids
     * @throws APIMgtDAOException if failed to retrieve policy ids
     */
    Set<String> getThreatProtectionPolicyIdsForApi(String apiId) throws APIMgtDAOException;

}
