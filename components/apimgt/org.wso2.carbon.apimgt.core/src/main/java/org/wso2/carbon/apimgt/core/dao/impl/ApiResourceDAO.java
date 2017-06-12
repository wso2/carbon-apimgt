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
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
            ResourceCategory category, String dataType, String textValue, String createdBy)
            throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_CATEGORY_ID, " +
                "DATA_TYPE, RESOURCE_TEXT_VALUE, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME) " 
                + "VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(4, dataType);
            statement.setString(5, textValue);
            statement.setString(6, createdBy);
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(8, createdBy);
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            statement.execute();
        }
    }

    static void addBinaryResource(Connection connection, String apiID, String resourceID, ResourceCategory category,
            String dataType, InputStream binaryValue, String createdBy) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_CATEGORY_ID, " +
                "DATA_TYPE, RESOURCE_BINARY_VALUE, CREATED_BY, CREATED_TIME, UPDATED_BY, LAST_UPDATED_TIME) " 
                + "VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(4, dataType);
            statement.setBinaryStream(5, binaryValue);
            statement.setString(6, createdBy);
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(8, createdBy);
            statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
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
                                           String resourceValue, String updatedBy) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ?, UPDATED_BY = ?, " 
                + "LAST_UPDATED_TIME = ? WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, updatedBy);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, apiID);
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static InputStream getBinaryValueForCategory(Connection connection, String apiID,
                                                 ResourceCategory category, ApiType apiType)
                                                                                throws SQLException, IOException {
        final String query = "SELECT res.RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES res " +
                "INNER JOIN AM_API api ON res.API_ID = api.UUID " +
                "WHERE res.API_ID = ? AND res.RESOURCE_CATEGORY_ID = ? AND " +
                "api.API_TYPE_ID = (SELECT TYPE_ID FROM AM_API_TYPES WHERE TYPE_NAME = ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(3, apiType.toString());
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

    static InputStream getBinaryResource(Connection connection, String resourceID) throws SQLException, IOException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    InputStream inputStream = rs.getBinaryStream("RESOURCE_BINARY_VALUE");
                    if (inputStream != null) {
                        return new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
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
                                                InputStream resourceValue, String updatedBy)
                                                                                                throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ?, UPDATED_BY = ?, "
                + "LAST_UPDATED_TIME = ? WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBinaryStream(1, resourceValue);
            statement.setString(2, updatedBy);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, apiID);
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static String getResourceLastUpdatedTime(Connection connection, String apiId, String resourceID,
            ResourceCategory category) throws SQLException {
        final String query = "SELECT LAST_UPDATED_TIME FROM AM_API_RESOURCES WHERE API_ID = ? AND UUID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.setString(2, resourceID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("LAST_UPDATED_TIME");
                }
            }
        }
        return null;
    }

    static String getAPIUniqueResourceLastUpdatedTime(Connection connection, String apiID, ResourceCategory category)
            throws SQLException {
        final String query = "SELECT LAST_UPDATED_TIME FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("LAST_UPDATED_TIME");
                }
            }
        }
        return null;
    }

    static int updateBinaryResource(Connection connection, String resourceID, InputStream resourceValue, String
            dataType, String updatedBy) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ?, DATA_TYPE = ?, UPDATED_BY = ?, " 
                + "LAST_UPDATED_TIME = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBinaryStream(1, resourceValue);
            statement.setString(2, dataType);
            statement.setString(3, updatedBy);
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(5, resourceID);
            return statement.executeUpdate();
        }
    }

    static int updateTextResource(Connection connection, String resourceID, String resourceValue, String updatedBy)
            throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ?, UPDATED_BY = ?, " 
                + "LAST_UPDATED_TIME = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, updatedBy);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, resourceID);

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
