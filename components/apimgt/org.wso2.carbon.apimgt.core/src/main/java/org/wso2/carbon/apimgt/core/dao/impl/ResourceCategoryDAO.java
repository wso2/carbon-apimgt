/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.dao.impl;

import javax.annotation.CheckForNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Provides access to Resource Categories which maybe shared across multiple entities
 */
class ResourceCategoryDAO {

    @CheckForNull
    static String getResourceCategoryID(Connection connection, String resourceCategory) throws SQLException {
        final String query = "SELECT RESOURCE_CATEGORY_ID FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY = ?";
        String resourceTypeID = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    resourceTypeID = rs.getString("RESOURCE_CATEGORY_ID");
                }
            }
        }

        return resourceTypeID;
    }

    static void addResourceCategory(Connection connection, String resourceCategory, String dataType)
                                                                                                throws SQLException {
        String resourceCategoryID = UUID.randomUUID().toString();

        String dataTypeID = getDataTypeID(connection, dataType);

        if (dataTypeID == null) {
            dataTypeID = UUID.randomUUID().toString();
            addDataType(connection, dataTypeID, dataType);
        }

        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY_ID, DATA_TYPE_ID, " +
                "RESOURCE_CATEGORY) VALUES (?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategoryID);
            statement.setString(2, dataTypeID);
            statement.setString(3, resourceCategory);
            statement.execute();
        }
    }

    @CheckForNull
    private static String getDataTypeID(Connection connection, String dataType) throws SQLException {
        final String query = "SELECT DATA_TYPE_ID FROM AM_RESOURCE_DATA_TYPES WHERE DATA_TYPE_NAME = ?";
        String dataTypeID = null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, dataType);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    dataTypeID = rs.getString("DATA_TYPE_ID");
                }
            }
        }

        return dataTypeID;
    }

    private static void addDataType(Connection connection, String dataTypeID, String dataType) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_DATA_TYPES (DATA_TYPE_ID, DATA_TYPE_NAME) VALUES (?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, dataTypeID);
            statement.setString(2, dataType);

            statement.execute();
        }
    }
}
