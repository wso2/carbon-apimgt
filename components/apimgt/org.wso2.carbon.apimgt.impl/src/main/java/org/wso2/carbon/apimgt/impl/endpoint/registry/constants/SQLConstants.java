/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.endpoint.registry.constants;

public class SQLConstants {

    public static final String ADD_ENDPOINT_REGISTRY_SQL =
            "INSERT INTO ENDPOINT_REG (UUID, REG_NAME, DISPLAY_NAME, REG_TYPE, TENANT_ID, " +
                    "CREATED_BY, UPDATED_BY, CREATED_TIME, UPDATED_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_ENDPOINT_REGISTRY_SQL =
            "UPDATE ENDPOINT_REG " +
                    "SET DISPLAY_NAME = ?, " +
                    "REG_TYPE = ?, " +
                    "UPDATED_BY = ?, " +
                    "UPDATED_TIME = ? " +
                    "WHERE UUID = ?";

    public static final String GET_ENDPOINT_REGISTRY_OF_TENANT_WITH_TYPE =
            " SELECT " +
                    "   UUID, " +
                    "   REG_NAME, " +
                    "   DISPLAY_NAME, " +
                    "   REG_TYPE, " +
                    "   TENANT_ID, " +
                    "   CREATED_BY, " +
                    "   UPDATED_BY, " +
                    "   CREATED_TIME, " +
                    "   UPDATED_TIME " +
                    " FROM " +
                    "   ENDPOINT_REG " +
                    " WHERE " +
                    "   REG_TYPE = ? AND " +
                    "   TENANT_ID = ?";

    public static final String GET_ENDPOINT_REGISTRY_BY_UUID = " SELECT UUID, REG_NAME, DISPLAY_NAME, REG_TYPE, " +
            "TENANT_ID, ID, " +
            "CREATED_BY, UPDATED_BY, CREATED_TIME, UPDATED_TIME FROM " +
            "ENDPOINT_REG WHERE UUID = ? AND TENANT_ID = ?";

    public static final String DELETE_ENDPOINT_REGISTRY_SQL = "DELETE FROM ENDPOINT_REG WHERE UUID = ?";

    public static final String IS_ENDPOINT_REGISTRY_NAME_EXISTS = "SELECT COUNT(UUID) AS ENDPOINT_REGISTRY_COUNT" +
            " FROM ENDPOINT_REG WHERE LOWER(REG_NAME) = LOWER(?) AND TENANT_ID = ?";

    public static final String IS_ENDPOINT_REGISTRY_DISPLAY_NAME_EXISTS =
            "SELECT COUNT(UUID) AS ENDPOINT_REGISTRY_COUNT" +
                    " FROM ENDPOINT_REG WHERE LOWER(DISPLAY_NAME) = LOWER(?) AND TENANT_ID = ?";

    public static final String IS_ENDPOINT_REGISTRY_TYPE_EXISTS = "SELECT COUNT(UUID) AS ENDPOINT_REGISTRY_COUNT" +
            " FROM ENDPOINT_REG WHERE REG_TYPE = ? AND TENANT_ID = ?";

    public static final String GET_ENDPOINT_REGISTRY_ENTRY_BY_UUID =
            " SELECT UUID, ENTRY_NAME, DISPLAY_NAME, ENTRY_VERSION, DESCRIPTION, DEFINITION_TYPE, DEFINITION_URL, " +
                    "SERVICE_TYPE, SERVICE_CATEGORY, " +
                    "PRODUCTION_SERVICE_URL, SANDBOX_SERVICE_URL, ENDPOINT_DEFINITION, " +
                    "CREATED_BY, UPDATED_BY, CREATED_TIME, UPDATED_TIME, REG_ID FROM ENDPOINT_REG_ENTRY WHERE UUID = ?";

    public static final String ADD_ENDPOINT_REGISTRY_ENTRY_SQL =
            "INSERT INTO ENDPOINT_REG_ENTRY (UUID, ENTRY_NAME, DISPLAY_NAME, ENTRY_VERSION, PRODUCTION_SERVICE_URL, " +
                    "SANDBOX_SERVICE_URL, DEFINITION_TYPE, DEFINITION_URL, DESCRIPTION, SERVICE_TYPE, SERVICE_CATEGORY,"
                    + " ENDPOINT_DEFINITION, REG_ID, CREATED_BY, UPDATED_BY, CREATED_TIME, UPDATED_TIME) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_ENDPOINT_REGISTRY_ENTRY_SQL =
            "UPDATE ENDPOINT_REG_ENTRY SET " +
                    "DISPLAY_NAME = ?, " +
                    "ENTRY_VERSION = ?, " +
                    "PRODUCTION_SERVICE_URL = ?, " +
                    "SANDBOX_SERVICE_URL = ?, " +
                    "DEFINITION_TYPE = ?, " +
                    "DEFINITION_URL = ?, " +
                    "DESCRIPTION = ?, " +
                    "SERVICE_TYPE = ?, " +
                    "SERVICE_CATEGORY = ?, " +
                    "ENDPOINT_DEFINITION = ?, " +
                    "UPDATED_BY = ?, " +
                    "UPDATED_TIME = ? " +
                    "WHERE UUID = ?";

    public static final String DELETE_ENDPOINT_REGISTRY_ENTRY_SQL = "DELETE FROM ENDPOINT_REG_ENTRY WHERE UUID = ?";

    public static final String IS_ENDPOINT_REGISTRY_ENTRY_NAME_AND_VERSION_EXISTS = "SELECT COUNT(UUID) AS" +
            " REGISTRY_ENTRY_COUNT" +
            " FROM ENDPOINT_REG_ENTRY WHERE LOWER(ENTRY_NAME) = LOWER(?) AND LOWER(ENTRY_VERSION) = LOWER(?)" +
            " AND REG_ID = ?";

    public static final String IS_ENDPOINT_REGISTRY_ENTRY_NAME_EXISTS = "SELECT COUNT(UUID) AS REGISTRY_ENTRY_COUNT" +
            " FROM ENDPOINT_REG_ENTRY WHERE LOWER(ENTRY_NAME) = LOWER(?) AND REG_ID = ?";

    public static final String IS_ENDPOINT_REGISTRY_ENTRY_DISPLAY_NAME_EXISTS =
            "SELECT COUNT(UUID) AS REGISTRY_ENTRY_COUNT" +
                    " FROM ENDPOINT_REG_ENTRY WHERE LOWER(DISPLAY_NAME) = LOWER(?) AND REG_ID = ?";

}
