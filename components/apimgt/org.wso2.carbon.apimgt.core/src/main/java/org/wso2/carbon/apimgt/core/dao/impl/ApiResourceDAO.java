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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

class ApiResourceDAO {

    static String getTextResource(Connection connection, String apiID, String resourceCategoryID) throws SQLException {
        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, resourceCategoryID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return null;
    }

    static void addTextResource(Connection connection, String apiID, String resourceName, String resourceCategoryID,
                            String visibility, String resourceValue) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (RESOURCE_ID, API_ID, RESOURCE_NAME, " +
                "RESOURCE_CATEGORY_ID, VISIBILITY, RESOURCE_TEXT_VALUE) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, apiID);
            statement.setString(3, ResourceConstants.Resource.WSDL_URI.toString());
            statement.setString(4, resourceCategoryID);
            statement.setString(5, visibility);
            statement.setString(6, resourceValue);

            statement.execute();
        }
    }

    static void updateTextResource(Connection connection, String apiID, String resourceCategoryID,
                                                                    String resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, apiID);
            statement.setString(3, resourceCategoryID);

            statement.execute();
        }
    }

    static InputStream getBinaryResource(Connection connection, String apiID, String resourceCategoryID) throws SQLException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, resourceCategoryID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getBlob("RESOURCE_BINARY_VALUE").getBinaryStream();
                }
            }
        }

        return null;
    }

    static void addBinaryResource(Connection connection, String apiID, String resourceName, String resourceCategoryID,
                                                String visibility, InputStream resourceValue) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (RESOURCE_ID, API_ID, RESOURCE_NAME, " +
                "RESOURCE_CATEGORY_ID, VISIBILITY, RESOURCE_BINARY_VALUE) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, apiID);
            statement.setString(3, ResourceConstants.Resource.WSDL_URI.toString());
            statement.setString(4, resourceCategoryID);
            statement.setString(5, visibility);
            statement.setBlob(6, resourceValue);

            statement.execute();
        }
    }

    static void updateBinarytResource(Connection connection, String apiID, String resourceCategoryID,
                                                            InputStream resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBlob(1, resourceValue);
            statement.setString(2, apiID);
            statement.setString(3, resourceCategoryID);

            statement.execute();
        }
    }

    static boolean isResourceExists(Connection connection, String apiID, String resourceCategoryID) throws SQLException {
        final String query = "SELECT 1 FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, resourceCategoryID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    static void deleteResource(Connection connection, String apiID, String resourceCategoryID)
            throws SQLException {
        final String query = "DELETE FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, resourceCategoryID);

            statement.execute();
        }
    }
}
