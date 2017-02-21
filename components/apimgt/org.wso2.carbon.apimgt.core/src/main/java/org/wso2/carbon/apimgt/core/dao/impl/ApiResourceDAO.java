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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class ApiResourceDAO {

    static boolean isResourceExistsForCategory(Connection connection, String apiID,
                                               ResourceCategory category) throws SQLException {
        final String query = "SELECT 1 FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    static void addResourceWithoutValue(Connection connection, String apiID, String resourceID,
                                        ResourceCategory category) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_CATEGORY_ID) VALUES (?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static void addTextResource(Connection connection, String apiID, String resourceID,
                                ResourceCategory category, String dataType, String textValue) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_CATEGORY_ID, " +
                "DATA_TYPE, RESOURCE_TEXT_VALUE) VALUES (?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(4, dataType);
            statement.setString(5, textValue);

            statement.execute();
        }
    }

    static void addBinaryResource(Connection connection, String apiID, String resourceID,
                                ResourceCategory category, String dataType, InputStream binaryValue)
                                                                                                throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_CATEGORY_ID, " +
                "DATA_TYPE, RESOURCE_BINARY_VALUE) VALUES (?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(4, dataType);
            statement.setBinaryStream(5, binaryValue);

            statement.execute();
        }
    }


    static String getTextValueForCategory(Connection connection, String apiID,
                                          ResourceCategory resourceCategory) throws SQLException {
        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return null;
    }

    static void updateTextValueForCategory(Connection connection, String apiID,
                                           ResourceCategory category,
                                           String resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static InputStream getBinaryValueForCategory(Connection connection, String apiID,
                                                 ResourceCategory category) throws SQLException, IOException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    InputStream inputStream = new ByteArrayInputStream(IOUtils.toByteArray(rs.getBinaryStream
                            ("RESOURCE_BINARY_VALUE")));
                    return inputStream;
                }
            }
        }

        return null;
    }

    static InputStream getBinaryResource(Connection connection, String resourceID) throws SQLException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("RESOURCE_BINARY_VALUE");
                    if (blob != null) {
                        return blob.getBinaryStream();
                    }
                }
            }
        }

        return null;
    }

    static String getTextResource(Connection connection, String resourceID) throws SQLException {
        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return null;
    }

    static void updateBinaryResourceForCategory(Connection connection, String apiID, ResourceCategory category,
                                                InputStream resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBinaryStream(1, resourceValue);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static int updateBinaryResource(Connection connection, String resourceID, InputStream resourceValue, String
            fileName) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ?, DATA_TYPE = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBlob(1, resourceValue);
            statement.setString(2, fileName);
            statement.setString(3, resourceID);
            return statement.executeUpdate();
        }
    }

    static int updateTextResource(Connection connection, String resourceID, String resourceValue)
            throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, resourceID);

            return statement.executeUpdate();
        }
    }

    static void deleteUniqueResourceForCategory(Connection connection, String apiID, ResourceCategory resourceCategory)
            throws SQLException {
        final String query = "DELETE FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));

            statement.execute();
        }
    }

    static void deleteResource(Connection connection, String resourceID)
            throws SQLException {
        final String query = "DELETE FROM AM_API_RESOURCES WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);

            statement.execute();
        }
    }
}
