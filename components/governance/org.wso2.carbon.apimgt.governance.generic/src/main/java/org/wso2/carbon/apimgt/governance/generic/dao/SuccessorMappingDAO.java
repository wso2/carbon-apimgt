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

package org.wso2.carbon.apimgt.governance.generic.dao;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

/**
 * DAO interface for managing API successor mappings.
 * Persists the user-confirmed successor selection made during
 * Deprecate / Retire lifecycle transitions (part of "The Guide" engine).
 */
public interface SuccessorMappingDAO {

    /**
     * Stores or updates the successor mapping for an API.
     * Uses UPSERT semantics — if a mapping already exists for the given
     * (apiUuid, organization) pair, the successor is updated in place.
     *
     * @param apiUuid       The UUID of the API being deprecated/retired
     * @param successorUuid The UUID of the chosen successor API
     * @param organization  The organization (tenant domain)
     * @throws APIMGovernanceException If storage fails
     */
    void addSuccessorMapping(String apiUuid, String successorUuid, String organization)
            throws APIMGovernanceException;

    /**
     * Retrieves the successor UUID for a given API.
     *
     * @param apiUuid      The UUID of the deprecated/retired API
     * @param organization The organization (tenant domain)
     * @return The successor API UUID, or null if no mapping exists
     * @throws APIMGovernanceException If retrieval fails
     */
    String getSuccessorId(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Deletes the successor mapping for a given API.
     *
     * @param apiUuid      The UUID of the API
     * @param organization The organization (tenant domain)
     * @throws APIMGovernanceException If deletion fails
     */
    void deleteSuccessorMapping(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Checks if a successor mapping exists for a given API.
     *
     * @param apiUuid      The UUID of the API
     * @param organization The organization (tenant domain)
     * @return true if a mapping exists
     * @throws APIMGovernanceException If check fails
     */
    boolean mappingExists(String apiUuid, String organization) throws APIMGovernanceException;

    /**
     * Deletes all successor mappings referencing a given API UUID —
     * whether it appears as the deprecated API or as the successor target.
     * Called during API deletion to fully clean up orphaned rows.
     *
     * @param apiUuid      The UUID of the deleted API
     * @param organization The organization (tenant domain)
     * @throws APIMGovernanceException If deletion fails
     */
    void deleteAllReferences(String apiUuid, String organization) throws APIMGovernanceException;
}
