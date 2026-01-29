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
 * SQL constants for MinHash DAO operations.
 */
public final class MinHashSQLConstants {

    private MinHashSQLConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Insert a new API signature.
     */
    public static final String INSERT_SIGNATURE =
            "INSERT INTO AM_API_MINHASH (API_UUID, SIGNATURE_BLOB, ORGANIZATION, CREATED_TIME, UPDATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?)";

    /**
     * Update an existing API signature.
     */
    public static final String UPDATE_SIGNATURE =
            "UPDATE AM_API_MINHASH SET SIGNATURE_BLOB = ?, UPDATED_TIME = ? " +
                    "WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Get signature by API UUID and organization.
     */
    public static final String GET_SIGNATURE =
            "SELECT API_UUID, SIGNATURE_BLOB, ORGANIZATION, CREATED_TIME, UPDATED_TIME " +
                    "FROM AM_API_MINHASH WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Get all signatures for an organization.
     */
    public static final String GET_ALL_SIGNATURES_BY_ORG =
            "SELECT API_UUID, SIGNATURE_BLOB, ORGANIZATION, CREATED_TIME, UPDATED_TIME " +
                    "FROM AM_API_MINHASH WHERE ORGANIZATION = ?";

    /**
     * Get all signatures across all organizations.
     */
    public static final String GET_ALL_SIGNATURES =
            "SELECT API_UUID, SIGNATURE_BLOB, ORGANIZATION, CREATED_TIME, UPDATED_TIME " +
                    "FROM AM_API_MINHASH";

    /**
     * Delete a signature.
     */
    public static final String DELETE_SIGNATURE =
            "DELETE FROM AM_API_MINHASH WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Check if signature exists.
     */
    public static final String CHECK_SIGNATURE_EXISTS =
            "SELECT 1 FROM AM_API_MINHASH WHERE API_UUID = ? AND ORGANIZATION = ?";

    /**
     * Delete all signatures for an organization.
     */
    public static final String DELETE_ALL_SIGNATURES_BY_ORG =
            "DELETE FROM AM_API_MINHASH WHERE ORGANIZATION = ?";
}
