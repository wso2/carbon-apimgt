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

/**
 * SQL constants for AM_API_SUCCESSOR_MAPPING DAO operations.
 * This table persists the user-confirmed successor selection made during
 * Deprecate / Retire lifecycle transitions (part of "The Guide" engine).
 */
public final class SuccessorMappingSQLConstants {

    private SuccessorMappingSQLConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Insert or update a successor mapping (MySQL ON DUPLICATE KEY UPDATE).
     */
    public static final String UPSERT_MAPPING =
            "INSERT INTO AM_API_SUCCESSOR_MAPPING (API_UUID, SUCCESSOR_UUID, ORGANIZATION, MAPPING_TIMESTAMP) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE SUCCESSOR_UUID = VALUES(SUCCESSOR_UUID), " +
                    "MAPPING_TIMESTAMP = VALUES(MAPPING_TIMESTAMP)";

    /**
     * Get the successor UUID for a deprecated/retired API.
     */
    public static final String GET_SUCCESSOR =
            "SELECT SUCCESSOR_UUID, MAPPING_TIMESTAMP " +
                    "FROM AM_API_SUCCESSOR_MAPPING WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Delete a successor mapping (e.g. when an API is un-deprecated or deleted).
     */
    public static final String DELETE_MAPPING =
            "DELETE FROM AM_API_SUCCESSOR_MAPPING WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Check if a successor mapping exists for a given API.
     */
    public static final String CHECK_MAPPING_EXISTS =
            "SELECT 1 FROM AM_API_SUCCESSOR_MAPPING WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Get all APIs that point to a given successor (reverse lookup).
     * Useful for determining if a successor API itself is being deprecated.
     */
    public static final String GET_APIS_BY_SUCCESSOR =
            "SELECT API_UUID, MAPPING_TIMESTAMP " +
                    "FROM AM_API_SUCCESSOR_MAPPING WHERE SUCCESSOR_UUID = ? AND ORGANIZATION = ?";

    /**
     * Delete all successor mappings referencing a given API — either as
     * the deprecated API itself or as the successor target.
     * Used during API deletion to fully clean up the successor table.
     */
    public static final String DELETE_ALL_REFERENCES =
            "DELETE FROM AM_API_SUCCESSOR_MAPPING WHERE (API_UUID = ? OR SUCCESSOR_UUID = ?) AND ORGANIZATION = ?";
}
