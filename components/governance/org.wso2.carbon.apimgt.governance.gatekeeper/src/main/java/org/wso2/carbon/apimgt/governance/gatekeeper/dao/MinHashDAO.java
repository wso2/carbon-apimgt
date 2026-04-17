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

package org.wso2.carbon.apimgt.governance.gatekeeper.dao;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.APISignature;

import java.util.List;

/**
 * DAO interface for managing API MinHash signatures.
 */
public interface MinHashDAO {

    /**
     * Stores an API signature in the database.
     *
     * @param apiSignature The API signature to store
     * @throws APIMGovernanceException If storage fails
     */
    void storeSignature(APISignature apiSignature) throws APIMGovernanceException;

    /**
     * Updates an existing API signature.
     *
     * @param apiSignature The updated API signature
     * @throws APIMGovernanceException If update fails
     */
    void updateSignature(APISignature apiSignature) throws APIMGovernanceException;

    /**
     * Retrieves an API signature by UUID.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return The API signature or null if not found
     * @throws APIMGovernanceException If retrieval fails
     */
    APISignature getSignature(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Retrieves all API signatures for an organization.
     *
     * @param organization The organization
     * @return List of API signatures
     * @throws APIMGovernanceException If retrieval fails
     */
    List<APISignature> getAllSignatures(String organization) throws APIMGovernanceException;

    /**
     * Retrieves all API signatures across all organizations.
     * Used for hydration during server startup.
     *
     * @return List of all API signatures
     * @throws APIMGovernanceException If retrieval fails
     */
    List<APISignature> getAllSignatures() throws APIMGovernanceException;

    /**
     * Deletes an API signature.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @throws APIMGovernanceException If deletion fails
     */
    void deleteSignature(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Checks if a signature exists for the given API.
     *
     * @param apiUuid      The API UUID
     * @param organization The organization
     * @return True if signature exists
     * @throws APIMGovernanceException If check fails
     */
    boolean signatureExists(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Stores or updates a signature based on existence.
     *
     * @param apiSignature The API signature
     * @throws APIMGovernanceException If operation fails
     */
    void upsertSignature(APISignature apiSignature) throws APIMGovernanceException;
}
